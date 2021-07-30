/**
 *
 */
package com.insta.hms.emr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author pragna.p
 *
 */
@SuppressWarnings("serial")
public class InstaComparator implements Comparator<EMRDoc>, Serializable {
	
	static Logger log = LoggerFactory.getLogger(InstaComparator.class);
    public int compare(EMRDoc o1, EMRDoc o2) {
    	if (o1 == null && o2 == null) {
    		return 0;
    	}

        if (o1 != null && o2 != null && o1.getDate() == null && o2.getDate() == null) {
        	return 0;
        }

    	if (o1 == null || o1.getDate() == null) {
    		return -1;
    	}
            
        if (o2 == null || o2.getDate() == null) {
        	return 1;
        }    
        
        int result = 0;
        if ( o1.getDate().equals(o2.getDate()) ) {
        	return 0;
        }
        
        result = o1.getDate().compareTo(o2.getDate()) < 0 ? -1 : 1;
        
        return result;
    }
}
