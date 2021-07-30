package com.insta.hms.common.utils;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.exception.ConfigFileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

/**
 * The Class EnvironmentUtil for environment related common methods.
 *
 * @author tanmay.k
 */
public class EnvironmentUtil {

  public static final String URI_IMPORT_CSVS = "uri.import.csvs";

  public static final String URI_EXPORT_CSVS = "uri.export.csvs";

  public static final String HOST_PRACTO_INSTA_SUPPORT = "host.practo.insta.support";
  public static final String HOST_PRACTO_INSTA_DISTRIBUTION = "host.practo.insta.distribution";
  public static final String HOST_PRACTO_INSTA_MONITOR = "host.practo.insta.monitor";
  public static final String HOST_PRACTO_INSTA_SUBSCRIPTION = "host.practo.insta.subscription";

  public static final String INSTA_TMP_DIR = "insta.tmp.dir";

  /** The Constant DISTRIBUTED_IDENTIFIER. */
  private static final String DISTRIBUTED_IDENTIFIER = "distributed";

  /** The Constant ENVIRONMENT_PROPERTIES_PATH. */
  private static final String ENVIRONMENT_PROPERTIES_PATH =
      "/java/resources/environment.properties";

  /** The Constant QUERY_TIMEOUT_KEY. */
  private static final String QUERY_TIMEOUT_KEY = "database.query.timeout";

  /** The Constant MESSAGE_DISPATCHER_TIMEOUT_KEY. */
  private static final String MESSAGE_DISPATCHER_TIMEOUT_KEY = "message.dispatcher.timeout";

  /** The Constant SUPPLIER_RETURNS_DEBIT_NOTE_CSV_EXPORT_PATH. */
  private static final String SUPPLIER_RETURNS_DEBIT_NOTE_CSV_EXPORT_PATH =
      "path.export.supplierreturnswithdebitnotecsvexporterjob";

  /** The environment properties. */
  private static Properties environmentProperties = null;

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(EnvironmentUtil.class);

  /** Instantiates a new environment util. */
  private EnvironmentUtil() {
  }

  /**
   * Checks if is distributed.
   *
   * @return true, if is distributed
   */
  public static boolean isDistributed() {
    return Arrays.asList(getActiveProfiles()).contains(DISTRIBUTED_IDENTIFIER);
  }

  /**
   * Gets the active profiles.
   *
   * @return the active profiles
   */
  public static String[] getActiveProfiles() {
    String[] profiles = null;
    ApplicationContext context = ApplicationContextProvider.getApplicationContext();

    if (null != context) {
      profiles = context.getBean(Environment.class).getActiveProfiles();
    }

    return profiles;
  }

  /**
   * Gets the database query timeout.
   *
   * @return the database query timeout
   */
  public static Integer getDatabaseQueryTimeout() {
    return Integer.parseInt(getEnvironmentProperties().getProperty(QUERY_TIMEOUT_KEY));
  }

  /**
   * Gets the database query timeout.
   *
   * @return the database query timeout
   */
  public static Integer getMessageDispatcherTimeout() {
    return Integer.parseInt(getEnvironmentProperties().getProperty(MESSAGE_DISPATCHER_TIMEOUT_KEY));
  }

  /**
   * Gets the environment properties. Mocks Properties file for a test environment.
   *
   * @return the environment properties
   */
  public static Properties getEnvironmentProperties() {
    if (null == environmentProperties) {
      String[] activeProfiles = getActiveProfiles();

      if (null != activeProfiles && !(Arrays.asList(activeProfiles).contains("test"))) {
        environmentProperties = getPropertiesFile(ENVIRONMENT_PROPERTIES_PATH);
      } else {
        environmentProperties = getTestProperties();
      }
    }
    return environmentProperties;
  }

  /**
   * Gets the test properties.
   *
   * @return the test properties
   */
  private static Properties getTestProperties() {
    Properties testProperties = new Properties();
    testProperties.setProperty(QUERY_TIMEOUT_KEY, "30");
    return testProperties;
  }

  /**
   * Gets the properties file. 
   * TODO - Should we replace this with Spring support for properties and just inject or use
   * application context to get the Properties.
   *
   * @param filePath the file path
   * @return the properties file
   */
  public static Properties getPropertiesFile(String filePath) {
    Properties properties = new Properties();
    InputStream fileStream = null;

    try {
      fileStream = EnvironmentUtil.class.getClassLoader().getResourceAsStream(filePath);
      if (null == fileStream) {
        throw new ConfigFileNotFoundException();
      }

      properties.load(fileStream);
      return properties;
    } catch (IOException exception) {
      throw new ConfigFileNotFoundException(exception);
    } finally {
      if (null != fileStream) {
        try {
          fileStream.close();
        } catch (IOException streamClosingException) {
          logger.error("Error closing ConfigFileInputStream", streamClosingException);
        }
      }
    }
  }

  /**
   * Gets the AWS credentials.
   *
   * @return a @Properties object with keys "MINIO_ACCESS_KEY" and "MINIO_SECRET_KEY"
   */
  public static Properties getAWSCredentialsAndRegion() {
    Properties awsProperties = new Properties();
    awsProperties.setProperty("MINIO_ACCESS_KEY",
        getEnvironmentProperties().getProperty("minio.access.key"));
    awsProperties.setProperty("MINIO_SECRET_KEY",
        getEnvironmentProperties().getProperty("minio.secret.key"));
    awsProperties.setProperty("MINIO_REGION",
        getEnvironmentProperties().getProperty("minio.region"));
    return awsProperties;
  }

  public static String getMinioURL() {
    return getEnvironmentProperties().getProperty("minio.url");
  }

  public static String getMinioDocumentsBucketName() {
    return getEnvironmentProperties().getProperty("minio.documents.bucket.name");
  }

  public static Boolean isMinioEnabled() {
    String minioEnabled = getEnvironmentProperties().getProperty("minio.enabled");
    return minioEnabled != null && minioEnabled.equals("true");
  }

  public static String getCsvImportUri() {
    return getEnvironmentProperties().getProperty(URI_IMPORT_CSVS);
  }

  public static String getCsvExportUri() {
    return getEnvironmentProperties().getProperty(URI_EXPORT_CSVS);
  }
  
  public static String getTempDirectory() {
    return getEnvironmentProperties().getProperty(INSTA_TMP_DIR);
  }

  public static String getSupplierReturnsWithDebitNoteCsvExportPath() {
    return getEnvironmentProperties().getProperty(SUPPLIER_RETURNS_DEBIT_NOTE_CSV_EXPORT_PATH);
  }

  /**
   * Get URL for Insta Subscription Service.
   *
   * @return Java URL Object for insta subscription service
   */
  public static URL getInstaSubscriptionHost() {
    try {
      return new URL(getEnvironmentProperties().getProperty(HOST_PRACTO_INSTA_SUBSCRIPTION));
    } catch (MalformedURLException ex) {
      logger.error("Insta build subscription service host not configured");
      return null;
    }
  }

  /**
   * Get URL for Insta build distribution service.
   *
   * @return Java URL Object for insta build distribution service
   */
  public static URL getInstaDistributionHost() {
    try {
      return new URL(getEnvironmentProperties().getProperty(HOST_PRACTO_INSTA_DISTRIBUTION));
    } catch (MalformedURLException ex) {
      logger.error("Insta build distribution service host not configured");
      return null;
    }
  }

  /**
   * Get URL for Insta application monitoring service.
   *
   * @return Java URL Object for insta application monitoring service
   */
  public static URL getInstaMonitorHost() {
    try {
      return new URL(getEnvironmentProperties().getProperty(HOST_PRACTO_INSTA_MONITOR));
    } catch (MalformedURLException ex) {
      logger.error("Insta application monitoring service host not configured");
      return null;
    }
  }

  /**
   * Get URL for Insta support service.
   *
   * @return Java URL Object for insta support service
   */
  public static URL getInstaSupportHost() {
    try {
      return new URL(getEnvironmentProperties().getProperty(HOST_PRACTO_INSTA_SUPPORT));
    } catch (MalformedURLException ex) {
      logger.error("Insta support service host not configured");
      return null;
    }
  }
}
