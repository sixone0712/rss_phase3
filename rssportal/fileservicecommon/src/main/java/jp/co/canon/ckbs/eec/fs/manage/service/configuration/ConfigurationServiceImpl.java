package jp.co.canon.ckbs.eec.fs.manage.service.configuration;

import jp.co.canon.ckbs.eec.fs.configuration.Category;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.category.CategoryInfo;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.category.CategoryLoader;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure.MpaInfo;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure.MpaInfoEx;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure.OtsInfo;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure.StructureLoader;

import java.io.IOException;
import java.util.ArrayList;

public class ConfigurationServiceImpl implements ConfigurationService{

    StructureLoader structureLoader;
    CategoryLoader categoryLoader;

    public ConfigurationServiceImpl(String structureFileName, String categoriesFileName){
        structureLoader = new StructureLoader(structureFileName);
        categoryLoader = new CategoryLoader(categoriesFileName);
    }

    @Override
    public Machine[] getMachineList() {
        MpaInfo[] mpas = null;
        try {
            mpas = structureLoader.getMpaList();
        } catch (IOException e){
            return new Machine[0];
        }
        if (mpas == null){
            return new Machine[0];
        }
        ArrayList<Machine> machineArrayList = new ArrayList<>();
        for(MpaInfo mpa : mpas){
            Machine machine = new Machine();
            machine.setMachineName(mpa.getName());
            machine.setOts(mpa.getOts());
            machine.setLine(mpa.getLine());
            machineArrayList.add(machine);
        }
        return machineArrayList.toArray(new Machine[0]);
    }

    @Override
    public Category[] getCategories(String machineName) {
        try {
            ArrayList<Category> categoryArrayList = new ArrayList<>();
            CategoryInfo[] categoryInfos = categoryLoader.getCategories();
            for(CategoryInfo info: categoryInfos){
                Category category = new Category();
                category.setCategoryCode(String.format("%03X", info.getNo()));
                category.setCategoryName(info.getName());
                categoryArrayList.add(category);
            }
            return categoryArrayList.toArray(new Category[0]);
        } catch (IOException e) {
            return new Category[0];
        }
    }

    @Override
    public String[] getAllFileServiceHost() {
        OtsInfo[] otsArr = null;
        try {
            otsArr = structureLoader.getOtsList();
        } catch (IOException e){
            return new String[0];
        }
        if (otsArr == null) {
            return new String[0];
        }
        ArrayList<String> otsList = new ArrayList<>();
        for (OtsInfo ots : otsArr){
            otsList.add(ots.getName());
        }
        return otsList.toArray(new String[0]);
    }

    @Override
    public String getFileServiceHost(String machineName) {
        MpaInfoEx mpaInfoEx = structureLoader.getMpaInfoEx(machineName);
        if (mpaInfoEx != null){
            OtsInfo ots = mpaInfoEx.getOts();
            return String.format("%s:%d", ots.getHost(), ots.getPort());
        }
        return null;
    }

    @Override
    public String getOtsServiceHost(String otsName) {
        OtsInfo[] otsArr = null;
        try {
            otsArr = structureLoader.getOtsList();
        } catch (IOException e){
            return null;
        }
        for(OtsInfo ots: otsArr) {
            if(ots.getName().equals(otsName)) {
                return String.format("%s:%d", ots.getHost(), ots.getPort());
            }
        }
        return null;
    }

    @Override
    public String getFileServiceDownloadUrlPath(String machineName, String filePath) {
        MpaInfoEx mpaInfoEx = structureLoader.getMpaInfoEx(machineName);
        if (mpaInfoEx != null){
            OtsInfo ots = mpaInfoEx.getOts();
            return String.format("ftp://%s:50121/CANON/LOG/downloads/%s<%s/%s/%s>", ots.getHost(), filePath, "eespuser", "EEC-user", "passive");
        }
        return null;
    }

    @Override
    public MpaInfoEx getMpaInfoEx(String machineName){
        return structureLoader.getMpaInfoEx(machineName);
    }

    @Override
    public MpaInfo getMpaInfo(String machineName){
        MpaInfoEx mpaInfoEx = structureLoader.getMpaInfoEx(machineName);
        if (mpaInfoEx != null){
            return mpaInfoEx.getMpa();
        }
        return null;
    }

    @Override
    public CategoryInfo getCategory(String category){
        return categoryLoader.getCategory(category);
    }
}
