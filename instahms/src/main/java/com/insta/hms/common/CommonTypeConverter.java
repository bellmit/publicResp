package com.insta.hms.common;

import org.apache.commons.beanutils.Converter;

/**
 * The Class CommonTypeConverter.
 *
 * @author krishnat
 */
public class CommonTypeConverter implements Converter {

  /** The converter. */
  Converter converter = null;

  /**
   * Instantiates a new common type converter.
   *
   * @param converter the converter
   */
  public CommonTypeConverter(Converter converter) {
    this.converter = converter;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.beanutils.Converter#convert(java.lang.Class, java.lang.Object)
   */
  @Override
  public Object convert(Class clazz, Object value) {
    if (value == null) {
      return null;
    }
    // if the target type is not a string.class and the value that is to be converted is empty
    // string then return null.
    // when adding a new row to grid, in jsp way we were following a standard everywhere that the
    // primary key will have '_'
    // if the primary key is a character varying in the database there is no problem, but if the
    // primary key is a number,
    // then its failed to convert to number. changing approach is huge so to get avoid that use case
    // returning null for '_' as earlier.
    Class targetType = clazz;
    if ((value.equals("") || value.equals("_")) && targetType != null
        && !targetType.equals(String.class)) {
      return null;
    }

    return converter.convert(clazz, value);
  }

}
