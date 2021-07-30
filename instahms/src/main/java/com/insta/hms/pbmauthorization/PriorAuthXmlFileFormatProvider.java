/**
 *
 */
package com.insta.hms.pbmauthorization;

import org.apache.commons.digester.Digester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;

/**
 * @author lakshmi
 *
 */
public class PriorAuthXmlFileFormatProvider {

	static Logger logger = LoggerFactory.getLogger(PriorAuthXmlFileFormatProvider.class);

	private Digester digester;

	public PriorAuthXmlFileFormatProvider() {
		digester = new Digester();
		digester.setValidating(false);

		digester.addObjectCreate("Files", "com.insta.hms.pbmauthorization.XMLFiles");

		// File
		digester.addObjectCreate("Files/File", "com.insta.hms.pbmauthorization.XMLFile");
		digester.addSetProperties("Files/File", "FileID", "fileId");
		digester.addSetProperties("Files/File", "FileName", "fileName");
		digester.addSetProperties("Files/File", "SenderID", "senderId");
		digester.addSetProperties("Files/File", "ReceiverID", "receiverId");
		digester.addSetProperties("Files/File", "TransactionDate", "transactionDate");
		digester.addSetProperties("Files/File", "RecordCount", "recordCount");
		digester.addSetProperties("Files/File", "IsDownloaded", "isDownloaded");

		digester.addSetNext("Files/File", "addFile");
	}

	public XMLFiles getPriorAuthXmlFileFormatMetaData(String txnXMLs)
			throws IOException, org.xml.sax.SAXException, SQLException {

		XMLFiles filedesc = (XMLFiles) new PriorAuthXmlFileFormatProvider().digester.parse(new StringReader(txnXMLs));
		return filedesc;
	}
}
