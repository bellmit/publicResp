/** */

package com.insta.hms.genericdocuments;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.NumberToWordFormat;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.PdfUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.documentpersitence.AbstractDocumentPersistence;
import com.insta.hms.emr.EMRDocFilter;
import com.insta.hms.master.GenericDocumentTemplate.GenericDocumentTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.messaging.MessageUtil;
import com.insta.hms.pdf2dom.PdfFormToDom;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.fit.pdfdom.PDFDomTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

// TODO: Auto-generated Javadoc

/**
 * The Class GenericDocumentsAction.
 * 
 * @author krishna.t
 */

/*
 * TODO - New insurance case for patient (general) not saving mr_no in case details.
 */
public class GenericDocumentsAction extends BaseAction {

  /**
   * The log.
   */
  static Logger log = LoggerFactory.getLogger(GenericDocumentsAction.class);

  /**
   * The pdftemplatedao.
   */
  private static GenericDAO pdftemplatedao = new GenericDAO("doc_pdf_form_templates");

  /**
   * The pdfvaluesdocdao.
   */
  private static GenericDAO pdfvaluesdocdao = new GenericDAO("patient_pdf_form_doc_values");

  /**
   * The patientgendocdao.
   */
  private static GenericDocumentsDAO patientgendocdao = new GenericDocumentsDAO();

  /**
   * The hvfdocvaluesdao.
   */
  private static PatientHVFDocValuesDAO hvfdocvaluesdao = new PatientHVFDocValuesDAO();

  /**
   * The patientdocdao.
   */
  private static PatientDocumentsDAO patientdocdao = new PatientDocumentsDAO();

  /**
   * The transformer factory.
   */
  private static TransformerFactory transformerFactory = TransformerFactory.newInstance();

  private static EMRDocFilter emrDocFilter = new EMRDocFilter();

  private static GenericDAO documentsTypeDao = new GenericDAO("doc_type");
  
  private static final GenericDAO docHvfTemplateFieldsDAO =
      new GenericDAO("doc_hvf_template_fields");

  /**
   * Document to byte array.
   * 
   * @param dom
   *          the Document
   * @return the byte[]
   * @throws TransformerException
   *           the transformer exception
   */
  public static byte[] documentToByteArray(Document dom) throws TransformerException {
    DOMSource domSource = new DOMSource(dom);
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    Transformer transformer = transformerFactory.newTransformer();
    transformer.transform(domSource, result);
    return writer.toString().getBytes();
  }

  /**
   * Launch screen.
   * 
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward launchScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    return mapping.findForward("patientgeneraldocuments");
  }

  /*
   * This action returns a page where a list of document templates is shown, so that the user can
   * choose one of them as a template to base the new document on.
   */

  /**
   * Search patient general documents.
   * 
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param respone
   *          the respone
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public ActionForward searchPatientGeneralDocuments(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse respone)
      throws ServletException, IOException, SQLException, ParseException {

    ArrayList statusList = new ArrayList();
    String[] status = request.getParameterValues("status");
    if (status != null && !status[0].equals("")) {
      for (int i = 0; i < status.length; i++) {
        statusList.add(status[i]);
      }
    }
    String allowToDelPatDoc = null;
    String mrNo = request.getParameter("mr_no");
    if (mrNo == null || mrNo.equals("")) {
      return mapping.findForward("patientgeneraldocuments");
    }
    Map patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo);
    if (patmap == null) {
      FlashScope flash = FlashScope.getScope(request);
      flash.put("error", mrNo + " doesn't exists.");
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("successRedirect"));
      redirect.addParameter("defaultScreen", true);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }
    Boolean specialized = new Boolean(mapping.getProperty("specialized"));
    String specializedDocType = mapping.getProperty("documentType");
    String userId = (String) request.getSession(false).getAttribute("userid");

    int roleId = (Integer) request.getSession().getAttribute("roleId");
    if ((roleId != 1) && (roleId != 2)) {
      allowToDelPatDoc = (String) ((Map) request.getSession(false).getAttribute("actionRightsMap"))
          .get("allow_delete_patient_doc");
    } else {
      allowToDelPatDoc = "A";
    }
    AbstractDocumentPersistence persistenceAPI = AbstractDocumentPersistence
        .getInstance(specializedDocType, specialized);

    Map params = new HashMap(request.getParameterMap());
    Map extraParams = persistenceAPI.populateKeys(params, null);
    extraParams.put("username", userId);
    extraParams.put("fromDate", request.getParameter("doc_from_date"));
    extraParams.put("toDate", request.getParameter("doc_to_date"));
    extraParams.put("doc_type_id", request.getParameter("doc_type_id"));
    int centerId = RequestContext.getCenterId();
    extraParams.put("center_id", centerId);
    if (status != null && !status[0].equals("")) {
      extraParams.put("status", statusList);
    }

    extraParams.put("doc_name", request.getParameter("doc_name"));
    extraParams.put("template_name", request.getParameter("template_name"));
    Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(params);

    PagedList pagedList = persistenceAPI.searchDocuments(listingParams, extraParams, specialized,
        specializedDocType);
    List<BasicDynaBean> pagedDtoList = pagedList.getDtoList();
    List<BasicDynaBean> result = new ArrayList<>();
    List<Map> map = emrDocFilter.applyFilter(ConversionUtils.listBeanToListMap(pagedDtoList),
        request);
    for (BasicDynaBean pagedMap : pagedDtoList) {
      for (Map m : map) {
        if (String.valueOf(pagedMap.get("doc_id")).equals(String.valueOf(m.get("doc_id")))) {
          result.add(pagedMap);
        }
      }
    }
    pagedList.setDtoList(result);
    request.setAttribute("pagedList", pagedList);

    // TODO: this should come from the provider
    request.setAttribute("visitsList", VisitDetailsDAO.getAllCenterVisitsAndDoctors(mrNo));
    BasicDynaBean printPref = PrintConfigurationsDAO.getPageOptions("Discharge");
    request.setAttribute("printpreferneces", printPref);
    request.setAttribute("allowToDelPatDoc", allowToDelPatDoc);

    if (null != request.getAttribute("print")) {
      request.setAttribute("print", request.getAttribute("print").toString());
    }
    if (null != request.getAttribute("format")) {
      request.setAttribute("format", request.getAttribute("format").toString());
    }
    if (null != request.getAttribute("rawMode")) {
      request.setAttribute("rawMode", request.getAttribute("rawMode").toString());
    }
    if (null != request.getAttribute("printerId")) {
      request.setAttribute("printerId", request.getAttribute("printerId").toString());
    }

    return mapping.findForward("patientgeneraldocuments");
  }

  /**
   * Adds the patient document.
   * 
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   * @throws FileUploadException
   *           the file upload exception
   */
  @IgnoreConfidentialFilters
  public ActionForward addPatientDocument(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException, ParseException, FileUploadException {

    String documentType = mapping.getProperty("documentType");
    Boolean specialized = new Boolean(mapping.getProperty("specialized"));

    String mrNo = request.getParameter("mr_no");
    if (mrNo != null && !mrNo.equals("")) {
      Map patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo);
      if (patmap == null) {
        FlashScope flash = FlashScope.getScope(request);
        flash.put("error", mrNo + " doesn't exists.");
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("addPatientDocRedirect"));
        redirect.addParameter("defaultScreen", true);
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
      // TODO: this can be part of the mapping properties
      Map filterParams = new HashMap();
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
      if (request.getParameter("docTypeId") != null
          && !request.getParameter("docTypeId").equals("")) {
        filterParams.put("doc_type_id", new String[] { request.getParameter("docTypeId") });
      }
      if (request.getParameter("templateId") != null
          && !request.getParameter("templateId").equals("")) {
        filterParams.put("template_id", new String[] { request.getParameter("templateId") });
      }
      if (request.getParameter("templateName") != null
          && !request.getParameter("templateName").equals("")) {
        filterParams.put("template_name", new String[] { request.getParameter("templateName") });
      }
      Map<LISTING, Object> listing = ConversionUtils.getListingParameter(request.getParameterMap());
      PagedList list = GenericDocumentTemplateDAO.getGenericDocTemplates(filterParams, specialized,
          listing);
      List<BasicDynaBean> results = emrDocFilter.applyFilterOnDocTypes(list.getDtoList(), null,
          request);
      list.setDtoList(results);
      request.setAttribute("pagedList", list);
      JSONSerializer jsonSerializer = new JSONSerializer().exclude("class");
      request.setAttribute("listJson",
          jsonSerializer.serialize(ConversionUtils.copyListDynaBeansToMap(GenericDocumentTemplateDAO
              .getAllTemplatesBycenterFilter(filterParams, specialized))));
      request.setAttribute("visitsList", VisitDetailsDAO.getAllCenterVisitsAndDoctors(mrNo));
      BasicDynaBean activeVisitBean = new VisitDetailsDAO().getLatestActiveVisit(mrNo);
      request.setAttribute("activeVisitBean", activeVisitBean);
    }
    request.setAttribute("documentType", mapping.getProperty("documentType"));
    request.setAttribute("specialized", new Boolean(mapping.getProperty("specialized")));
    request.setAttribute("uploadFile", new Boolean(mapping.getProperty("uploadFile")));
    request.setAttribute("docLink", new Boolean(mapping.getProperty("doclink")));
    return mapping.findForward("addpatientdocument");
  }

  /**
   * Adds the.
   * 
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws TemplateException
   *           the template exception
   * @throws Exception
   *           the exception
   */
  public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException, SQLException,
      freemarker.template.TemplateException, Exception {
    int templateId = 0;
    String templateIdStr = request.getParameter("template_id");
    if (templateIdStr != null && !templateIdStr.equals("")) {
      templateId = Integer.parseInt(templateIdStr);
    }
    String format = request.getParameter("format");
    GenericDAO dao = new GenericDAO(format);
    String mrNo = request.getParameter("mr_no");
    String patientId = request.getParameter("patient_id");
    String forward = "";
    BasicDynaBean templateDetails = null;

    if (format.equals("doc_hvf_templates")) {
      forward = "addoredithvf";
      templateDetails = dao.findByKey("template_id", templateId);
      Map filterMap = new HashMap();
      filterMap.put("template_id", templateId);
      filterMap.put("field_status", "A");
      request.setAttribute("hvf_template_fields",
          docHvfTemplateFieldsDAO.listAll(null, filterMap, "display_order"));

    } else if (format.equals("doc_rich_templates")) {
      templateDetails = dao.findByKey("template_id", templateId);
      forward = "addoreditrichtext";
      String docContent = (String) templateDetails.get("template_content");

      Map<String, String> fields = new HashMap<String, String>();

      // add standard (hosp header etc.) and doctype specific fields
      // (patient, insurance details
      // etc.)
      AbstractDocumentPersistence persistenceAPI = getPersistenceAPI(mapping);
      Map keyParams = persistenceAPI.populateKeys(request.getParameterMap(), null);
      GenericDocumentsFields.copyStandardFields(fields, false);
      persistenceAPI.copyReplaceableFields(fields, keyParams, false);

      // process the template by replacing the tags with field values
      docContent = CommonHelper.replaceTags(docContent, fields, false);

      // add rich text title and optionally patient header
      docContent = CommonHelper.addRichTextTitle(docContent, (String) templateDetails.get("title"),
          (Integer) templateDetails.get("pheader_template_id"), patientId, mrNo);

      templateDetails.set("template_content", docContent);

    } else if (format.equals("doc_pdf_form_templates")) {
      forward = "addoreditpdfform";
      templateDetails = dao.getBean();
      dao.loadByteaRecords(templateDetails, "template_id", templateId);

    } else if (format.equals("doc_rtf_templates")) {
      forward = "addoreditrtf";
      templateDetails = dao.getBean();
      dao.loadByteaRecords(templateDetails, "template_id", templateId);
    } else if (format.equals("doc_fileupload")) {
      forward = "uploaddoc";

    } else {
      forward = "documentlink";
    }

    Integer printerId = (Integer) PrintConfigurationsDAO.getPatientDefaultPrintPrefs()
        .get("printer_id");
    String templateName = (templateDetails != null && templateDetails.get("template_name") != null)
        ? (String) templateDetails.get("template_name") : null;
    if (mapping.getProperty("documentType") != null
        && mapping.getProperty("documentType").equals("reg")
        && (format.equals("doc_rich_templates") || format.equals("doc_hvf_templates"))) {
      printerId = (Integer) DocumentPrintConfigurationsDAO
          .getRegistrationPrintConfiguration(templateName).get("printer_settings");
    }
    request.setAttribute("template_details", templateDetails);
    request.setAttribute("defaultPrintDefId", printerId);
    request.setAttribute("documentType", mapping.getProperty("documentType"));
    request.setAttribute("specialized", new Boolean(mapping.getProperty("specialized")));
    request.setAttribute("uploadFile", new Boolean(mapping.getProperty("uploadFile")));
    request.setAttribute("docNameRequired", mapping.getProperty("docNameRequired"));
    request.setAttribute("docDateRequired", mapping.getProperty("docDateRequired"));
    request.setAttribute("docLink", mapping.getProperty("doclink"));
    List<BasicDynaBean> docBeans = documentsTypeDao.listAll(null,"status","A");
    request.setAttribute("doc_details",
        emrDocFilter.applyFilterOnDocTypes(docBeans, null, request));
    return mapping.findForward(forward);
  }

  /**
   * Show.
   * 
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws InvocationTargetException
   *           the invocation target exception
   * @throws NoSuchMethodException
   *           the no such method exception
   * @throws IllegalAccessException
   *           the illegal access exception
   * @throws NumberFormatException
   *           the number format exception
   * @throws TemplateException
   *           the template exception
   * @throws Exception
   *           the exception
   */
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException, SQLException,
      InvocationTargetException, NoSuchMethodException, IllegalAccessException,
      NumberFormatException, freemarker.template.TemplateException, Exception {

    int docId = Integer.parseInt(request.getParameter("doc_id"));

    int templateId = 0;
    String templateIdStr = request.getParameter("template_id");
    if (templateIdStr != null && !templateIdStr.equals("")) {
      templateId = Integer.parseInt(templateIdStr);
    }

    String format = request.getParameter("format");
    String mrNo = request.getParameter("mr_no");
    BasicDynaBean templateDetails = null;

    GenericDAO templatedao = new GenericDAO(format);

    Map documentDetails = new HashMap();
    documentDetails.put("doc_id", docId);
    documentDetails.put("doc_name", null);
    documentDetails.put("doc_date", null);

    BasicDynaBean patientdocbean = patientdocdao.getPatientDocument(docId);
    documentDetails.putAll(patientdocbean.getMap());

    AbstractDocumentPersistence persistenceAPI = getPersistenceAPI(mapping);
    persistenceAPI.copyDocumentDetails(docId, documentDetails);

    request.setAttribute("document_details", documentDetails);
    String forward = "";

    if (format.equals("doc_hvf_templates")) {
      forward = "addoredithvf";
      templateDetails = templatedao.findByKey("template_id", templateId);
      request.setAttribute("template_details", templateDetails);
      List<BasicDynaBean> fieldslist =
          docHvfTemplateFieldsDAO.listAll(null, "template_id", templateId, "display_order");

      PatientHVFDocValuesDAO hvffieldsvaluesdao = new PatientHVFDocValuesDAO();
      List<BasicDynaBean> fieldsvalueslist = hvffieldsvaluesdao.findAllByKey("doc_id", docId);

      List imageFieldvalues = PatientHVFDocValuesDAO.getHVFDocImageValues(docId, true);

      List filledDocument = new ArrayList();
      if (!fieldslist.isEmpty()) {
        if (!fieldslist.isEmpty()) {
          for (BasicDynaBean fieldBean : fieldslist) {
            Map map = new HashMap(fieldBean.getMap());

            map.put("value_id", "");
            map.put("default_value", "");

            for (BasicDynaBean valueBean : fieldsvalueslist) {
              if (((Integer) fieldBean.get("field_id"))
                  .equals((Integer) valueBean.get("field_id"))) {
                map.put("value_id", valueBean.get("value_id"));
                map.put("default_value", valueBean.get("field_value"));
              }
            }

            if (imageFieldvalues != null && imageFieldvalues.size() > 0) {

              for (Object imgObj : imageFieldvalues) {
                BasicDynaBean imgbean = (BasicDynaBean) imgObj;
                if (((Integer) fieldBean.get("field_id"))
                    .equals((Integer) imgbean.get("field_id"))) {

                  int docImgeId = (Integer) imgbean.get("doc_image_id");
                  File imgFile = File.createTempFile("tempImage", "");
                  InputStream is = patientgendocdao.getHVFDocImage(docImgeId);

                  if (is != null) {
                    patientgendocdao.inputStreamToFile(is, imgFile);
                    is.close();

                    map.put("doc_image_id", (Integer) imgbean.get("doc_image_id"));
                    map.put("image_url", imgFile.getAbsolutePath());
                    map.put("field_image_content_type",
                        (String) imgbean.get("field_image_content_type"));
                    map.put("device_ip", (String) imgbean.get("device_ip"));
                    map.put("device_info", (String) imgbean.get("device_info"));
                    map.put("capture_time", (Timestamp) imgbean.get("capture_time"));
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
      request.setAttribute("hvf_template_fields", filledDocument);

    } else if (format.equals("doc_rich_templates")) {
      forward = "addoreditrichtext";
      templateDetails = templatedao.findByKey("template_id", templateId);
      BasicDynaBean valueBean = patientdocdao.getBean();
      patientdocdao.loadByteaRecords(valueBean, "doc_id", docId);
      Map map = new HashMap(valueBean.getMap());
      map.put("template_name", templateDetails.get("template_name"));
      map.put("title", templateDetails.get("title"));
      request.setAttribute("template_details", map);

    } else if (format.equals("doc_pdf_form_templates")) {
      forward = "addoreditpdfform";
      templateDetails = templatedao.getBean();
      templatedao.loadByteaRecords(templateDetails, "template_id", templateId);
      request.setAttribute("template_details", templateDetails);

      List pdfImageFields = new ArrayList();
      List<BasicDynaBean> imageTemplateFieldvalues = null;

      if (docId != 0) {
        imageTemplateFieldvalues = PatientPDFDocValuesDAO.getPDFTemplateImageValues(templateId);

        if (imageTemplateFieldvalues != null && imageTemplateFieldvalues.size() > 0) {

          List<BasicDynaBean> imageFieldvalues = PatientPDFDocValuesDAO.getPDFDocImageValues(docId);
          for (BasicDynaBean imgbean : imageTemplateFieldvalues) {
            Map map = new HashMap(imgbean.getMap());

            map.put("doc_image_id", 0);
            map.put("image_url", "");
            map.put("field_image_content_type", "");
            map.put("device_ip", "");
            map.put("device_info", "");
            map.put("capture_time", "");

            if (imageFieldvalues != null && imageFieldvalues.size() > 0) {
              for (BasicDynaBean fieldImgBean : imageFieldvalues) {

                if (((Integer) fieldImgBean.get("field_id"))
                    .equals((Integer) imgbean.get("field_id"))) {

                  int docImgeId = (Integer) fieldImgBean.get("doc_image_id");
                  File imgFile = File.createTempFile("tempImage", "");
                  InputStream is = PatientPDFDocValuesDAO.getPDFDocImage(docImgeId);

                  map.put("doc_image_id", (Integer) fieldImgBean.get("doc_image_id"));

                  if (is != null) {
                    patientgendocdao.inputStreamToFile(is, imgFile);
                    is.close();
                    map.put("image_url", imgFile.getAbsolutePath());
                    map.put("field_image_content_type",
                        (String) fieldImgBean.get("field_image_content_type"));
                    map.put("device_ip", (String) fieldImgBean.get("device_ip"));
                    map.put("device_info", (String) fieldImgBean.get("device_info"));
                    map.put("capture_time", (Timestamp) fieldImgBean.get("capture_time"));
                  }
                }
              }
            }

            pdfImageFields.add(map);
          }
        }
      }

      request.setAttribute("pdf_template_ext_fields", pdfImageFields);

    } else if (format.equals("doc_rtf_templates")) {
      forward = "addoreditrtf";
      templateDetails = templatedao.getBean();
      templatedao.loadByteaRecords(templateDetails, "template_id", templateId);
      request.setAttribute("template_details", templateDetails);

    } else if (format.equals("doc_fileupload")) {
      BasicDynaBean valueBean = patientdocdao.getBean();
      patientdocdao.loadByteaRecords(valueBean, "doc_id", docId);
      documentDetails.put("doc_type", valueBean.get("doc_type"));

      forward = "uploaddoc";

    } else {
      BasicDynaBean valueBean = patientdocdao.getBean();
      patientdocdao.loadByteaRecords(valueBean, "doc_id", docId);
      documentDetails.put("doc_type", valueBean.get("doc_type"));
      documentDetails.put("doc_location", valueBean.get("doc_location"));
      forward = "documentlink";
    }

    Integer printerId = (Integer) PrintConfigurationsDAO.getPatientDefaultPrintPrefs()
        .get("printer_id");

    if (null != templateDetails) {
      String templateName = (String) templateDetails.get("template_name");
      if (templateName != null && "reg".equals(mapping.getProperty("documentType"))
          && (format.equals("doc_rich_templates") || format.equals("doc_hvf_templates"))) {
        BasicDynaBean docPrintBean = DocumentPrintConfigurationsDAO
            .getRegistrationPrintConfiguration(templateName);
        printerId = (Integer) docPrintBean.get("printer_settings");
      }
    }

    request.setAttribute("defaultPrintDefId", printerId);
    request.setAttribute("documentType", mapping.getProperty("documentType"));
    request.setAttribute("specialized", new Boolean(mapping.getProperty("specialized")));
    request.setAttribute("uploadFile", new Boolean(mapping.getProperty("uploadFile")));
    request.setAttribute("docNameRequired", mapping.getProperty("docNameRequired"));
    request.setAttribute("docDateRequired", mapping.getProperty("docDateRequired"));
    request.setAttribute("docLink", mapping.getProperty("doclink"));
    List<BasicDynaBean> docBeans = documentsTypeDao.listAll(null,"status","A");
    request.setAttribute("doc_details",
        emrDocFilter.applyFilterOnDocTypes(docBeans, null, request));
    return mapping.findForward(forward);
  }

  /**
   * View document field image.
   * 
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward viewDocumentFieldImage(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
    String format = request.getParameter("format");
    String docImageIdStr = request.getParameter("doc_image_id");
    Integer docImageId = (docImageIdStr != null && !docImageIdStr.equals(""))
        ? new Integer(docImageIdStr) : null;
    if (docImageId != null) {
      response.setContentType("image/png");
      OutputStream os = response.getOutputStream();

      InputStream is = null;
      byte[] bytes = null;

      if (format.equals("doc_hvf_templates")) {
        is = patientgendocdao.getHVFDocImage(docImageId);
      } else if (format.equals("doc_pdf_form_templates")) {
        is = PatientPDFDocValuesDAO.getPDFDocImage(docImageId);
      }

      if (is != null) {
        bytes = new byte[is.available()];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
            && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
          offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
          throw new IOException("Could not completely read.");
        }

        os.write(bytes);
        os.flush();
        is.close();
        return null;

      } else {
        return mapping.findForward("error");
      }
    } else {
      return mapping.findForward("error");
    }
  }

  /**
   * Creates the.
   * 
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   * @throws FileUploadException
   *           the file upload exception
   */
  public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException, SQLException, ParseException, FileUploadException {

    log.debug("Getting parameter map using getParameterMap in base action");
    Map params = getParameterMap(request);

    String error = null;

    if (params.get("fileSizeError") != null) {
      // if the file size is greater than 10 MB prompting the user with
      // the failure message.
      return mapping.findForward("fileUploadSizeError");
    }
    String userid = (String) request.getSession(false).getAttribute("userid");
    copyStringToMap(params, "username", userid);
    String format = getParameter(params, "format");
    String templateId = getParameter(params, "template_id");
    String isNewUx = getParameter(params, "is_new_ux");
    if (isNewUx == null) {
      isNewUx = "false";
    }
    /*
     * GenericDAO templatedao = new GenericDAO(format);
     *
     * List<String> fields = new ArrayList<String>(); fields.add("template_name"); Map<String,
     * Object> key = new HashMap<String, Object>(); key.put("template_id",
     * Integer.parseInt(templateId)); BasicDynaBean templateDetails = templatedao.findByKey(fields,
     * key); String templateName = (templateDetails != null && templateDetails.get("template_name")
     * != null) ? (String)templateDetails.get("template_name") : null;
     */
    if (format.equals("doc_rich_templates")) {
      GenericDAO richTempDAO = new GenericDAO(format);
      BasicDynaBean richTextBean = richTempDAO.findByKey("template_id",
          Integer.parseInt(templateId));
      Integer pheaderTemplateId = (Integer) richTextBean.get("pheader_template_id");
      copyObjectToMap(params, "pheader_template_id", pheaderTemplateId);
    }

    AbstractDocumentPersistence persistenceAPI = getPersistenceAPI(mapping);
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean result = false;
    try {
      result = persistenceAPI.create(params, con);
    } finally {
      DataBaseUtil.commitClose(con, result);
    }

    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = null;
    if (result) {
      flash.put("success", params.get("success"));
      String printerDef = request.getParameter("printerDef");
      redirect = new ActionRedirect(mapping.findForward("failureShowRedirect"));

      if ((format.equals("doc_hvf_templates") || format.equals("doc_rich_templates"))
          && !isNewUx.equals("true")) {
        ActionRedirect printRedirect = new ActionRedirect(mapping.findForward("printRedirect"));
        printRedirect.addParameter("doc_id", getParameter(params, "doc_id"));
        printRedirect.addParameter("allFields", "N");
        printRedirect.addParameter("printerId", printerDef);
        // printRedirect.addParameter("templateName", templateName);

        List<String> printURLs = new ArrayList<String>();
        printURLs.add(request.getContextPath() + printRedirect.getPath());
        request.getSession(false).setAttribute("printURLs", printURLs);
      }

    } else {
      flash.put("error", params.get("error"));
      redirect = new ActionRedirect(mapping.findForward("failureAddRedirect"));
      redirect.addParameter("format", format);
      redirect.addParameter("template_id", templateId);
    }

    redirect.addParameter("mr_no", getParameter(params, "mr_no"));
    redirect.addParameter("patient_id", getParameter(params, "patient_id"));
    redirect.addParameter("insurance_id", getParameter(params, "insurance_id"));
    redirect.addParameter("consultation_id", getParameter(params, "consultation_id"));
    redirect.addParameter("doc_id", getParameter(params, "doc_id"));
    redirect.addParameter("prescription_id", getParameter(params, "prescription_id"));
    redirect.addParameter("format", getParameter(params, "format"));
    redirect.addParameter("template_id", templateId);
    redirect.addParameter("operation_details_id", getParameter(params, "operation_details_id"));
    redirect.addParameter("prescribed_id", getParameter(params, "prescribed_id"));
    redirect.addParameter("visitId", getParameter(params, "visitId"));
    redirect.addParameter("is_new_ux", getParameter(params, "is_new_ux"));
    String documentType = mapping.getProperty("documentType");
    Boolean specialized = new Boolean(mapping.getProperty("specialized"));
    if (specialized
        && (documentType.equals("lab_test_doc") || documentType.equals("rad_test_doc"))) {
      redirect.addParameter("prescribed_id", getParameter(params, "prescribed_id"));
      redirect.addParameter("isIncomingPatient", getParameter(params, "isIncomingPatient"));
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Sets the print attributes.
   * 
   * @param flash
   *          the flash
   * @param format
   *          the format
   * @param templateId
   *          the template id
   * @throws SQLException
   *           the SQL exception
   */
  private void setPrintAttributes(FlashScope flash, String format, String templateId)
      throws SQLException {
    BasicDynaBean printPref = PrintConfigurationsDAO.getPageOptions("Discharge");
    flash.put("printerId", printPref.get("printer_id"));
  }

  /**
   * Update.
   * 
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   * @throws SQLException
   *           the SQL exception
   * @throws FileUploadException
   *           the file upload exception
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException, ParseException, SQLException, FileUploadException {
    Map<String, Object[]> params = getParameterMap(request);

    if (params.get("fileSizeError") != null) {
      // if the file size is greater than 10 MB prompting the user with
      // the failure message.
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("fileUploadSizeError"));
      return redirect;
    }
    String format = getParameter(params, "format");
    int docId = Integer.parseInt(getParameter(params, "doc_id"));
    String templateId = getParameter(params, "template_id");
    String userid = (String) request.getSession(false).getAttribute("userid");
    String isNewUx = getParameter(params, "is_new_ux");
    if (isNewUx == null) {
      isNewUx = "false";
    }
    params.put("username", new Object[] { userid });
    /*
     * GenericDAO templatedao = new GenericDAO(format);
     *
     * List<String> fields = new ArrayList<String>(); fields.add("template_name"); Map<String,
     * Object> key = new HashMap<String, Object>(); key.put("template_id",
     * Integer.parseInt(templateId)); BasicDynaBean templateDetails = templatedao.findByKey(fields,
     * key); String templateName = (templateDetails != null && templateDetails.get("template_name")
     * != null) ? (String)templateDetails.get("template_name") : null;
     */
    String action = getParameter(params, "_action");
    boolean finalize = action != null && action.equals("finalize");
    boolean saveExtFields = action != null && action.equals("saveExtFields");

    AbstractDocumentPersistence persistenceAPI = getPersistenceAPI(mapping);

    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean result = false;
    try {
      result = persistenceAPI.update(params, con, (saveExtFields || finalize));
      if (result && finalize
          && MessageUtil.allowMessageNotification(request, "general_message_send")) {
        params.put("deleteDocument",
            new String[] { request.getParameter("doc_id") + "," + format });
        result = persistenceAPI.finalize(con, params);
        if (result && format.equalsIgnoreCase("doc_fileupload")) {
          Map reportData = new HashMap();
          MessageManager mgr = new MessageManager();
          reportData.put("doc_id", new String[] { request.getParameter("doc_id") });
          mgr.processEvent("gen_doc_finalize", reportData);
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, result);
    }

    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = null;

    if (result) {
      flash.put("success", params.get("success"));

      String printerDef = request.getParameter("printerDef");
      redirect = new ActionRedirect(mapping.findForward("failureShowRedirect"));

      if ((format.equals("doc_hvf_templates") || format.equals("doc_rich_templates"))
          && !isNewUx.equals("true")) {
        ActionRedirect printRedirect = new ActionRedirect(mapping.findForward("printRedirect"));
        printRedirect.addParameter("doc_id", docId);
        printRedirect.addParameter("allFields", "N");
        printRedirect.addParameter("printerId", printerDef);
        // printRedirect.addParameter("templateName", templateName);

        List<String> printURLs = new ArrayList<String>();
        printURLs.add(request.getContextPath() + printRedirect.getPath());
        request.getSession(false).setAttribute("printURLs", printURLs);
      }

    } else {
      flash.put("error", params.get("error"));
      redirect = new ActionRedirect(mapping.findForward("failureShowRedirect"));
      redirect.addParameter("format", format);
      redirect.addParameter("template_id", templateId);
      redirect.addParameter("doc_id", docId);
    }

    redirect.addParameter("mr_no", getParameter(params, "mr_no"));
    redirect.addParameter("patient_id", getParameter(params, "patient_id"));
    redirect.addParameter("insurance_id", getParameter(params, "insurance_id"));
    redirect.addParameter("consultation_id", getParameter(params, "consultation_id"));
    redirect.addParameter("format", format);
    redirect.addParameter("template_id", templateId);
    redirect.addParameter("doc_id", docId);
    redirect.addParameter("prescription_id", getParameter(params, "prescription_id"));
    redirect.addParameter("operation_details_id", getParameter(params, "operation_details_id"));
    redirect.addParameter("prescribed_id", getParameter(params, "prescribed_id"));
    redirect.addParameter("visitId", getParameter(params, "visitId"));
    redirect.addParameter("is_new_ux", getParameter(params, "is_new_ux"));
    String documentType = mapping.getProperty("documentType");
    Boolean specialized = new Boolean(mapping.getProperty("specialized"));
    if (specialized
        && (documentType.equals("lab_test_doc") || documentType.equals("rad_test_doc"))) {
      redirect.addParameter("prescribed_id", getParameter(params, "prescribed_id"));
      redirect.addParameter("isIncomingPatient", getParameter(params, "isIncomingPatient"));
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * Open pdf form.
   * 
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws DocumentException
   *           the document exception
   * @throws TransformerException
   *           the transformer exception
   * @throws ParserConfigurationException
   *           the parser configuration exception
   */
  @IgnoreConfidentialFilters
  public ActionForward openPdfForm(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException, DocumentException, TransformerException,
      ParserConfigurationException {

    BasicDynaBean bean = pdftemplatedao.getBean();
    int documentId = Integer.parseInt(request.getParameter("template_id"));
    pdftemplatedao.loadByteaRecords(bean, "template_id", documentId);
    InputStream pdf = (InputStream) bean.get("template_content");

    // Read the html template.
    InputStream html = (InputStream) bean.get("html_template");
    if (null == html && null != pdf) {
      PDDocument document = null;
      document = PDDocument.load(pdf);
      PDFDomTree domTree = new PdfFormToDom();
      Document dom = domTree.createDOM(document);
      byte[] htmlBytes = documentToByteArray(dom);
      Map<String, byte[]> dataMap = new HashMap<>();
      dataMap.put("html_template", htmlBytes);
      String[] updateColumns = { "html_template" };
      Map keyMap = new HashMap();
      keyMap.put("template_id", documentId);
      Connection con = DataBaseUtil.getConnection();
      pdftemplatedao.update(con, updateColumns, dataMap, keyMap);
      DataBaseUtil.closeConnections(con, null);

      html = new ByteArrayInputStream(htmlBytes);
    }

    Map<String, String> fields = new HashMap<String, String>();

    AbstractDocumentPersistence persistenceAPI = getPersistenceAPI(mapping);
    Map keyParams = persistenceAPI.populateKeys(request.getParameterMap(), null);

    String userId = (String) request.getSession(false).getAttribute("userid");
    keyParams.put("userId", userId);

    GenericDocumentsFields.copyStandardFields(fields, true);
    try {
      persistenceAPI.copyReplaceableFields(fields, keyParams, true);
    } catch (Exception ex) {
      log.error("", ex);
      throw (new RuntimeException("Unable to fill data"));
    }

    String display = request.getParameter("display");
    boolean pdfFieldsFlatten = false;
    if (display != null && display.equals("view")) {
      pdfFieldsFlatten = true;
    }

    String docId = request.getParameter("doc_id");
    if ((docId != null) && (!docId.equals(""))) {
      List<BasicDynaBean> fieldslist = pdfvaluesdocdao.listAll(null, "doc_id",
          Integer.parseInt(docId));
      for (BasicDynaBean fieldsBean : fieldslist) {
        fields.put(fieldsBean.get("field_name").toString(),
            fieldsBean.get("field_value").toString());
      }
    }

    String submitUrl;
    if ("mlc".equalsIgnoreCase(mapping.getProperty("documentType"))) {
      submitUrl = request.getContextPath() + "/MLCDocuments/MLCDocumentsAction.do";
    } else if ("reg".equalsIgnoreCase(mapping.getProperty("documentType"))) {
      submitUrl = request.getContextPath() + "/pages/RegistrationDocuments.do";
    } else if ("insurance".equalsIgnoreCase(mapping.getProperty("documentType"))) {
      submitUrl = request.getContextPath() + "/Insurance/InsuranceGenericDocuments.do";
    } else if ("tpapreauth".equalsIgnoreCase(mapping.getProperty("documentType"))) {
      submitUrl = request.getContextPath() + "/Insurance/PreAuthorizationForms.do";
    } else if ("dietary".equalsIgnoreCase(mapping.getProperty("documentType"))) {
      submitUrl = request.getContextPath() + "/Dietary/DietaryGenericDocuments.do";
    } else if ("op_case_form_template".equalsIgnoreCase(mapping.getProperty("documentType"))) {
      submitUrl = request.getContextPath() + "/Outpatient/OutPatientDocuments.do";
    } else if ("ot".equalsIgnoreCase(mapping.getProperty("documentType"))) {
      submitUrl = request.getContextPath() + "/otservices/AddrEditOperationDocuments.do";
    } else if ("service".equalsIgnoreCase(mapping.getProperty("documentType"))) {
      submitUrl = request.getContextPath() + "/Services/ServiceReports.do";
    } else {
      submitUrl = request.getContextPath() + "/pages/GenericDocuments/GenericDocumentsAction.do";
    }

    // Set some hidden params so that the form submission knows what to do
    // with this
    // form that is being submitted
    HashMap<String, String> hiddenParams = new HashMap<String, String>();
    if ((docId != null) && (!docId.equals(""))) {
      hiddenParams.put("_method", "update");
    } else {
      hiddenParams.put("_method", "create");
    }
    hiddenParams.put("doc_name", request.getParameter("doc_name"));
    hiddenParams.put("mr_no", request.getParameter("mr_no"));
    hiddenParams.put("patient_id", request.getParameter("patient_id"));
    hiddenParams.put("template_id", request.getParameter("template_id"));
    hiddenParams.put("doc_id", request.getParameter("doc_id"));
    hiddenParams.put("doc_date", request.getParameter("doc_date"));
    hiddenParams.put("format", request.getParameter("format"));
    hiddenParams.put("insurance_id", request.getParameter("insurance_id"));
    hiddenParams.put("consultation_id", request.getParameter("consultation_id"));
    hiddenParams.put("prescription_id", request.getParameter("prescription_id"));
    hiddenParams.put("doc_type", request.getParameter("doc_type"));
    hiddenParams.put("isIncomingPatient", request.getParameter("isIncomingPatient"));
    hiddenParams.put("is_new_ux", request.getParameter("is_new_ux"));
    if (request.getParameter("finalise_doc") != null
        && request.getParameter("finalise_doc").equals("true")) {
      hiddenParams.put("_action", "finalize");
    }

    String[] fieldIdArr = request.getParameterValues("field_id");
    String[] fieldInputArr = request.getParameterValues("field_input");
    String[] deviceIpArr = request.getParameterValues("device_ip");
    String[] deviceInfoArr = request.getParameterValues("device_info");
    String[] fieldImgTextArr = request.getParameterValues("fieldImgText");

    if (fieldIdArr != null && fieldIdArr.length > 0) {
      for (int i = 0; i < fieldIdArr.length; i++) {
        hiddenParams.put("field_id" + "_" + i, fieldIdArr[i]);
        hiddenParams.put("field_input" + "_" + i, fieldInputArr[i]);
        hiddenParams.put("device_ip" + "_" + i, deviceIpArr[i]);
        hiddenParams.put("device_info" + "_" + i, deviceInfoArr[i]);
        hiddenParams.put("fieldImgText" + "_" + i, fieldImgTextArr[i]);
      }
    }

    // response.setContentType("application/pdf");
    OutputStream os = response.getOutputStream();
    InputStream is = html;
    if (pdfFieldsFlatten) {
      submitUrl = null;
      is = pdf;
      response.setContentType("application/pdf");
    }
    PdfUtils.sendFillableForm(os, is, fields, pdfFieldsFlatten, submitUrl, hiddenParams, null);

    return null;
  }

  /**
   * Gets the rtf document.
   * 
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the rtf document
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws Exception
   *           the exception
   */
  public ActionForward getRtfDocument(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, IOException, Exception {

    int templateId = Integer.parseInt(request.getParameter("template_id"));
    String format = request.getParameter("format");
    String mrNo = request.getParameter("mr_no");
    String docId = request.getParameter("doc_id");
    BasicDynaBean bean = null;
    String fileName = null;
    GenericDAO genericdao = new GenericDAO(format);
    OutputStream stream = response.getOutputStream();
    if ((docId == null) || docId.equals("")) {
      bean = genericdao.getBean();
      genericdao.loadByteaRecords(bean, "template_id", templateId);
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

      Map<String, String> fields = new HashMap<String, String>();
      GenericDocumentsFields.copyStandardFields(fields, false);

      AbstractDocumentPersistence persistenceAPI = getPersistenceAPI(mapping);
      Map keyParams = persistenceAPI.populateKeys(request.getParameterMap(), null);
      persistenceAPI.copyReplaceableFields(fields, keyParams, false);

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
        stream.write(
            DataBaseUtil.readInputStream((java.io.InputStream) bean.get("template_content")));
      }

    } else {
      PatientDocumentsDAO dao = new PatientDocumentsDAO();
      bean = dao.getBean();
      dao.loadByteaRecords(bean, "doc_id", Integer.parseInt(docId));
      BasicDynaBean genbean = null;

      String documentType = mapping.getProperty("documentType");
      Boolean specialized = new Boolean(mapping.getProperty("specialized"));

      if (specialized) {
        if (documentType.equals("insurance")) {
          genbean = new GenericDAO("insurance_docs").findByKey("doc_id", Integer.parseInt(docId));
          fileName = genbean.get("doc_name").toString();
        } else {
          genbean = genericdao.getBean();
          genericdao.loadByteaRecords(genbean, "template_id", templateId);
          fileName = genbean.get("template_name").toString();
        }
      } else {
        genbean = patientgendocdao.findByKey("doc_id", Integer.parseInt(docId));
        fileName = genbean.get("doc_name").toString();
      }

      if (bean.get("content_type").toString().equals("application/rtf")
          || bean.get("content_type").toString().equals("text/rtf")) {
        fileName = fileName + "_" + mrNo + ".rtf";
      } else {
        fileName = fileName + "_" + mrNo;
      }
      response.setContentType(bean.get("content_type").toString());
      response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");

      stream
          .write(DataBaseUtil.readInputStream((java.io.InputStream) bean.get("doc_content_bytea")));
    }

    stream.close();
    return null;
  }

  /**
   * Empty.
   * 
   * @param str
   *          the str
   * @return true, if successful
   */
  public boolean empty(Object str) {
    return (str == null || str.toString().equals(""));
  }

  /**
   * Not empty.
   * 
   * @param str
   *          the str
   * @return true, if successful
   */
  public boolean notEmpty(Object str) {
    return !empty(str);
  }

  /**
   * Delete documents.
   * 
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws Exception
   *           the exception
   */
  public ActionForward deleteDocuments(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, Exception {

    Map params = new HashMap(request.getParameterMap());

    AbstractDocumentPersistence persistenceAPI = getPersistenceAPI(mapping);
    String[] deleted = (String[]) params.get("deleteDocument");
    Map docData = new HashMap();
    for (int i = 0; i < deleted.length; i++) {
      String[] temp = deleted[i].split(",");
      if (temp[1].equalsIgnoreCase("doc_fileupload")) {
        BasicDynaBean bean = new PatientDocumentsDAO()
            .getPatientDocument(Integer.parseInt(temp[0]));
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("doc_type_id", (String) bean.get("doc_type"));
        BasicDynaBean docTypeBean = documentsTypeDao.findByKey(map);
        docData.put(temp[0], (String) docTypeBean.get("doc_type_name"));
      }
    }
    boolean result = persistenceAPI.delete(params);
    if (result && MessageUtil.allowMessageNotification(request, "general_message_send")) {

      MessageManager mgr = new MessageManager();
      for (int i = 0; i < deleted.length; i++) {
        String[] temp = deleted[i].split(",");
        if (temp[1].equalsIgnoreCase("doc_fileupload")) {
          Map reportData = new HashMap();
          String[] data = new String[] { temp[0] };
          reportData.put("doc_id", data);
          String[] mrNo = (String[]) params.get("mr_no");
          reportData.put("mr_no", mrNo);
          reportData.put("record_type", "doc_" + docData.get(temp[0]));
          reportData.put("document_id", Integer.parseInt(temp[0]));
          mgr.processEvent("gen_doc_delete", reportData);
        }
      }
    }

    FlashScope flash = FlashScope.getScope(request);
    flash.put("error", params.get("error"));
    flash.put("success", params.get("success"));
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("successRedirect"));
    redirect.addParameter("mr_no", request.getParameter("mr_no"));
    redirect.addParameter("patient_id", request.getParameter("patient_id"));
    redirect.addParameter("insurance_id", request.getParameter("insurance_id"));
    redirect.addParameter("consultation_id", request.getParameter("consultation_id"));

    String documentType = mapping.getProperty("documentType");
    Boolean specialized = new Boolean(mapping.getProperty("specialized"));
    if (specialized) {
      if (documentType.equals("lab_test_doc") || documentType.equals("rad_test_doc")) {
        redirect.addParameter("prescribed_id", getParameter(params, "prescribed_id"));
        redirect.addParameter("isIncomingPatient", getParameter(params, "isIncomingPatient"));
      }
      if (documentType.equals("ot")) {
        redirect.addParameter("prescription_id", getParameter(params, "prescription_id"));
        redirect.addParameter("operation_details_id", request.getParameter("operation_details_id"));
        redirect.addParameter("prescribed_id", request.getParameter("prescribed_id"));
        redirect.addParameter("visitId", request.getParameter("visitId"));
      }
    }

    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Finalize documents.
   * 
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward finalizeDocuments(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, Exception {

    Map params = new HashMap(request.getParameterMap());

    AbstractDocumentPersistence persistenceAPI = getPersistenceAPI(mapping);
    boolean result = persistenceAPI.finalize(params);

    if (result && MessageUtil.allowMessageNotification(request, "general_message_send")) {
      String[] finalized = (String[]) params.get("deleteDocument");
      MessageManager mgr = new MessageManager();
      for (int i = 0; i < finalized.length; i++) {
        String[] temp = finalized[i].split(",");
        if (temp[1].equalsIgnoreCase("doc_fileupload")) {
          Map reportData = new HashMap();
          String[] data = new String[] { temp[0] };
          reportData.put("doc_id", data);
          mgr.processEvent("gen_doc_finalize", reportData);
        }
      }
    }
    FlashScope flash = FlashScope.getScope(request);
    flash.put("error", params.get("error"));
    flash.put("success", params.get("success"));
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("successRedirect"));
    redirect.addParameter("mr_no", request.getParameter("mr_no"));
    redirect.addParameter("patient_id", request.getParameter("patient_id"));
    redirect.addParameter("insurance_id", request.getParameter("insurance_id"));
    redirect.addParameter("consultation_id", request.getParameter("consultation_id"));

    String documentType = mapping.getProperty("documentType");
    Boolean specialized = new Boolean(mapping.getProperty("specialized"));
    if (specialized) {
      if (documentType.equals("lab_test_doc") || documentType.equals("rad_test_doc")) {
        redirect.addParameter("prescribed_id", getParameter(params, "prescribed_id"));
      }
      if (documentType.equals("ot")) {
        redirect.addParameter("prescription_id", getParameter(params, "prescription_id"));
      }
    }

    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Gets the persistence API.
   * 
   * @param mapping
   *          the mapping
   * @return the persistence API
   */
  public AbstractDocumentPersistence getPersistenceAPI(ActionMapping mapping) {
    String specializedDocType = mapping.getProperty("documentType");
    Boolean specialized = new Boolean(mapping.getProperty("specialized"));

    return AbstractDocumentPersistence.getInstance(specializedDocType, specialized);
  }

  /**
   * View text image.
   * 
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward viewTextImage(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, Exception {

    String base64String = request.getParameter("imageText");

    Base64 decoder = new Base64();
    byte[] decodedBytes = decoder.decode(base64String.getBytes());

    ByteArrayInputStream is = new ByteArrayInputStream(decodedBytes);

    File imgFile = File.createTempFile("tempImage", "");

    BufferedImage image = null;
    byte[] bytes = null;
    if (is != null) {

      response.setContentType("image/png");
      OutputStream os = response.getOutputStream();

      image = ImageIO.read(is);
      if (image == null) {
        log.error("Buffered Image is null");
      }

      // write the image
      ImageIO.write(image, "png", imgFile);

      try (FileInputStream fis = new FileInputStream(imgFile)) {
        bytes = new byte[fis.available()];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
            && (numRead = fis.read(bytes, offset, bytes.length - offset)) >= 0) {
          offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
          throw new IOException("Could not completely read.");
        }

        os.write(bytes);
        os.flush();
        is.close();
      }
    }

    response.flushBuffer();
    response.reset();
    response.resetBuffer();
    return null;
  }
}
