package com.insta.hms.core.prints;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.lowagie.text.DocumentException;

import freemarker.template.TemplateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * The Class PrintController.
 *
 * @author sonam
 */
@RestController
@RequestMapping(URLRoute.PRINT_URL)
public class PrintController extends BaseRestController {

  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(PrintController.class);

  /** The print service. */
  @LazyAutowired
  private PrintService printService;

  /**
   * Gets the printers.
   *
   * @param request the request
   * @param mmap the mmap
   * @param response the response
   * @return the printers
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/getPrinterDefinition", method = RequestMethod.GET)
  public Map<String, Object> getPrinters(HttpServletRequest request,
      ModelMap mmap, HttpServletResponse response) throws SQLException {

    return printService.getPrinterDefinitions();
  }

  /**
   * Gets the templates.
   *
   * @param request
   *          the request
   * @param mmap
   *          the mmap
   * @param response
   *          the response
   * @return the templates
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/getPrintTemplateName", method = RequestMethod.GET)
  public Map<String, String> getTemplates(HttpServletRequest request,
      ModelMap mmap, HttpServletResponse response) {
    return printService.getTemplates();
  }

  /**
   * Gets the all templates.
   *
   * @param request
   *          the request
   * @param mmap
   *          the mmap
   * @param response
   *          the response
   * @return the all templates
   */
  @RequestMapping(value = "/getDischargeSummaryTemplate", method = RequestMethod.GET)
  public Map<String, Object> getAllTemplates(HttpServletRequest request,
      ModelMap mmap, HttpServletResponse response) {
    return printService.getAllTemplates();
  }

  /**
   * Prints the consultation.
   *
   * @param consultationId
   *          the consultation id
   * @param templateName
   *          the template name
   * @param printerId
   *          the printer id
   * @param logoHeader
   *          the logo header
   * @param response
   *          the response
   * @return the model and view
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws XPathExpressionException
   *           the x path expression exception
   * @throws DocumentException
   *           the document exception
   * @throws TransformerException
   *           the transformer exception
   * @throws TemplateException
   *           the template exception
   */
  @RequestMapping(value = "/printConsultation", method = RequestMethod.GET)
  public ModelAndView printConsultation(
      @RequestParam(value = "consultation_id", required = true) Integer consultationId,
      @RequestParam(required = true) String templateName,
      @RequestParam(required = true) Integer printerId,
      @RequestParam(required = false) String logoHeader,
      HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException,
      DocumentException, TransformerException, TemplateException {
    Map<String, Object> requestMap = new HashMap<>();
    printService.printConsultation(consultationId, templateName, printerId,
        logoHeader, requestMap, response);
    return new ModelAndView().addAllObjects(requestMap);
  }
  
  /**
   * Prints the discharge medication.
   *
   * @param patientId
   *          the patientId for OP -> consultationId and IP -> PatientId
   * @param templateName
   *          the template name
   * @param printerId
   *          the printer id
   * @param logoHeader
   *          the logo header
   * @param response
   *          the response
   * @param consultationType
   *            whether IP/OP
   * @return the model and view
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws XPathExpressionException
   *           the x path expression exception
   * @throws DocumentException
   *           the document exception
   * @throws TransformerException
   *           the transformer exception
   * @throws TemplateException
   *           the template exception
   */
  @GetMapping(value = "/printDischargeMedication")
  public ModelAndView printDischargeMedication(
      @RequestParam(value = "patientId", required = true) String patientId,
      @RequestParam(required = false) String templateName,
      @RequestParam(required = true) Integer printerId,
      @RequestParam(required = false) String logoHeader, 
      @RequestParam(required = true) String consultationType, HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException, DocumentException,
      TransformerException, TemplateException {
    Map<String, Object> requestMap = new HashMap<>();
    printService.printDischargeMedication(patientId, templateName, printerId, logoHeader,
        requestMap, response, consultationType);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Prints the emr consultation.
   *
   * @param consultationId the consultation id
   * @param templateName the template name
   * @param printerId the printer id
   * @param logoHeader the logo header
   * @param response the response
   * @return the model and view
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws DocumentException the document exception
   * @throws TransformerException the transformer exception
   * @throws TemplateException the template exception
   */
  @RequestMapping(value = "/printEmrConsultation", method = RequestMethod.GET)
  public ModelAndView printEmrConsultation(
      @RequestParam(value = "consultation_id", required = true) Integer consultationId,
      @RequestParam(required = false) String templateName,
      @RequestParam(required = true) Integer printerId,
      @RequestParam(required = false) String logoHeader,
      HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException,
      DocumentException, TransformerException, TemplateException {
    Map<String, Object> requestMap = new HashMap<>();
    printService.printEmrConsultation(consultationId, templateName, printerId,
        logoHeader, requestMap, response);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Prints the pres consultation.
   *
   * @param consultationId the consultation id
   * @param templateName the template name
   * @param printerId the printer id
   * @param logoHeader the logo header
   * @param response the response
   * @return the model and view
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws DocumentException the document exception
   * @throws TransformerException the transformer exception
   * @throws TemplateException the template exception
   */
  @RequestMapping(value = "/printPresConsultation", method = RequestMethod.GET)
  public ModelAndView printPresConsultation(
      @RequestParam(value = "consultation_id", required = true) Integer consultationId,
      @RequestParam(required = false) String templateName,
      @RequestParam(required = false) Integer printerId,
      @RequestParam(required = false) String logoHeader,
      HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException,
      DocumentException, TransformerException, TemplateException {
    Map<String, Object> requestMap = new HashMap<>();
    printService.printPresConsultation(consultationId, templateName, printerId,
        logoHeader, requestMap, response);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Prints the triage.
   *
   * @param consultationId
   *          the consultation id
   * @param printerId
   *          the printer id
   * @param logoHeader
   *          the logo header
   * @param response
   *          the response
   * @return the model and view
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws XPathExpressionException
   *           the x path expression exception
   * @throws DocumentException
   *           the document exception
   * @throws TransformerException
   *           the transformer exception
   * @throws TemplateException
   *           the template exception
   */
  @RequestMapping(value = "/printTriage", method = RequestMethod.GET)
  public ModelAndView printTriage(
      @RequestParam(required = true) Integer consultationId,
      @RequestParam(required = false) Integer printerId,
      @RequestParam(required = false) String logoHeader,
      HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException,
      DocumentException, TransformerException, TemplateException {
    Map<String, Object> requestMap = new HashMap<>();
    printService.getTriagePrint(consultationId, printerId, logoHeader,
        requestMap, response);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Prints the clinical info.
   *
   * @param consultationId
   *          the consultation id
   * @param printerId
   *          the printer id
   * @param logoHeader
   *          the logo header
   * @param response
   *          the response
   * @return the model and view
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws XPathExpressionException
   *           the x path expression exception
   * @throws DocumentException
   *           the document exception
   * @throws TransformerException
   *           the transformer exception
   * @throws TemplateException
   *           the template exception
   */
  @RequestMapping(value = "/printClinicalInfo", method = RequestMethod.GET)
  public ModelAndView printClinicalInfo(
      @RequestParam(required = true) Integer consultationId,
      @RequestParam(required = false) Integer printerId,
      @RequestParam(required = false) String logoHeader,
      HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException,
      DocumentException, TransformerException, TemplateException {
    Map<String, Object> requestMap = new HashMap<>();
    printService.printClinicalInfo(consultationId, printerId, logoHeader,
        requestMap, response);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Prints the ip emr.
   *
   * @param patientId
   *          the patient id
   * @param printerId
   *          the printer id
   * @param logoHeader
   *          the logo header
   * @param response
   *          the response
   * @return the model and view
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws XPathExpressionException
   *           the x path expression exception
   * @throws DocumentException
   *           the document exception
   * @throws TransformerException
   *           the transformer exception
   * @throws TemplateException
   *           the template exception
   */
  @RequestMapping(value = "/printIpEmr", method = RequestMethod.GET)
  public ModelAndView printIpEmr(
      @RequestParam(required = true) String patientId,
      @RequestParam(required = false) Integer printerId,
      @RequestParam(required = false) String logoHeader,
      HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException,
      DocumentException, TransformerException, TemplateException {
    Map<String, Object> requestMap = new HashMap<>();
    printService.printIpEmr(patientId, printerId, logoHeader, requestMap,
        response);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Prints the patient notes.
   *
   * @param patientId the patient id
   * @param printerId the printer id
   * @param logoHeader the logo header
   * @param response the response
   * @return the model and view
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws DocumentException the document exception
   * @throws TransformerException the transformer exception
   * @throws TemplateException the template exception
   */
  @RequestMapping(value = "/printPatientNotes", method = RequestMethod.GET)
  public ModelAndView printPatientNotes(
      @RequestParam(required = true) String patientId,
      @RequestParam(required = false) Integer printerId,
      @RequestParam(required = false) String logoHeader,
      @RequestParam(required = false) String noteTypeId,
      @RequestParam(required = false) String hospitalRoleId,
      HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException,
      DocumentException, TransformerException, TemplateException {
    Map<String, Object> requestMap = new HashMap<>();
    printService.printPatientNotes(patientId, printerId, logoHeader, requestMap,
        noteTypeId, hospitalRoleId, response);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Prints the ip physician orders.
   *
   * @param patientId the patient id
   * @param printerId the printer id
   * @param logoHeader the logo header
   * @param response the response
   * @return the model and view
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws DocumentException the document exception
   * @throws TransformerException the transformer exception
   * @throws TemplateException the template exception
   */
  @RequestMapping(value = "/printPhysicianOrders", method = RequestMethod.GET)
  public ModelAndView printIpPhysicianOrders(
      @RequestParam(required = true) String patientId,
      @RequestParam(required = false) Integer printerId,
      @RequestParam(required = false) String logoHeader,
      HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException,
      DocumentException, TransformerException, TemplateException {
    Map<String, Object> requestMap = new HashMap<>();
    printService.printPhysicianOrders(patientId, printerId, logoHeader,
        requestMap, response);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Prints the vitals chart.
   *
   * @param patientId the patient id
   * @param printerId the printer id
   * @param logoHeader the logo header
   * @param response the response
   * @return the model and view
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws DocumentException the document exception
   * @throws TransformerException the transformer exception
   * @throws TemplateException the template exception
   */
  @RequestMapping(value = "/printVitalsChart", method = RequestMethod.GET)
  public ModelAndView printVitalsChart(
      @RequestParam(required = true) String patientId,
      @RequestParam(required = false) Integer printerId,
      @RequestParam(required = false) String logoHeader,
      HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException,
      DocumentException, TransformerException, TemplateException {
    Map<String, Object> requestMap = new HashMap<>();
    printService.printVitalsChart(patientId, printerId, logoHeader, requestMap,
        response);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Gets the visits.
   *
   * @param mrNo
   *          the mr no
   * @return the visits
   */
  @RequestMapping(value = "/patient/{mrNo}/visits", method = RequestMethod.GET)
  public Map<String, Object> getVisits(@PathVariable("mrNo") String mrNo) {
    Map<String, Object> visits = new HashMap<>();
    // getting op all visits active/inactive.
    visits.put("visits", printService.getPatientVisits(mrNo, "o", false));
    return visits;
  }

  /**
   * Gets the consultation list.
   *
   * @param mrNo
   *          the mr no
   * @return the consultation list
   */
  @RequestMapping(value = "/patient/{mrNo}/list", method = RequestMethod.GET)
  public Map<String, Object> getConsultationList(
      @PathVariable("mrNo") String mrNo) {
    return printService.getConsultationList(mrNo);
  }

  /**
   * Gets the all ip visits.
   *
   * @param mrNo
   *          the mr no
   * @return the all ip visits
   */
  @RequestMapping(value = "/patient/{mrNo}/allVisits", method = RequestMethod.GET)
  public Map<String, Object> getAllIpVisits(@PathVariable("mrNo") String mrNo) {
    Map<String, Object> visits = new HashMap<>();
    // getting all visits active/inactive.
    visits.put("visits", printService.getAllVisits(mrNo, false));
    return visits;
  }

}
