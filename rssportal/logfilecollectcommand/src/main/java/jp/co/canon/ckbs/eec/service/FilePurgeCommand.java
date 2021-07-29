package jp.co.canon.ckbs.eec.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@Slf4j
public class FilePurgeCommand {
    static void deleteFile(File file){
        if (file.isDirectory()){
            log.trace("delete directory : {}", file.getName());
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
            }
        } else {
            log.trace("delete file : {}", file.getName());
            file.delete();
        }
    }

    static boolean isExpired(long currentTime, long fileTime, long diff){
        if (currentTime - fileTime > diff){
            return true;
        }
        return false;
    }

    static boolean isNameMatched(String name, String pattern){
        return name.matches(pattern);
    }

    public static void purgeDirs(String dest, long diff, String pattern){
        long deleteCount = 0;
        File destDir = new File(dest);
        long currentTime = System.currentTimeMillis();
        if (destDir.exists() && destDir.isDirectory()){
            File[] files = destDir.listFiles();
            for (File file : files){
                if (file.isDirectory()) {
                    if (isNameMatched(file.getName(), pattern) && isExpired(currentTime, file.lastModified(), diff)) {
                        deleteFile(file);
                        deleteCount++;
                    }
                }
            }
        }
        log.info("Total deleted file count :{}", deleteCount);
    }

    public static void purgeFiles(String dest, long diff, String pattern){
        long deleteCount = 0;
        File destDir = new File(dest);
        long currentTime = System.currentTimeMillis();
        if (destDir.exists() && destDir.isDirectory()){
            File[] files = destDir.listFiles();
            for (File file : files){
                if (file.isFile()) {
                    if (isNameMatched(file.getName(), pattern) && isExpired(currentTime, file.lastModified(), diff)) {
                        deleteFile(file);
                        deleteCount++;
                    }
                }
            }
        }
        log.info("Total deleted file count :{}", deleteCount);
    }

    public static void main(String[] args){
        Options options = new Options();
        options.addRequiredOption("dest", "dest", true, "purge destination directory");
        options.addRequiredOption("t", "t", true, "purge time in minutes");
        options.addRequiredOption("pattern", "pattern", true, "filename pattern");
        options.addRequiredOption("type", "type", true, "filetype, D or F");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine commandLine = parser.parse(options, args);
            String dest = commandLine.getOptionValue("dest");
            String timeStr = commandLine.getOptionValue("t");
            String patternStr = commandLine.getOptionValue("pattern");
            String typeStr = commandLine.getOptionValue("type");
            long t = Long.parseLong(timeStr);
            long diff = t * 60 * 1000;

            log.info("FilePurge dir : {}, time : {}, pattern: {}, type: {}", dest, t, patternStr, typeStr);
            if(typeStr.equals("D")) {
                purgeDirs(dest, diff, patternStr);
            } else if (typeStr.equals("F")){
                purgeFiles(dest, diff, patternStr);
            }
        } catch (ParseException e) {

        }
    }
}
