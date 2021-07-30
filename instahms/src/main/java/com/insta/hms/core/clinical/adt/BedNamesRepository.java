package com.insta.hms.core.clinical.adt;

import com.insta.hms.common.GenericRepository;
import org.springframework.stereotype.Repository;

@Repository
public class BedNamesRepository extends GenericRepository {

  public BedNamesRepository() {
    super("bed_names");
  }

}
