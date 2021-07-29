package jp.co.canon.ckbs.eec.service.command;

public class FtpFileAccessor extends FileAccessor {

    @Override
    FileConnection createFileConnection() {
        return new FtpFileConnection();
    }
}
