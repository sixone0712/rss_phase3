package jp.co.canon.rss.logmanager.manager;

import jp.co.canon.rss.logmanager.repository.LocalJobRepository;

import java.text.ParseException;

public class LocalJobStatus extends JobStatus{
    public LocalJobStatus(
            String jobId,
            String jobType,
            JobManager manager
    ){
        super(jobId, jobType, manager);
    }
}
