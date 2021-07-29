package jp.co.canon.ckbs.eec.fs.collect.controller.param;

import lombok.Getter;
import lombok.Setter;

public class FscListFilesRequestParam {
    @Getter @Setter
    String machine;
    @Getter @Setter
    String category;
    @Getter @Setter
    String from;
    @Getter @Setter
    String to;
    @Getter @Setter
    String keyword;
    @Getter @Setter
    String path;

    @Getter @Setter
    String host; // ftp://host:port/.....
    @Getter @Setter
    String pattern;
    @Getter @Setter
    String user;
    @Getter @Setter
    String password;

    // v2
    @Getter @Setter
    boolean recursive;
}
