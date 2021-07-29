package jp.co.canon.cks.eec.fs.rssportal.background.autocollect;

import jp.co.canon.ckbs.eec.fs.collect.controller.param.VFtpSssListRequestResponse;
import jp.co.canon.ckbs.eec.fs.collect.service.VFtpFileInfo;
import jp.co.canon.cks.eec.fs.rssportal.background.DownloadRequestForm;
import jp.co.canon.cks.eec.fs.rssportal.background.FileDownloader;
import jp.co.canon.cks.eec.fs.rssportal.background.VFtpSssDownloadRequestForm;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import jp.co.canon.cks.eec.fs.rssportal.common.Tool;
import jp.co.canon.cks.eec.fs.rssportal.dao.CollectionPlanDao;
import jp.co.canon.cks.eec.fs.rssportal.vo.CollectPlanVo;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class VFtpSssCollectProcess extends CollectProcess {

    private long lastPointMillis;

    public VFtpSssCollectProcess(PlanManager manager, CollectPlanVo plan, CollectionPlanDao dao, FileDownloader downloader, EspLog log) {
        super(manager, plan, dao, downloader, log);
        if (!plan.getPlanType().equalsIgnoreCase("vftp_sss")) {
            printError("invalid planType " + plan.getPlanType());
        }
    }

    @Override
    protected void createDownloadFileList() throws CollectException, InterruptedException {
        __checkPlanType();

        String[] machines = plan.getTool().split(",");
        String[] fabs = plan.getFab().split(",");
        String[] directories = plan.getDirectory().split(",");
        if (machines.length == 0 || machines.length != fabs.length)
            throw new CollectException(plan, "parameter exception");

        lastPointMillis = 0;
        String startTime, endTime;
        Timestamp startTs;
        long endMillis;

        if (plan.getLastPoint() == null) {
            startTs = plan.getStart();
        } else {
            startTs = plan.getLastPoint();
        }

        if(getCollectBase()>startTs.getTime()) {
            Calendar endCal = Calendar.getInstance();
            endCal.setTimeInMillis(startTs.getTime() + aDayMillis);
            endCal.set(endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH), endCal.get(Calendar.DATE),
                    0, 0, 0);

            endMillis = endCal.getTimeInMillis();
            if (endMillis > plan.getEnd().getTime()) {
                endMillis = plan.getEnd().getTime();
            }

            if (endMillis > currentMillis) {
                throw new CollectException(plan, false);
            }

            startTime = Tool.getVFtpTimeFormat(startTs);
            endTime = Tool.getVFtpTimeFormat(new Timestamp(endMillis));
        } else {
            SimpleDateFormat dateFormat = Tool.getVFtpSimpleDateFormat();
            startTime = Tool.getVFtpTimeFormat(startTs);
            if(currentMillis>plan.getEnd().getTime()) {
                endTime = Tool.getVFtpTimeFormat(plan.getEnd());
                endMillis = plan.getEnd().getTime();
            } else {
                endTime = dateFormat.format(currentMillis);
                endMillis = currentMillis;
            }
        }

        printInfo("start=" + startTime + " end=" + endTime);

        List<DownloadRequestForm> list = new ArrayList<>();

        loop_top:
        for (int i = 0; i < machines.length; ++i) {
            for (String directory : directories) {
                String _directory = String.format(directory, startTime, endTime);
                VFtpSssListRequestResponse response = connector.createVFtpSssListRequest(machines[i], _directory);
                if (response == null || response.getErrorMessage() != null || response.getRequest() == null) {
                    printError("failed to get file-list machine="+machines[i]+" dir="+_directory);
                    continue loop_top;
                }
                try {
                    response = waitListRequestDone(machines[i], response.getRequest().getRequestNo());
                } catch (CollectMpaException e) {
                    printError("machine "+e.getMachine()+" msg="+e.getMessage());
                    failMachines.add(e.getMachine());
                    continue loop_top;
                }
                VFtpFileInfo[] files = response.getRequest().getFileList();
                if (files.length > 0) {
                    VFtpSssDownloadRequestForm form = new VFtpSssDownloadRequestForm(fabs[i], machines[i], _directory);
                    for (VFtpFileInfo file : files) {
                        form.addFile(file.getFileName(), file.getFileSize());
                    }
                    list.add(form);
                }
            }
        }

        if(machines.length==failMachines.size()) {
            String msg = "all machines are invalid";
            printError(msg);
            throw new CollectException(plan, msg);
        }

        printInfo("total "+list.size()+" forms created. "+failMachines.size()+" machines failed");
        for(DownloadRequestForm _f: list) {
            VFtpSssDownloadRequestForm f = (VFtpSssDownloadRequestForm)_f;
            printInfo(" machine=" +f.getMachine()+" directory="+f.getDirectory());
        }
        requestList = list;
        requestFiles = list.size();
        lastPointMillis = endMillis;
    }

    private VFtpSssListRequestResponse waitListRequestDone(String machine, String requestNo) throws CollectMpaException, InterruptedException {
        final long timeout = 30000;
        long start = System.currentTimeMillis();
        while (true) {
            VFtpSssListRequestResponse resp = connector.getVFtpSssListRequest(machine, requestNo);
            if (resp == null || resp.getErrorMessage() != null || resp.getRequest() == null) {
                break;
            } else if (resp.getRequest().getFileList() != null) {
                return resp;
            } else if ((System.currentTimeMillis() - start) > timeout) {
                printError("create list timeout");
                break;
            }
            Thread.sleep(100);
        }
        throw new CollectMpaException(machine, "failed to create file-list");
    }

    @Override
    protected Timestamp getLastPoint() {
        if (lastPointMillis != 0) {
            return new Timestamp(lastPointMillis);
        }
        return null;
    }

    private void __checkPlanType() throws CollectException {
        if(!plan.getPlanType().equals("vftp_sss")) {
            throw new CollectException(plan, "wrong plan type");
        }
    }


}
