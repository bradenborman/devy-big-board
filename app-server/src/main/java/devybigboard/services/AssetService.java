package devybigboard.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class AssetService {

    private final S3Client s3Client;
    private final String bucketName;
    private final String endpoint;

    public AssetService(S3Client s3Client, 
                       @Value("${app.s3.bucket-name}") String bucketName,
                       @Value("${app.s3.endpoint}") String endpoint) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.endpoint = endpoint;
    }

    /**
     * Upload an image to S3
     * @param file The image file to upload
     * @param folder Optional folder path (e.g., "players", "logos")
     * @return The filename (not full URL)
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename(), folder);
        String contentType = file.getContentType();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // Return just the filename part (after the last /)
        return fileName.substring(fileName.lastIndexOf('/') + 1);
    }

    /**
     * Upload an image from InputStream
     * @param inputStream The image input stream
     * @param originalFileName Original file name
     * @param contentType MIME type
     * @param contentLength File size
     * @param folder Optional folder path
     * @return The public URL of the uploaded image
     */
    public String uploadImage(InputStream inputStream, String originalFileName, String contentType, long contentLength, String folder) {
        String fileName = generateFileName(originalFileName, folder);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));

        return getPublicUrl(fileName);
    }

    /**
     * Read/Download an image from S3
     * @param fileKey The S3 key/path of the file
     * @return InputStream of the file
     */
    public InputStream readImage(String fileKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        return s3Client.getObject(getObjectRequest);
    }

    /**
     * Delete an image from S3
     * @param fileKey The S3 key/path of the file to delete
     */
    public void deleteImage(String fileKey) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    /**
     * Delete an image by its public URL
     * @param publicUrl The public URL of the image
     */
    public void deleteImageByUrl(String publicUrl) {
        String fileKey = extractKeyFromUrl(publicUrl);
        deleteImage(fileKey);
    }

    /**
     * Check if a file exists in S3
     * @param fileKey The S3 key/path of the file
     * @return true if exists, false otherwise
     */
    public boolean fileExists(String fileKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    private String generateFileName(String originalFileName, String folder) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String uniqueId = UUID.randomUUID().toString();
        
        if (folder != null && !folder.isEmpty()) {
            return folder + "/" + uniqueId + extension;
        }
        return uniqueId + extension;
    }

    private String getPublicUrl(String fileKey) {
        // Railway storage uses the endpoint URL + bucket + key
        return String.format("%s/%s/%s", endpoint, bucketName, fileKey);
    }

    private String extractKeyFromUrl(String publicUrl) {
        // Extract the key from the public URL
        String prefix = String.format("%s/%s/", endpoint, bucketName);
        return publicUrl.replace(prefix, "");
    }
}
