package com.insta.hms.integration.scm.inventory;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.RequestContext;
import com.insta.hms.batchjob.CsvItemsExportJob;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.inventory.stocks.StoreItemCodesRepository;
import com.insta.hms.jobs.JobService;
import com.insta.hms.mdm.integration.packageuom.PackageUomIntegrationService;
import com.insta.hms.mdm.itemtaxuploaddownloads.StoreItemSubGroupService;
import com.insta.hms.mdm.servicegroup.ServiceGroupService;
import com.insta.hms.mdm.servicesubgroup.ServiceSubGroupService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class ScmItemMasterOutBoundService {
  private static final String MEDICINE_ID = "medicine_id";
  private static final String SERVICE_SUB_GROUP_ID = "service_sub_group_id";
  private final SimpleDateFormat dateTimeFormat =
      new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

  /**
   * The job service.
   */
  @LazyAutowired
  private JobService jobService;

  @LazyAutowired
  private ServiceGroupService serviceGroupService;

  @LazyAutowired
  private ServiceSubGroupService serviceSubGroupService;

  @LazyAutowired
  private PackageUomIntegrationService packageUomService;

  @LazyAutowired
  private StoreItemSubGroupService storeItemSubGroupService;

  @LazyAutowired
  private StoreItemCodesRepository storeItemCodesRepository;

  /**
   * Gets the item master map from the bean and other data also get fetched and set in map.
   *
   * @param itemBean the itemBean
   * @return the map
   */
  public Map<String, Object> getItemMasterJobMap(BasicDynaBean itemBean) {
    Map<String, Object> jobData = new HashMap<>();
    //TODO replace field names with constants from CsvItemsExportJob
    jobData.put("TRANSACTION_ID", itemBean.get(MEDICINE_ID));
    jobData.put("TRANSACTION_TYPE", "ITEM_MASTER");
    jobData.put("MEDICINE_NAME", itemBean.get("medicine_name"));
    jobData.put("MEDICINE_SHORT_NAME", itemBean.get("medicine_short_name"));
    jobData.put("CUST_ITEM_CODE", itemBean.get("cust_item_code"));
    jobData.put("MED_CATEGORY_ID", itemBean.get("med_category_id"));
    jobData.put("MED_CATEGORY_INTEGRATION_ID", itemBean.get("integration_category_id"));
    jobData.put("ITEM_CATEGORY_NAME", itemBean.get("category_name"));
    jobData.put("SERVICE_SUB_GROUP_ID", itemBean.get(SERVICE_SUB_GROUP_ID));
    Map<String, Object> subGroupFilterMap = new HashMap<>();
    subGroupFilterMap.put(SERVICE_SUB_GROUP_ID, itemBean.get(SERVICE_SUB_GROUP_ID));
    BasicDynaBean subgroupBean = serviceSubGroupService.findByPk(subGroupFilterMap);
    jobData.put("SERVICE_SUB_GROUP_NAME", subgroupBean.get("service_sub_group_name"));
    jobData.put("SERVICE_SUB_GROUP_INTEGRATION_ID",
        subgroupBean.get("integration_service_sub_group_id"));
    Map<String, Object> groupFilterMap = new HashMap<>();
    groupFilterMap.put("service_group_id", itemBean.get("service_group_id"));
    BasicDynaBean serviceGroupBean = serviceGroupService.findByPk(groupFilterMap);
    jobData.put("SERVICE_GROUP_NAME", serviceGroupBean.get("service_group_name"));
    jobData.put("SERVICE_GROUP_INTEGRATION_ID",
        serviceGroupBean.get("integration_service_group_id"));
    jobData.put("SERVICE_GROUP_CODE", itemBean.get("service_group_id"));
    String packUOM = (String) itemBean.get("package_uom");
    jobData.put("PACKAGE_UOM", packUOM);
    String issueUOM = (String) itemBean.get("issue_units");
    jobData.put("ISSUE_UOM", issueUOM);
    jobData.put("PACKAGE_SIZE", itemBean.get("issue_base_unit"));
    Map<String, Object> uomFilterMap = new HashMap<>();
    uomFilterMap.put("package_uom", packUOM);
    uomFilterMap.put("issue_uom", issueUOM);
    jobData.put("INTEGRATION_UOM_ID",
        packageUomService.findByKey(uomFilterMap).get("integration_uom_id"));
    jobData.put("BATCH_NO_APPLICABLE", itemBean.get("batch_no_applicable"));
    jobData.put("ITEM_BARCODE_ID", itemBean.get("item_barcode_id"));
    jobData.put("MAX_COST_PRICE", itemBean.get("max_cost_price"));
    jobData.put("ITEM_SELLING_PRICE", itemBean.get("item_selling_price"));
    jobData.put("STATUS", itemBean.get("status"));
    jobData.put("VALUE", itemBean.get("value"));
    jobData.put("HIGH_COST_CONSUMABLE", itemBean.get("high_cost_consumable"));
    jobData.put("TAX_TYPE", itemBean.get("tax_type"));
    jobData.put("TAX_RATE", itemBean.get("tax_rate"));
    jobData.put("BIN", itemBean.get("bin"));
    Date createdDateTime = (Date) itemBean.get("created_timestamp");
    jobData.put("CREATED_DATE_TIME", dateTimeFormat.format(createdDateTime));
    Date lastUpdatedDateTime = (Date) itemBean.get("updated_timestamp");
    jobData.put("LAST_UPDATED_DATE_TIME", dateTimeFormat.format(lastUpdatedDateTime));
    List<BasicDynaBean> taxBeans = storeItemSubGroupService
        .getItemSubgroupCodes((int) itemBean.get(MEDICINE_ID));
    Iterator<BasicDynaBean> taxBeanIterator = taxBeans.iterator();
    StringBuilder taxCode = new StringBuilder();
    StringBuilder taxRate = new StringBuilder();
    while (taxBeanIterator.hasNext()) {
      BasicDynaBean taxBean = taxBeanIterator.next();
      if (taxBean.get("integration_subgroup_id") != null) {
        taxCode.append(taxBean.get("integration_subgroup_id").toString());
        if (taxBeanIterator.hasNext()) {
          taxCode.append(",");
        }
      }
      taxRate.append(taxBean.get("tax_rate").toString());
      if (taxBeanIterator.hasNext()) {
        taxRate.append(",");
      }
    }
    jobData.put("TAX_SUB_GROUPS", taxCode.toString());
    jobData.put("TAX_RATE", taxRate.toString());

    List<BasicDynaBean> healthAuthCodes = storeItemCodesRepository
        .getMappedDrugCode((int) itemBean.get(MEDICINE_ID));
    Iterator<BasicDynaBean> healthAuthIterator = healthAuthCodes.iterator();
    StringBuilder healthAuthCode = new StringBuilder();
    StringBuilder healthAuthCodeType = new StringBuilder();
    StringBuilder healthAuthDrugCode = new StringBuilder();
    while (healthAuthIterator.hasNext()) {
      BasicDynaBean healthAuthBean = healthAuthIterator.next();
      healthAuthCode.append(healthAuthBean.get("health_authority").toString());
      if (healthAuthIterator.hasNext()) {
        healthAuthCode.append(",");
      }

      healthAuthCodeType.append(healthAuthBean.get("code_type").toString());
      if (healthAuthIterator.hasNext()) {
        healthAuthCodeType.append(",");
      }

      healthAuthDrugCode.append(healthAuthBean.get("item_code").toString());
      if (healthAuthIterator.hasNext()) {
        healthAuthDrugCode.append(",");
      }
    }
    jobData.put("HEALTH_AUTORITY_CODE", healthAuthCode);
    jobData.put("HEALTH_AUTORITY_CODE_TYPE", healthAuthCodeType);
    jobData.put("HEALTH_AUTORITY_DRUG_CODE", healthAuthDrugCode);
    jobData.put(CsvItemsExportJob.UOM_ID, itemBean.get("uom_id"));
    jobData.put(CsvItemsExportJob.ITEM_FORM_ID, itemBean.get("item_form_id"));
    jobData.put(CsvItemsExportJob.ITEM_FORM_NAME, itemBean.get("item_form_name"));
    jobData.put(CsvItemsExportJob.INTEGRATION_FORM_ID, itemBean.get("integration_form_id"));
    jobData.put(CsvItemsExportJob.MANF_CODE, itemBean.get("manf_code"));
    jobData.put(CsvItemsExportJob.MANF_NAME, itemBean.get("manf_name"));
    jobData.put(CsvItemsExportJob.MANF_MNEMONIC, itemBean.get("manf_mnemonic"));
    jobData.put(CsvItemsExportJob.INTEGRATION_MANF_ID, itemBean.get("integration_manf_id"));
    jobData.put(CsvItemsExportJob.GENERIC_CODE, itemBean.get("generic_code"));
    jobData.put(CsvItemsExportJob.GENERIC_NAME, itemBean.get("generic_name"));
    jobData
        .put(CsvItemsExportJob.INTEGRATION_GENERIC_ID, itemBean.get("integration_generic_name_id"));
    jobData.put(CsvItemsExportJob.FIELD_ITEM_STRENGTH, itemBean.get("item_strength"));
    jobData.put(CsvItemsExportJob.FIELD_STRENGTH_UNIT_ID, itemBean.get("item_strength_units"));
    jobData.put(CsvItemsExportJob.FIELD_UNIT_NAME, itemBean.get("unit_name"));
    jobData.put(CsvItemsExportJob.FIELD_INTEGRATION_STRENGTH_UNIT_ID,
        itemBean.get("integration_strength_unit_id"));

    return jobData;
  }

  /**
   * Schedule txn export.
   *
   * @param transactions    the transactions
   * @param transactionType the transaction type
   */
  public void scheduleTxnExport(List<Map<String, Object>> transactions, String transactionType) {
    Map<String, Object> jobData = new HashMap<>();
    Date date = new Date();
    jobData.put("schema", RequestContext.getSchema());
    jobData.put("eventId", transactionType + "_" + date.getTime());
    jobData.put("eventData", transactions);

    jobService.scheduleImmediate(
        buildJob((String) jobData.get("eventId"), CsvItemsExportJob.class, jobData));
  }

}
