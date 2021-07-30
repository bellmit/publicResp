package com.insta.hms.core.billing;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.billing.Bill;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.preferences.ippreferences.IpPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.adt.IpBedDetailsService;
import com.insta.hms.core.clinical.order.beditems.BedOrderItemService;
import com.insta.hms.mdm.bedtypes.BedTypeService;
import com.insta.hms.mdm.chargeheads.ChargeHeadsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class BedChargeCalculationHelper {
	private static Logger log = LoggerFactory.getLogger(BedChargeCalculationHelper.class);
	
	@LazyAutowired
	private IpBedDetailsService ipBedDetailsService;
	
	@LazyAutowired
	private BedTypeService bedTypeService;
	
	@LazyAutowired
	private DiscountService discountService;
	
	@LazyAutowired
	private BedOrderItemService bedItemService;
	
	@LazyAutowired
	private IpPreferencesService ipPrefService;
	
	@LazyAutowired
	private DiscountService discountPlanService;
	
	@LazyAutowired
	private SessionService sessionService;
	
	@LazyAutowired
	private BillActivityChargeService billActivityChargeService;
	
	@LazyAutowired
	private BillChargeClaimService billChargeClaimService;
	
	@LazyAutowired
	private BillChargeService billChargeService;
	
	@LazyAutowired
	private ChargeHeadsService chargeHeadsService;
	 
	@LazyAutowired
	private GenericPreferencesService genPrefService;

	@LazyAutowired
	private BillService billService;

	@SuppressWarnings("unchecked")
	public void recalculateBedCharges(BasicDynaBean visitbean,
			BasicDynaBean bill, int[] planIds, String[] preAuthIds,
			Integer[] preAuthModeIds) throws ParseException {
		
		String visitId = (String)visitbean.get("patient_id");
		List<BasicDynaBean> mainBeds = ipBedDetailsService.getVistMainBeds(visitId);
		BasicDynaBean existingBed = ipBedDetailsService.getActiveBedDetails(visitId);
		if (existingBed == null) {
			//possible in case in a transaction cancelling current bed and updating charges
			existingBed = ipBedDetailsService.getAdmissionDetails(visitId);
		}		
		Map<String, BasicDynaBean> ICUchargesMap = ConversionUtils.listBeanToMapBean(
				bedTypeService.getIcuBedChargesList(
						 (String)visitbean.get("bill_bed_type"), (String)bill.get("bill_rate_plan_id")), "intensive_bed_type");
		Map<String, BasicDynaBean> normalBedChargesMap = ConversionUtils.listBeanToMapBean(
				bedTypeService.getNormalBedChargesList((String)bill.get("bill_rate_plan_id")), "bed_type");
		String userName = (String) sessionService.getSessionAttributes().get("userId");
		for (BasicDynaBean mainBed : mainBeds ) {
			int mainAdmitId = (Integer) mainBed.get("admit_id");
			List<BasicDynaBean> referencedBeds = ipBedDetailsService.getReferencedBeds(visitId, mainAdmitId);
			Timestamp[] startEndDates = getBedStartEnd(referencedBeds, false);
			log.debug("Thread start/end (" + mainAdmitId + "): " +
					startEndDates[0] + " - " + startEndDates[1]);

			Map<String, BasicDynaBean> bedCharges = mainBed.get("is_icu").equals("Y") ? ICUchargesMap :
					normalBedChargesMap;

			List<BasicDynaBean> newCharges = getAllBedCharges( mainAdmitId,
					existingBed, referencedBeds, bedCharges, visitbean, bill, planIds, startEndDates);  

			for (BasicDynaBean charge: newCharges) {
				charge.set("prescribing_dr_id",(String)visitbean.get("doctor"));//admitting doctor
				charge.set("activity_conducted",mainBed.get("bed_state").equals("F") ? "Y" : "N");
				charge.set("username",userName);
				if (charge.get("charge_head").equals(BillChargeService.CH_DUTY_DOCTOR )
						|| charge.get("charge_head").equals(BillChargeService.CH_DUTY_DOCTOR_ICU)){
					charge.set("payee_doctor_id",(String)mainBed.get("duty_doctor_id"));
				}
				charge.set("discount",((BigDecimal) charge.get("discount")).multiply((BigDecimal) charge.get("act_quantity")));
				
				if(charge.get("allow_discount").equals("Y"))
					discountService.applyDiscountRule(charge,(Integer)bill.get("discount_category_id"),(String)visitbean.get("visit_type"));
			}

			String chargeId = billActivityChargeService.getChargeId("BED", "" + mainAdmitId);
			if (chargeId == null) {
				// newCharges can be 0 if the period is very small.
				if (newCharges.size() > 0) {
					// we will never have two beds being inserted the first time, so this is OK.
					// Otherwise, this is the wrong thing to do, as we will need two main charges.
					bedItemService.insertOrderCharges(newCharges, "BED", mainAdmitId,
							null, (String)newCharges.get(0).get("prescribing_dr_id"),
							DateUtil.getCurrentTimestamp(), "N",
							null, null,null,null,bill, planIds);
				}
			} else {
				if(billActivityChargeService.getBillStatus(chargeId).equals(Bill.BILL_STATUS_OPEN)) {//only open bills should get updated
					List<BasicDynaBean> originalCharges = billChargeService.getChargeAndRefs(chargeId);
					recalculateChargeAmounts(originalCharges, newCharges, bill, userName,planIds,preAuthIds,preAuthModeIds);
				}
			}
		}
		
	}
	
	 /*
	  * Gets a list of bed charges for the given thread. Note this can return multiple
	  * sets of charges, one for each slot. The first charge is the "main" charge, and all
	  * other charges are ref charges to the main charge. Only the main charge has an
	  * activity (thus an entry in bill_activity_charge), which is the admit_id of the first bed
	  * in the thread, ie, main bed.
	  */
	 private List<BasicDynaBean> getAllBedCharges(int mainAdmitId,
			BasicDynaBean existingBed, List<BasicDynaBean> referencedBeds,
			Map<String, BasicDynaBean> bedChargesMap, BasicDynaBean visitbean, BasicDynaBean bill, int[] planIds, 
			Timestamp[] startEndDates) throws ParseException {

			String dayCareStatus = (String) existingBed.get("daycare_status");
			boolean isDayCare = dayCareStatus.equals("Y");
			List<BasicDynaBean> allBedCharges = new ArrayList<BasicDynaBean>();
			BasicDynaBean ipPrefs = ipPrefService.getPreferences();
			boolean cutOff = ipPrefs.get("cut_off_required").equals("Y") && !isDayCare;

			List<SlotDesc> slotDetailsList = getThreadSlots(startEndDates[0], startEndDates[1],
					(Integer)ipPrefs.get("slab1_duration"), (Integer)ipPrefs.get("next_slabs_duration"),
					isDayCare, cutOff);

			SlotDesc prvsSlot = null;
			BasicDynaBean prvsBed = null;
			BigDecimal[] prvsDaysHours = null;
			List<BasicDynaBean> prvsCharges = null;
			int firstBedIdx = 0;
			int numBeds = referencedBeds.size();

			for (SlotDesc slot : slotDetailsList) {
				Timestamp from = slot.getStartTime();
				Timestamp to = slot.getEndTime();
				// find the first bed in the slot: skip all beds whose end time is before slot start
				// sync start/end times of beds in thread for beds cancelled in between. Or,
				// break the thread if a bed gets cancelled in between.
				while (firstBedIdx < numBeds) {
					if (((Timestamp)referencedBeds.get(firstBedIdx).get("end_date")).compareTo(from) <0 ) {
						firstBedIdx++;
					} else {
						break;
					}
				}
				if (firstBedIdx == numBeds) {
					log.error("No beds in slot: " + from + " for admit_id" + mainAdmitId);
					return null;
				}

				// iterate through all beds in slot and find the max bed type
				int bedIdx = firstBedIdx;
				BasicDynaBean maxBed = referencedBeds.get(bedIdx);
				BasicDynaBean maxBedRates = bedChargesMap.get(maxBed.get("charged_bed_type"));

				while (bedIdx < numBeds) {
					BasicDynaBean curBed = referencedBeds.get(bedIdx);
					if (((Timestamp)curBed.get("start_date")).compareTo(slot.getEndTime()) > 0) {
						break;
					}
					BasicDynaBean curBedRates = bedChargesMap.get(curBed.get("charged_bed_type"));
					if (((BigDecimal)maxBedRates.get("bed_charge")).
							compareTo((BigDecimal)curBedRates.get("bed_charge")) < 0) {
						maxBed = curBed;
						maxBedRates = curBedRates;
					}
					bedIdx++;
				}

				// now we have the max bed in our slot. Get the charges for this.
				BigDecimal[] daysHours = new BigDecimal[2];
				List<BasicDynaBean> slotCharges = getSlotCharges(ipPrefs, maxBed, slot, dayCareStatus,
						maxBedRates, daysHours, visitbean, bill, planIds, ipPrefs);

				// see if we can merge it to previous set of charges
				if (prvsCharges != null && prvsCharges.size() > 0 &&
						(prvsBed != null) && prvsBed.get("bed_id").equals(maxBed.get("bed_id"))
						&& prvsDaysHours[1].equals(BigDecimal.ZERO)		// previous slot should not have hourly
						&& !daysHours[0].equals(BigDecimal.ZERO) ) {	// this days != 0

					prvsSlot.setEndTime(to);		// merge
					List<BasicDynaBean> newCharges =
						mergeBedCharges(prvsCharges, slotCharges, prvsSlot.getStartTime(), prvsSlot.getEndTime());
					allBedCharges.addAll(newCharges);
				} else {
					prvsCharges = slotCharges;
					prvsSlot = slot;
					allBedCharges.addAll(slotCharges);
				}

				prvsBed = maxBed;
				prvsDaysHours = daysHours;
				firstBedIdx = bedIdx - 1;	 // next slot can at most have our last bed as its first bed
			}
//TODO Remove after testing if not required
//			if (allBedCharges.size() > 0) {
//				allBedCharges.get(0).setActivityDetails("BED", mainAdmitId, "N", null);
//			}
			return allBedCharges;
	}

	public Timestamp[] getBedStartEnd(List<BasicDynaBean> threadBeds, boolean cutOff) throws ParseException{

		 Timestamp startDate = null, endDate = null;
		 int startBed = 0, endBed = threadBeds.size()-1;

		 if (threadBeds.size() > 0) {
			 startDate = (Timestamp) threadBeds.get(startBed).get("start_date");
			 endDate = (Timestamp ) threadBeds.get(endBed).get("end_date");
		 }

		 if (cutOff) {
			 startDate = DateUtil.parseTimestamp(
					 DataBaseUtil.dateFormatter.format(startDate).toString(),"00:00");
		 }

		 return new Timestamp[]{startDate ,endDate };
	}
	
	/*
	  * Divides the slots as per slabs and sets start and end dates, and returns
	  * a list of SlotDescs.
	  */
	 public List<SlotDesc> getThreadSlots(Timestamp actStart, Timestamp actEnd,
			 Integer mainSlab, Integer subSlab,
			 boolean dayCare, boolean cutOff) {

		 List<SlotDesc> slotDetailsList = new ArrayList<SlotDesc>();

		 if (dayCare) {
			 slotDetailsList.add(new SlotDesc(actStart, actEnd, false));

		 } else if (cutOff) {
			 // start with actStart and unknown end.
			 SlotDesc slot = new SlotDesc(actStart, null, true);
			 // Get 00:00 of actStart date, ie, beginning of the day.
			 Calendar cal = Calendar.getInstance();
			 cal.setTime(actStart);
			 DateUtil.dateTrunc(cal);		// truncate the min/hour etc. part.

			 int i = 0;
			 while (DateUtil.dateDiff(cal, actEnd) < 0) {
				 // till we reach the same date as the actEnd, keep creating new slots.
				 cal.add(Calendar.DATE, 1);
				 slot.setEndTime(new java.sql.Timestamp(cal.getTime().getTime()));
				 slotDetailsList.add(slot);
				 log.debug("Cutoff Slot: " + slot.getStartTime() + "-" + slot.getEndTime());
				 // create a new slot.
				 slot = new SlotDesc(slot.getEndTime(), null, false);
				 if (i++ > 2000) break;
			 }
			 // set the last slot's end time to actEnd and add that also.
			 slot.setEndTime(actEnd);
			 slotDetailsList.add(slot);
			 log.debug("Final Cutoff Slot: " + slot.getStartTime() + "-" + slot.getEndTime());

		 } else {
			 SlotDesc slot = new SlotDesc(actStart, null, true);
			 int slabDuration = mainSlab;

			 int i = 0;
			 while (DateUtil.addHours(slot.getStartTime(), slabDuration).compareTo(actEnd) < 0) {
				 Timestamp slabEnd = DateUtil.addHours(slot.getStartTime(), slabDuration);
				 slabDuration = subSlab;
				 slot.setEndTime(slabEnd);
				 slotDetailsList.add(slot);
				 log.debug("Slot: " + slot.getStartTime() + "-" + slot.getEndTime());
				 // create a new slot.
				 slot = new SlotDesc(slot.getEndTime(), null, false);
				 if (i++ > 2000) break;
			 }
			 // set the last slot's end time to actEnd and add that also.
			 slot.setEndTime(actEnd);
			 slotDetailsList.add(slot);
			 log.debug("Final Slot: " + slot.getStartTime() + "-" + slot.getEndTime());
		 }

		 return slotDetailsList;
	 }
	 
	 public class SlotDesc{
		 private Timestamp startTime;
		 private Timestamp endTime;
		 private boolean isFirstSlab;

		 public SlotDesc(Timestamp startTime,Timestamp endTime,boolean isFirstSlab) {
			 this.startTime = startTime;
			 this.endTime = endTime;
			 this.isFirstSlab = isFirstSlab;
		 }

		 public Timestamp getEndTime() { return endTime; }
		 public void setEndTime(Timestamp endTime) { this.endTime = endTime; }

		 public Timestamp getStartTime() { return startTime; }
		 public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

		 public boolean isFirstSlab() { return isFirstSlab; }
		 public void setFirstSlab(boolean isFirstSlab) { this.isFirstSlab = isFirstSlab; }
	 }
	 
	 public List<BasicDynaBean> getSlotCharges(BasicDynaBean prefs, BasicDynaBean ipBed, SlotDesc slot,
				String dayCareStatus, BasicDynaBean bedCharges, BigDecimal[] retDaysHours, BasicDynaBean visitbean, 
				BasicDynaBean bill, int[] planIds, BasicDynaBean ipPrefs) {

			boolean onlyMainCharges = false;
			if ((Boolean)ipBed.get("is_bystander") ||
					( (Boolean)ipBed.get("is_retained") && prefs.get("retain_bed_charges").equals("B")))
				onlyMainCharges = true;

			String bedType = ipBed.get("is_icu").equals("Y") ? "ICU" : "BED";
			boolean isBystander = (Boolean)ipBed.get("is_bystander");
			Timestamp fromTime = slot.getStartTime();
			Timestamp toTime = slot.getEndTime();

			List<BasicDynaBean> charges = getBedCharges(bedType, bedCharges, fromTime, toTime, null,
					isBystander, dayCareStatus, onlyMainCharges,
					(boolean)bill.get("is_tpa"), planIds, (String)visitbean.get("visit_type"), (String)visitbean.get("patient_id"),
					slot.isFirstSlab(), (String)ipBed.get("bed_state"), null, retDaysHours, (int)bill.get("discount_category_id"),
					(String)bill.get("bill_no"), ipPrefs);

			DateUtil dateUtil = new DateUtil();
			String remarks = dateUtil.getTimeStampFormatter().format(fromTime) + " to "+
				dateUtil.getTimeStampFormatter().format(toTime);
			log.debug("Bed remarks: " + remarks);

			if (charges.size() == 0)
				return charges;

			for (BasicDynaBean charge : charges) {
				log.debug("Bed Charge head: " + charge.get("charge_head") + ", qty: " + charge.get("act_quantity") +
						", charge: " + charge.get("amount"));
				charge.set("act_description_id","" + (Integer)ipBed.get("bed_id"));
				charge.set("act_description",(String)ipBed.get("bed_name"));
				charge.set("act_remarks",remarks);
				charge.set("activity_conducted",ipBed.get("bed_state").equals("F") ? "Y" : "N");
				if (charge.get("charge_head").equals(BillChargeService.CH_DUTY_DOCTOR )||
						charge.get("charge_head").equals(BillChargeService.CH_DUTY_DOCTOR_ICU)) {
					charge.set("payee_doctor_id",(String)ipBed.get("duty_doctor_id"));
				}
			}

			return charges;
		}
	 
	 /*
	 * Merge a set of bed charges into another set. The "to" set cannot have hourly charges. If
	 * the "from" set has hourly charges, then it cannot be merged, it needs to be added. This
	 * charge is returned as a list of newCharges that could not be merged. Caller has to
	 * deal with it.
	 */
	public List<BasicDynaBean> mergeBedCharges(List<BasicDynaBean> toCharges,
			List<BasicDynaBean> fromCharges, Timestamp fromDate, Timestamp toDate){

		if (toCharges.size() == 0)
			return fromCharges;			// all are to be added

		ArrayList<BasicDynaBean> newCharges = new ArrayList<BasicDynaBean>();

		if (fromCharges.size() == 0)
			return newCharges;			// nothing to be added

		log.debug("Merging: " + fromCharges.size() + " into "
				+ toCharges.size() + " [" + fromDate + "-" + toDate + "]");

		for (BasicDynaBean mergeFrom : fromCharges) {
			// find the corresponding charge head in the list of toCharges
			BasicDynaBean mergeTo = null;
			for (BasicDynaBean to : toCharges) {
				if (to.get("charge_head").equals(mergeFrom.get("charge_head"))
						&& to.get("act_unit").equals(mergeFrom.get("act_unit"))) {
					mergeTo = to;
					break;
				}
			}

			if (mergeTo != null) {
				mergeCharges(mergeTo, mergeFrom, fromDate, toDate);
			} else {
				// could not find a charge to merge into, need to add it to the original list.
				newCharges.add(mergeFrom);
			}
		}
		return newCharges;
	}
		
	public void mergeCharges(BasicDynaBean to,BasicDynaBean from, Timestamp fromDate, Timestamp toDate){

		to.set("act_quantity",to.get("charge_head").equals(BillChargeService.CH_LUXURY_TAX) ? BigDecimal.ONE :
				((BigDecimal) to.get("act_quantity")).add((BigDecimal) from.get("act_quantity")));

		to.set("act_remarks",DateUtil.formatTimestamp(fromDate) + " to "+ DateUtil.formatTimestamp(toDate));

		if (to.get("charge_head").equals(BillChargeService.CH_LUXURY_TAX) ) {
			to.set("amount",((BigDecimal) to.get("amount")).add((BigDecimal) from.get("amount")));
			to.set("act_rate",to.get("amount"));
		} else {
			to.set("amount",(((BigDecimal) to.get("act_rate")).subtract((BigDecimal) to.get("discount")).multiply((BigDecimal) to.get("act_quantity"))));
			
		}

		// TODO: can we just add the insurance claim amounts?
		/*if (isInsurance) {
			BasicDynaBean chrgbean = new ChargeHeadsDAO().findByKey("chargehead_id",to.getChargeHead());

			if (chrgbean.get("insurance_payable") != null &&
				((String)chrgbean.get("insurance_payable")).equals("Y")) {
				to.setInsuranceAmtForPlan(planId, visitType, to.getFirstOfCategory());
			} else {
				to.setInsuranceClaimAmount(BigDecimal.ZERO);
			}
		}*/
	}
	
	public List<BasicDynaBean> getBedCharges(String bed, BasicDynaBean bc,
			Timestamp fromTime, Timestamp toTime, BigDecimal quantity,
			boolean isBystander, String daycareStatus, boolean onlyMainCharges,
			boolean isInsurance, int[] planIds, String visitType, String patientId,
			boolean isFirstBed, String bedState, Boolean firstOfCategory,
			BigDecimal[] retBedDaysHours,Integer discCatId, String billNo, BasicDynaBean ipPrefs){

		String bedType = (String) (bed.equals("ICU") ? bc.get("intensive_bed_type") : bc.get("bed_type"));
		BasicDynaBean bedTypeBean = bedTypeService.getBedTypeDetails(bedType);

		BigDecimal[] bedDaysHours = getBedDaysHours(fromTime, toTime, quantity, daycareStatus, isFirstBed,
				(String)bedTypeBean.get("is_icu"), ipPrefs);

		BigDecimal bedDays = bedDaysHours[0], bedHours = bedDaysHours[1];
		//TODO discount plan details probably not required. Remove after testing
		//discountService.setDiscountPlanDetails( null != discCatId ?(Integer)discCatId : 0);
		
		if (retBedDaysHours != null) {
			retBedDaysHours[0] = bedDays;
			retBedDaysHours[1] = bedHours;
		}

		List<BasicDynaBean> l = new ArrayList<BasicDynaBean>();
		BigDecimal bedAmount = BigDecimal.ZERO;
		BigDecimal totalAmount = BigDecimal.ZERO;
		
		Map<String,String> keys = new HashMap<String, String>();
		if(isBystander){
			keys.put("chargehead_id","BYBED");
		}else{
			keys.put("chargehead_id","B"+bed);
		}
		
		BasicDynaBean mainChargeHeadBean = chargeHeadsService.findByPk(keys);
		int mainServiceSubGroup = (Integer) mainChargeHeadBean.get("service_sub_group_id");

		int insuranceCategoryId = getIntegerValueFromBean(bedTypeBean,"insurance_category_id");

		/*
		 * Daily bed charge: One daily charge OR one hourly charge is added
		 */
		BasicDynaBean mainCharge = billChargeService.getBean();

		if (isBystander && bedDays.compareTo(BigDecimal.ZERO) > 0) {
			BigDecimal bystanderBedCharges = BigDecimal.ZERO;
			BigDecimal bystanderBedDiscount = BigDecimal.ZERO;
			if (ipPrefs.get("bystander_bed_charges_applicable_on").equals("W")) {
				bystanderBedCharges = ((BigDecimal)bc.get("bed_charge")).
										add((BigDecimal) bc.get("nursing_charge")).
										add((BigDecimal) bc.get("duty_charge")).
										add((BigDecimal) bc.get("maintainance_charge"));

				bystanderBedDiscount = ((BigDecimal)bc.get("bed_charge_discount")).
										add((BigDecimal) bc.get("nursing_charge_discount")).
										add((BigDecimal) bc.get("duty_charge_discount")).
										add((BigDecimal) bc.get("maintainance_charge_discount"));
			} else {
				bystanderBedCharges = (BigDecimal)bc.get("bed_charge");
				bystanderBedDiscount = (BigDecimal)bc.get("bed_charge_discount");
			}
			BigDecimal discount = bystanderBedDiscount.multiply(bedDays);
			boolean allowRateIncrease = (Boolean)mainChargeHeadBean.get("allow_rate_increase");
			boolean allowRateDecrease = (Boolean)mainChargeHeadBean.get("allow_rate_decrease");
			
			addBedCharges(bed, isInsurance, visitType, firstOfCategory,
					discCatId, bedType, bedDays, mainServiceSubGroup,
					insuranceCategoryId, mainCharge, bystanderBedCharges,
					"BYBED", "Days", discount,
					allowRateIncrease, allowRateDecrease, null, fromTime, toTime,null);
			
		} else if (daycareStatus.equals("Y")) {
			// day care: special handling based on slabs and incr rate
			int duration = bedHours.intValue();
			int minDuration = (Integer) ipPrefs.get("daycare_min_duration");
			int slab1Duration = (Integer) ipPrefs.get("daycare_slab_1_threshold");
			int slab2Duration = (Integer) ipPrefs.get("daycare_slab_2_threshold");
			int incrDuration = 1;

			if (minDuration==0 && slab1Duration==0 && slab2Duration==0) {
				// only hourly charges: now we can have rate*hours = amount, qty is Hours.
				BigDecimal rate = (BigDecimal) bc.get("hourly_charge");
				BigDecimal discount = (BigDecimal) bc.get("hourly_charge_discount");
				addBedCharges(bed, isInsurance, visitType, firstOfCategory,
						discCatId, bedType, bedHours, mainServiceSubGroup,
						insuranceCategoryId, mainCharge, rate,
						"B"+bed, "Days", discount.multiply(bedHours),
						true, true, null, fromTime, toTime,null);

			} else {
				// rate is slab determined, quantity has to be 1
				BigDecimal minRate = (BigDecimal) bc.get("daycare_slab_1_charge");
				BigDecimal minDiscount = (BigDecimal) bc.get("daycare_slab_1_charge_discount");
				BigDecimal slab1Rate = (BigDecimal) bc.get("daycare_slab_2_charge");
				BigDecimal slab1Discount = (BigDecimal) bc.get("daycare_slab_2_charge_discount");
				BigDecimal slab2Rate = (BigDecimal) bc.get("daycare_slab_3_charge");
				BigDecimal slab2Discount = (BigDecimal) bc.get("daycare_slab_3_charge_discount");
				BigDecimal incrRate = (BigDecimal) bc.get("hourly_charge");
				BigDecimal incrDiscount = (BigDecimal) bc.get("hourly_charge_discount");

				BigDecimal charge = getDurationCharge(duration,
						minDuration, slab1Duration, slab2Duration, incrDuration,
						minRate, slab1Rate, slab2Rate, incrRate, false);
				BigDecimal discount = getDurationCharge(duration,
						minDuration, slab1Duration, slab2Duration, incrDuration,
						minDiscount, slab1Discount, slab2Discount, incrDiscount, false);

				// "BED", "BBED" or "ICU", "BICU"
				addBedCharges(bed, isInsurance, visitType, firstOfCategory,
						discCatId, bedType, BigDecimal.ONE, mainServiceSubGroup,
						insuranceCategoryId, mainCharge, charge,
						"B"+bed, "", discount,
						true, true, null,fromTime, toTime,null);
			}
		} else {
			// normal bed (or ICU): add a "Day" charge if num days is not 0
			if (bedDays.compareTo(BigDecimal.ZERO) > 0) {
				log.debug("Main charge bed days: " + bedDays);
				// "BED", "BBED" or "ICU", "BICU"
				addBedCharges(bed, isInsurance, visitType, firstOfCategory,
						discCatId, bedType, bedDays, mainServiceSubGroup,
						insuranceCategoryId, mainCharge, (BigDecimal) bc.get("bed_charge"),
						"B"+bed, "Days", ((BigDecimal) bc.get("bed_charge_discount")).multiply(bedDays),
						true, true, null,fromTime,toTime,null);
			}
		}

		if (mainCharge != null) {
			mainCharge.set("act_rate_plan_item_code",(String) bc.get("item_code"));
			mainCharge.set("code_type",(String) bc.get("code_type"));
			l.add(mainCharge);
			bedAmount = (BigDecimal)mainCharge.get("amount");
			totalAmount = (BigDecimal)mainCharge.get("amount");
		}

		/*
		 * Additional hourly charge component for the main bed charge, if not daycare
		 */
		if (daycareStatus.equals("N") && bedHours.compareTo(BigDecimal.ZERO) > 0 ) {
			// add another charge item for the hourly charge, even if hourly rate is 0
			BasicDynaBean additionalCharge = billChargeService.getBean();
			addBedCharges(bed, isInsurance, visitType, firstOfCategory,
					discCatId, bedType, bedHours, mainServiceSubGroup,
					insuranceCategoryId, additionalCharge, (BigDecimal) bc.get("hourly_charge"),
					isBystander ?"BYBED" : "B"+bed, "Hrs", ((BigDecimal) bc.get("hourly_charge_discount")).multiply(bedHours),
					true, true, null,fromTime,toTime,null);
			
			if (mainCharge == null)
				mainCharge = additionalCharge;
			l.add(additionalCharge);
			bedAmount = bedAmount.add((BigDecimal)additionalCharge.get("amount"));
			totalAmount = totalAmount.add((BigDecimal)additionalCharge.get("amount"));
		}

		if (mainCharge == null)		// no charges if the time is too short.
			return l;

		/*
		 * Associated charges for non-bystander, non-daycare beds provided !onlyMainCharges:
		 * nurse, duty doctor, professional charge: only daily charges added
		 * if the corresponding rate is non-zero.
		 */
		if (daycareStatus.equals("N") && !onlyMainCharges && bedDays.compareTo(BigDecimal.ZERO) > 0) {
			BasicDynaBean charge = billChargeService.getBean();
			BasicDynaBean chargeHeadBean;
			if (((BigDecimal) bc.get("nursing_charge")).compareTo(BigDecimal.ZERO) > 0) {
				chargeHeadBean = getChargeHeadForBedType("NC"+bed);
				addBedCharges(bed, isInsurance, visitType, firstOfCategory,
						discCatId, bedType, bedDays, (Integer)chargeHeadBean.get("service_sub_group_id"),
						insuranceCategoryId, charge, (BigDecimal) bc.get("nursing_charge"),
						"NC"+bed, "Days", ((BigDecimal) bc.get("nursing_charge_discount")).multiply(bedDays),
						true, true, null,fromTime,toTime,null);
			}
			if (((BigDecimal) bc.get("duty_charge")).compareTo(BigDecimal.ZERO) > 0) {
				chargeHeadBean = getChargeHeadForBedType("DD"+bed);
				addBedCharges(bed, isInsurance, visitType, firstOfCategory,
						discCatId, bedType, bedDays, (Integer)chargeHeadBean.get("service_sub_group_id"),
						insuranceCategoryId, charge, (BigDecimal) bc.get("duty_charge"),
						"DD"+bed, "Days", ((BigDecimal) bc.get("duty_charge_discount")).multiply(bedDays),
						true, true, null,fromTime,toTime,null);
			}
			if (((BigDecimal) bc.get("maintainance_charge")).compareTo(BigDecimal.ZERO) > 0) {
				chargeHeadBean = getChargeHeadForBedType("PC"+bed);
				addBedCharges(bed, isInsurance, visitType, firstOfCategory,
						discCatId, bedType, bedDays, (Integer)chargeHeadBean.get("service_sub_group_id"),
						insuranceCategoryId, charge, (BigDecimal) bc.get("maintainance_charge"),
						"PC"+bed, "Days", ((BigDecimal) bc.get("maintainance_charge_discount")).multiply(bedDays),
						true, true, null,fromTime,toTime,null);
			}
			l.add(charge);
			totalAmount = totalAmount.add((BigDecimal)charge.get("amount"));
		}

		/*
		 * Luxury tax
		 */
		BigDecimal taxPer = (BigDecimal) bc.get("luxary_tax");

		if (taxPer.compareTo(BigDecimal.ZERO) > 0) {
			BasicDynaBean charge = billChargeService.getBean();
			BasicDynaBean chargeHeadBean;
			chargeHeadBean = getChargeHeadForBedType("LTAX");
			BasicDynaBean gprefs = genPrefService.getAllPreferences();
			BigDecimal taxAmount = ((String) gprefs.get("luxary_tax_applicable_on")).equals("B")
				? bedAmount : totalAmount;
			taxAmount = taxAmount.multiply(taxPer).divide(new BigDecimal(100), 2);

			addBedCharges("TAX", isInsurance, visitType, firstOfCategory,
					discCatId, bedType, BigDecimal.ONE, (Integer)chargeHeadBean.get("service_sub_group_id"),
					insuranceCategoryId, charge, taxAmount,
					"LTAX", "", BigDecimal.ZERO,
					true, true, null,fromTime,toTime,"On Bed Charges (" + bedType + ")");
			l.add(charge);
		}

		return l;
	}

	private BasicDynaBean getChargeHeadForBedType(String bed) {
		Map<String,String> filterKeys= new HashMap<String, String>();
		filterKeys.put("chargehead_id", bed);
		BasicDynaBean chargeHeadBean = chargeHeadsService.findByPk(filterKeys);
		return chargeHeadBean;
	}

	private void addBedCharges(String bed, boolean isInsurance,
			String visitType, Boolean firstOfCategory, Integer discCatId,
			String desc, BigDecimal qty, int serviceSubGroupId,
			int insuranceCategoryId, BasicDynaBean chargeBean,
			BigDecimal rate, String chargeHead, String units,
			BigDecimal discount,
			boolean allowRateIncrease, boolean allowRateDecrease, String deptId, 
			Timestamp fromTime, Timestamp toTime, String descId	) {
		billService.addCharges(bed, chargeHead,
				rate, qty,
				discount, units,
				descId, desc, deptId, isInsurance,
				serviceSubGroupId, insuranceCategoryId,firstOfCategory, chargeBean);
		chargeBean.set("allow_rate_increase",allowRateIncrease);
		chargeBean.set("allow_rate_decrease",allowRateDecrease);
		chargeBean.set("from_date",fromTime);
		chargeBean.set("to_date",toTime);
		discountService.applyDiscountRule(chargeBean,discCatId,visitType);
	}
	
	public static BigDecimal[] getBedDaysHours(Timestamp from, Timestamp to, BigDecimal quantity,
			String daycareStatus, boolean isFirstBed, String icuStatus, BasicDynaBean prefs) {

		int[] daysHours;

		if (from == null) {
			if (daycareStatus.equals("Y")) {
				daysHours = new int[]{0, quantity.intValue()};  	// supports only integer values
			} else {
				daysHours = DateUtil.getDaysHours(quantity);
			}
		} else if(daycareStatus.equals("Y")) {
			if (prefs.get("merge_beds").equals("Y"))
				//	 returns days, hours for the given range with out round off
				daysHours = new int[]{ 0, DateUtil.getHours(from, to, false) };
			else
				//  returns days, hours for the given range
				daysHours = new int[]{ 0, DateUtil.getHours(from, to) };
		} else {
			if (prefs.get("merge_beds").equals("Y"))
				daysHours = DateUtil.getDaysHours(from, to, false);
			else
				daysHours = DateUtil.getDaysHours(from, to);
		}

		if (daycareStatus.equals("Y"))
			return new BigDecimal[] {BigDecimal.ZERO, new BigDecimal(daysHours[1])};

		// use days, hours and get number of days and hours to charge for the bed.
		String[] thresholds = getThresholds(prefs, daysHours, isFirstBed, icuStatus);

		String hourlyThreshold = thresholds[0], halfDayThreshold = thresholds[1],
			   fullDayThreshold = thresholds[2];
		int days = daysHours[0], hours = daysHours[1];

		int fullDay = 24;					// > this is treated as full day
		if (!fullDayThreshold.equals("-")) {
			fullDay = Integer.parseInt(fullDayThreshold);
		}

		int halfDay = fullDay;				// (> halfDay, <= fullDay) considered half-a-day.
		if (!halfDayThreshold.equals("-")) {
			halfDay = Integer.parseInt(halfDayThreshold);
		}

		int hourly = halfDay;				// (> hourly, <= halfDay), then it is charged hourly
		if (!hourlyThreshold.equals("-")) {
			hourly = Integer.parseInt(hourlyThreshold);
		}
											// > 0, <= hourly no charges
		BigDecimal bedDays = new BigDecimal(days).setScale(1);
		BigDecimal bedHours = BigDecimal.ZERO;

		if (hours > fullDay) {
			bedDays = bedDays.add(BigDecimal.ONE);
		} else if (hours > halfDay) {
			bedDays = bedDays.add(new BigDecimal("0.5"));
		} else if (hours > hourly) {
			bedHours = new BigDecimal(hours);
		} else {
			// the hours are not charged
		}
		log.debug("Actual: " + days + " Days " + hours + " hours; Thresholds (first bed " + isFirstBed
				+ "): " + hourlyThreshold + "," + halfDayThreshold + "," + fullDayThreshold
				+ " Bed days/hours: " + bedDays + "," + bedHours);

		return new BigDecimal[] {bedDays, bedHours};
	}
	
	public static String[] getThresholds(BasicDynaBean prefs, int[] daysHours, boolean isFirstBed,
			 String isICU) {
		String hrlyThreshHold = null;
		String halfDayThreshold = null;
		String dailyThreshold = null;
		if(isICU.equals("Y")){
			// use days, hours and get number of days and hours to charge for the bed.
			String prefix = isFirstBed ? "icu_" : "icu_bedshift_";
			hrlyThreshHold = 	(String) prefs.get(prefix+"hrly_charge_threshold");
			halfDayThreshold = (String) prefs.get(prefix+"halfday_charge_threshold");
			dailyThreshold = (String) prefs.get(prefix+"fullday_charge_threshold");
		}else{
			// use days, hours and get number of days and hours to charge for the bed.
			String prefix = isFirstBed ? "" : "bedshift_";
			hrlyThreshHold = 	(String) prefs.get(prefix+"hrly_charge_threshold");
			halfDayThreshold = (String) prefs.get(prefix+"halfday_charge_threshold");
			dailyThreshold = (String) prefs.get(prefix+"fullday_charge_threshold");
		}
		return new String[]{hrlyThreshHold,halfDayThreshold,dailyThreshold};
	 }
	
	public static int getIntegerValueFromBean(BasicDynaBean b, String value){
		if (b == null) return 0;
		if (value == null) return 0;
		if (b.get(value) == null) return 0;
		return (Integer) b.get(value);
	}
	
	/*
	 * The following get the duration based on "any part thereof" calculation. Suitable
	 * for use in Equipment charges.
	 */
	public static int getDuration(String fromDateTimeStr, String toDateTimeStr, String units)
		throws ParseException {

		Timestamp from = DateUtil.parseTimestamp(fromDateTimeStr);
		Timestamp to = DateUtil.parseTimestamp(toDateTimeStr);
		return getDuration(from, to, units);
	}

	public static int getDuration(Timestamp from, Timestamp to, String units) {
		return getDuration(from, to, units, 60);
	}

	public static int getDuration(Timestamp from, Timestamp to, String type, int unitSize) {
		long timeDiff = to.getTime() - from.getTime();		// milliseconds
		int minutes = (int) (timeDiff/60/1000);

		int duration;
		if (type.equals("D")) {
			// any part of an hour is considered a full hour, eg, 60 minutes = 1hr, but 61 minutes = 2 hrs
			int hours = minutes/60 + ((minutes%60 > 0) ? 1 : 0);
			duration = hours/24 + ((hours%24>0) ? 1 : 0);
		} else {
			/*
			 * We use ceil (any part thereof): if unitSize is 15, then the following are the conversions:
			 *  0-15: 1, 16-30: 2, 31-45: 3, 46-60: 4,  61-75: 5 ...
			 */
			duration = minutes/unitSize + ((minutes%unitSize > 0) ? 1 : 0);
		}

		return duration;
	}

	/*
	 * 2-slab duration charge calculation
	 */
	public static BigDecimal getDurationCharge(int duration,
			int minDuration, int slab1Duration, int incrDuration,
			BigDecimal minCharge, BigDecimal slab1Rate, BigDecimal incrRate, boolean splitCharge) {

		return getDurationCharge(duration, minDuration, slab1Duration, slab1Duration, incrDuration,
				minCharge, slab1Rate, slab1Rate, incrRate, splitCharge);
	}

	/*
	 * 3-slab duration charge calculation
	 */
	public static BigDecimal getDurationCharge(int duration,
			int minDuration, int slab1Duration, int slab2Duration, int incrDuration,
			BigDecimal minCharge, BigDecimal slab1Rate, BigDecimal slab2Rate,
			BigDecimal incrRate, boolean splitCharge) {

		log.debug("Getting duration charge for " + duration + ": " +
				minDuration + " " + slab1Duration + " " + slab2Duration + " " + incrDuration + " " +
				minCharge + " " + slab1Rate + " " + slab2Rate + " " + incrRate);

		if (duration <= minDuration) {
			return minCharge;

		} else if(duration <= slab1Duration) {
			return slab1Rate;

		} else if(duration <= slab2Duration) {
			return slab2Rate;

		} else {
			if (incrDuration != 0) {
				int addnlUnits = duration - slab2Duration;	// eg, 5 - 4 = 1
				// again we apply ceil, ie, any part thereof will get into the next slot
				// eg, 1-2 => 1, 3-4 => 2 etc.
				int incrUnits = addnlUnits/incrDuration + (addnlUnits%incrDuration > 0 ? 1 : 0);
				if (splitCharge)
					return incrRate.multiply(new BigDecimal(incrUnits));
				else
					return slab2Rate.add(incrRate.multiply(new BigDecimal(incrUnits)));

			} else {
				if (minDuration == 0) // assume plain hourly charge
					return minCharge.add(incrRate.multiply(new BigDecimal(duration)));
				else 	// they don't want the incr charge, only the min charge.
					return minCharge;
			}
		}
	}
	
	public void recalculateChargeAmounts(List<BasicDynaBean> originalCharges, List<BasicDynaBean> newCharges, 
			BasicDynaBean bill, String userName, int[] planIds, String[] preAuthIds, Integer[] preAuthModeIds){
		
		List<BasicDynaBean> deleteCharges = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> updatedCharges = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> insertedCharges = new ArrayList<BasicDynaBean>();
		String origMainChargeId = (String)originalCharges.get(0).get("charge_id");
		BasicDynaBean mainCharge = null;
		if (newCharges.size() > 0) {
			// new charges can be empty if we are removing all charges.
			mainCharge = newCharges.get(0);
			mainCharge.set("charge_refs",null);
		}
		String mainChargeId = null;

		for (BasicDynaBean ch : newCharges) {
			String chargeHead = (String) ch.get("charge_head");
			String units = (String) ch.get("act_unit");
			String descrId = (String) ch.get("act_description_id");
			BasicDynaBean existingCharge = null;

			/*
			 * Find the corresponding existing charge. As long as the charge head, description and units
			 * are the same, we can have a match. Multiple charges may match, eg, GENERAL1, PVT1, GENERAL1,
			 * so we need to remove all matched charges once they are matched. We are doing this only
			 * to save clutter in the audit logs, otherwise we could have just deleted all old charges
			 * and added new ones.
			 */
			for (BasicDynaBean eCharge : originalCharges) {
				if (eCharge.get("charge_head").equals(chargeHead) && eCharge.get("act_unit").equals(units)
						&& eCharge.get("act_description_id").equals(descrId)) {
					existingCharge = eCharge;
					originalCharges.remove(eCharge);
					break;
				}
			}

			if (existingCharge != null) {
				// update this charge with new amounts
				existingCharge = ch;
				updatedCharges.add(existingCharge);
				if (mainChargeId == null) {
					mainChargeId = (String) existingCharge.get("charge_id");
				} else {
					existingCharge.set("charge_refs",mainChargeId);
				}

			} else {
				// need to add a new charge
				bedItemService.setOrderAttributes(ch,billChargeService.getNextPrefixedId(),(String) bill.get("bill_no"), userName,
					null, (String)mainCharge.get("prescribing_dr_id"), DateUtil.getCurrentTimestamp());
				ch.set("hasactivity",true);
				ch.set("activity_conducted",mainCharge.get("activity_conducted"));
				if (mainChargeId == null) {
					mainChargeId = (String)ch.get("charge_id");
				} else {
					ch.set("charge_refs",mainChargeId);
				}
				insertedCharges.add(ch);
			}
		}

		// All remaining charges in original set have to be deleted.
		for (BasicDynaBean ch : originalCharges) {
			ch.set("status","X");
			ch.set("charge_refs",mainChargeId);
			deleteCharges.add(ch);
		}

		/*
		 * If the main charge has changed, remove the old the bill_activity_charge entry,
		 * If insertedCharges has this charge id we will delete activity in activity table
		 * 	  inertCharges call will insert a new one for the new main charge.
		 * else the activity charge_id shd be new mainChargeId.
		 *
		 */
		boolean newChargeForMainChageExists = false;
		for (BasicDynaBean ch : insertedCharges) {
			newChargeForMainChageExists = ( ch.get("charge_id").equals(mainChargeId) );
			if ( newChargeForMainChageExists )
				break;
		}
		if ((newCharges.size() > 0) && !mainChargeId.equals(origMainChargeId)) { }
		
		billChargeService.updateChargeAmounts(updatedCharges);
		billChargeService.batchInsert(insertedCharges);
		billChargeService.updateChargeAmounts(deleteCharges);

		billChargeClaimService.cancelBillChargeClaimAndReference(deleteCharges);//to set claim amt to 0
		if(null != insertedCharges && insertedCharges.size() > 0 && null != planIds && (Boolean)bill.get("is_tpa")){
			billChargeClaimService.insertBillChargeClaims(insertedCharges, planIds, (String)bill.get("visit_id"), bill,
					preAuthIds,preAuthModeIds);
		}		
		if(null != planIds && (Boolean)bill.get("is_tpa"))
			billChargeClaimService.updateBillChargeClaims(updatedCharges, (String)bill.get("visit_id"), (String)bill.get("bill_no"), planIds, false,
					preAuthIds,preAuthModeIds);
	}
	

}
