package com.insta.hms.common;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * The Class JsonStringToMap.
 *
 * @author teja
 */

@Component
public class JsonStringToMap {

  /** The log. */
  private Logger log = LoggerFactory.getLogger(JsonStringToMap.class);

  /**
   * Convert.
   *
   * @param jsonString the json string
   * @return the map
   */
  public Map<String, Object> convert(String jsonString) {

    try {

      ObjectMapper mapper = new ObjectMapper();

      Map<String, Object> map = mapper.readValue(jsonString,
          new TypeReference<Map<String, Object>>() {
          });

      return map;
    } catch (JsonGenerationException exception) {
      log.error("JsonGenerationException: ", exception);
    } catch (JsonMappingException exception) {
      log.error("JsonMappingException: ", exception);
    } catch (IOException exception) {
      log.error("IOException: ", exception);
    }
    return null;
  }
}
