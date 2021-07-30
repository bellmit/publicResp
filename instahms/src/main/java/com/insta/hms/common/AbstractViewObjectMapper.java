package com.insta.hms.common;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The Class AbstractViewObjectMapper.
 *
 * @param <M> the Model type
 * @param <V> the viewObject type
 */
public abstract class AbstractViewObjectMapper<M, V> {

  /**
   * Convert model to view object.
   *
   * @param modelObj the model obj
   * @return the v
   * @throws Exception the exception
   */
  public abstract V convertModelToViewObject(M modelObj) throws Exception;

  /**
   * Convert view object to model.
   *
   * @param viewObj the view obj
   * @return the m
   * @throws Exception the exception
   */
  public abstract M convertViewObjectToModel(V viewObj) throws Exception;

  /**
   * Convert models to view objects.
   *
   * @param modelObjects the model objects
   * @return the list
   */
  public List<V> convertModelsToViewObjects(Collection<M> modelObjects) {
    List<V> viewObjects = new ArrayList<>();
    if (modelObjects != null) {
      for (M modelObject : modelObjects) {
        try {
          V viewObject = convertModelToViewObject(modelObject);
          if (viewObject != null) {
            viewObjects.add(viewObject);
          }
        } catch (Exception exception) {
          throw new RuntimeException(exception);
        }
      }
    }
    return viewObjects;
  }

  /**
   * Convert view objects to models.
   *
   * @param viewObjects the view objects
   * @return the list
   */
  public List<M> convertViewObjectsToModels(Collection<V> viewObjects) {
    List<M> modelObjects = new ArrayList<>();
    if (viewObjects != null) {
      for (V viewObject : viewObjects) {
        try {
          M modelObject = convertViewObjectToModel(viewObject);
          if (modelObject != null) {
            modelObjects.add(modelObject);
          }
        } catch (Exception exception) {
          throw new RuntimeException(exception);
        }
      }
    }
    return modelObjects;
  }

}
