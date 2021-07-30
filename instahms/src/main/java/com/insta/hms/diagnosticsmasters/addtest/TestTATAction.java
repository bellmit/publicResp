package com.insta.hms.diagnosticsmasters.addtest;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.diagnosticsmasters.outhousemaster.OutHouseMasterDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CityMaster.CityMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.StateMaster.StateMasterDAO;
import flexjson.JSONSerializer;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class TestTATAction.
 */
public class TestTATAction extends BaseAction {
  
  /** The dao. */
  CenterMasterDAO dao = new CenterMasterDAO();
  
  /** The out house master DAO. */
  OutHouseMasterDAO outHouseMasterDAO = new OutHouseMasterDAO();
  
  /** The tatdao. */
  TestTATDAO tatdao = new TestTATDAO("diag_tat_center_master");

  /**
   * Gets the details.
   *
   * @param mapping the mapping
   * @param af the af
   * @param request the request
   * @param response the response
   * @return the details
   * @throws Exception the exception
   */
  public ActionForward getDetails(ActionMapping mapping, ActionForm af, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    request.setAttribute("testDeatils",
        AddTestDAOImpl.getTestDetails(request.getParameter("test_id"), "GENERAL", null));
    Integer maxCentersIncDefault = (Integer) GenericPreferencesDAO.getAllPrefs()
        .get("max_centers_inc_default");
    if (maxCentersIncDefault > 1) {
      request.setAttribute("centers", dao.getAllCentersExceptSuper());
    } else {
      request.setAttribute("centers", dao.getAllCenters());
    }
    request.setAttribute("outHouseNames", OutHouseMasterDAO.getAllOutSources());
    request.setAttribute("states", StateMasterDAO.getStateIdName());
    request.setAttribute("city", CityMasterDAO.getAvalCitynames());
    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("cities_json", js.deepSerialize(
        ConversionUtils.copyListDynaBeansToMap(new CityMasterDAO().listAll("city_name"))));
    Map filterParams = new HashMap(request.getParameterMap());
    PagedList list = tatdao.getTATDetails(request.getParameter("test_id"), filterParams);
    List<BasicDynaBean> tatdtoList = list.getDtoList();
    List<BasicDynaBean> dtoList = new TestTATBO().getDistinctTATDetails(tatdtoList);
    Boolean[][] processingDaysArray = new Boolean[dtoList.size()][7];
    String[] conductionStartTimeArray = new String[dtoList.size()];

    int inc = 0;
    for (Iterator iterator = dtoList.iterator(); iterator.hasNext();) {

      BasicDynaBean basicDynaBean = (BasicDynaBean) iterator.next();
      // String outSourceName=tatdao.getOutsuorceName(request.getParameter("test_id"),
      // (Integer)basicDynaBean.get("center_id"));
      // basicDynaBean.set("outsource_name", outSourceName);
      String processingDay = (String) basicDynaBean.get("processing_days");
      Time conductionStartTime = (Time) basicDynaBean.get("conduction_start_time");
      if (conductionStartTime != null) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
        Date date = sdf1.parse(conductionStartTime.toString());
        conductionStartTimeArray[inc] = sdf2.format(date).toString();
      }
      if (processingDay != null && !"".equals(processingDay)) {
        for (int j = 0; j < processingDay.length(); j++) {
          processingDaysArray[inc][j] = processingDay.charAt(j) != 'X';
        }
      }
      inc++;
    }

    request.setAttribute("dtoList", dtoList);
    request.setAttribute("_conduction_start_time", conductionStartTimeArray);
    request.setAttribute("_processing_days", processingDaysArray);

    return mapping.findForward("edit_test_tat");
  }

  /**
   * Gets the screen.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the screen
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, ParseException {
    request.setAttribute("testDeatils",
        AddTestDAOImpl.getTestDetails(request.getParameter("test_id"), "GENERAL", null));
    Integer maxCentersIncDefault = (Integer) GenericPreferencesDAO.getAllPrefs()
        .get("max_centers_inc_default");
    if (maxCentersIncDefault > 1) {
      request.setAttribute("centers", dao.getAllCentersExceptSuper());
    } else {
      request.setAttribute("centers", dao.getAllCenters());
    }
    request.setAttribute("outHouseNames", OutHouseMasterDAO.getAllOutSources());
    request.setAttribute("states", StateMasterDAO.getStateIdName());
    request.setAttribute("city", CityMasterDAO.getAvalCitynames());
    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("cities_json", js.deepSerialize(
        ConversionUtils.copyListDynaBeansToMap(new CityMasterDAO().listAll("city_name"))));
    List<BasicDynaBean> tatdtoList = tatdao.getTATHours(request.getParameter("test_id"));
    List<BasicDynaBean> dtoList = new TestTATBO().getDistinctTATDetails(tatdtoList);
    Boolean[][] processingDays = new Boolean[dtoList.size()][7];
    String[] conductionStartTimeStr = new String[dtoList.size()];

    int inc = 0;
    for (Iterator iterator = dtoList.iterator(); iterator.hasNext();) {
      BasicDynaBean basicDynaBean = (BasicDynaBean) iterator.next();
      // String outSourceName=tatdao.getOutsuorceName(request.getParameter("test_id"),
      // (Integer)basicDynaBean.get("center_id"));
      // basicDynaBean.set("outsource_name", outSourceName);
      String processingDay = (String) basicDynaBean.get("processing_days");
      Time conductionStartTime = (Time) basicDynaBean.get("conduction_start_time");

      if (conductionStartTime != null) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
        Date date = sdf1.parse(conductionStartTime.toString());
        conductionStartTimeStr[inc] = sdf2.format(date).toString();
      }
      if (processingDay != null && !"".equals(processingDay)) {
        for (int j = 0; j < processingDay.length(); j++) {
          processingDays[inc][j] = processingDay.charAt(j) != 'X';
        }
      }
      inc++;
    }
    request.setAttribute("dtoList", dtoList);
    request.setAttribute("_conduction_start_time", conductionStartTimeStr);
    request.setAttribute("_processing_days", processingDays);

    return mapping.findForward("edit_test_tat");

  }

  /**
   * Save.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, Exception {
    boolean success = false;
    Connection con = null;
    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("show"));// 10:00:00
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      success = updateTATHours(con, req);
      if (success) {
        con.commit();
        flash.success("TAT Center Master details inserted successfully..");
        redirect.addParameter("test_id", req.getParameter("test_id"));
        return redirect;
      } else {
        con.rollback();
        flash.error("Failed to add  TAT details..");
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return redirect;
  }

  /**
   * Update TAT hours.
   *
   * @param con the con
   * @param req the req
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public boolean updateTATHours(Connection con, HttpServletRequest req)
      throws SQLException, Exception {
    boolean success = true;
    String[] conductionTATHours = req.getParameterValues("conduction_tat_hours");
    String[] tatCenterId = req.getParameterValues("tat_center_id");
    String[] centerIDs = req.getParameterValues("center_id");
    String[] logisticsTatHours = req.getParameterValues("logistics_tat_hours");
    String[] conductionStartTime = req.getParameterValues("conduction_start_time");
    String testID = req.getParameter("test_id");
    GenericDAO diagTatCenterDAO = new GenericDAO("diag_tat_center_master");
    BasicDynaBean diagTatCenterBean = null;
    for (int i = 0; i < tatCenterId.length; i++) {
      StringBuilder pdays = new StringBuilder();
      for (int j = 0; j < 7; j++) {
        if (req.getParameter("processing_Day" + (i + 1) + j) != null) {
          pdays.append(j);
        } else {
          pdays.append('X');
        }
      }
      diagTatCenterBean = diagTatCenterDAO.getBean();
      diagTatCenterBean.set("tat_center_id", tatCenterId[i]);
      diagTatCenterBean.set("test_id", testID);
      // Integer
      // tathour=!conductionTATHours[i].equals("")?Integer.parseInt(conductionTATHours[i]):null;
      BigDecimal tathour = !conductionTATHours[i].equals("")
          ? BigDecimal.valueOf(Double.parseDouble(conductionTATHours[i]))
          : null;
      diagTatCenterBean.set("conduction_tat_hours", tathour);
      diagTatCenterBean.set("center_id", Integer.parseInt(centerIDs[i]));
      diagTatCenterBean.set("processing_days", pdays.toString());
      BigDecimal logTathour = !logisticsTatHours[i].equals("")
          ? BigDecimal.valueOf(Double.parseDouble(logisticsTatHours[i]))
          : null;
      diagTatCenterBean.set("logistics_tat_hours", logTathour);
      if (!conductionStartTime[i].equals("")) {
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
        Date date = sdf2.parse(conductionStartTime[i]);
        long ms = date.getTime();
        Time time = new Time(ms);
        diagTatCenterBean.set("conduction_start_time", time);
      } else {
        diagTatCenterBean.set("conduction_start_time", null);
      }
      if (null != tatCenterId[i] && !tatCenterId[i].equals("")) {
        success &= diagTatCenterDAO.update(con, diagTatCenterBean.getMap(), "tat_center_id",
            diagTatCenterBean.get("tat_center_id")) > 0;
      } else {
        diagTatCenterBean.set("tat_center_id", diagTatCenterDAO.getNextSequence() + "");
        success &= diagTatCenterDAO.insert(con, diagTatCenterBean);
      }

      if (!success) {
        break;
      }
    }
    return success;
  }

}
