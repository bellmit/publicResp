package com.insta.hms.common;

import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.imageretriever.CommonImageRetriever;
import com.insta.hms.imageretriever.ImageRetriever;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.codec.binary.Base64;
import org.apache.struts.util.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.PDFEncryption;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.util.XRRuntimeException;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * The Class HtmlConverter. Class to convert a given html to PDF or text
 */
public class HtmlConverter {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(HtmlConverter.class);

  /** The temp files. */
  private List<File> tempFiles;

  /** The sc. */
  private ServletContext sc;

  /** The retriever. */
  private ImageRetriever retriever;

  /** The renderer. */
  private ITextRenderer renderer = null;

  /** The rtl processor. */
  private RtlProcessor rtlProcessor = null;

  /** The user pdf password. */
  private String userPdfPassword = null;

  /** The Constant LIGATURE_TO_SWAP. */
  // Refer to Bug HMS-17900 for an explanation of why this is required
  private static final char LIGATURE_TO_SWAP = '\u093F'; // DEVANAGARI VOWEL SIGN I, "ि"

  /**
   * Inits the renderer.
   */
  private void initRenderer() {
    if (null != rtlProcessor) {
      // Need to initialize the processor before the fonts are loaded, so that
      // fonts are loaded with appropriate encoding
      rtlProcessor.init();
    }
    try {
      renderer.getFontResolver().addFontDirectory("/usr/share/fonts/truetype/msttcorefonts", true);
      renderer.getFontResolver().addFontDirectory("/usr/share/fonts/truetype/freefont", true);
      addUnicodeFonts("/usr/share/fonts/truetype/unicodefonts");
    } catch (DocumentException exception) {
      // the above has to work: ignore the exception even if it doesn't. It renders properly
      // anyway
      log.error("Error adding fonts to renderer", exception);
    } catch (IOException exception) {
      // the above has to work: ignore the exception even if it doesn't. It renders properly
      // anyway
      log.error("Error adding fonts to renderer", exception);
    }
  }

  /*
   * Constructor: we require the servlet context so that we can find any scripts/css files
   * that are referred in the HTML body.
   */

  /**
   * Instantiates a new html converter.
   */
  public HtmlConverter() {
    this(null);
  }

  /**
   * Instantiates a new html converter.
   *
   * @param retriever the r
   */
  public HtmlConverter(ImageRetriever retriever) { // , boolean supportRTL) {
    this.sc = AppInit.getServletContext();
    this.retriever = (null != retriever) ? retriever : new CommonImageRetriever();
    this.renderer = new ITextRenderer();

    this.rtlProcessor = new RtlProcessor(renderer, true, false);
    tempFiles = new ArrayList();
    // Should be called only after the renderer and processor objects are instantiated.
    initRenderer();
  }

  /**
   * Instantiates a new html converter.
   *
   * @param retriever       the r
   * @param userPdfPassword the user pdf password
   */
  public HtmlConverter(ImageRetriever retriever, String userPdfPassword) {
    this(retriever);
    this.userPdfPassword = userPdfPassword;
  }

  /**
   * Sets the pdf password.
   *
   * @param itextRenderer the new pdf password
   */
  private void setPdfPassword(ITextRenderer itextRenderer) {
    if (userPdfPassword != null) {
      PDFEncryption pdfEncryption = new PDFEncryption();
      pdfEncryption.setUserPassword((userPdfPassword.getBytes()));
      pdfEncryption.setOwnerPassword("InstaPracto".getBytes());
      renderer.setPDFEncryption(pdfEncryption);
    }
  }

  /**
   * Write pdf. Write the bodyContent without wrapping (input is a complete HTML)
   *
   * @param os          the os
   * @param htmlContent the html content
   * @throws IOException       Signals that an I/O exception has occurred.
   * @throws DocumentException the document exception
   * @throws SQLException      the SQL exception
   */
  public void writePdf(OutputStream os, String htmlContent)
      throws IOException, DocumentException, SQLException {
    try {
      htmlContent = processLigatures(htmlContent);
      setPdfPassword(renderer);
      renderer.setDocumentFromString(replaceImages(htmlContent));
      renderer.layout();
      renderer.createPDF(os);
    } catch (XRRuntimeException exception) {
      log.error("Unable to parse document:");
      log.error(htmlContent);
      throw exception;
    } finally {
      cleanupTempFiles();
    }
  }

  /**
   * Write pdf. Write the given bodyContent into a stream after converting it to a PDF
   *
   * @param os            the os
   * @param bodyContent   the body content
   * @param title         the title
   * @param prefs         the prefs
   * @param print         the print
   * @param repeatPHeader the repeat P header
   * @param embedHeader   the embed header
   * @param embedFooter   the embed footer
   * @param isFinal       the is final
   * @param isDuplicate   the is duplicate
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws SQLException             the SQL exception
   * @throws DocumentException        the document exception
   * @throws XPathExpressionException the x path expression exception
   */
  public void writePdf(OutputStream os, String bodyContent, String title, BasicDynaBean prefs,
      boolean print, boolean repeatPHeader, boolean embedHeader, boolean embedFooter,
      boolean isFinal, boolean isDuplicate)
      throws IOException, SQLException, DocumentException, XPathExpressionException {
    writePdf(os, bodyContent, title, prefs, print, repeatPHeader, embedHeader, embedFooter, isFinal,
        isDuplicate, RequestContext.getCenterId());

  }

  /**
   * Write pdf.
   *
   * @param os            the os
   * @param bodyContent   the body content
   * @param title         the title
   * @param prefs         the prefs
   * @param print         the print
   * @param repeatPHeader the repeat P header
   * @param embedHeader   the embed header
   * @param embedFooter   the embed footer
   * @param isFinal       the is final
   * @param isDuplicate   the is duplicate
   * @param centerId      the center id
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws SQLException             the SQL exception
   * @throws DocumentException        the document exception
   * @throws XPathExpressionException the x path expression exception
   */
  public void writePdf(OutputStream os, String bodyContent, String title, BasicDynaBean prefs,
      boolean print, boolean repeatPHeader, boolean embedHeader, boolean embedFooter,
      boolean isFinal, boolean isDuplicate, int centerId)
      throws IOException, SQLException, DocumentException, XPathExpressionException {

    bodyContent = processLigatures(bodyContent);
    Document doc = wrapReportAsDoc(bodyContent, title, prefs, print, repeatPHeader, embedHeader,
        embedFooter, false, isFinal, isDuplicate, centerId);
    log.debug("{}", doc);
    setPdfPassword(renderer);
    renderer.setDocument(doc, null);
    renderer.layout();
    renderer.createPDF(os);
    cleanupTempFiles();
  }

  /**
   * Process ligatures.
   *
   * @param str the str
   * @return the string
   */
  // HACK, HACK -- Refer to HMS-17900 for details
  private String processLigatures(String str) {
    if (null != str) {
      StringBuilder builder = new StringBuilder(str);
      int fromIndex = str.indexOf(LIGATURE_TO_SWAP);
      while (fromIndex > 0) {
        char letter = builder.charAt(fromIndex - 1); // previous char is the actual letter
        builder.setCharAt(fromIndex, letter);
        builder.setCharAt(fromIndex - 1, LIGATURE_TO_SWAP); // swap the positions
        fromIndex = str.indexOf(LIGATURE_TO_SWAP, fromIndex + 1);
      }
      return builder.toString();
    }
    return str;
  }

  /**
   * Gets the pdf bytes. Return the pdf as bytes, given a body content as complete HTML
   *
   * @param htmlContent the html content
   * @return the pdf bytes
   * @throws IOException       Signals that an I/O exception has occurred.
   * @throws DocumentException the document exception
   */
  public byte[] getPdfBytes(String htmlContent) throws IOException, DocumentException {
    htmlContent = processLigatures(htmlContent);
    setPdfPassword(renderer);
    renderer.setDocumentFromString(htmlContent);
    renderer.layout();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    renderer.createPDF(os);
    cleanupTempFiles();
    return os.toByteArray();
  }

  /**
   * Gets the pdf bytes.
   *
   * @param bodyContent   the body content
   * @param title         the title
   * @param prefs         the prefs
   * @param repeatPHeader the repeat P header
   * @param embedHeader   the embed header
   * @param embedFooter   the embed footer
   * @param isFinal       the is final
   * @param isDuplicate   the is duplicate
   * @return the pdf bytes
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws SQLException             the SQL exception
   * @throws DocumentException        the document exception
   * @throws XPathExpressionException the x path expression exception
   */
  public byte[] getPdfBytes(String bodyContent, String title, BasicDynaBean prefs,
      boolean repeatPHeader, boolean embedHeader, boolean embedFooter, boolean isFinal,
      boolean isDuplicate)
      throws IOException, SQLException, DocumentException, XPathExpressionException {

    return getPdfBytes(bodyContent, title, prefs, repeatPHeader, embedHeader, embedFooter, isFinal,
        isDuplicate, RequestContext.getCenterId());
  }

  /**
   * Gets the pdf bytes. Return the pdf as bytes, given a body content, after wrapping the
   * report
   *
   * @param bodyContent   the body content
   * @param title         the title
   * @param prefs         the prefs
   * @param repeatPHeader the repeat P header
   * @param embedHeader   the embed header
   * @param embedFooter   the embed footer
   * @param isFinal       the is final
   * @param isDuplicate   the is duplicate
   * @param centerId      the center id
   * @return the pdf bytes
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws SQLException             the SQL exception
   * @throws DocumentException        the document exception
   * @throws XPathExpressionException the x path expression exception
   */
  public byte[] getPdfBytes(String bodyContent, String title, BasicDynaBean prefs,
      boolean repeatPHeader, boolean embedHeader, boolean embedFooter, boolean isFinal,
      boolean isDuplicate, int centerId)
      throws IOException, SQLException, DocumentException, XPathExpressionException {

    bodyContent = processLigatures(bodyContent);
    Document doc = wrapReportAsDoc(bodyContent, title, prefs, false, repeatPHeader, embedHeader,
        embedFooter, false, isFinal, isDuplicate, centerId);
    setPdfPassword(renderer);
    renderer.setDocument(doc, null);
    renderer.layout();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    renderer.createPDF(os);
    cleanupTempFiles();
    return os.toByteArray();
  }

  /**
   * Gets the text.
   *
   * @param bodyContent the body content
   * @param title       the title
   * @param prefs       the prefs
   * @param embedHeader the embed header
   * @param embedFooter the embed footer
   * @return the text
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws SQLException             the SQL exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException     the transformer exception
   */
  public byte[] getText(String bodyContent, String title, BasicDynaBean prefs, boolean embedHeader,
      boolean embedFooter)
      throws IOException, SQLException, XPathExpressionException, TransformerException {

    return getText(bodyContent, title, prefs, embedHeader, embedFooter,
        RequestContext.getCenterId());
  }

  /**
   * Gets the text.
   *
   * @param bodyContent the body content
   * @param title       the title
   * @param prefs       the prefs
   * @param embedHeader the embed header
   * @param embedFooter the embed footer
   * @param centerId    the center id
   * @return the text
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws SQLException             the SQL exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException     the transformer exception
   */
  public byte[] getText(String bodyContent, String title, BasicDynaBean prefs, boolean embedHeader,
      boolean embedFooter, int centerId)
      throws IOException, SQLException, XPathExpressionException, TransformerException {

    String wrappedHtml = wrapReportAsString(bodyContent, title, prefs, false, false, embedHeader,
        embedFooter, true, false, false, centerId);
    int printcols = (Integer) prefs.get("text_mode_column");
    int extraLines = (Integer) prefs.get("text_mode_extra_lines");
    W3mTransformer transformer = new W3mTransformer(printcols, extraLines);
    return transformer.toText(wrappedHtml.getBytes());
  }

  /**
   * Wrap report as doc. Wrap the report with html and head tags, making it suitable for
   * rendering, esp. for PDFs. Note: This method can create temp files for images. Call
   * cleanupTempFiles after the returned value has been converted to a PDF.
   *
   * @param bodyContent   the body content
   * @param title         the title
   * @param prefs         the prefs
   * @param print         the print
   * @param repeatPHeader the repeat P header
   * @param embedHeader   the embed header
   * @param embedFooter   the embed footer
   * @param textMode      the text mode
   * @param isFinal       the is final
   * @param isDuplicate   the is duplicate
   * @param centerId      the center id
   * @return the document
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws SQLException             the SQL exception
   * @throws XPathExpressionException the x path expression exception
   */
  public Document wrapReportAsDoc(String bodyContent, String title, BasicDynaBean prefs,
      boolean print, boolean repeatPHeader, boolean embedHeader, boolean embedFooter,
      boolean textMode, boolean isFinal, boolean isDuplicate, int centerId)
      throws IOException, SQLException, XPathExpressionException {

    log.debug("Wrapping report content");

    int topMargin = 36;
    int pageWidth = 595;
    int pageHeight = 842;
    int leftMargin = 36;
    int rightMargin = 36;
    int bottomMargin = 36;
    String fontFamily = "Arial Unicode MS";
    String header1 = "";
    String header2 = "";
    String header3 = "";
    int fontSize = 11;

    if (prefs != null) {
      topMargin = ((Integer) prefs.get("top_margin")).intValue();
      pageWidth = ((Integer) prefs.get("page_width")).intValue();
      pageHeight = ((Integer) prefs.get("page_height")).intValue();
      leftMargin = ((Integer) prefs.get("left_margin")).intValue();
      rightMargin = ((Integer) prefs.get("right_margin")).intValue();
      bottomMargin = ((Integer) prefs.get("bottom_margin")).intValue();
      fontFamily = (String) prefs.get("font_name");
      fontSize = ((Integer) prefs.get("font_size")).intValue();
    }

    StringBuffer out = new StringBuffer();

    out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n");

    /*
     * The following is required if we use &nbsp;. xerces parser fails if it is there, but
     * using setDocumentFromString in itextRenderer works OK.
     */
    out.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"")
        .append(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">").append("\n");

    out.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">").append("\n");

    out.append("<head>");

    out.append("<title>").append(ResponseUtils.filter(title)).append("</title>");

    String styleCss = sc.getRealPath("/css/editorstyles.css");
    File cssFile = new File(styleCss);
    out.append("<link rel='stylesheet' type='text/css' href='"
        + cssFile.toURI().toString()
        + "' media='print'/>\n");

    out.append("<style type=\"text/css\">\n");
    out.append("@page { \n");
    out.append("size: ").append(pageWidth).append("pt ").append(pageHeight).append("pt ;\n");
    out.append("margin: ").append(topMargin).append("pt ").append(rightMargin).append("pt ")
        .append(bottomMargin).append("pt ").append(leftMargin).append("pt; \n");

    if (prefs.get("page_number").toString().equals("Y")) {
      String pgNoPosition = prefs.get("pg_no_position").toString().equals("C") ? "center" : "right";

      String pgNoVerticalPosition = "middle";
      if (prefs.get("pg_no_vertical_position").equals("t")) {
        pgNoVerticalPosition = "top";
      } else if (prefs.get("pg_no_vertical_position").equals("b")) {
        pgNoVerticalPosition = "bottom";
      } else {
        pgNoVerticalPosition = "middle";
      }

      int pgNoFontSize = 12;
      if (null != prefs.get("pg_no_font_size") && !prefs.get("pg_no_font_size").equals("")
          && !prefs.get("pg_no_font_size").equals(0)) {
        pgNoFontSize = (Integer) prefs.get("pg_no_font_size");
      }

      out.append("@bottom-"
          + pgNoPosition
          + " {").append("content: \"Page \" counter(page) \" of \" counter(pages);\n")
          .append("vertical-align: "
              + pgNoVerticalPosition
              + ";\n")
          .append("font-size: "
              + pgNoFontSize
              + "pt;")
          .append("}\n");
    }

    String footerVerticalPosition = "middle";
    if (prefs.get("footer_vertical_position").equals("t")) {
      footerVerticalPosition = "top";
    } else if (prefs.get("footer_vertical_position").equals("b")) {
      footerVerticalPosition = "bottom";
    } else {
      footerVerticalPosition = "middle";
    }

    out.append("@bottom-left {").append("content: element(pageFooter);\n").append("vertical-align: "
        + footerVerticalPosition
        + ";\n").append("}\n");

    if (!isFinal) {
      // non-finalized document, add a watermark according to prefs.
      String waterMark = (String) prefs.get("pre_final_watermark");
      if (waterMark != null && !waterMark.equals("")) {
        String bgImage = sc.getRealPath("/images/"
            + waterMark
            + ".png");
        out.append("  background: url("
            + bgImage
            + ") repeat-y center; \n");
      }
    }

    if (isDuplicate) {
      String waterMark = (String) prefs.get("duplicate_watermark");
      if (waterMark != null && !waterMark.equals("")) {
        String bgImage = sc.getRealPath("/images/"
            + waterMark
            + ".png");
        out.append("  background: url("
            + bgImage
            + ") repeat-y center; \n");
      }
    }
    out.append("} \n");
    out.append("</style>");

    if (print) {
      out.append("<script type='text/javascript'>function makePrint(){window.print()}</script>");
    }
    out.append("</head>");

    out.append("<body ");
    out.append("style=\"");
    out.append("font-family: ").append(fontFamily).append("; ");
    out.append("font-size: ").append(fontSize).append("pt; ");
    if (print) {
      out.append("\" onload='makePrint()'>");
    } else {
      out.append("\">");
    }

    String includeLogoHeader = (String) prefs.get("logo_header");

    if (!includeLogoHeader.equals("N") && embedHeader) {
      out.append("<div class=\"header\">\n");

      out.append("<table cellspacing='0' cellpadding='0' width='100%'>\n");

      out.append("<tr>");
      if (includeLogoHeader.equals("L") || includeLogoHeader.equals("Y")) {
        File tmpFile = File.createTempFile("logo_", "");
        tempFiles.add(tmpFile);

        InputStream is = PrintConfigurationsDAO.getLogo(centerId);
        if (is != null && is.available() != 0) {
          writeStreamToFile(is, tmpFile);
          is.close();

          out.append("<td width=\"108\"><img src=\"").append(tmpFile.toURI().toString())
              .append("\" ").append("height='108'/></td>");
        } else {
          is = PrintConfigurationsDAO.getLogo(0);
          if (is != null && is.available() != 0) {
            writeStreamToFile(is, tmpFile);
            is.close();

            out.append("<td width=\"108\"><img src=\"").append(tmpFile.toURI().toString())
                .append("\" ").append("height='108'/></td>");
          }
        }
      }
      if (includeLogoHeader.equals("Y") || includeLogoHeader.equals("H")) {
        if ((String) prefs.get("header1") != null) {
          header1 = (String) prefs.get("header1");
        }
        if ((String) prefs.get("header2") != null) {
          header2 = (String) prefs.get("header2");
        }
        if ((String) prefs.get("header3") != null) {
          header3 = (String) prefs.get("header3");
        }
        out.append("<td align='center' style='white-space: nowrap'>").append("<b><font size='4'>")
            .append(ResponseUtils.filter(header1)).append("</font></b><br/>")
            .append(ResponseUtils.filter(header2)).append("<br/>")
            .append(ResponseUtils.filter(header3)).append("<br/>").append("</td>");
      }
      out.append("</tr>");
      out.append("</table>");

      out.append("</div>\n");
    }

    /*
     * for pdfs: to repeat the footer on all pages( in pdf ) it is required to append this div
     * in the first page of document. beacuse editor styles checking for running footer.
     */
    if (!textMode && embedFooter) {
      appendFooter(out, prefs);
    }

    /*
     * replace refereces to all the images. Append the body, but only after replacing
     * references to any images
     */
    out.append(replaceImages(bodyContent));

    /*
     * for text mode prints: (repeating footer on all pages wont work). Hence, we will add the
     * footer after appending bodycontent.
     */
    if (textMode && embedFooter) {
      appendFooter(out, prefs);
    }

    out.append("</body>");
    out.append("</html>");

    String outString = out.toString();

    log.debug("Wrapped report:");
    log.debug(outString);

    return addOrRemovePatientHeaderFromHeader(outString, repeatPHeader);
  }

  /**
   * Wrap report as string.
   *
   * @param bodyContent   the body content
   * @param title         the title
   * @param prefs         the prefs
   * @param print         the print
   * @param repeatPHeader the repeat P header
   * @param embedHeader   the embed header
   * @param embedFooter   the embed footer
   * @param textMode      the text mode
   * @param isFinal       the is final
   * @param isDuplicate   the is duplicate
   * @param centerId      the center id
   * @return the string
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws SQLException             the SQL exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException     the transformer exception
   */
  public String wrapReportAsString(String bodyContent, String title, BasicDynaBean prefs,
      boolean print, boolean repeatPHeader, boolean embedHeader, boolean embedFooter,
      boolean textMode, boolean isFinal, boolean isDuplicate, int centerId)
      throws IOException, SQLException, XPathExpressionException, TransformerException {
    Document doc = wrapReportAsDoc(bodyContent, title, prefs, print, repeatPHeader, embedHeader,
        embedFooter, textMode, isFinal, isDuplicate, centerId);
    DOMSource domSource = new DOMSource(doc);
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    TransformerFactory tf = TransformerFactory.newInstance();
    tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

    Transformer transformer = tf.newTransformer();
    transformer.transform(domSource, result);

    // log.debug(writer.toString());
    return writer.toString();
  }

  /**
   * Append footer.
   *
   * @param out   the out
   * @param prefs the prefs
   */
  private void appendFooter(StringBuffer out, BasicDynaBean prefs) {

    String footer = (String) prefs.get("footer");
    if (footer.equalsIgnoreCase("Y")) {

      // embeding footers from print master.
      String f1 = ResponseUtils.filter((String) prefs.get("footer1"));
      String f2 = ResponseUtils.filter((String) prefs.get("footer2"));
      String f3 = ResponseUtils.filter((String) prefs.get("footer3"));

      if (f1 != null && !f1.equals("")) {
        out.append("<br/>");
        out.append("<div class=\"footer\">\n");
        out.append("<table cellspacing='0' cellpadding='0' width='100%'>\n");
        out.append("<tr><td>").append(f1).append("<br/>");
        if (f2 != null & !f2.equals("")) {
          out.append(f2).append("<br/>");
        }
        if (f3 != null & !f3.equals("")) {
          out.append(f3).append("<br/>");
        }
        out.append("</td></tr>");
        out.append("</table>\n");
        out.append("</div>\n");
      }
    }
  }

  /**
   * searches for patientHeader and header classes in content. if the user want repeating
   * patient header in all the pages then this method shifts the patientHeader into the
   * header part(if no header class elements present then it creates one with the header
   * class). example: <html> <body> <div class="header"> some header information like logo
   * and address </div><div class="patientHeader">patient information</div> <div>document
   * content</div> </body> </html> for this document if the user want the patientHeader to
   * be repeating(is set printer definitions). then result doument will like <html> <body>
   * <div class="header"> some header information like logo and address
   * <div class="patientHeader">patient information</div> </div> <div>document content</div>
   * </body> </html>
   *
   * @param content       wrapped(using wrapreport) content
   * @param repeatPHeader true: repeats the patient header in all the pages, false: doesn't
   *                      repeats.
   * @return the document
   * @throws XPathExpressionException the x path expression exception
   */
  public static Document addOrRemovePatientHeaderFromHeader(String content, boolean repeatPHeader)
      throws XPathExpressionException {
    Element headerDivEl = null;

    Document doc = null;
    try {
      InputSource is = new InputSource(new BufferedReader(new StringReader(content)));
      doc = XMLResource.load(is).getDocument();
    } catch (XRRuntimeException exception) {
      log.error("Unable to parse document:");
      log.error(content);
      throw exception;
    }

    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();
    /*
     * wrapReport() wraps the report according to xhtml namespace and xhtml dtd. content
     * passed into this method is wrapped using wrapReport(); so we have to set the name space
     * context externally.
     */
    xpath.setNamespaceContext(new XHtmlNameSpace());

    XPathExpression expr = xpath.compile("//xml:div[@class='header']");
    Object result = expr.evaluate(doc, XPathConstants.NODE);
    headerDivEl = (Element) result;
    log.debug("found /headers :"
        + headerDivEl);

    expr = xpath.compile("//xml:div[@class='patientHeader']");
    result = expr.evaluate(doc, XPathConstants.NODESET);
    NodeList pheaderNodes = (NodeList) result;
    log.debug("found Patient Headers :"
        + pheaderNodes.getLength());

    expr = xpath.compile("//xml:body");
    result = expr.evaluate(doc, XPathConstants.NODE);

    Element bodyEl = (Element) result;
    log.debug("found body Element: "
        + bodyEl);

    if (pheaderNodes.getLength() == 0 || bodyEl == null) {
      return doc; // is not wrapped using wrapReport.
    }

    if (repeatPHeader) {
      if (headerDivEl == null) {
        /*
         * creating the div element with class named header when no div element with class is
         * present in document.
         */
        headerDivEl = doc.createElement("div");
        headerDivEl.setAttribute("class", "header");
        Node el = bodyEl.getFirstChild();
        bodyEl.insertBefore(headerDivEl, el);
      }

      /*
       * moves the all the divs (with class name 'patientHeader') to the main div (having class
       * name 'header');
       */
      for (int i = 0; i < pheaderNodes.getLength(); i++) {
        Node pheaderNode = pheaderNodes.item(i);
        pheaderNode.getParentNode().removeChild(pheaderNode);
        headerDivEl.appendChild(pheaderNode);
        log.debug("include header : "
            + headerDivEl.getTextContent());
      }
    }

    return doc;
  }

  /**
   * Wrap report.
   *
   * @param bodyContent the body content
   * @param title       the title
   * @param prefs       the prefs
   * @param textMode    the text mode
   * @param isFinal     the is final
   * @param isDuplicate the is duplicate
   * @return the string
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws SQLException             the SQL exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException     the transformer exception
   */
  public String wrapReport(String bodyContent, String title, BasicDynaBean prefs, boolean textMode,
      boolean isFinal, boolean isDuplicate)
      throws IOException, SQLException, XPathExpressionException, TransformerException {
    return wrapReport(bodyContent, title, prefs, textMode, isFinal, isDuplicate,
        RequestContext.getCenterId());
  }

  /**
   * Wrap report.
   *
   * @param bodyContent the body content
   * @param title       the title
   * @param prefs       the prefs
   * @param textMode    the text mode
   * @param isFinal     the is final
   * @param isDuplicate the is duplicate
   * @param centerId    the center id
   * @return the string
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws SQLException             the SQL exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException     the transformer exception
   */
  public String wrapReport(String bodyContent, String title, BasicDynaBean prefs, boolean textMode,
      boolean isFinal, boolean isDuplicate, int centerId)
      throws IOException, SQLException, XPathExpressionException, TransformerException {
    return wrapReportAsString(bodyContent, title, prefs, false, false, false, false, textMode,
        isFinal, isDuplicate, centerId);
  }

  /**
   * Wrap with root element.
   *
   * @param content the content
   * @return the string
   */
  public static String wrapWithRootElement(String content) {
    StringBuffer html = new StringBuffer();
    html.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n");
    /*
     * The following is required if we use &nbsp;. xerces parser fails if it is there, but
     * using setDocumentFromString in itextRenderer works OK.
     */
    html.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"")
        .append(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">").append("\n");

    html.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">").append("\n");
    html.append("<div class='reportContent'>");
    html.append(content);
    html.append("</div>");
    html.append("</html>");

    return html.toString();
  }

  /**
   * removes the header and patient information and footer div elements from document.
   *
   * @param content the content
   * @return the string
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException     the transformer exception
   */
  public static String deleteHeaderAndFooter(String content)
      throws XPathExpressionException, TransformerException {
    String wrappedContent = wrapWithRootElement(content);
    Document doc = null;
    try {
      InputSource is = new InputSource(new BufferedReader(new StringReader(wrappedContent)));
      doc = XMLResource.load(is).getDocument();
    } catch (XRRuntimeException exception) {
      log.error("Unable to parse document:");
      log.error(wrappedContent);
      throw exception;
    }

    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();

    xpath.setNamespaceContext(new XHtmlNameSpace());

    XPathExpression expr = xpath.compile("//xml:div[@class='header']");
    Object result = expr.evaluate(doc, XPathConstants.NODE);
    Element headerDivEl = (Element) result;

    /*
     * removes the header div element.
     */
    if (headerDivEl != null) {
      headerDivEl.getParentNode().removeChild(headerDivEl);
    }

    expr = xpath.compile("//xml:div[@class='patientHeader']");
    result = expr.evaluate(doc, XPathConstants.NODESET);
    NodeList pheaderNodes = (NodeList) result;

    /*
     * removes the patient information div element
     */
    for (int i = 0; i < pheaderNodes.getLength(); i++) {
      Node pheaderNode = pheaderNodes.item(i);
      pheaderNode.getParentNode().removeChild(pheaderNode);
    }

    expr = xpath.compile("//xml:div[@class='footer']");
    result = expr.evaluate(doc, XPathConstants.NODE);
    Element footerDivEl = (Element) result;

    /*
     * removes the footer div element.
     */
    if (footerDivEl != null) {
      footerDivEl.getParentNode().removeChild(footerDivEl);
    }

    expr = xpath.compile("//xml:div[@class='reportContent']");
    result = expr.evaluate(doc, XPathConstants.NODE);

    TransformerFactory tf = TransformerFactory.newInstance();
    tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    Transformer transformer = tf.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    Element contentDivEl = (Element) result;
    DOMSource domSource = new DOMSource(contentDivEl);
    StringWriter writer = new StringWriter();
    StreamResult resultStream = new StreamResult(writer);
    transformer.transform(domSource, resultStream);

    return writer.toString();
  }

  /**
   * Replace images.
   *
   * @param bodyContent the body content
   * @return the string
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public String replaceImages(String bodyContent) throws IOException, SQLException {
    StringBuffer out = new StringBuffer();
    Pattern pattern = Pattern.compile("img ([^>]*)src=\"([^\"]*)\"");
    Matcher matcher = pattern.matcher(bodyContent);

    while (matcher.find()) {
      String match = matcher.group(0); // img src="url"
      String imgUrl = matcher.group(2); // url
      log.debug("Found image: "
          + match);
      if (imgUrl.contains("report.do?method=getImage")) {
        log.debug("Replacement for hospital print logo");

        File tmpFile = File.createTempFile("logo_", "");
        tempFiles.add(tmpFile);
        int centerId = RequestContext.getCenterId();
        InputStream is = PrintConfigurationsDAO.getLogo(centerId);
        if (is != null && is.available() != 0) {
          writeStreamToFile(is, tmpFile);
          is.close();

          log.debug("Logo Temp file path is: "
              + tmpFile.toURI().toString());
          matcher.appendReplacement(out, "img $1src=\""
              + tmpFile.toURI().toString()
              + "\"");
        } else {
          is = PrintConfigurationsDAO.getLogo(0);
          if (is != null && is.available() != 0) {
            writeStreamToFile(is, tmpFile);
            is.close();

            log.debug("Logo Temp file path is: "
                + tmpFile.toURI().toString());
            matcher.appendReplacement(out, "img $1src=\""
                + tmpFile.toURI().toString()
                + "\"");
          } else {
            // some other kind of URL, keep the original full match in place
            log.warn("Unknown image URL, cannot resolve: "
                + imgUrl);
            matcher.appendReplacement(out, match);
          }
        }

      } else if (imgUrl.contains("GeneralRegistration.do?method=viewPatientPhoto")) {
        imgUrl = imgUrl.replace("&amp;", "&");
        String mrno = imgUrl.split("&")[1].split("=")[1];

        File tmpFile = File.createTempFile("patient_photo_", "");
        tempFiles.add(tmpFile);

        InputStream is = PatientDetailsDAO.getPatientPhoto(mrno);
        if (is != null) {
          writeStreamToFile(is, tmpFile);
          is.close();

          log.debug("Patient Photo Temp file path is: "
              + tmpFile.toURI().toString());
          matcher.appendReplacement(out, "img $1src=\""
              + tmpFile.toURI().toString()
              + "\"");
        } else {
          // some other kind of URL, keep the original full match in place
          log.warn("Unknown image URL, cannot resolve: "
              + imgUrl);
          matcher.appendReplacement(out, match);
        }

      } else if (imgUrl.contains("GeneralRegistration.do?method=viewInsuranceCardImage")) {
        imgUrl = imgUrl.replace("&amp;", "&");
        String patientId = imgUrl.split("&")[1].split("=")[1];

        File tmpFile = File.createTempFile("_plan_card", "");
        tempFiles.add(tmpFile);

        InputStream is = PatientDetailsDAO.getCurrentPatientCardImage(patientId, "I");
        if (is != null) {
          writeStreamToFile(is, tmpFile);
          is.close();

          log.debug("Plan Card Image file path is: "
              + tmpFile.toURI().toString());
          matcher.appendReplacement(out, "img $1src=\""
              + tmpFile.toURI().toString()
              + "\"");
        } else {
          // some other kind of URL, keep the original full match in place
          log.warn("Unknown image URL, cannot resolve: "
              + imgUrl);
          matcher.appendReplacement(out, match);
        }
      } else if (imgUrl.contains("GeneralRegistration.do?method=viewCorporateCardImage")) {
        imgUrl = imgUrl.replace("&amp;", "&");
        String patientId = imgUrl.split("&")[1].split("=")[1];

        File tmpFile = File.createTempFile("_corporate_card", "");
        tempFiles.add(tmpFile);

        InputStream is = PatientDetailsDAO.getCurrentPatientCardImage(patientId, "C");
        if (is != null) {
          writeStreamToFile(is, tmpFile);
          is.close();

          log.debug("Plan Card Image file path is: "
              + tmpFile.toURI().toString());
          matcher.appendReplacement(out, "img $1src=\""
              + tmpFile.toURI().toString()
              + "\"");
        } else {
          // some other kind of URL, keep the original full match in place
          log.warn("Unknown image URL, cannot resolve: "
              + imgUrl);
          matcher.appendReplacement(out, match);
        }
      } else if (imgUrl.contains("GeneralRegistration.do?method=viewNationalCardImage")) {
        imgUrl = imgUrl.replace("&amp;", "&");
        String patientId = imgUrl.split("&")[1].split("=")[1];

        File tmpFile = File.createTempFile("_national_card", "");
        tempFiles.add(tmpFile);

        InputStream is = PatientDetailsDAO.getCurrentPatientCardImage(patientId, "N");
        if (is != null) {
          writeStreamToFile(is, tmpFile);
          is.close();

          log.debug("Plan Card Image file path is: "
              + tmpFile.toURI().toString());
          matcher.appendReplacement(out, "img $1src=\""
              + tmpFile.toURI().toString()
              + "\"");
        } else {
          // some other kind of URL, keep the original full match in place
          log.warn("Unknown image URL, cannot resolve: "
              + imgUrl);
          matcher.appendReplacement(out, match);
        }
      } else if (imgUrl.contains("data:image")) {
        byte[] decodedBytes = Base64.decodeBase64(imgUrl.split(",")[1].getBytes());
        InputStream is = new ByteArrayInputStream(decodedBytes);

        if (is != null) {
          File tmpFile = File.createTempFile("image_", "");
          tempFiles.add(tmpFile);

          writeStreamToFile(is, tmpFile);
          is.close();

          log.debug("replace image path : "
                  + tmpFile.toURI().toString());
          matcher.appendReplacement(out, "img $1src=\""
                  + tmpFile.toURI().toString()
                  + "\"");
        }
      } else if (retriever != null) {
        InputStream is = retriever.retrieve(imgUrl);
        if (is != null) {
          File tmpFile = File.createTempFile("patient_", "");
          tempFiles.add(tmpFile);

          writeStreamToFile(is, tmpFile);
          is.close();

          // log.debug("Patient general Image file path is: " + tmpFile.getAbsolutePath());
          log.debug("replace image path : "
              + tmpFile.toURI().toString());
          matcher.appendReplacement(out, "img $1src=\""
              + tmpFile.toURI().toString()
              + "\"");

        } else {
          log.warn("Retriever could not match the image: "
              + imgUrl);
          matcher.appendReplacement(out, match);
        }

      } else {
        // some other kind of URL, keep the original full match in place
        log.warn("Unknown image URL, cannot resolve: "
            + imgUrl);
        matcher.appendReplacement(out, match);
      }

    }
    matcher.appendTail(out);
    // background image pattern
    Pattern bip = Pattern
        .compile("div ([^>]*)style=\"([^\"]*)background-image: url\\('?([^'\\)]*)'?\\)([^\"]*)\"");
    // pass the images replaced content to replace the background images.
    Matcher bim = bip.matcher(out.toString());
    StringBuffer bout = new StringBuffer();
    while (bim.find()) {
      // div style="background-image: url('')" or div style="background-image: url()"
      String match = bim.group(0);
      String imgUrl = bim.group(3); // url
      if (imgUrl.contains(".do?")) {
        log.debug("Found background image: "
            + match);
        InputStream is = retriever.retrieve(imgUrl);
        if (is != null) {
          File tmpFile = File.createTempFile("patient_", "");
          tempFiles.add(tmpFile);

          writeStreamToFile(is, tmpFile);
          is.close();

          log.debug("Patient background Image file path is: "
              + tmpFile.toURI().toString());
          bim.appendReplacement(bout, "div $1style=\"$2background-image: url('"
              + tmpFile.toURI().toString()
              + "')$4\"");

        } else {
          log.warn("Retriever could not match the image: "
              + imgUrl);
          bim.appendReplacement(bout, match);
        }
      } else { // image from disk
        bim.appendReplacement(bout, match);
      }
    }
    bim.appendTail(bout);
    return bout.toString();
  }

  /**
   * Write stream to file.
   *
   * @param is   the is
   * @param file the f
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void writeStreamToFile(InputStream is, File file) throws IOException {
    OutputStream os = new FileOutputStream(file);
    byte[] buf = new byte[4096];
    int len = 0;
    while ((len = is.read(buf)) > 0) {
      os.write(buf, 0, len);
    }
    os.close();
  }

  /**
   * Write fileto stream.
   *
   * @param is the is
   * @param os the os
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void writeFiletoStream(InputStream is, OutputStream os) throws IOException {
    byte[] buf = new byte[4096];
    int len = 0;
    while ((len = is.read(buf)) > 0) {
      os.write(buf, 0, len);
    }
    os.close();
  }

  /**
   * Cleanup temp files.
   */
  public void cleanupTempFiles() {
    /*
     * for (File f : tempFiles) { f.delete(); } tempFiles = new ArrayList();
     */
  }

  /**
   * The Class XHtmlNameSpace.
   */
  static class XHtmlNameSpace implements NamespaceContext {

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(String prefix) {
      log.debug("http://www.w3.org/1999/xhtml");
      if (prefix.equals("xml")) {
        return "http://www.w3.org/1999/xhtml";
      } else {
        return XMLConstants.NULL_NS_URI;
      }

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
     */
    public String getPrefix(String uri) {
      throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     */
    public Iterator getPrefixes(String uri) {
      throw new UnsupportedOperationException();
    }

  }

  /**
   * Text to PDF.
   *
   * @param textContent the text content
   * @param os          the os
   * @param prefs       the prefs
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws DocumentException        the document exception
   * @throws SQLException             the SQL exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException     the transformer exception
   */
  public void textToPDF(String textContent, OutputStream os, BasicDynaBean prefs)
      throws IOException, DocumentException, SQLException, XPathExpressionException,
      TransformerException {

    int topMargin = 36;
    int pageWidth = 595;
    int pageHeight = 842;
    int leftMargin = 36;
    int rightMargin = 36;
    int bottomMargin = 36;
    String fontFamily = "Arial Unicode MS";
    int fontSize = 11;

    if (prefs != null) {
      topMargin = ((Integer) prefs.get("top_margin")).intValue();
      pageWidth = ((Integer) prefs.get("page_width")).intValue();
      pageHeight = ((Integer) prefs.get("page_height")).intValue();
      leftMargin = ((Integer) prefs.get("left_margin")).intValue();
      rightMargin = ((Integer) prefs.get("right_margin")).intValue();
      bottomMargin = ((Integer) prefs.get("bottom_margin")).intValue();
      fontFamily = (String) prefs.get("font_name");
      fontSize = ((Integer) prefs.get("font_size")).intValue();
    }

    StringBuffer strOut = new StringBuffer();
    strOut.append("<html>");
    strOut.append("<head>");
    strOut.append("<style type=\"text/css\">\n");
    strOut.append("@page { \n");
    strOut.append("size: ").append(pageWidth).append("pt ").append(pageHeight).append("pt ;\n");
    strOut.append("margin: ").append(topMargin).append("pt ").append(rightMargin).append("pt ")
        .append(bottomMargin).append("pt ").append(leftMargin).append("pt; \n");
    strOut.append("} \n");
    strOut.append("</style>");
    strOut.append("</head>");
    strOut.append("<body ");
    strOut.append(" style=\"");
    strOut.append("font-family: ").append(fontFamily).append("; ");
    strOut.append("font-size: ").append(fontSize).append("pt; ");
    strOut.append("\" >");

    strOut.append("<pre>");
    strOut.append(ResponseUtils.filter(textContent));
    strOut.append("</pre>");
    strOut.append("</body>");
    strOut.append("</html>");

    ITextRenderer renderer = new ITextRenderer();
    renderer.setDocumentFromString(strOut.toString());
    renderer.layout();
    renderer.createPDF(os);

  }

  private void addUnicodeFonts(String directoryPath) throws DocumentException, IOException {
    File fontDirectory = new File(directoryPath);
    ITextFontResolver fontResolver = renderer.getFontResolver();
    if (!fontDirectory.isDirectory()) {
      return;
    }
    for (File file : fontDirectory.listFiles()) {
      String fileName = file.getName().toLowerCase();
      if (fileName.endsWith(".otf") || fileName.endsWith(".ttf")) {
        fontResolver.addFont(file.getAbsolutePath(), BaseFont.IDENTITY_H, true);
      }
    }
  }
}
