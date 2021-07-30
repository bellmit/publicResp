package com.insta.hms.mdm.sequences.posequences;

import com.insta.hms.mdm.sequences.SequenceMasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class PurchaseOrderSequenceRepository.
 */
@Repository
public class PurchaseOrderSequenceRepository extends SequenceMasterRepository<Integer> {

  /** The Constant PURCHASE_ORDER_SEQUENCE_UNIQUE_COLUMNS. */
  public static final String[] PURCHASE_ORDER_SEQUENCE_UNIQUE_COLUMNS =
      new String[] {"pattern_id", "priority", "store_id"};

  /**
   * Instantiates a new purchase order sequence repository.
   */
  public PurchaseOrderSequenceRepository() {
    super("hosp_po_seq_prefs", "po_seq_id", "PON", PURCHASE_ORDER_SEQUENCE_UNIQUE_COLUMNS);
  }
}
