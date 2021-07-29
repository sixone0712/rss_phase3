package jp.co.canon.ckbs.eec.fs.manage.service;

import jp.co.canon.ckbs.eec.fs.collect.FileServiceCollectConnector;
import jp.co.canon.ckbs.eec.fs.collect.FileServiceCollectConnectorFactory;
import jp.co.canon.ckbs.eec.fs.collect.controller.param.MachineStatusRequestResponse;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MachineService {

    @Autowired
    ConfigurationService configurationService;

    @Autowired
    FileServiceCollectConnectorFactory connectorFactory;

    public MachineStatusRequestResponse getMachineStatus(String machine) throws FileServiceManageException {
        String host = configurationService.getFileServiceHost(machine);
        if(host==null) {
            log.error("cannot find the machine {} host", machine);
            throw new FileServiceManageException(400, "unknown machine name");
        }

        MachineStatusRequestResponse response;

        if(!isOtsServiceOnByHost(host)) {
            response = new MachineStatusRequestResponse();
            response.setMachine(machine);
            response.setFtpStatus("disconnected");
            response.setVFtpStatus("disconnected");
            return response;
        }

        FileServiceCollectConnector connector = connectorFactory.getConnector(host);
        response = connector.getMachineStatus(machine);

        if(response==null) {
            response = new MachineStatusRequestResponse();
            response.setErrorCode(400);
            response.setErrorMessage("failed to get machine status");
            return response;
        }

        return response;
    }

    public boolean isOtsServiceOnByHost(String host) {
        if(host==null || host.isEmpty()) {
            return false;
        }
        FileServiceCollectConnector connector = connectorFactory.getConnector(host);
        return connector.isServiceOn();
    }

    public boolean isOtsServiceOn(String ots) {
        String host = configurationService.getOtsServiceHost(ots);
        if(host==null) {
            return false;
        }
        FileServiceCollectConnector connector = connectorFactory.getConnector(host);
        return connector.isServiceOn();
    }

}
