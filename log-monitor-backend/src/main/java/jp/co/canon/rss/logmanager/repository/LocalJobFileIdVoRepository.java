package jp.co.canon.rss.logmanager.repository;

import jp.co.canon.rss.logmanager.vo.LocalJobFileIdVo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocalJobFileIdVoRepository extends JpaRepository<LocalJobFileIdVo, Integer> {
}
