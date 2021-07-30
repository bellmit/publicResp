package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.PlanMaster.PlanDetailsDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class InsuranceItemCategoryMasterAction.
 */
public class InsuranceItemCategoryMasterAction extends DispatchAction {

  /**
   * List.
   *
   * @param mapping    the m
   * @param actionForm the f
   * @param req        the req
   * @param resp       the resp
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  public ActionForward list(ActionMapping mapping, ActionForm actionForm, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {

    InsuranceItemCategoryMasterDAO dao = new InsuranceItemCategoryMasterDAO();
    Map map = req.getParameterMap();
    PagedList pagedList = dao.search(map,
        ConversionUtils.getListingParameter(req.getParameterMap()), "insurance_category_id");
    req.setAttribute("pagedList", pagedList);
    return mapping.findForward("list");
  }

  /**
   * Adds the.
   *
   * @param mapping    the m
   * @param actionForm the f
   * @param req        the req
   * @param resp       the resp
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  public ActionForward add(ActionMapping mapping, ActionForm actionForm, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {
    InsuranceItemCategoryMasterDAO dao = new InsuranceItemCategoryMasterDAO();
    req.setAttribute("existingPriority", dao.getColumnList("priority"));
    return mapping.findForward("addshow");
  }

  /**
   * Creates the.
   *
   * @param actionMapping the m
   * @param actionForm    the f
   * @param req           the req
   * @param resp          the resp
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  public ActionForward create(ActionMapping actionMapping, ActionForm actionForm,
      HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException, Exception {

    Map params = req.getParameterMap();
    List errors = new ArrayList();
    Connection con = null;

    InsuranceItemCategoryMasterDAO dao = new InsuranceItemCategoryMasterDAO();
    BasicDynaBean bean = dao.getBean();
    ConversionUtils.copyToDynaBean(params, bean, errors);
    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = null;

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      if (errors.isEmpty()) {
        BasicDynaBean exists = dao.findByKey("insurance_category_name",
            bean.get("insurance_category_name"));
        if (exists == null) {
          BasicDynaBean priorityExist = dao.findByKey("priority", bean.get("priority"));
          if (null != priorityExist) {
            flash.error("Priority " + bean.get("priority") + " already exists..");
          } else {
            bean.set("insurance_category_id", dao.getNextSequence());

            boolean success = dao.insert(con, bean);
            // Changing as per philhealth solution scenario
            // success &=
            // dao.InsertInsuranceCategoryNamesAndIdsIntoPla
            // n((Integer)bean.get("insurance_category_id"),
            // (String)bean.get("insurance_payable"));
            if (success) {

              con.commit();
              redirect = new ActionRedirect(actionMapping.findForward("showRedirect"));
              redirect.addParameter("insurance_category_id", bean.get("insurance_category_id"));
              flash.success("Insurance Category details inserted successfully..");
              redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
              return redirect;
            } else {
              con.rollback();
              flash.error("Failed to add  Insurance Category Details..");
            }
          }
        } else {
          flash.error("Insurance Category name already exists..");
        }
      } else {
        flash.error("Incorrectly formatted values supplied");
      }

      redirect = new ActionRedirect(actionMapping.findForward("addRedirect"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      redirect.addParameter("insurance_category_id", bean.get("insurance_category_id"));
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return redirect;
  }

  /**
   * Show.
   *
   * @param mapping    the m
   * @param actionForm the f
   * @param req        the req
   * @param resp       the resp
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward show(ActionMapping mapping, ActionForm actionForm, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {

    JSONSerializer js = new JSONSerializer().exclude("class");
    InsuranceItemCategoryMasterDAO dao = new InsuranceItemCategoryMasterDAO();
    BasicDynaBean bean = dao.findByKey("insurance_category_id",
        Integer.parseInt(req.getParameter("insurance_category_id")));
    req.setAttribute("bean", bean);
    req.setAttribute("InsuranceCategoriesLists",
        js.serialize(dao.getInsuranceCategoryNamesAndIds()));
    req.setAttribute("existingPriority", dao.getColumnList("priority"));
    return mapping.findForward("addshow");
  }

  /**
   * Update.
   *
   * @param mapping    the m
   * @param actionForm the f
   * @param req        the req
   * @param resp       the resp
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward update(ActionMapping mapping, ActionForm actionForm, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {

    Connection con = null;
    Map params = req.getParameterMap();
    List errors = new ArrayList();

    InsuranceItemCategoryMasterDAO dao = new InsuranceItemCategoryMasterDAO();
    BasicDynaBean bean = dao.getBean();
    ConversionUtils.copyToDynaBean(params, bean, errors);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));

    Object key = req.getParameter("insurance_category_id");
    String insurancePayable = (String) bean.getMap().get("insurance_payable");
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("insurance_category_id", Integer.parseInt(key.toString()));
    FlashScope flash = FlashScope.getScope(req);
    PlanDetailsDAO planDetailsDao = new PlanDetailsDAO();
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      if (errors.isEmpty()) {
        BasicDynaBean priorityExist = dao.findByKey("priority", bean.get("priority"));
        if (null != priorityExist && !priorityExist.get("insurance_category_id")
            .equals(bean.get("insurance_category_id"))) {
          flash.error("Priority " + bean.get("priority") + " already exists..");
        } else {
          int success = dao.update(con, bean.getMap(), keys);
          if ("N".equals(insurancePayable)) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("category_payable", "N");
            Map<String, Object> keyMap = new HashMap<String, Object>();
            keyMap.put("insurance_category_id",
                Integer.parseInt(req.getParameter("insurance_category_id")));
            planDetailsDao.update(con, "insurance_plan_details", map, keyMap);
          }
          if (success > 0) {
            con.commit();
            flash.success("Insurance category details updated successfully..");
          } else {
            con.rollback();
            flash.error("Failed to update Insurance category details..");
          }
        }
      } else {
        flash.error("Incorrectly formatted values supplied");
      }
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      redirect.addParameter("insurance_category_id", req.getParameter("insurance_category_id"));
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return redirect;
  }

}
