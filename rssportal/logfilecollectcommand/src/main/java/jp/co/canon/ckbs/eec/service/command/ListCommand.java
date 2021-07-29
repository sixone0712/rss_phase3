package jp.co.canon.ckbs.eec.service.command;

import jp.co.canon.ckbs.eec.service.Command;
import jp.co.canon.ckbs.eec.service.exception.FtpConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Slf4j
public class ListCommand implements Command {

    /*
    -url : top url for list command
        ex 1 > -url ftp://192.168.0.22/LOG/001
        ex 2 > -url file://LOG/001

    -md : ftp mode (passive, active)

    -u : user and password for ftp
        ex> -u root/password

    -pattern : search pattern
        ex> -pattern YYMMDD/YYMMDDhhmmss

    -p : search period
        ex> -p 20201030000000,20201030235959

    -d : search directory.......

    -k : search keyword
        ex> -k keyword1,keyword2
     */

    String getProtocol(String url){
        if (url.startsWith("ftp://")){
            return "ftp";
        }
        if (url.startsWith("file://")){
            return "file";
        }
        return null;
    }

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    public static Calendar dateToCalendar(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    @Override
    public void execute(String[] args) throws FtpConnectionException {
        Options options = new Options();
        options.addRequiredOption("url", "url", true, "");
        options.addRequiredOption("md", "md", true, "");
        options.addRequiredOption("u", "u", true, "");
        options.addRequiredOption("pattern", "pattern", true, "");

        options.addOption("p", "p", true, "");
        options.addOption("d", "d", true, "");
        options.addOption("k", "k", true, "");

        CommandLineParser parser = new DefaultParser();
        try {
            Configuration configuration = new Configuration();

            CommandLine commandLine = parser.parse(options, args);
            String url = commandLine.getOptionValue("url");

            URI uri = null;
            try {
                uri = new URI(url);
                configuration.setScheme(uri.getScheme());
                configuration.setHost(uri.getHost());
                configuration.setPort(uri.getPort());
                configuration.setRootPath(uri.getPath());
            } catch (URISyntaxException e) {
                System.out.println(String.format("ERR: uri is invalid (%s).", url));
                System.exit(-1);
                return;
            }

            String md = commandLine.getOptionValue("md");
            configuration.setMode(md);

            String uStr = commandLine.getOptionValue("u");
            String[] strs = uStr.split("/");
            if (strs.length != 2){
                System.out.println(String.format("ERR: user is invalid (%s).", uStr));
                System.exit(-1);
                return;
            }
            configuration.setUser(strs[0]);
            configuration.setPassword(strs[1]);

            String patternStr = commandLine.getOptionValue("pattern");
            RuleChecker ruleChecker = RuleChecker.create(patternStr);

            Calendar startDate = null;
            Calendar endDate = null;
            String dir = null;
            String keyword = null;
            if (commandLine.hasOption("p")){
                String periodStr = commandLine.getOptionValue("p");
                String[] dateStr = periodStr.split(",");
                if (dateStr.length !=2){
                    System.out.println(String.format("ERR: period is invalid (%s).", periodStr));
                    System.exit(-1);
                    return;
                }
                try {
                    startDate = dateToCalendar(simpleDateFormat.parse(dateStr[0]));
                    endDate = dateToCalendar(simpleDateFormat.parse(dateStr[1]));
                    ruleChecker.setPeriod(dateStr[0], dateStr[1]);
                } catch (java.text.ParseException e) {
                    System.out.println(String.format("ERR: period is invalid (%s).", periodStr));
                    System.exit(-1);
                }
            }
            if (commandLine.hasOption("d")){
                dir = commandLine.getOptionValue("d");
            }
            if (commandLine.hasOption("k")){
                keyword = commandLine.getOptionValue("k");
            }
            long listStartTime = System.currentTimeMillis();
            log.info("List Start at {}", listStartTime);
            FileAccessor accessor = FileAccessorFactory.createInstance(configuration);
            LogFileInfo[] logFiles = accessor.listFiles(ruleChecker, startDate, endDate, dir, keyword, false);
            for(LogFileInfo logFile : logFiles){
                System.out.println(logFile);
            }
            long listEndTime = System.currentTimeMillis();
            log.info("List End at {}, spent {}", listEndTime, listEndTime - listStartTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public LogFileInfo[] execute(String url,
                                 String pattern,
                                 String startdate,
                                 String enddate,
                                 String user,
                                 String password,
                                 String dir,
                                 String keyword,
                                 boolean recursive) throws FtpConnectionException {
        Configuration configuration = new Configuration();
        URI uri = null;
        try {
            uri = new URI(url);
            configuration.setScheme(uri.getScheme());
            configuration.setHost(uri.getHost());
            configuration.setPort(uri.getPort());
            configuration.setRootPath(uri.getPath());

            configuration.setMode("passive");
            configuration.setUser(user);
            configuration.setPassword(password);
            configuration.setPurpose("list");

            RuleChecker ruleChecker = RuleChecker.create(pattern);

            Calendar startDate = null;
            Calendar endDate = null;
            try {
                startDate = dateToCalendar(simpleDateFormat.parse(startdate));
                endDate = dateToCalendar(simpleDateFormat.parse(enddate));
                ruleChecker.setPeriod(startdate, enddate);
            } catch (java.text.ParseException e) {
                return null;
            }
            FileAccessor accessor = FileAccessorFactory.createInstance(configuration);
            return accessor.listFiles(ruleChecker, startDate, endDate, dir, keyword, recursive);
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
