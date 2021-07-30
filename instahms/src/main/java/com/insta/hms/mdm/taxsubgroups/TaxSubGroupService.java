package com.insta.hms.mdm.taxsubgroups;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.itemsubgroupstaxdetails.ItemSubgroupTaxDetailsRepository;
import com.insta.hms.mdm.itemsubgroupstaxdetails.ItemSubgroupTaxDetailsValidator;

import freemarker.core.InvalidReferenceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class TaxSubGroupService.
 */
@Service
public class TaxSubGroupService extends MasterService {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(TaxSubGroupService.class);

  /**
   * Instantiates a new tax sub group service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public TaxSubGroupService(TaxSubGroupRepository repository, TaxSubGroupValidator validator) {
    super(repository, validator);
  }

  /** The tax sub group repository. */
  @LazyAutowired
  private TaxSubGroupRepository taxSubGroupRepository;
  
  /** The item subgroup tax details repository. */
  @LazyAutowired
  private ItemSubgroupTaxDetailsRepository itemSubgroupTaxDetailsRepository;

  /** The validator. */
  @LazyAutowired
  private TaxSubGroupValidator validator;
  
  /** The item subgroup tax details validator. */
  @LazyAutowired
  private ItemSubgroupTaxDetailsValidator itemSubgroupTaxDetailsValidator;

  /**
   * Insert item sub group.
   *
   * @param parameters
   *          the parameters
   * @return the basic dyna bean
   * @throws ConversionException
   *           the conversion exception
   */
  @Transactional(rollbackFor = Exception.class)
  public BasicDynaBean insertItemSubGroup(Map<String, String[]> parameters)
      throws ConversionException {

    BasicDynaBean itemSubGroupDetailsBean = itemSubgroupTaxDetailsRepository.getBean();
    BasicDynaBean itemSubGroupBean = taxSubGroupRepository.getBean();

    List<String> errors = new ArrayList<>();
    ConversionUtils.copyToDynaBean(parameters, itemSubGroupBean, errors);
    ConversionUtils.copyToDynaBean(parameters, itemSubGroupDetailsBean, errors);

    if (errors.isEmpty()) {
      validator.validateInsert(itemSubGroupBean);
      itemSubgroupTaxDetailsValidator.validateInsert(itemSubGroupDetailsBean);
      String itemSubGroupName = ((String) itemSubGroupBean.get("item_subgroup_name")).trim();
      Integer itemSubGroupId = taxSubGroupRepository.getNextSequence();
      itemSubGroupBean.set("item_subgroup_id", itemSubGroupId);
      taxSubGroupRepository.insert(itemSubGroupBean);
      logger.debug("Added new tax subgroup with taxSubGroupId :" + itemSubGroupId 
          + " and tax subgroup name :" + itemSubGroupName);
      itemSubGroupDetailsBean.set("item_subgroup_id", itemSubGroupId);
      itemSubgroupTaxDetailsRepository.insert(itemSubGroupDetailsBean);

      validator.validateInsert(itemSubGroupBean);
      itemSubgroupTaxDetailsValidator.validateInsert(itemSubGroupDetailsBean);
    } else {
      throw new ConversionException(errors);
    }
    return itemSubGroupBean;
  }

  /**
   * Gets the item sub group.
   *
   * @param itemSubGroupId the item sub group id
   * @return the item sub group
   */
  public BasicDynaBean getItemSubGroup(String itemSubGroupId) {
    BasicDynaBean dynaBean = taxSubGroupRepository
        .getItemSubGroupCode(Integer.parseInt(itemSubGroupId));
    return dynaBean;
  }

  /**
   * Update item sub group.
   *
   * @param parameters the parameters
   * @return the int
   */
  @Transactional(rollbackFor = Exception.class)
  public int updateItemSubGroup(Map<String, String[]> parameters) {

    BasicDynaBean itemSubgroupBean = taxSubGroupRepository.getBean();
    BasicDynaBean itemSubGroupDetailsBean = itemSubgroupTaxDetailsRepository.getBean();

    List<String> errors = new ArrayList<>();

    ConversionUtils.copyToDynaBean(parameters, itemSubgroupBean, errors);
    ConversionUtils.copyToDynaBean(parameters, itemSubGroupDetailsBean, errors);

    validator.validateUpdate(itemSubgroupBean);
    itemSubgroupTaxDetailsValidator.validateUpdate(itemSubGroupDetailsBean);

    Integer itemSubgroupId = null;
    String itemsubgroupid = parameters.get("item_subgroup_id")[0];
    try {
      itemSubgroupId = Integer.parseInt(itemsubgroupid);
    } catch (NumberFormatException exception) {
      errors.add("Tax subgroup id is null");
    }

    List<String> columns = new ArrayList<>();
    // columns of item_sub_groups
    columns.add("item_subgroup_id");
    columns.add("item_subgroup_name");
    columns.add("item_subgroup_display_order");
    columns.add("item_group_id");
    columns.add("status");
    // columns of item_sub_groups_tax_details

    Map<String, Object> keys = null;
    int success = 0;
    if (errors.isEmpty()) {
      keys = new HashMap<>();
      keys.put("item_subgroup_id", itemSubgroupId);
      success = taxSubGroupRepository.update(itemSubgroupBean, keys);
      success = itemSubgroupTaxDetailsRepository.update(itemSubGroupDetailsBean, keys);
    }

    return success;

  }

  /**
   * Gets the all item sub group.
   *
   * @return the all item sub group
   */
  public List<BasicDynaBean> getAllItemSubGroup() {
    return ((TaxSubGroupRepository) getRepository()).getItemSubGroupList();
  }

  /**
   * Gets the item sub group list.
   *
   * @param date the date
   * @return the item sub group list
   */
  public List<BasicDynaBean> getItemSubGroupList(Date date) {
    return ((TaxSubGroupRepository) getRepository()).getItemSubGroup(date);
  }

  /**
   * Gets the tax sub group.
   *
   * @param itemSubGroupId the item sub group id
   * @return the tax sub group
   */
  public List<BasicDynaBean> getTaxSubGroup(int itemSubGroupId) {
    return taxSubGroupRepository.getSubGroup(itemSubGroupId);
  }
  
  public Boolean taxSubGroupHasMasterReferences(int itemSubGroupId) {
    return taxSubGroupRepository.taxSubGroupHasMasterReferences(itemSubGroupId);
  }

  /**
   * Gets the item sub grp map.
   *
   * @return the item sub grp map
   */
  public Map<String, Integer> getItemSubGrpMap() {
    Map<String, Integer> itemSubGrpMaps = new HashMap<>();
    List<BasicDynaBean> list = taxSubGroupRepository.getItemSubGroups();
    for (BasicDynaBean bean : list) {
      itemSubGrpMaps.put((String) bean.get("item_subgroup_name"),
          (Integer) bean.get("item_subgroup_id"));
    }
    return itemSubGrpMaps;
  }

  /**
   * Gets the sub group name.
   *
   * @param itemgrpId the itemgrp id
   * @param itemSubgrpId the item subgrp id
   * @return the sub group name
   */
  public BasicDynaBean getSubGroupName(Integer itemgrpId, Integer itemSubgrpId) {
    return taxSubGroupRepository.getSubGroupId(itemgrpId, itemSubgrpId);
  }

  /**
   * Find by criteria.
   *
   * @param mapcri the mapcri
   * @return the list
   */
  public List<BasicDynaBean> findByCriteria(Map mapcri) {
    return ((TaxSubGroupRepository) getRepository()).findByCriteria(mapcri);
  }

  /**
   * Gets the sub groups.
   *
   * @param filter the filter
   * @return the sub groups
   */
  public List<BasicDynaBean> getSubGroups(Map filter) {
    return ((TaxSubGroupRepository) getRepository()).getSubgroups(filter);
  }

  public List<BasicDynaBean> getValidItemSubGroup(int itemGroupId) {
    return TaxSubGroupRepository.getValidItemSubGroup(itemGroupId);
  }

  /**
   * Validates expressions.
   * 
   * @param expression user defined tax expression
   * @return valid status
   * @throws ArithmeticException throws on process
   */
  
  public boolean isValidExpression(String expression)
      throws ArithmeticException {
    Map results = new HashMap();
    results.put("center_id", 1);
    results.put("dept_name", "DEP0001");
    
    StringWriter writer = new StringWriter();
    String expr = "<#setting number_format=\"##.##\">\n" + expression;
    try { 
      Template expressionTemplate = new 
          Template("expression", new StringReader(expr),new Configuration());
      expressionTemplate.process(results, writer);
    } catch ( TemplateException | ArithmeticException | IOException mulExe ) {
      logger.error("Error while validating expression", mulExe);
      return false;
    }
    
    boolean valid = !writer.toString().contains("[^.\\d]");
    try { 
      if ( !writer.toString().trim().isEmpty() ) { 
        BigDecimal validNumber = new BigDecimal(writer.toString().trim());
        //if no error meaning valid number
      }
    } catch (NumberFormatException ne) { 
      logger.error("", ne);
      valid = false;
    }
    //it check non integer nos
    return  valid;
  }
}
