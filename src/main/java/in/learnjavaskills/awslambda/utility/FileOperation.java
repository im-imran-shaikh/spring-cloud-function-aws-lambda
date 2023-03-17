package in.learnjavaskills.awslambda.utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ProgressListener;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

import in.learnjavaskills.awslambda.dto.FileDetailsDTO;
import in.learnjavaskills.awslambda.exception.FileOperationException;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public final class FileOperation 
{
	
	private final String FILE_VALIDATION_ERROR = "File can't be empty or null in the file detailsDTO";
	private final String ERROR_MESSAGE = "Something went wrong in uploading file into the amazon s3 bucket, Inspect manually";
	private final String FILE_CREATION_ERROR = "Something went wrong in writing content into file";
	
	/**
	 * this this method if the file size is less then 5 MB
	 * @param fileDetailsDto - details of the file to upload into the  aws s3 
	 * @return true if file uploaded successfully else false
	 */
	public boolean uplaodToS3(final FileDetailsDTO fileDetailsDto)
	{	
		try
		{
			if (Objects.isNull(fileDetailsDto) || Objects.isNull(fileDetailsDto.getFile()) || !fileDetailsDto.getFile().isPresent())
				throw new FileOperationException(FILE_VALIDATION_ERROR);
			
			File file = fileDetailsDto.getFile().get();
			log.info("Started uploading file : " + file.getAbsolutePath() + 
					" into s3 bucket : " + fileDetailsDto.getBucketName() + " in s3 key : " + fileDetailsDto.getKey());
			
			final AmazonS3 amazonS3 = getAmazonS3(fileDetailsDto.getAccessKey(), fileDetailsDto.getSecretKey());
			
			PutObjectRequest putObjectRequest = new PutObjectRequest(fileDetailsDto.getBucketName(),
					fileDetailsDto.getKey() + File.separator + file.getName(), file);
			
			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.addUserMetadata("source", "aws-lambda-function");
		
			putObjectRequest.setMetadata(objectMetadata);
			
			
			amazonS3.putObject(putObjectRequest);
			
			log.info("File upload completted");
			return true;
		} 
		catch (AmazonServiceException exception) { log.error(ERROR_MESSAGE, exception); }
		catch (SdkClientException exception) { log.error(ERROR_MESSAGE, exception); }
		catch (Exception exception) { log.error(ERROR_MESSAGE, exception); }
		return false;
	}
	
	/**
	 * Multipart upload threshold specifies the size, in bytes, above which the upload should be performed as multipart upload.
	 * Amazon S3 imposes a minimum part size of 5 MB (for parts other than last part), so we have used 5 MB as multipart upload threshold.
	 * 
	 * @param fileDetailsDto
	 */
	@SuppressWarnings("deprecation")
	public boolean multiPartUploadToS3(final FileDetailsDTO fileDetailsDto)
	{
		try
		{
			if (Objects.isNull(fileDetailsDto) || Objects.isNull(fileDetailsDto.getFile()) || !fileDetailsDto.getFile().isPresent())
				throw new FileOperationException(FILE_VALIDATION_ERROR);
			
			File file = fileDetailsDto.getFile().get();
			String fullPathKey = fileDetailsDto.getKey() + File.separator + file.getName();
			log.info("Started uploading file : " + file.getAbsolutePath() + 
					" into s3 bucket : " + fileDetailsDto.getBucketName() + " in s3 key : " + fullPathKey);
			
			final AmazonS3 amazonS3 = getAmazonS3(fileDetailsDto.getAccessKey(), fileDetailsDto.getSecretKey());
			
			TransferManager transferManager = TransferManagerBuilder.standard()
				.withS3Client(amazonS3)
				.withMultipartUploadThreshold( (long) (5 * 1024 * 1025) ) // 5 MB
				.build();
			
			ProgressListener progressListener = progressEvent -> 
				log.info("Uploading progress in byte : " + progressEvent.getBytesTransferred());
				
			PutObjectRequest putObjectRequest = new PutObjectRequest(fileDetailsDto.getBucketName(), fullPathKey, file);
			putObjectRequest.setProgressListener(progressListener);
			
			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.addUserMetadata("source", "aws-lambda-function");
			objectMetadata.addUserMetadata("source server ip address", Utility.getIpAddress());
			objectMetadata.addUserMetadata("Upload Date Time", LocalDateTime.now().toString());
			putObjectRequest.setMetadata(objectMetadata);
			
			Upload upload = transferManager.upload(putObjectRequest);
			log.info("waiting for to complete upload");
			upload.waitForCompletion();
			log.info("File upload completted " + upload.isDone());
			return true;
		} catch (Exception exception) { log.error(ERROR_MESSAGE, exception); }
		return false;
	}
	
	/**
	 * @param basicAwsCredentials
	 * @return AmazonS3
	 */
	@SuppressWarnings("deprecation")
	private AmazonS3 getAmazonS3(final String accessKey, final String secretKey)
	{
		final BasicAWSCredentials basicAwsCredentials = new BasicAWSCredentials(accessKey, secretKey);
		final AWSCredentialsProvider awsCredentialsProvider = new StaticCredentialsProvider(basicAwsCredentials);
		
		return AmazonS3ClientBuilder.standard()
				.withRegion(Regions.US_EAST_1)
				.withCredentials(awsCredentialsProvider)
				.build();
	}
	
	
	/**
	 * Generating a file with the given data
	 * @param content - writing data into the file
	 * @param filePath - absolute path is required
	 * @return Optional of File if filePath non-empty or non null else optional of empty.
	 * @throws IOException 
	 */
	public Optional<File> generateFile(final String content, final String filePath) throws IOException
	{
		log.info("Generationg new file at : " + filePath + " with file content : " + content);
		if (Objects.isNull(filePath) || filePath.isEmpty())
			return Optional.empty();
	
		File file = new File(filePath);
		if (!file.exists())
			file.createNewFile();
		
		try(FileWriter fileWriter = new FileWriter(file))
		{
			fileWriter.write(content);
		}
		catch (Exception exception) { log.error(FILE_CREATION_ERROR, exception); return Optional.empty(); }
		
		log.info("File generation completed and create file at " + file.getAbsolutePath());
		return Optional.of(file);
	}
}
