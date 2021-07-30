package com.insta.hms.mdm.suppliercategories;

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
 * The Class SupplierCategoryController.
 *
 * @author irshad
 */
@Controller
@RequestMapping(URLRoute.SUPPLIER_CATEGORY_PATH)
public class SupplierCategoryController extends MasterController {

  /** The supplier category service. */
  SupplierCategoryService supplierCategoryService;

  /** The message util. */
  @LazyAutowired
  private MessageUtil messageUtil;

  /**
   * Instantiates a new supplier category controller.
   *
   * @param service
   *          the service
   */
  public SupplierCategoryController(SupplierCategoryService service) {
    super(service, MasterResponseRouter.SUPPLIER_CATEGORY_ROUTER);
    supplierCategoryService = service;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @Override
  public Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    Map<String, List<BasicDynaBean>> refSupplierCategoryMap = new HashMap<>();
    refSupplierCategoryMap.put("supplierCatList", getService().lookup(false));
    return refSupplierCategoryMap;
  }

  /**
   * Delete.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @param attribs
   *          the attribs
   * @return the model and view
   */
  @RequestMapping(value = "/delete", method = RequestMethod.GET)
  public ModelAndView delete(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes attribs) {
    ModelAndView modelView = new ModelAndView();
    BasicDynaBean bean = supplierCategoryService.toBean(req.getParameterMap());
    Integer ret = supplierCategoryService.delete(bean, true);
    if (ret != 0) {
      String deletedMessage = messageUtil.getMessage("flash.deleted.successfully", null);
      attribs.addFlashAttribute("info", deletedMessage);
    }
    modelView.setViewName(URLRoute.SUPPLIER_CATEGORY_REDIRECT_TO_LIST);
    return modelView;
  }
}
