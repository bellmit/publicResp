package com.insta.hms.mdm.equipment;

import com.insta.hms.common.AutoIdGenerator;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.bedtypes.BedTypeService;
import com.insta.hms.mdm.bulk.BulkDataService;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.equipmentcharges.EquipmentChargesService;
import com.insta.hms.mdm.equuipmentorganization.EquipmentOrganizationService;
import com.insta.hms.mdm.organization.OrganizationService;
import com.insta.hms.mdm.servicegroup.ServiceGroupService;
import com.insta.hms.mdm.servicesubgroup.ServiceSubGroupService;
import com.insta.hms.mdm.taxgroups.TaxGroupService;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@Service
public class EquipmentService extends BulkDataService {

  @LazyAutowired private BedTypeService bedTypeService;
  @LazyAutowired private EquipmentChargesService equipmentChargeService;
  @LazyAutowired private EquipmentRepository equipmentRepository;
  @LazyAutowired private ServiceSubGroupService serviceSubGroupService;
  @LazyAutowired private ServiceGroupService serviceGroupService;
  @LazyAutowired private DepartmentService departmentService;
  @LazyAutowired private OrganizationService organizationService;
  @LazyAutowired private EquipmentOrganizationService equipmentOrganizationService;
  @LazyAutowired private TaxGroupService taxGroupService;
  @LazyAutowired private TaxSubGroupService taxSubGroupService;
  @LazyAutowired private EquipmentInsuranceCategoryRepository equipmentInsuranceCategoryRepository;

  public EquipmentService(
      EquipmentRepository equipmentRepository,
      EquipmentValidator equipmentValidator,
      EquipmentCsvBulkDataEntity csvDataEntity) {
    super(equipmentRepository, equipmentValidator, csvDataEntity);
  }

  @Override
  public Map<String, List<BasicDynaBean>> getMasterData() {
    return null;
  }

  /**
   * get list of page date.
   *
   * @param requestParams Map
   * @return map
   */
  public Map<String, List<BasicDynaBean>> getListPageData(Map requestParams) {

    String orgId = null;
    String[] orgid = (String[]) requestParams.get("org_id");
    if (orgid == null || orgid.equals("")) {
      orgId = "ORG0001";
    } else {
      orgId = orgid[0];
    }
    String chargeType = null;
    String[] chargetype = (String[]) requestParams.get("_chargeType");
    if ((chargetype == null) || chargetype.equals("")) {
      chargeType = "daily_charge";
    } else {
      chargeType = chargetype[0];
    }
    List<BasicDynaBean> chargeTypes = new ArrayList<BasicDynaBean>();
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("chargeType");
    BasicDynaBean chargetypebean = builder.build();
    chargetypebean.set("chargeType", chargeType);
    chargeTypes.add(chargetypebean);

    Map<String, List<BasicDynaBean>> beanMap = new HashMap<String, List<BasicDynaBean>>();

    PagedList list =
        equipmentRepository.getEquipmentDetails(
            requestParams, ConversionUtils.getListingParameter(requestParams));
    List<String> ids = new ArrayList<String>();
    for (Map obj : (List<Map>) list.getDtoList()) {
      ids.add((String) obj.get("eq_id"));
    }
    List<BasicDynaBean> bedTypes = bedTypeService.getAllBedTypes();
    List<BasicDynaBean> chargeList = equipmentChargeService.getAllChargesForOrg(orgId, ids);
    List<BasicDynaBean> names = equipmentRepository.lookup(false);
    beanMap.put("orgMasterData", organizationService.getRateSheetForCharge());
    beanMap.put("serviceSubGroup", serviceSubGroupService.listOrderActiveRecord());
    beanMap.put("departments", departmentService.lookup(true));
    beanMap.put("bedTypes", bedTypes);
    beanMap.put("equipmentCharges", chargeList);
    beanMap.put("equipmentNames", names);
    beanMap.put("chargeType", chargeTypes);

    return beanMap;
  }

  /**
   * get add page data.
   *
   * @param params Map
   * @return Map
   */
  public Map<String, List<BasicDynaBean>> getAddPageData(Map params) {
    Map<String, List<BasicDynaBean>> beanMap = new HashMap<String, List<BasicDynaBean>>();
    String orgId = null;
    String[] orgid = (String[]) params.get("org_id");
    if (orgid == null || orgid.equals("")) {
      orgId = "ORG0001";
    } else {
      orgId = orgid[0];
    }
    List<BasicDynaBean> bedTypes = bedTypeService.getAllBedTypes();
    List<BasicDynaBean> serviceSubGroupsList = serviceSubGroupService.listOrderActiveRecord();
    List<BasicDynaBean> names = equipmentRepository.lookup(false);
    List<BasicDynaBean> serviceGroups = serviceGroupService.getAllServiceGroups();
    List<BasicDynaBean> departments = departmentService.lookup(true);

    beanMap.put("insuranceCategory", equipmentRepository.getInsuranceCategories());
    beanMap.put("serviceGroups", serviceGroups);
    beanMap.put("bedTypes", bedTypes);
    beanMap.put("serviceSubGroupsList", serviceSubGroupsList);
    beanMap.put("equipmentNames", names);
    beanMap.put("departments", departments);

    beanMap.put(
        "itemGroupTypeList",
        new GenericRepository("item_group_type").listAll(null, "item_group_type_id", "TAX"));
    List<BasicDynaBean> itemGroupListJson = taxGroupService.getAllItemGroup();
    beanMap.put("itemGroupListJson", itemGroupListJson);
    //List<BasicDynaBean> itemSubGroupListJson = taxSubGroupService.getAllItemSubGroup();
    List<BasicDynaBean> itemSubGroupList =
        taxSubGroupService.getItemSubGroupList(new java.sql.Date(new java.util.Date().getTime()));
    Iterator<BasicDynaBean> itemSubGroupListIterator = itemSubGroupList.iterator();
    List<BasicDynaBean> validateItemSubGrouList = new ArrayList<BasicDynaBean>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String currentDateStr = sdf.format(new java.util.Date());
    while (itemSubGroupListIterator.hasNext()) {
      BasicDynaBean itenSubGroupbean = itemSubGroupListIterator.next();
      if (itenSubGroupbean.get("validity_end") != null) {
        Date endDate = (Date) itenSubGroupbean.get("validity_end");

        try {
          if (sdf.parse(currentDateStr).getTime() <= endDate.getTime()) {
            validateItemSubGrouList.add(itenSubGroupbean);
          }
        } catch (ParseException ex) {
          continue;
        }
      } else {
        validateItemSubGrouList.add(itenSubGroupbean);
      }
    }
    beanMap.put("itemSubGroupListJson", validateItemSubGrouList);

    return beanMap;
  }

  /**
   * insert equipment details.
   *
   * @param req HttpServletRequest
   * @param parameters Map
   * @param msg StringBuilder
   * @return String equipmentId
   */
  public String insertEquipmentDetails(
      HttpServletRequest req, Map<String, String[]> parameters, StringBuilder msg) {

    BasicDynaBean equipmentBean = equipmentRepository.getBean();
    String equipmentId = AutoIdGenerator.getNewId("EQ_ID", "EQUIPMENT_MASTER", "EQIPMENTID");
    ArrayList errors = new ArrayList();
    String[] serviceSubGroupId = parameters.get("serviceSubGroupId");
    ConversionUtils.copyToDynaBean(parameters, equipmentBean, errors);
    equipmentBean.set("eq_id", equipmentId);
    equipmentBean.set("service_sub_group_id", Integer.parseInt(serviceSubGroupId[0]));
    if (!errors.isEmpty()) {
      msg = msg.append("Incorrectly formatted values supplied");
      return null;
    }
    boolean success = true;
    success = insertEquipment(equipmentBean);

    if (success) {
      success = saveOrUpdateInsuranceCategory(equipmentId, req);
    }

    if (success) {
      String eqId = (String) equipmentBean.get("eq_id");
      success = saveOrUpdateItemSubGroup(eqId, req);
    }

    if (!success) {
      msg = msg.append("Equipment with the same name already exists");
      return null;
    }
    success &= equipmentChargeService.initItemCharges(equipmentId);
    if (!success) {
      msg = msg.append("Equipment failed to insert charges.");
      return null;
    }
    return equipmentId;
  }

  private boolean insertEquipment(BasicDynaBean equipmentBean) {
    int result = equipmentRepository.insert(equipmentBean);
    if (result == 0) {
      return false;
    }
    return true;
  }

  /**
   * get edit page data.
   *
   * @param equipmentId String
   * @param orgId String
   * @return Map
   */
  public Map getEditPageDate(String equipmentId, String orgId) {
    Map detailsMap = new HashMap();
    //Map<String, List<BasicDynaBean>> detailsMap = new HashMap<String, List<BasicDynaBean>>();
    List<BasicDynaBean> bedTypes = bedTypeService.getAllBedTypes();
    BasicDynaBean bean = equipmentRepository.getEquipmentDetails(equipmentId, orgId);
    int serviceSubGroupId = (Integer) (bean).get("service_sub_group_id");
    Map params = new HashMap();
    params.put("service_sub_group_id", serviceSubGroupId);
    String groupId = serviceSubGroupService.findByPk(params).get("service_group_id").toString();
    List<BasicDynaBean> serviceSubGroupsList = serviceSubGroupService.listOrderActiveRecord();
    List<BasicDynaBean> names = equipmentRepository.lookup(false);
    List<BasicDynaBean> serviceGroups = serviceGroupService.getAllServiceGroups();
    List<BasicDynaBean> departments = departmentService.lookup(true);

    detailsMap.put("bedTypes", bedTypes);
    detailsMap.put("groupId", groupId);
    detailsMap.put("serviceSubGroup", serviceSubGroupId);
    detailsMap.put("departmentId", bean.get("dept_id"));
    detailsMap.put("bean", bean);
    detailsMap.put("serviceGroups", serviceGroups);
    detailsMap.put("serviceSubGroupsList", ConversionUtils.listBeanToListMap(serviceSubGroupsList));
    detailsMap.put("equipmentNames", ConversionUtils.listBeanToListMap(names));
    detailsMap.put("departments", departments);
    List<BasicDynaBean> activeInsuranceCategories =
        equipmentRepository.getActiveInsuranceCategories(equipmentId);
    List<Integer> activeInsuranceCategory = new ArrayList<>();
    for (BasicDynaBean activeInsurance : activeInsuranceCategories) {
      activeInsuranceCategory.add((Integer) activeInsurance.get("insurance_category_id"));
    }
    detailsMap.put("activeInsuranceCategory", activeInsuranceCategory);
    detailsMap.put("insuranceCategory", equipmentRepository.getInsuranceCategories());

    detailsMap.put(
        "itemGroupTypeList",
        ConversionUtils.listBeanToListMap(
            new GenericRepository("item_group_type").listAll(null, "item_group_type_id", "TAX")));
    List<BasicDynaBean> taxsubgroup = equipmentRepository.getEquipItemSubGroupDetails(equipmentId);
    detailsMap.put("taxsubgroup", ConversionUtils.listBeanToListMap(taxsubgroup));
    List<BasicDynaBean> itemGroupListJson = taxGroupService.getAllItemGroup();
    detailsMap.put("itemGroupListJson", ConversionUtils.listBeanToListMap(itemGroupListJson));
    //List<BasicDynaBean> itemSubGroupListJson = taxSubGroupService.getAllItemSubGroup();
    List<BasicDynaBean> itemSubGroupList =
        taxSubGroupService.getItemSubGroupList(new java.sql.Date(new java.util.Date().getTime()));
    Iterator<BasicDynaBean> itemSubGroupListIterator = itemSubGroupList.iterator();
    List<BasicDynaBean> validateItemSubGrouList = new ArrayList<BasicDynaBean>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String currentDateStr = sdf.format(new java.util.Date());
    while (itemSubGroupListIterator.hasNext()) {
      BasicDynaBean itemSubGroupbean = itemSubGroupListIterator.next();
      if (itemSubGroupbean.get("validity_end") != null) {
        Date endDate = (Date) itemSubGroupbean.get("validity_end");

        try {
          if (sdf.parse(currentDateStr).getTime() <= endDate.getTime()) {
            validateItemSubGrouList.add(itemSubGroupbean);
          }
        } catch (ParseException ex) {
          continue;
        }
      } else {
        validateItemSubGrouList.add(itemSubGroupbean);
      }
    }
    detailsMap.put(
        "itemSubGroupListJson", ConversionUtils.listBeanToListMap(validateItemSubGrouList));

    return detailsMap;
  }

  /**
   * update equipment details.
   *
   * @param req HttpServletRequest
   * @param parameters Map
   * @param equipId String
   * @param msg StringBuilder
   * @return boolean
   */
  public boolean updateEquipmentDetails(
      HttpServletRequest req, Map<String, String[]> parameters, String equipId, StringBuilder msg) {

    BasicDynaBean equipmentBean = equipmentRepository.getBean();
    ArrayList errors = new ArrayList();
    String[] serviceSubGroupId = parameters.get("serviceSubGroupId");
    ConversionUtils.copyToDynaBean(parameters, equipmentBean, errors);
    equipmentBean.set("eq_id", equipId);
    equipmentBean.set("service_sub_group_id", Integer.parseInt(serviceSubGroupId[0]));

    if (!errors.isEmpty()) {
      msg = msg.append("Incorrectly formatted values supplied");
      return false;
    }
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("eq_id", equipId);
    boolean success = true;
    success = updateEquipment(equipmentBean, keys);

    if (success) {
      success = saveOrUpdateInsuranceCategory(equipId, req);
    }

    if (success) {
      String eqId = (String) equipmentBean.get("eq_id");
      success = saveOrUpdateItemSubGroup(eqId, req);
    }
    if (!success) {
      msg = msg.append("Equipment with the same name already exists");
      return false;
    }
    msg = msg.append("Equipment updated successfully");
    return success;
  }

  /**
   * update equipment info.
   *
   * @param equipmentBean BasicDynaBean
   * @param keys Map
   * @return boolean
   */
  public boolean updateEquipment(BasicDynaBean equipmentBean, Map<String, Object> keys) {
    int result = equipmentRepository.update(equipmentBean, keys);
    if (result == 0) {
      return false;
    }
    return true;
  }

  /**
   * get list of edit charges data.
   *
   * @param equipId String
   * @param orgId String
   * @return Map
   */
  public Map getListEditChargeData(String equipId, String orgId) {
    Map chargesMap = new HashMap();

    List<BasicDynaBean> bedTypes = bedTypeService.getAllBedTypes();
    BasicDynaBean bean = equipmentRepository.getEquipmentDetails(equipId, orgId);
    List<BasicDynaBean> chargeList =
        equipmentChargeService.getAllChargesForOrgEquipment(orgId, equipId);

    chargesMap.put("bedTypes", ConversionUtils.listBeanToListMap(bedTypes));
    chargesMap.put("bean", bean);
    chargesMap.put("charges", ConversionUtils.listBeanToMapMap(chargeList, "bed_type"));
    List<BasicDynaBean> derivedRatePlanDetails =
        equipmentChargeService.getDerivedRatePlanDetails(orgId, equipId);
    if (derivedRatePlanDetails.size() < 0) {
      chargesMap.put("derivedRatePlanDetails", Collections.EMPTY_LIST);
    } else {
      chargesMap.put(
          "derivedRatePlanDetails", ConversionUtils.listBeanToListMap(derivedRatePlanDetails));
    }
    chargesMap.put(
        "orgMasterData",
        ConversionUtils.listBeanToListMap(organizationService.getRateSheetForCharge()));
    List<BasicDynaBean> names = equipmentRepository.lookup(false);
    chargesMap.put("equipmentNames", ConversionUtils.listBeanToListMap(names));

    return chargesMap;
  }

  /**
   * update charges.
   *
   * @param parameters Map
   * @param orgId String
   * @param equipId String
   * @param msg String
   * @return boolean
   * @throws Exception Exception
   */
  public boolean updatecharges(
      Map<String, String[]> parameters, String orgId, String equipId, StringBuilder msg)
      throws Exception {

    boolean status = true;
    status = equipmentOrganizationService.updateEquipmentOrgDetails(parameters, equipId, orgId);
    if (!status) {
      return false;
    }
    status &= equipmentChargeService.updateEquipCharges(parameters, equipId, orgId, msg);

    return status;
  }

  public List<BasicDynaBean> getEquipmentItemSubGroupTaxDetails(String actDescriptionId) {
    return ((EquipmentRepository) getRepository())
        .getEquipmentItemSubGroupTaxDetails(actDescriptionId);
  }

  private boolean saveOrUpdateItemSubGroup(String eqId, HttpServletRequest request) {
    Map params = request.getParameterMap();
    List errors = new ArrayList();

    //boolean flag = true;
    int result = 1;
    String[] itemSubgroupId = request.getParameterValues("item_subgroup_id");
    String[] delete = request.getParameterValues("deleted");

    if (errors.isEmpty()) {
      if (itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")) {
        GenericRepository itemsubgroupdao = new GenericRepository("equipment_item_sub_groups");
        BasicDynaBean itemsubgroupbean = itemsubgroupdao.getBean();
        ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
        BasicDynaBean records = itemsubgroupdao.findByKey("eq_id", eqId);
        if (records != null) {
          result = itemsubgroupdao.delete("eq_id", eqId);
        }
        //result = itemsubgroupdao.delete("test_id", test_id);
        for (int i = 0; i < itemSubgroupId.length; i++) {
          if (itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
            if (delete[i].equalsIgnoreCase("false")) {
              itemsubgroupbean.set("eq_id", eqId);
              itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
              result = itemsubgroupdao.insert(itemsubgroupbean);
            }
          }
        }
        if (result == 0) {
          return false;
        } else {
          return true;
        }
      }
    }
    return true;
  }

  /**
   * Save or update Insurance Category.
   *
   * @param equipmentId the equipment id
   * @param request the request
   * @return true, if successful
   */
  private boolean saveOrUpdateInsuranceCategory(String equipmentId, HttpServletRequest request) {
    int result = 1;
    String[] insuranceCategories = request.getParameterValues("insurance_category_id");
    if (insuranceCategories != null
        && insuranceCategories.length > 0
        && !insuranceCategories[0].equals("")) {
      BasicDynaBean insuranceCategoryBean = equipmentInsuranceCategoryRepository.getBean();
      Map<String, Object> criteriaParams = new HashMap<>();
      criteriaParams.put("equipment_id", equipmentId);
      List<BasicDynaBean> records =
          equipmentInsuranceCategoryRepository.findByCriteria(criteriaParams);
      if (records != null && records.size() > 0) {
        result = equipmentInsuranceCategoryRepository.delete("equipment_id", equipmentId);
      }
      for (String insuranceCategory : insuranceCategories) {
        insuranceCategoryBean.set("equipment_id", equipmentId);
        insuranceCategoryBean.set("insurance_category_id", Integer.parseInt(insuranceCategory));
        result = equipmentInsuranceCategoryRepository.insert(insuranceCategoryBean);
      }
      if (result == 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * Find by key.
   *
   * @param keyColumn the key column
   * @param identifier the identifier
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(String keyColumn, Object identifier) {
    return ((EquipmentRepository) getRepository()).findByKey(keyColumn, identifier);
  }

  /**
   * Gets the equipment charge.
   *
   * @param equipmentId the equipment id
   * @param bedType the bed type
   * @param ratePlanId the rate plan id
   * @return the equipment charge
   */
  public BasicDynaBean getEquipmentCharge(String equipmentId, String bedType, String ratePlanId) {
    return ((EquipmentRepository) getRepository())
        .getEquipmentCharge(equipmentId, bedType, ratePlanId);
  }
}
