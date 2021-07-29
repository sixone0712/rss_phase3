package jp.co.canon.cks.eec.fs.rssportal.background.fileserviceproc;

public class FileDownloadServiceException extends Throwable {

    public enum Error { error, timeout }

    private String message;
    private Error error;

    public FileDownloadServiceException(Error error) {
        super();
        this.message = error.name();
        this.error = error;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Error getError() {
        return error;
    }
}
