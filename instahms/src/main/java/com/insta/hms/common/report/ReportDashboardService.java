package com.insta.hms.common.report;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.common.utils.EnvironmentUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ReportDashboardService {

  @LazyAutowired
  RedisTemplate template;

  @LazyAutowired
  SessionService sessionService;

  private static Logger logger = LoggerFactory.getLogger(ReportDashboardService.class);

  private List<String> getAllKeys() {
    String keyTemplate = String.format("schema:%s;user:%s;uid:%s", RequestContext.getSchema(),
        sessionService.getSessionAttributes().get("userId"), "*");

    Set<String> redisKeys = template.keys(keyTemplate);
    // Store the keys in a List
    List<String> keysList = new ArrayList<>();
    Iterator<String> it = redisKeys.iterator();
    while (it.hasNext()) {
      String data = it.next();
      keysList.add(data);
    }

    return keysList;
  }

  /**
   * Gets status of all reports and the download paths if report generation is completed.
   * 
   * @return a list of list containing status,filename and download link of all reports
   */
  public List<Map<String, String>> getAllReports() {

    List<Map<String, String>> listValues = new ArrayList<>();
    for (String key : getAllKeys()) {
      Map<String, String> entry = new HashMap<>();
      // value is of the format status:<completed,queued,failed>;filename:<filename>;folder:<folder>
      entry.put("id", key);
      String value = (String) template.opsForValue().get(key);
      String[] tokenize = value.split(";");
      String[] status = tokenize[0].split(":");
      String[] fileName = tokenize[1].split(":");
      if (status[1].equals("completed") || status[1].equals("failed")) {
        entry.put("status", status[1]);
        String generatedAt = null;
        if (tokenize.length > 3) {
          generatedAt = tokenize[3].split(":")[1] + ":" + tokenize[3].split(":")[2] + ":"
              + tokenize[3].split(":")[3];
        }
        entry.put("creation_time", generatedAt);
        entry.put("file_name", fileName[1]);
      } else {
        entry.put("status", status[1]);
        entry.put("file_name", fileName[1]);
      }
      listValues.add(entry);
    }

    // sorting values based on creation time (descending)
    java.util.Collections.sort(listValues, new Comparator<Map<String, String>>() {
      @Override
      public int compare(Map<String, String> first, Map<String, String> second) {
        String firstCreationTime = first.get("creation_time");
        String secondCreationTime = second.get("creation_time");
        if (null == firstCreationTime && null == secondCreationTime) {
          return 0;
        }
        if (firstCreationTime == null) {
          return -1;
        }
        if (secondCreationTime == null) {
          return 1;
        }
        return secondCreationTime.compareTo(firstCreationTime);
      }
    });

    return listValues;

  }

  /**
   * Gets file path by Redis Key.
   *
   * @param redisKey
   *          report id for which status is to be returned
   * 
   * @return file path for given redis key
   */
  public String getFilePathForRedisKey(String redisKey) {
    String value = (String) template.opsForValue().get(redisKey);
    String[] tokenize = value.split(";");
    String generatedAt = null;
    String downloadPath = null;
    String[] folder = tokenize[2].split(":");
    String[] fileName = tokenize[1].split(":");
    String[] status = tokenize[0].split(":");
    if (tokenize.length > 3) { // check to support old redis keys
      generatedAt = tokenize[3].split(":")[1] + ":" + tokenize[3].split(":")[2] + ":"
          + tokenize[3].split(":")[3];
      String fileExtension = fileName[1].substring(fileName[1].lastIndexOf("."),
          fileName[1].length());
      String fileNamePart = fileName[1].substring(0, fileName[1].lastIndexOf("."));
      downloadPath = EnvironmentUtil.getTempDirectory() + "/" + folder[1] + "/"
          + fileName[1].split("\\.")[0] + generatedAt + "." + fileName[1].split("\\.")[1];
      if (status[1].equals("completed")) {
        downloadPath = EnvironmentUtil.getTempDirectory() + "/" + folder[1] + "/" + fileNamePart
            + generatedAt + fileExtension;
      } else {
        downloadPath = EnvironmentUtil.getTempDirectory() + "/" + folder[1] + "/" + fileNamePart
            + generatedAt + "-errors.txt";
      }
    } else {
      downloadPath = EnvironmentUtil.getTempDirectory() + "/" + folder[1] + "/" + fileName[1];
    }
    return downloadPath;
  }

  /**
   * Get status by Redis Key.
   *
   * @param redisKey
   *          report id for which status is to be returned
   *
   * @return file path for given redis key
   */

  public String getStatusForRedisKey(String redisKey) {
    String value = (String) template.opsForValue().get(redisKey);
    if (value == null) {
      return value;
    }
    String[] tokenize = value.split(";");
    String[] status = tokenize[0].split(":");
    return status[1];
  }

}