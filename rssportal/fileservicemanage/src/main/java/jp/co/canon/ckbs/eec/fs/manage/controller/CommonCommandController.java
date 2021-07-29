package jp.co.canon.ckbs.eec.fs.manage.controller;

import jp.co.canon.ckbs.eec.fs.collect.controller.param.MachineStatusRequestResponse;
import jp.co.canon.ckbs.eec.fs.manage.service.MachineService;
import jp.co.canon.ckbs.eec.fs.manage.service.FileServiceManageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class CommonCommandController {

    @Autowired
    private MachineService service;

    @GetMapping("/common/machine/{machine}")
    ResponseEntity<MachineStatusRequestResponse> getMachineStatus(@PathVariable String machine) {
        MachineStatusRequestResponse response;

        try {
            response = service.getMachineStatus(machine);
            return ResponseEntity.ok(response);
        } catch (FileServiceManageException e) {
            log.error("failed to get machine {} status (error={})", machine, e.getCode());
            response = new MachineStatusRequestResponse();
            response.setErrorCode(e.getCode());
            response.setErrorMessage("FileServiceManagerException occurs");
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/common/ots/{ots}")
    ResponseEntity<MachineStatusRequestResponse> getOtsStatus(@PathVariable String ots) {
        MachineStatusRequestResponse response = new MachineStatusRequestResponse();
        response.setOts(ots);
        String status = service.isOtsServiceOn(ots)?"connected":"disconnected";
        response.setFtpStatus(status);
        response.setVFtpStatus(status);
        return ResponseEntity.ok(response);
    }


}
