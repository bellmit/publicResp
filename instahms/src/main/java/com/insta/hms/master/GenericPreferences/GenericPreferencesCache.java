package com.insta.hms.master.GenericPreferences;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.HashMap;
import java.util.Map;

public class GenericPreferencesCache {

	public static final Map<String, BasicDynaBean> CACHEDPREFERENCESBEAN	= 
												new HashMap<String, BasicDynaBean>();
	
	public static final Map<String, BasicDynaBean> REGCACHEDPREFERENCESBEAN = new HashMap<String, BasicDynaBean>();
	
    /*
     * Map containing one DTO entry per schema (hospital). This is used to store
     * a cache of the DTO, so whenever anyone queries for the DTO, we just
     * return the same object. But when the preferences are saved, the cached
     * item is removed, so we go fetch it from the DB the next time.
     */
    public static Map<String, GenericPreferencesDTO> CACHEDPREFERENCESDTO     = new HashMap<String, GenericPreferencesDTO>();
    
    public static final Map<String, BasicDynaBean> IPCACHEDPREFERENCESBEAN = new HashMap<String, BasicDynaBean>();

}
