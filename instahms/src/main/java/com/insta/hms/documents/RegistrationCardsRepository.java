package com.insta.hms.documents;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class RegistrationCardsRepository extends GenericRepository {

  public RegistrationCardsRepository() {
    super("registration_cards");
    // TODO Auto-generated constructor stub
  }

}
