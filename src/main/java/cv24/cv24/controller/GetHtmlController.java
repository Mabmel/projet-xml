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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
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
            // Log the error
            logger.error("Une erreur est survenue lors de la récupération des CVs : {}", e.getMessage());

            // Add an error message to the model
            model.addAttribute("errorMessage", "Une erreur est survenue lors de la récupération des CVs. Veuillez réessayer plus tard.");

            // Return an error page or redirect to an error page
            return "error"; // Assuming you have an "error" template
        }
    }

    @GetMapping(value = "/cv24/html")
    public String getCVDetailHTML(@RequestParam("id") Long id, Model model) throws ParserConfigurationException, IOException, SAXException, TransformerException {

        Identite identite = identiteRepository.findById(id).orElse(null);
        XMLParser xp = new XMLParser();
        if (identite == null) {
            return xp.generateErrorXML("Identité non trouvée pour l'ID: " + id);
        }

        CV cv = new CV();
        cv.setIdentite(identite);
        cv.setPoste(posteRepository.findByIdentiteId(identite.getId()).orElse(null));
        cv.setExperiences(experienceRepository.findByIdentiteId(identite.getId()));
        cv.setDiplomes(diplomeRepository.findByIdentiteId(identite.getId()));
        cv.setCertifications(certificationRepository.findByIdentiteId(identite.getId()));
        cv.setLangues(langueRepository.findByIdentiteId(identite.getId()));
        cv.setAutres(autreRepository.findByIdentiteId(identite.getId()));

        String fxml = xp.parseDataCVToXML(cv);
        String xsdFichierPath = "classpath:xml/shema.xsd";

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(fxml)));

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new StreamSource(getClass().getClassLoader().getResourceAsStream(xsdFichierPath)));
            Validator validator = schema.newValidator();;
            validator.validate(new DOMSource(document));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return xp.generateErrorXML("Erreur de validation du XML par rapport au schéma XSD: " + e.getMessage());
        }


        String xsltFilePath = "classpath:xml/parser.xslt";
        //String outputchemin = "src/main/resources/Resultatv3.html";// Chemin vers votre fichier XSLT
        //String outputchemin = "src/main/resources/Resultat.html";//
        String outputchemin = "resources/Resultat.html";

        //String outputchemin = "Resultat.html"; // Crée le fichier Resultat.html dans le répertoire src/main/resources


        System.out.println("tima");
        InputStream xsltStream = getClass().getClassLoader().getResourceAsStream(xsltFilePath);
        System.out.println("coucou");
        Source xslt = new StreamSource(xsltStream);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer(xslt);
        System.out.println("ghilas");
        transformer.setOutputProperty(OutputKeys.METHOD, "html");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");


        Source xmlSource = new StreamSource(new StringReader(fxml));
        File outputFile = new File(outputchemin);
        OutputStream htmlStream = new FileOutputStream(outputFile);
        Result output = new StreamResult(htmlStream);

        // Transformation
        transformer.transform(xmlSource, output);

        // Fermeture des flux
        xsltStream.close();
        htmlStream.close();
        transformer.transform(xmlSource, output);
        return "DetailCV";
    }
}
