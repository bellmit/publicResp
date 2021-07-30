/**
 *
 */
package com.insta.hms.pbmauthorization;

import java.util.ArrayList;

/**
 * @author lakshmi
 *
 */
public class XMLFiles {

	private ArrayList<XMLFile> xmlfiles;

	public XMLFiles(){
		xmlfiles = new ArrayList<XMLFile>();
	}
	public ArrayList<XMLFile> getFiles() {
		return xmlfiles;
	}
	public void setFiles(ArrayList<XMLFile> xmlfiles) {
		this.xmlfiles = xmlfiles;
	}
	public void addFile(XMLFile xmlfile){
		xmlfiles.add(xmlfile);
	}
}
