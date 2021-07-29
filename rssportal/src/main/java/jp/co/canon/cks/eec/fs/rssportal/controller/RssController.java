package jp.co.canon.cks.eec.fs.rssportal.controller;

import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RssController {
    private final EspLog log = new EspLog(getClass());

    @Value("${test-version}")
    private String testVersion;

    @RequestMapping(value={"/"})
    public String rss(Model model) {
        log.info("[RssController] rss url called");
        //model.addAttribute("pageName", "index");
        //return "page";
		    return "/index.html";
    }

    @RequestMapping(value={"/page/**"})
    public String redirect() {
        log.info("[RssController] other url called");
        return "redirect:/";
    }

    /*
    @RequestMapping(value={"/version"})
    public void version(HttpServletResponse response) throws IOException {
        response.getWriter().print(testVersion);
    }
    */

    @RequestMapping(value={"/notsupport"})
    public String notSupport(Model model) {
        log.info("[RssController] notsupport url called");
        return "/notsupport.html";
    }
}
