function empty(obj) {
	if (obj == null || obj == undefined || obj == '') return true;
}

var insCompanyArray = null;
var tpaArray = null;
var categoryArray = null;
var planArray = null;

var selectOptn = new Array("(All)", "");

// Initialize Ins. comp, TPA, Plan type i.e category, Plan Arrays.
function initArrays() {
	insCompanyArray	= new Array();
	tpaArray		= new Array();
	categoryArray	= new Array();
	planArray		= new Array();

	insCompanyArray[0] = selectOptn;
	tpaArray[0] = selectOptn;
	categoryArray[0] = selectOptn;
	planArray[0] = selectOptn;

	var i = 1;
   	for (var n=0; n<companyList.length; n++) {
    	insCompanyArray[i] = new Array(companyList[n].insurance_co_name, companyList[n].insurance_co_id);
     	i++;
   	}

   	var j = 1;
   	for (var n=0; n<tpaList.length; n++) {
    	tpaArray[j] = new Array(tpaList[n].tpa_name, tpaList[n].tpa_id);
     	j++;
   	}

	if (document.getElementById('category_id')) {
	   	var k = 1;
	   	for (var n=0; n<categoryList.length; n++) {
	    	categoryArray[k] = new Array(categoryList[n].category_name, categoryList[n].category_id);
	     	k++;
	   	}
	}

	if (document.getElementById('plan_id')) {
	   	var m = 1;
	   	for (var n=0; n<planList.length; n++) {
	    	planArray[m] = new Array(planList[n].plan_name, planList[n].plan_id);
	     	m++;
	   	}
   	}
}

function loadInsCompArray() {
	var insCompObj	= document.getElementById('insurance_co_id');
	var insCompId	= insCompObj.value;
	var len = 1;
	for (var n=0; n<insCompanyArray.length; n++) {
		insCompObj.options.length = len;
		insCompObj.options[len - 1] = new Option(insCompanyArray[n][0], insCompanyArray[n][1]);
		len++;
   	}
   	sortDropDown(insCompObj);
   	setSelectedIndex(insCompObj, insCompId);
}

function loadTpaArray() {
	var tpaObj		= document.getElementById('tpa_id');
	var tpaId		= tpaObj.value;
	var len = 1;
	for (var n=0; n<tpaArray.length; n++) {
		tpaObj.options.length = len;
		tpaObj.options[len - 1] = new Option(tpaArray[n][0], tpaArray[n][1]);
		len++;
   	}
	sortDropDown(tpaObj);
   	setSelectedIndex(tpaObj, tpaId);
}

function loadCategoryArray() {
	var catObj		= document.getElementById('category_id');
	if (catObj != null) {
		var catList		= getSelectedCategories();
		var len = 1;
		for (var n=0; n<categoryArray.length; n++) {
			catObj.options.length = len;
			catObj.options[len - 1] = new Option(categoryArray[n][0], categoryArray[n][1]);
			len++;
	   	}
	   	sortDropDown(catObj);
	   	setSelectedCategories(catList);
   	}
}

function loadPlanArray() {
	var planObj		= document.getElementById('plan_id');
	if (planObj != null) {
		var planId		= planObj.value;
		var len = 1;
		for (var n=0; n<planArray.length; n++) {
			planObj.options.length = len;
			planObj.options[len - 1] = new Option(planArray[n][0], planArray[n][1]);
			len++;
	   	}
	   	sortDropDown(planObj);
	   	setSelectedIndex(planObj, planId);
   	}
}

function setSelectedCategories(catList) {
	var catObj		= document.getElementById('category_id');
	for (var i=0;i<catObj.options.length;i++) {
		for (var j=0;j<catList.length;j++) {
			if (catList[j] == catObj.options[i].value) {
				catObj.options[i].selected = true;
			}
		}
	}
}

function getSelectedCategories() {
	var catObj		= document.getElementById('category_id');
	var selectedCategories = catObj.selectedOptions;

	if(selectedCategories){
		return Array.from(selectedCategories).map(selectedOption => selectedOption.value);
	}else{
		return [];
	}
}

function getCompanyTpaListAJAX(insCompId) {
	
	var ajaxobj = newXMLHttpRequest();
	var url = cpath
			+ '/billing/claimSubmissionsList.do?_method=getInsuranceCompanyTpaList'
			+ '&insurance_co_id=' + insCompId;
	
	ajaxobj.open("POST", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj && ajaxobj.readyState == 4
			&& (ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				return JSON.parse(ajaxobj.responseText);
	}
}

function onChangeInsuranceCompany() {
	var insCompObj	= document.getElementById('insurance_co_id');
	var tpaObj		= document.getElementById('tpa_id');
	var catObj		= document.getElementById('category_id');
	var planObj		= document.getElementById('plan_id');

	var insCompId	= insCompObj.value;
	var tpaId		= tpaObj.value;

	if (insCompId != '') {
		// get companyTpaList
		companyTpaList = getCompanyTpaListAJAX(insCompId);
		var insCompTpaDetails = filterList(companyTpaList, 'insurance_co_id', insCompId);

		if (empty(insCompTpaDetails))
			loadSelectBox(tpaObj, tpaList, 'tpa_name', 'tpa_id' , '(All)');
		else
			loadSelectBox(tpaObj, insCompTpaDetails, 'tpa_name', 'tpa_id' , '(All)');

		sortDropDown(insCompObj);
		sortDropDown(tpaObj);

		setSelectedIndex(tpaObj, tpaId);
		setSelectedIndex(insCompObj, insCompId);

		if (planObj != null && planObj.options) {
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
		if (tpaId == '')
			loadTpaArray();

		if (insCompId == '') {
			loadCategoryArray();

			if (planObj != null && planObj.options) {
				var optn = new Option("(All)", "");
				planObj.options.length = 1;
				planObj.options[0] = optn;
			}
		}
	}
}

function onChangeTPA() {
	var insCompObj	= document.getElementById('insurance_co_id');
	var tpaObj		= document.getElementById('tpa_id');
	var catObj		= document.getElementById('category_id');
	var planObj		= document.getElementById('plan_id');

	var insCompId	= insCompObj.value;
	var tpaId		= tpaObj.value;

	if (tpaId != '') {

		setSelectedIndex(insCompObj, insCompId);
		setSelectedIndex(tpaObj, tpaId);

	}else {
		if (insCompId == '') {
			loadTpaArray();
		}
	}
}

function onChangeInsuranceCategory() {
	var catObj		= document.getElementById('category_id');
	var planObj		= document.getElementById('plan_id');

	var catList		= getSelectedCategories();
	var categoryId	= catObj.value;
	var planId		= planObj.value;

	if (catList.length == 1) {
		var insurancePlanList = filterList(planList, 'category_id', categoryId);
		loadSelectBox(planObj, insurancePlanList, 'plan_name', 'plan_id', '(All)');
	}else if (catList.length > 1) {

		var len = 1;
		var optn = new Option("(All)", "");
		planObj.options.length = len;
		planObj.options[len-1] = optn;
		for (var i=0;i<catList.length;i++) {
			var insurancePlanList = filterList(planList, 'category_id', catList[i]);
			if (!empty(insurancePlanList)) {
				for (var n=0; n<insurancePlanList.length; n++) {
					len++;
					planObj.options.length = len;
					planObj.options[len - 1] = new Option(insurancePlanList[n].plan_name, insurancePlanList[n].plan_id);
		   		}
		   	}
		}

	} else{
		var optn = new Option("(All)", "");
		planObj.options.length = 1;
		planObj.options[0] = optn;
	}

	sortDropDown(planObj);
	setSelectedIndex(planObj, planId);
}

function sortDropDown(obj) {
	var objMultiple = obj.getAttribute("multiple");
	var isMultiple = (!empty(objMultiple) && objMultiple == "multiple");
	var objArr = new Array();
	if (!empty(obj)) {
		objArr = new Array();
		var objValue = obj.value;
		var i = 0;
    	for (var n=0; n<obj.options.length; n++) {
    		if (!empty(obj.options[n].value)) {
      			objArr[i] = new Array(obj.options[n].text, {text: obj.options[n].text, value: obj.options[n].value});
      			i++;
      		}
    	}
    	objArr.sort();

		var len = 1;
		var optn = new Option("(All)", "");
		obj.options.length = len;
		obj.options[len - 1] = optn;

		if (objArr.length > 0) {
	    	for (var n=0; n<objArr.length; n++) {
	    		var optn = new Option(objArr[n][1].text, objArr[n][1].value);
	    		if (isMultiple)
	    			optn.setAttribute("onmouseover", 'this.title="'+objArr[n][1].text+'"');
				len++;
				obj.options.length = len;
				obj.options[len - 1] = optn;
	    	}
		}
    	setSelectedIndex(obj, objValue);
    }
}