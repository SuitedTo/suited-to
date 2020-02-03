package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;


import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.converter.WordToHtmlUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import play.Logger;

public class MSWordUtil {
	
	private MSWordUtil(){}

	public static InputStream docToHtml(InputStream is){

		//could use tika (call docxToHtml) for this but poi looks better
		//somehow.
		try{
			HWPFDocumentCore wordDocument = WordToHtmlUtils.loadDoc(is);

			WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
					DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
			wordToHtmlConverter.processDocument(wordDocument);
			Document htmlDocument = wordToHtmlConverter.getDocument();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DOMSource domSource = new DOMSource(htmlDocument);
			StreamResult streamResult = new StreamResult(out);

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer serializer = tf.newTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.setOutputProperty(OutputKeys.METHOD, "html");
			serializer.transform(domSource, streamResult);
			out.close();

			return new ByteArrayInputStream(out.toByteArray());
		}catch(Exception e){
			Logger.error("Unable to convert doc to html %s", e.getMessage());
			return null;
		}
	}
	
	public static InputStream anyToHtml(InputStream is){

	    Parser parser = new AutoDetectParser();

	    StringWriter sw = new StringWriter();
	    SAXTransformerFactory factory = (SAXTransformerFactory)
	             SAXTransformerFactory.newInstance();
	    TransformerHandler handler = null;
		try {
			handler = factory.newTransformerHandler();
			handler.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "html");
		    handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
		    handler.setResult(new StreamResult(sw));
		} catch (TransformerConfigurationException e) {
			Logger.error("Unable to convert document to html %s", e.getMessage());
			return null;
		}
	    try {
	        Metadata metadata = new Metadata();
	        ParseContext context = new ParseContext();
	        parser.parse(is, handler, metadata, context);
	        return new ByteArrayInputStream(sw.toString().getBytes());
	    } catch (Exception e) {
	    	Logger.error("Unable to convert document to html %s", e.getMessage());
			return null;
		}
	}

}
