package cv24.cv24.repository;

import cv24.cv24.entities.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByIdentiteId(Long identiteId);
    void deleteByIdentiteId(Long identiteId);
}
