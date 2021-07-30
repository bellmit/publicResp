package testng.utils;

import com.insta.hms.common.DynaBeanBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author teja
 *
 */
@Component
public class TestingUtils {

	public BasicDynaBean getGenericPreferences() {
		DynaBeanBuilder builder = new DynaBeanBuilder();
		builder.add("prescription_uses_stores");
		BasicDynaBean genprefs = builder.build();
		return genprefs;
	}
	
	public static BasicDynaBean getDummyBean(String ...properties){
		DynaBeanBuilder builder = new DynaBeanBuilder();
		if(properties != null)
			for(String property: properties){
				builder.add(property);
			}
		return builder.build();
	}

	public static BasicDynaBean getDummyBean(Map <String, Class> properties){
		DynaBeanBuilder builder = new DynaBeanBuilder();
		if(properties != null)
			for(String property: properties.keySet()){
				builder.add(property, properties.get(property));
			}
		return builder.build();
	}

}
