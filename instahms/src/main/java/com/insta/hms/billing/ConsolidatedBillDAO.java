package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.MasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author prasanna.kumar
 *
 */

public class ConsolidatedBillDAO extends MasterDAO{

	static Logger logger = LoggerFactory.getLogger(ConsolidatedBillDAO.class);

	protected ConsolidatedBillDAO() {
		super("consolidated_patient_bill", "consolidated_bill_no");
	}

	private static final String LIST_QUERY_FIELDS = " SELECT *  ";
	private static final String COUNT_QUERY = " SELECT count(*) ";
	private static final String LIST_QUERY_TABLES = " FROM ( SELECT " +
			" cpb.main_visit_id,cpb.consolidated_bill_no,date(cpb.open_date) as open_date, "+
			" get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name) as patient_name ,cpb.status,"+
			" pr.mr_no,sum(b.total_amount) as total_amount, cpb.is_consolidated_credit_note,"+
			" (sum(b.total_amount)-sum(b.total_claim)) as pat_amt," +
			" (case when cpb.status='A' then 'Open' "+
			" when cpb.status='C' then 'Closed' "+
			" when cpb.status='F' then 'Finalized' "+
			" end) as cpbstatus"+
			" FROM consolidated_patient_bill cpb "+
			" JOIN bill b ON (cpb.bill_no=b.bill_no) "+
			" JOIN patient_registration pr ON (pr.patient_id= b.visit_id)"+
			" JOIN patient_details pd ON (pd.mr_no = pr.mr_no)"+

			" GROUP BY cpb.consolidated_bill_no,cpb.main_visit_id,cpb.open_date,cpb.status,pr.mr_no,"+
			" pd.salutation, pd.patient_name, pd.middle_name, pd.last_name, cpb.is_consolidated_credit_note) as list ";

	public  PagedList search(Map filter, Map<LISTING, Object> listingParams)
			throws SQLException, ParseException {
		String secondarySort = getIdColumnName();
		return super.search(LIST_QUERY_FIELDS, COUNT_QUERY, LIST_QUERY_TABLES, filter, listingParams, secondarySort);
	}

	private static final String GET_CONSOLIDATED_BILL_LIST = "" 
      + "SELECT b.bill_no,  "
      + "       cpb.main_visit_id,  "
      + "       pr.patient_id                              AS visit_id,  "
      + "       pr.reg_date                                AS visit_date,  "
      + "       b.open_date,  "
      + "       tpa.tpa_name                               AS sponsor_name,  "
      + "       bc.act_description                         AS item_name,  "
      + "       bc.amount,  "
      + "       coalesce(charge.insurance_claim_amt, 0.00) AS sponsor_amt,  "
      + "       CASE  "
      + "           WHEN b.is_tpa AND charge.claim_id IS NOT NULL  "
      + "               THEN (bc.amount - bc.insurance_claim_amount)  "
      + "           ELSE bc.amount  "
      + "           END                                    AS patient_amt  "
      + "FROM consolidated_patient_bill cpb  "
      + "         JOIN bill b ON (b.bill_no = cpb.bill_no)  "
      + "         JOIN bill_charge AS bc ON (b.bill_no = bc.bill_no)  "
      + "         LEFT JOIN LATERAL (SELECT bccl.claim_id,  "
      + "                                   bccl.insurance_claim_amt,  "
      + "                                   bccl.sponsor_id,  "
      + "                                   bcl.priority  "
      + "                            FROM bill_claim bcl  "
      + "                                     JOIN bill_charge_claim AS bccl ON  "
      + "                                (bccl.charge_id = bc.charge_id AND  "
      + "                                 bccl.claim_id = bcl.claim_id AND  "
      + "                                 coalesce(bccl.insurance_claim_amt, 0) > 0)  "
      + "                            WHERE bccl.charge_id = bc.charge_id  "
      + "    ) AS charge ON true  "
      + "         JOIN patient_registration pr ON (pr.patient_id = b.visit_id AND  "
      + "                                          pr.main_visit_id = cpb.main_visit_id)  "
      + "         JOIN patient_details pd ON (pd.mr_no = pr.mr_no)  "
      + "         LEFT JOIN tpa_master tpa ON (charge.sponsor_id = tpa.tpa_id)  "
      + "WHERE cpb.consolidated_bill_no = ?  " 
      + "  AND bc.status != 'X' "
      + "  AND (patient_confidentiality_check(pd.patient_group, pd.mr_no))  "
      + "ORDER BY visit_id,  "
      + "         open_date,  "
      + "         priority,  "
      + "         amount DESC ";

	public List<BasicDynaBean> getConsolidatedBillList(String consolidatedBillNo) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_CONSOLIDATED_BILL_LIST);
			ps.setString(1, consolidatedBillNo);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_AVAILABLE_TEMPLATE_LIST = "SELECT DISTINCT sponsor_id,template_name FROM sponsor_print_templates ";

	public List<BasicDynaBean> getAvailableTemplateList() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_AVAILABLE_TEMPLATE_LIST);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String GET_CONSOLIDATED_BILL_TOTALS = "SELECT sum(total_amount) AS amount, sum(total_claim) AS sponsor_amount, "+
			" sum(total_amount - total_claim) AS patient_amt, "+ 
			" sum(b.total_amount - total_claim - total_receipts) AS patient_due, "+
			" sum(b.total_claim - b.primary_total_sponsor_receipts - b.secondary_total_sponsor_receipts) AS sponsor_due "+
			" FROM consolidated_patient_bill cpb "+
			" JOIN bill b ON(b.bill_no = cpb.bill_no) "+
			" WHERE cpb.consolidated_bill_no=? ";

	public BasicDynaBean getConsolidatedBillTotals(String consolidatedBillNo) throws SQLException{
		return DataBaseUtil.queryToDynaBean(GET_CONSOLIDATED_BILL_TOTALS,consolidatedBillNo);
	}
}
