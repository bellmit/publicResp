package com.insta.hms.billing;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class DepositsDAO extends GenericDAO {

	static Logger logger = LoggerFactory
			.getLogger(DepositsDAO.class);

	private Connection con = null;

	public DepositsDAO() {
		super("receipt_usage_view");
	}

	public String getNextDepositId(String depositType) throws SQLException {
		String seq = null;
		String typeNum = null;

		if (depositType.equalsIgnoreCase("R")) {
			seq = "deposit_collect_sequence";
			typeNum = "DEPOSIT RECEIPTS";
		} else if (depositType.equalsIgnoreCase("F")) {
			seq = "deposit_refund_sequence";
			typeNum = "DEPOSIT REFUNDS";
		}

		return AutoIncrementId.getSequenceId(seq, typeNum);
	}

	public static final String BILL_DEPOSIT_DETAILS =
		"SELECT mr_no, visit_id, bill_no, COALESCE(deposit_set_off,0) AS deposit_set_off,  " +
		" COALESCE(ip_deposit_set_off,0) AS ip_deposit_set_off, " +
		" hosp_total_deposits AS total_deposits, hosp_total_setoffs AS total_set_offs, " +
		" hosp_total_setoffs AS total_deposit_set_off, 0 AS package_id " +
		"FROM bill_deposit_details_view " +
		"WHERE bill_no=? ";

	private static final String BILL_DEPOSIT_DETAILS_EXCLUDING_PACKAGES = " SELECT bddv.mr_no, visit_id, bill_no, COALESCE(deposit_set_off,0) AS deposit_set_off, "
	    + " COALESCE(ip_deposit_set_off,0) AS ip_deposit_set_off, "
	    + " hosp_total_deposits - coalesce(mdv.package_deposits,0) AS total_deposits, "
	    + " hosp_total_setoffs - coalesce(mdv.package_set_offs,0) AS total_set_offs, "
	    + " hosp_total_setoffs - coalesce(mdv.package_set_offs,0) AS total_deposit_set_off, 0 AS package_id "
	    + " FROM bill_deposit_details_view bddv LEFT JOIN LATERAL "
	    + " (select mr_no, coalesce(sum(total_deposits), 0) as package_deposits, "
	    + "   coalesce(sum(total_set_offs), 0) as package_set_offs from ( "
	    + "     SELECT deposits.*, setoffs.total_set_offs FROM ( "
	    + "     SELECT pd.mr_no, pd.package_id, sum(pd.amount) as total_deposits "
	    + "       FROM patient_package_deposits_view pd WHERE bddv.mr_no = pd.mr_no "
	    + "       GROUP BY pd.mr_no, pd.package_id) as deposits "
	    + "     LEFT JOIN LATERAL ( "
	    + "       SELECT r.mr_no, package_bills.package_id as package_id, sum(b.deposit_set_off) as total_set_offs "
	    + "       FROM bill b JOIN patient_registration r ON (b.visit_id = r.patient_id) "
	    + "       JOIN patient_details p ON p.mr_no = r.mr_no "
	    + "         JOIN LATERAL (SELECT distinct bill_no, package_id from  "
	    + "         (SELECT orders.bill_no,orders.package_id  "
	    + "       FROM (SELECT bc.bill_no, p.package_id "
	    + "       FROM services_prescribed sp  "
	    + "       JOIN bill_charge bc ON(bc.order_number = sp.common_order_id) "
	    + "       JOIN package_prescribed pp ON (sp.package_ref = pp.prescription_id) "
	    + "       JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
	    + "       where b.bill_no = bc.bill_no "
	    + "       UNION ALL "
	    + "       SELECT bc.bill_no,p.package_id "
	    + "       FROM tests_prescribed tp "
	    + "       JOIN bill_charge bc ON(bc.order_number = tp.common_order_id) "
	    + "       JOIN package_prescribed pp ON (tp.package_ref = pp.prescription_id) "
	    + "       JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
	    + "       where b.bill_no = bc.bill_no "
	    + "       UNION ALL "
	    + "       SELECT bcc.bill_no,p.package_id "
	    + "       FROM doctor_consultation dc  "
	    + "       JOIN bill_charge bcc ON(bcc.order_number = dc.common_order_id) "
	    + "       JOIN package_prescribed pp ON (dc.package_ref = pp.prescription_id) "
	    + "       JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
	    + "       LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text AND bac.activity_code='DOC' "
	    + "       LEFT JOIN bill_charge bc ON (bc.charge_id = bac.charge_id) "
	    + "       where b.bill_no = bc.bill_no "
	    + "       UNION ALL "
	    + "       SELECT bc.bill_no,p.package_id "
	    + "       FROM other_services_prescribed osp  "
	    + "       JOIN bill_charge bc ON (bc.order_number = osp.common_order_id) "
	    + "       JOIN package_prescribed pp ON (osp.package_ref = pp.prescription_id) "
	    + "       JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
	    + "       where b.bill_no = bc.bill_no "
	    + "       ) as orders) as foo1) AS package_bills "
	    + "       ON (b.bill_no = package_bills.bill_no) "
	    + "       WHERE (patient_confidentiality_check(COALESCE(p.patient_group, 0), p.mr_no) and deposits.mr_no = r.mr_no) "
	    + " GROUP BY r.mr_no, package_bills.package_id )AS setoffs  "
	    + "   ON (deposits.mr_no = setoffs.mr_no AND deposits.package_id::integer = setoffs.package_id) "
	    + " ) as foo "
	    + "  WHERE mr_no = ? "
	    + " group by mr_no) as mdv "
	    + " ON bddv.mr_no = mdv.mr_no "
	    + " WHERE bddv.bill_no= ?";

	private static final String BILL_DEPOSIT_EXCLUDING_IP_AND_PKG_DEPOSITS =  " SELECT bddv.mr_no, visit_id, bill_no,"
	    + " COALESCE(deposit_set_off,0) AS deposit_set_off,  COALESCE(ip_deposit_set_off,0) AS ip_deposit_set_off, "
	    + " hosp_total_deposits - coalesce(mdv.package_deposits,0) - coalesce(ipdv.ip_deposits,0)  AS total_deposits, "
	    + " hosp_total_setoffs - coalesce(mdv.package_set_offs,0) - coalesce(ipdv.ip_set_offs,0)  AS total_set_offs, "
	    + " hosp_total_setoffs - coalesce(mdv.package_set_offs,0) - coalesce(ipdv.ip_set_offs,0) AS total_deposit_set_off, 0 AS package_id "
	    + " FROM bill_deposit_details_view bddv "
	    + " LEFT JOIN ( "
	     + "  SELECT mr_no, coalesce(sum(total_deposits), 0) as package_deposits, "
	    + "   coalesce(sum(total_set_offs), 0) as package_set_offs from (SELECT deposits.*, setoffs.total_set_offs FROM ( "
	     + "     SELECT pd.mr_no, pd.package_id, sum(pd.amount) as total_deposits "
	     + "       FROM patient_package_deposits_view pd "
	     + "       GROUP BY pd.mr_no, pd.package_id) as deposits "
	     + "     LEFT JOIN LATERAL ( "
	     + "     SELECT r.mr_no, package_bills.package_id as package_id, sum(b.deposit_set_off) as total_set_offs "
	     + "       FROM bill b JOIN patient_registration r ON (b.visit_id = r.patient_id) "
	     + "       JOIN patient_details p ON p.mr_no = r.mr_no "
	     + "         JOIN LATERAL (SELECT distinct bill_no, package_id from  "
	     + "         ( "
	     + "           SELECT orders.bill_no,orders.package_id "
	     + "         FROM (SELECT bc.bill_no, p.package_id "
	     + "         FROM services_prescribed sp "
	     + "         JOIN bill_charge bc ON(bc.order_number = sp.common_order_id) "
	     + "         JOIN package_prescribed pp ON (sp.package_ref = pp.prescription_id) "
	     + "         JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
	     + "         where b.bill_no = bc.bill_no "
	     + "         UNION ALL "
	     + "         SELECT bc.bill_no,p.package_id "
	      + "        FROM tests_prescribed tp "
	      + "        JOIN bill_charge bc ON(bc.order_number = tp.common_order_id) "
	     + "         JOIN package_prescribed pp ON (tp.package_ref = pp.prescription_id) "
	     + "         JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
	     + "         where b.bill_no = bc.bill_no "
	     + "         UNION ALL "
	     + "         SELECT bcc.bill_no,p.package_id "
	     + "         FROM doctor_consultation dc  "
	     + "         JOIN bill_charge bcc ON(bcc.order_number = dc.common_order_id) "
	     + "         JOIN package_prescribed pp ON (dc.package_ref = pp.prescription_id) "
	     + "         JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
	     + "         LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text AND bac.activity_code='DOC' "
	     + "         LEFT JOIN bill_charge bc ON (bc.charge_id = bac.charge_id) "
	     + "         where b.bill_no = bc.bill_no "
	     + "         UNION ALL "
	     + "         SELECT bc.bill_no,p.package_id "
	     + "         FROM other_services_prescribed osp  "
	     + "         JOIN bill_charge bc ON (bc.order_number = osp.common_order_id) "
	     + "         JOIN package_prescribed pp ON (osp.package_ref = pp.prescription_id) "
	     + "         JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
	     + "         where b.bill_no = bc.bill_no "
	     + "         ) as orders "
	     + "         ) as foo1 where b.bill_no = foo1.bill_no)  AS package_bills "
	     + "       ON (b.bill_no = package_bills.bill_no) "
	     + "       WHERE (patient_confidentiality_check(COALESCE(p.patient_group, 0), p.mr_no) and deposits.mr_no = r.mr_no) "
	     + " GROUP BY r.mr_no, package_bills.package_id )AS setoffs "
	     + "   ON (deposits.mr_no = setoffs.mr_no AND deposits.package_id::INTEGER = setoffs.package_id)) as foo "
	     + "  WHERE mr_no = ? "
	     + "  GROUP BY mr_no  "
	     + " ) as mdv ON bddv.mr_no = mdv.mr_no "
	     + " LEFT JOIN (   "
	     + "  SELECT mr_no, coalesce(sum(total_ip_deposits), 0) as ip_deposits, "
	     + "  coalesce(sum(total_ip_set_offs), 0) as ip_set_offs from ip_deposits_view  "
	     + "  WHERE mr_no = ? "
	     + "  GROUP BY mr_no "
	     + " ) as ipdv ON ipdv.mr_no = bddv.mr_no "
	     + " WHERE bddv.bill_no= ? ";

	public static BasicDynaBean getBillDepositDetails(String billNo,
			boolean excludePackageDeposits) throws SQLException {
		Connection connection = DataBaseUtil.getConnection(true);
		try {
			if (excludePackageDeposits) {
				String mrNo = getMrNo(connection, billNo);
				return DataBaseUtil.queryToDynaBean(connection,
						BILL_DEPOSIT_DETAILS_EXCLUDING_PACKAGES, new Object[] {mrNo, billNo });
			}
			return DataBaseUtil.queryToDynaBean(connection,BILL_DEPOSIT_DETAILS, new Object[] { billNo });
		} finally {
			DataBaseUtil.closeConnections(connection, null);
		}
	}


	public static BasicDynaBean getBillDepositDetails(String billNo,
			boolean excludePackageDeposits, String visitType)
			throws SQLException {
		Connection connection = DataBaseUtil.getConnection(true);
		try {
			if (excludePackageDeposits) {
				String mrNo = getMrNo(connection, billNo);
					if(visitType.equals("i")){
						return DataBaseUtil.queryToDynaBean(connection,BILL_DEPOSIT_DETAILS_EXCLUDING_PACKAGES, new Object[] {mrNo, billNo});
					} else {
						return DataBaseUtil.queryToDynaBean(connection,BILL_DEPOSIT_EXCLUDING_IP_AND_PKG_DEPOSITS, new Object[] {mrNo, mrNo, billNo});
					}
			}
			return DataBaseUtil.queryToDynaBean(connection,BILL_DEPOSIT_DETAILS,  new Object[] { billNo });
		} finally {
			DataBaseUtil.closeConnections(connection, null);
		}
	}


	private static final String BILL_DEPOSIT_DETAILS_EXCLUDING_PACKAGES_CENTER_WISE = " SELECT bddv.mr_no, visit_id, bill_no, COALESCE(deposit_set_off,0) AS deposit_set_off, "
		    + " COALESCE(ip_deposit_set_off,0) AS ip_deposit_set_off, "
		    + " COALESCE(rcpt.hosp_total_deposits,0) - coalesce(mdv.package_deposits,0) AS total_deposits, "
		    + " COALESCE(rc.hosp_total_setoffs,0) - coalesce(mdv.package_set_offs,0) AS total_set_offs, "
		    + " COALESCE(rc.hosp_total_setoffs,0) - coalesce(mdv.package_set_offs,0) AS total_deposit_set_off, 0 AS package_id "
		    + " FROM bill_deposit_details_view bddv "
		    + " LEFT JOIN LATERAL (SELECT mr_no , SUM(amount) AS hosp_total_deposits  "
			+ " FROM receipts r  "
			+ " WHERE  bddv.mr_no=r.mr_no AND (r.center_id IS NULL OR r.center_id=?)  AND r.is_deposit  "
			+ " GROUP BY mr_no) rcpt ON TRUE "
		    + " LEFT JOIN LATERAL (SELECT mr_no , SUM(allocated_amount) AS hosp_total_setoffs "
			+ " FROM receipts r  "
			+ " LEFT JOIN bill_receipts br ON br.receipt_no=r.receipt_id "
			+ " WHERE  bddv.mr_no=r.mr_no AND (r.center_id IS NULL OR r.center_id=?) AND r.is_deposit = true  "
			+ " GROUP BY mr_no) rc ON rc.mr_no=bddv.mr_no"
		    + " LEFT JOIN LATERAL "
		    + " (select mr_no, center_id,coalesce(sum(total_deposits), 0) as package_deposits, "
		    + "   coalesce(sum(total_set_offs), 0) as package_set_offs from ( "
		    + "     SELECT deposits.*, setoffs.total_set_offs FROM ( "
		    + "     SELECT pd.mr_no, pd.package_id, pd.center_id,sum(pd.amount) as total_deposits "
		    + "       FROM patient_package_deposits_view pd WHERE bddv.mr_no = pd.mr_no "
		    + "       GROUP BY pd.mr_no, pd.package_id,pd.center_id) as deposits "
		    + "     LEFT JOIN LATERAL ( "
		    + "       SELECT r.mr_no, r.center_id,package_bills.package_id as package_id, sum(b.deposit_set_off) as total_set_offs "
		    + "       FROM bill b JOIN patient_registration r ON (b.visit_id = r.patient_id) "
		    + "       JOIN patient_details p ON p.mr_no = r.mr_no "
		    + "         JOIN LATERAL (SELECT distinct bill_no, package_id from  "
		    + "         (SELECT orders.bill_no,orders.package_id  "
		    + "       FROM (SELECT bc.bill_no, p.package_id "
		    + "       FROM services_prescribed sp  "
		    + "       JOIN bill_charge bc ON(bc.order_number = sp.common_order_id) "
		    + "       JOIN package_prescribed pp ON (sp.package_ref = pp.prescription_id) "
		    + "       JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
		    + "       where b.bill_no = bc.bill_no "
		    + "       UNION ALL "
		    + "       SELECT bc.bill_no,p.package_id "
		    + "       FROM tests_prescribed tp "
		    + "       JOIN bill_charge bc ON(bc.order_number = tp.common_order_id) "
		    + "       JOIN package_prescribed pp ON (tp.package_ref = pp.prescription_id) "
		    + "       JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
		    + "       where b.bill_no = bc.bill_no "
		    + "       UNION ALL "
		    + "       SELECT bcc.bill_no,p.package_id "
		    + "       FROM doctor_consultation dc  "
		    + "       JOIN bill_charge bcc ON(bcc.order_number = dc.common_order_id) "
		    + "       JOIN package_prescribed pp ON (dc.package_ref = pp.prescription_id) "
		    + "       JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
		    + "       LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text AND bac.activity_code='DOC' "
		    + "       LEFT JOIN bill_charge bc ON (bc.charge_id = bac.charge_id) "
		    + "       where b.bill_no = bc.bill_no "
		    + "       UNION ALL "
		    + "       SELECT bc.bill_no,p.package_id "
		    + "       FROM other_services_prescribed osp  "
		    + "       JOIN bill_charge bc ON (bc.order_number = osp.common_order_id) "
		    + "       JOIN package_prescribed pp ON (osp.package_ref = pp.prescription_id) "
		    + "       JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
		    + "       where b.bill_no = bc.bill_no "
		    + "       ) as orders) as foo1) AS package_bills "
		    + "       ON (b.bill_no = package_bills.bill_no) "
		    + "       WHERE (patient_confidentiality_check(COALESCE(p.patient_group, 0), p.mr_no) and deposits.mr_no = r.mr_no) "
		    + " GROUP BY r.mr_no, r.center_id,package_bills.package_id )AS setoffs  "
		    + "   ON (deposits.mr_no = setoffs.mr_no AND deposits.package_id::integer = setoffs.package_id AND setoffs.center_id=deposits.center_id) "
		    + " ) as foo "
		    + "  WHERE mr_no = ? AND (center_id IS NULL OR center_id=?) "
		    + " group by mr_no,center_id) as mdv "
		    + " ON bddv.mr_no = mdv.mr_no "
		    + " WHERE bddv.bill_no= ? ";


	private static final String BILL_DEPOSIT_EXCLUDING_IP_AND_PKG_DEPOSITS_CENTER_WISE =  " SELECT bddv.mr_no, visit_id, bill_no,"
		    + " COALESCE(deposit_set_off,0) AS deposit_set_off,  COALESCE(ip_deposit_set_off,0) AS ip_deposit_set_off, "
		    + " COALESCE(rcpt.hosp_total_deposits,0) - coalesce(mdv.package_deposits,0) - coalesce(ipdv.ip_deposits,0)  AS total_deposits, "
		    + " COALESCE(rc.hosp_total_setoffs,0) - coalesce(mdv.package_set_offs,0) - coalesce(ipdv.ip_set_offs,0)  AS total_set_offs, "
		    + " COALESCE(rc.hosp_total_setoffs,0) - coalesce(mdv.package_set_offs,0) - coalesce(ipdv.ip_set_offs,0) AS total_deposit_set_off, 0 AS package_id "
		    + " FROM bill_deposit_details_view bddv "
		    + " LEFT JOIN LATERAL (SELECT mr_no , SUM(amount) AS hosp_total_deposits  "
			+ " FROM receipts r  "
			+ " WHERE  bddv.mr_no=r.mr_no AND (r.center_id IS NULL OR r.center_id=?)  AND r.is_deposit  "
			+ " GROUP BY mr_no) rcpt ON TRUE "
			+ " LEFT JOIN LATERAL (SELECT mr_no , SUM(allocated_amount) AS hosp_total_setoffs  "
			+ " FROM receipts r  "
			+ " LEFT JOIN bill_receipts br ON br.receipt_no=r.receipt_id "
			+ " WHERE  bddv.mr_no=r.mr_no AND (r.center_id IS NULL OR r.center_id=?)  AND r.is_deposit "
			+ " GROUP BY mr_no) rc ON rc.mr_no=bddv.mr_no"
		    + " LEFT JOIN ( "
		    + "  SELECT mr_no, center_id,coalesce(sum(total_deposits), 0) as package_deposits, "
		    + "   coalesce(sum(total_set_offs), 0) as package_set_offs from (SELECT deposits.*, setoffs.total_set_offs FROM ( "
		     + "     SELECT pd.mr_no, pd.package_id, pd.center_id,sum(pd.amount) as total_deposits "
		     + "       FROM patient_package_deposits_view pd "
		     + "       GROUP BY pd.mr_no, pd.package_id,pd.center_id) as deposits "
		     + "     LEFT JOIN LATERAL ( "
		     + "     SELECT r.mr_no, r.center_id,package_bills.package_id as package_id, sum(b.deposit_set_off) as total_set_offs "
		     + "       FROM bill b JOIN patient_registration r ON (b.visit_id = r.patient_id) "
		     + "       JOIN patient_details p ON p.mr_no = r.mr_no "
		     + "         JOIN LATERAL (SELECT distinct bill_no, package_id from  "
		     + "         ( "
		     + "           SELECT orders.bill_no,orders.package_id "
		     + "         FROM (SELECT bc.bill_no, p.package_id "
		     + "         FROM services_prescribed sp "
		     + "         JOIN bill_charge bc ON(bc.order_number = sp.common_order_id) "
		     + "         JOIN package_prescribed pp ON (sp.package_ref = pp.prescription_id) "
		     + "         JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
		     + "         where b.bill_no = bc.bill_no "
		     + "         UNION ALL "
		     + "         SELECT bc.bill_no,p.package_id "
		      + "        FROM tests_prescribed tp "
		      + "        JOIN bill_charge bc ON(bc.order_number = tp.common_order_id) "
		     + "         JOIN package_prescribed pp ON (tp.package_ref = pp.prescription_id) "
		     + "         JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
		     + "         where b.bill_no = bc.bill_no "
		     + "         UNION ALL "
		     + "         SELECT bcc.bill_no,p.package_id "
		     + "         FROM doctor_consultation dc  "
		     + "         JOIN bill_charge bcc ON(bcc.order_number = dc.common_order_id) "
		     + "         JOIN package_prescribed pp ON (dc.package_ref = pp.prescription_id) "
		     + "         JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
		     + "         LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text AND bac.activity_code='DOC' "
		     + "         LEFT JOIN bill_charge bc ON (bc.charge_id = bac.charge_id) "
		     + "         where b.bill_no = bc.bill_no "
		     + "         UNION ALL "
		     + "         SELECT bc.bill_no,p.package_id "
		     + "         FROM other_services_prescribed osp  "
		     + "         JOIN bill_charge bc ON (bc.order_number = osp.common_order_id) "
		     + "         JOIN package_prescribed pp ON (osp.package_ref = pp.prescription_id) "
		     + "         JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
		     + "         where b.bill_no = bc.bill_no "
		     + "         ) as orders "
		     + "         ) as foo1 where b.bill_no = foo1.bill_no)  AS package_bills "
		     + "       ON (b.bill_no = package_bills.bill_no) "
		     + "       WHERE (patient_confidentiality_check(COALESCE(p.patient_group, 0), p.mr_no) and deposits.mr_no = r.mr_no) "
		     + " GROUP BY r.mr_no, r.center_id,package_bills.package_id )AS setoffs "
		     + "   ON (deposits.mr_no = setoffs.mr_no AND deposits.package_id::INTEGER = setoffs.package_id AND setoffs.center_id=deposits.center_id)) as foo "
		     + "  WHERE mr_no = ?  AND (center_id IS NULL OR center_id=?) "
		     + "  GROUP BY mr_no,center_id  "
		     + " ) as mdv ON bddv.mr_no = mdv.mr_no "
		     + " LEFT JOIN (   "
		     + " SELECT pidv.mr_no,pidv.center_id,  "
			 + " COALESCE(SUM(CASE WHEN pidv.realized ='Y'  "
			 + " THEN amount ELSE 0 END), 0::numeric) AS ip_deposits,  "
			 + " SUM(total_ip_set_offs)AS ip_set_offs   "
			 + " FROM patient_ip_deposits_view pidv  "
			 + " LEFT JOIN LATERAL ( SELECT pr.mr_no,br.receipt_no,  "
			 + " (br.allocated_amount) AS total_ip_set_offs   "
			 + " FROM bill_receipts br   "
			 + " JOIN bill b ON br.bill_no=b.bill_no   "
			 + " JOIN patient_registration pr ON b.visit_id::text = pr.patient_id::text  "
			 + " WHERE pr.visit_type = 'i'::bpchar AND b.ip_deposit_set_off > 0::numeric  "
			 + " ) setoffs ON pidv.mr_no::text = setoffs.mr_no::text and pidv.receipt_id=setoffs.receipt_no   "
			 + " WHERE pidv.mr_no=? AND (pidv.center_id IS NULL OR pidv.center_id=?)   "
			 + " GROUP BY pidv.mr_no,pidv.center_id  "
		     + " ) as ipdv ON ipdv.mr_no = bddv.mr_no "
		     + " WHERE bddv.bill_no= ?";

	public static final String BILL_DEPOSIT_DETAILS_CENTER_WISE =
			"SELECT bddv.mr_no, visit_id, bill_no, COALESCE(deposit_set_off,0) AS deposit_set_off,  " +
			" COALESCE(ip_deposit_set_off,0) AS ip_deposit_set_off, " +
			" COALESCE(rcpt.hosp_total_deposits,0) AS total_deposits, "+
			" COALESCE(rc.hosp_total_setoffs,0) AS total_set_offs, " +
			" COALESCE(rc.hosp_total_setoffs,0) AS total_deposit_set_off, 0 AS package_id " +
			" FROM bill_deposit_details_view bddv " +
			" LEFT JOIN LATERAL (SELECT mr_no , SUM(amount) AS hosp_total_deposits  " +
			" FROM receipts r  " +
			" WHERE  bddv.mr_no=r.mr_no AND (r.center_id IS NULL OR r.center_id=?)  AND r.is_deposit  " +
			" GROUP BY mr_no) rcpt ON TRUE " +
		    " LEFT JOIN LATERAL (SELECT mr_no,SUM(allocated_amount) AS hosp_total_setoffs " +
			" FROM receipts r " +
			" LEFT JOIN bill_receipts br ON br.receipt_no=r.receipt_id " +
			" WHERE  bddv.mr_no=r.mr_no AND r.is_deposit = true  " +
			" AND (r.center_id IS NULL OR r.center_id=?) "+
			" GROUP BY mr_no) rc ON rc.mr_no=bddv.mr_no" +
			" WHERE bill_no=?";

	public static BasicDynaBean getBillDepositDetails(String billNo,
			boolean excludePackageDeposits, String visitType,int centerId)
			throws SQLException {
		Connection connection = DataBaseUtil.getConnection(true);
		try {
			if (excludePackageDeposits) {
				String mrNo = getMrNo(connection, billNo);
					if(visitType.equals("i")){
						return DataBaseUtil.queryToDynaBean(connection,BILL_DEPOSIT_DETAILS_EXCLUDING_PACKAGES_CENTER_WISE, new Object[] {centerId, centerId,mrNo,centerId,billNo,});
					} else {
						return DataBaseUtil.queryToDynaBean(connection,BILL_DEPOSIT_EXCLUDING_IP_AND_PKG_DEPOSITS_CENTER_WISE, new Object[] {centerId, centerId,mrNo,centerId, mrNo,centerId,billNo});
					}
			}
			return DataBaseUtil.queryToDynaBean(connection,BILL_DEPOSIT_DETAILS_CENTER_WISE,  new Object[] { centerId, centerId,billNo });
		} finally {
			DataBaseUtil.closeConnections(connection, null);
		}
	}


	private static final String GET_MR_NO = "SELECT mr_no FROM patient_registration "
			+ " JOIN bill ON bill.visit_id = patient_registration.patient_id "
			+ " WHERE bill.bill_no = ?";

	private static String getMrNo(Connection connection, String billNo) throws SQLException {
		String mrNo = null;
		BasicDynaBean patientBean = DataBaseUtil.queryToDynaBean(connection,GET_MR_NO, new Object[] { billNo });
		if(null != patientBean){
			mrNo = (String)patientBean.get("mr_no");
		}
			return mrNo;
	}

	private static String getMrNo(String billNo) throws SQLException {
		String mrNo = null;	
		BasicDynaBean patientBean = DataBaseUtil.queryToDynaBean(GET_MR_NO, new Object[] { billNo });
		if(null != patientBean){
			mrNo = (String)patientBean.get("mr_no");
		}
			return mrNo;
	}

	public static BasicDynaBean getBillDepositDetails(String billNo) throws SQLException {
		return getBillDepositDetails(billNo, false);
	}

	private static final String IP_DEPOSIT_DETAILS = "SELECT ipd.*, b.ip_deposit_set_off " +
			" FROM ip_deposits_view ipd "+
			" JOIN patient_registration pr ON(pr.mr_no = ipd.mr_no) "+
			" JOIN bill b ON(b.visit_id = pr.patient_id) "+
			" WHERE b.bill_no = ? and ipd.mr_no = ?";
	public static BasicDynaBean getIPBillDepositDetails(String billNo) throws SQLException {
		String mrNo = getMrNo(billNo); 
		return DataBaseUtil.queryToDynaBean(IP_DEPOSIT_DETAILS, new Object[] { billNo, mrNo });
	}

	public static BasicDynaBean getIPBillDepositDetails(Connection con, String billNo) throws SQLException {
		String mrNo = getMrNo(con, billNo); 
    	return DataBaseUtil.queryToDynaBean(con, IP_DEPOSIT_DETAILS, new Object[] {billNo, mrNo});
  	}

	 private static final String IP_DEPOSIT_DETAILS_CENTER_WISE = "SELECT ipdeposits.*,"
	 		+ " COALESCE(setoffs.total_ip_set_offs,0)AS total_ip_set_offs,"
	 		+ " COALESCE(non_ip_bill_setoffs.total_ip_set_offs_non_ip_bills,0)AS "
	 		+ " total_ip_set_offs_non_ip_bill "
	 		+ " FROM (SELECT pd.mr_no,COALESCE(total_ip_deposits,0)AS total_ip_deposits, "
	 		+ " COALESCE(ip_unrealized_amount,0)AS ip_unrealized_amount,pd.center_id, b.bill_no "
	 		+ " FROM patient_ip_deposits_view pd "
	 		+ " JOIN patient_registration pr ON(pd.mr_no = pr.mr_no) "
	 		+ " JOIN bill b ON(b.visit_id = pr.patient_id) "
	 		+ " LEFT JOIN LATERAL(SELECT sum(COALESCE(pds.amount,0))AS ip_unrealized_amount "
	 		+ "  FROM patient_ip_deposits_view pds WHERE pds.realized<>'Y'::bpchar "
	 		+ "  AND pds.mr_no=pd.mr_no)AS ip_unra ON TRUE "
	 		+ " LEFT JOIN LATERAL(SELECT sum(COALESCE(pas.amount,0))AS total_ip_deposits FROM "
	 		+ "  patient_ip_deposits_view pas WHERE pas.realized='Y'::bpchar "
	 		+ "  AND pas.mr_no=pd.mr_no)AS total_ip_dep ON TRUE "
	 		+ " WHERE b.bill_no = ? AND(pd.center_id IS NULL OR pd.center_id= ?) "
	 		+ " GROUP BY pd.mr_no,total_ip_deposits,ip_unrealized_amount,"
	 		+ "  pd.center_id, b.bill_no)AS ipdeposits"
	 		+ " LEFT JOIN LATERAL(SELECT pr.mr_no,sum(b.ip_deposit_set_off)AS total_ip_set_offs "
	 		+ "  FROM bill b JOIN patient_registration pr ON(b.visit_id=pr.patient_id) "
	 		+ "  WHERE pr.visit_type='i' AND b.ip_deposit_set_off>0 AND pr.mr_no=ipdeposits.mr_no "
	 		+ "  GROUP BY pr.mr_no)AS setoffs ON TRUE "
	 		+ " LEFT JOIN LATERAL(SELECT pr.mr_no,sum(b.ip_deposit_set_off)AS "
	 		+ "  total_ip_set_offs_non_ip_bills FROM bill b JOIN patient_registration pr "
	 		+ "  ON(b.visit_id=pr.patient_id)WHERE pr.visit_type!='i' AND b.ip_deposit_set_off>0 "
	 		+ "  AND pr.mr_no=ipdeposits.mr_no GROUP BY pr.mr_no)AS non_ip_bill_setoffs ON TRUE "
	 		+ " ORDER BY ipdeposits.mr_no";

			 public static BasicDynaBean getIPBillDepositDetails(String billNo,int centerId) throws SQLException {
			return DataBaseUtil.queryToDynaBean(IP_DEPOSIT_DETAILS_CENTER_WISE, new Object[] {billNo,centerId});
		}

	 public static BasicDynaBean getIPBillDepositDetails(Connection con, String billNo,int centerId) throws SQLException {
		    return DataBaseUtil.queryToDynaBean(con, IP_DEPOSIT_DETAILS_CENTER_WISE, new Object[] {billNo,centerId});
		  }


	private static final String GET_PATIENT_DEPOSIT_DETAILS =
		"SELECT mr_no, hosp_total_deposits AS total_deposits, " +
		"  hosp_total_setoffs as total_deposit_set_off, hosp_total_balance as total_balance "
		+" FROM deposit_setoff_total "
		+" WHERE mr_no=?";

	private static final String GET_PATIENT_DEPOSITS_EXCL_PACKAGES =
	    "SELECT dst.mr_no, dst.hosp_total_deposits -coalesce( mdv.package_deposits, 0) as total_deposits, " +
	    " dst.hosp_total_setoffs - coalesce(mdv.package_set_offs,0) as total_deposit_set_off, " +
	    " dst.hosp_total_balance - coalesce(mdv.package_balance, 0) as total_balance from " +
	    " deposit_setoff_total dst LEFT JOIN " +
	    " (select mr_no, coalesce(sum(total_deposits), 0) as package_deposits, " +
	    " coalesce(sum(total_set_offs), 0) as package_set_offs, " +
	    " coalesce(sum(total_deposits), 0) - coalesce(sum(total_set_offs), 0) as package_balance " +
	    " FROM multivisit_deposits_view GROUP BY mr_no) as mdv " +
	    " ON dst.mr_no = mdv.mr_no WHERE dst.mr_no = ?";

    public BasicDynaBean getPatientDepositDetails(String mrno, boolean excludePackageDeposits) throws SQLException {
	    if (excludePackageDeposits) {
	    	return DataBaseUtil.queryToDynaBean(GET_PATIENT_DEPOSITS_EXCL_PACKAGES, mrno);
	    } else {
	    	return DataBaseUtil.queryToDynaBean(GET_PATIENT_DEPOSIT_DETAILS, mrno);
	    }
	}

    public BasicDynaBean getPatientDepositDetails(String mrno) throws SQLException {
    	return getPatientDepositDetails(mrno, false);
    }

    private static final String GET_PATIENT_DEPOSITS_EXCL_IP_AND_PACKAGES =
	    "SELECT dst.mr_no, dst.hosp_total_deposits -coalesce( mdv.package_deposits, 0) - coalesce( ipd.ip_deposits, 0) as total_deposits, " +
	    " dst.hosp_total_setoffs - coalesce(mdv.package_set_offs,0) - coalesce(ipd.ip_set_offs,0) as total_deposit_set_off, " +
	    " dst.hosp_total_balance - coalesce(mdv.package_balance, 0) - coalesce(ipd.ip_balance, 0) as total_balance from " +
	    " deposit_setoff_total dst LEFT JOIN " +
	    " (select mr_no, coalesce(sum(total_deposits), 0) as package_deposits, " +
	    " coalesce(sum(total_set_offs), 0) as package_set_offs, " +
	    " coalesce(sum(total_deposits), 0) - coalesce(sum(total_set_offs), 0) as package_balance " +
	    " FROM multivisit_deposits_view GROUP BY mr_no) as mdv " +
	    " ON dst.mr_no = mdv.mr_no "+
	    " LEFT JOIN "+
	    " (" +
	    " SELECT mr_no, coalesce(sum(total_ip_deposits), 0) as ip_deposits, "+
		" coalesce(sum(total_ip_set_offs), 0) as ip_set_offs, " +
		" coalesce(sum(total_ip_deposits), 0) - coalesce(sum(total_ip_set_offs), 0) as ip_balance " +
		" FROM ip_deposits_view GROUP BY mr_no ) as ipd  "+
		" ON ipd.mr_no = dst.mr_no "+
	    " WHERE dst.mr_no = ?" ;

    public BasicDynaBean getPatientDepositDetails(String mrno, boolean excludePackageDeposits, String visitType) throws SQLException {
    	if (excludePackageDeposits) {
			if(visitType.equals("i"))
				return DataBaseUtil.queryToDynaBean(GET_PATIENT_DEPOSITS_EXCL_PACKAGES, mrno);
			else
				return DataBaseUtil.queryToDynaBean(GET_PATIENT_DEPOSITS_EXCL_IP_AND_PACKAGES, mrno);
		}
		return DataBaseUtil.queryToDynaBean(GET_PATIENT_DEPOSIT_DETAILS, mrno);
	}

	private static final String ALL_DEPOSITS =
		" SELECT r.payment_type, r.receipt_no, r.amount, "
		+ "		date(r.display_date) as display_date, r.counter,  "
		+ " 	r.payment_mode, r.bank_name, r.reference_no, r.username, r.remarks,  "
		+ " 	r.status,  r.mr_no, r.salutation, r.patient_name, "
		+ "	r.last_name, r.dob, r.patient_gender, r.patient_full_name, "
		+ " payment_mode_account, r.bank_name as bank, ref_required, bank_required, hcm.center_code "
		+ " FROM deposits_receipts_view r "
		+ "  JOIN counter_associated_accountgroup_view cav On r.counter = counter_id"
		+ "  JOIN hospital_center_master hcm ON (hcm.center_id=r.center_id) ";

	public static List getAllDeposits(Connection con, java.sql.Timestamp fromDate,
			java.sql.Timestamp toDate, Integer accountGroup, int centerId, List receiptNos) throws SQLException {
		//PreparedStatement ps = null;
		List<Object> args = new ArrayList<Object>();
		try {
			StringBuilder where = new StringBuilder();
			where.append(" WHERE payment_type IN ('DR', 'DF')");

			if (fromDate != null && toDate != null) {
				where.append(" AND r.mod_time BETWEEN ? AND ? AND COALESCE(cav.account_group_id, 1)=? ");
				if (centerId != 0)
					where.append(" AND r.center_id=?");
			} else {
				if (receiptNos == null || receiptNos.isEmpty()) {
				  return Collections.EMPTY_LIST;
				}
				  //DataBaseUtil.addWhereFieldInList(where, "r.receipt_no", receiptNos);
				  String[] placeHolderArr = new String[receiptNos.size()];
				  Arrays.fill(placeHolderArr, "?");
				  String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
				  where.append("AND r.receipt_no in ( " + placeHolders  + ")");
			}

			String query = ALL_DEPOSITS + where.toString();
			//ps = con.prepareStatement(ALL_DEPOSITS + where.toString());
			if (fromDate != null && toDate != null) {
			  args.add(fromDate);
			  args.add(toDate);
			  args.add(accountGroup);
				//ps.setTimestamp(1, fromDate);
				//ps.setTimestamp(2, toDate);
				//ps.setInt(3, accountGroup);
				if (centerId != 0) {
					//ps.setInt(4, centerId);
				  args.add(centerId);
				}
			} else {
				Iterator it =  receiptNos.iterator();
				int i = 1;
				List<String> voucher = new ArrayList<>();
				while (it.hasNext()) {
				  voucher.add((String) ((Map) it.next()).get("voucher_no"));
					//ps.setString(i++, (String) ((Map) it.next()).get("voucher_no"));
				}
				args.addAll(voucher);
			}
			//return DataBaseUtil.queryToDynaList(ps);
			return DataBaseUtil.queryToDynaList(query, args.toArray());
		} finally {
			DataBaseUtil.closeConnections(null, null);
		}
	}

	public static final String PACKAGE_DEPOSIT_DETAILS =
		" SELECT pr.mr_no, b.visit_id, b.bill_no, coalesce(b.deposit_set_off, 0) as deposit_set_off, "
		+ " coalesce(b.ip_deposit_set_off, 0) as ip_deposit_set_off, "
		+ " coalesce(mdv.total_deposits, 0) as total_deposits, "
		+ " coalesce(mdv.total_set_offs, 0) as total_set_offs, "
		+ " coalesce(mdv.total_set_offs, 0) as total_deposit_setoffs, mdv.package_id, coalesce(pcpd.package_name, "
		+  "pm.package_name) as package_name,"
    	+ " (mdv.total_deposits-mdv.total_set_offs) as package_unallocated_amount"
		+ " FROM bill b JOIN patient_registration pr ON (b.visit_id = pr.patient_id) "
		+ " LEFT JOIN multivisit_deposits_view as mdv "
+ " ON (mdv.mr_no = pr.mr_no) "
+ " JOIN packages pm ON(pm.package_id = mdv.package_id) "
+ " LEFT JOIN patient_customised_package_details pcpd ON (mdv.pat_package_id = pcpd.patient_package_id)"
+ " WHERE b.bill_no = ? and mdv.package_id = ? and mdv.pat_package_id = ? ";

	public static final String BILL_MULTIVISIT_PACKAGE =
		"SELECT distinct pack_id as package_id FROM multivisit_bills_view " +
		"WHERE bill_no = ?"; 
	
	
	public static final String BILL_MULTIVISIT_PACKAGE_FOR_MRNO = 
			" SELECT distinct orders.package_id, orders.pat_package_id  "+
			" FROM ( "+
				" SELECT sp.common_order_id as common_order_id, p.package_id, pp.pat_package_id "+
				" FROM services_prescribed sp "+
				" JOIN bill_charge bc ON(bc.order_number = sp.common_order_id) "+
				" JOIN package_prescribed pp ON (sp.package_ref = pp.prescription_id) "+
				" JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
				" WHERE bc.bill_no = ?  "+
				" UNION ALL "+
				" SELECT tp.common_order_id as common_order_id, p.package_id, pp.pat_package_id "+
				" FROM tests_prescribed tp "+ 
				" JOIN bill_charge bc ON(bc.order_number = tp.common_order_id) "+
				" JOIN package_prescribed pp ON (tp.package_ref = pp.prescription_id) "+
				" JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
				" WHERE bc.bill_no = ?  "+
				" UNION ALL "+
				" SELECT dc.common_order_id as common_order_id, p.package_id, pp.pat_package_id "+
				" FROM doctor_consultation dc "+
				" JOIN bill_charge bcc ON (bcc.order_number = dc.common_order_id) "+
				" JOIN package_prescribed pp ON (dc.package_ref = pp.prescription_id) "+
				" JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
				" LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text AND bac.activity_code='DOC' "+
				" LEFT JOIN bill_charge bc ON (bc.charge_id = bac.charge_id) "+
				" WHERE bcc.bill_no = ?  "+
				" UNION ALL "+
				" SELECT osp.common_order_id as common_order_id, p.package_id, pp.pat_package_id "+
				" FROM other_services_prescribed osp "+ 
				" JOIN bill_charge bc ON(bc.order_number = osp.common_order_id) "+
				" JOIN package_prescribed pp ON (osp.package_ref = pp.prescription_id) "+
				" JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "+
				" WHERE bc.bill_no = ?  "+
			" ) as orders " ;

	public static BasicDynaBean getPackageDepositDetails(String billNo) throws SQLException {
		BasicDynaBean b = DataBaseUtil.queryToDynaBean(BILL_MULTIVISIT_PACKAGE_FOR_MRNO, new Object[] {billNo, billNo, billNo, billNo});
		if (null != b) {
			List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(PACKAGE_DEPOSIT_DETAILS,
					new Object[]{billNo, b.get("package_id"), b.get("pat_package_id")});
			if (list.size() > 0) return list.get(0);
		}
		return null;
	}

	public static BasicDynaBean getMultiPackageDepositDetails(String billNo) throws SQLException {
		BasicDynaBean b = DataBaseUtil.queryToDynaBean(BILL_MULTIVISIT_PACKAGE_FOR_MRNO, new Object[] {billNo,billNo,billNo,billNo});
		if (null != b) {
			List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(PACKAGE_DEPOSIT_DETAILS,
					new Object[]{billNo, b.get("package_id"), b.get("pat_package_id")});
			if (list.size() > 0) return list.get(0);
		}
		return null;
	}

	public static BasicDynaBean getMultiPackageDepositDetails(Connection con, String billNo) throws SQLException {
    BasicDynaBean b = DataBaseUtil.queryToDynaBean(con,BILL_MULTIVISIT_PACKAGE_FOR_MRNO, new Object[] {billNo,billNo,billNo,billNo});
    if (null != b) {
      List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(con,PACKAGE_DEPOSIT_DETAILS,
          new Object[]{billNo, b.get("package_id"), b.get("pat_package_id")});
      if (list.size() > 0) return list.get(0);
    }
    return null;
  }


	public static final String PACKAGE_DEPOSIT_DETAILS_CENTER_WISE =
		     " SELECT mdv.center_id,pr.mr_no, b.visit_id, b.bill_no, coalesce(b.deposit_set_off, 0) as deposit_set_off, "
		    + " coalesce(b.ip_deposit_set_off, 0) as ip_deposit_set_off, "
		    + " coalesce(mdv.total_deposits, 0) as total_deposits, "
		    + " coalesce(mdv.total_set_offs, 0) as total_set_offs, "
		    + " coalesce(mdv.total_set_offs, 0) as total_deposit_setoffs, mdv.package_id, pm.package_name, "
		    + " mdv.package_unallocated_amount "
		    + " FROM bill b JOIN patient_registration pr ON (b.visit_id = pr.patient_id) "
		    + " LEFT JOIN multivisit_deposits_center_wise_view mdv "
		+ " ON (mdv.mr_no = pr.mr_no) "
		+ " JOIN packages pm ON(pm.package_id = mdv.package_id::INTEGER) "
		+ " WHERE (mdv.center_id IS NULL OR mdv.center_id=?) AND b.bill_no = ? AND mdv.package_id = ? ";


	public static BasicDynaBean getPackageDepositDetails(String billNo,int centerId) throws SQLException {
		BasicDynaBean b = DataBaseUtil.queryToDynaBean(BILL_MULTIVISIT_PACKAGE_FOR_MRNO, new Object[] {billNo, billNo, billNo, billNo});
		if (null != b) {
			List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(PACKAGE_DEPOSIT_DETAILS_CENTER_WISE,
					new Object[]{centerId,billNo, b.get("package_id")});
			if (list.size() > 0) return list.get(0);
		}
		return null;
	}

	public static BasicDynaBean getMultiPackageDepositDetails(Connection con, String billNo,int centerId) throws SQLException {
	    BasicDynaBean b = DataBaseUtil.queryToDynaBean(con,BILL_MULTIVISIT_PACKAGE_FOR_MRNO, new Object[] {billNo,billNo,billNo,billNo});
	    if (null != b) {
	      List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(con,PACKAGE_DEPOSIT_DETAILS_CENTER_WISE,
	          new Object[]{centerId,billNo, b.get("package_id")});
	      if (list.size() > 0) return list.get(0);
	    }
	    return null;
	  }


	public static final String DEPOSIT_PATIENTS_FIELDS = " SELECT * ";
	public static final String DEPOSIT_PATIENTS_COUNT = " SELECT count(mr_no)";
    //public static final String DEPOSIT_PATIENTS_TABLES = " FROM patient_deposit_details_view ";

        public static final String DEPOSIT_PATIENTS_TABLES = " FROM (SELECT pddv.*, coalesce(package_deposits, 0) as package_deposit, " +
	    "coalesce(package_setoffs, 0) as package_setoff from patient_deposit_details_view pddv " +
	    "LEFT JOIN (SELECT mr_no, sum(total_deposits) as package_deposits, sum(total_set_offs) as package_setoffs " +
	    "FROM multivisit_deposits_view mdv GROUP BY mr_no) as package_deposits ON (pddv.mr_no = package_deposits.mr_no)) " +
	    "as foo";

	public static PagedList searchPatients(Map filter, Map listing)
			throws SQLException, ParseException {


		String sortField = (String) listing.get(LISTING.SORTCOL);
		boolean sortReverse = (Boolean) listing.get(LISTING.SORTASC);
		int pageSize = (Integer) listing.get(LISTING.PAGESIZE);
		int pageNum = (Integer) listing.get(LISTING.PAGENUM);

		Connection con = null;
		try{
		  con = DataBaseUtil.getReadOnlyConnection();

	    SearchQueryBuilder qb = new SearchQueryBuilder(con,
	        DEPOSIT_PATIENTS_FIELDS, DEPOSIT_PATIENTS_COUNT,
	        DEPOSIT_PATIENTS_TABLES, null, null, sortField, sortReverse,
	        pageSize, pageNum);

	    qb.addFilterFromParamMap(filter);
	    qb.build();

	    PagedList l = null;

	    try(PreparedStatement psData = qb.getDataStatement();
	        PreparedStatement psCount = qb.getCountStatement();){
	      List list = DataBaseUtil.queryToDynaList(psData);

	      int totalCount = 0;
	      try(ResultSet rsCount = psCount.executeQuery();){
	        if (rsCount.next()) {
	          totalCount = rsCount.getInt(1);
	        }
	      }

	      l = new PagedList(list, totalCount, pageSize, pageNum);

	      qb.close();
	      return l;
	    }
		}finally{
		  DataBaseUtil.closeConnections(con, null);
		}

	}

	public static final String GET_PAT_DEPOSITS = "SELECT * FROM patient_deposit_details_view WHERE mr_no=?";

	public static BasicDynaBean getDepositAmounts(String mrNo) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_PAT_DEPOSITS, mrNo);
	}

	public static BasicDynaBean getDepositAmounts(Connection con, String mrNo) throws SQLException {

	  List l = DataBaseUtil.queryToDynaList(con, GET_PAT_DEPOSITS, mrNo);
    if (l.size() > 0)
      return (BasicDynaBean) l.get(0);
    return null;
	}

	public static final String GET_CENTER_PAT_DEPOSITS = "SELECT "
			+ " COALESCE(SUM(CASE WHEN pdv.realized='Y' "
			+ " AND pdv.receipt_type!='F' THEN pdv.unallocated_amount "
			+ " ELSE 0 END),0::numeric)+COALESCE(SUM(allocated_amount),0::numeric) AS hosp_total_deposits, "
			+ " COALESCE(SUM(allocated_amount),0::numeric) AS hosp_total_setoffs, "
			+ " COALESCE(SUM(CASE WHEN pdv.realized='Y' AND pdv.receipt_type !='F' THEN "
			+ " pdv.unallocated_amount ELSE 0 END),0::numeric) AS hosp_total_balance, "
			+ " COALESCE(SUM(CASE WHEN pdv.realized <> 'Y' "
			+ " THEN pdv.amount ELSE 0 END),0::numeric) AS hosp_unrealized_amount "
			+ " FROM patient_deposits_view pdv "
			+ " LEFT JOIN LATERAL (SELECT receipt_no , SUM(allocated_amount) AS allocated_amount "
			+ " FROM bill_receipts WHERE  pdv.receipt_id = receipt_no  AND pdv.is_deposit = true "
			+ " GROUP BY receipt_no) rc ON pdv.receipt_id = rc.receipt_no "
			+ " WHERE mr_no=? AND (center_id IS NULL OR center_id=?) ";

	public static BasicDynaBean getDepositAmounts(String mrNo,int centerId) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_CENTER_PAT_DEPOSITS, new Object[] {mrNo, centerId});
	}

	public static final String GET_AVAILABLE_GENERAL_AND_IP_DEPOSIT = "SELECT patDepDet.hosp_total_balance - "
	    + " SUM( COALESCE(foo.total_deposits,0) - COALESCE(foo.total_set_offs,0) ) AS hosp_available_deposit "
	    + " FROM patient_deposit_details_view patDepDet "
	    + " LEFT JOIN LATERAL (SELECT deposits.*, setoffs.total_set_offs FROM ( "
	    + " SELECT pd.mr_no, pd.package_id, sum(pd.amount) as total_deposits "
	    + " FROM patient_package_deposits_view pd WHERE patDepDet.mr_no = pd.mr_no"
	    + " GROUP BY pd.mr_no, pd.package_id) as deposits"
	    + " LEFT JOIN LATERAL ("
	    + " SELECT r.mr_no, package_bills.package_id as package_id, sum(b.deposit_set_off) as total_set_offs"
	    + " FROM bill b JOIN patient_registration r ON (b.visit_id = r.patient_id)"
	    + " JOIN patient_details p ON p.mr_no = r.mr_no"
	    + " JOIN LATERAL (SELECT distinct foo1.bill_no, foo1.package_id from (SELECT orders.bill_no,orders.package_id "
	    + "     FROM (SELECT bc.bill_no, p.package_id"
	    + "     FROM services_prescribed sp "
	    + "     JOIN bill_charge bc ON(bc.order_number = sp.common_order_id)"
	    + "     JOIN package_prescribed pp ON (sp.package_ref = pp.prescription_id)"
	    + "     JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true)"
	    + "     where b.bill_no = bc.bill_no"
	    + "     UNION ALL"
	    + "     SELECT bc.bill_no,p.package_id"
	    + "     FROM tests_prescribed tp"
	    + "     JOIN bill_charge bc ON(bc.order_number = tp.common_order_id)"
	    + "     JOIN package_prescribed pp ON (tp.package_ref = pp.prescription_id)"
	    + "     JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true)"
	    + "     where b.bill_no = bc.bill_no"
	    + "     UNION ALL"
	    + "     SELECT bcc.bill_no,p.package_id"
	    + "     FROM doctor_consultation dc "
	    + "     JOIN bill_charge bcc ON(bcc.order_number = dc.common_order_id)"
	    + "     JOIN package_prescribed pp ON (dc.package_ref = pp.prescription_id)"
	    + "     JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true)"
	    + "     LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text AND bac.activity_code='DOC'"
	    + "     LEFT JOIN bill_charge bc ON (bc.charge_id = bac.charge_id)"
	    + "     where b.bill_no = bc.bill_no"
	    + "     UNION ALL"
	    + "     SELECT bc.bill_no,p.package_id"
	    + "     FROM other_services_prescribed osp "
	    + "     JOIN bill_charge bc ON (bc.order_number = osp.common_order_id)"
	    + "     JOIN package_prescribed pp ON (osp.package_ref = pp.prescription_id)"
	    + "     JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true)"
	    + "     where b.bill_no = bc.bill_no"
	    + "     ) as orders) as foo1)  AS package_bills"
	    + " ON (b.bill_no = package_bills.bill_no)"
	    + " WHERE (patient_confidentiality_check(COALESCE(p.patient_group, 0), p.mr_no) and deposits.mr_no = r.mr_no) "
	    + " GROUP BY r.mr_no, package_bills.package_id )AS setoffs"
	    + " ON (deposits.mr_no = setoffs.mr_no AND deposits.package_id::INTEGER = setoffs.package_id)) as foo"
	    + " ON (foo.mr_no = patDepDet.mr_no)"
	    + " WHERE patDepDet.mr_no= ?"
	    + " GROUP BY foo.mr_no, patDepDet.hosp_total_balance ;";

	public static BasicDynaBean getAvailableGeneralAndIpDeposit(String mrNo) throws SQLException {
    return DataBaseUtil.queryToDynaBean(GET_AVAILABLE_GENERAL_AND_IP_DEPOSIT, mrNo);
  }

	public static BasicDynaBean getAvailableGeneralAndIpDeposit(Connection con, String mrNo) throws SQLException {
    return DataBaseUtil.queryToDynaBean(con,GET_AVAILABLE_GENERAL_AND_IP_DEPOSIT, mrNo);
  }

	/*
	 * Deposit summary (count and total) between two dates: used by CFD
	 */
	public static final String DEPOSIT_SUMMARY = " SELECT count(*) as count, sum(amount) as total "
			+ " FROM patient_deposits_view pd"
			+ " JOIN counters cs ON cs.counter_id = pd.counter "
			+ " WHERE pd.is_deposit AND date(display_date) BETWEEN ? AND ? ";

	public static BasicDynaBean getDepositSummary(java.sql.Date fromDate,
			java.sql.Date toDate, int centerId) throws SQLException {
		String query = centerId == 0 ? DEPOSIT_SUMMARY : DEPOSIT_SUMMARY + " AND cs.center_id = "+ centerId ;
		List l = DataBaseUtil.queryToDynaListDates(query, fromDate, toDate);
		if (l.size() > 0)
			return (BasicDynaBean) l.get(0);
		return null;
	}

	/*
	 * Deposit Receipts (count and total) between two dates: used by CFD
	 */
	public static final String DEPOSIT_RECEIPTS_SUMMARY = " SELECT count(*) as count, sum(amount) as total "
			+ " FROM patient_deposits_view pd"
			+" JOIN counters cs ON cs.counter_id = pd.counter "
			+ " WHERE receipt_type='R' AND is_deposit AND date(display_date) BETWEEN ? AND ? ";

	public static BasicDynaBean getDepositReceiptsSummary(
			java.sql.Date fromDate, java.sql.Date toDate, int centerId) throws SQLException {
		String query = centerId == 0 ? DEPOSIT_RECEIPTS_SUMMARY : DEPOSIT_RECEIPTS_SUMMARY + " AND cs.center_id = "+ centerId ;
		List l = DataBaseUtil.queryToDynaListDates(query,
				fromDate, toDate);
		if (l.size() > 0)
			return (BasicDynaBean) l.get(0);
		return null;
	}

	/*
	 * Deposit Refunds (count and total) between two dates: used by CFD
	 */
	public static final String DEPOSIT_REFUNDS_SUMMARY = " SELECT count(*) as count, sum(amount) as total "
			+ " FROM patient_deposits_view pd "
			+" JOIN counters cs ON cs.counter_id = pd.counter "
			+ " WHERE receipt_type='F' AND is_deposit AND date(display_date) BETWEEN ? AND ? ";

	public static BasicDynaBean getDepositRefundsSummary(
			java.sql.Date fromDate, java.sql.Date toDate, int centerId) throws SQLException {
		String query = centerId == 0 ? DEPOSIT_REFUNDS_SUMMARY : DEPOSIT_REFUNDS_SUMMARY + " AND cs.center_id = "+ centerId ;
		List l = DataBaseUtil.queryToDynaListDates(query, fromDate,
				toDate);
		if (l.size() > 0)
			return (BasicDynaBean) l.get(0);
		return null;
	}

	/*
	 * Deposit set offs (used up in Bill Now bills) between two dates: used by CFD
	 */
	public static final String GET_DEPOSIT_SETOFFS =
		" SELECT count(bill_no) as count, coalesce(sum(deposit_set_off),0) as amount "
		+ " FROM bill b "
		+ " LEFT JOIN u_user u ON u.emp_username = b.username "
		+ " LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id "
		+ " LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id "
		+ " LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = b.visit_id"
		+ " WHERE b.bill_type != 'C' AND b.status = 'C'  "
		+ "   AND b.deposit_set_off > 0 "
		+ "   AND date(b.finalized_date) BETWEEN ? AND ? "
		+ "   AND (?=0 OR b.account_group=?) AND (?=0 OR COALESCE(pr.center_id,prc.center_id,isr.center_id,u.center_id) =?) ";

	public static BasicDynaBean getDepositSetOffs(java.sql.Date from, java.sql.Date to, int accountGroup, int centerId)
		throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_DEPOSIT_SETOFFS,
				new Object[] {from, to, accountGroup, accountGroup, centerId, centerId });
	}

	public static final String EXCLUDE_BILL_FROM_TOTAL_DEPOSIT_SETOFF =
		 " SELECT hosp_total_setoffs , "
		+ " (SELECT deposit_set_off FROM bill WHERE bill_no = ? AND status != 'X') AS deposit_set_off "
		+ " FROM deposit_setoff_total where mr_no = ? ";

	public static BasicDynaBean getDepositSetOffBillExcluded(String mrNo, String billNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(EXCLUDE_BILL_FROM_TOTAL_DEPOSIT_SETOFF);
			ps.setString(1, billNo);
			ps.setString(2, mrNo);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static BasicDynaBean getDepositSetOffBillExcluded(Connection con, String mrNo, String billNo) throws SQLException {

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(EXCLUDE_BILL_FROM_TOTAL_DEPOSIT_SETOFF);
			ps.setString(1, billNo);
			ps.setString(2, mrNo);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			if(ps!=null) ps.close();
		}
	}

	public static final String EXCLUDE_BILL_FROM_TOTAL_IPDEPOSIT_SETOFF =
		 " SELECT total_ip_set_offs , "
		+ " (SELECT ip_deposit_set_off FROM bill WHERE bill_no = ? AND status != 'X') AS ip_deposit_set_off "
		+ " FROM ip_deposits_view where mr_no = ? ";

	public static BasicDynaBean getIPDepositSetOffBillExcluded(String mrNo, String billNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(EXCLUDE_BILL_FROM_TOTAL_IPDEPOSIT_SETOFF);
			ps.setString(1, billNo);
			ps.setString(2, mrNo);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static BasicDynaBean getIPDepositSetOffBillExcluded(Connection con, String mrNo, String billNo) throws SQLException {

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(EXCLUDE_BILL_FROM_TOTAL_IPDEPOSIT_SETOFF);
			ps.setString(1, billNo);
			ps.setString(2, mrNo);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			if(ps!=null) ps.close();
		}
	}

	public static final String DEPOSIT_RECEIPT_REFUND_PRINT ="SELECT receipt_id as deposit_no, pd.mr_no, receipt_type as deposit_type, "+
		" pd.amount, display_date as deposit_date, counter, bank_name, reference_no, pd.created_by as username, pd.remarks, pd.modified_at as mod_time, "+
		" deposit_avalibility, pd.created_by as deposit_payer_name, mob_number as payer_phone_no, '' as payer_address,"+
	 	" pd.payment_mode_id, pd.card_type_id,pm.payment_mode AS payment_mode_name ,cm.card_type,  "+
		" get_patient_name(pde.salutation, pde.patient_name, pde.middle_name, pde.last_name) as patient_name, "+
		" get_patient_age(pde.dateofbirth, pde.expected_dob) as patient_age, "+
		" get_patient_age_in(pde.dateofbirth, pde.expected_dob) as patient_age_in, "+
		" pde.patient_gender,  pde.patient_phone, " +
		" (CASE WHEN pd.payment_mode_id = -1 THEN 'C' " +
		"	   WHEN  pd.payment_mode_id = 1  THEN 'R' " +
		"	   WHEN  pd.payment_mode_id = 2  THEN 'B' " +
		"	   WHEN  pd.payment_mode_id = 3  THEN 'Q' " +
		"	   WHEN  pd.payment_mode_id = 4  THEN 'D' ELSE 'U' END) AS payment_mode, " +
		" bank_batch_no, card_auth_code, card_holder_name, pd.currency_id, pd.exchange_rate, " +
		" pd.exchange_date, currency_amt, currency, card_number, card_exp_date as card_expdate," +
		" COALESCE(pcpd.package_name, p.package_name) as package_name, " +
		" pd.credit_card_commission_amount, pd.credit_card_commission_percentage, "+
		" CASE WHEN receipt_type='F' THEN -round(refund_tax_amount,2) ELSE "+
		" round((pd.amount - (pd.amount/(1+(total_tax_rate/100)))),2) END AS tax_amount, "+
		" CASE WHEN receipt_type='F' THEN refund_tax_rate ELSE total_tax_rate END "+
		" AS total_tax_rate, "+
		" round(pd.amount/(1+(total_tax_rate/100)),2) as net_amount "+
	 	"  FROM generic_preferences, patient_deposits_view pd " +
	 	"  JOIN patient_details pde ON (pde.mr_no=pd.mr_no) "+
	 	"  JOIN payment_mode_master pm ON (pm.mode_id = pd.payment_mode_id) " +
		"  LEFT JOIN card_type_master cm ON (cm.card_type_id = pd.card_type_id) " +
		"  LEFT JOIN foreign_currency fc ON (fc.currency_id = pd.currency_id) " +
		"  LEFT JOIN packages p ON (p.package_id = pd.package_id::INTEGER) " +
		"  LEFT JOIN patient_customised_package_details pcpd ON" +
		" (pcpd.patient_package_id = pd.pat_package_id::INTEGER)" +
		" WHERE receipt_id = ? ";

	public static List depositReceiptRefundPrint(String receiptNo) throws SQLException {
		return DataBaseUtil.queryToDynaList(DEPOSIT_RECEIPT_REFUND_PRINT, receiptNo);
	}


	public static final String DEPOSIT_RECEIPTS_FIELDS =
		"SELECT payment_type, receipt_no, amount, display_date, counter, counter_type, "
		+ " payment_mode, bank_name, reference_no, username, remarks, "
		+ " status, mr_no, patient_full_name, dob, patient_gender, counter_type,"
		+ " center_name,receipt_center_id ";

	public static final String DEPOSIT_RECEIPTS_COUNT = "SELECT count(receipt_no)";

	public static final String DEPOSIT_RECEIPTS_TABLES = " "
			+ " FROM deposits_receipts_view ";

	public static PagedList getDepositsReceiptsPagedList(Map filter, Map listing)
			throws SQLException, ParseException {

		Connection con = DataBaseUtil.getReadOnlyConnection();

		SearchQueryBuilder qb = new SearchQueryBuilder(con,
				DEPOSIT_RECEIPTS_FIELDS, DEPOSIT_RECEIPTS_COUNT,
				DEPOSIT_RECEIPTS_TABLES, listing);

		qb.addFilterFromParamMap(filter);
		if (RequestContext.getCenterId() != 0) {

		    StringBuilder qryExp = new StringBuilder("receipt_center_id = ");

        qryExp.append(RequestContext.getCenterId());
        qryExp.append(" OR receipt_center_id IS NULL");

      if(!qryExp.toString().isEmpty()) {
        qb.appendToQuery(" ( "+qryExp+" ) ");
      }
		}

		qb.build();

		PagedList l = qb.getMappedPagedList();

		qb.close();
		con.close();

		return l;
	}

	private static final String GET_IP_DEPOSIT_AMOUNT = "SELECT * FROM ip_deposits_view WHERE mr_no = ?";

	public static BasicDynaBean getIPDepositAmounts(String mrNo) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_IP_DEPOSIT_AMOUNT, mrNo);
	}

	public static BasicDynaBean getIPDepositAmounts(Connection con, String mrNo) throws SQLException {
		List<BasicDynaBean> ipDeposit = DataBaseUtil.queryToDynaList(con, GET_IP_DEPOSIT_AMOUNT, mrNo);
		return (null != ipDeposit && ipDeposit.size() > 0) ? ipDeposit.get(0) : null;
	}

	
	public static final String GET_CENTER_WISE_IP_DEPOSITS = "SELECT ipdeposits.*, "
			+ "COALESCE(setoffs.total_ip_set_offs,0)AS total_ip_set_offs, "
			+ " COALESCE(non_ip_bill_setoffs.total_ip_set_offs_non_ip_bills,0) "
			+ " AS total_ip_set_offs_non_ip_bill FROM "
			+ "  (SELECT pd.mr_no,COALESCE(total_ip_deposits,0)AS total_ip_deposits,"
			+ "  COALESCE(ip_unrealized_amount,0)AS ip_unrealized_amount,pd.center_id FROM "
			+ "  patient_ip_deposits_view pd "
			+ "LEFT JOIN LATERAL(SELECT sum(COALESCE(pds.amount,0))AS ip_unrealized_amount "
			+ "  FROM patient_ip_deposits_view pds WHERE pds.realized<>'Y'::bpchar "
			+ "  AND pds.mr_no=pd.mr_no)AS ip_unra ON TRUE "
			+ "LEFT JOIN LATERAL(SELECT sum(COALESCE(pas.amount,0))AS total_ip_deposits "
			+ "  FROM patient_ip_deposits_view pas WHERE pas.realized='Y'::bpchar "
			+ "  AND pas.mr_no=pd.mr_no)AS total_ip_dep ON TRUE "
			+ "GROUP BY pd.mr_no,total_ip_deposits,ip_unrealized_amount,pd.center_id)AS ipdeposits "
			+ "LEFT JOIN LATERAL(SELECT pr.mr_no,sum(b.ip_deposit_set_off)AS total_ip_set_offs "
			+ "  FROM bill b JOIN patient_registration pr ON(b.visit_id=pr.patient_id) "
			+ "  WHERE pr.visit_type='i' AND b.ip_deposit_set_off>0 AND pr.mr_no=ipdeposits.mr_no "
			+ "GROUP BY pr.mr_no)AS setoffs ON TRUE "
			+ "LEFT JOIN LATERAL(SELECT pr.mr_no,sum(b.ip_deposit_set_off)AS "
			+ "  total_ip_set_offs_non_ip_bills FROM bill b JOIN patient_registration pr "
			+ "  ON(b.visit_id=pr.patient_id)WHERE pr.visit_type!='i' AND b.ip_deposit_set_off>0 "
			+ "  AND pr.mr_no=ipdeposits.mr_no GROUP BY pr.mr_no)AS non_ip_bill_setoffs ON TRUE "
			+ "WHERE ipdeposits.mr_no=? AND (ipdeposits.center_id IS NULL OR ipdeposits.center_id=?) "
			+ "ORDER BY ipdeposits.mr_no;";
			
	
	

	public static BasicDynaBean getIPDepositAmounts(String mrNo,int centerId) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_CENTER_WISE_IP_DEPOSITS, new Object[] {mrNo, centerId});
	}

	private static final String GET_MULTIVISIT_PKG_CENTER_WISE_DEPOSIT_AMOUNT =
			"   SELECT * FROM multivisit_deposits_center_wise_view WHERE mr_no = ? "
			+ " AND (center_id IS NULL OR center_id=?)";

	public static List<BasicDynaBean> getMultiVisitPkgDepositAmounts(String mrNo,int centerId) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_MULTIVISIT_PKG_CENTER_WISE_DEPOSIT_AMOUNT,new Object[] {mrNo,centerId});
	}

	private static final String GET_MULTIVISIT_PKG_LIST = " SELECT p.package_id, "
			+ " pp.is_discontinued, pp.discontinue_remark, pp.pat_package_id, "
			+ " COALESCE(pcpd.package_name, p.package_name) AS package_name, pp.status "
			+ " FROM packages p JOIN patient_packages pp ON p.package_id = pp.package_id "
			+ " LEFT JOIN patient_customised_package_details pcpd "
			+ "  ON pp.pat_package_id = pcpd.patient_package_id WHERE pp.mr_no = ? "
			+ " AND p.multi_visit_package ";
	
	public static List<BasicDynaBean> getMvpPatPackagesList(String mrNo) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_MULTIVISIT_PKG_LIST, new Object[] {mrNo});
	}


	public static BigDecimal getTotalAvailableDeposit(Connection con, String mrNo, String billNo) throws SQLException{

		BasicDynaBean depositBean =  DepositsDAO.getDepositAmounts(con,mrNo);

		BigDecimal totalDepositAmt = BigDecimal.ZERO;
		BigDecimal billDepositSetOffAmt = BigDecimal.ZERO;
		BigDecimal totalDepositSetOffAmt = BigDecimal.ZERO;
		BigDecimal depositSetOffAmt = BigDecimal.ZERO;

		if (depositBean!= null && depositBean.get("hosp_total_deposits") != null)
			totalDepositAmt = (BigDecimal)depositBean.get("hosp_total_deposits");

		// Get the total deposit set off for the patient and the current bill deposit set off.
		BasicDynaBean billDepositBean = DepositsDAO.getDepositSetOffBillExcluded(con,
				mrNo, billNo);

		if (billDepositBean != null) {
			totalDepositSetOffAmt =
				billDepositBean.get("hosp_total_setoffs") != null ? (BigDecimal)billDepositBean.get("hosp_total_setoffs") : BigDecimal.ZERO;
			billDepositSetOffAmt =
				billDepositBean.get("deposit_set_off") != null ? (BigDecimal)billDepositBean.get("deposit_set_off") : BigDecimal.ZERO;
		}

		// bill deposit set off excluded
		depositSetOffAmt = totalDepositSetOffAmt.subtract(billDepositSetOffAmt);

		// calculate deposit remaining after the current bill set off is deducted.
		BigDecimal depositAmtRemaining = totalDepositAmt.subtract(depositSetOffAmt);

		return depositAmtRemaining;
	}

	public static BigDecimal getTotalIPAvailableDeposit(Connection con, String mrNo, String billNo) throws SQLException{

		BasicDynaBean depositBean =  DepositsDAO.getIPDepositAmounts(con, mrNo);

		BigDecimal totalIPDepositAmt = BigDecimal.ZERO;
		BigDecimal ipBillDepositSetOffAmt = BigDecimal.ZERO;
		BigDecimal totalIPDepositSetOffAmt = BigDecimal.ZERO;
		BigDecimal ipDepositSetOffAmt = BigDecimal.ZERO;

		if (depositBean!= null && depositBean.get("total_ip_deposits") != null)
			totalIPDepositAmt = (BigDecimal)depositBean.get("total_ip_deposits");

		// Get the total deposit set off for the patient and the current bill deposit set off.
		BasicDynaBean billDepositBean = DepositsDAO.getIPDepositSetOffBillExcluded(con,
				mrNo, billNo);

		if (billDepositBean != null) {
			totalIPDepositSetOffAmt =
				billDepositBean.get("total_ip_set_offs") != null ? (BigDecimal)billDepositBean.get("total_ip_set_offs") : BigDecimal.ZERO;
				ipBillDepositSetOffAmt =
				billDepositBean.get("ip_deposit_set_off") != null ? (BigDecimal)billDepositBean.get("ip_deposit_set_off") : BigDecimal.ZERO;
		}

		// bill deposit set off excluded
		ipDepositSetOffAmt = totalIPDepositSetOffAmt.subtract(ipBillDepositSetOffAmt);

		BigDecimal depositAmtRemaining = totalIPDepositAmt.subtract(ipDepositSetOffAmt);

		return depositAmtRemaining;
	}

	private static final String INSERT_PATIENT_DEPOSITS_SETOFF = "INSERT INTO patient_deposits_setoff_adjustments(mr_no,bill_no,amount,deposit_for,is_multi_pkg)VALUES" +
			"(?,?,?,?,?)";
	public static boolean insertPatientDepositSetOffAdjustment(String mr_no, String bill_no,BigDecimal amount, String depositFor, boolean ismultipkg) throws SQLException{
		boolean status = false;
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con= DataBaseUtil.getConnection();
			ps= con.prepareStatement(INSERT_PATIENT_DEPOSITS_SETOFF);
			ps.setString(1, mr_no);
			ps.setString(2, bill_no);
			ps.setBigDecimal(3, amount);
			ps.setString(4, depositFor);
			ps.setBoolean(5, ismultipkg);

			int i = ps.executeUpdate();
			if(i>0){
				status = true;
			}
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return status;
	}


}
