package jp.co.canon.rss.logmanager.dto.address;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class ReqAddAddressDTO {
	@NotNull
	@Size(min = 1)
	String name;

	@NotNull
	@Email
	String email;

	@NotNull
	List<Long> groupIds;
}
