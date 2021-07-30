package com.insta.hms.integration.regulatory.ohsrsdohgovph;

import com.insta.hms.exception.HMSException;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

@Component
public class OhsrsdohgovphWebserviceClient {

  private static final DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
  
  /**
   * Get a port for OHSRS training site. 
   * @return Port
   */
  public com.insta.hms.integration.regulatory.ohsrsdohgovph.test.generated
      .OnlineHealthFacilityStatisticalReportSystemPortType getTrainingPort() {
    com.insta.hms.integration.regulatory.ohsrsdohgovph.test.generated
        .OnlineHealthFacilityStatisticalReportSystem trainingWebservice = 
        new com.insta.hms.integration.regulatory.ohsrsdohgovph.test.generated
          .OnlineHealthFacilityStatisticalReportSystem();
    return trainingWebservice.getOnlineHealthFacilityStatisticalReportSystemPort();
  }

  /**
   * Get a port for OHSRS production site. 
   * @return Port
   */
  public com.insta.hms.integration.regulatory.ohsrsdohgovph.generated
      .OnlineHealthFacilityStatisticalReportSystemPortType getProductionPort() {
    com.insta.hms.integration.regulatory.ohsrsdohgovph.generated
        .OnlineHealthFacilityStatisticalReportSystem productionWebservice = 
        new com.insta.hms.integration.regulatory.ohsrsdohgovph.generated
          .OnlineHealthFacilityStatisticalReportSystem();  
    return productionWebservice.getOnlineHealthFacilityStatisticalReportSystemPort();
    
  }
  
  /**
   * Parse and validate incoming response from OHSRS Service for success.
   * @param responseXml Incoming RAW XML Data
   * @return true if success, raise HMS exception otherwise
   */
  public boolean parseWebserviceResponse(String responseXml) {
    try {
      DocumentBuilder builder = xmlFactory.newDocumentBuilder();
      Document response = builder.parse(new InputSource(new StringReader(responseXml)));
      String responseCode = response.getElementsByTagName("response_code").item(0).getTextContent();
      String responseDesc = response.getElementsByTagName("response_desc").item(0).getTextContent();
      if (responseCode.equals("104")) {
        return true;
      }
      throw new HMSException("ui.exception.ohsrs.service.responded.with.triple.placeholder",
          new String[] {responseCode, responseDesc, responseXml});
    } catch (ParserConfigurationException ex) {
      return false;
    } catch (SAXException ex) {
      return false;
    } catch (IOException ex) {
      return false;
    }
  }
}
