package com.insta.hms.insurance;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.adminmasters.services.MasterServicesDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillChargeClaimDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.billing.DialysisOrderDao;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
import com.insta.hms.master.CommonChargesMaster.CommonChargesDAO;
import com.insta.hms.master.DietaryMaster.DietaryMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.EquipmentMaster.EquipmentChargeDAO;
import com.insta.hms.master.InsuranceCompMaster.InsuCompMasterDAO;
import com.insta.hms.master.PlanMaster.PlanMasterDAO;
import com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO;
import com.insta.hms.orders.OrderBO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class DavitaSponsorDAO.
 */
public class DavitaSponsorDAO {

  /** The charge dao. */
  GenericDAO chargeDao = new GenericDAO("bill_charge");

  /** The spon apr chg dao. */
  GenericDAO sponAprChgDao = new GenericDAO("sponsor_approved_charges");

  /** The pat ins plans dao. */
  GenericDAO patInsPlansDao = new GenericDAO("patient_insurance_plans");

  /** The pat poly det. */
  GenericDAO patPolyDet = new GenericDAO("patient_policy_details");

  /** The bill dao. */
  GenericDAO billDao = new GenericDAO("bill");

  /** The charge claim dao. */
  GenericDAO chargeClaimDao = new GenericDAO("bill_charge_claim");

  /** The dia ord dao. */
  DialysisOrderDao diaOrdDao = new DialysisOrderDao();

  /** The bill charge dao. */
  GenericDAO billChargeDao = new GenericDAO("bill_charge");

  /** The serv sub grps dao. */
  ServiceSubGroupDAO servSubGrpsDao = new ServiceSubGroupDAO();

  /** The dia order dao. */
  DialysisOrderDao diaOrderDao = new DialysisOrderDao();

  /** The Pat reg dao. */
  GenericDAO patRegDao = new GenericDAO("patient_registration");

  /** The conso bill dao. */
  GenericDAO consoBillDao = new GenericDAO("consolidated_patient_bill");

  /** The credit note dao. */
  GenericDAO creditNoteDao = new GenericDAO("bill_credit_notes");

  /** The Constant GET_CONSUMED_QTY_OR_AMT_DETAILS. */
  private static final String GET_CONSUMED_QTY_OR_AMT_DETAILS = " SELECT sa.mr_no, "
      + " sad.sponsor_approval_detail_id, " + " sad.sponsor_approval_id, "
      + " coalesce(sum(bc.act_quantity),0) as used_qty, "
      + " coalesce(sum(bc.insurance_claim_amount),0) as used_amt "

      + " FROM patient_sponsor_approval_details sad " + " JOIN patient_sponsor_approvals sa ON "
      + " (sa.sponsor_approval_id = sad.sponsor_approval_id) "
      + " LEFT JOIN sponsor_approved_charges sdc ON "
      + " (sdc.sponsor_approval_detail_id = sad.sponsor_approval_detail_id) "
      + " LEFT JOIN bill_charge bc ON (bc.charge_id = sdc.charge_id AND bc.status !='X') "
      + " LEFT JOIN tpa_master tm on (tm.tpa_id = sa.sponsor_id)"
      + " WHERE sa.is_monthly_limits=false "
      + " AND ((bc.posted_date>=sa.validity_start AND bc.posted_date<=sa.validity_end)  OR "
      + " bc.posted_date is null) " + " AND sa.mr_no=? AND sad.sponsor_approval_detail_id=? "
      // + "AND bc.is_claim_locked " +

      + " group by sa.mr_no,sad.sponsor_approval_detail_id,sad.sponsor_approval_id "

      + " UNION "

      + " SELECT sa.mr_no, " + " sad.sponsor_approval_detail_id, " + " sad.sponsor_approval_id, "
      + " coalesce(sum(bc.act_quantity),0) as used_qty, "
      + " coalesce(sum(bc.insurance_claim_amount),0) as used_amt "

      + " FROM patient_sponsor_approval_details sad " + " JOIN patient_sponsor_approvals sa ON "
      + " (sa.sponsor_approval_id = sad.sponsor_approval_id) "
      + " LEFT JOIN patient_registration pr ON (sa.mr_no = pr.mr_no AND"
      + " (pr.main_visit_id=? OR pr.main_visit_id is null ) ) "
      + " LEFT JOIN bill b ON (b.visit_id = pr.patient_id) "
      + " LEFT JOIN sponsor_approved_charges sdc ON "
      + " (sdc.sponsor_approval_detail_id = sad.sponsor_approval_detail_id) "
      + " LEFT JOIN bill_charge bc ON "
      + " (sdc.charge_id = bc.charge_id and bc.bill_no = b.bill_no AND bc.status !='X') "
      + " LEFT JOIN tpa_master tm on (tm.tpa_id = sa.sponsor_id) "

      + " WHERE sa.is_monthly_limits=true "
      + " AND sa.mr_no=? AND sad.sponsor_approval_detail_id=? "
      // + "AND bc.is_claim_locked " +

      + " group by sa.mr_no,sad.sponsor_approval_detail_id,sad.sponsor_approval_id ";

  /**
   * Gets the consumed qty or amt.
   *
   * @param con          the con
   * @param approvalBean the approval bean
   * @param mainVisitId  the main visit id
   * @return the consumed qty or amt
   * @throws SQLException   the SQL exception
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public BasicDynaBean getConsumedQtyOrAmt(Connection con, BasicDynaBean approvalBean,
      String mainVisitId) throws SQLException, IOException, ParseException {
    // Connection con = null;
    PreparedStatement ps = null;
    try {
      // con = DataBaseUtil.getConnection();

      ps = con.prepareStatement(GET_CONSUMED_QTY_OR_AMT_DETAILS);
      ps.setString(1, (String) approvalBean.get("mr_no"));
      ps.setInt(2, (Integer) approvalBean.get("sponsor_approval_detail_id"));
      ps.setString(3, mainVisitId);
      ps.setString(4, (String) approvalBean.get("mr_no"));
      ps.setInt(5, (Integer) approvalBean.get("sponsor_approval_detail_id"));

      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Gets the consumed qty or amt.
   *
   * @param approvalBean the approval bean
   * @param mainVisitId  the main visit id
   * @return the consumed qty or amt
   * @throws SQLException   the SQL exception
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public BasicDynaBean getConsumedQtyOrAmt(BasicDynaBean approvalBean, String mainVisitId)
      throws SQLException, IOException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      return getConsumedQtyOrAmt(con, approvalBean, mainVisitId);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant GET_CONSUMED_QTY_OR_AMT. */
  private static final String GET_CONSUMED_QTY_OR_AMT = " SELECT pr.patient_id,pr.main_visit_id, "
      + " sum(bc.act_quantity) OVER (partition BY pr.main_visit_id) as used_qty, "
      + " sum(bc.insurance_claim_amount) OVER (partition BY pr.main_visit_id) as used_amt "
      + " FROM  bill_charge bc " + " JOIN bill_charge_claim bcc ON (bc.charge_id = bcc.charge_id) "
      + " JOIN bill b on (b.bill_no = bc.bill_no) "
      + " JOIN patient_registration pr ON (pr.patient_id = b.visit_id) "
      + " JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = bc.service_sub_group_id) "
      + " WHERE pr.main_visit_id=? AND bcc.sponsor_id=? " + " AND # ";

  /**
   * Gets the consumed qty or amt.
   *
   * @param mainVisitId    the main visit id
   * @param itemId         the item id
   * @param sponsorId      the sponsor id
   * @param applicableTo   the applicable to
   * @param applicableToId the applicable to id
   * @return the consumed qty or amt
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public BasicDynaBean getConsumedQtyOrAmt(String mainVisitId, String itemId, String sponsorId,
      String applicableTo, String applicableToId) throws SQLException, IOException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      String query = GET_CONSUMED_QTY_OR_AMT;
      if (applicableTo.equals("I")) {
        query = query.replaceAll(" # ", " bc.act_description_id=? ");
      } else {
        query = query.replaceAll(" # ", " ssg.service_group_id=? ");
      }
      ps = con.prepareStatement(query);
      ps.setString(1, mainVisitId);
      ps.setString(2, sponsorId);
      // applicableToId has itemId incase of
      // applicable to is 'I' else service group Id
      if (applicableTo.equals("I")) {
        ps.setString(3, applicableToId);
      } else {
        ps.setInt(3, Integer.valueOf(applicableToId));
      }

      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Calculate.
   *
   * @param con       the con
   * @param visitBean the visit bean
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws Exception    the exception
   */
  public void calculate(Connection con, BasicDynaBean visitBean)
      throws SQLException, IOException, Exception {
    String mrNo = (String) visitBean.get("mr_no");
    String mainVisitId = (String) visitBean.get("main_visit_id");
    String visitOrgId = (String) visitBean.get("org_id");
    if (visitOrgId == null) {
      visitOrgId = "ORG0001";
    }
    String type = null;
    ArrayList<String> approvedDetailIdsList = new ArrayList<String>();
    ArrayList<String> approvedLimitValuesList = new ArrayList<String>();
    // Connection con = null;

    try {
      // con = DataBaseUtil.getConnection();
      List<BasicDynaBean> chgBean = getVisitCharges(con, mainVisitId);
      // Collections.reverse(chgBean);
      // Delete the entries from sponsor_approved_charges which is not edited from Bill screen
      // based on isClaimLocked field.
      deleteSponsorApprovedChargesEntity(con, mainVisitId);
      for (BasicDynaBean chBean : chgBean) {
        String chargeHead = (String) chBean.get("charge_head");
        String bilNo = (String) chBean.get("bill_no");
        String chargeId = (String) chBean.get("charge_id");
        type = getOrderChargeType(chargeHead);
        String itemId = (String) chBean.get("act_description_id");
        String servGrpId = ((Integer) chBean.get("service_group_id")).toString();
        BigDecimal orderQty = (BigDecimal) chBean.get("act_quantity");
        if ((Boolean) chBean.get("is_claim_locked")) {
          continue;
        }
        // if((sponAprChgDao.exist("charge_id", chargeId))) continue;
        BasicDynaBean aprvlLtsBean = diaOrdDao.getSponsorApprovalDetails(con, mrNo, servGrpId,
            itemId, mainVisitId, approvedDetailIdsList.toArray(new String[0]),
            approvedLimitValuesList.toArray(new String[0]));
        Bill bii = new BillDAO(con).getBill(bilNo);
        BigDecimal aprvdQtyOrAmt = BigDecimal.ZERO;
        BigDecimal remQtyOrAmt = BigDecimal.ZERO;
        String aprvlType = null;
        String copayType = null;
        String sponsorId = null;
        BigDecimal copayPerOrAmt = BigDecimal.ZERO;
        BigDecimal orderAmt = (BigDecimal) chBean.get("amount") == null ? BigDecimal.ZERO
            : (BigDecimal) chBean.get("amount");

        if (aprvlLtsBean != null) {
          aprvlType = (String) aprvlLtsBean.get("limit_type");

          String orgId = (String) aprvlLtsBean.get("org_id");
          sponsorId = (String) aprvlLtsBean.get("sponsor_id");
          String applicableTo = (String) aprvlLtsBean.get("applicable_to");
          String applicableToId = (String) aprvlLtsBean.get("applicable_to_id");

          // orgId = (orgId != null && !orgId.equals("")) ? orgId : visitOrgId;
          // Map rateMap = getApplicableRates(orgId , itemId , bedType , type);
          // orderAmt =
          // ((BigDecimal)rateMap.get("item_rate")).
          // multiply((BigDecimal)chBean.get("act_quantity"));

          /*
           * BasicDynaBean consumBean = getConsumedQtyOrAmt(mainVisitId , itemId, sponsorId,
           * applicableTo, applicableToId);
           */

          BasicDynaBean consumBean = getConsumedQtyOrAmt(con, aprvlLtsBean, mainVisitId);

          aprvdQtyOrAmt = (BigDecimal) aprvlLtsBean.get("limit_value");
          // aprvlType = (String)aprvlLtsBean.get("limit_type");

          if (consumBean != null) {
            if (aprvlType.equals("Q")) {
              remQtyOrAmt = aprvdQtyOrAmt.subtract((BigDecimal) consumBean.get("used_qty"));
            } else {
              remQtyOrAmt = aprvdQtyOrAmt.subtract((BigDecimal) consumBean.get("used_amt"));
            }
          } else {
            remQtyOrAmt = aprvdQtyOrAmt;
          }

          // get the copay type and copay amount
          copayPerOrAmt = (BigDecimal) aprvlLtsBean.get("copay_value");
          copayType = (String) aprvlLtsBean.get("copay_type");

          /*
           * BasicDynaBean chargeBean = calculateSponsorAmount(itemId , remQtyOrAmt , orderQty ,
           * orderAmt , aprvlType , copayType , copayPerOrAmt); chargeBean.set("charge_id",
           * (String)chBean.get("charge_id")); chargeDao.updateWithName(con, chargeBean.getMap(),
           * "charge_id");
           */
          if (bii.getIs_tpa()) {
            insertChargeAndSponsorDetailMapping(con, chargeId,
                (Integer) aprvlLtsBean.get("sponsor_approval_detail_id"), null);
          }
        }
        BasicDynaBean chargeBean = calculateSponsorAmount(itemId, remQtyOrAmt, orderQty, orderAmt,
            aprvlType, copayType, copayPerOrAmt);
        chargeBean.set("charge_id", (String) chBean.get("charge_id"));
        if (!bii.getIs_tpa()) {
          chargeBean.set("insurance_claim_amount", BigDecimal.ZERO);
        }
        // chargeBean.set("is_claim_locked", Boolean.TRUE);

        if (aprvlLtsBean != null && bii.getIs_tpa()) {
          List<BasicDynaBean> chgClmList = chargeClaimDao.findAllByKey(con, "charge_id",
              (String) chBean.get("charge_id"));
          for (BasicDynaBean bean : chgClmList) {
            bean.set("insurance_claim_amt", BigDecimal.ZERO);
            if (((String) bean.get("sponsor_id")).equals((String) aprvlLtsBean.get("sponsor_id"))) {
              bean.set("insurance_claim_amt", chargeBean.get("insurance_claim_amount"));
            }
            chargeClaimDao.updateWithNames(con, bean.getMap(),
                new String[] { "charge_id", "sponsor_id" });
          }

          /*
           * keyMap.put("sponsor_id", (String)aprvlLtsBean.get("sponsor_id")); BasicDynaBean
           * chgClaimBean = chargeClaimDao.findByKey(con , keyMap);
           * chgClaimBean.set("insurance_claim_amt", chargeBean.get("insurance_claim_amount"));
           * chargeClaimDao.updateWithNames(con, chgClaimBean.getMap(), new
           * String[]{"charge_id","sponsor_id"});
           */
        } else {
          List<BasicDynaBean> chgClmList = chargeClaimDao.findAllByKey(con, "charge_id",
              (String) chBean.get("charge_id"));
          if (chgClmList != null && chgClmList.size() > 0) {
            for (BasicDynaBean bean : chgClmList) {
              bean.set("insurance_claim_amt", BigDecimal.ZERO);
              chargeClaimDao.updateWithNames(con, bean.getMap(),
                  new String[] { "charge_id", "sponsor_id" });
            }
          }

        }
        chargeDao.updateWithName(con, chargeBean.getMap(), "charge_id");
      }

    } finally {
      // DataBaseUtil.closeConnections(con, null);
    }
    // TODO Auto-generated method stub
  }

  /**
   * Insert charge and sponsor detail mapping.
   *
   * @param con         the con
   * @param chargeId    the charge id
   * @param detailId    the detail id
   * @param orgChargeId the org charge id
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void insertChargeAndSponsorDetailMapping(Connection con, String chargeId, Integer detailId,
      String orgChargeId) throws SQLException, IOException {
    if ((sponAprChgDao.findByKey(con, "charge_id", chargeId)) == null) {
      BasicDynaBean bean = sponAprChgDao.getBean();
      bean.set("charge_id", chargeId);
      bean.set("sponsor_approval_detail_id", detailId);
      bean.set("original_charge_id",
          (orgChargeId != null && !orgChargeId.equals("")) ? orgChargeId : null);
      sponAprChgDao.insert(con, bean);
    }
  }

  /**
   * Gets the order charge type.
   *
   * @param chargeHead the charge head
   * @return the order charge type
   */
  private String getOrderChargeType(String chargeHead) {
    String type = null;

    if (chargeHead.equals("SERSNP")) {
      type = "Service";
    } else if (chargeHead.equals("EQOPE")) {
      type = "Equipment";
    } else if (chargeHead.equals("LTDIA")) {
      type = "Laboratory";
    } else if (chargeHead.equals("RTDIA")) {
      type = "Radiology";
    } else if (chargeHead.equals("OCOTC") || chargeHead.equals("EQUOTC")
        || chargeHead.equals("CONOTC") || chargeHead.equals("MISOTC")
        || chargeHead.equals("IMPOTC")) {
      type = "Other Charge";
    } else if (chargeHead.equals("")) {
      type = "Meal";
    } else if (chargeHead.equals("OPDOC") || chargeHead.equals("ROPDOC")
        || chargeHead.equals("IPDOC")) {
      type = "Doctor";
    } else if (chargeHead.equals("PKGPKG")) {
      type = "Package";
    } else if (chargeHead.equals("BBED")) {
      type = "Bed";
    } else if (chargeHead.equals("BICU") || chargeHead.equals("NCICU") || chargeHead.equals("DDICU")
        || chargeHead.equals("PCICU")) {
      type = "ICU";
    } else {
      type = "Other Charge";
    }
    return type;
  }

  /** The Constant GET_CHARGES_OF_VISIT. */
  private static final String GET_CHARGES_OF_VISIT = " select "
      + " bc.charge_id,bc.charge_group,bc.charge_head, "
      + " bc.act_rate,bc.act_unit,bc.act_quantity,bc.amount,"
      + " bc.discount,bc.status,bc.act_description_id, "
      + " bc.insurance_claim_amount,bc.service_sub_group_id,"
      + " ssg.service_group_id,b.visit_id, bc.is_claim_locked, "
      + " pr.org_id as visit_org_id, bc.bill_no " + " FROM bill_charge bc "
      // " LEFT JOIN bill_charge_claim bcc ON (bc.charge_id = bcc.charge_id) " +
      + " JOIN bill b ON (b.bill_no = bc.bill_no) "
      + " JOIN patient_registration pr ON (pr.patient_id = b.visit_id) "
      + " JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = bc.service_sub_group_id) "
      + " WHERE bc.status != 'X' AND b.status NOT IN ('X') "
      + " AND pr.main_visit_id = ? order by bc.charge_id ";

  /**
   * Gets the visit charges.
   *
   * @param con         the con
   * @param mainVisitId the main visit id
   * @return the visit charges
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getVisitCharges(Connection con, String mainVisitId)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_CHARGES_OF_VISIT);
      ps.setString(1, mainVisitId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /** The Constant GET_MAIN_VISIT_NON_TPA_CHARGES. */
  private static final String GET_MAIN_VISIT_NON_TPA_CHARGES = " select "
      + " bc.charge_id,bc.charge_group,bc.charge_head, "
      + " bc.act_rate,bc.act_unit,bc.act_quantity,bc.amount,"
      + " bc.discount,bc.status,bc.act_description_id, "
      + " bc.insurance_claim_amount,bc.service_sub_group_id,"
      + " ssg.service_group_id,b.visit_id, bc.is_claim_locked, "
      + " pr.org_id as visit_org_id, bc.bill_no " + " FROM bill_charge bc "
      + " JOIN bill b ON (b.bill_no = bc.bill_no AND b.total_amount > 0) "
      + " JOIN patient_registration pr ON (pr.patient_id = b.visit_id) "
      + " JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = bc.service_sub_group_id) "
      + " WHERE bc.status != 'X' AND b.status NOT IN ('X') "
      + " AND NOT b.is_tpa AND NOT bc.is_claim_locked "
      + " AND pr.main_visit_id = ? order by bc.charge_id ";

  /**
   * Gets the main visit non tpa charges.
   *
   * @param con         the con
   * @param mainVisitId the main visit id
   * @return the main visit non tpa charges
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getMainVisitNonTpaCharges(Connection con, String mainVisitId)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_MAIN_VISIT_NON_TPA_CHARGES);
      ps.setString(1, mainVisitId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /** The Constant GET_SPONOSR_APPROVED_CHARGES. */
  private static final String GET_SPONOSR_APPROVED_CHARGES = " select "
      + " sac.charge_id,sac.original_charge_id,sac.sponsor_approval_detail_id "
      + " FROM bill_charge bc " + " JOIN bill b ON (b.bill_no = bc.bill_no AND b.total_amount > 0) "
      + " JOIN patient_registration pr ON (pr.patient_id = b.visit_id) "
      + " JOIN sponsor_approved_charges sac on (sac.charge_id = bc.charge_id) "
      + " WHERE bc.status != 'X' AND b.status NOT IN ('X') " + " AND pr.main_visit_id =? "
      + " order by bc.charge_id ";

  /**
   * Gets the main visit sponsor approved charges.
   *
   * @param con         the con
   * @param mainVisitId the main visit id
   * @return the main visit sponsor approved charges
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getMainVisitSponsorApprovedCharges(Connection con, String mainVisitId)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_SPONOSR_APPROVED_CHARGES);
      ps.setString(1, mainVisitId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Gets the applicable rates.
   *
   * @param orgId           the org id
   * @param itemId          the item id
   * @param bedType         the bed type
   * @param itemType        the item type
   * @param chargeType      the charge type
   * @param visitType       the visit type
   * @param orderQty        the order qty
   * @param isInsurance     the is insurance
   * @param planIds         the plan ids
   * @param patientId       the patient id
   * @param firstOfCategory the first of category
   * @return the applicable rates
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws Exception    the exception
   */
  public Map<String, Object> getApplicableRates(String orgId, String itemId, String bedType,
      String itemType, String chargeType, String visitType, BigDecimal orderQty,
      boolean isInsurance, int[] planIds, String patientId, boolean firstOfCategory)
      throws SQLException, IOException, Exception {

    Map<String, Object> rateMap = new HashMap<String, Object>();
    BasicDynaBean rateBean = null;
    if (itemType.equals("Service")) {
      rateBean = new MasterServicesDao().getServiceChargeBean(itemId, bedType, orgId);
      rateMap.put("item_rate",
          (rateBean.get("unit_charge")) == null ? BigDecimal.ZERO : rateBean.get("unit_charge"));
    } else if (itemType.equals("Equipment")) {
      rateBean = new EquipmentChargeDAO().getEquipmentCharge(itemId, bedType, orgId);
      rateMap.put("item_rate",
          (rateBean.get("charge")) == null ? BigDecimal.ZERO : rateBean.get("charge"));
    } else if (itemType.equals("Laboratory") || itemType.equals("Radiology")) {
      rateBean = AddTestDAOImpl.getTestDetails(itemId, bedType, orgId);
      rateMap.put("item_rate",
          (rateBean.get("charge")) == null ? BigDecimal.ZERO : rateBean.get("charge"));
    } else if ("Other Charge".equals(itemType)) {
      rateBean = new CommonChargesDAO().getCommonCharge(itemId);
      if (rateBean != null) {
        rateMap.put("item_rate",
            (rateBean.get("charge")) == null ? BigDecimal.ZERO : rateBean.get("charge"));
      }
    } else if ("Meal".equals(itemType)) {
      rateBean = new DietaryMasterDAO().getChargeForMeal(orgId, itemId, bedType);
      rateMap.put("item_rate",
          (rateBean.get("charge")) == null ? BigDecimal.ZERO : rateBean.get("charge"));
    } else if ("Doctor".equals(itemType)) {
      BasicDynaBean doctorBean = DoctorMasterDAO.getDoctorCharges(itemId, orgId, bedType);
      BasicDynaBean consTypeBean = OrderBO.getConsultationTypeBean(Integer.parseInt(chargeType));
      List<ChargeDTO> charges = OrderBO.getDoctorConsCharges(doctorBean, consTypeBean, visitType,
          OrgMasterDao.getOrgdetailsDynaBean(orgId), orderQty, isInsurance, planIds, bedType,
          patientId, firstOfCategory);
      ChargeDTO charge = charges.get(0);
      rateMap.put("item_rate", charge.getActRate());
      // rateMap.put("item_rate", (rateBean.get("op_charge")) == null ? BigDecimal.ZERO :
      // rateBean.get("op_charge"));
    } else if ("Package".equals(itemType)) {
      rateBean = PackageDAO.getPackageDetails(Integer.parseInt(itemId), orgId, bedType);
      rateMap.put("item_rate",
          (rateBean.get("charge")) == null ? BigDecimal.ZERO : rateBean.get("charge"));
    } else if ("Bed".equals(itemType)) {
      rateBean = new BedMasterDAO().getNormalBedChargesBean(itemId, orgId);
      rateMap.put("item_rate",
          (rateBean.get("charge")) == null ? BigDecimal.ZERO : rateBean.get("charge"));
    } else if ("ICU".equals(itemType)) {
      rateBean = new BedMasterDAO().getIcuBedChargesBean(itemId, bedType, orgId);
      rateMap.put("item_rate",
          (rateBean.get("charge")) == null ? BigDecimal.ZERO : rateBean.get("charge"));
    }

    return rateMap;
  }

  /**
   * Calculate sponsor amount.
   *
   * @param itemId        the item id
   * @param remQtyOrAmt   the rem qty or amt
   * @param orderQty      the order qty
   * @param orderAmt      the order amt
   * @param aprvlType     the aprvl type
   * @param copayType     the copay type
   * @param copayPerOrAmt the copay per or amt
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean calculateSponsorAmount(String itemId, BigDecimal remQtyOrAmt,
      BigDecimal orderQty, BigDecimal orderAmt, String aprvlType, String copayType,
      BigDecimal copayPerOrAmt) throws SQLException {
    BasicDynaBean chgBean = billChargeDao.getBean();
    BigDecimal insClaimAmt = BigDecimal.ZERO;
    BigDecimal copay = BigDecimal.ZERO;
    chgBean.set("act_quantity", orderQty);
    chgBean.set("amount", orderAmt);
    chgBean.set("act_description_id", itemId);
    if (remQtyOrAmt.compareTo(BigDecimal.ZERO) > 0) {
      if (copayType.equals("P")) {
        copay = orderAmt.multiply(copayPerOrAmt.divide(new BigDecimal(100)));
      } else {
        copay = (copayPerOrAmt.multiply(orderQty)).min(orderAmt);
      }

      if (aprvlType.equals("Q")) {
        // remQtyOrAmt = remQtyOrAmt.min(orderQty);
        BigDecimal rate = BigDecimal.ZERO;
        if (orderQty.compareTo(BigDecimal.ZERO) > 0) {
          rate = orderAmt.divide(orderQty);
        }
        BigDecimal avblQty = remQtyOrAmt;
        remQtyOrAmt = rate.multiply(remQtyOrAmt); // in terms of amount
        if (copayType.equals("P")) {
          remQtyOrAmt = remQtyOrAmt
              .subtract(remQtyOrAmt.multiply(copayPerOrAmt.divide(new BigDecimal(100))));
        } else {
          // remQtyOrAmt = remQtyOrAmt.subtract(
          // (remQtyOrAmt.multiply(orderQty)).min(copayPerOrAmt));
          remQtyOrAmt = remQtyOrAmt.subtract((copayPerOrAmt.multiply(avblQty)).min(remQtyOrAmt));
        }
        insClaimAmt = orderAmt.subtract(copay);
        insClaimAmt = remQtyOrAmt.min(insClaimAmt);
        /*
         * if(remQtyOrAmt.compareTo(orderQty) >= 0) { //chgBean.set("insurance_claim_amount",
         * orderAmt); insClaimAmt = orderAmt.subtract(copay); }
         */
      } else {
        insClaimAmt = orderAmt.subtract(copay);
        insClaimAmt = remQtyOrAmt.min(insClaimAmt);

        /*
         * if(remQtyOrAmt.compareTo(orderAmt) >= 0) { chgBean.set("insurance_claim_amount",
         * orderAmt); insClaimAmt = orderAmt.subtract(copay); }
         */
      }
      chgBean.set("insurance_claim_amount", insClaimAmt);
    } else {
      chgBean.set("insurance_claim_amount", BigDecimal.ZERO);
    }
    return chgBean;
  }

  /**
   * Insert bill charge claim entries.
   *
   * @param con          the con
   * @param visitDetBean the visit det bean
   * @return Set of bill numbers
   * @throws SQLException Signals that an SQL exception has occurred.
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Set<String> insertBillChargeClaimEntries(Connection con, BasicDynaBean visitDetBean)
      throws SQLException, IOException {
    ChargeDAO chargeDaoObj = new ChargeDAO(con);
    String visitId = (String) visitDetBean.get("patient_id");
    List<BasicDynaBean> insPlansList = patInsPlansDao.findAllByKey(con, "patient_id", visitId);
    int[] planIds = new int[insPlansList.size()];
    int planCnt = 0;
    for (BasicDynaBean planBean : insPlansList) {
      planIds[planCnt++] = (Integer) planBean.get("plan_id");
    }

    BigDecimal[] claimAmounts = new BigDecimal[planIds.length];
    BigDecimal[] claimTaxAmounts = new BigDecimal[planIds.length];
    String[] inclInClaimcalc = new String[planIds.length];
    String[] preAuthIds = new String[planIds.length];
    Integer[] preAuthModeIds = new Integer[planIds.length];
    Set<String> newBillNos = new HashSet<>();

    List<BasicDynaBean> billsList = billDao.findAllByKey(con, "visit_id", visitId);
    for (BasicDynaBean billBean : billsList) {
      boolean isTpaBill = (Boolean) billBean.get("is_tpa");
      if (!isTpaBill) {
        continue;
      }
      String billNo = (String) billBean.get("bill_no");
      newBillNos.add(billNo);
      List<BasicDynaBean> chgList = chargeDao.findAllByKey(con, "bill_no", billNo);
      List<ChargeDTO> chgDtoList = new ArrayList<ChargeDTO>();

      for (BasicDynaBean chgBean : chgList) {
        String chargeId = (String) chgBean.get("charge_id");
        /*
         * List<BasicDynaBean> chgClaimList = chargeDao.findAllByKey(con, "charge_id", chargeId);
         * if(chgClaimList.size() != insPlansList.size()) { for(BasicDynaBean chgClaimBean :
         * chgClaimList) { String clmChgId = (String)chgClaimBean.get("charge_id"); String sponsorId
         * = (String)chgClaimBean.get("sponsor_id"); String claimId =
         * (String)chgClaimBean.get("claim_id");
         * 
         * } }
         */
        ChargeDTO chgDto = chargeDaoObj.getCharge(chargeId);
        if (planIds.length > 0) {
          claimAmounts[0] = BigDecimal.ZERO;
          claimTaxAmounts[0] = BigDecimal.ZERO;
          inclInClaimcalc[0] = "Y";
          preAuthModeIds[0] = 0;
          if (planIds.length == 2) {
            claimAmounts[1] = BigDecimal.ZERO;
            claimTaxAmounts[1] = BigDecimal.ZERO;
            inclInClaimcalc[1] = "Y";
            preAuthModeIds[1] = 0;
          }
          // insert bill_charge_claim entry with zero
          // sponsor amt
          chgDto.setClaimAmounts(claimAmounts);
          chgDto.setSponsorTaxAmounts(claimTaxAmounts);
          chgDto.setIncludeInClaimCalc(inclInClaimcalc);
          chgDto.setPreAuthIds(preAuthIds);
          chgDto.setPreAuthModeIds(preAuthModeIds);
        }
        chgDtoList.add(chgDto);
      }

      new BillChargeClaimDAO().insertBillChargeClaims(con, chgDtoList, planIds, visitId, billNo);
    }
    return newBillNos;

  }

  /**
   * Recalculate previous visit items.
   *
   * @param con         the con
   * @param mainVisitId the main visit id
   * @return Set of bill numbers
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public Set<String> recalculatePreviousVisitItems(Connection con, String mainVisitId)
      throws SQLException, Exception {

    Set<String> billNos =  new HashSet<>();
    List<BasicDynaBean> visitBeanList = patRegDao.findAllByKey(con, "main_visit_id", mainVisitId);

    updateConsolidatedAndInternalBillsStatus(con, mainVisitId);
    updateAllChargesRateAndAmountAndInsPlans(con, mainVisitId);

    for (BasicDynaBean visitBean : visitBeanList) {

      // update the primary and secondary insurance plans from patinent
      // patient insurance plans to patient registration
      String visitId = (String) visitBean.get("patient_id");
      BasicDynaBean prBean = patRegDao.findByKey(con, "patient_id", visitId);
      List<BasicDynaBean> plansList = patInsPlansDao.findAllByKey(con, "patient_id", visitId);
      for (BasicDynaBean planBean : plansList) {
        int priority = (Integer) planBean.get("priority");
        if (priority == 1) {
          prBean.set("primary_sponsor_id", (String) planBean.get("sponsor_id"));
          prBean.set("primary_insurance_co", (String) planBean.get("insurance_co"));
        } else if (priority == 2) {
          prBean.set("secondary_sponsor_id", (String) planBean.get("sponsor_id"));
          prBean.set("secondary_insurance_co", (String) planBean.get("insurance_co"));
        }
      }
      patRegDao.updateWithName(con, prBean.getMap(), "patient_id");

      billNos.addAll(insertBillChargeClaimEntries(con, visitBean));
    }
    return billNos;
  }

  /** The Constant UPDATE_CONSOLIDATED_BILL_STATUS. */
  private static final String UPDATE_CONSOLIDATED_BILL_STATUS = " UPDATE "
      + " consolidated_patient_bill set status=? WHERE "
      + " consolidated_bill_no=? ";

  /**
   * Update consolidated bill status.
   *
   * @param con                the con
   * @param consolidatedBillNo the consolidated bill no
   * @param status             the status
   * @throws SQLException the SQL exception
   */
  public void updateConsolidatedBillStatus(Connection con, String consolidatedBillNo, String status)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      if (consolidatedBillNo != null) {
        ps = con.prepareStatement(UPDATE_CONSOLIDATED_BILL_STATUS);
        ps.setString(1, status);
        ps.setString(2, consolidatedBillNo);
        ps.executeUpdate();
      }
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /** The Constant UPDATE_CONSOLIDATED_INTERNAL_BILLS. */
  private static final String UPDATE_CONSOLIDATED_INTERNAL_BILLS = " UPDATE bill b set status=? "
      + " FROM consolidated_patient_bill cb "
      + " WHERE  (cb.bill_no = b.bill_no) and cb.consolidated_bill_no=? ";

  /**
   * Update consolidated internal bills.
   *
   * @param con                the con
   * @param consolidatedBillNo the consolidated bill no
   * @param status             the status
   * @throws SQLException the SQL exception
   */
  public void updateConsolidatedInternalBills(Connection con, String consolidatedBillNo,
      String status) throws SQLException {
    PreparedStatement ps = null;
    try {
      if (consolidatedBillNo != null) {
        ps = con.prepareStatement(UPDATE_CONSOLIDATED_INTERNAL_BILLS);
        ps.setString(1, status);
        ps.setString(2, consolidatedBillNo);
        ps.executeUpdate();
      }
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Update consolidated and internal bills status.
   *
   * @param con         the con
   * @param mainVisitId the main visit id
   * @throws SQLException the SQL exception
   */
  public void updateConsolidatedAndInternalBillsStatus(Connection con, String mainVisitId)
      throws SQLException {
    BasicDynaBean cosolidatedBean = consoBillDao.findByKey(con, "main_visit_id", mainVisitId);
    String consolidatedBillNo = (String) cosolidatedBean.get("consolidated_bill_no");
    updateConsolidatedBillStatus(con, consolidatedBillNo, "A");
    updateConsolidatedInternalBills(con, consolidatedBillNo, "A");
  }

  /**
   * Update all charges rate and amount and ins plans.
   *
   * @param con         the con
   * @param mainVisitId the main visit id
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public void updateAllChargesRateAndAmountAndInsPlans(Connection con, String mainVisitId)
      throws SQLException, Exception {

    boolean success = true;
    ArrayList<String> approvedDetailIdsList = new ArrayList<String>();
    ArrayList<String> approvedLimitValuesList = new ArrayList<String>();
    Map servGrpsMap = servSubGrpsDao.getServSubGrpAndServGrpsMap();

    BasicDynaBean mainVisitBean = patRegDao.findByKey(con, "patient_id", mainVisitId);
    String mrNo = (String) mainVisitBean.get("mr_no");

    Map<String, BigDecimal> detailIdConsumedMap = new HashMap<String, BigDecimal>();
    List<BasicDynaBean> chargeBeanList = getVisitCharges(con, mainVisitId);
    // Collections.reverse(chargeBeanList);
    deleteSponsorApprovedChargesEntity(con, mainVisitId);
    for (BasicDynaBean chgBean : chargeBeanList) {

      String servGrpId = ((Integer) servGrpsMap.get((Integer) chgBean.get("service_sub_group_id")))
          .toString();
      String visitId = (String) chgBean.get("visit_id");
      String visitOrgId = (String) chgBean.get("visit_org_id");
      if ((Boolean) chgBean.get("is_claim_locked")) {
        continue;
      }
      BasicDynaBean approvalBean = diaOrderDao.getSponsorApprovalDetails(con, mrNo, servGrpId,
          (String) chgBean.get("act_description_id"), mainVisitId,
          approvedDetailIdsList.toArray(new String[0]),
          approvedLimitValuesList.toArray(new String[0]));
      if (approvalBean == null) {
        String itemId = (String) chgBean.get("act_description_id");
        approvalBean = new DialysisOrderDao().getRatePlanBean(con, mrNo, mainVisitId, itemId);
        BasicDynaBean chargeBean = chargeDao.findByKey(con, "charge_id",
            (String) chgBean.get("charge_id"));
        updateRateAndAmountOfCharge(con, visitId, visitOrgId, approvalBean, chargeBean);
        continue;
      }

      BasicDynaBean chargeBean = chargeDao.findByKey(con, "charge_id",
          (String) chgBean.get("charge_id"));
      updateRateAndAmountOfCharge(con, visitId, visitOrgId, approvalBean, chargeBean);

      String aprvlType = (String) approvalBean.get("limit_type");
      approvedDetailIdsList.add(approvalBean.get("sponsor_approval_detail_id") == null ? "0"
          : approvalBean.get("sponsor_approval_detail_id").toString());
      if (aprvlType != null && aprvlType.equals("Q")) {
        approvedLimitValuesList.add(
            chgBean.get("act_quantity") == null ? "0" : chgBean.get("act_quantity").toString());
      } else {
        String detailId = approvalBean.get("sponsor_approval_detail_id") == null ? "0"
            : approvalBean.get("sponsor_approval_detail_id").toString();

        BigDecimal aprvdQtyOrAmt = (BigDecimal) approvalBean.get("limit_value");
        BigDecimal remQtyOrAmt = BigDecimal.ZERO;

        if (detailIdConsumedMap.containsKey(detailId)) {
          remQtyOrAmt = aprvdQtyOrAmt.subtract((BigDecimal) detailIdConsumedMap.get(detailId));
        } else {
          remQtyOrAmt = aprvdQtyOrAmt.subtract(BigDecimal.ZERO);
        }

        BasicDynaBean charge = calculateSponsorAmount((String) chgBean.get("act_description_id"),
            remQtyOrAmt, (BigDecimal) chgBean.get("act_quantity"),
            (BigDecimal) chgBean.get("amount"), aprvlType, (String) approvalBean.get("copay_type"),
            (BigDecimal) approvalBean.get("copay_value"));

        approvedLimitValuesList
            .add(charge == null ? "0" : (charge.get("insurance_claim_amount").toString()));
        if (null != charge) {
          if (detailIdConsumedMap.containsKey(detailId)) {
            detailIdConsumedMap.put(detailId, ((BigDecimal) detailIdConsumedMap.get(detailId))
                .add((BigDecimal) charge.get("insurance_claim_amount")));
          } else {
            detailIdConsumedMap.put(detailId, (BigDecimal) charge.get("insurance_claim_amount"));
          }
        }
      }

      Map insPlankeyMap = new HashMap();
      insPlankeyMap.put("patient_id", visitId);
      insPlankeyMap.put("sponsor_id", (String) approvalBean.get("sponsor_id"));
      BasicDynaBean planExist = patInsPlansDao.findByKey(con, insPlankeyMap);
      if (planExist != null) {
        continue;
      }

      BasicDynaBean insPlanBean = patInsPlansDao.getBean();
      insPlanBean.set("patient_insurance_plans_id", patInsPlansDao.getNextSequence());
      insPlanBean.set("mr_no", mrNo);
      insPlanBean.set("patient_id", visitId);
      insPlanBean.set("insurance_co", new InsuCompMasterDAO().getInsuranceCompaniesNamesAndIdsMap()
          .get("Default Insurance Company"));
      insPlanBean.set("sponsor_id", (String) approvalBean.get("sponsor_id"));
      insPlanBean.set("patient_policy_id",
          DataBaseUtil.getNextSequence("patient_policy_details_patient_policy_id_seq"));
      // insPlanBean.set("plan_id",(new PlanMasterDAO().findPlan("plan_name",
      // "Default Insurance Company Plan")).get("plan_id"));
      BasicDynaBean planBean = diaOrderDao.getPlanId((String) approvalBean.get("sponsor_id"));
      if (planBean != null) {
        insPlanBean.set("plan_id", (Integer) planBean.get("plan_id"));
        insPlanBean.set("plan_type_id", (Integer) planBean.get("category_id"));
      } else {
        insPlanBean.set("plan_id",
            (new PlanMasterDAO().findPlan("plan_name", "Default Insurance Company Plan"))
                .get("plan_id"));
        insPlanBean.set("plan_type_id",
            (new PlanMasterDAO().findPlan("plan_name", "Default Insurance Company Plan"))
                .get("category_id"));
      }

      List<BasicDynaBean> plansList = patInsPlansDao.findAllByKey(con, "patient_id", visitId);
      insPlanBean.set("priority", plansList.size() + 1);

      success = patInsPlansDao.insert(con, insPlanBean);

      List<BasicDynaBean> billExist = null;
      List<BasicDynaBean> plansList1 = patInsPlansDao.findAllByKey(con, "patient_id", visitId);
      billExist = billDao.findAllByKey(con, "visit_id", visitId);
      if (plansList1.size() > 0) {
        for (BasicDynaBean b : billExist) {
          Bill bii = new BillDAO(con).getBill((String) b.get("bill_no"));
          bii.setIs_tpa(true);
          new BillDAO(con).updateBill(bii);
        }
      } else {
        for (BasicDynaBean b : billExist) {
          Bill bii = new BillDAO(con).getBill((String) b.get("bill_no"));
          bii.setIs_tpa(false);
          new BillDAO(con).updateBill(bii);
        }
      }
      /*
       * List<BasicDynaBean> billExist = billDao.findAllByKey(con, "visit_id", visitId);
       * for(BasicDynaBean b:billExist){ Bill bii=new
       * BillDAO(con).getBill((String)b.get("bill_no")); bii.setIs_tpa(true); new
       * BillDAO(con).updateBill(bii); }
       */

      // insert patient_policy_details entries.
      BasicDynaBean patPolyBean = patPolyDet.getBean();
      patPolyBean.set("mr_no", mrNo);
      patPolyBean.set("plan_id", (Integer) insPlanBean.get("plan_id"));
      patPolyBean.set("patient_policy_id", insPlanBean.get("patient_policy_id"));
      patPolyBean.set("status", "A");
      patPolyBean.set("visit_id", visitId);

      success = patPolyDet.insert(con, patPolyBean);
    }
  }

  /**
   * Update rate and amount of charge.
   *
   * @param con          the con
   * @param visitId      the visit id
   * @param visitOrgId   the visit org id
   * @param approvalBean the approval bean
   * @param chgBean      the chg bean
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public void updateRateAndAmountOfCharge(Connection con, String visitId, String visitOrgId,
      BasicDynaBean approvalBean, BasicDynaBean chgBean) throws SQLException, Exception {
    String orgId = null;
    if (approvalBean == null) {
      orgId = visitOrgId;
    } else {
      orgId = ((String) approvalBean.get("org_id") != null
          || !((String) approvalBean.get("org_id")).equals(""))
              ? (String) approvalBean.get("org_id")
              : null;
    }

    if (orgId == null) {
      // If no org_id available then General as org_id
      orgId = "ORG0001";
    }

    int[] planIds = new int[1];
    planIds[0] = (Integer) (new PlanMasterDAO()
        .findPlan("plan_name", "Default Insurance Company Plan").get("plan_id"));
    String type = getOrderChargeType((String) chgBean.get("charge_head"));

    Map rateMap = getApplicableRates(orgId, (String) chgBean.get("act_description_id"), "GENERAL",
        type, null, "o", (BigDecimal) chgBean.get("act_quantity"), true, planIds, visitId, false);
    if (!rateMap.isEmpty()) {
      chgBean.set("act_rate", (BigDecimal) rateMap.get("item_rate"));
      chgBean.set("amount", ((BigDecimal) rateMap.get("item_rate"))
          .multiply((BigDecimal) chgBean.get("act_quantity")));
    }

    chargeDao.updateWithNames(con, chgBean.getMap(), new String[] { "charge_id" });
  }

  // DELETE FROM sponsor_approved_charges WHERE charge_id in(Select bc.charge_id from
  /** The Constant DELETE_CHARGES_OF_VISIT. */
  // bill_charge bc JOIN bill b ON (b.bill_no = bc.bill_no) )
  private static final String DELETE_CHARGES_OF_VISIT = "DELETE "
      + " FROM sponsor_approved_charges WHERE charge_id in(Select bc.charge_id "
      + " FROM bill_charge bc " + " JOIN bill b ON (b.bill_no = bc.bill_no) "
      + " JOIN patient_registration pr ON (pr.patient_id = b.visit_id) "
      // " JOIN service_sub_groups ssg ON (ssg.service_sub_group_id =
      // bc.service_sub_group_id) " +
      + " WHERE bc.is_claim_locked = false " + " AND pr.main_visit_id = ?) ";

  /**
   * Delete sponsor approved charges entity.
   *
   * @param con         the con
   * @param mainVisitId the main visit id
   * @throws SQLException the SQL exception
   */
  private void deleteSponsorApprovedChargesEntity(Connection con, String mainVisitId)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(DELETE_CHARGES_OF_VISIT);
      ps.setString(1, mainVisitId);
      ps.execute();
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Process and calculate prev month.
   *
   * @param con         the con
   * @param mainVisitId the main visit id
   * @return Map of new bill_no created
   * @throws Exception the exception
   */
  public Set<String> processAndCalculatePrevMonth(Connection con, String mainVisitId)
      throws Exception {

    boolean success = true;

    ArrayList<String> approvedDetailIdsList = new ArrayList<String>();
    ArrayList<String> approvedLimitValuesList = new ArrayList<String>();
    Map servGrpsMap = servSubGrpsDao.getServSubGrpAndServGrpsMap();
    // mapping
    // for
    // old
    // bill
    // and
    // corresponding
    // credit
    // note
    // bill
    Map<String, BasicDynaBean> creditNoteBillsMap = new HashMap<String, BasicDynaBean>();
    // mapping
    // for
    // old
    // and
    // new
    // bills;
    Map<String, BasicDynaBean> newBillsMappingMap = new HashMap<String, BasicDynaBean>();
    Set<String> newBillNos = new HashSet<>();

    // visitIds for updating bill_charge_claim entries
    List<String> visitIdsForChgClaims = new ArrayList<String>();
    // these charges claim amount should update in bill_charge_claim after all patient
    // insurance plans entries are created
    // key:new
    // charge_id,
    // value=its
    // bean
    Map<String, BasicDynaBean> chargeBeansToUpdate = new HashMap<String, BasicDynaBean>();
    // key:new
    // charge_id,
    // value=its
    // approvalBean
    Map<String, BasicDynaBean> chargeAndAprvlMap = new HashMap<String, BasicDynaBean>();

    List<BasicDynaBean> chargeAndApprvlMappingList = new ArrayList<BasicDynaBean>();

    BasicDynaBean mainVisitBean = patRegDao.findByKey(con, "patient_id", mainVisitId);
    String mrNo = (String) mainVisitBean.get("mr_no");

    try {
      List<BasicDynaBean> chargeBeanList = getMainVisitNonTpaCharges(con, mainVisitId);
      List<BasicDynaBean> approvedCharges = getMainVisitSponsorApprovedCharges(con, mainVisitId);

      Map<String, BasicDynaBean> approvedChgsMap = ConversionUtils
          .listBeanToMapBean(approvedCharges, "original_charge_id");

      for (BasicDynaBean chgBean : chargeBeanList) {

        // If the charge is already approved should not recalculate or process
        if (approvedChgsMap.containsKey((String) chgBean.get("charge_id"))) {
          continue;
        }

        String servGrpId = ((Integer) servGrpsMap
            .get((Integer) chgBean.get("service_sub_group_id"))).toString();
        String visitId = (String) chgBean.get("visit_id");
        String visitOrgId = (String) chgBean.get("visit_org_id");

        BasicDynaBean approvalBean = diaOrderDao.getSponsorApprovalDetails(con, mrNo, servGrpId,
            (String) chgBean.get("act_description_id"), mainVisitId,
            approvedDetailIdsList.toArray(new String[0]),
            approvedLimitValuesList.toArray(new String[0]));
        if (approvalBean == null) {
          continue;
        }
        /*
         * if(approvalBean == null ){ String itemId = (String)chgBean.get("act_description_id");
         * approvalBean = new DialysisOrderDao().getRatePlanBean(con, mrNo, mainVisitId , itemId);
         * BasicDynaBean chargeBean = chargeDao.findByKey(con, "charge_id",
         * (String)chgBean.get("charge_id")); updateRateAndAmountOfCharge(con , visitId , visitOrgId
         * , approvalBean , chargeBean); continue; }
         */

        BasicDynaBean newChargeBean = null;

        if (newBillsMappingMap.containsKey((String) chgBean.get("bill_no"))) {
          // Insert the charge for existing bill
          BasicDynaBean newBillBean = (BasicDynaBean) newBillsMappingMap
              .get((String) chgBean.get("bill_no"));
          BasicDynaBean newBillChargeBean = chargeDao.findByKey(con, "charge_id",
              (String) chgBean.get("charge_id"));
          newBillChargeBean.set("charge_id", new ChargeDAO(con).getNextChargeId());
          newBillChargeBean.set("bill_no", (String) newBillBean.get("bill_no"));
          newBillChargeBean.set("posted_date", new Timestamp(new Date().getTime()));
          newBillChargeBean.set("username", RequestContext.getUserName());
          chargeDao.insert(con, newBillChargeBean);
          newChargeBean = newBillChargeBean;

          // insert charge for existing credit note
          BasicDynaBean creditNoteBillBean = (BasicDynaBean) creditNoteBillsMap
              .get((String) chgBean.get("bill_no"));
          BasicDynaBean creditChargeBean = chargeDao.findByKey(con, "charge_id",
              (String) chgBean.get("charge_id"));
          creditChargeBean.set("charge_id", new ChargeDAO(con).getNextChargeId());
          creditChargeBean.set("bill_no", (String) creditNoteBillBean.get("bill_no"));
          creditChargeBean.set("act_rate",
              ((BigDecimal) creditChargeBean.get("act_rate")).negate());
          creditChargeBean.set("amount", ((BigDecimal) creditChargeBean.get("amount")).negate());
          creditChargeBean.set("posted_date", new Timestamp(new Date().getTime()));
          creditChargeBean.set("username", RequestContext.getUserName());
          chargeDao.insert(con, creditChargeBean);
        } else {
          // create the new bill and insert charges in that bill
          // error = orderBo.setBillInfo(con, (String)chgBean.get("visit_id"),
          // null, false, (String) RequestContext.getUserName(), "C");
          Bill bill = new BillBO().getBill((String) chgBean.get("bill_no"));

          BasicDynaBean newBill = billDao.getBean();
          newBill.set("bill_no",
              BillDAO.getNextBillNo(con, "C", "o", "N", RequestContext.getCenterId(), true, false));
          newBill.set("visit_id", bill.getVisitId());
          newBill.set("visit_type", bill.getVisitType());
          newBill.set("username", RequestContext.getUserName());
          newBill.set("opened_by", RequestContext.getUserName());
          newBill.set("status", "F");
          newBill.set("discharge_status", "Y");
          newBill.set("open_date", new Timestamp(new Date().getTime()));
          newBill.set("mod_time", new Timestamp(new Date().getTime()));
          newBill.set("finalized_date", new Timestamp(new Date().getTime()));
          newBill.set("last_finalized_at", new Timestamp(new Date().getTime()));
          newBill.set("closed_date", new Timestamp(new Date().getTime()));
          newBill.set("is_tpa", true);
          newBill.set("bill_rate_plan_id", bill.getBillRatePlanId());
          newBill.set("bill_type", "C");
          newBill.set("payment_status", "U");
          newBill.set("remarks", "created from back calculation process");
          newBill.set("credit_note_reasons", null);
          newBill.set("total_amount", BigDecimal.ZERO);
          newBill.set("discharge_status", "Y");
          newBill.set("total_claim", BigDecimal.ZERO);
          success = billDao.insert(con, newBill);

          BasicDynaBean newBillChargeBean = chargeDao.findByKey(con, "charge_id",
              (String) chgBean.get("charge_id"));
          newBillChargeBean.set("charge_id", new ChargeDAO(con).getNextChargeId());
          newBillChargeBean.set("bill_no", (String) newBill.get("bill_no"));
          newBillChargeBean.set("posted_date", new Timestamp(new Date().getTime()));
          newBillChargeBean.set("username", RequestContext.getUserName());
          chargeDao.insert(con, newBillChargeBean);
          newChargeBean = newBillChargeBean;

          // create credit note bill;
          // Bill creditBill = new BillBO().getBill((String)chgBean.get("bill_no"));
          BasicDynaBean creditNoteBillBean = billDao.getBean();

          creditNoteBillBean.set("bill_no",
              BillDAO.getNextBillNo(con, "C", "o", "N", RequestContext.getCenterId(), false, true));
          creditNoteBillBean.set("visit_id", bill.getVisitId());
          creditNoteBillBean.set("visit_type", bill.getVisitType());
          creditNoteBillBean.set("username", RequestContext.getUserName());
          creditNoteBillBean.set("opened_by", RequestContext.getUserName());
          creditNoteBillBean.set("status", "C");
          creditNoteBillBean.set("discharge_status", "Y");
          creditNoteBillBean.set("open_date", new Timestamp(new Date().getTime()));
          creditNoteBillBean.set("mod_time", new Timestamp(new Date().getTime()));
          creditNoteBillBean.set("finalized_date", new Timestamp(new Date().getTime()));
          creditNoteBillBean.set("last_finalized_at", new Timestamp(new Date().getTime()));
          creditNoteBillBean.set("closed_date", new Timestamp(new Date().getTime()));
          creditNoteBillBean.set("is_tpa", false);
          creditNoteBillBean.set("bill_rate_plan_id", bill.getBillRatePlanId());
          creditNoteBillBean.set("bill_type", "C");
          creditNoteBillBean.set("payment_status", "P");
          creditNoteBillBean.set("remarks", "created from back calculation process");
          creditNoteBillBean.set("credit_note_reasons", "created from back calculation");
          creditNoteBillBean.set("total_amount", BigDecimal.ZERO);
          creditNoteBillBean.set("discharge_status", "Y");
          creditNoteBillBean.set("total_claim", BigDecimal.ZERO);
          success = billDao.insert(con, creditNoteBillBean);

          BasicDynaBean creditChargeBean = chargeDao.findByKey(con, "charge_id",
              (String) chgBean.get("charge_id"));
          creditChargeBean.set("charge_id", new ChargeDAO(con).getNextChargeId());
          creditChargeBean.set("bill_no", (String) creditNoteBillBean.get("bill_no"));
          creditChargeBean.set("act_rate",
              ((BigDecimal) creditChargeBean.get("act_rate")).negate());
          creditChargeBean.set("amount", ((BigDecimal) creditChargeBean.get("amount")).negate());
          creditChargeBean.set("posted_date", new Timestamp(new Date().getTime()));
          creditChargeBean.set("username", RequestContext.getUserName());
          chargeDao.insert(con, creditChargeBean);

          // insert bill and credit note mapping
          BasicDynaBean creditNoteMapping = creditNoteDao.getBean();
          creditNoteMapping.set("bill_no", (String) chgBean.get("bill_no"));
          creditNoteMapping.set("credit_note_bill_no", (String) creditNoteBillBean.get("bill_no"));
          success = creditNoteDao.insert(con, creditNoteMapping);

          newBillsMappingMap.put((String) chgBean.get("bill_no"), newBill);
          newBillNos.add((String) chgBean.get("bill_no"));
          creditNoteBillsMap.put((String) chgBean.get("bill_no"), creditNoteBillBean);
        }

        updateRateAndAmountOfCharge(con, visitId, visitOrgId, approvalBean, newChargeBean);

        // calculate sponsor amount and update.
        Integer aprvlDetailId = (Integer) approvalBean.get("sponsor_approval_detail_id");
        String applicableTo = (String) approvalBean.get("applicable_to");
        String applicableToId = (String) approvalBean.get("applicable_to_id");
        String aprvdLimitType = (String) approvalBean.get("limit_type");
        BigDecimal aprvdLimitValue = (BigDecimal) approvalBean.get("limit_value");
        BigDecimal remQtyOrAmt = BigDecimal.ZERO;
        String orgId = (String) approvalBean.get("org_id");
        String sponsorId = (String) approvalBean.get("sponsor_id");

        String newChargeId = (String) newChargeBean.get("charge_id");

        BasicDynaBean consumBean = getConsumedQtyOrAmt(con, approvalBean, mainVisitId);

        if (consumBean != null) {
          if (aprvdLimitType.equals("Q")) {
            remQtyOrAmt = aprvdLimitValue.subtract((BigDecimal) consumBean.get("used_qty"));
          } else {
            remQtyOrAmt = aprvdLimitValue.subtract((BigDecimal) consumBean.get("used_amt"));
          }
        } else {
          remQtyOrAmt = aprvdLimitValue;
        }

        if (null != approvedDetailIdsList) {
          for (int i = 0; i < approvedDetailIdsList.size(); i++) {
            int newlyAddedDetailId = Integer.parseInt(approvedDetailIdsList.get(i));
            if (aprvlDetailId == newlyAddedDetailId) {
              BigDecimal newlyAddedItemQtyOrAmt = new BigDecimal(approvedLimitValuesList.get(i));
              remQtyOrAmt = remQtyOrAmt.subtract(newlyAddedItemQtyOrAmt);
            }
          }
        }

        // insertChargeAndSponsorDetailMapping(con, (String)newChargeBean.get("charge_id") ,
        // aprvlDetailId, (String)chgBean.get("charge_id"));

        BasicDynaBean chgAndAprvlMappingBean = sponAprChgDao.getBean();
        chgAndAprvlMappingBean.set("charge_id", (String) newChargeBean.get("charge_id"));
        chgAndAprvlMappingBean.set("sponsor_approval_detail_id", aprvlDetailId);
        chgAndAprvlMappingBean.set("original_charge_id", (String) chgBean.get("charge_id"));
        chargeAndApprvlMappingList.add(chgAndAprvlMappingBean);
        BigDecimal orderAmt = (BigDecimal) newChargeBean.get("amount");
        String itemId = (String) newChargeBean.get("act_description_id");
        BigDecimal orderQty = (BigDecimal) newChargeBean.get("act_quantity");
        BasicDynaBean sponsorAmtBean = calculateSponsorAmount(itemId, remQtyOrAmt, orderQty,
            orderAmt, aprvdLimitType, (String) approvalBean.get("copay_type"),
            (BigDecimal) approvalBean.get("copay_value"));

        newChargeBean.set("insurance_claim_amount",
            (BigDecimal) sponsorAmtBean.get("insurance_claim_amount"));

        approvedDetailIdsList.add(approvalBean.get("sponsor_approval_detail_id") == null ? "0"
            : approvalBean.get("sponsor_approval_detail_id").toString());
        if (aprvdLimitType.equals("Q")) {
          approvedLimitValuesList.add(orderQty.toString());
        } else {
          approvedLimitValuesList.add(newChargeBean.get("insurance_claim_amount") == null ? "0"
              : (((BigDecimal) newChargeBean.get("insurance_claim_amount")).toString()));
        }

        if (!visitIdsForChgClaims.contains(visitId)) {
          visitIdsForChgClaims.add((String) chgBean.get("visit_id"));
        }

        chargeBeansToUpdate.put((String) newChargeBean.get("charge_id"), newChargeBean);
        chargeAndAprvlMap.put((String) newChargeBean.get("charge_id"), approvalBean);

        Map insPlankeyMap = new HashMap();
        insPlankeyMap.put("patient_id", visitId);
        insPlankeyMap.put("sponsor_id", (String) approvalBean.get("sponsor_id"));
        BasicDynaBean planExist = patInsPlansDao.findByKey(con, insPlankeyMap);
        if (planExist == null) {
          BasicDynaBean insPlanBean = patInsPlansDao.getBean();
          insPlanBean.set("patient_insurance_plans_id", patInsPlansDao.getNextSequence());
          insPlanBean.set("mr_no", mrNo);
          insPlanBean.set("patient_id", visitId);
          insPlanBean.set("insurance_co", new InsuCompMasterDAO()
              .getInsuranceCompaniesNamesAndIdsMap().get("Default Insurance Company"));
          insPlanBean.set("sponsor_id", (String) approvalBean.get("sponsor_id"));
          insPlanBean.set("patient_policy_id",
              DataBaseUtil.getNextSequence("patient_policy_details_patient_policy_id_seq"));
          // insPlanBean.set("plan_id",(new PlanMasterDAO().findPlan("plan_name",
          // "Default Insurance Company Plan")).get("plan_id"));
          BasicDynaBean planBean = diaOrderDao.getPlanId((String) approvalBean.get("sponsor_id"));
          if (planBean != null) {
            insPlanBean.set("plan_id", (Integer) planBean.get("plan_id"));
            insPlanBean.set("plan_type_id", (Integer) planBean.get("category_id"));
          } else {
            insPlanBean.set("plan_id",
                (new PlanMasterDAO().findPlan("plan_name", "Default Insurance Company Plan"))
                    .get("plan_id"));
            insPlanBean.set("plan_type_id",
                (new PlanMasterDAO().findPlan("plan_name", "Default Insurance Company Plan"))
                    .get("category_id"));
          }

          List<BasicDynaBean> plansList = patInsPlansDao.findAllByKey(con, "patient_id", visitId);
          insPlanBean.set("priority", plansList.size() + 1);

          success = patInsPlansDao.insert(con, insPlanBean);

          // insert patient_policy_details entries.
          BasicDynaBean patPolyBean = patPolyDet.getBean();
          patPolyBean.set("mr_no", mrNo);
          patPolyBean.set("plan_id", (Integer) insPlanBean.get("plan_id"));
          patPolyBean.set("patient_policy_id", insPlanBean.get("patient_policy_id"));
          patPolyBean.set("status", "A");
          patPolyBean.set("visit_id", visitId);

          success = patPolyDet.insert(con, patPolyBean);

          // update the primary and secondary insurance plans from patient
          // patient insurance plans to patient registration

          BasicDynaBean prBean = patRegDao.findByKey(con, "patient_id", visitId);

          int priority = (Integer) insPlanBean.get("priority");
          if (priority == 1) {
            prBean.set("primary_sponsor_id", (String) insPlanBean.get("sponsor_id"));
            prBean.set("primary_insurance_co", (String) insPlanBean.get("insurance_co"));
          } else if (priority == 2) {
            prBean.set("secondary_sponsor_id", (String) insPlanBean.get("sponsor_id"));
            prBean.set("secondary_insurance_co", (String) insPlanBean.get("insurance_co"));
          }

          patRegDao.updateWithName(con, prBean.getMap(), "patient_id");
        }

      } // end of for loop on charge beans

      for (BasicDynaBean mappingBean : chargeAndApprvlMappingList) {
        insertChargeAndSponsorDetailMapping(con, (String) mappingBean.get("charge_id"),
            (Integer) mappingBean.get("sponsor_approval_detail_id"),
            (String) mappingBean.get("original_charge_id"));
      }

      // insert bill_charge_claim entries
      for (String visitId : visitIdsForChgClaims) {
        BasicDynaBean visitBean = VisitDetailsDAO.getVisitDetails(visitId);
        insertBillChargeClaimEntries(con, visitBean);
      }

      // insert sponsor amounts.
      for (Map.Entry<String, BasicDynaBean> entry : chargeBeansToUpdate.entrySet()) {
        BasicDynaBean newChBean = entry.getValue();
        List<BasicDynaBean> chgClmList = chargeClaimDao.findAllByKey(con, "charge_id",
            (String) newChBean.get("charge_id"));
        BasicDynaBean chargeAprvlBean = (BasicDynaBean) chargeAndAprvlMap
            .get((String) newChBean.get("charge_id"));
        for (BasicDynaBean bean : chgClmList) {
          bean.set("insurance_claim_amt", BigDecimal.ZERO);
          if (((String) bean.get("sponsor_id"))
              .equals((String) chargeAprvlBean.get("sponsor_id"))) {
            bean.set("insurance_claim_amt", newChBean.get("insurance_claim_amount"));
          }
          chargeClaimDao.updateWithNames(con, bean.getMap(),
              new String[] { "charge_id", "sponsor_id" });
        }
        chargeDao.updateWithName(con, newChBean.getMap(), "charge_id");
      }

      // create or insert consolidated bill.
      BasicDynaBean consoBillBean = getConsolidatedBill(con, mainVisitId,
          DateUtil.getDateRange("tm"), false);
      if (consoBillBean == null) {
        consoBillBean = consoBillDao.getBean();
        consoBillBean.set("consolidated_bill_no",
            DataBaseUtil.getNextPatternId("CONSOLIDATED_BILL"));
        consoBillBean.set("main_visit_id", mainVisitId);
        consoBillBean.set("open_date", DateUtil.getCurrentTimestamp());
        consoBillBean.set("status", "A");

        for (Map.Entry<String, BasicDynaBean> entry : newBillsMappingMap.entrySet()) {
          BasicDynaBean newBillBean = entry.getValue();
          consoBillBean.set("bill_no", (String) newBillBean.get("bill_no"));
          consoBillDao.insert(con, consoBillBean);
        }
      } else {
        for (Map.Entry<String, BasicDynaBean> entry : newBillsMappingMap.entrySet()) {
          BasicDynaBean newBillBean = entry.getValue();
          consoBillBean.set("bill_no", (String) newBillBean.get("bill_no"));
          consoBillDao.insert(con, consoBillBean);
        }
      }

      // create or insert consolidated credit note
      BasicDynaBean consoCreditNote = getConsolidatedBill(con, mainVisitId,
          DateUtil.getDateRange("tm"), true);
      if (consoCreditNote == null) {
        consoCreditNote = consoBillDao.getBean();
        consoCreditNote.set("consolidated_bill_no",
            DataBaseUtil.getNextPatternId("CONSOLIDATED_CREDIT_NOTE"));
        consoCreditNote.set("main_visit_id", mainVisitId);
        consoCreditNote.set("open_date", DateUtil.getCurrentTimestamp());
        consoCreditNote.set("status", "A");
        consoCreditNote.set("is_consolidated_credit_note", Boolean.TRUE);

        for (Map.Entry<String, BasicDynaBean> entry : creditNoteBillsMap.entrySet()) {
          BasicDynaBean newBillBean = entry.getValue();
          consoCreditNote.set("bill_no", (String) newBillBean.get("bill_no"));
          consoBillDao.insert(con, consoCreditNote);
        }
      } else {
        for (Map.Entry<String, BasicDynaBean> entry : creditNoteBillsMap.entrySet()) {
          BasicDynaBean newBillBean = entry.getValue();
          consoCreditNote.set("bill_no", (String) newBillBean.get("bill_no"));
          consoBillDao.insert(con, consoCreditNote);
        }
      }
      return newBillNos;

    } catch (Exception exception) {
      throw exception;
    }
  }

  /** The Constant CONSOLIDATED_BILL_OF_MONTH. */
  private static final String CONSOLIDATED_BILL_OF_MONTH = " SELECT * "
      + " FROM consolidated_patient_bill "
      + " WHERE main_visit_id=? AND open_date >= ? AND  open_date <= ?"
      + " AND is_consolidated_credit_note=? ";

  /**
   * Gets the consolidated bill.
   *
   * @param con                      the con
   * @param mainVisitId              the main visit id
   * @param dateRange                the date range
   * @param isConsolidatedCreditNote the is consolidated credit note
   * @return boolean
   * 
   *         This method is for getting whether consolidated bill is exist or not for a main visitId
   *         with this month or prev month. The Date range is generally this month or one previous
   *         month date range. (i.e. month start date and month end date) use
   *         DateUtil.getDateRange("tm") , DateUtil.getDateRange("pm")
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getConsolidatedBill(Connection con, String mainVisitId,
      java.sql.Date[] dateRange, boolean isConsolidatedCreditNote) throws SQLException {
    // TODO Auto-generated method stub
    PreparedStatement ps = null;
    BasicDynaBean consoBean = null;
    try {
      ps = con.prepareStatement(CONSOLIDATED_BILL_OF_MONTH);
      ps.setString(1, mainVisitId);
      ps.setTimestamp(2, new Timestamp(dateRange[0].getTime()));
      ps.setTimestamp(3, new Timestamp(dateRange[1].getTime()));
      ps.setBoolean(4, isConsolidatedCreditNote);
      consoBean = DataBaseUtil.queryToDynaBean(ps);
      return consoBean;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }
}
