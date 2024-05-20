package cv24.cv24.controller;

import cv24.cv24.entities.CV;
import cv24.cv24.entities.Diplome;
import cv24.cv24.entities.Identite;
import cv24.cv24.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.ArrayList;
import java.util.List;

@Controller
public class GetHtmlController {
    private static final Logger logger = LoggerFactory.getLogger(CvController.class);



    private final IdentiteRepository identiteRepository;
    private final PosteRepository posteRepository;
    private final ExperienceRepository experienceRepository;
    private final DiplomeRepository diplomeRepository;
    private final CertificationRepository certificationRepository;
    private final LangueRepository langueRepository;
    private final AutreRepository autreRepository;

    public GetHtmlController(IdentiteRepository identiteRepository, PosteRepository posteRepository, ExperienceRepository experienceRepository, DiplomeRepository diplomeRepository, CertificationRepository certificationRepository, LangueRepository langueRepository, AutreRepository autreRepository) {
        this.identiteRepository = identiteRepository;
        this.posteRepository = posteRepository;
        this.experienceRepository = experienceRepository;
        this.diplomeRepository = diplomeRepository;
        this.certificationRepository = certificationRepository;
        this.langueRepository = langueRepository;
        this.autreRepository = autreRepository;
    }


    @GetMapping(value = "/cv24/resume")
    public String getAllCVsForHTML(Model model) {
        try {
            List<Identite> identites = identiteRepository.findAll();
            List<CV> cvs = new ArrayList<>();

            for (Identite identite : identites) {
                CV cv = new CV();
                cv.setIdentite(identite);
                cv.setPoste(posteRepository.findByIdentiteId(identite.getId()).orElse(null));
                cv.setExperiences(experienceRepository.findByIdentiteId(identite.getId()));
                cv.setDiplomes(diplomeRepository.findByIdentiteId(identite.getId()));
                cv.setCertifications(certificationRepository.findByIdentiteId(identite.getId()));
                cv.setLangues(langueRepository.findByIdentiteId(identite.getId()));
                cv.setAutres(autreRepository.findByIdentiteId(identite.getId()));
                // Récupération du diplôme le plus récent et ajout au CV
                Diplome diplomePlusRecent = cv.getDiplomePlusRecent();
                cv.setDiplomePlusRecent(diplomePlusRecent);

                cvs.add(cv);
            }
            if (cvs.isEmpty()) {
                logger.info("Aucun CV trouvé dans la base de données.");
                model.addAttribute("message", "Aucun CV trouvé dans la base de données.");
            } else {
                logger.info("Nombre de CVs trouvés dans la base de données : {}", cvs.size());
                model.addAttribute("cvs", cvs);
            }

            return "resume";
        }  catch (Exception e) {
            logger.error("Une erreur est survenue lors de la récupération des CVs : {}", e.getMessage());

            model.addAttribute("errorMessage", "Une erreur est survenue lors de la récupération des CVs. Veuillez réessayer plus tard.");
            return "error";
        }
    }


}
