package com.insta.hms.messaging.processor;

import ca.uhn.hl7v2.model.v23.datatype.EI;
import ca.uhn.hl7v2.model.v23.message.ORM_O01;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.core.diagnostics.DiagnosticsService;
import com.insta.hms.messaging.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class OrmLabMessageProcessor extends GenericMessageProcessor {

  public static final Logger logger = LoggerFactory.getLogger(OrmLabMessageProcessor.class);
  protected static final Map<String, String> hl7OrderCtrlToInstaMap = new HashMap<>();

  static {
    hl7OrderCtrlToInstaMap.put("A", "P");
    hl7OrderCtrlToInstaMap.put("IP", "P");
    hl7OrderCtrlToInstaMap.put("SC", "P");
    hl7OrderCtrlToInstaMap.put("CM", "C");
  }

  @Override
  public boolean process(Map processorDataMap) {
    return ((DiagnosticsService) ApplicationContextProvider.getApplicationContext()
        .getBean("diagnosticsService")).statusChange(processorDataMap);
  }

  @Override
  public Map parse(Message message) {
    ca.uhn.hl7v2.model.Message hl7Message = message.getHl7ModelMessage();
    ORM_O01 ormMessage = (ORM_O01) hl7Message;
    EI[] ei = ormMessage.getORDER().getORC().getPlacerOrderNumber();
    ca.uhn.hl7v2.model.v23.datatype.ID id = ormMessage.getORDER().getORC().getOrderControl();
    String orderNumber = ei[0].getEi1_EntityIdentifier().getValue();
    Map<String, Object> map = new HashMap<>();
    if (orderNumber == null || orderNumber.equals("")) {
      logger.error("Placer order number is empty");
      return null;
    } else {
      if (orderNumber.contains(".")) {
        orderNumber = orderNumber.split("\\.")[0];
      }
    }
    String ctrlCode = id.getValue();
    if (ctrlCode == null || ctrlCode.equals("")
        || hl7OrderCtrlToInstaMap.get(id.getValue()) == null) {
      logger.error("Ctrl code: " + ctrlCode + " Not supported");
      return null;
    }
    map.put("conducted", hl7OrderCtrlToInstaMap.get(ctrlCode));
    map.put("prescribed_id", orderNumber);

    return map;
  }
}
