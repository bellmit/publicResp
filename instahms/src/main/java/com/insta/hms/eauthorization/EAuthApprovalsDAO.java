/**
 *
 */

package com.insta.hms.eauthorization;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.core.billing.BillChargeClaimService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsService;
import com.insta.hms.eauthorization.priorauth.PriorAuthAuthorization;
import com.insta.hms.eauthorization.priorauth.PriorAuthorization;
import com.insta.hms.eauthorization.priorauth.PriorAuthorizationActivity;
import com.insta.hms.eauthorization.priorauth.PriorAuthorizationHeader;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class EAuthApprovalsDAO.
 *
 * @author lakshmi
 */
public class EAuthApprovalsDAO {

  /**
   * The logger.
   */
  static Logger logger = LoggerFactory.getLogger(EAuthApprovalsDAO.class);
  

  private static final BillChargeClaimService billChargeClaimService = ApplicationContextProvider
      .getBean(BillChargeClaimService.class);

  private static final BillChargeService billChargeService = ApplicationContextProvider
      .getBean(BillChargeService.class);
  
  private static final PendingPrescriptionsService pendingPrescriptionService =
      ApplicationContextProvider.getBean(PendingPrescriptionsService.class);

  /**
   * Gets the approval bean.
   *
   * @param preauthPrescId the preauth presc id
   * @return the approval bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getApprovalBean(int preauthPrescId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(
          " SELECT prmp.preauth_presc_id, prd.* "
              + " FROM preauth_prescription prmp  "
              + " LEFT JOIN preauth_request_approval_details prd on (prd.preauth_request_id = "
              + "prmp.preauth_request_id) "
              + " WHERE prmp.preauth_presc_id=?");
      ps.setInt(1, preauthPrescId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }


  /**
   * The Constant SELECT_EAUTH_APPROVAL_FIELDS.
   */
  private static final String SELECT_EAUTH_APPROVAL_FIELDS = "SELECT * ";

  /**
   * The Constant SELECT_EAUTH_APPROVAL_COUNT.
   */
  private static final String SELECT_EAUTH_APPROVAL_COUNT = " SELECT count(*) ";

  /**
   * The Constant SELECT_EAUTH_APPROVAL_TABLES.
   */
  private static final String SELECT_EAUTH_APPROVAL_TABLES = " FROM (SELECT "
      + " sm.salutation || ' ' || patient_name || case when coalesce(middle_name, '') = '' "
      + " then '' else (' ' || middle_name) end || case when coalesce(last_name, '') = '' "
      + " then ''  else (' ' || last_name) end as patname,  get_patient_age(dateofbirth, "
      + "expected_dob) as age,"
      + " get_patient_age_in(dateofbirth, expected_dob) as age_in,pd.patient_phone,pd"
      + ".patient_gender, "
      + " pr.mr_no, pr.patient_id, pr.op_type, pr.status as patstatus, pr.center_id, "
      + " CASE WHEN (pr.op_type != 'O') THEN d.doctor_name ELSE ref.referal_name END AS doctor, "
      + " CASE WHEN (pr.visit_type = 'i' OR pr.op_type = 'O') THEN (pr.reg_date + pr.reg_time) "
      + "ELSE dc.visited_date END AS visited_date, "
      + " grp.consultation_id,  prmp.preauth_presc_id, prmp.preauth_status, prmp.resubmit_type, "
      + " icm.insurance_co_id AS primary_insurance_co_id, icms.insurance_co_id AS "
      + "secondary_insurance_co_id, "
      + " icm.insurance_co_name AS primary_insurance_co_name, icms.insurance_co_name AS "
      + "secondary_insurance_co_name, "
      + " tp.tpa_id AS primary_tpa_id, tps.tpa_id AS secondary_tpa_id, tp.tpa_name AS "
      + "primary_tpa_name, tps.tpa_name AS secondary_tpa_name,"
      + " pm.plan_id AS primary_plan_id, pms.plan_id AS secondary_plan_id, pip.plan_type_id :: "
      + "text AS primary_category_id,"
      + " pips.plan_type_id :: text AS secondary_category_id, cat.category_name AS "
      + "primary_category_name, "
      + " cats.category_name AS secondary_category_name, prd.request_date, prmp"
      + ".preauth_request_id, prd.preauth_request_type,"
      + " prd.preauth_id_payer, prd.approval_recd_date, prd.approval_status, prd.file_id, pip"
      + ".priority AS priority" + " FROM "
      + " (SELECT preauth_presc_id, consultation_id, visit_id "
      + " FROM preauth_prescription_activities GROUP by preauth_presc_id, consultation_id, "
      + "visit_id ) as grp "
      + " JOIN patient_registration pr ON (grp.visit_id = pr.patient_id) "
      + " JOIN patient_details pd ON (pr.mr_no = pd.mr_no) "
      + " JOIN preauth_prescription prmp ON (grp.preauth_presc_id = prmp.preauth_presc_id ) "
      + " LEFT JOIN patient_insurance_plans pip ON (prmp.preauth_payer_id = pip.insurance_co AND "
      + "pip.patient_id = pr.patient_id)"
      + " LEFT JOIN patient_insurance_plans pips ON (prmp.preauth_payer_id = pips.insurance_co "
      + "AND pips.patient_id = pr.patient_id) "
      + " LEFT JOIN insurance_plan_main pm ON (pm.plan_id = pip.plan_id) "
      + " LEFT JOIN insurance_plan_main pms ON (pms.plan_id = pips.plan_id) "
      + " LEFT JOIN tpa_master tp ON (tp.tpa_id = pip.sponsor_id) "
      + " LEFT JOIN tpa_master tps ON (tps.tpa_id = pips.sponsor_id) "
      + " LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = pip.insurance_co) "
      + " LEFT JOIN insurance_company_master icms ON (icms.insurance_co_id = pips.insurance_co) "
      + " LEFT JOIN insurance_category_master cat ON (cat.category_id = pip.plan_type_id) "
      + " LEFT JOIN insurance_category_master cats ON (cats.category_id = pips.plan_type_id) "
      + " LEFT JOIN preauth_request_approval_details prd on (prd.preauth_request_id = prmp"
      + ".preauth_request_id) "
      + " LEFT JOIN doctor_consultation dc ON (dc.consultation_id = grp.consultation_id) "
      + " LEFT JOIN doctors d ON (d.doctor_id = pr.doctor) "
      + " LEFT JOIN (SELECT referal_name,referal_no FROM referral UNION "
      + " SELECT doctor_name,doctor_id FROM doctors ) AS ref ON (ref.referal_no = pr"
      + ".reference_docto_id) "
      + " LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id) "
      + " WHERE ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )"
      + " ) as list";

  /**
   * Search E auth approval list.
   *
   * @param filter  the filter
   * @param listing the listing
   * @return the paged list
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public static PagedList searchEAuthApprovalList(Map filter, Map listing)
      throws SQLException, ParseException {

    Connection con = null;
    try {

      con = DataBaseUtil.getReadOnlyConnection();

      Map<Object, Object> temp = new HashMap<Object, Object>();
      temp.putAll(filter);

      String insuranceCoId = null;
      String[] categoryIds = null;
      String tpaId = null;
      String planId = null;

      if (temp.containsKey("insurance_co_id")
          && ((String[]) temp.get("insurance_co_id"))[0] != null) {
        insuranceCoId = ((String[]) temp.get("insurance_co_id"))[0];
        temp.remove("insurance_co_id");
      }
      if (temp.containsKey("category_id")
          && (((String[]) temp.get("category_id")) != null
          && !((String[]) temp.get("category_id"))[0]
          .isEmpty())) {
        categoryIds = ((String[]) temp.get("category_id"));
        temp.remove("category_id");
      }
      if (temp.containsKey("tpa_id")
          && ((String[]) temp.get("tpa_id"))[0] != null) {
        tpaId = ((String[]) temp.get("tpa_id"))[0];
        temp.remove("tpa_id");
      }
      if (filter.containsKey("plan_id")
          && ((String[]) temp.get("plan_id"))[0] != null) {
        planId = ((String[]) temp.get("plan_id"))[0];
        temp.remove("plan_id");
      }

      SearchQueryBuilder qb = new SearchQueryBuilder(con,
          SELECT_EAUTH_APPROVAL_FIELDS, SELECT_EAUTH_APPROVAL_COUNT,
          SELECT_EAUTH_APPROVAL_TABLES, listing);
      qb.addFilterFromParamMap(temp);
      if (insuranceCoId != null && !insuranceCoId.equals("")) {
        qb.appendToQuery(" primary_insurance_co_id = '" + insuranceCoId
            + "' OR secondary_insurance_co_id = '" + insuranceCoId
            + "' ");
      }
      if (categoryIds != null) {
        String catStr = null;
        Boolean first = true;
        for (String categoryId : categoryIds) {
          if (categoryId == null || categoryId.equals("")) {
            continue;
          }
          if (first) {
            catStr = "'" + categoryId + "'";
          } else {
            catStr += ", '" + categoryId + "'";
          }
          first = false;
        }
        qb.appendToQuery(" primary_category_id IN (" + catStr
            + ") OR secondary_category_id IN (" + catStr + ") ");
      }
      if (tpaId != null && !tpaId.equals("")) {
        qb.appendToQuery(" primary_tpa_id = '" + tpaId
            + "' OR secondary_tpa_id = '" + tpaId + "' ");
      }
      if (planId != null && !planId.equals("")) {
        qb.appendToQuery(" primary_plan_id = '" + planId
            + "' OR  secondary_plan_id = '" + planId + "' ");
      }

      int centerId = RequestContext.getCenterId();
      if (centerId != 0) {
        qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=",
            centerId);
      }
      qb.addSecondarySort("preauth_presc_id", true);
      qb.build();
      PagedList list = qb.getMappedPagedList();

      qb.close();
      con.close();

      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * The e auth presc DAO.
   */
  EAuthPrescriptionDAO eauthPrescDAO = new EAuthPrescriptionDAO();

  /**
   * The preauth act DAO.
   */
  EAuthPrescriptionActivitiesDAO preauthActDAO = new EAuthPrescriptionActivitiesDAO();

  /**
   * The preauth req DAO.
   */
  GenericDAO preauthReqDAO = new GenericDAO("preauth_prescription_request");

  /**
   * The preauth req app DAO.
   */
  GenericDAO preauthReqAppDAO = new GenericDAO(
      "preauth_request_approval_details");

  /**
   * The preauth req amt DAO.
   */
  GenericDAO preauthReqAmtDAO = new GenericDAO(
      "preauth_approval_amount_details");

  /**
   * Update E auth approval details.
   *
   * @param desc the desc
   * @return true, if successful
   * @throws SQLException   the SQL exception
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public boolean updateEAuthApprovalDetails(PriorAuthorization desc, String fileId)
      throws SQLException, IOException, ParseException {
    boolean success = true;
    boolean allSuccess = false;

    Connection con = null;

    try {
      approvalTxn:
      {
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);

        PriorAuthorizationHeader header = desc.getHeader();
        String transactionDate = header.getTransactionDate();
        if (transactionDate.trim().length() <= 10) {
          transactionDate = transactionDate + " 00:00";
        }

        PriorAuthAuthorization priorAuth = desc.getAuthorization();

        String authReqID = priorAuth.getAuthorizationID();
        String authIdPayer = priorAuth.getAuthorizationIDPayer();
        String authDenialCode = priorAuth.getDenialCode();

        BasicDynaBean prescReqbean = preauthReqDAO
            .findByKey("preauth_request_id", authReqID);

        Integer eauthPrescId = (Integer) prescReqbean
            .get("preauth_presc_id");
        Map<String, Object> key = new HashMap<String, Object>();
        key.put("preauth_presc_id", eauthPrescId);
        BasicDynaBean bean = eauthPrescDAO
            .findByKey(Arrays.asList("preauth_payer_id"), key);
        String insuranceCoId = (String) bean.get("preauth_payer_id");
        BasicDynaBean eauthBean = eauthPrescDAO
            .getEAuthPresc(eauthPrescId, insuranceCoId);
        String preauthResubmitRequestId = eauthBean
            .get("preauth_resubmit_request_id") != null
            ? (String) eauthBean
            .get("preauth_resubmit_request_id")
            : null;

        // Get the resubmission request id or the request id for saving
        // approval amount details.
        String preauthPrescriptionRequestId = (preauthResubmitRequestId != null
            && !preauthResubmitRequestId.equals(""))
            ? preauthResubmitRequestId
            : authReqID;

        Map keys = new HashMap();
        keys.put("preauth_act_status", "S");
        keys.put("preauth_presc_id", eauthPrescId);
        List<BasicDynaBean> sentActivities = preauthActDAO.listAll(con,
            null, keys, null);
        int sentActivityCount = (sentActivities != null)
            ? sentActivities.size()
            : 0;

        int approvedActivityCount = 0;
        boolean denied = false;

        ArrayList<PriorAuthorizationActivity> activities = priorAuth
            .getActivities();
        /*
         * Insert activity details into preauth approval amount details
         * table.
         */
        for (PriorAuthorizationActivity activity : activities) {

          String type = activity.getActivityType();
          String code = activity.getActivityCode();
          String activityId = activity.getActivityID();
          BigDecimal net = activity.getNet();
          String activityDenialCode = activity
              .getActivityDenialCode();
          BigDecimal quantity = activity.getQuantity();
          BigDecimal list = activity.getList();
          BigDecimal patientShare = activity.getPatientShare();
          BigDecimal paymentAmount = activity.getPaymentAmount();

          String actId = activityId.split("-")[0];
          int preauthActId = Integer.parseInt(actId);

          BasicDynaBean actAmtBean = preauthReqAmtDAO.getBean();
          actAmtBean.set("preauth_request_id",
              preauthPrescriptionRequestId);
          actAmtBean.set("preauth_act_id", preauthActId);
          actAmtBean.set("denial_code", activityDenialCode);
          actAmtBean.set("preauth_id_payer", authIdPayer);
          actAmtBean.set("quantity", quantity);
          actAmtBean.set("net", net);
          actAmtBean.set("list", list);
          actAmtBean.set("patient_share", patientShare);
          actAmtBean.set("payment_amount", paymentAmount);
          patientShare = (patientShare == null)
              ? BigDecimal.valueOf(0.00)
              : patientShare;
          activityDenialCode = (activityDenialCode == null)
              ? authDenialCode
              : activityDenialCode;

          success = preauthReqAmtDAO.insert(con, actAmtBean);
          if (!success) {
            break approvalTxn;
          }

          BasicDynaBean activityBean = preauthActDAO
              .findByKey("preauth_act_id", preauthActId);
          BigDecimal claimNet = (BigDecimal) activityBean
              .get("claim_net_amount");

          /*
           * The denied activities need to be resubmitted. The
           * Approved activitied rate details needs to be saved. No
           * check required if the sent claim amount mismatches with
           * approved claim amount.
           */
          if (activityDenialCode == null
              || activityDenialCode.equals("")) {

            activityBean.set("patient_share", patientShare);
            activityBean.set("claim_net_approved_amount",
                paymentAmount);
            activityBean.set("preauth_act_status", "C");
            
            approvedActivityCount++;

            if (null != net && null != list
                && null != patientShare) {

              BigDecimal actAmt = patientShare.add(paymentAmount);
              BigDecimal actRate = ConversionUtils.setScale(actAmt
                  .divide(quantity, RoundingMode.HALF_UP));
              BigDecimal discount = BigDecimal.ZERO;

              activityBean.set("amount", actAmt);
              activityBean.set("rate", actRate);
              activityBean.set("claim_net_amount", paymentAmount);
              activityBean.set("discount", discount);
            }

          } else {
            denied = true;
            activityBean.set("patient_share", patientShare);
            activityBean.set("claim_net_approved_amount",
                paymentAmount);
            activityBean.set("denial_code", activityDenialCode);
            activityBean.set("preauth_act_status", "D");
          }
          
          if (BigDecimal.ZERO.compareTo(paymentAmount) < 0 && quantity.intValue() > 0) {

            if (denied) {
              // If denied but payment amount > 0 then mark it as approved(partially approved items
              // can get denied as status)
              activityBean.set("preauth_act_status", "C");
            }

            activityBean.set("approved_qty", quantity.intValue());
            Integer requestedQty = (Integer) activityBean.get("act_qty");
            Integer remainingQty = (Integer) activityBean.get("rem_qty");
            Integer usedQty = requestedQty - remainingQty;
            activityBean.set("rem_approved_qty", quantity.intValue() - usedQty);

            /*
             * Update preauth status in the PPD tables if the requestedQty and the approved quantity
             * are different and mod_pat_pending_prescription is enabled.
             */
            if (!requestedQty.equals(quantity.intValue())) {
              // call the ppd function to update preauth status
              // Mark quantity number of items as approved.
              pendingPrescriptionService.updatePriorAuthStatus(preauthActId, "C",
                  quantity.intValue());
              // Mark the remaining as denied.
              pendingPrescriptionService.updatePriorAuthStatus(preauthActId, "D",
                  requestedQty - quantity.intValue());
            }
          } else {
            activityBean.set("approved_qty", 0);
            activityBean.set("rem_approved_qty", 0);
          }
          
          activityBean.set("preauth_id", authIdPayer);
          // Set preauth mode to Electronic(4) by default.
          activityBean.set("preauth_mode", 4);
          /*
           * Update preauth prescriptions table with net and denials.
           */
          int updateWithName = preauthActDAO.updateWithName(con,
              activityBean.getMap(), "preauth_act_id");
          success = (updateWithName > 0);
          if (!success) {
            break approvalTxn;
          }
          
          /*
           * If the preference update_claim_of_ordered_item_on_preauth_approval is Y then update the
           * charge and claim amounts of those charges.
           */
          BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
          boolean shouldUpdateClaim = "Y"
              .equals(genericPrefs.get("update_claim_of_ordered_item_on_preauth_approval"));
          if (shouldUpdateClaim) {
            // Have to send the amount because spring creates another transaction for the
            // below queries, thus setting the old approval amount or 0 as claim amount
            // when calculated in query.
            Object approvedAmount = activityBean.get("claim_net_approved_amount");
            String priorAuthId = (String) activityBean.get("preauth_id");
            Integer priorAuthModeId = (Integer) activityBean.get("preauth_mode");
            billChargeClaimService.setPriorAuthApprovalAmountAsClaimAmount(preauthActId,
                approvedAmount, priorAuthId, priorAuthModeId);
            billChargeService.setPriorAuthApprovalAmountAsClaimAmount(preauthActId, approvedAmount);
            billChargeService.setPriorAuthDetailsForCharges(preauthActId, priorAuthId,
                priorAuthModeId);
          }
        }

        /*
         * Mark Prior Auth Approval as F/P/R: Fully Approved/Partially
         * Approved/Fully Rejected
         */
        String approvalStatus = "";
        if (approvedActivityCount == sentActivityCount) {
          approvalStatus = "F";
        } else if (approvedActivityCount == 0) {
          approvalStatus = "R";
        } else {
          approvalStatus = "P";
        }

        BasicDynaBean preauthReqAppBean = preauthReqAppDAO
            .findByKey("preauth_request_id", authReqID);

        // got the response for the cancelled request, so marking the
        // status as cancelled in approval details table.
        if (preauthReqAppBean.get("preauth_request_type")
            .equals("Cancellation")) {
          approvalStatus = "X";
        }

        /*
         * Update preauth_request_approval_details table with approval
         * details.
         */
        preauthReqAppBean.set("preauth_request_id", authReqID);
        String authResult = priorAuth.getAuthorizationResult();
        preauthReqAppBean.set("approval_result", authResult);
        String start = priorAuth.getStart();
        String end = priorAuth.getEnd();
        preauthReqAppBean.set("start_date",
            DateUtil.stringToTimestamp(start));
        preauthReqAppBean.set("end_date",
            DateUtil.stringToTimestamp(end));
        preauthReqAppBean.set("preauth_id_payer", authIdPayer);

        SimpleDateFormat timeStampFormatterSecs = new SimpleDateFormat(
            "dd/MM/yyyy HH:mm");
        Timestamp txnDate = new java.sql.Timestamp(
            timeStampFormatterSecs.parse(transactionDate)
                .getTime());
        preauthReqAppBean.set("approval_recd_date", txnDate);
        String comments = priorAuth.getComments();
        preauthReqAppBean.set("approval_comments", comments);
        BigDecimal limit = priorAuth.getLimit();
        preauthReqAppBean.set("approval_limit", limit);
        preauthReqAppBean.set("approval_status", approvalStatus);

        int updateCount = preauthReqAppDAO.updateWithName(con,
            preauthReqAppBean.getMap(), "preauth_request_id");
        success = (updateCount > 0);
        if (!success) {
          break approvalTxn;
        }

        success = updatePreauthPrescriptionReq(con, authIdPayer, txnDate, comments, limit, 
                      approvalStatus, fileId , authResult, eauthPrescId);
        if (!success) {
          break approvalTxn;
        }

        String eauthStatus = "";
        if (approvalStatus.equals("X")) {
          eauthStatus = "X";
        } else if (!denied) {
          eauthStatus = "C"; // Closed
        } else {
          eauthStatus = "D"; // Denied
        }

        /* Update preauth_prescription table with approval details. */
        BasicDynaBean preauthBean = eauthPrescDAO.getBean();
        preauthBean.set("preauth_presc_id", eauthPrescId);
        preauthBean.set("preauth_status", eauthStatus);
        int count = eauthPrescDAO.updateWithName(con, preauthBean.getMap(),
            "preauth_presc_id");
        success = (count > 0);
        if (!success) {
          break approvalTxn;
        }

      } // label approvalTxn
      allSuccess = true;

    } finally {
      DataBaseUtil.commitClose(con, allSuccess);
    }

    return success;
  }

  /**
   * Update to cancellation status.
   *
   * <p>while sending a cancelling request itself, marking as cancelled without
   * waiting for cancellation request. updates only the approval status,
   * prescription status and item activity status.
   * @param preauthPrescId the preauth presc id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean updateToCancellationStatus(int preauthPrescId) throws SQLException, IOException {
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean success = false;
    try {
      txn:
      {
        BasicDynaBean record = eauthPrescDAO.getPreauthPrescriptionBean(preauthPrescId);
        BasicDynaBean appBean = preauthReqAppDAO.getBean();
        appBean.set("approval_status", "X");
        appBean.set("preauth_request_id", record.get("preauth_request_id"));
        int count = preauthReqAppDAO.updateWithName(con, appBean.getMap(), "preauth_request_id");
        success = (count > 0);
        if (!success) {
          break txn;
        }

        BasicDynaBean prescBean = eauthPrescDAO.getBean();
        prescBean.set("preauth_status", "X");
        prescBean.set("preauth_presc_id", preauthPrescId);
        count = eauthPrescDAO.updateWithName(con, prescBean.getMap(),
            "preauth_presc_id");
        success = (count > 0);
        if (!success) {
          break txn;
        }

        Map keys = new HashMap();
        keys.put("preauth_presc_id", preauthPrescId);
        List<BasicDynaBean> activityBeanList = preauthActDAO.listAll(con, null, keys, null);
        if (!activityBeanList.isEmpty()) {
          for (BasicDynaBean activityBean : activityBeanList) {
            activityBean.set("preauth_act_status", "O");
            activityBean.set("preauth_id", null);
            activityBean.set("preauth_mode", 0);
            int actCount = preauthActDAO
                .updateWithName(con, activityBean.getMap(), "preauth_act_id");
            success = (actCount > 0);
            if (!success) {
              break txn;
            }
          }
        }
        success = true;
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }
  
  /**
   * Update preauth req approval type.
   *
   * @param preauthPrescId the preauth presc id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean updatePreauthReqApprovalType(int preauthPrescId) throws SQLException, IOException {
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean success = false;
    try {
      txn:
      {
        BasicDynaBean record = eauthPrescDAO.getPreauthPrescriptionBean(preauthPrescId);
        BasicDynaBean appBean = preauthReqAppDAO.getBean();
        appBean.set("preauth_request_type", "Authorization");
        appBean.set("preauth_request_id", record.get("preauth_request_id"));
        int count = preauthReqAppDAO.updateWithName(con, appBean.getMap(), "preauth_request_id");
        success = (count > 0);
        if (!success) {
          break txn;
        }
        success = true;
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  /** The get eauth act approval. */
  private static String GET_EAUTH_ACT_APPROVAL =
      " SELECT * FROM preauth_approval_amount_details "
          + " WHERE preauth_act_id = ? ORDER BY preauth_request_id DESC LIMIT 1 ";

  /**
   * Gets the e auth approval bean.
   *
   * @param preauthActId the preauth act id
   * @return the e auth approval bean
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getEAuthApprovalBean(int preauthActId)
      throws SQLException {
    return DataBaseUtil.queryToDynaBean(GET_EAUTH_ACT_APPROVAL, preauthActId);
  }


  private static final String UPDATE_PREAUTH_PRESCRIPTION_REQUEST =
      " UPDATE preauth_prescription_request set preauth_id_payer=?, approval_recd_date=?, " 
      + " approval_comments =?, approval_limit = ?,approval_status = ?, " 
      + " file_id = ?, approval_result = ? " 
      + " WHERE preauth_presc_id =? AND (file_id IS NULL OR file_id ='') ";

  /**
   * Update preauth prescription request.
   *
   * @param eauthPrescId the preauth presc id
   * @return true, if successful
   * @throws SQLException the SQL exception
  */
  public static boolean updatePreauthPrescriptionReq(Connection con, String authIdPayer, 
      Timestamp txnDate, String comments, BigDecimal limit, String approvalStatus, 
      String fileId, String authResult ,Integer eauthPrescId) throws SQLException {

    boolean success = false;
    try (PreparedStatement ps = con.prepareStatement(UPDATE_PREAUTH_PRESCRIPTION_REQUEST)) {
      ps.setString(1, authIdPayer);
      ps.setTimestamp(2, txnDate);
      ps.setString(3, comments);
      ps.setBigDecimal(4, limit);
      ps.setString(5, approvalStatus);
      ps.setString(6, fileId);
      ps.setString(7, authResult);
      ps.setInt(8, eauthPrescId);
      success = ps.executeUpdate() > 0;
    }
    return success;
  }
}
