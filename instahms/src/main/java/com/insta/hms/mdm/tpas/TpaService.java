package com.insta.hms.mdm.tpas;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.sponsors.SponsorTypeRepository;
import com.insta.hms.mdm.tpapreauthforms.TpaPreauthFormsRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/** The Class TpaService. */
@Service
public class TpaService extends MasterService {

  /** Tpa repository. */
  @LazyAutowired
  private TpaRepository tpaRepository;

  /** The ha tpa code repository. */
  @LazyAutowired
  HaTpaCodeRepository haTpaCodeRepository;

  /** The generic preferences service. */
  @LazyAutowired
  GenericPreferencesService genericPreferencesService;

  /** Sponsor type repository. */
  @LazyAutowired
  private SponsorTypeRepository sponsorTypeRepository;

  /** Tpa Preauth forms repository. */
  @LazyAutowired
  private TpaPreauthFormsRepository tpaPreauthFormsRepository;

  /**
   * Instantiates a new tpa service.
   *
   * @param tpaRepository the tpa repository
   * @param tpaValidator  the tpa validator
   */
  public TpaService(TpaRepository tpaRepository, TpaValidator tpaValidator) {
    super(tpaRepository, tpaValidator);
  }

  /**
   * Ha tpa code list all by.
   *
   * @param filterMap the filter map
   * @return the list
   */
  public List<BasicDynaBean> haTpaCodeListAllBy(Map<String, Object> filterMap) {
    return haTpaCodeRepository.listAll(null, filterMap, null);
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return ((TpaRepository) getRepository()).getBean();
  }

  /**
   * Gets the details.
   *
   * @param tpaId the tpa id
   * @return the details
   */
  public BasicDynaBean getDetails(String tpaId) {
    return ((TpaRepository) getRepository()).getDetails(tpaId);
  }

  /**
   * Gets the details.
   *
   * @param filterText the filter text
   * @param categoryId the category id
   * @return the details
   */
  public List<BasicDynaBean> getDetails(String filterText, Integer categoryId) {
    return ((TpaRepository) getRepository()).getDetails(new Object[] { categoryId, filterText });
  }

  public BasicDynaBean getTpaHealthAuthorityDetails(String tpaId, Integer centerId) {
    return ((TpaRepository) getRepository()).getTpaHealthAuthorityDetails(tpaId, centerId);
  }

  /**
   * Company tpa XML list.
   *
   * @return the list
   */
  public List<BasicDynaBean> companyTpaXmlList() {
    Integer maxCenters = (Integer) genericPreferencesService.getPreferences()
        .get("max_centers_inc_default");
    int centerId = RequestContext.getCenterId();
    return ((TpaRepository) getRepository()).getCompanyTpaXmlList(maxCenters, centerId);
  }

  /**
   * Xml tpa list.
   *
   * @return the list
   */
  public List<BasicDynaBean> xmlTpaList() {
    Map xmlTpaKeyMap = new HashMap();
    xmlTpaKeyMap.put("status", "A");
    xmlTpaKeyMap.put("claim_format", "XML");
    return ((TpaRepository) getRepository()).getXmlTpaList(xmlTpaKeyMap);
  }

  /**
   * All xml tpa list.
   *
   * @return the list
   */
  public List<BasicDynaBean> allXmlTpaList() {
    Integer maxCenters = (Integer) genericPreferencesService.getPreferences()
        .get("max_centers_inc_default");
    int centerId = RequestContext.getCenterId();
    return ((TpaRepository) getRepository()).getAllTpaXmlList(maxCenters, centerId);
  }

  /**
   * Gets the integer.
   *
   * @param tpaId the tpa id
   * @return the integer
   */
  public int getInteger(String tpaId) {
    return ((TpaRepository) getRepository()).getSponsorType(new Object[] { tpaId });
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterService#autocomplete(java.lang.String,
   * java.util.Map)
   */
  @Override
  public List<BasicDynaBean> autocomplete(String match, Map<String, String[]> parameters) {
    String tpaActiveStatus = (null != parameters && parameters.containsKey("tpa_active_status"))
        ? parameters.get("tpa_active_status")[0]
        : null;
    if (tpaActiveStatus != null && !tpaActiveStatus.equals("")) {
      boolean activeOnly = new Boolean(tpaActiveStatus);
      return customAutocomplete("tpa_name", match, activeOnly, parameters);
    }
    return customAutocomplete("tpa_name", match, false, parameters);
  }

  /**
   * Custom autocomplete.
   *
   * @param field      the field
   * @param match      the match
   * @param activeOnly the active only
   * @param parameters the parameters
   * @return the list
   */
  private List<BasicDynaBean> customAutocomplete(String field, String match, boolean activeOnly,
      Map<String, String[]> parameters) {
    String categoryId = (null != parameters && parameters.containsKey("category_id"))
        ? parameters.get("category_id")[0]
        : null;
    Integer pageLimit = (null != parameters && parameters.containsKey("page_limit"))
        ? Integer.parseInt(parameters.get("page_limit")[0])
        : 25;
    if (null != categoryId && !categoryId.equals("")) {
      if (match.trim().isEmpty()) {
        return ((TpaRepository) getRepository())
            .getDetailsAll(new Object[] { Integer.parseInt(categoryId), pageLimit });
      }
      return ((TpaRepository) getRepository())
          .getDetails(new Object[] { Integer.parseInt(categoryId), match.trim(), pageLimit });
    }
    return super.autocomplete(field, match.trim(), activeOnly, parameters);
  }

  /**
   * Gets the company tpa list.
   *
   * @return the company tpa list
   */
  public List<BasicDynaBean> getCompanyTpaList() {
    return ((TpaRepository) getRepository()).getCompTpaList();
  }

  /**
   * Gets the tpas names and ids.
   *
   * @return the tpas names and ids
   */
  public List<BasicDynaBean> getTpasNamesAndIds() {
    return ((TpaRepository) getRepository()).lookup(true);
  }

  /**
   * List all.
   *
   * @param filterMap the filter map
   * @return the list
   */
  public List<BasicDynaBean> listAll(Map<String, Object> filterMap) {
    // TODO Auto-generated method stub
    return ((TpaRepository) getRepository()).listAll(null, filterMap, null);
  }

  /**
   * Gets the allowed sponsors.
   *
   * @param patientCategoryId the patient category id
   * @param visitType         the visit type
   * @return the allowed sponsors
   */
  public List<BasicDynaBean> getAllowedSponsors(int patientCategoryId, String visitType) {
    // TODO Auto-generated method stub
    return ((TpaRepository) getRepository()).getAllowedSponsors(patientCategoryId, visitType);
  }

  /**
   * Bulk inserts tpa beans.
   * 
   * @param list the list of map
   * @throws SQLException the exception
   */
  @SuppressWarnings("unchecked")
  @Transactional(rollbackFor = Exception.class)
  private void insertBulkTpaMasterBeans(List<Map<String, Object>> list) throws SQLException {
    BasicDynaBean bean = null;
    for (Map<String, Object> map : list) {
      bean = (BasicDynaBean) map.get("tpa");
      TpaMasterDAO tpaDAO = new TpaMasterDAO();
      String tpaid = tpaDAO.getNextTPAId();
      bean.set("tpa_id", tpaid);
      tpaRepository.insert(bean);

      List<BasicDynaBean> beanList = null;
      beanList = (List<BasicDynaBean>) map.get("healthAutorityBeanList");
      for (BasicDynaBean habean : beanList) {
        habean.set("tpa_id", tpaid);
        habean.set("ha_tpa_code_id", haTpaCodeRepository.getNextSequence());
        haTpaCodeRepository.insert(habean);
      }
    }
  }

  /**
   * imports bulk tpa.
   * 
   * @param map the map
   * @throws SQLException exception
   */
  public void importBulkTpa(Map<String, List<Map<String, Object>>> map) throws SQLException {
    List<Map<String, Object>> tpaSheetData = map.get("TPA");
    List<Map<String, Object>> healthAuthoritySheetData = map.get("HealthAuthorityCode");

    List<String> errors = new ArrayList<String>();

    BasicDynaBean tpaBean = null;
    BasicDynaBean healthAutorityBean = null;
    Map<String, Object> tpaMap = null;
    List<BasicDynaBean> healthAutorityBeanList = null;
    List<Map<String, Object>> tpaMasterMapList = new ArrayList<Map<String, Object>>();

    for (Map<String, Object> tpa : tpaSheetData) {
      try {
        tpaMap = new HashMap<String, Object>();
        tpaBean = validateTpaDataAndConvertToBean(tpa);
        for (Map<String, Object> checkExistance : tpaMasterMapList) {
          BasicDynaBean exInsBean = (BasicDynaBean) checkExistance.get("tpa");
          if (exInsBean.get("tpa_name").equals(tpaBean.get("tpa_name"))) {
            errors.add("Contains duplicate tpa : " + tpaBean.get("tpa_name"));
          }
        }
        BasicDynaBean exInsInDb = tpaRepository.findByKey("tpa_name", tpaBean.get("tpa_name"));
        if (exInsInDb != null) {
          errors.add("TPA already exists in database : " + tpaBean.get("tpa_name"));
        }
        tpaMap.put("tpa", tpaBean);

        healthAutorityBeanList = new ArrayList<BasicDynaBean>();
        for (Map<String, Object> healthAutority : healthAuthoritySheetData) {
          if (tpa.get("tpa_name").toString()
              .equalsIgnoreCase(healthAutority.get("tpa_name").toString())) {
            healthAutorityBean = validateHADataAndConvertToBean(healthAutority);
            // check for duplicate
            for (BasicDynaBean checkExistance : healthAutorityBeanList) {
              if (checkExistance.get("health_authority")
                  .equals(healthAutorityBean.get("health_authority"))) {
                errors.add("Contains duplicate health authority for : " + tpaBean.get("tpa_name"));
              }
            }
            healthAutorityBeanList.add(healthAutorityBean);
          }
        }
        tpaMap.put("healthAutorityBeanList", healthAutorityBeanList);

        tpaMasterMapList.add(tpaMap);
      } catch (HMSException exception) {
        errors.addAll(exception.getErrorsList());
      }
    }

    if (errors.isEmpty()) {
      insertBulkTpaMasterBeans(tpaMasterMapList);
    } else {
      throw new HMSException(errors);
    }
  }

  /**
   * validates tpa data and convert to bean.
   * 
   * @param map the map
   * @return bean
   */
  private BasicDynaBean validateTpaDataAndConvertToBean(Map<String, Object> map) {
    List<String> errors = new ArrayList<String>();

    // Status
    if (map.get("status") != null) {
      String status = map.get("status").toString();
      if (status.equalsIgnoreCase("Active") || status.equalsIgnoreCase("A")) {
        map.put("status", "A");
      } else if (status.equalsIgnoreCase("Inactive") || status.equalsIgnoreCase("I")) {
        map.put("status", "I");
      } else {
        errors.add("Invalid status " + status);
      }
    } else {
      map.put("status", "A");
    }

    BasicDynaBean tempBean;
    // TPA/Sponsor Prior Auth Form
    if (map.get("tpa_prior_auth_form") != null) {
      String tpapriorauth = map.get("tpa_prior_auth_form").toString();
      tempBean = tpaPreauthFormsRepository.findByKey("title", tpapriorauth);
      if (tempBean != null) {
        map.put("tpa_pdf_form", tempBean.get("tpa_form_id"));
      } else {
        errors.add("Invalid TPA_Prior_Auth_Form : " + tpapriorauth);
      }
    }

    // TPA/Sponsor Claim Form
    if (map.get("tpa_claim_form") != null) {
      String tpaclaimformat = map.get("tpa_claim_form").toString();
      if (tpaclaimformat.equalsIgnoreCase("HTML")) {
        map.put("default_claim_template", "P");
      } else if (tpaclaimformat.equalsIgnoreCase("RTF")) {
        map.put("default_claim_template", "R");
      } else if (tpaclaimformat.equalsIgnoreCase("")) {
        map.put("default_claim_template", "N");
      } else {
        errors.add("Invalid claim form (Accepted values HTML/RTF):" + tpaclaimformat);
      }
    } else {
      map.put("default_claim_template", "N");
    }

    // claim_format
    String claimformat = map.get("claim_format").toString();
    if (!claimformat.equalsIgnoreCase("XML") && !claimformat.equalsIgnoreCase("XL")) {
      errors.add("Invalid claim format :" + claimformat);
    }

    // TPA Sponsor Type
    String tpasponsortype = map.get("tpa_type").toString();
    tempBean = sponsorTypeRepository.findByKey("sponsor_type_name", tpasponsortype);
    if (tempBean != null) {
      map.put("sponsor_type_id", tempBean.get("sponsor_type_id"));
    } else {
      errors.add("Invalid sponsor type :" + tpasponsortype);
    }

    // Scanned Doc Upload
    String scanneddocupload = map.get("scanned_doc_upload").toString();
    if (scanneddocupload.equalsIgnoreCase("Required")) {
      map.put("scanned_doc_required", "R");
    } else if (scanneddocupload.equalsIgnoreCase("Optional")) {
      map.put("scanned_doc_required", "O");
    } else if (scanneddocupload.equalsIgnoreCase("Not Required")) {
      map.put("scanned_doc_required", "N");
    } else {
      errors.add("Invalid scanned doc upload : " + scanneddocupload);
    }

    // Prior Auth Mode
    String priorauthmode = map.get("prior_authorization_mode").toString();
    if (priorauthmode.equalsIgnoreCase("Manual")) {
      map.put("pre_auth_mode", "M");
    } else if (priorauthmode.equalsIgnoreCase("Online")) {
      map.put("pre_auth_mode", "O");
    } else {
      errors.add("Invalid prior auth mode :" + priorauthmode);
    }

    // Duplicate membershipId
    if (map.get("duplicate_membership_id") != null) {
      String dupmemid = map.get("duplicate_membership_id").toString();
      final String TPA_MEMBERID_VALIDATION_TYPE = "tpa_member_id_validation_type";
      if (dupmemid.equalsIgnoreCase("Allow")) {
        map.put(TPA_MEMBERID_VALIDATION_TYPE, "A");
      } else if (dupmemid.equalsIgnoreCase("Block")) {
        map.put(TPA_MEMBERID_VALIDATION_TYPE, "B");
      } else if (dupmemid.equalsIgnoreCase("Warn")) {
        map.put(TPA_MEMBERID_VALIDATION_TYPE, "W");
      } else if (dupmemid.equalsIgnoreCase("Allow Child Birth Only")) {
        map.put(TPA_MEMBERID_VALIDATION_TYPE, "C");
        // Child_duplicate_mem_id_days
        if (map.get("child_duplicate_mem_id_days") == null) {
          errors.add("Child Duplicate membership id validit days is required for " + dupmemid);
        } else {
          map.put("child_dup_memb_id_validity_days", map.get("child_duplicate_mem_id_days"));
        }
      } else {
        errors.add("Invalid duplicate membeshipid type :" + dupmemid);
      }
    }

    // Claim amount includes tax
    String claimincludestax = map.get("claim_amount_includes_tax").toString();
    if (claimincludestax.equalsIgnoreCase("Yes") || claimincludestax.equalsIgnoreCase("Y")) {
      map.put("claim_amount_includes_tax", "Y");
    } else if (claimincludestax.equalsIgnoreCase("No") || claimincludestax.equalsIgnoreCase("N")) {
      map.put("claim_amount_includes_tax", "N");
    } else {
      errors.add("Claim Amount includes tax accepts Yes/No ");
    }

    // Limit includes Tax
    String limitincludestax = map.get("limit_includes_tax").toString();
    if (limitincludestax.equalsIgnoreCase("Yes") || limitincludestax.equalsIgnoreCase("Y")) {
      map.put("limit_includes_tax", "Y");
    } else if (limitincludestax.equalsIgnoreCase("No") || limitincludestax.equalsIgnoreCase("N")) {
      map.put("limit_includes_tax", "N");
    } else {
      errors.add("Limit includes tax accepts Yes/No ");
    }

    if (map.get("max_resubmission_count") == null) {
      map.put("max_resubmission_count", "3");
    }

    BasicDynaBean tpaBean = tpaRepository.getBean();
    ConversionUtils.copyJsonToDynaBean(map, tpaBean, errors, true);
    if (errors.isEmpty()) {
      return tpaBean;
    } else {
      throw new HMSException(errors);
    }
  }

  /**
   * validates ha code details and converts to bean.
   * 
   * @param map the map
   * @return bean
   */
  private BasicDynaBean validateHADataAndConvertToBean(Map<String, Object> map) {
    List<String> errors = new ArrayList<String>();

    if (map.get("health_authority") == null) {
      errors.add("Health Authority is empty for " + map.get("tpa_name"));
    }

    if (map.get("enable_eligibility_authorization") != null) {
      final String ENABLE_ELIGIBILITY_AUTH = map.get("enable_eligibility_authorization").toString();
      if (ENABLE_ELIGIBILITY_AUTH.equalsIgnoreCase("Enabled")
          || ENABLE_ELIGIBILITY_AUTH.equalsIgnoreCase("Enable")) {
        map.put("enable_eligibility_authorization", "t");
      } else if (ENABLE_ELIGIBILITY_AUTH.equalsIgnoreCase("Disabled")
          || ENABLE_ELIGIBILITY_AUTH.equalsIgnoreCase("Disable")) {
        map.put("enable_eligibility_authorization", "f");
      } else {
        errors.add("Invalid Data in enable eligibility authorization : "
            + map.get("enable_eligibility_authorization"));
      }
    } else {
      errors.add("Enable eligibity authorization is empty for " + map.get("tpa_name"));
    }

    if (map.get("enable_eligibility_auth_in_xml") != null) {
      final String ENABLE_ELIGIBILITY_AUTH_IN_XML = map.get("enable_eligibility_auth_in_xml")
          .toString();
      if (ENABLE_ELIGIBILITY_AUTH_IN_XML.equalsIgnoreCase("As observation")) {
        map.put("enable_eligibility_auth_in_xml", "O");
      } else if (ENABLE_ELIGIBILITY_AUTH_IN_XML.equalsIgnoreCase("Eligibility ID Payer")) {
        map.put("enable_eligibility_auth_in_xml", "T");
      } else if (ENABLE_ELIGIBILITY_AUTH_IN_XML.equalsIgnoreCase("Disable")) {
        map.put("enable_eligibility_auth_in_xml", "N");
      } else {
        errors.add("Invalid data in enable eligibility auth in xml : "
            + map.get("enable_eligibility_auth_in_xml"));
      }
    } else {
      errors.add("Enable eligibity auth in xml is empty for " + map.get("tpa_name"));
    }

    BasicDynaBean haBean = haTpaCodeRepository.getBean();
    ConversionUtils.copyJsonToDynaBean(map, haBean, errors, true);
    if (errors.isEmpty()) {
      return haBean;
    } else {
      throw new HMSException(errors);
    }
  }
}
