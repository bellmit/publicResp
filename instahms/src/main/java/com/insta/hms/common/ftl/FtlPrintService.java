package com.insta.hms.common.ftl;

import com.bob.hms.common.Constants;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.documents.PrintConfigurationRepository;
import com.insta.hms.imageretriever.ImageRetriever;
import com.insta.hms.mdm.printerdefinition.PrinterDefinitionService;
import com.lowagie.text.DocumentException;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;


public abstract class FtlPrintService {

  /** The logger. */
  public static Logger logger = LoggerFactory
      .getLogger(FtlPrintService.class);

  /**
   * The Enum ReturnType.
   */
  public enum PrintFormat {
    /** The pdf. */
    PDF,
    /** The pdf bytes. */
    PDF_BYTES,
    /** The text bytes. */
    TEXT_BYTES,
  }

  /** The Constant PRINT_MODE_PDF. */
  public static final String PRINT_MODE_PDF = "P";

  public static final String TEMPLATE_MODE_HTML = "H";
  public static final String TEMPLATE_MODE_TEXT = "T";

  public static final String TEMPLATE_NAME_BUILTIN_HTML = "BUILTIN_HTML";
  public static final String TEMPLATE_NAME_BUILTIN_TEXT = "BUILTIN_TEXT";
  public static final String TEMPLATE_NAME_CUSTOM_HTML = "CUSTOM-BUILTIN_HTML";
  public static final String TEMPLATE_NAME_CUSTOM_TEXT = "CUSTOM-BUILTIN_TEXT";
  
  private static final String CONTENT_TYPE_PDF = "application/pdf";

  // Output parameters to be sent with the text print.
  private static final String TEXT_OUTPUT_PARAM_TEXT_COLUMNS = "textColumns";
  private static final String TEXT_OUTPUT_PARAM_TEXT_REPORT = "textReport";
  private static final String TEXT_OUTPUT_PARAM_TEXT_MODE_COLUMN = "text_mode_column";
  private static final String TEXT_OUTPUT_PARAM_PRINTER_TYPE = "printerType";

  // Printer Type for text prints.
  private static final String TEXT_PRINTER_TYPE_DMP = "DMP";

  private String defaultHtmlPrintFile;
  private String defaultTextPrintFile;
  private String reportTitle;
  private String printType;
  private ImageRetriever imageRetriever;

  /** The printer definition service. */
  @LazyAutowired
  private PrinterDefinitionService printerDefinitionService;

  /**
   * 
   * @param printType
   *          This typically the print category, one of the constants in
   *          PrintConfigurationRepository.
   * @param reportTitle
   *          The main title for the print
   * @param defaultHtmlPrintFile
   *          The template file to be used when the BUILTIN-HTML template is
   *          selected
   * @param defaultTextPrintFile
   *          The template file to be used when the BUILTIN_TEXT template is
   *          selected.
   */
  
  public FtlPrintService(String printType, String reportTitle,
      String defaultHtmlPrintFile, String defaultTextPrintFile) {

    this(printType, reportTitle, defaultHtmlPrintFile, defaultTextPrintFile,
        null);
  }

  /**
   * 
   * @param printType
   *          This typically the print category, one of the constants in
   *          PrintConfigurationRepository.
   * @param reportTitle
   *          The main title for the print
   * @param defaultHtmlPrintFile
   *          The template file to be used when the BUILTIN-HTML template is
   *          selected
   * @param defaultTextPrintFile
   *          The template file to be used when the BUILTIN_TEXT template is
   *          selected.
   * @param imageRetriever
   *          The imageRetriever object instance to be used to render images,
   *          mostly used for logo in the print
   */
  public FtlPrintService(String printType, String reportTitle,
      String defaultHtmlPrintFile, String defaultTextPrintFile,
      ImageRetriever imageRetriever) {
    this.defaultHtmlPrintFile = defaultHtmlPrintFile;
    this.defaultTextPrintFile = defaultTextPrintFile;
    this.reportTitle = reportTitle;
    this.printType = printType;
    this.imageRetriever = imageRetriever;
  }

  protected String getPrintType() {
    return printType;
  }

  protected ImageRetriever getImageRetriever() {
    return imageRetriever;
  }

  protected String getDefaultTextPrintFile() {
    return defaultTextPrintFile;
  }

  protected String getDefaultHtmlPrintFile() {
    return defaultHtmlPrintFile;
  }

  protected String getReportTitle() {
    return reportTitle;
  }

  /**
   * Fetches a list of printer definitions from the database.
   * 
   * @return List - a list of printer definition beans.
   */
  public List<BasicDynaBean> getPrinterSettings() {
    return printerDefinitionService.lookup(true);
  }

  /**
   * Fetches as map of built-in templates.
   * 
   * @return Map - of key / value pairs, key being the template identifier and
   *         value being the display name of the template.
   */
  public Map<String, String> getTemplateNames() {
    Map<String, String> defaultTemplateMap = new HashMap<String, String>();
    defaultTemplateMap.put(TEMPLATE_NAME_BUILTIN_HTML, "Built-in HTML Template");
    defaultTemplateMap.put(TEMPLATE_NAME_BUILTIN_TEXT, "Built-in TEXT Template");
    return defaultTemplateMap;
  }

  /**
   * Gets the default printer settings applicable for this.getPrintType().
   * @return BasicDynaBean - printer definition bean corresponding to the 
   *         default printer for the given print type and center
   */
  public BasicDynaBean getDefaultPrinterSettings() {
    return PrintConfigurationRepository.getPageOptions(getPrintType());
  }

  /**
   * Get the http content-type for the given printer.
   * @param printerId 
   *          The Id of the printer for which print has to be generated.
   * @return
   *          Content-Type header value to be sent in the http response
   *          for the print generated, null if no special header is required.
   */
  public String getPrintContentType(Integer printerId) {
    BasicDynaBean prefs = getPrinterPreferences(printerId);
    PrintFormat format = getPrintFormat(prefs);
    if (format.equals(PrintFormat.PDF)) {
      return CONTENT_TYPE_PDF;
    }
    return null;
  }

  protected PrintFormat getPrintFormat(BasicDynaBean prefs) {
    String printMode = PRINT_MODE_PDF;
    PrintFormat outputFormat = null;

    if (null != prefs) {
      Map beanMap = prefs.getMap();
      if (beanMap.containsKey("print_mode")) {
        printMode = (String) beanMap.get("print_mode");
      }
    }

    if (PRINT_MODE_PDF.equalsIgnoreCase(printMode)) {
      outputFormat = PrintFormat.PDF;
    } else {
      outputFormat = PrintFormat.TEXT_BYTES;
    }
    return outputFormat;
  }

  public byte[] generatePrint(Map<String, Object> documentParams,
      Integer printerId, String templateName, OutputStream os, Map outputMap)
          throws XPathExpressionException, DocumentException,
          TransformerException, SQLException, IOException, TemplateException {
    return generatePrint(documentParams, printerId, templateName, true, os,
        outputMap); // No water mark required
  }

  /**
   * Generate a print for the given document and template for a target printer.
   * 
   * @param documentParams
   *          document ID and other parameters required to fetch the print data,
   *          passed in as a map. This will be passed back to the subclass as an
   *          argument to getTemplateDataMap() method, which the subclass can
   *          utilize in any way to fetch the required data to create the
   *          template token map
   * @param printerId
   *          The Id of the printer for which print has to be generated.
   * @param templateName
   *          The name of the template to be used for the print
   * @param isFinalizedDoc
   *          Flag indicating whether the input document is a final one. If true
   *          no watermark will be printed If false water mark as specified in
   *          the print configuration will be used
   * @param os
   *          Output stream into which PDF print will be sent to. This parameter
   *          will be used only if the output print format is PDF.
   * @param outputMap
   *          Output map into which the printable content will be set. This will
   *          be used set the output only in case of text prints.
   * @return Byte array of printable content. Will be null in case of PDF and
   *         Text Prints. A valid byte array is sent only if the print format
   *         specified is PDF_BYTES.
   * @throws XPathExpressionException
   *           XPath Expression Exception.
   * @throws DocumentException
   *           Document Exception.
   * @throws TransformerException
   *           Transformer Exception.
   * @throws SQLException
   *           SQL Exception.
   * @throws IOException
   *           IO Exception.
   * @throws TemplateException
   *           Template Parsing Exception.
   */
  public byte[] generatePrint(Map<String, Object> documentParams,
      Integer printerId, String templateName, boolean isFinalizedDoc,
      OutputStream os, Map outputMap)
          throws XPathExpressionException, DocumentException,
          TransformerException, SQLException, IOException, TemplateException {

    byte[] bytes = null;
    Writer writer = new StringWriter();

    TemplateDescriptor templateDesc = processFtl(documentParams, templateName,
        writer);
    if (null != templateDesc) {
      bytes = toPrintFormat(printerId, templateDesc, isFinalizedDoc, writer, os,
          outputMap);
    }
    return bytes;
  }

  private TemplateDescriptor processFtl(Map<String, Object> documentParams,
      String templateName, Writer writer)
          throws IOException, TemplateException {

    Map<String, Object> dataMap = getTemplateDataMap(documentParams);
    TemplateDescriptor templateDesc = getTemplateDescriptor(templateName);
    FtlReportGenerator ftlGen = getReportGenerator(templateDesc);
    if (null != ftlGen) {
      try {
        ftlGen.setReportParams(dataMap);
        ftlGen.process(writer);
      } catch (TemplateException te) {
        logger.error(te.getMessage());
        throw te;
      }
      return templateDesc;
    }
    return null;
  }

  public abstract Map<String, Object> getTemplateDataMap(
      Map<String, Object> documentParams);

  private TemplateDescriptor getTemplateDescriptor(String templateName) {
    TemplateDescriptor templateDesc = getDefaultTemplateDescriptor(
        templateName);
    if (null == templateDesc) {
      templateDesc = getCustomTemplateDescriptor(templateName);
    }
    return templateDesc;
  }

  protected TemplateDescriptor getDefaultTemplateDescriptor(
      String templateName) {
    String templateMode = null;
    TemplateDescriptor desc = null;
    if (templateName.equals(TEMPLATE_NAME_BUILTIN_HTML)
        || templateName.equals(TEMPLATE_NAME_CUSTOM_HTML)) {
      templateMode = TEMPLATE_MODE_HTML;
    } else if (templateName.equals(TEMPLATE_NAME_BUILTIN_TEXT)
        || templateName.equals(TEMPLATE_NAME_CUSTOM_TEXT)) {
      templateMode = TEMPLATE_MODE_TEXT;
    }
    String templateFile = getTemplateFileName(templateMode);
    if (null != templateMode && null != templateFile) {
      desc = TemplateDescriptor.getDefaultTemplateDescriptor(templateName,
          templateMode, templateFile);
    }
    return desc;
  }

  protected String getTemplateFileName(String templateMode) {
    if (TEMPLATE_MODE_HTML.equalsIgnoreCase(templateMode)) {
      return getDefaultHtmlPrintFile();
    }

    if (TEMPLATE_MODE_TEXT.equalsIgnoreCase(templateMode)) {
      return getDefaultTextPrintFile();
    }

    return null;
  }

  private TemplateDescriptor getCustomTemplateDescriptor(String templateName) {
    BasicDynaBean templateBean = null;
    templateBean = getCustomTemplateDetails(templateName);
    if (null != templateBean) {
      String mode = getCustomTemplateMode(templateBean.getMap());
      String content = getCustomTemplateContent(templateBean.getMap());
      return TemplateDescriptor.getCustomTemplateDescriptor(templateName, mode,
          content);
    }
    return null;
  }

  protected BasicDynaBean getCustomTemplateDetails(String templateName) {
    return null;
  }

  protected String getCustomTemplateMode(Map<String, Object> templateBeanMap) {
    String templateMode = null;
    if (null != templateBeanMap) {
      templateMode = (String) templateBeanMap.get("template_mode");
    }
    return templateMode;
  }

  protected String getCustomTemplateContent(
      Map<String, Object> templateBeanMap) {
    String templateContent = null;
    if (null != templateBeanMap) {
      templateContent = (String) templateBeanMap.get("template_cotent");
    }
    return templateContent;
  }

  protected FtlReportGenerator getReportGenerator(
      TemplateDescriptor templateDesc) throws IOException {
    FtlReportGenerator ftlGen = null;

    if (null != templateDesc) {
      if (templateDesc.isDefaultTemplate()) {
        ftlGen = new FtlReportGenerator(templateDesc.getTemplateFileName());
      } else {
        String templateContent = templateDesc.getTemplateContent();
        StringReader reader = new StringReader(templateContent);
        ftlGen = new FtlReportGenerator(templateDesc.getTemplateFileName(),
            reader);
      }
    }
    return ftlGen;
  }

  private byte[] toPrintFormat(Integer printerId,
      TemplateDescriptor templateDesc, boolean isFinalizedDoc, Writer writer,
      OutputStream os, Map outputMap) throws IOException, SQLException,
          TransformerException, DocumentException, XPathExpressionException {

    BasicDynaBean prefs = null;
    byte[] bytes = null;

    String title = getReportTitle();

    String templateMode = templateDesc.getTemplateMode();
    prefs = getPrinterPreferences(printerId);
    PrintFormat printFormat = getPrintFormat(prefs);

    if (printFormat.equals(PrintFormat.PDF)) {
      toPdf(prefs, title, isFinalizedDoc, templateMode, writer, os);
    } else if (printFormat.equals(PrintFormat.PDF_BYTES)) {
      bytes = toPdfBytes(prefs, title, isFinalizedDoc, templateMode, writer,
          os);
    } else if (printFormat.equals(PrintFormat.TEXT_BYTES)) {
      toTextBytes(prefs, title, templateMode, writer, outputMap);
    }
    writer.close();
    return bytes;

  }

  protected BasicDynaBean getPrinterPreferences(Integer printerId) {
    BasicDynaBean prefs = PrintConfigurationRepository.getPageOptions(
        getPrintType(), printerId, RequestContext.getCenterId());
    return prefs;
  }

  protected byte[] toPdfBytes(BasicDynaBean prefs, String title,
      boolean isFinal, String templateMode, Writer in, OutputStream out)
          throws XPathExpressionException, DocumentException,
          TransformerException, SQLException, IOException {
    byte[] bytes = null;
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    ImageRetriever imageRetriever = getImageRetriever();
    HtmlConverter hc = new HtmlConverter(imageRetriever);
    Boolean repeatPHeader = ((String) prefs.get(Constants.REPEAT_PATIENT_INFO))
        .equals(Constants.STRING_Y);

    if (templateMode != null && templateMode.equals(TEMPLATE_MODE_TEXT)) {
      hc.textToPDF(in.toString(), stream, prefs);
    } else {
      hc.writePdf(stream, in.toString(), title, prefs, false, repeatPHeader,
          true, true, isFinal, false);
    }
    bytes = stream.toByteArray();
    stream.close();
    return bytes;
  }

  protected void toPdf(BasicDynaBean prefs, String title, boolean isFinal,
      String templateMode, Writer in, OutputStream out)
          throws XPathExpressionException, DocumentException,
          TransformerException, SQLException, IOException {
    ImageRetriever imageRetriever = getImageRetriever();
    HtmlConverter hc = new HtmlConverter(imageRetriever);
    Boolean repeatPHeader = ((String) prefs.get(Constants.REPEAT_PATIENT_INFO))
        .equals(Constants.STRING_Y);
    if (templateMode != null && templateMode.equals(TEMPLATE_MODE_TEXT)) {
      hc.textToPDF(in.toString(), out, prefs);
    } else {
      hc.writePdf(out, in.toString(), title, prefs, false, repeatPHeader, true,
          true, isFinal, false);
    }
  }

  protected void toTextBytes(BasicDynaBean prefs, String title,
      String templateMode, Writer in, Map outputMap)
          throws XPathExpressionException, TransformerException, SQLException,
          IOException {
    String printContent = null;
    ImageRetriever imageRetriever = getImageRetriever();
    HtmlConverter hc = new HtmlConverter(imageRetriever);
    if (templateMode != null && templateMode.equals(TEMPLATE_MODE_TEXT)) {
      printContent = in.toString();
    } else {
      printContent = new String(
          hc.getText(in.toString(), title, prefs, true, true));
    }
    outputMap.put(TEXT_OUTPUT_PARAM_TEXT_REPORT, printContent);
    outputMap.put(TEXT_OUTPUT_PARAM_TEXT_COLUMNS,
        prefs.get(TEXT_OUTPUT_PARAM_TEXT_MODE_COLUMN));
    outputMap.put(TEXT_OUTPUT_PARAM_PRINTER_TYPE, TEXT_PRINTER_TYPE_DMP);

  }

  public static class TemplateDescriptor {

    private static final String TEMPLATE_TYPE_DEFAULT = "default";
    private static final String TEMPLATE_TYPE_CUSTOM = "custom";

    private static final String CUSTOM_TEMPLATE_FILE_PREFIX = "custom_";

    public static TemplateDescriptor getDefaultTemplateDescriptor(
        String templateName, String templateMode, String templateFile) {
      return new TemplateDescriptor(templateMode, templateName,
          TEMPLATE_TYPE_DEFAULT, templateFile, null);
    }

    /**
     * 
     * @param templateName
     *          Name of the custom template for which a template descriptor
     *          should be returned.
     * @param templateMode
     *          The template mode as specified for the custom template record
     *          in the database 
     * @param templateContent
     *          The template content set up in the database.
     * @return
     *          Returns a completely filled template descriptor object
     */
    public static TemplateDescriptor getCustomTemplateDescriptor(
        String templateName, String templateMode, String templateContent) {
      return new TemplateDescriptor(templateMode, templateName,
          TEMPLATE_TYPE_CUSTOM, CUSTOM_TEMPLATE_FILE_PREFIX + templateName,
          templateContent);
    }

    protected TemplateDescriptor(String templateMode, String templateName,
        String templateType, String templateFileName, String templateContent) {
      super();
      this.templateMode = templateMode;
      this.templateName = templateName;
      this.templateType = templateType;
      this.templateFileName = templateFileName;
      this.templateContent = templateContent;
    }

    public String getTemplateMode() {
      return templateMode;
    }

    public String getTemplateName() {
      return templateName;
    }

    public String getTemplateType() {
      return templateType;
    }

    public String getTemplateFileName() {
      return templateFileName;
    }

    public String getTemplateContent() {
      return templateContent;
    }

    public boolean isDefaultTemplate() {
      return TEMPLATE_TYPE_DEFAULT.equalsIgnoreCase(getTemplateType());
    }

    private String templateMode; // Text or HTML
    private String templateName; // User given or Default name for the template
    private String templateType; // build-in or custom
    private String templateFileName; // File name for default templates
    private String templateContent; // FTL content for custom template
  }

}
