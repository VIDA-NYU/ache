package focusedCrawler.memex.cdr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class S3Uploader {
    
    private final AmazonS3 s3client;
    private String bucketName = "";
    
    public S3Uploader(String access_key_id, String secret_key_id, String bucketName) {
        System.out.println("S3 Access Key: "+access_key_id);
        System.out.println("S3 Secret Key ID: "+secret_key_id);
        System.out.println("S3 Bucket Name: "+bucketName);
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(access_key_id, secret_key_id);
        this.s3client = AmazonS3ClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                        .withRegion("us-east-1")
                        .build();
        this.bucketName = bucketName;
        System.out.println("Initializing S3 Uploader");
    }

 
    public String upload(String keyName, byte[] content) throws IOException { 
         try { 
            InputStream is = new ByteArrayInputStream(content);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(content.length);
            PutObjectRequest put = new PutObjectRequest(this.bucketName, keyName, is, metadata);
            s3client.putObject(put);
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
