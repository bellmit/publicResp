package com.insta.hms.mdm.storesrateplanmaster;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class StoresRatePlanService.
 *
 * @author yashwant
 */
@Service
public class StoresRatePlanService extends MasterService {  
  
  private static final String STORE_RATE_PLAN_ID = "store_rate_plan_id";
  
  @LazyAutowired
  private StoresRatePlanRepository storesRatePlanRepository;

  /**
   * Instantiates a new stores rate plan service.
   *
   * @param repository
   *          the repository
   * @param validator
   *          the validator
   */
  public StoresRatePlanService(StoresRatePlanRepository repository,
      StoresRatePlanValidator validator) {
    super(repository, validator);
  }
  
  
  /**
   * Gets the store rate plan map.
   *
   * @return the store rate plan map
   */
  public Map<Integer, BasicDynaBean> getStoreRatePlanMap() {
    Map<Integer, BasicDynaBean> itemSubGrpMaps = new HashMap<>();
    List<BasicDynaBean> list = storesRatePlanRepository.listAll(null, "status", "A");
    for (BasicDynaBean bean : list) {
      itemSubGrpMaps.put((Integer) bean.get(STORE_RATE_PLAN_ID),bean);
    }
    return itemSubGrpMaps;
  }

  public List<BasicDynaBean> listAll() {
    return getRepository().listAll();
  }
  
}
