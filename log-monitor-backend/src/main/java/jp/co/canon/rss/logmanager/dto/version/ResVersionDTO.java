package jp.co.canon.rss.logmanager.dto.version;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ResVersionDTO {
    private String version;
}
