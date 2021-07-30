package com.insta.hms.common;

import net.sf.ehcache.Cache;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * Extend this class if caching is required . NOTE: If the db value is changed from backend, cache
 * doesn't get reflected
 * 
 * @author utpal.lotlikar
 *
 */
public class BasicCachingDAO extends AbstractCachingDAO {

  private int maxElementsInMemory = 1000;

  public BasicCachingDAO(String tablename) {
    super(tablename);
  }

  /**
   * Allot the maximum elements in memory with caution.
   */
  public BasicCachingDAO(String tablename, int maxElementsInMemory) {
    super(tablename);

    if (maxElementsInMemory < this.maxElementsInMemory) {
      this.maxElementsInMemory = maxElementsInMemory;
    }
  }

  @Override
  /**
   * The cache with the following setting region: name of region maxElementsInMemory: Maximum number
   * of elements that can be stored in memory memoryStoreEvictionPolicy: LRU overflowToDisk: false
   * diskStorePath: "/tmp" eternal: true timeToLiveSeconds: 0 //this is not used as we are eternal
   * timeToIdleSeconds: 0 //this is not used as we are eternal diskPersistent: false
   * diskExpiryThreadIntervalSeconds: 0 //not used registeredEventListeners: null
   */
  protected Cache newCache(String region) {
    return new Cache(region, maxElementsInMemory, MemoryStoreEvictionPolicy.LRU, false, "/tmp",
        true, 0, 0, false, 0, null);
  }

}
