package cv24.cv24.repository;

import cv24.cv24.entities.Autre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutreRepository extends JpaRepository<Autre, Long> {
}
