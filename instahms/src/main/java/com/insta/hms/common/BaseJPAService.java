package com.insta.hms.common;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The Class BaseJPAService.
 * 
 * @param <R> the generic type, denoting Repository.
 * @param <M> the generic type, denoting the Model.
 * @param <K> the key type, denoting the Primary Key.
 */

public abstract class BaseJPAService<R extends JpaRepository<M, K>, M, K extends Serializable> {

  /** The repository. */
  protected R repository;

  /**
   * Instantiates a new base JPA service.
   *
   * @param repository the repository
   */
  public BaseJPAService(final R repository) {
    this.repository = repository;
  }

  /**
   * Save all.
   *
   * @param model the model
   * @param isFlushImmediate the is flush immediate
   * @return the list
   */
  public Collection<M> saveAll(Collection<M> model, boolean isFlushImmediate) {
    List<M> listModels = this.repository.save(model);
    if (isFlushImmediate) {
      this.repository.flush();
    }
    return listModels;
  }

  /**
   * Save.
   *
   * @param model the model
   * @param isFlushImmediate the is flush immediate
   * @return the m
   */
  public M save(M model, boolean isFlushImmediate) {
    M updatedModel = this.repository.save(model);
    if (isFlushImmediate) {
      this.repository.flush();
    }
    return updatedModel;
  }

  /**
   * Delete.
   * 
   * @param model the model
   * @param isFlushImmediate the is flush immediate
   */
  public void delete(M model, boolean isFlushImmediate) {
    this.repository.delete(model);
    if (isFlushImmediate) {
      this.repository.flush();
    }
  }

  /**
   * Delete in batch.
   *
   * @param entities the entities
   * @param isFlushImmediate the is flush immediate
   */
  public void deleteInBatch(Iterable<M> entities, boolean isFlushImmediate) {
    this.repository.deleteInBatch(entities);
    if (isFlushImmediate) {
      this.repository.flush();
    }
  }

  /**
   * Delete all in batch.
   *
   * @param isFlushImmediate the is flush immediate
   */
  public void deleteAllInBatch(boolean isFlushImmediate) {
    this.repository.deleteAllInBatch();
    if (isFlushImmediate) {
      this.repository.flush();
    }
  }

  /**
   * Find all.
   *
   * @return the list
   */
  public List<M> findAll() {
    return this.repository.findAll();
  }

  /**
   * Find all.
   *
   * @param sort the sort
   * @return the list
   */
  public List<M> findAll(Sort sort) {
    return this.repository.findAll(sort);
  }


  /**
   * Find all.
   *
   * @param ids the ids
   * @return the list
   */
  public List<M> findAll(Iterable<K> ids) {
    return this.repository.findAll(ids);
  }

  /**
   * Gets the one.
   *
   * @param id the id
   * @return the one
   */
  public M getOne(K id) {
    return this.repository.getOne(id);
  }

  /**
   * Flush.
   */
  public void flush() {
    this.repository.flush();
  }

  /**
   * Exists.
   *
   * @param id the id
   * @return true, if successful
   */
  public boolean exists(K id) {
    return this.repository.exists(id);
  }

  /**
   * Save or update.
   *
   * @param oldEntities the old entities
   * @param newEntities the new entities
   * @param isFlushImmediate the is flush immediate
   * @return the collection
   */
  public Collection<M> saveOrUpdate(Collection<M> oldEntities, Collection<M> newEntities,
      boolean isFlushImmediate) {
    if (CollectionUtils.isNotEmpty(oldEntities)) {
      deleteInBatch(oldEntities, isFlushImmediate);
    }
    if (CollectionUtils.isNotEmpty(newEntities)) {
      return saveAll(newEntities, isFlushImmediate);
    }
    return new ArrayList<M>();
  }
}
