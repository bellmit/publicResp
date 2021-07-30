package com.insta.hms.clinicaldata.bloodtransfusions;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class BloodTransfusionsAction.
 *
 * @author mithun.saha
 */

public class BloodTransfusionsAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(BloodTransfusionsDAO.class);

  /** The json. */
  JSONSerializer json = new JSONSerializer().exclude("class");

  /** The dao. */
  BloodTransfusionsDAO dao = new BloodTransfusionsDAO();
  
  private static final GenericDAO clinicalTransfusionDetailsDAO =
      new GenericDAO("clinical_transfusion_details");

  /**
   * List.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, Exception {
    PagedList pl = null;
    int userCenterId = (Integer) request.getSession(false).getAttribute("centerId");
    Map requestParams = request.getParameterMap();
    pl = dao.getBloodTransfusions(requestParams,
        ConversionUtils.getListingParameter(request.getParameterMap()), userCenterId);
    request.setAttribute("pagedList", pl);
    return mapping.findForward("list");
  }

  /**
   * Adds the.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, Exception {
    return mapping.findForward("addshow");
  }

  /**
   * Show.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws Exception
   *           the exception
   */
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, Exception {
    String mrno = (String) request.getParameter("mr_no");
    String tranfusionId = request.getParameter("transfusion_id");
    BasicDynaBean bean = null;
    if (mrno != null && !mrno.equals("")) {
      Map patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrno);
      if (patmap == null) {
        FlashScope flash = FlashScope.getScope(request);
        flash.put("error", mrno + " doesn't exists.");
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
    }

    List<BasicDynaBean> tranfusionInformationList = null;
    if (tranfusionId != null && !tranfusionId.equals("")) {
      tranfusionInformationList = dao.getBloodTransfusionBean(mrno, Integer.parseInt(tranfusionId));
    } else {
      tranfusionInformationList = dao.getBloodTransfusionBean(mrno);
    }
    if ((mrno != null && !mrno.equals(""))) {
      bean = dao.findByKey("mr_no", mrno);
      request.setAttribute("tranfusionInformationList", tranfusionInformationList);
      if (bean != null) {
        if (tranfusionId != null && !tranfusionId.equals("")) {
          request.setAttribute("mod_time",
              (dao.findByKey("transfusion_id", Integer.parseInt(tranfusionId))).get("mod_time"));
          request.setAttribute("userName",
              (dao.findByKey("transfusion_id", Integer.parseInt(tranfusionId))).get("user_name"));
        } else {
          request.setAttribute("mod_time", bean.get("mod_time"));
          request.setAttribute("userName", bean.get("user_name"));
        }
      }
      if (bean != null) {
        request.setAttribute("dataAsOfDate", (dao.findByKey("mr_no", mrno)).get("data_as_of_date"));
      } else {
        request.setAttribute("dataAsOfDate", null);
      }
    }

    return mapping.findForward("addshow");
  }

  /**
   * Save tranfusion details.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward saveTranfusionDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    Connection con = null;
    boolean success = false;
    ActionRedirect redirect = null;
    String mrno = (String) req.getParameter("mr_no");
    String userName = (String) req.getSession(false).getAttribute("userid");
    Timestamp modTime = DataBaseUtil.getDateandTime();
    String[] deleteItem = req.getParameterValues("hdeleted");
    String[] tranfusionIdStr = req.getParameterValues("transfusion_id");
    String[] transfusionDate = req.getParameterValues("transfusion_date");
    String[] tranfusionDetId = req.getParameterValues("transfusion_details_id");
    BasicDynaBean clinicalTranfusionInfoBean = null;
    BasicDynaBean clinicalTranfusionDetBean = null;
    List errors = new ArrayList();
    BasicDynaBean bean = dao.findByKey("mr_no", mrno);
    Map keys = new HashMap();
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      if (bean != null) {
        clinicalTranfusionInfoBean = dao.getBean();
        ConversionUtils.copyToDynaBean(req.getParameterMap(), clinicalTranfusionInfoBean, errors);
        if (errors.isEmpty()) {
          clinicalTranfusionInfoBean.set("transfusion_id", bean.get("transfusion_id"));
          clinicalTranfusionInfoBean.set("user_name", userName);
          clinicalTranfusionInfoBean.set("mod_time", modTime);
          keys.put("transfusion_id", bean.get("transfusion_id"));
          dao.update(con, clinicalTranfusionInfoBean.getMap(), keys);

          if (deleteItem != null) {
            for (int i = 0; i < deleteItem.length; i++) {
              if (deleteItem[i].equals("false")) {
                if (tranfusionDetId != null && !tranfusionDetId[i].equals("")) {
                  Map keys1 = new HashMap();
                  keys1.put("transfusion_details_id", Integer.parseInt(tranfusionDetId[i]));
                  clinicalTranfusionDetBean = clinicalTransfusionDetailsDAO.getBean();
                  ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i,
                      clinicalTranfusionDetBean, errors);
                  if (errors.isEmpty()) {
                    clinicalTranfusionDetBean.set("transfusion_details_id",
                        Integer.parseInt(tranfusionDetId[i]));
                    clinicalTranfusionDetBean.set("transfusion_id", bean.get("transfusion_id"));

                  }

                  clinicalTransfusionDetailsDAO.update(con, clinicalTranfusionDetBean.getMap(),
                      keys1);

                } else {
                  if (transfusionDate[i] != null && !transfusionDate[i].equals("")) {
                    clinicalTranfusionDetBean = clinicalTransfusionDetailsDAO.getBean();
                    ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i,
                        clinicalTranfusionDetBean, errors);
                    if (errors.isEmpty()) {
                      clinicalTranfusionDetBean.set("transfusion_details_id",
                          clinicalTransfusionDetailsDAO.getNextSequence());
                      clinicalTranfusionDetBean.set("transfusion_id", bean.get("transfusion_id"));

                      clinicalTransfusionDetailsDAO.insert(con, clinicalTranfusionDetBean);
                    }
                  }
                }
              } else if (deleteItem[i].equals("true") && tranfusionDetId != null
                  && !tranfusionDetId[i].equals("")) {
                clinicalTransfusionDetailsDAO.delete(con, "transfusion_details_id",
                    Integer.parseInt(tranfusionDetId[i]));
              }
            }
          }
          success = true;
        }
      } else {
        int transfutionId = dao.getNextSequence();
        clinicalTranfusionInfoBean = dao.getBean();
        ConversionUtils.copyToDynaBean(req.getParameterMap(), clinicalTranfusionInfoBean, errors);
        if (errors.isEmpty()) {
          clinicalTranfusionInfoBean.set("transfusion_id", transfutionId);
          clinicalTranfusionInfoBean.set("user_name", userName);
          clinicalTranfusionInfoBean.set("mod_time", modTime);
          dao.insert(con, clinicalTranfusionInfoBean);
        }

        if (deleteItem != null) {
          for (int i = 0; i < deleteItem.length; i++) {
            if (deleteItem[i].equals("false")
                && (transfusionDate[i] != null && !transfusionDate[i].equals(""))) {
              clinicalTranfusionDetBean = clinicalTransfusionDetailsDAO.getBean();
              ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i,
                  clinicalTranfusionDetBean, errors);
              if (errors.isEmpty()) {
                clinicalTranfusionDetBean.set("transfusion_details_id",
                    clinicalTransfusionDetailsDAO.getNextSequence());
                clinicalTranfusionDetBean.set("transfusion_id", transfutionId);

                clinicalTransfusionDetailsDAO.insert(con, clinicalTranfusionDetBean);
              }
            }
          }
        }
        success = true;
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    redirect.addParameter("transfusion_id", clinicalTranfusionInfoBean.get("transfusion_id"));
    redirect.addParameter("mr_no", clinicalTranfusionInfoBean.get("mr_no"));
    redirect.addParameter("values_as_of_date", clinicalTranfusionInfoBean.get("data_as_of_date"));
    FlashScope flash = FlashScope.getScope(req);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

}
