package good.stuff.backend.soap;

import good.stuff.backend.model.Country;
import jakarta.jws.WebService;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@WebService(endpointInterface = "good.stuff.backend.soap.CountrySearchService")
public class CountrySearchServiceImpl implements CountrySearchService {

    private final File xmlFile = new File("data/countries.xml");

    @Override
    public List<String> searchCountriesByTerm(String term) {
        try {
            // 1. Fetch country data from REST API filtered by term
            fetchAndSaveCountryData(term);

            // 2. Use XPath on saved XML file to filter countries matching the term
            return filterCountriesByTerm(term);

        } catch (Exception e) {
            e.printStackTrace();
            return List.of("Error: " + e.getMessage());
        }
    }

    private void fetchAndSaveCountryData(String term) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        // Replace with your actual API URL & key, add query param with 'term'
        String url = "https://your-rest-api.com/countries?search=" + term;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-api-key", "YOUR_API_KEY") // if needed
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch countries: HTTP " + response.statusCode());
        }

        // Save the raw XML or convert JSON -> XML depending on your API
        try (FileWriter writer = new FileWriter(xmlFile)) {
            writer.write(response.body());
        }
    }

    private List<String> filterCountriesByTerm(String term) throws Exception {
        List<String> result = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        // XPath expression to find country nodes where Code or Name contains the term (case-insensitive)
        String expression = String.format(
                "//Country[contains(translate(Code, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '%s') " +
                        "or contains(translate(Name, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '%s')]",
                term.toLowerCase(), term.toLowerCase());

        XPathExpression expr = xpath.compile(expression);
        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); i++) {
            Node countryNode = nodes.item(i);
            // Convert node back to string (XML snippet)
            String xmlString = nodeToString(countryNode);
            result.add(xmlString);
        }

        return result;
    }

    private String nodeToString(Node node) throws Exception {
        DOMImplementationLS domImplementationLS = (DOMImplementationLS) node.getOwnerDocument()
                .getImplementation().getFeature("LS", "3.0");
        LSSerializer lsSerializer = domImplementationLS.createLSSerializer();
        return lsSerializer.writeToString(node);
    }
}
