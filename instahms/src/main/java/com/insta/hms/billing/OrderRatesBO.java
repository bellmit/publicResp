package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.orders.OrderBO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.IPPreferences.IPPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class OrderRatesBO {

	static Logger logger = LoggerFactory.getLogger(OrderRatesBO.class);
	
	public static Map<String, BigDecimal> getRegistrationCharges(String bedType, String orgId, String chargeHead,
			boolean isRenewal, Boolean isInsurance, int[] planIds, boolean excludeZero, String visitType,
			String patientId, Connection con, Boolean firstOfCategory) throws SQLException {

		Map<String,BigDecimal> map = new HashMap<String, BigDecimal>();
		Map<String,BigDecimal> ratesMap = OrderBO.getRegChargeandDiscount(chargeHead,isRenewal,orgId,bedType,visitType);
		
		map.put("item_rate", ratesMap.get("charge"));
		map.put("discount", ratesMap.get("discount"));
		return map;
	}
	
	/*
	 * OT Doctor charges depend on the doctor's OT charges plus the operation's charge depending
	 * on the ot doctor role.
	 */
	public static Map<String , BigDecimal> getOtDoctorCharges(BasicDynaBean doctor, String otDocRole,
			String visitType, BasicDynaBean operationRates, BigDecimal quantity,
			boolean isInsurance, int[] planIds, String bedType, String patientId, Boolean firstOfCategory)
		throws SQLException {

		String desc = (String) doctor.get("doctor_name");
		String chargeGroup = "OPE";
		int subGroupId = (Integer) doctor.get("service_sub_group_id");
		int insuranceCategoryId = 0;

		BigDecimal doctorCharge = BigDecimal.ZERO;
		BigDecimal discount = BigDecimal.ZERO;
		
		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>();
		// base charge is based on the operation
		if (operationRates == null) {
			logger.warn("Surgeon/anaesthetist charge without operation " + desc);

		} else {
			if (otDocRole.equals("SUOPE") || otDocRole.equals("ASUOPE") || otDocRole.equals("COSOPE")) {
				doctorCharge = (BigDecimal) operationRates.get("surgeon_charge");
				discount = (BigDecimal) operationRates.get("surg_discount");
			} else if (otDocRole.equals("ANAOPE") || otDocRole.equals("AANOPE")) {
				doctorCharge = (BigDecimal) operationRates.get("anesthetist_charge");
				discount = (BigDecimal) operationRates.get("anest_discount");
			} else {
				logger.error("Invalid OT Doc role supplied: " + otDocRole);
			}
			desc = operationRates.get("operation_name") + "/" + desc;
			insuranceCategoryId = (Integer) operationRates.get("insurance_category_id");
		}

		// the doctor premium is based on the ot doc role, hardcoded to doctor fields
		if (otDocRole.equals("COSOPE")) {
			doctorCharge = doctorCharge.add((BigDecimal) doctor.get("co_surgeon_charge"));
			discount = discount.add((BigDecimal) doctor.get("co_surgeon_charge_discount"));
		} else if (otDocRole.equals("ASUOPE") || otDocRole.equals("AANOPE")) {
			doctorCharge = doctorCharge.add((BigDecimal) doctor.get("assnt_surgeon_charge"));
			discount = discount.add((BigDecimal) doctor.get("assnt_surgeon_charge_discount"));
		} else if (otDocRole.equals("IPDOC")) {
			doctorCharge = doctorCharge.add((BigDecimal) doctor.get("doctor_ip_charge"));
			discount = discount.add((BigDecimal) doctor.get("doctor_ip_charge_discount"));
		} else {
			doctorCharge = doctorCharge.add((BigDecimal) doctor.get("ot_charge"));
			discount = discount.add((BigDecimal) doctor.get("ot_charge_discount"));
		}

		rateMap.put("item_rate", doctorCharge);
		rateMap.put("discount", discount.multiply(quantity));
		
		return rateMap;
	}
	
	
	/*
	 * Doctor charges depend on the consultation type charges, plus a premium for each doctor.
	 * The doctor premium field is based on the consultation's doctor_charge_type. Eg, if
	 * doctor_charge_type is "doctor_ip_charge", then we look at the doctor_ip_charge field
	 * in the doctor charges master.
	 *
	 * TODO: consTypeBean should have the charge information as well.
	 */
	public static Map<String , BigDecimal> getDoctorConsCharges(BasicDynaBean doctor, BasicDynaBean consTypeBean,
			String visitType, BasicDynaBean orgDetails, BigDecimal quantity,
			boolean isInsurance, int[] planIds, String bedType, String patientId, Boolean firstOfCategory)
		throws SQLException {

		BigDecimal doctorCharge = BigDecimal.ZERO;
		BigDecimal discount = BigDecimal.ZERO;

		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>();
		
		int consTypeId = (Integer) consTypeBean.get("consultation_type_id");
		BasicDynaBean consultationTypeCharge = OrderBO.getConsultationCharge(consTypeId, bedType,
				(String)orgDetails.get("org_id"));

		String docChargeType = (String)consTypeBean.get("doctor_charge_type");
		
		doctorCharge = (BigDecimal)doctor.get(docChargeType);
		discount = (BigDecimal) doctor.get(docChargeType+"_discount");
		
		doctorCharge = doctorCharge.add((BigDecimal)consultationTypeCharge.get("charge"));
		discount = ( consultationTypeCharge.get("discount") != null ? discount.add((BigDecimal)consultationTypeCharge.get("discount")) : discount );

		rateMap.put("item_rate", doctorCharge);
		rateMap.put("discount", discount.multiply(quantity));
		return rateMap;
	}
	
	/*
	 * Returns a list of charges as a charge DTO applicable to an equipment being ordered.
	 * For equipment, there can be only one charge, either daily charge or hourly.
	 */
	public static Map<String , BigDecimal> getEquipmentCharges(BasicDynaBean equip,
			Timestamp from, Timestamp to, String units, boolean isOperation, BigDecimal quantity,
			Boolean isInsurance, int[] planIds, String visitType, String patientId, Boolean firstOfCategory) throws SQLException {

		BigDecimal rate = null;
		BigDecimal discount = null;
		int qty = 1;
		int duration = 0;
		String unitsStr = "";
		
		if(units== null || units.equals("") ) units = "H";
		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>();
		if ((units.equals("D") || units.equals("Days"))) {
			/*
			 * For Daily Charge, we put num days as qty, so that rate*qty-discount = amt
			 * is maintained. This cannot be done for hourly charge.
			 */
			rate = (BigDecimal) equip.get("charge");
			discount = (BigDecimal) equip.get("daily_charge_discount");
			if (from == null) {
				qty = quantity.intValue();		// equipment supports only integer quantities
			} else {
				qty = OrderBO.getDuration(from, to, "D");
			}
			unitsStr = "Days";

		} else {
			/*
			 * rate*qty-discount = amt must be maintained. So, we calculate the total charge
			 * as per min/incr and put the rate as the total charge, set the qty=1.
			 * Note that we should not display units as Hrs because it is not 1 hrs.
			 * The trade off is between maintaining rate*qty-discount=amt vs. showing
			 * the correct amount of Hrs in the display. We choose the former.
			 */
			qty = 1;
			BigDecimal minRate = (BigDecimal) equip.get("min_charge");
			BigDecimal minDiscount = (BigDecimal) equip.get("min_charge_discount");
			BigDecimal slab1Rate = (BigDecimal) equip.get("slab_1_charge");
			BigDecimal slab1Discount = (BigDecimal) equip.get("slab_1_charge_discount");
			BigDecimal incrRate = (BigDecimal) equip.get("incr_charge");
			BigDecimal incrDiscount = (BigDecimal) equip.get("incr_charge_discount");

			int minDuration = ((BigDecimal) equip.get("min_duration")).intValue();
			int slab1Duration = ((BigDecimal) equip.get("slab_1_threshold")).intValue();
			int incrDuration = ((BigDecimal) equip.get("incr_duration")).intValue();

			if (from == null) {
				duration = quantity.intValue();		// equipment supports only integer quantities
				unitsStr = "Hrs";
			} else {
				duration = OrderBO.getDuration(from, to, "H", (Integer)equip.get("duration_unit_minutes"));
				unitsStr = "";
			}

			rate = OrderBO.getDurationCharge(duration, minDuration, slab1Duration, incrDuration,
					minRate, slab1Rate, incrRate, false);
			discount = OrderBO.getDurationCharge(duration, minDuration,slab1Duration ,
					incrDuration, minDiscount,slab1Discount, incrDiscount, false);
		}

		rateMap.put("item_rate", rate);
		rateMap.put("discount", discount.multiply(new BigDecimal(qty)));
		return rateMap;
	}
	
	
	
	public static Map<String , BigDecimal> getOperationChargesForTheaterTCOPE(BasicDynaBean theatre, Timestamp from, Timestamp to,
			String units, BigDecimal itemQty, Boolean firstOfCategory)
		throws SQLException {
		
		/*
		 * Theater charge: this is like equipment charge: we get only a single charge amount.
		 * Depending on Daily or Hourly, we get number of Days or number of Hours the equipment is
		 * used. For Days, it is a straightforward calculation: rate*numDays. For hourly, depending
		 * on the min duration etc, we get an amount that is not directly proportional to the number
		 * of hours, thus qty=1 and rate is variable.
		 */
		
		if(theatre == null) return null;
		
		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>();
		
		BasicDynaBean gprefs = GenericPreferencesDAO.getPrefsBean();
		BasicDynaBean ipprefs = new IPPreferencesDAO().getPreferences();
		String splitTheatreCharges = ipprefs.get("split_theatre_charges") != null ? (String)ipprefs.get("split_theatre_charges") : "N";

		if(gprefs.get("fixed_ot_charges").equals("Y")){//from and to times are required for Anesthecia type
			from = DataBaseUtil.getDateandTime();
			to = DataBaseUtil.getDateandTime();
		}
		
		if (units.equals("D")) {
			BigDecimal rate = (BigDecimal) theatre.get("daily_charge");
			BigDecimal discount = (BigDecimal) theatre.get("daily_charge_discount");
			// int qty = OrderBO.getDuration(from, to, "D");
			rateMap.put("item_rate", rate);
			rateMap.put("discount", discount.multiply(itemQty));
		} else if (splitTheatreCharges.equals("N")) {
			/*
			 * Do the hourly charge calculations
			 */
			BigDecimal minRate = (BigDecimal) theatre.get("min_charge");
			BigDecimal minDiscount = (BigDecimal) theatre.get("min_charge_discount");
			BigDecimal minDuration = (BigDecimal) theatre.get("min_duration");
			BigDecimal slab1Rate = (BigDecimal) theatre.get("slab_1_charge");
			BigDecimal slab1Discount = (BigDecimal) theatre.get("slab_1_charge_discount");
			BigDecimal slab1Duration = (BigDecimal) theatre.get("slab_1_threshold");
			BigDecimal incrRate = (BigDecimal) theatre.get("incr_charge");
			BigDecimal incrDiscount = (BigDecimal) theatre.get("incr_charge_discount");
			BigDecimal incrDuration = (BigDecimal) theatre.get("incr_duration");
			String slab1ChrgItemCode = ipprefs.get("theatre_slab1_charge_code") == null ? "" : (String)ipprefs.get("theatre_slab1_charge_code");
			String minChrgItemCode = ipprefs.get("theatre_min_charge_code") == null ? "" : (String)ipprefs.get("theatre_min_charge_code");

			int duration = OrderBO.getDuration(from, to, "H",(Integer)theatre.get("duration_unit_minutes"));
			BigDecimal rate = OrderBO.getDurationCharge(duration,
					minDuration.intValue(), slab1Duration.intValue(), incrDuration.intValue(),
					minRate, slab1Rate, incrRate, false);
			BigDecimal discount = OrderBO.getDurationCharge(duration,
					minDuration.intValue(),slab1Duration.intValue(), incrDuration.intValue(),
					minDiscount, slab1Discount, incrDiscount, false);
			rateMap.put("item_rate", rate);
			rateMap.put("discount", discount);
		}else {

			BigDecimal minRate = (BigDecimal) theatre.get("min_charge");
			BigDecimal minDiscount = (BigDecimal) theatre.get("min_charge_discount");
			BigDecimal minDuration = (BigDecimal) theatre.get("min_duration");
			BigDecimal slab1Rate = (BigDecimal) theatre.get("slab_1_charge");
			BigDecimal slab1Discount = (BigDecimal) theatre.get("slab_1_charge_discount");
			BigDecimal slab1Duration = (BigDecimal) theatre.get("slab_1_threshold");
			BigDecimal incrRate = (BigDecimal) theatre.get("incr_charge");
			BigDecimal incrDiscount = (BigDecimal) theatre.get("incr_charge_discount");
			BigDecimal incrDuration = (BigDecimal) theatre.get("incr_duration");
			String minChrgItemCode = ipprefs.get("theatre_min_charge_code") == null ? "" : (String)ipprefs.get("theatre_min_charge_code");
			String slab1ChrgItemCode = ipprefs.get("theatre_slab1_charge_code") == null ? "" : (String)ipprefs.get("theatre_slab1_charge_code");
			String incrChrgItemCode = ipprefs.get("theatre_incr_charge_code") == null ? "" : (String)ipprefs.get("theatre_incr_charge_code");
			int unitSize = (Integer)theatre.get("duration_unit_minutes");
			String hrlytItemCode = "";

			int duration = OrderBO.getDuration(from, to, "H", unitSize);
			int hrlyDuration = 0;
			int addlnDuration = 0;

			if (duration <= minDuration.intValue()) {
				hrlyDuration = minDuration.intValue();
				hrlytItemCode = minChrgItemCode;
				addlnDuration = duration - minDuration.intValue();

			} else {
				hrlyDuration = slab1Duration.intValue();
				hrlytItemCode = slab1ChrgItemCode;
				addlnDuration = duration - slab1Duration.intValue();
			}

			BigDecimal rate = OrderBO.getDurationCharge(hrlyDuration,
					minDuration.intValue(), slab1Duration.intValue(), 0,
					minRate, slab1Rate, incrRate, false);
			BigDecimal discount = OrderBO.getDurationCharge(hrlyDuration,
					minDuration.intValue(),slab1Duration.intValue(), 0,
					minDiscount, slab1Discount, incrDiscount, false);

			rateMap.put("item_rate", rate);
			rateMap.put("discount", discount);

			/* if ((addlnDuration > 0) && incrDuration.intValue() > 0) {

				rate = getDurationCharge(addlnDuration, 0, 0, incrDuration.intValue(),
						BigDecimal.ZERO, slab1Rate, incrRate, true);
				discount = getDurationCharge(addlnDuration, 0, 0, incrDuration.intValue(),
						BigDecimal.ZERO, slab1Discount, incrDiscount, true);

				thCharge = new ChargeDTO("OPE", "TCAOPE",
					incrRate, (incrRate.compareTo(BigDecimal.ZERO) > 0) ? rate.divide(incrRate) : BigDecimal.ZERO, discount, "",
					(String) theatre.get("theatre_id"), operName + "/" + (String) theatre.get("theatre_name"),
					null, isInsurance, serviceSubGroupId,insuranceCategoryId, visitType, patientId, firstOfCategory);

				thCharge.setInsuranceAmt(planIds, visitType, thCharge.getFirstOfCategory());
				thCharge.setActItemCode(itemCode);
				thCharge.setActRatePlanItemCode(incrChrgItemCode);
				thCharge.setCodeType(theatreCodeType);
				thCharge.setOp_id(opnId);
				thCharge.setAllowRateIncrease((Boolean)opn.get("allow_rate_increase"));
				thCharge.setAllowRateDecrease((Boolean)opn.get("allow_rate_decrease"));
				if(!finalizationStatus.equals("N"))
					thCharge.setActRemarks(DataBaseUtil.timeStampFormatter.format(from)+" to  "
							+ DataBaseUtil.timeStampFormatter.format(to));
				thCharge.setFrom_date(from);
				thCharge.setTo_date(to);

				l.add(thCharge);
			} */
		}
		return rateMap;
	}
	
	
	public static Map<String , BigDecimal> getSurgeonChargesSUOPE(BasicDynaBean operationBean, BasicDynaBean surgeonDocBean) throws SQLException {
		
		if(operationBean == null) return null;
		
		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>();
		BigDecimal rate = BigDecimal.ZERO;
		if (surgeonDocBean != null) {
			rate = (BigDecimal) operationBean.get("surgeon_charge");
			rate = rate.add((BigDecimal) surgeonDocBean.get("charge"));

			//if (rate.compareTo(BigDecimal.ZERO) > 0) {
				BigDecimal discount = (BigDecimal) operationBean.get("surg_discount");
				discount = discount.add((BigDecimal) surgeonDocBean.get("discount"));
				rateMap.put("item_rate", rate);
				rateMap.put("discount", discount);
			//}
		}
		return rateMap;
	}
	
	public static Map<String , BigDecimal> getAnestiatistChargesANAOPE(BasicDynaBean operationBean, BasicDynaBean anaDocBean) throws SQLException {
		
		if(operationBean == null) return null;
		
		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>();
		BigDecimal rate = BigDecimal.ZERO;
		if (anaDocBean != null) {
			rate = (BigDecimal) operationBean.get("anesthetist_charge");
			rate = rate.add((BigDecimal) anaDocBean.get("charge"));
			//if (rate.compareTo(BigDecimal.ZERO) > 0) {
				BigDecimal discount = (BigDecimal) operationBean.get("anest_discount");
				discount = discount.add((BigDecimal) anaDocBean.get("discount"));
				rateMap.put("item_rate", rate);
				rateMap.put("discount", discount);
			//}
		}
		return rateMap;
	}
	
	public static Map<String , BigDecimal> getAnaesthesiaTypeChargesANATOPE(BasicDynaBean anasthesiaTypeBean, Timestamp from, Timestamp to) throws SQLException {

		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>();
		BigDecimal rate = BigDecimal.ZERO;
		BigDecimal discount = BigDecimal.ZERO;
		
		if(anasthesiaTypeBean != null) {
			BigDecimal minRate = (BigDecimal) anasthesiaTypeBean.get("min_charge");
			BigDecimal minDiscount = (BigDecimal) anasthesiaTypeBean.get("min_charge_discount");
			BigDecimal minDuration = (BigDecimal) anasthesiaTypeBean.get("min_duration");
			BigDecimal slab1Rate = (BigDecimal) anasthesiaTypeBean.get("slab_1_charge");
			BigDecimal slab1Discount = (BigDecimal) anasthesiaTypeBean.get("slab_1_charge_discount");
			BigDecimal slab1Duration = (BigDecimal) anasthesiaTypeBean.get("slab_1_threshold");
			BigDecimal incrRate = (BigDecimal) anasthesiaTypeBean.get("incr_charge");
			BigDecimal incrDiscount = (BigDecimal) anasthesiaTypeBean.get("incr_charge_discount");
			BigDecimal incrDuration = (BigDecimal) anasthesiaTypeBean.get("incr_duration");
			Integer baseUnit = (Integer) anasthesiaTypeBean.get("base_unit");
			Integer totalUnit = 0;

			int duration = OrderBO.getDuration(from, to, "H",
					(Integer)anasthesiaTypeBean.get("duration_unit_minutes"));
			if (baseUnit != null) {
				totalUnit = baseUnit+duration;
				rate = incrRate;
				discount = OrderBO.getBaseCharge(totalUnit,incrDiscount);
				rateMap.put("item_rate", rate);
				rateMap.put("discount", discount);
			} else {
				totalUnit = 1;
				rate = OrderBO.getDurationCharge(duration,
						minDuration.intValue(),slab1Duration.intValue(), incrDuration.intValue(),
						minRate, slab1Rate, incrRate, false);
				discount = OrderBO.getDurationCharge(duration,
						minDuration.intValue(),slab1Duration.intValue(), incrDuration.intValue(),
						minDiscount, slab1Discount, incrDiscount, false);
				rateMap.put("item_rate", rate);
				rateMap.put("discount", discount);
			}
		}
		return rateMap;
	}
	
	public static Map<String , BigDecimal> getSurgicalAssistanceChargeSACOPE(BasicDynaBean operationBean) throws SQLException {

		if(operationBean == null)
			return null;
		
		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>();
		BigDecimal rate = BigDecimal.ZERO;
		BigDecimal discount = BigDecimal.ZERO;
		rate = (BigDecimal) operationBean.get("surg_asstance_charge");
		discount = (BigDecimal) operationBean.get("surg_asst_discount");
		
		rateMap.put("item_rate", rate);
		rateMap.put("discount", discount);
		
		return rateMap;
	}

	public static Map<String, BigDecimal> getCoOperateSurgeonFeeCOSOPE(BasicDynaBean operationBean, BasicDynaBean doctor) {
		
		if(operationBean == null || doctor == null)
			return null;
		
		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>();
		BigDecimal doctorCharge = BigDecimal.ZERO;
		BigDecimal discount = BigDecimal.ZERO;
		
		doctorCharge = (BigDecimal) operationBean.get("surgeon_charge");
		discount = (BigDecimal) operationBean.get("surg_discount");
		
		doctorCharge = doctorCharge.add((BigDecimal) doctor.get("co_surgeon_charge"));
		discount = discount.add((BigDecimal) doctor.get("co_surgeon_charge_discount"));
		
		rateMap.put("item_rate", doctorCharge);
		rateMap.put("discount", discount);
		
		return rateMap;
	}

	public static Map<String, BigDecimal> getAsstSurgeonFeeASUOPE(BasicDynaBean operationBean, BasicDynaBean doctor) {
		
		if(operationBean == null || doctor == null)
			return null;
		
		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>();
		BigDecimal doctorCharge = BigDecimal.ZERO;
		BigDecimal discount = BigDecimal.ZERO;
		
		doctorCharge = (BigDecimal) operationBean.get("surgeon_charge");
		discount = (BigDecimal) operationBean.get("surg_discount");
		
		doctorCharge = doctorCharge.add((BigDecimal) doctor.get("assnt_surgeon_charge"));
		discount = discount.add((BigDecimal) doctor.get("assnt_surgeon_charge_discount"));
		
		rateMap.put("item_rate", doctorCharge);
		rateMap.put("discount", discount);
		
		return rateMap;
	}

	public static Map<String, BigDecimal> getAsstAnaesthetistFeeAANOPE(BasicDynaBean operationBean, BasicDynaBean doctor) {
		
		if(operationBean == null || doctor == null)
			return null;
		
		Map<String , BigDecimal> rateMap = new HashMap<String, BigDecimal>();
		BigDecimal doctorCharge = BigDecimal.ZERO;
		BigDecimal discount = BigDecimal.ZERO;
		
		doctorCharge = (BigDecimal) operationBean.get("anesthetist_charge");
		discount = (BigDecimal) operationBean.get("anest_discount");
		
		doctorCharge = doctorCharge.add((BigDecimal) doctor.get("assnt_surgeon_charge"));
		discount = discount.add((BigDecimal) doctor.get("assnt_surgeon_charge_discount"));
		
		rateMap.put("item_rate", doctorCharge);
		rateMap.put("discount", discount);
		
		return rateMap;
	}
	
}
