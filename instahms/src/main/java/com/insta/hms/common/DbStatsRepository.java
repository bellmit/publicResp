package com.insta.hms.common;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The Class DbStatsRepository.
 */
@Repository("dbStatsRepository")
public class DbStatsRepository extends GenericRepository {

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private RedisTemplate template;

  /** The Constant THRESHOLD_LARGE_DATASET. */
  private static final Integer THRESHOLD_LARGE_DATASET = 500000;

  /** The Constant IS_LARGE_DATASET_QUERY. */
  private static final String IS_LARGE_DATASET_QUERY = "SELECT relname as dataset, "
      + "CASE WHEN n_live_tup > ? then 'Y' else 'N' end as is_large_dataset"
      + " FROM pg_stat_user_tables WHERE schemaname = ?";

  /** The Constant IS_LARGE_DATASET_FILTER_CLAUSE. */
  private static final String IS_LARGE_DATASET_FILTER_CLAUSE = " AND relname = ?";

  /**
   * Instantiates a new db stats repository.
   */
  DbStatsRepository() {
    super("pg_stat_user_tables");
  }

  /**
   * Returns Map of table name : boolean where truthy value represent dataset (table in db) having
   * more records than threshold.
   *
   * @return boolean
   */
  public Map<String, Boolean> getLargeDatasetMap() {
    String key = String.format("schema:%s;dbstats", RequestContext.getSchema());
    Map<String, Boolean> responseMap = (Map<String, Boolean>) template.opsForHash().entries(key);
    if (responseMap != null && !responseMap.isEmpty()) {
      return responseMap;
    }
    List<BasicDynaBean> results = DatabaseHelper.queryToDynaList(IS_LARGE_DATASET_QUERY,
        new Object[] { THRESHOLD_LARGE_DATASET,
            sessionService.getSessionAttributes().get("sesHospitalId") });
    responseMap = new HashMap<String, Boolean>();
    for (BasicDynaBean bean : results) {
      responseMap.put((String) bean.get("dataset"),
          ((String) bean.get("is_large_dataset")).equals("Y"));
    }
    template.opsForHash().putAll(key, responseMap);
    template.expire(key, 7, TimeUnit.DAYS);

    return responseMap;
  }

  /**
   * Returns true if given dataset (table in db) has more records than threshold.
   *
   * @param dataset the dataset
   * @return boolean
   */
  public Boolean isLargeDataset(String dataset) {
    Map<String, Boolean> map = this.getLargeDatasetMap();
    return map != null && map.get(dataset);
  }
}