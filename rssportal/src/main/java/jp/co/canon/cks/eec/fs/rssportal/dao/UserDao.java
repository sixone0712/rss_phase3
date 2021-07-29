package jp.co.canon.cks.eec.fs.rssportal.dao;

import jp.co.canon.cks.eec.fs.rssportal.vo.UserVo;
import org.springframework.lang.NonNull;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface UserDao {

    List<UserVo> findAll();
    UserVo find(Map<String, Object> param);
    boolean add(UserVo user);
    boolean modify(UserVo user);
    boolean delete(UserVo user);
    boolean UpdateAccessDate(Map<String, Object> param);
    boolean updateRefreshToken(Map<String, Object> param);
    boolean getToken(String token);
    boolean setToken(Map<String, Object> param);
    boolean cleanBlacklist(Date now);
    int getTotalCnt();
}
