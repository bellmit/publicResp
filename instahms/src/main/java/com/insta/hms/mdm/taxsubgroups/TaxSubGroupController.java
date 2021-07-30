package com.insta.hms.mdm.taxsubgroups;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The Class TaxSubGroupController.
 */
@Controller
@RequestMapping(URLRoute.ITEM_SUB_GROUP_PATH)
public class TaxSubGroupController extends MasterController {
  static Logger logger = LoggerFactory.getLogger(TaxSubGroupController.class);

  /** The tax sub group service. */
  @LazyAutowired
  private TaxSubGroupService taxSubGroupService;

  /**
   * Instantiates a new tax sub group controller.
   *
   * @param service
   *          the service
   */
  public TaxSubGroupController(TaxSubGroupService service) {
    super(service, MasterResponseRouter.ITEM_SUB_GROUP_ROUTER);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterController#create(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse,
   * org.springframework.web.servlet.mvc.support.RedirectAttributes)
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  @Override
  public ModelAndView create(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes redirect) throws ValidationException, ConversionException {
    Map<String, String[]> parameters = request.getParameterMap();
    String[] expression = parameters.get("tax_rate_expr");
    BasicDynaBean itemBean = null;
    MessageUtil messageutil = ApplicationContextProvider.getBean(MessageUtil.class);
    String createdMessage = messageutil.getMessage("flash.created.successfully", null);
    Boolean validExp = null;
    if (expression != null) {
      validExp = taxSubGroupService.isValidExpression(expression[0]);
    }

    if (validExp != null && !validExp) {
      createdMessage = messageutil
          .getMessage("exception.tax.sub.group.invalid.tax.expression", null);
      redirect.addFlashAttribute("error", createdMessage);
    } else { 
      itemBean = taxSubGroupService.insertItemSubGroup(parameters);
      Map itemMap = itemBean.getMap();
      redirect.mergeAttributes(itemMap);
      response.setStatus(HttpStatus.CREATED.value());
      redirect.addFlashAttribute("info", createdMessage);
    }
    
    return new ModelAndView(URLRoute.ITEM_SUB_GROUP_REDIRECT_TO_SHOW);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterController#show(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @RequestMapping(value = "/show", method = RequestMethod.GET)
  @Override
  public ModelAndView show(HttpServletRequest req, HttpServletResponse response) {

    ModelAndView mav = new ModelAndView();
    String itemSubGroupId = req.getParameter("item_subgroup_id");
    BasicDynaBean bean = taxSubGroupService.getItemSubGroup(itemSubGroupId);

    if (bean == null) {
      throw new EntityNotFoundException(new String[] { "Tax subgroup", "id", itemSubGroupId });
    }

    mav.addObject("subgroupHasMasterReferences", taxSubGroupService
        .taxSubGroupHasMasterReferences(Integer.parseInt(itemSubGroupId)));
    mav.addObject("bean", bean.getMap());
    mav.setViewName(URLRoute.ITEM_SUB_GROUP_SHOW);
    response.setStatus(HttpStatus.OK.value());
    return mav;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterController#update(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse,
   * org.springframework.web.servlet.mvc.support.RedirectAttributes)
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  @Override
  public ModelAndView update(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes redirect) {

    Map<String, String[]> parameters = request.getParameterMap();
    MessageUtil messageutil = ApplicationContextProvider.getBean(MessageUtil.class);
    
    String[] expression = parameters.get("tax_rate_expr");
    Boolean validExp = null;
    if (expression != null) {
      validExp = taxSubGroupService.isValidExpression(expression[0]);
    }

    String itemSubgroupId = parameters.get("item_subgroup_id")[0];
    BasicDynaBean itemSubGroupBean = null; // updated item subgroup

    if (validExp != null && !validExp) {
      redirect.addFlashAttribute("error", messageutil
          .getMessage("exception.tax.sub.group.invalid.tax.expression", null));
    } else { 

      int success = taxSubGroupService.updateItemSubGroup(parameters);
      if (success < 1) {
        redirect.addFlashAttribute("info", messageutil.getMessage("flash.update.failed", null));
        throw new EntityNotFoundException(new String[] { "Tax subgroup", "id", itemSubgroupId });
      } else {
        redirect.addFlashAttribute("info",
            messageutil.getMessage("flash.updated.successfully", null));
      }
    }
    itemSubGroupBean = taxSubGroupService.getItemSubGroup(itemSubgroupId);
    Map itemSubGroupMap = itemSubGroupBean.getMap();
    redirect.mergeAttributes(itemSubGroupMap);
    return new ModelAndView(URLRoute.ITEM_SUB_GROUP_REDIRECT_TO_SHOW);
  }

}
