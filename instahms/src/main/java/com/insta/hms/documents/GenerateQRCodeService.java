package com.insta.hms.documents;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * The Class GenerateQRCodeService.
 */
@Service
public class GenerateQRCodeService {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(GenerateQRCodeService.class);
  
  /** The Constant DEFAULT_FORMAT. */
  static final String DEFAULT_FORMAT = "QR_CODE";
 
  /** The Constant CHARACTER_FORMAT. */  
  static final String CHARACTER_FORMAT = "ISO-8859-1";

  /** The Constant DEFAULT_WIDTH. */
  static final int DEFAULT_WIDTH = 250;
  
  /** The Constant DEFAULT_HEIGHT. */
  static final int DEFAULT_HEIGHT = 250;
  
  /** The Constant IMAGE_FORMAT. */
  static final String IMAGE_FORMAT = "png";
  
  /**
   * Gets the QR code.
   *
   * @param params the params
   * @return the QR code
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws WriterException the writer exception
   */
  public static String getQRCode(Map<String, Object> params)
      throws IOException, WriterException {
    return createQRCode(params);  
  }

  /**
   * Creates the QR code.
   *
   * @param params the params
   * @return the string
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws WriterException the writer exception
   */
  private static String createQRCode(Map<String, Object> params)
      throws IOException, WriterException {
    String binary = null;
    HashMap<EncodeHintType, Object> hints = new HashMap();
    String contents = (String) (!StringUtils.isEmpty((String) params.get("text")) 
          ? params.get("text")  : "") ;
    int width = (params.get("width") != null
          ? Integer.valueOf((String) params.get("width")) : DEFAULT_WIDTH);
    int height = (params.get("height") != null
          ? Integer.valueOf((String) params.get("height")) : DEFAULT_HEIGHT);
    String barCodeFormat = (String) 
          (!StringUtils.isEmpty((String) params.get("barcode_format"))
          ? params.get("barcode_format") : DEFAULT_FORMAT);
      
    hints.put(EncodeHintType.CHARACTER_SET, CHARACTER_FORMAT);
    
    BitMatrix bitMatrix = new MultiFormatWriter().encode(
              contents, BarcodeFormat.valueOf(barCodeFormat), width, height, hints);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BufferedImage image = toBufferedImage(bitMatrix);
    ImageIO.write(image, IMAGE_FORMAT, out);
    byte[] bytes = out.toByteArray();

    binary = Base64.encodeBase64String(bytes);
    return binary;
  }
  
  /**
   * To buffered image.
   *
   * @param matrix the matrix
   * @return the buffered image
   */
  public static BufferedImage toBufferedImage(BitMatrix matrix) {
    int width = matrix.getWidth();
    int height = matrix.getHeight();
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
      }
    }
    return image;
  }

}
