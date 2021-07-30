package com.insta.hms.documents;

import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.stereotype.Service;

@Service
public class PatientHvfDocValuesServices {

  @LazyAutowired
  private PatientHvfDocValuesRepository hvfDocRepository;

  public Integer delete(String key, Object value) {
    return hvfDocRepository.delete(key, value);
  }

}
