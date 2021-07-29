package jp.co.canon.ckbs.eec.service.command;

public class FileAccessorFactory {
    public static FileAccessor createInstance(Configuration configuration){
        FileAccessor accessor = null;
        if (configuration.scheme.equals("ftp")){
            accessor = new FtpFileAccessor();
        }
        if (accessor != null){
            accessor.setConfiguration(configuration);
        }
        return accessor;
    }
}
