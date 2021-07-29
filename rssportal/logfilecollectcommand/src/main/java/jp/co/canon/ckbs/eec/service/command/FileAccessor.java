package jp.co.canon.ckbs.eec.service.command;

import jp.co.canon.ckbs.eec.service.*;
import jp.co.canon.ckbs.eec.service.exception.ConnectionClosedException;
import jp.co.canon.ckbs.eec.service.exception.FtpConnectionException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
abstract public class FileAccessor {
    Configuration configuration;

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    abstract FileConnection createFileConnection();

    static boolean checkDate(Calendar startDate, Calendar endDate, Calendar dateToCheck) {
        if (startDate == null && endDate == null) {
            return true;
        }
        long date = dateToCheck.getTimeInMillis();
        if (startDate != null && date < startDate.getTimeInMillis()) {
            return false;
        }
        if (endDate != null && date > endDate.getTimeInMillis()) {
            return false;
        }
        return true;
    }

    static String mergePath(String path, String str) {
        if (str == null || str.length() == 0) {
            return path;
        }
        if (path == null || path.length() == 0) {
            return str;
        }
        if (str.startsWith("/")) {
            return String.format("%s%s", path, str);
        }
        return String.format("%s/%s", path, str);
    }

    static class DirInfo {
        DirInfo(String path, int level) {
            this.path = path;
            this.level = level;
        }

        String path;
        int level;
    }

    void listFiles_type1_inner(ArrayList<LogFileInfo> logFileInfoArrayList,
                               RuleChecker ruleChecker,
                               Calendar startDate,
                               Calendar endDate,
                               String keyword,
                               boolean recursive) throws ConnectionClosedException, FtpConnectionException {
        boolean rc = false;
        String rootPath = configuration.rootPath;
        int matchLevel = ruleChecker.getCount() - 1;

        FileConnection connection = createFileConnection();

        boolean connected = connection.connect(configuration);
        if (connected == false) {
            throw new FtpConnectionException("failed to connect");
        }
        Queue<DirInfo> directoriesToVisit = new LinkedList<>();

        directoriesToVisit.add(new DirInfo("", 0));
        DirInfo currentDirInfo = null;
        while ((currentDirInfo = directoriesToVisit.poll()) != null) {
            String pullPath = mergePath(rootPath, currentDirInfo.path);
            rc = connection.changeDirectory(pullPath);
            if (!rc) {
                continue;
            }

            LogFileInfo[] files = connection.listFiles();
            if (currentDirInfo.level == matchLevel) {
                for (LogFileInfo file : files) {
                    if (file.getIsFile()) {
                        if (checkDate(startDate, endDate, file.getTimestamp())) {
                            String name = file.getName();
                            if (ruleChecker.matches(currentDirInfo.path, name, currentDirInfo.level)) {
                                String pullName = mergePath(currentDirInfo.path, name);
                                file.setName(pullName);
                                logFileInfoArrayList.add(file);
                            }
                        }
                    } else {
                        String name = file.getName();
                        if (ruleChecker.matches(currentDirInfo.path, name, currentDirInfo.level)) {
                            String pullName = mergePath(currentDirInfo.path, name);
                            file.setName(pullName);
                            logFileInfoArrayList.add(file);
                        }
                    }
                }
            } else {
                for (LogFileInfo file : files) {
                    if (!file.getIsFile()) {
                        if (ruleChecker.matches(currentDirInfo.path, file.getName(), currentDirInfo.level)) {
                            String newPath = mergePath(currentDirInfo.path, file.getName());
                            directoriesToVisit.add(new DirInfo(newPath, currentDirInfo.level + 1));
                        }
                    }
                }
            }
        }
        if(recursive && logFileInfoArrayList.size()>0) {
            listFilesInDirectories(connection, logFileInfoArrayList, ruleChecker, startDate, endDate, keyword);
        }
        connection.disconnect();
    }

    void listFilesInDirectories(FileConnection connect, List<LogFileInfo> logFiles, RuleChecker ruleChecker,
                                Calendar start, Calendar end, String keyword)
            throws FtpConnectionException {

        List<LogFileInfo> outs = new ArrayList<>();

        for(LogFileInfo log: logFiles) {
            if(log.getIsFile()) {
                outs.add(log);
            } else {
                outs.addAll(listFilesRecursive(connect, log.getName(), ruleChecker, start, end, keyword));
            }
        }
        logFiles.clear();
        logFiles.addAll(outs);
    }

    List<LogFileInfo> listFilesRecursive(FileConnection connect, String path, RuleChecker ruleChecker,
                                         Calendar start, Calendar end, String keyword) throws FtpConnectionException {

        List<LogFileInfo> outs = new ArrayList<>();
        String fullPath = mergePath(configuration.rootPath, path);
        try {
            if(!connect.changeDirectory(fullPath)) {
                throw new ConnectionClosedException("changeDirectory error");
            }
            LogFileInfo[] files = connect.listFiles();
            for(LogFileInfo file: files) {
                if (file.getIsFile()) {
                    if(checkDate(start, end, file.getTimestamp())) {
                        file.setName(mergePath(path, file.getName()));
                        outs.add(file);
                    }
                } else {
                    outs.addAll(listFilesRecursive(connect, mergePath(path, file.getName()), ruleChecker, start, end, keyword));
                }
            }
        } catch (ConnectionClosedException e) {
            throw new FtpConnectionException("listFilesRecursive error");
        }
        return outs;
    }

    void listFiles_type1(ArrayList<LogFileInfo> logFileInfoArrayList,
                         RuleChecker ruleChecker,
                         Calendar startDate,
                         Calendar endDate,
                         String keyword,
                         boolean recursive
    ) throws FtpConnectionException {
        boolean retry = false;
        int retryCount = 0;
        do {
            retry = false;
            try {
                logFileInfoArrayList.clear();
                listFiles_type1_inner(logFileInfoArrayList, ruleChecker, startDate, endDate, keyword, recursive);
            } catch (ConnectionClosedException e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                retry = true;
                if(++retryCount>=5) {
                    throw new FtpConnectionException("retry failed");
                }
            }
        } while (retry);
    }

    void listFiles_type2_inner(ArrayList<LogFileInfo> logFileInfoArrayList,
                               RuleChecker ruleChecker,
                               Calendar startDate,
                               Calendar endDate,
                               String dir) throws ConnectionClosedException, FtpConnectionException {
        String rootPath = configuration.rootPath;
        rootPath = mergePath(rootPath, dir);
        FileConnection connection = createFileConnection();

        boolean connected = connection.connect(configuration);
        if (connected == false) {
            throw new FtpConnectionException("failed to connect");
        }
        boolean rc = connection.changeDirectory(rootPath);
        if (!rc) {
            return;
        }

        LogFileInfo[] files = connection.listFiles();
        for (LogFileInfo file : files) {
            if (ruleChecker.matches(dir, file.getName(), 0)) {
                Calendar timestamp = file.getTimestamp();
                String name = mergePath(dir, file.getName());
                if (file.getIsFile()) {
                    if (checkDate(startDate, endDate, timestamp)) {
                        file.setName(name);
                        logFileInfoArrayList.add(file);
                    }
                } else {
                    file.setName(name);
                    logFileInfoArrayList.add(file);
                }
            }
        }
    }

    void listFiles_type2(ArrayList<LogFileInfo> logFileInfoArrayList,
                         RuleChecker ruleChecker,
                         Calendar startDate,
                         Calendar endDate,
                         String dir) throws FtpConnectionException {
        boolean retry = false;
        int retryCount = 0;
        do {
            retry = false;
            try {
                logFileInfoArrayList.clear();
                listFiles_type2_inner(logFileInfoArrayList, ruleChecker, startDate, endDate, dir);
            } catch (ConnectionClosedException e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                retry = true;
                if(++retryCount>=5) {
                    throw new FtpConnectionException("retry failed");
                }
            }
        } while (retry);
    }

    public LogFileInfo[] listFiles(RuleChecker ruleChecker,
                                   Calendar startDate,
                                   Calendar endDate,
                                   String dir,
                                   String keyword,
                                   boolean recursive) throws FtpConnectionException {
        ArrayList<LogFileInfo> logFileInfoArrayList = new ArrayList<>();
        if (dir != null && dir.length() > 0) {
            RuleChecker newRuleChecker = RuleChecker.create("*");
            listFiles_type2(logFileInfoArrayList, newRuleChecker, startDate, endDate, dir);
        } else {
            listFiles_type1(logFileInfoArrayList, ruleChecker, startDate, endDate, keyword, recursive);
        }
        return logFileInfoArrayList.toArray(new LogFileInfo[0]);
    }

    public boolean downloadFiles(FileInfoQueue fileInfoQueue,
                                 String destDirStr,
                                 boolean zip,
                                 String zipFileName,
                                 int maxThreadCount,
                                 boolean preserveDirStructure,
                                 StopChecker stopChecker,
                                 DownloadStatusCallback callback) {
        int threadCount = fileInfoQueue.size() < maxThreadCount ? fileInfoQueue.size() : maxThreadCount;
        log.info("Request File Count: {}, Total Thread Count : {}", fileInfoQueue.size(), threadCount);
        File destDir = new File(destDirStr);
        destDir.mkdirs();
        long downloadStartTime = System.currentTimeMillis();
        log.info("Download start at {}", downloadStartTime);
        DownloadInfo downloadInfo = new DownloadInfo();

        ArrayList<GetCommandExecuteThread> threadArrayList = new ArrayList<>();

        FileInfoQueue downloadedQueue = new FileInfoQueue();

        for (int idx = 0; idx < threadCount; ++idx) {
            GetCommandExecuteThread getThread = new GetCommandExecuteThread(configuration,
                    this,
                    destDirStr,
                    fileInfoQueue,
                    preserveDirStructure,
                    downloadInfo,
                    downloadedQueue,
                    stopChecker,
                    callback);
            threadArrayList.add(getThread);
            getThread.start();
        }

        for (GetCommandExecuteThread th : threadArrayList) {
            try {
                th.join();
            } catch (InterruptedException e) {

            }
        }
        int errorCount = 0;
        for (GetCommandExecuteThread th : threadArrayList) {
            if (th.getExitWithError()) {
                errorCount++;
            }
        }
        if (errorCount > 0) {
            return false;
        }
        if (stopChecker.isStopped()) {
            return false;
        }
        if (zip) {
            long zipStartTime = System.currentTimeMillis();
            log.info("Zip Files Start at {}", zipStartTime);
            boolean zipSuccess = false;
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(new File(destDir, zipFileName)))) {
                FileInfo fileInfo = null;
                while ((fileInfo = downloadedQueue.poll()) != null) {
                    if (stopChecker.isStopped()) {
                        throw new Exception("Stopped");
                    }
                    Path downloadPath = Paths.get(destDirStr, fileInfo.getDownloadPath());
                    if (Files.isDirectory(downloadPath)) {
                        String directoryName = fileInfo.getDownloadPath();
                        AtomicBoolean _success = new AtomicBoolean(true);
                        Files.walk(downloadPath).filter(path -> !Files.isDirectory(path)).forEach(path -> {
                            try {
                                ZipEntry entry = new ZipEntry(directoryName + "/" + downloadPath.relativize(path).toString());
                                zos.putNextEntry(entry);
                                Files.copy(path, zos);
                                zos.closeEntry();
                            } catch (IOException e) {
                                log.error("Exception {}", e.getMessage());
                                _success.set(false);
                            }
                        });
                        deleteDir(downloadPath.toFile());
                        if (!_success.get()) {
                            throw new IOException("failed to zip directories");
                        }
                    } else {
                        ZipEntry entry = new ZipEntry(fileInfo.getDownloadPath());
                        zos.putNextEntry(entry);
                        Files.copy(Paths.get(destDirStr, fileInfo.getDownloadPath()), zos);
                        zos.closeEntry();
                        File fileToDelete = new File(destDir, fileInfo.getDownloadPath());
                        fileToDelete.delete();
                    }
                }
                zipSuccess = true;
            } catch (Exception e) {
                log.error("Exception :{}", e.getMessage());
                return false;
            }
            long zipEndTime = System.currentTimeMillis();
            log.info("Zip Files End at {}, zip spent time {}", zipEndTime, zipEndTime - zipStartTime);
            if (zipSuccess) {
                File zipFile = new File(destDir, zipFileName);
                if (callback != null) {
                    callback.archiveCompleted(zipFileName, zipFile.length());
                }
            }
        }
        long downloadEndTime = System.currentTimeMillis();
        log.info("Download End at {}, spent time {}", downloadEndTime, downloadEndTime - downloadStartTime);
        return true;
    }

    private void deleteDir(File file) {
        File[] contents = file.listFiles();
        if(contents!=null)
        {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }
}
