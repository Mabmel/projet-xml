package cv24.cv24.repository;

import cv24.cv24.entities.Langue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LangueRepository extends JpaRepository<Langue, Long> {
    List<Langue> findByIdentiteId(Long identiteId);
   void deleteByIdentiteId(Long identiteId);
}
