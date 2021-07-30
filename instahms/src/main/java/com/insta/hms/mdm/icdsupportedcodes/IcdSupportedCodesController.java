package com.insta.hms.mdm.icdsupportedcodes;

import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.vitals.VitalsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** The Class IcdSupportedCodesController. */
@Controller("icdSupportedCodesController")
@RequestMapping(URLRoute.ICD_SUPPORTED_CODES_MASTER_PATH)
public class IcdSupportedCodesController extends MasterController {

  /** The message util. */
  @LazyAutowired private MessageUtil messageUtil;

  /**
   * Instantiates a new icd supported codes controller.
   *
   * @param service the service
   */
  public IcdSupportedCodesController(IcdSupportedCodesService service) {
    super(service, MasterResponseRouter.ICD_SUPPORTED_CODES_ROUTER);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((IcdSupportedCodesService) getService()).getAddEditPageData();
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterController#delete(javax.servlet.http.HttpServletRequest,
   *  javax.servlet.http.HttpServletResponse, java.lang.Boolean,
   *  org.springframework.web.servlet.mvc.support.RedirectAttributes)
   */
  @Override
  @RequestMapping(value = "/delete", method = RequestMethod.POST)
  protected ModelAndView delete(
      HttpServletRequest request,
      HttpServletResponse response,
      Boolean hardDelete,
      RedirectAttributes attribs) {
    Map<String, String[]> params = request.getParameterMap();
    String[] deleteIds = params.get("delete_ids");
    Boolean ret = this.getService().batchDelete(deleteIds, true);
    if (ret) {
      String message = messageUtil.getMessage("flash.deleted.successfully", null);
      attribs.addFlashAttribute("info", message);
    }
    attribs.addAttribute("code_category", request.getParameter("c_category"));
    attribs.addAttribute("code_type", request.getParameter("c_type"));

    ModelAndView modelView = new ModelAndView();
    modelView.setViewName(router.route("delete"));
    return modelView;
  }
  
  /**
   * Gets the mrd supported codes.
   *
   * @return the mrd supported codes
   */
  @GetMapping(value = "/getMrdSupportedCodes")
  public Map<String, Object> getMrdSupportedCodes() {
    return ((IcdSupportedCodesService) getService()).getMrdSupportedCodes();
  }
}
