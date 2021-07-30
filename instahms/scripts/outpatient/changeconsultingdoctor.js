function init() {
	doctorAutoComplete();
}
function doctorAutoComplete() {
	localDs = new YAHOO.util.LocalDataSource(doctorlist, { queryMatchContains : true } );
	localDs.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
	localDs.responseSchema = {

		resultsList : "doctors",
		fields: [ {key : "doctor_name"},
				  {key: "doctor_id"},
				  {key: "dept_name"},
				  {key: "dept_id"}
				]
	};

	var autoComp = new YAHOO.widget.AutoComplete("modifiedDoctor", "doctorContainer", localDs);

	autoComp.prehightlightClassName = "yui-ac-prehighlight";
	autoComp.typeAhead = false;
	autoComp.useShadow = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 0;
	autoComp.maxResultsDisplayed = 20;
	autoComp.autoHighlight = true;
	autoComp.forceSelection = true;
	autoComp.animVert = false;
	autoComp.resultTypeList = false;
	var reArray = [];
	autoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var escapedComp = Insta.escape(sQuery);
		reArray[0] = new RegExp(escapedComp, 'i');
    	var det = highlight(oResultData.doctor_name, reArray);
    	det += "(" + oResultData.dept_name + ")";
    	return det;
    };

    var doctor_id;
	var itemSelectHandler = function(sType, aArgs) {
		doctor_id=aArgs[2].doctor_id;
		document.getElementById("modified_doctor_id").value =  aArgs[2].doctor_id;
		document.getElementById('modified_doctor_dept_id').value = aArgs[2].dept_id;
	};
	autoComp.itemSelectEvent.subscribe(itemSelectHandler);
}

function changeDoctor() {
	var doctorName = document.getElementById('modifiedDoctor').value;
	if (doctorName == '') {
		showMessage("js.outpatient.consultation.mgmt.changedoctor.mandatory");
		document.getElementById('modifiedDoctor').focus();
		return false;
	}

	document.changedoctorform.submit();
	return true;
}