package jp.co.canon.ckbs.eec.service.command;

public class Configuration {
    String scheme;
    String host;
    int port;
    String rootPath;
    String user;
    String password;
    String mode;
    String purpose; // Optional

    public void setScheme(String scheme){
        this.scheme = scheme;
    }

    public void setHost(String host){
        this.host = host;
    }

    public void setPort(int port){
        this.port = port;
    }

    public void setRootPath(String path){
        this.rootPath = path;
    }

    public void setUser(String user){
        this.user = user;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public void setMode(String mode){
        this.mode = mode;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
