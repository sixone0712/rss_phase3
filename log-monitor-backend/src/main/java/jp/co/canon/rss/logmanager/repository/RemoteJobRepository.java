package jp.co.canon.rss.logmanager.repository;

import jp.co.canon.rss.logmanager.dto.job.ResRemoteJobListDTO;
import jp.co.canon.rss.logmanager.vo.RemoteJobVo;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RemoteJobRepository extends JpaRepository<RemoteJobVo, Integer> {
    List<ResRemoteJobListDTO> findBy(Sort sort);
    Optional<RemoteJobVo> findByJobId(int jobId);
}
