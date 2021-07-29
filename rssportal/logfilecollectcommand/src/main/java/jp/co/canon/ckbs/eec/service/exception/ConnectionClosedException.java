package jp.co.canon.ckbs.eec.service.exception;

public class ConnectionClosedException extends Exception{
    public ConnectionClosedException(String msg){
        super(msg);
    }
}
