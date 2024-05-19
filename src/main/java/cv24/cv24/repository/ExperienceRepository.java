package cv24.cv24.repository;

import cv24.cv24.entities.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    List<Experience> findByIdentiteId(Long identiteId);
      void deleteByIdentiteId(Long identiteId);
}
