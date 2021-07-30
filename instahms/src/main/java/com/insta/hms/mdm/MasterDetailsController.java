package com.insta.hms.mdm;

import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MasterDetailsController extends MasterController {

  @LazyAutowired
  private MessageUtil messageUtil;

  public MasterDetailsController(MasterService service, ResponseRouter router) {
    super(service, router);
  }

  @Override
  @RequestMapping(value = "/show", method = RequestMethod.GET)
  public ModelAndView show(HttpServletRequest req, HttpServletResponse resp) {
    Map<String, List<Map>> detailBeanMap = new HashMap<String, List<Map>>();
    ModelAndView mav = super.show(req, resp);
    detailBeanMap
        .putAll(((MasterDetailsService) getService()).findDetailsByPk(req.getParameterMap()));
    mav.addAllObjects(detailBeanMap);
    return mav;
  }

  @SuppressWarnings("unchecked")
  @Override
  /**
   * Method called when add / edit action is invoked on an entity. Implements the most common use
   * case of not involving Action Forms
   * 
   * @param m
   * @return
   * @param req
   */
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  protected ModelAndView create(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes attribs) {
    ModelAndView mav = new ModelAndView();
    BasicDynaBean parentBean = getService().toBean(req.getParameterMap());
    Map<String, List<BasicDynaBean>> detailListMapBean = getService()
        .toBeanList(req.getParameterMap(), parentBean);
    int res = ((MasterDetailsService) getService()).insertDetails(parentBean, detailListMapBean);
    if (res > 0) {
      attribs.mergeAttributes(parentBean.getMap());
      String createdMessage = messageUtil.getMessage("flash.created.successfully", null);
      attribs.addFlashAttribute("info", createdMessage);
    }
    mav.setViewName(router.route("create"));
    return mav;
  }

  @SuppressWarnings("unchecked")
  @Override
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  protected ModelAndView update(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes attribs) {
    ModelAndView mav = new ModelAndView();
    BasicDynaBean parentBean = getService().toBean(req.getParameterMap());
    Map<String, List<BasicDynaBean>> detailListMapBean = ((MasterDetailsService) getService())
        .toBeanList(req.getParameterMap(), parentBean);
    int[] res = ((MasterDetailsService) getService()).updateDetails(parentBean, detailListMapBean);
    attribs.mergeAttributes(parentBean.getMap());
    String updatedMessage = messageUtil.getMessage("flash.updated.successfully", null);
    attribs.addFlashAttribute("info", updatedMessage);
    /*
     * res is null so if block us unused if(res != null && res.length >=1 ) {
     * //attribs.mergeAttributes(parentBean.getMap()); }else { // Flush message failed to insert }
     */
    mav.setViewName(router.route("update"));
    return mav;
  }

}
