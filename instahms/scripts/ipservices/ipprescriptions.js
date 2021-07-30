function initDoctorVisits(){
	editDialog();
	doctorAutoComplete("doctor1","doctorcontainer1", doctorlist,1);
	getConsultationChargeHeads(1);
}
function dateandtime(date,time) {

		var months=new Array('JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC');
		document.getElementById(date).value=da;
		startTimer(document.getElementById(time));
		document.getElementById('presdate').value=da;
		startTimer(document.getElementById('prestime'));
		startTimer(document.getElementById('usetime1'));
		startTimer(document.getElementById('tilltime1'));
	}


	var sysdate = getServerTime().getTime();
	var currTime = new Date().getTime();
	var dateDiff = sysdate - currTime;
	var tdVariable = "";

	function startTimer(timerTimeObj){
		tdVariable = timerTimeObj;
		showTimer();
	}

	function showTimer() {
		var myDt = new Date().getTime();
		myDt += dateDiff;
		var myDate = new Date(myDt);
		var hour = myDate.getHours();
	    var min = myDate.getMinutes();

		if (min <= 9) {
	      min = "0" + min;
	    }
	    tdVariable.value = ((hour<=9) ? "0" + hour : hour) + ":" + min  ;
	}
	var sysdate = getServerTime().getTime();

	var NoofVisitsDeleted = 0;
	function addRowToDocotrVisitTable(){
				var doctorVisitTab = document.getElementById("doctorVisitTab");
				var len = doctorVisitTab.rows.length;
				if(YAHOO.util.Dom.get("doctor"+(len-1)).value != ""){
				var tdObj="", trObj="";
				var docRow = "docRow" + len;
				trObj = doctorVisitTab.insertRow(len);
				trObj.id = docRow;
				if(NoofVisitsDeleted > 0){
					len = len + NoofVisitsDeleted;
				}

				var doctor = "doctor" + len;
				var doctorId = "doctorId" + len;
				var doctorcontainer= "doctorcontainer" + len;
				var doctorac = "doctorac" + len;
				var visitdate = "visitdate" + len;
				var doctorremarks = "doctorremarks" + len;
				var visittime = "visittime" + len;
				var doctorCheckBox = "doctorCheckBox" + len;
				var docrate = "docrate" + len;
				var docqty = "docqty" + len;
				var docamt = "docamt" + len;
				var chargetype = "chargetype" + len;
				var docDeleteCharge = "docDeleteCharge" + len;
				var docpercent = "docpercent" + len;
			    var docpayment = "docpayment" + len;
			    var docdiscount = "docdiscount" +len;

				tdObj = trObj.insertCell(0);
				tdObj.innerHTML = '<img class="imgDelete" name="'+doctorCheckBox+'" id="'+doctorCheckBox+'" src="' + cpath + '/icons/Delete.png" onclick="cancel(this,'+docDeleteCharge+','+docRow+')" /> '+
				      '<input type="hidden" name="'+docDeleteCharge+'" id="'+docDeleteCharge+'" value="false"/>';

				tdObj = trObj.insertCell(1);
				tdObj.setAttribute("class", "yui-skin-sam");
				tdObj.setAttribute("valign", "top");
				tdObj.innerHTML = '<div id="'+doctorac+'" class="autocomplete" style="width:15em;"><input id="'+doctor+'" name="'+doctor+'" type="text" style="width: 15em;" class="text-input" /><input id="'+doctorId+'" name="'+doctorId+'" type="hidden" style="width: 15em;" /> <div id="'+doctorcontainer+'" class="scrolForContainer"></div></div>';

				tdObj = trObj.insertCell(2);
				tdObj.setAttribute("valign", "top");
				tdObj.innerHTML = '<select name="'+chargetype+'" id="'+chargetype+'" onchange="populateDoctorCharge('+len+')">'+
									'</select>';

				tdObj = trObj.insertCell(3);
				tdObj.setAttribute("valign", "top");
				tdObj.innerHTML = '<input type="text" name="'+ doctorremarks +'" id="'+ doctorremarks +'" maxlength="200" style="width : 15em;" />';

				tdObj = trObj.insertCell(4);
				tdObj.setAttribute("class", "yui-skin-sam");
				tdObj.setAttribute("valign", "top");

				tdObj.innerHTML = getDateWidget(visitdate, visitdate, gServerNow,null, null, false, true);
				makePopupCalendar(visitdate);


				tdObj = trObj.insertCell(5);
				tdObj.setAttribute("valign", "top");
				tdObj.innerHTML = '<input type="text" name="'+ visittime +'" id="'+ visittime +'" '+
					'onblur="document.forms[0].moredoctorvisit.focus();populateDoctorCharge('+len+');"  style="width: 8em;" />';
				startTimer(document.getElementById(visittime));

				if(billing == 'Y'){
					tdObj = trObj.insertCell(6);
					tdObj.setAttribute("valign", "top");
					tdObj.innerHTML = '<input type="text" name="'+ docqty +'" id="'+ docqty +'"  style="width: 5em;"value="1" readonly  '+
									  '	onblur="return resetAmounts('+docrate+','+docqty+','+docamt+');" onkeypress="return enterNumOnly(event);"/>'+
									  '<input type="hidden" name="'+ docrate +'" id="'+ docrate +'"  style="width: 5em;" value="0" readonly />';


					tdObj = trObj.insertCell(7);
					tdObj.setAttribute("valign", "top");
					tdObj.innerHTML = '<input type="text" name="'+ docamt +'" id="'+ docamt +'"  style="width: 5em;" value="0" readonly />'+
						' <input type="hidden" name="'+docpercent+'" id="'+docpercent+'" style="width: 5em;" readonly value="0" /> '+
						' <input type="hidden" name="'+docpayment+'" id="'+docpayment+'" style="width: 5em;" readonly value="0" /> '+
						' <input type="hidden" name="'+ docdiscount +'" id="'+ docdiscount +'"  style="width: 5em;" value="0" readonly /> ';
				}
				else {
					tdObj = trObj.insertCell(6);
                    tdObj.setAttribute("valign", "top");
                    tdObj.innerHTML = '<input type="hidden" name="'+ docqty +'" id="'+ docqty +'"  style="width: 5em;"value="1" readonly  '+
                                                  '     onblur="return resetAmounts('+docrate+','+docqty+','+docamt+');" onkeypress="return enterNumOnly(event);"/>'+
                                                  '<input type="hidden" name="'+ docrate +'" id="'+ docrate +'"  style="width: 5em;" value="0" readonly />';


                    tdObj = trObj.insertCell(7);
                    tdObj.setAttribute("valign", "top");
                    tdObj.innerHTML = '<input type="hidden" name="'+ docamt +'" id="'+ docamt +'"  style="width: 5em;" value="0" readonly />'+
                            ' <input type="hidden" name="'+docpercent+'" id="'+docpercent+'" style="width: 5em;" readonly value="0" /> '+
                            ' <input type="hidden" name="'+docpayment+'" id="'+docpayment+'" style="width: 5em;" readonly value="0" /> '+
                            ' <input type="hidden" name="'+ docdiscount +'" id="'+ docdiscount +'"  style="width: 5em;" value="0" readonly /> ';

				}
				doctorAutoComplete(doctor,doctorcontainer,doctor_list,len);
				getConsultationChargeHeads(len);
				YAHOO.util.Dom.get(doctor).focus();
				}
			}

function setParams(obj){
	if(dataentered){
			alert("Please save your changes");
		}else {
     	var href = obj.getAttribute("href");
		href = cpath+"/pages/ipservices/Ipservices.do?_method=getNewBorn&mrno="+mrno+"&patientid="+patientid;
		obj.setAttribute("href",href);
		return true;
		}
}

function getDisReport(obj){
	if(dataentered){
		alert("Please save your changes");
		return false;
		}else{
			var mrNo = document.getElementById("mrno").value;
			var PatId = document.getElementById("patientid").value;
			var href = obj.getAttribute("href");
			var href1 = href+"&Mrno="+mrNo+"&PatId="+PatId+"";
			obj.setAttribute("href",href1);
			return true;
	}
}

function getConsultationChargeHeads(len){

	var patType = document.getElementById("patientType").value;
	var chargetype = "chargetype" + len;
	var consultationChargeHeadsJSON = null;

	if(chargeHeadsJSON != null){
		var doctorChargeHeads  = filterList(chargeHeadsJSON,"CHARGEGROUP_ID","DOC");

		if(patType == 'o'){
			var consultationChargeHeadsJSON = filterList(doctorChargeHeads,"OP_APPLICABLE","Y");
		}else if(patType == 'i'){
			var consultationChargeHeadsJSON = filterList(doctorChargeHeads,"IP_APPLICABLE","Y");
		}

		YAHOO.util.Dom.get(chargetype).options.length = consultationChargeHeadsJSON.length +1;

		var option = new Option("...Select...","");
		YAHOO.util.Dom.get(chargetype).options[0]= option;

		for(var i=0;i<consultationChargeHeadsJSON.length;i++){
			 var item = consultationChargeHeadsJSON[i];
			 var head_name = item["CHARGEHEAD_NAME"];
			 var head_id = item["CHARGEHEAD_ID"];
			 var option = new Option(head_name,head_id);
			 YAHOO.util.Dom.get(chargetype).options[i+1]= option;
			 if((patType == 'i') && (head_id == "IPDOC")){
				YAHOO.util.Dom.get(chargetype).options[i+1].selected = true;
			 }else if((patType == 'o') && (head_id == "OPDOC")){
				YAHOO.util.Dom.get(chargetype).options[i+1].selected = true;
			 }
		}
	}
}

function editDialog() {
	addDoctorDialog = new YAHOO.widget.Dialog("doctorVisitDiag",
			{
				width:"500px",
				context : ["dialogId", "tl", "bl"],
				visible:false,
				modal:true,
				constraintoviewport:true,

			} );
	YAHOO.util.Event.addListener("doctorVisitOkBtn", "click", handleSubmit, addDoctorDialog, true);
	YAHOO.util.Event.addListener("doctorVisitCancelBtn", "click", handleCancel, addDoctorDialog, true);
	subscribeKeyListeners(addDoctorDialog);
	addDoctorDialog.render();
}
function subscribeKeyListeners(dialog) {
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
			{ fn:handleCancel, scope:dialog, correctScope:true } );

	// Alt+Shift+K
	var okButtonListener = new YAHOO.util.KeyListener(document, { alt:true, shift: true, keys:75 },
			{ fn:handleSubmit, scope:dialog, correctScope:true } );
	addDoctorDialog.cfg.setProperty("keylisteners", [escKeyListener, okButtonListener]);
}

function funAddNewDoctorVisit(){
	if(!(checkBillStatus("Add Doctor Visit","",document.getElementById("patientId").value,null)))
		return false;

	addDoctorDialog.cfg.setProperty("context",["dialogId", "tl", "bl"], false);
	addDoctorDialog.show();
}

function handleSubmit(){

	if(YAHOO.util.Dom.get('doctor1').value == ""){
		alert("Please select Doctor");
		YAHOO.util.Dom.get('doctor1').focus();
		return false;
	}
	if(document.forms[0].chargetype1.value == ""){
		alert("Please select Consultation");
		document.forms[0].chargetype1.focus();
		return false;
	}
	if (document.forms[0].visit_date.value == "") {
		alert("Please select visit date");
		document.forms[0].visit_date.focus();
		return false;
	}
	var dateValidity =  (allowBackDateRights == 'A' || roleId <= 2) ? '' : 'future';
	if (!doValidateDateField(document.forms[0].visit_date)) {
		return false;
	}
	if (!doValidateTimeField(document.forms[0].visit_time)) {
		return false;
	}
	var d = getDateFromField(document.forms[0].visit_date);
	var time = document.forms[0].visit_time.value.split(":");
	d.setHours(time[0]);
	d.setMinutes(time[1]);
	var errorStr = validateDateTime(d, dateValidity);
	if (errorStr != null) {
		alert(errorStr);
		return false;
	}

	addDoctorDialog.hide();
	document.forms[0].action="Ipservices.do?_method=AddNewDoctorVisit";
	document.forms[0].submit();
}

function handleCancel() {
	this.cancel();
}
