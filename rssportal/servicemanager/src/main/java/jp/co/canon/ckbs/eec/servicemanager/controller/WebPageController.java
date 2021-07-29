package jp.co.canon.ckbs.eec.servicemanager.controller;

import org.springframework.web.bind.annotation.RequestMapping;

@org.springframework.stereotype.Controller
public class WebPageController {

    @RequestMapping(value={"/", "/dashboard/**", "/login"})
    public String index() {
        return "/index.html";
    }

    @RequestMapping(value={"/notsupport"})
    public String notSupport() { return "/notsupport.html"; }
}
