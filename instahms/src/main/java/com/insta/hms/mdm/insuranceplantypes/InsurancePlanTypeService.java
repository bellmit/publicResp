package com.insta.hms.mdm.insuranceplantypes;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.HMSException;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class InsurancePlanTypeService.
 */
@Service
public class InsurancePlanTypeService extends MasterService {

  /** Insurance Company. */
  @LazyAutowired
  private InsuranceCompanyRepository insuranceCompanyRepository;
  
  /** Insurance plan type repository. */
  @LazyAutowired
  private InsurancePlanTypeRespository insurancePlanTypeRespository;
  
  /**
   * Instantiates a new insurance plan type service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public InsurancePlanTypeService(InsurancePlanTypeRespository repository,
      InsurancePlanTypeValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the mapped plan types.
   *
   * @param insCompId the ins comp id
   * @param centerId the center id
   * @return the mapped plan types
   */
  public List<BasicDynaBean> getMappedPlanTypes(String insCompId, Integer centerId) {
    List<BasicDynaBean> insPlanTypesList = ((InsurancePlanTypeRespository) getRepository())
        .getPlanTypes(insCompId, centerId);
    return insPlanTypesList;
  }

  /**
   * Gets the plan types for sponsor.
   *
   * @param tpaId the tpa id
   * @param categoryId the category id
   * @param centerId the center id
   * @return the plan types for sponsor
   */
  public List<BasicDynaBean> getPlanTypesForSponsor(String tpaId, Integer categoryId,
      Integer centerId) {
    return ((InsurancePlanTypeRespository) getRepository()).getPlanTypesForSponsor(tpaId,
        categoryId, centerId);
  }
  
  /**
   * Imports bulk insurance plan data.
   * 
   * @param map the map
   */
  @Transactional(rollbackFor = Exception.class)
  public void importBulkInsuranceCategory(Map<String, List<Map<String, Object>>> map) {
    List<Map<String, Object>> tpaSheetData = map.get("InsurancePlanType");

    List<String> errors = new ArrayList<String>();

    BasicDynaBean insPlanTypeBean = null;
    List<BasicDynaBean> insPlanTypeBeanList = new ArrayList<BasicDynaBean>();

    for (Map<String, Object> tpa : tpaSheetData) {
      try {
        insPlanTypeBean = validateInsPlantypeDataAndConvertToBean(tpa);
        insPlanTypeBean.set("category_id", insurancePlanTypeRespository.getNextSequence());
        Object newCatName = insPlanTypeBean.get("category_name");
        for (BasicDynaBean isExistingBean : insPlanTypeBeanList) {
          if (newCatName.equals(isExistingBean.get("category_name"))) {
            errors.add("Duplicate Entry :" + newCatName);
          }
        }
        insPlanTypeBeanList.add(insPlanTypeBean);
      } catch (HMSException exception) {
        errors.addAll(exception.getErrorsList());
      }
    }

    if (errors.isEmpty()) {
      TransactionStatus insTs = DatabaseHelper.startTransaction("ins_plan_type_upload_begin");
      try {
        insurancePlanTypeRespository.batchInsert(insPlanTypeBeanList);
        DatabaseHelper.commit(insTs);
      } catch (Exception exception) {
        DatabaseHelper.rollback(insTs);
        errors.add(exception.toString());
        throw new HMSException(errors);
      }
    } else {
      throw new HMSException(errors);
    }
  }

  /**
   * Validates ins plan type data.
   * 
   * @param map the map
   * @return bean
   */
  private BasicDynaBean validateInsPlantypeDataAndConvertToBean(Map<String, Object> map) {
    List<String> errors = new ArrayList<String>();

    // checks if the data exists in db
    Object newCatName = map.get("category_name");
    BasicDynaBean isExisting = insurancePlanTypeRespository.findByKey("category_name", newCatName);
    if (isExisting != null) {
      errors.add("Data already exists in db :" + newCatName);
      throw new HMSException(errors);
    }

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

    // Insurance Company Name
    String insCompany = map.get("insurance_company_name").toString();
    BasicDynaBean tempBean = insuranceCompanyRepository.findByKey("insurance_co_name", insCompany);
    if (tempBean != null) {
      map.put("insurance_co_id", tempBean.get("insurance_co_id"));
    } else {
      errors.add("Insurance Company not available in master " + insCompany);
    }

    BasicDynaBean insPlanTypeBean = insurancePlanTypeRespository.getBean();
    ConversionUtils.copyJsonToDynaBean(map, insPlanTypeBean, errors, true);
    if (errors.isEmpty()) {
      return insPlanTypeBean;
    } else {
      throw new HMSException(errors);
    }
  }
}
