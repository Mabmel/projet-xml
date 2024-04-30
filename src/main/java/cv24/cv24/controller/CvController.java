package cv24.cv24.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

@RestController
public class CvController {

    @PostMapping("/validate-xml")
    public ResponseEntity<String> validateXML(@RequestBody String xmlString) {
        // Chemin vers le fichier XSD
        String xsdFichierPath = "classpath:xml/shema.xsd";

        try {
            // Créer une fabrique de documents
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            // Créer un parseur DOM
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlString)));

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new StreamSource(getClass().getClassLoader().getResourceAsStream(xsdFichierPath)));

            // Créer un validateur avec le schéma
            Validator validator = schema.newValidator();

            // Valider le document
            validator.validate(new DOMSource(document));

            // Si aucune exception n'est levée, la validation est réussie
            return ResponseEntity.ok("Le document XML est valide selon le schéma XSD.");
        } catch (ParserConfigurationException | SAXException | IOException e) {
            // Si une exception est levée, la validation échoue
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Le document XML n'est pas valide selon le schéma XSD. Erreur : " + e.getMessage());
        }

    }
}
