package com.insta.hms.common;

import java.io.InputStream;
import java.util.Properties;

/**
 * The Class ConfigManager.
 */
public class ConfigManager {

  /** The props. */
  private Properties props = new Properties();

  /** The instance. */
  private static ConfigManager _instance = null;

  /** The lock. */
  private static Object LOCK = new Object();

  /**
   * Instantiates a new config manager.
   */
  private ConfigManager() {
    try {
      initialize("apiconfig.properties");
    } catch (Exception ex) {
      throw new RuntimeException("Problem Found in initializing" + ex.getMessage());
    }
  }
  
  /**
   * Gets the single instance of ConfigManager.
   *
   * @return single instance of ConfigManager
   */

  public static synchronized ConfigManager getInstance() {

    if (null == _instance) {
      synchronized (LOCK) {
        if (null == _instance) {
          _instance = new ConfigManager();
        }
      }
    }

    return _instance;

  }

  /**
   * Initialize.
   *
   * @param configFile the config file
   * @throws Exception the exception
   */
  public void initialize(String configFile) throws Exception {
    try {
      InputStream is = Thread.currentThread().getContextClassLoader()
          .getResourceAsStream(configFile);

      this.props.clear();
      this.props.load(is);
    } catch (Exception exception) {
      throw new Exception(exception);
    }
  }

  /**
   * Lookup.
   *
   * @param property the property
   * @return the string
   */
  public String lookup(String property) {
    if (property == null) {
      return null;
    }
    if (this.props.getProperty(property) != null) {
      return this.props.getProperty(property).trim();
    }
    return null;
  }

  /**
   * Gets the props.
   *
   * @return the props
   */
  public Properties getProps() {
    return this.props;
  }
}