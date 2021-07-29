package jp.co.canon.ckbs.eec.servicemanager.executor;

public interface CustomOutputStreamLineHandler {
    boolean processOutputLine(String line);
    boolean processErrorLine(String line);
}
