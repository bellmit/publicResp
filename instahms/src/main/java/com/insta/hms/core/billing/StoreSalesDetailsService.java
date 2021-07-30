package com.insta.hms.core.billing;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StoreSalesDetailsService {
	
	@LazyAutowired
	StoreSalesDetailsRepository storeSalesdetailsRepository;
	
	@LazyAutowired
	GenericPreferencesService genPrefService;
	
	@LazyAutowired
	BillClaimRepository billClaimRepository;
	
	@LazyAutowired
	PatientRegistrationRepository patientRegistrationRepository;
	
	public List<BasicDynaBean> findAllCharges(String billNo) {
		
		return storeSalesdetailsRepository.findAllCharges(billNo, genPrefService.getAllPreferences());
	}
	
  /**
   * Find all by key.
   *
   * @param keyMap the key map
   * @return the list
   */
  public List<BasicDynaBean> findAllByKey(Map keyMap) {
    return storeSalesdetailsRepository.findAllByKey(keyMap);
  }
  
  /**
   * Gets store item id which are marked as drug category.
   * 
   * @param saleId the saleid
   * @return list
   */
  public List<Integer> filterMedicineSaleItemId(String saleId) {
    List<BasicDynaBean> beanList = storeSalesdetailsRepository.filterMedicineSaleItemId(saleId);
    List<Integer> saleItemIds = new ArrayList<>();
    for (BasicDynaBean bean : beanList) {
      saleItemIds.add((int)bean.get("sale_item_id"));
    }
    return saleItemIds;
  }
}
