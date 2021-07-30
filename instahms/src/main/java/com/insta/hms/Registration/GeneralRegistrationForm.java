package com.insta.hms.Registration;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class GeneralRegistrationForm extends ActionForm{
	
	private static final long serialVersionUID = 1L;
	
	private FormFile patientPhoto;

	public FormFile getPatientPhoto() {
		return patientPhoto;
	}

	public void setPatientPhoto(FormFile patientPhoto) {
		this.patientPhoto = patientPhoto;
	}


	
}