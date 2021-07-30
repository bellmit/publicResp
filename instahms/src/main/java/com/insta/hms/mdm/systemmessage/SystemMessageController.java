package com.insta.hms.mdm.systemmessage;

import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class SystemMessageController.
 */
@Controller
@RequestMapping(URLRoute.SYSTEM_MESSAGE)
public class SystemMessageController extends MasterController {

  SystemMessageService systemMessageService;

  @LazyAutowired
  private MessageUtil messageUtil;

  /**
   * Instantiates a new system message controller.
   *
   * @param service the service
   */
  SystemMessageController(SystemMessageService service) {
    super(service, MasterResponseRouter.SYSTEM_MESSAGE_ROUTER);
    systemMessageService = service;
  }

  /**
   * Method invoked when search action is invoked on an entity.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the model and view
   */
  @Override
  @SuppressWarnings({"rawtypes"})
  @GetMapping(value = {"/list", ""})
  public ModelAndView list(HttpServletRequest req, HttpServletResponse resp) {

    ModelAndView modelView = new ModelAndView();
    Map paramMap = new HashMap();
    paramMap.put("system_type", new String[] {"User"});
    PagedList pagedList = systemMessageService.search(paramMap);
    modelView.addObject("pagedList", pagedList);
    addReferenceData(getFilterLookups(paramMap), modelView);
    modelView.setViewName(router.route("list"));
    return modelView;
  }

  /**
   * Details.
   *
   * @param request the request
   * @param response the response
   * @return the list
   */
  @GetMapping(value = "/details")
  public Map<String, Object> details(HttpServletRequest request, HttpServletResponse response) {
    return ((SystemMessageService) getService()).systemMessages("search");
  }

  /**
   * Delete.
   *
   * @param req the req
   * @param resp the resp
   * @param attribs the attribs
   * @return the model and view
   */
  @GetMapping(value = "/delete")
  public ModelAndView delete(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes attribs) {
    ModelAndView modelView = new ModelAndView();
    BasicDynaBean bean = systemMessageService.toBean(req.getParameterMap());
    Integer ret = systemMessageService.delete(bean, true);
    if (ret != 0) {
      String deletedMessage = messageUtil.getMessage("flash.deleted.successfully", null);
      attribs.addFlashAttribute("info", deletedMessage);
    }
    modelView.setViewName(URLRoute.SUPPLIER_CATEGORY_REDIRECT_TO_LIST);
    return modelView;
  }
}
