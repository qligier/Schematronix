package ch.qligier.schematronix.utils;

import lombok.NonNull;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 * A simple, insecure XML parser for tests only.
 *
 * @author Quentin Ligier
 * @version 0.1.0
 */
public class SimpleXmlParser {

    /**
     * The {@link Document} builder.
     */
    private static DocumentBuilder documentBuilder;

    /**
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the configuration requested.
     * @throws IOException if any IO errors occur.
     * @throws SAXException if any parse errors occur.
     */
    public static Document parseString(@NonNull final String xmlContent) throws ParserConfigurationException, IOException, SAXException {
        if (documentBuilder == null) {
            initDocumentBuilder();
        }
        return documentBuilder.parse(new InputSource(new StringReader(xmlContent)));
    }

    /**
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the configuration requested.
     */
    private static void initDocumentBuilder() throws ParserConfigurationException {
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        documentBuilder = dbFactory.newDocumentBuilder();
    }
}
