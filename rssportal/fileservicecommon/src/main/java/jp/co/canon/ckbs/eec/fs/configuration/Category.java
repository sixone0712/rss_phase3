package jp.co.canon.ckbs.eec.fs.configuration;

import lombok.Getter;
import lombok.Setter;

public class Category {
    @Getter @Setter
    String categoryCode;

    @Getter @Setter
    String categoryName;

    @Getter @Setter
    String description;

    @Getter @Setter
    String dest; // "Cons", "LogSrv"

    @Getter @Setter
    String filePath; // "/home/mpa/log/console/online/hsms/*/"

    @Getter @Setter
    String fileName; // "err_*.log"

    @Getter @Setter
    boolean auto;

    @Getter @Setter
    boolean display;

    @Getter @Setter
    int port;


    public Category(){

    }

    public Category(String categoryCode, String categoryName, String description, String dest,
                    String filePath, String fileName, boolean auto, boolean display){
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.description = description;
        this.dest = dest;
        this.filePath = filePath;
        this.fileName = fileName;
        this.auto = auto;
        this.display = display;
    }
}
