package jp.co.canon.rss.logmanager.manager;

import jp.co.canon.rss.logmanager.repository.RemoteJobRepository;

public class RemoteNotiStatus extends NotiStatus {
    public RemoteNotiStatus(
            String jobId,
            NotiManager manager
    ){
        super(jobId, manager);
    }
}
