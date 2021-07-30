package com.insta.hms.mdm.sequences.hospitalidpatterns;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class HospitalIdPatternsController.
 */
@Controller
@RequestMapping(URLRoute.HOSPITAL_ID_PATTERNS_PATH)
public class HospitalIdPatternsController extends MasterController {
  
  /** The service. */
  HospitalIdPatternsService service;

  /**
   * Instantiates a new hospital id patterns controller.
   *
   * @param service the service
   */
  public HospitalIdPatternsController(HospitalIdPatternsService service) {
    super(service, MasterResponseRouter.HOSPITAL_ID_PATTERNS_ROUTER);
    this.service = service;
  }

  /**
   * Gets the hospital id pattern detail.
   *
   * @param request the request
   * @param response the response
   * @return the hospital id pattern detail
   */
  @RequestMapping(value = "/hospidpattern", method = RequestMethod.GET)
  public ModelAndView getHospitalIdPatternDetail(
      HttpServletRequest request, HttpServletResponse response) {
    String patternId = request.getParameter("pattern_id");
    BasicDynaBean bean = service.getHospitalIdPatternDetails(patternId);
    ModelAndView modelView = new ModelAndView();
    modelView.addObject("pattern_details", bean.getMap());
    return modelView;
  }
}
