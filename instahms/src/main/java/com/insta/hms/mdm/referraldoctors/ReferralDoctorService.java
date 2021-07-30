package com.insta.hms.mdm.referraldoctors;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.QueryAssembler;
import com.insta.hms.common.QueryBuilder;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.registration.RegistrationPreferencesService;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReferralDoctorService extends MasterService {
  
  /** The reg pref service. */
  @LazyAutowired
  private RegistrationPreferencesService regPrefService;

  public ReferralDoctorService(ReferralDoctorRepository repo, ReferralDoctorValidator validator) {
    super(repo, validator);
  }

  public BasicDynaBean findByKey(String referalNo) {
    return ((ReferralDoctorRepository) this.getRepository()).findByKey("referal_no", referalNo);
  }

  @Override
  public List<BasicDynaBean> autocomplete(String match, Map<String, String[]> parameters) {
    return autocomplete("referal_name", match, false, parameters);
  }

  @Override
  public SearchQueryAssembler getLookupQueryAssembler(
      String lookupQuery, Map<String, String[]> parameters) {
    SearchQueryAssembler qb = null;
    qb =
        new SearchQueryAssembler(
            lookupQuery, null, null, ConversionUtils.getListingParameter(parameters));
    // Autocomplete is for center_id from the parameters
    String[] centerIds =
        (null != parameters && parameters.containsKey("center_id"))
            ? parameters.get("center_id")
            : null;
    List<Integer> valueList = new ArrayList<Integer>();
    valueList.add(0);
    if (centerIds != null) {
      for (String centerId : centerIds) {
        valueList.add(Integer.parseInt(centerId));
      }
    }
    qb.addFilter(QueryBuilder.INTEGER, "center_id", "IN", valueList);

    return qb;
  }

  @Override
  public void addFilterForLookUp(
      SearchQueryAssembler qb,
      String likeValue,
      String matchField,
      boolean contains,
      Map<String, String[]> parameters) {
    if (!likeValue.trim().isEmpty()) {
      String filterText = likeValue.trim() + "%";
      if (contains) {
        filterText = "%" + likeValue.trim() + "%";
      }
      ArrayList<Object> types = new ArrayList<Object>();
      types.add(QueryAssembler.STRING);
      types.add(QueryAssembler.STRING);
      ArrayList<String> values = new ArrayList<String>();
      values.add(filterText);
      values.add(filterText);
      qb.appendExpression(
          " ( " + matchField + " ILIKE ? " + "OR  clinician_id ILIKE ? ) ", types, values);
    }
    
    Map regPrefMap = regPrefService.getRegistrationPreferences().getMap();
    if (regPrefMap.get("show_referrral_doctor_filter") != null
        && ((String) regPrefMap.get("show_referrral_doctor_filter")).equals("Y")) {
      String countryId = (null != parameters && parameters.containsKey("country_id")) ? parameters
          .get("country_id")[0] : null;
      if (countryId != null && !"".equals(countryId.trim())) {
        qb.addFilter(QueryAssembler.STRING, "country_id", "=", countryId);
      }
      String stateId = (null != parameters && parameters.containsKey("state_id")) ? parameters
          .get("state_id")[0] : null;
      if (stateId != null && !"".equals(stateId.trim())) {
        qb.addFilter(QueryAssembler.STRING, "state_id", "=", stateId);
      }
      String districtId = (null != parameters && parameters.containsKey("district_id")) ? parameters
          .get("district_id")[0] : null;
      if (districtId != null && !"".equals(districtId.trim())) {
        qb.addFilter(QueryAssembler.STRING, "district_id", "=", districtId);
      }
      String cityId = (null != parameters && parameters.containsKey("city_id")) ? parameters
          .get("city_id")[0] : null;
      if (cityId != null && !"".equals(cityId.trim())) {
        qb.addFilter(QueryAssembler.STRING, "city_id", "=", cityId);
      }
      String areaId = (null != parameters && parameters.containsKey("area_id")) ? parameters
          .get("area_id")[0] : null;
      if (areaId != null && !"".equals(areaId.trim())) {
        qb.addFilter(QueryAssembler.STRING, "area_id", "=", areaId);
      }
    }
  }

  public BasicDynaBean getReferralDocMobile(String id) {
    return ((ReferralDoctorRepository) super.getRepository()).getReferralDocMobile(id);
  }

  public BasicDynaBean getReferralForVisit(String id) {
    return ((ReferralDoctorRepository) super.getRepository()).getReferralForVisit(id);
  }
}
