package com.insta.hms.common.minio;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class MinioSseRepository extends GenericRepository {

  public MinioSseRepository() {
    super("minio_sse");
  }

}

