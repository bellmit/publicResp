package com.insta.hms.mdm.formcomponents;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class FormComponentsController.
 *
 * @author teja
 */
@Controller
@RequestMapping(URLRoute.FORM_COMPONENTS_MASTER_PATH)
public class FormComponentsController extends BaseController {

  /** The form components service. */
  @LazyAutowired
  private FormComponentsService formComponentsService;

  /** The message util. */
  @LazyAutowired
  private MessageUtil messageUtil;

  /**
   * List.
   *
   * @param req the req
   * @param resp the resp
   * @return the model and view
   */
  @RequestMapping(value = { "/list", "" }, method = RequestMethod.GET)
  public ModelAndView list(HttpServletRequest req, HttpServletResponse resp) {
    ModelAndView modelView = new ModelAndView();
    modelView.setViewName(URLRoute.FORM_COMPONENTS_LIST);
    modelView.addObject("componentsList",
        ConversionUtils.listBeanToListMap(formComponentsService.getComponents()));
    modelView
        .addObject("prefs", formComponentsService.getAllPrefs().get("max_centers_inc_default"));
    return modelView;
  }

  /**
   * Show.
   *
   * @param req the req
   * @param resp the resp
   * @return the model and view
   */
  @RequestMapping(value = { "/show" }, method = RequestMethod.GET)
  public ModelAndView show(HttpServletRequest req, HttpServletResponse resp) {
    ModelAndView modelView = new ModelAndView();
    modelView.setViewName(URLRoute.FORM_COMPONENTS_SHOW);
    Map params = req.getParameterMap();
    modelView.addObject("bean", formComponentsService.getFormMap(params));
    modelView.addAllObjects(formComponentsService.getReferenceData(params));
    modelView
    .addObject("max_centers", formComponentsService.getAllPrefs().get("max_centers_inc_default"));
    return modelView;
  }

  /**
   * Adds the.
   *
   * @param req the req
   * @param resp the resp
   * @return the model and view
   */
  @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
  public ModelAndView add(HttpServletRequest req, HttpServletResponse resp) {
    ModelAndView modelView = new ModelAndView();
    Map params = req.getParameterMap();
    modelView.setViewName(URLRoute.FORM_COMPONENTS_ADD);
    modelView.addAllObjects(formComponentsService.getReferenceData(params));
    return modelView;
  }

  /**
   * Insert.
   *
   * @param req the req
   * @param resp the resp
   * @param redirectAttributes the redirect attributes
   * @return the model and view
   */
  @RequestMapping(value = { "/create" }, method = RequestMethod.POST)
  public ModelAndView insert(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes redirectAttributes) {
    Map params = req.getParameterMap();
    BasicDynaBean bean = formComponentsService.insertForm(params);
    redirectAttributes.addAttribute("id", bean.get("id"));
    redirectAttributes.addAttribute("form_type", bean.get("form_type"));
    String createdMessage = messageUtil.getMessage("flash.created.successfully", null);
    redirectAttributes.addFlashAttribute("info", createdMessage);
    ModelAndView modelView = new ModelAndView();
    modelView.setViewName("redirect:show");
    return modelView;
  }

  /**
   * Update.
   *
   * @param req the req
   * @param resp the resp
   * @param redirectAttributes the redirect attributes
   * @return the model and view
   */
  @RequestMapping(value = { "/update" }, method = RequestMethod.POST)
  public ModelAndView update(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes redirectAttributes) {
    ModelAndView modelView = new ModelAndView();
    modelView.setViewName("redirect:show");
    Map params = req.getParameterMap();
    BasicDynaBean bean = formComponentsService.updateForm(params);
    redirectAttributes.addAttribute("id", bean.get("id"));
    redirectAttributes.addAttribute("form_type", bean.get("form_type"));
    String createdMessage = messageUtil.getMessage("flash.updated.successfully", null);
    redirectAttributes.addFlashAttribute("info", createdMessage);
    return modelView;
  }

  /**
   * Show center.
   *
   * @param request the request
   * @param response the response
   * @return the model and view
   */
  @RequestMapping(value = "/showCenter", method = RequestMethod.GET)
  public ModelAndView showCenter(HttpServletRequest request, HttpServletResponse response) {

    ModelAndView modelView = new ModelAndView();
    Integer formId = Integer.parseInt(request.getParameter("id"));
    modelView.addObject("params", formComponentsService.showList(formId));
    modelView.setViewName(URLRoute.FORM_CENTER_SHOWFORM);
    return modelView;
  }

  /**
   * Update center.
   *
   * @param request the request
   * @param response the response
   * @param redirectAttributes the redirect attributes
   * @return the model and view
   */
  @SuppressWarnings("unused")
  @RequestMapping(value = "/updateCenter", method = RequestMethod.POST)
  public ModelAndView updateCenter(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes redirectAttributes) {
    formComponentsService.updateFormCenter(request.getParameterMap());
    ModelAndView modelView = new ModelAndView();
    redirectAttributes.addAttribute("id", request.getParameterMap().get("form_component_id")[0]);
    modelView.setViewName("redirect:showCenter");
    return modelView;
  }

}
