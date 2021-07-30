package com.insta.hms.outpatient;

import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.GenericDAO;

public class PhysicianFormValuesDAO extends GenericDAO {

  DoctorConsultationDAO consultDao = new DoctorConsultationDAO();
  VisitDetailsDAO visitDAO = new VisitDetailsDAO();

  public PhysicianFormValuesDAO() {
    super("patient_physician_form_values");
  }
}
