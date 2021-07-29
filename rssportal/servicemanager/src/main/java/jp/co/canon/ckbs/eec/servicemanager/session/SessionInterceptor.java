package jp.co.canon.ckbs.eec.servicemanager.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class SessionInterceptor extends HandlerInterceptorAdapter {
    private Log log = LogFactory.getLog(getClass());

    @Value("${servicemanager.sessionTimeout}")
    private int sessionTimeOut;

    @Value("${servicemanager.type}")
    private String deviceType;

    private static final String[] allowedRegex = {
        "/",
        "/api/auth/login",
        "/api/auth/logout",
        "/api/auth/me",
        "/dashboard/.*",
        "/login",
        "/static/.*",
        "/notsupport",
        "^(\\/[^\\/:*?\"<>|]*\\.[^\\/:*?\"<>|]*)"       // allow : /abc.def   not allow: /abc/def/ghi.jkl
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession();
        String url = request.getServletPath();
        StringBuilder sb = new StringBuilder("request ");
        sb.append(url).append(" ");

        if(request.getMethod().equals("OPTIONS")) {
            return true;
        }

        if(deviceType.equals("OTS")) {
            return true;
        }

        // If the client requests a page which permits guest access, this method does nothing here.
        if(!isUserspace(url)) {
            //response.addHeader("userauth", "true");
            sb.append("[allowed url][true]");
            log.info(sb.toString());
            return true;
        }

        // Check session has been authorized.
        SessionContext context = (SessionContext)session.getAttribute("context");
        if(context == null || !context.isAuthenticated()) {
//            if(isPageMove(url)) {
//                response.sendRedirect("/rss");
//            }
            sb.append("[invalid-session][false]");
            log.info(sb.toString());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        long current = System.currentTimeMillis();
        if((current - session.getLastAccessedTime()) > sessionTimeOut) {
//            if(isPageMove(url)) {
//                response.sendRedirect("/servicemanager");
//            }
            session.invalidate();
            sb.append("[timeout][false]");
            log.info(sb.toString());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        //response.addHeader("userauth", "true");
        sb.append("[true]");
        log.info(sb.toString());
        return true;


    }

    private boolean isUserspace(@NonNull String url) {
        for(String pat: allowedRegex) {
            if(url.matches(pat))
                return false;
        }
        return true;
    }
}
