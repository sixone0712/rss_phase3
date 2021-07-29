package jp.co.canon.ckbs.eec.service;

import jp.co.canon.ckbs.eec.service.command.Configuration;
import jp.co.canon.ckbs.eec.service.command.FtpFileConnection;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class CheckMachineCommand extends BaseFtpCommand{

    public enum Result {
        idle,
        ok,
        ng,
        stop
    }

    @FunctionalInterface
    public interface Callback {
        void call(Result result);
    }

    private Callback callback;

    @Setter @Getter
    private long retryIntervalMillis = 1000;

    @Setter @Getter
    private int retryCount = 3;

    private Result result = Result.idle;

    public CheckMachineCommand(String host, int port, String ftpMode, String user, String password) {
        this(host, port, ftpMode, user, password, null);
    }

    public CheckMachineCommand(String host, int port, String ftpMode, String user, String password, Callback callback) {
        this.host = host;
        this.port = port;
        this.ftpmode = ftpMode;
        this.user = user;
        this.password = password;
        this.callback = callback;
    }

    public void execute() {
        doCheck();
    }

    public Result getResult() {
        return getResult(false);
    }

    public Result getResult(boolean block) {
        if(!block) {
            return result;
        }
        try {
            // The below might be busy loop.
            while(result==Result.idle) {
                Thread.sleep(10);
            }
            return result;
        } catch (InterruptedException e) {
            log.error("error on waiting mpa alive result");
            return Result.idle;
        }
    }

    public boolean isStatusOk() {
        return isStatusOk(false);
    }

    public boolean isStatusOk(boolean block) {
        if(block) {
            Result result = getResult(true);
            return result==Result.ok;
        }
        return result==Result.ok;
    }

    private void doCheck() {
        Thread thread = new Thread(checker);
        thread.start();
    }

    private Runnable checker = ()->{

        Configuration config = new Configuration();
        config.setScheme("ftp");
        config.setHost(host);
        config.setPort(port);
        config.setMode(ftpmode);
        config.setUser(user);
        config.setPassword(password);
        config.setPurpose("check");

        int retry = 0;

        FtpFileConnection connection = new FtpFileConnection();

        try {
            while(retry++<retryCount) {

                boolean isConnected = connection.connect(config);
                //log.info("try to connect {}", host);

                if(isConnected) {
                    //log.info("machine {} alive", host);
                    result = Result.ok;
                    break;
                }
                Thread.sleep(retryIntervalMillis);
            }
        } catch (InterruptedException e) {
            //log.info("stop checking machine {} alive", host);
            result = Result.stop;
        }

        if(result==Result.idle) {
            result = Result.ng;
        }

        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(callback!=null) {
            callback.call(result);
        }
    };
}
