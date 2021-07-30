package com.insta.hms.insurance;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

/**
 * The Class XMLRemittanceProvider.
 */
public abstract class XMLRemittanceProvider {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(XMLRemittanceProvider.class);

  /**
   * Gets the remittance advice.
   *
   * @param is        the is
   * @param remitBean the remit bean
   * @param errorMap  the error map
   * @return the remittance advice
   * @throws IOException    Signals that an I/O exception has occurred.@throws SAXException the SAX
   *                        exception
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  // Strategy method
  public abstract RemittanceAdvice getRemittanceAdvice(InputStream is, BasicDynaBean remitBean,
      Map errorMap) throws IOException, org.xml.sax.SAXException, SQLException, ParseException;

}
