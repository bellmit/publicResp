package com.insta.hms.messaging.hl7.providers;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v23.message.ADT_A18;
import ca.uhn.hl7v2.model.v23.segment.EVN;
import ca.uhn.hl7v2.model.v23.segment.MRG;
import ca.uhn.hl7v2.model.v23.segment.MSH;
import ca.uhn.hl7v2.model.v23.segment.PID;
import ca.uhn.hl7v2.model.v23.segment.PV1;

import com.insta.hms.common.ApplicationContextProvider;
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
 * The Class HL7ADT18Provider.
 * 
 * @author yashwant
 */
public class HL7ADT18Provider extends HL7ADTProvider {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(HL7ADT18Provider.class);
  /** The Constant THIS_PROVIER. */
  private static final String THIS_PROVIER = "HL7_ADT_08_PROVIDER";

  /**
   * Instantiates a new HL 7 ADT 18 provider.
   */
  public HL7ADT18Provider() {
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
    configParams.putAll(eventData);
    logger.info("####################################");
    logger.debug("Building ADT04 Message ");
    logger.info("MESSAGE_DATA" + configParams);
    List<Map> list = new ArrayList<>();
    try {
      list.add(getAdtMessage(configParams));
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
    logger.info("Featched message details from DB");
    return list;
  }

  /**
   * Gets the adt message.
   *
   * @param adtData
   *          the adt data
   * @return the adt message
   * @throws HL7Exception
   *           the HL 7 exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map<String, String> getAdtMessage(Map adtData) throws HL7Exception, IOException {

    ADT_A18 adt = new ADT_A18();
    adt.initQuickstart("ADT", "A18", "P");
    String currentTime = getCurrentTimeStamp();
    adtData.put("event_date_time", currentTime);
    createMSH(adt, adtData);
    createEvn(adt, adtData);
    String patientId = (String) adtData.get("patient_id");
    String mrNo = (String) adtData.get("mr_no");
    BasicDynaBean patientDynaBean = getPatientDynaList(patientId, mrNo);

    if (patientDynaBean != null) {
      Map<String, Object> patientMap = patientDynaBean.getMap();
      createPID(adt, patientMap);
      createPV1(adt, patientMap, adtData);
      createMRG(adt, adtData);
    }

    Map<String, String> item = new HashMap<>();
    String encodedMessage = getParsedMessage(adt);
    item.put("message_body", encodedMessage);
    item.put("_hl7_message", encodedMessage);
    return item;
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
  private void createMSH(ADT_A18 adt, Map configParams) throws DataTypeException {
    Map<String, String> dataMap = new HashMap<>();
    dataMap.put("sending_application_namespace_id", "ADT18");
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
  private void createPID(ADT_A18 adt, Map<String, Object> patientMap) throws HL7Exception {
    PID pid = adt.getPID();
    InstaPID instaPid = (InstaPID) ApplicationContextProvider.getApplicationContext()
        .getBean("instaPID");
    instaPid.createPID(pid, patientMap);
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
  private void createEvn(ADT_A18 adt, Map<String, String> data) throws DataTypeException {
    EVN evn = adt.getEVN();
    evn.getEventTypeCode().setValue("A18");
    evn.getRecordedDateTime().getTimeOfAnEvent().setValue((String) data.get("event_date_time"));
  }

  /**
   * Creates the PV 1.
   *
   * @param adt
   *          the adt
   * @param patientMap
   *          the patient map
   * @throws DataTypeException
   *           the data type exception
   */
  private void createPV1(ADT_A18 adt, Map<String, Object> patientMap,
      Map<String, String> configParams) throws DataTypeException {
    PV1 pv1 = adt.getPV1();
    InstaPV1 instaPV1 = (InstaPV1) ApplicationContextProvider.getApplicationContext()
        .getBean("instaPV1");
    instaPV1.createPV1(pv1, patientMap, configParams);
  }

  /**
   * Creates the MRG.
   *
   * @param adt
   *          the adt
   * @param oldMrnoMap
   *          the old mrno map
   * @throws HL7Exception
   *           the HL 7 exception
   */
  @SuppressWarnings("rawtypes")
  private void createMRG(ADT_A18 adt, Map adtData) throws HL7Exception {
    if (adtData.get("old_mr_no") != null) {
      MRG mrg = adt.getMRG();
      mrg.insertMrg1_PriorPatientIDInternal(0).getCx1_ID()
          .setValue((String) adtData.get("old_mr_no"));
      mrg.getPriorVisitNumber().getCx1_ID().setValue((String) adtData.get("old_visit_id"));
    }
  }
}
