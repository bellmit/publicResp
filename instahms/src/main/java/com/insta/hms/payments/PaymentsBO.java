package com.insta.hms.payments;


import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.stores.DirectStockEntryDAO;
import com.insta.hms.stores.StockEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
public class PaymentsBO {

	static Logger logger = LoggerFactory.getLogger(PaymentsBO.class);


	/*
	 * Inserting payment details when paying to the payees
	 */
	public static boolean makePayments(ArrayList<PaymentDetailsDTO> pd)throws SQLException{

			Connection con = null;
			String paymentId = null;
			String paytype = null;
			Boolean success = true;
			try {
				ArrayList partialRecords = new ArrayList();
				for (int i=0; i<pd.size(); i++) {
					partialRecords.add((PaymentDetailsDTO) pd.get(i));
					if (partialRecords.size() == 50 || i == pd.size()-1) {
						if (con == null){
							con = DataBaseUtil.getConnection();
							con.setAutoCommit(false);
						}
						if (PaymentsDAO.insertPaymentDetails(con, partialRecords) &&
								commonPaymentDetails(con, partialRecords)){
							partialRecords.clear();
							DataBaseUtil.commitClose(con,success);
							con = null;
						}else {
							success = false;
							DataBaseUtil.commitClose(con,success);
							break;
						}
					}
				}

			} finally{

			}
			return success;
	}


	/*************************************PAYMENT  VOUCHER CREATION*****************************************/

	public static String creatVoucher(PaymentsDTO payments,String payType, String screen, String voucherType, Integer centerId)throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean success = true;
		String voucherno = null;

		try{
			PaymentsDAO paymentsDAO = new PaymentsDAO(con);
			String newVoucherNo = paymentsDAO.getVoucherNo(centerId);
			payments.setVoucherNo(newVoucherNo);
			success = success && paymentsDAO.insertPayments(payments) &&
			paymentsDAO.updatePaymentDetails(payments,payType, screen, voucherType);

			if (payments.getPaymentType().equals("S") && payments.getDirectPayment().equals(""))
				success = success && paymentsDAO.updateInvoice(payments, payType);
			if(success){
				voucherno =  payments.getVoucherNo();
			}
		}catch (SQLException e) {
			logger.error("Unable to create voucher ", e);
			voucherno = null;
		}finally{
			DataBaseUtil.commitClose(con,success);
		}
		return voucherno;
	}

	/*******************************PAYMENT REVERSAL VOUCHER CREATION***************************************/

	public static String createReversalVoucher(PaymentsDTO payments,String payType,
			ArrayList<PaymentDetailsDTO> pd)throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean success = true;
		String voucherno = null;

		try{
			PaymentsDAO paymentsDAO = new PaymentsDAO(con);
			String newVoucherNo = paymentsDAO.getReversalVoucherNo();
			payments.setVoucherNo(newVoucherNo);
			Iterator<PaymentDetailsDTO>it = pd.iterator();
			while(it.hasNext()){
				PaymentDetailsDTO payDetails = it.next();
				payDetails.setPaymentId(paymentsDAO.getPaymentId());
				payDetails.setVoucherNo(newVoucherNo);
			}
			success = success && paymentsDAO.insertPayments(payments) &&
					paymentsDAO.insertPaymentDetails(con, pd);
			if(success){
				voucherno =  payments.getVoucherNo();
			}
		}finally{
			DataBaseUtil.commitClose(con,success);
		}
		return voucherno;
	}

	/*
	 * Common method for inserting doc_payment_id into bill_charge and other back-reference tables
	 *
	 */
	public static boolean commonPaymentDetails(Connection con, ArrayList<PaymentDetailsDTO> payments)
		throws SQLException{

		boolean success = true;

		PaymentsDAO pddao = new PaymentsDAO(con);
		ChargeDAO cdao = new ChargeDAO(con);
		BillActivityChargeDAO bacDao = new BillActivityChargeDAO(con);
		DirectStockEntryDAO dstockDao = new DirectStockEntryDAO(con);

		ArrayList<ChargeDTO> charge = new ArrayList<ChargeDTO>();
		ArrayList<StockEntry> grn = new ArrayList<StockEntry>();

		Iterator<PaymentDetailsDTO> it = payments.iterator();

		while (it.hasNext()) {
			PaymentDetailsDTO pddto = it.next();

			if (pddto.getPaymentType().equals("D")) {
				if (pddto.getPackagCharge().equals("Y")) {
					bacDao.updateActivityPaymentDetails(con, pddto.getPkgActivityCode(),
							pddto.getPkgActivityId(), pddto.getAmount(), pddto.getPaymentId());
				} else {
					cdao.updateDoctorPaymentDetails(con, pddto.getChargeId(),
							pddto.getAmount(), pddto.getPaymentId());
				}

			} else if (pddto.getPaymentType().equals("R") || pddto.getPaymentType().equals("F") ) {
				cdao.updateRefDocPaymentDetails(con, pddto.getChargeId(),
						pddto.getAmount(), pddto.getPaymentId());

			} else if (pddto.getPaymentType().equals("P")) {
				cdao.updatePrescPaymentDetails(con, pddto.getChargeId(),
						pddto.getAmount(), pddto.getPaymentId());

			} else if (pddto.getPaymentType().equals("O")){
				cdao.updateOhPaymentDetails(con, pddto.getChargeId(),
						pddto.getAmount(), pddto.getPaymentId());

			} else if(pddto.getPaymentType().equals("S")) {
				if(pddto.getGrnNo()==null) {
					grn = new ArrayList<StockEntry>();
					success= true;
				} else {
					StockEntry se = new StockEntry();
					se.setGrnNo(pddto.getGrnNo());
					se.setPaymentId(pddto.getPaymentId());
					se.setInvoiceType(pddto.getInvoiceType());
					se.setSupplier(pddto.getPayeeName());
					se.setConsignmentStatus(pddto.getConsignmentStatus());
					se.setIssueId(pddto.getIssueId());
					se.setInvoiceDate(pddto.getInvoiceDate());
					grn.add(se);
					success = dstockDao.updateGrnPaymentId(con,grn);
				}
			}
		}

		return success;
	}

	/*
	 *
	 * Updating doc_payment_id with null if the doc amount is removed from payments_details table
	 */
	public static boolean updateDoctorChargeId(Connection con, ArrayList<PaymentDetailsDTO> payments)
		throws SQLException, Exception {
		boolean success = true;
		ChargeDAO cdao = new ChargeDAO(con);
		BillActivityChargeDAO bacdao = new BillActivityChargeDAO(con);
		DirectStockEntryDAO dstockDao = new DirectStockEntryDAO(con);

		Iterator<PaymentDetailsDTO> it = payments.iterator();
		ArrayList<ChargeDTO> charge = new ArrayList<ChargeDTO>();
		ArrayList<StockEntry> grn = new ArrayList<StockEntry>();

		while(it.hasNext()){
			PaymentDetailsDTO pddto = it.next();


			String chargeId =(String) pddto.getChargeId();
			if (pddto.getPaymentType().equals("D")){

				if (pddto.getPackagCharge().equals("Y")) {
					bacdao.removeDoctorPaymentId(con, pddto.getPkgActivityCode(), pddto.getPkgActivityId());
				} else {
					cdao.removeDoctorPaymentId(con, chargeId);
				}

			} else if (pddto.getPaymentType().equals("R") || pddto.getPaymentType().equals("F")) {
				cdao.removeRefDocPaymentId(con, chargeId);

			} else if (pddto.getPaymentType().equals("P")) {
				cdao.removePrescPaymentId(con, chargeId);

			} else if (pddto.getPaymentType().equals("O")) {
				cdao.removeOhPaymentId(con, chargeId);

			} else if (pddto.getPaymentType().equals("S")) {
				StockEntry se = new StockEntry();
				se.setGrnNo(pddto.getGrnNo());
				se.setPaymentId(null);
				se.setInvoiceType(pddto.getInvoiceType());
				se.setSupplier(pddto.getPayeeName());
				se.setConsignmentStatus(pddto.getConsignmentStatus());
				se.setIssueId(pddto.getIssueId());
				se.setInvoiceDate(pddto.getInvoiceDate());
				grn.add(se);
				success = dstockDao.updateGrnPaymentId(con,grn);
			}
		}

		return success;
	}

}

