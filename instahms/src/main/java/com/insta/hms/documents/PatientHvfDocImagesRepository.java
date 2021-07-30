package com.insta.hms.documents;

import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PatientHvfDocImagesRepository extends GenericRepository {

  public PatientHvfDocImagesRepository() {
    super("patient_hvf_doc_images");
    // TODO Auto-generated constructor stub
  }

  public boolean insertAll(List<BasicDynaBean> records) {
    int[] result = batchInsert(records);
    return result.length == records.size();
  }

}
