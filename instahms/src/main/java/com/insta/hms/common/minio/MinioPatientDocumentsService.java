package com.insta.hms.common.minio;

import com.insta.hms.common.BusinessService;
import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class MinioPatientDocumentsService.
 */
@Service
public class MinioPatientDocumentsService extends BusinessService {

  /** The minio patient documents repository. */
  @LazyAutowired
  MinioPatientDocumentsRepository minioPatientDocumentsRepository;

  /**
   * Find by doc id.
   *
   * @param docId the doc id
   * @return the basic dyna bean
   */
  public BasicDynaBean findByDocId(int docId) {
    return minioPatientDocumentsRepository.findByKey("doc_id", docId);
  }

  /**
   * Insert.
   *
   * @param bean the bean
   * @return the integer
   */
  public Integer insert(BasicDynaBean bean) {
    return minioPatientDocumentsRepository.insert(bean);
  }

  /**
   * Insert.
   *
   * @param docId the doc id
   * @param path the path
   * @return the integer
   */
  public Integer insert(int docId, String path) {
    BasicDynaBean bean = minioPatientDocumentsRepository.getBean();
    bean.set("path", path);
    bean.set("doc_id", docId);
    return minioPatientDocumentsRepository.insert(bean);
  }

  /**
   * Update.
   *
   * @param docId the doc id
   * @param path the path
   * @return the integer
   */
  public Integer update(int docId, String path) {
    BasicDynaBean bean = minioPatientDocumentsRepository.getBean();
    bean.set("path", path);
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("doc_id", docId);
    return minioPatientDocumentsRepository.update(bean, keys);
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return minioPatientDocumentsRepository.getBean();
  }
}
