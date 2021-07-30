/**
 *
 */

package com.insta.hms.erxprescription.erxauthorization;

import com.insta.hms.erxprescription.ERxRequest;
import com.insta.hms.eservice.EResult;

import java.util.ArrayList;

/**
 * The Class XMLFiles.
 *
 * @author lakshmi
 */
public class XMLFiles extends ERxRequest implements EResult {

  /**
   * The xmlfiles.
   */
  public ArrayList<XMLFile> xmlfiles;

  /**
   * Instantiates a new XML files.
   */
  public XMLFiles() {
    xmlfiles = new ArrayList<XMLFile>();
  }

  /**
   * Gets the files.
   *
   * @return the files
   */
  public ArrayList<XMLFile> getFiles() {
    return xmlfiles;
  }

  /**
   * Sets the files.
   *
   * @param xmlfiles the new files
   */
  public void setFiles(ArrayList<XMLFile> xmlfiles) {
    this.xmlfiles = xmlfiles;
  }

  /**
   * Adds the file.
   *
   * @param xmlfile the xmlfile
   */
  public void addFile(XMLFile xmlfile) {
    xmlfiles.add(xmlfile);
  }
}
