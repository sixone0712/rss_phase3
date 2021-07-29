package jp.co.canon.cks.eec.fs.rssportal.common.loginitializer;

import jp.co.canon.cks.eec.fs.rssportal.common.LogType;

public class ExceptionLogInitializer implements LogInitializer {

    @Override
    public String getLogType() {
        return LogType.exception.name();
    }

    @Override
    public String getPattern() {
        return "%r %thread %level - %msg%n";
    }

    @Override
    public String getFileName() {
        return "exception.log";
    }
}
