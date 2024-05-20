package cv24.cv24.repository;

import cv24.cv24.entities.Poste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PosteRepository extends JpaRepository<Poste, Long> {
    Optional<Poste> findByIdentiteId(Long identiteId);
    void deleteByIdentiteId(Long identiteId);



}
