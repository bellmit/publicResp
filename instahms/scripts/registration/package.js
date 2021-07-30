var checkOtSchedule = false;
var packagesList ;
var operation_id = '';
function getpackagecharge(packageid){
	removeRows();
	if(packageid!=""){
		var packageComponents = getPackageComponents(packageid);
		var url = cpath + baseUrl + ".do?method=getPackageCharge&packageid="+packageid+"&orgid="+document.forms[0].orgid.value+"&bedType="+document.getElementById("bedtype").value;
		var xhr = newXMLHttpRequest();
		getResponseHandlerText(xhr, function(responseData) {addRows(responseData,packageid);}, url);
		return null;
	 }else{
		document.mainform.packageid.value="";
		document.mainform.packagecharge.value=0;
	 }

}//end of getpackagecharge

function addRows(responseData,packageid) {
	var packageComponents = eval(responseData);
	document.getElementById("packagecharge").innerHTML = packageComponents[0].DISCOUNT == undefined?packageComponents[0].CHARGE:packageComponents[0].CHARGE-packageComponents[0].DISCOUNT;
	document.getElementById("pkgDetails").innerHTML = packageComponents[0].DESCRIPTION;
	for(var i = 0;i<packageComponents.length;i++){
			if(packageComponents[i].CHARGE_GROUP == 'DOC' ){
				addToTable(packageComponents[i].CHARGE_GROUP,packageComponents[i].CHARGE_HEAD,packageComponents[i].CHARGEHEAD_NAME);
			}
			for(var j = 0;j<packagesList.length;j++){
				var package = packagesList[j];
				if(packageid == package.package_id){
					if(package.operation_id != null && package.operation_id != ""){
						document.getElementById("otschedule").style.display = 'block';
						checkOtSchedule = true;
						operation_id = package.operation_id;
						otSchedule();
						document.getElementById("operation_id").value = package.operation_id;
					}
				}
			}
		}
	}
function getPackageComponents(packageid){
		 document.mainform.packagename.value = packageid;
		 var orgname = orgName;
		 var url = cpath + baseUrl + ".do?method=getPackageCharge&packageid="+packageid+"&orgid="+document.forms[0].orgid.value+"&bedType="+document.getElementById("bedtype").value;
		 var reqObject = newXMLHttpRequest();
			reqObject.open("POST",url.toString(), false);
			reqObject.send(null);
			if (reqObject.readyState == 4) {
				if ( (reqObject.status == 200) && (reqObject.responseText!=null) ) {
					return reqObject.responseText;
				}
			}
}
function packagecharge(domDocObj,responseText){
	var test = eval(responseText);
	 var selectedpackageid = document.mainform.packagename.options[document.mainform.packagename.selectedIndex].value;
	 list=document.getElementById("PACKAGES").getElementsByTagName("PACKAGESs");
	 root = list.item(0);
	 offlist = root.getElementsByTagName("packages");
	 len=offlist.length;
	 var selectedpackagetype = null;

	for(var i=0;i<len;i++){
		off = offlist.item(i);
		if(selectedpackageid==off.attributes.getNamedItem('class5').nodeValue){
			selectedpackagetype = off.attributes.getNamedItem('class4').nodeValue
			//document.mainform.selectedpackagetype.value = selectedpackagetype;
		}
	}
	if(selectedpackagetype == 'i'){
		var packagecharge = domDocObj.getElementsByTagName("IPPCAKAGE")[0].getElementsByTagName("packagecharge")[0].firstChild.nodeValue;
		document.mainform.packagecharge.value =  packagecharge;
		if (domDocObj.getElementsByTagName("IPPCAKAGE")[0].getElementsByTagName("remarks")[0].firstChild.nodeValue !=null) {
		document.mainform.pkgDetails.value =  replaceXmlContent(domDocObj.getElementsByTagName("IPPCAKAGE")[0].getElementsByTagName("remarks")[0].firstChild.nodeValue);}
		else document.mainform.pkgDetails.value = '';

	}else if(selectedpackagetype == 'o'){
		var packagecharge = domDocObj.getElementsByTagName("OPPCAKAGE")[0].getElementsByTagName("packagecharge")[0].firstChild.nodeValue;
		document.mainform.packagecharge.value =  packagecharge;
		document.mainform.pkgDetails.value =  replaceXmlContent(domDocObj.getElementsByTagName("OPPCAKAGE")[0].getElementsByTagName("remarks")[0].firstChild.nodeValue);

	}else{
		var packagecharge = domDocObj.getElementsByTagName("DIAPPCAKAGE")[0].getElementsByTagName("packagecharge")[0].firstChild.nodeValue;
		document.mainform.packagecharge.value =  packagecharge;
		document.mainform.pkgDetails.value =  replaceXmlContent(domDocObj.getElementsByTagName("DIAPPCAKAGE")[0].getElementsByTagName("remarks")[0].firstChild.nodeValue);
	}
}//end of packagecharge



function populatepackages(orgId){
     var ajaxobj = newXMLHttpRequest();
	 var url = cpath + '/pages/masters/PackagesQuery.do?method=getPackandbeds&orgId='+orgId;
	 getResponseHandler(ajaxobj,getpackageswithbeds,url.toString());
}

function getpackageswithbeds(domDocObj,responseText){
     packagesList = eval(responseText);
	 var obj = document.mainform.packagename;
	 obj.length=1;
     if (patientType == 'i') {
		 for(var i=0;i<packagesList.length;i++ ){
		 	var record = packagesList[i];
			obj.length=  obj.length + 1;
			obj.options[obj.length-1].value = record.package_id;
			obj.options[obj.length-1].text  = record.package_name;
		 }
	 }
	 if (patientType == 'o') {
		 for(var i=0;i<packagesList.length;i++ ){
		 	var record = packagesList[i];
			if (record.package_type == 'o' || record.package_type == 'd') {
				obj.length=  obj.length + 1;
				obj.options[obj.length-1].value = record.package_id;
				obj.options[obj.length-1].text  = record.package_name;
			}
		 }
	 }

	 if (patientType == 'd') {
		 for(var i=0;i<packagesList.length;i++ ){
		 	var record = packagesList[i];
			if (record.package_type == 'd') {
				obj.length=  obj.length + 1;
				obj.options[obj.length-1].value = record.package_id;
				obj.options[obj.length-1].text  = record.package_name;
			}
		 }
	 }

}//end of populatepackages

function popup_mrsearch(){

 window.open(
		 cpath +
   '/pages/Common/PatientSearchPopup.do?title=active%20Patients&mrnoForm=mainform&mrnoField=mrno&searchType=active',
		'Search',
		'width=655, height=430, screenX=175, screenY=70, left=200, top=200, scrollbars=yes, menubar=0, resizeble=0'
		)

    return false;
}//end of popup_mrsear
var patientType = '';
function getRegDetails(){
	removeRows();
	var mrno = document.mainform.mrno.value;
	var mrnoexist = false;
	list = document.getElementById("XMLMRNO").getElementsByTagName("XMLMRNOs");
	root = list.item(0);
	offlist = root.getElementsByTagName("xmlmrno");
	len=offlist.length;
	for(var i=0;i<len;i++){
		off=offlist.item(i);
		if(mrno == off.attributes.getNamedItem('class1').nodeValue){
			patientType = off.attributes.getNamedItem('class7').nodeValue;
			if (patientType == 'i') {
				patientType = 'i';
			}else if (patientType == 'o') {
				patientType = 'o';
			}else if (patientType == 'd') {
				patientType = 'd';
			}
			mrnoexist = true;
		}
	}
	if(mrnoexist==false){
		alert("enter valid mrno");
		document.mainform.mrno.value = '';
		document.mainform.reset();
		return false;
	}



	var ajaxobj = newXMLHttpRequest();
	var url = cpath + '/pages/DiagnosticModule/diagnostic.do?method=getpatientdetails&mrno='+mrno;

	getResponseHandler(ajaxobj,getresponsepatientdetails,url.toString());


}//end of getRegDetails

var orgName = '';
function getresponsepatientdetails(domDocObj,responseText){
	var length =domDocObj.childNodes[0].childNodes.length;


	for(var i=0;i<length;i++){
		var record = domDocObj.childNodes[0].childNodes[i];
		var orgId = record.attributes.getNamedItem('class33').nodeValue;
		document.getElementById("orgid").value = orgId;
		document.getElementById("bedtype").value = record.attributes.getNamedItem('class34').nodeValue;

		document.forms[0].orgid.value=orgId;
		document.getElementById("patientBedType").innerHTML=record.attributes.getNamedItem('class34').nodeValue;
		document.getElementById('patientMrno').innerHTML = record.attributes.getNamedItem('class1').nodeValue;
		document.getElementById('patientName').innerHTML = record.attributes.getNamedItem('class2').nodeValue + ' ' + record.attributes.getNamedItem('class13').nodeValue;
		document.getElementById("patientAge").innerHTML = record.attributes.getNamedItem('class3').nodeValue + ' ' + record.attributes.getNamedItem('class4').nodeValue;
		document.getElementById("patientSex").innerHTML=record.attributes.getNamedItem('class5').nodeValue;
		document.getElementById("patientContactNo").innerHTML=record.attributes.getNamedItem('class10').nodeValue;
		document.getElementById("patientVisitNo").innerHTML=record.attributes.getNamedItem('class28').nodeValue;
		document.forms[0].patid.value = record.attributes.getNamedItem('class28').nodeValue;
		document.getElementById("patientDept").innerHTML=record.attributes.getNamedItem('class35').nodeValue;
		document.getElementById("patientDoctor").innerHTML=record.attributes.getNamedItem('class27').nodeValue;

 		populatepackages(orgId);
 		var ajaxobj = newXMLHttpRequest();
    	var url = cpath + baseUrl + '.do?method=getPatBills&visitid='+document.forms[0].patid.value;
        getResponseHandlerText(ajaxobj,getpatientbills,url.toString());
	}



}//end of getresponsepatientdetails

function getpatientbills(responseText){
	if (responseText==null) return;
	if (responseText=="") return;
    eval("gbills = " + responseText);
    var selectedBill = '';
    var visit = '';
    var index = 0;
    var optn ;
    document.getElementById("bill").options.length = 0;
    var selectBox = document.getElementById("bill");
	for (i = selectBox.length - 1; i>=0; i--) {
	    if (selectBox.options[i].selected) {
	      selectBox.remove(i);
	    }
	}
	if(gbills.length > 1){
			document.getElementById("bill").options.length=1;
		    document.getElementById("bill").options[0].value = '';
			document.getElementById("bill").options[0].text = ".......Bill......";
		}
		for (var i=0;i<gbills.length;i++) {
			document.getElementById("bill").options.length= document.getElementById("bill").options.length;
		    var item = gbills[i];
		    visit = item.VISIT_TYPE;
		    optn = document.createElement("OPTION");
			optn.value = item.BILL_NO;
			optn.text = item.BILL_NO+item.BILL_TYPE;
			document.getElementById("bill").options.add(optn);
		}
		document.getElementById("bill").options.length=document.getElementById("bill").options.length;
		optn = document.createElement("OPTION");
		optn.value = "New";
		optn.text = "New Bill";
		document.getElementById("bill").options.add(optn);
}

function setSelectedIndex(opt, set_value) {
  var index=0;
  for(var i=0; i<opt.options.length; i++) {
    var opt_value = opt.options[i].value;
    if (opt_value == set_value) {
      opt.selectedIndex = i;
      return;
    }
  }
}
function savepackage() {
	if(validate()==false){
		return false;
    }
	if(!confirm("do you want to save"))
		return false;
	else
	{
	    document.mainform.patType.value = patientType ;
	    document.mainform.date.value = getCurrentDate();
		document.mainform.time.value = getCurrentTime();
		document.mainform.action= cpath+"/services/prescribepackages.do?method=savePackagePrescription";
		document.mainform.submit();
	}
}

function validate() {
		var operationstartdate = YAHOO.util.Dom.get("start_date").value;
		var operationenddate = YAHOO.util.Dom.get("end_date").value;
		var hstarttime = YAHOO.util.Dom.get("starttime").value;
		var hendtime = YAHOO.util.Dom.get("endtime").value;
	var innerTableObj = document.getElementById("innertable");
		var length = innerTableObj.rows.length ;
		for (var i=1; i<length; i++) {
			if(document.getElementById("dept"+i).value == ""){
				alert("Please select Doctor Department");
				document.getElementById("dept"+i).focus();
				return false;
			}
			if(document.getElementById("doctor"+i).value == ""){
				alert("Please select Doctor");
				document.getElementById("doctor"+i).focus();
				return false;
			}
		}
	if (document.mainform.mrno.value == '') {
		alert("pls Enter MRNO");
		document.mainform.mrno.focus();
		return false;
	}
	if (document.getElementById('packagename').options.selectedIndex == 0) {
		alert("select the package");
		document.mainform.packagename.focus();
		return false;
	}
	if (document.getElementById('bill').value == "") {
		if (document.getElementById("bill").options.length > 1) {
			alert("select the Bill");
			document.mainform.bill.focus();
			return false;
		}else {
			alert("This Patient Bill(s) are closed so,prescription not allowed");
			return false;
		}
	}
	if(checkOtSchedule){
		if(document.mainform.theatre.value == ""){
			alert("Theatre is mandetory");
			document.mainform.theatre.focus();
			return false;
		}
		if(YAHOO.util.Dom.get("primarysurgeon").value == "" ){
			alert("Surgeon is mandetory");
			document.mainform.primarysurgeon.focus();
			return false;
		}
		if(operationstartdate == ""){
				alert("Please select Operation Start Date");
				document.getElementById("start_date").focus();
				return false;
			}
			if(operationenddate == ""){
				alert("Please select Operation End Date");
				document.getElementById("end_date").focus();
				return false;
			}
			if(operationstartdate == operationenddate){
				var startHours = hstarttime.split(':')[0];
				var startMinutes = hstarttime.split(':')[1];
				var endHours = hendtime.split(':')[0];
				var endMinutes = hendtime.split(':')[1];
				var sthrinsec = startHours*60*60 + startMinutes*60;
				var endhrinsec = endHours*60*60 + endMinutes*60;
				if(startHours == endHours){
				if(startMinutes > endMinutes){
				alert("End time must be greater that start time");
				return false;
				}
				}else if(sthrinsec > endhrinsec){
					alert("End time must be greater that start time");
					return false;
				}
			}
			if(operationenddate.split('-')[1] == operationstartdate.split('-')[1] && operationenddate.split('-')[2] == operationstartdate.split('-')[2]){
			if(operationenddate.split('-')[0] < operationstartdate.split('-')[0]  ){

				alert("operation End date must be greater than operation start date");
			return false;
			}
			}
			if(operationenddate.split('-')[2] == operationstartdate.split('-')[2]){
			if(operationenddate.split('-')[1] < operationstartdate.split('-')[1]){
				alert("operation End date must be greater than operation start date");
				return false;
			}
			}
			if(operationenddate.split('-')[0] == operationstartdate.split('-')[0] && operationenddate.split('-')[1] == operationstartdate.split('-')[1] && operationenddate.split('-')[2] == operationstartdate.split('-')[2]){

			var starthr = hstarttime.split(':')[0];
			var startmin = hstarttime.split(':')[1];
			var endhr = hendtime.split(':')[0];
			var endmin = hendtime.split(':')[1];
			var fromtimesecs = starthr*60*60 + startmin * 60 ;
		    var totimesecs = endhr*60*60 + endmin * 60 ;
				if(fromtimesecs >= totimesecs){
					alert("operation End time must be greater than operation start time");
					return false;
				}
			}

			if(hstarttime == ""){
				alert("Please Enter Start Time in HH:MI Format");
				document.getElementById(starttime).focus();
				return false;
			}else{
				if (!validateTime(YAHOO.util.Dom.get(starttime), 1)){
						return false;
					}
			}

			if(hendtime == ""){
				alert("Please Enter End Time in HH:MI Format");
				document.getElementById(endtime).focus();
				return false;
			}else{
				if (!validateTime(YAHOO.util.Dom.get(endtime), 1)){
						return false;
					}
			}
	}
    return true;
}
function addToTable(group, head,headName) {
	var chargeTable = document.getElementById("innertable");
	var numRows = chargeTable.rows.length;
	var id = numRows ;
	var row = chargeTable.insertRow(id);
	var cell;
	cell = row.insertCell(-1);
	cell.innerHTML='<lable name="doctorcharge" id="doctorcharge'+id+'" >Doctor Charges</lable>';
	cell = row.insertCell(-1);
	cell.innerHTML='<lable  >'+headName+'</lable>'+
	'<input type="hidden" name="doctorHead" id="doctorHead'+id+'" value="'+head+'" />';
	cell = row.insertCell(-1);
	cell.innerHTML = '<select name="dept" id="dept'+id+'" onchange="fillDoctor(this.value,'+id+');"></select>';
	loadSelectBox(document.getElementById("dept"+id), deptList, "dept_name", "dept_id","Department");
	cell = row.insertCell(-1);
	cell.innerHTML = '<select name="doctor" id="doctor'+id+'" ><option value="">...Doctor..</option></select>';
	cell = row.insertCell(-1);
	cell.innerHTML = '<input type="text" name="remarks" id="remarks'+id+'" />';
}
function fillDoctor(dept,id){
	var doctorList = getFilteredList(deptList, doctors, "dept_id", "DEPT_NAME","DEPT_NAME",document.getElementById("dept"+id).value);
	document.getElementById("doctor"+id).length = 0;
	var index = 0;
	var optn = document.createElement("OPTION");
	optn.value = "...Doctor..";
	optn.text = "";
	document.getElementById("doctor"+id).options.add(optn);

	document.getElementById("doctor"+id).options[index].text = "...Doctor..";
	document.getElementById("doctor"+id).options[index].value = "";
	for (var i=0; i<doctorList.length; i++) {
		var doctor = doctorList[i];
		if(dept== doctor["DEPT_NAME"]){
			index++;
			optn = document.createElement("OPTION");
			optn.value = doctor["DOCTOR_ID"];
			optn.text = doctor["DOCTOR_NAME"];
			document.getElementById("doctor"+id).options.add(optn);
		}
	}
}
function removeRows(){
		var innerTableObj = document.getElementById("innertable");
		var length = innerTableObj.rows.length ;
		for (var i=1; i<length; i++) {
			innerTableObj.deleteRow(1);
		}
	}
function getFilteredList(subList, mainList, slId, mlId, mlId2, mlVal) {
	var getList = new Array();

	if(mlId2 != null && mlVal != null) {
		for (var k=0; k<mainList.length; k++) {
			if((subList[0][slId] == mainList[k][mlId])  && (mainList[k][mlId2] == mlVal)) {
				getList.push(mainList[k]);
			}
		}
	}else {
		for (var k=0; k<mainList.length; k++) {
			if(subList[0][slId] == mainList[k][mlId]) {
				getList.push(mainList[k]);
			}
		}
	}

	var exists = false;
	for (var i=0; i<subList.length; i++) {
		exists = false;
		for(var j=0; j<getList.length; j++) {
			if(subList[i][slId] == getList[j][mlId]) {
				exists = true;
				break;
			}
		}
		if(exists) continue;
		else {
			for(var k=0; k<mainList.length; k++) {
				if(mlId2 != null && mlVal != null) {
					if ((subList[i][slId] == mainList[k][mlId]) && (mainList[k][mlId2] == mlVal) ) {
						getList.push(mainList[k]);
					}
				}else {
					if(subList[i][slId] == mainList[k][mlId]) {
						getList.push(mainList[k]);
					}
				}

			}
		}
	}
	return getList;
}
function otSchedule(){
	doctorAutoComplete("primarysurgeon","otsurgeoncontainer",otsurgeonslist,'Surgeon');
	doctorAutoComplete("primaryanae","otanesthetistcontainer",otanaesthesistslist,'Anae');
	anesthesiatypeautocomplete(anesthesia_type,anesthesiatypecontainer);
}
function anesthesiatypeautocomplete(anesthesia_type,anesthesiatypecontainer){
		YAHOO.example.aneArray = [];
		YAHOO.example.aneArray.length = 0;
		var n =0;
		for(var k =0;k<anaesthesiatypeslist.length;k++){
			YAHOO.example.aneArray[n] = anaesthesiatypeslist[k].ANESTHESIA_TYPE;
			n++;
		}
		this.aneSCDS = new YAHOO.widget.DS_JSArray(YAHOO.example.aneArray);
		this.aneAutoComp = new YAHOO.widget.AutoComplete(anesthesia_type,anesthesiatypecontainer, this.aneSCDS);
		this.aneAutoComp.prehightlightClassName = "yui-ac-prehighlight";
		this.aneAutoComp.typeAhead = true;
		this.aneAutoComp.useShadow = false;
		this.aneAutoComp.allowBrowserAutocomplete = false;
		this.aneAutoComp.minQueryLength = 0;
		this.aneAutoComp.forceSelection = false;
		this.aneAutoComp.animVert = false;
}
function setTime(time){
	if(time.value.length == 2){
			time.value = time.value+":00";
		}
		if(time.value.length == 1){
			time.value = "0"+time.value+":00";
		}
}
function makeblank(timefield){
	timefield.value="";
}
var operationDoctor = '';
var anaesteciaDoctor= '';
function doctorAutoComplete(field,dropdown,list,type){

		YAHOO.example.ACJSAddArray = new function() {
			localDs = new YAHOO.util.LocalDataSource(list,{ queryMatchContains : true });
			localDs.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
			localDs.responseSchema = {

				resultsList : "doctors",
				fields: [ {key : "DOCTOR_NAME"},
						  {key: "DOCTOR_ID"}
						]
			};

			var autoComp = new YAHOO.widget.AutoComplete(field, dropdown, localDs);

			autoComp.prehightlightClassName = "yui-ac-prehighlight";
			autoComp.typeAhead = true;
			autoComp.useShadow = true;
			autoComp.allowBrowserAutocomplete = false;
			autoComp.minQueryLength = 0;
			autoComp.maxResultsDisplayed = 20;
			autoComp.autoHighlight = true;
			autoComp.forceSelection = true;
			autoComp.animVert = false;
			autoComp.useIFrame = true;
			autoComp.formatResult = Insta.autoHighlight;

			var itemSelectHandler = function(sType, aArgs) {
				if(type == 'Surgeon'){
					document.getElementById("surgeon").value =  aArgs[2][1];
				}else{
					document.getElementById("anaesthetist").value =  aArgs[2][1];
				}
			};
			autoComp.itemSelectEvent.subscribe(itemSelectHandler);
		 }
}
function addOperations(surgeon,anastetist,operation_id){
	var innerrtable = document.getElementById("inneroperationtable");
	var innertr = document.createElement("tr");
	innerrtable.appendChild(innertr);
	var innertd = document.createElement("td");
	innertd.innerHTML = '<input type="hidden" name="operation_name" id="operation_name" value="'+operation_id+'" />';
	innertr.appendChild(innertd);

	innertd = document.createElement("td");
	innertd.innerHTML = '<input type="hidden" name="patient_id" id="patient_id" value="'+document.mainform.patid.value+'" />';
	innertr.appendChild(innertd);

	innertd = document.createElement("td");
	innertd.innerHTML = '<input type="hidden" name="mr_no" id="mr_no" value="'+document.getElementById("mrno").value+'" />';
	innertr.appendChild(innertd);

	innertd = document.createElement("td");
	innertd.innerHTML = '<input type="hidden" name="operation_time" id="operation_time" value="'+document.getElementById("starttime").value+':00'+'" />';
	innertr.appendChild(innertd);

	innertd = document.createElement("td");
	innertd.innerHTML = '<input type="hidden" name="theatre_name" id="theatre_name" value="'+document.mainform.theatre.value+'" />';
	innertr.appendChild(innertd);

	innertd = document.createElement("td");
	innertd.innerHTML = '<input type="hidden" name="start_date" id="start_date" value="'+document.getElementById("startdate").value+'" />';
	innertr.appendChild(innertd);

	innertd = document.createElement("td");
	innertd.innerHTML = '<input type="hidden" name="expected_end_time" id="expected_end_time" value="'+document.getElementById("endtime").value+':00'+'" />';
	innertr.appendChild(innertd);

	innertd = document.createElement("td");
	innertd.innerHTML = '<input type="hidden" name="end_date" id="end_date" value="'+document.getElementById("enddate").value+'"';
	innertr.appendChild(innertd);

	innertd = document.createElement("td");
	innertd.innerHTML = '<input type="hidden" name="surgeon" id="surgeon" value="'+surgeon+'"/>';
	innertr.appendChild(innertd);

	innertd = document.createElement("td");
	innertd.innerHTML = '<input type="hidden" name="anaesthetist" id="anaesthetist" value="'+anastetist+'"/>';
	innertr.appendChild(innertd);


}