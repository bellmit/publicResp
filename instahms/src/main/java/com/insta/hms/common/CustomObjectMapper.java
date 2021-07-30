package com.insta.hms.common;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

/**
 * The Class CustomObjectMapper.
 *
 * @author aditya
 */
public class CustomObjectMapper extends ObjectMapper {

  /**
   * Default Serial Version UID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Auto generated constructors.
   *
   * @param sessionFactoryBean the session factory bean
   */
  @Autowired
  public CustomObjectMapper(LocalSessionFactoryBean sessionFactoryBean) {
    super();
    SimpleModule module = new SimpleModule("dateModule");
    module.addSerializer(new SqlDateSerializer(java.sql.Date.class));
    module.addSerializer(new SqlTimeSerializer(java.sql.Time.class));
    module.addSerializer(new SqlTimeStampSerializer(java.sql.Timestamp.class));
    this.registerModule(module);

    // This is added to map hibernate models into JSON and not to fetch the LAZYliy fetched models.
    Hibernate4Module hbmModule = new Hibernate4Module(
        sessionFactoryBean.getConfiguration().buildMapping());
    hbmModule.enable(Hibernate4Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS);
    //To allow transient fields to be serialized and de-serialized
    hbmModule.disable(Hibernate4Module.Feature.USE_TRANSIENT_ANNOTATION);

    this.registerModule(hbmModule);
    configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
  }

  /**
   * Instantiates a new custom object mapper.
   *
   * @param jf the jf
   */
  public CustomObjectMapper(JsonFactory jf) {
    super(jf);
  }

  /**
   * Instantiates a new custom object mapper.
   *
   * @param src the src
   */
  public CustomObjectMapper(ObjectMapper src) {
    super(src);
  }

  /**
   * Instantiates a new custom object mapper.
   *
   * @param jf the jf
   * @param sp the sp
   * @param dc the dc
   */
  public CustomObjectMapper(JsonFactory jf, DefaultSerializerProvider sp,
      DefaultDeserializationContext dc) {
    super(jf, sp, dc);
  }
}
