package com.insta.hms.mdm.caserate;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.mdm.MasterDetailsService;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CaseRateService extends MasterDetailsService {

  public CaseRateService(CaseRateRepository caseRateRepository, CaseRateValidator caseRateValidator,
                         CaseRateDetailRepository caseRateDetailsRepository) {
    super(caseRateRepository, caseRateValidator, caseRateDetailsRepository);
  }

  /**
   * Find Case rate by filter.
   *
   * @param params filter parameters
   * @return result map
   */
  public Map<String, Object> findByFilters(Map<String, String> params) {
    return ((CaseRateRepository) getRepository()).findByFilters(params);
  }
  
  /**
   * Get category details by plan id.
   *
   * @param planId identifier of insurance plan
   * @return response map
   */
  public Map<String, Object> getCategoryDetailsByPlanId(Integer planId) {
    Map<String, Object> result = new HashMap<>();
    result.put("category_details", ConversionUtils
        .listBeanToListMap(((CaseRateRepository) getRepository())
        .getCategoryDetailsByPlanId(planId)));
    return result;
  }
}
