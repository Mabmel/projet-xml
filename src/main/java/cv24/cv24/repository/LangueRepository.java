package cv24.cv24.repository;

import cv24.cv24.entities.Langue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LangueRepository extends JpaRepository<Langue, Long> {
}
