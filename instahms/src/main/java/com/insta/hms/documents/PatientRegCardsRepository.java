package com.insta.hms.documents;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class PatientRegCardsRepository extends GenericRepository {

  public PatientRegCardsRepository() {
    super("patient_registration_cards");
    // TODO Auto-generated constructor stub
  }

}
