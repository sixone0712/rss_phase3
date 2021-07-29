package jp.co.canon.ckbs.eec.fs.collect.controller;

import jp.co.canon.ckbs.eec.fs.collect.controller.param.FscMachineStatusRequestParam;
import jp.co.canon.ckbs.eec.fs.collect.controller.param.MachineStatusRequestResponse;
import jp.co.canon.ckbs.eec.fs.collect.service.XFtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class CommonCommandController {

    @Autowired
    XFtpService commonService;

    @GetMapping(value = {"/common", "/common/hello"})
    ResponseEntity hello() {
        //log.info("GET /fsc/common/hello");
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/common/status")
    ResponseEntity<MachineStatusRequestResponse> getMachineStatus(@RequestBody FscMachineStatusRequestParam param) {
        //log.info("POST /fsc/commom/status");
        MachineStatusRequestResponse response = commonService.getMachineStatus(param);

        if(response==null) {
            response = new MachineStatusRequestResponse();
            response.setMachine(param.getMachine());
            response.setErrorCode(400);
            response.setErrorMessage("failed to get status");
            log.error("failed to get status of {}", param.getMachine());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.ok(response);
    }
}
