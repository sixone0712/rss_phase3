package jp.co.canon.cks.eec.fs.rssportal.common.loginitializer;

import jp.co.canon.cks.eec.fs.rssportal.common.LogType;

public class ControlLogInitializer implements LogInitializer {

    @Override
    public String getLogType() {
        return LogType.control.name();
    }

    @Override
    public String getPattern() {
        return "%r %thread %level - %msg%n";
    }

    @Override
    public String getFileName() {
        return "control.log";
    }
}
