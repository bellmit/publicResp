package com.insta.hms.core.cashlimit;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.billing.AllocationRepository;
import com.insta.hms.core.billing.ReceiptModel;
import com.insta.hms.core.billing.ReceiptService;
import com.insta.hms.mdm.paymentmode.PaymentModeMasterModel;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

@Service
public class CashLimitService {

	@LazyAutowired
	private ReceiptService receiptService;

	@LazyAutowired
	AllocationRepository allocationRepository;

	@LazyAutowired
	  GenericPreferencesService genericPreferencesService;

	/**
	 * get Cash Payment Amount of the Patient.
	 * 
	 * @param mrNo the mrNo,Visit Id ,Cash amount and Refund
	 * @return map
	 */
	@Transactional
	public Map<String, BigDecimal> cashLimitPayments(String mrNo, String visitId, BigDecimal amt, BigDecimal refundAmt,
			BigDecimal genDepSetOffAmt, BigDecimal ipDepSetOffAmt, BigDecimal pkgDepSetOffAmt) {
		BigDecimal dayCash = BigDecimal.ZERO, visitCash = BigDecimal.ZERO, dayRefund = BigDecimal.ZERO,
				visitRefund = BigDecimal.ZERO, genCashDepSetOff = BigDecimal.ZERO, ipCashDepSetOff = BigDecimal.ZERO,
				pkgCashDepSetOff = BigDecimal.ZERO, un_allocatedAmount = BigDecimal.ZERO,
				avblCashLimit = BigDecimal.ZERO;
		Map<String, Object> filterMap = new HashMap<String, Object>();
		filterMap.put("mr_no", mrNo);
		filterMap.put("visit_id", visitId);
		Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
		BasicDynaBean bean = receiptService.getCashLimit(mrNo, visitId);
		if (bean == null) {
			return map;
		}
		Boolean hasGenCashDepSetOff = false;
		Boolean hasIpCashDepSetOff = false;
		Boolean hasPackCashDepSetOff = false;
		BigDecimal cashAvailable = (BigDecimal) bean.get("day_cash_payment");
		BigDecimal transactionLimit = (BigDecimal) bean.get("transaction_limit");
		if (amt.compareTo(BigDecimal.ZERO) != 0) {
			/* check the Cash Limit for particular Day */
			if (cashAvailable.add(amt).compareTo(transactionLimit) <= 0) {
				/* check the Cash Limit for particular Visit */
				BigDecimal visitCashAvailable = (BigDecimal) bean.get("visit_cash_amount");
				if (visitCashAvailable.add(amt).compareTo(transactionLimit) < 0) {
					visitCash = (transactionLimit).subtract((visitCashAvailable).add(amt));
				} else {
					visitCash = (transactionLimit).subtract(visitCashAvailable).subtract(amt);
				}
			} else {
				dayCash = (transactionLimit).subtract(cashAvailable).subtract(amt);
			}
		}

		if (genDepSetOffAmt.compareTo(BigDecimal.ZERO) != 0 || ipDepSetOffAmt.compareTo(BigDecimal.ZERO) != 0
				|| pkgDepSetOffAmt.compareTo(BigDecimal.ZERO) != 0) {
			BigDecimal genDepSetOff = BigDecimal.ZERO, ipDepositSetOff = BigDecimal.ZERO,
					packageDepositSetOff = BigDecimal.ZERO;
			BigDecimal visitCashAvailable = (BigDecimal) bean.get("visit_cash_amount");
			avblCashLimit = (BigDecimal) bean.get("avbl_cash_limit");
			int centerId = RequestContext.getCenterId();
		    BasicDynaBean gprefs = genericPreferencesService.getAllPreferences();
		    String enablePatientDepositAvailability = (String) gprefs.get("enable_patient_deposit_availability");
			List<ReceiptModel> depositList = null;

			if (genDepSetOffAmt.compareTo(BigDecimal.ZERO) > 0) {
				//If the Deposit Availability needs to show at each center level
			    if ("E".equals(enablePatientDepositAvailability)) {
			      depositList = allocationRepository.getGeneralDepositList(mrNo,centerId, false);
			    } else {
			      depositList = allocationRepository.getGeneralDepositList(mrNo, false);
			    }
			}
			if (ipDepSetOffAmt.compareTo(BigDecimal.ZERO) > 0) {
				if ("E".equals(enablePatientDepositAvailability)) {
				  depositList = allocationRepository.getIpDepositList(mrNo,centerId, false);
				} else {
				  depositList = allocationRepository.getIpDepositList(mrNo, false);
				}
			}
			if (pkgDepSetOffAmt.compareTo(BigDecimal.ZERO) > 0) {
				if ("E".equals(enablePatientDepositAvailability)) {
				  depositList = allocationRepository.getCenterWisePackageDepositList(mrNo,centerId, false);
				} else {
				  depositList = allocationRepository.getPackageDepositList(mrNo, false);
				}
			}
			for (ReceiptModel deposit : depositList) {
				BigDecimal unallocatedAmount = deposit.getUnallocatedAmount();
				BigDecimal minAmount = BigDecimal.ZERO;
				if (unallocatedAmount.compareTo(BigDecimal.ZERO) == 0) {
					continue;
				}
				PaymentModeMasterModel payModeModel = deposit.getPaymentModeId();
				int id = payModeModel.getModeId();
				if (id != -1) {
					un_allocatedAmount = un_allocatedAmount.add(unallocatedAmount);
				}
				/* check the Cash Limit for General Deposit */
				if (id == -1 && genDepSetOffAmt.compareTo(BigDecimal.ZERO) > 0
						&& avblCashLimit.compareTo(BigDecimal.ZERO) > 0) {
					BigDecimal genDepSetOffAmount = genDepSetOffAmt.min(unallocatedAmount);
					if (genDepSetOffAmount.add(visitCashAvailable).compareTo(transactionLimit) < 0) {
						genCashDepSetOff = (transactionLimit).subtract((visitCashAvailable).add(genDepSetOffAmount));
					} else {
						genCashDepSetOff = (transactionLimit).subtract(visitCashAvailable).subtract(genDepSetOffAmount);
					}
					minAmount = genDepSetOffAmt.min(avblCashLimit);
					genDepSetOffAmt = genDepSetOffAmt.subtract(minAmount);
					hasGenCashDepSetOff = true;
				} else if (id != -1) {
					genDepSetOff = genDepSetOffAmt.min(unallocatedAmount);
					genDepSetOffAmt = genDepSetOffAmt.subtract(genDepSetOff);
				}
				/* check the Cash Limit for IP Deposit */
				if (id == -1 && ipDepSetOffAmt.compareTo(BigDecimal.ZERO) > 0
						&& avblCashLimit.compareTo(BigDecimal.ZERO) > 0) {
					BigDecimal ipDepSetOffAmount = ipDepSetOffAmt.min(unallocatedAmount);
					if (ipDepSetOffAmount.add(visitCashAvailable).compareTo(transactionLimit) < 0) {
						ipCashDepSetOff = (transactionLimit).subtract((visitCashAvailable).add(ipDepSetOffAmount));
					} else {
						ipCashDepSetOff = (transactionLimit).subtract(visitCashAvailable).subtract(ipDepSetOffAmount);
					}
					minAmount = ipDepSetOffAmt.min(avblCashLimit);
					ipDepSetOffAmt = ipDepSetOffAmt.subtract(minAmount);
					hasIpCashDepSetOff = true;

				} else if (id != -1) {
					ipDepositSetOff = ipDepSetOffAmt.min(unallocatedAmount);
					ipDepSetOffAmt = ipDepSetOffAmt.subtract(minAmount);
				}
				/* check the Cash Limit for Package Deposit */
				if (id == -1 && pkgDepSetOffAmt.compareTo(BigDecimal.ZERO) > 0
						&& avblCashLimit.compareTo(BigDecimal.ZERO) > 0) {
					BigDecimal packDepSetOffAmount = pkgDepSetOffAmt.min(unallocatedAmount);
					if (packDepSetOffAmount.add(visitCashAvailable).compareTo(transactionLimit) < 0) {
						pkgCashDepSetOff = (transactionLimit).subtract((visitCashAvailable).add(packDepSetOffAmount));
					} else {
						pkgCashDepSetOff = (transactionLimit).subtract(visitCashAvailable)
								.subtract(packDepSetOffAmount);
					}
					minAmount = pkgDepSetOffAmt.min(avblCashLimit);
					pkgDepSetOffAmt = pkgDepSetOffAmt.subtract(minAmount);
					hasPackCashDepSetOff = true;
				} else if (id != -1) {
					packageDepositSetOff = pkgDepSetOffAmt.min(unallocatedAmount);
					pkgDepSetOffAmt = pkgDepSetOffAmt.subtract(minAmount);
				}

				if (genDepSetOffAmt.compareTo(BigDecimal.ZERO) == 0 && ipDepSetOffAmt.compareTo(BigDecimal.ZERO) == 0
						&& pkgDepSetOffAmt.compareTo(BigDecimal.ZERO) == 0) {
					break;
				}
			}
		}
		if (refundAmt.compareTo(BigDecimal.ZERO) != 0) {
			/* check the Refund Cash Limit for particular Day */
			BigDecimal dayRefundAvailable = (BigDecimal) bean.get("day_refund_cash_amt");
			if (dayRefundAvailable.add(refundAmt).compareTo(transactionLimit) <= 0) {
				/* check the Refund Cash Limit for particular Visit */
				BigDecimal visitRefundAviailable = (BigDecimal) bean.get("visit_refund_cash_amt");
				if (visitRefundAviailable.add(refundAmt).compareTo(transactionLimit) < 0) {
					visitRefund = (transactionLimit).subtract((visitRefundAviailable).add(refundAmt));
				} else {
					visitRefund = (transactionLimit).subtract(visitRefundAviailable).subtract(refundAmt);
				}
			} else {
				dayRefund = (transactionLimit).subtract(dayRefundAvailable).subtract(refundAmt);
			}
		}
		map.put("visitCash", visitCash);
		map.put("dayCash", dayCash);
		map.put("visitRefund", visitRefund);
		map.put("dayRefund", dayRefund);
		if (hasGenCashDepSetOff) {
		  map.put("genCashDepSetOff", genCashDepSetOff);
		}
		if (hasIpCashDepSetOff) {
		  map.put("ipCashDepSetOff", ipCashDepSetOff);
		}
		if (hasPackCashDepSetOff) {
		map.put("pkgCashDepSetOff", pkgCashDepSetOff);
		}
		map.put("genDepSetOffAmt", genDepSetOffAmt);
		map.put("ipDepSetOffAmt", ipDepSetOffAmt);
		map.put("pkgDepSetOffAmt", pkgDepSetOffAmt);
		map.put("un_allocatedAmount", un_allocatedAmount);
		map.put("avblCashLimit", avblCashLimit);
		map.put("transactionLimit", transactionLimit);
		return map;
	}

	/**
	 * get Deposit Cash Payment Amount Limit of the Patient.
	 *
	 * @param mrNo the mrNo, Cash amount and Refund
	 * @return map
	 */
	public Map<String, BigDecimal> depositCashLimitPayments(String mrNo, BigDecimal amt, BigDecimal refundAmt) {
		BigDecimal dayDepositCash = BigDecimal.ZERO, dayDepositRefund = BigDecimal.ZERO,
				availableDepositCash = BigDecimal.ZERO, depositCashAvailable = BigDecimal.ZERO,
				transactionLimit = BigDecimal.ZERO, dayDepositRefundAvailable = BigDecimal.ZERO,
				availableDepsoit = BigDecimal.ZERO;
		Map<String, Object> filterMap = new HashMap<String, Object>();
		filterMap.put("mr_no", mrNo);
		BasicDynaBean bean = receiptService.getDepositCashLimit(mrNo);
		Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
		if (bean == null) {
			return map;
		}
		depositCashAvailable = (BigDecimal) bean.get("day_cash_collection");
		transactionLimit = (BigDecimal) bean.get("transaction_limit");
		availableDepsoit = (BigDecimal) bean.get("unallocated_amount");
		if (amt.compareTo(BigDecimal.ZERO) != 0) {
			/* check the Deposit Cash Limit for particular Day */
			if (availableDepsoit.add(amt).compareTo(transactionLimit) <= 0) {
				/* check the Available Cash Limit */
				if (depositCashAvailable.add(amt).compareTo(transactionLimit) < 0) {
					dayDepositCash = (transactionLimit).subtract((depositCashAvailable).add(amt));
				} else {
					dayDepositCash = (transactionLimit).subtract(depositCashAvailable).subtract(amt);
				}
			} else {
				availableDepositCash = (transactionLimit).subtract(availableDepsoit).subtract(amt);
			}
		}
		if (refundAmt.compareTo(BigDecimal.ZERO) != 0) {
			/* check the Refund Cash Limit for particular Day */
			dayDepositRefundAvailable = (BigDecimal) bean.get("day_refund_cash");
			if (dayDepositRefundAvailable.add(refundAmt).compareTo(transactionLimit) < 0) {
				dayDepositRefund = (transactionLimit).subtract(dayDepositRefundAvailable).add(refundAmt);
			} else {
				dayDepositRefund = (transactionLimit).subtract(dayDepositRefundAvailable).subtract(refundAmt);
			}
		}

		map.put("availableDepositCash", availableDepositCash);
		map.put("dayDepositCash", dayDepositCash);
		map.put("dayDepositRefund", dayDepositRefund);
		map.put("transactionLimit", transactionLimit);
		return map;
	}
}
