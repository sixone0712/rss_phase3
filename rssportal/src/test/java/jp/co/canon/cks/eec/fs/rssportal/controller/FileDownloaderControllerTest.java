package jp.co.canon.cks.eec.fs.rssportal.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jp.co.canon.ckbs.eec.fs.collect.controller.param.FtpDownloadRequestResponse;
import jp.co.canon.ckbs.eec.fs.collect.service.FileInfo;
import jp.co.canon.ckbs.eec.fs.collect.service.LogFileList;
import jp.co.canon.ckbs.eec.fs.configuration.Category;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnector;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnectorFactory;
import jp.co.canon.ckbs.eec.fs.manage.service.CategoryList;
import jp.co.canon.ckbs.eec.fs.manage.service.MachineList;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.Machine;
import jp.co.canon.cks.eec.fs.rssportal.model.*;
import jp.co.canon.cks.eec.fs.rssportal.model.auth.AccessToken;
import jp.co.canon.cks.eec.fs.rssportal.model.auth.RefreshToken;
import jp.co.canon.cks.eec.fs.rssportal.service.JwtService;
import jp.co.canon.cks.eec.fs.rssportal.session.SessionContext;
import jp.co.canon.cks.eec.fs.rssportal.vo.UserVo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileDownloaderControllerTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final FileDownloaderController downloadController;
    private final FileServiceController fileServiceController;
    private final FileServiceManageConnectorFactory connectorFactory;
    private FileServiceManageConnector connector;

    @Value("${rssportal.file-service-manager.addr}")
    private String fileServiceAddress;

    @Autowired
    public FileDownloaderControllerTest(FileDownloaderController downloadController,
                                        FileServiceController fileServiceController,
                                        FileServiceManageConnectorFactory connectorFactory) {
        this.downloadController = downloadController;
        this.fileServiceController = fileServiceController;
        this.connectorFactory = connectorFactory;
    }

    @PostConstruct
    void __init() {
        assertNotNull(connectorFactory);
        connector = connectorFactory.getConnector(fileServiceAddress);
        assertNotNull(connector);
    }

    @Test
    @Timeout(300)
    void request() throws Exception {

        printSeparator("request");

        String downloadId = requestDownload();
        assertNotNull(downloadId);
        log.info("downloadId="+downloadId);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setServletPath("download/"+downloadId);
        {
            while(true) {
                ResponseEntity _response = downloadController.ftpDownloadStatus(request, downloadId);
                assertNotNull(_response);
                assertEquals(HttpStatus.OK, _response.getStatusCode());
                DownloadStatusResponseBody body = (DownloadStatusResponseBody) _response.getBody();
                log.info("status="+body.getStatus()+" size="+body.getDownloadSize()+"/"+body.getTotalSize());
                assertNotNull(body);
                if(body.getStatus().equalsIgnoreCase("done"))
                    break;
                log.info("waiting..."+body.getStatus());
                Thread.sleep(2000);
            }
            printSeparator("download done");
        }

        Field field = DownloadControllerCommon.class.getDeclaredField("jwtService");
        assertNotNull(field);
        field.setAccessible(true);
        field.set(downloadController, dummyJwtService);

        request.setServletPath("storage/"+downloadId);
        ResponseEntity responseEntity = downloadController.ftpDownloadFile(downloadId, "123", request, response);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    JwtService dummyJwtService = new JwtService() {

        AccessToken accessToken;

        @Override
        public <T> String create(T data, String subject) {
            return null;
        }

        @Override
        public String getCurrentAccessToken() {
            return null;
        }

        @Override
        public String getCurAccTokenUserName() {
            return "user";
        }

        @Override
        public int getCurAccTokenUserID() {
            return 0;
        }

        @Override
        public String getCurAccTokenUserPermission() {
            return null;
        }

        @Override
        public AccessToken decodeAccessToken(String jwt) {
            accessToken = new AccessToken();
            accessToken.setUserName("ted");
            return accessToken;
        }

        @Override
        public RefreshToken decodeRefreshToken(String jwt) {
            return null;
        }

        @Override
        public boolean isUsable(String jwt) {
            return false;
        }
    };



    @Test
    void getStatus() {
        printSeparator("getStatus");
        // normal operation will be tested in request() testing.
        MockHttpServletRequest request = new MockHttpServletRequest();
        {
            request.setServletPath("download/123123");
            ResponseEntity _response = downloadController.ftpDownloadStatus(request, "123123");
            assertEquals(HttpStatus.NOT_FOUND, _response.getStatusCode());
        }

        {
            request.setServletPath("download/null");
            ResponseEntity _response = downloadController.ftpDownloadStatus(request, null);
            assertEquals(HttpStatus.NOT_FOUND, _response.getStatusCode());
        }
    }

    @Test
    void downloadFile() {
        printSeparator("downloadFile");
        // this method will be tested in request() testing.
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        {
            request.setServletPath("storage/null");
            ResponseEntity resp = downloadController.ftpDownloadFile(null, "123", request, response);
            assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        }

        {
            request.setServletPath("storage/123123");
            ResponseEntity resp = downloadController.ftpDownloadFile("123123", "123",request, response);
            assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        }
    }

    @Test
    void cancelDownload() {
        printSeparator("cancelDownload");
        MockHttpServletRequest request = new MockHttpServletRequest();
        {
            request.setServletPath("cancel/123123");
            ResponseEntity response = downloadController.ftpDownloadCancel(request, "123123");
            assertNotNull(response);
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        {
            request.setServletPath("cancel/null");
            ResponseEntity response = downloadController.ftpDownloadCancel(request, null);
            assertNotNull(response);
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

    }

    @Test
    void search() throws Exception {
        printSeparator("search");
        MockHttpServletRequest requset = new MockHttpServletRequest();
        requset.setServletPath("/api/ftp");

        {
            ResponseEntity response = downloadController.searchFTPFileListWithThreadPool(requset, null);
            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
        {
            ResponseEntity response = downloadController.searchFTPFileListWithThreadPool(requset, new HashMap<>());
            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
        {
            Map<String, Object> map = new HashMap<>();
            List<String> fabs = new ArrayList<>();
            List<String> machines = new ArrayList<>();
            fabs.add("a"); fabs.add("b");
            machines.add("c");
            ResponseEntity response = downloadController.searchFTPFileListWithThreadPool(requset, map);
            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
        {
            Map<String, Object> map2 = new HashMap<>();

            List<String> fabNames = new ArrayList<>();
            List<String> machineNames = new ArrayList<>();
            Machine machine = connector.getMachineList().getMachines()[0];
            assertNotNull(machine);
            fabNames.add(machine.getLine());
            machineNames.add(machine.getMachineName());
            map2.put("fabNames", fabNames);
            map2.put("machineNames", machineNames);

            List<String> categoryCodes = new ArrayList<>();
            List<String> categoryNames = new ArrayList<>();
            Category category = connector.getCategoryList().getCategories()[0];
            assertNotNull(category);
            categoryCodes.add(category.getCategoryCode());
            categoryNames.add(category.getCategoryName());
            map2.put("categoryCodes", categoryCodes);
            map2.put("categoryNames", categoryNames);

            long cur = System.currentTimeMillis();
            long week = 7*24*3600000;
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            map2.put("startDate", format.format(new Date(cur)));
            map2.put("endDate", format.format(new Date(cur-week)));

            ResponseEntity response = downloadController.searchFTPFileListWithThreadPool(requset, map2);
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    private String requestDownload() throws Exception {

        assertNotNull(connector);

        MachineList machineList = connector.getMachineList();
        assertNotNull(machineList);

        CategoryList categoryList = connector.getCategoryList();
        assertNotNull(categoryList);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        long endMillis = System.currentTimeMillis();
        long startMillis = endMillis-(24*3600000);
        String end = dateFormat.format(endMillis);
        String start = dateFormat.format(startMillis);

        class _Param {
            String fabName;
            String machineName;
            String categoryCode;
            String categoryName;
            String fileName;
            int fileSize;
            String fileDate;
        }

        List<_Param> paramList = new ArrayList<>();

        ___machine_loop:
        for(int i=0; i<1 && i<machineList.getMachineCount(); ++i) {
            Machine machine = machineList.getMachines()[i];
            assertNotNull(machine);
            log.info("requestDownload machine="+machine.getMachineName());
            ___category_loop:
            for(int j=1; j<5 && j<categoryList.getCategories().length; ++j) {
                Category category = categoryList.getCategories()[j];
                assertNotNull(category);
                log.info("requestDownload category="+category.getCategoryName());
                LogFileList fileList = connector.getFtpFileList(machine.getMachineName(), category.getCategoryCode(),
                        start, end, null, null);
                assertNull(fileList.getErrorMessage());
                for(FileInfo file: fileList.getList()) {
                    if(file.getType().equalsIgnoreCase("D"))
                        continue ___category_loop;
                    _Param _param = new _Param();
                    _param.fabName = machine.getLine();
                    _param.machineName = machine.getMachineName();
                    _param.categoryCode = category.getCategoryCode();
                    _param.categoryName = category.getCategoryName();
                    _param.fileName = file.getFilename();
                    _param.fileSize = (int)file.getSize();
                    _param.fileDate = file.getTimestamp();
                    paramList.add(_param);
                }
                if(paramList.size()>0)
                    break ___machine_loop;
            }
        }

        assertTrue(paramList.size()>0);
        Map<String, Object> param = new HashMap<>();
        List<Map> lists = new ArrayList<>();
        for(_Param p: paramList) {
            Map<String, Object> m = new HashMap<>();
            m.put("fabName", p.fabName);
            m.put("machineName", p.machineName);
            m.put("categoryCode", p.categoryCode);
            m.put("categoryName", p.categoryName);
            m.put("fileName", p.fileName);
            m.put("fileSize", p.fileSize);
            m.put("fileDate", p.fileDate);
            lists.add(m);
        }

        param.put("lists", lists);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("download");
        ResponseEntity response = downloadController.ftpDownloadRequest(request, param);
        assertNotNull(response);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        String downloadId = (String) getResponseBody(response, "downloadId");
        assertNotNull(downloadId);
        return downloadId;
    }

    private Object getResponseBody(ResponseEntity response, String paramName) {
        assertNotNull(response);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(body.containsKey(paramName));
        return body.get(paramName);
    }

    private void printSeparator(String name) {
        log.info("-------------------- "+name+" --------------------");
    }
}