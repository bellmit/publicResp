package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillChargeClaimDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.BillingHelper;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.billing.ClaimDAO;
import com.insta.hms.billing.DepositsDAO;
import com.insta.hms.billing.Receipt;
import com.insta.hms.billing.paymentdetails.AbstractPaymentDetails;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.core.inventory.sales.SalesService;
import com.insta.hms.dischargemedication.DischargeMedicationDAO;
import com.insta.hms.integration.scm.inventory.ScmOutBoundInvService;
import com.insta.hms.master.Accounting.ChargeHeadsDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDTO;
import com.insta.hms.master.StoreMaster.StoreMasterDAO;
import com.insta.hms.modules.ModulesDAO;
import com.insta.hms.outpatient.PrescribeDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MedicineSalesBO {

	static Logger logger = LoggerFactory.getLogger(MedicineSalesBO.class);

	private static RetailCustomerDAO rcDao = new RetailCustomerDAO();
	private static ClaimDAO claimDAO = new ClaimDAO();
	private static ModulesDAO modulesDao = new ModulesDAO();
	private static ScmOutBoundInvService invScmService = ApplicationContextProvider
	    .getBean(ScmOutBoundInvService.class);
	
    private static final GenericDAO storeRetialSponsorsDAO =
        new GenericDAO("store_retail_sponsors");
    private static final GenericDAO storeRetailDoctorsDAO = new GenericDAO("store_retail_doctor");

    private static SalesService salesService =
        ApplicationContextProvider.getBean(SalesService.class);
    
	/*
	 * Make a medicine sale for a retail customer
	 */
	public static Map retailMedicineSale(BasicDynaBean cust, String billType,
			String creditBillNo, boolean isReturns, MedicineSalesMainDTO sale, List<Receipt> receiptList,
			boolean existingCustomer, String doctorName, boolean isEstimate, String retailCustomerId,
			Object storeRatePlanId,String overallDiscountAuth, int rewardPointsRedeemed, String depositType)
		throws SQLException, java.io.IOException, ParseException {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean success = true;
		boolean allSuccess = false;
		Map resultMap = null;
		List<Map<String, Object>> cacheSaleTxns = new ArrayList<>();

		try {
			do {
				if (!existingCustomer) {
				  if(null == retailCustomerId) { //only for retail customer
				    resultMap = validateGovernmentIdentifier(cust);
				    if (resultMap.get("error") != null) break;
				  }
				  resultMap = formatAndValidateMobileNumber(cust);
				  if (resultMap.get("error") != null) break;
				  

					cust.set("customer_id", rcDao.getNextId());
					if (billType.equalsIgnoreCase("BL"))
						cust.set("is_credit", "Y");
					// checking wheather doctor is existing r new if new insert into doc master...
					String docId = MedicineSalesDAO.doctorNameToId(doctorName);
					if ( docId == null){
						BasicDynaBean bean = storeRetailDoctorsDAO.getBean();
						int doc_Id = storeRetailDoctorsDAO.getNextSequence();
						bean.set("doctor_id",doc_Id);
						bean.set("doctor_name", doctorName);
						success = storeRetailDoctorsDAO.insert(con, bean);
						if (!success) break;
					}
					String sponsor = sale.getSponserName();
					if ((sponsor != null) && !sponsor.equals("")) {
						boolean exist = storeRetialSponsorsDAO.exist("sponsor_name", sponsor);
						if (!exist) {
							BasicDynaBean b = storeRetialSponsorsDAO.getBean();
							int sponsor_Id = storeRetialSponsorsDAO.getNextSequence();
							b.set("sponsor_id",sponsor_Id);
							b.set("sponsor_name", sponsor);
							success = storeRetialSponsorsDAO.insert(con, b);
							cust.set("sponsor_name", sponsor_Id);
							if (!success) break;
						} else {
							BasicDynaBean sponBean = storeRetialSponsorsDAO.findByKey("sponsor_name", sponsor);
							int sponsor_Id = Integer.parseInt((sponBean.get("sponsor_id").toString()));
							cust.set("sponsor_name", sponsor_Id);
						}
					}

					success = rcDao.insert(con, cust);
					retailCustomerId = (String) cust.get("customer_id");
					if (!success) break;
				}else{
				  final String[] phoneColumn = new String[]{"phone_no"};
				  Map<String, String> keys = new HashMap<>();
				  keys.put("customer_id", retailCustomerId);
				  rcDao.update(con, phoneColumn, cust.getMap(), keys );
				}

				if ( isEstimate ){
					//new GenericDAO("pharmacy_retail_customer")
					resultMap = saveEstimate(con, retailCustomerId, sale);

					if (resultMap.get("error") != null) break;
					allSuccess = success;
				}else {
					resultMap = commonMedicineSale(con, "r", (String) cust.get("customer_id"),	sale, receiptList,
							billType, creditBillNo, isReturns, false, null, "", false,
							storeRatePlanId, overallDiscountAuth, rewardPointsRedeemed, depositType, cacheSaleTxns);
					
					if (resultMap.get("error") != null) break;
					allSuccess = success;
				}
			} while (false);

		} catch(Exception e){
		  allSuccess = false;
		  throw e;
		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
			//Put the sale transaction into redis
      BasicDynaBean module = modulesDao.findByKey("module_id", "mod_scm");
			if (!cacheSaleTxns.isEmpty() && module != null && allSuccess &&
          ((String)module.get("activation_status")).equals("Y")) {
			  invScmService.scheduleSaleTxns(cacheSaleTxns, isReturns);
			}
			//update stock timestamp
			StockFIFODAO stockFIFODAO = new StockFIFODAO();
			stockFIFODAO.updateStockTimeStamp();
			stockFIFODAO.updateStoresStockTimeStamp(Integer.parseInt(sale.getStoreId()));
		}
		return resultMap;
	}

	/*
	 * Make a medicine sale for a hospital patient
	 */
	public static Map patientMedicineSale(String visitType, String visitId, String billType,
			String creditBillNo, boolean isReturns, boolean isReturnAgstVisit, MedicineSalesMainDTO sale,
			List<Receipt> receiptList, String[] conId, String[] medDispOpt,
			boolean isEstimate,String depositsetOff,
			String dispenseStatus, String planId, boolean istpa,
			Map medAndQuantityMap, boolean prescriptionsByGenerics,
			Object storeRatePlanId,String overallDiscountAuth, int rewardPointsRedeemed,
			String[] saleQty,Map<String,String> indentDisStatusMap, int pbm_presc_id, String depositType, 
			String[] medicationId, String[] dischargeId)
		throws SQLException, ParseException, IOException {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean success = false;
		List<Map<String, Object>> cacheSaleTxns = new ArrayList<>();
		Map resultMap = null;
		List<Integer> docPrescIdList = new ArrayList<>();
		List<Integer> dischargeMedPrescIdList = new ArrayList<>();

		try {
			if (isEstimate) {
				resultMap = saveEstimate(con, visitId, sale);
			}else
				resultMap = commonMedicineSale(con, visitType, visitId, sale, receiptList, billType, creditBillNo,
						isReturns, isReturnAgstVisit, depositsetOff, planId, istpa,storeRatePlanId,
						overallDiscountAuth, rewardPointsRedeemed, depositType, cacheSaleTxns);

			if (resultMap.get("error") == null) {
				success=true;
			}

            if (success && ((conId != null && !StringUtils.isEmpty(conId[0]))
                || (visitType.equals("i") && !StringUtils.isEmpty(visitId))) && !isEstimate) {
              success = PrescribeDAO.updateStatus(con, medAndQuantityMap, conId, medDispOpt,
                  sale.getSaleId(), prescriptionsByGenerics, pbm_presc_id, visitType, visitId, docPrescIdList);
              if (!success)
                resultMap.put("error", "Failed to update the prescriptions status..");
            }
			//Added For discharge medication status update. 
			if (success && dischargeId!= null && dischargeId[0]!=null && !dischargeId[0].isEmpty() && !isEstimate) {
				success =  DischargeMedicationDAO.updateMedicinesStatus(con, medAndQuantityMap, dischargeId, medDispOpt,
						sale.getSaleId(), prescriptionsByGenerics, dischargeMedPrescIdList);
				if (!success) resultMap.put("error", "Failed to update the discharge medication prescriptions status..");
			}

			if( indentDisStatusMap.size() > 0) {//update indents
				StoresPatientIndentDAO storesPatDAO = new StoresPatientIndentDAO();
				for(MedicineSalesDTO saleItem : sale.getSaleItems()){
					if (storesPatDAO.isMedicinePartOfIndent(con,Integer.parseInt(saleItem.getMedicineId()),indentDisStatusMap.keySet())) {
						success &= storesPatDAO.updateIndentDetailsDispenseStatus(con,visitId,
								indentDisStatusMap, saleItem.getQuantity().abs(),saleItem.getMedicineId(),saleItem.getSaleItemId(),"sale_item_id",saleItem,sale.getType());						
					}
				}

				//if user selects Close All as dispense status,we ll update dispense status of the indent to 'C' even if its not dispensed
				for(String key : new HashSet<String>(indentDisStatusMap.keySet())){
					if (indentDisStatusMap.get(key).equals("all")) {
						success &= storesPatDAO.closeAllIndents(con,key);

					}
				}

				success &= StoresPatientIndentDAO.updateIndentDispenseStatus(con, visitId);
				// S represents 'sale' process_type column of store_patient_indent_main table
				success &= StoresPatientIndentDAO.updateProcessType(con, visitId, "S");
			}

		} catch(Exception e){
		  success = false;
		  throw e;
		} finally {
			DataBaseUtil.commitClose(con, success);
			//Put the sale transactions into redis cache.
			BasicDynaBean module = modulesDao.findByKey("module_id", "mod_scm");
			if (!cacheSaleTxns.isEmpty() && module != null && success &&
			    ((String)module.get("activation_status")).equals("Y")) {
        invScmService.scheduleSaleTxns(cacheSaleTxns, isReturns);
      }
			//update stock timestamp
			StockFIFODAO stockFIFODAO = new StockFIFODAO();
			stockFIFODAO.updateStockTimeStamp();
			stockFIFODAO.updateStoresStockTimeStamp(Integer.parseInt(sale.getStoreId()));
			
			// triggers hl7 message for dispensed medicines.
            if (success) {
              salesService.triggerEvent(docPrescIdList,dischargeMedPrescIdList,sale.getSaleId(),visitId);
            }
		}
		return resultMap;
	}

	/*
	 * Common to hospital and retail customers: make a medcine sale. The list of medicines,
	 * charges and the receipt are all passed in. The type is one of "r", "i", "o", "d".
	 * billType determines the type of bill, and creditBillNo indicates to use an existing
	 * credit bill no. Note that we may be asked to create a new credit bill for retail
	 * credit customers.
	 */
	public static Map commonMedicineSale(Connection con, String visitType, String visitId,
			MedicineSalesMainDTO sale, List<Receipt> receiptList, String billType,
			String creditBillNo, boolean isReturns, boolean isReturnAgstVisit, String depositsetOff,
			String planIdStr, boolean istpa,Object storeRatePlanId, String overallDiscountAuth,
			int rewardPointsRedeemed, String depositType, List<Map<String,Object>> cacheSaleTxns)
		throws SQLException, ParseException, IOException {

		AbstractPaymentDetails ppdImpl =
			AbstractPaymentDetails.getReceiptImpl(AbstractPaymentDetails.PHARMACY_PAYMENT);
		String salepref = GenericPreferencesDAO.getGenericPreferences().getStockNegativeSale();
		PatientInsurancePlanDAO patInsrPlanDao = new PatientInsurancePlanDAO();
		GenericDAO salesClaimDAO = new GenericDAO("sales_claim_details");
		BillChargeClaimDAO billChargeClaimDAO = new BillChargeClaimDAO();
		// doctor id is needed
		String docId = MedicineSalesDAO.doctorNameToId(sale.getDoctor());
		BillingHelper billingHelper = new BillingHelper();

		Map resultMap = new HashMap();
		boolean success = true;

		MedicineSalesDAO mdao = new MedicineSalesDAO(con);
		MedicineStockDAO sdao = new MedicineStockDAO(con);
		ChargeDAO cdao = new ChargeDAO(con);

		boolean isCounterSales = (sale.getCounter() != null) && !sale.getCounter().equals("");
		int storeId = Integer.parseInt(sale.getStoreId());
		int planid = (planIdStr != null && !planIdStr.equals("")) ? Integer.parseInt(planIdStr) : 0;

        //setting revenue a/c group a/c to store wise preferences
		BasicDynaBean store = new StoreMasterDAO().findByKey("dept_id", storeId);
		int  accountGroup = (Integer) store.get("account_group");
		BasicDynaBean patient = new GenericDAO("patient_registration").findByKey("patient_id", visitId);

		/*
		 * Iterate through saleItems, reduce stock, set off against original if required.
		 * Also calculate total consolidated amounts for bill_charge
		 */
		BigDecimal itemTotalAmt = new BigDecimal(0);
		BigDecimal itemTotalDisc =new BigDecimal(0);
		BigDecimal itemTotalTax = new BigDecimal(0);
		BigDecimal itemTotalOrigAmtDiff = new BigDecimal(0);
		BigDecimal totClaimAmt = new BigDecimal(0);

		ArrayList<MedicineSalesDTO> saleItemsForReturns = new ArrayList<MedicineSalesDTO>();
		sale.setSaleItemsForReturns(saleItemsForReturns);

		ArrayList<ChargeDTO> saleIdChargesToUpdate = new ArrayList<ChargeDTO>();
		sale.setSaleIdChargesToUpdate(saleIdChargesToUpdate);

		List<String> chargesToUpdate = new ArrayList<String>();

		List<BasicDynaBean> salesForReturn = null;
		BasicDynaBean storeDetails = StoreDAO.findByStore(storeId);
		int centerId = (Integer)storeDetails.get("center_id");
		String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
		if (sale.getRBillNo() != null)
			salesForReturn = MedicineSalesDAO.getSoldItems(sale.getRBillNo(), planid, healthAuthority);
		else if (isReturnAgstVisit && (visitId != null) && (!visitId.isEmpty())) {
			salesForReturn = MedicineSalesDAO.getVisitSoldItems(visitId, storeId, planid);
		}

		StockFIFODAO stockFIFODAO = new StockFIFODAO();
		List<BasicDynaBean> planList = patInsrPlanDao.getPlanDetails(con, visitId);
		int[] planIds = new int[planList.size()];
		ArrayList<BigDecimal> usedClaimAmts =  null;
		GenericDAO salesTaxDao = new GenericDAO("store_sales_tax_details");
		
		for (MedicineSalesDTO saleItem : sale.getSaleItems()) {

			saleItem.setSaleItemId(mdao.getNextSaleItemId());
			usedClaimAmts = saleItem.getClaimAmts();

			itemTotalAmt = itemTotalAmt.add(saleItem.getAmount());
			itemTotalDisc = itemTotalDisc.add(saleItem.getMedDiscRS());
			itemTotalTax = itemTotalTax.add(saleItem.getTax());
			itemTotalOrigAmtDiff = itemTotalOrigAmtDiff.add(
					saleItem.getQuantity()
					.multiply(saleItem.getOrigRate().subtract(saleItem.getRate()))
					.divide(saleItem.getPackageUnit(),BigDecimal.ROUND_HALF_UP));

			Map statusMap = null;

			if (isReturns) {
				List<BasicDynaBean> listOfSoldStock = new ArrayList<BasicDynaBean>();

				// get a list of sales done for the same item-batch
				for (BasicDynaBean soldItem : salesForReturn ){
					if ((Integer)soldItem.get("item_batch_id") == saleItem.getItemBatchId()
							&& ((BigDecimal)soldItem.get("qty")).compareTo(BigDecimal.ZERO) > 0)
						listOfSoldStock.add(soldItem);
				}

				BigDecimal remainingQty = saleItem.getQuantity();
				BigDecimal remainingAmt = saleItem.getAmount();
				BigDecimal remainingTaxAmt = saleItem.getTax();
				BigDecimal remainingOriginalTaxAmt = saleItem.getOrgTaxAmt();
				BigDecimal remainingClaimAmt = saleItem.getInsuranceClaimAmt();
				ArrayList<BigDecimal> saleClaimAmts = saleItem.getClaimAmts();
				ArrayList<BigDecimal> saleClaimTaxAmts = saleItem.getClaimTaxAmt();
				BigDecimal remainingpriClaimAmt = BigDecimal.ZERO;
				BigDecimal remainingpriClaimTaxAmt = BigDecimal.ZERO;
				BigDecimal remainingsecClaimAmt = BigDecimal.ZERO;
				BigDecimal remainingsecClaimTaxAmt = BigDecimal.ZERO;
				if( saleClaimAmts.size() > 0 && planIds.length > 0 ) {
					remainingpriClaimAmt = saleClaimAmts.get(0).negate();
					remainingpriClaimTaxAmt = saleClaimTaxAmts.get(0).negate();
					remainingsecClaimAmt = ( planIds.length > 1 ? saleClaimAmts.get(1).negate() : BigDecimal.ZERO );
					remainingsecClaimTaxAmt = ( planIds.length > 1 ? saleClaimTaxAmts.get(1).negate() : BigDecimal.ZERO );
				}
				List<Integer> soldItemIds = new ArrayList<Integer>();
				for (BasicDynaBean soldStock : listOfSoldStock) {
					
					if (remainingQty.compareTo(BigDecimal.ZERO) <= 0)
						break;

					BigDecimal soldQty = (BigDecimal) soldStock.get("qty");
					BigDecimal setOffQty, setOffAmt, setOffTaxAmt, setOffClaim, setOffOriginalTax, setOffPriClaim = BigDecimal.ZERO, setOffSecClaim = BigDecimal.ZERO,
              setOffPriClaimTax = BigDecimal.ZERO, setOffSecClaimTax = BigDecimal.ZERO;
					if (soldQty.compareTo(remainingQty) > 0) {
						// there is enough sale to set it off against, set off the entire return qty.
						setOffQty = remainingQty;
						setOffAmt = remainingAmt;
						//setOffAmt = remainingAmt.subtract(remainingTaxAmt);
						setOffClaim = remainingClaimAmt;
						setOffTaxAmt = remainingTaxAmt;
						setOffOriginalTax = remainingOriginalTaxAmt;
					} else {
						// we can set off a max of the original sale qty only, and continue iteration.
						setOffQty = soldQty;
						setOffAmt = (BigDecimal)soldStock.get("amount");
						//setOffAmt = ((BigDecimal)soldStock.get("amount")).subtract((BigDecimal)soldStock.get("tax"));
						setOffClaim =  (BigDecimal)soldStock.get("insurance_claim_amt");
						setOffTaxAmt = (BigDecimal)soldStock.get("tax");
						setOffOriginalTax = (BigDecimal)soldStock.get("original_tax_amt");
					}

					remainingQty = remainingQty.subtract(setOffQty);
					remainingAmt = remainingAmt.subtract(setOffAmt);
					remainingTaxAmt = remainingTaxAmt.subtract(setOffTaxAmt);
					remainingClaimAmt = remainingClaimAmt.subtract(setOffClaim);
					remainingOriginalTaxAmt = remainingOriginalTaxAmt.subtract(setOffOriginalTax);

					// TODO: make this a set
					if (!chargesToUpdate.contains((String)soldStock.get("charge_id")))
						chargesToUpdate.add((String)soldStock.get("charge_id"));

					int origSaleId = (Integer)soldStock.get("sale_item_id");
					soldItemIds.add(origSaleId);

					// add back the stock against the original transaction
					statusMap = stockFIFODAO.addStock(con, storeId, origSaleId,
							"S", setOffQty, sale.getUsername(), sale.getChange_source(),null,
							"SR",saleItem.getSaleItemId());

					if(!(Boolean)statusMap.get("transaction_lot_exists")){//this is true if sales happened before fifo
						success &= stockFIFODAO.addToEarlierStock(con, (Integer)soldStock.get("item_batch_id"), storeId,setOffQty);
					}

					//This method is used to update the return amount and tax against sale for return.
					// set off the quantity etc. against original sold stock
					mdao.setOffAgainstSaleItem(origSaleId, setOffQty, setOffAmt, setOffClaim, setOffTaxAmt, setOffOriginalTax);
					
					BillDAO billdao = new BillDAO(con);
					Bill bill = billdao.getBill(billType);
					

					if ((istpa  && ((bill != null && bill.getIs_tpa()) || billType.equalsIgnoreCase("BN-I")) ) 
					    && !billType.equalsIgnoreCase("BN") && !billType.equalsIgnoreCase("BL") ){


						for(int l = 0;l<planList.size();l++){
							planIds[l] = (Integer)((BasicDynaBean)planList.get(l)).get("plan_id");
						}

						List<BasicDynaBean> origSalesClaims = salesClaimDAO.listAll(null,"sale_item_id", origSaleId,"sales_item_plan_claim_id");
						List<BigDecimal> origClaims = saleItem.getClaimAmts();
						List<BigDecimal> origClaimTaxs = saleItem.getClaimTaxAmt();
						BigDecimal[] consolidatedReturnClaimAmt = new BigDecimal[origClaims.size()];
						BigDecimal[] consolidatedReturnClaimTaxAmt = new BigDecimal[origClaimTaxs.size()];
						for(int c = 0;c<origSalesClaims.size();c++){

							BasicDynaBean origClaim = origSalesClaims.get(c);
							if (((BigDecimal)origClaim.get("insurance_claim_amt")).compareTo(remainingpriClaimAmt) > 0
									  && ((BigDecimal)origClaim.get("insurance_claim_amt")).compareTo(remainingsecClaimAmt) > 0) {
								// there is enough sale to set it off against, set off the entire claim amt.

								setOffPriClaim = remainingpriClaimAmt;
								setOffPriClaimTax = remainingpriClaimTaxAmt;
								setOffSecClaim = remainingsecClaimAmt;
								setOffSecClaimTax = remainingsecClaimTaxAmt;
							} else {
								// we can set off a max of the claim only, and continue iteration.
								if ( c== 0 ) {
									setOffPriClaim = (BigDecimal)origClaim.get("insurance_claim_amt");
									setOffPriClaimTax = (BigDecimal)origClaim.get("tax_amt");
								}
								else {
									setOffSecClaim = (BigDecimal)origClaim.get("insurance_claim_amt");
									setOffSecClaimTax = (BigDecimal)origClaim.get("tax_amt");
								}
							}

							if ( c== 0 ) {
								remainingpriClaimAmt = remainingpriClaimAmt.subtract((BigDecimal)origClaim.get("insurance_claim_amt"));
								remainingpriClaimTaxAmt = remainingpriClaimTaxAmt.subtract((BigDecimal)origClaim.get("tax_amt"));
							}
							else {
								remainingsecClaimAmt = remainingsecClaimAmt.subtract((BigDecimal)origClaim.get("insurance_claim_amt"));
								remainingsecClaimTaxAmt = remainingsecClaimTaxAmt.subtract((BigDecimal)origClaim.get("tax_amt"));
							}

							if ( c== 0 ) {
								origClaim.set("insurance_claim_amt", ((BigDecimal)origClaim.get("insurance_claim_amt")).subtract(setOffPriClaim));
								origClaim.set("ref_insurance_claim_amount", ((BigDecimal)origClaim.get("ref_insurance_claim_amount")).subtract(setOffPriClaim));
								origClaim.set("tax_amt", ((BigDecimal)origClaim.get("tax_amt")).subtract(setOffPriClaimTax));
								
								success &= salesClaimDAO.update(con, origClaim.getMap(), "sales_item_plan_claim_id",(Integer)origClaim.get("sales_item_plan_claim_id")) > 0;
								consolidatedReturnClaimAmt[c] = consolidatedReturnClaimAmt[c] != null
									? consolidatedReturnClaimAmt[c].add(setOffPriClaim) : setOffPriClaim ;
									
									consolidatedReturnClaimTaxAmt[c] = consolidatedReturnClaimTaxAmt[c] != null ? consolidatedReturnClaimTaxAmt[c].add(setOffPriClaimTax): setOffPriClaimTax;

							} else {
								origClaim.set("insurance_claim_amt", ((BigDecimal)origClaim.get("insurance_claim_amt")).subtract(setOffSecClaim));
								origClaim.set("ref_insurance_claim_amount", ((BigDecimal)origClaim.get("ref_insurance_claim_amount")).subtract(setOffSecClaim));
								origClaim.set("tax_amt", ((BigDecimal)origClaim.get("tax_amt")).subtract(setOffSecClaimTax));
                
								success &= salesClaimDAO.update(con, origClaim.getMap(), "sales_item_plan_claim_id",(Integer)origClaim.get("sales_item_plan_claim_id")) > 0;
								consolidatedReturnClaimAmt[c] = consolidatedReturnClaimAmt[c] != null
									? consolidatedReturnClaimAmt[c].add(setOffSecClaim) : setOffSecClaim ;

									consolidatedReturnClaimTaxAmt[c] = consolidatedReturnClaimTaxAmt[c] != null ? consolidatedReturnClaimTaxAmt[c].add(setOffSecClaimTax): setOffSecClaimTax;
							}

						}

						ChargeDTO origCharge = cdao.getCharge((String)soldStock.get("charge_id"));
						if(origSalesClaims.size() == 0)
							consolidatedReturnClaimAmt[0] = setOffClaim;
						origCharge.setClaimAmounts(consolidatedReturnClaimAmt);
						origCharge.setSponsorTaxAmounts(consolidatedReturnClaimTaxAmt);

						ArrayList<ChargeDTO> chargeList = new ArrayList<ChargeDTO>();
						chargeList.add(origCharge);
						//update bill charge claim
						billChargeClaimDAO.reduceBillChargeClaims(con, chargeList, visitId, origCharge.getBillNo(), planIds,false);
					}
				}
				//set map of sold items sale id against returns sale id;
				saleItem.setSoldItemsIds(soldItemIds);
				
				if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
					// possible in multi-user situation.
					resultMap.put("error", "Insufficient sold quantity to set off against");
					return resultMap;
				}

				// make quantity,amount,tax amount negtive for returns
				saleItem.setAmount(saleItem.getAmount().negate());
				saleItem.setQuantity(saleItem.getQuantity().negate());
				saleItem.setTax(saleItem.getTax().negate());
				saleItem.setOrgTaxAmt(saleItem.getOrgTaxAmt().negate());
				saleItem.setMedDiscRS(saleItem.getMedDiscRS().negate());
				// set the insurance claim here as 0
				saleItem.setInsuranceClaimAmt(BigDecimal.ZERO);
				saleItem.setCostValue(((BigDecimal)statusMap.get("costValue")).negate());

			} else {
				statusMap = stockFIFODAO.reduceStock(con,storeId, saleItem.getItemBatchId(), "S",
						saleItem.getQuantity(), null, sale.getUsername(),
						sale.getChange_source(), saleItem.getSaleItemId(), !salepref.equals("D"));

				if ((Boolean)statusMap.get("status") == false) {
					resultMap.put("error", statusMap.get("statusReason"));
					return resultMap;
				}
				saleItem.setCostValue((BigDecimal)statusMap.get("costValue"));
			}

			totClaimAmt = totClaimAmt.add(saleItem.getInsuranceClaimAmt());
			saleItem.setClaimRecdAmt(BigDecimal.ZERO);
			saleItem.setClaimStatus("O");
		}

		/*
		 * Set totals in Sale
		 */
		if (isReturns) {	 // make quantity,amount,tax amount negative for returns
			sale.setTotalItemAmount(itemTotalAmt.negate());
			sale.setTotalItemTax(itemTotalTax.negate());
			sale.setTotalItemDiscount(itemTotalDisc.negate());
		} else {
			sale.setTotalItemAmount(itemTotalAmt);
			sale.setTotalItemTax(itemTotalTax);
			sale.setTotalItemDiscount(itemTotalDisc);

		}

		/*
		 * Generate a billNo if required, or use creditBillNo
		 */
		Bill bill = new Bill();
		bill.setOpenDate(sale.getSaleDate());
		bill.setOpenedBy(sale.getUsername());
		bill.setUserName(sale.getUsername());
		bill.setAccount_group(accountGroup);
		bill.setBillRatePlanId(patient != null ? (String)patient.get("org_id") : "ORG0001");

		bill.setIs_tpa(istpa);

		if (billType.equalsIgnoreCase("BL")) {
			bill.setBillType(Bill.BILL_TYPE_CREDIT);
			bill.setStatus(Bill.BILL_STATUS_OPEN);
			bill.setOkToDischarge(Bill.BILL_DISCHARGE_NOTOK);
			bill.setDepositSetOff(BigDecimal.ZERO);
			bill.setIpDepositSetOff(BigDecimal.ZERO);

		} else {
			bill.setBillType(Bill.BILL_TYPE_PREPAID);

			if (billType.equalsIgnoreCase("BN")) {
				bill.setIs_tpa(false);
			} else { // bill now insured
				bill.setIs_tpa(true);
			}

			if (isCounterSales) {
				// For reward points earning calculation,
				// initially bill is created with Open status.
				// After the charges are inserted, the bill is closed.
				bill.setStatus(Bill.BILL_STATUS_OPEN);
				bill.setOkToDischarge(Bill.BILL_DISCHARGE_NOTOK);

			} else {
				// this is a "pending" sale -- user is allowed to sell, and the cash is collected
				// at a different counter using "retail pending sales" screen.
				bill.setStatus(Bill.BILL_STATUS_OPEN);
				bill.setOkToDischarge(Bill.BILL_DISCHARGE_NOTOK);
			}
		}
		BigDecimal depSetOff =  BigDecimal.ZERO;

		if (depositsetOff != null && !depositsetOff.equalsIgnoreCase(""))
			depSetOff = new BigDecimal(depositsetOff);

		// set of deposit against orginal bill no
		if (isReturns && depSetOff.compareTo(BigDecimal.ZERO) != 0) {
			bill.setDepositSetOff(depSetOff.negate());
		} else {
			bill.setDepositSetOff(depSetOff);
		}

		bill.setVisitId(visitId);
		bill.setVisitType(visitType);
		bill.setRestrictionType("P"); // when creating a bill from here, it can only be type P

		String billNo = null;
		boolean newBill = false;
		boolean isBillNow = billType.equals("BN") || billType.equals("BN-I");

		if (isBillNow) {
			Map msgMap = new BillBO().createNewBill(con, bill, false);
    		if (msgMap.get("error") != null && !msgMap.get("error").equals("")) {
    			resultMap.put("error", msgMap.get("error"));
    			return resultMap;
    		} else {
    			billNo = bill.getBillNo();
			}

			logger.debug("Bill Now: (" + visitType + ") generated bill number: " + billNo);
			newBill = true;

		} else if (creditBillNo.equalsIgnoreCase("") || creditBillNo.equalsIgnoreCase("BL")) {
			// we may raise a new credit bill for retail credit customers.
			// or a new pharmacy credit bill for the patient.
			bill.setIs_tpa(false);         // Added due to Bug# :22432
			Map msgMap = new BillBO().createNewBill(con, bill, false);
    		if (msgMap.get("error") != null && !msgMap.get("error").equals("")) {
    			resultMap.put("error", msgMap.get("error"));
    			return resultMap;
    		} else {
    			billNo = bill.getBillNo();
			}
			logger.debug("Bill Later: (" + visitType + ") generated bill number: " + billNo);
			newBill = true;

		} else {
			// add to bill: newBill remains false
			billNo = creditBillNo;
			if (success) success = PharmacyItemsCreditBillDAO.updateDepositSetOffInBill(con,billNo,bill.getDepositSetOff().toString());
			bill = new BillDAO(con).getBill(billNo);
			logger.debug("Add to bill: " + billNo);
		}

		BigDecimal redemptionRate = GenericPreferencesDAO.getGenericPreferences().getPoints_redemption_rate();
		redemptionRate = redemptionRate == null ? BigDecimal.ZERO : redemptionRate;

		if (newBill && !bill.getIs_tpa() && !isReturns) {
			bill.setRewardPointsRedeemed(rewardPointsRedeemed);
			bill.setRewardPointsRedeemedAmount(redemptionRate.multiply(new BigDecimal(bill.getRewardPointsRedeemed())));
		}else {
			bill.setRewardPointsRedeemed(0);
			bill.setRewardPointsRedeemedAmount(BigDecimal.ZERO);
		}

		if(null != patient) {
			String mrNo = (String)patient.get("mr_no");
			BasicDynaBean ipDepositBean = DepositsDAO.getIPDepositAmounts(mrNo);
			boolean ipDepositExists = ipDepositBean != null && visitType.equals("i");

			BigDecimal totDepositAvl = DepositsDAO.getTotalAvailableDeposit(con, mrNo, billNo);
			BigDecimal totIPDepsoitAvl = DepositsDAO.getTotalIPAvailableDeposit(con, mrNo, billNo);
			BigDecimal totGenDepositAvl = totDepositAvl.subtract(totIPDepsoitAvl);

			BigDecimal depositSetoff = depSetOff;

			if(ipDepositExists && null != depositType){
				if(depositType.equals("i")){
					if(depositSetoff.compareTo(totIPDepsoitAvl)>0){
						bill.setIpDepositSetOff(totIPDepsoitAvl);
					}else{
						bill.setIpDepositSetOff(depositSetoff);
					}
				}else if(depositType.equals("g")){
					if(depositSetoff.compareTo(totGenDepositAvl)>0){
						bill.setIpDepositSetOff(depositSetoff.subtract(totGenDepositAvl));
					}
				}
			}
		}

		sale.setSaleId(mdao.getNextSaleId(isBillNow ? "P" : "C", visitType,
					isReturns ? "R" : "S",sale.getStoreId()));

		for (MedicineSalesDTO saleItem : sale.getSaleItems()) {
			saleItem.setSaleId(sale.getSaleId());
		}

		if (isReturns) {
			sale.setType(sale.TYPE_SALES_RETURN);
		} else {
			sale.setType(sale.TYPE_SALE);
		}
		sale.setBillNo(billNo);
		sale.setDateTime(new java.sql.Timestamp((new java.util.Date()).getTime()));


		/*
		 * Create one bill_charge item post into the bill: this is the financial summary of the sale.
		 */
		ChargeDTO charge = null;
		String chargeID = cdao.getNextChargeId();

		// final amount = itemTotalAmt + bill level disc - bill level roundoff
		BigDecimal chargeAmount = ConversionUtils.setScaleDown(itemTotalAmt.subtract(sale.getDiscount()).add(sale.getRoundOffPaise()).subtract(itemTotalTax));

		// add up item level and bill level discounts as the total discount
		BigDecimal chargeDiscount = itemTotalDisc.add(sale.getDiscount());

		// rate*qty-disc = amt, qty=1, so rate=amt+disc
		BigDecimal chargeRate = chargeAmount.add(chargeDiscount);
		BigDecimal chargeOrigRate = chargeRate.add(itemTotalOrigAmtDiff);

		String billStatus = new BillDAO(con).getBillStatus(billNo);
		if (isBillNow && isCounterSales) {
			// nothing to do
		} else if ( billStatus != null && !billStatus.equals("A") ) {
			resultMap.put("error", "Bill : "+billNo+" is not open. Cannot make sale");
			return resultMap;
		}

		String chargeGroup = isReturns ? "RET" : "MED";
		String chargeHead = (isBillNow ? "PH" : "PHC") + chargeGroup;
		BasicDynaBean chead = new ChargeHeadsDAO().findByKey("chargehead_id", chargeHead);
		int subGroupId = (Integer) chead.get("service_sub_group_id");

		if (!isReturns) {
			charge = new ChargeDTO(chargeGroup, chargeHead, chargeRate, new BigDecimal(1),
					chargeDiscount, "", null, "Pharmacy sales bill", null,
					false, 0, subGroupId,0, visitType, visitId, null);

		} else {
			// invert the sale discount and round off as passed from UI.
			sale.setDiscount(sale.getDiscount().negate());
			sale.setRoundOffPaise(sale.getRoundOffPaise().negate());

			// use negative amount and discount in the charge
			charge = new ChargeDTO(chargeGroup, chargeHead, chargeRate.negate(),
					new BigDecimal(1), chargeDiscount.negate(), "",
					null, "Pharmacy return bill", null,
					false, 0, subGroupId,0, visitType, visitId, null);
		}

		if (newBill && !isReturns && isBillNow && !bill.getIs_tpa()) {
			charge.setRedeemed_points(bill.getRewardPointsRedeemed());
		}

		charge.setHasActivity(true);
		charge.setActRemarks("No. " + sale.getSaleId());
		charge.setUserRemarks(sale.getUserRemarks());
		charge.setBillNo(billNo);
		charge.setChargeId(chargeID);
		charge.setPrescribingDrId(docId);
		if(isReturns) {
			charge.setTaxAmt(itemTotalTax.negate());
			charge.setOriginalTaxAmt(itemTotalTax.negate());
		} else {
			charge.setTaxAmt(itemTotalTax);
			charge.setOriginalTaxAmt(itemTotalTax);
		}
	
		charge.setPostedDate(sale.getSaleDate());
		charge.setUsername(sale.getUsername());
		charge.setAccount_group(accountGroup);
		if (null != overallDiscountAuth)
			charge.setOverall_discount_auth(overallDiscountAuth);

		//since we are calculating the claim amts, set those here.
		if (isReturns){
			charge.setInsuranceClaimAmount(BigDecimal.ZERO);
		} else{
			charge.setInsuranceClaimAmount(totClaimAmt);
		}

		sale.setStoreRatePlanId(storeRatePlanId);
		sale.setChargeId(chargeID);

		/*
		 * Now write everything to the DB
		 */
		// 1. add a list of sales to the sales and sales_main table
		success = mdao.insertSale(sale);
		if (!success) {
			resultMap.put("error", "Sale insertion unsuccessful...");
			return resultMap;
		}
		success = mdao.insertSaleItems(sale.getSaleItems());
		if (!success) {
			resultMap.put("error", "Sale items insertion unsuccessful...");
			return resultMap;
		} else {
			for(MedicineSalesDTO saleItem: sale.getSaleItems()) {
				ArrayList<Map<String, Object>> taxMap = saleItem.getTaxMap();
				BasicDynaBean taxBean = salesTaxDao.getBean();
				Iterator<Map<String, Object>> taxMapIterator = taxMap.iterator();
				while(taxMapIterator.hasNext()) {
					Map<String, Object> taxSubMap = taxMapIterator.next();
					if(taxSubMap.get("tax_rate") != null) {
						taxBean.set("sale_item_id", saleItem.getSaleItemId());
						taxBean.set("item_subgroup_id", taxSubMap.get("item_subgroup_id"));
						taxBean.set("tax_rate", taxSubMap.get("tax_rate"));
						taxBean.set("tax_amt", isReturns ?  ((BigDecimal)taxSubMap.get("tax_amt")).negate() : taxSubMap.get("tax_amt"));
						taxBean.set("original_tax_amt", isReturns ?  ((BigDecimal)taxSubMap.get("tax_amt")).negate() : taxSubMap.get("tax_amt"));
						success = success && salesTaxDao.insert(con, taxBean);
					}
				}
				
			}
		}
		if (!success) {
			resultMap.put("error", "Sale items tax details insertion unsuccessful...");
			return resultMap;
		}
		//  2.5. Send the sale transactions to be cached into redis
    BasicDynaBean module = modulesDao.findByKey("module_id", "mod_scm");
    if ( module != null &&
        ((String)module.get("activation_status")).equals("Y")) {
      cacheSaleTransaction(visitType, visitId, sale, isReturns, cacheSaleTxns, centerId);
    }

		// Update the return qty, return amount and return claim amt for the charge of the sale id
		// which was used to set-off against any return.
		if (chargesToUpdate != null) {
			for (String chargeId : chargesToUpdate) {
				cdao.recalcSaleReturnAmounts(chargeId);
			}
		}

		if (!success) {
			resultMap.put("error", "Stock deduction unsuccessful");
			return resultMap;
		}
		charge.setPreAuthId(sale.getPreAuthId());
		// 3. Post the charges to the bill
		success = cdao.insertCharge(charge);

		List<ChargeDTO> actChargeList=new ArrayList<ChargeDTO>();

        //multi-payer
		//4.insert sales claim details and bill claim details
		BasicDynaBean salesClaimBean = null;
		BillChargeClaimDAO billChrClaDAO = new BillChargeClaimDAO();
		GenericDAO billClaimDAO = new GenericDAO("bill_claim");
		Map<Integer, ArrayList<BasicDynaBean>> planSalesDetails = new HashMap<Integer, ArrayList<BasicDynaBean>>();

		if ( bill.getIs_tpa() ){
			List<BasicDynaBean> visitPlanDetails = patInsrPlanDao.getPlanDetails(visitId);
			BigDecimal[] consolidatedsalesClaimAmounts = new BigDecimal[visitPlanDetails.size()];
			BigDecimal[] consolidatedsalesClaimTaxAmounts = new BigDecimal[visitPlanDetails.size()];
			
			ArrayList<BasicDynaBean> salesClaimList = null;
			for ( int i = 0;i<visitPlanDetails.size();i++ ) {
				BigDecimal claimOfSponsor = BigDecimal.ZERO;
				BigDecimal taxClaimOfSponsor = BigDecimal.ZERO;
				int planId = Integer.parseInt(visitPlanDetails.get(i).get("plan_id").toString());
				String sponsorId = patInsrPlanDao.getSponsorId(con,visitId,planId);
				salesClaimList = new ArrayList<BasicDynaBean>();
				//GenericDAO salesChargeClaimTax = new GenericDAO("sales_claim_tax_details");
				String claimId = billChrClaDAO.getClaimId(con,planId,billNo,visitId,sponsorId);

				for ( MedicineSalesDTO saleItem : sale.getSaleItems() ) {
					salesClaimBean = salesClaimDAO.getBean();
					claimOfSponsor = claimOfSponsor.add(isReturns ? BigDecimal.ZERO : saleItem.getClaimAmts().get(i));
					taxClaimOfSponsor = taxClaimOfSponsor.add(isReturns ? BigDecimal.ZERO : saleItem.getClaimTaxAmt().get(i));
					salesClaimBean.set("sale_item_id", saleItem.getSaleItemId());
					salesClaimBean.set("claim_status", saleItem.getClaimStatus());
					if(!isReturns){ 
					  // if completely sponsor payable
						if((ConversionUtils.setScale(saleItem.getTax()).compareTo(saleItem.getClaimTaxAmt().get(i)) == 0 || 
								ConversionUtils.setScaleDown(saleItem.getTax()).compareTo(saleItem.getClaimTaxAmt().get(i)) == 0)
								&& ConversionUtils.setScale(saleItem.getAmount().subtract(saleItem.getTax())).compareTo(saleItem.getClaimAmts().get(i)) == 0
								&& !(saleItem.getTax().compareTo(BigDecimal.ZERO) == 0 )) {
							salesClaimBean.set("ref_insurance_claim_amount", isReturns ? BigDecimal.ZERO : saleItem.getAmount().subtract(saleItem.getTax()));
							salesClaimBean.set("org_insurance_claim_amount", isReturns ? BigDecimal.ZERO : saleItem.getAmount().subtract(saleItem.getTax()));
						} else {
							salesClaimBean.set("ref_insurance_claim_amount", saleItem.getClaimAmts().get(i));
							salesClaimBean.set("org_insurance_claim_amount", saleItem.getClaimAmts().get(i));
						}
					}
					
					salesClaimBean.set("insurance_claim_amt", isReturns ? BigDecimal.ZERO : saleItem.getClaimAmts().get(i));
					salesClaimBean.set("include_in_claim_calc", isReturns ? false : saleItem.getInclude_in_claim_calc().get(i));
					salesClaimBean.set("claim_recd", saleItem.getClaimRecdAmt());
					salesClaimBean.set("denial_code", saleItem.getDenialCode());
					billingHelper.checkSaleItemsForInsCatInRedis(charge,saleItem,planId);
					salesClaimBean.set("insurance_category_id", saleItem.getInsuranceCategoryId());
					salesClaimBean.set("insurance_claim_amt", isReturns ? BigDecimal.ZERO : saleItem.getClaimAmts().get(i));
					salesClaimBean.set("return_insurance_claim_amt", saleItem.getReturnInsuranceClaimAmt());
					//salesClaimBean.set("prior_auth_id", saleItem.getPreAuthId());
					salesClaimBean.set("prior_auth_id", saleItem.getPriorAuthIds().get(i));
					salesClaimBean.set("prior_auth_mode_id",  saleItem.getPriorAuthMode().get(i));
					salesClaimBean.set("sponsor_id", sponsorId);
					salesClaimBean.set("tax_amt", isReturns ? BigDecimal.ZERO :saleItem.getClaimTaxAmt().get(i));
					
					salesClaimList.add(salesClaimBean);
					
					//success &= salesClaimDAO.insert(con, salesClaimBean);
				}

				planSalesDetails.put(planId, salesClaimList);
				consolidatedsalesClaimAmounts[i] = claimOfSponsor;
				consolidatedsalesClaimTaxAmounts[i] = taxClaimOfSponsor;
			}


			charge.setClaimAmounts(consolidatedsalesClaimAmounts);
			charge.setSponsorTaxAmounts(consolidatedsalesClaimTaxAmounts);
			actChargeList.add(charge);
			success = updateSalesBillClaimDetails(con,visitId,actChargeList,billNo);

			ArrayList<BasicDynaBean> salesClaimDetails = null;

			for ( int i = 0;i<visitPlanDetails.size();i++ ) {
				int planId = Integer.parseInt(visitPlanDetails.get(i).get("plan_id").toString());
				String sponsorId = patInsrPlanDao.getSponsorId(con,visitId,planId);
				String claimId = billChrClaDAO.getClaimId(con,planId,billNo,visitId,sponsorId);
				salesClaimDetails = planSalesDetails.get(planId);

				for(int j = 0;j<salesClaimDetails.size();j++){
					salesClaimBean = salesClaimDetails.get(j);
					salesClaimBean.set("claim_id", claimId);
					success &= salesClaimDAO.insert(con, salesClaimBean);
					
				}
			}

		}


		if (!success) {
			resultMap.put("error", "Bill charge insertion unsuccessful");
			return resultMap;
		}

		// 4. If returning against a visit, update the information in all the bills related to
		// the visit. If return bill no is empty, then it is a return against visit.
		if (isReturnAgstVisit && (visitId != null) && (!visitId.isEmpty()))
			mdao.updateReturnAgainstVisit(visitId, "Y");


		/**
		 * Update bill action in the last (when bill is to be closed for reward points earning)
		 */
		// 5. Update bill status and payment status after all the bill charges are inserted.

		if (newBill && isBillNow && isCounterSales) {
			bill.setInsuranceDeduction(BigDecimal.ZERO);
			bill.setClaimRecdAmount(BigDecimal.ZERO);

			if (!bill.getIs_tpa() || isReturns) { // close if bill now non-insured or returns
				bill.setStatus(Bill.BILL_STATUS_CLOSED);
				bill.setClosedDate(sale.getSaleDate());
				bill.setFinalizedDate(sale.getSaleDate());
				bill.setLastFinalizedAt(DateUtil.getCurrentTimestamp());
			} else { // finalize if bill now insured
				bill.setStatus(Bill.BILL_STATUS_FINALIZED);
				bill.setFinalizedDate(sale.getSaleDate());
				bill.setFinalizedBy(sale.getUsername());
				bill.setLastFinalizedAt(DateUtil.getCurrentTimestamp());
			}
			bill.setPaymentStatus("P");
			bill.setClosedBy(sale.getUsername());
			bill.setOkToDischarge(Bill.BILL_DISCHARGE_OK);

			success = new BillDAO(con).updateBill(bill);
			if (!success) {
				resultMap.put("error", "Bill status updation unsuccessful");
				return resultMap;
			}
		}

		// 6. Generate receipt if required
		if (newBill && isBillNow && isCounterSales) {
		  
		  // HMS-30960: Store mr_no in receipts for pharmacy new bill.
		  String mrNo = null;
		  if (null != patient) {
		    mrNo = (String) patient.get("mr_no");
		  }
		  bill.setMrno(mrNo);

			BigDecimal recptAmount = chargeAmount.subtract(totClaimAmt);
			if (depositsetOff != null && !depositsetOff.equalsIgnoreCase("")) {
				recptAmount = recptAmount.subtract(new BigDecimal(depositsetOff));
			}

			if (receiptList != null && receiptList.size() > 0) {
				success = ppdImpl.createReceipts(con, receiptList, bill, visitType, bill.getStatus());
			}

			if (!success) {
				resultMap.put("error", "Receipt creation unsuccessful");
				return resultMap;
			}
		}

		// return the bill number generated
		logger.debug("Sale success, returning bill number: " + billNo);

		resultMap.put("chargesUpdated", chargesToUpdate);
		if(newBill && isBillNow) {
		  resultMap.put("hospBillNo", billNo);
		}

		return resultMap;
	}

	/**
	 *
	 * @param returnItem -- Item which is being returned.
	 * @param salesForReturn -- List of all sale items for this patient/bill. Qty etc. is net of returns.
	 * @param saleItemsForReturns -- (Return param) List of original items against which returns are set off
	 * @param chargesToUpdate -- (Return Param) Sale item charges in bill to be updated.
	 */
	public static boolean setSaleItemsForReturns(MedicineSalesDTO returnItem,
			List<BasicDynaBean> salesForReturn, ArrayList<MedicineSalesDTO> saleItemsForReturns,
			List<String> chargesToUpdate) {

		if (salesForReturn == null)		// must be return without bill/visit
			return true;

		// Get the return item total return qty, net & amount.
		BigDecimal returnQty = returnItem.getQuantity().negate();
		BigDecimal returnAmt = returnItem.getAmount().negate();
		BigDecimal returnClaim = returnItem.getInsuranceClaimAmt().negate();

		int medicineId = Integer.parseInt(returnItem.getMedicineId());
		String batchNo = returnItem.getBatchNo();

		/*
		 * Iterate over the all the sold items to find a matching id/batch to set off against.
		 * We may need multiple sale records to satisfy the entire return quantity.
		 */
		for (BasicDynaBean sale : salesForReturn) {

			if ( (Integer)sale.get("medicine_id") != medicineId
					|| !((String)sale.get("batch_no")).equals(batchNo) )
				continue;

			// found a matching med-batch. Try and set it off
			BigDecimal saleQty = (BigDecimal)sale.get("qty");
			BigDecimal setOffQty, setOffAmt, setOffClaim;

			if (saleQty.compareTo(returnQty) > 0) {
				// there is enough sale to set it off against, set off the entire return qty.
				setOffQty = returnQty;
				setOffAmt = returnAmt;
				setOffClaim = returnClaim;
			} else {
				// we can set off a max of the original sale qty only, and continue iteration.
				setOffQty = saleQty;
				setOffAmt = (BigDecimal)sale.get("amount");
				setOffClaim =  (BigDecimal)sale.get("insurance_claim_amt");
			}

			if (!chargesToUpdate.contains((String)sale.get("charge_id")))
				chargesToUpdate.add((String)sale.get("charge_id"));

			MedicineSalesDTO rmdto = new MedicineSalesDTO();

			rmdto.setSaleItemId((Integer)sale.get("sale_item_id"));
			rmdto.setReturnQty(setOffQty.negate());
			rmdto.setReturnAmt(setOffAmt.negate());

			// Return claim amount is always zero for pharmacy sale item.
			rmdto.setReturnInsuranceClaimAmt(BigDecimal.ZERO);

			// Set the claim amount against the original sale item.
			// This includes the return claim amount.
			rmdto.setInsuranceClaimAmt(((BigDecimal)sale.get("insurance_claim_amt")).subtract(setOffClaim));

			saleItemsForReturns.add(rmdto);

			returnQty = returnQty.subtract(setOffQty);	// this will become 0 if enough sale qty
			returnAmt = returnAmt.subtract(setOffAmt);
			returnClaim = returnClaim.subtract(setOffClaim);

			if (returnQty.compareTo(BigDecimal.ZERO) <=0)
				break;
		}

		if (returnQty.compareTo(BigDecimal.ZERO) > 0) {
			// we couldn't set off completely: this should be an error.
			return false;
		}
		return true;
	}

	private static final String GET_SALES_DETAILS = " SELECT * FROM store_sales_details  pmsm ";

	public static final String GET_SALES_DETAILS_FROM_SALE_ID = GET_SALES_DETAILS + " WHERE sale_item_id=?";

	private static final String GET_SALE_ITEM_IDS = " SELECT sale_item_id FROM  store_sales_details where sale_id =? ";
	public static List getMedicineSalesDetailsDTO(Connection con,
			String saleId) throws SQLException{
		ArrayList<MedicineSalesDTO> saleItems = new ArrayList<MedicineSalesDTO>();
		PreparedStatement stmt = con.prepareStatement(GET_SALE_ITEM_IDS);
		stmt.setString(1, saleId);
		List saleItemIdList = DataBaseUtil.queryToArrayList1(stmt);
		stmt.close();
		if(saleItemIdList!= null && !saleItemIdList.isEmpty()) {
			for(int i=0; i<saleItemIdList.size(); i++ ){
				PreparedStatement ps = con.prepareStatement(GET_SALES_DETAILS_FROM_SALE_ID);
				ps.setInt(1, saleItemIdList.get(i)== null ? 0 : Integer.parseInt((String)saleItemIdList.get(i)));
				ResultSet rs = ps.executeQuery();
				MedicineSalesDTO charge = null;
				if (rs.next()) {
					charge = new MedicineSalesDTO();
					populateMedicineSalesDTO(charge, rs);
					saleItems.add(charge);
				}
				rs.close();
				ps.close();
			}
			return saleItems;
		} else {
			return null;
		}
	}

	private static void populateMedicineSalesDTO(MedicineSalesDTO charge, ResultSet rs)
	throws SQLException {
		charge.setSaleItemId(rs.getInt("sale_item_id"));
		charge.setSaleId(rs.getString("sale_id"));
		charge.setMedicineId(rs.getString("medicine_id"));
		charge.setBatchNo(rs.getString("batch_no"));
		charge.setQuantity(rs.getBigDecimal("quantity"));
		charge.setTax(rs.getBigDecimal("tax"));
		charge.setTaxPer(rs.getBigDecimal("tax_rate"));
		charge.setRate(rs.getBigDecimal("rate"));
		charge.setAmount(rs.getBigDecimal("amount"));
		charge.setOrigRate(rs.getBigDecimal("orig_rate"));
		charge.setPackageUnit(rs.getBigDecimal("package_unit"));
		charge.setExpiryDate(rs.getDate("expiry_date"));
		charge.setMedDiscRS(rs.getBigDecimal("disc"));
		charge.setBasis(rs.getString("basis"));
		charge.setMedDisc(rs.getBigDecimal("discount_per"));
		charge.setMrp(rs.getBigDecimal("pkg_mrp"));
		charge.setCp(rs.getBigDecimal("pkg_cp"));
		charge.setCodeType(rs.getString("code_type"));
		charge.setItemCode(rs.getString("item_code"));
		charge.setMedDiscType(rs.getString("discount_type"));
		charge.setClaimStatus(rs.getString("claim_status"));
		charge.setInsuranceClaimAmt(rs.getBigDecimal("insurance_claim_amt"));
		charge.setClaimRecdAmt(rs.getBigDecimal("claim_recd_total"));
		charge.setDenialCode(rs.getString("denial_code"));
		charge.setInsuranceCategoryId(rs.getInt("insurance_category_id"));
		charge.setSaleUnit(rs.getString("sale_unit"));
		charge.setReturnInsuranceClaimAmt(rs.getBigDecimal("return_insurance_claim_amt"));
		charge.setReturnAmt(rs.getBigDecimal("return_amt"));
		charge.setReturnQty(rs.getBigDecimal("return_qty"));
		charge.setPreAuthId(rs.getString("prior_auth_id"));
		charge.setPreAuthModeId(rs.getInt("prior_auth_mode_id"));
	}





	private static Map saveEstimate(Connection con, String visitId,
			MedicineSalesMainDTO estimate) throws SQLException{

		Map resultMap = new HashMap();

		MedicineSalesDAO mDao = new MedicineSalesDAO(con);
		boolean success = true;

		Iterator<MedicineSalesDTO> it = estimate.getSaleItems().iterator();
		estimate.setSaleId(mDao.getNextEstimateId());

		while (it.hasNext()) {
			MedicineSalesDTO estimateItem = it.next();
			estimateItem.setSaleItemId(mDao.getNextEstimateItemId());
			estimateItem.setSaleId(estimate.getSaleId());
			estimateItem.setSaleItemId(mDao.getNextSaleItemId());
		}
		success = mDao.insertEstimate(estimate, visitId);
		if (success)
			success = mDao.insertEstimateItems(estimate.getSaleItems());

		if (!success)
			resultMap.put("error", "Estimate save unsuccessful...");

		return resultMap;
	}


	public MedicineSalesDTO getSaleItemDetail(Connection con, int saleItemId) throws SQLException {
		MedicineSalesDTO saleItem = new MedicineSalesDTO();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_SALES_DETAILS_FROM_SALE_ID);
			ps.setInt(1, saleItemId);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				saleItem = new MedicineSalesDTO();
				populateMedicineSalesDTO(saleItem, rs);
			}
			rs.close();

		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
		return saleItem;
	}

	public MedicineSalesDTO getSaleItemDetail(int saleItemId) throws SQLException {
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			return getSaleItemDetail(con, saleItemId);

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	/**
	 * Inserts claim detaikls of issued items
	 * @param con
	 * @param visitId
	 * @param actChargeList
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	private static boolean updateSalesBillClaimDetails(Connection con,String visitId,List<ChargeDTO> actChargeList,String billNo)
	throws SQLException,IOException{
		PatientInsurancePlanDAO insPlanDAO = new PatientInsurancePlanDAO();
	    BillChargeClaimDAO chgClaimDAO = new BillChargeClaimDAO();
	    boolean sucess = true;

		List<BasicDynaBean> planList = insPlanDAO.getPlanDetails(con, visitId);
		int[] planIds = new int[planList.size()];
		for(int j = 0;j<planList.size();j++){
			planIds[j] = (Integer)((BasicDynaBean)planList.get(j)).get("plan_id");
		}
		if ( planIds.length > 0 )
			sucess = chgClaimDAO.insertBillChargeClaims(con, actChargeList, planIds,visitId,billNo);

		return sucess;
	}
	
	
	public void updateStoreSalesTaxDetailsForSale(MedicineSalesMainDTO saleMain, String saleType) throws SQLException, IOException {
		Connection con = null;
		boolean success = true;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			MedicineSalesDAO medicineSalesDAO = new MedicineSalesDAO(con);
			
			List<BasicDynaBean> salesClaimTaxDetails = medicineSalesDAO.getSalesClaimTaxDetailsForSale(saleMain.getSaleId());
			Iterator<BasicDynaBean> salesClaimTaxDetailsIterator = salesClaimTaxDetails.iterator();
			GenericDAO storeSalesTaxDetailsDAO = new GenericDAO("store_sales_tax_details");
			BasicDynaBean storeSalesTaxDetailsBean = storeSalesTaxDetailsDAO.getBean();
			
			//If salesClaimTaxDetails size > 0 means adjustments are there for sale item tax details so update it(Purpose is for KSA).
			if(salesClaimTaxDetails.size() > 0) {
				while(salesClaimTaxDetailsIterator.hasNext()) {
					BasicDynaBean salesClaimTaxDetailsBean = salesClaimTaxDetailsIterator.next();
					storeSalesTaxDetailsBean.set("tax_amt", salesClaimTaxDetailsBean.get("tax_amt"));
					
					Map<String, Object> keys = new HashMap<String, Object>();
					keys.put("sale_item_id", salesClaimTaxDetailsBean.get("sale_item_id"));
					keys.put("item_subgroup_id", salesClaimTaxDetailsBean.get("item_subgroup_id"));
					
					success &= storeSalesTaxDetailsDAO.update(con, storeSalesTaxDetailsBean.getMap(), keys) > 0;
				}
			
			// else we are going to update sale item tax details return_tax_amt against sale for returns.
			} else if(saleType.equals(MedicineSalesMainDTO.TYPE_SALES_RETURN)){
				for (MedicineSalesDTO saleItem : saleMain.getSaleItems()) {
					List<BasicDynaBean> salesReturnTaxDetails = medicineSalesDAO.getSalesTaxDetails(saleItem.getSaleItemId());
					List<Integer> soldItemsIds = saleItem.getSoldItemsIds();
					
					for(BasicDynaBean salesReturnTax:salesReturnTaxDetails) {
						BigDecimal taxAmt = (BigDecimal)salesReturnTax.get("tax_amt");
						Integer itemSubgroupId = (Integer)salesReturnTax.get("item_subgroup_id");
						for(Integer soldItemsId:soldItemsIds) {
							List<BasicDynaBean> salesTaxDetails = medicineSalesDAO.getSalesTaxDetails(soldItemsId, itemSubgroupId);
							for(BasicDynaBean salesTaxDetail:salesTaxDetails) {
								success &= medicineSalesDAO.updateItemSalesTaxDetails((Integer)salesTaxDetail.get("sale_item_id"), 
										itemSubgroupId, taxAmt);
							}
						}
					}
					
				}
			}
			

		} finally {
			DataBaseUtil.commitClose(con, success);
		}
	}
	
	public void updateStoreSalesTaxDetailsForVisit(String visitId) throws SQLException, IOException {
		Connection con = null;
		boolean success = true;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			MedicineSalesDAO medicineSalesDAO = new MedicineSalesDAO(con);
			
			List<BasicDynaBean> salesClaimTaxDetails = new MedicineSalesDAO(con).getSalesClaimTaxDetailsForVisit(visitId);
			Iterator<BasicDynaBean> salesClaimTaxDetailsIterator = salesClaimTaxDetails.iterator();
			while(salesClaimTaxDetailsIterator.hasNext()) {
				BasicDynaBean salesClaimTaxDetailsBean = salesClaimTaxDetailsIterator.next();
				success &= medicineSalesDAO.updateItemSalesTaxDetailsForSaleReturn((Integer)salesClaimTaxDetailsBean.get("sale_item_id"), salesClaimTaxDetailsBean.get("item_subgroup_id"), salesClaimTaxDetailsBean.get("tax_amt"));
			}

		} finally {
			DataBaseUtil.commitClose(con, success);
		}
		
	}
	
	public Map<String, Object> getSaleTaxDetails(int saleItemId, String reqQty) throws SQLException {
		Map<String, Object> result = new HashMap<String, Object>();
		BasicDynaBean salesItemDetails = MedicineSalesDAO.getSalesItemDetailsReturns(saleItemId, reqQty);
		List<BasicDynaBean> salesItemTaxDetails = MedicineSalesDAO.getSalesItemTaxDetailsReturns(saleItemId, reqQty);
		int medicineId = -1;
		BigDecimal netAmount = BigDecimal.ZERO;
		BigDecimal discountAmt = BigDecimal.ZERO;
		BigDecimal originalTaxAmt = BigDecimal.ZERO;
		
		if(salesItemDetails != null) {
			medicineId = (Integer)salesItemDetails.get("medicine_id");
			netAmount = (BigDecimal)salesItemDetails.get("sale_amount");
			discountAmt = (BigDecimal)salesItemDetails.get("discount");
			originalTaxAmt = (BigDecimal)salesItemDetails.get("original_tax_amt");
		}
		
		
		Iterator<BasicDynaBean> salesTaxDetailsIterator = salesItemTaxDetails.iterator();
		List<Map<Integer, Object>> subgroupMapList = new ArrayList<Map<Integer, Object>>();
		while(salesTaxDetailsIterator.hasNext()) {
			Map<Integer, Object> subgroupMap = new HashMap<Integer, Object>();
			BasicDynaBean salesTaxBean = salesTaxDetailsIterator.next();
			subgroupMap.put((Integer)salesTaxBean.get("tax_sub_group_id"), salesTaxBean.getMap());
			subgroupMapList.add(subgroupMap);
		}
		result.put("medicine_id", medicineId);
		result.put("net_amount", netAmount.add(discountAmt));
		result.put("discount_amount", discountAmt);
		result.put("original_tax", originalTaxAmt);
		result.put("tax_details", subgroupMapList);
		return result;
	}
	
	public boolean updateSaleTaxDetails(Connection con, BasicDynaBean saleDetailsBean) throws SQLException, IOException {
		boolean success = true;
		MedicineSalesDAO medicineSalesDAO = new MedicineSalesDAO(con);
		
		GenericDAO storeSalesTaxDetailsDAO = new GenericDAO("store_sales_tax_details");
		BasicDynaBean storeSalesTaxDetailsBean = storeSalesTaxDetailsDAO.getBean();
		
		GenericDAO storeSalesDetails = new GenericDAO("store_sales_details");
		
		List<BasicDynaBean> salesClaimTaxDetails = medicineSalesDAO.getSalesClaimTaxDetailsForSale(String.valueOf(saleDetailsBean.get("sale_id")));
		Iterator<BasicDynaBean> salesClaimTaxDetailsIterator = salesClaimTaxDetails.iterator();
		//If salesClaimTaxDetails size > 0 means adjustments are there for sale item tax details so update it(Purpose is for KSA).
		if(salesClaimTaxDetails.size() > 0) {
			while(salesClaimTaxDetailsIterator.hasNext()) {
				BasicDynaBean salesClaimTaxDetailsBean = salesClaimTaxDetailsIterator.next();
				storeSalesTaxDetailsBean.set("tax_amt", salesClaimTaxDetailsBean.get("tax_amt"));
				
				Map<String, Object> keys = new HashMap<String, Object>();
				keys.put("sale_item_id", salesClaimTaxDetailsBean.get("sale_item_id"));
				keys.put("item_subgroup_id", salesClaimTaxDetailsBean.get("item_subgroup_id"));
				
				success &= storeSalesTaxDetailsDAO.update(con, storeSalesTaxDetailsBean.getMap(), keys) > 0;
			}
		
			BasicDynaBean saleTax = medicineSalesDAO.getSumOfItemTax((Integer)saleDetailsBean.get("sale_item_id"));
			if(saleDetailsBean.get("amount") != null) {
				BigDecimal existTaxAmt = (BigDecimal)saleDetailsBean.get("tax");
				BigDecimal existAmt = (BigDecimal)saleDetailsBean.get("amount");
				BigDecimal newAmt = (existAmt.subtract(existTaxAmt)).add((BigDecimal)saleTax.get("tax_amt"));
				saleDetailsBean.set("amount", newAmt);
			}
			saleDetailsBean.set("tax", saleTax.get("tax_amt"));
			
			Map<String, Object> keys = new HashMap<String, Object>();
			keys.put("sale_item_id", saleDetailsBean.get("sale_item_id"));
			
			success &= storeSalesDetails.update(con, saleDetailsBean.getMap(), keys) > 0;
		} 
		return success;
	}
	
	public boolean insertOrUpdateBillChargeTaxesForSales(Connection con, String saleId) throws SQLException, IOException {
		MedicineSalesDAO medicineSalesDAO = new MedicineSalesDAO(null);
		return medicineSalesDAO.insertOrUpdateBillChargeTaxesForSales(con, saleId);
	}
	
	public static Map validateGovernmentIdentifier(BasicDynaBean custBean) throws SQLException {
      RegistrationPreferencesDTO regPrf = RegistrationPreferencesDAO.getRegistrationPreferences();
      Integer govtIdentifierId = null;
      Map<String, Object> resultMap = new HashMap<String, Object>();
      if(custBean.get("identifier_id") != null)
         govtIdentifierId = (Integer)custBean.get("identifier_id");
      String govtIdentifierValue = (String)custBean.get("government_identifier");
      
      Map<String, Object> key = new HashMap<String, Object>();
      key.put("identifier_id", govtIdentifierId);
      
    if (govtIdentifierId != null) { // field is present

      if (null != govtIdentifierValue && !StringUtils.isEmpty(govtIdentifierValue)) { 
        // Government identifier value is not empty.
        
        BasicDynaBean govtIdentfierBean = new GenericDAO("govt_identifier_master").findByKey(key);
        // Check the pattern
        if (null != govtIdentfierBean && null != govtIdentfierBean.get("govt_id_pattern")
            && !StringUtils.isEmpty((String) govtIdentfierBean.get("govt_id_pattern"))) {

          String govtIdPattern = (String) govtIdentfierBean.get("govt_id_pattern");
          String regExPattern = getGovtIdRegEx(govtIdPattern);
          Pattern p = Pattern.compile(regExPattern);
          Matcher m = p.matcher(govtIdentifierValue);
          if (!m.matches()) {
            // Government Identifier value is wrong. Not matched with pattern.
            resultMap.put("error", regPrf.getGovernment_identifier_label()
                + " is Invalid. Correct Format is : " + govtIdPattern);
            return resultMap;
          }
        }
      }
    }
    return resultMap;
  }
    
	private static String getGovtIdRegEx(String pattern) {
      String regexp = "";
      for (int i = 0; i < pattern.length(); i++) {
          String str1 = "";
          str1 = str1 + pattern.charAt(i);
          str1.trim();
          if (str1.equals("9")) {
              regexp = regexp + "(\\d{1})";
          } else if (str1.equals("x") || str1.equals("X")) {
              regexp = regexp + "([A-Za-z]{1})";
          } else {
              regexp = regexp + "([" + str1.toLowerCase() + "" + str1.toUpperCase() + "]{1})";
          }
      }
      return "^" + regexp + "$";
	}
	
	public BasicDynaBean getBillDetails(String saleId) throws SQLException {
		return new MedicineSalesDAO(null).getBillDetails(saleId);
	}
	
  private static Map formatAndValidateMobileNumber(BasicDynaBean bean) throws SQLException {
    BasicDynaBean genericPreferences = GenericPreferencesDAO.getAllPrefs();
    String mobileNo = "";
    Integer centerId = 0;
    if (null != bean.get("phone_no"))
      mobileNo = (String) bean.get("phone_no");
    if (null != bean.get("center_id"))
      centerId = (Integer) bean.get("center_id");

    Map<String, Object> errorMap = new HashMap<String, Object>();
    if (mobileNo != null && !mobileNo.isEmpty()) {
      CenterMasterDAO centerDAO = new CenterMasterDAO();
      String defaultCode = centerDAO.getCountryCode(centerId);

      if (defaultCode == null) {
        defaultCode = centerDAO.getCountryCode(0);
      }
      if (defaultCode != null && !mobileNo.startsWith("+")) {
        bean.set("phone_no", "+" + defaultCode + mobileNo);
      }
      if (bean.get("phone_no").toString().length() > 16) {
        errorMap.put("error", "Please enter a valid mobile number.");
        return errorMap;
      }
      if("Y".equals(genericPreferences.get("mobile_number_validation"))){
        errorMap = validateMobileNumber(bean, defaultCode);
      }
    }
    return errorMap;
  }
  
  public static Map<String, Object> validateMobileNumber(BasicDynaBean bean, String defaultCode)
      throws SQLException {
    Map<String, Object> errorMap = new HashMap<String, Object>();
    if (null != bean) {
      BasicDynaBean genericPreferences = GenericPreferencesDAO.getAllPrefs();
      String phoneNumber = (String) bean.get("phone_no");
      phoneNumber = phoneNumber.replaceFirst("^0+(?!$)", "");
      if (phoneNumber == null || phoneNumber.isEmpty()) {
        return errorMap;
      }
      boolean isValid = false;
      List<String> phone = PhoneNumberUtil.getCountryCodeAndNationalPart(phoneNumber, null);
      String countryCode = null;
      String national = null;
      if ((phone == null || phone.get(0) == null) && defaultCode == null) {
        errorMap.put("error", "Please enter a mobile number with country code.");
        return errorMap;
      }
      if (phone == null) {
        String appendedNo = (defaultCode != null ? ("+" + defaultCode) : "") + phoneNumber;
        // phone is not in E.164 format, So check whether number is valid using appendedNo which is
        // in E.164
        isValid =
            PhoneNumberUtil.isValidNumberMobile(appendedNo)
                || PhoneNumberUtil.isMatches(phoneNumber,
                    (String) genericPreferences.get("mobile_starting_pattern"),
                    (String) genericPreferences.get("mobile_length_pattern"));
        if (!isValid && defaultCode != null && phoneNumber.startsWith(defaultCode)) {
          // If the phoneNumber is in E.164 without "+" prefix, check if it is valid by prepending
          // "+"
          appendedNo = "+" + phoneNumber;
          isValid =
              PhoneNumberUtil.isValidNumberMobile(appendedNo)
                  || PhoneNumberUtil.isMatches(phoneNumber.substring(defaultCode.length()),
                      (String) genericPreferences.get("mobile_starting_pattern"),
                      (String) genericPreferences.get("mobile_length_pattern"));
        }
        if (isValid) {
          bean.set("phone_no", appendedNo);
        }
      } else {
        countryCode = phone.get(0);
        national = phone.get(1);
        if (countryCode.equals(defaultCode)) { // is the country code equal to country of DEFAULT
                                               // CENTER
          isValid =
              PhoneNumberUtil.isMatches(national,
                  (String) genericPreferences.get("mobile_starting_pattern"),
                  (String) genericPreferences.get("mobile_length_pattern"))
                  || PhoneNumberUtil.isValidNumberMobile(phoneNumber);
        } else { // for International number i.e other the Hospital country
          isValid = PhoneNumberUtil.isValidNumberMobile(phoneNumber);
        }
        if (isValid && countryCode != null) {
          bean.set("phone_no", "+" + countryCode + national);
        }
      }
      if (!isValid) {
        countryCode = countryCode == null ? defaultCode : countryCode;
        if (countryCode != null) {
          errorMap.put(
              "error",
              "Mobile No. is invalid. Example mobile no. -"
                  + Arrays.asList("+" + countryCode
                      + PhoneNumberUtil.getExampleNumber(Integer.parseInt(countryCode))));
        } else {
          errorMap.put(
              "error",
              "Mobile number is invalid. Please enter a mobile number with country code or enter a "
                  + genericPreferences.get("mobile_length_pattern")
                  + " digit long number starting with "
                  + genericPreferences.get("mobile_starting_pattern"));
        }
      }
    }
    return errorMap;
  }

  protected static void cacheSaleTransaction(String visitType, String visitId, MedicineSalesMainDTO sale,
      Boolean isReturns, List<Map<String, Object>> cacheSaleTxns, Integer centerId) {
    try {
      BasicDynaBean saleMain = new GenericDAO("store_sales_main").getBean();
      saleMain.set("sale_id", sale.getSaleId());
      if (NumberUtils.isParsable(sale.getStoreId())) {
        saleMain.set("store_id", Integer.parseInt(sale.getStoreId()));
      }
      saleMain.set("sale_date", sale.getSaleDate());
      saleMain.set("date_time", sale.getDateTime());
      saleMain.set("bill_no", sale.getBillNo());
      saleMain.set("discount", sale.getDiscount());
      saleMain.set("round_off", sale.getRoundOffPaise());
      saleMain.set("username", sale.getUsername());
      String mrNo = null;
      BasicDynaBean patient = new GenericDAO("patient_registration")
          .findByKey("patient_id", visitId);
      if (patient != null) {
        mrNo = patient.get("mr_no").toString();
      }
      Boolean billFlag = true;

      for (MedicineSalesDTO saleItem : sale.getSaleItems()) {
        BasicDynaBean saleDetails = new GenericDAO("store_sales_details").getBean();
        saleDetails.set("sale_item_id", saleItem.getSaleItemId());
        if (NumberUtils.isParsable(saleItem.getMedicineId())) {
          saleDetails.set("medicine_id", Integer.parseInt(saleItem.getMedicineId()));
        }
        saleDetails.set("batch_no", saleItem.getBatchNo());
        saleDetails.set("orig_rate", saleItem.getOrigRate());
        saleDetails.set("quantity", saleItem.getQuantity());
        saleDetails.set("disc", saleItem.getMedDiscRS());
        saleDetails.set("discount_per", saleItem.getMedDisc());
        saleDetails.set("sale_unit", saleItem.getSaleUnit());
        saleDetails.set("cost_value", saleItem.getCostValue());
        saleDetails.set("amount", saleItem.getAmount());
        saleDetails.set("tax", saleItem.getTax());
        saleDetails.set("expiry_date", saleItem.getExpiryDate());
        saleDetails.set("package_unit",saleItem.getPackageUnit());

        Map<String, Object> saleData = invScmService.getSaleMap(saleMain,
            saleDetails, visitId, mrNo, centerId, billFlag);
        cacheSaleTxns.add(saleData);

        billFlag = false;
      }
    } catch (SQLException e) {
      logger.error("Error in porcessing for caching : ", e);
    }

  }
	
}