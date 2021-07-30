package com.insta.hms.common;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 * The Class AesEncryption.
 */
public class AesEncryption {

  /** The Constant encryptionKey. */
  private static final String encryptionKey = "864E7B0121F8A93EDD47C68C733C5880";
  // A856346EF73BF011A2A1FF384AD903FE4DADE4B7CCE6C85CB783134C55783890

  /**
   * The main method.
   *
   * @param args the arguments
   * @throws Exception the exception
   */
  public static void main(String[] args) throws Exception {
    byte[] secKeyByte = DatatypeConverter.parseHexBinary(encryptionKey);
    SecretKey secKey = new SecretKeySpec(secKeyByte, 0, secKeyByte.length, "AES");
    byte[] cipherText = encryptText(args[0], secKey);
    System.out.println(DatatypeConverter.printHexBinary(cipherText));
  }

  /**
   * Encrypt text.
   *
   * @param plainText the plain text
   * @param secKey    the sec key
   * @return the byte[]
   * @throws Exception the exception
   */
  public static byte[] encryptText(String plainText, SecretKey secKey) throws Exception {
    Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
    aesCipher.init(Cipher.ENCRYPT_MODE, secKey);
    byte[] byteCipherText = aesCipher.doFinal(plainText.getBytes());
    return byteCipherText;
  }
}
