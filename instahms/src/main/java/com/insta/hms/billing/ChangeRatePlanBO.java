/**
 *
 */
package com.insta.hms.billing;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.adminmasters.services.MasterServicesDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.bob.hms.otmasters.opemaster.OperationMasterDAO;
import com.bob.hms.otmasters.theamaster.TheatreMasterDAO;
import com.insta.hms.OTServices.OperationDetailsDAO;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.core.insurance.SponsorService;
import com.insta.hms.core.inventory.issues.PatientIssueService;
import com.insta.hms.core.inventory.stockmgmt.StockService;
// import com.insta.hms.core.tax.ItemTaxDetails;
// import com.insta.hms.core.tax.TaxContext;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
import com.insta.hms.master.AnaesthesiaTypeMaster.AnaesthesiaTypeChargesDAO;
import com.insta.hms.master.CenterPreferences.CenterPreferencesDAO;
import com.insta.hms.master.CommonChargesMaster.CommonChargesDAO;
import com.insta.hms.master.ConsultationCharges.ConsultationChargesDAO;
import com.insta.hms.master.DietaryMaster.DietaryMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.EquipmentMaster.EquipmentChargeDAO;
import com.insta.hms.mdm.organization.OrganizationService;
import com.insta.hms.orders.OrderBO;
import com.insta.hms.outpatient.DoctorConsultationDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author deepasri.prasad
 *
 */
public class ChangeRatePlanBO {

	private static final GenericDAO doctorsDao = new GenericDAO("doctors");

  static Logger logger = LoggerFactory.getLogger(ChangeRatePlanBO.class);
	
	private static PatientInsurancePlanDAO patientInsurancePlanDAO = new PatientInsurancePlanDAO();
	private static GenericDAO patientRegistrationDao = new GenericDAO("patient_registration");
	private static GenericDAO admissionDao = new GenericDAO("admission");
	private static BillChargeClaimDAO billChargeClaimDao = new BillChargeClaimDAO();
	private static BillChargeTaxDAO billChargeTaxDAO = new BillChargeTaxDAO();
	private static DoctorConsultationDAO doctorConsultationDao = new DoctorConsultationDAO();
	private static GenericDAO bedOperationScheduleDao = new GenericDAO("bed_operation_schedule");
	private static GenericDAO bedOperationSecondaryScheduleDao = new GenericDAO("bed_operation_secondary");
	private static OperationMasterDAO operationMasterDao = new OperationMasterDAO();
	private static ConsultationChargesDAO consultationChargesDao = new ConsultationChargesDAO();
	private static TheatreMasterDAO theatreMasterDao = new TheatreMasterDAO();
	private static AnaesthesiaTypeChargesDAO anaesthesiaTypeChargesDao = new AnaesthesiaTypeChargesDAO();
	private static GenericDAO storeItemDetailsDao = new GenericDAO("store_item_details");
	private static GenericDAO billChargeDao = new GenericDAO("bill_charge");
	private static GenericDAO patPackageContentConsumed = new GenericDAO("patient_package_content_consumed");
	private static GenericDAO packagePrescribedDao = new GenericDAO("package_prescribed");
	private static GenericDAO patCustPackDetails = new GenericDAO("patient_customised_package_details");
	private static final GenericDAO surgeryAnesthesiaDetails = new GenericDAO("surgery_anesthesia_details");
	private static final GenericDAO equipmentPrescribedDAO = new GenericDAO("equipment_prescribed");

  private static final PatientIssueService patIssueService = ApplicationContextProvider
      .getBean(PatientIssueService.class);
  private static final StockService stockService = ApplicationContextProvider
      .getBean(StockService.class);
  private static final OrganizationService organizationService = ApplicationContextProvider
      .getBean(OrganizationService.class);
  private static final SponsorService sponsorService = ApplicationContextProvider
      .getBean(SponsorService.class);
	private static Map patientDetailsMap = new HashMap();
	
	BasicDynaBean patientDetails = null;
	BasicDynaBean existingBillDetails = null;
	BasicDynaBean updatedBillDetails = null;

	private Connection con = null;

	private static Boolean isInsurance = false;

	private static int planId = 0;
	private static int[] planIds = null;

	private static String visitType = "", visitId = "", userName = "";

	public enum ChargeGroup {
		REG, DIA, DOC, BED, ICU, OPE, MED, ITE, OTC, TAX, SNP, DIS, PKG, RET, DIE, DRG, PDM;
	};

	private StringBuilder successMsg = new StringBuilder();
	private List<String> ratePlanNotApplicableList = null;
	private boolean editVisits = false;

	public void setRatePlanNotApplicableList(List<String> ratePlanNotApplicableList) {
		this.ratePlanNotApplicableList = ratePlanNotApplicableList;
	}

	public List getRatePlanNotApplicableList() {
		return ratePlanNotApplicableList;
	}

	public void setSuccessMsg(StringBuilder successMsg) {
		this.successMsg = successMsg;
	}

	public StringBuilder getSuccessMsg() {
		return successMsg;
	}

	public void setEditVisits(boolean editVisits) {
		this.editVisits = editVisits;
	}

	public boolean getEditVisits() {
		return editVisits;
	}

	public static void initBillDetails() {
		isInsurance = patientDetailsMap.get("primary_sponsor_id") != null && !((String)patientDetailsMap.get("primary_sponsor_id")).isEmpty();
		planId = patientDetailsMap.get("plan_id") == null ? 0 : (Integer) patientDetailsMap.get("plan_id");
		visitType = (String)patientDetailsMap.get("visit_type");
		visitId = (String)patientDetailsMap.get("patient_id");
	}

	public String updateChargesBedAndRateWise(Connection con, String visitId, String newBedType)throws Exception{
		this.con = con;
		return updateChargesBedAndRateWise(visitId, newBedType, null, null);
	}

	public String updateChargesBedAndRateWise(String visitId, String newBedType)throws Exception{
		return updateChargesBedAndRateWise(visitId, newBedType, null, null);
	}

	public String updateChargesBedAndRateWiseNew(Connection con, String visitId, String newBedType)throws Exception{
		this.con = con;
		return updateChargesBedAndRateWiseNew(visitId, newBedType, null, null);
	}

	public String updateChargesBedAndRateWiseNew(String visitId, String newBedType, 
			java.sql.Timestamp from, java.sql.Timestamp to) throws Exception {

		Boolean success = false;
		userName = RequestContext.getUserName();
		try{
			if (this.con == null) {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				if (visitId == null || visitId.equals("")) {
					logger.error("visitId was not found");
					con.close();
					return "";
				}

				patientDetails = new VisitDetailsDAO().findByKey("patient_id", visitId);
			}else {

				patientDetails = new VisitDetailsDAO().findByKey(con,"patient_id", visitId);
			}

			planIds = patientInsurancePlanDAO.getPlanIds(con, visitId);

			initBillDetailsNew();

			String orgId = (String) patientDetails.get("org_id");
			String mrNo = (String) patientDetails.get("mr_no");

			if (orgId == null || orgId.equals("")) {
				logger.warn("Rate Plan not found...Setting to the Default Rate Plan ");
				orgId = "ORG0001";
			}
			Map params = new HashMap<>();
			params.put("org_id", orgId);
			Object storeRatePlan = organizationService.findByPk(params).get("store_rate_plan_id");
			String storeRatePlanId = null;
			if(null != storeRatePlan) {
			  storeRatePlanId = String.valueOf(storeRatePlan);
			}
			String bedType = (String) patientDetails.get("bed_type");
			if (newBedType != null && !newBedType.equals("")) {
				bedType = newBedType;
			}
			if (bedType == null || bedType.equals("")) {
				logger.warn("Bed Type not found...Setting to the Default Bed Type");
				bedType = "GENERAL";
			}

			boolean insuranceDetailsChanged = true;
			//List openBills = BillDAO.getAllActiveBills(visitId);
			List<BasicDynaBean> openBills = BillDAO.getAllActiveBillsNew(visitId);
			List<ChargeDTO> chargeList = new ArrayList<ChargeDTO>();
			List<ChargeDTO> refChargeList = new ArrayList<ChargeDTO>();
			BillBO billBOObj = new BillBO();
			DiscountPlanBO discBO = new DiscountPlanBO();
			ChargeDAO cdao = new ChargeDAO(con);
			List<ChargeDTO> excludedChargeList = new ArrayList<ChargeDTO>();
			
			if (!editVisits)
				successMsg.append("This patient has "+openBills.size()+" Open Bill(s)...<br/> Updated Charges for Bill(s) : </br>");
			Map<String, BasicDynaBean> visitIssues = new HashMap<>();
			Map<String, BasicDynaBean> visitIssueReturns = new HashMap<>();

			BillDetails billDetails = null;
			for (BasicDynaBean billBean : openBills) {
				//get all charges from open patient bills...
				String billNo = (String)billBean.get("bill_no");
				billDetails = billBOObj.getBillDetails(con,billNo);
				visitIssues.putAll(patIssueService.getVisitIssuedItemMap(billNo));
        visitIssueReturns.putAll(patIssueService.getVisitIssueReturnedItemMap(billNo));
				if (editVisits) {
					//String visitSponsor		= (String)patientDetails.get("primary_sponsor_id");
					String visitRatePlan	= (String)patientDetails.get("org_id");
					//int visitPlan			= (Integer)patientDetails.get("plan_id");

					BasicDynaBean existingVisit		= patientRegistrationDao.findByKey("patient_id", visitId);
					//String existingVisitSponsor		= (String)existingVisit.get("primary_sponsor_id");
					String existingVisitRatePlan	= (String)existingVisit.get("org_id");
					//int existingVisitPlan			= (Integer)existingVisit.get("plan_id");

					Map<String,Object> priPlanKeys = new HashMap<String, Object>();
					priPlanKeys.put("patient_id", visitId);
					priPlanKeys.put("priority", 1);
					BasicDynaBean priPlanBean = patientInsurancePlanDAO.findByKey(con,priPlanKeys);
					BasicDynaBean existingPriPlanBean = patientInsurancePlanDAO.findByKey(priPlanKeys);

					Map<String, Object> secPlanKeys = new HashMap<String, Object>();
					secPlanKeys.put("patient_id", visitId);
					secPlanKeys.put("priority", 2);
					BasicDynaBean secPlanBean = patientInsurancePlanDAO.findByKey(con,secPlanKeys);
					BasicDynaBean existingsecPlanBean = patientInsurancePlanDAO.findByKey(secPlanKeys);

					String exisitnPriSponsor = null, primarySponsorId = null, existingSecSponsor = null, secondarySponsorId = null;
					int exisitngPriPlan = 0, existingSecPlan = 0, planId = 0, secPlanId = 0;

					if(null != priPlanBean && null != priPlanBean.get("sponsor_id"))
						primarySponsorId = (String)priPlanBean.get("sponsor_id");
					if(null != existingPriPlanBean && null != existingPriPlanBean.get("sponsor_id"))
						exisitnPriSponsor = (String)existingPriPlanBean.get("sponsor_id");
					if(null != secPlanBean && null != secPlanBean.get("sponsor_id"))
						secondarySponsorId = (String)secPlanBean.get("sponsor_id");
					if(null != existingsecPlanBean && null != existingsecPlanBean.get("sponsor_id"))
						existingSecSponsor = (String)existingsecPlanBean.get("sponsor_id");


					if(null != priPlanBean && null != priPlanBean.get("plan_id"))
						planId = (Integer)priPlanBean.get("plan_id");
					if(null != existingPriPlanBean && null != existingPriPlanBean.get("plan_id"))
						exisitngPriPlan = (Integer)existingPriPlanBean.get("plan_id");
					if(null != secPlanBean && null != secPlanBean.get("plan_id"))
						secPlanId = (Integer)secPlanBean.get("plan_id");
					if(null != existingsecPlanBean && null != existingsecPlanBean.get("plan_id"))
						existingSecPlan = (Integer)existingsecPlanBean.get("plan_id");

					// If tpa is removed and/or no rate plan change (or) no insurance details change (including rate plan)
					// then continue i.e rate changes not required.
					if (( (exisitnPriSponsor != null && primarySponsorId != null && exisitnPriSponsor.equals(primarySponsorId))
							&& ( (existingSecSponsor == null && secondarySponsorId == null )
								|| (existingSecSponsor != null && secondarySponsorId != null && existingSecSponsor.equals(secondarySponsorId)) ))
						&& ( ( exisitngPriPlan == planId ) && (existingSecPlan == secPlanId) )
						&& visitRatePlan.equals(existingVisitRatePlan)){
						insuranceDetailsChanged = false;
						continue;
					}
				}
				chargeList.addAll(billDetails.getCharges());
				
				successMsg.append(billBean.get("bill_no"));
				successMsg.append(", ");
			}
			for (int i = 0; i < chargeList.size(); i++) {
				ChargeDTO cdto = (ChargeDTO) chargeList.get(i);
				existingBillDetails = new BillDAO().findByKey("bill_no", cdto.getBillNo());
				    //BillDAO.getBillBean(cdto.getBillNo());
				updatedBillDetails = new BillDAO(con).findByKey("bill_no", cdto.getBillNo());
				    //BillDAO.getBillBean(con,cdto.getBillNo());			
				if (cdto != null && ("X".equals(cdto.getStatus()) || (cdto.getChargeRef() != null
						&& !cdto.getChargeRef().equals("") && !"PKG".equals(cdto.getChargeGroup())))) {
					continue; // ignore cancelled && referral charges
				}
				if(from != null && to != null){ // apply date filters
					if(cdto.getPostedDate().before(from) || cdto.getPostedDate().after(to)){
						continue; // ignore charges which dont fall within specified dates.
					}
				}

				//Bill b = new BillDAO(con).getBill(cdto.getBillNo());
				Integer centerId = (Integer)patientDetails.get("center_id");
				String prefRatePlan = CenterPreferencesDAO.getRatePlanForNonInsuredBills(centerId);
				String  mvPackageId = billBOObj.getMultiVisitBillPackageId(cdto.getBillNo()).toString();
				boolean isMultiVisitPkgBill = Integer.parseInt(mvPackageId) > 0;

				if(patientDetails.get("primary_sponsor_id") != null && !patientDetails.get("primary_sponsor_id").equals("")
						&& !(boolean)updatedBillDetails.get("is_tpa") && prefRatePlan != null){
					orgId = prefRatePlan;
					//commenting as it id Fix for 56261 resetDiscountCategory(con, b.getBillNo());
				} else {
					orgId =  (String) patientDetails.get("org_id");
				}

				refChargeList = getRefCharges(chargeList, cdto);
				success = false;
				if(isMultiVisitPkgBill){
					continue; // ignoring the multi-visit package charges update
					/* String mvpChargeGroup = cdto.getChargeGroup();
					if(mvpChargeGroup.equals("DOC") || mvpChargeGroup.equals("DIA") || 
							(mvpChargeGroup.equals("SNP") && cdto.getChargeHead().equals("SERSNP")) || mvpChargeGroup.equals("OTC"))
					success = updateMultiVisitPackageItemCharegsNew(cdto,orgId,bedType,refChargeList,mvPackageId);*/
				} else {
					switch (ChargeGroup.valueOf(cdto.getChargeGroup())) {
					case REG:
						success = updateRegistrationChargeNew(cdto, orgId, bedType, refChargeList);
						break;

					case DOC:
						success = updateDoctorChargeNew(cdto, orgId, bedType, refChargeList);
						break;

					case DIA:
						success = updateDiagChargeNew(cdto, orgId, bedType, refChargeList);
						break;

					case SNP:
						success = updateServiceChargeNew(cdto, orgId, bedType, refChargeList);
						break;

					case DIE:
						success = updateDietaryChargeNew(cdto, orgId, bedType, refChargeList);
						break;

					case OTC:
						if (cdto.getChargeHead().startsWith("EQ")) {
							success = updateEquipmentChargeNew(cdto, orgId, bedType, refChargeList);
						} else if (cdto.getChargeHead().startsWith("MIS")) {
							success = true;
						} else {
							success = updateOtherChargeNew(cdto, orgId, bedType, refChargeList);
						}
						break;

					case PKG:
						if ("PKGPKG".equals(cdto.getChargeHead()) || cdto.getPackageId() != null ) {
							success = updatePackageChargeNew(cdto, orgId, bedType, refChargeList);
						}
						break;

					case BED:
					case ICU:
						success = updateBedChargeNew(cdto, orgId, bedType, refChargeList);
						break;

					case OPE:
						if (cdto.getChargeHead().startsWith("EQ")) {
							success = updateEquipmentChargeNew(cdto, orgId, bedType, refChargeList);
						} else {
							success = updateSurgeryChargeNew(cdto, orgId, bedType, refChargeList);
						}
						break;

					case MED:
					case ITE:
            if (cdto.getChargeHead().equals("INVITE")) {
              success = updatePatientIssuesChargeNew(cdto, mrNo, storeRatePlanId, refChargeList,
                  visitIssues);
            } else {
              success = true;
            }
            break;
					case RET:
            if (cdto.getChargeHead().equals("INVRET")) {
              success = updatePatientIssuesChargeNew(cdto, mrNo, storeRatePlanId, refChargeList,
                  visitIssueReturns);
            } else {
              success = true;
            }
            break;
					case TAX:
					case DIS:
					default:
						success = true;
						/*
						 * Ignore medicine, inventory, tax, DRG, PDM charges and other misc charges as
						 * they are rate plan independent charges.
						 */
						break;
					}
				}
				
				if(!cdto.getChargeGroup().equals("BED") && !cdto.getChargeGroup().equals("ICU") 
				    && !cdto.getChargeGroup().equals("MED") && !cdto.getChargeGroup().equals("RET"))
						excludedChargeList.add(cdto);				
			}
			
			ChargeDTO discCharge = null;
			for (int i = 0; i < chargeList.size(); i++) {
				discCharge = (ChargeDTO) chargeList.get(i);
			
				if ( isDiscountNotApplicableChargeGrp(discCharge)){
					continue;//pharmacy / inventory / returns are avoided
				}
				Bill b = new BillDAO(con).getBill(discCharge.getBillNo());
				//say some details abt discount plan
				discCharge = cdao.getCharge(discCharge.getChargeId());
				discBO.setDiscountPlanDetails(b.getBillDiscountCategory());
				discBO.applyDiscountRule(con,discCharge);
				cdao.updateChargeAmounts(discCharge);
			}
			
			/*
			 * Tax calculation for bed charges is happening in recalculateBedCharges(where new charges for bed gets calculated based on days etc...) 
			 * hence excluded from this list since chargelist will contain old rates/amount for bed.
			 */
			// New method was added to fix performances for rate plan change/edit Insurance. since a common method was used in other screens, we have called this 
			// two screens, once regression is complete and this code is stable will move it to all other screens which references this.
			//TODO code clean after 11.13 testing is complete.
			if(!excludedChargeList.isEmpty() && null != billDetails){ // HMS-19823:Throwing Null pointer Exception when we click on save in edit patient Insurance screen if the bill is finalized.
			  BasicDynaBean bill = new BillDAO().findByKey("bill_no", billDetails.getBill().getBillNo());
			  billChargeTaxDAO.calculateAndUpdateBillChargeTaxes(con,excludedChargeList, bill);
			} 
			if(insuranceDetailsChanged) {
				for (BasicDynaBean bill: openBills) {
					String billNo = (String)bill.get("bill_no");
					boolean isTpaBill = (boolean)bill.get("is_tpa");
					if(isTpaBill && null != planIds){
						List<ChargeDTO> chgList = new ChargeDAO(con).getBillCharges(billNo);
						List<BasicDynaBean> chgClaimList = billChargeClaimDao.getBillChargeClaims(con, billNo);
						List<ChargeDTO> updateChgList = new ArrayList<ChargeDTO>();
						List<ChargeDTO> insertChgList = new ArrayList<ChargeDTO>();

						for(int j = 0;j<chgList.size();j++){
							ChargeDTO charge = chgList.get(j);
							if( !charge.getChargeGroup().equals("MED") && !charge.getChargeGroup().equals("RET")) {
								updateChgList.add(charge);

								boolean isNewCharge = true;
								for(BasicDynaBean bean : chgClaimList){
									String claimCharge = (String)bean.get("charge_id");
									if(charge.getChargeId().equals(claimCharge)) {
										isNewCharge = false;
										break;
									}
								}

								if(isNewCharge)
									insertChgList.add(charge);
							}
						}

						/*for(int k=0; k<updateChgList.size(); k++){
							ChargeDTO charge = updateChgList.get(k);
							charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
						}

						for(int l=0; l<insertChgList.size(); l++){
							ChargeDTO charge = insertChgList.get(l);
							charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
						}*/

						billChargeClaimDao.updateBillChargeClaims(con, updateChgList, visitId, billNo, planIds,false);
						billChargeClaimDao.insertBillChargeClaims(con, insertChgList, planIds, visitId, billNo);
					}
				}
			}

			
			/*boolean useDRG = VisitDetailsDAO.visitUsesDRG(visitId);
			if (useDRG) {
				Map drgCodeMap = new MRDUpdateScreenBO().getDRGCode(visitId);
				if (drgCodeMap != null && drgCodeMap.get("drg_bill_no") != null)
					success = BillBO.billProcessDRG(con, (String)drgCodeMap.get("drg_bill_no"));
			}*/

			//if (!editVisits)
				//successMsg.append("This patient has "+openBills.size()+" Open Bill(s)...<br/> Updated Charges for Bill(s) : </br>");

			/*if(openBills != null && openBills.size()>0) {
				for(int i = 0 ; i<openBills.size(); i++){
					successMsg.append(((Bill)openBills.get(i)).getBillNo());
					successMsg.append(", ");
				}
			}*/

			String sucsMsg = successMsg.toString();
			if (!editVisits)
				return sucsMsg;
			else
				return null;
		} catch(Exception e){
			success = false;
			throw(e);
		}finally {
			if (!editVisits)
				DataBaseUtil.commitClose(con, success);
		}
	}

	private void initBillDetailsNew() {
		isInsurance = patientDetails.get("primary_sponsor_id") != null && !((String)patientDetails.get("primary_sponsor_id")).isEmpty();
		planId = patientDetails.get("plan_id") == null ? 0 : (Integer) patientDetails.get("plan_id");
		visitType = (String)patientDetails.get("visit_type");
		visitId = (String)patientDetails.get("patient_id");
		
	}

	public String updateChargesBedAndRateWiseNew(String visitId, String newBedType)throws Exception{
		return updateChargesBedAndRateWiseNew(visitId, newBedType, null, null);
	}
	
	@SuppressWarnings("unchecked")
	public String updateChargesBedAndRateWise(String visitId, String newBedType, java.sql.Timestamp from, java.sql.Timestamp to)
			throws Exception {
		Boolean success = false;
		userName = RequestContext.getUserName();

		try{
			if (this.con == null) {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				if (visitId == null || visitId.equals("")) {
					logger.error("visitId was not found");
					con.close();
					return "";
				}

				patientDetailsMap = VisitDetailsDAO.getPatientVisitDetailsMap(visitId);
			}else {

				patientDetailsMap = VisitDetailsDAO.getPatientVisitDetailsMap(con, visitId);
			}

			planIds = patientInsurancePlanDAO.getPlanIds(con, visitId);

			initBillDetails();

			String orgId = (String) patientDetailsMap.get("org_id");

			if (orgId == null || orgId.equals("")) {
				logger.warn("Rate Plan not found...Setting to the Default Rate Plan ");
				orgId = "ORG0001";
			}

			String bedType = (String) patientDetailsMap.get("bill_bed_type");
			if (newBedType != null && !newBedType.equals("")) {
				bedType = newBedType;
			}
			if (bedType == null || bedType.equals("")) {
				logger.warn("Bed Type not found...Setting to the Default Bed Type");
				bedType = "GENERAL";
			}

			boolean insuranceDetailsChanged = true;
			List openBills = BillDAO.getAllActiveBills(visitId);
			List<ChargeDTO> chargeList = new ArrayList<ChargeDTO>();
			List<ChargeDTO> refChargeList = new ArrayList<ChargeDTO>();
			BillBO billBOObj = new BillBO();
			DiscountPlanBO discBO = new DiscountPlanBO();
			ChargeDAO cdao = new ChargeDAO(con);
			List<ChargeDTO> excludedChargeList = new ArrayList<ChargeDTO>();
			
			BillDetails billDetails = null;
			for (int i = 0; i < openBills.size(); i++) {
				//get all charges from open patient bills...
				String billNo = ((Bill) openBills.get(i)).getBillNo();
				billDetails = billBOObj.getBillDetails(con,billNo);
				if (editVisits) {
					BasicDynaBean visitBean = VisitDetailsDAO.getPatientVisitDetailsBean(con, visitId);
					String visitSponsor		= (String)visitBean.get("primary_sponsor_id");
					String visitRatePlan	= (String)visitBean.get("org_id");
					int visitPlan			= (Integer)visitBean.get("plan_id");

					BasicDynaBean existingVisit		= patientRegistrationDao.findByKey("patient_id", visitId);
					String existingVisitSponsor		= (String)existingVisit.get("primary_sponsor_id");
					String existingVisitRatePlan	= (String)existingVisit.get("org_id");
					int existingVisitPlan			= (Integer)existingVisit.get("plan_id");

					Map<String,Object> priPlanKeys = new HashMap<String, Object>();
					priPlanKeys.put("patient_id", visitId);
					priPlanKeys.put("priority", 1);
					BasicDynaBean priPlanBean = patientInsurancePlanDAO.findByKey(con,priPlanKeys);
					BasicDynaBean existingPriPlanBean = patientInsurancePlanDAO.findByKey(priPlanKeys);

					Map<String, Object> secPlanKeys = new HashMap<String, Object>();
					secPlanKeys.put("patient_id", visitId);
					secPlanKeys.put("priority", 2);
					BasicDynaBean secPlanBean = patientInsurancePlanDAO.findByKey(con,secPlanKeys);
					BasicDynaBean existingsecPlanBean = patientInsurancePlanDAO.findByKey(secPlanKeys);

					String exisitnPriSponsor = null, primarySponsorId = null, existingSecSponsor = null, secondarySponsorId = null;
					int exisitngPriPlan = 0, existingSecPlan = 0, planId = 0, secPlanId = 0;

					if(null != priPlanBean && null != priPlanBean.get("sponsor_id"))
						primarySponsorId = (String)priPlanBean.get("sponsor_id");
					if(null != existingPriPlanBean && null != existingPriPlanBean.get("sponsor_id"))
						exisitnPriSponsor = (String)existingPriPlanBean.get("sponsor_id");
					if(null != secPlanBean && null != secPlanBean.get("sponsor_id"))
						secondarySponsorId = (String)secPlanBean.get("sponsor_id");
					if(null != existingsecPlanBean && null != existingsecPlanBean.get("sponsor_id"))
						existingSecSponsor = (String)existingsecPlanBean.get("sponsor_id");


					if(null != priPlanBean && null != priPlanBean.get("plan_id"))
						planId = (Integer)priPlanBean.get("plan_id");
					if(null != existingPriPlanBean && null != existingPriPlanBean.get("plan_id"))
						exisitngPriPlan = (Integer)existingPriPlanBean.get("plan_id");
					if(null != secPlanBean && null != secPlanBean.get("plan_id"))
						secPlanId = (Integer)secPlanBean.get("plan_id");
					if(null != existingsecPlanBean && null != existingsecPlanBean.get("plan_id"))
						existingSecPlan = (Integer)existingsecPlanBean.get("plan_id");

					// If tpa is removed and/or no rate plan change (or) no insurance details change (including rate plan)
					// then continue i.e rate changes not required.
					if (( (exisitnPriSponsor != null && primarySponsorId != null && exisitnPriSponsor.equals(primarySponsorId))
							&& ( (existingSecSponsor == null && secondarySponsorId == null )
								|| (existingSecSponsor != null && secondarySponsorId != null && existingSecSponsor.equals(secondarySponsorId)) ))
						&& ( ( exisitngPriPlan == planId ) && (existingSecPlan == secPlanId) )
						&& visitRatePlan.equals(existingVisitRatePlan)){
						insuranceDetailsChanged = false;
						continue;
					}
				}
				chargeList.addAll(billDetails.getCharges());
				
			}
			for (int i = 0; i < chargeList.size(); i++) {
				ChargeDTO cdto = (ChargeDTO) chargeList.get(i);
				if (cdto != null && ("X".equals(cdto.getStatus()) || (cdto.getChargeRef() != null && !cdto.getChargeRef().equals("") 
						&& !"PKG".equals(cdto.getChargeGroup())))) {
					continue; // ignore cancelled && referral charges
				}
				if(from != null && to != null){ // apply date filters
					if(cdto.getPostedDate().before(from) || cdto.getPostedDate().after(to)){
						continue; // ignore charges which dont fall within specified dates.
					}
				}

				Bill b = new BillDAO(con).getBill(cdto.getBillNo());
				Integer centerId = (Integer)patientDetailsMap.get("center_id");
				String prefRatePlan = CenterPreferencesDAO.getRatePlanForNonInsuredBills(centerId);
				String  mvPackageId = billBOObj.getMultiVisitBillPackageId(b.getBillNo()).toString();
				boolean isMultiVisitPkgBill = Integer.parseInt(mvPackageId) > 0;

				if(patientDetailsMap.get("primary_sponsor_id") != null && !patientDetailsMap.get("primary_sponsor_id").equals("")
						&& !b.getIs_tpa() && prefRatePlan != null){
					orgId = prefRatePlan;
					//commenting as it id Fix for 56261 resetDiscountCategory(con, b.getBillNo());
				} else {
					orgId =  (String) patientDetailsMap.get("org_id");
				}

				refChargeList = getRefCharges(chargeList, cdto);
				success = false;
				if(isMultiVisitPkgBill){
					continue; // ignoring the multi-visit package charges update
					/*String mvpChargeGroup = cdto.getChargeGroup();
					if(mvpChargeGroup.equals("DOC") || mvpChargeGroup.equals("DIA") || 
							(mvpChargeGroup.equals("SNP") && cdto.getChargeHead().equals("SERSNP")) || mvpChargeGroup.equals("OTC"))
					success = updateMultiVisitPackageItemCharegs(cdto,orgId,bedType,refChargeList,mvPackageId);*/
				} else {
					switch (ChargeGroup.valueOf(cdto.getChargeGroup())) {
					case REG:
						success = updateRegistrationCharge(cdto, orgId, bedType, refChargeList);
						break;

					case DOC:
						success = updateDoctorCharge(cdto, orgId, bedType, refChargeList);
						break;

					case DIA:
						success = updateDiagCharge(cdto, orgId, bedType, refChargeList);
						break;

					case SNP:
						success = updateServiceCharge(cdto, orgId, bedType, refChargeList);
						break;

					case DIE:
						success = updateDietaryCharge(cdto, orgId, bedType, refChargeList);
						break;

					case OTC:
						if (cdto.getChargeHead().startsWith("EQ")) {
							success = updateEquipmentCharge(cdto, orgId, bedType, refChargeList);
						} else if (cdto.getChargeHead().startsWith("MIS")) {
							success = true;
						} else {
							success = updateOtherCharge(cdto, orgId, bedType, refChargeList);
						}
						break;

					case PKG:
						if ("PKGPKG".equals(cdto.getChargeHead()) || cdto.getPackageId() != null ) {
							success = updatePackageCharge(cdto, orgId, bedType, refChargeList);
						}
						break;

					case BED:
					case ICU:
						success = updateBedCharge(cdto, orgId, bedType, refChargeList);
						break;

					case OPE:
						if (cdto.getChargeHead().startsWith("EQ")) {
							success = updateEquipmentCharge(cdto, orgId, bedType, refChargeList);
						} else {
							success = updateSurgeryCharge(cdto, orgId, bedType, refChargeList);
						}
						break;

					case MED:
					case ITE:
					case RET:
					case TAX:
					case DIS:
					default:
						success = true;
						/*
						 * Ignore medicine, inventory, tax, DRG, PDM charges and other misc charges as
						 * they are rate plan independent charges.
						 */
						break;
					}
				}
				
				if(!cdto.getChargeGroup().equals("BED") && !cdto.getChargeGroup().equals("ICU") 
				    && !cdto.getChargeGroup().equals("MED") && !cdto.getChargeGroup().equals("RET"))
						excludedChargeList.add(cdto);
				
			}
			
			ChargeDTO discCharge = null;
			for (int i = 0; i < chargeList.size(); i++) {
				discCharge = (ChargeDTO) chargeList.get(i);
			
				if ( isDiscountNotApplicableChargeGrp(discCharge)){
					continue;//pharmacy / inventory / returns are avoided
				}
				Bill b = new BillDAO(con).getBill(discCharge.getBillNo());
				//say some details abt discount plan
				discCharge = cdao.getCharge(discCharge.getChargeId());
				discBO.setDiscountPlanDetails(b.getBillDiscountCategory());
				discBO.applyDiscountRule(con,discCharge);
				cdao.updateChargeAmounts(discCharge);
			}
			
			/*
			 * Tax calculation for bed charges is happening in recalculateBedCharges(where new charges for bed gets calculated based on days etc...) 
			 * hence excluded from this list since chargelist will contain old rates/amount for bed.
			 */
			if(!excludedChargeList.isEmpty()){
			  BasicDynaBean bill = new BillDAO().findByKey("bill_no", billDetails.getBill().getBillNo());
				billChargeTaxDAO.calculateAndUpdateBillChargeTaxes(con,excludedChargeList,bill);
			}
			 
			if(insuranceDetailsChanged) {
				for (int i = 0; i < openBills.size(); i++) {
					String billNo = ((Bill) openBills.get(i)).getBillNo();
					boolean isTpaBill = ((Bill) openBills.get(i)).getIs_tpa();
					if(isTpaBill && null != planIds){
						List<ChargeDTO> chgList = new ChargeDAO(con).getBillCharges(billNo);
						List<BasicDynaBean> chgClaimList = billChargeClaimDao.getBillChargeClaims(con, billNo);
						List<ChargeDTO> updateChgList = new ArrayList<ChargeDTO>();
						List<ChargeDTO> insertChgList = new ArrayList<ChargeDTO>();

						for(int j = 0;j<chgList.size();j++){
							ChargeDTO charge = chgList.get(j);
							if( !charge.getChargeGroup().equals("MED") && !charge.getChargeGroup().equals("RET")) {
								updateChgList.add(charge);

								boolean isNewCharge = true;
								for(BasicDynaBean bean : chgClaimList){
									String claimCharge = (String)bean.get("charge_id");
									if(charge.getChargeId().equals(claimCharge)) {
										isNewCharge = false;
										break;
									}
								}

								if(isNewCharge)
									insertChgList.add(charge);
							}
						}

						for(int k=0; k<updateChgList.size(); k++){
							ChargeDTO charge = updateChgList.get(k);
							charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
						}

						for(int l=0; l<insertChgList.size(); l++){
							ChargeDTO charge = insertChgList.get(l);
							charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
						}

						billChargeClaimDao.updateBillChargeClaims(con, updateChgList, visitId, billNo, planIds,false);
						billChargeClaimDao.insertBillChargeClaims(con, insertChgList, planIds, visitId, billNo);
					}
				}
			}

			
			/*boolean useDRG = VisitDetailsDAO.visitUsesDRG(visitId);
			if (useDRG) {
				Map drgCodeMap = new MRDUpdateScreenBO().getDRGCode(visitId);
				if (drgCodeMap != null && drgCodeMap.get("drg_bill_no") != null)
					success = BillBO.billProcessDRG(con, (String)drgCodeMap.get("drg_bill_no"));
			}*/

			if (!editVisits)
				successMsg.append("This patient has "+openBills.size()+" Open Bill(s)...<br/> Updated Charges for Bill(s) : </br>");

			if(openBills != null && openBills.size()>0) {
				for(int i = 0 ; i<openBills.size(); i++){
					successMsg.append(((Bill)openBills.get(i)).getBillNo());
					successMsg.append(", ");
				}
			}

			String sucsMsg = successMsg.toString();
			if (!editVisits)
				return sucsMsg;
			else
				return null;
		} catch(Exception e){
			success = false;
			throw(e);
		}finally {
			if (!editVisits)
				DataBaseUtil.commitClose(con, success);
		}
	}

	public boolean resetDiscountCategory(Connection con, String billNo) throws SQLException, IOException{
		boolean success = false;
		GenericDAO billDao = new GenericDAO("bill");
		BasicDynaBean billBean = billDao.findByKey(con, "bill_no", billNo);
		billBean.set("discount_category_id", 0);
		success = billDao.update(con, billBean.getMap(), "bill_no", billNo) >= 0;
		return success;
	}

	public Boolean updateMultiVisitPackageItemCharegs(ChargeDTO cdto,String orgId,String bedType,List<ChargeDTO> refChargeList,String packageId) throws Exception{
		Boolean success = false;
		String itemType = null;
		String itemId = null;
		String packObId = null;
		List<ChargeDTO> newChgLst = new ArrayList<ChargeDTO>();
		if(cdto.getChargeGroup().equals("DOC")) {
			itemType = "doctor";
			itemId = new Integer(cdto.getConsultation_type_id()).toString();
		} else if (cdto.getChargeGroup().equals("DIA")) {
			itemType = "test";
			itemId = cdto.getActDescriptionId();
		} else if (cdto.getChargeGroup().equals("SNP") && cdto.getChargeHead().equals("SERSNP")) {
			itemType = "service";
			itemId = cdto.getActDescriptionId();
		} else if (cdto.getChargeGroup().equals("OTC")) {
			itemType = "other";
			itemId = cdto.getActDescriptionId();
		}
		packObId = new PackageDAO(null).getMultiVisitPackageObId(itemId,packageId,itemType);
		if(packObId != null && !packObId.isEmpty()) {
			newChgLst = OrderBO.getMultiVisitPackItemCharges(packageId, packObId, bedType, orgId, null, cdto.getActQuantity(),
					isInsurance, visitType, visitId, cdto.getFirstOfCategory());

			success = updateNewChargeValue(newChgLst, cdto, refChargeList).equals("success");
			success = true;
		}

		return success;
	}
	
	public Boolean updateMultiVisitPackageItemCharegsNew(ChargeDTO cdto,String orgId,String bedType,List<ChargeDTO> refChargeList,String packageId) throws Exception{
		Boolean success = false;
		String itemType = null;
		String itemId = null;
		String packObId = null;
		List<ChargeDTO> newChgLst = new ArrayList<ChargeDTO>();
		if(cdto.getChargeGroup().equals("DOC")) {
			itemType = "doctor";
			itemId = new Integer(cdto.getConsultation_type_id()).toString();
		} else if (cdto.getChargeGroup().equals("DIA")) {
			itemType = "test";
			itemId = cdto.getActDescriptionId();
		} else if (cdto.getChargeGroup().equals("SNP") && cdto.getChargeHead().equals("SERSNP")) {
			itemType = "service";
			itemId = cdto.getActDescriptionId();
		} else if (cdto.getChargeGroup().equals("OTC")) {
			itemType = "other";
			itemId = cdto.getActDescriptionId();
		}
		packObId = new PackageDAO(null).getMultiVisitPackageObId(itemId,packageId,itemType);
		if(packObId != null && !packObId.isEmpty()) {
			newChgLst = OrderBO.getMultiVisitPackItemCharges(packageId, packObId, bedType, orgId, null, cdto.getActQuantity(),
					isInsurance, visitType, visitId, cdto.getFirstOfCategory());

			success = updateNewChargeValueNew(newChgLst, cdto, refChargeList).equals("success");
			success = true;
		}

		return success;
	}
	public List<ChargeDTO> getRefCharges(List<ChargeDTO> chargeList, ChargeDTO cdto) {
		List<ChargeDTO> refList = new ArrayList<ChargeDTO>();
		String mainChargeId = cdto.getChargeId();
		for (int i = 0; i < chargeList.size(); i++) {
			ChargeDTO charge = ((ChargeDTO) chargeList.get(i));
			if (charge != null && charge.getChargeRef() != null
					&& charge.getChargeRef().equals(mainChargeId) && !charge.getChargeGroup().equals("PKG")) {
				refList.add(charge);
			}
		}
		return refList;
	}

	public String updateNewChargeValue(List<ChargeDTO> newChgLst, ChargeDTO cdto,
			List<ChargeDTO> refChgList) throws SQLException,Exception {
		String result = "success";
		List<ChargeDTO> origCharges = new ArrayList<ChargeDTO>();
		origCharges.add(cdto);
		origCharges.addAll(refChgList);
		OrderBO orderbo = new OrderBO();
		orderbo.setUserName(userName);
		orderbo.setPatientInfo((String)patientDetailsMap.get("patient_id"), (String)patientDetailsMap.get("mr_no"), userName);
		orderbo.setPlanIds(planIds);
		orderbo.setBillInfo(con, (String)patientDetailsMap.get("patient_id"), cdto.getBillNo(), false, userName);
		boolean keepOriginalDiscounts = needOldDiscounts(con,cdto.getBillNo());
		orderbo.updateChargeAmounts(con, origCharges, newChgLst, null, null,keepOriginalDiscounts );
		return result;
	}
	
	public String updateNewChargeValueNew(List<ChargeDTO> newChgLst, ChargeDTO cdto,
			List<ChargeDTO> refChgList) throws SQLException,Exception {
		String result = "success";
		List<ChargeDTO> origCharges = new ArrayList<ChargeDTO>();
		origCharges.add(cdto);
		origCharges.addAll(refChgList);
		OrderBO orderbo = new OrderBO();
		orderbo.setUserName(userName);
		orderbo.setPatientInfo((String)patientDetails.get("patient_id"), (String)patientDetailsMap.get("mr_no"), userName);
		orderbo.setPlanIds(planIds);
		orderbo.setBillInfo(con, (String)patientDetails.get("patient_id"), cdto.getBillNo(), false, userName);
		boolean keepOriginalDiscounts = needOldDiscountsNew(cdto.getBillNo());
		orderbo.updateChargeAmounts(con, origCharges, newChgLst, null, null,keepOriginalDiscounts );
		return result;
	}

	/*
	 * This updates the registration charge based on the organization details
	 */
	public boolean updateRegistrationCharge(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException,Exception {
		String result = null;
		Boolean isRegCharge = checkIfValidRegistrationCharge(cdto.getActDescriptionId());
		if(!isRegCharge)
			return true;
		Boolean isRenewal = patientDetailsMap.get("reg_charge_accepted") != null
				&& patientDetailsMap.get("revisit").equals('Y');
		List<ChargeDTO> newChgLst = OrderBO.getRegistrationCharges(bedType, orgId, cdto.getChargeHead(),
				isRenewal, isInsurance, planIds, false, visitType, visitId, con, cdto.getFirstOfCategory());
		result = updateNewChargeValue(newChgLst, cdto, refChargeList);
		return result != null && result.equals("success");
	}
	
	/*
	 * This updates the registration charge based on the organization details
	 */
	public boolean updateRegistrationChargeNew(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException,Exception {
		String result = null;
		Boolean isRegCharge = checkIfValidRegistrationCharge(cdto.getActDescriptionId());
		if(!isRegCharge)
			return true;
		Boolean isRenewal = patientDetails.get("reg_charge_accepted") != null
				&& patientDetails.get("revisit").equals('Y');
		List<ChargeDTO> newChgLst = OrderBO.getRegistrationCharges(bedType, orgId, cdto.getChargeHead(),
				isRenewal, isInsurance, planIds, false, visitType, visitId, con, cdto.getFirstOfCategory());
		result = updateNewChargeValueNew(newChgLst, cdto, refChargeList);
		return result != null && result.equals("success");
	}
	
	public boolean updateDoctorChargeNew(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException,Exception {
		String result = null;
		Boolean isDoctorCharge = checkIfValidDoctorCharge(cdto.getActDescriptionId());
		if(!isDoctorCharge)
			return true;
		List<ChargeDTO> newChgLst = getDoctorCharges(cdto, orgId, bedType);
		/*for(ChargeDTO charge : newChgLst){
			charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
		}*/

		result = updateNewChargeValueNew(newChgLst, cdto, refChargeList);
		return result != null && result.equals("success");
	}

  public List<ChargeDTO> getDoctorCharges(ChargeDTO cdto, String orgId, String bedType)
      throws SQLException {
    BasicDynaBean consTypeBean = null;
		List<ChargeDTO> newChgLst = null;
		// Doctor added from order
		if (cdto.getHasActivity()) {
			DynaBean dcBean = null;
			dcBean = doctorConsultationDao.getDoctorConsultationCharge(cdto.getChargeId());
			if (dcBean == null) {
			  return newChgLst;
			}
			Integer operationRef = (Integer) dcBean.get("operation_ref");
			BasicDynaBean opMasterBean = null;
			if (null != dcBean.get("operation_ref")) {
			  BasicDynaBean opBean;
			  String operId = null;
			  if (dcBean.get("oper_priority") == null ||
		          dcBean.get("oper_priority").equals("P")) {
			    opBean = bedOperationScheduleDao.findByKey("prescribed_id", operationRef);
			    operId = (opBean != null) ? (String) opBean.get("operation_name") : null;
			  } 
			  if (dcBean.get("oper_priority") != null 
			      && (dcBean.get("oper_priority").equals("S") || operId == null)) {
			    opBean = bedOperationSecondaryScheduleDao.findByKey("sec_prescribed_id", operationRef);
			    operId = (opBean != null) ? (String) opBean.get("operation_id") : null;
			  }
			  if (operId != null) {
			    opMasterBean = operationMasterDao.getOperationChargeBean(operId, bedType,
			        (String) orgId);			    
			  }
			}

			String otDocRole = (String) dcBean.get("ot_doc_role");
			String consType = (String) dcBean.get("head");

			if (opMasterBean == null) {
				// get the consultation type bean
				consTypeBean = OrderBO.getConsultationTypeBean(Integer.parseInt(consType));
			}

			BasicDynaBean doctor = DoctorMasterDAO.getDoctorCharges((String) dcBean.get("doctor_name"),
					orgId, (String) bedType);

			if (opMasterBean != null) {
				// This is an operation related doctor order...
				newChgLst = OrderBO.getOtDoctorCharges(doctor, otDocRole, (String) patientDetails.get("visit_type"),
						opMasterBean, cdto.getActQuantity(), isInsurance, planIds, bedType, visitId, cdto.getFirstOfCategory());
			} else {
				newChgLst = OrderBO.getDoctorConsCharges(doctor, consTypeBean, visitType,
						OrgMasterDao.getOrgdetailsDynaBean(orgId), cdto.getActQuantity(), isInsurance, planIds,
						bedType, cdto.getVisitId(), cdto.getFirstOfCategory());

				BasicDynaBean consOrgBean = consultationChargesDao.getConsultOrgDetailsBean(consType, orgId);
				if (consOrgBean != null && !(Boolean)consOrgBean.get("applicable"))
					ratePlanNotApplicableList.add(cdto.getBillNo()+" ("+cdto.getActDescription()+" Dated:"+cdto.getPostedDate()+")");
			}

		} else {

			consTypeBean = OrderBO.getConsultationTypeBean(cdto.getConsultation_type_id());
			if (consTypeBean == null) { return null; }
			BasicDynaBean doctor = DoctorMasterDAO.getDoctorCharges((String) cdto.getActDescriptionId(),
					orgId, (String) bedType);
			newChgLst = OrderBO.getDoctorConsCharges(doctor, consTypeBean, visitType, OrgMasterDao.getOrgdetailsDynaBean(orgId),
					cdto.getActQuantity(), isInsurance, planIds, bedType, visitId, cdto.getFirstOfCategory());

			BasicDynaBean consOrgBean = consultationChargesDao.getConsultOrgDetailsBean(cdto.getConsultation_type_id()+"", orgId);
			if (consOrgBean != null && !(Boolean)consOrgBean.get("applicable"))
				ratePlanNotApplicableList.add(cdto.getBillNo()+" ("+cdto.getActDescription()+" Dated:"+cdto.getPostedDate()+")");
		}
    return newChgLst;
  }


	/*
	 * This function updates the doctor consultation charge based on the
	 * organization details, considering both doctor wise and consultation type
	 * wise charges
	 */
	public boolean updateDoctorCharge(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException,Exception {
		String result = null;
		Boolean isDoctorCharge = checkIfValidDoctorCharge(cdto.getActDescriptionId());
		if(!isDoctorCharge)
			return true;
		BasicDynaBean consTypeBean = null;
		List<ChargeDTO> newChgLst = null;
		// Doctor added from order
		if (cdto.getHasActivity()) {
			DynaBean dcBean = null;
			dcBean = doctorConsultationDao.getDoctorConsultationCharge(con, cdto.getChargeId());

			Integer operationRef = (Integer) dcBean.get("operation_ref");
			BasicDynaBean opMasterBean = null;
			if (null != dcBean.get("operation_ref")) {
			  BasicDynaBean opBean;
			  String operId = null;
			  if (dcBean.get("oper_priority") == null ||
		          dcBean.get("oper_priority").equals("P")) {
			    opBean = bedOperationScheduleDao.findByKey("prescribed_id", operationRef);
			    operId = (opBean != null) ? (String) opBean.get("operation_name") : null;
			  }
			  if (dcBean.get("oper_priority") != null
			      && (dcBean.get("oper_priority").equals("S") || operId == null)) {
			    opBean = bedOperationSecondaryScheduleDao.findByKey("sec_prescribed_id", operationRef);
			    operId = (opBean != null) ? (String) opBean.get("operation_id") : null;
			  }
			  if (operId != null) {
			    opMasterBean = operationMasterDao.getOperationChargeBean(operId, bedType,
			        (String) orgId);
			  }
			}
			String otDocRole = (String) dcBean.get("ot_doc_role");
			String consType = (String) dcBean.get("head");

			if (opMasterBean == null) {
				// get the consultation type bean
				consTypeBean = OrderBO.getConsultationTypeBean(Integer.parseInt(consType));
			}

			BasicDynaBean doctor = DoctorMasterDAO.getDoctorCharges((String) dcBean.get("doctor_name"),
					orgId, (String) bedType);

			if (opMasterBean != null) {
				// This is an operation related doctor order...
				newChgLst = OrderBO.getOtDoctorCharges(doctor, otDocRole, (String) patientDetailsMap.get("visit_type"),
						opMasterBean, cdto.getActQuantity(), isInsurance, planIds, bedType, visitId, cdto.getFirstOfCategory());
			} else {
				newChgLst = OrderBO.getDoctorConsCharges(doctor, consTypeBean, visitType,
						OrgMasterDao.getOrgdetailsDynaBean(orgId), cdto.getActQuantity(), isInsurance, planIds,
						bedType, cdto.getVisitId(), cdto.getFirstOfCategory());

				BasicDynaBean consOrgBean = consultationChargesDao.getConsultOrgDetailsBean(consType, orgId);
				if (consOrgBean != null && !(Boolean)consOrgBean.get("applicable"))
					ratePlanNotApplicableList.add(cdto.getBillNo()+" ("+cdto.getActDescription()+" Dated:"+cdto.getPostedDate()+")");
			}

		} else {

			consTypeBean = OrderBO.getConsultationTypeBean(cdto.getConsultation_type_id());
			BasicDynaBean doctor = DoctorMasterDAO.getDoctorCharges((String) cdto.getActDescriptionId(),
					orgId, (String) bedType);
			newChgLst = OrderBO.getDoctorConsCharges(doctor, consTypeBean, visitType, OrgMasterDao.getOrgdetailsDynaBean(orgId),
					cdto.getActQuantity(), isInsurance, planIds, bedType, visitId, cdto.getFirstOfCategory());

			BasicDynaBean consOrgBean = consultationChargesDao.getConsultOrgDetailsBean(cdto.getConsultation_type_id()+"", orgId);
			if (consOrgBean != null && !(Boolean)consOrgBean.get("applicable"))
				ratePlanNotApplicableList.add(cdto.getBillNo()+" ("+cdto.getActDescription()+" Dated:"+cdto.getPostedDate()+")");
		}
		for(ChargeDTO charge : newChgLst){
			charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
		}

		result = updateNewChargeValue(newChgLst, cdto, refChargeList);
		return result != null && result.equals("success");
	}

	/*
	 * This function updates the bed charges based on the organization details
	 */
	public boolean updateBedCharge(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException, ParseException, Exception {

		boolean isBedCharge = checkIfValidBedCharge(cdto.getActDescriptionId());

		if(!isBedCharge)
			return true;

		String result = null;
		boolean hasAdmission = false;
		String dayCare = "N";
		List<ChargeDTO> newChgLst = null;

		if (admissionDao.findByKey("patient_id", visitId) != null)
			hasAdmission = true;

		if (cdto.getHasActivity()) {
			BasicDynaBean admBean = admissionDao.getBean();

			if (hasAdmission) {
				admBean = admissionDao.findByKey("patient_id", visitId);
				dayCare = (String) admBean.get("daycare_status");
			}

			OrderBO orderbo = new OrderBO();
			orderbo.setUserName(userName);
			orderbo.setBillInfo(con, visitId, cdto.getBillNo(), new BillBO().getBillDetails(cdto.getBillNo()).getBill().getIs_tpa(),
					userName);
			result= orderbo.recalculateBedCharges(con, visitId)== null? "success": null;
		} else {
			newChgLst = getBedChargeNew(cdto, orgId, bedType, hasAdmission, dayCare);
			result = updateNewChargeValue(newChgLst, cdto, refChargeList);

		}
		return result != null && result.equals("success");
	}
	
	/*
	 * This function updates the bed charges based on the organization details
	 */
	public boolean updateBedChargeNew(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException, ParseException, Exception {

		boolean isBedCharge = checkIfValidBedCharge(cdto.getActDescriptionId());

		if(!isBedCharge)
			return true;

		String result = null;
		boolean hasAdmission = false;
		String dayCare = "N";
		List<ChargeDTO> newChgLst = null;

		if (admissionDao.findByKey("patient_id", visitId) != null)
			hasAdmission = true;

		if (cdto.getHasActivity()) {
			BasicDynaBean admBean = admissionDao.getBean();

			if (hasAdmission) {
				admBean = admissionDao.findByKey("patient_id", visitId);
				dayCare = (String) admBean.get("daycare_status");
			}

			OrderBO orderbo = new OrderBO();
			orderbo.setUserName(userName);
			orderbo.setBillInfo(con, visitId, cdto.getBillNo(), new BillBO().getBillDetails(cdto.getBillNo()).getBill().getIs_tpa(),
					userName);
			result= orderbo.recalculateBedCharges(con, visitId)== null? "success": null;
		} else {
			newChgLst = getBedChargeNew(cdto, orgId, bedType, hasAdmission, dayCare);
			result = updateNewChargeValueNew(newChgLst, cdto, refChargeList);

		}
		return result != null && result.equals("success");
	}

  public List<ChargeDTO> getBedChargeNew(ChargeDTO cdto, String orgId, String bedType,
      boolean hasAdmission, String dayCare) throws SQLException, ParseException {
    List<ChargeDTO> newChgLst;
    String baseBedType = bedType;
    Timestamp fromDate = cdto.getFrom_date();
    Timestamp toDate = cdto.getTo_date();
    BasicDynaBean bedRates = null;
    if (cdto.getChargeGroup().equals("ICU")) {
    	bedRates = new BedMasterDAO().getIcuBedChargesBean(cdto.getActDescriptionId(), baseBedType, orgId);
    } else {
    	bedRates = new BedMasterDAO().getNormalBedChargesBean(cdto.getActDescriptionId(), orgId);
    }
    OrderBO orderbo = new OrderBO();
    orderbo.setUserName(userName);
    newChgLst = new OrderBO().getBedCharges(cdto.getChargeGroup(), bedRates, fromDate, toDate, cdto.getActQuantity(),
    		false, dayCare, false, !hasAdmission, "O", null);
    return newChgLst;
  }

	/*
	 * This function updates the surgery charges based on the new rate plan
	 * details.
	 */
	public boolean updateSurgeryCharge(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException,Exception {

		String result = null;
		List<ChargeDTO> newChgLst = null;
		//Update the anaesthetist charge
		if (cdto.getChargeRef() == null && cdto.getHasActivity()
				&& doctorsDao.findByKey("doctor_id", cdto.getActDescriptionId()) != null) {
			return updateDoctorCharge(cdto, orgId, bedType, refChargeList);
		}
		if (cdto.getHasActivity()) {
			BillActivityChargeDAO bacdao = new BillActivityChargeDAO(con);
			BillActivityCharge bac = bacdao.getActivity(cdto.getChargeId());
			//Continue only if the charge is a valid bed charge else stop proccessing.
			if(bac.getActivityCode().equals("OTC"))
				return true;

			BasicDynaBean bedOpBean = null;
			BasicDynaBean secSurgeonTeam = null;
			BasicDynaBean procedureDetails = OperationDetailsDAO.getOperationDetailsByPrescribedId(Integer.parseInt(bac
					.getActivityId()),cdto.getOp_id());
			BasicDynaBean primaryDetails = null;
			boolean isSecondary = false;
			String opid = null;
			
			if ( procedureDetails == null
					|| procedureDetails.get("oper_priority") == null
					|| procedureDetails.get("oper_priority").equals("P") ) {
  			 bedOpBean  = bedOperationScheduleDao.findByKey("prescribed_id", Integer.parseInt(bac
                .getActivityId()));
  			 opid = (bedOpBean != null) ? (String) bedOpBean.get("operation_name") : null;
  			 primaryDetails = bedOpBean;
			} else {
			  bedOpBean  = bedOperationSecondaryScheduleDao.findByKey("sec_prescribed_id", Integer.parseInt(bac
                .getActivityId()));
			  if (bedOpBean != null) {
			    primaryDetails = bedOperationScheduleDao.findByKey("prescribed_id",(int) bedOpBean.get("prescribed_id"));
			    isSecondary = true;
			    secSurgeonTeam = OperationDetailsDAO.getOperationTeam(con,
					  (int)procedureDetails.get("operation_proc_id"));
			    opid = (String) bedOpBean.get("operation_id");
			  }
			}
            List<BasicDynaBean> anaesthesiaTypeDetails= new ArrayList<BasicDynaBean>();
            Map<String, String> filterMap = new HashMap<String, String>();
            filterMap.put("charge_head", "ANATOPE");
            filterMap.put("charge_ref", cdto.getChargeId());
            List<BasicDynaBean> anaeasList = billChargeDao.listAll(con, null, filterMap, "charge_id");
            if (anaeasList != null) {
              for (BasicDynaBean anaeas : anaeasList) {
                if (anaeas.get("surgery_anesthesia_details_id") != null) {
                  BasicDynaBean bean = surgeryAnesthesiaDetails.findByKey("surgery_anesthesia_details_id", 
                      anaeas.get("surgery_anesthesia_details_id"));
                      anaesthesiaTypeDetails.add(bean);
                }
              }
            } else {
               anaesthesiaTypeDetails= surgeryAnesthesiaDetails.findAllByKey("prescribed_id", 
                 isSecondary ? primaryDetails.get("prescribed_id") : Integer.parseInt(bac.getActivityId()));
            }
			BasicDynaBean operation =  operationMasterDao.getOperationChargeBean(opid, bedType, orgId);

			if (operation != null && !(Boolean)operation.get("applicable"))
				ratePlanNotApplicableList.add(cdto.getBillNo()+" ("+cdto.getActDescription()+" Dated:"+cdto.getPostedDate()+")");

			String ot = isSecondary ? (String) primaryDetails.get("theatre_name") : (String) bedOpBean.get("theatre_name");
			String surgeon = null;
			if(isSecondary && secSurgeonTeam != null) {
		        surgeon = (String)secSurgeonTeam.get("team_doc_id");
		    }else if (!isSecondary) {
		        surgeon =  (String) bedOpBean.get("surgeon");
		    }
			String units = isSecondary ? (String) primaryDetails.get("hrly") :  (String) bedOpBean.get("hrly");
			units = (units == null || units.equals("")) ? "" : units;
			units = units.equals("checked") ? "H" : "D";
			Timestamp from = isSecondary ? (Timestamp) primaryDetails.get("start_datetime") :  (Timestamp) bedOpBean.get("start_datetime");
			Timestamp to =  isSecondary ? from : (Timestamp) bedOpBean.get("end_datetime");
			String finalizationStatus =   isSecondary ? (String) primaryDetails.get("finalization_status") :  (String) bedOpBean.get("finalization_status");

			BasicDynaBean theatre = theatreMasterDao.getTheatreChargeDetails(ot, bedType, orgId);
			BasicDynaBean surgeonBean = null;
			if(!StringUtils.isEmpty(surgeon)) {
		        surgeonBean = DoctorMasterDAO.getOTDoctorChargesBean(surgeon, bedType, orgId);
		    }
			BasicDynaBean anasthesiaTypeChargeBean = null;

			newChgLst = OrderBO.getOperationCharges(cdto.getOp_id(), operation, theatre, surgeonBean, null,
					from, to, units, isInsurance, planIds, finalizationStatus, visitType,
					anasthesiaTypeChargeBean, visitId, cdto.getFirstOfCategory());

			if(anaesthesiaTypeDetails != null && anaesthesiaTypeDetails.size() > 0) {

				for (int i=0;i<anaesthesiaTypeDetails.size();i++) {
					String anTypeId = (String)anaesthesiaTypeDetails.get(i).get("anesthesia_type");
					Timestamp anFrom = (Timestamp)anaesthesiaTypeDetails.get(i).get("anaes_start_datetime");
					Timestamp anTo = (Timestamp)anaesthesiaTypeDetails.get(i).get("anaes_end_datetime");
					anasthesiaTypeChargeBean = anaesthesiaTypeChargesDao.getAnasthesiaTypeCharge(
							anTypeId, bedType, orgId);

					if (anasthesiaTypeChargeBean != null && !(Boolean)anasthesiaTypeChargeBean.get("applicable"))
						ratePlanNotApplicableList.add(cdto.getBillNo()+" ("+cdto.getActDescription()+" Dated:"+cdto.getPostedDate()+")");

					newChgLst.addAll(OrderBO.getAnaesthesiaTypeCharges(cdto.getOp_id(), operation, anFrom, anTo, units,
							isInsurance, planIds, finalizationStatus, visitType, anasthesiaTypeChargeBean, visitId, cdto.getFirstOfCategory()));

				/*	OrderBO.getOperationCharges(cdto.getOp_id(), operation, theatre, surgeonBean, null,
							anFrom, anTo, units, isInsurance, planIds, finalizationStatus, visitType,
					anasthesiaTypeChargeBean, visitId, cdto.getFirstOfCategory())*/
				}
			}
		} else {
			// No reference to the operation implies that its an other charge and not a surgical charge.
			if(cdto.getOp_id() == null || cdto.getOp_id().trim().equals("")){
				return true;
			}

			BasicDynaBean operation = operationMasterDao.getOperationChargeBean(cdto.getOp_id(),
					bedType, orgId);
			BasicDynaBean theatre = null;
			BasicDynaBean surgeonBean = null;
			BasicDynaBean anasthesiaTypeChargeBean = null;
			BasicDynaBean anaBean = null;
			Timestamp from = cdto.getFrom_date();
			Timestamp to = cdto.getTo_date();

			refChargeList.add(cdto);
			String  surgId = null, anethId = null, anaTypId = null, theId = null;
			for (int i = 0; i < refChargeList.size(); i++) {
				if (refChargeList.get(i).getChargeHead().equals("TCOPE")
						|| cdto.getChargeHead().equals("TCOPE"))
					theId = refChargeList.get(i).getActDescriptionId();
				if (refChargeList.get(i).getChargeHead().equals("SUOPE")
						|| cdto.getChargeHead().equals("SUOPE"))
					surgId = refChargeList.get(i).getActDescriptionId();
				if (refChargeList.get(i).getChargeHead().equals("ANAOPE")
						|| cdto.getChargeHead().equals("ANAOPE"))
					anethId = refChargeList.get(i).getActDescriptionId();
				if (refChargeList.get(i).getChargeHead().equals("ANATOPE")
						|| cdto.getChargeHead().equals("ANATOPE"))
					anaTypId = refChargeList.get(i).getActDescriptionId();
			}

			refChargeList.remove(cdto);
			if (theId != null && !theId.equals("")) {
				theatre = theatreMasterDao.getTheatreChargeDetails(theId, bedType, orgId);
			}

			if (surgId != null && !surgId.equals("")) {
				surgeonBean = DoctorMasterDAO.getOTDoctorChargesBean(surgId, bedType, orgId);
			}

			if (anaTypId != null && !anaTypId.equals("")) {
				anasthesiaTypeChargeBean = anaesthesiaTypeChargesDao.getAnasthesiaTypeCharge(anaTypId,
						orgId, bedType);

				if (anasthesiaTypeChargeBean != null && !(Boolean)anasthesiaTypeChargeBean.get("applicable"))
					ratePlanNotApplicableList.add(cdto.getBillNo()+" ("+cdto.getActDescription()+" Dated:"+cdto.getPostedDate()+")");
			}

			if (anethId != null && !anethId.equals("")) {
				anaBean = DoctorMasterDAO.getOTDoctorChargesBean(anethId, bedType, orgId);
			}

			String units = (cdto.getActUnit()).equals("Days") ? "D" : "H";

			if(cdto.getOp_id() != null)
			newChgLst = OrderBO.getOperationCharges(cdto.getOp_id(), operation, theatre, surgeonBean,
					anaBean, from, to, units, isInsurance, planIds , "F", visitType,
					anasthesiaTypeChargeBean, visitId, cdto.getFirstOfCategory());
		}
		if(cdto.getOp_id() == null) {
			return true;
		} else {
			for(ChargeDTO charge : newChgLst){
				charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
			}
			result = updateNewChargeValue(newChgLst, cdto, refChargeList);
			return result != null && result.equals("success");
		}
	}

	/*
	 * This function updates the surgery charges based on the new rate plan
	 * details.
	 */
	public boolean updateSurgeryChargeNew(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException,Exception {

		String result = null;
		List<ChargeDTO> newChgLst = null;
		//Update the anaesthetist charge
		if (cdto.getChargeRef() == null && cdto.getHasActivity()
				&& doctorsDao.findByKey("doctor_id", cdto.getActDescriptionId()) != null) {
			return updateDoctorChargeNew(cdto, orgId, bedType, refChargeList);
		}
		if (cdto.getHasActivity()) {
			BillActivityChargeDAO bacdao = new BillActivityChargeDAO(con);
			BillActivityCharge bac = bacdao.getActivity(cdto.getChargeId());
			//Continue only if the charge is a valid bed charge else stop proccessing.
			if(bac.getActivityCode().equals("OTC"))
				return true;
			
			BasicDynaBean bedOpBean = null;
			BasicDynaBean secSurgeonTeam = null;
      BasicDynaBean procedureDetails = OperationDetailsDAO.getOperationDetailsByPrescribedId(Integer.parseInt(bac
          .getActivityId()),cdto.getOp_id());
      BasicDynaBean primaryDetails = null;
      boolean isSecondary = false;
      String opid = null;
      
      if ( procedureDetails == null 
            || procedureDetails.get("oper_priority") == null 
            || procedureDetails.get("oper_priority").equals("P") ) {
        //opertion order from Order screen or OT management
         bedOpBean  = bedOperationScheduleDao.findByKey("prescribed_id", Integer.parseInt(bac
            .getActivityId()));
         opid = (bedOpBean != null) ? (String) bedOpBean.get("operation_name") : null;
      } else {
        bedOpBean  = bedOperationSecondaryScheduleDao.findByKey("sec_prescribed_id", Integer.parseInt(bac
            .getActivityId()));
        if (bedOpBean != null) {
          primaryDetails = bedOperationScheduleDao.findByKey("prescribed_id", (int)bedOpBean.get("prescribed_id"));
          isSecondary = true;
          secSurgeonTeam = OperationDetailsDAO.getOperationTeam(con,
              (int)procedureDetails.get("operation_proc_id"));
          opid = (String) bedOpBean.get("operation_id");
        }
      }
            List<BasicDynaBean> anaesthesiaTypeDetails= new ArrayList<BasicDynaBean>();
            Map<String, String> filterMap = new HashMap<String, String>();
            filterMap.put("charge_head", "ANATOPE");
            filterMap.put("charge_ref", cdto.getChargeId());
            List<BasicDynaBean> anaeasList = billChargeDao.listAll(con, null, filterMap, "charge_id");
            if (anaeasList != null) {
              for (BasicDynaBean anaeas : anaeasList) {
                if (anaeas.get("surgery_anesthesia_details_id") != null) {
                  BasicDynaBean bean = surgeryAnesthesiaDetails.findByKey("surgery_anesthesia_details_id", 
                      anaeas.get("surgery_anesthesia_details_id"));
                      anaesthesiaTypeDetails.add(bean);
                }
              }
            } else {
                anaesthesiaTypeDetails= surgeryAnesthesiaDetails.findAllByKey("prescribed_id", 
			    isSecondary ? primaryDetails.get("prescribed_id") : Integer.parseInt(bac.getActivityId()));
            }
			BasicDynaBean operation = operationMasterDao.getOperationChargeBean(opid, bedType, orgId);

			if (operation != null && !(Boolean)operation.get("applicable"))
				ratePlanNotApplicableList.add(cdto.getBillNo()+" ("+cdto.getActDescription()+" Dated:"+cdto.getPostedDate()+")");
			
			String ot = isSecondary ? (String) primaryDetails.get("theatre_name") : (String) bedOpBean.get("theatre_name");
			String surgeon = null;
			
			if(isSecondary && secSurgeonTeam != null) {
			  surgeon = (String)secSurgeonTeam.get("team_doc_id");
			}else if (!isSecondary) {
			  surgeon =  (String) bedOpBean.get("surgeon");
			}
			
      String units = isSecondary ? (String) primaryDetails.get("hrly") :  (String) bedOpBean.get("hrly");
      units = (units == null || units.equals("")) ? "" : units;
      units = units.equals("checked") ? "H" : "D";
      Timestamp from = isSecondary ? (Timestamp) primaryDetails.get("start_datetime") :  (Timestamp) bedOpBean.get("start_datetime");
      Timestamp to = isSecondary ? from : (Timestamp) bedOpBean.get("end_datetime");
      String finalizationStatus = isSecondary ? (String) primaryDetails.get("finalization_status") :  (String) bedOpBean.get("finalization_status");

			BasicDynaBean theatre = theatreMasterDao.getTheatreChargeDetails(ot, bedType, orgId);
			BasicDynaBean surgeonBean = null;
			
			if(!StringUtils.isEmpty(surgeon)) {
			  surgeonBean = DoctorMasterDAO.getOTDoctorChargesBean(surgeon, bedType, orgId);
			}
			
			BasicDynaBean anasthesiaTypeChargeBean = null;

			newChgLst = OrderBO.getOperationCharges(cdto.getOp_id(), operation, theatre, surgeonBean, null,
					from, to, units, isInsurance, planIds, finalizationStatus, visitType,
					anasthesiaTypeChargeBean, visitId, cdto.getFirstOfCategory());

			if(anaesthesiaTypeDetails != null && anaesthesiaTypeDetails.size() > 0) {

				for (int i=0;i<anaesthesiaTypeDetails.size();i++) {
					String anTypeId = (String)anaesthesiaTypeDetails.get(i).get("anesthesia_type");
					Timestamp anFrom = (Timestamp)anaesthesiaTypeDetails.get(i).get("anaes_start_datetime");
					Timestamp anTo = (Timestamp)anaesthesiaTypeDetails.get(i).get("anaes_end_datetime");
					anTo = anTo == null? cdto.getPostedDate() : anTo;
					anFrom = anFrom == null? cdto.getPostedDate() : anFrom;
					anasthesiaTypeChargeBean = anaesthesiaTypeChargesDao.getAnasthesiaTypeCharge(
							anTypeId, bedType, orgId);

					if (anasthesiaTypeChargeBean != null && !(Boolean)anasthesiaTypeChargeBean.get("applicable"))
						ratePlanNotApplicableList.add(cdto.getBillNo()+" ("+cdto.getActDescription()+" Dated:"+cdto.getPostedDate()+")");

					newChgLst.addAll(OrderBO.getAnaesthesiaTypeCharges(cdto.getOp_id(), operation, anFrom, anTo, units,
							isInsurance, planIds, finalizationStatus, visitType, anasthesiaTypeChargeBean, visitId, cdto.getFirstOfCategory()));

				/*	OrderBO.getOperationCharges(cdto.getOp_id(), operation, theatre, surgeonBean, null,
							anFrom, anTo, units, isInsurance, planIds, finalizationStatus, visitType,
					anasthesiaTypeChargeBean, visitId, cdto.getFirstOfCategory())*/
				}
			}
		} else {
			// No reference to the operation implies that its an other charge and not a surgical charge.
			if(cdto.getOp_id() == null || cdto.getOp_id().trim().equals("")){
				return true;
			}

			BasicDynaBean operation = operationMasterDao.getOperationChargeBean(cdto.getOp_id(),
					bedType, orgId);
			BasicDynaBean theatre = null;
			BasicDynaBean surgeonBean = null;
			BasicDynaBean anasthesiaTypeChargeBean = null;
			BasicDynaBean anaBean = null;
			Timestamp from = cdto.getFrom_date();
			Timestamp to = cdto.getTo_date();

			refChargeList.add(cdto);
			String  surgId = null, anethId = null, anaTypId = null, theId = null;
			for (int i = 0; i < refChargeList.size(); i++) {
				if (refChargeList.get(i).getChargeHead().equals("TCOPE")
						|| cdto.getChargeHead().equals("TCOPE"))
					theId = refChargeList.get(i).getActDescriptionId();
				if (refChargeList.get(i).getChargeHead().equals("SUOPE")
						|| cdto.getChargeHead().equals("SUOPE"))
					surgId = refChargeList.get(i).getActDescriptionId();
				if (refChargeList.get(i).getChargeHead().equals("ANAOPE")
						|| cdto.getChargeHead().equals("ANAOPE"))
					anethId = refChargeList.get(i).getActDescriptionId();
				if (refChargeList.get(i).getChargeHead().equals("ANATOPE")
						|| cdto.getChargeHead().equals("ANATOPE"))
					anaTypId = refChargeList.get(i).getActDescriptionId();
			}

			refChargeList.remove(cdto);
			if (theId != null && !theId.equals("")) {
				theatre = theatreMasterDao.getTheatreChargeDetails(theId, bedType, orgId);
			}

			if (surgId != null && !surgId.equals("")) {
				surgeonBean = DoctorMasterDAO.getOTDoctorChargesBean(surgId, bedType, orgId);
			}

			if (anaTypId != null && !anaTypId.equals("")) {
				anasthesiaTypeChargeBean = anaesthesiaTypeChargesDao.getAnasthesiaTypeCharge(anaTypId,
						orgId, bedType);

				if (anasthesiaTypeChargeBean != null && !(Boolean)anasthesiaTypeChargeBean.get("applicable"))
					ratePlanNotApplicableList.add(cdto.getBillNo()+" ("+cdto.getActDescription()+" Dated:"+cdto.getPostedDate()+")");
			}

			if (anethId != null && !anethId.equals("")) {
				anaBean = DoctorMasterDAO.getOTDoctorChargesBean(anethId, bedType, orgId);
			}
			
			String units = "H";
			if (cdto.getActUnit() != null && cdto.getActUnit() != "") {
				units = (cdto.getActUnit()).equals("Days") ? "D" : "H";				
			}

			if(cdto.getOp_id() != null)
			newChgLst = OrderBO.getOperationCharges(cdto.getOp_id(), operation, theatre, surgeonBean,
					anaBean, from, to, units, isInsurance, planIds , "F", visitType,
					anasthesiaTypeChargeBean, visitId, cdto.getFirstOfCategory());
		}
		if(cdto.getOp_id() == null) {
			return true;
		} else {
			/*for(ChargeDTO charge : newChgLst){
				charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
			}*/
			result = updateNewChargeValueNew(newChgLst, cdto, refChargeList);
			return result != null && result.equals("success");
		}
	}

	/*
	 * This function updates the Laboratory charges (both diagnostics and
	 * radiology) based on the organization details.
	 */
	public boolean updateDiagCharge(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException,Exception {
		String result = null;
		boolean isValidTest = checkIfValidTest(cdto.getActDepartmentId());
		if(!isValidTest)
			return true;
		List<ChargeDTO> newChgLst = getDiagChargeNew(cdto, orgId, bedType);
		for(ChargeDTO charge:newChgLst){
			charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
		}
		result = updateNewChargeValue(newChgLst, cdto, refChargeList);
		return result != null && result.equals("success");
	}

	/*
	 * This function updates the Laboratory charges (both diagnostics and
	 * radiology) based on the organization details.
	 */
	public boolean updateDiagChargeNew(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException,Exception {
		String result = null;
		boolean isValidTest = checkIfValidTest(cdto.getActDepartmentId());
		if(!isValidTest)
			return true;
		List<ChargeDTO> newChgLst = getDiagChargeNew(cdto, orgId, bedType);
		/*for(ChargeDTO charge:newChgLst){
			charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
		}*/
		result = updateNewChargeValueNew(newChgLst, cdto, refChargeList);
		return result != null && result.equals("success");
	}

  public List<ChargeDTO> getDiagChargeNew(ChargeDTO cdto, String orgId, String bedType)
      throws SQLException {
    BasicDynaBean test = AddTestDAOImpl.getTestDetails(cdto.getActDescriptionId(), bedType,
				(String) orgId);

		if (test != null && !(Boolean)test.get("applicable"))
			ratePlanNotApplicableList.add(cdto.getBillNo()+" ("+cdto.getActDescription()+" Dated:"+cdto.getPostedDate()+")");

		List<ChargeDTO> newChgLst = OrderBO.getTestCharges(test, cdto.getActQuantity(), isInsurance, planIds, visitType,
				visitId, cdto.getFirstOfCategory(), cdto.getPayeeDoctorId());
    return newChgLst;
  }
	/*
	 * This function updates the Service charges based on the organization
	 * details.
	 */
	public boolean updateServiceCharge(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException, Exception {
		String result = null;
		//Service charges always have a department, unless its an Other charge.
		if(cdto.getActDepartmentId()== null || cdto.getActDepartmentId().isEmpty())
			return true;
		List<ChargeDTO> newChgLst = getServiceChargeNew(cdto, orgId, bedType);
		for(ChargeDTO charge:newChgLst){
			charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
		}
		result = updateNewChargeValue(newChgLst, cdto, refChargeList);
		return result != null && result.equals("success");
	}
	
	/*
	 * This function updates the Service charges based on the organization
	 * details.
	 */
	public boolean updateServiceChargeNew(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException, Exception {
		String result = null;
		//Service charges always have a department, unless its an Other charge.
		if(cdto.getActDepartmentId()== null || cdto.getActDepartmentId().isEmpty())
			return true;
		List<ChargeDTO> newChgLst = getServiceChargeNew(cdto, orgId, bedType);
		/*for(ChargeDTO charge:newChgLst){
			charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
		}*/
		result = updateNewChargeValueNew(newChgLst, cdto, refChargeList);
		return result != null && result.equals("success");
	}

  public List<ChargeDTO> getServiceChargeNew(ChargeDTO cdto, String orgId, String bedType)
      throws SQLException {
    BasicDynaBean service = new MasterServicesDao().getServiceChargeBean(cdto.getActDescriptionId(),
				bedType, orgId);

		if (service != null && !(Boolean)service.get("applicable"))
			ratePlanNotApplicableList.add(cdto.getBillNo()+" ("+cdto.getActDescription()+" Dated: "+cdto.getPostedDate()+")");

		List<ChargeDTO> newChgLst = OrderBO.getServiceCharges(service, cdto.getActQuantity(), isInsurance, planIds,
				visitType, visitId, cdto.getFirstOfCategory(), cdto.getPayeeDoctorId());
    return newChgLst;
  }

	/*
	 * This function updates the Dietary charges based on the organization
	 * details.
	 */
	public boolean updateDietaryCharge(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException, Exception {
		String result = null;
		boolean validMeal = checkIfValidDietCharge(cdto.getActDescriptionId());
		if(!validMeal)
			return true;
		List<ChargeDTO> newChgLst = getDietaryChargeNew(cdto, orgId, bedType);
		for(ChargeDTO charge : newChgLst){
			charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
		}
		result = updateNewChargeValue(newChgLst, cdto, refChargeList);
		return result != null && result.equals("success");
	}
	/*
	 * This function updates the Dietary charges based on the organization
	 * details.
	 */
	public boolean updateDietaryChargeNew(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException, Exception {
		String result = null;
		boolean validMeal = checkIfValidDietCharge(cdto.getActDescriptionId());
		if(!validMeal)
			return true;
		List<ChargeDTO> newChgLst = getDietaryChargeNew(cdto, orgId, bedType);
		/*for(ChargeDTO charge : newChgLst){
			charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
		}*/
		result = updateNewChargeValueNew(newChgLst, cdto, refChargeList);
		return result != null && result.equals("success");
	}

  public List<ChargeDTO> getDietaryChargeNew(ChargeDTO cdto, String orgId, String bedType)
      throws SQLException {
    BasicDynaBean meal = new DietaryMasterDAO().getChargeForMeal(orgId, cdto.getActDescriptionId(),
				bedType);
		List<ChargeDTO> newChgLst = OrderBO.getMealCharges(meal, cdto.getActQuantity(), isInsurance, planIds,
				visitType, visitId, cdto.getFirstOfCategory());
    return newChgLst;
  }
	
	/*
	 * This function updates the Other Services ordered based on the
	 * organization details.
	 */
	public boolean updateOtherCharge(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException, Exception {
		String result = null;
		List<ChargeDTO> newChgLst = getOtherChargeNew(cdto);
		for(ChargeDTO charge:newChgLst){
			charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
		}
		result = updateNewChargeValue(newChgLst, cdto, refChargeList);
		return result != null && result.equals("success");
	}

	/*
	 * This function updates the Other Services ordered based on the
	 * organization details.
	 */
	public boolean updateOtherChargeNew(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException, Exception {
		String result = null;
		List<ChargeDTO> newChgLst = getOtherChargeNew(cdto);
		/*for(ChargeDTO charge:newChgLst){
			charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
		}*/
		result = updateNewChargeValueNew(newChgLst, cdto, refChargeList);
		return result != null && result.equals("success");
	}

  public List<ChargeDTO> getOtherChargeNew(ChargeDTO cdto) throws  SQLException {
    BasicDynaBean otherService = new CommonChargesDAO().getCommonCharge(cdto.getActDescriptionId());
		List<ChargeDTO> newChgLst = OrderBO.getOtherCharges(otherService, cdto.getActQuantity(), isInsurance,
				planIds, visitType, visitId, cdto.getFirstOfCategory());
    return newChgLst;
  }

	/*
	 * This function updates the Packages ordered based on the organization
	 * details.
	 */
	public boolean updatePackageCharge(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException, Exception {
		String result = null;
		String packageId = null;
		if ("PKGPKG".equals(cdto.getChargeHead())) {
		boolean validPackage = checkIfValidPackage(cdto.getActDescriptionId());
		if(!validPackage)
			return true;
		}
		if ("PKGPKG".equals(cdto.getChargeHead())) {
			packageId =  cdto.getActDescriptionId();
		} else {
			packageId =  String.valueOf((Integer)cdto.getPackageId());
		}
		boolean isRatePlanApplicable = checkRatePlanApplicable(Integer.parseInt(packageId), orgId);
		if(!isRatePlanApplicable) {
			  return true;
		}
		boolean isCustomizedPack = isCustomizedPackage(cdto);
		if(isCustomizedPack) {
			return true;
		}
		List<ChargeDTO> newChgLst = getPackageChargeNew(cdto, orgId, bedType);
		for(ChargeDTO charge:newChgLst){
			charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
		}
		result = updateNewChargeValue(newChgLst, cdto, refChargeList);
		return result != null && result.equals("success");
	}
	
	/*
	 * This function updates the Packages ordered based on the organization
	 * details.
	 */
	public boolean updatePackageChargeNew(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException, Exception {
		String result = null;
		String packageId = null;
		if ("PKGPKG".equals(cdto.getChargeHead())) {
		  boolean validPackage = checkIfValidPackage(cdto.getActDescriptionId());
		  if(!validPackage)
			  return true;
		}
		if ("PKGPKG".equals(cdto.getChargeHead())) {
			packageId =  cdto.getActDescriptionId();
	    } else {
	        packageId =  String.valueOf((Integer)cdto.getPackageId());
	    }
		boolean isRatePlanApplicable = checkRatePlanApplicable(Integer.parseInt(packageId), orgId);
		if(!isRatePlanApplicable) {
			if ("PKGPKG".equals(cdto.getChargeHead())) {
				ratePlanNotApplicableList.add(cdto.getBillNo()+" ("+cdto.getActDescription()+" Dated:"+cdto.getPostedDate()+")");
			}
			  return true;
		}
		boolean isCustomizedPack = isCustomizedPackage(cdto);
		if(isCustomizedPack) {
			return true;
		}
		List<ChargeDTO> newChgLst = getPackageChargeNew(cdto, orgId, bedType);
		/*for(ChargeDTO charge:newChgLst){
			charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
		}*/
		result = updateNewChargeValueNew(newChgLst, cdto, refChargeList);
		return result != null && result.equals("success");
	}

  public List<ChargeDTO> getPackageChargeNew(ChargeDTO cdto, String orgId, String bedType)
      throws SQLException {
	  List<ChargeDTO> newChgLst = Collections.EMPTY_LIST;
	  List<BasicDynaBean>  packComList = billChargeDao.findAllByKey("charge_ref",
				cdto.getChargeId());
	  BasicDynaBean pkg  = null;
	  String packageId = null;
		if ("PKGPKG".equals(cdto.getChargeHead())) {
			packageId = cdto.getActDescriptionId();
		} else {
			packageId = String.valueOf((Integer) cdto.getPackageId());
		}
	  BasicDynaBean pkgChargeBean  = PackageDAO.getPackageDetails(Integer.parseInt(packageId), orgId, bedType);

	  if ("PKGPKG".equals(cdto.getChargeHead()) && packComList.isEmpty()) {
		//Package Charges update for old packages
	        if (null != pkgChargeBean && !(Boolean)pkgChargeBean.get("applicable")) {
					ratePlanNotApplicableList.add(cdto.getBillNo()+" ("+cdto.getActDescription()+" Dated:"+cdto.getPostedDate()+")");
	        }
	        if (null != pkgChargeBean) {
	           newChgLst = OrderBO.getPackageCharges(pkgChargeBean, cdto.getActQuantity(), cdto.getPayeeDoctorId(),
								isInsurance, planIds, visitType, visitId, cdto.getFirstOfCategory());
	        }
	  } else {
		 BigDecimal itemAmount = BigDecimal.ZERO;
		 BigDecimal itemDisc = BigDecimal.ZERO;
		 BigDecimal itemCharge = BigDecimal.ZERO;
		 BigDecimal chgDisc = BigDecimal.ZERO;
		 BigDecimal packageCharge = (BigDecimal) pkgChargeBean.get("charge");
		 BigDecimal packageDiscount = (BigDecimal) pkgChargeBean.get("discount");
		 if ("PKGPKG".equals(cdto.getChargeHead())) {
			 pkg = pkgChargeBean ;
			 if (cdto.getAmount().compareTo(BigDecimal.ZERO) != 0 ) {
				 BasicDynaBean pkgInv = PackageDAO.getPackContChargesForInventory(cdto.getPackageId(), orgId, bedType);
				 itemAmount =((BigDecimal) pkgInv.get("charge"));
				 if (cdto.getActQuantity().compareTo(BigDecimal.ZERO) != 0 ) {
				   itemCharge = (itemAmount).divide(cdto.getActQuantity(), 2);
				 }
				 chgDisc = OrderBO.discountSplit(itemCharge, packageCharge, packageDiscount);
				 itemDisc = itemDisc.add(chgDisc.multiply(cdto.getActQuantity()));
			}
			pkg.set("charge", itemAmount);
			pkg.set("discount", itemDisc);
		 } else {
			//fetching the Package Content Details of new packages
			  int packContentId = Integer.parseInt(cdto.getActDescriptionId());
			  pkg  = PackageDAO.getPackageContentDetails(packContentId, orgId, bedType );
			  if(pkg != null ){
				  itemAmount =((BigDecimal) pkg.get("charge"));
				  if (cdto.getActQuantity().compareTo(BigDecimal.ZERO) != 0 ) {
					  itemCharge = (itemAmount).divide(cdto.getActQuantity(),2,RoundingMode.HALF_UP);
				  }
				  chgDisc = OrderBO.discountSplit(itemAmount, packageCharge, packageDiscount);
				  itemDisc = itemDisc.add(chgDisc.divide(cdto.getActQuantity(), 2,RoundingMode.HALF_UP));
				  pkg.set("charge", itemCharge);
				  pkg.set("discount", itemDisc);
			  }else{
			  	logger.warn("Rates not available for " + orgId + " and " + bedType);
			  }

		 }
		 if (null != pkg) {
		      newChgLst = OrderBO.getPackageContentCharges(pkg, cdto.getActQuantity(), cdto.getPayeeDoctorId(),
					isInsurance, planIds, visitType, visitId, cdto.getFirstOfCategory(),cdto.getChargeHead(),
					cdto.getActDescription(), cdto.getActDescriptionId(),cdto.getActRemarks(),cdto.getChargeRef());
		 }
	 }
      return newChgLst;
  }
	
	/*
   * This function updates the Inventory issues based on the organization
   * details.
   */
  public boolean updatePatientIssuesChargeNew(ChargeDTO cdto, String mrNo, String storeRatePlanId,
      List<ChargeDTO> refChargeList, Map<String, BasicDynaBean> visitIssues)
          throws SQLException, Exception {
    String result = null;
    boolean validMedicine = checkIfValidMedicine(cdto.getActDescriptionId());
    if (!validMedicine)
      return true;
    
    BasicDynaBean bean = visitIssues.get(cdto.getChargeId());
    Map<String, Object> params = new HashMap<>();
    BigDecimal qty = BigDecimal.ZERO;
    if (cdto.getChargeHead().equals("INVITE")) {
      params.put("store_id", Integer.toString((int) bean.get("dept_from")));
      params.put("visit_id", bean.get("issued_to"));
      params.put("pkg_size", bean.get("issue_pkg_size").toString());
      qty =cdto.getActQuantity();


    } else {
      params.put("store_id", Integer.toString((int) bean.get("dept_to")));
      params.put("visit_id", bean.get("returned_by"));
      params.put("pkg_size", bean.get("rtn_pkg_size").toString());
      qty =cdto.getActQuantity().abs();
    }
    

    params.put("medicine_id", Integer.toString((int) bean.get("medicine_id")));
    params.put("item_batch_id", Integer.toString((int) bean.get("item_batch_id")));
    params.put("qty", String.valueOf(qty));
    params.put("issue_type", bean.get("item_unit"));
    params.put("bill_no", cdto.getBillNo());
    params.put("change_type", "B");
    params.put("mr_no", mrNo);
    params.put("visitStoreRatePlanId", storeRatePlanId);

    Map<String, Object> newAmoutMap = patIssueService.getItemAmounts(params);
    
    billChargeTaxDAO.deleteBillChargeTax(con, (String) cdto.getChargeId());
    billChargeTaxDAO.deleteBillChargeClaimTax(con, (String) cdto.getChargeId());
    
    Map taxDeatails = (Map) newAmoutMap.get("tax_details");
    
    List <Map<Integer,Object>> taxSubGroupList = (ArrayList)taxDeatails.get("tax_map");
    if (!taxSubGroupList.isEmpty()) {
      for (Map.Entry<Integer, Object> mapEntry : taxSubGroupList.get(0).entrySet()) {
        Map<String, Object> mapEntryValue = (Map<String, Object>) mapEntry.getValue();
        BasicDynaBean taxBean = billChargeTaxDAO.getBean();
        taxBean.set("charge_id", cdto.getChargeId());
        taxBean.set("tax_sub_group_id",
            Integer.parseInt((String) mapEntryValue.get("tax_sub_group_id")));
        taxBean.set("tax_rate", new BigDecimal((String) mapEntryValue.get("rate")));
        if (cdto.getChargeHead().equals("INVITE")) {
          taxBean.set("tax_amount", new BigDecimal((String) mapEntryValue.get("amount")));
        } else {
          taxBean.set("tax_amount", new BigDecimal((String) mapEntryValue.get("amount")).negate());
        }
        billChargeTaxDAO.insert(con, taxBean);
      }
    }
    Map amtDeatails = (Map) newAmoutMap.get("amount_details");

    BigDecimal disc = (BigDecimal) amtDeatails.get("discount_amt");
    BigDecimal amt = (BigDecimal) amtDeatails.get("unit_mrp");

    if (disc.compareTo(BigDecimal.ZERO) == -1) {
    	successMsg.append("The following item has a negitive discount.<br />");
	successMsg.append(cdto.getActDescription());

    	return false;
    }
    if (cdto.getChargeHead().equals("INVITE")) {
      BigDecimal netQty = cdto.getActQuantity().add(cdto.getReturnQty());
      disc = disc.divide(qty,3 , BigDecimal.ROUND_HALF_DOWN).multiply(netQty);
      List<ChargeDTO> newChgLst = patIssueService.getIssueCharges(cdto, isInsurance, planIds,
          visitType, visitId, amt, disc);
      result = updateNewChargeValueNew(newChgLst, cdto, refChargeList);
    } else {
      List<ChargeDTO> newChgLst = patIssueService.getIssueCharges(cdto, isInsurance, planIds,
          visitType, visitId, amt, BigDecimal.ZERO);
      result = updateNewChargeValueNew(newChgLst, cdto, refChargeList);
    }
    return result != null && result.equals("success");
  }
  

	/*
	 * This function updates the Equipment charges based on organization
	 * details. For the quatity calculation, in case the equipment used_till
	 * date is null, the quantity is fetched from the bill charge.
	 */
	public boolean updateEquipmentCharge(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException,Exception {
		String result = null;
		Boolean isOperation = cdto.getChargeHead().equals("EQOPE");

		Boolean isValidEquipCharge = checkIfValidEquipCharge(cdto.getActDescriptionId());
		if(!isValidEquipCharge) {
			return true;
		}
		BasicDynaBean equipDetails = new EquipmentChargeDAO().getEquipmentCharge(cdto.getActDescriptionId(),
				bedType, orgId);
		List<ChargeDTO> newChgLst = null;

		if (cdto.getHasActivity()) {
			BillActivityChargeDAO bacdao = new BillActivityChargeDAO(con);
			BillActivityCharge bac = bacdao.getActivity(cdto.getChargeId());
			String prescId = bac.getActivityId();
			BasicDynaBean equipPres = equipmentPrescribedDAO.findByKey("prescribed_id",
					Integer.parseInt(prescId));

			Timestamp from = (Timestamp) equipPres.get("used_from");
			Timestamp to = (Timestamp) equipPres.get("used_till");
			String units = (String) equipPres.get("units");

			newChgLst = OrderBO.getEquipmentCharges(equipDetails, from, to, units, isOperation,
					cdto.getActQuantity(), isInsurance, planIds, visitType, visitId, cdto
							.getFirstOfCategory());
		} else {
			Timestamp from = cdto.getFrom_date();
			Timestamp to = cdto.getTo_date();
			String units = cdto.getActUnit();

			newChgLst = OrderBO.getEquipmentCharges(equipDetails, from, to, units, isOperation, cdto.getActQuantity(),
					isInsurance, planIds, visitType, visitId, cdto.getFirstOfCategory());
		}
		for(ChargeDTO charge:newChgLst){
			charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
		}
		result = updateNewChargeValue(newChgLst, cdto, refChargeList);
		return result != null && result.equals("success");
	}

	/*
	 * This function updates the Equipment charges based on organization
	 * details. For the quatity calculation, in case the equipment used_till
	 * date is null, the quantity is fetched from the bill charge.
	 */
	public boolean updateEquipmentChargeNew(ChargeDTO cdto, String orgId, String bedType,
			List<ChargeDTO> refChargeList) throws SQLException,Exception {
		String result = null;

		Boolean isValidEquipCharge = checkIfValidEquipCharge(cdto.getActDescriptionId());
		if(!isValidEquipCharge) {
			return true;
		}
		List<ChargeDTO> newChgLst = getEquipmentChargeNew(cdto, orgId, bedType);
		/*for(ChargeDTO charge:newChgLst){
			charge.setInsuranceAmt(planIds, visitType, charge.getFirstOfCategory());
		}*/
		result = updateNewChargeValueNew(newChgLst, cdto, refChargeList);
		return result != null && result.equals("success");
	}

  public List<ChargeDTO> getEquipmentChargeNew(ChargeDTO cdto, String orgId, String bedType)
      throws SQLException {
    Boolean isOperation = cdto.getChargeHead().equals("EQOPE");
		BasicDynaBean equipDetails = new EquipmentChargeDAO().getEquipmentCharge(cdto.getActDescriptionId(),
				bedType, orgId);
		List<ChargeDTO> newChgLst = null;
		if (equipDetails == null) {
		  return newChgLst;
		}
		if (cdto.getHasActivity()) {
			BillActivityChargeDAO bacdao = new BillActivityChargeDAO(con);
			BillActivityCharge bac = bacdao.getActivity(cdto.getChargeId());
			String prescId = bac.getActivityId();
			BasicDynaBean equipPres = equipmentPrescribedDAO.findByKey("prescribed_id",
					Integer.parseInt(prescId));

			Timestamp from = (Timestamp) equipPres.get("used_from");
			Timestamp to = (Timestamp) equipPres.get("used_till");
			String units = (String) equipPres.get("units");

			newChgLst = OrderBO.getEquipmentCharges(equipDetails, from, to, units, isOperation,
					cdto.getActQuantity(), isInsurance, planIds, visitType, visitId, cdto
							.getFirstOfCategory());
		} else {
			Timestamp from = cdto.getFrom_date();
			Timestamp to = cdto.getTo_date();
			String units = cdto.getActUnit();

			newChgLst = OrderBO.getEquipmentCharges(equipDetails, from, to, units, isOperation, cdto.getActQuantity(),
					isInsurance, planIds, visitType, visitId, cdto.getFirstOfCategory());
		}
    return newChgLst;
  }
	
	public static boolean checkIfValidRegistrationCharge(String actDescriptionId){
		return actDescriptionId.equals("GREG") || actDescriptionId.equals("IPREG")
				|| actDescriptionId.equals("OPREG") || actDescriptionId.equals("MLREG") || actDescriptionId.equals("EMREG");
	}

	public static boolean checkIfValidDoctorCharge(String actDescriptionId) throws SQLException{
		BasicDynaBean docBean = DoctorMasterDAO.getDoctorById(actDescriptionId);
		return docBean != null;
	}

	public static boolean checkIfValidBedCharge(String actDescriptionId) throws SQLException, IOException {
		BasicDynaBean bedBean = BedMasterDAO.getBedDetailsBean(actDescriptionId);
		return bedBean != null;
	}

	public static boolean checkIfValidTest(String actDeptId){
		return actDeptId != null && !actDeptId.isEmpty();
	}

	public static boolean checkIfValidDietCharge( String actDescriptionId) throws SQLException {
		if(actDescriptionId == null || actDescriptionId.equals(""))
			return false;

		int id = 0;
		try {
			id = Integer.parseInt(actDescriptionId);
		} catch( NumberFormatException ne) {
			return false;
		}

		return new GenericDAO("diet_master").findByKey("diet_id", id) != null;
	}

	public static boolean checkIfValidPackage(String actDescriptionId) throws SQLException {
		if(actDescriptionId == null || actDescriptionId.equals(""))
			return false;
		int id = 0;
		try {
			id = Integer.parseInt(actDescriptionId);
		} catch( NumberFormatException ne) {
			return false;
		}
		return new GenericDAO("packages").findByKey("package_id", id) != null;
	}
	
  public static boolean checkIfValidMedicine(String actDescriptionId) throws SQLException {
    if (StringUtils.isEmpty(actDescriptionId))
      return false;
    int id = 0;
    try {
      id = Integer.parseInt(actDescriptionId);
    } catch (NumberFormatException ne) {
      return false;
    }
    return storeItemDetailsDao.exist("medicine_id", id);
  }

	public static boolean checkIfValidEquipCharge(String actDescriptionId) throws SQLException {
		if(actDescriptionId == null || actDescriptionId.equals(""))
			return false;
		return new GenericDAO("equipment_master").findByKey("eq_id",actDescriptionId) != null;
	}
	
	public boolean needOldDiscounts(Connection con,String billNo) throws Exception{
		BasicDynaBean existingBillDetails = BillDAO.getBillBean(billNo);
		BasicDynaBean updatedBillDetails = BillDAO.getBillBean(con,billNo);
		boolean keepExisitngDiscounts = true;
		
		if ( existingBillDetails.get("discount_category_id") != null && updatedBillDetails.get("discount_category_id") != null ) {
			keepExisitngDiscounts = ( (int)existingBillDetails.get("discount_category_id") 
					== (int)updatedBillDetails.get("discount_category_id") );
		}
		
		return keepExisitngDiscounts;
	}
	
	public boolean needOldDiscountsNew(String billNo) throws Exception{
		//BasicDynaBean existingBillDetails = BillDAO.getBillBean(billNo);
		//BasicDynaBean updatedBillDetails = BillDAO.getBillBean(con,billNo);
		boolean keepExisitngDiscounts = true;
		
		if (existingBillDetails.get("discount_category_id") != null && updatedBillDetails.get("discount_category_id") != null ) {
			keepExisitngDiscounts = ( (int)existingBillDetails.get("discount_category_id") 
					== (int)updatedBillDetails.get("discount_category_id") );
		}
		
		return keepExisitngDiscounts;
	}
	
	public boolean isDiscountNotApplicableChargeGrp(ChargeDTO charge){
		String[] discNotApplChargeGrps = new String[]{"MED","ITE","RET"};
		return ( Arrays.asList(discNotApplChargeGrps).contains(charge.getChargeGroup()));
	}

	public static boolean checkRatePlanApplicable(int packId, String orgId) throws SQLException, IOException {
		BasicDynaBean packBean = PackageDAO.getRatePlanBean(packId, orgId);
		return packBean != null;
	}

	public boolean isCustomizedPackage(ChargeDTO charge) throws SQLException, IOException {
		boolean isCustomizedPackage = false;
		BasicDynaBean packPrescribedBean = null;
		int prescId = 0;
		if ("PKGPKG".equals(charge.getChargeHead())) {
			BillActivityChargeDAO bacdao = new BillActivityChargeDAO(con);
			BillActivityCharge bac = bacdao.getPkgActivity(charge.getChargeId(),"PKG");
			if (bac == null) {
				return false;
			}
			 prescId = Integer.parseInt(bac.getActivityId());
		} else {
			BasicDynaBean patPackContentConsumed = patPackageContentConsumed.findByKey("bill_charge_id",
					charge.getChargeId());
		    if (patPackContentConsumed == null) {
		        return false;
		    }
			prescId= (int) patPackContentConsumed.get("prescription_id");
		}

		packPrescribedBean = packagePrescribedDao.findByKey("prescription_id",prescId);
		if (null != packPrescribedBean) {
			BasicDynaBean patCustPackDetailsBean = patCustPackDetails.findByKey("patient_package_id",packPrescribedBean.get("pat_package_id"));
			if (null != patCustPackDetailsBean && patCustPackDetailsBean.get("is_customized_package") != null) {
				isCustomizedPackage=(boolean) patCustPackDetailsBean.get("is_customized_package");
			}
		}
		return isCustomizedPackage;
	}
}
