package jp.co.canon.rss.logmanager.dto.job;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class ResMailContextAddDTO {
    private String [] customEmails;
    private long [] emailBookIds;
    private long [] groupBookIds;
    @NotNull
    private String subject;
    private String body;
    @NotNull
    private int before;
}
