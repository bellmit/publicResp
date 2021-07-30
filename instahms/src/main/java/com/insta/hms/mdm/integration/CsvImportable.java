package com.insta.hms.mdm.integration;

import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public interface CsvImportable {
  public String importCsv(InputStreamReader csvStreamReader,
      Map<String, MultiValueMap<Object, Object>> feedback) throws IOException;
}
