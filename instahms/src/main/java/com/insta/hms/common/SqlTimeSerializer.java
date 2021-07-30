package com.insta.hms.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;

/**
 * The Class SqlTimeSerializer.
 */
public class SqlTimeSerializer extends StdSerializer<Time> {

  /** Default serial version UID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new sql time serializer.
   *
   * @param time the time
   */
  protected SqlTimeSerializer(Class<Time> time) {
    super(time);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize( java.lang.Object,
   * com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
   */
  @Override
  public void serialize(Time value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    String time = timeFormat.format(value);
    gen.writeString(time);
  }

}
