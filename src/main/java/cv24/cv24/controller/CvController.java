package cv24.cv24.controller;
import cv24.cv24.entities.*;
import cv24.cv24.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> validateXML(@RequestBody String xmlString) {
        Boolean etat = false;
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

            etat = true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            etat = false;
        }

        if (etat) {
            XMLParser xp = new XMLParser();
            CV cv = xp.parseXML(xmlString);

            if (existeIdentiteDupliquee(cv)) {
                // Le CV existe déjà, retourner un message d'erreur
                logger.warn("Le CV existe déjà dans la base de données");
                String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><status>Erreur : CV déjà existant</status>";
                return ResponseEntity.badRequest()
                        .header(HttpHeaders.CONTENT_TYPE, "application/xml")
                        .body(response);
            } else {
                // Enregistrement du CV dans la base de données
                Long id = saveCV(cv);

                // Construire la réponse XML avec l'en-tête
                logger.info("CV inséré avec succès dans la base de données");
                String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cv id=\"" + id + "\" status=\"INSERTED\"/>";
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "application/xml")
                        .body(response);
            }
        } else {
            // La validation a échoué, retourner un message d'erreur
            logger.warn("Validation XML a échoué");
            String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><status>Erreur : Validation XML a échoué</status>";
            return ResponseEntity.badRequest()
                    .header(HttpHeaders.CONTENT_TYPE, "application/xml")
                    .body(response);
        }
    }


    // Méthode pour enregistrer un nouveau CV dans la base de données
    private Long saveCV(CV cv) {

        Identite savedIdentite = identiteRepository.save(cv.getIdentite());
        Long identiteId = savedIdentite.getId();
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

        // Retourner l'identifiant principal, par exemple, l'identifiant de l'identité
        return identiteId;

    }

    // Méthode pour vérifier s'il existe une identité dupliquée dans la base de données
    private boolean existeIdentiteDupliquee(CV cv) {
        // Récupérer toutes les données d'identité de la base de données
        List<Identite> identites = identiteRepository.findAll();
        // Parcourir les données d'identité de la base de données pour comparer avec le CV
        for (Identite identite : identites) {
            // Comparer nom, prénom et téléphone
            if (identite.getNom().equals(cv.getIdentite().getNom()) &&
                    identite.getPrenom().equals(cv.getIdentite().getPrenom()) &&
                    identite.getTel().equals(cv.getIdentite().getTel())) {
                // Si des correspondances sont trouvées, retourner une indication d'erreur
                return true;
            }
        }
        // Aucune identité dupliquée trouvée
        return false;
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