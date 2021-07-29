package jp.co.canon.cks.eec.fs.rssportal.controller;

import jp.co.canon.cks.eec.fs.rssportal.background.FileDownloader;
import jp.co.canon.cks.eec.fs.rssportal.downloadlist.DownloadListVo;
import jp.co.canon.cks.eec.fs.rssportal.model.auth.AccessToken;
import jp.co.canon.cks.eec.fs.rssportal.model.auth.RefreshToken;
import jp.co.canon.cks.eec.fs.rssportal.service.JwtService;
import jp.co.canon.cks.eec.fs.rssportal.vo.CollectPlanVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DownloadControllerCommonTest {

    @Autowired
    private FileDownloaderController c;

    @Test
    void test() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        assertNotNull(c);

        Method method = DownloadControllerCommon.class.getDeclaredMethod("createZipFilename",
                String.class);
        assertNotNull(method);
        method.setAccessible(true);

        Field field = DownloadControllerCommon.class.getDeclaredField("jwtService");
        assertNotNull(field);
        field.setAccessible(true);
        field.set(c, dummyJwtService);

        method.invoke(c, "123123");
    }

    JwtService dummyJwtService = new JwtService() {
        @Override
        public <T> String create(T data, String subject) {
            return null;
        }

        @Override
        public String getCurrentAccessToken() {
            return null;
        }

        @Override
        public String getCurAccTokenUserName() {
            return "user:)";
        }

        @Override
        public int getCurAccTokenUserID() {
            return 0;
        }

        @Override
        public String getCurAccTokenUserPermission() {
            return null;
        }

        @Override
        public AccessToken decodeAccessToken(String jwt) {
            return null;
        }

        @Override
        public RefreshToken decodeRefreshToken(String jwt) {
            return null;
        }

        @Override
        public boolean isUsable(String jwt) {
            return false;
        }
    };

}