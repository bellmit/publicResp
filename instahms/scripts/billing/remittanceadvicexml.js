function onChangeRAInsuranceCompany() {
	
	var insCompObj	= document.getElementById('insurance_co_id');
	var tpaObj		= document.getElementById('tpa_id');

	var insCompId	= insCompObj.value;
	var tpaId		= tpaObj.value;

	if (insCompId != '') {
		var insCompTpaDetails = filterList(companyTpaList, 'insurance_co_id', insCompId);

		if (empty(insCompTpaDetails))
			loadSelectBox(tpaObj, xmlTpaListJSON, 'tpa_name', 'tpa_id' , '(All)');
		else
			loadSelectBox(tpaObj, insCompTpaDetails, 'tpa_name', 'tpa_id' , '(All)');

		sortDropDown(insCompObj);
		sortDropDown(tpaObj);

		setSelectedIndex(tpaObj, tpaId);
		setSelectedIndex(insCompObj, insCompId);

	}else {
		loadSelectBox(tpaObj, xmlTpaListJSON, 'tpa_name', 'tpa_id' , '(All)');
	}
}