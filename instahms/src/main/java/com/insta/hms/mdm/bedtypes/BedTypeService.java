package com.insta.hms.mdm.bedtypes;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.beddetails.BedDetailsRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/** The Class BedTypeService. */
@Service
public class BedTypeService extends MasterService {

  /** The bed type repository. */
  @Autowired private BedTypeRepository bedTypeRepository;

  /** The ICU bed type repository. */
  @LazyAutowired private IcuBedChargesRepository icuBedChargesRepository;

  /** The bed Details repository. */
  @LazyAutowired private BedDetailsRepository bedDetailsRepository;

  /**
   * Instantiates a new bed type service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public BedTypeService(BedTypeRepository repository, BedTypeValidator validator) {
    super(repository, validator);
  }

  /**
   * Look up active billing bed.
   *
   * @return the list
   */
  public List<BasicDynaBean> lookUpActiveBillingBed() {
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("billing_bed_type", "Y");
    return ((BedTypeRepository) getRepository()).lookup(true, filterMap);
  }

  /**
   * Gets the all bed types.
   *
   * @return the all bed types
   */
  public List<BasicDynaBean> getAllBedTypes() {
    List<BasicDynaBean> bedTypes = bedTypeRepository.getAllActiveBedTypes();
    // need to do discussion
    /*
     * ArrayList<String> duplicateList = new ArrayList<String> (); Iterator<BasicDynaBean> it =
     * bedTypes.iterator(); while(it.hasNext()){ String bed = (String) it.next().get("bed_type");
     * if(!bed.equals("GENERAL")){ duplicateList.add(bed); } }
     */
    return bedTypes;
  }

  /**
   * Gets the inactive beds.
   *
   * @return the inactive beds
   */
  public List<BasicDynaBean> getInactiveBeds() {
    return bedTypeRepository.getInactiveBeds();
  }

  /**
   * Gets the bed item sub group tax details.
   *
   * @param actDescriptionId the act description id
   * @return the bed item sub group tax details
   */
  public List<BasicDynaBean> getBedItemSubGroupTaxDetails(String actDescriptionId) {
    return ((BedTypeRepository) getRepository()).getBedItemSubGroupTaxDetails(actDescriptionId);
  }

  /**
   * Gets the bed type sub group tax details.
   *
   * @param actDescriptionId the act description id
   * @return the bed type sub group tax details
   */
  public List<BasicDynaBean> getBedTypeSubGroupTaxDetails(String actDescriptionId) {
    // TODO Auto-generated method stub
    return ((BedTypeRepository) getRepository()).getBedTypeSubGroupTaxDetails(actDescriptionId);
  }

  
  /**
   * Gets the ICU bed charges list.
   *
   * @param bedType the bed type
   * @param ratePlan the rate plan
   * @return the ICU bed charges list
   */
  public List<BasicDynaBean> getIcuBedChargesList(String bedType, String ratePlan) {
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("", bedType);
    filterMap.put("", ratePlan);
    return icuBedChargesRepository.listAll(null, filterMap, null);
  }

  
  /**
   * Gets the normal bed charges list.
   *
   * @param bedType the bed type
   * @return the normal bed charges list
   */
  public List<BasicDynaBean> getNormalBedChargesList(String bedType) {
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("", bedType);
    return bedDetailsRepository.listAll(null, filterMap, null);
  }

 
  /**
   * Gets the bed type details.
   *
   * @param bedType the bed type
   * @return the bed type details
   */
  public BasicDynaBean getBedTypeDetails(String bedType) {
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("", bedType);
    return bedDetailsRepository.findByKey(filterMap);
  }

  /**
   * Method to get bed type list.
   * 
   * @param listItemIds the list of bedIds
   * @return list of bedTypes
   */
  public List<String> getBedTypeList(List<String> listItemIds) {
    return bedTypeRepository.getBedTypeList(listItemIds);
  }

  /**
   * Get all active bed types of packages.
   * @return list of bed types
   */
  public List<BasicDynaBean> getAllSortedBedTypes() {
    List<BasicDynaBean> bedTypes = bedTypeRepository.getAllSortedBedTypes();
    return bedTypes;
  }

  /**
   * Get list of bed type names.
   * @return list of bed type names
   */
  public List<String> getAllBedTypeNames() {
    List<BasicDynaBean> bedTypes = bedTypeRepository.getAllSortedBedTypes();
    List<String> bedTypeNames = new ArrayList<>();
    for (BasicDynaBean bean : bedTypes) {
      bedTypeNames.add((String) bean.get("bed_type"));
    }
    return bedTypeNames;
  }
}
