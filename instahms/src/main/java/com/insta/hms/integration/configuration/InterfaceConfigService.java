package com.insta.hms.integration.configuration;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.model.AbstractMessage;
import ca.uhn.hl7v2.model.AbstractPrimitive;
import ca.uhn.hl7v2.model.AbstractSegment;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.ParserConfiguration;
import ca.uhn.hl7v2.parser.PipeParser;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.exception.DuplicateEntityException;
import com.insta.hms.jobs.JobService;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.codec.CharEncoding;
import org.apache.http.HttpHeaders;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

@Service
public class InterfaceConfigService extends MasterService {

  static Logger logger = LoggerFactory.getLogger(InterfaceConfigService.class);

  @LazyAutowired
  private InterfaceConfigRepository repository;
  
  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private JobService jobService;

  private static final String MSG_SENDER_JOB_NAME_FORMAT = "Hl7MsgSenderJob_%s_Interface_%s";

  private static HapiContext context;
  
  private static final String USER_SYSTEM = "_system";

  public InterfaceConfigService(InterfaceConfigRepository repository,
      InterfaceConfigValidator validator) {
    super(repository, validator);
  }

  public List<BasicDynaBean> getSegmentsListForMessage(int interfaceId, String messageType,
      String messageVersion) {
    return repository.getSegmentsListForMessage(interfaceId, messageType, messageVersion);
  }

  public List<BasicDynaBean> getMessagesListByEventAndCenterId(int eventId, int centerId) {
    return repository.getMessagesListByEventAndCenterId(eventId, centerId);
  }

  /**
   * Update connection type status of interface.
   * 
   * @param interfaceBean the bean
   * @param conStatus the updated status
   * @return int
   */
  public int setConnectionTypeStatus(BasicDynaBean interfaceBean, String conStatus) {
    if (!InterfaceConfigRepository.CON_STATUS_STOPPED
        .equals(interfaceBean.get(InterfaceConfigRepository.CONNECTION_STATUS))) {
      interfaceBean.set(InterfaceConfigRepository.CONNECTION_STATUS, conStatus);
      interfaceBean.set(InterfaceConfigRepository.MODIFIED_AT, DateUtil.getCurrentTimestamp());
      interfaceBean.set(InterfaceConfigRepository.MODIFIED_BY, USER_SYSTEM);
      return update(interfaceBean);
    }
    return 0;
  }

  public BasicDynaBean getInterfaceDetails(int interfaceId) {
    return repository.findByKey(InterfaceConfigRepository.INTERFACE_ID, interfaceId);
  }
  
  /**
   * Insert interface details to create / update interface.
   *
   * @param params the params
   * @return the map
   */
  public Map<String, Object> saveInterface(Map<String, Object> params) {
    Map<String, Object> map = null;
    if (params.get(InterfaceConfigRepository.INTERFACE_ID) != null
        && params.get(InterfaceConfigRepository.INTERFACE_ID).equals("")) {
      map = updateInterface(params);
    } else {
      //map = insertInterface(params);
    }
    return map;
  }
  
  /**
   * Gets the interface details.
   *
   * @param paramMap the param map
   * @return the note types details
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public PagedList getInterfacesDetails(Map<String, String[]> paramMap) {
    PagedList interfaces = repository.getInterfaceDetails(paramMap);
    List<Object> interfacelist = new ArrayList<>();
    for (Map<String, Object> map : (List<Map<String, Object>>) interfaces.getDtoList()) {
      Map<String, Object> interfaceMap = new HashMap();
      interfaceMap.putAll(map);
      interfacelist.add(interfaceMap);
    }
    interfaces.setDtoList(interfacelist);
    return interfaces;
  }
  
  /**
   * Update interface.
   *
   * @param params the params
   * @return the map
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Map<String, Object> updateInterface(Map<String, Object> params) {
    ArrayList errors = new ArrayList();
    String strInterfaceId = String.valueOf(params.get(InterfaceConfigRepository.INTERFACE_ID));
    int interfaceId = 0;
    try {
      interfaceId = Integer.parseInt(strInterfaceId);
    } catch (NumberFormatException exception) {
      errors.add("interface_id is not valid");
    }
    BasicDynaBean interfaceConfigBean = repository.getBean();
    ConversionUtils.copyToDynaBean(params, interfaceConfigBean, errors);
    if (errors.isEmpty()) {
      String userName = (String) sessionService.getSessionAttributes().get("userId");
      interfaceConfigBean.set("mod_user", userName);
      interfaceConfigBean.set("mod_time", DateUtil.getCurrentTimestamp());
      String interfaceName =
          (String) interfaceConfigBean.get(InterfaceConfigRepository.INTERFACE_NAME);
      DynaBean exists =
          repository.findByKey(InterfaceConfigRepository.INTERFACE_NAME, interfaceName);
      if (exists != null
          && interfaceId != (Integer) exists.get(InterfaceConfigRepository.INTERFACE_ID)) {
        throw new DuplicateEntityException(
            new String[] {"Interface", InterfaceConfigRepository.INTERFACE_NAME});
      } else {
        Map keys = new HashMap();
        keys.put(InterfaceConfigRepository.INTERFACE_ID, interfaceId);
        repository.update(interfaceConfigBean, keys);
      }
    } else {
      throw new ConversionException(errors);
    }
    return params;
  }

  public BasicDynaBean getSenderReceiverDetails(int interfaceId) {
    return repository.getSenderReceiverDetails(interfaceId);
  }

  /**
   * Initiates or reschedules the message sending job.
   * 
   * @param schema the schema
   * @param centerId the center id
   * @param interfaceId the interface id
   * @param reschedule identifier to reschedule or not
   * @param rescheduleDate reschedule date time
   */
  public void scheduleSendingMsg(String schema, int centerId, int interfaceId, boolean reschedule,
      Date rescheduleDate) {
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("interface_id", interfaceId);
    jobData.put("schema", schema);
    jobData.put("userLocale", RequestContext.getLocale());
    jobData.put("centerId", centerId);
    String jobName = String.format(MSG_SENDER_JOB_NAME_FORMAT, schema, interfaceId);
    if (reschedule) {
      jobService.rescheduleJobAt(buildJob(jobName, MessageExporterJob.class, jobData),
          rescheduleDate);
    } else {
      jobService.scheduleImmediateOnlyIfNotScheduled(
          buildJob(jobName, MessageExporterJob.class, jobData));
    }
  }

  /**
   * Sends HL7 message.
   * 
   * @param interfaceBean the interface bean
   * @param msg the HL7 message
   * @return boolean value
   */
  public boolean sendMessages(BasicDynaBean interfaceBean, Object msg, Map<String, Object> ackMap,
      String msgCtrlId) {
    if (InterfaceConfigRepository.CON_TYPE_SOCKET.equals(
        interfaceBean.get(InterfaceConfigRepository.CONNECTION_TYPE)) && msg instanceof Message) {
      return sendMessageOverSocket(interfaceBean, (Message) msg, ackMap, msgCtrlId);
    } else if (InterfaceConfigRepository.CON_TYPE_HTTPS
        .equals(interfaceBean.get(InterfaceConfigRepository.CONNECTION_TYPE))) {
      return sendMessageOverHttps(interfaceBean, msg, ackMap, msgCtrlId);
    } else {
      logger.error("Message cannot send via connection type {}",
          interfaceBean.get("connection_type"));
      return false;
    }
  }

  /**
   * Get Hapi context.
   * 
   * @return HapiContext
   */
  public static HapiContext getHapiContext() {
    if (context != null) {
      return context;
    }
    context = new DefaultHapiContext();
    ParserConfiguration parserConfiguration = context.getParserConfiguration();
    parserConfiguration.setValidating(false);
    return context;
  }

  private boolean sendMessageOverSocket(BasicDynaBean interfaceBean, Message msg,
      Map<String, Object> ackMap, String msgCtrlId) {
    boolean msgSentStatus = true;

    Connection connection = null;
    try {
      logger.info("SENDING {} MESSAGE TO HOST : {} Port : {}", msgCtrlId,
          interfaceBean.get(InterfaceConfigRepository.DESTINATION_HOST),
          interfaceBean.get(InterfaceConfigRepository.DESTINATION_PORT));
      connection = getHapiContext().newLazyClient(
          (String) interfaceBean.get(InterfaceConfigRepository.DESTINATION_HOST),
          (int) interfaceBean.get(InterfaceConfigRepository.DESTINATION_PORT), false);
      Initiator initiator = connection.getInitiator();
      initiator.setTimeout((int) interfaceBean.get(InterfaceConfigRepository.TIMEOUT_IN_SEC),
          TimeUnit.SECONDS);
      Message ackMessage = initiator.sendAndReceive(msg);
      logger.info("ACKNOWLEDGEMENT OF {} MESSAGE : {}", msgCtrlId, ackMessage.getMessage());
      ackMap.put(ExportMessageQueueRepository.ACKNOWLEDGE_MSG, ackMessage);
      logAckReceived(ackMessage, ackMap);
    } catch (SocketException exception) {
      msgSentStatus = false;
      logger.error("Socket exception {} : {}", msgCtrlId, exception.getMessage());
    } catch (HL7Exception excption) {
      msgSentStatus = false;
      logger.error("HL7Exception sending fail {} : {}", msgCtrlId, excption.getMessage());
    } catch (Exception excption) {
      msgSentStatus = false;
      logger.error("Exception in sending message {} : {}", msgCtrlId, excption.getMessage());
    } finally {
      if (connection != null && connection.isOpen()) {
        connection.close();
        connection = null;
      }
    }
    return msgSentStatus;
  }

  private boolean sendMessageOverHttps(BasicDynaBean interfaceBean, Object msg,
      Map<String, Object> ackMap, String msgCtrlId) {
    boolean msgSentStatus = true;
    String response = null;
    String msgString = null;
    if (msg instanceof String) {
      msgString = (String) msg;
    } else if (msg instanceof Message) {
      msgString = ((Message) msg).getMessage().toString();
    } else {
      return false;
    }
    try {
      HttpsURLConnection httpPost = getHttpsConnection(interfaceBean);
      DataOutputStream dos = new DataOutputStream(httpPost.getOutputStream());
      try {
        dos.writeBytes(msgString);
      } finally {
        dos.flush();
        dos.close();
      }
      response = getResponse(httpPost);
      Parser parser = PipeParser.getInstanceWithNoValidation();
      Message ackMsg = parser.parse(response);
      ackMap.put(ExportMessageQueueRepository.ACKNOWLEDGE_MSG, ackMsg.getMessage());
      logAckReceived(ackMsg.getMessage(), ackMap);
    } catch (HL7Exception exception) {
      ackMap.put("ack_message", response);
      logger.info("Exception in parsing received message {} : {}", msgCtrlId, exception);
      msgSentStatus = false;
    } catch (Exception exception) {
      ackMap.put("ack_message", response);
      msgSentStatus = false;
      logger.error("Failed to send message {} : {}", msgCtrlId, exception);
    }
    return msgSentStatus;
  }

  private void logAckReceived(Message ackMessage, Map<String, Object> ackMap) throws HL7Exception {
    if (ackMessage instanceof AbstractMessage) {
      AbstractMessage ackResponseMessage = (AbstractMessage) ackMessage;
      String ackCode =
          ((AbstractPrimitive) ((AbstractSegment) ackResponseMessage.get("MSA")).getField(1, 0))
              .getValue();
      String ackStatus = null;
      if ("AA".equals(ackCode)) {
        ackStatus = "Accepted";
      } else if ("AE".equals(ackCode)) {
        ackStatus = "Error";
      } else if ("AR".equals(ackCode)) {
        ackStatus = "Reject";
      } else if ("CE".equals(ackCode)) {
        ackStatus = "Commit Error";
      } else if ("CR".equals(ackCode)) {
        ackStatus = "Commit Reject";
      } else {
        ackStatus = ackCode;
      }
      ackMap.put(ExportMessageQueueRepository.ACKNOWLEDGE_STATUS, ackStatus);
    }
  }

  /**
   * Get HTTPS connection.
   * 
   * @param interfaceBean the interface details
   * @return connection
   * @throws IOException the exception
   * @throws NoSuchAlgorithmException the exception
   * @throws KeyManagementException the exception
   */
  private HttpsURLConnection getHttpsConnection(BasicDynaBean interfaceBean)
      throws IOException, NoSuchAlgorithmException, KeyManagementException {
    try {
      StringBuilder urlString = new StringBuilder(String.format("https://%s/%s",
          interfaceBean.get(InterfaceConfigRepository.DESTINATION_HOST),
          interfaceBean.get(InterfaceConfigRepository.URI)));
      if (null != interfaceBean.get(InterfaceConfigRepository.REQ_PARAMETERS)) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> reqParamMap = new HashMap<>();
        reqParamMap =
            mapper.readValue((String) interfaceBean.get(InterfaceConfigRepository.REQ_PARAMETERS),
                new TypeReference<Map<String, Object>>() {});
        if (reqParamMap != null) {
          Iterator<String> keyIterator = reqParamMap.keySet().iterator();
          urlString.append("?");
          while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            String value = (String) reqParamMap.get(key);
            urlString.append(URLEncoder.encode(key, CharEncoding.UTF_8));
            urlString.append("=").append(URLEncoder.encode(value, CharEncoding.UTF_8));
            if (keyIterator.hasNext()) {
              urlString.append("&");
            }
          }
        }
      }

      HashMap<String, Object> header = new HashMap<String, Object>();
      header.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
      header.put(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);

      SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
      sslContext.init(null, null, new java.security.SecureRandom());

      URL url = new URL(urlString.toString());
      HttpsURLConnection request = (HttpsURLConnection) url.openConnection();

      request.setSSLSocketFactory(sslContext.getSocketFactory());
      request.setRequestMethod("POST");
      request.setDoInput(true);
      request.setDoOutput(true);
      request.setUseCaches(false);
      request.setRequestProperty(HttpHeaders.ACCEPT_CHARSET, CharEncoding.UTF_8);
      request.setConnectTimeout(
          ((int) interfaceBean.get(InterfaceConfigRepository.TIMEOUT_IN_SEC)) * 1000);
      request.setReadTimeout(
          ((int) interfaceBean.get(InterfaceConfigRepository.TIMEOUT_IN_SEC)) * 1000);

      if (header != null && !header.isEmpty()) {
        Iterator<String> keyIterator = header.keySet().iterator();
        while (keyIterator.hasNext()) {
          String key = keyIterator.next();
          request.setRequestProperty(key, header.get(key).toString());
        }
      }
      return request;
    } catch (IOException exception) {
      logger.info("Unable to open connection : {}", exception);
      throw new IOException("Unable to open connection");
    } catch (NoSuchAlgorithmException exception) {
      logger.info("Exception in SSL context : {}", exception);
      throw new NoSuchAlgorithmException("Exception in SSL context");
    } catch (KeyManagementException exception) {
      logger.info("Exception in SSL context in initiating random security: {}", exception);
      throw new KeyManagementException("Exception in SSL context in initiating random security");
    } catch (Exception exception) {
      logger.info("Exception in getting connection {}", exception);
      return null;
    }
  }

  /**
   * Get Response.
   * 
   * @param request the https connection
   * @return string
   * @throws IOException the exception
   * @throws JSONException the exception
   */
  private String getResponse(HttpsURLConnection request) throws IOException, JSONException {
    try {
      int status = request.getResponseCode();
      String line = "";
      String responseLine = "";
      BufferedReader br;
      String response;

      if (status == HttpStatus.OK.value() || status == HttpStatus.CREATED.value()) {
        br = new BufferedReader(new InputStreamReader(request.getInputStream()));
        while ((line = br.readLine()) != null) {
          responseLine += line + "\r\n";
        }
        response = responseLine;
      } else {
        br = new BufferedReader(new InputStreamReader(request.getErrorStream()));
        while ((line = br.readLine()) != null) {
          responseLine += line;
        }
        JSONObject jsonResponse =
            new JSONObject("{ \"response\": \"" + responseLine.trim() + "\"}");
        response = jsonResponse.toString();
      }
      return response;
    } catch (IOException exception) {
      logger.error("Error in getting response code or input stream from request : {}", exception);
      throw new IOException("Error in getting response code or input stream from request");
    } catch (JSONException exception) {
      logger.error("Received a response in non-hl7 format : {}", exception);
      throw new JSONException("Received a response in non-hl7 format");
    }
  }
}
