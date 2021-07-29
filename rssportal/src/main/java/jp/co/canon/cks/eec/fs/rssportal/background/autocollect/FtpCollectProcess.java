package jp.co.canon.cks.eec.fs.rssportal.background.autocollect;

import jp.co.canon.ckbs.eec.fs.collect.service.FileInfo;
import jp.co.canon.ckbs.eec.fs.collect.service.LogFileList;
import jp.co.canon.cks.eec.fs.rssportal.background.DownloadRequestForm;
import jp.co.canon.cks.eec.fs.rssportal.background.FileDownloader;
import jp.co.canon.cks.eec.fs.rssportal.background.FtpDownloadRequestForm;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import jp.co.canon.cks.eec.fs.rssportal.common.Tool;
import jp.co.canon.cks.eec.fs.rssportal.dao.CollectionPlanDao;
import jp.co.canon.cks.eec.fs.rssportal.vo.CollectPlanVo;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FtpCollectProcess extends CollectProcess {

    private long lastPointMillis;

    private long[] lastTimestamp;
    private String[] machines;
    private String[] fabs;
    private String[] categoryCodes;
    private String[] categoryNames;
    private long endPoint;
    private long searchTime;

    private List<DownloadRequestForm> fileList;

    public FtpCollectProcess(PlanManager manager, CollectPlanVo plan, CollectionPlanDao dao, FileDownloader downloader, EspLog log) {
        super(manager, plan, dao, downloader, log);
        if(!plan.getPlanType().equalsIgnoreCase("ftp")) {
            printError("invalid planType "+plan.getPlanType());
        }
    }

    @Override
    protected void createDownloadFileList() throws CollectException, InterruptedException {
        __checkPlanType();

        machines = plan.getTool().split(",");
        fabs = plan.getFab().split(",");
        categoryCodes = plan.getLogType().split(",");
        categoryNames = plan.getLogTypeStr().split(",");

        if(machines.length==0 || machines.length!=fabs.length || categoryCodes.length==0 ||
                categoryCodes.length!=categoryNames.length)
            throw new CollectException(plan, "parameter exception");

        fileList = new ArrayList<>();

        setLastTimestamp();

        setEndPoint();

        for(int i=0; i<machines.length; ++i) {
            try {
                getMachineFileList(i);
            } catch (CollectMpaException e) {
                printError("machine "+e.getMachine()+" msg="+e.getMessage());
                failMachines.add(machines[i]);
            }
        }

        if(machines.length==failMachines.size()) {
            String msg = "all machines are invalid";
            printError(msg);
            throw new CollectException(plan, msg);
        }

        long files = 0;
        printInfo("total "+fileList.size()+" forms created. "+failMachines.size()+" machines failed");
        for(DownloadRequestForm _f: fileList) {
            FtpDownloadRequestForm f = (FtpDownloadRequestForm)_f;
            files += f.getFiles().size();
            printInfo(" machine=" +f.getMachine()+" category="+f.getCategoryName()+" files="+f.getFiles().size());
        }
        requestList = fileList;
        requestFiles = files;
        lastPointMillis = endPoint;
        searchTime = System.currentTimeMillis()-currentMillis;
        /*
        String startTime, endTime;
        Timestamp startTs = plan.getLastPoint()==null?plan.getStart():plan.getLastPoint();
        SimpleDateFormat dateFormat = Tool.getSimpleDateFormat();
        long endMillis;

        printInfo("---base="+Tool.getFtpTimeFormat(new Timestamp(getCollectBase())));
        if(getCollectBase()>startTs.getTime()) {
            Calendar _end = Calendar.getInstance();
            _end.setTimeInMillis(startTs.getTime());
            _end.add(Calendar.DATE, 1);
            _end.set(Calendar.HOUR_OF_DAY, 0);
            _end.set(Calendar.MINUTE, 0);
            _end.set(Calendar.SECOND, 0);
            _end.set(Calendar.MILLISECOND, 0);
            endMillis = _end.getTimeInMillis();

            if(endMillis>getCollectBase()) {
                endMillis = getCollectBase();
            }

            startTime = Tool.getFtpTimeFormat(startTs);
            endTime = dateFormat.format(endMillis);
        } else {
            startTime = Tool.getFtpTimeFormat(startTs);
            if (currentMillis > plan.getEnd().getTime()) {
                endTime = Tool.getFtpTimeFormat(plan.getEnd());
                endMillis = plan.getEnd().getTime();
            } else {
                endTime = dateFormat.format(currentMillis);
                endMillis = currentMillis;
            }
        }

        printInfo("collecting start="+startTime+" end="+endTime);
        List<DownloadRequestForm> list = new ArrayList<>();

        for(int i=0; i<machines.length; ++i) {
            List<DownloadRequestForm> _list = new ArrayList<>();
            try {
                for(int j=0; j<categoryCodes.length; ++j) {
                    getFileList(_list, machines[i].trim(), fabs[i].trim(), categoryCodes[j].trim(), categoryNames[j].trim(),
                            startTime, endTime, "", "");
                    if (_list.size() > 0) {
                        list.addAll(_list);
                    }
                }
            } catch (CollectMpaException e) {
                printError("machine "+e.getMachine()+" msg="+e.getMessage());
                failMachines.add(machines[i]);
            }
        }

        if(machines.length==failMachines.size()) {
            String msg = "all machines are invalid";
            printError(msg);
            throw new CollectException(plan, msg);
        }

        long files = 0;
        printInfo("total "+list.size()+" forms created. "+failMachines.size()+" machines failed");
        for(DownloadRequestForm _f: list) {
            FtpDownloadRequestForm f = (FtpDownloadRequestForm)_f;
            files += f.getFiles().size();
            printInfo(" machine=" +f.getMachine()+" category="+f.getCategoryName()+" files="+f.getFiles().size());
        }
        requestList = list;
        requestFiles = files;
        lastPointMillis = endMillis;
        */
    }

    @Override
    protected Timestamp getLastPoint() {
        if(lastPointMillis!=0) {
            return new Timestamp(lastPointMillis);
        }
        return null;
    }

    @Override
    protected long[] updateLastTimestamp() throws CollectException {
        if(machines==null || lastTimestamp==null || machines.length==0 || machines.length!=lastTimestamp.length) {
            throw new CollectException(plan, "updateLastTimestamp: sequence error");
        }

        if(requestFiles==0) {
            return null;
        }

        long[] updateTimestamp = new long[lastTimestamp.length];
        for(int i=0; i<lastTimestamp.length; ++i) {
            updateTimestamp[i] = lastTimestamp[i];
        }

        for(DownloadRequestForm _f: requestList) {
            FtpDownloadRequestForm form = (FtpDownloadRequestForm)_f;
            if(form.getFiles().size()>0) {
                long ts = form.getLastTimestamp()-searchTime;
                for (int i = 0; i < machines.length; ++i) {
                    if (machines[i].trim().equals(form.getMachine().trim())) {
                        if(ts>updateTimestamp[i]) {
                            updateTimestamp[i] = ts;
                        }
                        break;
                    }
                }
            }
        }
        return updateTimestamp;
    }

    @Override
    protected void finish() {
        lastTimestamp = null;
        machines = null;
        fabs = null;
        categoryCodes = null;
        categoryNames = null;
        fileList = null;
    }

    private void getFileList(List<DownloadRequestForm> list, String machine, String fab, String categoryCode,
                             String categoryName, String start, String end, String keyword, String path)
            throws CollectMpaException, InterruptedException {
        // Interrupt point.
        Thread.sleep(1);

        printInfo("getFtpFileList machine="+machine+" category="+categoryCode);
        LogFileList fileList = connector.getFtpFileList(machine, categoryCode, start, end, keyword, path, true);
        if(fileList==null) {
            printError("getFileList: null fileList");
            throw new CollectMpaException(machine, "null fileList");
        }
        if(fileList.getErrorMessage()!=null) {
            printError("getFileList error "+fileList.getErrorMessage());
            throw new CollectMpaException(machine, "failed to get file list");
        }

        FtpDownloadRequestForm form = new FtpDownloadRequestForm(fab, machine, categoryCode, categoryName);

        for(FileInfo file: fileList.getList()) {
            if(file.getFilename().endsWith(".") || file.getFilename().endsWith("..") || file.getFilename().startsWith("###")) {
                continue;
            }

            if(file.getType().equalsIgnoreCase("D")) {
                SimpleDateFormat dateFormat = Tool.getSimpleDateFormat();
                try {
                    long _timestamp = dateFormat.parse(file.getTimestamp()).getTime();
                    long _start = dateFormat.parse(start).getTime();
                    long _end = dateFormat.parse(end).getTime();
                    if(_timestamp<_start || _timestamp>_end)
                        continue;
                } catch (ParseException e) {
                    printError("timestamp parsing failed");
                }
                getFileList(list, machine, fab, categoryCode, categoryName, start, end, keyword, file.getFilename());
            } else {
                if(file.getSize()==0) {
                    continue;
                }
                try {
                    form.addFile(file.getFilename(), file.getSize(), file.getTimestamp());
                } catch (ParseException e) {
                    log.error("create download form error (date="+file.getTimestamp()+")");
                    throw new CollectMpaException(machine, "create download form error");
                }
            }
        }
        list.add(form);
    }

    private void setLastTimestamp() throws CollectException {
        if(machines.length==0) {
            throw new CollectException(plan, "last point calculation error@1");
        }
        lastTimestamp = new long[machines.length];

        // For migration from old version.
        String _lastTimestamp = plan.getLastTimestamp();
        if(_lastTimestamp==null) {
            printError("setLastTimestamp: null lastTimestamp");
            throw new CollectException(plan, "null lastTimestamp");
        }

        String[] lastTimestamp = _lastTimestamp.split(",");
        if(lastTimestamp.length!=machines.length) {
            throw new CollectException(plan, "last point calculation error@2");
        }
        for(int i=0; i<machines.length; ++i) {
            this.lastTimestamp[i] = Long.parseLong(lastTimestamp[i]);
        }
    }

    private void setEndPoint() {
        long collectBase = getCollectBase();
        Timestamp lastPointTs = plan.getLastPoint();
        long lastPoint = lastPointTs==null?plan.getStart().getTime():lastPointTs.getTime();
        {
            SimpleDateFormat dateFormat = Tool.getSimpleDateFormat();
            printInfo("base="+dateFormat.format(new Date(collectBase))+ "last="+dateFormat.format(new Date(lastPoint)));
        }
        if(collectBase>lastPoint) {
            Calendar _end = Calendar.getInstance();
            _end.setTimeInMillis(lastPoint);
            _end.add(Calendar.DATE, 1);
            _end.set(Calendar.HOUR_OF_DAY, 0);
            _end.set(Calendar.MINUTE, 0);
            _end.set(Calendar.SECOND, 0);
            _end.set(Calendar.MILLISECOND, 0);

            endPoint = _end.getTimeInMillis();
            if(endPoint>collectBase) {
                endPoint = collectBase;
            }
        } else {
            if(currentMillis>plan.getEnd().getTime()) {
                endPoint = plan.getEnd().getTime();
            } else {
                endPoint = currentMillis;
            }
        }

    }

    private void getMachineFileList(int machineIndex) throws CollectException, CollectMpaException, InterruptedException {
        if(machines.length!= lastTimestamp.length) {
            throw new CollectException(plan, "getMachineFileList: param error. machineIndex="+machineIndex);
        }

        long startPoint = lastTimestamp[machineIndex];
        SimpleDateFormat dateFormat = Tool.getSimpleDateFormat();
        String startTime = dateFormat.format(startPoint);
        String endTime = dateFormat.format(endPoint);

        printInfo("machine="+machines[machineIndex]+" start="+startTime+" end="+endTime);
        List<DownloadRequestForm> machineFileList = new ArrayList<>();
        for(int i=0; i<categoryCodes.length; ++i) {
            getFileList(
                    machineFileList,
                    machines[machineIndex].trim(),
                    fabs[machineIndex].trim(),
                    categoryCodes[i].trim(),
                    categoryNames[i].trim(),
                    startTime,
                    endTime,
                    "",
                    "");
        }
        if(fileList==null) {
            throw new CollectException(plan, "getMachineFileList: null fileList");
        }
        fileList.addAll(machineFileList);
    }

    private void __checkPlanType() throws CollectException {
        if(!plan.getPlanType().equals("ftp")) {
            throw new CollectException(plan, "wrong plan type");
        }
    }
}
