package com.insta.hms.mdm.consumptionuom;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * Consumption UOM Repository.
 * 
 * @author VinayKumarJavalkar
 */
@Repository
public class ConsumptionUOMRepository extends MasterRepository<Integer> {

  public static final String CONS_UOM_ID = "cons_uom_id";
  public static final String CONSUMPTION_UOM = "consumption_uom";
  
  public ConsumptionUOMRepository() {
    super("consumption_uom_master", CONS_UOM_ID, CONSUMPTION_UOM);
  }
}