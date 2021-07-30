package com.insta.hms.documents;

import com.insta.hms.common.BaseController;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.ValidationException;
import com.lowagie.text.DocumentException;

import freemarker.template.TemplateException;

import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


// TODO: Auto-generated Javadoc
/**
 * The Class GenericDocumentsController.
 */
public abstract class DocumentsController extends BaseController {

  /** The service. */
  private DocumentsService service;

  /**
   * Instantiates a new generic documents controller.
   *
   * @param service
   *          the service
   */
  public DocumentsController(DocumentsService service) {
    this.service = service;
  }

  /**
   * Gets the url.
   *
   * @param contextPath
   *          the context path
   * @param docId
   *          the doc id
   * @return the url
   */
  public abstract String getURL(String contextPath, String docId);

  /** The Constant MAXIMUM_SIZE. */
  private static final Long MAXIMUM_SIZE = 10L * 1024L * 1024L;

  /**
   * Search patient general documents.
   *
   * @param request
   *          the request
   * @param respone
   *          the respone
   * @param redirect
   *          the redirect
   * @return the model and view
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   */
  @RequestMapping(value = "/search", method = RequestMethod.GET)
  public ModelAndView searchPatientGeneralDocuments(HttpServletRequest request,
      HttpServletResponse respone, RedirectAttributes redirect) throws IOException, ParseException {
    Map<String, String[]> params = request.getParameterMap();
    Map<String, Object> requestMap = new HashMap<>();
    service.searchDocuments(params, requestMap);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /*
   * This action returns a page where a list of document templates is shown, so that the user can
   * choose one of them as a template to base the new document on.
   */
  /**
   * Adds the patient document.
   *
   * @param request
   *          the request
   * @param redirect
   *          the redirect
   * @param response
   *          the response
   * @return the model and view
   * @throws ParseException
   *           the parse exception
   */
  @RequestMapping(value = "/addDoc", method = RequestMethod.GET)
  public ModelAndView addPatientDocument(HttpServletRequest request, RedirectAttributes redirect,
      HttpServletResponse response) throws ParseException {
    Map<String, String[]> params = request.getParameterMap();
    Map<String, Object> requestMap = new HashMap<>();
    service.addDocument(params, requestMap);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Adds the.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the model and view
   * @throws TemplateException
   *           the template exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  @RequestMapping(value = "/add", method = RequestMethod.GET)
  public ModelAndView add(HttpServletRequest request, HttpServletResponse response)
      throws TemplateException, IOException, SQLException {
    Map<String, String[]> params = request.getParameterMap();
    Map<String, Object> requestMap = new HashMap<>();

    service.add(params, requestMap);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Open pdf form.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws DocumentException
   *           the document exception
   * @throws SQLException
   *           the SQL exception
   */
  @RequestMapping(value = "/openPdfForm", method = RequestMethod.GET)
  public void openPdfForm(HttpServletRequest request, HttpServletResponse response)
      throws IOException, DocumentException, SQLException {

    Map<String, String[]> params = request.getParameterMap();
    String docId = getParameter(params, "doc_id");

    // Set some hidden params so that the form submission knows what to do
    // with this
    // form that is being submitted
    HashMap<String, String> hiddenParams = new HashMap<>();
    if ((docId != null) && (!docId.equals(""))) {
      hiddenParams.put("_method", "update");
    } else {
      hiddenParams.put("_method", "create");
    }
    hiddenParams.put("doc_name", getParameter(params, "doc_name"));
    hiddenParams.put("mr_no", getParameter(params, "mr_no"));
    hiddenParams.put("patient_id", getParameter(params, "patient_id"));
    hiddenParams.put("template_id", getParameter(params, "template_id"));
    hiddenParams.put("doc_id", getParameter(params, "doc_id"));
    hiddenParams.put("doc_date", getParameter(params, "doc_date"));
    hiddenParams.put("format", getParameter(params, "format"));
    hiddenParams.put("insurance_id", getParameter(params, "insurance_id"));
    hiddenParams.put("consultation_id", getParameter(params, "consultation_id"));
    hiddenParams.put("prescription_id", getParameter(params, "prescription_id"));
    hiddenParams.put("doc_type", getParameter(params, "doc_type"));
    hiddenParams.put("isIncomingPatient", getParameter(params, "isIncomingPatient"));// (String)
    // params.get("isIncomingPatient")[0]);

    String[] fieldIdArr = params.get("field_id");
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
    response.setContentType("application/pdf");
    OutputStream os = response.getOutputStream();
    String contextPath = request.getContextPath();
    String submitUrl = getURL(contextPath, docId);
    service.openPdfForm(params, os, submitUrl, hiddenParams);
  }

  /**
   * Gets the rtf document.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the rtf document
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  @RequestMapping(value = "/getRtfDoc", method = RequestMethod.GET)
  public ModelAndView getRtfDocument(HttpServletRequest request, HttpServletResponse response)
      throws IOException, SQLException {
    Map<String, String[]> params = request.getParameterMap();
    service.getRtfDocument(params, response);
    return null;
  }

  /**
   * Creates the.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @param redirect
   *          the redirect
   * @return the model and view
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  public ModelAndView create(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes redirect) throws IOException {
    Map<String, Object[]> params = new HashMap<String, Object[]>(request.getParameterMap());
    MultipartFile file = super.getFiles(request).get("fileName");
    params.put("fileName", new Object[] { file });
    Map<String, Object> requestMap = new HashMap<>();
    if (file != null && file.getSize() > MAXIMUM_SIZE) {
      throw new ValidationException("exception.maximum.size.violation");
    } else if (file != null) {
      Map<String, Object> result = service.create(params);
      if ((Boolean) result.get("status")) {
        requestMap.put("docId", result.get("docId"));
        response.setStatus(HttpStatus.OK.value());
      } else {
        requestMap.put("status", params.get("error"));
        response.setStatus(HttpStatus.BAD_REQUEST.value());
      }
    } else {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Bulk upload.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @param redirect
   *          the redirect
   * @return the model and view
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = "/bulkupload", method = RequestMethod.POST)
  public ModelAndView bulkUpload(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes redirect) throws IOException {
    Map<String, Object[]> params = new HashMap<String, Object[]>(request.getParameterMap());
    MultiValueMap<String, MultipartFile> filesMap = super.getAllFiles(request);
    List<MultipartFile> files = null != filesMap ? filesMap.get("fileName") : null;
    MultipartFile[] fileArray = new MultipartFile[files.size()];
    files.toArray(fileArray);
    params.put("fileName", fileArray);
    Map<String, Object> requestMap = new HashMap<>();
    Map<String, Object> result = new HashMap<>();
    if (filesMap != null) {
      result = service.bulkUpload(params);
      if ((Boolean) result.get("status")) {
        requestMap.put("docId", result.get("docId"));
        response.setStatus(HttpStatus.OK.value());
      } else {
        requestMap.put("status", params.get("error"));
        response.setStatus(HttpStatus.BAD_REQUEST.value());
      }
    } else {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Update.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @param redirect
   *          the redirect
   * @return the model and view
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   * @throws SQLException
   *           the SQL exception
   */
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  public ModelAndView update(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes redirect) throws IOException, ParseException, SQLException {

    Map<String, Object[]> params = new HashMap<String, Object[]>(request.getParameterMap());
    MultipartFile file = super.getFiles(request).get("fileName");
    params.put("fileName", new Object[] { file });
    Map<String, Object> requestMap = new HashMap<>();
    boolean result = false;
    if (file != null && file.getSize() > MAXIMUM_SIZE) {
      throw new ValidationException("exception.maximum.size.violation");
    } else if (file != null) {
      result = service.update(params);
      if (result) {
        response.setStatus(HttpStatus.OK.value());
      } else {
        requestMap.put("status", params.get("error"));
        response.setStatus(HttpStatus.BAD_REQUEST.value());
      }
    } else {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }

    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Show.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the model and view
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = "/show", method = RequestMethod.GET)
  public ModelAndView show(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    Map<String, String[]> params = new HashMap<>(request.getParameterMap());
    Map<String, Object> requestMap = new HashMap<>();
    service.show(params, requestMap);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Delete documents.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the model and view
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = "/delete", method = RequestMethod.GET)
  public ModelAndView deleteDocuments(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    Map<String, String[]> params = new HashMap<>(request.getParameterMap());
    Map<String, Object> requestMap = new HashMap<>();
    service.deleteDocuments(params, requestMap);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Finalize documents.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the model and view
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = "/finalize", method = RequestMethod.GET)
  public ModelAndView finalizeDocuments(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    Map<String, String[]> params = new HashMap<>(request.getParameterMap());
    Map<String, Object> requestMap = new HashMap<>();
    service.finalizeDocuments(params, requestMap);
    return new ModelAndView().addAllObjects(requestMap);
  }
}
