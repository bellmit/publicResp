package com.insta.hms.core.clinical.patientactivities;

import com.insta.hms.common.GenericHibernateRepository;
import com.insta.hms.common.annotations.LazyAutowired;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

/**
 * The Class PatientActivitiesHibernateRepository.
 */
@Repository
public class PatientActivitiesHibernateRepository extends GenericHibernateRepository {

  /** The session factory. */
  @LazyAutowired
  private SessionFactory sessionFactory;

  /**
   * Save.
   *
   * @param entity the entity
   */
  public void save(PatientActivitiesModel entity) {
    Session session = this.sessionFactory.getCurrentSession();
    session.update(entity);
  }

  /**
   * Gets the.
   *
   * @param id the id
   * @return the patient activities model
   */
  public PatientActivitiesModel get(long id) {
    return (PatientActivitiesModel) this.sessionFactory.getCurrentSession()
        .load(PatientActivitiesModel.class, id);
  }
}
