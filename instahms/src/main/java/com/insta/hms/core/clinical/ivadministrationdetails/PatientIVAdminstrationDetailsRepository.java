package com.insta.hms.core.clinical.ivadministrationdetails;

import com.insta.hms.common.GenericHibernateRepository;
import com.insta.hms.common.annotations.LazyAutowired;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository
public class PatientIVAdminstrationDetailsRepository extends GenericHibernateRepository {
  
  @LazyAutowired
  private SessionFactory sessionFactory;
  
  
  /**
   * Save.
   * @param patientIVAdminstrationDetails Object to persist
   */
  public  void save(PatientIVAdminstrationDetails patientIVAdminstrationDetails) {
    Session session = this.sessionFactory.getCurrentSession();
    session.save(patientIVAdminstrationDetails);
  }

}
