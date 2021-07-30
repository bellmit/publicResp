package com.insta.hms.fpmodule;

import com.bob.hms.common.MimeTypeDetector;
import com.digitalpersona.uareu.UareUException;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.exception.EntityNotFoundException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

@Service
@SuppressFBWarnings(value = "HARD_CODE_KEY", justification = "To be refactored later")
public class FingerprintService {

  private static Logger log = LoggerFactory.getLogger(FingerprintService.class);
  
  @LazyAutowired
  private FingerprintRepository fingerprintRepository;

  @LazyAutowired
  private SessionService sessionService;

  // This is the class containing the compare method using the DigitalPersona SDK.
  @LazyAutowired
  private FingerprintApiService fingerprintApiService;

  /**
   * This method compares the fingerprints.
   *
   * @param mrNo          MR Number
   * @param file          the file
   * @param purpose       the purpose
   * @param finger        the finger
   * @param visitId       the visit id
   * @param token         the token
   * @return boolean
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public boolean compareFingerprint(String mrNo, MultipartFile file, String purpose, String finger,
      String visitId, String token) throws IOException, SQLException {
    boolean res = false;
    byte[] fp = null;
    if (!file.isEmpty()) {
      fp = file.getBytes();
    }
    String[] attr = { "fpToken" };
    String fpToken = (String) sessionService.getSessionAttributes(attr).get("fpToken");
    if (!fpToken.equals(token)) {
      return false;
    }
    int threshold = (int) fingerprintRepository.getThreshold().get("fingerprint_dp_threshold");
    try {
      res = fingerprintApiService.compare(fp, fingerprintRepository.getFingerPrint(mrNo, finger),
          threshold);
      String userId = (String) sessionService.getSessionAttributes().get("userId");
      int purposeId = fingerprintRepository.getPurposeIdByPurpose(purpose);
      if (res) {
        res = fingerprintRepository.logAuditEntry(mrNo, visitId, purposeId, userId, finger);
      }
    } catch (UareUException ex) {
      log.error("", ex);
    }
    return res;
  }

  /**
   * This function registers the fingerprint of a patient.
   *
   * @param mrNo          MR Number
   * @param fpData        the fp data
   * @param finger        the finger
   * @return boolean
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean addFingerprint(String mrNo, byte[] fpData, String finger)
      throws SQLException, IOException {
    String userId = (String) sessionService.getSessionAttributes().get("userId");
    String contentType = MimeTypeDetector.getMimeUtil().getMimeTypes(fpData).toString()
        .split("/")[1];
    BufferedImage image = toBufferedImage(fpData);
    int type = (image.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
        : BufferedImage.TYPE_INT_ARGB;
    boolean res = fingerprintRepository.addFingerPrint(mrNo, fpData, userId, finger,
        getScaledInstance(image, 80, 88, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true,
            contentType, type));
    if (res) {
      res = fingerprintRepository.logAuditEntry(mrNo, null, 1/* Fingerprint Registration */,
          userId, finger);
    }
    return res;

  }

  /**
   * Returns all the registered fingers for the MR number.
   * 
   * @param mrNo MR Number
   * @return List of fingers registered for given Mr No
   * @throws SQLException the SQL exception
   */
  public List<String> getFingerByMrNo(String mrNo) throws SQLException {
    return fingerprintRepository.getFingerByMrNo(mrNo);
  }

  /**
   * Deletes all fingerprints registered for the MR number.
   * 
   * @param mrNo         MR Number
   * @param finger       the finger
   * @return boolean
   * @throws SQLException the SQL exception
   */
  public boolean deleteFingerprintByMrNo(String mrNo, String finger) throws SQLException {
    boolean res = fingerprintRepository.deleteFingerPrintByMrNo(mrNo, finger);
    String userId = (String) sessionService.getSessionAttributes().get("userId");
    if (res) {
      res = fingerprintRepository.logAuditEntry(mrNo, null, 3/* Fingerprint Delete */, userId,
          finger);
    }
    return res;
  }

  /**
   * Gets the all purpose.
   * 
   * @return list of Purposes
   * @throws SQLException the SQL exception
   */
  public List<String> getAllPurpose() throws SQLException {
    return fingerprintRepository.getAllPurpose();
  }

  /**
   * Converts Bytes to hexadecimal.
   * 
   * @param bytes   bytes to convert
   * @return String
   */
  private String bytesToHex(byte[] bytes) {
    StringBuffer result = new StringBuffer();
    for (byte byt : bytes) {
      result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
    }
    return result.toString().toLowerCase();
  }

  /**
   * Create authentication token for verification.
   * 
   * @param mrNo MR Number
   * @return Map of token
   */
  public Map<String, String> createTokenForCompareFingerprints(String mrNo) {
    Map token = new HashMap<String, String>();
    token.put("mr_no", mrNo);
    String[] attr = { "fpToken" };
    try {
      String salt = "UWlf7bJkfgZ0jrCbSAYBHDSJY6yRCpdDHGFUmvbVl1SjJ9rxO35VmLEVOv6nlQcs";
      String toHash = mrNo + (new Date()).toString();
      Mac sha256HMac = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKey = new SecretKeySpec(salt.getBytes(), "HmacSHA256");
      sha256HMac.init(secretKey);
      byte[] hash = sha256HMac.doFinal(toHash.getBytes());
      String hexDigest = bytesToHex(hash);
      sessionService.setSessionAttribute("fpToken", hexDigest);
      token.put("value", hexDigest);
    } catch (NoSuchAlgorithmException ex) {
      log.error("", ex);
    } catch (InvalidKeyException ex) {
      log.error("", ex);
    }
    return token;
  }

  /**
   * Converts byte[] into BufferedImage
   * 
   * @param photo          byte array containing data on image
   * @return BufferedImage Image to return
   * @throws IOException   Signals that an I/O exception has occurred.
   */
  private static BufferedImage toBufferedImage(byte[] photo) throws IOException {
    ByteArrayInputStream inputStream = new ByteArrayInputStream(photo);
    BufferedImage img = ImageIO.read(inputStream);
    return img;
  }

  /**
   * This method scales the image to a specific size.
   * 
   * @param patientPhoto   Image to be converted
   * @param targetWidth    the target width
   * @param targetHeight   the target height
   * @param hint           the hint
   * @param higherQuality  the higher quality
   * @return Scaled Image as byte array
   * @throws IOException   Signals that an I/O exception has occurred.
   */
  private static byte[] getScaledInstance(BufferedImage img, int targetWidth, int targetHeight,
      Object hint, boolean higherQuality, String contentType, int type) throws IOException {
    BufferedImage ret = (BufferedImage) img;
    if (ret.getHeight() < targetHeight || ret.getWidth() < targetWidth) {
      higherQuality = false;
    }
    int width;
    int height;
    if (higherQuality) {
      // Use multi-step technique: start with original size, then
      // scale down in multiple passes with drawImage()
      // until the target size is reached
      width = img.getWidth();
      height = img.getHeight();
    } else {
      // Use one-step technique: scale directly from original
      // size to target size with a single drawImage() call
      width = targetWidth;
      height = targetHeight;
    }

    do {
      if (higherQuality && width > targetWidth) {
        width /= 2;
        if (width < targetWidth) {
          width = targetWidth;
        }
      }

      if (higherQuality && height > targetHeight) {
        height /= 2;
        if (height < targetHeight) {
          height = targetHeight;
        }
      }

      BufferedImage tmp = new BufferedImage(width, height, type);
      Graphics2D g2 = tmp.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
      g2.drawImage(ret, 0, 0, width, height, null);
      g2.dispose();

      ret = tmp;
    } while (width != targetWidth || height != targetHeight);

    ByteArrayOutputStream bo = new ByteArrayOutputStream();
    ImageIO.write(ret, contentType, bo);
    return bo.toByteArray();
  }

  /**
   * Returns InputStream for obtaining the fingerprint image.
   * 
   * @param mrNo         MR Number
   * @return InputStream
   */
  public InputStream getFingerprintImage(String mrNo) {

    if (null != mrNo) {
      List<BasicDynaBean> fingerprintImageBean = fingerprintRepository.getPhoto(mrNo);
      if (!fingerprintImageBean.isEmpty()) {
        return (InputStream) fingerprintImageBean.get(0).get("fp_thumbnail");
      } else {
        EntityNotFoundException ex = new EntityNotFoundException(
            new String[] { "Patient", "MR Number", mrNo });
        throw ex;
      }
    }

    return null;
  }

}