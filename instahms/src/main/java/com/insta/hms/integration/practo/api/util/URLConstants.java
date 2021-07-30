package com.insta.hms.integration.practo.api.util;

import com.insta.hms.common.ConfigManager;

import java.util.Properties;

public class URLConstants {
  static Properties properites = null;

  static {
    properites = ConfigManager.getInstance().getProps();
  }

  public static final String PRACTO_INSTA_MAPPED_SERVICE = (String) properites
      .get("practo.insta.mapped.service");
  public static final String PRACTO_FABRIC_API_AUTH_TOKEN_NAME = (String) properites
      .get("practo.fabric.api.auth.token.name");
  public static final String PRACTO_FABRIC_API_AUTH_TOKEN_VALUE = (String) properites
      .get("practo.fabric.api.auth.token.value");

  public static final String PRACTO_FABRIC_API_CONTEXTPATH = (String) properites
      .get("practo.fabric.api.contextpath");
  public static final String PRACTO_FABRIC_API_CITIES = (String) properites
      .get("practo.fabric.api.cities");
  public static final String PRACTO_FABRIC_API_LOCALITIES = (String) properites
      .get("practo.fabric.api.localities");
  public static final String PRACTO_FABRIC_API_DOCTOR_SPECIALIZATIONS = (String) properites
      .get("practo.fabric.api.doctor.specializations");

  public static final String PRACTO_PRACTICE_MAPPING_GET_ENDPOINT = (String) properites
      .get("practo.practice.mapping.get.endpoint");

  public static final String PRACTO_PRACTICE_FABRIC_PUT_ENDPOINT = (String) properites
      .get("practo.practice.fabric.put.endpoint");

  public static final String PRACTO_DOCTOR_FABRIC_PUT_ENDPOINT = (String) properites
      .get("practo.doctor.fabric.put.endpoint");
  public static final String PRACTO_DOCTOR_MAPPING_GET_ENDPOINT = (String) properites
      .get("practo.doctor.mapping.get.endpoint");

  public static final String PRACTO_PRACTICE_DOCTOR_FABRIC_PUT_ENDPOINT = (String) properites
      .get("practo.practice.doctor.fabric.put.endpoint");
  public static final String PRACTO_PRACTICE_DOCTOR_MAPPING_GET_ENDPOINT = (String) properites
      .get("practo.practice.doctor.mapping.get.endpoint");

}
