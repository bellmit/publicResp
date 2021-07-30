package com.insta.hms.core.inventory.supplierreturn.debit;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * Store Debit Note Repository.
 * @author anandpatel
 *
 */
@Repository
public class StoreDebitNoteRepository extends GenericRepository {
  /**
   * Instantiates a new StoreDebitNoteRepository.
   */
  public StoreDebitNoteRepository() {
    super("store_debit_note");
  }
}
