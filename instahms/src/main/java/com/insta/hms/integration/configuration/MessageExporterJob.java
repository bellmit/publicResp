package com.insta.hms.integration.configuration;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultEscaping;
import ca.uhn.hl7v2.parser.EncodingCharacters;
import ca.uhn.hl7v2.parser.PipeParser;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.utils.JsonUtility;
import com.insta.hms.jobs.GenericJob;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class MessageExporterJob extends GenericJob {

  private static Logger logger = LoggerFactory.getLogger(MessageExporterJob.class);

  @LazyAutowired
  private InterfaceConfigService interfaceConfigService;

  @LazyAutowired
  private ExportMessageQueueRepository exportMsgQueueRepository;

  private static final String INTERFACE_ID = "interface_id";

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    String schema = jobDataMap.getString("schema");
    String userLocale = jobDataMap.get("userLocale").toString();
    RequestContext.setConnectionDetails(new String[] {null, null, schema, "_system",
        jobDataMap.get("centerId").toString(), null, null, userLocale});

    int interfaceId = jobDataMap.getInt(INTERFACE_ID);
    BasicDynaBean messageQueueBean = exportMsgQueueRepository.getMessageToSend(interfaceId);
    BasicDynaBean interfaceBean = interfaceConfigService.getInterfaceDetails(interfaceId);

    boolean conStopped = InterfaceConfigRepository.CON_STATUS_STOPPED
        .equals(interfaceBean.get(InterfaceConfigRepository.CONNECTION_STATUS));

    interfaceConfigService.setConnectionTypeStatus(interfaceBean,
        InterfaceConfigRepository.CON_STATUS_STARTED);

    boolean rescheduledJob = false;
    String msgStatus = ExportMessageQueueRepository.STATUS_FAILED;

    PipeParser pipeParser = PipeParser.getInstanceWithNoValidation();

    Map<String, Object> ackMap = new HashMap<>();
    while (null != messageQueueBean && null != interfaceBean && !rescheduledJob && !conStopped) {
      Map<String, Object> messageDataMap = JsonUtility
          .toObjectMap((String) messageQueueBean.get(ExportMessageQueueRepository.JOB_DATA));
      String msgCtrlId =
          Long.toString((long) messageQueueBean.get(ExportMessageQueueRepository.MSG_ID));
      try {
        String msgString = (String) messageDataMap.get("hl7_message");
        ackMap = new HashMap<>();
        boolean success = Boolean.FALSE;
        if (!StringUtil.isNullOrEmpty((String) interfaceBean.get("do_escaping"))
            && "N".equalsIgnoreCase((String) interfaceBean.get("do_escaping"))) {
          DefaultEscaping escaping = new DefaultEscaping();
          String unparsedMsg = msgString.substring(0, 9).concat(
              escaping.unescape(msgString.substring(9), new EncodingCharacters('|', null)));
          success =
              interfaceConfigService.sendMessages(interfaceBean, unparsedMsg, ackMap, msgCtrlId);
          msgStatus = success ? ExportMessageQueueRepository.STATUS_SENT
              : ExportMessageQueueRepository.STATUS_FAILED;
        } else {
          Message msg = pipeParser.parse(msgString);
          success = interfaceConfigService.sendMessages(interfaceBean, msg, ackMap, msgCtrlId);
          msgStatus = success ? ExportMessageQueueRepository.STATUS_SENT
              : ExportMessageQueueRepository.STATUS_FAILED;
        }


        if (!success && checkForRetry(interfaceBean, messageQueueBean)) {
          msgStatus = ExportMessageQueueRepository.STATUS_QUEUED;
          messageQueueBean.set(ExportMessageQueueRepository.COUNT,
              ((int) messageQueueBean.get(ExportMessageQueueRepository.COUNT)) + 1);

          Calendar cal = Calendar.getInstance();
          cal.setTime(new Date());
          cal.add(Calendar.MINUTE,
              (int) interfaceBean.get(InterfaceConfigRepository.RETRY_INTERVAL_IN_MINUTES));

          // Re-schedule a job.
          interfaceConfigService.scheduleSendingMsg(schema, (int) jobDataMap.get("centerId"),
              interfaceId, true, cal.getTime());

          rescheduledJob = true;
          logger.error("Re-scheduled message : {}", msgCtrlId);
        }
      } catch (HL7Exception exception) {
        msgStatus = ExportMessageQueueRepository.STATUS_FAILED;
        logger.error("{} message sending failed with exception {}", msgCtrlId,
            exception.getMessage());
      } finally {
        updateMessageQueue(messageQueueBean, ackMap, msgStatus);

        messageQueueBean = exportMsgQueueRepository.getMessageToSend(interfaceId);
        interfaceBean = interfaceConfigService.getInterfaceDetails(interfaceId);

        conStopped = InterfaceConfigRepository.CON_STATUS_STOPPED
            .equals(interfaceBean.get(InterfaceConfigRepository.CONNECTION_STATUS));
      }
    }
    interfaceConfigService.setConnectionTypeStatus(interfaceBean,
        rescheduledJob ? InterfaceConfigRepository.CON_STATUS_RESCHEDULED
            : InterfaceConfigRepository.CON_STATUS_WAITING);
  }

  private int updateMessageQueue(BasicDynaBean messageQueueBean, Map<String, Object> ackMap,
      String msgStatus) {
    messageQueueBean.set(ExportMessageQueueRepository.ACKNOWLEDGE_MSG,
        String.valueOf(ackMap.get(ExportMessageQueueRepository.ACKNOWLEDGE_MSG)));
    messageQueueBean.set(ExportMessageQueueRepository.ACKNOWLEDGE_STATUS,
        String.valueOf(ackMap.get(ExportMessageQueueRepository.ACKNOWLEDGE_STATUS)));
    messageQueueBean.set(ExportMessageQueueRepository.STATUS, msgStatus);
    messageQueueBean.set(ExportMessageQueueRepository.MODIFIED_AT, DateUtil.getCurrentTimestamp());
    Map<String, Object> updateKeyMap = new HashMap<>();
    // if message sending is failed, updating the status as failed for all the messages of that
    // event which are yet to be sent
    if (ExportMessageQueueRepository.STATUS_FAILED.equals(msgStatus)
        && null != messageQueueBean.get(ExportMessageQueueRepository.EVENT_PROCESSING_ID)) {
      updateKeyMap.put(ExportMessageQueueRepository.EVENT_PROCESSING_ID,
          messageQueueBean.get(ExportMessageQueueRepository.EVENT_PROCESSING_ID));
      updateKeyMap.put(ExportMessageQueueRepository.INTERFACE_ID,
          messageQueueBean.get(ExportMessageQueueRepository.INTERFACE_ID));
      updateKeyMap.put(ExportMessageQueueRepository.STATUS,
          ExportMessageQueueRepository.STATUS_QUEUED);
    } else {
      updateKeyMap.put(ExportMessageQueueRepository.MSG_ID,
          messageQueueBean.get(ExportMessageQueueRepository.MSG_ID));
    }
    return exportMsgQueueRepository.update(messageQueueBean, updateKeyMap);
  }

  private boolean checkForRetry(BasicDynaBean interfaceBean, BasicDynaBean messageDetailBean) {
    if (0 == ((int) interfaceBean.get(InterfaceConfigRepository.RETRY_FOR_DAYS))) {
      return "A".equals(interfaceBean.get(InterfaceConfigRepository.STATUS))
          && (int) messageDetailBean.get(ExportMessageQueueRepository.COUNT) < (int) interfaceBean
              .get(InterfaceConfigRepository.RETRY_MAX_COUNT);
    } else {
      Calendar cal = Calendar.getInstance();
      cal.setTime((Date) messageDetailBean.get(ExportMessageQueueRepository.CREATED_AT));
      cal.add(Calendar.DATE, (int) interfaceBean.get(InterfaceConfigRepository.RETRY_FOR_DAYS));
      Date maxDate = cal.getTime();
      return "A".equals(interfaceBean.get(InterfaceConfigRepository.STATUS))
          && (new Date()).before(maxDate);
    }
  }
}
