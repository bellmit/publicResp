/**
 *
 */

package com.insta.hms.erxprescription.erxauthorization;

import com.insta.hms.eservice.EResult;
import com.insta.hms.eservice.EResultParser;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

/**
 * The Class ERxAuthorizationXmlFilesParser.
 *
 * @author lakshmi
 */
public class ERxAuthorizationXmlFilesParser implements EResultParser {

  /**
   * The digester.
   */
  private Digester digester;

  /**
   * Instantiates a new e rx authorization xml files parser.
   */
  public ERxAuthorizationXmlFilesParser() {
    digester = new Digester();
    digester.setValidating(false);

    digester.addObjectCreate("Files",
        "com.insta.hms.erxprescription.erxauthorization.XMLFiles");

    // File
    digester.addObjectCreate("Files/File",
        "com.insta.hms.erxprescription.erxauthorization.XMLFile");
    digester.addSetProperties("Files/File", "FileID", "fileId");
    digester.addSetProperties("Files/File", "FileName", "fileName");
    digester.addSetProperties("Files/File", "SenderID", "senderId");
    digester.addSetProperties("Files/File", "ReceiverID", "receiverId");
    digester.addSetProperties("Files/File", "TransactionDate",
        "transactionDate");
    digester.addSetProperties("Files/File", "RecordCount", "recordCount");
    digester.addSetProperties("Files/File", "IsDownloaded", "isDownloaded");

    digester.addSetNext("Files/File", "addFile");
  }

  /**
   * Parses the.
   *
   * @param xml the xml
   * @return the e result
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SAXException the SAX exception
   */
  public EResult parse(String xml) throws IOException, SAXException {
    XMLFiles xmlfiles = (XMLFiles) new ERxAuthorizationXmlFilesParser().digester
        .parse(new StringReader(xml));
    return xmlfiles;
  }
}
