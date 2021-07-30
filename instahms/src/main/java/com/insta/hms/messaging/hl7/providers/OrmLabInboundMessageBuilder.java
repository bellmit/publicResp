package com.insta.hms.messaging.hl7.providers;

import com.insta.hms.messaging.InboundMessageBuilder;
import com.insta.hms.messaging.MessageContext;
import com.insta.hms.messaging.processor.OrmLabMessageProcessor;

import java.sql.SQLException;

public class OrmLabInboundMessageBuilder extends InboundMessageBuilder {

  public OrmLabInboundMessageBuilder() {
    this.addProcessor(new OrmLabMessageProcessor());
  }

  /**
   * Pre process.
   *
   * @param ctxt
   *          the ctxt
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean preProcess(MessageContext ctxt) throws SQLException {

    return true;
  }

}
