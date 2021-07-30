package com.insta.hms.mdm.testresultranges;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class TestResultRangeRepository.
 *
 * @author anil.n
 */
@Repository
public class TestResultRangeRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new test result range repository.
   */
  public TestResultRangeRepository() {
    super("test_result_ranges", "result_range_id");
  }

}
