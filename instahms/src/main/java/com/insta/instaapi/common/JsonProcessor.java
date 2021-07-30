package com.insta.instaapi.common;

import flexjson.JSONSerializer;
import flexjson.transformer.AbstractTransformer;

/*
 * RC API : This class should extend JSONSerializer and call exclude and transform methods in the 
 * constructor Otherwise could lead to programmer errors - of using the original JSONSerializer.
*/

public class JsonProcessor {

  static JSONSerializer js = new JSONSerializer().exclude("class").transform(new NullConverter(),
      void.class);

  public static JSONSerializer getJSONParser() {
    return js;
  }

  private static class NullConverter extends AbstractTransformer {
    @Override
    public void transform(Object arg0) {
      // TODO Auto-generated method stub
      getContext().writeQuoted("");
    }
  }
}
