package com.insta.hms.integration.hl7.v2;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.Version;
import ca.uhn.hl7v2.model.AbstractMessage;
import ca.uhn.hl7v2.model.AbstractSuperMessage;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.ReflectionUtil;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;
import com.insta.hms.integration.configuration.InterfaceConfigService;
import com.insta.hms.mdm.codesets.CodeSetsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class Hl7Service {

  static Logger log = LoggerFactory.getLogger(Hl7Service.class);

  @LazyAutowired
  private Hl7Repository repository;

  @LazyAutowired
  private InterfaceConfigService hl7InterfaceService;

  @LazyAutowired
  private PatientRegistrationRepository patientRegistrationRepository;

  @LazyAutowired
  private CodeSetsService codeSetsService;

  @LazyAutowired
  private Hl7Validator validator;

  @LazyAutowired
  private BeanFactory beanfactory;

  protected Map<String, Object> availableMessages = new HashMap<>();

  private Map<String, InstaHl7Segment> availableSegments = new HashMap<>();

  /**
   * Sets available messages in hapi.
   */
  @Autowired
  public void setAvailableMessages() {
    List<Version> versions = Version.availableVersions();
    for (Version version : versions) {
      Reflections reflections =
          new Reflections("ca.uhn.hl7v2.model." + version.getPackageVersion() + ".message");
      Set<Class<? extends AbstractMessage>> abstractMessagesSet =
          reflections.getSubTypesOf(AbstractMessage.class);
      Set<Class<? extends AbstractSuperMessage>> abstractSuperMessagesSet =
          reflections.getSubTypesOf(AbstractSuperMessage.class);
      Map<String, Class<?>> msgTypeClasses = new HashMap<>();
      for (Class<?> cl : abstractMessagesSet) {
        if (!cl.getSimpleName().startsWith("ADT")) {
          msgTypeClasses.put(cl.getSimpleName(), cl);
        }
      }
      for (Class<?> cl : abstractSuperMessagesSet) {
        msgTypeClasses.put(cl.getSimpleName(), cl);
      }
      availableMessages.put(version.getVersion(), msgTypeClasses);
    }
  }

  /**
   * Instantiates and returns the message class.
   * 
   * @param <T> class type
   * @param msgType the message type
   * @param version the version
   * @return class type
   * @throws HL7Exception the exception
   */
  @SuppressWarnings("unchecked")
  private <T> T instantiateMessageType(String msgType, String version) throws HL7Exception {
    Map<String, Class<?>> msgTypeClassesMap =
        (Map<String, Class<?>>) availableMessages.get(version);
    String returnMsgType = msgType;
    if (msgType.startsWith("ADT")) {
      returnMsgType = "ADT_AXX";
    } else if (msgType.startsWith("PPR")) {
      returnMsgType = "PPR_PC1";
    } else if (msgType.startsWith("MDM")) {
      returnMsgType = "MDM_T02";
    }
    return ReflectionUtil.instantiate((Class<T>) msgTypeClassesMap.get(returnMsgType));
  }

  /**
   * Instantiates and returns the message.
   * 
   * @param messageType the message type
   * @param messageVersion the version of message
   * @return the message
   */
  public Message getNewMessage(String messageType, String messageVersion) {
    Message message = null;
    try {
      AbstractMessage abstractMessage = instantiateMessageType(messageType, messageVersion);
      abstractMessage.initQuickstart(messageType.split("_")[0], messageType.split("_")[1], "P");
      message = abstractMessage.getMessage();
      message.getParser().getParserConfiguration().setValidating(false);
    } catch (HL7Exception | IOException exception) {
      log.error("Failed to instantiate message {}", exception);
    }
    return message;
  }

  public List<BasicDynaBean> getSegmentsListForMessage(int interfaceId, String messageType,
      String messageVersion) {
    return hl7InterfaceService.getSegmentsListForMessage(interfaceId, messageType, messageVersion);
  }

  @Autowired
  private void setSegments() {
    Reflections ref = new Reflections("com.insta.hms.integration.hl7.v2");
    for (Class<?> cl : ref.getSubTypesOf(InstaHl7Segment.class)) {
      availableSegments.put(cl.getSimpleName(), (InstaHl7Segment) beanfactory.getBean(cl));
    }
  }

  private Map<String, InstaHl7Segment> getSegments() {
    return this.availableSegments;
  }

  public InstaHl7Segment getSegmentObject(String segmentName) {
    return getSegments().get(segmentName.concat("Segment"));
  }

  /**
   * Verify Basic Required Data For Creating Message.
   * 
   * @param dataMap the map
   */
  public void verifyBasicRequiredDataForCreatingMessage(Map<String, Object> dataMap) {
    BasicDynaBean bean = null;
    if (!StringUtils.isEmpty(dataMap.get("visit_id"))
        && StringUtils.isEmpty(dataMap.get("mr_no"))) {
      bean = patientRegistrationRepository.findByKey("patient_id", dataMap.get("visit_id"));
      String mrNo = (null != bean) ? (String) bean.get("mr_no") : null;
      dataMap.put("mr_no", mrNo);
    }
    // gets the latest active visit id if visit_id is null
    if (StringUtils.isEmpty(dataMap.get("visit_id"))
        && !StringUtils.isEmpty(dataMap.get("mr_no"))) {
      bean = patientRegistrationRepository.getLatestActiveVisit((String) dataMap.get("mr_no"));
      String visitId = (null != bean) ? (String) bean.get("patient_id") : null;
      dataMap.put("visit_id", visitId);
    }
  }

  /**
   * Get Current Time.
   * 
   * @return map
   */
  public Map<String, Object> getCurrentTimeMap() {
    Date date = new Date();
    Map<String, Object> map = new HashMap<>();
    map.put("date_time", new SimpleDateFormat("yyyyMMddHHmmss").format(date));
    map.put("date_time_with_time_zone", new SimpleDateFormat("yyyyMMddHHmmssZZZZ").format(date));
    return map;
  }

  public List<BasicDynaBean> getSegmentTemplate(String segmentName, String version,
      int interfaceId) {
    return repository.getSegmentTemplate(segmentName, version, interfaceId);
  }

  /**
   * Get code sets.
   * 
   * @return the map
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getCodeSets() {
    String schema = RequestContext.getSchema();
    Map<String, Object> codeSetMap =
        (Map<String, Object>) CodeSetsService.CODE_SET_CACHED_MAP.get(schema);
    if (codeSetMap == null) {
      codeSetMap = codeSetsService.getCodeSets();
      CodeSetsService.CODE_SET_CACHED_MAP.put(schema, codeSetMap);
    }
    return codeSetMap;
  }
  
  public boolean validateMsgApplicability(String applicableVisit, String visitId) {
    return validator.validateMsgApplicability(applicableVisit, visitId);
  }
}
