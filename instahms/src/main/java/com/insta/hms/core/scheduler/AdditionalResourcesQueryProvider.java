package com.insta.hms.core.scheduler;

import com.insta.hms.common.DatabaseHelper;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class AdditionalResourcesQueryProvider.
 */
@Component
public class AdditionalResourcesQueryProvider {

  /** The Constant GENERIC_RESOURCE_NAME. */
  protected static final String GENERIC_RESOURCE_NAME = 
      "select generic_resource_name "
      + "from generic_resource_master where generic_resource_id=?";

  /** The Constant GENERIC_RESOURCE_MASTER_QUERY. */
  protected static final String GENERIC_RESOURCE_MASTER_QUERY = 
      "SELECT generic_resource_name "
      + " as resource_name,generic_resource_id::text as resource_id,"
      + " scheduler_resource_type AS resource_type, overbook_limit, center_id "
      + " FROM generic_resource_master grm"
      + " JOIN generic_resource_type grt ON(grt.generic_resource_type_id = "
      + " grm.generic_resource_type_id)" 
      + " WHERE grm.status='A' AND grt.status = 'A' AND "
      + " schedule=true #CENTER_FILTER# #GEN_TYPE_FILTER#"
      + " order by resource_name";

  /** The Constant LABTECH_MASTER_QUERY. */
  private static final String LABTECH_MASTER_QUERY = 
      "select d.doctor_id::text as resource_id , "
      + " doctor_name as resource_name, 'DOC'"
      + " as resource_type,overbook_limit, center_id "
      + " from doctors d" 
      + " JOIN doctor_center_master dcm ON (dcm.doctor_id = d.doctor_id)"
      + " where d.status= 'A' and d.dept_id in ('DEP_RAD','DEP_LAB') "
      + " and dcm.status= 'A' and d.schedule = true #CENTER_FILTER#" 
      + " order by resource_name ";

  /** The Constant DOC_MASTER_QUERY. */
  private static final String DOC_MASTER_QUERY = 
      "select d.doctor_id::text as resource_id , "
      + " doctor_name as resource_name,"
      + " 'DOC' as resource_type,overbook_limit, center_id "
      + " from doctors d" 
      + " JOIN doctor_center_master dcm ON (dcm.doctor_id = d.doctor_id)"
      + " where d.status= 'A' and dcm.status= 'A'"
      + " and d.schedule = true #CENTER_FILTER#"
      + " order by resource_name ";

  /** The Constant EQID_MASTER_QUERY. */
  private static final String EQID_MASTER_QUERY = 
      "select eq_id::text as resource_id , "
      + " equipment_name as resource_name, 'EQID'"
      + " as resource_type,overbook_limit, center_id "
      + " from test_equipment_master where status= 'A'"
      + " and schedule = true #CENTER_FILTER# "
      + " order by resource_name ";
  
  /** The Constant SRID_MASTER_QUERY. */
  private static final String SRID_MASTER_QUERY = 
      "select serv_res_id::text as resource_id , "
      + " serv_resource_name as resource_name, 'SRID'"
      + " as resource_type,overbook_limit, center_id "
      + " from service_resource_master where status= 'A'"
      + " and schedule = true #CENTER_FILTER# "
      + " order by resource_name ";

  /** The additional res master map. */
  static Map<String, String> additionalResMasterMap = new HashMap<String, String>();

  static {
    additionalResMasterMap.put("EQID", EQID_MASTER_QUERY);
    additionalResMasterMap.put("SRID", SRID_MASTER_QUERY);
    additionalResMasterMap.put("LABTECH", LABTECH_MASTER_QUERY);
    additionalResMasterMap.put("DOC", DOC_MASTER_QUERY);
  }

  /**
   * Gets the generic resource name.
   *
   * @param id
   *          the id
   * @return the generic resource name
   */
  public String getGenericResourceName(String id) {
    return DatabaseHelper.getString(GENERIC_RESOURCE_NAME, Integer.parseInt(id));
  }

  /**
   * Gets the additional resource master query by type.
   *
   * @param resType
   *          the res type
   * @return the additional resource master query by type
   */
  public String getAdditionalResourceMasterQueryByType(String resType) {
    if (additionalResMasterMap.get(resType) == null) {
      return GENERIC_RESOURCE_MASTER_QUERY;
    } else {
      return additionalResMasterMap.get(resType);
    }
  }
}
