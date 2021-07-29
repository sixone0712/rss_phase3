package jp.co.canon.rss.logmanager.dto.job;

import jp.co.canon.rss.logmanager.dto.address.AddressBookDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class ResMailContextDTO {
    private String [] customEmails;
    private List<AddressBookDTO> emailBook;
    private List<AddressBookDTO> groupBook;
    @NotNull
    private String subject;
    private String body;
    @NotNull
    private int before;
}
