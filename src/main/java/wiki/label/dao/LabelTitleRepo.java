package wiki.label.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import wiki.label.entity.LabelTitleDO;

public interface LabelTitleRepo extends JpaRepository<LabelTitleDO, Long> {

  Page<LabelTitleDO> findAllByIdGreaterThanEqual(long startId, Pageable page);
}
