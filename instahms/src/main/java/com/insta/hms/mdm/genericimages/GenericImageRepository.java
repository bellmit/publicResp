package com.insta.hms.mdm.genericimages;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class GenericImageRepository extends GenericRepository {

  public GenericImageRepository() {
    super("doc_hosp_images");
  }

}