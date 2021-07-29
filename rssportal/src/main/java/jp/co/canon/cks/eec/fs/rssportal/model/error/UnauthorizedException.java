package jp.co.canon.cks.eec.fs.rssportal.model.error;

public class UnauthorizedException extends RuntimeException{
    private static final long serialVersionUID = -2238030302650813813L;

    public UnauthorizedException() {
        super("Token is invalid.");
    }
}