package com.insta.hms.extension.billing;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.extension.accounting.zoho.books.exception.BooksException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class AccountingController.
 */
@Controller
@RequestMapping("/billing/accounting")
public class AccountingController extends AccountingBaseController {

  /** The service. */
  @LazyAutowired
  AccountingService service;

  /** The message util. */
  @Autowired
  MessageUtil messageUtil;

  /**
   * Search.
   *
   * @param model
   *          the model
   * @param req
   *          the req
   * @param response
   *          the response
   * @return the model and view
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/search", "" }, method = RequestMethod.GET)
  public ModelAndView search(Model model, HttpServletRequest req, HttpServletResponse response) {
    Map<String, String[]> reqMap = req.getParameterMap();
    Map<String, Object> summary = new HashMap<String, Object>();
    Map result = new HashMap();
    if (null != reqMap && reqMap.size() > 0) {
      Map<String, String[]> paramMap = preprocessParams(reqMap);
      PagedList pagedList = service.search(paramMap);
      result = ConversionUtils.listBeanToMapListListBean(pagedList.getDtoList(), "voucher_date",
          "voucher_type");
      summary = service.getSearchSummary(paramMap);
    }
    List<Object> referenceData = getReferenceData("search", reqMap);
    AccountingModelAndView view = createView("search", new Object[] { summary, result },
        referenceData); // , summary, map);
    return view;
  }

  /**
   * Export.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @param redirect
   *          the redirect
   * @return the model and view
   * @throws Exception
   *           the exception
   */
  @RequestMapping(value = "/export", method = RequestMethod.POST)
  public ModelAndView export(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes redirect) throws Exception {
    Map<String, String[]> paramMap = preprocessParams(req.getParameterMap());
    List<BasicDynaBean> exportBeans = 
        service.export(paramMap, "zohobooks"); // TODO :device a way of getting zoho as a parameter
    // if (null != exportBeans && exportBeans.size() > 0) {
    setFlashMessage(redirect, exportBeans);
    // }
    AccountingModelAndView view = 
        createView("export", /* new Object[] {exportBeans} */ null, null); // ,summary,map);
    return view;
  }

  /**
   * Sets the flash message.
   *
   * @param redirect
   *          the redirect
   * @param exportBeans
   *          the export beans
   */
  // TODO : Move this into the model and view
  private void setFlashMessage(RedirectAttributes redirect, List<BasicDynaBean> exportBeans) {
    String exportMessage = "";
    String exportId = "";
    String exportStatus = "error";

    if (null != exportBeans && exportBeans.size() > 0) {

      Integer eid = (Integer) exportBeans.get(0).get("export_id");
      exportId = (null != eid) ? String.valueOf(eid) : null;
      String message = messageUtil.getMessage("billing.accountingexport.message.complete");
      String failureMessage = "";
      String successMessage = "";
      /* Get the voucher counts for successful and failed exports */

      Map resultMap = ConversionUtils.listBeanToMapBean(exportBeans, "status");

      BasicDynaBean failureBean = (BasicDynaBean) resultMap.get(new Integer(-1));
      if (null != failureBean && null != failureBean.get("journal_count")) {
        Object[] failureCount = new Object[] { failureBean.get("journal_count") };
        failureMessage = messageUtil.getMessage("billing.accountingexport.message.failed",
            failureCount);
      }
      BasicDynaBean successBean = (BasicDynaBean) resultMap.get(new Integer(1));
      if (null != successBean && null != successBean.get("journal_count")) {
        Object[] successCount = new Object[] { successBean.get("journal_count") };
        successMessage = messageUtil.getMessage("billing.accountingexport.message.success",
            successCount);
      }
      exportMessage = message + " " + successMessage + failureMessage;
      exportStatus = successMessage.isEmpty() ? "error"
          : failureMessage.isEmpty() ? "success" : "notify";
    }
    redirect.addFlashAttribute("export_id", exportId);
    redirect.addFlashAttribute("exportMessage", exportMessage);
    redirect.addFlashAttribute("exportStatus", exportStatus);
  }

  /**
   * Gets the reference data.
   *
   * @param action
   *          the action
   * @param reqMap
   *          the req map
   * @return the reference data
   */
  protected List<Object> getReferenceData(String action, Map<String, String[]> reqMap) {
    List<Object> referenceDataList = new ArrayList<Object>();
    Map<String, String> voucherTypeMap = service.getVoucherTypes();
    List<BasicDynaBean> accountGroups = service.getAccountGroups();
    List accountGroupMap = ConversionUtils.listBeanToListMap(accountGroups);
    referenceDataList.add(voucherTypeMap);
    referenceDataList.add(accountGroupMap);
    return referenceDataList;
  }

  /**
   * Handle error.
   *
   * @param request
   *          the request
   * @param redirect
   *          the redirect
   * @param ex
   *          the ex
   * @return the model and view
   */
  @ExceptionHandler(BooksException.class)
  public ModelAndView handleError(HttpServletRequest request, RedirectAttributes redirect,
      BooksException ex) {
    redirect.addFlashAttribute("export_id", 0);
    redirect.addFlashAttribute("exportMessage", "Export Failed :" + ex.getMessage());
    redirect.addFlashAttribute("exportStatus", "error");
    return createView("export", null, null);
  }
}
