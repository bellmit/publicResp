package com.insta.hms.mdm.item;

import com.insta.hms.mdm.MasterDetailsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * The Class StoreItemDetailsService.
 *
 * @author irshadmohammed
 */

@Service
public class StoreItemDetailsService extends MasterDetailsService {

  /**
   * Instantiates a new store item details service.
   *
   * @param repository
   *          the repository
   * @param validator
   *          the validator
   * @param detailsRepository
   *          the details repository
   */
  public StoreItemDetailsService(StoreItemDetailsRepository repository,
      StoreItemDetailsValidator validator, StoreItemSubgroupRepository detailsRepository) {
    super(repository, validator, detailsRepository);
  }

  /**
   * Gets the subgroups.
   *
   * @param medicineId
   *          the medicine id
   * @return the subgroups
   */
  public List<BasicDynaBean> getSubgroups(int medicineId) {
    return ((StoreItemDetailsRepository) getRepository()).getSubgroups(medicineId);
  }
  
  public List<BasicDynaBean> getSubgroups(int medicineId, int storeRatePlanId) {
    return ((StoreItemDetailsRepository) getRepository()).getSubgroups(medicineId, storeRatePlanId);
  }

  public BasicDynaBean getMedicineDetails(Integer medicineId) {
    return ((StoreItemDetailsRepository) getRepository()).getMedicineDetails(medicineId);
  }
  
  public List<BasicDynaBean> getMedicineSubgroups(int medicineId) {
    return ((StoreItemDetailsRepository) getRepository()).getMedicineSubgroups(medicineId);
  }
  
  public List<BasicDynaBean> getStoreTariffSubgroups(int itemId, int storeRatePlanId) {
    return ((StoreItemDetailsRepository) getRepository()).getStoreTariffSubgroups(itemId,
        storeRatePlanId);
  }

  public List<BasicDynaBean> getMedicinesByIds(List<Integer> medicineIds) {
    return ((StoreItemDetailsRepository) getRepository()).getMedicinesByIds(medicineIds);
  }

}
