package jp.co.canon.ckbs.eec.service.command;

import jp.co.canon.ckbs.eec.service.exception.ConnectionClosedException;
import jp.co.canon.ckbs.eec.service.exception.FtpConnectionException;

import java.io.IOException;
import java.io.InputStream;

abstract public class FileConnection {
    abstract public boolean connect(Configuration configuration);
    abstract public void disconnect();

    abstract public boolean changeDirectory(String directory) throws ConnectionClosedException;
    abstract public LogFileInfo[] listFiles() throws FtpConnectionException;
    abstract public LogFileInfo[] listFiles(String directory) throws FtpConnectionException;

    abstract public InputStream getInputStream(String fileName) throws IOException;

    abstract public void completePendingCommand();
}
