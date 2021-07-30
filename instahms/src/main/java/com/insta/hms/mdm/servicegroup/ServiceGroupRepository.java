package com.insta.hms.mdm.servicegroup;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class ServiceGroupRepository.
 */
@Repository
public class ServiceGroupRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new service group repository.
   */
  public ServiceGroupRepository() {
    super("service_groups", "service_group_id", "service_group_name");
  }

  /** The Constant GET_ALL_SERVICE_GROUPS. */
  private static final String GET_ALL_SERVICE_GROUPS =
      " SELECT * FROM service_groups WHERE status = 'A' ORDER BY service_group_name";

  /**
   * Gets the all service groups.
   *
   * @return the all service groups
   */
  public List<BasicDynaBean> getAllServiceGroups() {
    return DatabaseHelper.queryToDynaList(GET_ALL_SERVICE_GROUPS);
  }
}


