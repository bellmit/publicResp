package com.insta.hms.core.billing;

import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitRepository;
import com.insta.hms.core.clinical.order.packageitems.PackageOrderItemRepository;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.core.patient.registration.PatientRegistrationService;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.ordersets.PackOrgDetailsModel;
import com.insta.hms.mdm.ordersets.PackageChargesJpaRepository;
import com.insta.hms.mdm.ordersets.PackageChargesModel;
import com.insta.hms.mdm.ordersets.PackageContentCharges;
import com.insta.hms.mdm.ordersets.PackageContentChargesJpaRepository;
import com.insta.hms.mdm.ordersets.PackageOrgDetailsService;
import com.insta.hms.mdm.packages.PatientCustomisedPackageDetailsRepository;
import com.insta.hms.mdm.packages.PatientPackageContentConsumedService;
import com.insta.hms.mdm.registrationcharges.RegistrationChargesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ChangeItemRateService {

  @LazyAutowired
  private BillService billService;

  @LazyAutowired
  private BillChargeHelper billChargeHelper;

  @LazyAutowired
  private BillChargeService billChargeService;

  @LazyAutowired
  private DoctorService doctorService;

  @LazyAutowired
  private PatientRegistrationService patientRegistrationService;

  @LazyAutowired
  private RegistrationChargesService registrationChargeService;

  @LazyAutowired
  private MultiVisitRepository multiVisitRepo;

  @LazyAutowired
  private BillActivityChargeService billActivityChargeService;

  @LazyAutowired
  private DoctorConsultationService doctorConsService;

  @LazyAutowired
  private ConsultationTypesService consultationTypeService;

  @LazyAutowired
  private PackageContentChargesJpaRepository packageContentChargesJpaRepository;

  @LazyAutowired
  private PackageOrgDetailsService packageOrgDetailsService;

  @LazyAutowired
  private PackageChargesJpaRepository packageChargesJpaRepository;

  @LazyAutowired
  private PatientPackageContentConsumedService patientPackageContentConsumedService;

  @LazyAutowired
  private PackageOrderItemRepository packageOrderItemRepository;

  @LazyAutowired
  private PatientCustomisedPackageDetailsRepository patientCustomisedPackageDetailsRepository;

  /**
   * @param visitBean
   * @param billNos
   * @param ratePlanId
   * @param bedType
   */
  public void changeBillChargeItemRates(BasicDynaBean visitBean, String billNos, String ratePlanId,
      String bedType, List<BasicDynaBean> multiVisitPkgBills) {
    if (null != billNos) {
      String query = billChargeHelper.UPDATE_TEST_CHARGES;
      query = query.replaceAll("#", billNos);
      billChargeService.updateCharges(query, ratePlanId, bedType);

      query = billChargeHelper.UPDATE_SERVICE_CHARGES;
      query = query.replaceAll("#", billNos);
      billChargeService.updateCharges(query, ratePlanId, bedType);

      query = billChargeHelper.GET_PACKAGE_CONTENT_CHARGES;
      query = query.replaceAll("#", billNos);
      try {
        if (null == multiVisitPkgBills ||  multiVisitPkgBills.isEmpty()) {
         updatePackageContentCharges(query, ratePlanId, bedType);
        }
	   } catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	   }

      query = billChargeHelper.GET_CONSULTATION_CHARGES;
      query = query.replaceAll("#", billNos);
      updateDoctorConsultationCharge(query, ratePlanId, bedType);

      query = billChargeHelper.GET_REGISTRATION_CHARGES;
      query = query.replaceAll("#", billNos);
      updateRegistrationCharges(query, visitBean, ratePlanId, bedType);

      query = billChargeHelper.UPDATE_OTHER_CHARGES;
      query = query.replaceAll("#", billNos);
      billChargeService.updateCharges(query);
      
      query = billChargeHelper.GET_EQUIPMENT_CHARGES;
      query = query.replaceAll("#", billNos);
      updateEquipmentCharges(query, visitBean, ratePlanId, bedType);
      
      query = billChargeHelper.UPDATE_DYNA_PACKAGE_CHARGES;
      query = query.replaceAll("#", billNos);
      billChargeService.updateCharges(query, ratePlanId, bedType);
      
      // To update discount authorizer as "Rate Plan Discount" for items where discount amount exists.
      billChargeService.updateDiscountAuthAsRatePlanDiscount(billNos);

    }

  }

  private void updateEquipmentCharges(String query, BasicDynaBean visitBean, String ratePlanId,
      String bedType) {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param query
   * @param visitBean
   * @param ratePlanId
   * @param bedType
   * @return
   */
  private Boolean updateRegistrationCharges(String query, BasicDynaBean visitBean,
      String ratePlanId, String bedType) {
    // TODO Auto-generated method stub
    List<BasicDynaBean> regBillCharges = billChargeService.getCharges(query);
    Boolean isRenewal = visitBean.get("reg_charge_accepted") != null
        && ((String) visitBean.get("revisit")).equals("Y");
    BasicDynaBean regChargeBean = registrationChargeService.getRegistrationCharges(bedType,
        ratePlanId);

    String chargeType = null;
    Boolean success = true;
    for (BasicDynaBean charge : regBillCharges) {
      String chargeHead = (String) charge.get("charge_head");
      String chargeId = (String) charge.get("charge_id");

      if (chargeHead.equals("GREG")) {
        if (isRenewal)
          chargeType = "reg_renewal_charge";
        else
          chargeType = "gen_reg_charge";
      } else if (chargeHead.equals("OPREG"))
        chargeType = "op_reg_charge";
      else if (chargeHead.equals("IPREG"))
        chargeType = "ip_reg_charge";
      else if (chargeHead.equals("MLREG"))
        chargeType = "ip_mlccharge";
      else if (chargeHead.equals("EMREG"))
        chargeType = "mrcharge";

      BigDecimal itemRate = (BigDecimal) regChargeBean.get(chargeType);
      BigDecimal itemDiscount = (BigDecimal) regChargeBean.get(chargeType + "_discount");

      BigDecimal quantity = (BigDecimal) charge.get("act_quantity");

      BigDecimal amount = (itemRate.multiply(quantity)).subtract(itemDiscount);

      charge.set("amount", amount);
      charge.set("discount", itemDiscount);
      charge.set("act_rate", itemRate);

      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("charge_id", chargeId);
      success = success && billChargeService.update(charge, keys) >= 0;
    }
    return success;

  }

  /**
   * @param query
   * @param ratePlanId
   * @param bedType
   * @return
   */
  private Boolean updateDoctorConsultationCharge(String query, String ratePlanId, String bedType) {
    // TODO Auto-generated method stub
    Boolean success = true;
    List<BasicDynaBean> consultationCharges = billChargeService.getCharges(query);
    for (BasicDynaBean charge : consultationCharges) {
      String doctorName = (String) charge.get("doctor_name");
      int consultationTypeId = (Integer) charge.get("consultation_type_id");
      String chargeId = (String) charge.get("charge_id");

      BigDecimal qunatity = (BigDecimal) charge.get("act_quantity");

      BasicDynaBean doctorChargeBean = doctorService.getDoctorCharges(doctorName, ratePlanId,
          bedType);
      BasicDynaBean consultationChargeBean = doctorService.getConsultationCharges(
          consultationTypeId, bedType, ratePlanId);

      BasicDynaBean consultationTypeBean = consultationTypeService.findByKey(consultationTypeId);

      String doctorChargeType = (String) consultationTypeBean.get("doctor_charge_type");

      BigDecimal doctorRate = (BigDecimal) doctorChargeBean.get(doctorChargeType);
      BigDecimal doctorDiscount = null != doctorChargeBean.get(doctorChargeType + "_discount") ? 
          (BigDecimal) doctorChargeBean.get(doctorChargeType + "_discount") : BigDecimal.ZERO;

      BigDecimal consTypeRate = (BigDecimal) consultationChargeBean.get("charge");
      BigDecimal consTypeDiscount = null != consultationChargeBean.get("discount") ? 
          (BigDecimal) consultationChargeBean.get("discount") : BigDecimal.ZERO;
          
      String ratePlanItemCode = (String) consultationChargeBean.get("item_code");
      String codeType = (String) consultationChargeBean.get("code_type");

      BigDecimal rate = doctorRate.add(consTypeRate);
      BigDecimal discount = doctorDiscount.add(consTypeDiscount);

      BigDecimal chgAmount = (rate.multiply(qunatity)).subtract(discount);

      BasicDynaBean chargeBean = billChargeService.findByKey("charge_id", chargeId);
      chargeBean.set("amount", chgAmount);
      chargeBean.set("discount", discount);
      chargeBean.set("act_rate", rate);
      chargeBean.set("act_rate_plan_item_code", ratePlanItemCode);
      chargeBean.set("code_type", codeType);

      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("charge_id", chargeId);
      success = success && billChargeService.update(chargeBean, keys) >= 0;

    }
    return success;
  }

  /**
   * @param visitId
   * @return
   */
  public String getBillNos(String visitId) {
    List<BasicDynaBean> billList = new ArrayList<BasicDynaBean>();
    billList = getBillList(visitId, "open_bills");
    String billNos = null;

    for (BasicDynaBean bean : billList) {
      String billNo = (String) bean.get("bill_no");
      Boolean multiVisitPackageBill = billService.isMultiVisitPackageBill(billNo);

      if (!multiVisitPackageBill)
        billNos = billNos == null ? "'".concat(billNo).concat("'") : billNos.concat(",".concat("'")
            .concat(billNo).concat("'"));
    }
    return billNos;
  }

  /**
   * @param visitId
   * @param billsRequired
   * @return
   */
  public List<BasicDynaBean> getBillList(String visitId, String billsRequired) {
    // TODO Auto-generated method stub
    List<BasicDynaBean> billList = new ArrayList<BasicDynaBean>();
    if (billsRequired.equals("open_bills")) {
      billList = billService.getOpenTpaBills(visitId);
    } else {
      billList = billService.getAllTpaBills(visitId);
    }
    return billList;
  }

  /**
   * @param billsRequired
   * @param visitId
   * @return
   */
  public List<BasicDynaBean> getMultiVisitPackageTPABills(String billsRequired, String visitId) {
    // TODO Auto-generated method stub
    List<BasicDynaBean> multiVisitPkgBills = new ArrayList<BasicDynaBean>();
    if (billsRequired.equals("open_bills")) {
      multiVisitPkgBills = billService.getOpenMultiVisitPkgTPABills(visitId);
    } else {
      multiVisitPkgBills = billService.getAllMultiVisitPkgTPABills(visitId);
    }
    return multiVisitPkgBills;
  }

  /**
   * @param multiVisitPkgBills
   * @return
   */
  public String getMultiVisitPackageBillNos(List<BasicDynaBean> multiVisitPkgBills) {
    String multiVisitPkgBillNos = null;

    Set<String> billNos = new HashSet<>();
    for (BasicDynaBean billBean : multiVisitPkgBills) {
      billNos.add((String)billBean.get("bill_no"));
    }

    for (String billNo : billNos) {
      multiVisitPkgBillNos = multiVisitPkgBillNos == null ? "'".concat(billNo).concat("'")
          : multiVisitPkgBillNos.concat(",".concat("'").concat(billNo).concat("'"));
    }
    return multiVisitPkgBillNos;
  }

  /**
   * @param multiVisitPkgBills
   * @param orgId
   * @param bedType
   */
  public void updateMultiVisitPackageCharges(List<BasicDynaBean> multiVisitPkgBills, String orgId,
      String bedType) {
    // TODO Auto-generated method stub
    String multiVisitPkgBillNos = getMultiVisitPackageBillNos(multiVisitPkgBills);
    @SuppressWarnings("unchecked")
    Map<String, BasicDynaBean> multiVisitPkgMap = ConversionUtils.listBeanToMapBean(
        multiVisitPkgBills, "bill_no");

    List<BasicDynaBean> multiVisitPkgCharges = billChargeService
        .getAllBillCharges(multiVisitPkgBillNos);

    if (null == multiVisitPkgCharges) {
      return;
    }

    for (BasicDynaBean chargeBean : multiVisitPkgCharges) {
      String chargeGrp = (String) chargeBean.get("charge_group");
      int consultationTypeId = null != chargeBean.get("consultation_type_id") ? (Integer) chargeBean
          .get("consultation_type_id") : 0;
      String actDescriptionId = null != chargeBean.get("act_description_id") ? (String) chargeBean
          .get("act_description_id") : null;
      String chargeHead = (String) chargeBean.get("charge_head");
      BigDecimal qty = (BigDecimal) chargeBean.get("act_quantity");

      String billNo = (String) chargeBean.get("bill_no");
      BasicDynaBean pkgBean = multiVisitPkgMap.get(billNo);
      Integer packageId = (Integer) pkgBean.get("pack_id");

      String itemType = null;
      String itemId = null;
      BasicDynaBean itemBean = null;
      String chargeId = (String) chargeBean.get("charge_id");

      if (chargeGrp.equals("DOC")) {
        itemType = "doctors";
        itemId = new Integer(consultationTypeId).toString();
        BasicDynaBean chargeActivityBean = billActivityChargeService.getChargeActivities(chargeId)
            .get(0);
        int consultationId = Integer.parseInt((String) chargeActivityBean.get("activity_id"));
        itemBean = doctorConsService.findByKey(consultationId);
      } else if (chargeGrp.equals("DIA")) {
        itemType = "test";
        itemId = actDescriptionId;
      } else if (chargeGrp.equals("SNP") && chargeHead.equals("SERSNP")) {
        itemType = "service";
        itemId = actDescriptionId;
      } else if (chargeGrp.equals("OTC")) {
        itemType = "other";
        itemId = actDescriptionId;
      } else if (chargeGrp.equals("REG")) {
        continue;
      }
      BigDecimal amount = multiVisitRepo.getMultiVisitPackageItemCharge(packageId, itemBean,
          itemId, bedType, orgId, itemType, qty);
      BigDecimal rate = amount.divide(qty);
      chargeBean.set("act_rate", rate);
      chargeBean.set("amount", amount);

      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("charge_id", chargeId);
      billChargeService.update(chargeBean, keys);

    }
  }

  private static final String GET_MULTIVISIT_PKG_CASH_BILLS = "SELECT * FROM multivisit_bills_view mbv "
      + " JOIN bill b using(bill_no) "
      + " WHERE b.visit_id = ? AND b.status != 'X' AND b.bill_rate_plan_id != ? AND b.is_tpa = false ";

  /**
   * @param visitId
   * @param nonInsRatePlan
   * @return
   */
  public List<BasicDynaBean> getMultiVisitPackageCashBills(String visitId, String nonInsRatePlan) {
    // TODO Auto-generated method stub
    return DatabaseHelper.queryToDynaList(GET_MULTIVISIT_PKG_CASH_BILLS, new Object[] { visitId,
        nonInsRatePlan });
  }

	/**
	 * @param query
	 * @param ratePlanId
	 * @param bedType
	 * @return
	 * @throws SQLException
	 */
	private Boolean updatePackageContentCharges(String query, String ratePlanId, String bedType) throws SQLException {
		// TODO Auto-generated method stub
		List<BasicDynaBean> packBillCharges = billChargeService.getCharges(query);
		Boolean success = true;
		for (BasicDynaBean charge : packBillCharges) {
			List<BasicDynaBean> packComList = null;
			int prescId = 0;
			Integer packageId = (Integer) null;
			boolean isCustomizedPackage = false;

			String chargeId = (String) charge.get("charge_id");
			BigDecimal quantity = (BigDecimal) charge.get("act_quantity");

			if ("PKGPKG".equals(charge.get("charge_head"))) {
				packageId = Integer.parseInt((String) charge.get("act_description_id"));
				Map<String,Object> keys = new HashMap<String, Object>();
			    keys.put("charge_id", chargeId);
			    keys.put("activity_code", "PKG");
			    BasicDynaBean bacsBean  = billActivityChargeService.findByKey(keys);
			    if (bacsBean != null) {
			       prescId = Integer.parseInt((String) bacsBean.get("activity_id"));
			    }
			    packComList = (List<BasicDynaBean>) new GenericDAO("bill_charge").findAllByKey("charge_ref", chargeId);
			} else {
				//Package Contents
				packageId = (Integer) charge.get("package_id");
				BasicDynaBean patPackContConsBean = patientPackageContentConsumedService
						.findByKey("bill_charge_id",chargeId);
				if (patPackContConsBean != null) {
					prescId = (int) patPackContConsBean.get("prescription_id");
				}
			}
			BasicDynaBean packageOrderBean =packageOrderItemRepository.findByKey("prescription_id", prescId);
			if (packageOrderBean != null ) {
				BasicDynaBean patCustPackDetailsBean = patientCustomisedPackageDetailsRepository
						.findByKey("patient_package_id",packageOrderBean.get("pat_package_id"));
				if (null != patCustPackDetailsBean && patCustPackDetailsBean.get("is_customized_package") != null) {
					isCustomizedPackage=(boolean) patCustPackDetailsBean.get("is_customized_package");
				}
			}

			//If Package is Customized Package then ignore
			if (isCustomizedPackage) {
				continue;
			}

			//If the package has no rate plan applicability then ignore
			PackOrgDetailsModel packOrgDetails = packageOrgDetailsService
					.findByPackOrgDetailsIdSequence(packageId,ratePlanId);
			if (packOrgDetails == null) {
				continue;
			}
			BigDecimal rate = BigDecimal.ZERO;
			BigDecimal chgAmount = BigDecimal.ZERO;
			BigDecimal discount = BigDecimal.ZERO;
			BigDecimal chargeAmt = BigDecimal.ZERO;
			BigDecimal chgDiscount = BigDecimal.ZERO;

			String ratePlanItemCode = (String) packOrgDetails.getItemCode() != null ? (String) packOrgDetails.getItemCode() : null;
			String codeType = (String) packOrgDetails.getCodeType() != null ? (String) packOrgDetails.getCodeType() : null;

			if ("PKGPKG".equals(charge.get("charge_head")) && packComList.size() == 0) {
				PackageChargesModel packageChargeBean = packageChargesJpaRepository
						.findByPackageIdAndOrgIdAndBedType(packageId, ratePlanId, bedType);
				rate = packageChargeBean.getCharge();
				discount = packageChargeBean.getDiscount();
				chgAmount = (rate.multiply(quantity)).subtract(discount);
			} else {
				PackageChargesModel packageBean = packageChargesJpaRepository
						.findByPackageIdAndOrgIdAndBedType(packageId, ratePlanId, bedType);
				BigDecimal packageCharge = packageBean.getCharge();
				BigDecimal packageDiscount = packageBean.getDiscount();
				if ("PKGPKG".equals(charge.get("charge_head"))) {
					if (((BigDecimal) charge.get("amount")).compareTo(BigDecimal.ZERO) != 0  ) {
						 BasicDynaBean pkgInv = PackageDAO.getPackContChargesForInventory(packageId, ratePlanId, bedType);
						 rate = (BigDecimal) pkgInv.get("charge");
						 chargeAmt = (rate).divide(quantity, 2,RoundingMode.HALF_UP);
						 chgDiscount = discountSplit(chargeAmt, packageCharge, packageDiscount);
						 discount = discount.add(chgDiscount.multiply(quantity));
						 chgAmount = (rate.multiply(quantity)).subtract(discount);
					 }
				} else {
					int packContentId = Integer.parseInt((String) charge.get("act_description_id"));
					PackageContentCharges packageChargeBean = packageContentChargesJpaRepository
							.findByPackageContentIdAndBedTypeAndOrgId(packContentId, bedType, ratePlanId);
					BigDecimal packageComponentCharge = packageChargeBean.getCharge();
					chgDiscount = discountSplit(packageComponentCharge, packageCharge, packageDiscount);
					discount = discount.add(chgDiscount);
					chgAmount = packageComponentCharge.subtract(discount);
                    rate = packageComponentCharge.divide(quantity, 2, RoundingMode.HALF_UP);
				}
			}

			BasicDynaBean chargeBean = billChargeService.findByKey("charge_id", chargeId);
			chargeBean.set("amount", chgAmount);
			chargeBean.set("discount", discount);
			chargeBean.set("act_rate", rate);
			chargeBean.set("act_rate_plan_item_code", ratePlanItemCode);
			chargeBean.set("code_type", codeType);

			Map<String, Object> keys = new HashMap<String, Object>();
			keys.put("charge_id", chargeId);
			success = success && billChargeService.update(chargeBean, keys) >= 0;

		}
		return success;
	}

	/**
	 * Split discount.
	 *
	 * @param charge          Main charge
	 * @param packageCharge   Package charge
	 * @param packageDiscount Package discount
	 *
	 * @return BigDecimal
	 */
	public BigDecimal discountSplit(BigDecimal charge, BigDecimal packageCharge, BigDecimal packageDiscount) {
		BigDecimal discount = BigDecimal.ZERO;
		if ((packageCharge.compareTo(BigDecimal.ZERO) != 0) && (packageDiscount.compareTo(BigDecimal.ZERO) != 0)) {
			BigDecimal newCharg = charge.divide(packageCharge, 10, RoundingMode.CEILING);
			discount = (BigDecimal) packageDiscount.multiply(newCharg);
		}
		return discount;
	}

}
