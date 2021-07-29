package jp.co.canon.cks.eec.fs.rssportal.background;

import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnector;
import jp.co.canon.cks.eec.fs.portal.bussiness.CustomURL;
import jp.co.canon.cks.eec.fs.rssportal.background.fileserviceproc.FileDownloadInfo;
import jp.co.canon.cks.eec.fs.rssportal.model.FileInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FileDownloadContext {

    private final String ftpType;
    private final String id;
    private final String tool;
    @Getter
    private String logType;
    @Getter
    private String logTypeStr;

    private int files;
    private String[] fileNames;
    private long[] fileSizes;
    private Calendar[] fileDates;

    private final DownloadRequestForm downloadForm;

    @Getter @Setter
    private FileServiceManageConnector fileServiceManageConnector;

    private String requestNo;
    private CustomURL achieveUrl;
    private String localFilePath;

    private File rootDir;
    private String subDir;

    private boolean achieve;

    @Setter @Getter
    private boolean achieveDecompress;

    @Getter
    private String command;

    @Getter
    private String directory;

    @Getter @Setter
    private FileDownloadInfo downloadInfo;

    @Getter
    private long totalFileSize;

    @Deprecated
    private long downloadFiles;

    private String baseDir;

    public FileDownloadContext(@NonNull String ftpType, @NonNull String id, @NonNull DownloadRequestForm form, @NonNull String baseDir) {

        this.ftpType = ftpType;
        this.downloadForm = form;
        this.id = id;
        this.tool = form.getMachine();
        this.baseDir = baseDir;

        if(ftpType.equals("ftp")) {
            FtpDownloadRequestForm ftp = (FtpDownloadRequestForm)form;
            // When logs place in a sub-directory not the log root,
            // logType is possible to include sub-directory information.
            String[] split = ftp.getCategoryType().split("/");
            if (split.length == 1) {
                this.logType = ftp.getCategoryType();
            } else {
                this.logType = split[0];
            }
            this.logTypeStr = ftp.getCategoryName();

            files = ftp.getFiles().size();
            fileNames = new String[files];
            fileSizes = new long[files];
            fileDates = new Calendar[files];

            totalFileSize = 0;
            for (int i = 0; i < files; ++i) {
                FileInfo fileInfo = ftp.getFiles().get(i);
                fileNames[i] = fileInfo.getName();
                fileSizes[i] = fileInfo.getSize();
                fileDates[i] = convertStringToCalendar(fileInfo.getDate());
                totalFileSize += fileInfo.getSize();
            }

            // check the first file name in the array and, if a file name includes '/'
            // we consider it placed in a sub directory.
            String[] firsts = fileNames[0].split("/");
            if (firsts.length != 1) {
                subDir = firsts[0];
            } else {
                subDir = null;
            }

            /*Path path;
            if(achieveDecompress) {
                path = Paths.get(baseDir, tool, logTypeStr == null ? logType : logTypeStr);
            } else {
                path = Paths.get(baseDir, tool);
            }
            this.outPath = path.toString();*/

        } else if(ftpType.equals("vftp_compat")) {
            VFtpCompatDownloadRequestForm compat = (VFtpCompatDownloadRequestForm) form;
            files = 1;
            fileNames = new String[1];
            fileSizes = new long[1];
            fileDates = new Calendar[1];
            command = compat.getCommand();
            fileNames[0] = compat.getCommand();
            fileSizes[0] = 0;
            fileDates[0] = Calendar.getInstance();
            fileDates[0].setTimeInMillis(System.currentTimeMillis());
            totalFileSize = 0;

            /*Path path = Paths.get(baseDir, tool);
            this.outPath = path.toString();*/
            achieveDecompress = compat.isDecompress();
        } else if(ftpType.equals("vftp_sss")){
            VFtpSssDownloadRequestForm sss = (VFtpSssDownloadRequestForm) form;

            files = sss.getFiles().size();
            fileNames = new String[files];
            fileSizes = new long[files];
            fileDates = new Calendar[files];

            directory = sss.getDirectory();

            totalFileSize = 0;
            for(int i=0; i<sss.getFiles().size(); ++i) {
                FileInfo file = sss.getFiles().get(i);
                fileNames[i] = file.getName();
                fileSizes[i] = file.getSize();
                fileDates[i] = null;
                totalFileSize += file.getSize();
            }
        }

        downloadFiles = 0;
    }

    public void setAchieveUrl(@NonNull final String achieveUrl) {
        try {
            this.achieveUrl = new CustomURL(achieveUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

    public CustomURL getAchieveUrl() {
        return achieveUrl;
    }

    public void setRootDir(@NonNull final File rootDir) {
        this.rootDir = rootDir;
    }

    public File getRootDir() {
        return rootDir;
    }

    public String getTool() {
        return tool;
    }

    public int getFileCount() {
        return files;
    }

    public String[] getFileNames() {
        return fileNames;
    }

    public long[] getFileSizes() {
        return fileSizes;
    }

    public Calendar[] getFileDates() {
        return fileDates;
    }

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public String getOutPath() {
        Path path = null;
        if(ftpType.equals("ftp")) {
            if(achieveDecompress) {
                path = Paths.get(baseDir, tool, logTypeStr == null ? logType : logTypeStr);
            } else {
                path = Paths.get(baseDir, tool);
            }
        } else if(ftpType.equals("vftp_compat")) {
            path = Paths.get(baseDir, tool);
        } else if(ftpType.equals("vftp_sss")) {
            if(achieveDecompress) {
                path = Paths.get(baseDir, tool, directory);
            } else {
                path = Paths.get(baseDir, tool);
            }
        }
        return path.toString();
    }

    public long getDownloadFiles() {
        return downloadFiles;
    }

    public void setDownloadFiles(long downloadFiles) {
        this.downloadFiles = downloadFiles;
    }

    public String getFtpType() {
        return ftpType;
    }

    public String getSubDir() {
        return subDir;
    }

    public boolean isAchieve() {
        return achieve;
    }

    public void setAchieve(boolean achieve) {
        this.achieve = achieve;
    }

    private Calendar convertStringToCalendar(@NonNull final String str) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat fmter = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            cal.setTime(fmter.parse(str));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal;
    }
}
