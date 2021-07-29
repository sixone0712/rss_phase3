package jp.co.canon.ckbs.eec.servicemanager.executor;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

@Slf4j
public class SshExecutor {
    public int execute(String host, int port, String user, String password, String cmd, CustomOutputStreamLineHandler streamLineHandler){
        int exitValue = 0;

        JSch jsch = new JSch();
        Session session = null;
        Channel channel = null;

        try {
            session = jsch.getSession(user, host, port);
            session.setPassword(password);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            channel = session.openChannel("exec");
            ChannelExec channelExec = (ChannelExec) channel;
            channelExec.setCommand(cmd);

            InputStream in = channel.getInputStream();
            channel.connect();

            int i = 0;
            byte[] tmp = new byte[1024];
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))){
                String line = null;
                while (true){
                    line = reader.readLine();
                    if (line == null){
                        break;
                    }
                    streamLineHandler.processOutputLine(line);
                }
                while(true){
                    if (channel.isClosed()){
                        exitValue = channel.getExitStatus();
                        break;
                    }
                }
            }

            channel.disconnect();
            session.disconnect();
        } catch (JSchException e) {
            log.error("JSchException occurred. {}", e.getMessage());
            exitValue = -2;
        } catch (IOException e){
            log.error("IOException occurred. {}", e.getMessage());
            exitValue = -2;
        } finally {
            if (channel != null){
                channel.disconnect();
            }
            if (session != null){
                session.disconnect();
            }
        }
        log.info("ssh executed with exitValue = {}", exitValue);
        return exitValue;
    }
}
