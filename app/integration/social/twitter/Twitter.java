package integration.social.twitter;

import integration.social.Provider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.felix.scrplugin.helper.StringUtils;
import org.apache.http.HttpStatus;

import db.jpa.S3Blob;

import play.jobs.Job;
import play.libs.F.Promise;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;
import utils.EncodingUtil;

public class Twitter extends Provider{

	private static final String VERIFY = "https://api.twitter.com/1.1/account/verify_credentials.json";
	private static final String UPDATE = "http://api.twitter.com/1.1/statuses/update.json";
	private static final String UPDATE_WITH_MEDIA = "http://api.twitter.com/1.1/statuses/update_with_media.json";

	public void share(SocialUser socialUser, String title, String comment, String url, String imageUrl){
		if(comment == null){
			return;
		}
		if(imageUrl == null){
			update(socialUser, comment);
		}else{
			update(socialUser, comment, url, imageUrl);
		}
	}

	public void update(SocialUser socialUser, String comment){

		WSRequest request = WS.url(UPDATE + "?status=" + EncodingUtil.encodeURIComponent(comment));
		
		request.setHeader("Content-Type","application/x-www-form-urlencoded");

		request = request.oauth(socialUser.serviceInfo, socialUser.token, socialUser.secret);

		request.postAsync();
	}
	
	public void update(final SocialUser socialUser, String comment, String url, String imageUrl){
		
		WSRequest request = WS.url(UPDATE_WITH_MEDIA);
		
		Map<String,Object> params = new HashMap<String, Object>();
		params.put( "status", comment);
		request.params(params);

		String fileName = null;
		if(!StringUtils.isEmpty(imageUrl)){
			URLConnection connection;
			try {

				//if it's not one of our static files then it must be
				//an s3blob
				File file = new File(imageUrl);
				if(!file.exists()){
					if(imageUrl.contains(S3Blob.PUBLIC_PATH)){
						file = new File("tmp/" +
							imageUrl.substring(imageUrl.indexOf(S3Blob.PUBLIC_PATH) + S3Blob.PUBLIC_PATH.length()));
					} else{
						file = null;
					}
				}

				if(file != null){
					fileName = file.getPath();
					connection = new URL(imageUrl).openConnection();
					InputStream in = connection.getInputStream();


					OutputStream out = new FileOutputStream(file);

					int read = 0;
					byte[] bytes = new byte[1024];

					while ((read = in.read(bytes)) != -1) {
						out.write(bytes, 0, read);
					}

					in.close();
					out.flush();
					out.close();

					request.files(new WS.FileParam(file, "media[]"));
				}

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		request = request.oauth(socialUser.serviceInfo, socialUser.token, socialUser.secret);

		final File file = (fileName != null)?null:new File(fileName);
		final WSRequest fRequest = request;
		new Job(){
			public void doJob(){
				post(fRequest, socialUser, ProviderType.twitter);
				if((file != null) && file.getPath().startsWith("tmp/")){
					file.delete();
				}
			}
		}.now();

	}
	
	public boolean verify(SocialUser socialUser){

		WSRequest request = WS.url(VERIFY);
		
		request = request.oauth(socialUser.serviceInfo, socialUser.token, socialUser.secret);

		return request.get().success();

	}
	
}
