package com.insta.hms.messaging;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.batchjob.PractoMessageStatusUpdateJobUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.modules.ModulesDAO;
import com.insta.hms.usermanager.UserDAO;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** The Class MessageLogAction. */
public class MessageLogAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(MessageLogAction.class);

  /** The Constant messageLogFields. */
  private static final String messageLogFields = "SELECT * ";

  /** The Constant messageLogCount. */
  private static final String messageLogCount = "SELECT count(*) ";

  /** The Constant messageLogWhere. */
  private static final String messageLogWhere = " WHERE rank = 1 ";
  
  private static final String messagePatientConfidentialityCheck = "AND CASE WHEN "
      + "( pd.mr_no IS NOT NULL AND pd.mr_no != '') "
      + "THEN ( patient_confidentiality_check(pd.patient_group, pd.mr_no)) "
      + "ELSE true END ";

  /** The Constant messageLogTables. */
  private static final String messageLogTables =
      " from (SELECT ml.message_type_id, "
          + "ml.message_log_id, ml.message_subject , "
          + "mc.message_category_id, mt.category_id, ml.message_sender_id, "
          + "mt.message_type_name, mc.message_category_name, ml.message_mode, ml.message_to, "
          + "ml.last_sent_date, ml.last_status, ml.last_status_message, ml.retry_count, "
          + "r.message_status, pd.mr_no,pd.patient_group,"
          + "r.message_recipient_id ,rank() over (PARTITION BY r.message_log_id) as rank, "
          + "count(ml.message_log_id) OVER (PARTITION BY ml.message_log_id) as num_recipients "
          + "FROM message_log ml "
          + "LEFT OUTER JOIN message_types mt ON mt.message_type_id = ml.message_type_id "
          + "LEFT OUTER JOIN message_category mc ON mc.message_category_id = mt.category_id "
          + "LEFT JOIN message_recipient r on r.message_log_id = ml.message_log_id "
          + "LEFT JOIN patient_details pd ON (pd.mr_no = r.message_recipient_id) "
          + " where ml.last_status <> 'D' ";

  /** The Constant myMessage. */
  private static final String myMessage = " AND ml.message_mode = 'NOTIFICATION' AND "
      + "coalesce(ml.notification_status, '') "
      + "not in ('D', 'A') "
      + "AND ml.message_type_id != 'notification_diag_report_signed_off' "
      + "AND ml.message_sender_id = ";

  /** The Constant myMessageSent. */
  private static final String myMessageSent = " AND ml.message_mode = 'NOTIFICATION' "
      + "AND coalesce(ml.notification_status, '') "
      + "not in ('D', 'A') "
      + "AND ml.message_type_id != 'notification_diag_report_signed_off' "
      + "AND r.message_recipient_id = ";

  /** The Constant archiveMessage. */
  private static final String archiveMessage = " AND ml.notification_status = 'A' "
      + "AND ml.message_type_id != 'notification_diag_report_signed_off' "
      + "AND r.message_recipient_id = ";

  /** The Constant messageLogTablesAs. */
  private static final String messageLogTablesAs = ") as foo ";

  /**
   * add parameter method.
   *
   * @param key String key value
   * @param value String value
   * @param forward String action forward
   */
  public void addParameter(String key, String value, ActionForward forward) {
    StringBuffer sb = new StringBuffer(forward.getPath());
    if (key == null || key.length() < 1) {
      return;
    }
    if (forward.getPath().indexOf('?') == -1) {
      sb.append('?');
    } else {
      sb.append('&');
    }
    sb.append(key + "=" + value);
    forward.setPath(sb.toString());
  }

  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws Exception {

    String accessType = mapping.getProperty("access_type");
    HttpSession session = request.getSession();

    Connection con = null;
    PagedList pagedList = null;
    SearchQueryBuilder messageListBuilder = null;
    String dateRange = null;
    java.sql.Date endDate = null;
    java.sql.Date startDate = null;
    Map filterMap = null;
    try {
      con = DataBaseUtil.getConnection();
      String lastExec = "";
      Object lastExecution =
          new GenericDAO("cron_details")
              .findByKey("cron_name", "PractoMessageStatusUpdateJob")
              .get("last_exec_time");
      if (lastExecution != null) {
        lastExec = lastExecution.toString();
        lastExec = lastExec.substring(0, 16);
      }
      request.setAttribute("lastRunTime", lastExec);
      request.setAttribute(
          "practoSmsModule", new ModulesDAO().findByKey("module_id", "mod_practo_sms"));
      Map listingParams = ConversionUtils.getListingParameter(request.getParameterMap());

      StringBuilder queryBuilder = new StringBuilder(messageLogTables);
      filterMap = request.getParameterMap();
      Map newFilterMap = new HashMap(filterMap);
      dateRange =
          filterMap.get("date_range") != null ? ((String[]) filterMap.get("date_range"))[0] : null;

      if (dateRange != null && dateRange.equals("week")) {
        Timestamp timestamp = DateUtil.getCurrentTimestamp();
        endDate = DateUtil.getDatePart(timestamp);
        startDate = DateUtil.getDatePart(DateUtil.addDays(timestamp, -7));

        queryBuilder.append(
            " AND last_sent_date::date >= '"
                + startDate
                + "' AND last_sent_date::date <= '"
                + endDate
                + "'");

        newFilterMap.remove("last_sent_date");
        newFilterMap.remove("last_sent_date@op");
        newFilterMap.remove("last_sent_date@cast");
        newFilterMap.remove("last_sent_date@type");
        newFilterMap.remove("date_range");
      } else {
        if (filterMap.get("message_type_id") != null
            && ((String[]) filterMap.get("message_type_id")).length > 0
            && ((String[]) filterMap.get("message_type_id"))[0] != "") {
          String messageTypeId = ((String[]) filterMap.get("message_type_id"))[0];
          queryBuilder.append(" AND mt.message_type_id='" + messageTypeId + "'");
          newFilterMap.remove("message_type_id");
        }
        if (filterMap.get("last_status") != null
            && ((String[]) filterMap.get("last_status")).length > 0
            && ((String[]) filterMap.get("last_status"))[0] != "") {
          String lastStatus = ((String[]) filterMap.get("last_status"))[0];
          queryBuilder.append(" AND ml.last_status='" + lastStatus + "'");
          newFilterMap.remove("last_status");
        }
        if (filterMap.get("category_id") != null
            && ((String[]) filterMap.get("category_id")).length > 0
            && ((String[]) filterMap.get("category_id"))[0] != "") {
          String categoryId = ((String[]) filterMap.get("category_id"))[0];
          queryBuilder.append(" AND mt.category_id=" + categoryId);
          newFilterMap.remove("category_id");
        }
        if (filterMap.get("message_sender_id") != null
            && ((String[]) filterMap.get("message_sender_id")).length > 0
            && ((String[]) filterMap.get("message_sender_id"))[0] != "") {
          String messageSenderId = ((String[]) filterMap.get("message_sender_id"))[0];
          queryBuilder.append(" AND ml.message_sender_id='" + messageSenderId + "'");
          newFilterMap.remove("message_sender_id");
        }
        if (filterMap.get("message_recipient_id") != null
            && ((String[]) filterMap.get("message_recipient_id")).length > 0
            && ((String[]) filterMap.get("message_recipient_id"))[0] != "") {
          String messageRecipientId = ((String[]) filterMap.get("message_recipient_id"))[0];
          queryBuilder.append(" AND r.message_recipient_id='" + messageRecipientId + "'");
          newFilterMap.remove("message_recipient_id");
        }
        if (filterMap.get("message_mode") != null
            && ((String[]) filterMap.get("message_mode")).length > 0
            && ((String[]) filterMap.get("message_mode"))[0] != "") {
          String messageMode = ((String[]) filterMap.get("message_mode"))[0];
          queryBuilder.append(" AND ml.message_mode='" + messageMode + "'");
          newFilterMap.remove("message_mode");
        }
        if (filterMap.get("last_sent_date") != null
            && ((String[]) filterMap.get("last_sent_date")).length > 0
            && ((String[]) filterMap.get("last_sent_date"))[0] != ""
            && ((String[]) filterMap.get("last_sent_date"))[1] != "") {
          startDate = DateUtil.parseDate(((String[]) filterMap.get("last_sent_date"))[0]);
          endDate = DateUtil.parseDate(((String[]) filterMap.get("last_sent_date"))[1]);
          queryBuilder.append(
              " AND last_sent_date::date >= '"
                  + startDate
                  + "' AND last_sent_date::date <= '"
                  + endDate
                  + "'");
          newFilterMap.remove("last_sent_date");
          newFilterMap.remove("last_sent_date@op");
          newFilterMap.remove("last_sent_date@cast");
          newFilterMap.remove("last_sent_date@type");
        }
      }

      if (accessType.equalsIgnoreCase("owner")) {
        messageListBuilder =
            new SearchQueryBuilder(
                con,
                messageLogFields,
                messageLogCount,
                (queryBuilder
                    + myMessage
                    + " '"
                    + (String) session.getAttribute("userid")
                    + "' "
                    + messagePatientConfidentialityCheck
                    + messageLogTablesAs),
                messageLogWhere,
                listingParams);
      } else if (accessType.equalsIgnoreCase("archive_msg")) {
        messageListBuilder =
            new SearchQueryBuilder(
                con,
                messageLogFields,
                messageLogCount,
                (queryBuilder
                    + archiveMessage
                    + " '"
                    + (String) session.getAttribute("userid")
                    + "' "
                    + messagePatientConfidentialityCheck
                    + messageLogTablesAs),
                messageLogWhere,
                listingParams);
      } else if (accessType.equalsIgnoreCase("owner_notification")) {
        messageListBuilder =
            new SearchQueryBuilder(
                con,
                messageLogFields,
                messageLogCount,
                (queryBuilder
                    + myMessageSent
                    + " '"
                    + (String) session.getAttribute("userid")
                    + "' "
                    + messagePatientConfidentialityCheck
                    + messageLogTablesAs),
                messageLogWhere,
                listingParams);
      } else {
        messageListBuilder =
            new SearchQueryBuilder(
                con,
                messageLogFields,
                messageLogCount,
                (queryBuilder + messagePatientConfidentialityCheck + messageLogTablesAs),
                messageLogWhere,
                listingParams);
      }

      messageListBuilder.addFilterFromParamMap(newFilterMap);
      messageListBuilder.build();

      pagedList = messageListBuilder.getDynaPagedList();

      List<BasicDynaBean> doctorsNameEmpNameList = null;
      doctorsNameEmpNameList = new UserDAO().getDoctorsNameEmpName();
      request.setAttribute(
          "nameResourceNameList",
          new JSONSerializer()
              .serialize(ConversionUtils.listBeanToListMap(doctorsNameEmpNameList)));

      List<BasicDynaBean> dispatcherList = getDispatcherList(null);
      Map dispatcherMap = ConversionUtils.listBeanToMapBean(dispatcherList, "message_mode");
      request.setAttribute("dispatcherMap", dispatcherMap);

    } finally {

      if (null != messageListBuilder) {
        messageListBuilder.close();
      }

      if (null != con) {
        con.close();
      }
    }
    request.setAttribute("pagedList", pagedList);

    ActionForward forward = new ActionForward(mapping.findForward("list").getPath());

    if (dateRange != null && dateRange.equals("week")) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
      addParameter("last_sent_date", dateFormat.format(startDate), forward);
    }

    return forward;
  }

  /**
   * Show.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward show(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws Exception {
    String userId = (String) request.getSession(false).getAttribute("userId");
    String messageId = request.getParameter("message_log_id");
    String accessType = mapping.getProperty("access_type");
    if (null != messageId) {
      GenericDAO dao = new GenericDAO("message_log");
      BasicDynaBean bean = dao.findByKey("message_log_id", Integer.parseInt(messageId.toString()));
      request.setAttribute("messageLog", bean);

      GenericDAO msgRecipientdao = new GenericDAO("message_recipient");
      BasicDynaBean msgRecipientbean =
          msgRecipientdao.findByKey("message_log_id", Integer.parseInt(messageId.toString()));
      request.setAttribute("messageRecipientId", msgRecipientbean);

      if (null != msgRecipientbean) {
        String messageRecipient = (String) msgRecipientbean.get("message_recipient_id");
        // Access type is checking because of message log
        if (accessType.equalsIgnoreCase("owner")
            || accessType.equalsIgnoreCase("owner_notification")
            || accessType.equalsIgnoreCase("archive_msg")) {
          if (null != msgRecipientbean && userId.equalsIgnoreCase(messageRecipient.trim())) {
            // notification_status
            String msgStatus = (String) msgRecipientbean.get("message_status");
            if (!msgStatus.equalsIgnoreCase("R")) {
              Connection con = null;
              try {
                con = DataBaseUtil.getConnection();
                msgRecipientbean.set("message_status", "R");
                msgRecipientdao.update(
                    con,
                    msgRecipientbean.getMap(),
                    "message_log_id",
                    Integer.parseInt(messageId.toString()));
                // Getting Unread notification count
                HttpSession session = request.getSession();
                int count = Integer.parseInt((String) session.getAttribute("count"));
                count = count - 1;
                session.removeAttribute("count");
                session.setAttribute("count", Integer.toString(count));
              } finally {
                DataBaseUtil.closeConnections(con, null);
              }
            }
          }
        }
      }
    }

    List<BasicDynaBean> dispatcherList = getDispatcherList(null);
    Map dispatcherMap = ConversionUtils.listBeanToMapBean(dispatcherList, "message_mode");
    request.setAttribute("dispatcherMap", dispatcherMap);

    if (accessType.equalsIgnoreCase("owner")
        || accessType.equalsIgnoreCase("owner_notification")
        || accessType.equalsIgnoreCase("archive_msg")) {
      GenericDAO messageActionDao = new GenericDAO("message_actions");
      List<BasicDynaBean> myMsgLists = messageActionDao.listAll("message_action_id");
      request.setAttribute("myMsgLists", myMsgLists);

      if (null != messageId) {
        MessageActionService actionService = new MessageActionService();
        Map actionStatusMap =
            actionService.getActionStatusMap(Integer.parseInt(messageId.toString()), userId);
        request.setAttribute("actionStatusMap", actionStatusMap);
      }
    }
    return mapping.findForward("show");
  }

  /**
   * Resend.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward resend(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws Exception {

    String messageLogId = request.getParameter("message_log_id");

    Map overrides = new HashMap();
    String[] fields = new String[] {"message_sender", "message_to", "message_cc", "message_bcc"};

    ConversionUtils.copyStringFields(request.getParameterMap(), overrides, fields, null);
    FlashScope flash = FlashScope.getScope(request);
    boolean success = false;
    if (null != messageLogId) {
      MessageManager mgr = new MessageManager();
      success = mgr.resendMessage(Integer.parseInt(messageLogId), overrides);
    }

    ActionRedirect redirect = null;
    if (success) {
      redirect = new ActionRedirect(mapping.findForward("listRedirect"));
      flash.success("Message sent successfully");
    } else {
      redirect = new ActionRedirect(mapping.findForward("showRedirect"));
      redirect.addParameter("message_log_id", messageLogId);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      flash.error("Message could not be sent");
    }
    return redirect;
  }

  /**
   * Gets the dispatcher list.
   *
   * @param messageMode the message mode
   * @return the dispatcher list
   * @throws SQLException the SQL exception
   */
  private List<BasicDynaBean> getDispatcherList(String messageMode) throws SQLException {

    List<String> dispatcherColumns = new ArrayList<String>();
    dispatcherColumns.add("message_mode");
    dispatcherColumns.add("display_name");

    Map filterMap = new HashMap();

    if (null != messageMode && messageMode.trim().length() > 0) {
      filterMap.put("message_mode", messageMode);
    }

    GenericDAO dispatcherDao = new GenericDAO("message_dispatcher_config");
    List<BasicDynaBean> dispatcherList =
        dispatcherDao.listAll(dispatcherColumns, filterMap, "display_name");
    logger.debug("getDispatcherList....size" + dispatcherList.size());

    return dispatcherList;
  }

  /**
   * Gets the message status.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param res the res
   * @return the message status
   */
  @IgnoreConfidentialFilters
  public ActionForward getMessageStatus(
      ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse res) {
    Connection con = null;
    try {
      String startDate = null;
      Date date = null;
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      con = DataBaseUtil.getConnection();
      String schema = RequestContext.getSchema();
      Object lastExec =
          new GenericDAO("cron_details")
              .findByKey("cron_name", "PractoMessageStatusUpdateJob")
              .get("last_exec_time");

      if (lastExec == null) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        startDate = DateUtil.formatIso8601Timestamp(cal.getTime());

      } else {
        String lastExecTime = lastExec.toString();
        date = dateFormat.parse(lastExecTime);
        startDate = DateUtil.formatIso8601Timestamp(date);
      }
      PractoMessageStatusUpdateJobUtil.updateMessageStatus(schema, startDate);
    } catch (Exception ex) {
      logger.error("Exception at messageLogAction occured", ex);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }

    return null;
  }
}
