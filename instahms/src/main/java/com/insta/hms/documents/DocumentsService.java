package com.insta.hms.documents;

import com.bob.hms.common.NumberToWordFormat;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.common.BusinessService;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.PdfUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;
import com.insta.hms.genericdocuments.CommonHelper;
import com.insta.hms.mdm.documenttypes.DocumentTypeService;
import com.insta.hms.messaging.MessageManager;
import com.lowagie.text.DocumentException;

import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


// TODO: Auto-generated Javadoc
/**
 * The Class GenericDocumentsService.
 */
public abstract class DocumentsService extends BusinessService {

  /**
   * Instantiates a new generic documents service.
   *
   * @param store the s
   */
  public DocumentsService(AbstractDocumentStore store) {
    this.docStore = store;
  }

  /** The log. */
  static Logger log = LoggerFactory.getLogger(DocumentsService.class);

  /** The doc store. */
  protected AbstractDocumentStore docStore;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The security service. */
  @LazyAutowired
  private SecurityService securityService;

  /** The pdftemplaterepo. */
  @LazyAutowired
  private PdfFormTemplateRepository pdftemplaterepo;

  /** The pdfvaluesdocrepo. */
  @LazyAutowired
  private PatientPdfFormValuesRepository pdfvaluesdocrepo;

  /** The patientgendocrepo. */
  @LazyAutowired
  private PatientGeneralDocsRepository patientgendocrepo;

  /** The patientdocrepo. */
  @LazyAutowired
  private PatientDocumentRepository patientdocrepo;

  /** The patdetails service. */
  @LazyAutowired
  private PatientDetailsService patdetailsService;

  /** The hvftemplaterepo. */
  @LazyAutowired
  private HvfTemplateRepository hvftemplaterepo;

  /** The hvftemplatefieldsrpo. */
  @LazyAutowired
  private HvfTemplateFieldsRepository hvftemplatefieldsrpo;

  /** The richtexttemplaterepo. */
  @LazyAutowired
  private RichTextTemplateRepository richtexttemplaterepo;

  /** The docpdfformtemplaterepo. */
  @LazyAutowired
  private DocPdfFormTemplateRepository docpdfformtemplaterepo;

  /** The rtftemplaterepo. */
  @LazyAutowired
  private RtfTemplateRepository rtftemplaterepo;

  /** The patienthvfdocimagesrepo. */
  @LazyAutowired
  private PatientHvfDocImagesRepository patienthvfdocimagesrepo;

  /** The patientpdfdocimagesrepo. */
  @LazyAutowired
  private PatientPdfDocImagesRepository patientpdfdocimagesrepo;

  /** The generic documents util. */
  @LazyAutowired
  private GenericDocumentsUtil genericDocumentsUtil;

  /** The document type service. */
  @LazyAutowired
  private DocumentTypeService documentTypeService;

  /** The insurance docs repository. */
  @LazyAutowired
  private InsuranceDocsRepository insuranceDocsRepository;

  /**
   * Gets the uploaded documents.
   *
   * @param request
   *          the request
   * @return the uploaded documents
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   */
  public List getUploadedDocuments(HttpServletRequest request) 
      throws IOException, ParseException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Gets the document content by doc id.
   *
   * @param docId
   *          the doc id
   * @return the document content by doc id
   */
  public byte[] getDocumentContentByDocId(Integer docId) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Gets the document types by cat and emr rules.
   *
   * @param docTypeCatName
   *          the doc type cat name
   * @param visitId
   *          the visit id
   * @param specialized
   *          the specialized
   * @param request
   *          the request
   * @return the document types by cat and emr rules
   * @throws ParseException
   *           the parse exception
   */
  public Object getDocumentTypesByCatAndEmrRules(
      String docTypeCatName, String visitId,
      String specialized, HttpServletRequest request) 
          throws ParseException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Search documents.
   *
   * @param params the params
   * @param requestMap the request map
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public void searchDocuments(Map<String, String[]> params, Map<String, Object> requestMap)
      throws IOException, ParseException {

    String allowToDelPatDoc = null;
    String mrNo = getParameter(params, "mr_no");
    Boolean specialized = docStore.isSpecialized();
    Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(params);
    int roleId = (Integer) sessionService.getSessionAttributes().get("roleId");

    if ((roleId != 1) && (roleId != 2)) {
      allowToDelPatDoc = (String) ((Map) securityService.getSecurityAttributes()
          .get("actionRightsMap")).get("allow_delete_patient_doc");
    } else {
      allowToDelPatDoc = "A";
    }

    Map extraParams = docStore.populateKeys(params, null);
    extraParams.put("mr_no", mrNo);

    requestMap.put("document_list", ConversionUtils
        .listBeanToListMap(docStore.searchDocuments(listingParams, extraParams, specialized)));
    BasicDynaBean printPref = PrintConfigurationRepository.getPageOptions("Discharge");
    requestMap.put("print_preferneces", printPref.getMap());
    requestMap.put("allow_to_del_pat_document", allowToDelPatDoc);

  }

  /**
   * Adds the document.
   *
   * @param params the params
   * @param requestMap the request map
   * @throws ParseException the parse exception
   */
  public void addDocument(Map<String, String[]> params, Map<String, Object> requestMap)
      throws ParseException {

    String documentType = docStore.getDocumentType();
    Boolean specialized = docStore.isSpecialized();

    String mrNo = getParameter(params, "mr_no");

    if (mrNo != null && !mrNo.equals("")) {
      Map<String, Object> filterParams = new HashMap<>();
      if (specialized) {
        String docTypeId = "";
        if (documentType.equals("insurance")) {
          docTypeId = "SYS_INS";
        } else if (documentType.equals("reg")) {
          docTypeId = "SYS_RG";
        } else if (documentType.equals("tpapreauth")) {
          docTypeId = "SYS_TPA";
        } else if (documentType.equals("service")) {
          docTypeId = "SYS_ST";
        } else if (documentType.equals("ot")) {
          docTypeId = "SYS_OT";
        }

        filterParams.put("doc_type_id", new String[] { docTypeId });
      }
      filterParams.put("status", new String[] { "A" });
      Map<LISTING, Object> listing = ConversionUtils.getListingParameter(params);
      PagedList list = GenericDocumentTemplateRepository.getGenericDocTemplates(filterParams,
          specialized, listing);
      requestMap.put("document_list", (list));

      requestMap.put("visits_list", ConversionUtils
          .listBeanToListMap(PatientRegistrationRepository.getAllCenterVisitsAndDoctors(mrNo)));
      BasicDynaBean activeVisitBean = new PatientRegistrationRepository()
          .getLatestActiveVisit(mrNo);
      requestMap.put("active_visit_bean", activeVisitBean.getMap());
    }
    requestMap.put("documentType", documentType);
    requestMap.put("specialized", specialized);

  }

  /**
   * Adds the.
   *
   * @param params the params
   * @param requestMap the request map
   * @throws TemplateException the template exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public void add(Map<String, String[]> params, Map<String, Object> requestMap)
      throws TemplateException, IOException, SQLException {
    int templateId = 0;
    String templateIdStr = getParameter(params, "template_id");
    if (templateIdStr != null && !templateIdStr.equals("")) {
      templateId = Integer.parseInt(templateIdStr);
    }
    String format = getParameter(params, "format");
    String mrNo = getParameter(params, "mr_no");
    String patientId = getParameter(params, "patient_id");
    BasicDynaBean templateDetails = null;

    if (format.equals("doc_hvf_templates")) {
      HvfTemplateRepository repo = new HvfTemplateRepository();
      templateDetails = repo.findByKey("template_id", templateId);
      HvfTemplateFieldsRepository hvfFieldsRepo = new HvfTemplateFieldsRepository();
      Map filterMap = new HashMap();
      filterMap.put("template_id", templateId);
      filterMap.put("field_status", "A");
      requestMap.put("hvf_template_fields", ConversionUtils
          .copyListDynaBeansToMap(hvfFieldsRepo.listAll(null, filterMap, "display_order")));

    } else if (format.equals("doc_rich_templates")) {
      RichTextTemplateRepository repo = new RichTextTemplateRepository();
      templateDetails = repo.findByKey("template_id", templateId);
      String docContent = (String) templateDetails.get("template_content");

      Map<String, String> fields = new HashMap<>();

      Map keyParams = docStore.populateKeys(params, null);
      genericDocumentsUtil.copyStandardFields(fields, false);
      docStore.copyReplaceableFields(fields, keyParams, false);

      // process the template by replacing the tags with field values
      docContent = CommonHelper.replaceTags(docContent, fields, false);

      // add rich text title and optionally patient header
      docContent = CommonHelper.addRichTextTitle(docContent, (String) templateDetails.get("title"),
          (Integer) templateDetails.get("pheader_template_id"), patientId, mrNo);

      templateDetails.set("template_content", docContent);

    } else if (format.equals("doc_pdf_form_templates")) {
      templateDetails = new PdfFormTemplateRepository().findByKey("template_id", templateId);
    } else if (format.equals("doc_rtf_templates")) {
      RtfTemplateRepository repo = new RtfTemplateRepository();
      templateDetails = repo.findByKey("template_id", templateId);
    }

    Integer printerId = (Integer) PrintConfigurationRepository.getPatientDefaultPrintPrefs()
        .get("printer_id");
    String templateName = (templateDetails != null && templateDetails.get("template_name") != null)
        ? (String) templateDetails.get("template_name")
        : null;

    String documentType = docStore.getDocumentType();

    if (documentType != null && documentType.equals("reg")
        && (format.equals("doc_rich_templates") || format.equals("doc_hvf_templates"))) {
      printerId = (Integer) DocPrintConfigurationRepository
          .getRegistrationPrintConfiguration(templateName).get("printer_settings");
    }
    if (templateDetails != null) {
      requestMap.put("template_details", templateDetails.getMap());
    } else {
      requestMap.put("template_details", null);
    }
    requestMap.put("default_print_def_Id", printerId);
    requestMap.put("document_type", documentType);
    Boolean specialized = docStore.isSpecialized();
    requestMap.put("specialized", specialized);
  }

  /**
   * Open pdf form.
   *
   * @param params the params
   * @param os the os
   * @param submitUrl the submit url
   * @param hiddenParams the hidden params
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws DocumentException the document exception
   * @throws SQLException the SQL exception
   */
  public void openPdfForm(Map<String, String[]> params, OutputStream os, String submitUrl,
      HashMap<String, String> hiddenParams) throws IOException, DocumentException, SQLException {

    Map<String, String> fields = new HashMap<>();

    Map keyParams = docStore.populateKeys(params, null);

    genericDocumentsUtil.copyStandardFields(fields, true);
    docStore.copyReplaceableFields(fields, keyParams, true);

    String display = getParameter(params, "display");
    boolean pdfFieldsFlatten = false;
    if (display != null && display.equals("view")) {
      pdfFieldsFlatten = true;
    }

    String docId = getParameter(params, "doc_id");
    if ((docId != null) && (!docId.equals(""))) {
      List<BasicDynaBean> fieldslist = pdfvaluesdocrepo.listAll(null, "doc_id",
          Integer.parseInt(docId));
      for (BasicDynaBean fieldsBean : fieldslist) {
        fields.put(fieldsBean.get("field_name").toString(),
            fieldsBean.get("field_value").toString());
      }
    }

    if (pdfFieldsFlatten) {
      submitUrl = null;
    }
    BasicDynaBean bean = pdftemplaterepo.findByKey("template_id",
        Integer.parseInt(getParameter(params, "template_id")));
    InputStream pdf = (InputStream) bean.get("template_content");
    PdfUtils.sendFillableForm(os, pdf, fields, pdfFieldsFlatten, submitUrl, hiddenParams, null);

  }

  /**
   * Gets the rtf document.
   *
   * @param params the params
   * @param response the response
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public void getRtfDocument(Map<String, String[]> params, HttpServletResponse response)
      throws IOException, SQLException {
    int templateId = Integer.parseInt(getParameter(params, "template_id"));
    String format = getParameter(params, "format");
    String mrNo = getParameter(params, "mr_no");
    String docId = getParameter(params, "doc_id");
    BasicDynaBean bean = null;
    String fileName = null;
    OutputStream stream = response.getOutputStream();
    if ((docId == null) || docId.equals("")) {
      bean = rtftemplaterepo.findByKey("template_id", templateId);
      String contentType = (String) bean.get("content_type");
      boolean isRtf = false;
      if (contentType.equals("application/rtf") || contentType.equals("text/rtf")) {
        isRtf = true;
      }

      fileName = bean.get("template_name").toString() + "_" + mrNo;
      if (isRtf) {
        fileName = fileName + ".rtf";
      }
      response.setContentType(contentType);
      response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");

      Map<String, String> fields = new HashMap<>();
      genericDocumentsUtil.copyStandardFields(fields, false);

      Map keyParams = docStore.populateKeys(params, null);
      docStore.copyReplaceableFields(fields, keyParams, false);

      if (keyParams.get("patient_id") != null && !(keyParams.get("patient_id").equals(""))) {
        List<BasicDynaBean> charges = ChargeDAO.getChargeDetailsBean(fields.get("bill_no"));

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (BasicDynaBean charge : charges) {
          String chargeStatus = (String) charge.get("status");
          if (!chargeStatus.equals("X")) {
            BigDecimal chargeAmount = (BigDecimal) charge.get("amount");
            totalAmount = totalAmount.add(chargeAmount);
          }
        }
        fields.put("bill_amt", totalAmount.toString());
        fields.put("bill_amt_words", NumberToWordFormat.wordFormat().toRupeesPaise(totalAmount));
      }
      if (isRtf) {
        CommonHelper.replaceTags((java.io.InputStream) bean.get("template_content"), stream, fields,
            isRtf);
      } else {
        stream.write(IOUtils.toByteArray((java.io.InputStream) bean.get("template_content")));
      }

    } else {

      bean = patientdocrepo.findByKey("doc_id", Integer.parseInt(docId));
      BasicDynaBean genbean = null;

      String documentType = docStore.getDocumentType();
      Boolean specialized = docStore.isSpecialized();

      if (specialized) {
        if (documentType.equals("insurance")) {
          genbean = insuranceDocsRepository.findByKey("doc_id", Integer.parseInt(docId));
          fileName = genbean.get("doc_name").toString();
        } else {
          genbean = rtftemplaterepo.findByKey("template_id", templateId);
          fileName = genbean.get("template_name").toString();
        }
      } else {
        genbean = patientgendocrepo.findByKey("doc_id", Integer.parseInt(docId));
        fileName = genbean.get("doc_name").toString();
      }

      if (null != bean.get("content_type")
          && ((bean.get("content_type").toString().equals("application/rtf")
              || bean.get("content_type").toString().equals("text/rtf")))) {
        fileName = fileName + "_" + mrNo + ".rtf";
      } else {
        fileName = fileName + "_" + mrNo;
      }

      if (null != bean.get("content_type")) {
        response.setContentType(bean.get("content_type").toString());
      }
      response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");

      stream.write(IOUtils.toByteArray((InputStream) bean.get("doc_content_bytea")));
    }

    stream.close();
  }

  /**
   * Creates the.
   *
   * @param params the params
   * @return the map
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> create(Map<String, Object[]> params) throws IOException {

    String format = getParameter(params, "format");
    String templateId = getParameter(params, "template_id");
    if (format.equals("doc_rich_templates")) {
      RichTextTemplateRepository richTempRepo = new RichTextTemplateRepository();
      BasicDynaBean richTextBean = richTempRepo.findByKey("template_id",
          Integer.parseInt(templateId));
      Integer pheaderTemplateId = (Integer) richTextBean.get("pheader_template_id");
      copyObjectToMap(params, "pheader_template_id", pheaderTemplateId);
    }

    return docStore.create(params);
  }

  /**
   * Bulk upload.
   *
   * @param params the params
   * @return the map
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Map<String, Object> bulkUpload(Map<String, Object[]> params) throws IOException {
    return docStore.bulkUpload(params);
  }

  /**
   * Update.
   *
   * @param params the params
   * @return true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   */
  public boolean update(Map<String, Object[]> params)
      throws IOException, ParseException, SQLException {

    String format = (String) params.get("format")[0];
    String userid = (String) sessionService.getSessionAttributes().get("userId");
    params.put("username", new Object[] { userid });

    String action = null != params.get("_action") ? (String) params.get("_action")[0] : null;
    boolean finalize = action != null && action.equals("finalize");
    boolean saveExtFields = action != null && action.equals("saveExtFields");

    boolean result = docStore.update(params, (saveExtFields || finalize));
    if (result && finalize) { // &&
      // MessageUtil.allowMessageNotification(request,
      // "general_message_send")
      params.put("deleteDocument",
          new String[] { ((String) params.get("doc_id")[0]) + "," + format });
      result = docStore.finalizeDocument(params);
      if (result && format.equalsIgnoreCase("doc_fileupload")) {
        Map reportData = new HashMap();
        MessageManager mgr = new MessageManager();
        reportData.put("doc_id", new String[] { (String) params.get("doc_id")[0] });
        mgr.processEvent("gen_doc_finalize", reportData);
      }
    }

    return result;
  }

  /**
   * Copy string to map.
   *
   * @param params the params
   * @param key the key
   * @param value the value
   */
  public void copyStringToMap(Map<String, String[]> params, String key, String value) {

    if (params.containsKey(key)) {
      String[] obj = params.get(key);
      String[] newArray = Arrays.copyOf(obj, obj.length + 1);
      newArray[obj.length] = value;
      params.put(key, newArray);

    } else {
      params.put(key, new String[] { value });
    }
  }

  /**
   * Copy object to map.
   *
   * @param params the params
   * @param key the key
   * @param value the value
   */
  public void copyObjectToMap(Map params, String key, Object value) {

    if (params.containsKey(key)) {
      Object[] obj = (Object[]) params.get(key);
      Object[] newArray = Arrays.copyOf(obj, obj.length + 1);
      newArray[obj.length] = value;
      params.put(key, newArray);

    } else {
      params.put(key, new Object[] { value });
    }
  }

  /**
   * Gets the parameter.
   *
   * @param params the params
   * @param key the key
   * @return the parameter
   */
  public String getParameter(Map params, String key) {
    Object[] obj = (Object[]) params.get(key);
    if (obj == null || obj[0] == null) {
      return null;
    }
    return obj[0].toString();
  }

  /**
   * Show.
   *
   * @param params the params
   * @param requestMap the request map
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void show(Map<String, String[]> params, Map<String, Object> requestMap)
      throws IOException {

    int docId = Integer.parseInt(params.get("doc_id")[0]);
    int templateId = 0;
    String templateIdStr = params.get("template_id")[0];
    if (templateIdStr != null && !templateIdStr.equals("")) {
      templateId = Integer.parseInt(templateIdStr);
    }

    BasicDynaBean templateDetails = null;

    Map documentDetails = new HashMap();
    documentDetails.put("doc_id", docId);
    documentDetails.put("doc_name", null);
    documentDetails.put("doc_date", null);

    BasicDynaBean patientdocbean = PatientDocumentRepository.getPatientDocument(docId);
    documentDetails.putAll(patientdocbean.getMap());

    docStore.copyDocumentDetails(docId, documentDetails);

    requestMap.put("document_details", documentDetails);
    String format = params.get("format")[0];
    if (format.equals("doc_hvf_templates")) {
      templateDetails = hvftemplaterepo.findByKey("template_id", templateId);
      requestMap.put("template_details", templateDetails.getMap());
      List<BasicDynaBean> fieldslist = hvftemplatefieldsrpo.listAll(null, "template_id", templateId,
          "display_order");

      PatientHvfDocValuesRepository hvffieldsvaluesdao = new PatientHvfDocValuesRepository();
      Object docIdObj = docId;
      List<BasicDynaBean> fieldsvalueslist = hvffieldsvaluesdao.listAll(null, "doc_id", docIdObj);

      List imageFieldvalues = PatientHvfDocValuesRepository.getHVFDocImageValues(docId, true);

      List filledDocument = new ArrayList();
      if (!fieldslist.isEmpty()) {
        if (!fieldslist.isEmpty()) {
          for (BasicDynaBean fieldBean : fieldslist) {
            Map map = new HashMap(fieldBean.getMap());

            map.put("value_id", "");
            map.put("default_value", "");

            for (BasicDynaBean valueBean : fieldsvalueslist) {
              if (((Integer) fieldBean.get("field_id")).equals(valueBean.get("field_id"))) {
                map.put("value_id", valueBean.get("value_id"));
                map.put("default_value", valueBean.get("field_value"));
              }
            }

            if (imageFieldvalues != null && imageFieldvalues.size() > 0) {

              for (Object imgObj : imageFieldvalues) {
                BasicDynaBean imgbean = (BasicDynaBean) imgObj;
                if (((Integer) fieldBean.get("field_id")).equals(imgbean.get("field_id"))) {

                  int docImgeId = (Integer) imgbean.get("doc_image_id");
                  File imgFile = File.createTempFile("tempImage", "");
                  List<String> columns = new ArrayList<>();
                  columns.add("field_image");
                  Object docImgIdObj = docImgeId;
                  List<BasicDynaBean> beans = patienthvfdocimagesrepo.listAll(columns,
                      "doc_image_id", docImgIdObj);

                  if (beans != null) {
                    patientgendocrepo.inputStreamToFile(
                        ((InputStream) beans.get(0).get(columns.get(0))), imgFile);

                    map.put("doc_image_id", imgbean.get("doc_image_id"));
                    map.put("image_url", imgFile.getAbsolutePath());
                    map.put("field_image_content_type", imgbean.get("field_image_content_type"));
                    map.put("device_ip", imgbean.get("device_ip"));
                    map.put("device_info", imgbean.get("device_info"));
                    map.put("capture_time", imgbean.get("capture_time"));
                  }
                }
              }
            }

            // Need to remove the inactive fields
            /*
             * if ((String)fieldBean.get("field_status") != null &&
             * !((String)fieldBean.get("field_status")).equals("I")) {
             * 
             * filledDocument.add(map); }
             */
            filledDocument.add(map);
          }
        }
      }
      requestMap.put("hvf_template_fields", filledDocument);

    } else if (format.equals("doc_rich_templates")) {
      templateDetails = richtexttemplaterepo.findByKey("template_id", templateId);
      BasicDynaBean valueBean = patientdocrepo.findByKey("doc_id", docId);
      Map map = new HashMap(valueBean.getMap());
      map.put("template_name", templateDetails.get("template_name"));
      map.put("title", templateDetails.get("title"));
      requestMap.put("template_details", map);

    } else if (format.equals("doc_pdf_form_templates")) {
      templateDetails = docpdfformtemplaterepo.findByKey("template_id", templateId);
      requestMap.put("template_details", templateDetails);

      List<Map<String, Object>> pdfImageFields = new ArrayList<>();
      List<BasicDynaBean> imageTemplateFieldvalues = null;

      if (docId != 0) {
        imageTemplateFieldvalues = PatientPdfDocImagesRepository
            .getPDFTemplateImageValues(templateId);

        if (imageTemplateFieldvalues != null && imageTemplateFieldvalues.size() > 0) {

          List<BasicDynaBean> imageFieldvalues = PatientPdfDocImagesRepository
              .getPDFDocImageValues(docId);
          for (BasicDynaBean imgbean : imageTemplateFieldvalues) {
            Map<String, Object> map = new HashMap<>(imgbean.getMap());

            map.put("doc_image_id", 0);
            map.put("image_url", "");
            map.put("field_image_content_type", "");
            map.put("device_ip", "");
            map.put("device_info", "");
            map.put("capture_time", "");

            if (imageFieldvalues != null && imageFieldvalues.size() > 0) {
              for (BasicDynaBean fieldImgBean : imageFieldvalues) {

                if (((Integer) fieldImgBean.get("field_id")).equals(imgbean.get("field_id"))) {

                  int docImgeId = (Integer) fieldImgBean.get("doc_image_id");
                  File imgFile = File.createTempFile("tempImage", "");
                  // InputStream is =
                  // PatientPDFDocValuesDAO.getPDFDocImage(docImgeId);
                  // //Change this
                  List<String> columns = new ArrayList<>();
                  columns.add("field_image");
                  Object docImgIdObj = docImgeId;
                  List<BasicDynaBean> beans = patientpdfdocimagesrepo.listAll(columns,
                      "doc_image_id", docImgIdObj);
                  map.put("doc_image_id", fieldImgBean.get("doc_image_id"));

                  if (beans != null) {
                    // FileUtils.writeByteArrayToFile(imgFile,
                    // (byte[])
                    // beans.get(0).get(columns.get(0)));
                    patientgendocrepo
                        .inputStreamToFile((InputStream) beans.get(0).get(columns.get(0)), imgFile);
                    // is.close();
                    map.put("image_url", imgFile.getAbsolutePath());
                    map.put("field_image_content_type",
                        fieldImgBean.get("field_image_content_type"));
                    map.put("device_ip", fieldImgBean.get("device_ip"));
                    map.put("device_info", fieldImgBean.get("device_info"));
                    map.put("capture_time", fieldImgBean.get("capture_time"));
                  }
                }
              }
            }

            pdfImageFields.add(map);
          }
        }
      }

      requestMap.put("pdf_template_ext_fields", pdfImageFields);

    } else if (format.equals("doc_rtf_templates")) {
      templateDetails = rtftemplaterepo.findByKey("template_id", templateId);
      requestMap.put("template_details", templateDetails.getMap());
    } else if (format.equals("doc_fileupload")) {
      BasicDynaBean valueBean = patientdocrepo.findByKey("doc_id", docId);
      documentDetails.put("doc_type", valueBean.get("doc_type"));
    } else {
      BasicDynaBean valueBean = patientdocrepo.findByKey("doc_id", docId);
      documentDetails.put("doc_type", valueBean.get("doc_type"));
      documentDetails.put("doc_location", valueBean.get("doc_location"));
    }

    Integer printerId = (Integer) PrintConfigurationRepository.getPatientDefaultPrintPrefs()
        .get("printer_id");

    if (null != templateDetails) {
      String templateName = (String) templateDetails.get("template_name");
      if (templateName != null && "reg".equals(docStore.getDocumentType())
          && (format.equals("doc_rich_templates") || format.equals("doc_hvf_templates"))) {
        BasicDynaBean docPrintBean = DocPrintConfigurationRepository
            .getRegistrationPrintConfiguration(templateName);
        printerId = (Integer) docPrintBean.get("printer_settings");
      }
    }

    requestMap.put("default_print_def_Id", printerId);
    requestMap.put("documentType", docStore.getDocumentType());
    requestMap.put("specialized", docStore.isSpecialized());
    // requestMap.put("uploadFile", (Boolean)
    // propertyMap.get("uploadFile"));
    // requestMap.put("docNameRequired", (Boolean)
    // propertyMap.get("docNameRequired"));
    // requestMap.put("docDateRequired", (Boolean)
    // propertyMap.get("docDateRequired"));
    // requestMap.put("docLink", (Boolean) propertyMap.get("doclink"));
  }

  //
  // private void setPrintAttributes(FlashScope flash, String format, String
  // template_id) throws SQLException{
  // BasicDynaBean printPref =
  // PrintConfigurationsDAO.getPageOptions("Discharge"); //Change this
  // flash.put("printerId", printPref.get("printer_id"));
  // }
  //
  //
  //
  // public boolean empty(Object s) {
  // return (s==null || s.toString().equals(""));
  // }
  //
  // public boolean notEmpty(Object s) {
  // return !empty(s);
  // }
  //

  /**
   * Delete documents.
   *
   * @param params the params
   * @param requestMap the request map
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void deleteDocuments(Map<String, String[]> params, Map<String, Object> requestMap)
      throws IOException {
    String[] deleted;
    String docId = getParameter(params, "doc_id");
    String docFormat = getParameter(params, "doc_format");
    if (docId != null && docFormat != null)  {
      deleted = new String[] {docId + "," + docFormat};
      params.put("deleteDocument", deleted);
    } else {
      deleted = params.get("deleteDocument");
    }
    Map docData = new HashMap();
    for (int i = 0; i < deleted.length; i++) {
      String[] temp = deleted[i].split(",");
      if (temp[1].equalsIgnoreCase("doc_fileupload")) {
        BasicDynaBean bean = PatientDocumentRepository
            .getPatientDocument(Integer.parseInt(temp[0]));
        Map<String, Object> map = new HashMap<>();
        map.put("doc_type_id", bean.get("doc_type"));
        BasicDynaBean docTypeBean = documentTypeService.findByPk(map);
        docData.put(temp[0], docTypeBean.get("doc_type_name"));
      }
    }
    boolean result = docStore.delete(params);
    // if (result && MessageUtil.allowMessageNotification(request,
    // "general_message_send")) {
    //
    // MessageManager mgr = new MessageManager();
    // for (int i = 0; i < deleted.length; i++) {
    // String[] temp = deleted[i].split(",");
    // if (temp[1].equalsIgnoreCase("doc_fileupload")) {
    // Map reportData = new HashMap();
    // String[] data = new String[] { temp[0] };
    // reportData.put("doc_id", data);
    // String[] mr_no = (String[]) params.get("mr_no");
    // reportData.put("mr_no", mr_no);
    // reportData.put("record_type", "doc_" + docData.get(temp[0]));
    // reportData.put("document_id", Integer.parseInt(temp[0]));
    // mgr.processEvent("gen_doc_delete", reportData);
    // }
    // }
    // }
    if (result) {
      requestMap.put("success", params.get("success"));
    } else {
      requestMap.put("error", params.get("error"));
    }

  }

  /**
   * Finalize documents.
   *
   * @param params the params
   * @param requestMap the request map
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void finalizeDocuments(Map<String, String[]> params, Map<String, Object> requestMap)
      throws IOException {

    boolean result = docStore.finalizeDocument(params);

    // if (result && MessageUtil.allowMessageNotification(request,
    // "general_message_send")) {
    // String[] finalized = (String[]) params.get("deleteDocument");
    // MessageManager mgr = new MessageManager();
    // for (int i = 0; i < finalized.length; i++) {
    // String[] temp = finalized[i].split(",");
    // if (temp[1].equalsIgnoreCase("doc_fileupload")) {
    // Map reportData = new HashMap();
    // String[] data = new String[] { temp[0] };
    // reportData.put("doc_id", data);
    // mgr.processEvent("gen_doc_finalize", reportData);
    // }
    // }
    // }
    if (result) {
      requestMap.put("success", params.get("success"));
    } else {
      requestMap.put("error", params.get("error"));
    }
  }


  // public static void viewTextImage(HttpServletRequest request,
  // HttpServletResponse response) {
  //
  // String base64String = request.getParameter("imageText");
  //
  // Base64 decoder = new Base64();
  // byte[] decodedBytes = decoder.decode(base64String.getBytes());
  //
  // ByteArrayInputStream is = new ByteArrayInputStream(decodedBytes);
  //
  // File imgFile = File.createTempFile("tempImage", "");
  //
  // BufferedImage image = null;
  // byte[] bytes = null;
  // if (is != null) {
  //
  // response.setContentType("image/png");
  // OutputStream os = response.getOutputStream();
  //
  // try {
  // image = ImageIO.read(is);
  // if (image == null) {
  // log.error("Buffered Image is null");
  // }
  //
  // // write the image
  // ImageIO.write(image, "png", imgFile);
  //
  // FileInputStream fis = new FileInputStream(imgFile);
  // bytes = new byte[fis.available()];
  //
  // int offset = 0;
  // int numRead = 0;
  // while (offset < bytes.length
  // && (numRead = fis.read(bytes, offset, bytes.length-offset)) >= 0) {
  // offset += numRead;
  // }
  //
  // // Ensure all the bytes have been read in
  // if (offset < bytes.length) {
  // throw new IOException("Could not completely read.");
  // }
  //
  // os.write(bytes);
  // os.flush();
  // is.close();
  // fis.close();
  //
  // } catch (Exception e) {
  // throw e;
  // }
  // }
  //
  // response.flushBuffer();
  // response.reset();
  // response.resetBuffer();
  // }

  // public static boolean viewDocumentFieldImage() {
  // String format = request.getParameter("format");
  // String doc_image_id = request.getParameter("doc_image_id");
  // Integer docImageId = (doc_image_id != null && !doc_image_id.equals("")) ?
  // new Integer(doc_image_id) : null;
  // if (docImageId != null) {
  // response.setContentType("image/png");
  // OutputStream os = response.getOutputStream();
  //
  // InputStream is = null;
  // byte[] bytes = null;
  //
  // if (format.equals("doc_hvf_templates")) {
  // is = patientgendocrepo.getHVFDocImage(docImageId);
  // }else if (format.equals("doc_pdf_form_templates")) {
  // is = PatientPDFDocValuesDAO.getPDFDocImage(docImageId);
  // }
  //
  // if (is != null) {
  // bytes = new byte[is.available()];
  //
  // int offset = 0;
  // int numRead = 0;
  // while (offset < bytes.length
  // && (numRead = is.read(bytes, offset, bytes.length-offset)) >= 0) {
  // offset += numRead;
  // }
  //
  // // Ensure all the bytes have been read in
  // if (offset < bytes.length) {
  // throw new IOException("Could not completely read.");
  // }
  //
  // os.write(bytes);
  // os.flush();
  // is.close();
  // return false;
  //
  // } else {
  // return true;
  // }
  // } else {
  // return true;
  // }
  // }
  //

}
