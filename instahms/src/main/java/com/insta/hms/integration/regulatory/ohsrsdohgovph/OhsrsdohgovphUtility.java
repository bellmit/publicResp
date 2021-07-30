package com.insta.hms.integration.regulatory.ohsrsdohgovph;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.utils.JsonUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.testng.collections.Lists;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Component
public class OhsrsdohgovphUtility {

  @LazyAutowired
  RedisTemplate redis;

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(OhsrsdohgovphUtility.class);
  
  private final String rendererJsonFile = this.getClass().getClassLoader()
      .getResource("ohsrs_renderer_meta.json").getFile();
  
  private static final String REPORT_GENERATION_STATUS_REDIS_KEY = 
      "schema:%s:ohsrsgovph:generate:%s:%s";

  private static final String REPORT_SUBMISSION_STATUS_REDIS_KEY = 
      "schema:%s:ohsrsgovph:submit:%s:%s";

  public int getLoggedInCenterId() {
    return RequestContext.getCenterId();    
  }

  public String getLoggedInUser() {
    return RequestContext.getUserName();    
  }

  public String getLoggedInSchema() {
    return RequestContext.getSchema();    
  }

  protected Map<String, OhsrsFunctionMeta> getOhsrsFunctionMetaMap() {
    Map<String, OhsrsFunctionMeta> functionMap = new HashMap<>();
    Map<String,Object> rendererMeta = getRendererMetaJson();
    for (Entry<String,Object> functionDefinition : rendererMeta.entrySet()) {
      functionMap.put(functionDefinition.getKey(),
          mapToOhsrsFunctionMeta((Map<String,Object>) functionDefinition.getValue()));
    }
    return functionMap;
  }
  
  public OhsrsFunctionMeta getOhsrsFunctionMeta(String ohsrsFunction) {
    return mapToOhsrsFunctionMeta((Map<String,Object>) getRendererMetaJson().get(ohsrsFunction));
  }
  
  private OhsrsFunctionMeta mapToOhsrsFunctionMeta(Map<String, Object> map) {
    OhsrsFunctionMeta func = new OhsrsFunctionMeta();
    func.setKey((String) map.get("key"));
    func.setLabel((String) map.get("label"));
    func.setUploadable((String) map.get("uploadable"));
    func.setRepresentation((String) map.get("representation"));
    func.setGroupBy((String) map.get("group_by"));
    List<Map<String,String>> mapFields = (List) map.get("fields");
    List<OhsrsFunctionFieldMeta> fields = new ArrayList<>();
    for (Map<String,String> mapField : mapFields) {
      fields.add(new OhsrsFunctionFieldMeta(
          mapField.get("key"), mapField.get("label"), mapField.get("data_type")));
    }
    func.setFields(fields);
    return func;
  }
  
  protected Map<String, Object> getRendererMetaJson() {
    String json = (String) redis.opsForValue().get("ohsrsgovph:renderermeta");
    if (json != null && !json.isEmpty()) {
      return JsonUtility.toObjectMap(json);
    }
    StringBuffer jsonBuffer = new StringBuffer();
    try (BufferedReader reader = new BufferedReader(new FileReader(rendererJsonFile))) {
      String line = reader.readLine();
      while (line != null) {
        jsonBuffer.append(line);
        // read next line
        line = reader.readLine();
      }
      json = jsonBuffer.toString();
      redis.opsForValue().set("ohsrsgovph:renderermeta", json);
    } catch (IOException ex) {
      logger.info("IO Error getting text");
    }
    return JsonUtility.toObjectMap(json);
  }
  
  protected String getRendererMetaRaw() {
    String json = (String) redis.opsForValue().get("ohsrsgovph:renderermeta");
    if (json != null && !json.isEmpty()) {
      return json;
    }
    StringBuffer jsonBuffer = new StringBuffer();
    try (BufferedReader reader = new BufferedReader(new FileReader(rendererJsonFile))) {
      String line = reader.readLine();
      while (line != null) {
        jsonBuffer.append(line);
        // read next line
        line = reader.readLine();
      }
      json = jsonBuffer.toString();
      redis.opsForValue().set("ohsrsgovph:renderermeta", json);
    } catch (IOException ex) {
      logger.info("IO Error getting text");
    }
    return json;
  }

  protected void updateReportGenerationStatus(int year, String status) {
    redis.opsForValue().set(String.format(REPORT_GENERATION_STATUS_REDIS_KEY, 
        getLoggedInSchema(), getLoggedInCenterId(), String.valueOf(year)), status);
  }
  
  protected String getReportGenerationStatus(int year) {
    String status = (String) redis.opsForValue().get(
        String.format(REPORT_GENERATION_STATUS_REDIS_KEY, getLoggedInSchema(),
            getLoggedInCenterId(), String.valueOf(year)));
    return status == null ? "" : status; 
  }

  protected void removeReportGenerationStatus(int year) {
    redis.delete(String.format(REPORT_GENERATION_STATUS_REDIS_KEY, 
        getLoggedInSchema(), getLoggedInCenterId(), String.valueOf(year)));
  }

  protected void updateReportSubmissionStatus(int year, String status) {
    redis.opsForValue().set(String.format(REPORT_SUBMISSION_STATUS_REDIS_KEY, 
        getLoggedInSchema(), getLoggedInCenterId(), String.valueOf(year)), status);
  }
  
  protected String getReportSubmissionStatus(int year) {
    String status = (String) redis.opsForValue().get(
        String.format(REPORT_SUBMISSION_STATUS_REDIS_KEY, getLoggedInSchema(),
            getLoggedInCenterId(), String.valueOf(year)));
    return status == null ? "" : status; 
  }

  protected void removeReportSubmissionStatus(int year) {
    redis.delete(String.format(REPORT_SUBMISSION_STATUS_REDIS_KEY, 
        getLoggedInSchema(), getLoggedInCenterId(), String.valueOf(year)));
  }
}
