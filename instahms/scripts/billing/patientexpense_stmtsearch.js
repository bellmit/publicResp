var toolbar = 		{
		BillList : 			{	title: "Bill List",
								imageSrc: "icons/Edit.png",
								href: 'pages/BillDischarge/BillList.do?_method=getBills&mr_no@op=ico',
								onclick:null,
								description: "opene bill for view and edit",
				  			},
		SummaryPrint :		{
								title: "Exp Stmt Sum",
								imageSrc: "icons/Report.png",
								href: 'pages/BillDischarge/PatientExpenseStmtPrint.do?method=expenseStatement&detailed=SUM',
								target: "_blank",
								onclick : null,
								description: "Prints a Summary Expense Statement",
							},
		DetailedPrint :		{
								title: "Exp Stmt Detl",
								imageSrc: "icons/Report.png",
								href: 'pages/BillDischarge/PatientExpenseStmtPrint.do?method=expenseStatement&detailed=DET',
								target: "_blank",
								onclick: null,
								description: "Prints a detailed Expense Statement",
							},

		Order : 			{	title: "Order",
								imageSrc: "icons/Order.png",
								href: 'pages/ipservices/Ipservices.do?_method=getPrescriptionScreen',
								onclick: null,
								description: "Order test ,services operations etc..",
				  			},

		CancelOrder : 		{	title: "Cancel Order",
								imageSrc: "icons/Cancel.png",
								href: 'pages/ipservices/ipserviceView.do?method=getIPServicesPrescibeScreen',
								onclick: null,
								description: "cancel orders",
				  			}
};


function init(){
	autoCountry();
	autoState();
	autoCity();
	autoArea();
	initMrNoAutoComplete(cpath);
	createToolbar(toolbar);
}

function autoCountry() {
	var localDs = new YAHOO.util.LocalDataSource(countryList);
	localDs.responseSchema = {fields : ["COUNTRY_NAME"]};
	var auto = new YAHOO.widget.AutoComplete('country_name', 'country_dropdown', localDs);
	auto.allowBrowserAutocomplete = false;
	auto.typeAhead = true;
	auto.animVert = false;
	auto.minQueryLength = 0;
}

function autoState() {
	var localDs = new YAHOO.util.LocalDataSource(stateList);
	localDs.responseSchema = {fields : ["STATE_NAME"]};
	var auto = new YAHOO.widget.AutoComplete('statename', 'state_dropdown', localDs);
	auto.allowBrowserAutocomplete = false;
	auto.typeAhead = true;
	auto.animVert = false;
	auto.minQueryLength = 0;
}

function autoCity() {
	var localDs = new YAHOO.util.LocalDataSource(cityList);
	localDs.responseSchema = {fields : ["CITY_NAME"]};
	var auto = new YAHOO.widget.AutoComplete('cityname', 'city_dropdown', localDs);
	auto.allowBrowserAutocomplete = false;
	auto.typeAhead = true;
	auto.animVert = false;
	auto.minQueryLength = 0;
}

function autoArea() {
	var localDs = new YAHOO.util.LocalDataSource(areaList);
	localDs.responseSchema = {fields : ["PATIENT_AREA"]};
	var auto = new YAHOO.widget.AutoComplete('patient_area', 'area_dropdown', localDs);
	auto.allowBrowserAutocomplete = false;
	auto.typeAhead = true;
	auto.animVert = false;
	auto.minQueryLength = 0;
}

