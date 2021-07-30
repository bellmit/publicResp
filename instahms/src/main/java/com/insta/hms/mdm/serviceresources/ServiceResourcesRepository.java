package com.insta.hms.mdm.serviceresources;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class ServiceResourcesRepository extends MasterRepository<Integer> {

  public ServiceResourcesRepository() {
    super("service_resource_master", "serv_res_id");
  }
  
  private static final String SER_RES_OVERBOOK =
      "SELECT overbook_limit from service_resource_master where serv_res_id=?";

  public Integer getOverbookLimit(String id) {
    return DatabaseHelper.getInteger(SER_RES_OVERBOOK, Integer.parseInt(id));
  }
  
  private static final String SERV_EQUIPMENT_NAME = 
      "SELECT serv_resource_name from service_resource_master where serv_res_id=?";
  
  public String getServEquipmentName(String eqId) {
    return DatabaseHelper.getString(SERV_EQUIPMENT_NAME, Integer.parseInt(eqId));
  }

}
