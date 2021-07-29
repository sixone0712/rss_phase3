package jp.co.canon.ckbs.eec.service;

import jp.co.canon.ckbs.eec.service.command.DownloadInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class GetFtpCommand extends BaseFtpCommand{
    String rootDir = null;
    String directory = null;
    String downloadDirectory = null;
    String [] files;
    boolean zip = false;
    String zipFileName;

    String commandInfoString;

    FileInfoQueue fileQueue = new FileInfoQueue();
    StopChecker stopChecker = new StopChecker();

    String createCommandInfoString(){
        return String.format("(%s, %s, %d, %s, %s, %s, %s)", "get", this.host, this.port, this.ftpmode, this.rootDir, this.directory, this.downloadDirectory);
    }

    void loadFileList(String fileListFile) throws IOException{
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
                fileQueue.push(new FileInfo(lineArr[0], lineArr[0]));
            }
            files = fileList.toArray(new String[0]);
        } catch (FileNotFoundException e) {
            throw new IOException(e);
        }
    }

    void appendZipFile(ZipOutputStream out, File f) throws IOException {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(f));
            byte[] buffer = new byte[16384];
            while (!stopChecker.isStopped()) {
                int size = in.read(buffer);
                if (size <= 0) break;
                out.write(buffer, 0, size);
            }
        } finally {
            if (in != null) in.close();
        }
    }

    void zipFiles() throws Exception{
        File zipFile = new File(downloadDirectory, zipFileName);

        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(zipFile));
            for (String filename : files) {
                if (stopChecker.isStopped()){
                    throw new Exception("Stopped");
                }
                File f = new File(downloadDirectory, filename);
                ZipEntry temp = new ZipEntry(f.getName());
                temp.setTime(f.lastModified());
                out.putNextEntry(temp);
                appendZipFile(out, f);
                out.closeEntry();
            }
            out.close();
            out = null;
            for (String filename : files){
                File f = new File(downloadDirectory, filename);
                f.delete();
            }
            log.info("STATUS: compression finished {}", commandInfoString);
        } catch (Exception e){
            log.error("ERR: Exception({}) on zipFiles {}", e.getMessage(), commandInfoString);
            throw new Exception("Exception while zip Files");
        } finally {
            if (out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void doCommand(DownloadStatusCallback callback) throws Exception{
        int total_thread_count = fileQueue.size() < 4 ? fileQueue.size() : 4;
        log.info("Request File Count: {}, Total Thread Count : {}", fileQueue.size(), total_thread_count);
        ArrayList<GetFtpCommandExecuteThread> threadArrayList = new ArrayList<>();

        FtpServerInfo serverInfo = new FtpServerInfo();
        serverInfo.host = this.host;
        serverInfo.port = this.port;
        serverInfo.ftpmode = this.ftpmode;
        serverInfo.user = this.user;
        serverInfo.password = this.password;

        File downloadDirectoryFile = new File(downloadDirectory);
        if (!downloadDirectoryFile.exists()){
            downloadDirectoryFile.mkdirs();
        }

        DownloadInfo downloadInfo = new DownloadInfo();

        for(int idx = 0; idx < total_thread_count; ++idx){
            GetFtpCommandExecuteThread getThread = new GetFtpCommandExecuteThread(serverInfo, rootDir, directory, downloadDirectory, fileQueue, downloadInfo, stopChecker, callback);
            threadArrayList.add(getThread);
            getThread.start();
        }

        for(GetFtpCommandExecuteThread th : threadArrayList){
            try {
                th.join();
            } catch (InterruptedException e){
            }
        }

        for(GetFtpCommandExecuteThread th : threadArrayList){
            if (th.isExitWithError()){
                throw new Exception("Error Occurred");
            }
        }
        if (stopChecker.isStopped()){
            throw new Exception("Stopped");
        }

        if (zip){
            zipFiles();
        }

        log.info("END DOWNLOAD(SUCCESSFUL) TOTAL:{} {}", downloadInfo.getTotalDownloadCount(), commandInfoString);
    }

    public void execute(String host,
                           int port,
                           String ftpmode,
                           String user,
                           String password,
                           String root,
                           String dir,
                           String dest,
                           String fileList,
                           boolean zip,
                           String zipFileName,
                           DownloadStatusCallback callback
                           ) throws Exception{
        this.host = host;
        this.port = port;
        this.ftpmode = ftpmode;
        this.user = user;
        this.password = password;
        this.rootDir = root;
        this.directory = dir;
        this.downloadDirectory = dest;
        loadFileList(fileList);
        this.zip = zip;
        this.zipFileName = zipFileName;

        commandInfoString = createCommandInfoString();

        doCommand(callback);
    }

    public void stopDownload(){
        stopChecker.setStopped();
    }
}
