package com.insta.hms.mdm.servicesubgroup;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The Class ServiceSubGroupService.
 */
@Service
public class ServiceSubGroupService extends MasterService {

  /** The services sub groups columns. */
  List<String> servicesSubGroupsColumns = Arrays.asList("service_sub_group_name",
      "service_sub_group_id", "service_group_id");

  /**
   * Instantiates a new service sub group service.
   *
   * @param repo
   *          the repo
   * @param validator
   *          the validator
   */
  public ServiceSubGroupService(ServiceSubGroupRepositry repo, ServiceSubGroupValidator validator) {
    super(repo, validator);
  }

  /**
   * List active record.
   *
   * @return the list
   */
  public List<BasicDynaBean> listActiveRecord() {
    return ((ServiceSubGroupRepositry) getRepository()).listAll(null, "status", "A", null);
  }

  /**
   * List order active record.
   *
   * @return the list
   */
  public List<BasicDynaBean> listOrderActiveRecord() {
    return ((ServiceSubGroupRepositry) getRepository()).listAll(servicesSubGroupsColumns, "status",
        "A", "service_sub_group_name");
  }

  /**
   * List all.
   *
   * @return the list
   */
  public List<BasicDynaBean> listAll() {
    return ((ServiceSubGroupRepositry) getRepository()).listAll();
  }

  /**
   * Gets the service group id with name.
   *
   * @param filterMap
   *          the filter map
   * @return the service group id with name
   */
  public List<BasicDynaBean> getServiceGroupIdWithName(Map filterMap) {
    return ((ServiceSubGroupRepositry) getRepository())
        .listAll(Arrays.asList("service_sub_group_id", "service_group_id"), filterMap, null);
  }

  /**
   * Gets the service group id.
   *
   * @param grpIdList
   *          the grp Id List
   * @return the service group id
   */
  public List getAllServiceSubGrps(List grpIdList) {
    List<BasicDynaBean> list = ((ServiceSubGroupRepositry) getRepository())
        .getAllServiceSubGrps(grpIdList);
    List values = new ArrayList();
    for (BasicDynaBean bean : list) {
      values.add(bean.get("service_sub_group_id"));
    }
    return values;
  }
}
