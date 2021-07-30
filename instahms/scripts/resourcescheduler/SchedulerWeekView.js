var theaAutoComp = null;
var docAutoComp = null;
var resourceId ='';
var cen='';
function initResource() {
	if(category == 'DOC')
		initDoctor();
	else if (category == 'OPE')
		initTheatre();
}

function initDoctor(dept) {
	if (docAutoComp != null) {
		docAutoComp.destroy();
	}

	var docDeptNameArray = [];
	docDeptNameArray.length = jDocDeptNameList.length;

	for ( i=0 ; i< jDocDeptNameList.length; i++){
		var item = jDocDeptNameList[i];
		docDeptNameArray[i] = item["doctor_name"]+" ("+item["dept_name"]+")";
	}
	if(document.mainform.resource_name != null) {
		YAHOO.example.ACJSAddArray = new function() {
			var dataSource = new YAHOO.widget.DS_JSArray(docDeptNameArray);
			docAutoComp = new YAHOO.widget.AutoComplete('resource_name', 'resource_dropdown', dataSource);
			docAutoComp.maxResultsDisplayed = 10;
			docAutoComp.queryMatchContains = true;
			docAutoComp.allowBrowserAutocomplete = false;
			docAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			docAutoComp.typeAhead = false;
			docAutoComp.useShadow = false;
			docAutoComp.minQueryLength = 0;
			docAutoComp.forceSelection = false;
			docAutoComp.textboxBlurEvent.subscribe(function() {
			var docName = document.mainform.resource_name.value;
				if(docName == '') {
					document.resourceform.includeResources.value = '';
				}
			});

			docAutoComp.itemSelectEvent.subscribe(function() {
				var dName = document.mainform.resource_name.value;
				if(dName != '') {
					for ( var i=0 ; i< jDocDeptNameList.length; i++){
						if(dName == jDocDeptNameList[i]["doctor_name"]+" ("+jDocDeptNameList[i]["dept_name"]+")"){
							document.resourceform.includeResources.value = jDocDeptNameList[i]["doctor_id"];
							break;
						}
					}
					setResourceSchedule();
				}else{
					document.resourceform.includeResources.value = '';
				}
			});
		}
	}
}
function initTheatre(){

	var theaNameArray = [];
	   	theaNameArray.length = jTheatreNameList.length;
	for ( i=0 ; i< jTheatreNameList.length; i++){
		var item = jTheatreNameList[i];
		theaNameArray[i] = item["theatre_name"];

	}

	if(document.mainform.resource_name != null) {
		YAHOO.example.ACJSAddArray = new function() {
			var dataSource = new YAHOO.widget.DS_JSArray(theaNameArray);
			theaAutoComp = new YAHOO.widget.AutoComplete('resource_name', 'resource_dropdown', dataSource);
			theaAutoComp.maxResultsDisplayed = 10;
			theaAutoComp.queryMatchContains = true;
			theaAutoComp.allowBrowserAutocomplete = false;
			theaAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			theaAutoComp.typeAhead = false;
			theaAutoComp.useShadow = false;
			theaAutoComp.minQueryLength = 0;
			theaAutoComp.forceSelection = true;
			theaAutoComp.textboxBlurEvent.subscribe(function() {
			var docName = document.mainform.resource_name.value;
				if(docName == '') {
					document.resourceform.includeResources.value = '';
				}
			});

			theaAutoComp.itemSelectEvent.subscribe(function() {
				
				var dName = document.mainform.resource_name.value;
				if(dName != '') {
					for ( var i=0 ; i< jTheatreNameList.length; i++){
						if(dName == jTheatreNameList[i]["theatre_name"]){
							document.resourceform.includeResources.value = jTheatreNameList[i]["theatre_id"];
							break;
						}
					}
					setResourceSchedule();
				}else{
					document.resourceform.includeResources.value = '';
				}
			});
		}

	}
}

function setResourceSchedule() {
	var elmts = document.getElementsByName("resourceSchedule");
	for(var i=0;i<elmts.length;i++) {
		setSelectedIndex(elmts[i], document.resourceform.includeResources.value);
	}
	showSchedules();
}
//on change of center submit the page
function onResourceCenterChangeSubmit(obj) {
	document.resourceform.includeResources.value = '';
	document.mainform.includeResources.value = '';
	document.mainform.submit();	
}

function onCheckDoctor() {
	if (category == 'DOC') {
		if (roleId != 1 && roleId != 2) {
			if (docId != '') {
				if (isScheduled == 'false') {
					alert("User doctor is not marked as scheduled.");
					window.location.href=cpath+"/home.do?userId="+empUser+"&hospitalId="+cenName;
				} else {
					if (centerId != 0) {
						if (doctorsCount == 0 ){
							alert("User doctor is not mapped with this login center.");
							window.location.href=cpath+"/home.do?userId="+empUser+"&hospitalId="+cenName;
						}
					}
				}
			} 
		}
	}
}


function onSelectCenter() {
	var selectedCenter = document.getElementById("centerId").value;
	if (doctorCenters != null) {
		if (docId != '') {
			for (var i=0; i<doctorCenters.length; i++) {
				if(doctorCenters[i].center_id == selectedCenter && doctorCenters[i].status == 'A') {
					doctorsCount++;
				} 
			} 
			if (selectedCenter != 0) {
				if (doctorsCount == 0) {
					if (docId != '') {
						alert("Doctor is not mapped with the selected center.");
						document.mainform.centerId.value = 0;
						return false;
					}
				}
			}
		}
	}
}


