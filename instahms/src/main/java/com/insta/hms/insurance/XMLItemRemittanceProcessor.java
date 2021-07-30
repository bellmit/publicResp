package com.insta.hms.insurance;

/**
 * The Class XMLItemRemittanceProcessor.
 */
public class XMLItemRemittanceProcessor extends XMLRemittanceProcessor {

  /**
   * Instantiates a new XML item remittance processor.
   */
  public XMLItemRemittanceProcessor() {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.insurance.XMLRemittanceProcessor#getXMLRemittanceProvider()
   */
  public XMLRemittanceProvider getXMLRemittanceProvider() {
    return new XMLItemRemittanceProvider();
  }

}
