package com.insta.hms.diagnosticmodule.documents;

import java.util.Date;

/**
 * The Class TestDocumentDTO.
 *
 * @author krishna
 */
public class TestDocumentDTO {
  /**
   * The unique identifier for the document. It is expected that a document is uniquely identifiable
   * per provider.
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

  /** The url which will provide the display of this document. */
  private String displayUrl;

  /** Name of the user who added the document. */
  private String userName;

  /** The external link. */
  private boolean externalLink; // document is a external link

  /** The visit date. */
  private Date visitDate;

  /** The center name. */
  private String centerName;

  /**
   * Gets the center name.
   *
   * @return the center name
   */
  public String getCenterName() {
    return centerName;
  }

  /**
   * Sets the center name.
   *
   * @param centerName
   *          the new center name
   */
  public void setCenterName(String centerName) {
    this.centerName = centerName;
  }

  /**
   * Whether a PDF form is supported for this document. Most documents do support PDF output, but
   * some may only support HTML output (eg, old Investigation reports). this flag is used to
   * determine if the document can be part of a docket (collation of multiple documents).
   */
  private boolean pdfSupported;

  /**
   * Gets the docid.
   *
   * @return the docid
   */
  public String getDocid() {
    return docid;
  }

  /**
   * Sets the docid.
   *
   * @param docid
   *          the new docid
   */
  public void setDocid(String docid) {
    this.docid = docid;
  }

  /**
   * Gets the title.
   *
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title.
   *
   * @param title
   *          the new title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description.
   *
   * @param description
   *          the new description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets the date.
   *
   * @return the date
   */
  public Date getDate() {
    return date;
  }

  /**
   * Sets the date.
   *
   * @param date
   *          the new date
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the type.
   *
   * @param type
   *          the new type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Gets the content type.
   *
   * @return the content type
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * Sets the content type.
   *
   * @param contentType
   *          the new content type
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  /**
   * Gets the visitid.
   *
   * @return the visitid
   */
  public String getVisitid() {
    return visitid;
  }

  /**
   * Sets the visitid.
   *
   * @param visitid
   *          the new visitid
   */
  public void setVisitid(String visitid) {
    this.visitid = visitid;
  }

  /**
   * Gets the doctor.
   *
   * @return the doctor
   */
  public String getDoctor() {
    return doctor;
  }

  /**
   * Sets the doctor.
   *
   * @param doctor
   *          the new doctor
   */
  public void setDoctor(String doctor) {
    this.doctor = doctor;
  }

  /**
   * Gets the updated by.
   *
   * @return the updated by
   */
  public String getUpdatedBy() {
    return updatedBy;
  }

  /**
   * Sets the updated by.
   *
   * @param updatedBy
   *          the new updated by
   */
  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  /**
   * Gets the updated date.
   *
   * @return the updated date
   */
  public String getUpdatedDate() {
    return updatedDate;
  }

  /**
   * Sets the updated date.
   *
   * @param updatedDate
   *          the new updated date
   */
  public void setUpdatedDate(String updatedDate) {
    this.updatedDate = updatedDate;
  }

  /**
   * Gets the display url.
   *
   * @return the display url
   */
  public String getDisplayUrl() {
    return displayUrl;
  }

  /**
   * Sets the display url.
   *
   * @param displayUrl
   *          the new display url
   */
  public void setDisplayUrl(String displayUrl) {
    this.displayUrl = displayUrl;
  }

  /**
   * Gets the user name.
   *
   * @return the user name
   */
  public String getUserName() {
    return userName;
  }

  /**
   * Sets the user name.
   *
   * @param userName
   *          the new user name
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * Checks if is external link.
   *
   * @return true, if is external link
   */
  public boolean isExternalLink() {
    return externalLink;
  }

  /**
   * Sets the external link.
   *
   * @param externalLink
   *          the new external link
   */
  public void setExternalLink(boolean externalLink) {
    this.externalLink = externalLink;
  }

  /**
   * Gets the visit date.
   *
   * @return the visit date
   */
  public Date getVisitDate() {
    return visitDate;
  }

  /**
   * Sets the visit date.
   *
   * @param visitDate
   *          the new visit date
   */
  public void setVisitDate(Date visitDate) {
    this.visitDate = visitDate;
  }

  /**
   * Checks if is pdf supported.
   *
   * @return true, if is pdf supported
   */
  public boolean isPdfSupported() {
    return pdfSupported;
  }

  /**
   * Sets the pdf supported.
   *
   * @param pdfSupported
   *          the new pdf supported
   */
  public void setPdfSupported(boolean pdfSupported) {
    this.pdfSupported = pdfSupported;
  }
}
