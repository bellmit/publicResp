package com.insta.hms.integration.practo.allspark;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.common.utils.EnvironmentUtil;

import flexjson.JSONSerializer;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class AllsparkClient {

  @LazyAutowired
  private MessageUtil messageUtil;

  @LazyAutowired
  private RedisTemplate redis;

  private static final Logger logger = Logger.getLogger(AllsparkClient.class);

  private static final RestTemplate restTemplate = new RestTemplate();

  private static final URL supportHost = EnvironmentUtil.getInstaSupportHost();

  private static final JSONSerializer js = new JSONSerializer().exclude("class");

  private static final Pattern configPattern = Pattern
      .compile("^(.+)[\\s\\t]*=[\\s\\t]*\\\"(.+)\\\"$");

  private static final ObjectMapper objectMapper = new ObjectMapper();
  @LazyAutowired
  private SessionService sessionService;

  /**
   * Call a Allspark Support Endpoint that accepts POST HTTP Request.
   * @param endpoint Allspark Endpoint URI
   * @param requestParams Map containing parameters that needs to be sent as part of request body
   * @return Response returned by Allsaprk
   */
  public ResponseEntity<Map<String, Object>> callSupportPostAPI(String endpoint,
      Map<String, Object> requestParams) {
    URI supportEndpoint;
    try {
      supportEndpoint = new URI(supportHost.getProtocol(), null, supportHost.getHost(),
          supportHost.getPort(), endpoint, null, null);
    } catch (URISyntaxException ex) {
      return null;
    }

    Map<String, Object> session = sessionService.getSessionAttributes();

    Map<String, String> supportKeys = getSupportKeysMap();

    return doPost(supportEndpoint, js.deepSerialize(requestParams), supportKeys);
  }

  private ResponseEntity<Map<String, Object>> doPost(URI uri, String requestBody,
      Map<String, String> supportKeys) {
    LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

    headers.put("x-insta-signature",
        Arrays.asList(new String[] { signRequest(requestBody, supportKeys.get("signing_key")) }));
    headers.put("x-insta-host", Arrays.asList(new String[] { supportKeys.get("signing_host") }));
    headers.put("content-type", Arrays.asList(new String[] { "application/json" }));
    headers.put("accept", Arrays.asList(new String[] { "application/json" }));
    ResponseEntity<Map<String, Object>> response;
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.add("Content-Type", "application/json");
    try {
      ResponseEntity<String> inResponse = restTemplate.exchange(uri, HttpMethod.POST,
          new HttpEntity<String>(requestBody, headers), String.class);
      response = new ResponseEntity<Map<String, Object>>(toObjectMap(inResponse.getBody()),
          responseHeaders, inResponse.getStatusCode());
    } catch (HttpClientErrorException ex) {
      response = new ResponseEntity<Map<String, Object>>(toObjectMap(ex.getResponseBodyAsString()),
          responseHeaders, ex.getStatusCode());
    }
    return response;
  }

  private Map<String, Object> toObjectMap(String json) {
    try {
      return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
      });
    } catch (IOException ex) {
      return new HashMap<String, Object>();
    }
  }

  private String bytesToHex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte byt : bytes) {
      result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
    }
    return result.toString().toLowerCase();
  }

  private String signRequest(String data, String salt) {
    Mac sha256HMac;
    try {
      sha256HMac = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKey = new SecretKeySpec(salt.getBytes(), "HmacSHA256");
      sha256HMac.init(secretKey);
      byte[] hash = sha256HMac.doFinal(data.getBytes());
      String hexDigest = bytesToHex(hash);
      return Base64.encodeBase64String(hexDigest.getBytes());
    } catch (NoSuchAlgorithmException ex) {
      logger.error("Unsupported Algorithm: HmacSHA256");
    } catch (InvalidKeyException ex) {
      logger.error("Invalid salt supplied");
    }
    return "";
  }

  private Map<String, String> getSupportKeysMap() {
    Map<String, String> responseMap = (Map<String, String>) redis.opsForHash()
        .entries("practo:insta:supportkeys");
    if (responseMap == null || responseMap.isEmpty()) {
      String supportSaltFilePath = "/etc/practo/instahms/supportkeys";
      BufferedReader reader;
      responseMap = new HashMap<>();
      try {
        reader = new BufferedReader(new FileReader(supportSaltFilePath));
        String line = reader.readLine();
        while (line != null) {
          Matcher matcher = configPattern.matcher(line);
          if (matcher.matches()) {
            responseMap.put(matcher.group(1).trim(), matcher.group(2).trim());
          }
          // read next line
          line = reader.readLine();
        }
        reader.close();
        redis.opsForHash().putAll("practo:insta:supportkeys", responseMap);
      } catch (IOException ex) {
        logger.info("IO Error getting text");
      }
    }
    return responseMap;
  }

  /**
   * Attach session specific meta data to a request parameter map.
   * @param requestParameters Map containing parameters that needs to be sent 
   *                          as part of request body
   */
  public void attachSessionInfo(Map<String, Object> requestParameters) {
    Map<String, Object> session = sessionService.getSessionAttributes();
    requestParameters.put("schema", session.get("sesHospitalId"));
    requestParameters.put("build_version", messageUtil.getMessage("insta.software.version", null));
    requestParameters.put("username", session.get("userId"));
    requestParameters.put("role", session.get("roleName"));
    requestParameters.put("center", session.get("centerName"));
    requestParameters.put("center_id", session.get("centerId"));
  }
}
