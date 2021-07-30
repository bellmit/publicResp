package com.insta.hms.fpmodule;

import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;

import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class FingerprintApiService {

  /**
   * Compare Fingerprints.
   *
   * @param fpData the fp data
   * @param template the template
   * @param threshold the threshold
   * @return true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws UareUException the uare U exception
   */
  public boolean compare(byte[] fpData, byte[] template, int threshold)
      throws IOException, UareUException {

    BufferedImage img = null;
    byte[] fp;
    Fmd fpTemplate = null;

    ByteArrayInputStream bis = new ByteArrayInputStream(fpData);
    img = ImageIO.read(bis);

    fp = pngToRaw(img);
    Fmd finger = UareUGlobal.GetEngine().CreateFmd(fp, img.getWidth(), img.getHeight(), 508, 0,
        123456, Fmd.Format.ANSI_378_2004);

    if (template != null) {
      bis = new ByteArrayInputStream(template);
      img = ImageIO.read(bis);

      fp = pngToRaw(img);
      fpTemplate = UareUGlobal.GetEngine().CreateFmd(fp, img.getWidth(), img.getHeight(), 508, 0,
          123456, Fmd.Format.ANSI_378_2004);
    }

    int res = UareUGlobal.GetEngine().Compare(finger, 0, fpTemplate, 0);
    return (res < threshold);
  }

  private static byte[] pngToRaw(BufferedImage img) {
    // Create a grayscale image of the same size
    BufferedImage gray = new BufferedImage(img.getWidth(), img.getHeight(),
        BufferedImage.TYPE_BYTE_GRAY);

    // Convert the original image to grayscale
    ColorConvertOp op = new ColorConvertOp(img.getColorModel().getColorSpace(),
        gray.getColorModel().getColorSpace(), null);
    op.filter(img, gray);

    // Convert BufferedImage to RAW byte array
    WritableRaster raster = gray.getRaster();
    DataBufferByte data = (DataBufferByte) raster.getDataBuffer();
    return data.getData();

  }

}
