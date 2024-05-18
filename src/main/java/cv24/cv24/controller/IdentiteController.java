package cv24.cv24.controller;

import cv24.cv24.entities.Identite;
import cv24.cv24.repository.IdentiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/identites")
public class IdentiteController {

    private final IdentiteRepository identiteRepository;

    @Autowired
    public IdentiteController(IdentiteRepository identiteRepository) {
        this.identiteRepository = identiteRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<Identite> createIdentite(@RequestBody Identite identite) {
        Identite newIdentite = identiteRepository.save(identite);
        return new ResponseEntity<>(newIdentite, HttpStatus.CREATED);
    }
}
