package com.insta.hms.core.clinical.multiuser;

import com.insta.hms.common.annotations.LazyAutowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;

/**
 * The Class MultiUserRedisRepository.
 *
 * @author sonam
 */
@Repository
public class MultiUserRedisRepository {

  /** The redis template. */
  @LazyAutowired
  private RedisTemplate<String, Object> redisTemplate;
  /**
   * Gets the data.
   *
   * @param key the key
   * @return the data
   */

  @SuppressWarnings("unchecked")
  public Map<String, Object> getData(String key) {
    HashOperations hashOperations = redisTemplate.opsForHash();
    return hashOperations.entries(key);

  }

  /**
   * Adds the data.
   *
   * @param key the key
   * @param hashKey the hash key
   * @param value the value
   */
  @SuppressWarnings("unchecked")
  public void addData(String key, Object hashKey, Map<String, Object> value) {
    HashOperations hashOperations = redisTemplate.opsForHash();
    hashOperations.putIfAbsent(key, hashKey, value);
  }

  /**
   * Delete data.
   *
   * @param key the key
   * @param hashKey the hash key
   */
  @SuppressWarnings("unchecked")
  public void deleteData(String key, Object hashKey) {
    HashOperations hashOperations = redisTemplate.opsForHash();
    hashOperations.delete(key, hashKey);
  }

  /**
   * Gets the hash key data.
   *
   * @param key the key
   * @param hashKey the hash key
   * @return the hash key data
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getHashKeyData(String key, Object hashKey) {
    HashOperations hashOperations = redisTemplate.opsForHash();
    return (Map<String, Object>) hashOperations.get(key, hashKey);
  }

  /**
   * Delete key.
   *
   * @param key the key
   */
  @SuppressWarnings("unchecked")
  public void deleteKey(String key) {
    HashOperations hashOperations = redisTemplate.opsForHash();
    hashOperations.getOperations().delete(key);
  }
  
  /**
   * Gets the redis keys matching the pattern.
   *
   * @param pattern the pattern
   * @return the keys
   */
  public Set<String> getKeys(String pattern) {
    return redisTemplate.keys(pattern);
  }
}
