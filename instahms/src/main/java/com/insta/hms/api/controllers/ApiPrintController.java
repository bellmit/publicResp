package com.insta.hms.api.controllers;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.core.prints.PrintService;
import com.insta.hms.exception.ValidationException;
import com.lowagie.text.DocumentException;

import freemarker.template.TemplateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

@Controller
@RequestMapping("/api/print")
public class ApiPrintController extends BaseRestController {
  static Logger logger = LoggerFactory.getLogger(ApiPrintController.class);

  @LazyAutowired
  private PrintService printService;
  
  @LazyAutowired
  private DoctorConsultationService doctorConsultationService;

  /**
   * Get printer definitions.
   *
   * @param request  the request
   * @param response the response
   * @return the response entity
   */
  @RequestMapping(value = "/getPrinterDefinition", method = RequestMethod.GET)
  public Map<String, Object> getPrinters(HttpServletRequest request, HttpServletResponse response)
      throws SQLException {

    return printService.getPrinterDefinitions();
  }

  /**
   * Get print template names.
   *
   * @param request  the request
   * @param response the response
   * @return the response entity
   */
  @RequestMapping(value = "/getPrintTemplateName", method = RequestMethod.GET)
  public Map<String, String> getTemplates(HttpServletRequest request,
      HttpServletResponse response) {
    return printService.getTemplates();
  }

  /**
   * Get discharge summary template.
   *
   * @param request  the request
   * @param response the response
   * @return the response entity
   */
  @RequestMapping(value = "/getDischargeSummaryTemplate", method = RequestMethod.GET)
  public Map<String, Object> getAllTemplates(HttpServletRequest request,
      HttpServletResponse response) {
    return printService.getAllTemplates();
  }

  /**
   * Print Consultation.
   *
   * @param consultationId consultation id
   * @param templateName   template name
   * @param printerId      printer id
   * @param logoHeader     logo header
   * @param response       the response
   * @return the response entity
   */
  @RequestMapping(value = "/printConsultation", method = RequestMethod.GET)
  public ModelAndView printConsultation(
      @RequestParam(name = "consultation_id", required = true) Integer consultationId,
      @RequestParam(required = true) String templateName,
      @RequestParam(required = true) Integer printerId,
      @RequestParam(required = false) String logoHeader, HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException, DocumentException,
      TransformerException, TemplateException {
    Map<String, Object> requestMap = new HashMap<>();
    printService.printConsultation(consultationId, templateName, printerId, logoHeader, requestMap,
        response);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Print EMR Consultation.
   *
   * @param consultationId consultation id
   * @param templateName   template name
   * @param printerId      printer id
   * @param logoHeader     logo header
   * @param response       the response
   * @return the response entity
   */
  @RequestMapping(value = "/printEmrConsultation", method = RequestMethod.GET)
  public ModelAndView printEmrConsultation(
      @RequestParam(name = "consultation_id", required = true) Integer consultationId,
      @RequestParam(required = false) String templateName,
      @RequestParam(required = true) Integer printerId,
      @RequestParam(name = "request_handler_key", required = true) String requestHandlerKey,
      @RequestParam(required = false) String logoHeader, HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException, DocumentException,
      TransformerException, TemplateException {
    if (!doctorConsultationService.validateConsultationIdForLoggedInPatient(consultationId,
        requestHandlerKey)) {
      throw new ValidationException("exception.form.notvalid.sectionitemid");
    }
    Map<String, Object> requestMap = new HashMap<>();
    printService.printEmrConsultation(consultationId, templateName, printerId, logoHeader,
        requestMap, response);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Print Prescription Consultation.
   *
   * @param consultationId consultation id
   * @param templateName   template name
   * @param printerId      printer id
   * @param logoHeader     logo header
   * @param response       the response
   * @return the response entity
   */
  @RequestMapping(value = "/printPresConsultation", method = RequestMethod.GET)
  public ModelAndView printPresConsultation(
      @RequestParam(name = "consultation_id", required = true) Integer consultationId,
      @RequestParam(required = false) String templateName,
      @RequestParam(required = true) Integer printerId,
      @RequestParam(required = false) String logoHeader, HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException, DocumentException,
      TransformerException, TemplateException {
    Map<String, Object> requestMap = new HashMap<>();
    printService.printPresConsultation(consultationId, templateName, printerId, logoHeader,
        requestMap, response);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Print Triage.
   *
   * @param consultationId consultation id
   * @param printerId      printer id
   * @param logoHeader     logo header
   * @param response       the response
   * @return the response entity
   */
  @RequestMapping(value = "/printTriage", method = RequestMethod.GET)
  public ModelAndView printTriage(@RequestParam(required = true) Integer consultationId,
      @RequestParam(required = false) Integer printerId,
      @RequestParam(name = "request_handler_key", required = true) String requestHandlerKey,
      @RequestParam(required = false) String logoHeader, HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException, DocumentException,
      TransformerException, TemplateException {
    if (!doctorConsultationService.validateConsultationIdForLoggedInPatient(consultationId,
        requestHandlerKey)) {
      throw new ValidationException("exception.form.notvalid.sectionitemid");
    }
    Map<String, Object> requestMap = new HashMap<>();
    printService.getTriagePrint(consultationId, printerId, logoHeader, requestMap, response);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Print Clinical Information.
   *
   * @param consultationId consultation id
   * @param printerId      printer id
   * @param logoHeader     logo header
   * @param response       the response
   * @return the response entity
   */
  @RequestMapping(value = "/printClinicalInfo", method = RequestMethod.GET)
  public ModelAndView printClinicalInfo(@RequestParam(required = true) Integer consultationId,
      @RequestParam(required = false) Integer printerId,
      @RequestParam(required = false) String logoHeader, HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException, DocumentException,
      TransformerException, TemplateException {
    Map<String, Object> requestMap = new HashMap<>();
    printService.printClinicalInfo(consultationId, printerId, logoHeader, requestMap, response);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Print IP EMR.
   *
   * @param patientId  visit id
   * @param printerId  printer id
   * @param logoHeader logo header
   * @param response   the response
   * @return the response entity
   */
  @RequestMapping(value = "/printIpEmr", method = RequestMethod.GET)
  public ModelAndView printIpEmr(@RequestParam(required = true) String patientId,
      @RequestParam(required = false) Integer printerId,
      @RequestParam(required = false) String logoHeader, HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException, DocumentException,
      TransformerException, TemplateException {
    Map<String, Object> requestMap = new HashMap<>();
    printService.printIpEmr(patientId, printerId, logoHeader, requestMap, response);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Print Patient Notes.
   *
   * @param patientId  visit id
   * @param printerId  printer id
   * @param logoHeader logo header
   * @param response   the response
   * @return the response entity
   */
  @RequestMapping(value = "/printPatientNotes", method = RequestMethod.GET)
  public ModelAndView printPatientNotes(
      @RequestParam(required = true) String patientId,
      @RequestParam(required = false) Integer printerId,
      @RequestParam(required = false) String logoHeader,
      @RequestParam(required = false) String noteTypeId,
      @RequestParam(required = false) String hospitalRoleId,
      HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException, DocumentException,
      TransformerException, TemplateException {
    Map<String, Object> requestMap = new HashMap<>();
    printService.printPatientNotes(patientId, printerId, logoHeader,
        requestMap, noteTypeId, hospitalRoleId, response);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Print physician orders.
   *
   * @param patientId  visit id
   * @param printerId  printer id
   * @param logoHeader logo header
   * @param response   the response
   * @return the response entity
   */
  @RequestMapping(value = "/printPhysicianOrders", method = RequestMethod.GET)
  public ModelAndView printIpPhysicianOrders(@RequestParam(required = true) String patientId,
      @RequestParam(required = false) Integer printerId,
      @RequestParam(required = false) String logoHeader, HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException, DocumentException,
      TransformerException, TemplateException {
    Map<String, Object> requestMap = new HashMap<>();
    printService.printPhysicianOrders(patientId, printerId, logoHeader, requestMap, response);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Print vitals chart.
   *
   * @param patientId  visit id
   * @param printerId  printer id
   * @param logoHeader logo header
   * @param response   the response
   * @return the response entity
   */
  @RequestMapping(value = "/printVitalsChart", method = RequestMethod.GET)
  public ModelAndView printVitalsChart(@RequestParam(required = true) String patientId,
      @RequestParam(required = false) Integer printerId,
      @RequestParam(required = false) String logoHeader, HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException, DocumentException,
      TransformerException, TemplateException {
    Map<String, Object> requestMap = new HashMap<>();
    printService.printVitalsChart(patientId, printerId, logoHeader, requestMap, response);
    return new ModelAndView().addAllObjects(requestMap);
  }

  /**
   * Get all OP/OSP visits.
   *
   * @param mrNo mr no
   * @return the response entity
   */
  @RequestMapping(value = "/patient/{mrNo}/visits", method = RequestMethod.GET)
  public Map<String, Object> getVisits(@PathVariable("mrNo") String mrNo) {
    Map<String, Object> visits = new HashMap<>();
    // getting op all visits active/inactive.
    visits.put("visits", printService.getPatientVisits(mrNo, "o", false));
    return visits;
  }

  /**
   * Get all consultations.
   *
   * @param mrNo mr no
   * @return the response entity
   */
  @RequestMapping(value = "/patient/{mrNo}/list", method = RequestMethod.GET)
  public Map<String, Object> getConsultationList(@PathVariable("mrNo") String mrNo) {
    return printService.getConsultationList(mrNo);
  }

  /**
   * Get all visits.
   *
   * @param mrNo mr no
   * @return the response entity
   */
  @RequestMapping(value = "/patient/{mrNo}/allVisits", method = RequestMethod.GET)
  public Map<String, Object> getAllIpVisits(@PathVariable("mrNo") String mrNo) {
    Map<String, Object> visits = new HashMap<>();
    // getting all visits active/inactive.
    visits.put("visits", printService.getAllVisits(mrNo, false));
    return visits;
  }

}
