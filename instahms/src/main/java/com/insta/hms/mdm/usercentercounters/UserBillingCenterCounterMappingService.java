package com.insta.hms.mdm.usercentercounters;


import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.QueryAssembler;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.MasterDetailsService;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.counters.CounterService;
import com.insta.hms.security.usermanager.UserRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserBillingCenterCounterMappingService extends MasterDetailsService {
  
  @LazyAutowired
  private CounterService counterService;
  
  @LazyAutowired
  private CenterService centerService;
  
  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;
  
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;
  
  @LazyAutowired
  private UserBillingCenterCounterMappingRepository billingCounterMappingRepository;

  /** The Constant USER_FIELDS. */
  private static final String USER_FIELDS = "SELECT * ";

  /** The Constant USER_FROM. */
  private static final String USER_FROM =  " FROM "
        + " ( (SELECT ucbc.emp_username, ucbc.counter_id, "
        + "   ucbc.center_id, u.temp_username, u.emp_status "
        + "   FROM user_center_billing_counters ucbc  "
        + "   LEFT JOIN u_user u on (ucbc.emp_username = u.emp_username) ) "
        + "   UNION "
        + " ( SELECT emp_username, pharmacy_counter_id as counter_id, "
        + "   center_id, temp_username, emp_status from u_user ))"
        + "   as foo ";
  
  /** The Constant ACTIVE_STATUS. */
  private static final String ACTIVE_STATUS = "A";
  
  public UserBillingCenterCounterMappingService(UserRepository repository,
      UserBillingCenterCounterMappingValidator validator,
      UserBillingCenterCounterMappingRepository detailsRepository) {
    super(repository, validator, detailsRepository);
  }
  
  /**
   * To bean.
   *
   * @param requestParams the request params
   * @param fileMap       the file map
   * @return the basic dyna bean
   */
  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterService#toBean(java.util.Map, java.util.Map)
   */
  @Override
  public BasicDynaBean toBean(Map<String, String[]> requestParams,
      Map<String, MultipartFile> fileMap) {
    //BasicDynaBean bean = super.toBean(requestParams, fileMap);
    String userName = requestParams.get("userName")[0];
    BasicDynaBean bean = this.getRepository().getBean();
    bean.set("emp_username", userName);
    return bean;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterService#toBeanList(java.util.Map,
   * org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public Map<String, List<BasicDynaBean>> toBeanList(Map<String, String[]> requestParams,
      BasicDynaBean type) {
    Map<String, List<BasicDynaBean>> beanListMap = super.toBeanList(requestParams, type);
    Map session = sessionService.getSessionAttributes();
    List<MasterRepository<?>> detailsRepositories = getDetailRepository();
    if (detailsRepositories == null || detailsRepositories.isEmpty()) {
      return beanListMap;
    }
    MasterRepository<?> detailRepo = detailsRepositories.get(0);
    List<BasicDynaBean> insertedBeans = beanListMap.get(detailRepo.getBeanName() + "_inserted");
    List<BasicDynaBean> updatedBeans = beanListMap.get(detailRepo.getBeanName() + "_updated");
    for (BasicDynaBean bean : insertedBeans) {
      bean.set("created_by", session.get("userId"));
    }
    for (BasicDynaBean bean : updatedBeans) {
      bean.set("updated_by", session.get("userId"));
      bean.set("updated_at", DateUtil.getCurrentTimestamp());
    }
    return beanListMap;
  }
  
  
  /**
   * Gets the adds the show page data.
   *
   * @param paramMap the param map
   * @return the adds the show page data
   */
  @SuppressWarnings("rawtypes")
  public Map<String, List<BasicDynaBean>> getAddShowPageData(Map paramMap) {

    Map<String, List<BasicDynaBean>> addEditReference = new HashMap<>();
    BasicDynaBean genPrefBean = genericPreferencesService.getAllPreferences();
    int maxCenter = (int) genPrefBean.get("max_centers_inc_default");
    if (maxCenter == 1) {
      addEditReference.put("centerList", centerService.listAll(true));
    } else {
      addEditReference.put("centerList", centerService.listAll(false));
    }
    List<BasicDynaBean> defaultCenterBean = new ArrayList<>();
    defaultCenterBean.add(centerService.findByKey(0));
    addEditReference.put("defaultCenterBean", defaultCenterBean);
    addEditReference.put("counterList", counterService.getAllActiveBillingCounters());
    String userName = ((String[]) paramMap.get("emp_username"))[0];
    addEditReference.put("mappedCounterList",
        billingCounterMappingRepository.getMappedCounterList(userName));
    return addEditReference;
  }
  
  public int deleteMappedCounter(String userName) {
    return billingCounterMappingRepository.delete("emp_username", userName);
  }
  
  public BasicDynaBean getMappedCounterForCenter(String userName, Integer centerId) {
    return billingCounterMappingRepository.getMappedCounterForCenter(userName, centerId);
  }

  /**
   * Lookup users.
   *
   * @param parameters the parameters
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<String> lookupUsers(Map<String, String[]> parameters) {
    Integer centerId = null;
    if (parameters.get("center_id") != null ) {
      centerId = Integer.valueOf(parameters.get("center_id")[0]);
    } else {
      BasicDynaBean genericPrefs = genericPreferencesService.getPreferences();
      if ((Integer) genericPrefs.get("max_centers_inc_default") > 1) {
        centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
      }
    }
    SearchQueryAssembler qb = new SearchQueryAssembler(USER_FIELDS, null, USER_FROM,
        ConversionUtils.getListingParameter(parameters));
    qb.addFilter(QueryAssembler.STRING, "emp_username", 
            "ILIKE", parameters.get("filterText")[0]);
    if ( centerId != null && centerId != 0 ) {
      qb.addFilter(QueryAssembler.INTEGER, "center_id","=",centerId);
    }
    if (parameters.containsKey("counter_id") 
            && !parameters.get("counter_id")[0].trim().isEmpty()) {
      String counterId = parameters.get("counter_id")[0].trim();
      qb.addFilter(QueryAssembler.STRING, "counter_id","=",counterId);
    }
    qb.addFilter(QueryAssembler.STRING, "emp_status","=",ACTIVE_STATUS);    
    qb.build();
    PagedList pagedList = qb.getMappedPagedList();
    return pagedList.getDtoList();
  }

}
