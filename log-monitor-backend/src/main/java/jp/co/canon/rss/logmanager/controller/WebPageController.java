package jp.co.canon.rss.logmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebPageController {
	@RequestMapping(value={"/", "/status/**", "/configure/**", "/login"})
	public String index() {
		return "/index.html";
	}


	@RequestMapping(value={"/notsupport"})
	public String notSupport() {
		return "/notsupport.html";
	}
}
