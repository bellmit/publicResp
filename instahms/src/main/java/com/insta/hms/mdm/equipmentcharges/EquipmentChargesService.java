package com.insta.hms.mdm.equipmentcharges;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.bulk.BulkDataService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EquipmentChargesService extends BulkDataService {

  @LazyAutowired private EquipmentChargesRepository equipmentChargesRepository;

  public EquipmentChargesService(
      EquipmentChargesRepository repository,
      EquipmentChargesValidator validator,
      EquipmentChargesCsvBulkDataEntity csvEntity) {
    super(repository, validator, csvEntity);
    // TODO Auto-generated constructor stub
  }

  @Override
  public Map<String, List<BasicDynaBean>> getMasterData() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * get list of all charges based on orgId.
   *
   * @param orgId String
   * @param ids List
   * @return List
   */
  public List<BasicDynaBean> getAllChargesForOrg(String orgId, List<String> ids) {

    List<BasicDynaBean> chargeList =
        equipmentChargesRepository.getAllChargesForBedtypes(orgId, ids);

    return chargeList;
  }

  /**
   * Initialize Item charges.
   *
   * @param equipmentId String
   * @return boolean
   */
  public boolean initItemCharges(String equipmentId) {

    boolean success = true;
    success = equipmentChargesRepository.initItemCharges(equipmentId);
    return success;
  }

  /**
   * get all charges for org equipment.
   *
   * @param orgId String
   * @param equipId String
   * @return List
   */
  public List<BasicDynaBean> getAllChargesForOrgEquipment(String orgId, String equipId) {

    List<String> ids = new ArrayList();
    ids.add(equipId);
    return getAllChargesForOrg(orgId, ids);
  }

  /**
   * get derived rate plan details.
   *
   * @param orgId String
   * @param equipId String
   * @return List of BasicDynaBean
   */
  public List<BasicDynaBean> getDerivedRatePlanDetails(String orgId, String equipId) {
    List<BasicDynaBean> getRatePlanDetails =
        EquipmentChargesRepository.getDerivedRatePlanDetails(orgId, equipId);
    return getRatePlanDetails;
  }

  /**
   * update equipment charges.
   *
   * @param parameters Map
   * @param equipId String
   * @param orgId String
   * @param msg StringBuilder
   * @return Map
   * @throws Exception Exception
   */
  public boolean updateEquipCharges(
      Map<String, String[]> parameters, String equipId, String orgId, StringBuilder msg)
      throws Exception {
    boolean success = true;
    String[] beds = parameters.get("bed_type");
    ArrayList errors = new ArrayList();
    List<BasicDynaBean> chargeList = new ArrayList();

    for (int i = 0; i < beds.length; i++) {
      BasicDynaBean charge = equipmentChargesRepository.getBean();
      ConversionUtils.copyToDynaBean(parameters, charge, errors);
      ConversionUtils.copyIndexToDynaBean(parameters, i, charge, errors);
      chargeList.add(charge);
    }
    if (!errors.isEmpty()) {
      msg = msg.append("Incorrectly formatted values supplied");
      return false;
    }

    for (BasicDynaBean ecbean : chargeList) {
      Map keys = new HashMap();
      keys.put("equip_id", ecbean.get("equip_id"));
      keys.put("org_id", ecbean.get("org_id"));
      keys.put("bed_type", ecbean.get("bed_type"));
      success = equipmentChargesRepository.updateEquipCharge(ecbean, keys);
    }
    if (!success) {
      return false;
    }
    String[] derivedRateplanIds = parameters.get("ratePlanId");
    if (null != derivedRateplanIds && derivedRateplanIds.length > 0) {

      String[] dailyCharge = parameters.get("daily_charge");
      String[] minCharge = parameters.get("min_charge");
      String[] incrCharge = parameters.get("incr_charge");
      String[] slab1Charge = parameters.get("slab_1_charge");

      String[] dailyChargeDiscount = parameters.get("daily_charge_discount");
      String[] minChargeDiscount = parameters.get("min_charge_discount");
      String[] incrChargeDiscount = parameters.get("incr_charge_discount");
      String[] slab1ChargeDiscount = parameters.get("slab_1_charge_discount");
      String[] tax = parameters.get("tax");

      Double[] dailyChg = new Double[dailyCharge.length];
      Double[] minChg = new Double[minCharge.length];
      Double[] incrChg = new Double[incrCharge.length];
      Double[] slabChg = new Double[slab1Charge.length];
      Double[] dailyDisc = new Double[dailyChargeDiscount.length];
      Double[] minDisc = new Double[minChargeDiscount.length];
      Double[] incrDisc = new Double[incrChargeDiscount.length];
      Double[] slabDisc = new Double[slab1ChargeDiscount.length];
      Double[] equipTax = new Double[tax.length];

      for (int i = 0; i < dailyCharge.length; i++) {
        dailyChg[i] = new Double(dailyCharge[i]);
        minChg[i] = new Double(minCharge[i]);
        incrChg[i] = new Double(incrCharge[i]);
        slabChg[i] = new Double(slab1Charge[i]);
        dailyDisc[i] = new Double(dailyChargeDiscount[i]);
        minDisc[i] = new Double(minChargeDiscount[i]);
        incrDisc[i] = new Double(incrChargeDiscount[i]);
        slabDisc[i] = new Double(slab1ChargeDiscount[i]);
        equipTax[i] = new Double(tax[i]);
      }
      success =
          equipmentChargesRepository.updateChargesForDerivedRatePlans(
              orgId,
              derivedRateplanIds,
              beds,
              dailyChg,
              minChg,
              incrChg,
              slabChg,
              equipId,
              dailyDisc,
              minDisc,
              incrDisc,
              slabDisc,
              equipTax);
    }

    return success;
  }
}
