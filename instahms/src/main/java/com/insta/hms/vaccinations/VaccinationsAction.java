package com.insta.hms.vaccinations;

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

// TODO: Auto-generated Javadoc
/**
 * The Class VaccinationsAction.
 *
 * @author mithun.saha
 */

public class VaccinationsAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(VaccinationsAction.class);
  
  /** The json. */
  JSONSerializer json = new JSONSerializer().exclude("class");
  
  /** The dao. */
  VaccinationsDAO dao = new VaccinationsDAO();
  
  private static final GenericDAO clinicalVaccinationsDetailsDAO =
      new GenericDAO("clinical_vaccinations_details");
  private static final GenericDAO clinicalVaccinationsMasterDAO =
      new GenericDAO("clinical_vaccinations_master");

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
    pl = dao.getVaccinations(requestParams,
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
        .copyListDynaBeansToMap(new GenericDAO("clinical_vacc_no_reason").listAll("reason_name"))));
    request.setAttribute("vaccinationTypesJson", json.serialize(ConversionUtils
        .copyListDynaBeansToMap(clinicalVaccinationsMasterDAO.listAll("vaccination_type"))));
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
    String vaccinationId = request.getParameter("vaccination_id");
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

    List<BasicDynaBean> vaccinationInformationList = null;
    if (vaccinationId != null && !vaccinationId.equals("")) {
      vaccinationInformationList = dao.getVaccinationBean(mrno, Integer.parseInt(vaccinationId));
    } else {
      vaccinationInformationList = dao.getVaccinationBean(mrno);
    }
    if ((mrno != null && !mrno.equals(""))) {
      bean = dao.findByKey("mr_no", mrno);
      request.setAttribute("vaccinationInformationList", vaccinationInformationList);
      request.setAttribute("reasonListJson",
          json.serialize(ConversionUtils.copyListDynaBeansToMap(new GenericDAO(
              "clinical_vacc_no_reason").listAll("reason_name"))));
      request.setAttribute("vaccinationTypeListJson", json.serialize(ConversionUtils
          .copyListDynaBeansToMap(clinicalVaccinationsMasterDAO.listAll("vaccination_type"))));
      if (bean != null) {
        if (vaccinationId != null && !vaccinationId.equals("")) {
          request.setAttribute("mod_time",
              (dao.findByKey("vaccination_id", Integer.parseInt(vaccinationId))).get("mod_time"));
          request.setAttribute("userName",
              (dao.findByKey("vaccination_id", Integer.parseInt(vaccinationId))).get("user_name"));
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
   * Save vaccination details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward saveVaccinationDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    Connection con = null;
    boolean success = false;
    ActionRedirect redirect = null;
    String mrno = (String) req.getParameter("mr_no");
    String userName = (String) req.getSession(false).getAttribute("userid");
    Timestamp modTime = DataBaseUtil.getDateandTime();
    String[] vaccinTypeId = req.getParameterValues("vaccination_type_id");
    String[] deleteItem = req.getParameterValues("hdeleted");
    String[] reasonId = req.getParameterValues("no_reason_id");
    String[] vaccinDetId = req.getParameterValues("vaccination_details_id");
    BasicDynaBean clinicalVaccinInfoBean = null;
    BasicDynaBean clinicalVaccinDetBean = null;
    List errors = new ArrayList();
    BasicDynaBean bean = dao.findByKey("mr_no", mrno);
    Map keys = new HashMap();
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      if (bean != null) {
        clinicalVaccinInfoBean = dao.getBean();
        ConversionUtils.copyToDynaBean(req.getParameterMap(), clinicalVaccinInfoBean, errors);
        if (errors.isEmpty()) {
          clinicalVaccinInfoBean.set("vaccination_id", bean.get("vaccination_id"));
          clinicalVaccinInfoBean.set("user_name", userName);
          clinicalVaccinInfoBean.set("mod_time", modTime);
          keys.put("vaccination_id", bean.get("vaccination_id"));
          dao.update(con, clinicalVaccinInfoBean.getMap(), keys);

          if (deleteItem != null) {
            for (int i = 0; i < deleteItem.length; i++) {
              if (deleteItem[i].equals("false")) {
                if (vaccinDetId != null && !vaccinDetId[i].equals("")) {
                  Map keys1 = new HashMap();
                  keys1.put("vaccination_details_id", Integer.parseInt(vaccinDetId[i]));
                  clinicalVaccinDetBean = clinicalVaccinationsDetailsDAO.getBean();
                  ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i,
                      clinicalVaccinDetBean, errors);
                  if (errors.isEmpty()) {
                    clinicalVaccinDetBean.set("vaccination_details_id",
                        Integer.parseInt(vaccinDetId[i]));
                    clinicalVaccinDetBean.set("vaccination_id", bean.get("vaccination_id"));
                    clinicalVaccinDetBean.set("vaccination_type_id",
                        Integer.parseInt(vaccinTypeId[i]));
                    if (reasonId != null && !reasonId[i].equals("")) {
                      clinicalVaccinDetBean.set("no_reason_id", Integer.parseInt(reasonId[i]));
                    }

                  }
                  clinicalVaccinationsDetailsDAO.update(con, clinicalVaccinDetBean.getMap(), keys1);

                } else if (vaccinTypeId != null && !vaccinTypeId[i].equals("")) {
                  clinicalVaccinDetBean = clinicalVaccinationsDetailsDAO.getBean();
                  ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i,
                      clinicalVaccinDetBean, errors);
                  if (errors.isEmpty()) {
                    clinicalVaccinDetBean.set("vaccination_details_id", new GenericDAO(
                        "clinical_vaccinations_details").getNextSequence());
                    clinicalVaccinDetBean.set("vaccination_id", bean.get("vaccination_id"));
                    if (reasonId != null && !reasonId[i].equals("")) {
                      clinicalVaccinDetBean.set("no_reason_id", Integer.parseInt(reasonId[i]));
                    }

                    clinicalVaccinationsDetailsDAO.insert(con, clinicalVaccinDetBean);
                  }
                }
              } else if (deleteItem[i].equals("true") && vaccinDetId != null
                  && !vaccinDetId[i].equals("")) {
                clinicalVaccinationsDetailsDAO.delete(con, "vaccination_details_id",
                    Integer.parseInt(vaccinDetId[i]));
              }
            }
          }
          success = true;
        }
      } else {
        int vaccinationId = dao.getNextSequence();
        clinicalVaccinInfoBean = dao.getBean();
        ConversionUtils.copyToDynaBean(req.getParameterMap(), clinicalVaccinInfoBean, errors);
        if (errors.isEmpty()) {
          clinicalVaccinInfoBean.set("vaccination_id", vaccinationId);
          clinicalVaccinInfoBean.set("user_name", userName);
          clinicalVaccinInfoBean.set("mod_time", modTime);
          dao.insert(con, clinicalVaccinInfoBean);
        }

        if (deleteItem != null) {
          for (int i = 0; i < deleteItem.length; i++) {
            if (!vaccinTypeId[i].equals("") && deleteItem[i].equals("false")) {
              clinicalVaccinDetBean = clinicalVaccinationsDetailsDAO.getBean();
              ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, clinicalVaccinDetBean,
                  errors);
              if (errors.isEmpty()) {
                clinicalVaccinDetBean.set("vaccination_details_id", new GenericDAO(
                    "clinical_vaccinations_details").getNextSequence());
                if (bean != null) {
                  clinicalVaccinDetBean.set("vaccination_id", bean.get("vaccination_id"));
                } else {
                  clinicalVaccinDetBean.set("vaccination_id", vaccinationId);
                }
                if (reasonId != null && !reasonId[i].equals("")) {
                  clinicalVaccinDetBean.set("no_reason_id", Integer.parseInt(reasonId[i]));
                }

                clinicalVaccinationsDetailsDAO.insert(con, clinicalVaccinDetBean);
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
    redirect.addParameter("vaccination_id", clinicalVaccinInfoBean.get("vaccination_id"));
    redirect.addParameter("mr_no", clinicalVaccinInfoBean.get("mr_no"));
    redirect.addParameter("values_as_of_date", clinicalVaccinInfoBean.get("data_as_of_date"));
    FlashScope flash = FlashScope.getScope(req);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

}
