package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.insurance.DavitaSponsorDAO;

import java.util.Set;
import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class DialysisOrderDao {
	
	GenericDAO patRegDao = new GenericDAO("patient_registration");
	
	public BasicDynaBean getSponsorApprovalDetails(Connection con,String mr_no,String servGrpId,
				String itemId, String mainVisitId, String[] newlyAddedItemDetailIdsList,
				String[] newlyAddedApprovalLmtValuesList) throws SQLException, Exception {
		// TODO Auto-generated method stub
		PreparedStatement ps = null;
		BasicDynaBean approvalBean = null;
		if("".equals(itemId) || itemId == null){
		 return null;
		}
		approvalBean = getAllValidApprovals(con, mr_no, mainVisitId, servGrpId, itemId, newlyAddedItemDetailIdsList, newlyAddedApprovalLmtValuesList);
		//Map approvalMap = ConversionUtils.listBeanToMapListMap(approvalsList,"applicable_to_id");
		//if(approvalMap.containsKey(servGrpId)){

			/*if(approvalsList != null && approvalsList.size() > 0 )
				approvalBean = approvalsList.get(0);*/


			return approvalBean;
		//}
		//return null;
	}

	private static final String GET_ALL_PATIENT_APPROVALS = "SELECT sa.mr_no,sad.sponsor_approval_detail_id, " +
			" sad.sponsor_approval_id,sad.applicable_to,sad.applicable_to_id,sad.activity_type,sad.limit_type," +
			" sad.limit_value,sad.copay_type,sad.copay_value, sa.sponsor_id,sa.approval_no, " +
			" sa.org_id,sa.validity_start,sa.validity_end,sa.status,sa.priority " +
			" FROM patient_sponsor_approval_details sad " +
			" JOIN patient_sponsor_approvals sa ON (sa.sponsor_approval_id = sad.sponsor_approval_id) ";

	private static final String WHERE_COND_FOR_ITEM = " WHERE sa.validity_start <=? AND sa.validity_end >=? AND sa.mr_no=? " +
			" AND sad.applicable_to_id like ? AND sad.applicable_to='I' AND sad.item_status='A' AND sa.status='A' ";
	private static final String WHERE_COND_SERV_GRP = " WHERE sa.validity_start <=? AND sa.validity_end >=? AND sa.mr_no=? " +
			" AND sad.applicable_to_id=? AND sad.applicable_to='S' AND sad.item_status='A' AND sa.status='A' ";
	private static final String WHERE_COND_FOR_MRNO = " WHERE sa.mr_no=? ";

	private static final String APPROVALS_ORDER_BY = " order by sa.priority,sad.applicable_to ";
	
	private static final String APPROVALS_ORDER_BY_PRIORITY = " order by sa.priority";

	//returns a bean containing the rate plan with highest priority
	public BasicDynaBean getRatePlanBean(Connection con, String mr_no, String mainVisitId,
			String itemId) throws SQLException, Exception {
	// TODO Auto-generated method stub
	PreparedStatement ps = null;
	List<BasicDynaBean> approvalsList = null;
	BasicDynaBean rateBean = null;
	try {
		Date mainVisitDate = null;
		if(mainVisitId != null) {
			BasicDynaBean visitBean = patRegDao.findByKey(con, "main_visit_id", mainVisitId);
			mainVisitDate = (Date)visitBean.get("reg_date");
		} else {
			mainVisitDate = new Date();
		}
		
		ps = con.prepareStatement(GET_ALL_PATIENT_APPROVALS+WHERE_COND_FOR_ITEM+APPROVALS_ORDER_BY_PRIORITY);
		ps.setDate(1, new java.sql.Date(mainVisitDate.getTime()));
		ps.setDate(2, new java.sql.Date(mainVisitDate.getTime()));
		ps.setString(3, mr_no);
		ps.setString(4, "%"+itemId+"%");
		approvalsList = DataBaseUtil.queryToDynaList(ps);
		if(approvalsList != null && approvalsList.size() > 0) {
			return approvalsList.get(0);
		}
	}finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	return rateBean;
	}
	public BasicDynaBean getAllValidApprovals(Connection con, String mr_no, String mainVisitId, String servGrpId,
				String itemId, String[] newlyAddedItemDetailIdsList, String[] newlyAddedApprovalLmtValuesList) throws SQLException, Exception {
		// TODO Auto-generated method stub
		PreparedStatement ps = null;
		List<BasicDynaBean> approvalsList = null;
		BasicDynaBean approvalBean = null;
		try {
			Date mainVisitDate = null;
			if(mainVisitId != null) {
				BasicDynaBean visitBean = patRegDao.findByKey(con, "main_visit_id", mainVisitId);
				mainVisitDate = (Date)visitBean.get("reg_date");
			} else {
				mainVisitDate = new Date();
			}
			
			ps = con.prepareStatement(GET_ALL_PATIENT_APPROVALS+WHERE_COND_FOR_ITEM+APPROVALS_ORDER_BY);
			ps.setDate(1, new java.sql.Date(mainVisitDate.getTime()));
			ps.setDate(2, new java.sql.Date(mainVisitDate.getTime()));
			ps.setString(3, mr_no);
			ps.setString(4, "%"+itemId+"%");
			approvalsList = DataBaseUtil.queryToDynaList(ps);
			if(approvalsList != null && approvalsList.size() > 0) {
				for(BasicDynaBean bean : approvalsList) {
					boolean available = isSponsorLimitsAvailable(con, bean, mainVisitId, newlyAddedItemDetailIdsList, newlyAddedApprovalLmtValuesList);

					if(available) {
						approvalBean = bean;
						return approvalBean;
					}
				}
			}

			ps = con.prepareStatement(GET_ALL_PATIENT_APPROVALS+WHERE_COND_SERV_GRP+APPROVALS_ORDER_BY);
			ps.setDate(1, new java.sql.Date(new Date().getTime()));
			ps.setDate(2, new java.sql.Date(new Date().getTime()));
			ps.setString(3, mr_no);
			ps.setString(4, servGrpId);
			approvalsList = DataBaseUtil.queryToDynaList(ps);
			if(approvalsList != null && approvalsList.size() > 0) {
				for(BasicDynaBean bean : approvalsList) {
					boolean available = isSponsorLimitsAvailable(con, bean, mainVisitId, newlyAddedItemDetailIdsList, newlyAddedApprovalLmtValuesList);
					if(available) {
						approvalBean = bean;
						return approvalBean;
					}
				}
			}
			return null;
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String GET_ALL_PATIENT_SPONSOR_APPROVAL_DETAILS = " SELECT sa.mr_no, " +
			" sad.sponsor_approval_detail_id, " +
			" sad.sponsor_approval_id,sad.applicable_to,sad.applicable_to_name,sad.applicable_to_id, " +
			" sad.activity_type,sad.limit_type," +
			" sad.limit_value,sad.copay_type,sad.copay_value, sa.sponsor_id,sa.approval_no, " +
			" sa.org_id,sa.validity_start,sa.validity_end,sa.status,sa.priority,sa.approval_status, " +
			" tm.tpa_name, " +
			" CASE WHEN sad.limit_type = 'Q' THEN 'Quantity' " +
			"	WHEN sad.limit_type = 'A' THEN 'Amount' " +
			"	ELSE '' END as limit_type_name ," +
			" CASE WHEN " +
			"	sa.validity_start <=? AND sa.validity_end >=? THEN 'C' " + // current month
			" 	WHEN sa.validity_start < ? AND sa.validity_end < ? THEN 'E' " + // Expired
			"	WHEN sa.validity_start > ? AND sa.validity_end > ? THEN 'S' " + // soon
			"	ELSE 'X' " + //nothing
			" END as period, " +
			" coalesce(sum(bc.act_quantity),0) as used_qty, "+
			" coalesce(sum(bc.insurance_claim_amount),0) as used_amt "+

			" FROM patient_sponsor_approval_details sad " +
			" JOIN patient_sponsor_approvals sa ON (sa.sponsor_approval_id = sad.sponsor_approval_id) " +
			" LEFT JOIN sponsor_approved_charges sdc ON (sdc.sponsor_approval_detail_id = sad.sponsor_approval_detail_id) " +
			" LEFT JOIN bill_charge bc ON (bc.charge_id = sdc.charge_id AND bc.status !='X') " +
			" LEFT JOIN tpa_master tm on (tm.tpa_id = sa.sponsor_id)" +
			" WHERE sa.is_monthly_limits=false AND sad.item_status='A' " +
			" AND ((bc.posted_date>=sa.validity_start AND bc.posted_date<=sa.validity_end)  OR " +
			"	bc.posted_date is null) " + // for bringing approvals which are not yet consumed
			" AND sa.mr_no=? " +

			" group by sa.mr_no,sad.sponsor_approval_detail_id,sad.sponsor_approval_id,sad.applicable_to," +
			" sad.applicable_to_name,sad.applicable_to_id,sad.activity_type,sad.limit_type, "+
			" sad.limit_value,sad.copay_type,sad.copay_value, sa.sponsor_id,sa.approval_no, "+
			" sa.org_id,sa.validity_start,sa.validity_end,sa.status,sa.priority,sa.approval_status,  "+
			" tm.tpa_name " +

			" UNION " +

			" SELECT sa.mr_no, " +
			" sad.sponsor_approval_detail_id, " +
			" sad.sponsor_approval_id,sad.applicable_to,sad.applicable_to_name,sad.applicable_to_id, " +
			" sad.activity_type,sad.limit_type," +
			" sad.limit_value,sad.copay_type,sad.copay_value, sa.sponsor_id,sa.approval_no, " +
			" sa.org_id,sa.validity_start,sa.validity_end,sa.status,sa.priority,sa.approval_status, " +
			" tm.tpa_name, " +
			" CASE WHEN sad.limit_type = 'Q' THEN 'Quantity' " +
			"	WHEN sad.limit_type = 'A' THEN 'Amount' " +
			"	ELSE '' END as limit_type_name ," +
			" CASE WHEN " +
			"	sa.validity_start <=? AND sa.validity_end >=? THEN 'C' " + // current month
			" 	WHEN sa.validity_start < ? AND sa.validity_end < ? THEN 'E' " + // Expired
			"	WHEN sa.validity_start > ? AND sa.validity_end > ? THEN 'S' " + // soon
			"	ELSE 'X' " + //nothing
			" END as period, " +

			" CASE WHEN pr.main_visit_id is null " +
			" THEN 0 ELSE coalesce(sum(bc.act_quantity),0) END as used_qty, "+
			" CASE WHEN pr.main_visit_id is null " +
			" THEN 0 ELSE coalesce(sum(bc.insurance_claim_amount),0) END as used_amt "+

			" FROM patient_sponsor_approval_details sad "+
			" JOIN patient_sponsor_approvals sa ON (sa.sponsor_approval_id = sad.sponsor_approval_id) "+
			" LEFT JOIN patient_registration pr ON (sa.mr_no = pr.mr_no AND  (pr.main_visit_id=? OR pr.main_visit_id is null ) ) "+
			" LEFT JOIN bill b ON (b.visit_id = pr.patient_id) "+
			" LEFT JOIN sponsor_approved_charges sdc ON (sdc.sponsor_approval_detail_id = sad.sponsor_approval_detail_id) "+
			" LEFT JOIN bill_charge bc ON (sdc.charge_id = bc.charge_id and bc.bill_no = b.bill_no AND bc.status !='X') "+
			" LEFT JOIN tpa_master tm on (tm.tpa_id = sa.sponsor_id) "+

			" WHERE sa.is_monthly_limits=true AND sa.mr_no=? AND sad.item_status='A' " +
			
			" group by sa.mr_no,pr.main_visit_id,sad.sponsor_approval_detail_id,sad.sponsor_approval_id,sad.applicable_to," +
			" sad.applicable_to_name,sad.applicable_to_id,sad.activity_type,sad.limit_type, "+
			" sad.limit_value,sad.copay_type,sad.copay_value, sa.sponsor_id,sa.approval_no, "+
			" sa.org_id,sa.validity_start,sa.validity_end,sa.status,sa.priority,sa.approval_status,  "+
			" tm.tpa_name " +


			" order by validity_end,priority  "
			;

	public List<BasicDynaBean> getAllPatientSponsorApprovals(String mr_no, String mainVisitId)
		throws SQLException,ParseException {
		// TODO Auto-generated method stub
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getConnection();
			ps= con.prepareStatement(GET_ALL_PATIENT_SPONSOR_APPROVAL_DETAILS);

			// setting parameter values for CASE WHEN
			for(int i = 1 ; i <= 6 ; i++)
				ps.setDate(i, new java.sql.Date(new Date().getTime()));
			ps.setString(7, mr_no);

			for(int i = 8 ; i <= 13 ; i++)
				ps.setDate(i, new java.sql.Date(new Date().getTime()));

			ps.setString(14, mainVisitId);
			ps.setString(15, mr_no);

			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con , ps);
		}
	}

	private static final String GET_AVAILABLE_LIMITS_SPONSOR = " SELECT  ";

	private boolean isSponsorLimitsAvailable(Connection con, BasicDynaBean approvalBean ,
			String mainVisitId, String[] newlyAddedItemDetailIdsList, String[] newlyAddedApprovalLmtValuesList) throws SQLException,Exception  {

		DavitaSponsorDAO davSponDao = new DavitaSponsorDAO();
		BigDecimal remQtyOrAmt = BigDecimal.ZERO;
		String sponsorId = (String)approvalBean.get("sponsor_id");
		String applicableTo = (String)approvalBean.get("applicable_to");
		String applicableToId = (String)approvalBean.get("applicable_to_id");
		String aprvlType = (String)approvalBean.get("limit_type");
		BigDecimal aprvdQtyOrAmt = (BigDecimal)approvalBean.get("limit_value");

		/*BasicDynaBean consumBean = davSponDao.getConsumedQtyOrAmt(mainVisitId, itemId, sponsorId,
				applicableTo , applicableToId);*/
		BasicDynaBean consumBean = davSponDao.getConsumedQtyOrAmt(con, approvalBean , mainVisitId);

		if(consumBean != null) {
			if(aprvlType.equals("Q")) {
				remQtyOrAmt = aprvdQtyOrAmt.subtract((BigDecimal)consumBean.get("used_qty"));
			} else {
				remQtyOrAmt = aprvdQtyOrAmt.subtract((BigDecimal)consumBean.get("used_amt"));
			}
		} else {
			remQtyOrAmt = aprvdQtyOrAmt;
		}

		int approvalDetailId = (Integer)approvalBean.get("sponsor_approval_detail_id");

		if(null != newlyAddedItemDetailIdsList){
			for(int i=0; i<newlyAddedItemDetailIdsList.length; i++){
				int newlyAddedDetailId = Integer.parseInt(newlyAddedItemDetailIdsList[i]);
				if(approvalDetailId == newlyAddedDetailId ){
					BigDecimal newlyAddedItemQtyOrAmt = new BigDecimal(newlyAddedApprovalLmtValuesList[i]);
					remQtyOrAmt = remQtyOrAmt.subtract(newlyAddedItemQtyOrAmt);
				}
			}
		}

		return remQtyOrAmt.compareTo(BigDecimal.ZERO) > 0;
	}

	private static final String GET_PREVIOUS_UNPAID_BILLS = " SELECT b.*,COALESCE(cn.total_credits,0.00) as total_credits FROM bill b " +
			" JOIN patient_registration pr on (pr.patient_id = b.visit_id) " +
			" LEFT JOIN ( SELECT 'Y'::TEXT as is_creditnote, bcn.bill_no, sum(bil.total_amount) AS total_credits "
			+ "							FROM bill_credit_notes bcn "
			+ " 						JOIN bill bil on (bcn.credit_note_bill_no = bil.bill_no) "
			+ " 						GROUP BY bcn.bill_no"
			+ "				) AS cn ON(cn.bill_no = b.bill_no) "+
			" WHERE ((b.total_amount-b.total_claim-b.total_receipts+COALESCE(cn.total_credits,0.00)) > 0) " +
			" AND ((b.status NOT IN ('C','X')) OR cn.is_creditnote ='Y') AND pr.mr_no=? " +
			" ORDER BY b.open_date ";

	public List<BasicDynaBean> getPreviousUnpaidBills(String mr_no) throws SQLException {

		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			return getPreviousUnpaidBills(con , mr_no);
		} finally {
			DataBaseUtil.closeConnections(con , null);
		}
	}

	public List<BasicDynaBean> getPreviousUnpaidBills(Connection con , String mr_no) throws SQLException {

		PreparedStatement ps = null;
		try {
			ps= con.prepareStatement(GET_PREVIOUS_UNPAID_BILLS);

			ps.setString(1, mr_no);

			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(null , ps);
		}
	}

	private static final String GET_PREVIOUS_UNBALANCE_PAYMENT_BILLS = "SELECT b.*, COALESCE(cn.total_credits,0.00) as total_credits, cn.creditnote, pr.reg_date "
			+ " FROM bill b "
			+ " JOIN patient_registration pr on (pr.patient_id = b.visit_id) "
			+ " left JOIN ( select 'Y'::text as creditnote, bcn.bill_no, sum(bil.total_amount) as total_credits "
			+ "							FROM bill_credit_notes bcn "
			+ " 						JOIN bill bil on (bcn.credit_note_bill_no = bil.bill_no) "
			+ " 						GROUP BY bcn.bill_no"
			+ "				) AS cn ON(cn.bill_no = b.bill_no) "
			+ " WHERE((b.total_amount-b.total_claim-b.total_receipts+COALESCE(cn.total_credits,0.00)) != 0) "
			+ " AND ((b.status NOT IN ('C','X')) OR cn.creditnote ='Y' ) AND pr.mr_no=? "
			+ " ORDER BY b.open_date ";

	public List<BasicDynaBean> getPreviousUnbalaceBills(String mr_no) throws SQLException {

		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			return getPreviousUnbalaceBills(con , mr_no);
		} finally {
			DataBaseUtil.closeConnections(con , null);
		}
	}

	public List<BasicDynaBean> getPreviousUnbalaceBills(Connection con , String mr_no) throws SQLException {

		PreparedStatement ps = null;
		try {
			ps= con.prepareStatement(GET_PREVIOUS_UNBALANCE_PAYMENT_BILLS);

			ps.setString(1, mr_no);

			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(null , ps);
		}
	}

	private static final String GET_PREVIOUS_EXCESS_PAID_BILLS = " SELECT b.*, " +
		" (-(b.total_amount-b.total_claim-b.total_receipts)+COALESCE(cn.total_credits,0.00)) as excess_amt " +
		" FROM bill b " +
		" JOIN patient_registration pr on (pr.patient_id = b.visit_id) " +
		" LEFT JOIN ( SELECT 'Y'::TEXT as is_creditnote, bcn.bill_no, sum(bil.total_amount) as total_credits "
		+ "							FROM bill_credit_notes bcn "
		+ " 						JOIN bill bil on (bcn.credit_note_bill_no = bil.bill_no) "
		+ " 						GROUP BY bcn.bill_no"
		+ "				) AS cn ON(cn.bill_no = b.bill_no) "+ 
		" WHERE ((b.total_amount-b.total_claim-b.total_receipts+COALESCE(cn.total_credits,0.00)) < 0) " +
		" AND ((b.status NOT IN ('C','X')) OR cn.is_creditnote ='Y') AND pr.mr_no=? " +
		" ORDER BY b.open_date ";

	public List<BasicDynaBean> getPreviousExcessPaidBills(Connection con , String mr_no) throws SQLException {

		PreparedStatement ps = null;
		try {
			ps= con.prepareStatement(GET_PREVIOUS_EXCESS_PAID_BILLS);

			ps.setString(1, mr_no);

			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(null , ps);
		}
	}

	private static final String GET_PREVIOUS_BILLS_PAYMENTS = " SELECT b.* " +
		" FROM bill b " +
		" JOIN patient_registration pr on (pr.patient_id = b.visit_id) " +
		" WHERE b.total_receipts>0 " +
		" AND b.status NOT IN ('C','X') AND pr.mr_no=? " +
		" ORDER BY b.open_date desc ";

	public List<BasicDynaBean> getPreviousBillsPayments(Connection con, String mr_no) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps= con.prepareStatement(GET_PREVIOUS_BILLS_PAYMENTS);

			ps.setString(1, mr_no);

			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(null , ps);
		}
	}

	private static final String GET_PREVIOUS_BILLS_PAYMENTS_SUM = " SELECT " +
		" sum(b.total_receipts) as receipts_total " +
		" FROM bill b " +
		" JOIN patient_registration pr on (pr.patient_id = b.visit_id) " +
		" WHERE b.total_receipts>0 AND b.status NOT IN ('C','X') AND pr.mr_no=? ";

	public BasicDynaBean getPreviousBillsPaymentsSum(Connection con, String mr_no) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps= con.prepareStatement(GET_PREVIOUS_BILLS_PAYMENTS_SUM);

			ps.setString(1, mr_no);

			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(null , ps);
		}
	}

	public BasicDynaBean getPreviousBillsPaymentsSum(String mr_no) throws SQLException {
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			return getPreviousBillsPaymentsSum(con , mr_no);
		} finally {
			DataBaseUtil.closeConnections(con , null);
		}

	}

	private static final String GET_LATEST_CONSOLIDATED_BILL = " SELECT cpb.* " +
			" FROM consolidated_patient_bill cpb " +
			" JOIN patient_registration pr ON (pr.main_visit_id = cpb.main_visit_id) "+
			" WHERE pr.mr_no = ? AND cpb.status = 'A' ORDER BY cpb.open_date DESC LIMIT 1 ";

	public BasicDynaBean getLatestConsolidatedBill(Connection con, String mrNo) throws SQLException{
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(GET_LATEST_CONSOLIDATED_BILL);
			ps.setString(1, mrNo);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			if(null != ps) ps.close();
		}
	}

	private static final String GET_BILL_CHARGE_LIST = "SELECT bc.charge_id, bc.insurance_claim_amount, bc.amount," +
		" bc.act_description_id "+
		" FROM bill b "+
		" JOIN bill_charge bc ON(b.bill_no = bc.bill_no) "+
		" WHERE b.visit_id = ? ";

	public List<BasicDynaBean> getBillChargeList(String visitId) throws SQLException{
		Connection con =null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_BILL_CHARGE_LIST);
			ps.setString(1, visitId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String GET_RATE_PLAN_LIST =" SELECT od.org_name,od.org_id,od.status "+
		" FROM patient_registration pr "+
		" JOIN organization_details od ON(pr.org_id = od.org_id) "+
		" WHERE pr.patient_id = ? "+
		" UNION ALL "+
		" SELECT org_name,org_id,status " +
		" FROM organization_details "+
		" WHERE status='A' AND ( (has_date_validity AND "+
		" current_date BETWEEN valid_from_date AND valid_to_date ) OR (NOT has_date_validity)) "+
		" ORDER BY org_name ";

	public List<BasicDynaBean> getRatePlanList(String visitId) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_RATE_PLAN_LIST);
			ps.setString(1, visitId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public BasicDynaBean getPlanId(String sponsorId) throws SQLException {
		String query = " SELECT ictm.*, ipm.plan_id,ipm.category_id " +
				" FROM insurance_company_tpa_master ictm " +
				" LEFT JOIN insurance_plan_main ipm ON (ictm.insurance_co_id = ipm.insurance_co_id) " +
				" WHERE plan_id is not null AND ictm.tpa_id=? ";
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, sponsorId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
  private static final String GET_EXISTING_RECEIPT_USAGE = "SELECT * FROM receipt_usage "
      + "  WHERE receipt_id = ? AND entity_type = ? AND entity_id = ?";
  private static final String FIND_PATIENT_DEATH_DETAILS = "" 
      + " SELECT pd.death_date, pd.death_time"
      + " FROM patient_registration pr"
      + "          JOIN patient_details pd ON pr.mr_no = pd.mr_no"
      + " WHERE pr.mr_no = ?"
      + "   AND pr.discharge_type_id = 3;";

  public Boolean receiptUsageExist(String receiptId,
      String entityType, String entityId)
      throws SQLException {
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(GET_EXISTING_RECEIPT_USAGE);) {
      ps.setString(1, receiptId);
      ps.setString(2, entityType);
      ps.setString(3, entityId);
      List<BasicDynaBean> entries = DataBaseUtil.queryToDynaList(ps);
      return (entries == null || entries.isEmpty()) ? Boolean.FALSE : Boolean.TRUE;
    }
  }

  private static final String GET_ALL_BILLS_WITH_PATIENT_DUES = ""
      + " SELECT b.bill_no AS billno, "
      + "        COALESCE(b.total_amount, 0.00) + COALESCE(cn.total_credits, 0.00) + COALESCE(b.total_tax, 0.00) - COALESCE(b.total_claim, 0.00) - COALESCE(b.total_claim_tax, 0.00) AS amount, "
      + "        (COALESCE(b.total_amount, 0.00) + COALESCE(cn.total_credits, 0.00) + COALESCE(b.total_tax, 0.00) - COALESCE(b.total_claim, 0.00) - COALESCE(b.total_claim_tax, 0.00)) - "
      + "        (bill_receipts.allocated_amount)   AS patientDue"
      + " FROM bill b "
      + "         JOIN patient_registration pr ON b.visit_id = pr.patient_id "
      + "         LEFT JOIN LATERAL(SELECT 'Y'::TEXT as is_creditnote, bcn.bill_no, COALESCE(sum(bil.total_amount),0.00) AS total_credits "
      + "                    FROM bill_credit_notes bcn "
      + "                             JOIN bill bil on (bcn.credit_note_bill_no = bil.bill_no) "
      + "                    GROUP BY bcn.bill_no) AS cn ON (cn.bill_no = b.bill_no) "
      + "         LEFT JOIN LATERAL ( "
      + "            SELECT "
      + "             COALESCE(sum(allocated_amount), 0.00) AS allocated_amount "
      + "            FROM "
      + "             bill_receipts br "
      + "            WHERE "
      + "             br.bill_no = b.bill_no "
      + "        ) AS bill_receipts ON TRUE "
      + " WHERE pr.mr_no = ? AND "
      + " ( (COALESCE(b.total_amount, 0.00) + COALESCE(cn.total_credits, 0.00) + COALESCE(b.total_tax, 0.00) - COALESCE(b.total_claim, 0.00) - COALESCE(b.total_claim_tax, 0.00)) - "
      + "   (bill_receipts.allocated_amount) ) > 0"
      + " ORDER BY b.open_date";

  public List<BasicDynaBean> getAllBillsWithPatientDues(String mrNo)
      throws SQLException {
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(GET_ALL_BILLS_WITH_PATIENT_DUES)) {
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  private static final String GET_RECEIPT_WITH_UNALLOCATED_AMOUNT = ""
      + " SELECT * "
      + " FROM receipts r "
      + " WHERE mr_no = ? AND unallocated_amount > 0 ORDER BY created_at ASC";

  public List<BasicDynaBean> getReceiptWithUnAllocatedAmount(String mrNo) throws SQLException {
    StringBuilder query = new StringBuilder(GET_RECEIPT_WITH_UNALLOCATED_AMOUNT);
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(query.toString())) {
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    }
  }
  
  BasicDynaBean checkPatientDischargeStatus(String mrNo) throws SQLException {
    try (Connection con = DataBaseUtil.getConnection(true);
        PreparedStatement ps = con.prepareStatement(FIND_PATIENT_DEATH_DETAILS)) {
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaBean(ps);
    }
  }

  private static final String GET_GROSS_PATIENT_DUE = ""
      + " SELECT -COALESCE(SUM(unallocated_amount), 0.00) AS gross_patient_due "
      + " FROM receipts "
      + " WHERE mr_no = ? ";

  public BigDecimal getGrossPatientDue(String mrNo) throws SQLException {
    BigDecimal grossAmount = BigDecimal.ZERO;
    try (Connection con = DataBaseUtil.getConnection()) {
      BasicDynaBean bean = getGrossPatientDue(con, mrNo);
      if (bean != null) {
        grossAmount = (BigDecimal) bean.get("gross_patient_due");
      }
      return grossAmount;
    }
  }

  public BasicDynaBean getGrossPatientDue(Connection con, String mr_no) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_GROSS_PATIENT_DUE)) {
      ps.setString(1, mr_no);
      return DataBaseUtil.queryToDynaBean(ps);
    }
  }
	/*public Map<Integer, BigDecimal> getUsedQtyMapForApprovals(List<BasicDynaBean> patientApprovals)
													throws ParseException, SQLException, IOException {
		DavitaSponsorDAO davSpoDao = new DavitaSponsorDAO();
		VisitDetailsDAO visitDAO = new VisitDetailsDAO();

		Map<Integer, BigDecimal> usedQtyMap = new HashMap<Integer , BigDecimal>();

		for(BasicDynaBean bean : patientApprovals) {
			String mrNo = (String)bean.get("mr_no");
			BasicDynaBean mainVisit = visitDAO.getMainVisitOfCurrentMonth(mrNo);
			String sponsorId = (String)bean.get("sponsor_id");
			String applicableTo = (String)bean.get("applicable_to");
			String applicableToId = (String)bean.get("applicable_to_id");

			BasicDynaBean usedQtyBean = davSpoDao.getConsumedQtyOrAmt(bean);

			if(usedQtyBean != null) {
				if(((String)bean.get("limit_type")).equals("Q"))
					usedQtyMap.put((Integer)bean.get("sponsor_approval_detail_id"), (BigDecimal)usedQtyBean.get("used_qty"));
				else
					usedQtyMap.put((Integer)bean.get("sponsor_approval_detail_id"), (BigDecimal)usedQtyBean.get("used_amt"));
			} else {
				usedQtyMap.put((Integer)bean.get("sponsor_approval_detail_id"), BigDecimal.ZERO);
			}
		}
		return usedQtyMap;
	}*/
}
