package com.insta.hms.mdm;

public interface Converter<S, T> {

  T convert(S source);

}
