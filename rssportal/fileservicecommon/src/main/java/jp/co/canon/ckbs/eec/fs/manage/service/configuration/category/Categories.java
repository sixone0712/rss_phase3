package jp.co.canon.ckbs.eec.fs.manage.service.configuration.category;

import lombok.Getter;
import lombok.Setter;

public class Categories {
    @Getter @Setter
    String version;

    @Getter @Setter
    CategoryInfo[] categories;
}
