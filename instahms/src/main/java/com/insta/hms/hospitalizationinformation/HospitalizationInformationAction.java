package com.insta.hms.hospitalizationinformation;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.HospitalizationReasonsMaster.HospitalizationReasonsMasterAction;
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

// TODO: Auto-generated Javadoc
/**
 * The Class HospitalizationInformationAction.
 *
 * @author mithun.saha
 */

public class HospitalizationInformationAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(HospitalizationReasonsMasterAction.class);

  /** The json. */
  JSONSerializer json = new JSONSerializer().exclude("class");

  /** The dao. */
  HospitalizationInformationDAO dao = new HospitalizationInformationDAO();
  
  private static final GenericDAO clinicalHospitalizationDetailsDAO =
      new GenericDAO("clinical_hospitalization_details");
  private static final GenericDAO clinicalHospitalizationReasonsDAO =
      new GenericDAO("clinical_hospitalization_reasons");
  
  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, Exception {
    PagedList pl = null;
    int userCenterId = (Integer) request.getSession(false).getAttribute("centerId");
    Map requestParams = request.getParameterMap();
    pl =
        dao.getHospitalizationInformations(requestParams,
            ConversionUtils.getListingParameter(request.getParameterMap()), userCenterId);
    request.setAttribute("pagedList", pl);
    return mapping.findForward("list");
  }

  /**
   * Adds the.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, Exception {

    request.setAttribute("reasonListJson", json.serialize(ConversionUtils
        .copyListDynaBeansToMap(clinicalHospitalizationReasonsDAO.findAllByKey("status", "A"))));
    return mapping.findForward("addshow");
  }

  /**
   * Show.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, Exception {
    String mrno = (String) request.getParameter("mr_no");
    String hospitalizationId = request.getParameter("hospitalization_id");
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

    List<BasicDynaBean> hospitalizationInformationList = null;
    if (hospitalizationId != null && !hospitalizationId.equals("")) {
      hospitalizationInformationList =
          dao.getHospitalizationBean(mrno, Integer.parseInt(hospitalizationId));
    } else {
      hospitalizationInformationList = dao.getHospitalizationBean(mrno);
    }
    if ((mrno != null && !mrno.equals(""))) {
      bean = dao.findByKey("mr_no", mrno);
      request.setAttribute("hospitalizationInformationList", hospitalizationInformationList);
      request.setAttribute("reasonListJson", json.serialize(ConversionUtils
          .copyListDynaBeansToMap(clinicalHospitalizationReasonsDAO.findAllByKey("status", "A"))));
      if (bean != null) {
        if (hospitalizationId != null && !hospitalizationId.equals("")) {
          request.setAttribute("mod_time", (dao.findByKey("hospitalization_id",
              Integer.parseInt(hospitalizationId))).get("mod_time"));
          request.setAttribute("userName", (dao.findByKey("hospitalization_id",
              Integer.parseInt(hospitalizationId))).get("user_name"));
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
   * Save hospitalization details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward saveHospitalizationDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    Connection con = null;
    boolean success = false;
    ActionRedirect redirect = null;
    String mrno = (String) req.getParameter("mr_no");
    String userName = (String) req.getSession(false).getAttribute("userid");
    Timestamp modTime = DataBaseUtil.getDateandTime();
    String[] hospitalName = req.getParameterValues("hospital_name");
    String[] deleteItem = req.getParameterValues("hdeleted");
    String[] reasonId = req.getParameterValues("reason_id");
    String[] hospitalizationDetId = req.getParameterValues("hospitalization_details_id");
    BasicDynaBean clinicalHospInfoBean = null;
    BasicDynaBean clinicalHospInfoDetBean = null;
    List errors = new ArrayList();
    BasicDynaBean bean = dao.findByKey("mr_no", mrno);
    Map keys = new HashMap();
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      if (bean != null) {
        clinicalHospInfoBean = dao.getBean();
        ConversionUtils.copyToDynaBean(req.getParameterMap(), clinicalHospInfoBean, errors);
        if (errors.isEmpty()) {
          clinicalHospInfoBean.set("hospitalization_id", bean.get("hospitalization_id"));
          clinicalHospInfoBean.set("user_name", userName);
          clinicalHospInfoBean.set("mod_time", modTime);
          keys.put("hospitalization_id", bean.get("hospitalization_id"));
          dao.update(con, clinicalHospInfoBean.getMap(), keys);

          if (deleteItem != null) {
            for (int i = 0; i < deleteItem.length; i++) {
              if (deleteItem[i].equals("false")) {
                if (hospitalizationDetId != null && !hospitalizationDetId[i].equals("")) {
                  Map keys1 = new HashMap();
                  keys1.put("hospitalization_details_id",
                      Integer.parseInt(hospitalizationDetId[i]));
                  clinicalHospInfoDetBean = clinicalHospitalizationDetailsDAO.getBean();
                  ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i,
                      clinicalHospInfoDetBean, errors);
                  if (errors.isEmpty()) {
                    clinicalHospInfoDetBean.set("hospitalization_details_id",
                        Integer.parseInt(hospitalizationDetId[i]));
                    clinicalHospInfoDetBean.set("hospitalization_id",
                        bean.get("hospitalization_id"));
                    clinicalHospInfoDetBean.set("hospital_name", hospitalName[i]);
                    clinicalHospInfoDetBean.set("reason_id", Integer.parseInt(reasonId[i]));

                  }
                  clinicalHospitalizationDetailsDAO.update(con, clinicalHospInfoDetBean.getMap(),
                      keys1);

                } else if (hospitalName != null && !hospitalName[i].equals("")) {
                  clinicalHospInfoDetBean = clinicalHospitalizationDetailsDAO.getBean();
                  ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i,
                      clinicalHospInfoDetBean, errors);
                  if (errors.isEmpty()) {
                    clinicalHospInfoDetBean.set("hospitalization_details_id",
                        clinicalHospitalizationDetailsDAO.getNextSequence());
                    clinicalHospInfoDetBean.set("hospitalization_id",
                        bean.get("hospitalization_id"));
                    clinicalHospInfoDetBean.set("reason_id", Integer.parseInt(reasonId[i]));

                    clinicalHospitalizationDetailsDAO.insert(con, clinicalHospInfoDetBean);
                  }
                }
              } else if (deleteItem[i].equals("true") && hospitalizationDetId != null
                  && !hospitalizationDetId[i].equals("")) {
                clinicalHospitalizationDetailsDAO.delete(con, "hospitalization_details_id",
                    Integer.parseInt(hospitalizationDetId[i]));
              }
            }
          }
          success = true;
        }
      } else {
        int hospitalizationId = dao.getNextSequence();
        clinicalHospInfoBean = dao.getBean();
        ConversionUtils.copyToDynaBean(req.getParameterMap(), clinicalHospInfoBean, errors);
        if (errors.isEmpty()) {
          clinicalHospInfoBean.set("hospitalization_id", hospitalizationId);
          clinicalHospInfoBean.set("user_name", userName);
          clinicalHospInfoBean.set("mod_time", modTime);
          dao.insert(con, clinicalHospInfoBean);
        }

        if (deleteItem != null) {
          for (int i = 0; i < deleteItem.length; i++) {
            if (!hospitalName[i].equals("") && deleteItem[i].equals("false")) {
              clinicalHospInfoDetBean = clinicalHospitalizationDetailsDAO.getBean();
              ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, clinicalHospInfoDetBean,
                  errors);
              if (errors.isEmpty()) {
                clinicalHospInfoDetBean.set("hospitalization_details_id",
                    clinicalHospitalizationDetailsDAO.getNextSequence());
                if (bean != null) {
                  clinicalHospInfoDetBean.set("hospitalization_id", bean.get("hospitalization_id"));
                } else {
                  clinicalHospInfoDetBean.set("hospitalization_id",
                      clinicalHospInfoBean.get("hospitalization_id"));
                }
                clinicalHospInfoDetBean.set("reason_id", Integer.parseInt(reasonId[i]));
                clinicalHospitalizationDetailsDAO.insert(con, clinicalHospInfoDetBean);
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
    redirect.addParameter("hospitalization_id", clinicalHospInfoBean.get("hospitalization_id"));
    redirect.addParameter("mr_no", clinicalHospInfoBean.get("mr_no"));
    redirect.addParameter("values_as_of_date", clinicalHospInfoBean.get("data_as_of_date"));
    FlashScope flash = FlashScope.getScope(req);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

}
