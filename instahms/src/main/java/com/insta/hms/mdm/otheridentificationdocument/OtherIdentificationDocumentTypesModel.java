package com.insta.hms.mdm.otheridentificationdocument;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "other_identification_document_types", uniqueConstraints = @UniqueConstraint(columnNames = "other_identification_doc_name"))
public class OtherIdentificationDocumentTypesModel implements java.io.Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private int otherIdentificationDocId;
  private String otherIdentificationDocName;
  private String status;

  public OtherIdentificationDocumentTypesModel() {
  }

  public OtherIdentificationDocumentTypesModel(int otherIdentificationDocId) {
    this.otherIdentificationDocId = otherIdentificationDocId;
  }

  public OtherIdentificationDocumentTypesModel(int otherIdentificationDocId,
      String otherIdentificationDocName,
      String status) {
    this.otherIdentificationDocId = otherIdentificationDocId;
    this.otherIdentificationDocName = otherIdentificationDocName;
    this.status = status;
  }

  @Id
  @Column(name = "other_identification_doc_id", unique = true, nullable = false)
  public int getOtherIdentificationDocId() {
    return otherIdentificationDocId;
  }

  public void setOtherIdentificationDocId(int otherIdentificationDocId) {
    this.otherIdentificationDocId = otherIdentificationDocId;
  }

  @Column(name = "other_identification_doc_name", unique = true, length = 100, nullable = false)
  public String getOtherIdentificationDocName() {
    return otherIdentificationDocName;
  }

  public void setOtherIdentificationDocName(String otherIdentificationDocName) {
    this.otherIdentificationDocName = otherIdentificationDocName;
  }

  @Column(name = "status", length = 1)
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }


}
