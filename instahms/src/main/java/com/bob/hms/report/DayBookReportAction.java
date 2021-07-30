package com.bob.hms.report;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.JobSchedulingService;
import com.insta.hms.common.PrintPageOptions;
import com.insta.hms.common.ReportJob;
import com.insta.hms.jobs.JobService;
import com.insta.hms.master.CounterMaster.CounterMasterDAO;
import com.insta.hms.mdm.usercentercounters.UserBillingCenterCounterMappingService;
import com.insta.hms.usermanager.Role;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class DayBookReportAction.
 */
public class DayBookReportAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(DayBookReportAction.class);

  /** The job service. */
  private static JobService jobService = JobSchedulingService.getJobService();

  /** The redis template. */
  private static RedisTemplate<String, Object> redisTemplate = 
      (RedisTemplate) ApplicationContextProvider
      .getApplicationContext().getBean("redisTemplate");
  
  private static UserBillingCenterCounterMappingService counterMappingService = 
      ApplicationContextProvider.getBean(UserBillingCenterCounterMappingService.class);

  /** The counter dao. */
  private static CounterMasterDAO counterDao = new CounterMasterDAO();

  /**
   * Gets the daybook.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the daybook
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward getDaybook(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {

    Connection con = null;
    String page = request.getParameter("status");
    HttpSession session = request.getSession();
    String userid = (String) session.getAttribute("userid");
    String name = null;
    if (page.equalsIgnoreCase("daybook")) {
      name = "Day Book";
    } else {
      name = "Lab Day Book";
    }

    HashMap actionRightsMap = new HashMap();
    Role role = new Role();
    int roleId;
    actionRightsMap = (HashMap) request.getSession(false).getAttribute("actionRightsMap");
    String actionRightStatus = (String) actionRightsMap.get(role.USR_COUNTER_DAY_BOOK_ACCESS);
    roleId = (Integer) request.getSession(false).getAttribute("roleId");
    try {
      con = DataBaseUtil.getConnection();
      if (roleId == 1 || roleId == 2 || actionRightStatus.equals("A")) {
        request.setAttribute("counterList", counterDao.getAllCounters(con));
        request.setAttribute("users", counterDao.getAllHospitalUsers(con));
      } else {
        GenericDAO userDao = new GenericDAO("u_user");
        //String billingCounterId = userDao.findByKey("emp_username", userid).get("counter_id")
        //    .toString();
        Integer loggedInCenterId = RequestContext.getCenterId();
        BasicDynaBean counterMappedBean = counterMappingService.getMappedCounterForCenter(userid,
            loggedInCenterId);
        String billingCounterId = "";
        if (null != counterMappedBean) {
          billingCounterId = (String) counterMappedBean.get("counter_id");
        }
        String pharmacyCounterId = userDao.findByKey("emp_username", userid)
            .get("pharmacy_counter_id").toString();
        String counterId = billingCounterId.equals("")
            ? (pharmacyCounterId.equals("") ? "" : pharmacyCounterId) : billingCounterId;

        String counterName;
        if (counterId.equals("")) {
          counterName = "";
        } else {
          counterName = counterDao.findByKey("counter_id", counterId).get("counter_no").toString();
        }

        request.setAttribute("counter_name", counterName);
        request.setAttribute("counter_id", counterId);
        request.setAttribute("user_name", userid);
      }

    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    request.setAttribute("names", name);
    return mapping.findForward("daybook");
  }

  /** The Constant daybooksub. */
  public static final String daybooksub = "DaybookSummary";

  /**
   * Show report.
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
   * @throws Exception
   *           the exception
   */
  public ActionForward showReport(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    HashMap hm = new HashMap();
    String page = request.getParameter("page").trim();
    String cdate = request.getParameter("day");

    HashMap actionRightsMap = new HashMap();
    Role role = new Role();
    actionRightsMap = (HashMap) request.getSession(false).getAttribute("actionRightsMap");
    String actionRightStatus = (String) actionRightsMap.get(role.USR_COUNTER_DAY_BOOK_ACCESS);
    String userName;
    HttpSession session = request.getSession();
    String userid = (String) session.getAttribute("userid");
    int roleId;
    roleId = (Integer) request.getSession(false).getAttribute("roleId");
    int centerId = Integer.parseInt(request.getParameter("centerFilter"));

    hm.put("centers", " AND  (" + centerId + "=0 OR center_id = " + centerId + ")  ");

    if (roleId == 1 || roleId == 2 || actionRightStatus.equals("A")) {
      userName = request.getParameter("user");
    } else {
      userName = userid;
    }

    if (userName.equals("all")) {
      userName = "";
    } else {
      userName = " and username= '" + userName + "'";
    }

    String paymentMode = request.getParameter("paymentMode");
    String counterid = request.getParameter("counterid");

    Boolean allCounters = false;
    if (counterid.equals("all")) {
      allCounters = true;
      Connection con = DataBaseUtil.getConnection();
      try {
        List<BasicDynaBean> counters = counterDao.getAllCounters(con);
        counterid = " AND counter in (";
        boolean first = true;
        for (BasicDynaBean bean : counters) {
          if (!first) {
            counterid += ",";
          }
          counterid += "'" + bean.get("counter_id") + "'";
          first = false;
        }
        counterid += ") ";
      } finally {
        DataBaseUtil.closeConnections(con, null);
      }
    } else {
      counterid = " and counter ='" + counterid + "' ";
    }

    if (paymentMode.equals("all")) {
      paymentMode = "";
    } else {
      paymentMode = " and payment_mode ='" + paymentMode + "' ";
    }

    String bills = null;
    String billFrom = request.getParameter("billFrom");
    String billTo = request.getParameter("billTo");

    if (!billFrom.equals("") && !billTo.equals("")) {
      bills = " AND (bill_no  >= '" + billFrom + "' AND  bill_no <= '" + billTo + "' )";
    } else if (!billFrom.equals("") && billTo.equals("")) {
      bills = " AND (bill_no >= '" + billFrom + "') ";
    } else if (!billTo.equals("") && billFrom.equals("")) {
      bills = "AND (bill_no <= '" + billTo + "') ";
    } else {
      bills = "";
    }

    String receipts = null;
    String receiptFrom = request.getParameter("receiptFrom");
    String receiptTo = request.getParameter("receiptTo");

    if (!receiptFrom.equals("") && !receiptTo.equals("")) {
      receipts = " AND (receipt_no  >= '" + receiptFrom + "' AND  receipt_no <= '" + receiptTo
          + "') ";
    } else if (!receiptFrom.equals("") && receiptTo.equals("")) {
      receipts = " AND (receipt_no >= '" + receiptFrom + "') ";
    } else if (!receiptTo.equals("") && receiptFrom.equals("")) {
      receipts = "AND (receipt_no <= '" + receiptTo + "')";
    } else {
      receipts = "";
    }

    String visitTypes = request.getParameter("visitType");
    String visitType = null;
    if (visitTypes.equals("all")) {
      visitType = " ";
    } else if (visitTypes.equals("others")) {
      visitType = " and visitType not in ('i','o') ";
    } else {
      visitType = " and visittype in (" + visitTypes + ")";
    }

    String fromDate = request.getParameter("fromDate");
    String toDate = request.getParameter("toDate");
    String fromTime = request.getParameter("fromTime");
    String toTime = request.getParameter("toTime");

    hm.put("fromDateTime", DateUtil.parseTimestamp(fromDate, fromTime));
    hm.put("toDateTime", DateUtil.parseTimestamp(toDate, toTime));
    hm.put("countername", counterid);
    hm.put("visitType", visitType);
    hm.put("userName", userName);
    hm.put("bills", bills);
    hm.put("receipts", receipts);
    hm.put("paymentMode", paymentMode);
    hm.put("allCounters", allCounters);

    String printerMode = request.getParameter("printerType");

    Map<String, Object> jobData = new HashMap<String, Object>();
    jobData.put("schema", RequestContext.getSchema());
    jobData.put("userName", RequestContext.getUserName());
    jobData.put("centerId", RequestContext.getCenterId());
    jobData.put("outputMode", printerMode);
    jobData.put("hm", hm);
    jobData.put("rootRealPath", AppInit.getRootRealPath());
    jobData.put("reportAction", daybooksub);

    String redisKey = String.format("schema:%s;user:%s;uid:%s", jobData.get("schema").toString(),
        jobData.get("userName").toString(), System.currentTimeMillis());

    jobData.put("redisKey", redisKey);

    // putting status in redis
    String redisValue = "status:queued;fileName:" + "HospitalDayBook";
    redisTemplate.opsForValue().set(redisKey, redisValue);
    redisTemplate.expire(redisKey, 24, TimeUnit.HOURS); // setting expiry time to 1 day

    if (printerMode != null && printerMode.equals("text")) {
      PrintPageOptions opts = new PrintPageOptions(10, 10, 842, "Y");
      opts.pageWidth = 842;
      opts.charWidth = 6;
      jobData.put("opts", opts);
    }
    jobService.scheduleImmediate(
        buildJob("ReportJob_" + System.currentTimeMillis(), ReportJob.class, jobData));

    String url = "/reportdashboard/download.htm?id=" + URLEncoder.encode(redisKey, "UTF-8");
    ActionRedirect redirect = new ActionRedirect(url);
    redirect.setPath(url);
    return redirect;
  }

  /**
   * Gets the text.
   *
   * @param mapping
   *          the m
   * @param form
   *          the f
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the text
   * @throws Exception
   *           the exception
   */
  public ActionForward getText(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    return showReport(mapping, form, req, res);
  }

  /**
   * Gets the param default.
   *
   * @param req
   *          the req
   * @param paramName
   *          the param name
   * @param defaultValue
   *          the default value
   * @return the param default
   */
  private String getParamDefault(HttpServletRequest req, String paramName, String defaultValue) {
    String value = req.getParameter(paramName);
    if ((value == null) || value.equals("")) {
      value = defaultValue;
    }
    return value;
  }

}
