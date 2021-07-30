package com.insta.hms.messaging.hl7.providers;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v23.message.ADT_A04;
import ca.uhn.hl7v2.model.v23.segment.AL1;
import ca.uhn.hl7v2.model.v23.segment.DG1;
import ca.uhn.hl7v2.model.v23.segment.EVN;
import ca.uhn.hl7v2.model.v23.segment.IN1;
import ca.uhn.hl7v2.model.v23.segment.IN2;
import ca.uhn.hl7v2.model.v23.segment.MSH;
import ca.uhn.hl7v2.model.v23.segment.PID;
import ca.uhn.hl7v2.model.v23.segment.PV1;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.integration.hl7.message.v23.InstaAL1;
import com.insta.hms.integration.hl7.message.v23.InstaDG1;
import com.insta.hms.integration.hl7.message.v23.InstaIN1;
import com.insta.hms.integration.hl7.message.v23.InstaIN2;
import com.insta.hms.integration.hl7.message.v23.InstaMSH;
import com.insta.hms.integration.hl7.message.v23.InstaPID;
import com.insta.hms.integration.hl7.message.v23.InstaPV1;
import com.insta.hms.messaging.MessageContext;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class HL7ADTProvider.
 * 
 * @author yashwant
 */
public class HL7ADT04Provider extends HL7ADTProvider {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(HL7ADT04Provider.class);
  /** The Constant THIS_PROVIER. */
  private static final String THIS_PROVIER = "HL7_ADT_04_PROVIDER";

  /**
   * Instantiates a new HL 7 ADT provider.
   */
  public HL7ADT04Provider() {
    super(THIS_PROVIER);
  }

  /** The tokens. */
  private String[] tokens = new String[] {};

  /*
   * (non-Javadoc)
   *
   * @see com.insta.hms.messaging.providers.QueryDataProvider#getTokens()
   */
  @Override
  public List<String> getTokens() throws SQLException {
    List<String> tokenList = new ArrayList<>(Arrays.asList(tokens));
    Collections.sort(tokenList);
    return tokenList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.messaging.providers.QueryDataProvider#getMessageDataList(com.insta.hms.messaging.
   * MessageContext)
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public List<Map> getMessageDataList(MessageContext ctx) throws SQLException, ParseException {

    Map eventData = ctx.getEventData();
    Map configParams = ctx.getConfigParams();

    String patientId = (String) eventData.get("patient_id");
    String mrNo = (String) eventData.get("mr_no");

    logger.info("####################################");
    logger.info("HL7_COMMUNICATION_INIT : Building ADT04 Message");
    logger.info("PATIENT_ID : " + patientId + ", MR_NO : " + mrNo);
    List<Map> list = new ArrayList<>();
    try {
      list.add(getAdtMessage(configParams, patientId, mrNo));
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
    return list;
  }

  /**
   * Gets the adt message.
   *
   * @param configParams
   *          the config params
   * @param patientId
   *          the patient id
   * @param mrNo
   *          the mr no
   * @return the adt message
   * @throws HL7Exception
   *           the HL 7 exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @SuppressWarnings("unchecked")
  public Map<String, String> getAdtMessage(Map<String, String> configParams, String patientId,
      String mrNo) throws HL7Exception, IOException {

    ADT_A04 adt = new ADT_A04();
    adt.initQuickstart("ADT", "A04", "P");
    String currentTime = getCurrentTimeStamp();
    configParams.put("event_date_time", currentTime);
    createMSH(adt, configParams);
    createEvn(adt, configParams);
    BasicDynaBean patientDynaBean = getPatientDynaList(patientId, mrNo);

    if (patientDynaBean != null) {
      Map<String, Object> patientMap = patientDynaBean.getMap();
      createPID(adt, patientMap);
      createPV1(adt, patientMap, configParams);
      createIN(adt, patientMap);
      createDG1(adt, (String) patientDynaBean.get("patient_id"));
      createAL1(adt, (String) patientDynaBean.get("patient_id"));
    }
    String encodedMessage = getParsedMessage(adt);
    Map<String, String> item = new HashMap<>();
    item.put("message_body", encodedMessage);
    item.put("_hl7_message", encodedMessage);
    return item;
  }

  /**
   * Creates the evn.
   *
   * @param adt
   *          the adt
   * @param data
   *          the data
   * @throws DataTypeException
   *           the data type exception
   */
  private void createEvn(ADT_A04 adt, Map<String, String> data) throws DataTypeException {
    EVN evn = adt.getEVN();
    evn.getEventTypeCode().setValue("A04");
    evn.getRecordedDateTime().getTimeOfAnEvent().setValue((String) data.get("event_date_time"));
  }

  /**
   * Creates the MSH.
   *
   * @param adt
   *          the adt
   * @param configParams
   *          the config params
   * @throws DataTypeException
   *           the data type exception
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void createMSH(ADT_A04 adt, Map configParams) throws DataTypeException {
    Map<String, String> dataMap = new HashMap<>();
    dataMap.put("sending_application_namespace_id", "ADT04");
    dataMap.put("sending_application_universal_id", "ADT");
    dataMap.putAll(configParams);
    MSH msh = adt.getMSH();
    InstaMSH instaMsh = (InstaMSH) ApplicationContextProvider.getApplicationContext()
        .getBean("instaMSH");
    instaMsh.createMSH(msh, dataMap);
  }

  /**
   * Creates the PID.
   *
   * @param adt
   *          the adt
   * @param patientMap
   *          the patient map
   * @throws HL7Exception
   *           the HL 7 exception
   */
  private void createPID(ADT_A04 adt, Map<String, Object> patientMap) throws HL7Exception {
    PID pid = adt.getPID();
    InstaPID instaPid = (InstaPID) ApplicationContextProvider.getApplicationContext()
        .getBean("instaPID");
    instaPid.createPID(pid, patientMap);
  }

  /**
   * Creates the PV 1.
   *
   * @param adt
   *          the adt
   * @param patientMap
   *          the patient map
   * @param configParams
   *          the config params
   * @throws DataTypeException
   *           the data type exception
   */
  private void createPV1(ADT_A04 adt, Map<String, Object> patientMap,
      Map<String, String> configParams) throws DataTypeException {
    PV1 pv1 = adt.getPV1();
    InstaPV1 instaPV1 = (InstaPV1) ApplicationContextProvider.getApplicationContext()
        .getBean("instaPV1");
    instaPV1.createPV1(pv1, patientMap, configParams);

  }

  /**
   * Creates the IN 1.
   *
   * @param adt
   *          the adt
   * @param patientId
   *          the patient id
   * @throws DataTypeException
   *           the data type exception
   */
  private void createIN(ADT_A04 adt, Map<String, Object> patientMap) throws DataTypeException {

    if (patientMap.get("patient_id") == null) {
      return;
    }
    InstaIN1 instaIN1 = (InstaIN1) ApplicationContextProvider.getApplicationContext()
        .getBean("instaIN1");
    InstaIN2 instaIN2 = (InstaIN2) ApplicationContextProvider.getApplicationContext()
        .getBean("instaIN2");
    List<BasicDynaBean> beanList = getVisitInsuranceDetails((String) patientMap.get("patient_id"));
    int count = 0;
    for (BasicDynaBean bean : beanList) {
      IN1 in1 = adt.getINSURANCE(count).getIN1();
      IN2 in2 = adt.getINSURANCE(count).getIN2();
      in1.getSetIDInsurance().setValue(Integer.toString(++count));
      instaIN1.createIN1(in1, bean, patientMap);
      instaIN2.createIN2(in2, bean, patientMap);
    }
  }

  /**
   * Creates the DG 1.
   *
   * @param adt
   *          the adt
   * @param patientId
   *          the patient id
   * @throws DataTypeException
   *           the data type exception
   */
  private void createDG1(ADT_A04 adt, String patientId) throws DataTypeException {
    if (patientId == null) {
      return;
    }
    InstaDG1 instaDG1 = (InstaDG1) ApplicationContextProvider.getApplicationContext()
        .getBean("instaDG1");
    List<BasicDynaBean> beanList = getVisitDiagDetails(patientId);
    int count = 0;
    for (BasicDynaBean bean : beanList) {
      DG1 diagSeg1 = adt.getDG1(count);
      diagSeg1.getDg11_SetIDDiagnosis().setValue(Integer.toString(++count));
      instaDG1.createDG1(diagSeg1, bean);
    }

  }

  /**
   * Creates the AL 1.
   *
   * @param adt
   *          the adt
   * @param patientId
   *          the patient id
   * @throws DataTypeException
   *           the data type exception
   */
  @SuppressWarnings("unchecked")
  private void createAL1(ADT_A04 adt, String patientId) throws DataTypeException {
    if (patientId == null) {
      return;
    }

    InstaAL1 instaAL1 = (InstaAL1) ApplicationContextProvider.getApplicationContext()
        .getBean("instaAL1");
    List<BasicDynaBean> beanList = getVisitAllergyDetails(patientId);
    int count = 0;
    for (BasicDynaBean bean : beanList) {
      AL1 al1 = adt.getAL1(count);
      al1.getAl11_SetIDAL1().setValue(Integer.toString(++count));
      instaAL1.createAL1(al1, bean.getMap());
    }
  }

}
