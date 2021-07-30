package com.insta.hms.mdm.opvisittyperuleapplicability;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.ReferenceDataConverter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class OpVisitTypeApplicabilityController.
 *
 * @author Allabakash
 */
@Controller
@RequestMapping(URLRoute.FOLLOWUP_RULES_APPLICABILITY)
public class OpVisitTypeApplicabilityController extends BaseController {

  /** The form components service. */
  @LazyAutowired
  private OpVisitTypeRuleApplicabilityService opVisitTypeRuleApplicabilityService;

  /** The message util. */
  @LazyAutowired
  private MessageUtil messageUtil;

  /** The converter. */
  @Autowired
  private ReferenceDataConverter converter;

  /**
   * List.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the model and view
   */
  @RequestMapping(value = { "/list", "" }, method = RequestMethod.GET)
  public ModelAndView list(HttpServletRequest req, HttpServletResponse resp) {
    ModelAndView modelView = new ModelAndView();
    modelView.setViewName(URLRoute.FOLLOWUP_RULES_APPLICABILITY_LIST);
    modelView
        .addAllObjects(converter.convert(opVisitTypeRuleApplicabilityService.getAllComponents()));
    return modelView;
  }

  /**
   * Insert.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @param redirectAttributes
   *          the redirect attributes
   * @return the model and view
   */
  @RequestMapping(value = { "/create" }, method = RequestMethod.POST)
  public ModelAndView insert(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes redirectAttributes) {
    Map<String, Object> params = ConversionUtils.flatten(req.getParameterMap());
    Map<String, String> postSave = opVisitTypeRuleApplicabilityService.saveApplicability(params);
    if (postSave.get("status").equals("success")) {
      String createdMessage = messageUtil.getMessage("flash.created.successfully", null);
      redirectAttributes.addFlashAttribute("info", createdMessage);
    } else {
      String createdMessage = messageUtil.getMessage("exception.insert.failed",
          new String[] { "applicability" });
      redirectAttributes.addFlashAttribute("info", createdMessage);
    }
    ModelAndView modelView = new ModelAndView();
    modelView.setViewName("redirect:list");
    return modelView;
  }

  /**
   * Delete.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @param redirectAttributes
   *          the redirect attributes
   * @return the model and view
   */
  @RequestMapping(value = { "/delete" }, method = RequestMethod.GET)
  public ModelAndView delete(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes redirectAttributes) {
    Map<String, Object> params = ConversionUtils.flatten(req.getParameterMap());
    Map<String, String> postDelete = opVisitTypeRuleApplicabilityService
        .deleteApplicability(params);
    if (postDelete.get("status").equals("success")) {
      String deletedFlashMsg = messageUtil.getMessage("flash.deleted.successfully", null);
      redirectAttributes.addFlashAttribute("info", deletedFlashMsg);
    } else {
      String createdMessage = messageUtil.getMessage("exception.insert.failed", null);
      redirectAttributes.addFlashAttribute("info", createdMessage);
    }
    ModelAndView modelView = new ModelAndView();
    modelView.setViewName("redirect:list");
    return modelView;
  }

}
