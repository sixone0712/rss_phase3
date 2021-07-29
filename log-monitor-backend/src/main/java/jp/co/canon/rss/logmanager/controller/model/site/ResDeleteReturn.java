package jp.co.canon.rss.logmanager.controller.model.site;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema
public class ResDeleteReturn {
	public String deleted;
	public Boolean result;
}
