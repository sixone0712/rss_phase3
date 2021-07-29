package jp.co.canon.cks.eec.fs.rssportal.background.autocollect;

import jp.co.canon.cks.eec.fs.rssportal.background.DownloadRequestForm;
import jp.co.canon.cks.eec.fs.rssportal.background.FileDownloader;
import jp.co.canon.cks.eec.fs.rssportal.background.VFtpCompatDownloadRequestForm;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import jp.co.canon.cks.eec.fs.rssportal.common.Tool;
import jp.co.canon.cks.eec.fs.rssportal.dao.CollectionPlanDao;
import jp.co.canon.cks.eec.fs.rssportal.vo.CollectPlanVo;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class VFtpCompatCollectProcess extends CollectProcess {

    private long lastPointMillis;

    public VFtpCompatCollectProcess(
            PlanManager manager, CollectPlanVo plan, CollectionPlanDao dao, FileDownloader downloader, EspLog log) {
        super(manager, plan, dao, downloader, log);
        if(!plan.getPlanType().equalsIgnoreCase("vftp_compat")) {
            printError("invalid planType "+plan.getPlanType());
        }
    }

    @Override
    protected void createDownloadFileList() throws CollectException, InterruptedException {
        __checkPlanType();

        String[] machines = plan.getTool().split(",");
        String[] fabs = plan.getFab().split(",");
        String[] commands = plan.getCommand().split(",");
        if(machines.length==0 || machines.length!=fabs.length)
            throw new CollectException(plan, "parameter exception");

        lastPointMillis = 0;
        String startTime, endTime;
        Timestamp startTs;
        long endMillis;

        if(plan.getLastPoint()==null) {
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

        for(int i=0; i<machines.length; ++i) {
            for(String command: commands) {
                String _command;
                if(command.equals("") || command.startsWith("none")) {
                    _command = String.format("%s-%s.log", startTime, endTime);
                } else {
                    _command = String.format(command, startTime, endTime);
                    _command += ".log";
                }
                list.add(new VFtpCompatDownloadRequestForm(fabs[i], machines[i], _command, true));
            }
        }

        printInfo("total "+list.size()+" forms created. ");
        for(DownloadRequestForm _f: list) {
            VFtpCompatDownloadRequestForm f = (VFtpCompatDownloadRequestForm)_f;
            printInfo(" machine=" +f.getMachine()+" command="+f.getCommand());
        }
        requestList = list;
        requestFiles = list.size();
        lastPointMillis = endMillis;
    }

    @Override
    protected Timestamp getLastPoint() {
        if(lastPointMillis!=0) {
            return new Timestamp(lastPointMillis);
        }
        return null;
    }

    private void __checkPlanType() throws CollectException {
        if(!plan.getPlanType().equals("vftp_compat")) {
            throw new CollectException(plan, "wrong plan type");
        }
    }
}
