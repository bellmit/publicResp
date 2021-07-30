package com.insta.hms.master.SponsorProcedureMaster;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

@Service
public class SponsorProcedureService extends MasterService {

	public SponsorProcedureService(SponsorProcedureRepository r, 
			SponsorProcedureValidator v) {
		super(r, v);
	}

}
