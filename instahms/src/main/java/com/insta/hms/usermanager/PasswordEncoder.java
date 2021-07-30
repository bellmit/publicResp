package com.insta.hms.usermanager;

import com.insta.hms.common.DatabaseHelper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * The Class PasswordEncoder.
 */
public class PasswordEncoder {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(PasswordEncoder.class);

  /**
   * Encode.
   *
   * @param password
   *          the password
   * @return the string
   */
  public static String encode(String password) {
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    return passwordEncoder.encode(password);
  }

  /**
   * Matches.
   *
   * @param plaintext
   *          the plaintext
   * @param password
   *          the password
   * @param userBean
   *          the user bean
   * @return the boolean
   */
  public static Boolean matches(String plaintext, String password, BasicDynaBean userBean) {
    return matches(null, plaintext, password, userBean);
  }

  /**
   * Matches.
   *
   * @param con
   *          the con
   * @param plaintext
   *          the plaintext
   * @param password
   *          the password
   * @param userBean
   *          the user bean
   * @return the boolean
   */
  public static Boolean matches(Connection con, String plaintext, String password,
      BasicDynaBean userBean) {
    Boolean isEncrypted = (Boolean) userBean.get("is_encrypted");
    if (!isEncrypted) {
      return plaintext.equals(password);
    }
    String encryptionAlgorithm = (String) userBean.get("encrypt_algo");
    if (encryptionAlgorithm.equals("SHA-1")) {
      if (matchesSha1(plaintext, password)) {
        logger.info("New Password is ------------------" + password);
        String updatePassword = "UPDATE U_USER SET EMP_PASSWORD=?,ENCRYPT_ALGO=?, "
            + " PASSWORD_CHANGE_DATE=now() WHERE EMP_USERNAME=? AND EMP_PASSWORD=? ";
        if (con == null) {
          DatabaseHelper.update(updatePassword, encode(plaintext), "BCRYPT",
              (String) userBean.get("emp_username"), (String) userBean.get("emp_password"));
        } else {
          PreparedStatement ps = null;
          try {
            ps = con.prepareStatement(updatePassword);
            ps.setString(1, encode(plaintext));
            ps.setString(2, "BCRYPT");
            ps.setString(3, (String) userBean.get("emp_username"));
            ps.setString(4, (String) userBean.get("emp_password"));
            int rows = ps.executeUpdate();
            if (rows == 0) {
              logger.error("Failed to update password to BCRYPT algo for Apps login");
            }
            ps.close();
          } catch (SQLException ex) {
            logger.error("Failed to update password to BCRYPT algo for Apps login");
          }
        }
        return true;
      }
      return false;
    } else {
      return matches(plaintext, password);
    }
  }

  /**
   * Matches.
   *
   * @param plaintext
   *          the plaintext
   * @param password
   *          the password
   * @return the boolean
   */
  public static Boolean matches(String plaintext, String password) {
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    return passwordEncoder.matches(plaintext, password);
  }

  /**
   * Matches sha 1.
   *
   * @param plaintext
   *          the plaintext
   * @param password
   *          the password
   * @return the boolean
   */
  public static Boolean matchesSha1(String plaintext, String password) {
    return encodeSHA1(plaintext).equals(password);
  }

  /**
   * Encode SHA 1.
   *
   * @param password
   *          the password
   * @return the string
   */
  @SuppressFBWarnings(value = "WEAK_MESSAGE_DIGEST_SHA1", justification = "Usage of SHA-1 already"
      + " deprecated. This implementation is to take care of passwords hashes still in SHA-1"
      + " which are to be validated before switching to BCRYPT")
  private static String encodeSHA1(String password) {
    try {
      String encodedPassword = null;
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      byte[] buffer = password.getBytes();
      md.update(buffer);
      byte[] digest = md.digest();
      encodedPassword = byteToBase64(digest);
      return encodedPassword;
    } catch (NoSuchAlgorithmException exception) {
      logger.debug(exception.getMessage());
    } catch (UnsupportedEncodingException exception) {
      logger.debug(exception.getMessage());
    }
    return null;
  }

  /**
   * Byte to base 64.
   *
   * @param bt
   *          the bt
   * @return the string
   * @throws UnsupportedEncodingException
   *           the unsupported encoding exception
   */
  private static String byteToBase64(byte[] bt) throws UnsupportedEncodingException {
    byte[] returnbyte = Base64.encodeBase64(bt);
    String returnString = new String(returnbyte, "ASCII");
    return returnString;
  }
}
