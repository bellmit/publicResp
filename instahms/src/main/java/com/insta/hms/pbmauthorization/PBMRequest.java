/**
 *
 */
package com.insta.hms.pbmauthorization;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.List;
import java.util.Map;

/**
 * @author lakshmi
 *
 */
public class PBMRequest {

	BasicDynaBean pbmRequestBean;
	List diagnosis;
	List activities;
	Map  observationsMap;
	String attachment;


	public BasicDynaBean getPbmRequestBean() {
		return pbmRequestBean;
	}
	public void setPbmRequestBean(BasicDynaBean pbmRequestBean) {
		this.pbmRequestBean = pbmRequestBean;
	}
	public List getDiagnosis() {
		return diagnosis;
	}
	public void setDiagnosis(List diagnosis) {
		this.diagnosis = diagnosis;
	}
	public List getActivities() {
		return activities;
	}
	public void setActivities(List activities) {
		this.activities = activities;
	}
	public Map getObservationsMap() {
		return observationsMap;
	}
	public void setObservationsMap(Map observationsMap) {
		this.observationsMap = observationsMap;
	}
	public String getAttachment() {
		return attachment;
	}
	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}
}
