package com.insta.hms.messaging.hl7.parser;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;

import com.insta.hms.messaging.GenericParser;
import com.insta.hms.messaging.Message;
import com.insta.hms.messaging.MessageParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hl7Parser extends GenericParser implements MessageParser {

  public static final Logger logger = LoggerFactory.getLogger(Hl7Parser.class);

  @Override
  public Message getInstaMessage(String message) {
    Message msg = new Message();
    HapiContext context = null;
    try {
      context = new DefaultHapiContext();
      CanonicalModelClassFactory mcf = new CanonicalModelClassFactory("2.3");
      context.setModelClassFactory(mcf);
      PipeParser parser = context.getPipeParser();
      ca.uhn.hl7v2.model.Message hl7Message = parser.parse(message);
      msg.setHl7ModelMessage(hl7Message);
      if (hl7Message.getName().equals("ORM_O01")) {
        msg.setMessageType("orm_lab");
      } else if (hl7Message.getName().equals("ORU_R01")) {
        msg.setMessageType("ORU_R01");
      }
    } catch (HL7Exception ex) {
      msg.setErrorMsg(ex.getMessage());
      logger.error(ex.getMessage());
    }
    return msg;
  }

}
