package com.insta.hms.batchjob.pushevent;

import com.insta.hms.common.annotations.EventSub;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

@Component
public class EventClassMapInitializer {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  private HashMap<String, ArrayList<Class>> eventClassMap = new HashMap<>();

  @SuppressWarnings("unchecked")
  @Autowired
  private void setEventClassMap() {
    Reflections ref = new Reflections("com.insta.hms.batchjob.pushevent");
    for (Class cl : ref.getTypesAnnotatedWith(EventSub.class)) {
      try {
        Field field = cl.getField("subscribedEvents");
        ArrayList<String> eventList = (ArrayList<String>) field.get(cl.newInstance());
        for (String event : eventList) {
          if (eventClassMap.containsKey(event)) {
            eventClassMap.get(event).add(cl);
          } else {
            ArrayList<Class> classes = new ArrayList<>();
            classes.add(cl);
            eventClassMap.put(event, classes);
          }
        }

      } catch (NoSuchFieldException | SecurityException | IllegalAccessException
          | IllegalArgumentException | InstantiationException exp) {
        logger.error("Exception occured : " + exp.getStackTrace());
      }
    }
  }

  public HashMap<String, ArrayList<Class>> getEventClassMap() {
    return eventClassMap;
  }

}
