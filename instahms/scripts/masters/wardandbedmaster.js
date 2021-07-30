
function addRow(){

	var selectedBedType = document.getElementById('bedType1').value;
	var count =  document.getElementById('noOfBedToAdd1').value;



	var tableObj = document.getElementById('prescriptionTable');
	var rowObj = tableObj.insertRow(-1);

	tdObj = rowObj.insertCell(-1);
	var obj = document.createElement('input');
	obj.type = 'text';
	obj.name = 'bedType';
	obj.value = selectedBedType;
	obj.size = '8';
	obj.readOnly = 'readOnly';
	tdObj.appendChild(obj);


	tdObj = rowObj.insertCell(-1);
	tdObj.innerHTML = '<input type="text" name="noOfBedToAdd" size="3" '+
				' class="number" onkeypress="return enterNumOnlyzeroToNine(event)"  value="'+count+'" >';
	//var obj = document.createElement('input');
	//obj.type = 'input';
	//obj.name = 'noOfBedToAdd';
	//obj.onkeypress = 'return enterNumOnlyzeroToNine(event)';
	//obj.class = 'number';
	//obj.size = '3';
	//tdObj.appendChild(obj);

}

function validateBedType(){
 	var form = document.WardAndBedMasterForm;
	if(form.bedType1.options.selectedIndex == 0){
		alert("Select the BedType");
		form.bedType1.focus();
		return false;
	}else if(form.noOfBedToAdd1.value == ''){
	 		alert("Enter the No Of Beds");
	 		form.noOfBedToAdd1.focus();
	 		return false;
	}else{
		addRow();
	}

}

function validate() {

 	if(document.forms[0].method.value == 'editWardDetails') {
 		return true;
 	}else {
		 	var form = document.WardAndBedMasterForm;
			 	if(form.wardName.value == '') {
			 		alert("Enter the Ward Name");
			 		form.wardName.focus();
			 		return false;
			 	}
			 	if(form.bedType1.options.selectedIndex == 0){
			 		alert("Select the BedType");
			 		form.bedType1.focus();
			 		return false;
			 	}
			 	if(form.noOfBedToAdd1.value == '') {
			 		alert("Enter the No Of Beds");
			 		form.noOfBedToAdd1.focus();
			 		return false;
			 	}
			 	if(document.getElementById("prescriptionTable").rows.length==1 ){
			 		alert("Please add the row");
			 		return false;
			 	}
			   if ( multiCenters ) {
			   		if ( document.getElementById("center_id").value == '' ) {
			   			alert("No Center is linked to this ward,please select any center");
			   			document.getElementById("center_id").focus();
			   			return false;
			   		}
			   }
		 }

}
	function init() {
		focus();
	}

	function focus(){
		if(document.forms[0].method.value=='insertNewWardDetails'){
			document.forms[0].wardName.focus();
		}else if(document.forms[0].method.value=='editWardDetails'){
			document.forms[0].description.focus();
		}
	}

	var ward = null;
	var object = null;

	function canBeInactivate(wardId, bedId, obj) {
		if (obj.value == 'I') {
			ward = wardId;
			object = obj;
		 	var request = null;
			//var isOccupied = null;
			var url = cpath+"/pages/masters/insta/admin/WardAndBedMasterAction.do?method=isOccupied&wardId="+wardId+"&bedId="+bedId;
			if (wardId == '' && bedId == '') {
				return true;
			}
			if (window.XMLHttpRequest)
				request = new XMLHttpRequest();
			else if (window.ActiveXObject)
				request = new ActiveXObject("MSXML2.XMLHTTP");
			getResponseHandlerText(request, validateForInactive, url);
		} else {
			return true;
		}

	}

	function validateForInactive(responseText) {
		var isOccupied = responseText;
		if (isOccupied == 'occupied') {
			if (ward != null && ward != '')
				alert("Some of the beds of this ward are occupied,cannot inactivate");
			else
				alert("Occupied Bed cannot be inactivated");
			document.getElementById(object.id).focus();
			document.getElementById(object.id).value='A';
			return false;
		} else {
			return true;
		}
	}

