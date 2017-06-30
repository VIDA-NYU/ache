package focusedCrawler.memex.cdr;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

public class S3Uploader {
    private final AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());
    private String bucketName = "";
    
    public S3Uploader() {}

    public void init(String access_key_id, String secret_key_id, String bucketName) {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(access_key_id, secret_key_id);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                        .build();
        this.bucketName = bucketName;
        System.out.println("Initializing S3 Uploader");
    }

 
    public String upload(String keyName, byte[] content) throws IOException { 
         try { 
            InputStream is = new ByteArrayInputStream(content);
            s3client.putObject(this.bucketName, keyName, is, new ObjectMetadata());

         } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which " +
                    "means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which " +
                    "means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }

        return "https://s3.amazonaws.com/" + this.bucketName + "/" + keyName;
    }

    public static void main(String args[]) {
        return;
    }
}
