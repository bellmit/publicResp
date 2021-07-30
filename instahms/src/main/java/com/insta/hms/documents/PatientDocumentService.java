package com.insta.hms.documents;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * The Class PatientDocumentService.
 */
@Service
public class PatientDocumentService {

  /** The patient document repository. */
  @LazyAutowired
  private PatientDocumentRepository patientDocumentRepository;

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(PatientDocumentService.class);

  /**
   * Gets the doc format from doc id.
   *
   * @param docId
   *          the doc id
   * @return the doc format from doc id
   */
  public BasicDynaBean getDocFormatFromDocId(Integer docId) {
    return patientDocumentRepository.getDocFormatFromDocId(docId);
  }

  /**
   * Delete.
   *
   * @param docId
   *          the doc id
   * @return the int
   */
  public int delete(Integer docId) {
    return patientDocumentRepository.delete("doc_id", docId);
  }

  /**
   * Batch delete.
   *
   * @param docIds
   *          the doc ids
   * @return the int[]
   */
  public int[] batchDelete(List<Object> docIds) {
    return patientDocumentRepository.batchDelete("doc_id", docIds);
  }

  /**
   * Find by key.
   *
   * @param docId
   *          the doc id
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(int docId) {
    return patientDocumentRepository.findByKey("doc_id", docId);
  }

  /**
   * Gets the next sequence.
   *
   * @return the next sequence
   */
  public Integer getNextSequence() {
    return patientDocumentRepository.getNextSequence();
  }

  /**
   * Insert.
   *
   * @param bean
   *          the bean
   * @return the integer
   */
  public Integer insert(BasicDynaBean bean) {
    return patientDocumentRepository.insert(bean);
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return patientDocumentRepository.getBean();
  }

  /**
   * Gets the attach document file given the document Id. Returns null if docId is invalid.
   *
   * @param docId
   *          the doc id
   * @return the attach document file
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public File getAttachDocumentFile(int docId) throws IOException {
    BasicDynaBean patientDoc = findByKey(docId);
    if (patientDoc != null) {
      String fileName = (String) patientDoc.get("filename");
      String tempDir = System.getProperty("java.io.tmpdir");
      File file = new File(tempDir + "/" + fileName);
      IOUtils.copy((ByteArrayInputStream) patientDoc.get("doc_content_bytea"),
          new FileOutputStream(file));
      return file;
    }
    return null;
  }

  /**
   * Gets the associated mr no.
   *
   * @param docId
   *          the doc id
   * @return the associated mr no
   */
  public List<String> getAssociatedMrNo(List<String> docId) {
    return patientDocumentRepository.getAssociatedMrNo(docId);
  }

  /**
   * Checks if is doc id valid.
   *
   * @param docId the doc id
   * @return the boolean
   */
  public Boolean isDocIdValid(String docId) {
    Integer integerDocId = null;
    try {
      integerDocId = Integer.parseInt(docId);
    } catch (NumberFormatException exception) {
      logger.warn("Unable to parse string into docid for:" + docId + exception);
      return false;
    }
    return patientDocumentRepository.exist("doc_id", integerDocId);
  }

}
