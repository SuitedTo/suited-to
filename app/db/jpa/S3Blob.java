package db.jpa;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.lang.ObjectUtils;
import org.hibernate.HibernateException;
import org.hibernate.type.StringType;
import org.hibernate.usertype.UserType;
import play.Logger;
import play.Play;
import play.db.Model.BinaryField;
import play.jobs.Job;
import play.libs.Codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class S3Blob implements BinaryField, UserType, Serializable {
    public static final String PUBLIC_PATH = Play.configuration.getProperty("s3.bucket.public.path");
    private static final String S3_URL_PART = ".s3.amazonaws.com/";

	public static String s3Bucket;
	public static AmazonS3 s3Client;
	private String bucket;
	private String key;
    private boolean publiclyAvailable;

	public S3Blob() {
	}


    public S3Blob(boolean publiclyAvailable){
        this.publiclyAvailable = publiclyAvailable;
    }

	private S3Blob(String bucket, String s3Key) {
		this.bucket = bucket;
		this.key = s3Key;
	}
	
	public S3Blob(String val) {
		this(val.split("[|]")[0], val.split("[|]")[1]);
	}

	@Override
	public InputStream get() {
		S3Object s3Object = s3Client.getObject(bucket, key);
		return s3Object.getObjectContent();
	}
	
	/**
	 * Verify that this blob is in one of our buckets
	 * 
	 * @return
	 */
	public boolean isInternal(){
		return s3Bucket.equals(bucket);
	}

    /**
     * Generates a temporarily signed URL to the file on S3. The expiration date should typically be very short lived for
     * security purposed
     * @param expirationDate Date when then URL becomes inaccessible
     * @return URL
     */
    public URL getTemporarySignedUrl(Date expirationDate, 
            String suggestedFilename){
        
        GeneratePresignedUrlRequest request = 
                new GeneratePresignedUrlRequest(bucket, key);
        request.setExpiration(expirationDate);
        
        
        if(suggestedFilename != null){
        	ResponseHeaderOverrides header = new ResponseHeaderOverrides();
        	header.setContentDisposition(
                "inline; filename=" + suggestedFilename);
        
        	request.setResponseHeaders(header);
        }
        return s3Client.generatePresignedUrl(request);
    }
    
    /**
     * Generates a temporary signed put URL for the default bucket. The expiration date
     * should typically be very short lived for security purposes
     * 
     * @param expirationDate Optional date when then URL becomes inaccessible.
     * The default is 3 hours from now
     * @return Put request with url and contents descriptor
     */
    public static S3Request getTemporarySignedPutRequest(Date expirationDate, String extension, String contentType){
    	
    	String key = UUID.randomUUID().toString();
    	if(extension != null){
    		key += extension;
    	}

        return getTemporarySignedPutRequest(expirationDate, key, extension, contentType);
    }

    public static S3Request getTemporarySignedPutRequest(Date expirationDate, String key, String extension, String contentType){

        if(key == null) {
            throw new NullPointerException();
        }

        if(expirationDate == null){
            Calendar expiration = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            expiration.add(Calendar.HOUR, 3);
            expirationDate = expiration.getTime();
        }

        GeneratePresignedUrlRequest request =
                new GeneratePresignedUrlRequest(s3Bucket, key);
        request.setMethod(HttpMethod.PUT);
        request.setExpiration(expirationDate);
        
        if(contentType != null){
        	request.setContentType(contentType);
        }

        return new S3Request(s3Client.generatePresignedUrl(request).toString(),
                new S3Blob(s3Bucket, key).toString());    }

    public static void UploadObject(URL url) throws IOException
    {
        HttpURLConnection connection=(HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        OutputStreamWriter out = new OutputStreamWriter(
                connection.getOutputStream());
        out.write("This text uploaded as object.");
        out.close();
        int responseCode = connection.getResponseCode();

    }

    public URL getPublicUrl(){
    	StringBuilder urlBuilder = new StringBuilder("http://");
    	urlBuilder.append(bucket);
    	urlBuilder.append(S3_URL_PART);
    	urlBuilder.append(key);
    	try {

    		return new URL(urlBuilder.toString());
    	} catch (MalformedURLException e) {
    		Logger.error("Malformed URL for s3blob: %s", urlBuilder.toString());
    		return null;
    	}
    }
    
    /**
     * Determine whether or not the given url matches a publicly available
     * s3blob
     * 
     * @param url
     * @return
     */
    public static boolean exists(URL url){
    	if ((url != null) && (url.getPath().contains(PUBLIC_PATH))){
    		try{
    			return (s3Client.getObjectMetadata(s3Bucket, url.getPath().substring(1)) != null);
    		}catch(Exception e){
    			return false;
    		}
    	}
    	return false;
    }

    /**
     * Generates a temporarily signed URL to the file on S3 using the default Expiration date of 30 seconds from the
     * current time.
     * @return URL
     */
    public URL getTemporarySignedUrl(String suggestedFilename){
        return getTemporarySignedUrl(generateUrlExpirationDate(), suggestedFilename);
    }

    /**
     * Utility to calculate 30 seconds from the current time in order to set a default expiration date.
     * @return Date
     */
    public static Date generateUrlExpirationDate(){
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 30);
        return calendar.getTime();
    }
	
	/**
	 * @param is
	 * @param type
	 * @return
	 */
	public Job setAsJob(final InputStream is, final String type) {

            return new Job(){
                    public void doJob(){
                            set(is, type);
                    }
            };
	}

	@Override
    public void set(InputStream is, String type) {
        //if the bucket was not explicitly set use the default bucket from the configuration
        if (this.bucket == null) {
            this.bucket = s3Bucket;
        }

        StringBuilder keyBuilder = new StringBuilder();
        if (publiclyAvailable) {
            keyBuilder.append(PUBLIC_PATH);
        }
        keyBuilder.append(Codec.UUID());
        this.key = keyBuilder.toString();

        ObjectMetadata om = new ObjectMetadata();
        om.setContentType(type);
        s3Client.putObject(bucket, key, is, om);
    }

	public void delete () {
		s3Client.deleteObject(s3Bucket, key);
	}
	
	@Override
	public long length() {
		ObjectMetadata om = s3Client.getObjectMetadata(bucket, key);
		return om.getContentLength();
	}

	@Override
	public String type() {
		ObjectMetadata om = s3Client.getObjectMetadata(bucket, key);
		return om.getContentType();
	}

	@Override
	public boolean exists() {
		try{
			ObjectMetadata om = s3Client.getObjectMetadata(bucket, key);
			return om != null;
		}catch(Exception e){
			return false;
		}
	}

	@Override
	public int[] sqlTypes() {
		return new int[] { Types.VARCHAR };
	}

	@Override
	public Class returnedClass() {
		return S3Blob.class;
	}

	@Override
	public boolean equals(Object o, Object o1) throws HibernateException {
		return ObjectUtils.equals(o, o1);
	}

	@Override
	public int hashCode(Object o) throws HibernateException {
		return o.hashCode();
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, Object o) throws HibernateException, SQLException {
        Logger.debug("S3Blob nullSafeGet");
		String val = StringType.INSTANCE.nullSafeGet(rs, names[0]);
		if (val == null || val.length() == 0 || !val.contains("|")) {
            return null;
		}
		return new S3Blob(val.split("[|]")[0], val.split("[|]")[1]);
	}

	@Override
	public void nullSafeSet(PreparedStatement ps, Object o, int i) throws HibernateException, SQLException {
        Logger.debug("S3Blob nullSafeSet");
		if (o != null) {
			ps.setString(i, ((S3Blob) o).toString());
		} else {
			ps.setNull(i, Types.VARCHAR);
		}
	}

	@Override
	public Object deepCopy(Object o) throws HibernateException {
		if (o == null) {
			return null;
		}
		return new S3Blob(((S3Blob)o).bucket, ((S3Blob)o).key);
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	public Serializable disassemble(Object original) throws HibernateException {
        Logger.debug("S3Blob disassemble");
		return (Serializable) deepCopy(original);
	}

	public Object assemble(Serializable cached, Object owner) throws HibernateException {
        Logger.debug("S3Blob assemble");
		return deepCopy(cached);
	}
	
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
        Logger.debug("S3Blob replace");
		return deepCopy(original);
	}
	
	public String toString(){
		return bucket + "|" + key;
	}
	
	public static class S3Request{
		public String url;
		public String contents;
		public S3Request(String url, String contents) {
			this.url = url;
			this.contents = contents;
		}
	}
}
