package com.insta.hms.scheduledreport;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author krishna.t
 *
 */
public enum Event {
  Daily, Monthly, Weekly, ALL;

  /**
   * Gets the event map.
   *
   * @return the event map
   */
  @SuppressWarnings("unchecked")
  public Map getEventMap() {
    List<String> daily = new ArrayList<>();

    daily.add("Yesterday");
    daily.add("Month till Date");
    daily.add("Year till Date");

    List<String> weekly = new ArrayList<>();
    weekly.add("Last Week");
    weekly.add("Month till Date");
    weekly.add("Year till Date");

    List<String> monthly = new ArrayList<>();
    monthly.add("Last Month");
    monthly.add("Year till Date");

    EnumMap<Event, List> map = new EnumMap<>(Event.class);

    map.put(Event.Daily, daily);
    map.put(Event.Weekly, weekly);
    map.put(Event.Monthly, monthly);

    return map;
  }
}
