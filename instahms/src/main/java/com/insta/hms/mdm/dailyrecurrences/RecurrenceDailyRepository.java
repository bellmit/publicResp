package com.insta.hms.mdm.dailyrecurrences;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The repository for Recurrence Daily Master.
 * @author sainathbatthala
 */
@Repository
public class RecurrenceDailyRepository extends MasterRepository<Integer> {

  public RecurrenceDailyRepository() {
    super("recurrence_daily_master", "recurrence_daily_id", "display_name");
  }
}
