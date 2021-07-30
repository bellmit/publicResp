package com.insta.hms.mdm.systemmessage;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PushService;
import com.insta.hms.common.annotations.LazyAutowired;

import com.insta.hms.mdm.MasterService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class SystemMessageService.
 */
@Service
public class SystemMessageService extends MasterService {

  /**
    * Instantiates a new system message service.
    *
    * @param repository the repository
    * @param validator the validator
    */
  public SystemMessageService(SystemMessageRepository repository, 
      SystemMessageValidator validator) {
    super(repository, validator);
  }

  /**
    * System messages.
    *
    * @return the list
    */
  public Map<String, Object> systemMessages(String operation) {
    Map<String, Object> map = new HashMap();
    map.put("messages", ConversionUtils.listBeanToListMap(((SystemMessageRepository) 
        getRepository()).getSysMessages()));
    map.put("operation", operation);
    return map;
  }
  
  @Override
  public Integer insert(BasicDynaBean bean) {
    int count = super.insert(bean);
    return count;
  }
  
  @Override
  public Integer update(BasicDynaBean bean) {
    Integer count = super.update(bean);
    return count;
  }
}
