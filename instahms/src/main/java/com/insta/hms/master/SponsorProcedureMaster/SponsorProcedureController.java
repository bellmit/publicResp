package com.insta.hms.master.SponsorProcedureMaster;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(URLRoute.SPONSOR_PROCEDURE_PATH)
public class SponsorProcedureController extends MasterController {
	
	
	public SponsorProcedureController(SponsorProcedureService service) {
		super(service, MasterResponseRouter.SPONSOR_PROCEDURE_ROUTER);
	}
	
	@Override
	protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
		Map<String, List<BasicDynaBean>> refData = new HashMap<String, List<BasicDynaBean>>();
		refData.put("procedureNameList", getService().lookup(true));
		return refData;
	}	
}