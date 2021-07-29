package jp.co.canon.ckbs.eec.servicemanager.service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DownloadRequestThread extends Thread{
    FileDownloadService fileDownloadService;
    DownloadRequest request;
    DownloadRequestRepository downloadRequestRepository;

    boolean stopped = false;

    public DownloadRequestThread(FileDownloadService fileDownloadService,
                                 DownloadRequest request,
                                 DownloadRequestRepository downloadRequestRepository){
        this.fileDownloadService = fileDownloadService;
        this.request = request;
        this.downloadRequestRepository = downloadRequestRepository;
    }

    @Override
    public void run() {
        request.setStatus("process");
        File requestDir = new File(downloadRequestRepository.getRequestDirectory(request.getRequestNo()));
        requestDir.mkdirs();
        String archivePath = downloadRequestRepository.getArchiveFilePath(request.getRequestNo());
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(new File(archivePath))))
        {
            for(String fileName : request.getFileNames()){
                if (this.stopped){
                    break;
                }
                File destFile = new File("/CANON/DEVLOG", fileName);
                if (destFile.exists() && destFile.isFile()){
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zos.putNextEntry(zipEntry);
                    Files.copy(Paths.get("/CANON/DEVLOG", fileName), zos);
                    zos.closeEntry();
                }
            }
            if (this.stopped){
                request.setStatus("error");
                downloadRequestRepository.downloadEnded(request);
                downloadRequestRepository.deleteRequest(request.getRequestNo());
            }
            request.setUrl(String.format("/servicemanager/api/files/storage/%s", request.getRequestNo()));
            request.setStatus("done");
        } catch (Exception e){
            e.printStackTrace();
            request.setStatus("error");
        }
        downloadRequestRepository.downloadEnded(request);
        fileDownloadService.removeDownloadRequestThread(request.getRequestNo());
    }

    public void setStopped(boolean stopped){
        this.stopped = stopped;
    }
}
