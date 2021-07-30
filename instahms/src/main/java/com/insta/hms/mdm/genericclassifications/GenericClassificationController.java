package com.insta.hms.mdm.genericclassifications;

import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author irshadmohammed.
 *
 */
@Controller
@RequestMapping(URLRoute.GENERIC_CLASSIFICATION_PATH)
public class GenericClassificationController extends MasterController {

  GenericClassificationService genericClassificationService;

  @LazyAutowired
  private MessageUtil messageUtil;

  public GenericClassificationController(GenericClassificationService service) {
    super(service, MasterResponseRouter.GENERIC_CLASSIFICATION_ROUTER);
    genericClassificationService = service;
  }

  
  @Override
  public Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    Map<String, List<BasicDynaBean>> refGenericClassificationMap = 
        new HashMap<String, List<BasicDynaBean>>();
    refGenericClassificationMap.put("genericClassificationsLists", getService().lookup(false));
    return refGenericClassificationMap;
  }

  /**
   * Delete.
   *
   * @param req the req
   * @param resp the resp
   * @param attribs the attribs
   * @return the model and view
   */
  @RequestMapping(value = "/delete", method = RequestMethod.GET)
  public ModelAndView delete(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes attribs) {
    ModelAndView modelView = new ModelAndView();
    Map params = req.getParameterMap();
    String[] deleteIds = (String[]) params.get("del_classid");
    if (genericClassificationService.batchDelete(deleteIds, true)) {
      String deletedMessage = messageUtil.getMessage("flash.deleted.successfully", null);
      attribs.addFlashAttribute("info", deletedMessage);
    }
    modelView.setViewName(URLRoute.GENERIC_CLASSIFICATION_REDIRECT_TO_LIST);
    return modelView;
  }

}
