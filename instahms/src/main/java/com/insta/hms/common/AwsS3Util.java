package com.insta.hms.common;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.SSECustomerKey;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.minio.MinioPatientDocumentsRepository;
import com.insta.hms.common.minio.MinioSseRepository;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.common.utils.EnvironmentUtil;
import com.insta.hms.exception.FileStoreIOException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

/**
 * The Class AwsS3Util.
 */
@Component
@Scope("prototype")
public class AwsS3Util {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(AwsS3Util.class);

  /** The session service. */
  @LazyAutowired
  SessionService sessionService;

  /** The minio patient doc repo. */
  @LazyAutowired
  MinioPatientDocumentsRepository minioPatientDocRepo;

  /** The minio sse repository. */
  @LazyAutowired
  MinioSseRepository minioSseRepository;

  /** The s 3 client. */
  AmazonS3 s3Client = null;

  /**
   * Establish S 3 connection.
   */
  @PostConstruct
  public void establishS3Connection() {
    if (!EnvironmentUtil.isMinioEnabled()) {
      return;
    }
    String minioUrl = EnvironmentUtil.getMinioURL();
    Properties awsCredentials = EnvironmentUtil.getAWSCredentialsAndRegion();
    AWSCredentials credentials = new BasicAWSCredentials(
        awsCredentials.getProperty("MINIO_ACCESS_KEY"),
        awsCredentials.getProperty("MINIO_SECRET_KEY"));
    String region = awsCredentials.getProperty("MINIO_REGION");
    ClientConfiguration clientConfiguration = new ClientConfiguration();
    clientConfiguration.setSignerOverride("AWSS3V4SignerType");

    s3Client = AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(minioUrl, region))
        .withPathStyleAccessEnabled(true).withClientConfiguration(clientConfiguration)
        .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
  }

  /**
   * Gets the document.
   *
   * @param docId
   *          the doc id
   * @return the document
   */
  public byte[] getDocument(Integer docId) {
    byte[] byteArray = null;
    SSECustomerKey sseKey = new SSECustomerKey(
        (String) minioSseRepository.getRecord().get("sse_key"));
    try {
      List<String> columns = new ArrayList<>();
      columns.add("path");
      List<BasicDynaBean> listDocuments = minioPatientDocRepo.listAll(columns, "doc_id", docId);
      // assuming there is only one result since doc_id is unique
      String path = (String) listDocuments.get(0).get("path");
      GetObjectRequest rangeObjectRequest = new GetObjectRequest(
          EnvironmentUtil.getMinioDocumentsBucketName(), path).withSSECustomerKey(sseKey);
      S3Object object = s3Client.getObject(rangeObjectRequest);
      try {
        byteArray = IOUtils.toByteArray(object.getObjectContent());
      } catch (IOException exception) {
        logger.error("Error while converting a minio object to byte array for doc_id:" + docId,
            exception);
        throw new FileStoreIOException();
      }
    } catch (AmazonServiceException amazonServiceException) {
      logger.error("Encountered error while getting object from minio for doc_id:" + docId,
          amazonServiceException);
      logger.debug("Error Message:" + amazonServiceException.getMessage());
      logger.debug("HTTP Status Code: " + amazonServiceException.getStatusCode());
      logger.debug("AWS Error Code:   " + amazonServiceException.getErrorCode());
      logger.debug("Error Type:       " + amazonServiceException.getErrorType());
      throw new FileStoreIOException();
    } catch (AmazonClientException amazonClientException) {
      logger.error("Client unable to connect to document store", amazonClientException);
      throw new FileStoreIOException();
    }
    return byteArray;
  }

  /**
   * Sets the document for patient documents.
   *
   * @param path
   *          the path
   * @param file
   *          the file
   * @return the boolean
   */
  public Boolean setDocumentForPatientDocuments(String path, MultipartFile file) {
    SSECustomerKey sseKey = new SSECustomerKey(
        (String) minioSseRepository.getRecord().get("sse_key"));
    try {
      PutObjectRequest putRequest = new PutObjectRequest(
          EnvironmentUtil.getMinioDocumentsBucketName(), path, convertToFile(file))
              .withSSECustomerKey(sseKey);
      s3Client.putObject(putRequest);
      return true;
    } catch (AmazonServiceException amazonServiceException) {
      logger.error("Unable to upload file", amazonServiceException);
      return false;
    }
  }

  /**
   * Converting a multipart file to java.io.File.
   *
   * @param multipartFile
   *          the multipart file
   * @return the file
   */
  private File convertToFile(MultipartFile multipartFile) {
    File convertedFile = new File(System.getProperty("java.io.tmpdir") + File.separator
        + multipartFile.getOriginalFilename());
    try {
      convertedFile.createNewFile();
      FileOutputStream fos = new FileOutputStream(convertedFile);
      fos.write(multipartFile.getBytes());
      fos.close();
    } catch (IOException exception) {
      logger.error("Error while converting multipart file", exception);
    }
    return convertedFile;
  }
}
