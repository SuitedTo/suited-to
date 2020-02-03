package db.jpa;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.exceptions.ConfigurationException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;

public class S3Blobs extends PlayPlugin {

	@Override
	public void onApplicationStart() {
		Logger.info("Starting the S3Blobs module");
		if (!Play.configuration.containsKey("aws.access.key")) {
			throw new ConfigurationException("Bad configuration for s3: no access key");
		} else if (!Play.configuration.containsKey("aws.secret.key")) {
			throw new ConfigurationException("Bad configuration for s3: no secret key");
		} else if (!Play.configuration.containsKey("s3.bucket")) {
			throw new ConfigurationException("Bad configuration for s3: no s3 bucket");
		}
		S3Blob.s3Bucket = Play.configuration.getProperty("s3.bucket");
		String accessKey = Play.configuration.getProperty("aws.access.key");
		String secretKey = Play.configuration.getProperty("aws.secret.key");
		AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
		S3Blob.s3Client = new AmazonS3Client(awsCredentials);
        //removed the check for and possible creation of the S3 bucket - we can safely assume that the configured
        //bucket exists and removing this saves some time at application startup
		/*if (!S3Blob.s3Client.doesBucketExist(S3Blob.s3Bucket)) {
			S3Blob.s3Client.createBucket(S3Blob.s3Bucket);
		}*/
	}
}
