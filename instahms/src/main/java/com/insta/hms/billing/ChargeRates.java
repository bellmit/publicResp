package com.insta.hms.billing;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.adminmasters.services.MasterServicesDao;
import com.bob.hms.otmasters.opemaster.OperationMasterDAO;
import com.bob.hms.otmasters.theamaster.TheatreMasterDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
import com.insta.hms.master.AnaesthesiaTypeMaster.AnaesthesiaTypeChargesDAO;
import com.insta.hms.master.CommonChargesMaster.CommonChargesDAO;
import com.insta.hms.master.DietaryMaster.DietaryMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.EquipmentMaster.EquipmentChargeDAO;
import com.insta.hms.master.IPPreferences.IPPreferencesDAO;
import com.insta.hms.orders.OrderBO;
import com.insta.hms.outpatient.DoctorConsultationDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChargeRates {

	static Logger logger = LoggerFactory.getLogger(ChangeRatePlanBO.class);
	ChangeRatePlanBO chgRatePlanBO =null;
	private  String visitType = "", visitId = "", userName = "";
	private  int[] planIds = null;
	//private  int planId = 0;
	private  Boolean isInsurance = false;
	private  Map patientDetailsMap = new HashMap();
	private Connection con = null;
	
	private static final GenericDAO bedOperationSchedule = new GenericDAO("bed_operation_schedule");

	public ChargeRates(Connection con, String visitId, String visitType, int[] planIds, boolean isInsurance)  {
		this.con = con;
		this.visitId = visitId;
		this.visitType = visitType;
		this.planIds = planIds;
		this.isInsurance = isInsurance;
	}
	
	public void setPatientDetailsMap(String visitId)throws SQLException{
		this.patientDetailsMap = VisitDetailsDAO.getPatientVisitDetailsMap(visitId);
	}
	
	public Map<String , BigDecimal> getRegistrationChargeRate(String chargeHead, String orgId,
			String bedType, String itemId) throws SQLException {
		Map<String , BigDecimal> rateMap = new HashMap<String , BigDecimal>();
		
		BigDecimal outDrg = BigDecimal.ZERO;
		Boolean isRegCharge = checkIfValidRegistrationCharge(itemId);
		if(!isRegCharge)
			return rateMap;
		
		Boolean isRenewal = patientDetailsMap.get("reg_charge_accepted") != null
				&& patientDetailsMap.get("revisit").equals('Y');
		rateMap = OrderRatesBO.getRegistrationCharges(bedType, orgId, chargeHead,
				isRenewal, isInsurance, planIds, false, visitType, visitId, con, false);

		return rateMap;
	}
	
	public Map<String , BigDecimal> getDoctorCharge(String orgId, String bedType, String itemId, String chargeId, BigDecimal itemQty,
									String chargeType, boolean hasactivity) throws SQLException,Exception {
		
		Boolean isDoctorCharge = checkIfValidDoctorCharge(itemId);
		if(!isDoctorCharge)
			return null;
		BasicDynaBean consTypeBean = null;
		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>();
		// Doctor added from order
		if (hasactivity) {
			DynaBean dcBean = null;
			dcBean = new DoctorConsultationDAO().getDoctorConsultationCharge(con, chargeId);

			Integer operationRef = (Integer) dcBean.get("operation_ref");
			BasicDynaBean opMasterBean = null;
			if (null != dcBean.get("operation_ref")) {
				BasicDynaBean opBean = bedOperationSchedule.findByKey("prescribed_id", operationRef);
				String operId = (String) opBean.get("operation_name");
				opMasterBean = new OperationMasterDAO().getOperationChargeBean(operId, bedType,
						(String) orgId);
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
				rateMap = OrderRatesBO.getOtDoctorCharges(doctor, otDocRole, (String) patientDetailsMap.get("visit_type"),
						opMasterBean, itemQty, isInsurance, planIds, bedType, visitId, false);
			} else {
				rateMap = OrderRatesBO.getDoctorConsCharges(doctor, consTypeBean, visitType,
						OrgMasterDao.getOrgdetailsDynaBean(orgId), itemQty, isInsurance, planIds,
						bedType, visitId, false);
			}

		} else {

			consTypeBean = OrderBO.getConsultationTypeBean(Integer.parseInt(chargeType));
			BasicDynaBean doctor = DoctorMasterDAO.getDoctorCharges((String) itemId,
					orgId, (String) bedType);
			rateMap = OrderRatesBO.getDoctorConsCharges(doctor, consTypeBean, visitType, OrgMasterDao.getOrgdetailsDynaBean(orgId),
					itemQty, isInsurance, planIds, bedType, visitId, false);
		}
		return rateMap;
	}
	
	public Map<String , BigDecimal> getDiagCharge(String orgId, String bedType, String itemId, BigDecimal itemQty) throws SQLException,Exception {
		
		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>();
		boolean isValidTest = checkIfValidTest(itemId);
		if(!isValidTest)
			return null;
		BasicDynaBean rateBean = AddTestDAOImpl.getTestDetails(itemId, bedType, (String) orgId);
		rateMap.put("item_rate", (BigDecimal)rateBean.get("charge"));
		rateMap.put("discount", ((BigDecimal)rateBean.get("discount")).multiply(itemQty));
		return rateMap;
	}
	
	public Map<String , BigDecimal> getServiceCharge(String orgId, String bedType, String itemId, BigDecimal itemQty, String actDeptId) throws SQLException, Exception {
		
		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>();
		//Service charges always have a department, unless its an Other charge.
		if(actDeptId == null || actDeptId.isEmpty())
			return null;
		BasicDynaBean rateBean = new MasterServicesDao().getServiceChargeBean(itemId, bedType, orgId);
		
		rateMap.put("item_rate", (BigDecimal)rateBean.get("unit_charge"));
		rateMap.put("discount", ((BigDecimal)rateBean.get("discount")).multiply(itemQty));
		return rateMap;
	}
	
	public Map<String , BigDecimal> getDietaryCharge(String orgId, String bedType, String itemId, BigDecimal itemQty) throws SQLException, Exception {
		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>();
		boolean validMeal = checkIfValidDietCharge(itemId);
		if(!validMeal)
			return null;
		BasicDynaBean rateBean = new DietaryMasterDAO().getChargeForMeal(orgId, itemId, bedType);
		
		rateMap.put("item_rate", (BigDecimal)rateBean.get("charge"));
		rateMap.put("discount", ((BigDecimal)rateBean.get("discount")).multiply(itemQty));
		return rateMap;
	}
	
	/*
	 * This function updates the Equipment charges based on organization
	 * details. For the quatity calculation, in case the equipment used_till
	 * date is null, the quantity is fetched from the bill charge.
	 */
	public Map<String , BigDecimal> getEquipmentCharge(String orgId, String bedType, String itemId, String chargeHead, String chargeId, BigDecimal itemQty, 
			String actUnit, Timestamp fromDate, Timestamp toDate, boolean hasactivity) throws SQLException,Exception {
		
		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>();
		Boolean isOperation = chargeHead.equals("EQOPE");

		Boolean isValidEquipCharge = checkIfValidEquipCharge(itemId);
		if(!isValidEquipCharge) {
			return null;
		}
		BasicDynaBean equipDetails = new EquipmentChargeDAO().getEquipmentCharge(itemId, bedType, orgId);
		List<ChargeDTO> newChgLst = null;

		if (hasactivity) {
			BillActivityChargeDAO bacdao = new BillActivityChargeDAO(con);
			BillActivityCharge bac = bacdao.getActivity(chargeId);
			String prescId = bac.getActivityId();
			BasicDynaBean equipPres = new GenericDAO("equipment_prescribed").findByKey("prescribed_id",
					Integer.parseInt(prescId));

			Timestamp from = (Timestamp) equipPres.get("used_from");
			Timestamp to = (Timestamp) equipPres.get("used_till");
			String units = (String) equipPres.get("units");

			rateMap = OrderRatesBO.getEquipmentCharges(equipDetails, from, to, units, isOperation,
					itemQty, isInsurance, planIds, visitType, visitId, false);
		} else {
			rateMap = OrderRatesBO.getEquipmentCharges(equipDetails, fromDate, toDate, actUnit, isOperation, itemQty,
					isInsurance, planIds, visitType, visitId, false);
		}
		return rateMap;
	}
	
	/*
	 * This function updates the Other Services ordered based on the
	 * organization details.
	 */
	public Map<String , BigDecimal> getOtherCharge(String orgId, String bedType, String itemId, BigDecimal itemQty) throws SQLException, Exception {
		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>();
		BasicDynaBean rateBean = new CommonChargesDAO().getCommonCharge(itemId);
		
		rateMap.put("item_rate", (BigDecimal)rateBean.get("charge"));
		rateMap.put("discount", BigDecimal.ZERO);
		return rateMap;
	}
	
	/*
	 * This function updates the Packages ordered based on the organization
	 * details.
	 */
	public Map<String , BigDecimal> getPackageCharge(String orgId, String bedType, String itemId, BigDecimal itemQty) throws SQLException, Exception {
		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>();
		boolean validPackage = checkIfValidPackage(itemId);
		if(!validPackage)
			return null;
		BasicDynaBean rateBean = PackageDAO.getPackageDetails(Integer.parseInt(itemId), orgId, bedType);
		
		rateMap.put("item_rate", (BigDecimal)rateBean.get("charge"));
		rateMap.put("discount", ((BigDecimal)rateBean.get("discount")).multiply(itemQty));
		return rateMap;
		
	}
	
	/*
	 * This function updates the bed charges based on the organization details
	 */
	public Map<String , BigDecimal> getBedCharge(String orgId, String bedType, String itemId, BigDecimal itemQty,
				String chargeGroup, String chargeHead, String chargeId, boolean hasactivity) throws SQLException, ParseException, Exception {

		boolean isBedCharge = checkIfValidBedCharge(itemId);
		
		BasicDynaBean bedBean = BedMasterDAO.getBedDetailsBean(itemId);
		
		String bedTypeForBedCharges = bedType;
		
		if(null != bedBean && null != bedBean.get("bed_type"))
			bedTypeForBedCharges = (String)bedBean.get("bed_type");

		if(!isBedCharge)
			return null;

		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>(); 
		String result = null;
		boolean hasAdmission = false;
		String dayCare = "N";
		List<ChargeDTO> newChgLst = null;

		if (new GenericDAO("admission").findByKey("patient_id", visitId) != null)
			hasAdmission = true;

/*		if (hasactivity) {
			BasicDynaBean admBean = new GenericDAO("admission").getBean();
			GenericDAO ipBedDAO = new GenericDAO("ip_bed_details");
			BillActivityChargeDAO bacdao = new BillActivityChargeDAO(con);
			BillActivityCharge bac = bacdao.getActivity(chargeId);
			BasicDynaBean ipBedBean = ipBedDAO.findByKey("admit_id", Integer.parseInt(bac.getActivityId()));

			if (hasAdmission) {
				admBean = new GenericDAO("admission").findByKey("patient_id", visitId);
				dayCare = (String) admBean.get("daycare_status");
			}

			OrderBO orderbo = new OrderBO();
			orderbo.setUserName(userName);
			orderbo.setBillInfo(con, visitId, cdto.getBillNo(), new BillBO().getBillDetails(cdto.getBillNo()).getBill().getIs_tpa(),
					userName);
			result= orderbo.recalculateBedCharges(con, visitId)== null? "success": null;
		} else {
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
			result = updateNewChargeValue(newChgLst, cdto, refChargeList);

		}
		return result == null || !result.equals("success") ? false : true;	*/
		
		BasicDynaBean bedRates = null;
		BigDecimal rate = BigDecimal.ZERO;
		BigDecimal discount = BigDecimal.ZERO;
		if (chargeGroup.equals("ICU")) {
			bedRates = new BedMasterDAO().getIcuBedCharges(Integer.parseInt(itemId), orgId, bedTypeForBedCharges);
			if(bedRates == null)
				return null;
			if(chargeHead.equals("BICU")){
				rate = (BigDecimal)bedRates.get("bed_charge");
				discount = (BigDecimal)bedRates.get("bed_charge_discount");
			} else if(chargeHead.equals("PCICU")) {
				rate = (BigDecimal)bedRates.get("maintainance_charge");
				discount = (BigDecimal)bedRates.get("maintainance_charge_discount");
			} else if(chargeHead.equals("DDICU")) {
				rate = (BigDecimal)bedRates.get("duty_charge");
				discount = (BigDecimal)bedRates.get("duty_charge_discount");
			} else if(chargeHead.equals("NCICU")) {
				rate = (BigDecimal)bedRates.get("nursing_charge");
				discount = (BigDecimal)bedRates.get("nursing_charge_discount");
			}
		} else {
			bedRates = new BedMasterDAO().getNormalBedChargesBean(bedTypeForBedCharges, orgId);
			if(bedRates == null)
				return null;
			if(chargeHead.equals("PCBED")){
				rate = (BigDecimal)bedRates.get("maintainance_charge");
				discount = (BigDecimal)bedRates.get("maintainance_charge_discount");
			} else if(chargeHead.equals("BYBED")) {
				BasicDynaBean prefs = new IPPreferencesDAO().getPreferences();
				
				if (prefs.get("bystander_bed_charges_applicable_on").equals("W")) {
					rate = ((BigDecimal)bedRates.get("bed_charge")).
											add((BigDecimal) bedRates.get("nursing_charge")).
											add((BigDecimal) bedRates.get("duty_charge")).
											add((BigDecimal) bedRates.get("maintainance_charge"));

					discount = ((BigDecimal)bedRates.get("bed_charge_discount")).
											add((BigDecimal) bedRates.get("nursing_charge_discount")).
											add((BigDecimal) bedRates.get("duty_charge_discount")).
											add((BigDecimal) bedRates.get("maintainance_charge_discount"));
				} else {
					rate = (BigDecimal)bedRates.get("bed_charge");
					discount = (BigDecimal)bedRates.get("bed_charge_discount");
				}
			} else if(chargeHead.equals("BBED")) {
				rate = (BigDecimal)bedRates.get("bed_charge");
				discount = (BigDecimal)bedRates.get("bed_charge_discount");
			} else if(chargeHead.equals("NCBED")) {
				rate = (BigDecimal)bedRates.get("nursing_charge");
				discount = (BigDecimal)bedRates.get("nursing_charge_discount");
			} else if(chargeHead.equals("DDBED")) {
				rate = (BigDecimal)bedRates.get("duty_charge");
				discount = (BigDecimal)bedRates.get("duty_charge_discount");
			}
		}
		
		rateMap.put("item_rate", rate);
		rateMap.put("discount", discount.multiply(itemQty));
		return rateMap;
	}
	
	public Map<String , BigDecimal> getSurgeryCharge(String orgId, String bedType, String itemId, BigDecimal itemQty, String chargeId,
			String chargeGroup, String chargeHead, String actUnit, Timestamp from, Timestamp to, String opId) throws SQLException, ParseException, Exception {
		
		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>();
		
		String units = actUnit != null && (actUnit).equals("Days") ? "D" : "H";
		
		BasicDynaBean operationBean = new OperationMasterDAO().getOperationChargeBean(opId, bedType, orgId);
		
		if(operationBean == null) {
			BillActivityChargeDAO bacdao = new BillActivityChargeDAO(con);
			BillActivityCharge bac = bacdao.getActivity(chargeId);
			
			if(bac.getActivityCode().equals("OTC"))
				return null;
			BasicDynaBean bedOpBean = bedOperationSchedule.findByKey("prescribed_id", Integer.parseInt(bac.getActivityId()));
			if(bedOpBean != null)
				operationBean = new OperationMasterDAO().getOperationChargeBean((String) bedOpBean.get("operation_name"), bedType, orgId);
		}
		
		if (chargeHead.equals("TCOPE")) {
			BasicDynaBean theaterBean = new TheatreMasterDAO().getTheatreChargeDetails(itemId, bedType, orgId);
			rateMap = OrderRatesBO.getOperationChargesForTheaterTCOPE(theaterBean, from, to, units, itemQty, false);
		} else if (chargeHead.equals("SUOPE")) {
			BasicDynaBean surgeonDocBean = DoctorMasterDAO.getOTDoctorChargesBean(itemId, bedType, orgId);
			rateMap = OrderRatesBO.getSurgeonChargesSUOPE(operationBean, surgeonDocBean);
		} else if (chargeHead.equals("ANAOPE")) {
			BasicDynaBean anaDocBean = DoctorMasterDAO.getOTDoctorChargesBean(itemId, bedType, orgId);
			rateMap = OrderRatesBO.getAnestiatistChargesANAOPE(operationBean, anaDocBean);
		} else if (chargeHead.equals("ANATOPE")) {
			BasicDynaBean anaTypeBean = new AnaesthesiaTypeChargesDAO().getAnasthesiaTypeCharge(itemId, bedType, orgId);
			rateMap = OrderRatesBO.getAnaesthesiaTypeChargesANATOPE(anaTypeBean, from, to);
		} else if (chargeHead.equals("SACOPE")) {
			rateMap = OrderRatesBO.getSurgicalAssistanceChargeSACOPE(operationBean);
		} else if (chargeHead.equals("COSOPE")) {
			DynaBean dcBean = null;
			dcBean = new DoctorConsultationDAO().getDoctorConsultationCharge(con, chargeId);
			BasicDynaBean doctor = DoctorMasterDAO.getDoctorCharges((String) dcBean.get("doctor_name"),
					orgId, (String) bedType);
			rateMap = OrderRatesBO.getCoOperateSurgeonFeeCOSOPE(operationBean , doctor);
		} else if (chargeHead.equals("ASUOPE")) {
			DynaBean dcBean = null;
			dcBean = new DoctorConsultationDAO().getDoctorConsultationCharge(con, chargeId);
			BasicDynaBean doctor = DoctorMasterDAO.getDoctorCharges((String) dcBean.get("doctor_name"),
					orgId, (String) bedType);
			rateMap = OrderRatesBO.getAsstSurgeonFeeASUOPE(operationBean , doctor);
		} else if (chargeHead.equals("AANOPE")) {
			DynaBean dcBean = null;
			dcBean = new DoctorConsultationDAO().getDoctorConsultationCharge(con, chargeId);
			BasicDynaBean doctor = DoctorMasterDAO.getDoctorCharges((String) dcBean.get("doctor_name"),
					orgId, (String) bedType);
			rateMap = OrderRatesBO.getAsstAnaesthetistFeeAANOPE(operationBean , doctor);
		}
		return rateMap;
	}
	
	//----------------------------supporintg methods------------------------------
	
	
	public static boolean checkIfValidRegistrationCharge(String actDescriptionId){
		return actDescriptionId.equals("GREG") || actDescriptionId.equals("IPREG")
				|| actDescriptionId.equals("OPREG") || actDescriptionId.equals("MLREG") || actDescriptionId.equals("EMREG");
	}
	
	public static boolean checkIfValidDoctorCharge(String actDescriptionId) throws SQLException{
		BasicDynaBean docBean = DoctorMasterDAO.getDoctorById(actDescriptionId);
		return docBean != null;
	}
	
	public static boolean checkIfValidTest(String actDeptId){
		return actDeptId !=null && !actDeptId.isEmpty();
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
	
	public static boolean checkIfValidEquipCharge(String actDescriptionId) throws SQLException {
		if(actDescriptionId == null || actDescriptionId.equals(""))
			return false;
		return new GenericDAO("equipment_master").findByKey("eq_id",actDescriptionId) != null;
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
	
	public static boolean checkIfValidBedCharge(String actDescriptionId) throws SQLException, IOException {
		BasicDynaBean bedBean = BedMasterDAO.getBedDetailsBean(actDescriptionId);
		return bedBean != null;
	}

}
