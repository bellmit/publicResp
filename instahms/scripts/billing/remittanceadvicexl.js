
function onChangeRAInsuranceCompany() {
	var insCompObj	= document.getElementById('insurance_co_id');
	var tpaObj		= document.getElementById('tpa_id');
	var catObj		= document.getElementById('category_id');
	var planObj		= document.getElementById('plan_id');

	var insCompId	= insCompObj.value;
	var tpaId		= tpaObj.value;

	if (insCompId != '') {
		var insCompTpaDetails = filterList(companyTpaList, 'insurance_co_id', insCompId);

		if (empty(insCompTpaDetails))
			loadSelectBox(tpaObj, xlTpaListJSON, 'tpa_name', 'tpa_id' , '(All)');
		else
			loadSelectBox(tpaObj, insCompTpaDetails, 'tpa_name', 'tpa_id' , '(All)');

		sortDropDown(insCompObj);
		sortDropDown(tpaObj);

		setSelectedIndex(tpaObj, tpaId);
		setSelectedIndex(insCompObj, insCompId);

		if (planObj != null) {
			var optn = new Option("(All)", "");
			planObj.options.length = 1;
			planObj.options[0] = optn;
		}

		if (catObj != null) {
			var catList		= getSelectedCategories();
			var insuranceCategoryList = filterList(categoryList, 'insurance_co_id', insCompId);
			loadSelectBox(catObj, insuranceCategoryList, 'category_name', 'category_id', '(All)');
			sortDropDown(catObj);
			setSelectedCategories(catList);
		}

	}else {
		loadSelectBox(tpaObj, xlTpaListJSON, 'tpa_name', 'tpa_id' , '(All)');
	}
}