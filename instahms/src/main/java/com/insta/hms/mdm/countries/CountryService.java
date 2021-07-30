package com.insta.hms.mdm.countries;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.exception.DuplicateEntityException;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.states.StateService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class CountryService.
 */
@Service
public class CountryService extends MasterService {

  /** The state service. */
  @LazyAutowired
  private StateService stateService;

  /** The country repository. */
  @LazyAutowired
  private CountryRepository countryRepository;

  /** The validator. */
  @LazyAutowired
  private CountryValidator validator;

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(CountryService.class);

  /**
   * Instantiates a new country service.
   *
   * @param countryRepository
   *          the country repository
   * @param countryValidator
   *          the country validator
   */
  public CountryService(CountryRepository countryRepository, CountryValidator countryValidator) {
    super(countryRepository, countryValidator);
  }

  /**
   * Gets the country.
   *
   * @param keyColumn
   *          the key column
   * @param identifier
   *          the identifier
   * @return the country
   */
  public BasicDynaBean getCountry(String keyColumn, String identifier) {
    return countryRepository.findByKey(keyColumn, identifier);
  }

  /**
   * Update country.
   *
   * @param parameters
   *          the parameters
   * @return the int
   */
  @Transactional(rollbackFor = Exception.class)
  public int updateCountry(Map<String, String[]> parameters) {

    BasicDynaBean countryBean = countryRepository.getBean();
    List<String> errors = new ArrayList<String>();
    ConversionUtils.copyToDynaBean(parameters, countryBean, errors);
    // Get name of country from ISO -2 letter country code
    countryBean.set("country_name",
        PhoneNumberUtil.getDisplayCountry((String) countryBean.get("country_name")));

    validator.validateUpdate(countryBean);
    String countryId = parameters.get("country_id")[0];
    List<String> columns = new ArrayList<String>();
    columns.add("country_name");
    columns.add("country_id");
    String countryName = (String) countryBean.get("country_name");
    List<BasicDynaBean> listDynaBean = countryRepository.listAll(columns, "country_name",
        countryName);

    if (listDynaBean.size() == 1) {
      BasicDynaBean bean = listDynaBean.get(0);
      String id = (String) bean.get("country_id");
      if (!id.equals(countryId)) {
        throw new DuplicateEntityException(new String[] { "Country", countryName });
      }
    } else if (listDynaBean.size() > 1) {
      throw new DuplicateEntityException(new String[] { "Country", countryName });
    }

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("country_id", countryId);

    int success = 0;
    if (errors.isEmpty()) {
      success = countryRepository.update(countryBean, keys);
    }
    return success;

  }

  /**
   * Insert country.
   *
   * @param parameters
   *          the parameters
   * @return the basic dyna bean
   */
  @Transactional(rollbackFor = Exception.class)
  public BasicDynaBean insertCountry(Map<String, String[]> parameters) {

    BasicDynaBean countryBean = countryRepository.getBean();
    List<String> errors = new ArrayList<String>();
    ConversionUtils.copyToDynaBean(parameters, countryBean, errors);
    countryBean.set("country_name",
        PhoneNumberUtil.getDisplayCountry((String) countryBean.get("country_name")));

    if (errors.isEmpty()) {
      validator.validateInsert(countryBean);
      String countryName = ((String) countryBean.get("country_name"));
      boolean exists = countryRepository.exist("country_name", countryName);
      if (exists) {
        logger.warn("Country name already exists with name :" + countryName);
        throw new DuplicateEntityException(new String[] { "Country", countryName });
      } else {
        String countryId = (String) countryRepository.getNextId();
        countryBean.set("country_id", countryId);
        countryRepository.insert(countryBean);
        logger.debug(
            "Added new country with countryId :" + countryId + " and country name :" + countryName);
      }
    } else {
      throw new ConversionException(errors);
    }
    return countryBean;

  }

  /**
   * Returns BasicDynaBean when code is either alpha3_code, alpha2_code, country_name.
   *
   * @param nationalityCode
   *          the nationality code
   * @return the nationality
   */
  public BasicDynaBean getNationality(String nationalityCode) {
    return countryRepository.getNationality(nationalityCode);
  }

}
