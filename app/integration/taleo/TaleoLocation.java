/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package integration.taleo;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;
import nu.xom.converters.DOMConverter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.utils.NoOpEntityResolver;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;

//Could also move this dispatcher stuff over to jaxb some day. These ancient WSDLs just make it a chore.
public class TaleoLocation{

    private final URL myAccessPointURL;

    private final SOAPClient myDispatch = new SOAPClient("TBEDispatcherAPI");

    TaleoLocation(String companyCode) throws IOException {

        myAccessPointURL = new URL(getURL(companyCode));
        
//        System.out.println(myAccessPointURL);
    }

    private Document makeSOAPRequest(String destination, Document request)
            throws IOException {

//        System.out.println("SOAP REQUEST: " + destination);
//        System.out.println("=============================================");
//        System.out.println("Request: ");
//        prettyPrintXML(request);
//        System.out.println();

        String action = ((Element) request.getRootElement().getFirstChildElement("Body", "http://schemas.xmlsoap.org/soap/envelope/").getChild(0)).getQualifiedName();

        HttpResponse r = WS.url(destination)
                .setHeader("Content-Type", "text/xml")
                .setHeader("SOAPAction", action).body(request.toXML()).post();

        InputSource source = new InputSource(r.getStream());
        source.setEncoding(r.getEncoding());

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        }
        catch (ParserConfigurationException pce) {
            throw new RuntimeException(pce);
        }

        builder.setEntityResolver(new NoOpEntityResolver());

        org.w3c.dom.Document rawResponse;
        try {
            rawResponse = builder.parse(source);
        }
        catch (SAXException se) {
            throw new RuntimeException(se);
        }

        Document response = DOMConverter.convert(rawResponse);

//        System.out.println("Response: ");
//        prettyPrintXML(response);
//        System.out.println();
//        System.out.println();
//        System.out.println();

        Element root = response.getRootElement();
        Element body = root.getFirstChildElement("Body",
                "http://schemas.xmlsoap.org/soap/envelope/");

        Element fault = body.getFirstChildElement("Fault",
                "http://schemas.xmlsoap.org/soap/envelope/");

        if (fault != null) {
            throw new RuntimeException(
                    fault.getFirstChildElement("faultstring").getValue());
        }
        return response;

    }

    private void prettyPrintXML(Document d) {
        try {
            Serializer serializer = new Serializer(System.out, "ISO-8859-1");
            serializer.setIndent(4);
            serializer.setMaxLength(64);
            serializer.write(d);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public URL getURL(){
    	return myAccessPointURL;
    }
    
    private String getURL(String companyCode) throws IOException {
        Document request = myDispatch.buildSoapEnvelope(
                buildDispatchRequest(companyCode));

        Document response = makeSOAPRequest("https://tbe.taleo.net/MANAGER/dispatcher/servlet/rpcrouter",request);
        
        Element body = response.getRootElement().getFirstChildElement("Body","http://schemas.xmlsoap.org/soap/envelope/");

        Element urlResponse = body.getFirstChildElement("getURLResponse","urn:TBEDispatcherAPI");
        Element returnValue = urlResponse.getFirstChildElement("return");

        return returnValue.getChild(0).getValue();
    }
    
    private Element buildDispatchRequest(String orgCode) {
        Element orgCodeElement = new Element("orgCode");
        orgCodeElement.addAttribute(new Attribute("xsi:type", "http://www.w3.org/2001/XMLSchema-instance", "xsd:string"));
        orgCodeElement.appendChild(orgCode);

        Element getURL = new Element("urn:getURL", "urn:TBEDispatcherAPI");
        getURL.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
        getURL.addAttribute(new Attribute("soapenv:encodingStyle", "urn:TBEDispatcherAPI", "http://schemas.xmlsoap.org/soap/encoding/"));
        getURL.appendChild(orgCodeElement);

        return getURL;
    }
    
    private class SOAPClient {

        private final String myAPIName;

        public SOAPClient(String apiName) {
            myAPIName = apiName;
        }

        public Document buildSoapEnvelope(Element request) {
            Element body = new Element("soapenv:Body","http://schemas.xmlsoap.org/soap/envelope/");
            body.appendChild(request);

            Element header = new Element("soapenv:Header","http://schemas.xmlsoap.org/soap/envelope/");

            Element envelope = new Element("soapenv:Envelope","http://schemas.xmlsoap.org/soap/envelope/");

            envelope.addNamespaceDeclaration("urn", "urn:" + myAPIName);

            envelope.appendChild(header);
            envelope.appendChild(body);

            return new Document(envelope);
        }
    }
}
