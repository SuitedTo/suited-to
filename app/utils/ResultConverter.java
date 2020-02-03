package utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Hashtable;

import play.libs.IO;
import play.mvc.results.RenderBinary;

public class ResultConverter {
	
	private static Hashtable<String, StreamConverter> converters;
	
	static{
		converters = new Hashtable<String, StreamConverter>();
		converters.put("application/msword", new StreamConverter(){

			@Override
			public InputStream convert(InputStream stream) {
				return MSWordUtil.docToHtml(stream);
			}

			@Override
			public String resultingContentType() {
				return "text/html";
			}
			
		});
		
		converters.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
				new StreamConverter(){

			@Override
			public InputStream convert(InputStream stream) {
				return MSWordUtil.anyToHtml(stream);
			}

			@Override
			public String resultingContentType() {
				return "text/html";
			}
			
		});
		
		converters.put("text/plain",
				new StreamConverter(){

			@Override
			public InputStream convert(InputStream stream) {
				String txt = IO.readContentAsString(stream);
				String html = "<html><body>" + txt.replaceAll("\n", "<br>")  + "</body></html>";
				return new ByteArrayInputStream(html.getBytes());
			}

			@Override
			public String resultingContentType() {
				return "text/html";
			}
			
		});
	}

	private ResultConverter(){}
	
	public static RenderBinary toRenderBinary(InputStream is, String name, String contentType, boolean inline, boolean convert){
		if(!convert){
    		return new RenderBinary(is, name, contentType, inline);
    	}else{
    		StreamConverter converter = converters.get(contentType);
    		if(converter != null){
    			return new RenderBinary(converter.convert(is), name, converter.resultingContentType(), inline);
    		}
    		return new RenderBinary(is, name, contentType, inline);
    	}
	}
	
	interface StreamConverter{
		InputStream convert(InputStream stream);
		String resultingContentType();
	}
}
