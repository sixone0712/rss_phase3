package jp.co.canon.cks.eec.fs.rssportal.common;


import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


public class EspLog {

    private Logger log;

    @Autowired
    private EspLogFactory factory;

    /*public TedLog() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        FileAppender appender = new FileAppender();

        appender.setContext(context);
        appender.setName("appender-name");
        appender.setFile("logger/test.txt");

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%r %thread %level - %msg%n");
        encoder.start();

        appender.setEncoder(encoder);
        appender.start();

        log = context.getLogger("test");
        log.addAppender(appender);

        log.error("test hi");
    }*/

    public EspLog(Class clazz) {
        this(clazz.getName());
    }

    public EspLog(String name) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        log = context.getLogger(name);
    }

    public void info(String msg) {
        log.info(msg);
    }

    public void info(String msg, LogType ... types) {
        for(LogType type: types) {
            Logger logger = factory.getLogger(type);
            if(logger!=null) {
                logger.info(msg);
            }
        }
        log.info(msg);
    }

    public void error(String msg) {
        log.error(msg);
    }

    public void error(String msg, LogType ... types) {
        for(LogType type: types) {
            Logger logger = factory.getLogger(type);
            if(logger!=null) {
                logger.error(msg);
            }
        }
        log.error(msg);
    }

    public void warn(String msg) {
        log.warn(msg);
    }

    public void warn(String msg, LogType ... types) {
        for(LogType type: types) {
            Logger logger = factory.getLogger(type);
            if(logger!=null) {
                logger.warn(msg);
            }
        }
        log.warn(msg);
    }

    public void debug(String msg) {
        log.debug(msg);
    }

    public void debug(String msg, LogType ... types) {
        for(LogType type: types) {
            Logger logger = factory.getLogger(type);
            if(logger!=null) {
                logger.debug(msg);
            }
        }
        log.debug(msg);
    }

    public void trace(String msg) {
        log.trace(msg);
    }

    public void trace(String msg, LogType ... types) {
        for(LogType type: types) {
            Logger logger = factory.getLogger(type);
            if(logger!=null) {
                logger.trace(msg);
            }
        }
        log.trace(msg);
    }


}
