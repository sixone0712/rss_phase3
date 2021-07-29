package jp.co.canon.rss.logmanager.manager;

import jp.co.canon.rss.logmanager.repository.RemoteJobRepository;
import org.springframework.boot.autoconfigure.batch.BatchProperties;

import java.text.ParseException;

public class RemoteJobStatus extends JobStatus{
    public RemoteJobStatus(
            String jobId,
            String jobType,
            JobManager manager
    ){
        super(jobId, jobType, manager);
    }
}
