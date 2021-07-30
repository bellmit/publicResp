package com.insta.hms.infectiondata;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class InfectionInformationAction.
 *
 * @author mithun.saha
 */


public class InfectionInformationAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(InfectionInformationAction.class);

  /** The json. */
  JSONSerializer json = new JSONSerializer().exclude("class");

  /** The dao. */
  InfectionInformationDAO dao = new InfectionInformationDAO();

  /** The adao. */
  GenericDAO adao = new GenericDAO("clinical_infection_antibiotic_log");
  
  private static final GenericDAO clinicalInfectionsMasterDAO =
      new GenericDAO("clinical_infections_master");
  private static final GenericDAO clinicalInfectionsDAO = new GenericDAO("clinical_infections");
  private static final GenericDAO clinicalInfectionsSiteDAO =
      new GenericDAO("clinical_infection_site_master");
  
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
    pl = dao.getInfections(requestParams,
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

    request.setAttribute("infectionsTypesJson", json.serialize(ConversionUtils
        .copyListDynaBeansToMap(clinicalInfectionsMasterDAO.listAll("infection_type"))));
    request.setAttribute("infectionsSitesJson", json.serialize(ConversionUtils
        .copyListDynaBeansToMap(clinicalInfectionsSiteDAO.listAll("infection_site_name"))));
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
    String infectionRecId = request.getParameter("clinical_infections_recorded_id");
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

    List<BasicDynaBean> infectionsInformationList = null;
    if (infectionRecId != null && !infectionRecId.equals("")) {
      infectionsInformationList = dao.getInfectionBean(mrno, Integer.parseInt(infectionRecId));
    } else {
      infectionsInformationList = dao.getInfectionBean(mrno);
    }
    if ((mrno != null && !mrno.equals(""))) {
      bean = dao.findByKey("mr_no", mrno);
      request.setAttribute("infectionsInformationList", infectionsInformationList);
      request.setAttribute("infectionTypeListJson", json.serialize(ConversionUtils
          .copyListDynaBeansToMap(clinicalInfectionsMasterDAO.listAll("infection_type"))));
      request.setAttribute("infectionsSitesJson", json.serialize(ConversionUtils
          .copyListDynaBeansToMap(clinicalInfectionsSiteDAO.listAll("infection_site_name"))));
      if (bean != null) {
        if (infectionRecId != null && !infectionRecId.equals("")) {
          request.setAttribute("mod_time", (dao.findByKey("clinical_infections_recorded_id",
              Integer.parseInt(infectionRecId))).get("mod_time"));
          request.setAttribute("userName", (dao.findByKey("clinical_infections_recorded_id",
              Integer.parseInt(infectionRecId))).get("user_name"));
        } else {
          request.setAttribute("mod_time", bean.get("mod_time"));
          request.setAttribute("userName", bean.get("user_name"));
        }
      }
      if (bean != null) {
        request.setAttribute("dataAsOfDate",
            (dao.findByKey("mr_no", mrno)).get("values_as_of_date"));
      } else {
        request.setAttribute("dataAsOfDate", null);
      }
    }

    return mapping.findForward("addshow");
  }

  /**
   * Adds the or edit antibiotics.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public ActionForward addOrEditAntibiotics(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {
    List<BasicDynaBean> antiListOfBean = null;
    String infectionId = request.getParameter("infection_id");
    if (infectionId != null && !infectionId.equals("")) {
      antiListOfBean = dao.getAntibiotics(infectionId);
    }
    request.setAttribute("antibioticsList", antiListOfBean);
    return mapping.findForward("addorshow");
  }

  /**
   * Save infection details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward saveInfectionDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    Connection con = null;
    boolean success = false;
    ActionRedirect redirect = null;
    String mrno = (String) req.getParameter("mr_no");
    String userName = (String) req.getSession(false).getAttribute("userid");
    Timestamp modTime = DataBaseUtil.getDateandTime();
    String[] infectionTypeId = req.getParameterValues("infection_type_id");
    String[] deleteItem = req.getParameterValues("hdeleted");
    String[] infectionId = req.getParameterValues("infection_id");
    BasicDynaBean clinicalInfectionInfoBean = null;
    BasicDynaBean clinicalInfectionDetBean = null;
    List errors = new ArrayList();
    BasicDynaBean bean = dao.findByKey("mr_no", mrno);
    Map keys = new HashMap();
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      if (bean != null) {
        clinicalInfectionInfoBean = dao.getBean();
        ConversionUtils.copyToDynaBean(req.getParameterMap(), clinicalInfectionInfoBean, errors);
        if (errors.isEmpty()) {
          clinicalInfectionInfoBean.set("clinical_infections_recorded_id",
              bean.get("clinical_infections_recorded_id"));
          clinicalInfectionInfoBean.set("user_name", userName);
          clinicalInfectionInfoBean.set("mod_time", modTime);
          keys.put("clinical_infections_recorded_id", bean.get("clinical_infections_recorded_id"));
          dao.update(con, clinicalInfectionInfoBean.getMap(), keys);

          if (deleteItem != null) {
            for (int i = 0; i < deleteItem.length; i++) {
              if (deleteItem[i].equals("false")) {
                if (infectionId != null && !infectionId[i].equals("")) {
                  Map keys1 = new HashMap();
                  keys1.put("infection_id", Integer.parseInt(infectionId[i]));
                  clinicalInfectionDetBean = clinicalInfectionsDAO.getBean();
                  ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i,
                      clinicalInfectionDetBean, errors);
                  if (errors.isEmpty()) {
                    clinicalInfectionDetBean.set("infection_id", Integer.parseInt(infectionId[i]));
                    clinicalInfectionDetBean.set("clinical_infections_recorded_id",
                        bean.get("clinical_infections_recorded_id"));
                    clinicalInfectionDetBean.set("infection_type_id",
                        Integer.parseInt(infectionTypeId[i]));

                  }
                  clinicalInfectionsDAO.update(con,
                      clinicalInfectionDetBean.getMap(), keys1);

                } else if (infectionTypeId != null && !infectionTypeId[i].equals("")) {
                  clinicalInfectionDetBean = clinicalInfectionsDAO.getBean();
                  ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i,
                      clinicalInfectionDetBean, errors);
                  if (errors.isEmpty()) {
                    clinicalInfectionDetBean.set("infection_id",
                        clinicalInfectionsDAO.getNextSequence());
                    clinicalInfectionDetBean.set("clinical_infections_recorded_id",
                        bean.get("clinical_infections_recorded_id"));

                    clinicalInfectionsDAO.insert(con, clinicalInfectionDetBean);
                  }
                }
              } else if (deleteItem[i].equals("true") && infectionId != null
                  && !infectionId[i].equals("")) {
                clinicalInfectionsDAO.delete(con, "infection_id",
                    Integer.parseInt(infectionId[i]));
              }
            }
          }
          success = true;
        }
      } else {
        int infectionRecId = dao.getNextSequence();
        clinicalInfectionInfoBean = dao.getBean();
        ConversionUtils.copyToDynaBean(req.getParameterMap(), clinicalInfectionInfoBean, errors);
        if (errors.isEmpty()) {
          clinicalInfectionInfoBean.set("clinical_infections_recorded_id", infectionRecId);
          clinicalInfectionInfoBean.set("user_name", userName);
          clinicalInfectionInfoBean.set("mod_time", modTime);
          dao.insert(con, clinicalInfectionInfoBean);
        }

        if (deleteItem != null) {
          for (int i = 0; i < deleteItem.length; i++) {
            if (!infectionTypeId[i].equals("") && deleteItem[i].equals("false")) {
              clinicalInfectionDetBean = clinicalInfectionsDAO.getBean();
              ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i,
                  clinicalInfectionDetBean, errors);
              if (errors.isEmpty()) {
                clinicalInfectionDetBean.set("infection_id",
                    clinicalInfectionsDAO.getNextSequence());
                if (bean != null) {
                  clinicalInfectionDetBean.set("clinical_infections_recorded_id",
                      bean.get("clinical_infections_recorded_id"));
                } else {
                  clinicalInfectionDetBean.set("clinical_infections_recorded_id",
                      clinicalInfectionInfoBean.get("clinical_infections_recorded_id"));
                }
                clinicalInfectionsDAO.insert(con, clinicalInfectionDetBean);
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
    redirect.addParameter("clinical_infections_recorded_id",
        clinicalInfectionInfoBean.get("clinical_infections_recorded_id"));
    redirect.addParameter("mr_no", clinicalInfectionInfoBean.get("mr_no"));
    redirect.addParameter("values_as_of_date", clinicalInfectionInfoBean.get("values_as_of_date"));
    FlashScope flash = FlashScope.getScope(req);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Save antibiotic details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward saveAntibioticDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    Connection con = null;
    boolean success = false;
    ActionRedirect redirect = null;
    String[] medicineId = req.getParameterValues("op_medicine_pres_id");
    String[] deleteItem = req.getParameterValues("hdeleted");
    String infectionId = req.getParameter("infection_id");
    String[] antibioticLogId = req.getParameterValues("antibiotic_log_id");
    BasicDynaBean clinicalAntibioticBean = null;
    List errors = new ArrayList();
    Map keys = new HashMap();
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      if (infectionId != null && !infectionId.equals("")) {
        if (deleteItem != null) {
          for (int i = 0; i < deleteItem.length; i++) {
            if (antibioticLogId != null && !antibioticLogId[i].equals("")) {
              if (deleteItem[i].equals("false")) {
                clinicalAntibioticBean = adao.getBean();
                ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i,
                    clinicalAntibioticBean, errors);
                if (errors.isEmpty()) {
                  clinicalAntibioticBean.set("antibiotic_log_id",
                      clinicalAntibioticBean.get("antibiotic_log_id"));
                  clinicalAntibioticBean.set("infection_id", Integer.parseInt(infectionId));
                  keys.put("antibiotic_log_id", clinicalAntibioticBean.get("antibiotic_log_id"));
                  adao.update(con, clinicalAntibioticBean.getMap(), keys);
                }
              } else if (deleteItem[i].equals("true") && infectionId != null
                  && !infectionId.equals("")) {
                adao.delete(con, "antibiotic_log_id", Integer.parseInt(antibioticLogId[i]));
              }
            } else {
              if (deleteItem[i].equals("false")) {
                if (medicineId != null && !medicineId[i].equals("")) {
                  int antiLogId = adao.getNextSequence();
                  clinicalAntibioticBean = adao.getBean();
                  ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i,
                      clinicalAntibioticBean, errors);
                  if (errors.isEmpty()) {
                    clinicalAntibioticBean.set("antibiotic_log_id", antiLogId);
                    clinicalAntibioticBean.set("infection_id", Integer.parseInt(infectionId));
                    adao.insert(con, clinicalAntibioticBean);
                  }
                }
              }
            }
          }
        }
        success = true;
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    redirect = new ActionRedirect(mapping.findForward("addShowRedirect"));
    redirect.addParameter("mr_no", req.getParameter("mr_no"));
    redirect.addParameter("infection_id", infectionId);
    redirect.addParameter("infection_type", req.getParameter("infection_type"));
    FlashScope flash = FlashScope.getScope(req);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }


  /**
   * Find items.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public ActionForward findItems(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException, SQLException {

    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    JSONSerializer js = new JSONSerializer().exclude("class");
    String query = request.getParameter("query");


    List list = dao.getAllItems(query);
    HashMap retVal = new HashMap();
    retVal.put("result", ConversionUtils.copyListDynaBeansToMap(list));

    js.deepSerialize(retVal, response.getWriter());
    response.flushBuffer();

    return null;
  }

}
