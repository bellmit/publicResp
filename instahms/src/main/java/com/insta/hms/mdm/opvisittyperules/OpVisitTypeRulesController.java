package com.insta.hms.mdm.opvisittyperules;

import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterDetailsController;
import com.insta.hms.mdm.MasterResponseRouter;

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

/**
 * The Class OpVisitTypeRulesController.
 */
@Controller
@RequestMapping(URLRoute.OP_VISIT_TYPE_RULES)
public class OpVisitTypeRulesController extends MasterDetailsController {

  @LazyAutowired
  private MessageUtil messageUtil;

  @LazyAutowired
  private OpVisitTypeRulesService opVisitTypeRulesService;

  @LazyAutowired
  private OpVisitTypeRuleDetailsService opVisitTypeRuleDetailsService;

  /**
   * Instantiates a new op visit type rules controller.
   *
   * @param service
   *          the service
   */
  public OpVisitTypeRulesController(OpVisitTypeRulesService service) {
    super(service, MasterResponseRouter.OP_VISIT_TYPE_RULES_ROUTER);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((OpVisitTypeRulesService) getService()).getAddShowPageData(params);
  }

  /**
   * Performs a delete.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @param hardDelete
   *          the hard delete
   * @return the model and view
   */
  @RequestMapping(value = "/delete", method = RequestMethod.POST)
  protected ModelAndView delete(HttpServletRequest request, HttpServletResponse response,
      Boolean hardDelete, RedirectAttributes attribs) {
    ModelAndView modelView = new ModelAndView();
    Map<String, String[]> params = request.getParameterMap();
    String[] deleteIds = params.get("rule_details_id");
    Boolean ret = false;
    if (deleteIds != null && deleteIds.length > 0) {
      ret = opVisitTypeRuleDetailsService.batchDelete(deleteIds, true);
    } else {
      ret = true;
    }
    BasicDynaBean bean = mapToBean(params);
    if (Boolean.TRUE.equals(ret) && opVisitTypeRulesService.delete(bean) > 0) {
      String deletedMessage = messageUtil.getMessage("flash.deleted.successfully", null);
      attribs.addFlashAttribute("info", deletedMessage);
    }
    modelView.setViewName(router.route("delete"));
    return modelView;
  }

}
