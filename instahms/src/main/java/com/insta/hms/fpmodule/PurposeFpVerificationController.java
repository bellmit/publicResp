package com.insta.hms.fpmodule;

import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller("purposeFpVerificationController")
@RequestMapping(URLRoute.FP_VERIFICATION_PURPOSE_MASTER_PATH)
public class PurposeFpVerificationController extends MasterController {

  @LazyAutowired
  MessageUtil messageUtil;

  public PurposeFpVerificationController(PurposeFpVerificationService service) {
    super(service, PurposeFpVerificationRouter.PURPOSEFP_VERIFICATION);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((PurposeFpVerificationService) getService()).getAddEditPageData(params);
  }

  @Override
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  protected ModelAndView create(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes attribs) {
    ModelAndView modelView = super.create(req, resp, attribs);
    attribs.addAttribute("purpose_id",
        ((PurposeFpVerificationService) getService()).getLatestPurposeId());
    return modelView;
  }
}
