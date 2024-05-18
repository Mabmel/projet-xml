package cv24.cv24.repository;

import cv24.cv24.entities.Poste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PosteRepository extends JpaRepository<Poste, Long> {
}
