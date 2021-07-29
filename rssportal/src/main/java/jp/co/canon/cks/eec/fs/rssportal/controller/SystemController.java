package jp.co.canon.cks.eec.fs.rssportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jp.co.canon.ckbs.eec.fs.configuration.Category;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.Machine;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.category.Categories;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.category.CategoryInfo;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.category.CategoryLoader;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure.MpaInfo;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure.OtsInfo;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure.Structure;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure.StructureLoader;
import jp.co.canon.cks.eec.fs.rssportal.Defines.RSSErrorReason;
import jp.co.canon.cks.eec.fs.rssportal.background.machine.MachineStatusInspector;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import jp.co.canon.cks.eec.fs.rssportal.model.auth.AccessToken;
import jp.co.canon.cks.eec.fs.rssportal.model.error.RSSError;
import jp.co.canon.cks.eec.fs.rssportal.service.DownloadHistoryService;
import jp.co.canon.cks.eec.fs.rssportal.service.JwtService;
import jp.co.canon.cks.eec.fs.rssportal.vo.ConfigHistoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @Value("${rssportal.configuration.path}")
    private String configurationPath;

    @Value("${rssportal.configuration.structureFile}")
    private String systemMachineStructure;

    @Value("${rssportal.configuration.categoriesFile}")
    private String systemCategoryStructure;

    private final DownloadHistoryService configHistoryService;
    private final JwtService jwtService;
    private final MachineStatusInspector machineStatusInspector;

    private final EspLog log = new EspLog(getClass());
    private StructureLoader machineLoader;
    private CategoryLoader categoryLoader;
    ObjectMapper obj = null;

    @Autowired
    public SystemController(DownloadHistoryService configHistoryService, JwtService jwtService,
                            MachineStatusInspector machineStatusInspector) {

        this.configHistoryService = configHistoryService;
        this.jwtService = jwtService;
        this.machineStatusInspector = machineStatusInspector;
    }

    @PostConstruct
    void initialize() {
        this.machineLoader = new StructureLoader(configurationPath + systemMachineStructure);
        this.categoryLoader = new CategoryLoader(configurationPath + systemCategoryStructure);
    }

    @GetMapping("/machinesInfo")
    @ResponseBody
    public ResponseEntity<?> getMachines() throws Exception {
        log.info("[Get] /rss/api/system/machinesInfo");

        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        MpaInfo[] mpas = null;
        OtsInfo[] otss = null;
        try {
            mpas = machineLoader.getMpaList();
            otss = machineLoader.getOtsList();
        } catch (IOException e){
            log.error("[machines] IOException "+ e);
        }
        if (mpas == null || otss == null) {
            log.error("[machines]request totally failed");
            error.setReason(RSSErrorReason.INTERNAL_ERROR);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resBody);
        }

        ArrayList<Machine> machineArrayList = new ArrayList<>();
        for(OtsInfo ots : otss){
            Machine ots_machine = new Machine();
            ots_machine.setMachineName(ots.getName());
            ots_machine.setHost(ots.getHost());
            ots_machine.setPort(ots.getPort());
            ots_machine.setFtpConnected(machineStatusInspector.getOtsStatus(ots.getName()));
            ots_machine.setVFtpConnected(ots_machine.isFtpConnected());
            machineArrayList.add(ots_machine);
        }
        for(MpaInfo mpa : mpas){
            Machine machine = new Machine();
            machine.setMachineName(mpa.getName());
            machine.setOts(mpa.getOts());
            machine.setHost(mpa.getHost());
            machine.setLine(mpa.getLine());
            machine.setFtpUser(mpa.getFtpUser());
            machine.setFtpPassword(mpa.getFtpPassword());
            machine.setVftpUser(mpa.getVftpUser());
            machine.setVftpPassword(mpa.getVftpPassword());
            machine.setSerialNumber(mpa.getSerialNumber());
            machine.setToolType(mpa.getToolType());
            machine.setFtpConnected(machineStatusInspector.getMachineFtpStatus(mpa.getName()));
            machine.setVFtpConnected(machineStatusInspector.getMachineVFtpStatus(mpa.getName()));
            machineArrayList.add(machine);
        }

        resBody.put("lists", machineArrayList);
        resBody.put("version", machineLoader.getVersion());
        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }

    @GetMapping(value = {"/categoryInfo"})
    @ResponseBody
    public ResponseEntity<?> getCategories()  throws Exception {
        log.info("[Get] /rss/api/system/categoryInfo");
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        ArrayList<Category> categoryArrayList = new ArrayList<>();
        CategoryInfo[] categoryInfos = categoryLoader.getCategories();

        for(CategoryInfo info: categoryInfos){
            Category category = new Category();
            category.setCategoryCode(String.format("%03X", info.getNo()));
            category.setCategoryName(info.getName());
            category.setAuto(info.isAutocollect());
            category.setPort(info.getPort());
            category.setDisplay(info.isDisplay());
            category.setFilePath(info.getPath());
            category.setFileName(info.getFileName());
            category.setDescription(info.getDescription());
            category.setDest(info.getDest());
            categoryArrayList.add(category);
        }

        if (categoryInfos == null) {
            log.error("[categories]request totally failed");
            error.setReason(RSSErrorReason.INTERNAL_ERROR);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resBody);
        }

        resBody.put("lists", categoryArrayList);
        resBody.put("version", categoryLoader.getVersion());
        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }

    @PostMapping(value = {"/categoryInfo"})
    @ResponseBody
    public ResponseEntity<?> addCategories(HttpServletRequest request,@RequestBody Map<String, Object> category) throws Exception {
        log.info("[Post] /rss/api/system/categoryInfo");
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        Categories categories = new Categories();
        ArrayList<CategoryInfo> newList = new ArrayList<>();
        CategoryInfo[] categoryInfos = categoryLoader.getCategories();
        String type = null;

        CategoryInfo newCategory = createCategoryInfo(category);
        if(newCategory == null)
        {
            log.error("[categories]request totally failed");
            error.setReason(RSSErrorReason.INTERNAL_ERROR);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resBody);
        }

        newList.addAll(Arrays.asList(categoryInfos));
        for(CategoryInfo info: categoryInfos){
            if(info.getNo() == newCategory.getNo()) {
                error.setReason("duplicated LogCode");
                resBody.put("error", error.getRSSError());
                return ResponseEntity.status(HttpStatus.OK).body(resBody);
            }
        }
        newList.add(newCategory);
        //sort
        Collections.sort(newList, new Comparator<CategoryInfo>() {
            @Override
            public int compare(CategoryInfo s1, CategoryInfo s2) {
               return (s1.getNo() > s2.getNo()) ? 1 : (s1.getNo() == s2.getNo())  ?  0: -1; }
        });

        type ="[Category]"+newCategory.getName() +" ADD";
        categories.setVersion("-");
        categories.setCategories((CategoryInfo[])newList.toArray(new CategoryInfo[0]));

        String accessToken = request.getHeader("Authorization");
        AccessToken decodedAccess = jwtService.decodeAccessToken(accessToken);

        updateConfigFile(systemCategoryStructure, categories, decodedAccess.getUserName(), type);
        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }

    @PatchMapping(value = {"/categoryInfo"})
    @ResponseBody
    public ResponseEntity<?> updateCategories(HttpServletRequest request, @RequestBody Map<String, Object> category) throws Exception {
        log.info("[Patch] /rss/api/system/categoryInfo");
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();
        CategoryInfo newCategory = createCategoryInfo(category);
        if(newCategory == null)
        {
            log.error("[categories]request totally failed");
            error.setReason(RSSErrorReason.INTERNAL_ERROR);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resBody);
        }

        CategoryInfo[] categoryInfos = categoryLoader.getCategories();
        Categories categories = new Categories();
        ArrayList<CategoryInfo> newList = new ArrayList<>();
        String type = null;
        boolean isEdit = false;
        newList.addAll(Arrays.asList(categoryInfos));
        for(CategoryInfo info: categoryInfos){
            if(info.getNo()==newCategory.getNo()) {
                newList.set(newList.indexOf(info),newCategory);
                type ="[Category]"+info.getName() +" UPDATE";
                isEdit = true;
                break;
            }
        }
        if(!isEdit)
        {
            newList.add(newCategory);
            type ="[Category]"+newCategory.getName() +" ADD";
            //sort
            Collections.sort(newList, new Comparator<CategoryInfo>() {
                @Override
                public int compare(CategoryInfo s1, CategoryInfo s2) {
                    return (s1.getNo() > s2.getNo()) ? 1 : (s1.getNo() == s2.getNo())  ?  0: -1; }
            });
        }
        categories.setVersion("-");
        categories.setCategories(newList.toArray(new CategoryInfo[0]));

        String accessToken = request.getHeader("Authorization");
        AccessToken decodedAccess = jwtService.decodeAccessToken(accessToken);

        updateConfigFile(systemCategoryStructure, categories, decodedAccess.getUserName(), type);

       return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }



    @DeleteMapping("/categoryInfo/{LogCode}")
    @ResponseBody
    public ResponseEntity<?> deleteCategories(HttpServletRequest request,@PathVariable("LogCode") String LogCode) throws Exception {
        log.info("[Delete] /rss/api/system/categoryInfo");
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();
        if(LogCode == null)
        {
            log.error("[categories]LogCode is null");
            error.setReason(RSSErrorReason.INTERNAL_ERROR);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resBody);
        }
        log.info("[categories]LogCode"+LogCode);
        int LogNo = Integer.parseInt((String) LogCode, 16);

        Categories categories = new Categories();
        ArrayList<CategoryInfo> newList = new ArrayList<>();
        String type = null;

        newList.addAll(Arrays.asList(categoryLoader.getCategories()));
        for(CategoryInfo info: newList){
            if(info.getNo() == LogNo) {
                categories.setVersion("-");
                newList.remove(info);
                type ="[Category]"+info.getName() +" DELETE";
                break;
            }
        }
        categories.setCategories(newList.toArray(new CategoryInfo[0]));

        String accessToken = request.getHeader("Authorization");
        AccessToken decodedAccess = jwtService.decodeAccessToken(accessToken);

        updateConfigFile(systemCategoryStructure, categories, decodedAccess.getUserName(), type);
        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }

    @PostMapping(value = {"/import/categoryInfo"})
    @ResponseBody
    public ResponseEntity<?> importCategory(HttpServletRequest request,@RequestBody Map<String, Object> category) throws Exception {
        log.info("[Post] /rss/api/system/import/categoryInfo");
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        Categories categories = new Categories();
        ArrayList<CategoryInfo> newList = new ArrayList<>();
        String version = (String) category.get("version");
        ArrayList list = (ArrayList) category.get("list");

        categories.setVersion(version);
        for(Object obj: list){
            CategoryInfo nCategory = createCategoryInfo((Map<String, Object>) obj);
            if(nCategory != null) newList.add(nCategory);
        }
        categories.setCategories(newList.toArray(new CategoryInfo[0]));
        String accessToken = request.getHeader("Authorization");
        AccessToken decodedAccess = jwtService.decodeAccessToken(accessToken);
        String type ="[Category] newList import ";
        updateConfigFile(systemCategoryStructure, categories, decodedAccess.getUserName(), type);

        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }

    @PostMapping(value = {"/import/machinesInfo"})
    @ResponseBody
    public ResponseEntity<?> importMachine(HttpServletRequest request,@RequestBody Map<String, Object> object) throws Exception {
        log.info("[Post] /rss/api/system/import/machinesInfo");
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        Structure structure = new Structure();
        ArrayList<OtsInfo> otsList = new ArrayList<>();
        ArrayList<MpaInfo> mpaList = new ArrayList<>();
        String type = null;
        String version = (String) object.get("version");
        ArrayList list = (ArrayList) object.get("list");

        log.info("version"+version);
        log.info("list"+list);

        for(Object obj: list){
            Map<String, Object> res = (Map<String, Object>) obj;
            String mType =res.containsKey("type") ? (String) res.get("type") : null ;
            log.info("mType"+mType);
            if(mType!= null && mType.equals("OTS")) {
                OtsInfo ots = (OtsInfo)createMachineInfo((Map<String, Object>) obj, "OTS");
                log.info("OTS"+ots.getName());
                otsList.add(ots);
            }
            else if(mType!= null && mType.equals("MPA")){
                MpaInfo mpa = (MpaInfo)createMachineInfo((Map<String, Object>) obj,"MPA");
                log.info("MPA"+mpa.getName());
                mpaList.add(mpa);
            }
        }
        structure.setVersion(version);
        structure.setOtsList(otsList.toArray(new OtsInfo[0]));
        structure.setMpaList(mpaList.toArray(new MpaInfo[0]));

        String accessToken = request.getHeader("Authorization");
        AccessToken decodedAccess = jwtService.decodeAccessToken(accessToken);
        type ="[Machine] newList import ";
        updateConfigFile(systemMachineStructure, structure, decodedAccess.getUserName(), type);

        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }

    @PostMapping(value = {"/machinesInfo"})
    @ResponseBody
    public ResponseEntity<?> addMachine(HttpServletRequest request, @RequestBody Map<String, Object> machine) throws Exception {
        log.info("[Post] /rss/api/system/machinesInfo");
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();
        if(machine == null)
        {
            log.error("[categories]request totally failed");
            error.setReason(RSSErrorReason.INTERNAL_ERROR);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resBody);
        }

        Structure structure = new Structure();
        ArrayList<OtsInfo> otsList = new ArrayList<>();
        ArrayList<MpaInfo> mpaList = new ArrayList<>();
        String type = null;
        otsList.addAll(Arrays.asList(machineLoader.getOtsList()));
        mpaList.addAll(Arrays.asList(machineLoader.getMpaList()));

        if(machine.containsKey("ots") ){
            MpaInfo mpa = (MpaInfo)createMachineInfo(machine,"MPA");
            mpaList.add(mpa);
            type ="[Machine]"+mpa.getName() +" ADD";
        }
        else {
            OtsInfo ots = (OtsInfo)createMachineInfo(machine, "OTS");
            otsList.add(ots);
            type ="[Machine]"+ots.getName() +" ADD";
        }

        structure.setVersion("-");
        structure.setOtsList(otsList.toArray(new OtsInfo[0]));
        structure.setMpaList(mpaList.toArray(new MpaInfo[0]));

        String accessToken = request.getHeader("Authorization");
        AccessToken decodedAccess = jwtService.decodeAccessToken(accessToken);

        updateConfigFile(systemMachineStructure, structure, decodedAccess.getUserName(), type);
        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }

    @PatchMapping(value = {"/machinesInfo"})
    @ResponseBody
    public ResponseEntity<?> updateMachine(HttpServletRequest request,@RequestBody Map<String, Object> machine) throws Exception {
        log.info("[Patch] /rss/api/system/machinesInfo");
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();
        if(machine == null)
        {
            log.error("[categories]request totally failed");
            error.setReason(RSSErrorReason.INTERNAL_ERROR);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resBody);
        }

        Structure structure = new Structure();
        ArrayList<OtsInfo> otsList = new ArrayList<>();
        ArrayList<MpaInfo> mpaList = new ArrayList<>();
        String type = null;

        otsList.addAll(Arrays.asList(machineLoader.getOtsList()));
        mpaList.addAll(Arrays.asList(machineLoader.getMpaList()));
        String machineName = machine.containsKey("targetname") ? machine.get("targetname").toString() :"";
        boolean isEdit = false;

        if(machine.containsKey("ots") ) {
            MpaInfo newMpa = (MpaInfo) createMachineInfo(machine, "MPA");
            for(MpaInfo mpa: mpaList){
                if(machineName.equals(mpa.getName())) {
                    mpaList.set(mpaList.indexOf(mpa),newMpa);
                    isEdit = true;
                    type ="[Machine]"+mpa.getName() +"UPDATE";
                    break;
                }
            }
            if(!isEdit)
            {
                mpaList.add(newMpa);
                type ="[Machine]"+newMpa.getName() +"ADD";
            }
        }else {
            OtsInfo newOts = (OtsInfo) createMachineInfo(machine, "OTS");
            for(OtsInfo ots: otsList){
                if(machineName.equals(ots.getName())) {
                    otsList.set(otsList.indexOf(ots),newOts);
                    isEdit = true;
                    type ="[Machine]"+ots.getName() +"UPDATE";
                    break;
                }
            }
            if(!isEdit){
                otsList.add(newOts);
                type ="[Machine]"+newOts.getName() +"ADD";
            }
        }
        structure.setVersion("-");
        structure.setOtsList(otsList.toArray(new OtsInfo[0]));
        structure.setMpaList(mpaList.toArray(new MpaInfo[0]));

        String accessToken = request.getHeader("Authorization");
        AccessToken decodedAccess = jwtService.decodeAccessToken(accessToken);

        updateConfigFile(systemMachineStructure, structure, decodedAccess.getUserName(), type);

        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }


    @DeleteMapping("/machinesInfo/{machineName}")
    @ResponseBody
    public ResponseEntity<?> deleteMachine(HttpServletRequest request,@PathVariable("machineName") String machineName) throws Exception {
        log.info("[Delete] /rss/api/system/machinesInfo");
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();
        String type = null;
        if(machineName == null)
        {
            log.error("[machinesInfo]machineName is null");
            error.setReason(RSSErrorReason.INTERNAL_ERROR);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resBody);
        }
        log.info("[machinesInfo]machineName"+machineName);
        Structure structure = new Structure();
        ArrayList<OtsInfo> otsList = new ArrayList<>();
        ArrayList<MpaInfo> mpaList = new ArrayList<>();
        otsList.addAll(Arrays.asList(machineLoader.getOtsList()));
        mpaList.addAll(Arrays.asList(machineLoader.getMpaList()));
        for(MpaInfo mpa: mpaList){
            if(machineName.equals(mpa.getName())) {
                structure.setVersion("-");
                mpaList.remove(mpa);
                type="[Machine]"+ mpa.getName() +" DELETE";
                break;
            }
        }
        for(OtsInfo ots: otsList){
            if(machineName.equals(ots.getName())) {
                structure.setVersion("-");
                otsList.remove(ots);
                type="[Machine]"+ ots.getName() +" DELETE";
                break;
            }
        }
        structure.setOtsList(otsList.toArray(new OtsInfo[0]));
        structure.setMpaList(mpaList.toArray(new MpaInfo[0]));
        String accessToken = request.getHeader("Authorization");
        AccessToken decodedAccess = jwtService.decodeAccessToken(accessToken);

        updateConfigFile(systemMachineStructure, structure, decodedAccess.getUserName(), type);

        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }

    private Object createMachineInfo (Map<String, Object> machine, String type){

        if(type == null || machine == null)
        {
            return null;
        }
        else if(type =="OTS")
        {
            OtsInfo ots = new OtsInfo();
            int port = machine.containsKey("port")&& isNumeric(machine.get("port").toString())? Integer.parseInt(machine.get("port").toString()): 0;
            ots.setName(machine.containsKey("targetname") ? machine.get("targetname").toString() : "");
            ots.setHost(machine.containsKey("host") ? machine.get("host").toString() : "");
            ots.setPort( port);
            return (Object)ots;
        } else if(type =="MPA"){
            MpaInfo mpa = new MpaInfo();
            mpa.setName(machine.containsKey("targetname") ? machine.get("targetname").toString() : "");
            mpa.setHost(machine.containsKey("host") ? machine.get("host").toString() : "");
            mpa.setOts(machine.containsKey("ots") ? machine.get("ots").toString() : "");
            mpa.setLine(machine.containsKey("line") ? machine.get("line").toString() : "");
            mpa.setFtpUser(machine.containsKey("ftpUser") ? machine.get("ftpUser").toString() : "");
            mpa.setFtpPassword(machine.containsKey("ftpPassword") ? machine.get("ftpPassword").toString() : "");
            mpa.setVftpUser(machine.containsKey("vftpUser") ? machine.get("vftpUser").toString() : "");
            mpa.setVftpPassword(machine.containsKey("vftpPassword") ? machine.get("vftpPassword").toString() : "");
            mpa.setToolType(machine.containsKey("toolType") ? machine.get("toolType").toString() : "");
            mpa.setSerialNumber(machine.containsKey("serialNumber") ? machine.get("serialNumber").toString() : "");
            return (Object)mpa;
        }
        return null;
    }

    boolean isPatternStr(String str){
        if (str.equals("YYMMDD")){
            return true;
        }
        if (str.equals("YYYYMMDDhhmmss")){
            return true;
        }
        if (str.contains("*")){
            return true;
        }
        return false;
    }

    private CategoryInfo createCategoryInfo (Map<String, Object> category){
        CategoryInfo newCategory = new CategoryInfo();

        if(category == null)
        {
            return null;
        }
        else
        {
            int logNo = category.containsKey("No")  && (category.get("No").toString()).chars().allMatch(Character::isDigit)
                    ? Integer.parseInt(category.get("No").toString()) : 0;

            String logCode = category.containsKey("logCode") ? (String) category.get("logCode") : "000";
            if(logCode.equals("000") && logNo == 0)
            {
                return null;
            }
            String path = (String) category.get("filePath");
            String destination = (String) category.get("dest");
            newCategory.setNo(logNo != 0 ? logNo: Integer.parseInt(logCode, 16));
            newCategory.setName((String) category.get("logName"));
            newCategory.setDescription((String) category.get("description"));
            newCategory.setDest(destination);
            newCategory.setPath(path);
            newCategory.setFileName((String) category.get("fileName"));
            newCategory.setAutocollect(Boolean.parseBoolean(category.get("auto").toString()));
            newCategory.setDisplay(Boolean.parseBoolean(category.get("display").toString()));
            newCategory.setPort(destination.equals("Cons")? 21:22001);

            String[] vars = path.split("/");
            int patternIdx = -1;
            for(int idx = 0; idx < vars.length; ++idx){
                if (isPatternStr(vars[idx])){
                    patternIdx = idx;
                    break;
                }
            }
            String rootDir = String.join("/", vars);
            String patternDir = "";
            if (patternIdx != -1){
                rootDir = String.join("/", Arrays.copyOf(vars, patternIdx));
                patternDir = String.join("/", Arrays.copyOfRange(vars, patternIdx, vars.length));
            }
            newCategory.setRootDir(rootDir);
            newCategory.setPatternDir(patternDir);
        }
        return newCategory;
    }
    private void  updateConfigFile (String configfile , Object str, String user, String type) throws Exception {
        obj = new ObjectMapper();
        File folder = new File(configurationPath+"old/");
        if(!folder.exists()) folder.mkdirs();
        String dtStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String filename = configfile.substring(0,configfile.lastIndexOf(".")) +"_"+ dtStr +
                configfile.substring(configfile.lastIndexOf("."));
        obj.configure(SerializationFeature.INDENT_OUTPUT, true);
        obj.writeValue(new File(configurationPath+"old/"+filename), str);
        obj.writeValue(new File(configurationPath+configfile), str);

        ConfigHistoryVo log = new ConfigHistoryVo();
        log.setUser(user);
        log.setType(type);
        log.setFilename(filename);

        if(!configHistoryService.addConfigLog(log)) throw new Exception();
    }
    public static boolean isNumeric(String str) {
        return str.chars().allMatch(Character::isDigit);
    }

}