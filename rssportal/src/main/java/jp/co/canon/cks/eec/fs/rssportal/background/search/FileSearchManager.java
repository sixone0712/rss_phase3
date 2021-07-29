package jp.co.canon.cks.eec.fs.rssportal.background.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnector;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnectorFactory;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import jp.co.canon.cks.eec.fs.rssportal.common.LogType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class FileSearchManager {

    public static final String DONE = "done";
    public static final String IN_PROGRESS = "in-progress";
    public static final String ERROR = "error";
    public static final String CANCELED = "canceled";

    public static final String FTP_TYPE = "ftp";
    public static final String VFTP_SSS_TYPE = "vftp-sss";

    private EspLog log = new EspLog(getClass());
    private Thread parentThread;
    private Manager manager;

    @Value("${rssportal.file-search.max-jobs}")
    private int maxJobs;

    @Value("${rssportal.file-service-manager.addr}")
    private String fileServiceAddress;

    @Value("${rssportal.file-search.storage-root}")
    private String root;

    @Value("${rssportal.file-search.keeping-period}")
    private int keepingHours;

    @Value("${rssportal.file-search.purge-interval}")
    private int purgeIntervalMinutes;
    private Calendar nextPurge;

    @Value("${rssportal.file-search.kick-out}")
    private int kickOutSeconds;

    @Autowired
    private FileServiceManageConnectorFactory connectorFactory;
    private FileServiceManageConnector connector;

    @PostConstruct
    private void _init() {
        parentThread = new Thread(parentRunner);
        parentThread.start();
    }

    private Runnable parentRunner = ()->{
        log.info("SearchManager root-thread start");

        connector = connectorFactory.getConnector(fileServiceAddress);

        int i = 0;
        while(true) {
            if(manager ==null || !manager.isAlive()) {
                manager = new Manager(i++);
                manager.setName("FileSearchManager-"+i);
                manager.start();
            }
            try {
                Thread.sleep(60*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public String requestFtpSearch(String[] fabNames, String[] machineNames, String[] categoryNames,
                                   String[] categoryCodes, String start, String end, int depth)
            throws FileSearchException {

        if(manager!=null && manager.isAlive()) {
            return manager.requestFtpSearch(fabNames, machineNames, categoryNames, categoryCodes, start, end, depth);
        }
        return null;
    }

    public String requestVFtpSearch(String[] fabNames, String[] machineNames, String command) throws FileSearchException {
        if(manager!=null && manager.isAlive()) {
            return manager.requestVFtpSearch(fabNames, machineNames, command);
        }
        return null;
    }


    public void cancelJob(String searchId) {
        if(manager!=null && manager.isAlive()) {
            manager.cancelSearch(searchId);
        }
    }

    public FileSearchInfo getSearchInfo(String searchId) {
        if(manager!=null && manager.isAlive()) {
            return manager.getSearchInfo(searchId);
        }
        return null;
    }

    public List getSearchedFileList(String searchId) {
        if(manager!=null && manager.isAlive()) {
            return manager.getSearchedFileList(searchId);
        }
        return null;
    }

    private class Manager extends Thread {

        private final int managerId;
        private AtomicLong key = new AtomicLong(1);;
        private Map<String, FileSearchJob> jobList = new HashMap<>();


        private Manager(int id) {
            this.managerId = id;
        }

        @Override
        public void run() {
            log.info("FileSearchManager start");

            purgeAll();

            try {

                while(true) {
                    sleep(1000);

                    List<String> terminates = getTerminates();

                    shutdown(terminates);

                    timeoutSelf();

                    purge();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private List<String> getTerminates() {
            List<String> terminates = new ArrayList<>();
            jobList.forEach((jobId, job)->{
                if(!job.isAlive() && job.isDead()) {
                    log.info(jobId+" search task was terminated");
                    try {
                        job.join();
                    } catch (InterruptedException e) {
                        log.warn("interrupt occurs when thread join");
                        e.printStackTrace();
                    }
                    terminates.add(jobId);
                }
            });
            return terminates;
        }

        private void shutdown(List<String> target) {
            for(String jobId: target) {
                jobList.remove(jobId);
                log.info("removed "+jobId+" in memory");
            }
        }

        private void timeoutSelf() {
            jobList.forEach((jobId, job)->{
                Calendar reference = Calendar.getInstance();
                reference.add(Calendar.SECOND, -kickOutSeconds);
                if(job.getLastAccess().before(reference)) {
                    log.warn(jobId+" timeout occurs. try to cancel the searching");
                    job.cancel();
                }
            });
        }

        private String requestFtpSearch(String[] fabNames, String[] machineNames, String[] categoryNames,
                                        String[] categoryCodes, String start, String end, int depth)
                throws FileSearchException {

            prerequisite();

            FileSearchJob job;
            do {
                job = new FileSearchJob.Builder()
                        .connector(connector)
                        .jobType(FTP_TYPE)
                        .root(root)
                        .fabNames(fabNames)
                        .machineNames(machineNames)
                        .categoryNames(categoryNames)
                        .categoryCodes(categoryCodes)
                        .start(start)
                        .end(end)
                        .depthLimit(depth)
                        .build(key.getAndIncrement());
                if(job==null) {
                    log.error("failed to create ftp search job");
                    throw new FileSearchException(FileSearchException.Error.invalidParam);
                }
            } while(jobList.containsKey(job.getJobId()));

            addAndStartJob(job);
            return job.getJobId();
        }

        private String requestVFtpSearch(String[] fabNames, String[] machineNames, String command) throws FileSearchException {

            prerequisite();

            FileSearchJob job;
            do {
                job = new FileSearchJob.Builder()
                        .connector(connector)
                        .jobType(VFTP_SSS_TYPE)
                        .root(root)
                        .fabNames(fabNames)
                        .machineNames(machineNames)
                        .command(command)
                        .build(key.getAndIncrement());

                if(job==null) {
                    log.error("failed to create vftp search job");
                    throw new FileSearchException(FileSearchException.Error.invalidParam);
                }
            } while(jobList.containsKey(job.getJobId()));

            addAndStartJob(job);
            return job.getJobId();
        }

        private void cancelSearch(String jobId) {

            log.info("cancel "+jobId);

            if(jobList.containsKey(jobId)) {
                FileSearchJob job = jobList.get(jobId);
                job.cancel();
            }
        }

        private FileSearchInfo getSearchInfo(String jobId) {
            FileSearchInfo info;
            FileSearchJob job;

            if(jobList.containsKey(jobId) && (job=jobList.get(jobId))!=null) {
                return job.getInfo();
            }

            File data = Paths.get(root, jobId+".data").toFile();

            if(data.exists() && data.isFile()) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    Map json = mapper.readValue(data, Map.class);
                    info = new FileSearchInfo();
                    info.setSearchId(jobId);
                    if(json.containsKey("status")) {
                        String status = (String)json.get("status");
                        info.setStatus(status);
                        if(status.equals(DONE)) {
                            String jobType = (String)json.get("jobType");
                            info.setResultUrl(FileSearchJob.createResultUrl(jobId, jobType));
                        }
                    }
                    if(json.containsKey("searchedCount")) {
                        info.setSearchedCnt(((Number)json.get("searchedCount")).longValue());
                    }
                    return info;
                } catch (IOException e) {
                    log.error("failed to read search result file", LogType.exception);
                }
            }
            info = new FileSearchInfo();
            info.setSearchId(jobId);
            info.setStatus(ERROR);
            return info;
        }

        private List getSearchedFileList(String jobId) {
            ObjectMapper mapper = new ObjectMapper();
            // This retry routine guarantees an interval between the thread stops
            // and when the servlet is able to access data files.
            int retry = 5;
            while(--retry!=0) {
                try {
                    File data = Paths.get(root, jobId+".data").toFile();
                    if (data.exists() && data.isFile()) {
                        Map json = mapper.readValue(data, Map.class);
                        if (json.containsKey("files")) {
                            return (List) json.get("files");
                        }
                    } else {
                        Thread.sleep(300);
                    }
                } catch (IOException | InterruptedException e) {
                    log.error(String.format("failed to read searched file list (error=%s)",
                            e.getMessage()), LogType.exception);
                    return null;
                }
                log.warn(String.format("retry to open data file (retry=%d)", retry));
            }
            log.error("cannot find a data file for "+jobId);
            return null;
        }

        private void prerequisite() throws FileSearchException {
            if(jobList.size()>=maxJobs) {
                log.warn("job full");
                throw new FileSearchException(FileSearchException.Error.jobFull);
            }
        }

        private void addAndStartJob(FileSearchJob job) {
            jobList.put(job.getJobId(), job);
            job.start();
            log.info("file-search "+job.getJobId()+" start");
        }

        private void purge() {
            Calendar current = Calendar.getInstance();
            if(nextPurge==null || current.after(nextPurge)) {
                File rootFile = Paths.get(root).toFile();
                long millis = current.getTimeInMillis()-(keepingHours*3600*1000);
                for(File file: rootFile.listFiles()) {
                    if(file.isFile() && file.lastModified()<millis) {
                        log.info("purger deleted "+file.getName());
                        file.delete();
                    }
                }
                nextPurge = current;
                nextPurge.add(Calendar.MINUTE, purgeIntervalMinutes);
            }
        }

        private void purgeAll() {
            File rootFile = Paths.get(root).toFile();
            long start = System.currentTimeMillis();
            for(File file: rootFile.listFiles()) {
                if(file.isFile()) {
                    file.delete();
                }
            }
            log.info("purge all "+(System.currentTimeMillis()-start)+" ms");
        }
    }
}
