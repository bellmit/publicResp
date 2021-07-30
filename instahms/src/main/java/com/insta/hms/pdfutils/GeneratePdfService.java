package com.insta.hms.pdfutils;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfWriter;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * The Class GeneratePdfService.
 */
@Service
public class GeneratePdfService {

  /**
   * Generate pdf from image array.
   *
   * @param imageList the image list
   * @return the byte[]
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws DocumentException the document exception
   */
  public byte[] generatePdfFromImageArray(List<String> imageList)
      throws IOException, DocumentException {
    Document document = new Document();
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    PdfWriter.getInstance(document, outStream);
    document.open();
    Image img;
    for (String image : imageList) {
      byte[] decodedImage = Base64.decodeBase64(image.getBytes());
      img = Image.getInstance(decodedImage);
      document.setPageSize(img);
      document.newPage();
      img.setAbsolutePosition(0, 0);
      document.add(img);
    }
    document.close();
    byte[] response = outStream.toByteArray();
    outStream.close();
    return response;
  }

}
