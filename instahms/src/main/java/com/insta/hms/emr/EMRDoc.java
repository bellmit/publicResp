package com.insta.hms.emr;

import java.util.Date;

public class EMRDoc implements Comparable<EMRDoc> {

	/**
	 * The unique identifier for the document. It is expected that a document is uniquely  identifiable per provider.
	 */
	private String docid;

	/**
	 * The title to be displayed for the document.
	 */
	private String title;

	/**
	 * The description to be displayed on hover of the document title.
	 */
	private String description;

	/**
	 * Date of document creation.
	 */
	private Date date;

	/**
	 * The type of document.
	 */
	private String type;

	/**
	 * A icon representation of the document type
	 */
	private String icon;

	/**
	 * The mime type of the document.
	 */
	private String contentType;

	/**
	 * The visit to which the document should be associated.
	 */
	private String visitid;

	/**
	 * The doctor to whom this document is associated with.
	 */
	private String doctor;

	/**
	 * The user who updated this document last.
	 */
	private String updatedBy;

	/**
	 * The date on which the document was last updated.
	 */
	private String updatedDate;

	/**
	 * Free flow text to be displayed in the EMR associated with the document.
	 */
	private String anotation;

	/**
	 * The url which will provide the display of this document
	 */
	private String displayUrl;
	/**
	 * Documents access Permissions.
	 */
	private String accessRights;
	/**
	 * Name of the user who added the document
	 */
	private String userName;

	private boolean authorized; // true logged in has athorized to view the document.

	private boolean externalLink; // document is a external link

	private Date visitDate;

	/**
	 * Whether a PDF form is supported for this document. Most documents do support
	 * PDF output, but some may only support HTML output (eg, old Investigation reports).
	 * this flag is used to determine if the document can be part of a docket (collation
	 * of multiple documents).
	 */
	private boolean pdfSupported;

	/**
	 * The provider for the document
	 */
	private EMRInterface.Provider provider;

	private int printerId;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getAnotation() {
		return anotation;
	}

	public void setAnotation(String anotation) {
		this.anotation = anotation;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getDisplayUrl() {
		return displayUrl;
	}

	public void setDisplayUrl(String displayUrl) {
		this.displayUrl = displayUrl;
	}

	public String getDocid() {
		return docid;
	}

	public void setDocid(String docid) {
		this.docid = docid;
	}

	public String getDoctor() {
		return doctor;
	}

	public void setDoctor(String doctor) {
		this.doctor = doctor;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public EMRInterface.Provider getProvider() {
		return provider;
	}

	public void setProvider(EMRInterface.Provider provider) {
		this.provider = provider;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getVisitid() {
		return visitid;
	}

	public void setVisitid(String visitid) {
		this.visitid = visitid;
	}



	@Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((accessRights == null) ? 0 : accessRights.hashCode());
    result = prime * result + ((anotation == null) ? 0 : anotation.hashCode());
    result = prime * result + (authorized ? 1231 : 1237);
    result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
    result = prime * result + ((date == null) ? 0 : date.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((displayUrl == null) ? 0 : displayUrl.hashCode());
    result = prime * result + ((docid == null) ? 0 : docid.hashCode());
    result = prime * result + ((doctor == null) ? 0 : doctor.hashCode());
    result = prime * result + (externalLink ? 1231 : 1237);
    result = prime * result + ((icon == null) ? 0 : icon.hashCode());
    result = prime * result + (pdfSupported ? 1231 : 1237);
    result = prime * result + printerId;
    result = prime * result + ((provider == null) ? 0 : provider.hashCode());
    result = prime * result + ((title == null) ? 0 : title.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((updatedBy == null) ? 0 : updatedBy.hashCode());
    result = prime * result + ((updatedDate == null) ? 0 : updatedDate.hashCode());
    result = prime * result + ((userName == null) ? 0 : userName.hashCode());
    result = prime * result + ((visitDate == null) ? 0 : visitDate.hashCode());
    result = prime * result + ((visitid == null) ? 0 : visitid.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof EMRDoc) {
      EMRDoc doc = (EMRDoc)obj;
      return docid.equals(doc.getDocid()) && provider.equals(doc.getProvider());
    }
    return false;
  }

  public int compareTo(EMRDoc emrDoc) {
		int result = date.compareTo(emrDoc.date);
		return result == 0 ? date.compareTo(emrDoc.date) : result;
	}

	/**
	 * @return the pdfSupported
	 */
	public boolean isPdfSupported() {
		return pdfSupported;
	}

	/**
	 * @param pdfSupported
	 *            the pdfSupported to set
	 */
	public void setPdfSupported(boolean pdfSupported) {
		this.pdfSupported = pdfSupported;
	}

	public int getPrinterId() {
		return printerId;
	}

	public void setPrinterId(int printerId) {
		this.printerId = printerId;
	}

	public String getAccessRights() {
		return accessRights;
	}

	public void setAccessRights(String accessRights) {
		this.accessRights = accessRights;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isAuthorized() {
		return authorized;
	}

	public void setAuthorized(boolean authorized) {
		this.authorized = authorized;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isExternalLink() {
		return externalLink;
	}

	public void setExternalLink(boolean externalLink) {
		this.externalLink = externalLink;
	}

	public Date getVisitDate() {
		return visitDate;
	}

	public void setVisitDate(Date visitDate) {
		this.visitDate = visitDate;
	}
}
