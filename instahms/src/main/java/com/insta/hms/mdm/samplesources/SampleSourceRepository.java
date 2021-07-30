package com.insta.hms.mdm.samplesources;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository("sampleSourceRepository")
public class SampleSourceRepository extends MasterRepository<Integer> {
  public SampleSourceRepository() {
    super("sample_sources", "source_id", "source_name");
  }
}
