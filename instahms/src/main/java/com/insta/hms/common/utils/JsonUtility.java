package com.insta.hms.common.utils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Map;

public class JsonUtility {

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final Logger log = LoggerFactory.getLogger(JsonUtility.class);
  
  /**
   * Converts JSON to Key-Object Map.
   * @param str Input JSON as String
   * @return Key-Object Map
   */
  public static Map<String,Object> toObjectMap(String str) {
    try {
      return mapper.readValue(str, new TypeReference<Map<String, Object>>(){});
    } catch (JsonGenerationException exception) {
      log.error("JsonGenerationException: ", exception);
    } catch (JsonMappingException exception) {
      log.error("JsonMappingException: ", exception);
    } catch (IOException exception) {
      log.error("IOException: ", exception);
    }
    return null;
  }

  /**
   * Converts JSON to Key-Object Map.
   * @param stream Input JSON as InputStream
   * @return Key-Object Map
   */
  public static Map<String,Object> toObjectMap(InputStream stream)
      throws JsonParseException, JsonMappingException, IOException {
    try {
      return mapper.readValue(stream, new TypeReference<Map<String, Object>>(){});
    } catch (JsonGenerationException exception) {
      log.error("JsonGenerationException: ", exception);
    } catch (JsonMappingException exception) {
      log.error("JsonMappingException: ", exception);
    } catch (IOException exception) {
      log.error("IOException: ", exception);
    }
    return null;
  }

  /**
   * Converts JSON to Key-Object Map.
   * @param reader Input JSON as Reader
   * @return Key-Object Map
   */
  public static Map<String,Object> toObjectMap(Reader reader) {
    try {
      return mapper.readValue(reader, new TypeReference<Map<String, Object>>(){});
    } catch (JsonGenerationException exception) {
      log.error("JsonGenerationException: ", exception);
    } catch (JsonMappingException exception) {
      log.error("JsonMappingException: ", exception);
    } catch (IOException exception) {
      log.error("IOException: ", exception);
    }
    return null;
  }

  /**
   * Converts JSON to List of strings.
   * @param str Input JSON as String/InputStream
   * @return List of strings
   * @throws JsonParseException on parsing issues with json string
   * @throws JsonMappingException on failure to map to attributes in class
   * @throws IOException failure to perform IO
   */
  public static List<String> toStringList(String str) {
    try {
      return mapper.readValue(str, new TypeReference<List<String>>(){});
    } catch (JsonGenerationException exception) {
      log.error("JsonGenerationException: ", exception);
    } catch (JsonMappingException exception) {
      log.error("JsonMappingException: ", exception);
    } catch (IOException exception) {
      log.error("IOException: ", exception);
    }
    return null;
  }

  /**
   * Convert to JSON String.
   * @param obj Any Object
   * @return JSON String
   * @throws JsonProcessingException on failure to convert to string
   */
  public static String toJson(Object obj) {
    try {
      return mapper.writeValueAsString(obj);
    } catch (JsonProcessingException exception) {
      log.error("JsonProcessingException: ", exception);
    }
    return null;
  }

  /**
   * Convert to formatted JSON String.
   * @param obj Any Object
   * @return Formatted JSON String
   * @throws JsonProcessingException on failure to convert to string
   */
  public static String toPrettyJson(Object obj) throws JsonProcessingException {
    try {
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    } catch (JsonProcessingException exception) {
      log.error("JsonProcessingException: ", exception);
    }
    return null;
  }

  /**
   * Convert Object to Map.
   * @param obj Any Object
   * @return Map Object
   */
  public static Map<String,Object> objectToMap(Object obj) {
    return mapper.convertValue(obj, Map.class);
  }

}