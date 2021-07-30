package com.insta.hms.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * The Class SqlDateSerializer.
 *
 * @author aditya
 */
public class SqlDateSerializer extends StdSerializer<Date> {

  /**
   * Instantiates a new sql date serializer.
   *
   * @param class1 the class 1
   */
  protected SqlDateSerializer(Class<Date> class1) {
    super(class1);
  }

  /** default version id. */
  private static final long serialVersionUID = 1L;

  /*
   * (non-Javadoc)
   * 
   * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize( java.lang.Object,
   * com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
   */
  @Override
  public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException, JsonProcessingException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    String date = dateFormat.format(value);
    gen.writeString(date);
  }
}
