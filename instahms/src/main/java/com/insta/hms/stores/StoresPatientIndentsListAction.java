package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StoresPatientIndentsListAction extends BaseAction {

  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, Exception {
    StoresPatientIndentDAO patIndentDAO = new StoresPatientIndentDAO();
    Map map = getParameterMap(req);
    String defaultUserStore = (String) req.getSession().getAttribute("pharmacyStoreId");

    if (defaultUserStore != null && map.get("indent_store") == null) {// for the first time default
                                                                      // to user store
      map.put("indent_store", new String[] { defaultUserStore });
      map.put("indent_store@cast", new String[] { "y" });
    }
    // whenever we open the 'Patient Indent' link, it will display the last 7 days records by
    // default.
    String date_range = req.getParameter("date_range");
    String week_start_date = null;
    if (date_range != null && date_range.equals("week")) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, -7);
      Date expectedDt = cal.getTime();
      week_start_date = dateFormat.format(expectedDt);

      map.put("expected_date", new String[] { week_start_date, "" });
      map.put("expected_date@op", new String[] { "ge,le" });
      map.put("expected_date@cast", new String[] { "y" });
      map.remove("date_range");
    }

    req.setAttribute("pagedList",
        patIndentDAO.searchPatientIndents(map, ConversionUtils.getListingParameter(map)));

    ActionForward forward = new ActionForward(am.findForward("list").getPath());
    // when ever user uses a pagination expected_date should not append again.
    if (date_range != null && date_range.equals("week")
        && req.getParameter("expected_date") == null) {
      addParameter("expected_date", week_start_date, forward);
    }
    return forward;

  }

  public ActionForward closeIndents(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, Exception {

    Map reqMap = req.getParameterMap();
    StoresPatientIndentDAO patIndentDAO = new StoresPatientIndentDAO();

    String[] dispenseStatus = (String[]) reqMap.get("exclude_in_qb_dispense_status");
    String[] indentStatus = (String[]) reqMap.get("exclude_in_qb_indent_status");
    String[] patIndentNO = (String[]) reqMap.get("exclude_in_qb_patient_indent_no");

    if (patIndentNO == null)
      patIndentNO = new String[] {};
    Connection con = null;
    boolean success = true;

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      for (int i = 0; i < patIndentNO.length; i++) {

        Map<String, Object> keys = new HashMap<>();
        keys.put("patient_indent_no", patIndentNO[i]);
        keys.put("dispense_status", dispenseStatus[i]);// search has dispense_status
        keys.put("status", indentStatus[i]);

        success &= patIndentDAO.updateWithName(con, keys, "patient_indent_no") > 0;

      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    ActionRedirect redirect = new ActionRedirect(am.findForward("listRedirect"));
    redirect.addParameter("dispense_status", "O");
    redirect.addParameter("dispense_status", "P");
    return redirect;

  }

}
