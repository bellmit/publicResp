package com.insta.hms.mdm.bedtypes;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

// TODO: Auto-generated Javadoc
/** The Class IcuBedChargesRepository. */
@Repository
public class IcuBedChargesRepository extends MasterRepository<String> {

  /** Instantiates a new icu bed charges repository. */
  public IcuBedChargesRepository() {
    super("icu_bed_charges", "");
  }
}
