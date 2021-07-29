package jp.co.canon.ckbs.eec.servicemanager.service;

import jp.co.canon.ckbs.eec.servicemanager.controller.ErrorInfo;
import jp.co.canon.ckbs.eec.servicemanager.controller.RestartResponse;
import jp.co.canon.ckbs.eec.servicemanager.executor.CustomExecutor;
import jp.co.canon.ckbs.eec.servicemanager.executor.CustomOutputStreamLineHandler;
import org.apache.commons.exec.CommandLine;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DockerService {
    public ContainerInfo[] getContainers(){
        CustomExecutor executor = new CustomExecutor();
        CommandLine cmdLine = new CommandLine("docker")
                .addArgument("container")
                .addArgument("ls")
                .addArgument("-a")
                .addArgument("--format")
                .addArgument("\"{{.Names}}===={{.Status}}\"");

        List<ContainerInfo> containerInfoList = new ArrayList<>();

        int exitValue = executor.execute(cmdLine, new CustomOutputStreamLineHandler() {
            @Override
            public boolean processOutputLine(String line) {
                String[] result = line.split("====");
                if (result.length == 2){
                    ContainerInfo info = new ContainerInfo();
                    info.setName(result[0]);
                    info.setStatus(result[1]);
                    containerInfoList.add(info);
                }
                return true;
            }

            @Override
            public boolean processErrorLine(String line) {
                return true;
            }
        });
        return containerInfoList.toArray(new ContainerInfo[0]);
    }

    public RestartResponse restartContainer(String name){
        RestartResponse res = new RestartResponse();
        CustomExecutor executor = new CustomExecutor();
        CommandLine cmdLine = new CommandLine("docker")
                .addArgument("container")
                .addArgument("restart")
                .addArgument(name);

        StringBuilder errMsgBuilder = new StringBuilder();

        int exitValue = executor.execute(cmdLine, new CustomOutputStreamLineHandler() {
            @Override
            public boolean processOutputLine(String line) {
                return true;
            }

            @Override
            public boolean processErrorLine(String line) {
                errMsgBuilder.append(line);
                return true;
            }
        });
        if (exitValue != 0){
            String dockerErrorMsg = errMsgBuilder.toString();
            ErrorInfo errorInfo = new ErrorInfo();
            if (dockerErrorMsg.contains("No such container")){
                errorInfo.setCode(500101);
            } else {
                errorInfo.setCode(500102);
            }
            errorInfo.setMessage(dockerErrorMsg);
            res.setError(errorInfo);
        }
        return res;
    }
}
