package com.insta.hms.integration.configuration;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class HIEEventsRepository extends GenericRepository {

  public HIEEventsRepository() {
   super("hie_events");
  }
}
