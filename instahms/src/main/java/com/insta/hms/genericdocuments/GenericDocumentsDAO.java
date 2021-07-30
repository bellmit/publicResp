/**
 *
 */

package com.insta.hms.genericdocuments;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.PdfUtils;
import com.insta.hms.common.PrintPageOptions;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.documentpersitence.AbstractDocumentPersistence;
import com.insta.hms.imageretriever.ImageRetriever;
import com.insta.hms.imageretriever.PatientImageRetriever;
import com.insta.hms.imageretriever.VisitWiseImageRetriever;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.vitalForm.genericVitalFormDAO;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfWriter;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericDocumentsDAO.
 *
 * @author krishna.t
 */
public class GenericDocumentsDAO extends GenericDAO {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(GenericDocumentsDAO.class);

  /** The ph template dao. */
  private static PatientHeaderTemplateDAO phTemplateDao = new PatientHeaderTemplateDAO();

  /** The doc date. */
  private static int doc_date = 1;

  /** The none. */
  private static int none = 0;

  /** The sort fileds. */
  private static String[] SORT_FILEDS = { "", "pgd.doc_date" };
  
  private static final GenericDAO patientPdfFormDocValuesDAO =
      new GenericDAO("patient_pdf_form_doc_values");


  /**
   * Instantiates a new generic documents DAO.
   */
  public GenericDocumentsDAO() {
    super("patient_general_docs");
  }

  /** The Constant ALL_GENERAL_DOC_FIELDS. */
  private static final String ALL_GENERAL_DOC_FIELDS =
      "SELECT " + " pgd.doc_name, pgd.doc_id, pgd.doc_date, pgd.username, pgd.patient_id, "
          + " pr.status, pr.reg_date, pr.visit_type, "
          + " (CASE WHEN dat.template_id IS NULL THEN pd.doc_format"
          + " ELSE dat.doc_format END) as doc_format, "
          + " pd.doc_status, dat.template_id, dat.template_name, pd.doc_type,"
          + " dat.status, pd.content_type, "
          + " dt.doc_type_name,dat.access_rights, pd.doc_location,pr.reg_date, pd.center_id ";

  /** The Constant ALL_GENERAL_DOC_TABLES. */
  private static final String ALL_GENERAL_DOC_TABLES =
      "FROM patient_general_docs pgd " + " JOIN patient_documents pd USING (doc_id) "
          + " LEFT JOIN doc_all_templates_view dat USING (doc_format, template_id) "
          + " LEFT JOIN doc_type dt ON (dt.doc_type_id = pd.doc_type) "
          + " LEFT JOIN patient_registration pr USING (patient_id) ";

  /** The Constant ALL_GENERAL_DOCS_WHERE_COND. */
  private static final String ALL_GENERAL_DOCS_WHERE_COND =
      " WHERE (dat.specialized=false or pd.template_id is null) ";

  /** The Constant ALL_GENERAL_DOC_COUNT. */
  public static final String ALL_GENERAL_DOC_COUNT = " select count(pgd.doc_id) ";

  /**
   * Search patient general documents.
   *
   * @param listingParams the listing params
   * @param extraParams the extra params
   * @param specialized the specialized
   * @return the paged list
   * @throws SQLException the SQL exception
   */
  public static PagedList searchPatientGeneralDocuments(Map listingParams, Map extraParams,
      Boolean specialized) throws SQLException {

    int totalRecords = 0;

    SearchQueryBuilder qb = null;

    Connection con = null;
    List list = null;
    String sortColumn = (String) listingParams.get(LISTING.SORTCOL);
    String sortOrder = null;

    if (sortColumn != null && !sortColumn.equals("")) {
      if (sortColumn.equals("doc_date")) {
        sortOrder = SORT_FILEDS[doc_date];
      }
    }
    boolean sortReverse = (Boolean) listingParams.get(LISTING.SORTASC);
    int pageNum = ((Integer) listingParams.get(LISTING.PAGENUM)).intValue();
    int pageSize = ((Integer) listingParams.get(LISTING.PAGESIZE)).intValue();
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      qb = new SearchQueryBuilder(con, ALL_GENERAL_DOC_FIELDS, ALL_GENERAL_DOC_COUNT,
          ALL_GENERAL_DOC_TABLES, ALL_GENERAL_DOCS_WHERE_COND, null, sortOrder, sortReverse,
          pageSize, pageNum);

      qb.addFilter(SearchQueryBuilder.STRING, "pgd.mr_no", "=", extraParams.get("mr_no"));
      // qb.addFilter(SearchQueryBuilder.STRING, "pgd.patient_id", "=",
      // extraParams.get("patient_id"));
      qb.addFilter(SearchQueryBuilder.INTEGER, "pgd.doc_id", "=", extraParams.get("docId"));
      if (!specialized && extraParams.get("center_id") != null
          && (Integer) extraParams.get("center_id") != 0) {
        List temp = new ArrayList();
        temp.add(extraParams.get("center_id"));
        temp.add(0);
        qb.addFilter(SearchQueryBuilder.INTEGER, "pd.center_id", "in", temp);
      }
      try {
        if ((extraParams.get("doc_type_id") != "" && extraParams.get("doc_type_id") != null)) {
          qb.addFilter(SearchQueryBuilder.STRING, "pd.doc_type", "=",
              extraParams.get("doc_type_id"));
        }
        if ((extraParams.get("status") != "" && extraParams.get("status") != null)) {
          qb.addFilter(SearchQueryBuilder.STRING, "pd.doc_status", "IN", extraParams.get("status"));
        }
        if ((extraParams.get("fromDate") != null && extraParams.get("fromDate") != "")) {
          qb.addFilter(SearchQueryBuilder.DATE, "pgd.doc_date::date", ">=",
              DateUtil.parseDate((String) extraParams.get("fromDate")));
        }
        if ((extraParams.get("toDate") != null && extraParams.get("toDate") != "")) {
          qb.addFilter(SearchQueryBuilder.DATE, "pgd.doc_date::date", "<=",
              DateUtil.parseDate((String) extraParams.get("toDate")));
        }
        if ((extraParams.get("doc_name") != null && extraParams.get("doc_name") != "")) {
          qb.addFilter(qb.STRING, "pgd.doc_name", "ilike", extraParams.get("doc_name"));
        }
        if ((extraParams.get("template_name") != null && extraParams.get("template_name") != "")) {
          qb.addFilter(qb.STRING, "dat.template_name", "ilike", extraParams.get("template_name"));
        }
      } catch (ParseException parseExp) {
        log.debug("Exception occured in searchPatientGeneralDocuments method", parseExp);
      } catch (Exception ex) {
        log.debug("Exception occured in searchPatientGeneralDocuments method", ex);
      }
      qb.addSecondarySort("pgd.doc_id");

      qb.build();

      PreparedStatement psData = qb.getDataStatement();
      PreparedStatement psCount = qb.getCountStatement();
      list = DataBaseUtil.queryToDynaList(psData);
      try (ResultSet rsCount = psCount.executeQuery()) {
        if (rsCount.next()) {
          totalRecords = rsCount.getInt(1);
        }
      }
    } finally {
      if (qb != null) {
        qb.close();
      }
      DataBaseUtil.closeConnections(con, null);
    }
    return new PagedList(list, totalRecords, pageSize, pageNum);
  }

  /** The Constant ALL_PATIENT_DOCS. */
  public static final String ALL_PATIENT_DOCS = ALL_GENERAL_DOC_FIELDS + ALL_GENERAL_DOC_TABLES
      + ALL_GENERAL_DOCS_WHERE_COND + " AND pgd.mr_no=? AND pgd.patient_id='' ";

  /**
   * Gets the all patient documents.
   *
   * @param mrNo
   *          the mr no
   * @return the all patient documents
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getAllPatientDocuments(String mrNo) throws SQLException {
    return DataBaseUtil.queryToDynaList(ALL_PATIENT_DOCS, mrNo);
  }

  /** The Constant ALL_VISIT_DOCS_FOR_MR_NO. */
  public static final String ALL_VISIT_DOCS_FOR_MR_NO =
      ALL_GENERAL_DOC_FIELDS + ALL_GENERAL_DOC_TABLES + ALL_GENERAL_DOCS_WHERE_COND
          + " AND pgd.mr_no=? and coalesce(pgd.patient_id, '')!=''";

  /**
   * Gets the all visits docs for mr no.
   *
   * @param mrNo
   *          the mr no
   * @return the all visits docs for mr no
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getAllVisitsDocsForMrNo(String mrNo) throws SQLException {
    return DataBaseUtil.queryToDynaList(ALL_VISIT_DOCS_FOR_MR_NO, mrNo);
  }

  /** The Constant ALL_VISIT_DOCS. */
  public static final String ALL_VISIT_DOCS = ALL_GENERAL_DOC_FIELDS + ALL_GENERAL_DOC_TABLES
      + ALL_GENERAL_DOCS_WHERE_COND + " AND pgd.patient_id=?";

  /**
   * Gets the all visit documents.
   *
   * @param patientId
   *          the patient id
   * @return the all visit documents
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getAllVisitDocuments(String patientId) throws SQLException {
    return DataBaseUtil.queryToDynaList(ALL_VISIT_DOCS, patientId);
  }

  /** The Constant PDF_TEMPLATE_DETAILS_FOR_DOC. */
  public static final String PDF_TEMPLATE_DETAILS_FOR_DOC =
      " SELECT dpft.template_name, dpft.doc_type, dpft.specialized, dpft.template_id "
          + " FROM patient_documents pd JOIN doc_pdf_form_templates dpft USING (template_id)"
          + " WHERE pd.doc_id=?";

  /** The Constant PDF_TEMPLATE_DETAILS. */
  public static final String PDF_TEMPLATE_DETAILS =
      " SELECT dpft.template_name, dpft.doc_type, dpft.specialized, dpft.template_id "
          + " FROM doc_pdf_form_templates dpft WHERE template_id = ? ";

  /**
   * Gets the pdf template details.
   *
   * @param docIdorTemplateId
   *          the doc idor template id
   * @param forTheGivenDoc
   *          the for the given doc
   * @return the pdf template details
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getPdfTemplateDetails(int docIdorTemplateId, boolean forTheGivenDoc)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      if (forTheGivenDoc) {
        ps = con.prepareStatement(PDF_TEMPLATE_DETAILS_FOR_DOC);
      } else {
        ps = con.prepareStatement(PDF_TEMPLATE_DETAILS);
      }
      ps.setInt(1, docIdorTemplateId);

      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Empty.
   *
   * @param params
   *          the params
   * @param key
   *          the key
   * @return true, if successful
   */
  public static boolean empty(Map params, String key) {
    Object[] object = (Object[]) params.get(key);
    if (object != null && object[0] != null && !object[0].equals("")) {
      return false;
    }
    return true;
  }

  /**
   * Delete patient documents.
   *
   * @param con
   *          the con
   * @param format
   *          the format
   * @param docId
   *          the doc id
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean deletePatientDocuments(Connection con, String format, Object docId)
      throws SQLException {

    PatientDocumentsDAO patientdocdao = new PatientDocumentsDAO();
    boolean flag = patientdocdao.delete(con, "doc_id", docId);
    if (flag) {
      if (format.equals("doc_hvf_templates")) {
        flag = false;
        PatientHVFDocValuesDAO hvfValuesDao = new PatientHVFDocValuesDAO();
        flag = hvfValuesDao.delete(con, "doc_id", docId);
      } else if (format.equals("doc_pdf_form_templates")) {
        flag = false;
        flag = patientPdfFormDocValuesDAO.delete(con, "doc_id", docId);
      }
    }
    return flag;
  }

  /**
   * Delete patient general docs.
   *
   * @param con
   *          the con
   * @param format
   *          the format
   * @param docId
   *          the doc id
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean deletePatientGeneralDocs(Connection con, String format, Object docId)
      throws SQLException {

    if (deletePatientDocuments(con, format, docId)) {
      return delete(con, "doc_id", docId);
    }
    return false;
  }

  /**
   * Gets the document bytes.
   *
   * @param docidStr
   *          the docid str
   * @param allFields
   *          the all fields
   * @param mrNo
   *          the mr no
   * @param patientId
   *          the patient id
   * @param printerId
   *          the printer id
   * @return the document bytes
   * @throws SQLException
   *           the SQL exception
   * @throws IllegalArgumentException
   *           the illegal argument exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws DocumentException
   *           the document exception
   * @throws XPathExpressionException
   *           the x path expression exception
   * @throws TransformerException
   *           the transformer exception
   */
  public static byte[] getDocumentBytes(String docidStr, boolean allFields, String mrNo,
      String patientId, int printerId) throws SQLException, IllegalArgumentException, IOException,
      DocumentException, XPathExpressionException, TransformerException {

    byte[] pdfbytes = null;

    if (docidStr == null || docidStr.equals("")) {
      throw new IllegalArgumentException("docidStr is null");
    }
    PatientDocumentsDAO dao = new PatientDocumentsDAO();
    BasicDynaBean patientdocbean = dao.getBean();

    try {
      dao.loadByteaRecords(patientdocbean, "doc_id", Integer.parseInt(docidStr));
    } catch (NumberFormatException nfe) {
      // is captured but not thrown, thrown as a illegal exception in the following statement in
      // both the
      // conditions when docid is not an integer and when document is not exists for an given docid.
    }

    if (patientdocbean.get("doc_id") == null) {
      throw new IllegalArgumentException("Document not found for: " + docidStr);
    }
    String format = patientdocbean.get("doc_format").toString();
    int docid = Integer.parseInt(docidStr);
    BasicDynaBean prefs =
        PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT, printerId);

    if (format.equals("doc_hvf_templates")) {

      Map ftlParamMap = new HashMap();
      Map patientDetails = new HashMap();
      GenericDocumentsFields.copyPatientDetails(patientDetails, mrNo, patientId, false);
      ftlParamMap.put("visitdetails", patientDetails);

      ftlParamMap.put("mr_no_barcode", "*" + patientDetails.get("mr_no") + "*");
      ftlParamMap.put("mr_no", mrNo);
      ftlParamMap.put("patient_id", patientId);
      ftlParamMap.put("modules_activated",
          ((Preferences) RequestContext.getSession().getAttribute("preferences"))
              .getModulesActivatedMap());
      ftlParamMap.put("fieldvalues", PatientHVFDocValuesDAO.getHVFDocValues(docid, allFields));
      ftlParamMap.put("vitals",
          genericVitalFormDAO.getVitalReadings(patientDetails.get("patient_id").toString(), null));
      StringWriter writer = new StringWriter();
      try {
        Template ftlTemplate = AppInit.getFmConfig().getTemplate("PatientHVFDocumentPrint.ftl");
        ftlTemplate.process(ftlParamMap, writer);
      } catch (TemplateException te) {
        throw new IllegalArgumentException(te);
      }
      ImageRetriever imgretriever = null;
      patientId = patientId == null ? "" : patientId;
      if (patientId.equals("")) {
        imgretriever = new PatientImageRetriever();
      } else {
        imgretriever = new VisitWiseImageRetriever();
      }
      HtmlConverter hc = new HtmlConverter(imgretriever);
      Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");
      pdfbytes = hc.getPdfBytes(writer.toString(), "Patient HVF Document Print",
          PrintConfigurationsDAO.getPatientDefaultPrintPrefs(), repeatPHeader, true, true, true,
          false);

    } else if (format.equals("doc_rich_templates")) {
      ImageRetriever imgretriever = null;
      patientId = patientId == null ? "" : patientId;
      if (patientId.equals("")) {
        imgretriever = new PatientImageRetriever();
      } else {
        imgretriever = new VisitWiseImageRetriever();
      }
      String patientHeader = phTemplateDao
          .getPatientHeader((Integer) patientdocbean.get("pheader_template_id"), "Documents");

      Map ftlParamMap = new HashMap();
      Map patientDetails = new HashMap();
      GenericDocumentsFields.copyPatientDetails(patientDetails, mrNo, patientId, false);
      ftlParamMap.put("visitdetails", patientDetails);

      ftlParamMap.put("mr_no", mrNo);
      ftlParamMap.put("patient_id", patientId);
      ftlParamMap.put("modules_activated",
          ((Preferences) RequestContext.getSession().getAttribute("preferences"))
              .getModulesActivatedMap());
      StringWriter writer = new StringWriter();
      try {
        Template ftlTemplate = new Template("PatientHeader.ftl", new StringReader(patientHeader),
            AppInit.getFmConfig());
        ftlTemplate.process(ftlParamMap, writer);
      } catch (TemplateException te) {
        log.error(
            "error processing patient header when retrieving the pdf bytes for the document." + te);
        throw new IllegalArgumentException(te);
      }
      StringBuilder printContent = new StringBuilder();
      String content = (String) patientdocbean.get("doc_content_text");
      printContent.append(writer.toString());
      printContent.append(content);

      Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");
      HtmlConverter hc = new HtmlConverter(imgretriever);
      pdfbytes = hc.getPdfBytes(printContent.toString(), "", prefs, repeatPHeader, true, true, true,
          false);

    } else if (format.equals("doc_pdf_form_templates")) {

      GenericDAO templatedao = new GenericDAO("doc_pdf_form_templates");
      BasicDynaBean templatebean = templatedao.getBean();
      templatedao.loadByteaRecords(templatebean, "template_id", patientdocbean.get("template_id"));

      Map<String, String> fields = new HashMap<String, String>();
      GenericDocumentsFields.copyPatientDetails(fields, mrNo, patientId, true);

      List<BasicDynaBean> fieldslist = patientPdfFormDocValuesDAO.listAll(null, "doc_id", docid);
      for (BasicDynaBean fieldsBean : fieldslist) {
        fields.put(fieldsBean.get("field_name").toString(),
            fieldsBean.get("field_value").toString());
      }
      java.io.InputStream pdf = (java.io.InputStream) templatebean.get("template_content");
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      try {
        PdfUtils.sendFillableForm(os, pdf, fields, true, null, null, null);
      } catch (DocumentException de) {
        throw new IllegalArgumentException(de);
      }
      pdfbytes = os.toByteArray();

    } else if (format.equals("doc_rtf_templates")) {

      // This Blocck is For Insurance -- Sending RTF File as attachement
      GenericDAO templatedao = new GenericDAO("doc_rtf_templates");
      BasicDynaBean templatebean = templatedao.getBean();
      templatedao.loadByteaRecords(templatebean, "template_id", patientdocbean.get("template_id"));
      pdfbytes =
          DataBaseUtil.readInputStream((java.io.InputStream) templatebean.get("template_content"));
      HttpSession ses = RequestContext.getSession();
      ses.removeAttribute("FileType");
      ses.setAttribute("FileType", "RTF");

    } else if (format.equals("doc_fileupload")) {
      String contentType = (String) patientdocbean.get("content_type");
      if (contentType.split("/")[0].equals("image")) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // creation of a document object
        Document document = new Document();
        // creation of a pdf writer
        PdfWriter.getInstance(document, bos);
        // open the document
        document.open();
        Image image = null;
        try {
          // get the image instance and align it in center.
          image = Image.getInstance(DataBaseUtil
              .readInputStream((java.io.InputStream) patientdocbean.get("doc_content_bytea")));
          image.setAlignment(Image.MIDDLE);

          document.add(image);

        } catch (BadElementException bee) {
          throw new IllegalArgumentException(bee);
        }
        document.close();

        pdfbytes = bos.toByteArray();
      } else if (contentType.split("/")[0].equals("pdf")) {

        AbstractDocumentPersistence persistenceAPI =
            AbstractDocumentPersistence.getInstance(null, false);

        Map docParams = new HashMap();
        persistenceAPI.copyDocumentDetails(docid, docParams);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(DataBaseUtil
            .readInputStream((java.io.InputStream) patientdocbean.get("doc_content_bytea")));
        pdfbytes = bos.toByteArray();
        bos.flush();
      }

    } else {
      throw new IllegalArgumentException("Document not support pdf convertion");
    }
    return pdfbytes;
  }

  /**
   * Gets the page options.
   *
   * @return the page options
   * @throws SQLException
   *           the SQL exception
   */
  public static PrintPageOptions getPageOptions() throws SQLException {
    BasicDynaBean bean = PrintConfigurationsDAO.getDischargeDefaultPrintPrefs();
    return new PrintPageOptions(((Integer) bean.get("top_margin")).intValue(),
        ((Integer) bean.get("bottom_margin")).intValue(),
        ((Integer) bean.get("page_height")).intValue(), bean.get("continuous_feed").toString());
  }

  /** The Constant HVF_DOC_IMAGE. */
  private static final String HVF_DOC_IMAGE =
      " SELECT field_image  FROM patient_hvf_doc_images WHERE doc_image_id=?";

  /**
   * Gets the HVF doc image.
   *
   * @param docImageId
   *          the doc image id
   * @return the HVF doc image
   * @throws SQLException
   *           the SQL exception
   */
  public InputStream getHVFDocImage(int docImageId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(HVF_DOC_IMAGE);
      ps.setInt(1, docImageId);
      rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getBinaryStream(1);
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  /**
   * Input stream to file.
   *
   * @param is
   *          the is
   * @param imgFile
   *          the img file
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void inputStreamToFile(InputStream is, File imgFile) throws IOException {
    byte[] bytes = new byte[is.available()];
    try (FileOutputStream fos = new FileOutputStream(imgFile)) {
      while (true) {
        int readByte = is.read(bytes);
        if (readByte <= 0) {
          break;
        }
        fos.write(bytes, 0, readByte);
      }
    }
  }

  /** The Constant GET_CARD_ATTACHMENT. */
  private static final String GET_CARD_ATTACHMENT =
      "SELECT doc_content_bytea,content_type FROM patient_documents WHERE doc_id = ?";

  /**
   * Gets the patient card attachment.
   *
   * @param docId
   *          the doc id
   * @return the patient card attachment
   * @throws SQLException
   *           the SQL exception
   */
  public Map getPatientCardAttachment(int docId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_CARD_ATTACHMENT);
      ps.setInt(1, docId);
      rs = ps.executeQuery();
      if (rs.next()) {
        Map map = new HashMap();
        map.put("Content", rs.getBinaryStream(1));
        map.put("Type", rs.getString(2));
        return map;
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }
  
  private static final String GET_MR_FOR_TEST_DOC_ID = "select tp.mr_no from tests_prescribed"
      + " tp join test_documents td on (td.prescribed_id = tp.prescribed_id) where td.doc_id= ?";
  
  public String getMrForTestDocument(Integer docId) {
    return DatabaseHelper.getString(GET_MR_FOR_TEST_DOC_ID, docId);
  }
  
  private static final String GET_MR_FOR_SERVICE_DOC_ID = "select sp.mr_no from"
      + " services_prescribed sp join service_documents sd on "
      + "(sd.prescription_id = sp.prescription_id) where sd.doc_id = ?";
  
  public String getMrForServiceDocument(Integer docId) {
    return DatabaseHelper.getString(GET_MR_FOR_SERVICE_DOC_ID, docId);
  }
}
