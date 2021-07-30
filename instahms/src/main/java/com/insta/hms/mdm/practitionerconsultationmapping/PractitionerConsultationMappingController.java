package com.insta.hms.mdm.practitionerconsultationmapping;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterRestController;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.doctors.DoctorService;

import com.insta.hms.mdm.practitionertypes.PractitionerTypeService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class PractitionerConsultationMappingController.
 */
@Controller
@RequestMapping(URLRoute.PRACTITIONER_CONSULTATION_MAPPING_PATH)
public class PractitionerConsultationMappingController extends MasterRestController {

  /** The service. */
  @LazyAutowired
  private PractitionerConsultationMappingService service;
  
  
  /** The consultation types service. */
  @LazyAutowired
  private ConsultationTypesService consultationTypesService;
  
  /** The doctor service. */
  @LazyAutowired
  private DoctorService doctorService;

  /** practitioner type service. */
  @LazyAutowired
  private PractitionerTypeService practitionerTypeService;

  /**
   * Instantiates a new practitioner consultation mapping controller.
   *
   * @param service the service
   */
  public PractitionerConsultationMappingController(PractitionerConsultationMappingService 
      service) {
    super(service);
  }
  
  
  /**
   * Creates the.
   *
   * @param req the req
   * @param resp the resp
   * @param requestBody the request body
   * @return the response entity
   */
  @Override
  @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = "application/json")
  protected ResponseEntity create(HttpServletRequest req, HttpServletResponse resp,
      @RequestBody ModelMap requestBody) {

    if (requestBody == null) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    int practitionerTypeId = service.insert(requestBody);
    requestBody.addAttribute("practitioner_type_id", practitionerTypeId);
    return new ResponseEntity<>(requestBody, HttpStatus.CREATED);
  }
  
  
  /**
   * Gets the consultation types.
   *
   * @param request the request
   * @param response the response
   * @return the consultation types
   */
  @RequestMapping(value = "/getconsultationtypes" , method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> getConsultationTypes(HttpServletRequest request,
      HttpServletResponse response) {

    String practitionerTypeIdStr = request.getParameter("practitioner_type_id");
    Map<String, Object> responseMap = new HashMap<>();
    if (null == practitionerTypeIdStr || "".equals(practitionerTypeIdStr)) {
      List<String> consultationTypesColumns = new ArrayList<>();
      consultationTypesColumns.add("consultation_type");
      consultationTypesColumns.add("consultation_type_id");
      responseMap.put("consultation_types", ConversionUtils.listBeanToListMap(
          consultationTypesService.getAllConsultationTypes(consultationTypesColumns)));
      return new ResponseEntity<>(responseMap, HttpStatus.OK);
    }
    Integer practitionerTypeId = Integer.parseInt(practitionerTypeIdStr);
    List<BasicDynaBean> consultationTypes = service.getConsultationTypes(practitionerTypeId, null);
    responseMap.put("consultation_types", ConversionUtils.listBeanToListMap(consultationTypes));
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }
  
  /**
   * Update.
   *
   * @param req the req
   * @param resp the resp
   * @param requestBody the request body
   * @return the response entity
   */
  @Override
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  protected ResponseEntity update(HttpServletRequest req, HttpServletResponse resp,
      @RequestBody ModelMap requestBody) {
   
    if (requestBody == null) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    service.update(requestBody);
    return new ResponseEntity<>(requestBody, HttpStatus.CREATED);
  }
  
  /**
   * Gets the doctor consultation types.
   *
   * @param request the request
   * @param response the response
   * @return the doctor consultation types
   */
  @RequestMapping(value = "/getdoctorconsultationtypes" , method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> getDoctorConsultationTypes(HttpServletRequest request,
      HttpServletResponse response) {

    String doctorId = request.getParameter("doctor_id");
    Map<String, Object> responseMap = null;
    if (null == doctorId || "".equals(doctorId)) {
      responseMap = new HashMap<>();
      return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
    }
    Map<String, String> filterMap = new HashMap<>();
    filterMap.put("doctor_id", doctorId);
    BasicDynaBean doctorBean = doctorService.findByPk(filterMap);
    if (null == doctorBean) {
      responseMap = new HashMap<>();
      return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
    }
    Integer practitionerTypeId = (Integer) doctorBean.get("practitioner_id");
    String apptCat = request.getParameter("appt_cat");
    responseMap = service.getDoctorConsultationTypes(practitionerTypeId,apptCat);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }
  
  /**
   * Gets the index page.
   *
   * @return the index page
   */
  @RequestMapping(value = URLRoute.MASTER_INDEX_URL, method = RequestMethod.GET)
  public ModelAndView getIndexPage() {
    return renderMasterUi("Master", "hospitalAdminMasters");
  }
  
  

}
