package cv24.cv24.controller;
import cv24.cv24.entities.*;
import cv24.cv24.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class CvController {

    //Ajout desx log pour la gestion
    private static final Logger logger = LoggerFactory.getLogger(CvController.class);



    private final IdentiteRepository identiteRepository;
    private final PosteRepository posteRepository;
    private final ExperienceRepository experienceRepository;
    private final DiplomeRepository diplomeRepository;
    private final CertificationRepository certificationRepository;
    private final LangueRepository langueRepository;
    private final AutreRepository autreRepository;

    public CvController(IdentiteRepository identiteRepository, PosteRepository posteRepository, ExperienceRepository experienceRepository, DiplomeRepository diplomeRepository, CertificationRepository certificationRepository, LangueRepository langueRepository, AutreRepository autreRepository) {
        this.identiteRepository = identiteRepository;
        this.posteRepository = posteRepository;
        this.experienceRepository = experienceRepository;
        this.diplomeRepository = diplomeRepository;
        this.certificationRepository = certificationRepository;
        this.langueRepository = langueRepository;
        this.autreRepository = autreRepository;
    }

    @PostMapping("/validate-xml")
    public Boolean validateXML(@RequestBody String xmlString) {
        Boolean etat =false;
        String xsdFichierPath = "classpath:xml/shema.xsd";


        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlString)));

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new StreamSource(getClass().getClassLoader().getResourceAsStream(xsdFichierPath)));

            Validator validator = schema.newValidator();

            validator.validate(new DOMSource(document));

            etat=true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            etat=false;
        }
    if(etat){

        XMLParser xp = new XMLParser();

        CV cv = xp.parseXML(xmlString);

        identiteRepository.save(cv.getIdentite());
        Poste poste = cv.getPoste();
        poste.setIdentite(cv.getIdentite());
        // Enregistrer Poste
        posteRepository.save(poste);

        // Enregistrer Experiences
        for (Experience experience : cv.getExperiences()) {
            experience.setIdentite(cv.getIdentite());
            experienceRepository.save(experience);
        }

        // Enregistrer Diplomes
        for (Diplome diplome : cv.getDiplomes()) {
            diplome.setIdentite(cv.getIdentite());
            diplomeRepository.save(diplome);
        }

        // Enregistrer Certifications
        for (Certification certification : cv.getCertifications()) {
            certification.setIdentite(cv.getIdentite());
            certificationRepository.save(certification);
        }

        // Enregistrer Langues
        for (Langue langue : cv.getLangues()) {
            langue.setIdentite(cv.getIdentite());
            langueRepository.save(langue);
        }

        // Enregistrer Autres
        for (Autre autre : cv.getAutres()) {
            autre.setIdentite(cv.getIdentite());
            autreRepository.save(autre);
        }



        return true;
    }else {
        return false;
    }
    }

    @GetMapping("/resume")
    public String getAllCVsForHTML(Model model) {
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
            cvs.add(cv);
        }

        model.addAttribute("cvs", cvs);
        return "resume";
    }

    @DeleteMapping(value = "/cv24/delete/{id}", produces = "application/xml")
    @Transactional
    public ResponseEntity<String> deleteCV(@PathVariable Long id) {
        // Logger  enregistrer les messages de log
        logger.info("Requête reçue pour supprimer le CV avec l'id : {}", id);
        try {
            Optional<Identite> identiteOptional = identiteRepository.findById(id);
            if (identiteOptional.isPresent()) {
                Identite identite = identiteOptional.get();

                // Supprimer les certifications
                certificationRepository.deleteByIdentiteId(identite.getId());

                //  supprimer les autres données liées à cette identité
                posteRepository.deleteByIdentiteId(identite.getId());
                experienceRepository.deleteByIdentiteId(identite.getId());
                diplomeRepository.deleteByIdentiteId(identite.getId());
                langueRepository.deleteByIdentiteId(identite.getId());
                autreRepository.deleteByIdentiteId(identite.getId());

                //  supprimer l'identité
                identiteRepository.deleteById(id);

                // Construire la réponse XML avec l'en-tête
                StringWriter stringWriter = new StringWriter();
                stringWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                stringWriter.write("<cv id=\"" + id + "\" status=\"SUPPRIMÉ\"/>");
                String response = stringWriter.toString();

                // En cv  trouver
                logger.info("CV avec l'id : {} supprimé avec succès", id);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "application/xml")
                        .body(response);
            } else {
                // En cv non trouver
                logger.warn("CV avec l'id : {} non trouvé", id);
                StringWriter stringWriter = new StringWriter();
                stringWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                stringWriter.write("<status>Erreur : CV non trouvé</status>");
                String response = stringWriter.toString();

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header(HttpHeaders.CONTENT_TYPE, "application/xml")
                        .body(response);
            }
        } catch (Exception e) {
            //  retourner une réponse avec le message d'erreur
            logger.error("Une erreur est survenue lors de la suppression du CV avec l'id : {}", id, e);
            StringWriter stringWriter = new StringWriter();
            stringWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            stringWriter.write("<status>Erreur : " + e.getMessage() + "</status>");
            String response = stringWriter.toString();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header(HttpHeaders.CONTENT_TYPE, "application/xml")
                    .body(response);
        }
    }







}