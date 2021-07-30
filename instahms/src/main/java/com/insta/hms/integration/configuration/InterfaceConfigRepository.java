package com.insta.hms.integration.configuration;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class InterfaceConfigRepository extends MasterRepository<Integer> {

  public static final String INTERFACE_ID = "interface_id";
  
  public static final String INTERFACE_NAME = "interface_name";

  public static final String STATUS = "status";

  public static final String RETRY_MAX_COUNT = "retry_max_count";

  public static final String RETRY_FOR_DAYS = "retry_for_days";

  public static final String RETRY_INTERVAL_IN_MINUTES = "retry_interval_in_minutes";

  public static final String DESTINATION_HOST = "destination_host";

  public static final String DESTINATION_PORT = "destination_port";

  public static final String URI = "uri";

  public static final String REQ_PARAMETERS = "req_parameters";

  public static final String TIMEOUT_IN_SEC = "timeout_in_sec";

  public static final String CONNECTION_TYPE = "connection_type";

  public static final String CON_TYPE_HTTPS = "https";

  public static final String CON_TYPE_SOCKET = "socket";

  public static final String CONNECTION_STATUS = "connection_status";

  public static final String CON_STATUS_WAITING = "WAITING";

  public static final String CON_STATUS_STARTED = "STARTED";

  public static final String CON_STATUS_RESCHEDULED = "RESCHEDULED";

  public static final String CON_STATUS_STOPPED = "STOPPED";

  public static final String MODIFIED_AT = "modified_at";

  public static final String MODIFIED_BY = "modified_by";

  public InterfaceConfigRepository() {
    super("interface_config_master", INTERFACE_ID);
  }
  
  private static final String INTERFACE_CONFIG_FIELDS = "SELECT icm.interface_id, "
      + " icm.interface_type, icm.interface_name, icm.connection_type,"
      + " icm.status, icm.destination_host, icm.destination_port, icm.timeout_in_sec, "
      + " icm.retry_max_count, icm.retry_for_days, icm.retry_interval_in_minutes,"
      + " icm.uri, icm.req_parameters,"
      + " icm.code_system_id, cs.label AS code_system_name, icm.sending_facility,"
      + " icm.sending_application, icm.receving_facility, icm.receving_application";
          
  private static final String INTERFACE_CONFIG_TABLES = " FROM interface_config_master icm"
      + " LEFT JOIN code_systems cs ON (icm.code_system_id = cs.id)";
  
  /** The Constant COUNT. */
  private static final String COUNT = " SELECT count(distinct icm.interface_id) ";
  
  private static final String GET_INTERFACE_QUERY = "SELECT icm.interface_id,"
      + " icm.code_system_id, cs.label AS code_system_name, icm.sending_facility,"
      + " icm.sending_application, icm.receving_facility, icm.receving_application"
      + " FROM interface_config_master icm"
      + " LEFT JOIN code_systems cs ON (icm.code_system_id = cs.id) WHERE icm.interface_id = ?";

  public BasicDynaBean getInterfaceDetails(int interfaceId) {
    return DatabaseHelper.queryToDynaBean(GET_INTERFACE_QUERY, new Object[] {interfaceId});
  }
 
  /**
   * Get the interface list with details.
   *
   * @param paramMap the param map
   * @return the note types details
   */
  public PagedList getInterfaceDetails( Map<String, String[]> paramMap) {
    Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(paramMap);
    int pageNum = ((Integer) listingParams.get(LISTING.PAGENUM)).intValue();
    int pageSize = ((Integer) listingParams.get(LISTING.PAGESIZE)).intValue();
    
    SearchQueryAssembler qb = new SearchQueryAssembler(INTERFACE_CONFIG_FIELDS, COUNT, 
        INTERFACE_CONFIG_TABLES, null, "interface_name", false,pageSize,pageNum );
    qb.addFilterFromParamMap(paramMap);
    qb.build();
    return qb.getMappedPagedList();
  }

  private static final String GET_SEGMENTS_LIST_FOR_MESSAGE_QUERY = "SELECT msdh.message_type,"
      + " msdh.segment, msdh.repeat_segment FROM message_mapping_details_hl7 msdh"
      + " WHERE msdh.status = 'A' AND msdh.message_type = ?"
      + " AND msdh.version = ? AND msdh.interface_id = ? ORDER BY seg_order ASC";

  public List<BasicDynaBean> getSegmentsListForMessage(int interfaceId, String messageType,
      String messageVersion) {
    return DatabaseHelper.queryToDynaList(GET_SEGMENTS_LIST_FOR_MESSAGE_QUERY, messageType,
        messageVersion, interfaceId);
  }

  private static final String GET_MESSAGE_TYPES_BY_EVENT_AND_CENTER_ID = "SELECT"
      + " iem.event_mapping_id, iem.event_id, iem.visit_type AS applicable_visit,"
      + " iem.message_type, iem.priority, iem.center_id, iem.interface_id,"
      + " iem.version AS message_version, he.event_name"
      + " FROM interface_event_mapping iem"
      + " JOIN interface_config_master icm ON (iem.interface_id = icm.interface_id"
      + " AND icm.status = 'A')"
      + " LEFT JOIN hie_events he ON (iem.event_id = he.event_id)"
      + " WHERE iem.status = 'A' AND iem.event_id = ? AND iem.center_id = ?"
      + " ORDER BY iem.priority ASC";

  public List<BasicDynaBean> getMessagesListByEventAndCenterId(int eventId, int centerId) {
    return DatabaseHelper.queryToDynaList(GET_MESSAGE_TYPES_BY_EVENT_AND_CENTER_ID,
        new Object[] {eventId, centerId});
  }

  private static final String SENDER_RECEIVER_DETAILS_QUERY = "SELECT icm.sending_facility,"
      + " icm.sending_application, icm.receving_facility, icm.receving_application,"
      + " icm.code_system_id, cs.label AS code_system_name FROM interface_config_master icm"
      + " LEFT JOIN code_systems cs ON (cs.id = icm.code_system_id AND cs.status = 'A')"
      + " WHERE interface_id = ?";

  public BasicDynaBean getSenderReceiverDetails(int interfaceId) {
    return DatabaseHelper.queryToDynaBean(SENDER_RECEIVER_DETAILS_QUERY,
        new Object[] {interfaceId});
  }
  
  private static final String GET_INTERFACE_DETAILS_BY_EVENT_CENTER_AND_ID = "SELECT"
      + " icm.interface_name, icm.interface_type,"
      + " iem.event_mapping_id, iem.event_id, iem.visit_type AS applicable_visit,"
      + " iem.message_type, iem.priority, iem.center_id, iem.interface_id,"
      + " iem.version AS message_version, he.event_name"
      + " FROM interface_event_mapping iem"
      + " LEFT JOIN interface_config_master icm ON (iem.interface_id = icm.interface_id "
      + " AND icm.status = 'A')"
      + " LEFT JOIN hie_events he ON (iem.event_id = he.event_id)"
      + " WHERE iem.status = 'A' AND iem.event_id = ? AND iem.center_id = ? "
      + " AND icm.interface_id = ? "; 

  public List<BasicDynaBean> getInterfaceDetailsByEventCenterAndId(int eventId, int centerId, 
      int interfaceId) {
    return DatabaseHelper.queryToDynaList(GET_INTERFACE_DETAILS_BY_EVENT_CENTER_AND_ID,
      new Object[] {eventId, centerId,interfaceId});
  }
}
