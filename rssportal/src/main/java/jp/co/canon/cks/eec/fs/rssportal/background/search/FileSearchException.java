package jp.co.canon.cks.eec.fs.rssportal.background.search;

import lombok.Getter;
import lombok.Setter;

public class FileSearchException extends Exception {

    public enum Error {
        invalidParam,
        jobFull,
        searchFail,
        unknown
    }

    @Setter @Getter
    private FileSearchJob job;

    public FileSearchException(Error error) {
        super(error.name());
    }

    public FileSearchException(FileSearchJob job, Error error) {
        super(error.name());
        this.job = job;
    }
}
