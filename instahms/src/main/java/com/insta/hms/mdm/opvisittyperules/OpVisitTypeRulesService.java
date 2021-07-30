package com.insta.hms.mdm.opvisittyperules;

import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterDetailsService;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.opvisittyperuleapplicability.OpVisitTypeRuleApplicabilityRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class OpVisitTypeRulesService.
 */
@Service("opVisitTypeRulesService")
public class OpVisitTypeRulesService extends MasterDetailsService {

  /* The op visit type rule details repository */
  @LazyAutowired
  private OpVisitTypeRuleDetailsRepository opVisitTypeRuleDetailsRepository;
  
  /* The op visit type rule repository */
  @LazyAutowired 
  private OpVisitTypeRulesRepository opVisitTypeRulesRepository;
  
  /* The op visit type rule applicability repository. */
  @LazyAutowired
  OpVisitTypeRuleApplicabilityRepository opVisitTypeRuleApplicabilityRepository;


  /**
   * Instantiates a new Op Visit Type Rules Service.
   *
   * @param opVisitTypeRulesRepository
   *          the op visit type rules repository
   * @param opVisitTypeRulesValidator
   *          the op visit type rules validator
   * @param opVisitTypeRuleDetailsRepository
   *          the op visit type rules details repository
   */
  public OpVisitTypeRulesService(OpVisitTypeRulesRepository opVisitTypeRulesRepository,
      OpVisitTypeRulesValidator opVisitTypeRulesValidator,
      OpVisitTypeRuleDetailsRepository opVisitTypeRuleDetailsRepository) {
    super(opVisitTypeRulesRepository, opVisitTypeRulesValidator, opVisitTypeRuleDetailsRepository);
    this.opVisitTypeRuleDetailsRepository = opVisitTypeRuleDetailsRepository;
  }

  /**
   * Gets the adds the show page data.
   *
   * @param paramMap
   *          the param map
   * @return the adds the show page data
   */
  @SuppressWarnings("rawtypes")
  public Map<String, List<BasicDynaBean>> getAddShowPageData(Map<String, String[]> paramMap) {

    Map<String, List<BasicDynaBean>> addEditReference = new HashMap<String, List<BasicDynaBean>>();
    if (!paramMap.isEmpty() && !paramMap.containsKey("error")) {
      List<BasicDynaBean> visitTypeRules = opVisitTypeRuleDetailsRepository
          .getVisitTypeRules(Integer.parseInt((String) paramMap.get("rule_id")[0]));
      addEditReference.put("opVisitTypeRules", visitTypeRules);      
      List<BasicDynaBean> ruleApplicabilities = opVisitTypeRuleApplicabilityRepository
          .getRuleApplicabilities(Integer.parseInt((String) paramMap.get("rule_id")[0]));
      addEditReference.put("ruleApplicabilities", ruleApplicabilities);
      
    }
    addEditReference.put("opVisitTypeRuleMaster",
        ((OpVisitTypeRulesRepository) getRepository()).listAll());
    return addEditReference;
  }

  /**
   * List all.
   *
   * @param keys
   *          the keys
   * @param sortColumn
   *          the sort column
   * @return the list
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<BasicDynaBean> listAll(Map keys, String sortColumn) {
    return this.getRepository().listAll(null, keys, sortColumn);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected SearchQueryAssembler getSearchQueryAssembler(Map params,
      Map<LISTING, Object> listingParams) {

    return ((OpVisitTypeRulesRepository) getRepository()).getOpVisitTypeRulesQa(params,
        listingParams);
  }

  /**
   * Gets the rule.
   *
   * @param ruleId the rule id
   * @return the rule
   */
  public BasicDynaBean getRule(Integer ruleId) {
    return opVisitTypeRulesRepository.getRule(ruleId);
  }

  /**
   * Gets the visit type.
   *
   * @param mainVisitType the main visit type
   * @param daysFromMainVisit the days from main visit
   * @param ruleId the rule id
   * @return the visit type
   */
  public BasicDynaBean getVisitType(String mainVisitType, int daysFromMainVisit, int ruleId) {

    return opVisitTypeRulesRepository.getVisitType(mainVisitType,
        daysFromMainVisit, ruleId);
  }
  

  /**
   * Delete.
   *
   * @param bean
   *          the bean
   * @return the integer
   */
  @Override
  public Integer delete(BasicDynaBean bean) {
    String keyColumn = opVisitTypeRulesRepository.getKeyColumn();
    if (null != keyColumn && !keyColumn.isEmpty()) {
      return opVisitTypeRulesRepository.delete(keyColumn, bean.get(keyColumn));
    }
    return 0;
  }
  

  /**
   * Update details.
   *
   * @param parentBean the parent bean
   * @param detailListMapBean the detail list map bean
   * @return the int[]
   */
  @Override
  @Transactional
  public int[] updateDetails(BasicDynaBean parentBean,
      Map<String, List<BasicDynaBean>> detailListMapBean) {

    String parentKeyCol = opVisitTypeRulesRepository.getKeyColumn();
    Map<String, Object> mainKeys = new HashMap<String, Object>();
    mainKeys.put(parentKeyCol, parentBean.get(parentKeyCol));
    Integer res = opVisitTypeRulesRepository.update(parentBean, mainKeys);

    String beanName = opVisitTypeRuleDetailsRepository.getBeanName();

    /* Update the bean */
    List<BasicDynaBean> beanListUpdated = detailListMapBean.get(beanName + "_updated");

    Object parentKeyVal = parentBean.get(parentKeyCol);
    String detailKeyColumn = opVisitTypeRuleDetailsRepository.getKeyColumn();
    if (!beanListUpdated.isEmpty()) {
      for (BasicDynaBean bean : beanListUpdated) {
        opVisitTypeRuleDetailsRepository.delete(detailKeyColumn, 
            bean.get(detailKeyColumn));
        bean.set(parentKeyCol, parentKeyVal);
        Object nextId = null;
        nextId = opVisitTypeRuleDetailsRepository.getNextId();
        bean.set(detailKeyColumn, nextId);
      }
      //On update, have to delete the rule details and insert the updated rule details, 
      //As there is a no_overlapping_range constrain on op_visit_type_rule_details table
      //Postgres would not allow to overlap valid_from and valid_to days.
      opVisitTypeRuleDetailsRepository.batchInsert(beanListUpdated);
    }

    /* Delete the bean */
    List<BasicDynaBean> beanListDeleted = detailListMapBean.get(beanName + "_deleted");
    for (BasicDynaBean bean : beanListDeleted) {
      opVisitTypeRuleDetailsRepository.delete(detailKeyColumn, 
          bean.get(detailKeyColumn));
    }

    /* Insert the bean */
    List<BasicDynaBean> beanListInserted = detailListMapBean.get(beanName + "_inserted");
    for (BasicDynaBean bean : beanListInserted) {
      bean.set(parentKeyCol, parentKeyVal);
      Object nextId = null;
      if (null != detailKeyColumn && opVisitTypeRuleDetailsRepository.supportsAutoId()) {
        nextId = opVisitTypeRuleDetailsRepository.getNextId();
        bean.set(detailKeyColumn, nextId);
      }
    }
    opVisitTypeRuleDetailsRepository.batchInsert(beanListInserted);
    return null;
  }

}
