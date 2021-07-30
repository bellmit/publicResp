package com.insta.hms.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * The Class SqlTimeStampSerializer.
 *
 * @author aditya
 */
public class SqlTimeStampSerializer extends StdSerializer<Timestamp> {

  /**
   * Instantiates a new sql time stamp serializer.
   *
   * @param timestamp the timestamp
   */
  protected SqlTimeStampSerializer(Class<Timestamp> timestamp) {
    super(timestamp);
  }

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /*
   * (non-Javadoc)
   * 
   * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize( java.lang.Object,
   * com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
   */
  @Override
  public void serialize(Timestamp value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    SimpleDateFormat timeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    String timeStamp = timeFormat.format(value);
    gen.writeString(timeStamp);
  }

}
