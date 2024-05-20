package cv24.cv24;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
public class TestXML {
    @Test
    public void createHTML() {
        try {
            String xmlChemin = "src/main/resources/xml/test.xml";
            String xsltchemin = "src/main/resources/xml/parser.xslt";
            String outputchemin = "src/main/resources/templates/DetailCV.html";
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Source xslt = new StreamSource(new File(xsltchemin));
            Transformer transformer = transformerFactory.newTransformer(xslt);

            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            // fichier XML
            Source xml = new StreamSource(new File(xmlChemin));
            Result output = new StreamResult(new File(outputchemin));
            transformer.transform(xml, output);
            assertTrue(new java.io.File(outputchemin).exists(), "Le fichier HTML a été généré.");
            System.out.println("Le fichier HTML  généré avec succès : " + outputchemin);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}
