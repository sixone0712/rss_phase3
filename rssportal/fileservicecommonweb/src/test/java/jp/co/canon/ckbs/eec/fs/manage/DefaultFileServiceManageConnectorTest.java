package jp.co.canon.ckbs.eec.fs.manage;

import jp.co.canon.ckbs.eec.fs.collect.controller.param.FtpDownloadRequestResponse;
import jp.co.canon.ckbs.eec.fs.collect.service.FileInfo;
import jp.co.canon.ckbs.eec.fs.collect.service.LogFileList;
import jp.co.canon.ckbs.eec.fs.manage.service.CategoryList;
import jp.co.canon.ckbs.eec.fs.manage.service.MachineList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class DefaultFileServiceManageConnectorTest {
    FileServiceManageConnectorFactory connectorFactory = new DefaultFileServiceManageConnectorFactory();

    @Test
    void test_001(){
        FileServiceManageConnector connector = connectorFactory.getConnector("192.168.222.210:81");
        Assertions.assertNotNull(connector);

        MachineList machineList = connector.getMachineList();

        CategoryList categoryList = connector.getCategoryList();

        categoryList = connector.getCategoryList("MPA_1");

        LogFileList logFileList = connector.getFtpFileList("MPA_1", "003", "20200827000000", "20200827120000", null, null);
    }

    @Test
    void test_001_001(){
        FileServiceManageConnector connector = connectorFactory.getConnector("192.168.222.210:81");

        connector.getFtpFileList("MPA_1", "003", "20200828000000", "20200828120000", "", "");

        connector.getFtpFileList("MPA_AAA", "003", "20200828000000", "20200828120000", "", "");
    }

    String[] getFtpFileList(String machine, String category, String from, String to){
        FileServiceManageConnector connector = connectorFactory.getConnector("192.168.222.210");
        LogFileList logFileList = connector.getFtpFileList("MPA_1", "003", "20200828000000", "20200828120000", "", "");

        FileInfo[] fileInfos = logFileList.getList();
        ArrayList<String> fileNames = new ArrayList<>();
        for(FileInfo info : fileInfos){
            fileNames.add(info.getFilename());
            if (fileNames.size() >= 5){
                break;
            }
        }
        return fileNames.toArray(new String[0]);
    }

    @Test
    void test_002_001(){
        FileServiceManageConnector connectorFail = connectorFactory.getConnector("192.168.222.210:81");

        connectorFail.getFtpFileList("MPA_1", "003","20200828000000", "20200828120000", "", "");

        String[] files = getFtpFileList("MPA_1", "003", "20200828000000", "20200828120000");

        try {
            connectorFail.getMachineList();
        } catch (Exception e){

        }
        try {
            connectorFail.getCategoryList();
        } catch (Exception e){

        }
        try {
            connectorFail.getCategoryList("MPA_1");
        } catch (Exception e){
            
        }

        FtpDownloadRequestResponse res = connectorFail.createFtpDownloadRequest("MPA_1", "003", true, files);
        connectorFail.getFtpDownloadRequestList("MPA_1", "AAAA");
        connectorFail.cancelAndDeleteRequest("MPA_1", "AAAA");

        connectorFail.createVFtpSssListRequest("MPA_1", "IP_AS_RAW");
        connectorFail.getVFtpSssListRequest("MPA_1", "AAAA");
        connectorFail.cancelAndDeleteVFtpSssListRequest("MPA_1", "AAAA");

        connectorFail.createVFtpSssDownloadRequest("MPA_1", "AAAA", files, true);
        connectorFail.getVFtpSssDownloadRequest("MPA_1", "AAAA");
        connectorFail.cancelAndDeleteVFtpSssDownloadRequest("MPA_1", "AAAA");

        connectorFail.createVFtpCompatDownloadRequest("MPA_1", "AAAA", true);
        connectorFail.getVFtpCompatDownloadRequest("MPA_1", "AAAA");
        connectorFail.cancelAndDeleteVFtpCompatDownloadRequest("MPA_1", "AAAA");
    }

    @Test
    void test_002_002(){
        FileServiceManageConnector connector = connectorFactory.getConnector("192.168.222.210:81");
        String[] files = getFtpFileList("MPA_1", "003", "20200828000000", "20200828120000");
        FtpDownloadRequestResponse res;

        res = connector.createFtpDownloadRequest("MPA_1", "003", true, files);

        connector.cancelAndDeleteRequest("MPA_1", "AAAA");
        connector.cancelAndDeleteVFtpSssListRequest("MPA_1", "AAAA");
        connector.cancelAndDeleteVFtpSssDownloadRequest("MPA_1", "AAAA");
        connector.cancelAndDeleteVFtpCompatDownloadRequest("MPA_1", "AAAA");

    }
}
