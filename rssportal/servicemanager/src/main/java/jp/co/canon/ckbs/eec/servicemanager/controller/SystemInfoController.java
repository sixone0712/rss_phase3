package jp.co.canon.ckbs.eec.servicemanager.controller;

import jp.co.canon.ckbs.eec.servicemanager.service.LoginInfo;
import jp.co.canon.ckbs.eec.servicemanager.service.SystemInfo;
import jp.co.canon.ckbs.eec.servicemanager.service.SystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class SystemInfoController {
    @Autowired
    SystemService systemService;

    @Value("${servicemanager.type}")
    String systemType;

    @GetMapping(value="/api/system")
    ResponseEntity<?> getSystemInfo(){
        if (systemType.equals("ESP")){
            SystemInfo[] infoArr = systemService.getSystemInfoList();
            SystemInfoListResponse res = new SystemInfoListResponse();
            res.setList(infoArr);
            return ResponseEntity.ok(res);
        }
        else {
            SystemInfo info = systemService.getSystemInfo();
            return ResponseEntity.ok(info);
        }
    }

    @PostMapping(value="/api/docker/restart")
    ResponseEntity<?> restartSystemContainers(@RequestParam(required = false) String device){
        RestartResponse res = null;
        if (device == null || device.equals(systemType)) {
            res = systemService.restartContainers();
        } else {
            res = systemService.restartContainers(device);
        }
        if (res.getError() == null){
            return ResponseEntity.ok(res);
        }
        else {
            return ResponseEntity.status(res.getError().code / 1000).body(res);
        }
    }

    @PostMapping(value="/api/os/restart")
    ResponseEntity<?> restartSystem(@RequestParam(required = false) String device, @RequestBody LoginInfo loginInfo){
        RestartResponse res = null;
        if (device == null || device.equals(systemType)) {
            res = systemService.restartSystem(loginInfo);
        } else {
            res = systemService.restartSystem(device, loginInfo);
        }
        if (res.getError() == null){
            log.info("/api/os/restart successed");
            return ResponseEntity.ok(res);
        }
        else {
            log.error("/api/os/restart return error, {}", res.getError().getMessage());
            return ResponseEntity.status(res.getError().code / 1000).body(res);
        }
    }
}
