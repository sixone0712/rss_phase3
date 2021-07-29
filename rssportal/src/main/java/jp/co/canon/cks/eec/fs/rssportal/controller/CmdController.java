package jp.co.canon.cks.eec.fs.rssportal.controller;

import jp.co.canon.cks.eec.fs.rssportal.Defines.RSSErrorReason;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import jp.co.canon.cks.eec.fs.rssportal.model.error.RSSError;
import jp.co.canon.cks.eec.fs.rssportal.model.vftp.VFtpCmdResponse;
import jp.co.canon.cks.eec.fs.rssportal.service.CommandService;
import jp.co.canon.cks.eec.fs.rssportal.vo.CommandVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/vftp/command")
public class CmdController {
    @Autowired
    CommandService serviceCmd;

    private final EspLog log = new EspLog(getClass());

    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getCmdList(HttpServletRequest request,
                                        @RequestParam(name="type", required = false, defaultValue = "") String type) throws Exception {
        log.info(String.format("[Get] %s", request.getServletPath()));
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();
        List<CommandVo> list = null;
        if(type == null || type.isEmpty()) list = serviceCmd.getCommandListAll();
        else list = serviceCmd.getCommandList(type);

        if(list == null) {
            error.setReason(RSSErrorReason.INTERNAL_ERROR);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resBody);
        }

        List<VFtpCmdResponse> response = new ArrayList<VFtpCmdResponse>();
        SimpleDateFormat conTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        for(CommandVo item : list) {
            VFtpCmdResponse cmd = new VFtpCmdResponse();
            cmd.setId(item.getId());
            cmd.setCmd_name(item.getCmd_name());
            cmd.setCmd_type(item.getCmd_type());
            cmd.setCreated(conTimeFormat.format(item.getCreated()));
            cmd.setModified(conTimeFormat.format(item.getModified()));
            response.add(cmd);
        }

        resBody.put("lists", response);
        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> addCmd(HttpServletRequest request,
                                    @RequestBody Map<String, Object> param)  throws Exception {
        log.info(String.format("[Post] %s", request.getServletPath()));
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        String type = param.containsKey("cmd_type") ? (String)param.get("cmd_type") : null;
        String name = param.containsKey("cmd_name") ? (String)param.get("cmd_name") : null;

        if(type == null || type.isEmpty() || name == null || name.isEmpty())
        {
            error.setReason(RSSErrorReason.INVALID_PARAMETER);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resBody);
        }

        if(!type.equals("vftp_compat") && !type.equals("vftp_sss")) {
            error.setReason(RSSErrorReason.INVALID_PARAMETER);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resBody);
        }

        CommandVo findCommand = serviceCmd.findCommand(name, type);
        if(findCommand != null) {
            error.setReason(RSSErrorReason.DUPLICATE_USER);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resBody);
        }

        CommandVo newCommand = new CommandVo();
        newCommand.setCmd_type(type);
        newCommand.setCmd_name(name);
        int newCommandId = serviceCmd.addCmd(newCommand);
        if(newCommandId == -1) {
            error.setReason(RSSErrorReason.INTERNAL_ERROR);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resBody);
        }

        resBody.put("id", newCommandId);
        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteCmd(HttpServletRequest request,
                                       @PathVariable("id") String id) {
        log.info(String.format("[Delete] %s", request.getServletPath()));
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        if(id == null || id.isEmpty())
        {
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        int commandId = Integer.parseInt(id);
        CommandVo findCommand = serviceCmd.getCommand(commandId);
        if(findCommand == null) {
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        if(!serviceCmd.deleteCmd(commandId)) {
            error.setReason(RSSErrorReason.INTERNAL_ERROR);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resBody);
        }

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> modifyCmd(HttpServletRequest request,
                                       @PathVariable("id") String id,
                                       @RequestBody Map<String, Object> param) {
        log.info(String.format("[Put] %s", request.getServletPath()));
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        if(id == null || id.isEmpty()) {
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        if(param == null) {
            log.error("no param");
            error.setReason(RSSErrorReason.INVALID_PARAMETER);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resBody);
        }

        String cmdName = param.containsKey("cmd_name") ? (String) param.get("cmd_name") : null;

        if(cmdName == null || cmdName.isEmpty()) {
            error.setReason(RSSErrorReason.INVALID_PARAMETER);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resBody);
        }

        int commandId = Integer.parseInt(id);
        CommandVo findCommand = serviceCmd.getCommand(commandId);
        if(findCommand == null) {
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        findCommand.setCmd_name(cmdName);
        if(!serviceCmd.modifyCmd(findCommand)) {
            error.setReason(RSSErrorReason.INTERNAL_ERROR);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resBody);
        }

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
