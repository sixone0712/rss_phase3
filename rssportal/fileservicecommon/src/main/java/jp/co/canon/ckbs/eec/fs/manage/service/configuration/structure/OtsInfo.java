package jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure;

import lombok.Getter;
import lombok.Setter;

public class OtsInfo {
    @Getter @Setter
    String name;
    @Getter @Setter
    String host;
    @Getter @Setter
    int port;
}
