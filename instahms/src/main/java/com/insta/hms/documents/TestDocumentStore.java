package com.insta.hms.documents;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.order.testitems.TestOrderItemRepository;
import com.insta.hms.exception.ConversionException;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDocumentStore extends AbstractDocumentStore {

  public TestDocumentStore(String documentType, boolean specialized) {
    super(documentType, specialized);
  }

  public enum KEYS {
    prescribed_id
  }

  @LazyAutowired
  private TestDocumentsRepository testDocumentsRepository;

  @LazyAutowired
  private GenericDocumentsUtil genericDocumentsUtil;

  @LazyAutowired
  private TestOrderItemRepository testOrderItemRepository;

  @Override
  public Map<String, Object> getKeys() {
    Map<String, Object> keyValues = new HashMap<>();
    for (KEYS key : KEYS.values()) {
      keyValues.put(key.name(), null);
    }
    return keyValues;
  }

  @Override
  public boolean postCreate(int docId, Map requestParams, List errors) throws IOException {
    // logic to insert in test_documents table moved to testOrderItemService.
    return true;
  }

  @Override
  public boolean postUpdate(int docId, Map requestParams, List errors) throws IOException {
    Map<String, Object> keys = new HashMap<>();
    keys.put("doc_id", docId);

    BasicDynaBean testdocbean = testDocumentsRepository.getBean();
    ConversionUtils.copyToDynaBean(requestParams, testdocbean, errors);

    if (errors.isEmpty()) {
      testdocbean.set("doc_id", docId);
      return testDocumentsRepository.update(testdocbean, keys) > 0;
    } else {
      throw new ConversionException(errors);
    }
  }

  @Override
  public boolean postDelete(Object docId, String format) throws IOException {
    return testDocumentsRepository.delete("doc_id", docId) > 0;
  }

  @Override
  public List<BasicDynaBean> getDocumentsList(String key, Object valueCol, Boolean specialized,
      String specializedDocType) throws IOException {
    return Collections.emptyList();
  }

  @Override
  public List<BasicDynaBean> searchDocuments(Map listingParams, Map extraParams,
      Boolean specialized) throws IOException, ParseException {
    // TODO: not needed for CRUD. will be done later.
    return Collections.emptyList();
  }

  @Override
  public void copyReplaceableFields(Map fields, Map keyParams, boolean underscore)
      throws SQLException {
    if (fields == null) {
      return;
    }

    int prescribedId = Integer.parseInt((String) keyParams.get("prescribed_id"));
    BasicDynaBean presBean = testOrderItemRepository.getPrescribedDetails(prescribedId);

    // copy all the test details
    genericDocumentsUtil.convertAndCopy(presBean.getMap(), fields, underscore);

    // copy all patient details
    String mrNo = (String) presBean.get("mr_no");
    String patientId = (String) presBean.get("pat_id");
    genericDocumentsUtil.copyPatientDetails(fields, mrNo, patientId, underscore);
  }

  @Override
  public Map<String, Object> getDocKeyParams(int docId) {
    BasicDynaBean documentDetails = testDocumentsRepository.findByKey("doc_id", docId);
    Map<String, Object> map = new HashMap<>();
    if (null != documentDetails) {
      map.put("prescribed_id", documentDetails.get("prescribed_id"));
    }
    return map;
  }

  @Override
  public void copyDocumentDetails(int docId, Map to) {
    BasicDynaBean documentDetails = testDocumentsRepository.findByKey("doc_id", docId);
    if (null != documentDetails) {
      to.putAll(documentDetails.getMap());
    }
  }

  @Override
  public int getCenterId(Map requestParams) {
    return 0;
  }

}
