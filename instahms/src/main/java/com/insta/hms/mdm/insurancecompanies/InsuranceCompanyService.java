package com.insta.hms.mdm.insurancecompanies;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.exception.HMSException;
import com.insta.hms.master.InsuranceCompMaster.InsuCompMasterDAO;
import com.insta.hms.mdm.MasterDetailsService;
import com.insta.hms.mdm.insuranceitemcategories.InsuranceItemCategoryRepository;
import com.insta.hms.mdm.organization.OrganizationRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class InsuranceCompanyService.
 *
 * @author krishnat
 */
@Service
public class InsuranceCompanyService extends MasterDetailsService {

  @LazyAutowired
  private InsuranceCompanyRepository insuranceCompanyRepository;

  @LazyAutowired
  private OrganizationRepository organizationRepository;

  @LazyAutowired
  private InsuranceItemCategoryRepository insuranceItemCategoryRepository;

  /**
   * Instantiates a new insurance company service.
   *
   * @param repo      the repo
   * @param validator the validator
   */
  public InsuranceCompanyService(InsuranceCompanyRepository repo,
      InsuranceCompanyValidator validator) {
    super(repo, validator);
  }

  /**
   * Gets the mapped insurance companies.
   *
   * @param tpaId      the tpa id
   * @param categoryId the category id
   * @return the mapped insurance companies
   */
  public List<BasicDynaBean> getMappedInsuranceCompanies(String tpaId, String categoryId) {
    List<BasicDynaBean> insCompList = ((InsuranceCompanyRepository) getRepository())
        .getMappedInsuranceCompanies(tpaId, categoryId);
    return insCompList;
  }

  /**
   * Gets the insurance rules document.
   *
   * @param insCompId the ins comp id
   * @return the insurance rules document
   */
  public byte[] getInsuranceRulesDocument(String insCompId) {
    BasicDynaBean insRuleDocBean = ((InsuranceCompanyRepository) getRepository())
        .findByKey("insurance_co_id", insCompId);
    if (insRuleDocBean != null && insRuleDocBean.get("insurance_rules_doc_bytea") != null) {
      return (byte[]) insRuleDocBean.get("insurance_rules_doc_bytea");
    } else if (insRuleDocBean == null) {
      EntityNotFoundException ex = new EntityNotFoundException(
          new String[] { "Rules document", "Insurance Company", "" });
      throw ex;
    } else {
      EntityNotFoundException ex = new EntityNotFoundException(
          new String[] { "Rules Document", "Insurance Company", "" });
      throw ex;
    }
  }

  /**
   * Gets the ins comp default rate plan.
   *
   * @param insCompId  the ins comp id
   * @param categoryId the category id
   * @return the ins comp default rate plan
   */
  public List<BasicDynaBean> getInsCompDefaultRatePlan(String insCompId, String categoryId) {
    return ((InsuranceCompanyRepository) getRepository()).getInsuranceCompDefaultRatePlan(insCompId,
        categoryId);
  }

  /**
   * Gets the insurance companies names and ids.
   *
   * @return the insurance companies names and ids
   */
  public List<BasicDynaBean> getInsuranceCompaniesNamesAndIds() {
    return ((InsuranceCompanyRepository) getRepository()).lookup(true);
  }

  /**
   * Gets the insurance company code.
   *
   * @param healthAuthority the health authority
   * @param insuranceCoId   the insurance co id
   * @return the insurance company code
   */
  public BasicDynaBean getInsuranceCompanyCode(String healthAuthority, String insuranceCoId) {
    return ((InsuranceCompanyRepository) getRepository()).getInsuranceCompanyCode(healthAuthority,
        insuranceCoId);
  }

  /**
   * List all.
   *
   * @param columns    the columns
   * @param filterMap  the filter map
   * @param sortColumn the sort column
   * @return the list
   */
  public List<BasicDynaBean> listAll(List<String> columns, Map<String, Object> filterMap,
      String sortColumn) {
    // TODO Auto-generated method stub
    return ((InsuranceCompanyRepository) getRepository()).listAll(columns, filterMap, sortColumn);
  }

  /**
   * Gets the category mapped tpa list.
   *
   * @param patientCategoryId the patient category id
   * @param visitType         the visit type
   * @return the category mapped tpa list
   */
  public List<BasicDynaBean> getCategoryMappedTpaList(int patientCategoryId, String visitType) {
    // TODO Auto-generated method stub
    return ((InsuranceCompanyRepository) getRepository())
        .getCategoryMappedTpaList(patientCategoryId, visitType);
  }

  /**
   * Gets the company tpa list.
   *
   * @return the company tpa list
   */
  public List<BasicDynaBean> getCompanyTpaList(Integer centerId) {
    return ((InsuranceCompanyRepository) getRepository()).getCompanyTpaList(centerId);
  }

  /**
   * Get list of insurance companies.
   *
   * @return result list
   */
  public Map<String, Object> getInsuranceCompanyList() {
    Map<String, Object> result = new HashMap<>();
    result.put("insurance_companies", ConversionUtils.listBeanToListMap(
        ((InsuranceCompanyRepository) getRepository()).getInsuranceCompanyList()));
    return result;
  }

  /**
   * Bulk inserts insurance company.
   * 
   * @param list the list of map
   * @throws SQLException the exception
   */
  @SuppressWarnings("unchecked")
  @Transactional(rollbackFor = Exception.class)
  public void insertBulkInsuranceCompanyMasterBeans(List<Map<String, Object>> list)
      throws SQLException {
    BasicDynaBean bean = null;
    List<BasicDynaBean> beanList = null;
    InsuCompMasterDAO insDAO = new InsuCompMasterDAO();

    GenericRepository haRepo = new GenericRepository("ha_ins_company_code");
    GenericRepository insComCatRepo = new GenericRepository("insurance_company_category_mapping");
    for (Map<String, Object> map : list) {
      bean = (BasicDynaBean) map.get("insuranceCompany");
      String insurancecoid = insDAO.getNextICId();
      bean.set("insurance_co_id", insurancecoid);
      insuranceCompanyRepository.insert(bean);

      beanList = (List<BasicDynaBean>) map.get("healthAutorityBeanList");
      for (BasicDynaBean habean : beanList) {
        habean.set("ha_insurance_co_code_id", haRepo.getNextSequence());
        habean.set("insurance_co_id", insurancecoid);
        haRepo.insert(habean);
      }

      beanList = (List<BasicDynaBean>) map.get("insCategoryBeanList");
      for (BasicDynaBean insCatBean : beanList) {
        insCatBean.set("insurance_co_id", insurancecoid);
        insComCatRepo.insert(insCatBean);
      }
    }
  }

  /**
   * import bulk ins company.
   * 
   * @param map the map
   * @throws SQLException the exception
   */
  public void importBulkInsuranceCompanies(Map<String, List<Map<String, Object>>> map)
      throws SQLException {
    List<Map<String, Object>> insuranceCompaiesSheetData = map.get("InsuranceCompanies");
    List<Map<String, Object>> healthAuthoritySheetData = map.get("HealthAuthorityCode");
    List<Map<String, Object>> insCategorySheetData = map.get("InsuranceItemCategories");

    List<String> errors = new ArrayList<String>();

    BasicDynaBean insuranceCompanyBean = null;
    BasicDynaBean healthAutorityBean = null;
    BasicDynaBean insuranceCategoryBean = null;
    Map<String, Object> insCompanyMasterMap = null;
    List<BasicDynaBean> healthAutorityBeanList = null;
    List<BasicDynaBean> insuranceCategoryBeanList = null;
    List<Map<String, Object>> insCompanyMasterMapList = new ArrayList<Map<String, Object>>();

    for (Map<String, Object> insCompany : insuranceCompaiesSheetData) {
      try {
        insCompanyMasterMap = new HashMap<String, Object>();
        insuranceCompanyBean = validateInsCoDataAndConvertToBean(insCompany);
        // Check for duplicates
        for (Map<String, Object> checkExistance : insCompanyMasterMapList) {
          BasicDynaBean exInsBean = (BasicDynaBean) checkExistance.get("insuranceCompany");
          if (exInsBean.get("insurance_co_name")
              .equals(insuranceCompanyBean.get("insurance_co_name"))) {
            errors.add("Contains duplicate insurance company : "
                + insuranceCompanyBean.get("insurance_co_name"));
          }
        }
        BasicDynaBean exInsInDb = insuranceCompanyRepository.findByKey("insurance_co_name",
            insuranceCompanyBean.get("insurance_co_name"));
        if (exInsInDb != null) {
          errors.add("Insurance company already exists in database : "
              + insuranceCompanyBean.get("insurance_co_name"));
        }
        insCompanyMasterMap.put("insuranceCompany", insuranceCompanyBean);

        healthAutorityBeanList = new ArrayList<BasicDynaBean>();
        for (Map<String, Object> healthAutority : healthAuthoritySheetData) {
          if (insCompany.get("insurance_co_name").toString()
              .equalsIgnoreCase(healthAutority.get("insurance_co_name").toString())) {
            healthAutorityBean = validateHADataAndConvertToBean(healthAutority);
            // check for duplicate
            for (BasicDynaBean checkExistance : healthAutorityBeanList) {
              if (checkExistance.get("health_authority")
                  .equals(healthAutorityBean.get("health_authority"))) {
                errors.add("Contains duplicate health authority for : "
                    + insuranceCompanyBean.get("insurance_co_name"));
              }
            }
            healthAutorityBeanList.add(healthAutorityBean);
          }
        }
        insCompanyMasterMap.put("healthAutorityBeanList", healthAutorityBeanList);

        insuranceCategoryBeanList = new ArrayList<BasicDynaBean>();
        for (Map<String, Object> insCategory : insCategorySheetData) {
          if (insCompany.get("insurance_co_name").toString()
              .equalsIgnoreCase(insCategory.get("insurance_co_name").toString())) {
            insuranceCategoryBean = validateInsCatMappingDataAndConvertToBean(insCategory);
            // check for duplicate
            for (BasicDynaBean checkExistance : insuranceCategoryBeanList) {
              if (checkExistance.get("insurance_category_id")
                  .equals(insuranceCategoryBean.get("insurance_category_id"))) {
                errors.add("Contains duplicate insurance category for : "
                    + insuranceCompanyBean.get("insurance_co_name"));
              }
            }
            insuranceCategoryBeanList.add(insuranceCategoryBean);
          }
        }
        insCompanyMasterMap.put("insCategoryBeanList", insuranceCategoryBeanList);
        insCompanyMasterMapList.add(insCompanyMasterMap);
      } catch (HMSException exception) {
        errors.addAll(exception.getErrorsList());
      }
    }

    if (errors.isEmpty()) {
      insertBulkInsuranceCompanyMasterBeans(insCompanyMasterMapList);
    } else {
      throw new HMSException(errors);
    }
  }

  /*
   * gets insurance company bean.
   * 
   * @param map the map
   */
  private BasicDynaBean validateInsCoDataAndConvertToBean(Map<String, Object> map) {
    List<String> errors = new ArrayList<String>();
    BasicDynaBean tempBean = null;

    // Default Rate Plan
    if (map.get("default_rate_plan") != null) {
      tempBean = organizationRepository.findByKey("org_name", map.get("default_rate_plan"));
      if (tempBean != null) {
        map.put("default_rate_plan", tempBean.get("org_id"));
      } else {
        errors.add("Invalid default_rate_plan :" + map.get("default_rate_plan"));
      }
    } else {
      tempBean = organizationRepository.findByKey("org_name", "GENERAL");
      if (tempBean == null) {
        tempBean = organizationRepository.findByKey("org_name", "General");
      }
      if (tempBean != null) {
        map.put("default_rate_plan", tempBean.get("org_id"));
      } else {
        errors.add("Invalid default_rate_plan :" + map.get("default_rate_plan"));
      }
    }

    // Status
    if (map.get("status") != null) {
      String status = map.get("status").toString();
      if (status.equalsIgnoreCase("Active") || status.equalsIgnoreCase("A")) {
        map.put("status", "A");
      } else if (status.equalsIgnoreCase("Inactive") || status.equalsIgnoreCase("I")) {
        map.put("status", "I");
      } else {
        errors.add("Invalid status :" + status);
      }
    } else {
      map.put("status", "A");
    }

    map.put("insurance_co_address", map.get("address"));
    map.put("insurance_co_city", map.get("city"));
    map.put("insurance_co_state", map.get("state"));
    map.put("insurance_co_country", map.get("country"));
    map.put("insurance_co_phone", map.get("mobile"));
    map.put("insurance_co_email", map.get("email"));

    BasicDynaBean insCompBean = insuranceCompanyRepository.getBean();
    ConversionUtils.copyJsonToDynaBean(map, insCompBean, errors, true);
    if (errors.isEmpty()) {
      return insCompBean;
    } else {
      throw new HMSException(errors);
    }
  }

  /*
   * gets ha bean.
   * 
   * @param map the map
   */
  private BasicDynaBean validateHADataAndConvertToBean(Map<String, Object> map) {
    BasicDynaBean haBean = new GenericRepository("ha_ins_company_code").getBean();
    List<String> errors = new ArrayList<String>();
    ConversionUtils.copyJsonToDynaBean(map, haBean, errors, true);
    if (errors.isEmpty()) {
      return haBean;
    } else {
      throw new HMSException(errors);
    }
  }

  /*
   * gets insurance category bean.
   * 
   * @param map the map
   */
  private BasicDynaBean validateInsCatMappingDataAndConvertToBean(Map<String, Object> map) {
    BasicDynaBean insCat = new GenericRepository("insurance_company_category_mapping").getBean();
    List<String> errors = new ArrayList<String>();
    BasicDynaBean tempBean = null;

    // Plan Item-Category
    tempBean = insuranceItemCategoryRepository.findByKey("insurance_category_name",
        map.get("insurance_item_category"));
    if (tempBean != null) {
      map.put("insurance_category_id", tempBean.get("insurance_category_id"));
    } else {
      errors.add("Invalid insurance_category_name :" + map.get("insurance_item_category"));
    }
    ConversionUtils.copyJsonToDynaBean(map, insCat, errors, true);
    if (errors.isEmpty()) {
      return insCat;
    } else {
      throw new HMSException(errors);
    }
  }
}
