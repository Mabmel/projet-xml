package cv24.cv24.repository;

import cv24.cv24.entities.Identite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdentiteRepository extends JpaRepository<Identite, Long> {
}
