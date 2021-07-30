package com.insta.hms.core.fa;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class ScheduleAccountingForBillAction. This class is useful for pushing the accounting job
 * for a particular bill. or for a date range Example Usage :
 * http://localhost:8080/instahms/accounting/processaccountingforbill.do?billNo=BC18000106 OR
 * http://
 * localhost:8080/instahms/accounting/processaccountingforbill.do?billNo=BC18000106&startDate=
 * 01-04-2019&endDate=02-04-2019
 */
public class ScheduleAccountingForBillAction extends Action {

  /** The accounting job scheduler. */
  static AccountingJobScheduler accountingJobScheduler = ApplicationContextProvider
      .getBean(AccountingJobScheduler.class);

  /**
   * Execute.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws ParseException the parse exception
   * @throws SQLException   the SQL exception
   * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
   *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @IgnoreConfidentialFilters
  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws ParseException, SQLException {
    String billNo = request.getParameter("billNo");
    String startDateStr = request.getParameter("startDate");
    String endDateStr = request.getParameter("endDate");

    if (billNo != null) {
      scheduleAccountingForBills(billNo);
    }

    if (startDateStr != null && endDateStr != null) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
      Date startDate = atStartOfDay(dateFormat.parse(startDateStr));
      Date endDate = atEndOfDay(dateFormat.parse(endDateStr));
      scheduleAccountingForBillsDateRange(new java.sql.Timestamp(startDate.getTime()),
          new java.sql.Timestamp(endDate.getTime()));
    }
    return null;
  }

  /** The Constant GET_BILL. */
  private static final String GET_BILL = " SELECT * FROM bill WHERE bill_no=? ";

  /**
   * Schedule accounting for bills.
   *
   * @param billNo the bill no
   */
  private void scheduleAccountingForBills(String billNo) {
    List<BasicDynaBean> billsList = DatabaseHelper.queryToDynaList(GET_BILL, billNo);
    if (billsList != null && !billsList.isEmpty()) {
      accountingJobScheduler.scheduleAccountingForBills(billsList);
    }
  }

  /** The Constant GET_BILLS_DATE_RANGE. */
  private static final String GET_BILLS_DATE_RANGE = " SELECT * FROM bill "
      + " WHERE open_date >= ? AND open_date <= ? ";

  /**
   * Schedule accounting for bills date range.
   *
   * @param startDate the start date
   * @param endDate   the end date
   * @throws SQLException the SQL exception
   */
  private void scheduleAccountingForBillsDateRange(java.sql.Timestamp startDate,
      java.sql.Timestamp endDate) throws SQLException {
    List<BasicDynaBean> billsList = DataBaseUtil.queryToDynaList(GET_BILLS_DATE_RANGE,
        new Object[] { startDate, endDate });
    if (billsList != null && !billsList.isEmpty()) {
      accountingJobScheduler.scheduleAccountingForBills(billsList);
    }
  }

  /**
   * At end of day.
   *
   * @param date the date
   * @return the date
   */
  private Date atEndOfDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);
    calendar.set(Calendar.MILLISECOND, 999);
    return calendar.getTime();
  }

  /**
   * At start of day.
   *
   * @param date the date
   * @return the date
   */
  private Date atStartOfDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }
}
