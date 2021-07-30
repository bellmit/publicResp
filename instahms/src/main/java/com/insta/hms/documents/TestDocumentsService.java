package com.insta.hms.documents;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestDocumentsService {

  @LazyAutowired
  private TestDocumentsRepository testDocumentsRepo;

  public int updateDocumentsPrescribedId(List<Integer> docIdsList, Integer prescribedId) {
    return testDocumentsRepo.updateDocumentsPrescribedId(docIdsList, prescribedId);
  }

  public BasicDynaBean findByKey(Integer docId) {
    return testDocumentsRepo.findByKey("doc_id", docId);
  }

  public BasicDynaBean getBean() {
    return testDocumentsRepo.getBean();
  }

  public int[] insertDocuments(List<BasicDynaBean> beans) {
    return testDocumentsRepo.batchInsert(beans);
  }

  public List<BasicDynaBean> getAdditionalDocs(Integer prescribedId) {
    return testDocumentsRepo.getAdditionalDocs(prescribedId);
  }
}
