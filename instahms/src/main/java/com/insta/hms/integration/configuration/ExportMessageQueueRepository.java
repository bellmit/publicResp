package com.insta.hms.integration.configuration;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

@Repository
public class ExportMessageQueueRepository extends GenericRepository {

  public static final String MSG_ID = "msg_id";

  public static final String INTERFACE_ID = "interface_id";

  public static final String STATUS = "status";

  public static final String STATUS_SENT = "SENT";

  public static final String STATUS_FAILED = "FAILED";

  public static final String STATUS_QUEUED = "QUEUED";

  public static final String CREATED_AT = "created_at";

  public static final String MODIFIED_AT = "modified_at";

  public static final String COUNT = "count";

  public static final String JOB_DATA = "job_data";

  public static final String ACKNOWLEDGE_MSG = "acknowledge_msg";

  public static final String ACKNOWLEDGE_STATUS = "acknowledge_status";

  public static final String EVENT_PROCESSING_ID = "event_processing_id";

  public ExportMessageQueueRepository() {
    super("export_message_queue");
  }

  private static final String GET_MESSAGE = "SELECT msg_id, interface_id, status, created_at,"
      + " modified_at, count, job_data, acknowledge_msg, acknowledge_status, event_processing_id"
      + " FROM export_message_queue WHERE status = 'QUEUED' AND interface_id = ?"
      + " ORDER BY created_at ASC LIMIT 1";

  public BasicDynaBean getMessageToSend(int interfaceId) {
    return DatabaseHelper.queryToDynaBean(GET_MESSAGE, new Object[] {interfaceId});
  }
}
