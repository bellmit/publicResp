package com.insta.hms.messaging;

import com.insta.hms.common.BasicCachingDAO;

public class SystemDataDao extends BasicCachingDAO {
  private static String tableName = "system_data";
  private static int maxElementsInMemory = 10;

  public SystemDataDao() {
    super(tableName, maxElementsInMemory);
  }

}
