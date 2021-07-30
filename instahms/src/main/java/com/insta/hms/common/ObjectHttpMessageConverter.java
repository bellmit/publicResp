package com.insta.hms.common;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Converts HTTP requests with bodies that are application/x-www-form-urlencoded or
 * multipart/form-data to an Object annotated with
 * {@link org.springframework.web.bind.annotation.RequestBody} in the the handler method. author -
 * tanmay.k
 */
public class ObjectHttpMessageConverter implements HttpMessageConverter<Object> {

  /** The form http message converter. */
  private final FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();

  /** The object mapper. */
  private final ObjectMapper objectMapper = new ObjectMapper();

  /** The Constant LINKED_MULTI_VALUE_MAP. */
  private static final LinkedMultiValueMap<String, String> LINKED_MULTI_VALUE_MAP =
      new LinkedMultiValueMap<String, String>();

  /** The Constant LINKED_MULTI_VALUE_MAP_CLASS. */
  private static final Class<? extends MultiValueMap<String, ?>> LINKED_MULTI_VALUE_MAP_CLASS =
      (Class<? extends MultiValueMap<String, ?>>) LINKED_MULTI_VALUE_MAP
      .getClass();

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.http.converter.HttpMessageConverter#canRead(java.lang .Class,
   * org.springframework.http.MediaType)
   */
  @Override
  public boolean canRead(Class clazz, MediaType mediaType) {
    return objectMapper.canSerialize(clazz)
        && formHttpMessageConverter.canRead(MultiValueMap.class, mediaType);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.http.converter.HttpMessageConverter#canWrite(java. lang.Class,
   * org.springframework.http.MediaType)
   */
  @Override
  public boolean canWrite(Class clazz, MediaType mediaType) {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.http.converter.HttpMessageConverter# getSupportedMediaTypes()
   */
  @Override
  public List<MediaType> getSupportedMediaTypes() {
    return formHttpMessageConverter.getSupportedMediaTypes();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.http.converter.HttpMessageConverter#read(java.lang. Class,
   * org.springframework.http.HttpInputMessage)
   */
  @Override
  public Object read(Class clazz, HttpInputMessage inputMessage)
      throws IOException, HttpMessageNotReadableException {
    Map<String, String> input = formHttpMessageConverter
        .read(LINKED_MULTI_VALUE_MAP_CLASS, inputMessage).toSingleValueMap();
    return objectMapper.convertValue(input, clazz);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.http.converter.HttpMessageConverter#write(java.lang. Object,
   * org.springframework.http.MediaType, org.springframework.http.HttpOutputMessage)
   */
  @Override
  public void write(Object obj, MediaType contentType, HttpOutputMessage outputMessage)
      throws UnsupportedOperationException {
    throw new UnsupportedOperationException("");
  }
}