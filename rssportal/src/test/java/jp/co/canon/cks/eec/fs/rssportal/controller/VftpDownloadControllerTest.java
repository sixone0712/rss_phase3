package jp.co.canon.cks.eec.fs.rssportal.controller;

import jp.co.canon.ckbs.eec.fs.collect.controller.param.VFtpSssListRequestResponse;
import jp.co.canon.ckbs.eec.fs.collect.service.VFtpFileInfo;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnector;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnectorFactory;
import jp.co.canon.ckbs.eec.fs.manage.service.MachineList;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.Machine;
import jp.co.canon.cks.eec.fs.rssportal.model.DownloadStatusResponseBody;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VftpDownloadControllerTest {

    private Log log = LogFactory.getLog(getClass());

    @Autowired
    private VftpDownloadController controller;

    @Autowired
    private FileServiceManageConnectorFactory connectorFactory;

    @Value("${rssportal.file-service-manager.addr}")
    private String fileServiceAddress;

    @Test
    void requestVFtpCompat1() throws InterruptedException, IOException {
//        log("requestVFtpCompat1");
//
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        request.setServletPath("/api/vftp/compat/download");
//
//        assertNotNull(connectorFactory);
//        assertNotNull(fileServiceAddress);
//        FileServiceManageConnector connector = connectorFactory.getConnector(fileServiceAddress);
//
//        MachineList machines = connector.getMachineList();
//        assertTrue(machines.getMachineCount()>0);
//
//        Map<String, Object> param = new HashMap<>();
//        List<String> machineNames = new ArrayList<>();
//        List<String> fabNames = new ArrayList<>();
//
//        Machine[] _machines = machines.getMachines();
//        for(int i=0; i<_machines.length&&i<4; ++i) {
//            machineNames.add(_machines[i].getMachineName());
//            fabNames.add(_machines[i].getFabName());
//        }
//        param.put("fabNames", fabNames);
//        param.put("machineNames", machineNames);
//        param.put("command", "");
//
//        ResponseEntity response = controller.vftpCompatDownloadRequest(request, param);
//        assertNotNull(response);
//        assertTrue(response.getStatusCode()==HttpStatus.OK);
//
//        Map<String, Object> body = (Map<String, Object>) response.getBody();
//        String downloadId = (String) body.get("downloadId");
//        log.info("downloadId="+downloadId);
//
//        request.setServletPath("/api/vftp/compat/download/"+downloadId);
//        while(true) {
//            Thread.sleep(3000);
//            ResponseEntity<DownloadStatusResponseBody> resp =
//                    (ResponseEntity<DownloadStatusResponseBody>) controller.vftpCompatDownloadStatus(request, downloadId);
//            DownloadStatusResponseBody status = resp.getBody();
//            if(status.getStatus().equals("done")) {
//                log.info("download done");
//                log.info("download url="+status.getDownloadUrl());
//
//                request.setServletPath("/api/vftp/compat/storage/"+downloadId);
//                ResponseEntity entity = controller.vftpCompatDownloadFile(downloadId, request, new MockHttpServletResponse());
//                InputStreamResource isr = (InputStreamResource) entity.getBody();
//                assertNotNull(isr);
//                InputStream is = isr.getInputStream();
//                assertNotNull(is);
//
//                File dir = Paths.get("jtest").toFile();
//                if(!dir.exists()) {
//                    log.info("create jtest directory");
//                    dir.mkdirs();
//                }
//
//                /*int size;
//                File output = Paths.get("jtest", "vftp1_compat.zip").toFile();
//                try(FileOutputStream fos = new FileOutputStream(output)) {
//                    while((size=zis.read(buf))>0) {
//                        fos.write(buf, 0, size);
//                    }
//                }*/
//
//                break;
//            }
//            log.info("downloading... ");
//        }
    }

    @Test
    void vftpCompat2() throws Exception {
//        log("vftpCompat2");
//
//        assertNotNull(connectorFactory);
//        FileServiceManageConnector connector = connectorFactory.getConnector(fileServiceAddress);
//        assertNotNull(connector);
//
//        final int MACHINES = 1;
//
//        MachineList machines = connector.getMachineList();
//        assertNotNull(machines);
//        assertTrue(machines.getMachineCount()>=MACHINES);
//
//        List<String> machineList = new ArrayList<>();
//        List<String> fabList = new ArrayList<>();
//        String command = "";
//
//        for(int i=0; i<MACHINES; ++i) {
//            Machine machine = machines.getMachines()[i];
//            machineList.add(machine.getMachineName());
//            fabList.add(machine.getFabName());
//        }
//
//        MockHttpServletRequest request = new MockHttpServletRequest();
//
//        {
//            request.setServletPath("/compat/download");
//            ResponseEntity response = controller.vftpCompatDownloadRequest(request, null);
//            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//        }
//        {
//            request.setServletPath("/compat/download");
//            ResponseEntity response = controller.vftpCompatDownloadRequest(request, new HashMap<>());
//            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//        }
//        {
//            request.setServletPath("/compat/download");
//
//            Map<String, Object> param = new HashMap<>();
//            param.put("fabNames", fabList);
//            param.put("machineNames", machineList);
//            param.put("command", command);
//
//            ResponseEntity response = controller.vftpCompatDownloadRequest(request, param);
//            assertEquals(HttpStatus.OK, response.getStatusCode());
//            String downloadId = (String) getParam(response, "downloadId");
//            assertNotNull(downloadId);
//
//            request.setServletPath("/compat/storage/"+downloadId);
//            response = controller.vftpCompatDownloadFile(downloadId, request, new MockHttpServletResponse());
//            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//
//            request.setServletPath("/compat/download/"+downloadId);
//            ResponseEntity response2 = controller.vftpCompatDownloadCancel(request, downloadId);
//            assertEquals(HttpStatus.OK, response2.getStatusCode());
//
//            request.setServletPath("/compat/download/null");
//            response2 = controller.vftpCompatDownloadCancel(request, null);
//            assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());
//
//            response2 = controller.vftpCompatDownloadStatus(request, null);
//            assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());
//
//            request.setServletPath("/compat/download/:)");
//            response2 = controller.vftpCompatDownloadCancel(request, ":)");
//            assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());
//
//            response2 = controller.vftpCompatDownloadStatus(request, ":)");
//            assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());
//        }
//        {
//            request.setServletPath("/compat/storage/null");
//            ResponseEntity response = controller.vftpCompatDownloadFile(null, request, new MockHttpServletResponse());
//            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//
//            request.setServletPath("/compat/storage/:)");
//            response = controller.vftpCompatDownloadFile(":)", request, new MockHttpServletResponse());
//            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//        }
    }

    @Test
    void requestVFtpSss1() throws InterruptedException {
//        log.info("requestVFtpSss1");
//
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        request.setServletPath("/api/vftp/sss/download");
//
//        assertNotNull(connectorFactory);
//        assertNotNull(fileServiceAddress);
//        FileServiceManageConnector connector = connectorFactory.getConnector(fileServiceAddress);
//
//        MachineList machines = connector.getMachineList();
//        assertTrue(machines.getMachineCount()>0);
//
//        List<Map> lists = new ArrayList<>();
//
//        Machine[] _machines = machines.getMachines();
//        for(int i=0; i<_machines.length&&i<4; ++i) {
//            String machine = _machines[i].getMachineName();
//            String fab = _machines[i].getFabName();
//            VFtpSssListRequestResponse listResponse = connector.createVFtpSssListRequest(machine, "IP_AS_RAW_ERR-DE_TEST_PR_2nd");
//            VFtpFileInfo[] files = listResponse.getRequest().getFileList();
//            for(VFtpFileInfo file: files) {
//                Map<String, Object> f = new HashMap<>();
//                f.put("fabName", fab);
//                f.put("machineName", machine);
//                f.put("command", "IP_AS_RAW_ERR-DE_TEST_PR_2nd");
//                f.put("fileName", file.getFileName());
//                f.put("fileSize", (int)file.getFileSize());
//                lists.add(f);
//            }
//        }
//
//        Map<String, Object> param = new HashMap<>();
//        param.put("lists", lists);
//
//        ResponseEntity response = controller.vftpSSSDownloadRequest(request, param);
//        assertNotNull(response);
//        assertTrue(response.getStatusCode()==HttpStatus.OK);
//
//        Map<String, Object> body = (Map<String, Object>) response.getBody();
//        String downloadId = (String) body.get("downloadId");
//        log.info("downloadId="+downloadId);
//
//        request.setServletPath("/api/vftp/sss/download/"+downloadId);
//        while(true) {
//            Thread.sleep(3000);
//            ResponseEntity<DownloadStatusResponseBody> resp =
//                    (ResponseEntity<DownloadStatusResponseBody>) controller.vftpCompatDownloadStatus(request, downloadId);
//            DownloadStatusResponseBody status = resp.getBody();
//            if(status.getStatus().equals("done")) {
//                log.info("download done");
//                log.info("download url="+status.getDownloadUrl());
//                break;
//            }
//            log.info("downloading... ");
//        }
    }

    @Test
    void vftpSss2() throws Exception {
//        log("vftpSss2");
//
//        assertNotNull(connectorFactory);
//        FileServiceManageConnector connector = connectorFactory.getConnector(fileServiceAddress);
//        assertNotNull(connector);
//
//        MockHttpServletRequest request = new MockHttpServletRequest();
//
//        final int MACHINES = 1;
//
//        MachineList machines = connector.getMachineList();
//        assertNotNull(machines);
//        assertTrue(machines.getMachineCount()>=MACHINES);
//
//        List<String> machineList = new ArrayList<>();
//        List<String> fabList = new ArrayList<>();
//        String command = "";
//
//        for(int i=0; i<MACHINES; ++i) {
//            Machine machine = machines.getMachines()[i];
//            machineList.add(machine.getMachineName());
//            fabList.add(machine.getFabName());
//        }
//
//        {
//            request.setServletPath("/sss");
//            ResponseEntity entity = controller.searchVftpSSSFileList(request, null);
//            assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
//
//            entity = controller.searchVftpSSSFileList(request, new HashMap<>());
//            assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
//        }
//        {
//            request.setServletPath("/sss");
//            Map<String, Object> param = new HashMap<>();
//            List<String> _machine = new ArrayList<>();
//            List<String> _fab = new ArrayList<>();
//            _machine.add("machine:)");
//            _fab.add("fab:)"); _fab.add("fab=)");
//            param.put("machineNames", _machine);
//            param.put("fabNames", _fab);
//            param.put("command", command);
//            ResponseEntity entity = controller.searchVftpSSSFileList(request, param);
//            assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
//        }
//        {
//            request.setServletPath("/sss");
//            Map<String, Object> param = new HashMap<>();
//            param.put("machineNames", machineList);
//            param.put("fabNames", fabList);
//            param.put("command", command);
//            ResponseEntity entity = controller.searchVftpSSSFileList(request, param);
//            assertEquals(HttpStatus.OK, entity.getStatusCode());
//        }
    }

    private Object getParam(ResponseEntity entity, String key) {
        Map<String, Object> body = (Map<String, Object>) entity.getBody();
        assertNotNull(body);
        return body.get(key);
    }

    private void log(String str) {
        log.info("===============> "+str);
    }
}