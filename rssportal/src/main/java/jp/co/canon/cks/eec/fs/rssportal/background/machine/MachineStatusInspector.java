package jp.co.canon.cks.eec.fs.rssportal.background.machine;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jp.co.canon.cks.eec.fs.rssportal.common.FileLog;
import jp.co.canon.cks.eec.fs.rssportal.common.LogType;
import jp.co.canon.cks.eec.fs.rssportal.service.FileServiceInspector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class MachineStatusInspector extends Thread {

    private FileLog log;

    @Value("${rssportal.machine-status.period}")
    private long period;

    @Value("${rssportal.machine-status.keeping-period}")
    private long keeping;

    @Value("${rssportal.machine-status.root}")
    private String root;

    @Autowired
    FileServiceInspector inspector;

    private Thread thread;
    private long periodMillis;
    private long keepingMillis;
    private List<MachineStatus> machineList;
    private File machineInfo;

    @PostConstruct
    private void _init() {

        if(period<60) {
            period = 60;
        }
        periodMillis = period*1000;

        if(keeping<(period*10)) {
            keeping = period*10;
        }
        keepingMillis = keeping*1000;

        machineList = new ArrayList<>();

        File rootFile = Paths.get(root).toFile();
        if(rootFile.exists() && rootFile.isFile()) {
            rootFile.delete();
        }
        if(!rootFile.exists()) {
            rootFile.mkdirs();
        }

        //log = new FileLog(Paths.get(root, "machine-status-inspector.log").toFile(), "MachineStatusInspector");
        log = new FileLog.Builder()
                .file(Paths.get(root, "machine-status-inspector.log").toFile())
                .name("MachineStatusInspector")
                .useRolling(true)
                .build();

        thread = new Thread(loop);
        this.start();
    }

    /**
     * This thread maintains the instance scheduler is running well.
     * If not, it try to rerun a scheduler.
     */
    @Override
    public void run() {

        thread.start();

        while(true) {
            try {
                if(!thread.isAlive()) {
                    log.error("scheduler is not running");
                    thread.join();

                    log.info("start recovering the scheduler");
                    thread = new Thread(loop);
                    thread.start();
                }
                sleep(30000);
            } catch (InterruptedException e) {
                log.error("MachineStatusInspect has to be running on");
            }
        }
    }

    /**
     * Check the schedule to check machines.
     */
    private Runnable loop = ()->{

        log.info("start monitoring machines");

        try {
            while(true) {

                long startMillis = System.currentTimeMillis();

                try {
                    // Clear all machine status on heap.
                    machineList.clear();

                    // Get machine hierarchy.
                    Map<String, List<String>> hierarchy = inspector.getMachineHierarchy();

                    List<Thread> otsTasks = new ArrayList<>();

                    hierarchy.forEach((ots, machines)->{

                        // Check the ots status.
                        Thread otsTask = doTask(()->{
                            try {
                                boolean connected = inspector.checkOts(ots)==FileServiceInspector.connected;
                                MachineStatus otsStatus = new MachineStatus(ots, MachineStatus.Type.ots, connected?"connected":"disconnected");
                                machineList.add(otsStatus);
                                report(otsStatus);

                                if(connected) {
                                    // When the ots is available, it looks over each machines.
                                    List<Thread> tasks = new ArrayList<>();

                                    for(String machine: machines) {
                                        tasks.add(doTask(()->{
                                            int[] _connected = inspector.checkMachine(machine);
                                            MachineStatus status = new MachineStatus(
                                                    machine,
                                                    MachineStatus.Type.mpa,
                                                    _connected[FileServiceInspector.FTP_STATUS]==FileServiceInspector.connected?"connected":"disconnected",
                                                    _connected[FileServiceInspector.VFTP_STATUS]==FileServiceInspector.connected?"connected":"disconnected");
                                            machineList.add(status);
                                            report(status);
                                        }));
                                    }

                                    // Waiting all inquiries are done.
                                    for(Thread task: tasks) {
                                        while(task.isAlive()) {
                                            Thread.sleep(10);
                                        }
                                    }
                                } else {
                                    // When the ots isn't available..
                                    for(String machine: machines) {
                                        MachineStatus status = new MachineStatus(machine, MachineStatus.Type.mpa,
                                                "disconnected", "disconnected");
                                        machineList.add(status);
                                        report(status);
                                    }
                                }
                            } catch (InterruptedException e) {
                                // What's going on?
                                e.printStackTrace();
                            }
                        });

                        otsTasks.add(otsTask);
                    });

                    for(Thread task: otsTasks) {
                        while(task.isAlive()) {
                            Thread.sleep(10);
                        }
                    }

                    store();

                    purge();

                    log.info(String.format("inspection done.. %d ms", System.currentTimeMillis()-startMillis));

                } catch (IOException e) {
                    log.warn("cannot read machine list");
                }
                Thread.sleep(periodMillis);
            }
        } catch (InterruptedException e) {
            // What's going on?
            e.printStackTrace();
        }
    };

    private Thread doTask(Runnable runnable) {
        if(runnable!=null) {
            Thread thread = new Thread(runnable);
            thread.start();
            return thread;
        }
        return null;
    }

    /**
     * Put useful information on a debugging log file.
     */
    private void report(String machine, boolean connected) {
        log.info(String.format("machine %s connection %s", machine, connected));
    }

    private void report(MachineStatus status) {
        if(status.getType()==MachineStatus.Type.mpa) {
            log.info(String.format("[MPA] %s: ftp:%s vftp:%s", status.getName(),
                    status.getFtpStatus(), status.getVFtpStatus()));
        } else {
            log.info(String.format("[OTS] %s: ots:%s", status.getName(), status.getOtsStatus()));
        }
    }

    /**
     * The status of machines are managed on json files. This method stores current status into a json.
     */
    private void store() {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode array = mapper.createArrayNode();
        for(MachineStatus machine: machineList) {
            JsonNode node = mapper.convertValue(machine, JsonNode.class);
            array.add(node);
        }
        File file = createJsonFile();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        try {
            writer.writeValue(file, array);
            machineInfo = file;
        } catch (IOException e) {
            log.error("failed to create result json");
        }
    }

    /**
     * Purge formal json files.
     */
    private void purge() {
        long current = System.currentTimeMillis();
        File rootFile = Paths.get(root).toFile();
        for(File json: rootFile.listFiles(file->file.getName().endsWith("json"))) {
            long being = current-json.lastModified();
            if(being>keepingMillis) {
                json.delete();
            }
        }
        final long loggingPeriod = 7*24*3600*1000;
        for(File file: rootFile.listFiles()) {
            long being = current-file.lastModified();
            if(being>loggingPeriod) {
                file.delete();
            }
        }
    }

    private File createJsonFile() {
        while(true) {
            String dateStr = (new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SSS")).format(new Date(System.currentTimeMillis()));
            String fileName = String.format("machine-status-%s.json", dateStr);
            File file = Paths.get(root, fileName).toFile();
            if(file.exists()) {
                continue;
            }
            return file;
        }
    }

    /**
     * This method gets the specific machine status.
     * @param machine
     * @return  When the specific machine connected, it returns true,
     *          When disconnected or before trying to get information, return false.
     */
    public boolean getMachineFtpStatus(String machine) {
        if(machine==null) {
            return false;
        }
        try {
            if (machineInfo != null) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    MachineStatus[] machines = mapper.readValue(machineInfo, MachineStatus[].class);
                    for (MachineStatus m : machines) {
                        if (m.getType() == MachineStatus.Type.mpa && m.getName().equals(machine)) {
                            return m.getFtpStatus().equalsIgnoreCase("connected") ? true : false;
                        }
                    }
                } catch (IOException e) {
                }
            }
        } catch (RuntimeException e) {
            log.error("getMachineFtpStatus: runtime exception occurs machine="+machine);
        }
        return false;
    }

    /**
     * This method gets the specific machine status.
     * @param machine
     * @return  When the specific machine connected, it returns true,
     *          When disconnected or before trying to get information, return false.
     */
    public boolean getMachineVFtpStatus(String machine) {
        if(machine==null) {
            return false;
        }
        try {
            if (machineInfo != null) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    MachineStatus[] machines = mapper.readValue(machineInfo, MachineStatus[].class);
                    for (MachineStatus m : machines) {
                        if (m.getType() == MachineStatus.Type.mpa && m.getName().equals(machine)) {
                            return m.getVFtpStatus().equalsIgnoreCase("connected") ? true : false;
                        }
                    }
                } catch (IOException e) {
                }
            }
        } catch (RuntimeException e) {
            log.error("getMachineVFtpStatus: runtime exception occurs machine="+machine);
        }
        return false;
    }

    /**
     * This method returns the specific ots is available or not.
     * @param ots
     * @return
     */
    public boolean getOtsStatus(String ots) {
        if(ots==null) {
            return false;
        }
        try {
            if (machineInfo != null) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    MachineStatus[] machines = mapper.readValue(machineInfo, MachineStatus[].class);
                    try {
                        for (MachineStatus m : machines) {
                            if (m.getType() == MachineStatus.Type.ots && m.getName().equals(ots)) {
                                return m.getOtsStatus().equalsIgnoreCase("connected") ? true : false;
                            }
                        }
                    } catch (RuntimeException e) {
                        log.error("getOtsStatus RuntimeException occurs. " + e.getMessage(), LogType.exception);
                        log.error(machines.toString());
                    }
                } catch (IOException e) {
                }
            }
        } catch (RuntimeException e) {
            log.error("getOtsStatus: runtime exception occurs ots="+ots);
        }
        return false;
    }


}

