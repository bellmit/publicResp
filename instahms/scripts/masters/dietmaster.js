var toolbar = {
	Editmeal: {
		title: 'Edit Meal',
		imageSrc: 'icons/Edit.png',
		href: '/dietary/DietaryMaster.do?_method=show'
	},

	EditCharge: {
		title: 'Edit Charges',
		imageSrc: 'icons/Edit.png',
		href: '/dietary/DietaryMaster.do?_method=editCharges'
	}

}

var ajaxDateRange = null;
function init(){
	initDoctorAutoComplete();
	initMealAutoComplete();
	clearFields();
}

function validateTime(timeField, index) {
	var strTime = timeField.value;
	var timePattern = /[0-9]:[0-9]/;
	var regExp = new RegExp(timePattern);
	if (strTime == '') {
		return true;
	}
	if (regExp.test(strTime)) {
		var strHours = strTime.split(':')[0];
		var strMinutes = strTime.split(':')[1];
		if (!isInteger(strHours)) {
			alert("Incorrect time format : hour is not a number");
			timeField.focus();
			return false;
		}
		if (!isInteger(strMinutes)) {
			alert("Incorrect time format : minute is not a number");
			timeField.focus();
			return false;
		}
		if ((parseInt(strHours) > 23) || (parseInt(strHours) < 0)) {
			alert("Incorrect hour : please enter 0-23 for hour");
			timeField.focus();
			return false;
		}
		if ((parseInt(strMinutes) > 59) || (parseInt(strMinutes) < 0)) {
			alert("Incorrect minute : please enter 0-59 for minute");
			timeField.focus();
			return false;
		}
		if(strMinutes.length !=2){
			alert("incorrect minutes please enter 2 digit minuts");
			return false;
		}
		if (strHours.length !=2){
			alert("incorect hours please enter 2 digit hours");
			return false;
		}
	} else {
		alert("Incorrect time format : please enter HH:MI");
		timeField.focus();
		return false;
	}
	return true;
}

function changeElsColor(index)
{
	var markRowDelete=document.getElementById("delete"+index).value=='N'?'Y':'N';
	document.getElementById("delete"+index).value=document.getElementById("delete"+index).value=='N'?'Y':'N';

	if(markRowDelete=='Y')
	{
		addClassName('constituent_name'+index,'delete');
		addClassName('quantity'+index, 'delete');
		addClassName('units'+index, 'delete');
		addClassName('calorific_value'+index,'delete');
	}
	else
	{
		removeClassName('constituent_name'+index,'delete');
		removeClassName('quantity'+index, 'delete');
		removeClassName('units'+index, 'delete');
		removeClassName('calorific_value'+index, 'delete');
	}
}
function removeBottomBorder(index)
{
	addClassName('constituent_name'+index,'previousEl');
	addClassName('quantity'+index, 'previousEl');
	addClassName('units'+index, 'previousEl');
	addClassName('calorific_value'+index, 'previousEl');
}




function addresultlabels(){
	var myTable = document.getElementById('formatTable');
	if (document.getElementById("constituent_name"+(document.getElementById("formatTable").rows.length-1)).value!= ''){
		var length = myTable.rows.length;
		var imgSrc=cpath+"/icons/Delete.png";

		if (!checkDuplicate(length-1)){

			if(length>1)
				removeBottomBorder(length-1);

			var row = myTable.insertRow(length);
			dietPresFormName.recordLength.value = parseFloat(1) + parseFloat(dietPresFormName.recordLength.value );

			td1 = row.insertCell(0);
			td1.setAttribute('style','border-left:1px #cad6e3 solid;');
			td1.innerHTML = "<input type='text'  id='constituent_name"+length+"' name='constituent_name"+length+"'" +
			 " maxlength='100'  onblur = 'capWords(constituent_name"+length+"),checkDuplicate("+length+")'>";

			td2 = row.insertCell(1);
			td2.innerHTML = "<input type='text'  id='quantity"+length+"' name='quantity"+length+"'  maxlength='100' onkeypress='return enterNumOnlyzeroToNine(event)'/>";

			td3 = row.insertCell(2);
			td3.innerHTML = "<input type='text' id='units"+length+"' name='units"+length+"' rows='1'/>";

			td4 = row.insertCell(3);
			td4.innerHTML = "<input type='text' id='calorific_value"+length+"' maxlength='100'  name='calorific_value"+length+"' "+
			 " onkeypress='return enterNumOnlyzeroToNine(event)' onblur='calculateTotalCalory()'/>";

			td5=row.insertCell(4);
			td5.setAttribute('class','last');
			td5.innerHTML="<a href='javascript:void(0)' onclick='changeElsColor("+length+");disableRow("+length+")'> "+
				" <img src='"+imgSrc+"' id='delItem"+length+"' name='delItem"+length+"' class='imgDelete' /></a>"+
							"<input type='hidden' id='delete"+length+"' name='delete"+length+"' value='N'>" +
							"<input type='hidden' id='newAdded"+length+"' name='newAdded"+length+"' value='Y'>";
		}
	}
}

function disableRow(rowid){
	var table = document.getElementById('formatTable');
	var row = table.rows[rowid];
	if(document.getElementById("delete"+rowid).value == 'Y'){
		if (document.getElementById("calorific_value"+(rowid)).value != ""){
			var totalCalorificValue = document.getElementById("totalCalorificValue").value;
			totalCalorificValue = parseFloat(totalCalorificValue) - parseFloat(document.getElementById("calorific_value"+(rowid)).value);
			document.getElementById("totalCalorificValue").innerHTML = totalCalorificValue;
			document.getElementById("totalCalorificValue").value = totalCalorificValue;
		}
		document.getElementById("constituent_name"+rowid).readOnly = true;
		document.getElementById("delItem"+rowid).src=cpath + '/icons/Deleted.png';
	}else{
		checkDuplicate(rowid);
		if (document.getElementById("calorific_value"+(rowid)).value != ""){
			var totalCalorificValue = document.getElementById("totalCalorificValue").value;
			totalCalorificValue = parseFloat(totalCalorificValue) + parseFloat(document.getElementById("calorific_value"+(rowid)).value);
			document.getElementById("totalCalorificValue").innerHTML = totalCalorificValue;
			document.getElementById("totalCalorificValue").value = totalCalorificValue;
		}
		document.getElementById("constituent_name"+rowid).readOnly = false;
		document.getElementById("delItem"+rowid).src=cpath + '/icons/Delete.png';
	}

}


function checkDuplicate(rowid){
	var table = document.getElementById('formatTable');
	var totalRows = table.rows.length;

	for (var i=1;i<=totalRows-1;i++){
		if (i != rowid){
			if ((document.getElementById("delete"+i).value != 'Y') && (document.getElementById("constituent_name"+i).value != "")){
				if (document.getElementById("constituent_name"+i).value == document.getElementById("constituent_name"+rowid).value){
					alert ("Duplicate constituent name "+document.getElementById("constituent_name"+rowid).value);
					document.getElementById("constituent_name"+rowid).focus();

					if (document.getElementById("newAdded"+i).value == 'N'){
						document.getElementById("constituent_name"+i).value = document.getElementById("constituentName"+i).value;
						document.getElementById("constituent_name"+rowid).value = "";
					}else{
					}
					if (document.getElementById("newAdded"+i).value == 'Y'){
						if (document.getElementById("newAdded"+rowid).value == 'Y') {
							document.getElementById("constituent_name"+rowid).value = "";
						}else {
							document.getElementById("constituent_name"+i).value = "";
						}
					}
					document.getElementById("constituent_name"+i).focus();
					return true;
					break;
				}
			}
		}
	}
}


function checkFormFields(){
	var count = 0;
	var table = document.getElementById('formatTable');
	var updatedMealName = document.getElementById("meal_name");
	var totalRows = table.rows.length;
	var uncheckedRow = false;
	var uncheckedIndex = 0;
	if (document.getElementById('meal_name').value=="") {
		alert("Meal Name is required");
		document.getElementById('meal_name').focus();
		return false;
	}
	if (document.getElementById('diet_category').value=="") {
		alert("Diet Category is required");
		document.getElementById('diet_category').focus();
		return false;
	}
	if (document.getElementById('diet_type').value=="") {
		alert("Diet Type is required");
		document.getElementById('diet_type').focus();
		return false;
	}
	if (document.getElementById('service_group_id').selectedIndex==0) {
		alert("Service Group is required");
		document.getElementById('service_group_id').focus();
		return false;
	}
	if (document.getElementById('service_sub_group_id').selectedIndex==0) {
		alert("Service Sub Group is required");
		document.getElementById('service_sub_group_id').focus();
		return false;
	}

	var isInsuranceCatIdSelected = false;
	var insuranceCatId = document.getElementById('insurance_category_id');
	for (var i=0; i<insuranceCatId.options.length; i++) {
	  if (insuranceCatId.options[i].selected) {
		  isInsuranceCatIdSelected = true;
	  }
	}
	if (!isInsuranceCatIdSelected) {
		alert("Please select at least one insurance category");
		return false;
	}

	if (dietPresFormName._method.value == 'update'){

		if (trim(orginalMealName) != trim(updatedMealName.value)) {
			for (var i=0;i<mealName.length;i++){
				if (trim(updatedMealName.value) == mealName[i].MEAL_NAME){
					alert("Duplicate meal name "+updatedMealName.value);
					document.getElementById("meal_name").focus();
					return false;
				}
			}
			capWords(updatedMealName);
		}

		for (var i=1;i<=totalRows-1;i++){
			if (document.getElementById("delete"+i).value == 'Y'){
					count++;
			}else{
				if(!uncheckedRow){
					uncheckedRow = true;
					uncheckedIndex = i;
				}
			}
		}
		if(uncheckedRow){
			if (document.getElementById("constituent_name"+uncheckedIndex).value == ""){
				alert("Atleast one constituent should be there for the meal");
				return false;
			}
		}
		if (count == totalRows-1){
			alert("Atleast one constituent should be there for the meal");
			return false;
		}else{
			dietPresFormName.submit();
		}
	}
	dietPresFormName.submit();
}


function calculateTotalCalory(){
	var totalClorificValue = 0;
	var table = document.getElementById('formatTable');
	var totalRows = table.rows.length;

	for (var i=1;i<=totalRows-1;i++){
		if ((document.getElementById("calorific_value"+i).value != "")) {
			totalClorificValue = parseFloat(totalClorificValue) + parseFloat( document.getElementById("calorific_value"+i).value);
		}
	}
	document.getElementById("totalCalorificValue").innerHTML = totalClorificValue;
	document.getElementById("totalCalorificValue").value = totalClorificValue;
	if (document.getElementById('serviceSubGroup').value!="") {
		loadServiceSubGroup();
		setSelectedIndex(document.getElementById('service_sub_group_id'), document.getElementById('serviceSubGroup').value);
	}
}


function onClickDelete(rowid) {
 var image=cpath + '/icons/Delete.png';
     var disable=cpath + '/icons/Delete1.png';
     var marked=document.getElementById("delete" + rowid ).value;
     if(marked=='Y'){
      document.getElementById("delete" + rowid).value='N';
      document.getElementById("image"+rowid).src=image;
      removeClassName('tr'+rowid, 'delete');
     }
     else{
      document.getElementById("delete" + rowid ).value='Y';
      document.getElementById("image"+rowid).src=disable;
      addClassName('tr'+rowid, 'delete');
     }

}
/*
function addRow() {

	if(!(checkBillStatus("","",document.getElementById("visit_id").value,null)))
		return false;


	var date = document.getElementById("fromdate").value;

	if (mealName == "" || date == "" || time == "0"){
		if (date == "") {
			alert("Please enter the date  ");
			document.getElementById("fromdate").focus();
		}else if (mealName == ""){
			alert("Please enter the meal name ");
			document.getElementById("prescribeMeal").focus();
		}else {
			alert("Please select the time ");
			document.getElementById("prescribeTime").focus();
		}
	}else if (!validateAllFields()){
	}else{
		if (!checkDuplicateForPrescription(mealName,time,date)){

			var table = document.getElementById("mealPrescTab");
			var length = table.rows.length;

			var tdObj="", trObj="", el="";
			var trObj = table.insertRow(length);

			dietPresFormName.recordLength.value = parseFloat(1) + parseFloat(dietPresFormName.recordLength.value );
			tdObj = trObj.insertCell(-1);
			tdObj.innerHTML ="<input type='checkbox'  id='prescribedCheckBox"+length+"' name='prescribedCheckBox'  onclick='onClickDelete(this,"+length+")' >" +
					"<input type='hidden' id='delete"+length+"' name='delete' value='N'>" +
					"<input type='hidden' id='newAdded"+length+"' name='newAdded' value='Y'>" +
					"<input type='hidden' id='meal_date"+length+"' name='meal_date' value='"+date+"'>" +
					"<input type='hidden' id='meal_timing"+length+"' name='meal_timing' value='"+time+"'>" +
					"<input type='hidden' id='meal_name"+length+"' name='meal_name' value='"+mealName+"'>" +
					"<input type='hidden' id='meal_time"+length+"' name='meal_time' value='"+mealTime+"'>" +

					"<input type='hidden' id='special_instructions"+length+"' name='special_instructions' value='"+splIns+"'>";


			tdObj = trObj.insertCell(-1);
			tdObj.innerHTML = date;

			tdObj = trObj.insertCell(-1);
			tdObj.innerHTML = time;

			tdObj = trObj.insertCell(-1);
			tdObj.innerHTML = mealName;

			tdObj = trObj.insertCell(-1);
			tdObj.innerHTML = splIns;

			tdObj = trObj.insertCell(-1);
			tdObj.innerHTML = 'N';

			tdObj = trObj.insertCell(-1);
			tdObj.innerHTML = '';

				clearFields();

		}// duplicate check if closed
	}//else closed

}
*/
/*
This function is used in two pages
1.list.jsp
2.dietPrescription.jsp
*/
var mealMapArray = [];
var mealAutoCompArray = new Array();
function initMealAutoComplete(){
		var textFieldName = "";
		if (screentype == "searchScreen"){
			textFieldName = 'searchName';
		}else if (screentype == "prescriptionScreen") {
			textFieldName = 'prescribeMeal';
		}
		var mealNamesArray = [];
		if(mealNameAndCharges != null){
			mealMapArray = new Array(mealNameAndCharges.length);
			for(var l = 0;l<mealNameAndCharges.length;l++){
				if (screentype == "searchScreen"){
					mealNamesArray.push(mealNameAndCharges[l].MEAL_NAME);
				} else if (screentype == "prescriptionScreen") {
					if (mealNameAndCharges[l].STATUS == 'A') {
						mealNamesArray.push(mealNameAndCharges[l].MEAL_NAME);
					}
				}
			}
		}
		this.oServSCDS = new YAHOO.widget.DS_JSArray(mealNamesArray);
		oServAutoComp = new YAHOO.widget.AutoComplete(textFieldName,'mealnamecontainer', this.oServSCDS);
		oServAutoComp.prehightlightClassName = "yui-ac-prehighlight";
		oServAutoComp.typeAhead = false;
		oServAutoComp.useShadow = false;
		oServAutoComp.allowBrowserAutocomplete = false;
		oServAutoComp.minQueryLength = 0;
		if (screentype == "searchScreen"){
			oServAutoComp.forceSelection = false;
		}else if (screentype == "prescriptionScreen") {
			oServAutoComp.forceSelection = true;
		}
		oServAutoComp.itemSelectEvent.subscribe(function(sType,aArgs){
			var data = aArgs[2];
			for (var i=0;i<mealNameAndCharges.length;i++){
				if (data == mealNameAndCharges[i].MEAL_NAME){
					document.getElementById("mealid").value = mealNameAndCharges[i].DIET_ID;
				}
			}
		});
}


var doctorMapArray = [];
var doctorAutoCompArray = new Array();
function initDoctorAutoComplete(){

		var doctorNamesArray = [];
		if(doctorList != null){
		doctorMapArray = new Array(doctorList.length);
		doctorNamesArray.length = doctorList.length;
		for(var l = 0;l<doctorList.length;l++){
			doctorNamesArray[l] = doctorList[l].DOCTOR_NAME;
			}
		}
		this.oServSCDS = new YAHOO.widget.DS_JSArray(doctorNamesArray);
		oServAutoComp = new YAHOO.widget.AutoComplete('doctorName','doctornamecontainer', this.oServSCDS);
		oServAutoComp.prehightlightClassName = "yui-ac-prehighlight";
		oServAutoComp.typeAhead = false;
		oServAutoComp.useShadow = false;
		oServAutoComp.allowBrowserAutocomplete = false;
		oServAutoComp.minQueryLength = 0;
		oServAutoComp.forceSelection = true;
		oServAutoComp.itemSelectEvent.subscribe(function(sType,aArgs){
			var data = aArgs[2];
			for (var i=0;i<doctorList.length;i++){
				if (data == doctorList[i].DOCTOR_NAME){
					document.getElementById("prescribed_by").value = doctorList[i].DOCTOR_ID;
					dietPresFormName.prescribed_by.value = doctorList[i].DOCTOR_ID;
				}
			}
		});
}



function checkDuplicateForPrescription (name,time,date){

	var newName = name.trim().toLowerCase();
	var newTime = time.trim().toLowerCase();

	var prescribedNames = document.getElementsByName("meal_name");
	var prescribedTimes = document.getElementsByName("meal_timing");
	var prescribedDates = document.getElementsByName("meal_date");

	for (var i=0;i<prescribedNames.length;i++) {
		if (date == prescribedDates[i].value) {
			if (newName == prescribedNames[i].value.trim().toLowerCase()) {
				if (newTime == prescribedTimes[i].value.trim().toLowerCase()) {
					alert ("Duplicate entry : "+name +" For "+time);
				 	return true;
				}
			}
		}
	}

	return false;
}


function clearFields() {
	document.getElementById("prescribeMeal").value = "";
	document.getElementById("fromdate").value = "";
	document.getElementById("todate").value="";
	document.getElementById("prescribeTime").value = "0";
	document.getElementById("prescribeSplIns").value = "";
	document.getElementById("splTimeDiv").style.display = 'none';

}


function showTime(){
	if (document.getElementById("prescribeTime").value =='Spl') {
		document.getElementById("splTimeDiv").style.display = 'block';
		document.getElementById("splMealTime").value = document.getElementById("serverTime").value;
	}else {
		document.getElementById("splTimeDiv").style.display = 'none';
	}
}


function validateAllFields() {

	var fromdate = document.getElementById("fromdate");
	var tdate = document.getElementById("todate");
	var mealName = document.getElementById("prescribeMeal").value;
	var time = document.getElementById("prescribeTime").value;
	if (document.getElementById("prescribeTime").value == 'Spl'){

		var prescTime = document.getElementById("splMealTime");

		if (prescTime.value == "" ) {
			alert("Enter the special meal time");
			prescTime.focus();
			return false;
		}
		if (!validateTime(prescTime)) return false;
	}
	if (fromdate.value == "" ) {
		alert("Enter from  date");
		fromdate.focus();
		return false;
	}else if (fromdate.value != ""){
		if (getDateDiff(document.getElementById("reg_date").value,fromdate.value) == -1){
			alert("From date should be more than registration date("+document.getElementById("reg_date").value+")");
			fromdate.focus();
			return false;
		}
	}
	if (tdate.value != ""){
		if (getDateDiff(fromdate.value,tdate.value) == -1){
			alert("To date cannot be less than from date");
			return false;
		}
	}
	if (mealName == "" || time == "0"){
		if (mealName == ""){
			alert("Please enter the meal name ");
			document.getElementById("prescribeMeal").focus();
		}else {
			alert("Please select the time ");
			document.getElementById("prescribeTime").focus();
		}
		return false;
	}
	if (!doValidateDateField(fromdate)) return false;
	return true;
}

function validate () {

	var noOfRows = document.getElementById("mealPrescTab").rows.length ;
	var submit = true;
	var newRowsAdded = false;
	if (noOfRows == 1){
		alert("Add atleast one row to table ");
		submit =  false;
	}

	var newAdded = document.getElementsByName("newAdded");
	for (var i=0;i<newAdded.length;i++) {
		if (newAdded[i].value == 'Y') {
			newRowsAdded = true;
			break;
		}
	}

	if (newRowsAdded) {
		if (document.getElementById("doctorName").value == ""){
			alert("Prescribed doctor name is needed");
			document.getElementById("doctorName").focus();
			submit =  false;
		}
	}
	if (submit) {
		dietPresFormName.submit();
	} else {
	}
}


function checkUpdateFields () {

	var updateList = document.getElementsByName("_updateMealStatus");
	var checked = false;
	for (var i=1;i<=updateList.length;i++) {
		if (document.getElementById("_updateMealStatus"+i).checked){
			document.getElementById("_updateStatus"+i).value = "Y";
			if (!checked){
				checked = true;
			}
		} else {
			document.getElementById("_updateStatus"+i).value = "N";
		}
	}
	if (!checked){
		alert("Check atleast one meal for updation");
		return false;
	}else {
		dietPresFormName.action = "Canteen.do"
		dietPresFormName.method = 'POST';
		document.getElementById("_method").value ="updateMealDeliveredStatus";
		dietPresFormName.submit();
	}

}

function validateDateField() {
	if (document.getElementById("date").value == ""){
		alert ("Date is required");
		document.getElementById("date").focus();
		return false;
	}
   if  (!doValidateDateField(document.getElementById("date"))){
   	return false;
   }
}


function printprescription(print, printerId){
	if (print != '') {
		var r = false;
		if (print == 'MealPrescriptionPrint' ){
			var url = cpath + "/pages/ipservices/dietPrescribe.do?_method=printPrescription";
			url += "&patient_id=" + patient_id;
			url += "&printerId=" + printerId;
		}
		window.open(url);
	}
}


function getRatePlanCharges(Obj,fromScreen){
	if (fromScreen == 'list') {
	document.dietryMasterForm.action = "DietaryMaster.do?_method=list&org_id="+Obj.value;
	}else {
		var diet_id = document.dietryMasterForm.diet_id.value;
		document.dietryMasterForm.action = "DietaryMaster.do?_method=editCharges&diet_id="+diet_id;
	}
	document.dietryMasterForm.submit();
}


function getDecimalValues(el){
	var value = getPaise(el.value);
	el.value = value/100;
}


function selectAllBedTypes(){
	var selected = document.getElementById("allBedTypes").checked;
	var noOfBedTypes = document.getElementById("groupBedType").length;

	for (var i=0;i<noOfBedTypes;i++){
		document.getElementById("groupBedType").options[i].selected = selected;
	}
	createToolbar(toolbar);
}

function ValidateGropUpdate(){
	var form = dietPresFormName;


	var checked = false;
	if (form.dietID == undefined){
		alert('Please add at least one diet to update charges.');
		return false;
	}

	var length = form.dietID.length;
	if(length == undefined){
		if(form.dietID.checked ){
			checked = true;
		}
	}else{
		for(var i=0;i<length;i++){
			if(form.dietID[i].checked){
				checked = true;
				break;
			}
		}
	}

	if(form.updateAllMeals.checked){
		checked = true;
	}

	if(!checked){
		alert('at least one Meal has to checked for updation');
		return;
	}

    var selceted = false;
	var bedTypeLength = form.groupBedType.length;
	for(var i=0; i<bedTypeLength ; i++){
		if(form.groupBedType.options[i].selected){
				selceted = true;
				break;
		}
	}

	if(!selceted){
		alert('bed Types are required');
		return ;
	}

	if (!updateOption()) {
		alert("Select any update option");
		form.updateTable[0].focus();
		return ;
	}

	if(form.varianceBy.value=="" && form.varianceValue.value ==""){
		alert("Rate Variance value is required ");
		form.varianceBy.focus();
		return ;
	}

	if(getAmount(form.varianceBy.value) > 100){
		alert("Discount percent cannot be more than 100");
		updateform.amount.focus();
		return false;
	}

	form._method.value = "groupUpdate";
	form.submit();

}

function updateOption() {
	for (var i=0; i<dietPresFormName.updateTable.length ; i++) {
		if(dietPresFormName.updateTable[i].checked){
			return true;
		}
	}
	return false;
}

function selectAll(){
	var updateSelectedRecords = document.getElementById("updateSelectedRecords").checked;
	var updateAllMeals = document.getElementById("updateAllMeals").checked;
	var length = document.getElementById("mealsTable").rows.length-1;

	if (updateSelectedRecords) {
		for (var i=1;i<=length;i++){
			document.getElementById("dietID"+i).checked = updateSelectedRecords;
		}
	}else if (!updateSelectedRecords){
		for (var i=1;i<=length;i++){
			document.getElementById("dietID"+i).checked = updateSelectedRecords;
		}
	}

	if (updateAllMeals){
		for (var i=1;i<=length;i++){
			document.getElementById("dietID"+i).disabled = updateAllMeals;
		}

	}else if (!updateAllMeals){
		for (var i=1;i<=length;i++){
			document.getElementById("dietID"+i).disabled = updateAllMeals;
		}
	}
}

function addTemplate(e, url) {

	if(!(checkBillStatus("","",document.getElementById("visit_id").value,null)))
		return false;

	var format = trim(document.getElementById('dietFormat').value.split("+")[0]);
	var templateId = document.getElementById('dietFormat').value.split("+")[1];

	if (format == "") {
		alert('Select the format');
		document.getElementById('dietFormat').focus();
		return false;
	} else {
		href = url + "&format="+format +"&template_id="+templateId+"&documentType=dietary";
		document.getElementById("addTemplateUrl").href = href;
	}
}

function confirmDelete(){
	if (confirm("Do you want to delete the diet chart")){
		return true;
	}else {
		return false;
	}
}

function validateAllDiscounts() {
	var len = document.dietryMasterForm.ids.value;
	var valid = true;
	for(var i=0;i<len;i++) {
		valid = valid && validateDiscount('regularCharges','discount',i);
	}
	if(!valid) return false;
	else return true;
}

function handleAddDiet() {
	var dietId = document.getElementById("mealid").value;
	var mealName = document.getElementById("prescribeMeal").value;
	$.get(cpath + '/master/dietconstituents/getconstituentsfordiet.json?diet_id=' + dietId, function (data) {
		var isAllergicToDiet = false;
		var addDiet = true;

		for (var i=0; i < allAllergiesJSON.length; i++) {
			if (allAllergiesJSON[i].allergy_type === 'F' && allAllergiesJSON[i].status === 'A') {
				var allergy = allAllergiesJSON[i].allergy;
				if (allergy.toLowerCase() === mealName.toLowerCase()) {
					isAllergicToDiet = true;
					break;
				}

				for (var j=0; j < data.constituents.length; j++) {
					var constituent = data.constituents[j].constituent_name;
					if (allergy.toLowerCase() === constituent.toLowerCase()) {
						isAllergicToDiet = true;
						break;
					}
				}
			}
		}

		if (isAllergicToDiet) {
			addDiet = confirm("The patient has a Food Allergy to " + allergy + " and the same is prescribed in Diet " + mealName + ". Do you still want to prescribe the diet?");
		}
		if (addDiet) {
			getDatesByAjaxCall();
		}
	}
);

}


function getDatesByAjaxCall(){
		if (validateAllFields()){
			var fromdate = document.getElementById("fromdate").value;
			var todate = document.getElementById("todate").value;
			if (todate == ""){
				todate = fromdate;
			}
			var ajaxObjForDates = newXMLHttpRequest();
			var url = cpath+"/pages/ipservices/dietPrescribe.do?_method=getDatesInRange&fromdate="+fromdate+"&todate="+todate+"" ;
			getResponseHandlerText(ajaxObjForDates,getDatesInRange,url);
		}

}
function getDatesInRange(responseText){
	var	ajaxDateRange = eval(responseText);
	var splIns = document.getElementById("prescribeSplIns").value;
	var mealName = document.getElementById("prescribeMeal").value;
	var time = document.getElementById("prescribeTime").value;
	var date = "";
	var mealTime = "";
	if (time == 'Spl') {
		time = time +" "+ document.getElementById("splMealTime").value ;
		mealTime = document.getElementById("hiddensplMealTime").value ;
	}
	var table = document.getElementById("mealPrescTab");
	var length = table.rows.length;

	for(var i=0;i<ajaxDateRange.length;i++) {
		date = ajaxDateRange[i];
		if(!(checkBillStatus("","",document.getElementById("visit_id").value,null)))
			return false;
		else{
			if (!checkDuplicateForPrescription(mealName,time,date)){
				var tdObj="", trObj="", el="";
				var trObj = table.insertRow(length);
				    trObj.id = "tr" + length;

				dietPresFormName.recordLength.value = parseFloat(1) + parseFloat(dietPresFormName.recordLength.value );

				tdObj = trObj.insertCell(-1);
				tdObj.innerHTML = date;

				tdObj = trObj.insertCell(-1);
				tdObj.innerHTML = time;

				tdObj = trObj.insertCell(-1);
				tdObj.innerHTML = mealName;

				tdObj = trObj.insertCell(-1);
				tdObj.setAttribute("style","white-space:normal");
				tdObj.innerHTML = splIns;

				tdObj = trObj.insertCell(-1);
				tdObj.innerHTML = 'N';

				tdObj = trObj.insertCell(-1);
				tdObj.innerHTML = '';

				tdObj = trObj.insertCell(-1);
                tdObj.innerHTML='<img class="imgDelete" name="image" id="image'+length+'" src="' + cpath + '/icons/Delete.png" onclick="onClickDelete(\''+length + '\')" />'+
						"<input type='hidden' id='delete"+length+"' name='delete' value='N'>" +
						"<input type='hidden' id='newAdded"+length+"' name='newAdded' value='Y'>" +
						"<input type='hidden' id='meal_date"+length+"' name='meal_date' value='"+date+"'>" +
						"<input type='hidden' id='meal_timing"+length+"' name='meal_timing' value='"+time+"'>" +
						"<input type='hidden' id='meal_name"+length+"' name='meal_name' value='"+mealName+"'>" +
						"<input type='hidden' id='meal_time"+length+"' name='meal_time' value='"+mealTime+"'>" +
                        "<input type='hidden' id='special_instructions"+length+"' name='special_instructions' value='"+splIns+"'>";


				length = length +1;
			}// duplicate check if closed
		}//else closed

	}
	clearFields();
}

function loadServiceSubGroup() {
	var serviceGroupId = document.getElementById('service_group_id').value;
	var index = 1;
	document.getElementById("service_sub_group_id").length = 1;
	for (var i=0; i<serviceSubGroupsList.length; i++) {
		var item = serviceSubGroupsList[i];
	 	if (serviceGroupId == item["SERVICE_GROUP_ID"]) {
	 		document.getElementById("service_sub_group_id").length = document.getElementById("service_sub_group_id").length+1;
	 		document.getElementById("service_sub_group_id").options[index].text = item["SERVICE_SUB_GROUP_NAME"];
	  		document.getElementById("service_sub_group_id").options[index].value = item["SERVICE_SUB_GROUP_ID"];
	 		index++;
	 	}
	}
}
