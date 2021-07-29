package jp.co.canon.ckbs.eec.servicemanager.executor;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;

import java.io.IOException;

public class CustomExecutor {
    ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);

    public int execute(CommandLine cmdLine, CustomOutputStreamLineHandler streamLineHandler){
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWatchdog(watchdog);

        CustomExecuteStreamHandler streamHandler = new CustomExecuteStreamHandler();
        streamHandler.setOutputStreamLineHandler(streamLineHandler);
        executor.setStreamHandler(streamHandler);

        try {
            executor.execute(cmdLine, resultHandler);
        } catch (IOException e) {
            return -1;
        }
        try {
            resultHandler.waitFor();
        } catch (InterruptedException e) {
            return -1;
        }

        return resultHandler.getExitValue();
    }
}
