package jp.co.canon.ckbs.eec.fs.manage.service.configuration.category;

import lombok.Getter;
import lombok.Setter;

public class CategoryInfo {
    @Getter @Setter
    int no;
    @Getter @Setter
    String name;
    @Getter @Setter
    String description;
    @Getter @Setter
    String dest; // "Cons", "LogSrv"
    @Getter @Setter
    String path; // "/home/mpa/log/console/online/hsms/*/"
    @Getter @Setter
    String fileName; // "err_*.log"
    @Getter @Setter
    boolean autocollect;
    @Getter @Setter
    boolean display;
    @Getter @Setter
    int port;
    @Getter @Setter
    String rootDir; // "/home/mpa/log/console/online/hsms"
    @Getter @Setter
    String patternDir; // "*/"
}
