package jp.co.canon.rss.logmanager.repository;

import jp.co.canon.rss.logmanager.dto.job.ResLocalJobListDTO;
import jp.co.canon.rss.logmanager.vo.LocalJobVo;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocalJobRepository extends JpaRepository<LocalJobVo, Integer> {
    List<ResLocalJobListDTO> findBy(Sort sort);
    Optional<LocalJobVo> findByJobId(int jobId);
}
