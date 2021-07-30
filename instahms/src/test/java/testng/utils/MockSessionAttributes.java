package testng.utils;

import java.util.HashMap;
import java.util.Map;

public class MockSessionAttributes {
	
	public Map<String, Object> getMockSessionAttributes() {
		Map<String, Object> sessionAttributes = new HashMap<String, Object>();
		sessionAttributes.put("userId", "InstaAdmin");
		sessionAttributes.put("centerId", 1);
		sessionAttributes.put("roleId", 1);
		return sessionAttributes;
	}

}
