package com.insta.hms.core.insurance;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class EditInsuranceController.
 */

@Controller
@RequestMapping(URLRoute.EDIT_INSURANCE_URL)
public class EditInsuranceController extends BaseController {

  /** The Edit Insurance Service. */

  @LazyAutowired
  private EditInsuranceService editInsuranceService;

  /**
   * Show visit insurance details.
   *
   * @param request  the request
   * @param response the response
   * @param redirect the redirect
   * @return the model and view
   */
  @RequestMapping(value = "/showInsuranceDetails", method = RequestMethod.GET)
  public ModelAndView showVisitInsuranceDetails(HttpServletRequest request,
      HttpServletResponse response, RedirectAttributes redirect) {

    ModelAndView mav = new ModelAndView();
    Map<String, String[]> params = request.getParameterMap();

    editInsuranceService.setInsuranceDetails(params, mav);

    mav.setViewName(URLRoute.EDIT_INSURANCE_PAGE);
    return mav;
  }

  /**
   * Update visit insurance details.
   *
   * @param request  the request
   * @param response the response
   * @param redirect the redirect
   * @return the model and view
   */
  @RequestMapping(value = "/updateInsuranceDetails", method = RequestMethod.POST)
  public ModelAndView updateVisitInsuranceDetails(HttpServletRequest request,
      HttpServletResponse response, RedirectAttributes redirect) {

    Map<String, String[]> params = request.getParameterMap();
    String visitId = params.get("visitId")[0];
    String billNo = params.get("billNo")[0];
    String isNewUX = params.get("isNewUX")[0];
    redirect.addAttribute("visitId", visitId);
    redirect.addAttribute("billNo", billNo);
    redirect.addAttribute("isNewUX", isNewUX);

    Map<String, MultipartFile> fileParams = new HashMap<String, MultipartFile>();
    MultipartFile file = super.getFiles(request).get("primary_insurance_doc_content_bytea1");
    fileParams.put("primary_file_name", file);

    MultipartFile secFile = super.getFiles(request).get("secondary_insurance_doc_content_bytea1");
    fileParams.put("secondary_file_name", secFile);

    Map<String, Map> paramMap = new HashMap<String, Map>();

    paramMap.put("params", params);
    paramMap.put("file_params", fileParams);

    redirect.addAttribute("visitId", visitId);
    redirect.addAttribute("billNo", billNo);
    redirect.addAttribute("isNewUX", isNewUX);
    
    Map<String, Object> resultMap = editInsuranceService.updateInsuranceDetails(paramMap);
    editInsuranceService.scheduleAccountingForVisitBills(visitId);
    redirect.addFlashAttribute("info", resultMap.get("msg"));
    
    ModelAndView modelView = new ModelAndView();
    modelView.setViewName("redirect:showInsuranceDetails");
    return modelView;

  }
}
