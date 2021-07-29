package jp.co.canon.ckbs.eec.servicemanager.service;

import jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure.OtsInfo;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure.StructureLoader;
import jp.co.canon.ckbs.eec.servicemanager.connector.ServiceManagerConnector;
import jp.co.canon.ckbs.eec.servicemanager.connector.ServiceManagerConnectorFactory;
import jp.co.canon.ckbs.eec.servicemanager.controller.ErrorInfo;
import jp.co.canon.ckbs.eec.servicemanager.controller.RestartResponse;
import jp.co.canon.ckbs.eec.servicemanager.executor.CustomExecutor;
import jp.co.canon.ckbs.eec.servicemanager.executor.CustomOutputStreamLineHandler;
import jp.co.canon.ckbs.eec.servicemanager.executor.SshExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SystemService {
    @Autowired
    DockerService dockerService;

    @Autowired
    ServiceManagerConnectorFactory connectorFactory;

    @Value("${servicemanager.diskUsageDirectory}")
    String diskUsageDirectory;

    @Value("${servicemanager.type}")
    String systemType;

    @Value("${servicemanager.espContainerNameList}")
    String[] espContainerNameArr;

    @Value("${servicemanager.otsContainerNameList}")
    String[] otsContainerNameArr;

    String[] containerNameArr;

    StructureLoader structureLoader;

    @PostConstruct
    void postConstruct(){
        log.info("disk usage directory : " + diskUsageDirectory);

        if (systemType.equals("ESP")){
            containerNameArr = espContainerNameArr;
        } else if (systemType.equals("OTS")){
            containerNameArr = otsContainerNameArr;
        }

        structureLoader = new StructureLoader("/CANON/ENV/structure.json");

        for(String containerName : containerNameArr){
            log.info("ContainerName : " + containerName);
        }
    }

    public OtsInfo[] getOtsInfos(){
        try {
            return structureLoader.getOtsList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new OtsInfo[0];
    }

    public OtsInfo getOtsInfo(String device){
        OtsInfo[] otsList = getOtsInfos();
        OtsInfo info = null;
        for(OtsInfo x : otsList){
            if (x.getName().equals(device)){
                info = x;
                break;
            }
        }
        return info;
    }

    ContainerInfo[] getContainerInfos(){
        ContainerInfo[] containerInfos = dockerService.getContainers();

        ArrayList<ContainerInfo> list = new ArrayList<>();
        for(String containerName : containerNameArr){
            ContainerInfo info = new ContainerInfo();
            info.setName(containerName);
            info.setStatus("Not Exists.");
            list.add(info);
        }

        for(ContainerInfo info : containerInfos){
            for(ContainerInfo infoInList : list){
                if (info.getName().equals(infoInList.name)){
                    infoInList.setStatus(info.getStatus());
                }
            }
        }
        return list.toArray(new ContainerInfo[0]);
    }

    public SystemInfo getSystemInfo(){
        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setName(systemType);
        systemInfo.setHost("-");
        systemInfo.setVolumeTotal("Unknown");
        systemInfo.setVolumeUsed("Unknown");

        File diskUsageDirFile = new File(diskUsageDirectory);

        if (diskUsageDirFile.exists()){
            long totalSpace = diskUsageDirFile.getTotalSpace();
            long freeSpace = diskUsageDirFile.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            systemInfo.setVolumeTotal(String.format("%.1fG", (float)totalSpace / (1024 * 1024 * 1024)));
            systemInfo.setVolumeUsed(String.format("%.1fG", (float)usedSpace / (1024 * 1024 * 1024)));
        }

        systemInfo.setContainers(getContainerInfos());

        return systemInfo;
    }

    public SystemInfo[] getSystemInfoList(){
        List<SystemInfo> systemInfoList = new ArrayList<>();

        SystemInfo systemInfo = getSystemInfo();
        systemInfoList.add(systemInfo);

        OtsInfo[] otsList = getOtsInfos();

        for(OtsInfo info : otsList){
            ServiceManagerConnector connector = connectorFactory.getConnector(info.getHost(), info.getPort());
            SystemInfo otsStatus = connector.getSystemInfo();
            if (otsStatus == null){
                otsStatus = new SystemInfo();
                ArrayList<ContainerInfo> containerInfoList = new ArrayList<>();
                for(String containerName : otsContainerNameArr){
                    ContainerInfo containerInfo = new ContainerInfo();
                    containerInfo.setName(containerName);
                    containerInfo.setStatus("Unknown");
                    containerInfoList.add(containerInfo);
                }
                otsStatus.setContainers(containerInfoList.toArray(new ContainerInfo[0]));
                otsStatus.setVolumeUsed("Unknown");
                otsStatus.setVolumeTotal("Unknown");
            }
            otsStatus.setName(info.getName());
            otsStatus.setHost(info.getHost());
            systemInfoList.add(otsStatus);
        }

        return systemInfoList.toArray(new SystemInfo[0]);
    }

    public RestartResponse restartContainers(){
        RestartResponse r = new RestartResponse();
        for(String containerName : containerNameArr){
            RestartResponse res = dockerService.restartContainer(containerName);
            if (res.getError() != null){
                r.setError(res.getError());
                break;
            }
        }
        return r;
    }

    public RestartResponse restartContainers(String device){
        OtsInfo info = getOtsInfo(device);
        if (info != null){
            ServiceManagerConnector connector = connectorFactory.getConnector(info.getHost(), info.getPort());
            return connector.restartContainers();
        }
        else {
            RestartResponse res = new RestartResponse();
            ErrorInfo err = new ErrorInfo();
            err.setCode(400);
            err.setMessage("Bad Request : Device is not found.");
            res.setError(err);
            return res;
        }
    }

    public void doRestartNow(LoginInfo loginInfo, long millis){
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SshExecutor executor = new SshExecutor();
                int exitValue = executor.execute("hostsystem",
                        22,
                        loginInfo.getId(),
                        loginInfo.getPassword(),
                        "shutdown -r now", new CustomOutputStreamLineHandler() {
                            @Override
                            public boolean processOutputLine(String line) {
                                return true;
                            }

                            @Override
                            public boolean processErrorLine(String line) {
                                return true;
                            }
                        });
            }
        });
        th.start();
    }

    public RestartResponse restartSystem(LoginInfo loginInfo){
        RestartResponse res = new RestartResponse();
        SshExecutor executor = new SshExecutor();
        StringBuilder errStrBuilder = new StringBuilder();
        int exitValue = executor.execute("hostsystem",
                22,
                loginInfo.getId(),
                loginInfo.getPassword(),
                "shutdown -r", new CustomOutputStreamLineHandler() {
                    @Override
                    public boolean processOutputLine(String line) {
                        errStrBuilder.append(line + "\n");
                        return true;
                    }

                    @Override
                    public boolean processErrorLine(String line) {
                        errStrBuilder.append(line + "\n");
                        return true;
                    }
                });
        if (exitValue == 0){
            doRestartNow(loginInfo, 5000);
            return res;
        } else {
            ErrorInfo err = new ErrorInfo();
            if(exitValue == -2){
                err.setCode(500202);
                err.setMessage("ssh connection failed.");
            } else {
                err.setCode(500201);
                err.setMessage(errStrBuilder.toString());
            }
            res.setError(err);
            return res;
        }
    }

    public RestartResponse restartSystem(String device, LoginInfo loginInfo){
        OtsInfo info = getOtsInfo(device);
        if (info != null){
            ServiceManagerConnector connector = connectorFactory.getConnector(info.getHost(), info.getPort());
            return connector.restartSystem(loginInfo);
        }
        else {
            RestartResponse res = new RestartResponse();
            ErrorInfo err = new ErrorInfo();
            err.setCode(400);
            err.setMessage("Bad Request : Device is not found.");
            res.setError(err);
            return res;
        }
    }

}
