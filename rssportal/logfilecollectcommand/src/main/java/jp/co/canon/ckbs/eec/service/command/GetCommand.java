package jp.co.canon.ckbs.eec.service.command;

import jp.co.canon.ckbs.eec.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GetCommand {

/*
    -url : download top url
        ex 1> ftp://192.168.0.22:22001/LOG/001
        ex 2> file://CANON/LOG

    -md : ftp mode (passive, active)

    -u : user and password for ftp
        ex> -u root/password

    -fl : file list file.
        ex> -fl aaaa.LST

    -dest : save destintation directory.
        ex> -dest /LOG/downloads/MPA_XXXX

    -az : zip after download
        ex> -az zipFileName

    -max_thread : max thread count
        ex> -max_thread 4

    -structure : preserve directory structure or not
        ex> -structure true
        ex> -structure false
 */
    String [] files;
    FileInfoQueue fileQueue = new FileInfoQueue();
    StopChecker stopChecker = new StopChecker();

    void loadFileList(String fileListFile, boolean preserveStructure) throws IOException {
        try {
            List<String> fileList = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileListFile))));
            String line;
            while( (line = reader.readLine()) != null){
                if (line.length() == 0){
                    continue;
                }
                String[] lineArr = line.split(",");
                if (lineArr.length == 0){
                    continue;
                }
                fileList.add(lineArr[0]);
                String fileName = lineArr[0];
                String downloadPath = fileName;
                if (!preserveStructure){
                    int slashIdx = downloadPath.lastIndexOf("/");
                    if (slashIdx >= 0){
                        downloadPath = downloadPath.substring(slashIdx+1);
                    }
                }
                fileQueue.push(new FileInfo(fileName, downloadPath));
            }
            files = fileList.toArray(new String[0]);
        } catch (FileNotFoundException e) {
            throw new IOException(e);
        }
    }

    public void stopDownload(){
        stopChecker.setStopped();
    }

    public boolean execute(String url,
                        String md,
                        String user,
                        String password,
                        String fileList,
                        String dest,
                        boolean zip,
                        String zipFileName,
                           int max_thread,
                           boolean preserveStructure,
                           DownloadStatusCallback callback){

        Configuration configuration = new Configuration();
        URI uri = null;
        try {
            uri = new URI(url);
            configuration.setScheme(uri.getScheme());
            configuration.setHost(uri.getHost());
            configuration.setPort(uri.getPort());
            configuration.setRootPath(uri.getPath());
        } catch (URISyntaxException e) {
            return false;
        }
        configuration.setMode(md);

        configuration.setUser(user);
        configuration.setPassword(password);
        try {
            loadFileList(fileList, preserveStructure);
        } catch (IOException e) {
            return false;
        }

        FileAccessor accessor = FileAccessorFactory.createInstance(configuration);
        boolean result = accessor.downloadFiles(fileQueue, dest, zip, zipFileName, max_thread, preserveStructure, stopChecker, callback);
        return result;
    }
}
