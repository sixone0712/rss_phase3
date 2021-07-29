package jp.co.canon.ckbs.eec.service.command;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogFileInfo {
    String name;
    long size;
    boolean isFile;
    Calendar timestamp;

    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    public LogFileInfo(String name, long size, Calendar timestamp, boolean isFile){
        this.setName(name);
        this.setSize(size);
        this.setIsFile(isFile);
        this.setTimestamp(timestamp);
    }

    void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }

    void setSize(long size){
        this.size = size;
    }
    public long getSize(){
        return this.size;
    }

    void setIsFile(boolean isFile){
        this.isFile = isFile;
    }
    public boolean getIsFile(){
        return isFile;
    }

    void setTimestamp(Calendar timestamp){
        this.timestamp = timestamp;
    }
    Calendar getTimestamp(){
        return this.timestamp;
    }

    public String getTimestampStr(){
        return simpleDateFormat.format(timestamp.getTime());
    }
}
