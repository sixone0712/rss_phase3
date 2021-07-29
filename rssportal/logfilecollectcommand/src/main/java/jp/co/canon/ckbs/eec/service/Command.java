package jp.co.canon.ckbs.eec.service;

import jp.co.canon.ckbs.eec.service.exception.FtpConnectionException;

public interface Command {
    void execute(String[] args) throws FtpConnectionException;
}
