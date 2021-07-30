package com.insta.hms.orders;

import java.io.InputStream;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDocumentDTO.
 *
 * @author krishna
 */
public class TestDocumentDTO {

  /** The doc content. */
  private InputStream docContent;

  /** The content type. */
  private Object contentType;

  /** The notes edited. */
  private boolean notesEdited;

  /** The delete document. */
  private boolean deleteDocument;

  /** The doc id. */
  private int docId;

  /** The test presc id. */
  private int testPrescId;

  /** The activity index. */
  private int activityIndex;

  /** The clinical notes. */
  private String clinicalNotes;

  /** The file name. */
  private String fileName;

  /** The extension. */
  private String extension;

  /** The test category. */
  private String testCategory;

  /** The doc name. */
  private String docName;

  /**
   * Gets the test category.
   *
   * @return the test category
   */
  public String getTestCategory() {
    return testCategory;
  }

  /**
   * Sets the test category.
   *
   * @param testCategory the new test category
   */
  public void setTestCategory(String testCategory) {
    this.testCategory = testCategory;
  }

  /**
   * Gets the extension.
   *
   * @return the extension
   */
  public String getExtension() {
    return extension;
  }

  /**
   * Sets the extension.
   *
   * @param extension the new extension
   */
  public void setExtension(String extension) {
    this.extension = extension;
  }

  /**
   * Gets the file name.
   *
   * @return the file name
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Sets the file name.
   *
   * @param fileName the new file name
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   * Gets the clinical notes.
   *
   * @return the clinical notes
   */
  public String getClinicalNotes() {
    return clinicalNotes;
  }

  /**
   * Sets the clinical notes.
   *
   * @param clinicalNotes the new clinical notes
   */
  public void setClinicalNotes(String clinicalNotes) {
    this.clinicalNotes = clinicalNotes;
  }

  /**
   * Gets the content type.
   *
   * @return the content type
   */
  public Object getContentType() {
    return contentType;
  }

  /**
   * Sets the content type.
   *
   * @param contentType the new content type
   */
  public void setContentType(Object contentType) {
    this.contentType = contentType;
  }

  /**
   * Gets the doc content.
   *
   * @return the doc content
   */
  public InputStream getDocContent() {
    return docContent;
  }

  /**
   * Sets the doc content.
   *
   * @param docContent the new doc content
   */
  public void setDocContent(InputStream docContent) {
    this.docContent = docContent;
  }

  /**
   * Gets the doc id.
   *
   * @return the doc id
   */
  public int getDocId() {
    return docId;
  }

  /**
   * Sets the doc id.
   *
   * @param docId the new doc id
   */
  public void setDocId(int docId) {
    this.docId = docId;
  }

  /**
   * Gets the doc name.
   *
   * @return the doc name
   */
  public String getDocName() {
    return docName;
  }

  /**
   * Sets the doc name.
   *
   * @param docName the new doc name
   */
  public void setDocName(String docName) {
    this.docName = docName;
  }

  /**
   * Gets the test presc id.
   *
   * @return the test presc id
   */
  public int getTestPrescId() {
    return testPrescId;
  }

  /**
   * Sets the test presc id.
   *
   * @param testPrescId the new test presc id
   */
  public void setTestPrescId(int testPrescId) {
    this.testPrescId = testPrescId;
  }

  /**
   * Gets the activity index.
   *
   * @return the activity index
   */
  public int getActivityIndex() {
    return activityIndex;
  }

  /**
   * Sets the activity index.
   *
   * @param activityIndex the new activity index
   */
  public void setActivityIndex(int activityIndex) {
    this.activityIndex = activityIndex;
  }

  /**
   * Checks if is notes edited.
   *
   * @return true, if is notes edited
   */
  public boolean isNotesEdited() {
    return notesEdited;
  }

  /**
   * Sets the notes edited.
   *
   * @param notesEdited the new notes edited
   */
  public void setNotesEdited(boolean notesEdited) {
    this.notesEdited = notesEdited;
  }

  /**
   * Checks if is delete document.
   *
   * @return true, if is delete document
   */
  public boolean isDeleteDocument() {
    return deleteDocument;
  }

  /**
   * Sets the delete document.
   *
   * @param deleteDocument the new delete document
   */
  public void setDeleteDocument(boolean deleteDocument) {
    this.deleteDocument = deleteDocument;
  }
}
