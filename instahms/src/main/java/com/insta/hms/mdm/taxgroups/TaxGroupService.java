package com.insta.hms.mdm.taxgroups;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class TaxGroupService.
 */
@Service
public class TaxGroupService extends MasterService {

  /** The tax group repository. */
  private TaxGroupRepository taxGroupRepository;

  /**
   * Instantiates a new tax group service.
   *
   * @param repo
   *          the repo
   * @param validator
   *          the validator
   */
  public TaxGroupService(TaxGroupRepository repo, TaxGroupValidator validator) {
    super(repo, validator);
    this.taxGroupRepository = repo;
  }

  /**
   * Gets the adds the edit page data.
   *
   * @param param
   *          the param
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData(Map param) {
    Map<String, List<BasicDynaBean>> referenceMap = new HashMap<>();

    if (((String[]) param.get("item_group_id")) != null) {
      int groupId = Integer.valueOf(((String[]) param.get("item_group_id"))[0]);
      referenceMap.put("subgroupList", taxGroupRepository.getSubGroupList(groupId));
    }

    referenceMap.put("nameList", ((TaxGroupRepository) getRepository()).getNameList());
    return referenceMap;
  }

  /**
   * Gets the all item group.
   *
   * @return the all item group
   */
  public List<BasicDynaBean> getAllItemGroup() {
    return taxGroupRepository.getItemGroupList();
  }

  /**
   * Gets the item group type.
   *
   * @return the item group type
   */
  public List<BasicDynaBean> getItemGroupType() {
    return taxGroupRepository.getItemGroupType();
  }

  /**
   * Gets the item grp map.
   *
   * @return the item grp map
   */
  public Map<String, Integer> getItemGrpMap() {
    Map<String, Integer> itemGrpMaps = new HashMap<>();
    List<BasicDynaBean> list = taxGroupRepository.getItemGroups();
    for (BasicDynaBean bean : list) {
      itemGrpMaps.put((String) bean.get("item_group_name"), (Integer) bean.get("item_group_id"));
    }
    return itemGrpMaps;
  }

  /**
   * Find by group codes.
   *
   * @param groupCodes
   *          the group codes
   * @return the list
   */
  public List<BasicDynaBean> findByGroupCodes(String[] groupCodes) {
    Map<String, Object> filter = new HashMap<>();
    List<BasicDynaBean> beans = new ArrayList<>();
    for (String groupCode : groupCodes) {
      filter.put("group_code", groupCode);
      filter.put("status", "A");
      List<BasicDynaBean> list = taxGroupRepository.listAll(null, filter, null);
      beans.addAll(list);
      filter.clear();
    }
    return beans;
  }

  public List<BasicDynaBean> getTaxItemGroups() {
    return taxGroupRepository.getTaxItemGroups();
  }
}
