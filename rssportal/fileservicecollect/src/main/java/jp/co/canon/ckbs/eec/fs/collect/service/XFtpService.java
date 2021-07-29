package jp.co.canon.ckbs.eec.fs.collect.service;

import jp.co.canon.ckbs.eec.fs.collect.controller.param.FscMachineStatusRequestParam;
import jp.co.canon.ckbs.eec.fs.collect.controller.param.MachineStatusRequestResponse;
import jp.co.canon.ckbs.eec.service.CheckMachineCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class XFtpService {

    public MachineStatusRequestResponse getMachineStatus(FscMachineStatusRequestParam request) {

        //log.info("getMachineStatus {}", request.getMachine());
        CheckMachineCommand ftpChecker = new CheckMachineCommand(request.getHost(), 21, "passive",
                request.getFtpUser(), request.getFtpPassword());

        CheckMachineCommand vftpChecker = new CheckMachineCommand(request.getHost(), 22001, "passive",
                request.getVFtpUser(), request.getVFtpPassword());

        ftpChecker.execute();
        vftpChecker.execute();

        MachineStatusRequestResponse response = new MachineStatusRequestResponse();
        response.setMachine(request.getMachine());
        response.setFtpStatus(isMachineAlive(ftpChecker)?"connected":"disconnected");
        response.setVFtpStatus(isMachineAlive(vftpChecker)?"connected":"disconnected");
        return response;
    }

    private boolean isMachineAlive(CheckMachineCommand... checkers) {
        for(CheckMachineCommand checker: checkers) {
            if(!checker.isStatusOk(true)) {
                return false;
            }
        }
        return true;
    }

}
