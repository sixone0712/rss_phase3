package jp.co.canon.cks.eec.fs.rssportal.background.autocollect;

import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CollectThreadPool {

    private final EspLog log = new EspLog(getClass());

    @Value("${rssportal.collect.max-threads}")
    private int maxCollectThreads;

    private List<CollectThread> threads;
    private AtomicInteger threadId;

    @PostConstruct
    private void postContrcutor() {
        log.info("initialize collect thread pool ("+maxCollectThreads+" threads)");
        threads = new ArrayList<>();
        threadId = new AtomicInteger(0);
    }

    public CollectThread getThread() {
        CollectThread thread = null;
        if(threads.size()<maxCollectThreads) {
            thread = new CollectThread(threadId.getAndIncrement());
            threads.add(thread);
        }
        printCurrent();
        return thread;
    }

    public void putThread(CollectThread thread) {
        if(thread==null) {
            log.error("putThread error");
            return;
        }
        try {
            thread.join();
            threads.remove(thread);
        } catch (InterruptedException e) {
            log.error("thread join failed");
            e.printStackTrace();
        }
        printCurrent();
    }

    private void printCurrent() {
        log.info("CollectThreadPool running threads");
        for(CollectThread thread: threads) {
            log.info(" - thread "+thread.getNo());
        }
    }

}
