package com.insta.hms.core.inventory.patientindent;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.inventory.URLRoute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Used for Patient Indent Related Operations.
 * 
 * @author Ashok Pal
 *
 */
@RestController
@RequestMapping(URLRoute.PATIENTINDENT)
public class PatientIndentController extends BaseRestController {
		static Logger logger = LoggerFactory.getLogger(PatientIndentController.class);
		
		@LazyAutowired
		private PatientIndentService patientIndentService;
		
		@IgnoreConfidentialFilters
		@RequestMapping(value = URLRoute.GET_ITEM_LIST, method = RequestMethod.GET)
		public List<Map> getEquivalentMedicinesList(HttpServletRequest request) throws Exception {
			
			List<Map> medDetails = null;
			String medicineName = request.getParameter("medicineName");
			String genericName = request.getParameter("genericName");
			String storeId = request.getParameter("storeId");
			String saleType=request.getParameter("saleType");
			/*
			 * if allstores is true: it ignores the storeid and search for equivalent medicines from all stores.
			 * which is used in op/ip consultation screen.
			 */
			Boolean allStores = new Boolean(request.getParameter("allStores"));
			medDetails = ConversionUtils.copyListDynaBeansToMap(patientIndentService.getEquivalentMedicinesList(medicineName, genericName, storeId, allStores, saleType));
			return medDetails;
		}
}
