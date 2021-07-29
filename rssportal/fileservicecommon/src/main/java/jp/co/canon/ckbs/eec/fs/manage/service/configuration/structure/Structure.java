package jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure;

import lombok.Getter;
import lombok.Setter;

public class Structure {
    @Getter @Setter
    String version;

    @Getter @Setter
    OtsInfo[] otsList;

    @Getter @Setter
    MpaInfo[] mpaList;
}
