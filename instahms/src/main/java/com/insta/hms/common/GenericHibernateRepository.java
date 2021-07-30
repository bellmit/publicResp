package com.insta.hms.common;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.dialect.Dialect;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.sql.Connection;
import java.util.List;

import javax.transaction.Transactional;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericHibernateRepository.
 */
public class GenericHibernateRepository {
  
  /** The session factory. */
  @Autowired
  private SessionFactory sessionFactory;

  /**
   * Instantiates a new generic hibernate repository.
   */
  protected GenericHibernateRepository() {
  }

  /**
   * Gets the session factory.
   *
   * @return the session factory
   */
  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  /**
   * Sets the session factory.
   *
   * @param sessionFactory the new session factory
   */
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  // To be used when Transactional annotation is used in the service method.
  /**
   * Gets the session.
   *
   * @return the session
   */
  // No need of closing the session when this function is used to fetch the session.
  protected Session getSession() {
    return sessionFactory.getCurrentSession();
  }

  // To be used if Transactional is not used.
  /**
   * Creates the session.
   *
   * @return the session
   */
  // This session HAS to be closed by the user.
  public Session createSession() {
    return sessionFactory.openSession();
  }

  /**
   * Creates the session.
   *
   * @param con the con
   * @return the session
   */
  public Session createSession(Connection con) {
    return sessionFactory.withOptions().connection(con).openSession();
  }

  /**
   * Start transaction.
   *
   * @return the transaction
   */
  public Transaction startTransaction() {
    Session session = getSession();
    return session.beginTransaction();
  }

  /**
   * Gets the transaction.
   *
   * @return the transaction
   */
  public Transaction getTransaction() {
    Session session = getSession();
    return session.getTransaction();
  }

  /**
   * Rollback transaction.
   */
  public void rollbackTransaction() {
    Session session = getSession();
    session.getTransaction().rollback();
  }

  /**
   * Execute hql query.
   *
   * @param hqlQuery the hql query
   * @return the list
   */
  protected List executeHqlQuery(String hqlQuery) {
    Session session = getSession();
    return session.createQuery(hqlQuery).list();
  }

  /**
   * Execute hql query.
   *
   * @param hqlQry the hql qry
   * @param values the values
   * @return the list
   */
  public List executeHqlQuery(String hqlQry, Object[] values) {
    Session session = getSession();
    Query query = session.createQuery(hqlQry);
    int index = 0;
    for (Object val : values) {
      query.setParameter(index++, val);
    }
    return query.list();
  }

  /**
   * Execute hql query.
   *
   * @param hqlQry the hql qry
   * @param values the values
   * @param maxResults the max results
   * @return the list
   */
  protected List executeHqlQuery(String hqlQry, Object[] values, Integer maxResults) {
    Session session = getSession();
    Query query = session.createQuery(hqlQry);
    query.setMaxResults(maxResults);
    int index = 0;
    for (Object val : values) {
      query.setParameter(index++, val);
    }
    return query.list();
  }

  /**
   * Execute query.
   *
   * @param sqlQuery the sql query
   * @return the list
   */
  protected List executeQuery(String sqlQuery) {
    Session session = getSession();
    return session.createSQLQuery(sqlQuery).list();
  }

  /**
   * Execute query.
   *
   * @param sqlQry the sql qry
   * @param values the values
   * @return the list
   */
  protected List executeQuery(String sqlQry, Object[] values) {
    Session session = getSession();
    SQLQuery query = session.createSQLQuery(sqlQry);
    int index = 0;
    for (Object val : values) {
      query.setParameter(index++, val);
    }
    return query.list();
  }

  /**
   * Execute update query.
   *
   * @param sqlQuery the sql query
   * @return the int
   */
  public int executeUpdateQuery(String sqlQuery) {
    Session session = getSession();
    return session.createSQLQuery(sqlQuery).executeUpdate();
  }

  /**
   * Execute update query.
   *
   * @param sqlQry the sql qry
   * @param values the values
   * @return the int
   */
  protected int executeUpdateQuery(String sqlQry, Object[] values) {
    Session session = getSession();
    SQLQuery query = session.createSQLQuery(sqlQry);
    int index = 0;
    for (Object val : values) {
      query.setParameter(index++, val);
    }
    return query.executeUpdate();
  }

  /**
   * Save.
   *
   * @param entity the entity
   * @return the serializable
   */
  public Serializable save(Object entity) {
    Session session = getSession();
    return session.save(entity);
  }

  /**
   * Persist.
   *
   * @param entity the entity
   */
  @Transactional
  public void persist(Object entity) {
    Session session = getSession();
    session.persist(entity);
  }

  /**
   * Gets the.
   *
   * @param clazz the clazz
   * @param key the key
   * @return the object
   */
  @Transactional
  public Object get(Class clazz, Serializable key) {
    Session session = getSession();
    return session.get(clazz, key);
  }

  /**
   * Update.
   *
   * @param entity the entity
   */
  @Transactional
  public void update(Object entity) {
    Session session = getSession();
    session.update(entity);
  }

  /**
   * Load.
   *
   * @param clazz the clazz
   * @param key the key
   * @return the object
   */
  public Object load(Class clazz, Serializable key) {
    Session session = getSession();
    return session.load(clazz, key);
  }

  /**
   * Batch insert.
   *
   * @param models the models
   */
  public void batchInsert(List<?> models) {
    Session session = getSession();
    int batchSize = getDefaultBatchSize();
    for (int i = 0; i < models.size(); i++) {
      if (i % batchSize == 0 && i > 0 && batchSize > 0) {
        session.flush();
        session.clear();
      }
      session.persist(models.get(i));
    }
    session.flush();
  }

  /**
   * Gets the default batch size.
   *
   * @return the default batch size
   */
  protected int getDefaultBatchSize() {
    return Integer.valueOf(Dialect.DEFAULT_BATCH_SIZE);
  }

  /**
   * Flush.
   */
  public void flush() {
    getSession().flush();
  }

}
