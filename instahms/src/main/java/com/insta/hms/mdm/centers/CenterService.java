package com.insta.hms.mdm.centers;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.common.utils.JsonUtility;
import com.insta.hms.core.patient.registration.RegistrationPreferencesService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.accountpreferences.AccountingPreferenceService;
import com.insta.hms.mdm.cities.CityService;
import com.insta.hms.mdm.countries.CountryService;
import com.insta.hms.mdm.regions.RegionService;

import com.insta.hms.mdm.usercentercounters.UserBillingCenterCounterMappingService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * The Class CenterService.
 *
 * @author yashwant
 * 
 *         Center service is having type of master service and it is a public because it can call
 *         from many other service.
 */

@Service
public class CenterService extends MasterService {

  /** The city service. */
  @LazyAutowired
  private CityService cityService;

  /** The accounting preference service. */
  @LazyAutowired
  private AccountingPreferenceService accountingPreferenceService;

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The registration preference service. */
  @LazyAutowired
  private RegistrationPreferencesService registrationPreferencesService;

  /** The region service. */
  @LazyAutowired
  private RegionService regionService;

  /** The country service. */
  @LazyAutowired
  private CountryService countryService;
  
  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private CenterRepository centerRepository;

  @LazyAutowired
  private UserBillingCenterCounterMappingService counterMappingService;
  
  /**
   * Instantiates a new center service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public CenterService(CenterRepository repository, CenterValidator validator) {
    super(repository, validator);
  }

  /**
   * List all.
   *
   * @param includeDefaultCenter the include default center
   * @return the list
   */
  public List<BasicDynaBean> listAll(Boolean includeDefaultCenter) {

    List<BasicDynaBean> centerList = new ArrayList<BasicDynaBean>();
    centerList.addAll(getRepository().listAll(null, "status", "A", "center_name"));

    /* Removing default center from list */
    if (!includeDefaultCenter) {

      ListIterator<BasicDynaBean> lit = centerList.listIterator();
      while (lit.hasNext()) {
        BasicDynaBean listBean = lit.next();
        if ((Integer) listBean.get("center_id") == 0) {
          lit.remove();
        }
      }

    }
    return centerList;

  }

  /**
   * Gets the adds the edit page data.
   *
   * @param params the params
   * @return the adds the edit page data
   */
  @SuppressWarnings("rawtypes")
  public Map<String, List<BasicDynaBean>> getAddEditPageData(Map params) {
    Map<String, List<BasicDynaBean>> referenceMap = new HashMap<String, List<BasicDynaBean>>();

    referenceMap.put("cityStateCountryList", cityService.getCityStateCountryList());
    referenceMap.put("regionBean", regionService.getAllRegions());
    List<BasicDynaBean> regPrefs = new ArrayList<BasicDynaBean>();
    regPrefs.add(registrationPreferencesService.getRegistrationPreferences());
    referenceMap.put("regPref", regPrefs);
    List<BasicDynaBean> accPrefs = new ArrayList<BasicDynaBean>();
    accPrefs.add(accountingPreferenceService.getAllPreferences());
    referenceMap.put("acc_prefs", accPrefs);

    return referenceMap;
  }

  @Override
  public Integer insert(BasicDynaBean bean) {
    List<BasicDynaBean> rowList = ((CenterRepository) getRepository()).listAll();
    Integer maxCenterAllowed =
        (Integer) genericPreferencesService.getAllPreferences().getMap()
            .get("max_centers_inc_default");

    // TODO:move validation to validator
    if (rowList.size() >= maxCenterAllowed) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("Note", "exception.center.maxcenterchecks", null);
      throw new ValidationException(errorMap);
    }
    return super.insert(bean);
  }

  /**
   * Gets the country code.
   *
   * @param centerId the center id
   * @return the country code
   */
  public String getCountryCode(int centerId) {
    Map<String, Object> params = new HashMap<String, Object>();
    String countryId =
        (String) this.getRepository().findByKey("center_id", centerId).get("country_id");
    BasicDynaBean country = countryService.getCountry("country_id", countryId);
    if (country != null) {
      return (String) country.get("country_code");
    }
    return null;
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return ((CenterRepository) getRepository()).getBean();
  }

  /**
   * Gets the centers list.
   *
   * @return the centers list
   */
  public List<BasicDynaBean> getCentersList() {
    return ((CenterRepository) getRepository()).getCentersList();
  }

  /**
   * Gets active centers list.
   *
   * @return the active centers list
   */
  public List<BasicDynaBean> getActiveCentersList() {
    return ((CenterRepository) getRepository()).getActiveCentersList();
  }

  /**
   * Gets the all centers map.
   *
   * @return the all centers map
   */
  public Map<String, Integer> getAllCentersMap() {
    List<BasicDynaBean> centersList = ((CenterRepository) getRepository()).getAllCentersLList();
    Map<String, Integer> centersMap = new HashMap<String, Integer>();
    for (BasicDynaBean bean : centersList) {
      centersMap.put((String) bean.get("center_name"), (Integer) bean.get("center_id"));
    }
    return centersMap;
  }

  /**
   * Gets the center details list.
   *
   * @param centerNames the center names
   * @return the center details list
   */
  public List<BasicDynaBean> getCenterDetailsList(String[] centerNames) {
    return ((CenterRepository) getRepository()).getCenterDetailsList(centerNames);
  }

  /**
   * Gets the saved centers.
   *
   * @param resultlabelId the resultlabel id
   * @return the saved centers
   */
  public List<String> getSavedCenters(Integer resultlabelId) {
    List<BasicDynaBean> centersList =
        ((CenterRepository) getRepository()).getSavedCenters(resultlabelId);
    List<String> centerNameList = new ArrayList<String>();
    for (BasicDynaBean bean : centersList) {
      centerNameList.add((String) bean.get("center_name"));
    }
    return centerNameList;
  }

  /**
   * Gets the all centers details.
   *
   * @return the all centers details
   */
  public List<BasicDynaBean> getAllCentersDetails() {
    return ((CenterRepository) getRepository()).getAllCentersDetails();
  }

  /**
   * Gets the all centers and super center as first.
   *
   * @return the all centers and super center as first
   */
  public List<BasicDynaBean> getAllCentersAndSuperCenterAsFirst() {
    return ((CenterRepository) getRepository()).getAllCentersAndSuperCenterAsFirst();
  }

  /**
   * Find by key.
   *
   * @param centerId the center id
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(int centerId) {
    return ((CenterRepository) getRepository()).findByKey("center_id", centerId);
  }

  /**
   * Gets the center details.
   *
   * @param centerId the center id
   * @return the center details
   */
  public BasicDynaBean getCenterDetails(int centerId) {
    return ((CenterRepository) getRepository()).getCenterDetails(centerId);

  }

  /**
   * Gets the dhpo info center wise.
   *
   * @param visitId the visit id
   * @return the dhpo info center wise
   */
  public BasicDynaBean getDhpoInfoCenterWise(String visitId) {
    return ((CenterRepository) getRepository()).getDhpoInfoCenterWise(visitId);
  }

  /**
   * Gets the all centers except super.
   *
   * @return the all centers except super
   */
  public List<BasicDynaBean> getAllCentersExceptSuper() {
    return ((CenterRepository) getRepository()).getAllCentersExceptSuper();
  }

  /**
   * Gets the all centres.
   *
   * @return the all centres
   */
  public List<BasicDynaBean> getAllCentres() {
    return ((CenterRepository) getRepository()).getAllCentres();
  }

  /**
   * Gets the all centers data.
   *
   * @param sendOnlyActiveData the send only active data
   * @return the all centers data
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getAllCentersData(boolean sendOnlyActiveData) {
    return ConversionUtils.listBeanToListMap(((CenterRepository) getRepository())
        .getAllCentersData(sendOnlyActiveData));
  }

  /**
   * Gets the user centers.
   *
   * @param parameters the parameters
   * @param centerId the center id
   * @return the user centers
   */
  public Map<String, Object> getUserCenters(Map<String, String[]> parameters, Integer centerId) {
    List<BasicDynaBean> searchSet = new ArrayList<>();
    if (centerId > 1) {
      BasicDynaBean centerBean = findByKey(centerId);
      searchSet.add(centerBean);
    } else {
      String filterText =
          (null != parameters && parameters.containsKey("filterText")) ? parameters
              .get("filterText")[0] : null;
      searchSet = (null != filterText) ? autocomplete(filterText, parameters) : lookup(true);
    }
    Map<String, Object> centersMap = new ModelMap();
    centersMap.put("dtoList", ConversionUtils.listBeanToListMap(searchSet));
    centersMap.put("listSize", searchSet.size());
    return centersMap;
  }

  /**
   * Gets the health authority.
   *
   * @param centerId the center id
   * @return the health authority
   */
  public String getHealthAuthority(Integer centerId) {
    return (String) findByKey(centerId).get("health_authority");
  }

  /**
   * Centers include default.
   *
   * @return the map
   */
  public Map<String, Object> centersIncludeDefault() {
    Map<String, Object> map = new HashMap();
    map.put("centerIncDefault", ConversionUtils
        .listBeanToListMap(((CenterRepository) getRepository()).getCentersIncDefaultFirst()));
    map.put("sessionMap", sessionService.getSessionAttributes());
    
    return map;
  }
  
  public BasicDynaBean getCenterDefaults(Integer centerId) {
    return centerRepository.getCenterDefaults(centerId);
  }
  
  public Map<String,Object> getReportingMeta(Integer centerId) {
    return JsonUtility.toObjectMap((String) findByKey(centerId).get("reporting_meta"));
  }
  
  /**
   * Get the name of the billing counter mapped to a user for a center.
   *
   * @param userId emp user name of the user
   * @param centerId center id
   * @return name of the mapped billing counter
   */
  @Transactional(readOnly = true)
  public String getMappedBillingCounter(String userId, Integer centerId) {
    BasicDynaBean mappedCounterBean =
        counterMappingService.getMappedCounterForCenter(userId, centerId);
    return mappedCounterBean != null ? (String) mappedCounterBean.get("counter_no") : null;
  }
  
}
