var editDialog;
var editedRangeId ;
function getAddDialog(obj){
	var row = getThisRow(obj);
	var id = row.rowIndex - 1;
	document.getElementById("editedRow").value = id;
	if(obj.name != 'btnAddRange'){
		dialogState = 'Edit';
		editedRangeId = getIndexedValue("range_id", id);
		//loadSelectBox(document.addRange.eResultlabelId,resultLabels,"resultlabel","resultlabel_id",'-- Select --','');
		//document.addRange.eResultlabelId.value = getIndexedValue("resultlabel_id", id);
		document.addRange.eMinPatientAge.value = getIndexedValue("min_patient_age", id);
		document.addRange.eMaxPatientAge.value = getIndexedValue("max_patient_age", id);
		document.addRange.ePatientGender.value = getIndexedValue("patient_gender", id);
		document.addRange.eMinNormalValue.value = getIndexedValue("min_normal_value", id);
		document.addRange.eMaxNormalValue.value = getIndexedValue("max_normal_value", id);
		document.addRange.eMinCriticalValue.value = getIndexedValue("min_critical_value", id);
		document.addRange.eMaxCriticalValue.value = getIndexedValue("max_critical_value", id);
		document.addRange.eMinImprobableValue.value = getIndexedValue("min_improbable_value", id);
		document.addRange.eMaxImprobableValue.value = getIndexedValue("max_improbable_value", id);
		document.addRange.ePriority.value = getIndexedValue("priority", id);
		document.addRange.eRefernceRange.value = getIndexedValue("reference_range_txt", id);
		document.addRange.eApplicableToAll.checked = getIndexedValue("range_for_all", id) == 'Y';
		document.getElementById("eAgeUnit"+getIndexedValue("age_unit", id)).checked = true;
		disableFields(document.addRange.eApplicableToAll.checked,id,'Edit');
		document.addRange.editedRow.value = id;
		editDialog.cfg.setProperty("context", [row.cells[11], "tr", "br"], false);
	}else{
		clearDialogFields();
		disableFields(document.addRange.eApplicableToAll.checked,id,'Add');
		dialogState = 'Add'
	//	loadSelectBox(document.addRange.eResultlabelId,resultLabels,"resultlabel","resultlabel_id");
		editDialog.cfg.setProperty("context", [row.cells[0], "tr", "br"], false);
	}
	editDialog.show();
	document.addRange.ePriority.focus();
	return false;
}

function disableFields(applicableToAll,id,state){
	if( applicableToAll ){
		document.addRange.eMinPatientAge.value = 0;
		document.addRange.eMaxPatientAge.value = 999;
		document.addRange.ePatientGender.selectedIndex = 0;
		document.addRange.eMinPatientAge.disabled = true;
		document.addRange.eMaxPatientAge.disabled = true;
		document.addRange.ePatientGender.disabled = true;
	}else{
		if( state == 'Edit' ){
			document.addRange.eMinPatientAge.value = getIndexedValue("min_patient_age", id);
			document.addRange.eMaxPatientAge.value = getIndexedValue("max_patient_age", id);
		}
		document.addRange.eMinPatientAge.disabled = false;
		document.addRange.eMaxPatientAge.disabled = false;
		document.addRange.ePatientGender.disabled = false;
	}
}

function clearDialogFields(){
	editedRangeId = '';
	//loadSelectBox(document.addRange.eResultlabelId,resultLabels,"resultlabel","resultlabel_id","-- Select --","");
	//document.addRange.eResultlabelId.value = '';
	document.addRange.eMinPatientAge.value = '';
	document.addRange.eMaxPatientAge.value = '';
	document.addRange.ePatientGender.value = 'N';
	document.addRange.eMinNormalValue.value = '';
	document.addRange.eMaxNormalValue.value = '';
	document.addRange.eMinCriticalValue.value = '';
	document.addRange.eMaxCriticalValue.value = '';
	document.addRange.eMinImprobableValue.value = '';
	document.addRange.eMaxImprobableValue.value = '';
	document.addRange.ePriority.value = '';
	document.addRange.eRefernceRange.value = '';
	document.addRange.eApplicableToAll.checked = false;
	document.getElementById('eAgeUnitY').checked = true;
	document.addRange.editedRow.value = '';
}
function initDialog(){
	editDialog = new YAHOO.widget.Dialog("editDialog", {
					width:"800px",
					context :["", "tr", "br"],
					visible:false,
					modal:true,
					constraintoviewport:true,
				});
	editDialog.cfg.setProperty("context", ["myContextEl", "tr", "tl"]);
	editDialog.render();
}
function init(){
	initDialog();
}
function getEditableRow(i) {
	i = parseInt(i);
	var table = document.getElementById("resultsTable");
	return table.rows[i + 1];
}
function closeDialog() {
	editDialog.cancel();
}
function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(resultrangesform, name, index);
	if (obj)
		obj.value = value;
	return obj;
}
function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(resultrangesform, name, index);
	if (obj)
		return obj.value;
	else
		return null;
}
function onDialogOK(){
	if(checkDuplicate(document.addRange.eMaxPatientAge.value,
			document.addRange.eMinPatientAge.value,
			document.addRange.ePriority.value,document.addRange.ePatientGender.value)){
		if(dialogState == 'Add')
			addResultRange();
		else
		   editResultRange();
   	}
}
function editResultRange(){

	if(!validatePriority() || !validate())
		return false;

	var id = document.addRange.editedRow.value;
	var cell = 0;
	var row = getEditableRow(id);
	//setNodeText(row.cells[0], getSelText(document.addRange.eResultlabelId));
	//row.cells[0].appendChild(makeHidden('resultlabel_id', 'resultlabel_id', document.addRange.eResultlabelId.value));
	//row.cells[0].appendChild(makeHidden('resultlabel', 'resultlabel', getSelText(document.addRange.eResultlabelId)));
	setNodeText(row.cells[0], document.addRange.ePriority.value);
	row.cells[0].appendChild(makeHidden('priority', 'priority', document.addRange.ePriority.value));
	row.cells[0].appendChild(makeHidden('range_id', 'range_id',editedRangeId));

	setNodeText(row.cells[1], document.addRange.eApplicableToAll.checked ? "Yes" : "No");
	row.cells[1].appendChild(makeHidden('range_for_all', 'range_for_all', document.addRange.eApplicableToAll.checked ? "Y" : "N"));

	setNodeText(row.cells[2], document.addRange.eMinPatientAge.value);
	row.cells[2].appendChild(makeHidden('min_patient_age', 'min_patient_age', document.addRange.eMinPatientAge.value));

	var ageUnit = document.addRange.eAgeUnitY.checked ? 'Y' : 'D';
	setNodeText(row.cells[3], document.addRange.eMaxPatientAge.value+(ageUnit == 'Y' ?'Yrs' : 'Days' ));
	row.cells[3].appendChild(makeHidden('max_patient_age', 'max_patient_age', document.addRange.eMaxPatientAge.value));
	row.cells[3].appendChild(makeHidden('age_unit', 'age_unit', ageUnit));

	setNodeText(row.cells[4], document.addRange.ePatientGender.value == 'N'?'':getSelText(document.addRange.ePatientGender));
	row.cells[4].appendChild(makeHidden('patient_gender', 'patient_gender', document.addRange.ePatientGender.value));

	setNodeText(row.cells[5], document.addRange.eMinNormalValue.value);
	row.cells[5].appendChild(makeHidden('min_normal_value', 'min_normal_value', document.addRange.eMinNormalValue.value));

	setNodeText(row.cells[6], document.addRange.eMaxNormalValue.value);
	row.cells[6].appendChild(makeHidden('max_normal_value', 'max_normal_value', document.addRange.eMaxNormalValue.value));

	setNodeText(row.cells[7], document.addRange.eMinCriticalValue.value);
	row.cells[7].appendChild(makeHidden('min_critical_value', 'min_critical_value', document.addRange.eMinCriticalValue.value));

	setNodeText(row.cells[8], document.addRange.eMaxCriticalValue.value);
	row.cells[8].appendChild(makeHidden('max_critical_value', 'max_critical_value', document.addRange.eMaxCriticalValue.value));

	setNodeText(row.cells[9], document.addRange.eMinImprobableValue.value);
	row.cells[9].appendChild(makeHidden('min_improbable_value', 'min_improbable_value', document.addRange.eMinImprobableValue.value));

	setNodeText(row.cells[10], document.addRange.eMaxImprobableValue.value);
	row.cells[10].appendChild(makeHidden('max_improbable_value', 'max_improbable_value', document.addRange.eMaxImprobableValue.value));

	setNodeText(row.cells[11], document.addRange.eRefernceRange.value.length > 16
			? document.addRange.eRefernceRange.value.substring(0,16)+'...' : document.addRange.eRefernceRange.value);
	row.cells[11].setAttribute("title", document.addRange.eRefernceRange.value );
	setIndexedValue('reference_range_txt',id,document.addRange.eRefernceRange.value );

	editDialog.hide();

}
function validatePriority(){
	if(document.addRange.ePriority.value == 0){
		alert("Enter Valid Priority");
		document.addRange.ePriority.focus();
		return false;
	}
	return true;
}

function checkDuplicate(maxAge,minAge,priority,gender){
	var priorities = document.getElementsByName("priority");
	//var labelId = document.getElementsByName("resultlabel_id");
	var minPatAge = document.getElementsByName("min_patient_age");
	var maxPatAge = document.getElementsByName("max_patient_age");
	var editingRow = document.addRange.editedRow.value;
	var patGender = document.getElementsByName("patient_gender");
	for(var i = 0;i<priorities.length;i++){
		if(priorities[i].value == priority
			&&( editingRow == '' || editingRow != i )){
			alert("Duplicate Entry");
			return false;
		}

		if(patGender[i].value == gender
			&& maxPatAge[i].value == maxAge
			&& minPatAge[i].value == minAge
			&& editingRow == '' ){
				return confirm("Result with same Minimum age,Maximum Age,Gender is already existing,\nstill want to add?");
		}
	}
	return true;
}


function addResultRange(){
	var tabel = document.getElementById("resultsTable");
	if(!validatePriority() || !validate())
		return false;
	var row = insertResultRange(document.addRange.ePriority.value,dialogState,
				 document.addRange.eMinPatientAge.value,document.addRange.eMaxPatientAge.value);


	if(row == undefined)
		return false;
	//var cell = row.insertCell(-1);
	//setNodeText(cell, getSelText(document.addRange.eResultlabelId));
	//cell.appendChild(makeHidden('resultlabel_id', 'resultlabel_id', document.addRange.eResultlabelId.value));
	//cell.appendChild(makeHidden('resultlabel', 'resultlabel', getSelText(document.addRange.eResultlabelId)));

	cell = row.insertCell(-1);
	setNodeText(cell, document.addRange.ePriority.value);
	cell.appendChild(makeHidden('priority', 'priority', document.addRange.ePriority.value));
	cell.appendChild(makeHidden('range_id', 'range_id', ''));

	cell = row.insertCell(-1);
	setNodeText(cell, document.addRange.eApplicableToAll.checked ? "Yes" : "No");
	cell.appendChild(makeHidden('range_for_all', 'range_for_all', document.addRange.eApplicableToAll.checked ? "Y" : "N"));

	cell =  row.insertCell(-1);
	setNodeText(cell, document.addRange.eMinPatientAge.value);
	cell.appendChild(makeHidden('min_patient_age', 'min_patient_age', document.addRange.eMinPatientAge.value));

	cell = row.insertCell(-1);
	var ageUnit = document.addRange.eAgeUnitY.checked ? 'Y' : 'D';
	setNodeText(cell, document.addRange.eMaxPatientAge.value+(ageUnit == 'Y' ?'Yrs' : 'Days'));
	cell.appendChild(makeHidden('max_patient_age', 'max_patient_age', document.addRange.eMaxPatientAge.value));
	cell.appendChild(makeHidden('age_unit', 'age_unit', ageUnit));

	cell = row.insertCell(-1);
	setNodeText(cell, document.addRange.ePatientGender.value == 'N'?'':getSelText(document.addRange.ePatientGender));
	cell.appendChild(makeHidden('patient_gender', 'patient_gender', document.addRange.ePatientGender.value));

	cell = row.insertCell(-1);
	setNodeText(cell, document.addRange.eMinNormalValue.value);
	cell.appendChild(makeHidden('min_normal_value', 'min_normal_value', document.addRange.eMinNormalValue.value));

	cell = row.insertCell(-1);
	setNodeText(cell, document.addRange.eMaxNormalValue.value);
	cell.appendChild(makeHidden('max_normal_value', 'max_normal_value', document.addRange.eMaxNormalValue.value));

	cell = row.insertCell(-1);
	setNodeText(cell, document.addRange.eMinCriticalValue.value);
	cell.appendChild(makeHidden('min_critical_value', 'min_critical_value', document.addRange.eMinCriticalValue.value));

	cell = row.insertCell(-1);
	setNodeText(cell, document.addRange.eMaxCriticalValue.value);
	cell.appendChild(makeHidden('max_critical_value', 'max_critical_value', document.addRange.eMaxCriticalValue.value));

	cell = row.insertCell(-1);
	setNodeText(cell, document.addRange.eMinImprobableValue.value);
	cell.appendChild(makeHidden('min_improbable_value', 'min_improbable_value', document.addRange.eMinImprobableValue.value));

	cell = row.insertCell(-1);
	setNodeText(cell, document.addRange.eMaxImprobableValue.value);
	cell.appendChild(makeHidden('max_improbable_value', 'max_improbable_value', document.addRange.eMaxImprobableValue.value));

	cell = row.insertCell(-1);
	setNodeText(cell, document.addRange.eRefernceRange.value.length > 16
			? document.addRange.eRefernceRange.value.substring(0,16)+'...' : document.addRange.eRefernceRange.value);
	cell.setAttribute("title", document.addRange.eRefernceRange.value );
	cell.appendChild(makeHidden('reference_range_txt', 'reference_range_txt', document.addRange.eRefernceRange.value));

	cell = row.insertCell(-1);

	cell.innerHTML = '<a href="javascript:void(0)" name="btnEditRanges" id="btnEditRanges"'+
						'onclick="getAddDialog(this);"'+
						'title="Edit Result Ranges" >'+
						'<img src="'+cpath+'/icons/Edit.png" class="button" />';


	clearDialogFields();
	disableFields(document.addRange.eApplicableToAll.checked,row.rowIndex-1,'Add');
	dialogState = 'Add'
	editDialog.cfg.setProperty("context", [(tabel.rows[tabel.rows.length-1]).cells[11], "tr", "br"], false);
	editDialog.show();
	document.addRange.ePriority.focus();

}

function empty(obj) {
	if (obj == null || obj == '' || obj == undefined)
		return true
	else
		return false;
}
function insertResultRange(priority,dialogState,minAge,maxAge){
	var table = document.getElementById("resultsTable");
	var priorities = document.getElementsByName("priority");
	var index = -1;
	for (var i = 0; i<priorities.length; i++) {
		//var lResultlabelId = getIndexedValue("resultlabel_id", i);
		var firstPriority = document.getElementsByName("priority")[i];
		var secondPriority = document.getElementsByName("priority")[i+1];
		//var firstLabelId = document.getElementsByName("resultlabel_id")[i];
		//var secondLabelId = document.getElementsByName("resultlabel_id")[i+1];
		//var firstLabelId = document.getElementsByName("resultlabel_id")[i];
		//var secondLabelId = document.getElementsByName("resultlabel_id")[i+1];

		if(checkDuplicate(maxAge,minAge,priority)){
			if (empty(secondPriority)) {
				if (priority < firstPriority.value) {
					var row = document.createElement("tr");
					table.tBodies[0].insertBefore(row, table.rows[i+1]);
					return row;
				} else {
					row = table.insertRow(-1);
					return row;
				}
			} else {
				if (parseInt(priority) > parseInt(firstPriority.value) && parseInt(priority) < parseInt(secondPriority.value)) {
					var row = document.createElement("tr");
					table.tBodies[0].insertBefore(row, table.rows[i+2]);
					return row;
				}
			}
		}

	}
	return document.getElementById("resultsTable").insertRow(-1);
}
function getNextIndex(index){
	return index+1 ;
}

function compareMinMaxAge(obj){
	if(parseInt(document.addRange.eMinPatientAge.value) >= parseInt(document.addRange.eMaxPatientAge.value) ){
		alert("Patient min age can not exceed Patient max age");
		obj.value = '';
		obj.focus();
		return false;
	}
}

function validate(){

	//var resultLableId = document.addRange.eResultlabelId.value;
	var min_normal = document.addRange.eMinNormalValue.value;
	var min_critical = document.addRange.eMinCriticalValue.value;
	var min_improbable = document.addRange.eMinImprobableValue.value;
	var max_normal = document.addRange.eMaxNormalValue.value;
	var max_critical = document.addRange.eMaxCriticalValue.value;
	var max_improbable  = document.addRange.eMaxImprobableValue.value;

	if( parseInt(min_critical) > parseInt(min_normal)
				|| parseInt(max_critical) < parseInt(max_normal) ){
		alert(" Abnormal Range cannot fall within Normal Range ");
		return false;
	}else if( parseInt(min_improbable) > parseInt(min_critical)
				|| parseInt(max_improbable) < parseInt(max_critical)){
		alert(" Critical Range cannot fall within Abnormal Range ");
		return false;
	}

	if( ( min_normal == '' && min_critical != '' ) ||
		( max_normal == '' && max_critical != '' ) ||
		( (min_critical == '' || min_normal == '') && min_improbable != '' ) ||
		( (max_critical == '' || max_normal == '') && max_improbable != '' ) ){
			alert("Please enter values in the following order \n- Normal Range \n- Abnormal Range \n- Critical Range");
			return false;
		}
	if( parseInt(min_normal) > parseInt(max_normal) ){
		alert(" Min Normal can not be greater than Max Normal ");
		return false;
	}else if( parseInt(min_critical) > parseInt(max_critical) ){
		alert(" Min Abnormal can not be greater than Max Abnormal ");
		return false;
	}else if( parseInt(min_improbable) > parseInt(max_improbable) ){
		alert(" Min Critical can not be greater than Max Critical ");
		return false;
	}
	return true;
}