package jp.co.canon.cks.eec.fs.rssportal.background;

import jp.co.canon.ckbs.eec.fs.collect.controller.param.FtpDownloadRequestListResponse;
import jp.co.canon.ckbs.eec.fs.collect.controller.param.FtpDownloadRequestResponse;
import jp.co.canon.ckbs.eec.fs.collect.controller.param.VFtpSssDownloadRequestResponse;
import jp.co.canon.ckbs.eec.fs.collect.controller.param.VFtpSssListRequestResponse;
import jp.co.canon.ckbs.eec.fs.collect.model.FtpDownloadRequest;
import jp.co.canon.ckbs.eec.fs.collect.model.VFtpSssDownloadRequest;
import jp.co.canon.ckbs.eec.fs.collect.model.VFtpSssListRequest;
import jp.co.canon.ckbs.eec.fs.collect.service.FileInfo;
import jp.co.canon.ckbs.eec.fs.collect.service.LogFileList;
import jp.co.canon.ckbs.eec.fs.collect.service.VFtpFileInfo;
import jp.co.canon.ckbs.eec.fs.configuration.Category;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnector;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnectorFactory;
import jp.co.canon.ckbs.eec.fs.manage.service.CategoryList;
import jp.co.canon.ckbs.eec.fs.manage.service.MachineList;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.Machine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class FileDownloadExecutorTest {

    private Log log = LogFactory.getLog(getClass());

    @Autowired
    private FileDownloader downloader;

    @Autowired
    private FileServiceManageConnectorFactory connectorFactory;

    @Value("${rssportal.file-service-manager.addr}")
    private String fileServiceAddress;

    @Test
    void getMaxThreadsTest() {
        log.info("downloader maxThread="+downloader.getMaxThreads());
    }

    @Test
    @Timeout(360)
    void vFtpCompatBasicTest() throws InterruptedException {
//        assertNotNull(connectorFactory);
//        assertNotNull(fileServiceAddress);
//        FileServiceManageConnector connector = connectorFactory.getConnector(fileServiceAddress);
//
//        MachineList machines = connector.getMachineList();
//        assertTrue(machines.getMachineCount()>0);
//        Machine machine = machines.getMachines()[0];
//        assertNotNull(machine);
//
//        List list = new ArrayList();
//        list.add(new VFtpCompatDownloadRequestForm(machine.getFabName(), machine.getMachineName(),
//                "20200810_000000_20200810_154807", false));
//        FileDownloadExecutor e = new FileDownloadExecutor("vftp_compat", "test", downloader, list, false);
//        e.start();
//
//        log.info("waiting");
//        while(!e.getStatus().equals("complete")) {
//            Thread.sleep(1000);
//        }
//        log.info("download complete");
//        log.info("download path="+e.getDownloadPath());
    }

    @Test
    @Timeout(360)
    void vFtpSssBasicTest() throws InterruptedException {
        log.info("vFtpSssBasicTest: start");
        assertNotNull(connectorFactory);
        FileServiceManageConnector connector = connectorFactory.getConnector(fileServiceAddress);
        MachineList machines = connector.getMachineList();
        assertNotNull(machines);
        assertTrue(machines.getMachineCount()>0);
        Machine machine = machines.getMachines()[0];
        log.info("vFtpSssBasicTest: machine="+machine.getMachineName());

        final String directory = "aabbccdd";

        VFtpSssListRequestResponse fileResponse = connector.createVFtpSssListRequest(machine.getMachineName(), directory);
        assertNotNull(fileResponse);
        assertTrue(fileResponse.getErrorMessage()==null);
        VFtpFileInfo[] files = fileResponse.getRequest().getFileList();
        assertTrue(files.length>0);
        String[] fileNames = new String[files.length];
        for(int i=0; i<files.length; ++i) {
            fileNames[i] = files[i].getFileName();
        }

        log.info("vFtpSssBasicTest: request files="+fileNames.length);
        VFtpSssDownloadRequestResponse downloadResponse =
                connector.createVFtpSssDownloadRequest(machine.getMachineName(), directory, fileNames, true);
        assertNotNull(downloadResponse);
        assertTrue(downloadResponse.getErrorMessage()==null);
        VFtpSssDownloadRequest request = downloadResponse.getRequest();
        log.info("vFtpSssBasicTest: requestNo="+request.getRequestNo());

        log.info("vFtpSssBasicTest: waiting");
        while (true) {
            Thread.sleep(3000);

            VFtpSssDownloadRequestResponse listResponse =
                    connector.getVFtpSssDownloadRequest(machine.getMachineName(), request.getRequestNo());
            assertNotNull(listResponse);
            assertTrue(listResponse.getErrorMessage()==null);
            VFtpSssDownloadRequest downloadRequest = listResponse.getRequest();
            assertTrue(downloadRequest.getRequestNo().equals(request.getRequestNo()));

            if(downloadRequest.getStatus()==VFtpSssDownloadRequest.Status.ERROR) {
                log.info("vFtpSssBasicTest: download error");
                break;
            }

            if(downloadRequest.getStatus()==VFtpSssDownloadRequest.Status.EXECUTED) {
                log.info("vFtpSssBasicTest: downloaded achieve=" + downloadRequest.getArchiveFilePath());
                break;
            }
        }

    }

    @Test
    @Timeout(360)
    void vFtpSssBasicTest2() throws InterruptedException {
//        log.info("vFtpSssBasicTest2: start");
//        assertNotNull(connectorFactory);
//        FileServiceManageConnector connector = connectorFactory.getConnector(fileServiceAddress);
//        MachineList machines = connector.getMachineList();
//        assertNotNull(machines);
//        assertTrue(machines.getMachineCount()>0);
//        Machine machine = machines.getMachines()[0];
//        log.info("vFtpSssBasicTest2: machine="+machine.getMachineName());
//
//        final String directory = "aabbccdd";
//
//        VFtpSssListRequestResponse fileResponse = connector.createVFtpSssListRequest(machine.getMachineName(), directory);
//        assertNotNull(fileResponse);
//        assertTrue(fileResponse.getErrorMessage()==null);
//        VFtpSssListRequest fileRequest = fileResponse.getRequest();
//        assertTrue(fileRequest.getFileList().length>0);
//
//
//        VFtpSssDownloadRequestForm form =
//                new VFtpSssDownloadRequestForm(machine.getFabName(), machine.getMachineName(), directory);
//        for(VFtpFileInfo file: fileRequest.getFileList()) {
//            if(!file.getFileName().startsWith("###")) {
//                form.addFile(file.getFileName(), file.getFileSize());
//            }
//        }
//
//        log.info("vFtpSssBasicTest2: request files="+form.getFiles().size());
//
//        List<DownloadRequestForm> list = new ArrayList<>();
//        list.add(form);
//
//        FileDownloadExecutor e = new FileDownloadExecutor("vftp_sss", "test", downloader, list, true);
//        e.start();
//
//        while(!e.getStatus().equals("complete")) {
//            Thread.sleep(3000);
//            log.info("vFtpSssBasicTest2: waiting download files="+e.getDownloadFiles());
//        }
//        log.info("vFtpSssBasicTest2: download complete");
//        log.info("vFtpSssBasicTest2: download path="+e.getDownloadPath());
    }


    @Test
    @Timeout(360)
    void ftpBasicTest1() throws InterruptedException {
        log.info("ftpBasicTest: start");
        assertNotNull(connectorFactory);
        FileServiceManageConnector connector = connectorFactory.getConnector(fileServiceAddress);
        MachineList machines = connector.getMachineList();
        assertTrue(machines.getMachineCount()>0);

        for(Machine machine: machines.getMachines()) {
            log.info("ftpBasicTest: machine="+machine.getMachineName());
            CategoryList categories = connector.getCategoryList(machine.getMachineName());
            assertTrue(categories.getCategories().length>0);

            Category category = categories.getCategories()[1];
            log.info("ftpBasicTest: category="+category.getCategoryCode());
            LogFileList files = connector.getFtpFileList(
                    machine.getMachineName(), category.getCategoryCode(), "20201201000000", "20201201120000",
                    null, null);
            assertTrue(files.getList().length>0);
            List<String> fileNameList = new ArrayList<>();
            for(FileInfo file:files.getList()) {
                if(!file.getFilename().startsWith("###FileOverLimit")) {
                    fileNameList.add(file.getFilename());
                }
            }
            String[] fileNameArray = new String[fileNameList.size()];
            fileNameArray = fileNameList.toArray(fileNameArray);
            log.info("ftpBasicTest: request files="+fileNameArray.length);
            FtpDownloadRequestResponse requestResponse = connector.createFtpDownloadRequest(machine.getMachineName(),
                    category.getCategoryCode(), true, fileNameArray);
            assertNotNull(requestResponse);
            assertTrue(requestResponse.getErrorCode()==null);
            log.info("ftpBasicTest: requestNo="+requestResponse.getRequestNo());

            log.info("ftpBasicTest: waiting");
            while (true) {
                Thread.sleep(3000);

                FtpDownloadRequestListResponse listResponse =
                        connector.getFtpDownloadRequestList(machine.getMachineName(), requestResponse.getRequestNo());
                assertNotNull(listResponse);
                assertTrue(listResponse.getErrorCode()==null);
                assertTrue(listResponse.getRequestList().length>0);
                FtpDownloadRequest request = listResponse.getRequestList()[0];
                assertTrue(requestResponse.getRequestNo().equals(request.getRequestNo()));

                log.info("ftpBasicTest: download files="+request.getDownloadedFileCount());
                if(request.getErrorMessage()!=null) {
                    log.info("ftpBasicTest: download error occurs  "+request.getErrorMessage());
                    break;
                }
                if(request.getStatus()==FtpDownloadRequest.Status.EXECUTED) {
                    log.info("ftpBasicTest: downloaded achieve=" + request.getArchiveFilePath());
                    break;
                }
            }
            break;
        }
    }

    @Test
    @Timeout(360)
    void ftpBasicTest2() throws InterruptedException, ParseException {
        log.info("ftpBasicTest2: start");
        assertNotNull(connectorFactory);
        assertNotNull(fileServiceAddress);
        FileServiceManageConnector connector = connectorFactory.getConnector(fileServiceAddress);

        MachineList machines = connector.getMachineList();
        assertTrue(machines.getMachineCount()>0);
        Machine machine = machines.getMachines()[0];
        assertNotNull(machine);

        log.info("ftpBasicTest2: machine="+machine.getMachineName());
        CategoryList categories = connector.getCategoryList(machine.getMachineName());
        assertTrue(categories.getCategories().length>0);

        Category category = categories.getCategories()[1];
        log.info("ftpBasicTest2: category="+category.getCategoryCode());

        List<DownloadRequestForm> list = new ArrayList<>();
        FtpDownloadRequestForm form = new FtpDownloadRequestForm("Fab1", machine.getMachineName(),
                category.getCategoryCode(), category.getCategoryName());

        LogFileList files = connector.getFtpFileList(
                machine.getMachineName(), category.getCategoryCode(), "20201201000000", "20201201120000",
                null, null);
        assertTrue(files.getList().length>0);

        //SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        for(FileInfo file: files.getList()) {
            if(!file.getFilename().startsWith("###FileOverLimit")) {
                form.addFile(file.getFilename(), file.getSize(), file.getTimestamp());
            }
        }
        list.add(form);

        log.info("ftpBasicTest2: request files="+form.getFiles().size());
        FileDownloadExecutor e = new FileDownloadExecutor(JobType.manual.name(), "ftp", "test", downloader, list, true);
        e.start();

        while(!e.getStatus().equals("complete")) {
            Thread.sleep(3000);
            log.info("ftpBasicTest2: waiting download files="+e.getDownloadFiles());
        }
        log.info("ftpBasicTest2: download complete");
        log.info("ftpBasicTest2: download path="+e.getDownloadPath());
    }


}