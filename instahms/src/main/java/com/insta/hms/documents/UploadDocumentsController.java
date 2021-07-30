package com.insta.hms.documents;

import com.bob.hms.common.MimeTypeDetector;
import com.insta.hms.common.BaseController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.registration.PatientRegistrationService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.ValidationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class UploadDocumentsController.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@Controller
@RequestMapping("/uploaddocuments")
public class UploadDocumentsController extends BaseController {

  /** The document category factory. */
  @LazyAutowired
  private DocumentServiceFactory documentCategoryFactory;

  /** The patient registration service. */
  @LazyAutowired
  private PatientRegistrationService patientRegistrationService;

  /** The Constant MAXIMUM_SIZE. */
  private static final Long MAXIMUM_SIZE = 10L * 1024L * 1024L;

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
      DocumentsService service = documentCategoryFactory.getInstance(
          (String) params.get("category")[0], (String) params.get("specialized")[0]);
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
   * Gets the uploaded documents.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the uploaded documents
   * @throws ParseException
   *           the parse exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/getdoclist", method = RequestMethod.GET)
  public List<Map> getUploadedDocuments(HttpServletRequest request, 
      HttpServletResponse response)
      throws ParseException, IOException {
    List resultList = new ArrayList();
    DocumentsService service = documentCategoryFactory
        .getInstance((String) request
        .getParameterMap().get("category")[0], (String) request.getParameterMap()
        .get("specialized")[0]);
    resultList = service.getUploadedDocuments(request);
    return resultList;
  }

  /**
   * Gets the insurance document.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the insurance document
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = "/getdoccontent", method = RequestMethod.GET)
  public ResponseEntity<byte[]> getInsuranceDocument(HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    Integer docId = Integer.parseInt(request.getParameter("doc_id"));
    DocumentsService service = documentCategoryFactory.getInstance((String) request
        .getParameterMap().get("category")[0], (String) request.getParameterMap()
        .get("specialized")[0]);
    byte[] data = service.getDocumentContentByDocId(docId);

    String contentType = MimeTypeDetector.getMimeUtil().getMimeTypes(data).toString();
    response.addHeader("Content-Type", contentType);

    OutputStream responseStream = null;
    try {
      responseStream = response.getOutputStream();
      StreamUtils.copy(data, responseStream);
    } finally {
      if (null != responseStream) {
        responseStream.close();
      }
    }
    return null;
  }

  /**
   * Get document types by category.
   *
   * @param request
   *          request object
   * @param response
   *          response object
   * @return returns map
   * @throws ParseException
   *           the parse exception
   */
  @IgnoreConfidentialFilters
  @GetMapping(path = "/filterByCat")
  public Map<String, Object> getDocumentTypesByCategory(HttpServletRequest request,
      HttpServletResponse response) throws ParseException {
    Map<String, String[]> params = request.getParameterMap();
    String category = params.get("category")[0] != null
        || !((String) params.get("category")[0])
        .equals("") ? (String) params.get("category")[0]
        : null;
    Map<String, Object> returnMap = new HashMap();
    DocumentsService service = documentCategoryFactory.getInstance(category,
        (String) params.get("specialized")[0]);
    returnMap.put("documents", service.getDocumentTypesByCatAndEmrRules(category,
        (String) params.get("visitId")[0], 
        (String) params.get("specialized")[0], request));
    return returnMap;
  }

  /**
   * Get document types by category.
   *
   * @param request
   *          request object
   * @param response
   *          response object
   * @return returns map
   * @throws ParseException
   *           the parse exception
   */
  @IgnoreConfidentialFilters
  @GetMapping(path = "/fetchVisitsList")
  public Map<String, Object> getVisitsForMrNo(HttpServletRequest request,
      HttpServletResponse response) throws ParseException {
    Map<String, Object> responseMap = new HashMap();
    responseMap.put("visitList", patientRegistrationService.getVisitsForMrNo(request
        .getParameterMap().get("mr_no")[0], request.getParameterMap().get("visit_type")[0]));
    return responseMap;
  }

}
