package com.insta.hms.followupdashboard;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.exception.HMSException;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class FollowUpDashboardAction.
 */
public class FollowUpDashboardAction extends DispatchAction {

  /**
   * Gets the dash board search details.
   *
   * @param mapping  the mapping
   * @param form        the f
   * @param request  the request
   * @param response the response
   * @return the dash board search details
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getDashBoardSearchDetails(ActionMapping mapping, ActionForm form,
          HttpServletRequest request, HttpServletResponse response) throws Exception {

    Map params = new HashMap(request.getParameterMap());
    String[] followupDate = (String[]) params.get("fud.followup_date");
    String[] regDate = (String[]) params.get("pr.reg_date");
    DateUtil dateUtil = new DateUtil();

    if (followupDate[0].equals("today") && followupDate[1].equals("tomorrow")) {
      Calendar cal = Calendar.getInstance();
      DateUtil.dateTrunc(cal);
      String fromDate = dateUtil.getDateFormatter().format(cal.getTime());
      cal.add(Calendar.DATE, 1);
      String toDate = dateUtil.getDateFormatter().format(cal.getTime());
      params.put("fud.followup_date", new String[] {fromDate, toDate});
      params.put("fud.followup_date@type", new String[] {"date"});
      params.put("fud.followup_date@op", new String[] {"ge,le"});
    } else {  
      try {
        if (followupDate[0] != null && followupDate[0] != "") {
          Date date1 = DateUtil.parseDate(followupDate[0]);
          Calendar cal1 = Calendar.getInstance();
          cal1.setTime(date1);
          int month1 = cal1.get(Calendar.MONTH);
          int day1 = cal1.get(Calendar.DAY_OF_MONTH);
          int year1 = cal1.get(Calendar.YEAR);
          if (year1 > 9999 || month1 > 12 || day1 > 31) {
            throw new HMSException("ui.error.date.invalid");
          }
        }
        if (followupDate[1] != null && followupDate[1] != "") {
          Date date2 = DateUtil.parseDate(followupDate[1]);
          Calendar cal2 = Calendar.getInstance();
          cal2.setTime(date2);
          int month2 = cal2.get(Calendar.MONTH);
          int day2 = cal2.get(Calendar.DAY_OF_MONTH);
          int year2 = cal2.get(Calendar.YEAR);
          
          if (year2 > 9999 || month2 > 12 || day2 > 31) {
            throw new HMSException("ui.error.date.invalid");
          }
        }
      } catch (Exception exc) {
        throw new HMSException("ui.error.date.invalid");
      }
    }

    if (regDate[0].equals("today") && followupDate[1].equals("tomorrow")) {
      Calendar cal = Calendar.getInstance();
      DateUtil.dateTrunc(cal);
      String fromDate = dateUtil.getDateFormatter().format(cal.getTime());
      cal.add(Calendar.DATE, 1);
      String toDate = dateUtil.getDateFormatter().format(cal.getTime());
      params.put("pr.reg_date", new String[] {fromDate, toDate});
      params.put("pr.reg_date@type", new String[] {"date"});
      params.put("pr.reg_date@op", new String[] {"ge,le"});
    } else {
      try {
        if (regDate[0] != null && regDate[0] != "") {
          Date date1 = DateUtil.parseDate(regDate[0]);
          Calendar cal1 = Calendar.getInstance();
          cal1.setTime(date1);
          int month1 = cal1.get(Calendar.MONTH);
          int day1 = cal1.get(Calendar.DAY_OF_MONTH);
          int year1 = cal1.get(Calendar.YEAR);
          if (year1 > 9999 || month1 > 12 || day1 > 31) {
            throw new HMSException("ui.error.date.invalid");
          }
        }
        if (regDate[1] != null && regDate[1] != "") {
          Date date2 = DateUtil.parseDate(regDate[1]);
          Calendar cal2 = Calendar.getInstance();
          cal2.setTime(date2);
          int month2 = cal2.get(Calendar.MONTH);
          int day2 = cal2.get(Calendar.DAY_OF_MONTH);
          int year2 = cal2.get(Calendar.YEAR);

          if (year2 > 9999 || month2 > 12 || day2 > 31) {
            throw new HMSException("ui.error.date.invalid");
          }
        }
      } catch (Exception exc) {
        throw new HMSException("ui.error.date.invalid");
      }
    }

    int sortOrder = 0;
    Map<LISTING, Object> listingParams =
         ConversionUtils.getListingParameter(request.getParameterMap());
    String formSort = (String) listingParams.get(LISTING.SORTCOL);
    Boolean sortReverse = (Boolean) listingParams.get(LISTING.SORTASC);
    int pageNum = (Integer) listingParams.get(LISTING.PAGENUM);
    int pageSize = (Integer) listingParams.get(LISTING.PAGESIZE);

    if (formSort != null) {
      if (formSort.equals("mrno")) {
        sortOrder = FollowUpDashboardDAO.FIELD_MRNO;
      }
    }

    PagedList list = FollowUpDashboardDAO.getFollowUpDetails(params, sortOrder, sortReverse,
            pageSize, pageNum);
    request.setAttribute("pagedList", list);

    return mapping.findForward("getDashBoardScreen");
  }
}
