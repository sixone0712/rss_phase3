package jp.co.canon.rss.logmanager.dto.site;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ResDuplicateErrDTO {
    int errorCode;
    String errorMsg;
}
