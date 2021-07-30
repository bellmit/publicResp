package com.insta.hms.pdfutils;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.annotations.LazyAutowired;
import com.lowagie.text.DocumentException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(UrlRoutes.PDF_UTILS)
public class GeneratePdfController extends BaseController {

  @LazyAutowired
  GeneratePdfService generatePdfService;

  /**
   * Generate pdf document.
   *
   * @param imageList the image list
   * @param response the response
   * @return the response entity
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws DocumentException the document exception
   */
  @RequestMapping(value = UrlRoutes.GENERATE_PDF , method = RequestMethod.POST)
  public ResponseEntity<byte[]> generatePdfDocument(
      @RequestParam("image_list") List<String> imageList, HttpServletResponse response)
      throws IOException, DocumentException {
    byte[] data = generatePdfService.generatePdfFromImageArray(imageList);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("application/pdf"));
    String filename = "output.pdf";
    headers.set("Content-Disposition", "inline; filename='" + filename + "'");
    return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
  }

}
