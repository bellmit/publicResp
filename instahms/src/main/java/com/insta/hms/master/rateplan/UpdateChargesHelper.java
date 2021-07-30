package com.insta.hms.master.rateplan;

import com.bob.hms.otmasters.theamaster.TheatreMasterDAO;
import com.insta.hms.adminmaster.packagemaster.PackageChargeDAO;
import com.insta.hms.diagnosticsmasters.addtest.TestChargesDAO;
import com.insta.hms.master.AnaesthesiaTypeMaster.AnaesthesiaTypeChargesDAO;
import com.insta.hms.master.ConsultationCharges.ConsultationChargesDAO;
import com.insta.hms.master.DoctorMaster.DoctorChargeDAO;
import com.insta.hms.master.DynaPackage.DynaPackageCategoryLimitsDAO;
import com.insta.hms.master.DynaPackage.DynaPackageChargesDAO;
import com.insta.hms.master.EquipmentMaster.EquipmentChargeDAO;
import com.insta.hms.master.OperationMaster.OperationChargeDAO;
import com.insta.hms.master.RegistrationCharges.RegistrationChargesDAO;
import com.insta.hms.master.ServiceMaster.ServiceChargeDAO;

import java.sql.Connection;
import java.sql.SQLException;

public class UpdateChargesHelper {
	

	public static boolean updateChargesForDerivedRatePlans(String orgId,String varianceType,
			Double varianceValue,Double varianceBy,String baseOrgId,
			Double nearstRoundOfValue,String userName, String orgName,String category,boolean upload) throws SQLException, Exception {
		boolean success = false;

		if(category.equals("services")) {
			success = new ServiceChargeDAO().updateServiceChargesForDerivedRatePlans(orgId,varianceType,varianceValue,varianceBy,
					baseOrgId,nearstRoundOfValue,userName,orgName,upload);

		}else if(category.equals("operations")) {
			success = new OperationChargeDAO().updateOperationChargesForDerivedRatePlans(orgId,varianceType,varianceValue,varianceBy,
					baseOrgId,nearstRoundOfValue,userName,orgName,upload);

		}else if(category.equals("tests")) {
			success = new TestChargesDAO().updateTestChargesForDerivedRatePlans(orgId,varianceType,varianceValue,varianceBy,
					baseOrgId,nearstRoundOfValue,userName,orgName,upload);

		}else if(category.equals("consultation")){
			success = new ConsultationChargesDAO().updateConsultationChargesForDerivedRatePlans(orgId,varianceType,varianceValue,
					varianceBy,baseOrgId,nearstRoundOfValue,userName,orgName,upload);

		}else if(category.equals("anaesthesia")) {
			success = new AnaesthesiaTypeChargesDAO().updateAnaesthesiaChargesForDerivedRatePlans(orgId, varianceType, varianceValue,
					varianceBy,baseOrgId, nearstRoundOfValue,userName,orgName,upload);

		}else if(category.equals("registration")) {
			success = new RegistrationChargesDAO().updateRegChargesForDerivedRatePlans(orgId, varianceType, varianceValue, varianceBy,
					baseOrgId, nearstRoundOfValue,userName,orgName,upload);

		}else if(category.equals("equipment")) {
			success = new EquipmentChargeDAO().updateEquipChargesForDerivedRatePlans(orgId, varianceType, varianceValue, varianceBy,
					baseOrgId, nearstRoundOfValue,userName,orgName,upload);

		}else if(category.equals("theatre")) {
			success = new TheatreMasterDAO().updateTheatreChargesForDerivedRatePlans(orgId, varianceType, varianceValue, varianceBy,
					baseOrgId, nearstRoundOfValue);

		}else if(category.equals("doctorOPcharges")) {
			success = new DoctorChargeDAO().updateDrOPConschargesForDerivedRatePlans(orgId, varianceType, varianceValue, varianceBy,
					baseOrgId, nearstRoundOfValue,userName,orgName,upload);

		}else if(category.equals("doctorIPCharges")) {
			success = new DoctorChargeDAO().updateConstChargesForDerivedRatePlans(orgId, varianceType, varianceValue, varianceBy,
					baseOrgId, nearstRoundOfValue,userName,orgName,upload);

		}else if(category.equals("dynaPackages")) {
			success = new DynaPackageChargesDAO().updateDynaPackChargesForDerivedRatePlans(orgId, varianceType, varianceValue, varianceBy,
					baseOrgId, nearstRoundOfValue,userName,orgName,upload);

		}else if(category.equals("dynaPackCategoryLimits")){
			success = new DynaPackageCategoryLimitsDAO().updateLimitsForDerivedRatePlans(orgId, varianceType, varianceValue, varianceBy,
					baseOrgId, nearstRoundOfValue,userName,orgName,upload);

		}
		return success;
	}

	public static boolean updateChargesBasedOnNewRateSheet(Connection con, String ratePlanId,Double varianceBy,
			String rateSheetId,Double nearstRoundOfValue,String categoryId,String category)throws SQLException,Exception {

		boolean success = false;

		if(category.equals("anesthesia")) {
			success = AnaesthesiaTypeChargesDAO.updateChargesBasedOnNewRateSheet(con, ratePlanId, varianceBy,
					rateSheetId, nearstRoundOfValue,categoryId);

		}else if(category.equals("consultation")) {
			success = ConsultationChargesDAO.updateChargesBasedOnNewRateSheet(con, ratePlanId, varianceBy,
					rateSheetId, nearstRoundOfValue,categoryId);

		}else if(category.equals("diagnostics")) {
			success = TestChargesDAO.updateChargesBasedOnNewRateSheet(con, ratePlanId, varianceBy,
					rateSheetId, nearstRoundOfValue,categoryId);

		}else if(category.equals("dynapackages")) {
			success = DynaPackageChargesDAO.updateChargesBasedOnNewRateSheet(con, ratePlanId, varianceBy,
					rateSheetId, nearstRoundOfValue,categoryId);
			success = DynaPackageCategoryLimitsDAO.updateChargesBasedOnNewRateSheet(con, ratePlanId, varianceBy,
					rateSheetId, nearstRoundOfValue,categoryId);

		}else if(category.equals("operations")) {
			success = OperationChargeDAO.updateChargesBasedOnNewRateSheet(con, ratePlanId, varianceBy,
					rateSheetId, nearstRoundOfValue,categoryId);

		}else if(category.equals("packages")) {
			success = PackageChargeDAO.updateChargesBasedOnNewRateSheet(con, ratePlanId, varianceBy,
					rateSheetId, nearstRoundOfValue,categoryId);

		}else if(category.equals("services")) {
			success = ServiceChargeDAO.updateChargesBasedOnNewRateSheet(con, ratePlanId, varianceBy,
					rateSheetId, nearstRoundOfValue,categoryId);

		}
		return success;
	}
	
}