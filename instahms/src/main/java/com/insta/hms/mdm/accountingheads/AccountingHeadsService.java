package com.insta.hms.mdm.accountingheads;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class AccountingHeadsService.
 */
@Service
public class AccountingHeadsService extends MasterService {

  /**
   * Instantiates a new accounting heads service.
   *
   * @param accountingHeadsRepository the accounting heads repository
   * @param accountingHeadsValidator the accounting heads validator
   */
  public AccountingHeadsService(AccountingHeadsRepository accountingHeadsRepository,
      AccountingHeadsValidator accountingHeadsValidator) {
    super(accountingHeadsRepository, accountingHeadsValidator);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterService#search(java.util.Map)
   */
  @Override
  public PagedList search(Map params) {
    Map<LISTING, Object> listingParameterMap = ConversionUtils.getListingParameter(params);
    if (!params.containsKey("pageSize") && !params.containsKey("page_size")) {
      listingParameterMap.put(LISTING.PAGESIZE, 2000);
    }
    return super.search(params, listingParameterMap);
  }

  /**
   * Preprocess params.
   *
   * @param parameterMap the parameter map
   * @return the map
   */
  public Map<String, String[]> preprocessParams(Map<String, String[]> parameterMap) {
    Map<String, String[]> processedParams = new HashMap<>(parameterMap);
    if (ArrayUtils.isNotEmpty(parameterMap.get("status"))) {
      String status = StringUtils.trimToEmpty(parameterMap.get("status")[0]);
      if (status.equalsIgnoreCase("Active")) {
        processedParams.put("status", new String[] {"A"});
      } else if (status.equalsIgnoreCase("Inactive")) {
        processedParams.put("status", new String[] {"I"});
      } else if (status.equalsIgnoreCase("All")) {
        processedParams.remove("status");
      }
    }
    return processedParams;
  }


}
