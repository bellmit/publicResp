var toolbar = {};
	toolbar.AddorEdit= {
		title: toolbarOptions["editvisitappointment"]["name"],
		imageSrc: "icons/Edit.png",
		href: '#',
		onclick: null,
		description: toolbarOptions["editvisitappointment"]["description"]
	};

	toolbar.Arrived= {
		title: toolbarOptions["arrived"]["name"],
		imageSrc: "icons/Edit.png",
		href: '#',
		onclick: null,
		description: toolbarOptions["arrived"]["description"]
	};

	toolbar.Completed= {
		title: toolbarOptions["completed"]["name"],
		imageSrc: "icons/Edit.png",
		href: '#',
		onclick: null,
		description: toolbarOptions["completed"]["description"]
	};

	toolbar.Reschedule= {
		title: toolbarOptions["rescheduleappointment"]["name"],
		imageSrc: "icons/Replace.png",
		href: '#',
		onclick: null,
		description: toolbarOptions["rescheduleappointment"]["description"]
	};

	toolbar.PasteAppointment= {
		title: toolbarOptions["pasteappointment"]["name"],
		imageSrc: "icons/Replace.png",
		href: '#',
		onclick: null,
		description: toolbarOptions["pasteappointment"]["description"]
	};

	toolbar.PrintAppointment= {
		title: toolbarOptions["printappointment"]["name"],
		imageSrc: "icons/Print.png",
		href: 'todaysappointments.do?_method=printAppointments',
		target: '_blank',
		description: toolbarOptions["printappointment"]["description"]
	};

	toolbar.MarkResourceUnavailable= {
		title: toolbarOptions["resourceunavailable"]["name"],
		imageSrc: "icons/Change.png",
		href: '#',
		onclick: null,
		description: toolbarOptions["resourceunavailable"]["description"]
	};

	toolbar.MarkResourceAvailable = {
		title: toolbarOptions["resourceavailable"]["name"],
		imageSrc: "icons/Cancel.png",
		href: '#',
		onclick: null,
		description: toolbarOptions["resourceavailable"]["description"]
	};

	toolbar.EditResource= {
		title: toolbarOptions["resourcetimings"]["name"],
		imageSrc: "icons/Edit.png",
		href: '#',
		onclick: null,
		description: toolbarOptions["resourcetimings"]["description"]
	};

	toolbar.AuditLog= {
		title: toolbarOptions["auditlog"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'AuditLogSearch.do?_method=getAuditLogDetails',
		target: '_blank',
		description: toolbarOptions["auditlog"]["description"]
	};

var isAutoCompSelected = null;
var oAutoComp = null;
var setHrefs = function (params, id, enableList, toolbarKey) {
	if (empty(toolbarKey)) toolbarKey = defaultKey;
	var i = 0;
	var toolbar = gToolbars[toolbarKey];
	for (var key in toolbar) {
		var data = toolbar[key];
		var anchor = document.getElementById('toolbarAction' + toolbarKey + key);
		var href = data.href;
		if (!empty(anchor) && !empty(href)) {
			// append the params
			for (var paramname in params) {
				var paramvalue = params[paramname]
				href += "&" + paramname + "=" + paramvalue;
			}

			if (key == 'AddorEdit') {
				anchor.href = 'javascript:showAddorEditDialog("' + params['slotIndex']
									+ '","' + params['slotClass'] + '","' + params['dialogpopup']
									+ '","' + params['appntIds'] + '","' + params['addEdit']
									+ '","' + params['schName'] + '","' + params['overbook_limit']
									+ '","' + params['rowtime'] + '","' + params['appointmentId']
									+ '","' + params['slotTime'] + '","' + params['index']
									+ '","' + params['colDate'] + '","' + params['mrNo'] + '","' + params['contactId']
									+ '","' + params['visit_id'] + '","' + params['appt_applicable']
									+ '","' + params['defaultDur'] + '","' + params['apptDur']
									+ '","' + params['appointmentStatus']+'","'+params['centerId']
									+ '", "' + params['apptList']+'","Not Arrived")';

			} else if (key == 'Arrived') {
				anchor.href = 'javascript:checkMultipleVisitAndRegisterIfArrived("'
									+ params['slotIndex'] + '","' + params['slotClass']
									+ '","' + params['dialogpopup'] + '","' + params['appntIds']
									+ '","' + params['addEdit'] + '","' + params['schName']
									+ '","' + params['overbook_limit'] + '","' + params['rowtime']
									+ '","' + params['appointmentId'] + '","' + params['slotTime']
									+ '","' + params['index'] + '","' + params['colDate']
									+ '","'	+ params['mrNo'] + '","'	+ params['contactId'] + '","' + params['visit_id']
									+ '","'	+ params['appt_applicable'] + '","' + params['defaultDur'] + '","' + params['paymentStatus']
									+ '","' + params['apptDur'] + '","' + params['appointmentStatus']+'","'+params['centerId']
									+ '", "' + params['apptList']+'", "Arrived")';

			} else if (key == 'Completed') {
				anchor.href = 'javascript:changeAppoinmentStatusToCompleted("' + params['slotIndex']
									+ '","' + params['appointmentId']
									+ '","' + params['appointmentStatus']
									+ '", "' + category + '")';

			} else if (key == 'Reschedule') {
				anchor.href = 'javascript:setCookieToRescheduleAppointment("' + params['appointmentId']
									+ '", "' + params['dialogpopup']
									+ '","' + params['colDate'] + '")';

			} else if (key == 'PasteAppointment') {
				anchor.href = 'javascript:shiftAppointment("' + params['rowtime']
									+ '", "' + params['schName'] + '","' + params['colDate'] + '","'+params['category']+'","'+params['overbook_limit']
									+ '","' + params['appt_applicable'] + '", "' + params['apptList']+'")';

			} else if (key == 'PrintAppointment') {
				for (var paramname in params) {
					var paramvalue = params[paramname]

					if (paramname == 'appointmentId') {
						anchor.href = data.href + '&appointment_id=' + params['appointmentId'];
					}
				}
				anchor.href = anchor.href + '&category=' + category;
				anchor.href = anchor.href + '&print_type=' + document.getElementById("printType").value;

			} else if (key == 'MarkResourceUnavailable') {
				anchor.href = 'javascript:showResourceNonAvailabilityDialog("' + params['slotIndex']
										+ '","' + params['slotClass'] + '","' + params['schName']
										+ '", "' + params['rowtime'] + '","' + params['index']
										+ '","' + params['colDate'] + '","' + params['resourceAvailabiltyCenterId'] + '")';

			} else if (key == 'MarkResourceAvailable') {
					if(category == 'DOC' && max_centers_inc_default > 1){
						anchor.href = 'javascript:showResourceAvailabilityDialog("' + params['slotIndex']
						+ '","' + params['slotClass'] + '","' + params['schName']
						+ '", "' + params['rowtime'] + '","' + params['index']
						+ '","' + params['colDate'] + '" ,"' + params['resourceUnAvailCenterId'] + '")';

					}else{
				anchor.href = 'javascript:markResourceAvailable("' + params['schName']
										+ '", "' + params['rowtime'] + '","' + params['index']
										+ '","' + params['colDate'] + '" )';
					}

			} else if (key == 'EditResource') {
				var sIndex = params['slotIndex'];
				var resourceType = category;
				var resourceName =  params['schName'];
				var colDate = params['colDate'];
				var url = editResourceTimings(sIndex, resourceName, resourceType, colDate);
				anchor.href = url;
			}else if (key = 'AuditLog') {
				for (var paramname in params) {
					var paramvalue = params[paramname]

					if (paramname == 'appointmentId') {
						anchor.href = cpath + '/schedulerAppointments/auditlog/AuditLogSearch.do?_method=getAuditLogDetails' + '&appointment_id=' + params['appointmentId'];
					}
				}
				anchor.href = anchor.href + '&al_table=scheduler_appointments_audit_log'+'&appointment_id@type=integer';

			} else {
				anchor.href = cpath + "/" + href;
			}
			var centerId = document.getElementById("centerId").value;
			var cookie_value = getSchedulerCookies("reschedulepatient");

			// when enabling the arrived menu item check for logged in center
			// and center concept is enabled or not
			// if the center concept is enabled and logged in center is super center
			// then disable the Arrived menu item.
			if (enableList) {
				if (key == 'Reschedule') {
					if (!empty(params['packageId'])) {
						enableToolbarItem(key, false);
					} else if(!empty(cookie_value))
						enableToolbarItem(key, false);
					else if(max_centers_inc_default > 1 && loggedInCenterId == 0 && centerId == 0 && category == 'DOC'){
						enableToolbarItem(key, false);
					}
					else if(params['appointmentId'] == 'No') {
						enableToolbarItem(key, false);
					} else if ((params['appointmentStatus'] == 'Arrived')) {
						enableToolbarItem(key, false);
					} else {
						enableToolbarItem(key, true);
					}

				} else if (key == 'PasteAppointment' && !empty(cookie_value)) {
					if (!empty(params['packageId'])) {
						enableToolbarItem(key, false);
					}
					else if(max_centers_inc_default > 1 && loggedInCenterId == 0 && centerId == 0 && category == 'DOC'){
						enableToolbarItem(key, false);
					}
					else if (diffCurrentnColumnDate(params['colDate'])){
						     if( actionRight == 'A'){
						        enableToolbarItem(key, true);
						     } else {
						        enableToolbarItem(key, false);
						     }
						}
					else if (params['dialogpopup'] == 'true') {
							enableToolbarItem(key, true);

							if ((params['appointmentId'] != 'No' && params['overbook_limit'] != "" && overbookAllowed(params['overbook_limit'])== false)
									|| params['slotClass'].indexOf('notAvailble') != -1) {
								enableToolbarItem(key, false);
							} else {
								enableToolbarItem(key, true);
							}
					}else enableToolbarItem(key, false);

				} else if (key == 'AddorEdit'){
					if (!empty(params['packageId'])) {
						enableToolbarItem(key, false);
					} else if(!empty(cookie_value)) {
						enableToolbarItem(key, false);
					} else if (params['appointmentId'] == 'No' && (params['slotClass'].indexOf('notAvailble') != -1)) {
						enableToolbarItem(key, false);
					} else if (diffCurrentnColumnDate(params['colDate'])){
					     if( actionRight == 'A'){
					        enableToolbarItem(key, true);
					     } else {
					        enableToolbarItem(key, false);
					     }
					} else if(addeditAppointment == 'N' && (roleId !=1 || roleId !=2)) {
						enableToolbarItem(key, false);
					}
					else {
					   enableToolbarItem(key, true);
					}

				} else if (key == 'AuditLog' && empty(cookie_value)) {
					if (params['appointmentId'] == 'No') {
						enableToolbarItem(key, false);
					} else {
						enableToolbarItem(key, true);
					}

				} else if (key == 'PrintAppointment') {
					if (params['appointmentId'] == 'No' || !empty(cookie_value)) {
						enableToolbarItem(key, false);
					} else {
						enableToolbarItem(key, true);
					}

				} else if (key == 'Completed' && empty(cookie_value)) {
					if (!empty(params['packageId'])) {
						enableToolbarItem(key, false);
					} else if (params['appointmentStatus'] == 'Arrived' && params['appointmentId'] != 'No') {
						enableToolbarItem(key, true);
						enableToolbarItem('Arrived', false);
					}
					if (params['appointmentId'] == 'No') {
						enableToolbarItem(key, false);
					}

				} else if (key == 'MarkResourceUnavailable') {
					if (resourceAvailabilityOverridesRight == 'N')
						enableToolbarItem(key, false);
					else {
						if (params['slotClass'].indexOf('notAvailble') ==  -1)
							enableToolbarItem(key, true);
						else
							enableToolbarItem(key, false);
					}

				} else if (key == 'MarkResourceAvailable') {
					if (resourceAvailabilityOverridesRight == 'N')
						enableToolbarItem(key, false);
					else {
						if (params['slotClass'].indexOf('notAvailble') != -1)
							enableToolbarItem(key, true);
						else
							enableToolbarItem(key, false);
					}

				}  else if (key == 'Arrived' && empty(cookie_value)) {
					if (!empty(params['packageId'])) {
						enableToolbarItem(key, false);
					} else if (params['appointmentId'] != 'No'
						&& (params['appointmentStatus'] == 'Booked'
							|| params['appointmentStatus'] == 'Confirmed')) {
						// disabling the arrived menu item if the logged in center is a super center
						// and center concept is enabled.
						enableToolbarItem(key, !(max_centers_inc_default > 1 && loggedInCenterId == 0));
						enableToolbarItem("Completed", false)
					} else enableToolbarItem(key, false);

				} else if (key == 'EditResource') {
					if (!empty(params['packageId']) || resourceAvailabilityOverridesRight == 'N') {
						enableToolbarItem(key, false);
					}
				} else enableToolbarItem(key, enableList[i]);

			} else enableToolbarItem(key, true);

		} else {
			debug("No anchor for " + 'toolbarAction' + key + ":");
		}

		i++;
	}
	function overbookAllowed(overbookLimit){
		if(overbookLimit != null || overbookLimit != "" || parseInt(overbookLimit) > 0)
			return true;
		return false;
	}
}

var mform;
var resForm;
var nform;
var rform;
var avlform;
var gResAvailCenterId;
var gResNotAvailCenterId;
var psAc = null;
var gregConverted = false;
var hijriConverted = false;
var gMarkAvlNonAvl = '';
var imagePath = cpath + "/images/calendar.png";
var listOfAppt;
var initialselectedResourceValue;
var initialavailbiltyList;
function init() {
   $(window).on("unload", function() {
          setSchedulerCookies("reschedulepatient", '', 0);
   });
	//$('#calendarDiv').calendarsPicker({calendar: $.calendars.instance('ummalqura'), showOnFocus: false, showTrigger: '#calendarImg', dateFormat: 'DD d MM yyyy'});
	$('#hijriImagePicker').calendarsPicker({calendar: $.calendars.instance('ummalqura'), showOnFocus: true,
		dateFormat: 'yyyy-mm-dd',  showTrigger: '<img src="' + imagePath + '" alt="Popup">', onSelect:function() {
			 var dates = $('#hijriImagePicker').calendarsPicker('getDate');
			 //alert(dates[0]);
			 if(hijriConverted && dates[0] != null) {
			 document.resourceform.hijriDate.value = dates[0];
				if (document.resourceform.choosenWeekDate != null) {
					//document.resourceform.choosenWeekDate.value = txtDate.value;
					document.resourceform.method.value = "getWeekView";
				} else {
					document.resourceform.method.value = "getScheduleDetails";
				}
				document.resourceform.includeResources.value = includeresources();
				document.mainform.includeResources.value = includeresources();
				document.resourceform.submit();
			 }
			  else {
				 hijriConverted = true;
			 }
		 }
	});

	$('#gregImagePicker').calendarsPicker({calendar: $.calendars.instance('gregorian'), showOnFocus: true,
		 dateFormat: 'yyyy-mm-dd', showTrigger: '<img src="' + imagePath + '" alt="Popup">', onSelect:function() {
			 var dates = $('#gregImagePicker').calendarsPicker('getDate');
			 //alert(dates[0]);
			 document.resourceform.gregDate.value = dates[0];
			if(gregConverted && dates[0] != null) {
				document.resourceform.gregDate.value = dates[0];
				document.resourceform.hijriDate.value = "";
				if (document.resourceform.choosenWeekDate != null) {
					//document.resourceform.choosenWeekDate.value = txtDate.value;
					document.resourceform.method.value = "getWeekView";
				} else {
					document.resourceform.method.value = "getScheduleDetails";
				}
				document.resourceform.includeResources.value = includeresources();
				document.mainform.includeResources.value = includeresources();
				document.resourceform.submit();
			 } else {
				 gregConverted = true;
			 }
		 }
	});
	if(gregDate != null) {
		var datesArr = [];
		datesArr[0] = document.resourceform.gregDate.value;
		if(datesArr[0] != null || !datesArr[0].trim() == '') {
		    $('#gregImagePicker').calendarsPicker('setDate', datesArr);
	    }
		datesArr[0] = document.resourceform.hijriDate.value;
		if(datesArr[0] != null || !datesArr[0].trim() == '') {
		    $('#hijriImagePicker').calendarsPicker('setDate', datesArr);
		}
	}
    initPrescribedDoctorAutoComplete();
    
	default_gp_first_consultation	= healthAuthoPref.default_gp_first_consultation;
	default_gp_revisit_consultation	= healthAuthoPref.default_gp_revisit_consultation;
	default_sp_first_consultation	= healthAuthoPref.default_sp_first_consultation;
	default_sp_revisit_consultation	= healthAuthoPref.default_sp_revisit_consultation;

	createToolbar(toolbar);
	viewToolTip();

	rform = document.resourceform;
	nform = document.nonAvailableForm;
	resForm = document.rescheduleForm;
	mform = document.mainform;
	avlform = document.availabilityForm;

	if (roleId == 1 || roleId == 2) {
		cancelAppointment = 'A';
	}

	if (rform.centralResource != null) {
		rform.centralResource.selectedIndex = 0;
	}

	// Init mrno search
	psAc = Insta.initMRNoAcSearch(cpath, "mrno", "mrnoAcDropdown", "active",
	function (type, args) {
		getMrnoDetails();
	}, function (type, args) {
		resetFieldValues();
	},null,"Save",null,null,true);

	initDialog();
	dialog.configClose("dialog", handleCancel, dialog);
	document.getElementById("dialog").style.display = 'block';
	// Call resource availablity dialog for doctor
	if(category == 'DOC' && max_centers_inc_default > 1){
		initDoctorAvailabilityDialog();

		availabilitydialog.configClose("availability_dialog"
			, onEditDoctorAvailabilityCancel, availabilitydialog);
	}

	initDoctorNonAvailabilityDialog();
	nonavailabilitydialog.configClose("nonAvailabilityDialog"
			, onEditDoctorNonAvailabilityCancel, nonavailabilitydialog);

	setResources();
	var elmts = document.getElementsByName("scheduleName");
	if (elmts.length < 5) {
		for (var i = elmts.length; i < 5; i++)
		addNewSchedulerResourceColumn(i);
	}

	getSchedulerCookies();
	document.getElementById("cond_doc_star").style.visibility = 'hidden';
	document.getElementById("conducting_doctor").setAttribute("disabled", true);
	initialavailbiltyList = availbiltyList;
}

function onDeptChangeSubmit() {
	rform.includeResources.value = "";
	mform.includeResources.value = "";
	document.mainform.submit();
}

function editResourceTimings(id,resourceName,resourceType,colDate) {
	document.editResourceTimingsForm.res_sch_name.value = resourceName;
	document.editResourceTimingsForm.res_sch_type.value = resourceType;
	document.editResourceTimingsForm._col_date.value = colDate;
	document.getElementById('toolbarRow' + id).className = 'rowbgToolBar';
	var url = cpath + '/master/resourceoverrides/getResourceAvailDate.htm?resourceType=' + resourceType+'&resourceName='+resourceName+'&colDate='+colDate;
	return url;
}
function changeAppoinmentStatusToCompleted(id, appId, appStatus, category) {
	document.completeAppointmentForm.category.value = category;
	document.completeAppointmentForm.appointment_status.value = appStatus;
	document.completeAppointmentForm.appointment_id.value = appId;
	document.getElementById('toolbarRow' + id).className = 'rowbgToolBar';
	document.completeAppointmentForm.submit();
}

function getResourceTimings() {
	var category = rform.category.value;
	if (category == '') return;
	var primaryResource;
	if (category == 'DOC')
		primaryResource = rform.scNames.value;
	else
		primaryResource = rform.centralResource.value;


	var scheduleDate = rform.date.value;
	var scheduleName = rform.scheduleNameForAppointment.value;

	if (!empty(category) && !empty(scheduleName)) {
		var ajaxReqObject = newXMLHttpRequest();
		var url = "./addeditappointments.do?method=getPrimaryResourceUnAvailableTimings"
		url = url + "&category=" + category;
		url = url + "&scheduleName=" + primaryResource;
		url = url + "&scheduleDate=" + scheduleDate;
		var reqObject = newXMLHttpRequest();
		reqObject.open("POST", url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ((reqObject.status == 200) && (reqObject.responseText != null)) {
				return handleAjaxTimingResponse(reqObject.responseText);
			}
		}
	}
	return true;
}

function handleAjaxTimingResponse(responseText) {
	var category = rform.category.value;
	eval("var responseOfSelectedSchedule = " + responseText);
	var resTimeList = null;

	if (responseOfSelectedSchedule != null) {
		resTimeList = responseOfSelectedSchedule;
	}
	var flag = true;
	var curdate = new Date();
	var time = rform.timeList.value;
	var timeArr = time.split(":");
		curdate.setHours(timeArr[0]);
		curdate.setMinutes(timeArr[1]);
		curdate.setSeconds(0);
	var timeValInLong = curdate.getTime();
	var fromDate = new Date();
	var toDate = new Date();

	if (resTimeList != null && resTimeList.length > 0) {
		for (var i = 0; i < resTimeList.length; i++) {
			var from_time = resTimeList[i].from_time;
			var to_time = resTimeList[i].to_time;
			var status = resTimeList[i].availability_status;
			var d1 = new Date(from_time);
				d1.setDate(fromDate.getDate());
				d1.setMonth(fromDate.getMonth());
				d1.setFullYear(fromDate.getFullYear());
				d1.setSeconds(0);

			var d2 = new Date(to_time);
				d2.setDate(toDate.getDate());
				d2.setMonth(toDate.getMonth());
				d2.setFullYear(toDate.getFullYear());
				d2.setSeconds(0);

			var fromTimeInLong = d1.getTime();
			var toTimeInLong = d2.getTime();
			if (timeValInLong > fromTimeInLong && timeValInLong < toTimeInLong && status == 'N') {
				flag = false;
			}
		}
	}
	return flag;
}

function diffCurrentnColumnDate(colDate ){
    var currdate = new Date(serverNow);
    var appDate = new Date(currdate);
    var colDates = colDate.split('-');
    appDate.setDate(colDates[0]);
    appDate.setMonth(colDates[1]-1);
    appDate.setYear(colDates[2]);
    var diff = currdate-appDate;
    if( diff > 0){
       return true;
    }
    else{
       return false;
    }
}
function loadStatus(appStatus) {
	var statusObj = rform.status;
	if (appStatus == 'New') {
		statusObj.options.length = 3;
		var option0 = new Option("-- Select --", "");
		var option1 = new Option("Booked", "Booked");
		var option2 = new Option("Appt Confirmed", "Confirmed");
		statusObj.options[0] = option0;
		statusObj.options[1] = option1;
		statusObj.options[2] = option2;

	} else if(appStatus.toLowerCase() == 'Completed'.toLowerCase() || appStatus.toLowerCase() == 'noshow'.toLowerCase() || appStatus.toLowerCase() == 'cancel'.toLowerCase()){
      statusObj.options.length = 1;
      var option0 = new Option("-- Select --", "");
      statusObj.options[0] = option0;
	}
	else if (appStatus != 'Arrived') {
		statusObj.options.length = 5;
		var option0 = new Option("-- Select --", "");
		var option1 = new Option("Booked", "Booked");
		var option2 = new Option("Appt Confirmed", "Confirmed");
		var option3 = new Option("Noshow", "Noshow");
		var option4 = new Option("Cancel", "Cancel");
		statusObj.options[0] = option0;
		statusObj.options[1] = option1;
		statusObj.options[2] = option2;
		statusObj.options[3] = option3;
		statusObj.options[4] = option4;
	} else {
		statusObj.options.length = 2;
		var option0 = new Option("-- Select --", "");
		var option1 = new Option("Cancel", "Cancel");
		statusObj.options[0] = option0;
		statusObj.options[1] = option1;
	}
}

function disableNewButton(overbook_limit, appid, apptapplicable, defaultDuration, apptDur, slotClass) {
	//listCount global variable from showDialog function calling

	if (appid != 'NO' && (overbook_limit != "" && overbook_limit == 0) || (overbook_limit != "" && overbook_limit != 0 &&  listCount > overbook_limit))
		document.resourceform.add.disabled = true;
	else
		document.resourceform.add.disabled = false;

	if (slotClass.indexOf('notAvailble') != -1)
		document.resourceform.add.disabled = true;
}

function getMappedResources(addEdit) {
	var selectedResourceValue = rform.centralResource.value;
	if(category == 'SNP' || category == 'DIA' || category == 'OPE') {
		if (!empty(isAutoCompSelected) || addEdit == 'edit') {
			scheduleName = rform.scheduleNameForAppointment.value;
			document.getElementById("centralResource").options.length = 0;
			var ajaxReqObject = newXMLHttpRequest();
			var url = "./addeditappointments.do?method=getMappedResources";
			url = url + "&category=" + category;
			url = url + "&schName=" + scheduleName;
			var reqObject = newXMLHttpRequest();
			reqObject.open("POST", url.toString(), false);
			reqObject.send(null);
			if (reqObject.readyState == 4) {
				if ((reqObject.status == 200) && (reqObject.responseText != null && reqObject.responseText !== "[]")) {
					return handleAjaxMappedResourceResponse(reqObject.responseText, initialselectedResourceValue, selectedResourceValue);
				}
				else {
                    document.getElementById("centralResource").options.length = 0;
                    for (var i = 0; i < availbiltyList.length; i++) {
                    var centralResource = document.getElementById("centralResource");
                    opt = document.createElement("option");
                    opt.value = availbiltyList[i].id;
                    opt.textContent = availbiltyList[i].schedulename;
                    centralResource.appendChild(opt);
                    }
                    if(initialselectedResourceValue) {
                       document.getElementById("centralResource").value = initialselectedResourceValue;
                       setSelectedIndex(rform.centralResource, initialselectedResourceValue);
                    }
                    else{
                       setSelectedIndex(rform.centralResource, selectedResourceValue);
                    }
                }
			}
		}
	}
}

function handleAjaxMappedResourceResponse(responseText, initialselectedResourceValue, selectedResourceValue) {
	eval("var resourceResponseList = " + responseText);
	var centralResource = document.getElementById("centralResource");
	centralResource.options.length = 1;
    var option0 = new Option("-- Select --", "");
    var flag;
    centralResource.options[0] = option0;
	if (resourceResponseList.length > 0) {
	    if(category == 'SNP') {
		  for (var i = 0; i < resourceResponseList.length; i++) {
			opt = document.createElement("option");
			opt.value = resourceResponseList[i].serv_res_id;
			opt.textContent = resourceResponseList[i].serv_resource_name;
			centralResource.appendChild(opt);
			if(initialselectedResourceValue == resourceResponseList[i].serv_res_id){
			  flag = true;
			}
		  }
		}
		if(category == 'DIA') {
		   for (var i = 0; i < resourceResponseList.length; i++) {
           	  opt = document.createElement("option");
           	  opt.value = resourceResponseList[i].eq_id;
           	  opt.textContent = resourceResponseList[i].equipment_name;
           	  centralResource.appendChild(opt);
           	  if(initialselectedResourceValue == resourceResponseList[i].eq_id){
              	flag = true;
              }
           }
		}
		if (category == 'OPE') {
			for (var i = 0; i < resourceResponseList.length; i++) {
				opt = document.createElement("option");
				opt.value = resourceResponseList[i].theatre_id;
				opt.textContent = resourceResponseList[i].theatre_name;
				centralResource.appendChild(opt);
				if (initialselectedResourceValue == resourceResponseList[i].theatre_id) {
					flag = true;
				}
			}
		}
	}
	if(flag){
	  document.getElementById("centralResource").value = initialselectedResourceValue;
	  setSelectedIndex(rform.centralResource, initialselectedResourceValue);
	}
	if(!initialselectedResourceValue){
       document.getElementById("centralResource").value = selectedResourceValue;
       setSelectedIndex(rform.centralResource, selectedResourceValue);
    }
}

/* Loads appointment category master related default duration,
 * time slots and secondary resources.
 */
function getResourceList() {
	var category = rform.category.value;
	if (empty(category)) return;

	var scheduleDate = rform.date.value;
	var scheduleName = null;
	var resourceType = null;

	if(!empty(isAutoCompSelected)) {
		if(category == 'DOC') {
			resourceType = 'DOC';
		} else if(category == 'OPE') {
			resourceType = 'SUR';
		} else if(category == 'DIA') {
			resourceType = 'TST';
		} else if(category == 'SNP') {
			resourceType = 'SER';
		}
		scheduleName = rform.scheduleNameForAppointment.value;
	} else {
		if(category == 'DOC') {
			resourceType = 'DOC'
			scheduleName = rform.scheduleNameForAppointment.value;
		} else if(category == 'OPE') {
			resourceType = 'THID';
			scheduleName = rform.centralResource.value;
		} else if(category == 'DIA') {
			resourceType = 'EQID';
			scheduleName = rform.centralResource.value;
		} else if(category == 'SNP') {
			resourceType = 'SRID';
			scheduleName = rform.centralResource.value;
		}
	}

	if (!empty(category) && !empty(scheduleName)) {
		var ajaxReqObject = newXMLHttpRequest();
		var resourceCenterId = document.getElementById("centerId").value;
		resourceCenterId = !empty(resourceCenterId) ? resourceCenterId : 0;
		var url = "./addeditappointments.do?method=getResourceList";
		url = url + "&category=" + category;
		url = url + "&scheduleName=" + scheduleName;
		url = url + "&scheduleDate=" + scheduleDate;
		url = url + "&from_time="+timingsJson.startTime;
		url = url + "&to_time="+timingsJson.endTime;
		url = url + "&resource_type="+resourceType;
		url = url + "&resourceCenterId="+ resourceCenterId;
		var reqObject = newXMLHttpRequest();
		reqObject.open("POST", url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ((reqObject.status == 200) && (reqObject.responseText != null)) {
				handleAjaxResponse(reqObject.responseText,resourceType);
			}
		}
	}
}

var selectedtime = null;

function handleAjaxResponse(responseText,resourceType) {
	var category = rform.category.value;
	rform.duration.value = rform.defaultDuration.value;
	eval("var responseOfSelectedSchedule = " + responseText);
	if (resourceType == 'SER' || resourceType == 'TST' || resourceType == 'SUR') {
		rform.duration.value = responseOfSelectedSchedule["defaultDuration"];
	} else {
		var timeList = null;
		if (responseOfSelectedSchedule != null) {
			timeList = responseOfSelectedSchedule.timeList;
		}

		rform.time.length = 1;
		var curdate = new Date();

		if (timeList != null && timeList.length > 0) {
			for (var i = 0; i < timeList.length; i++) {
				curdate.setTime(timeList[i]);
				var hours = curdate.getHours();
				if (hours < 10) {
					hours = '0' + hours;
				}
				var minutes = curdate.getMinutes();
				if (minutes < 10) {
					minutes = '0' + minutes;
				}
				var time = hours + ':' + minutes;
				var option = new Option(time, time);
				rform.time.options[rform.time.length] = option;
			}
		}/*
			 * else { for (var i = 0; i < defaultTimeslotJSON.length; i++) {
			 * curdate.setTime(defaultTimeslotJSON[i]); var hours =
			 * curdate.getHours(); if (hours < 10) { hours = '0' + hours; } var
			 * minutes = curdate.getMinutes(); if (minutes < 10) { minutes = '0' +
			 * minutes; } var time = hours + ':' + minutes; var option = new
			 * Option(time, time); rform.time.options[rform.time.length] =
			 * option; } return; }
			 */

		if (selectedtime != null && selectedtime != '') {
			setSelectedIndex(rform.time, selectedtime);
		}

		rform.duration.value = responseOfSelectedSchedule["defaultDuration"];

		var selectedResourceList = responseOfSelectedSchedule.resourceList;
		if (selectedResourceList != null && selectedResourceList.length > 0) {
			var table = document.getElementById("resourceTable");
			var rowCount = table.rows.length;
			var itemIdTypeArr = new Array();
			var k = 0;
			if (rowCount > 0) {
				for (var i = 0; i < rowCount; i++) {
					if (document.getElementById("resourceName" + i) != null
							&& document.getElementById("resourceName" + i).value != '') {

						itemIdTypeArr[k] = [
								document.getElementById("resourceName" + i).value,
								document.getElementById("appointment_item_id"
										+ i).value ]
						k++;
					}
				}
			}
			deleteRows();
			for (var i = 0; i < selectedResourceList.length; i++) {
				var record = selectedResourceList[i];
				addResourceRow();
				var resourceName = "resourceName" + (i);
				var resourceNameObj = document.getElementById(resourceName);

				var resourceValue = "resourceValue" + (i);
				var resourceValueObj = document.getElementById(resourceValue);

				setSelectedIndex(resourceNameObj, record["resouceType"]);
				populateResources(i);
				setSelectedIndex(resourceValueObj, record["resouceId"]);
			}
			rowCount = table.rows.length;
			if (rowCount > 0 && itemIdTypeArr.length > 0) {
				for (var i = 0; i < rowCount; i++) {
					if (document.getElementById("resourceName" + i) != null
							&& document.getElementById("resourceName" + i).value != '') {
						for (var j = 0; j < itemIdTypeArr.length; j++) {
							if (document.getElementById("resourceName" + i).value == itemIdTypeArr[j][0]) {
								document.getElementById("appointment_item_id"
										+ i).value = itemIdTypeArr[j][1];
							}
						}
					}
				}
			}
		}
	}
	showPriorAuthDetailsRow();
}

var priorAuthRequiredForSurgery;
function showPriorAuthDetailsRow() {
	if (category == 'OPE' && document.getElementById('scheduler_prior_auth_row')) {
		var resourceId = document.getElementById('scheduleNameForAppointment').value;
		if (!empty(resourceId)) {
			var	list = findInList(surgeriesJson,"OP_ID",resourceId);
			priorAuthRequiredForSurgery = list['PRIOR_AUTH_REQUIRED'];
			if (!empty(priorAuthRequiredForSurgery) && priorAuthRequiredForSurgery != 'N') {
				document.getElementById('scheduler_prior_auth_row').style.display = "table-row";
			} else {
				document.getElementById('scheduler_prior_auth_row').style.display = "none";
			}
		}

	}
}

/* showing the add/edit dialog box. */

var slotIndexId = 0;
var gIsInsurance = false;
var gArrived;
var gschName;

function showAddorEditDialog(id, slotClass, dialogpopup, appntIds, addEdit, schName,
	overbook_limit, rowTime, apptid, slotTime, index, colDate, mrNo, contactId, visitId, apptapplicable,
	defaultDuration, apptDur, appointStatus,centerId, apptList, arrived) {

	prevClass = document.getElementById('toolbarRow' + id).className;
	slotIndexId = id;
	gArrived = arrived;
	isAutoCompSelected = null;
	gschName = schName;
	if(max_centers_inc_default > 1) {
		if(loggedInCenterId == 0) {
			if(addEdit == 'add') {
			} else {
				rform._center_id.value = centerId;
				rform.center_id.value = centerId;
			}
		} else {
			rform._center_id.value = loggedInCenterId;
			rform.center_id.value = loggedInCenterId;
			centerId = loggedInCenterId;
		}
	} else {
		rform._center_id.value = 0;
		rform.center_id.value = 0;
		centerId = 0;
	}

	if(max_centers_inc_default > 1)
		document.getElementById('appt_center_div').style.display = 'none';

	if (apptapplicable == 'false' && overbook_limit == 0) {
		showMessage("js.scheduler.doctorscheduler.slot.alreadybooked");
		document.getElementById('toolbarRow' + id).className = '';
		return;
	}

	document.getElementById('toolbarRow' + id).className = slotClass;
	rform.date.value = colDate;
	var duration = rform.duration.value;
	document.getElementById("datelbl").textContent = colDate;
	document.getElementById('appointdate').value = colDate + ' ' + slotTime;
	selectedtime = rowTime;
	gIsInsurance = false;
	availbiltyList = initialavailbiltyList;

	showDialog(id, slotClass, dialogpopup, appntIds, addEdit, slotTime,
		colDate, duration, overbook_limit, apptid, apptapplicable, defaultDuration,
		apptDur, mrNo, contactId, visitId, appointStatus,centerId, apptList, arrived);

	centerResourcesList = resourcesListJSON;
	if( max_centers_inc_default >1 && addEdit == 'add') {
		if(category != 'DOC') {
			var centerList = null;
			var primaryResource = rform.centralResource.value;
			if(!empty(primaryResourceCentersList)) {
				centerList = filterList(primaryResourceCentersList,"RESOURCE_ID",schName);
			}

			if(!empty(centerList)) {
				rform.center_id.value = centerList[0]['CENTER_ID'];
				rform._center_id.value = centerList[0]['CENTER_ID'];
				centerId = centerList[0]['CENTER_ID'];
			}
		}
	}
	getMappedResources('edit');

	if(max_centers_inc_default > 1) {
		if(category == 'DOC') {
			rform._appointmentCenterId.value = empty(centerId) ? loggedInCenterId : centerId;
		} else {
			centerResourcesList = filterList(resourcesListJSON,"center_id",rform.center_id.value);
		}
	}

	if (rform.centralResource != null) {
		setSelectedIndex(rform.centralResource, schName);
	} else {
		setSelectedIndex(rform.scheduleNameForAppointment, schName);
	}

	if (apptid != '' && apptid != 'No') {
		getResourceList();
		if(!empty(oAutoComp)){
			oAutoComp.destroy();
		}
		scheduleNameAutoComplete(schName);
		getAppointmentDetails(apptid, appntIds, dialogpopup, slotTime, index);
	} else {
		resetFieldValues();
		resetRecurranceTab();
		showHiderecurringTable(true);
		if(!empty(oAutoComp)){
			oAutoComp.destroy();
		}
		scheduleNameAutoComplete(schName);
		getResourceList();
		setSelectedIndex(rform.time, rowTime);
		rform.method.value = "saveAppointment";
	}

	rform.overbook_limit.value = overbook_limit;



	if (addEdit == 'edit') {
		resetRecurranceTab();
		showHiderecurringTable(false);
		getMappedResources(addEdit);
		var sNames = document.getElementById("scNames").value;
		if (sNames != '' && typeof(resNames) != 'undefined') {
			for (var i = 0; i < resNames.length; i++) {
				if (resNames[i]["resource_id"] == document.getElementById("scheduleNameForAppointment").value) {
					if (category == "DIA"){
						document.getElementById("conductingDoctorMandatory").value = resNames[i]["conducting_doc_mandatory"];
						if (document.getElementById("conductingDoctorMandatory").value=='O'){
							document.getElementById("cond_doc_star").style.visibility = 'visible';
							document.getElementById("conducting_doctor").removeAttribute("disabled");
						} else {
							document.getElementById("cond_doc_star").style.visibility = 'hidden';
							document.getElementById("conducting_doctor").setAttribute("disabled", true);
							document.getElementById("conducting_doctor").value = '';
							document.getElementById("cond_doc_id").value = '';
						}
						condDoctorsJson = filterList(doctorsJson,"dept_id",resNames[i]["category"]);
						initConductedDoctorAutoComplete();
					}
					break;
				}
			}
		}
	}

	if (arrived == 'Arrived') {
		document.getElementById("visitIdRow").style.display = 'table-row';
		document.getElementById("statusRow").style.display = 'none';
		document.getElementById("prenxtdiv").style.display = 'none';
		rform.patient_id.focus();
	}else {
		document.getElementById("visitIdRow").style.display = 'none';
		document.getElementById("statusRow").style.display = 'table-row';
	}
	if(category == 'DOC' && max_centers_inc_default > 1) {
		populateCenters(document.getElementById('scNames'),addEdit);
	}
    initialselectedResourceValue = rform.centralResource.value;
    rform.centralResource.disabled = true;
}

var oldClass = '';
var slotobj = null;
var addOrEdit = null;
var listCount;

function showDialog(id, slotClass, dialogpopup, appntIds, addEdit, slotTime, colDate, duration,
	overbook_limit, apptid, apptapplicable, defaultDuration, apptDur, mrno, contactId, visitId, appointStatus,centerId, apptList, arrived) {
	/*if (dialogpopup=='false'&& (appntIds != 'null' || appntIds.length==0)) {
		showMessage("Appointments cannot be booked in the past");
		YAHOO.lutsr.accordion.collapse(document.getElementById("recurranceDD"));
		return false;
	}*/
	if (category == 'OPE' && document.getElementById('scheduler_prior_auth_row')) {
		document.getElementById('scheduler_prior_auth_row').style.display = "none";
		document.getElementById('scheduler_prior_auth_row').style.display = "none";
		document.getElementById('scheduler_prior_auth_no').value = "";
		document.getElementById('scheduler_prior_auth_mode_id').value = "";
	}
	addOrEdit = addEdit;
	rform.mrno.value = mrno;
	if(rform.contactId != undefined){
		rform.contactId.value = contactId;	
	}
	getMrnoDetails();

	document.resourceform.arrived.value = arrived;
	apptStatus = appointStatus;

	if (arrived == 'Arrived') {
		document.getElementById('status_visit_label').textContent = 'Visit Association';
	} else {
		document.getElementById('status_visit_label').textContent = getString('js.scheduler.resourceavailability.appointmentstatus');
	}

	if (appointStatus == 'Arrived') {
		document.getElementById('currentstatus').textContent = "Arrived("+visitId+")";
		document.getElementById('editStatus').value = appointStatus;
		document.getElementById('statusStar').style.display = 'none';
	} else {
		document.getElementById('currentstatus').textContent = appointStatus;
		document.getElementById('editStatus').value = appointStatus;
		document.getElementById('statusStar').style.display = '';
	}

	document.resourceform.duration.value = defaultDuration;
	document.getElementById("scNames").value = "";
	document.getElementById("scNames").removeAttribute("title");
	rform._center_id.value = centerId;

	if (dialogpopup == 'true')
		slotTimeNull();

	if (addEdit == 'add') {
		document.getElementById("prenxtdiv").style.display = 'none';
		document.getElementById('currentStatusField').style.display = 'none';
		document.getElementById('currentStatusValue').style.display = 'none';
		document.getElementById('changeStatuslbl').style.display = 'none';
		document.getElementById('addStatuslbl').style.display = 'table-cell';
		document.getElementById('wardName').textContent = '';
		document.getElementById('bedName').textContent = '';
		document.getElementById('prevApptRow').style.display = 'none';
		document.getElementById('prevNoShowRow').style.display = 'none';
		document.getElementById("prescribing_doctor").value = '';
		document.getElementById("conducting_doctor").value = '';
		document.getElementById("presc_doc_id").value = '';
		document.getElementById("cond_doc_id").value = '';
		document.getElementById("cond_doc_star").style.visibility = 'hidden';
		document.getElementById("conducting_doctor").setAttribute("disabled", true);
		document.getElementById("conducting_doctor").value = '';
		

		loadStatus('New');

	} else if (addEdit == 'edit') {
		document.getElementById("prenxtdiv").style.display = 'block';
		document.getElementById('currentStatusField').style.display = 'block';
		document.getElementById('currentStatusValue').style.display = 'block';
		document.getElementById('addStatuslbl').style.display = 'none';
		document.getElementById('changeStatuslbl').style.display = 'table-cell';
		document.getElementById('prevApptRow').style.display = 'table-row';
		document.getElementById('prevNoShowRow').style.display = 'table-row';
		
		loadStatus(appointStatus);

	} else {
		document.getElementById("prenxtdiv").style.display = 'none';
	}
	 rform.apptList.value = appntIds.length-1;
	 listCount= rform.apptList.value;
	 disableNewButton(overbook_limit, apptid, apptapplicable, defaultDuration, apptDur, slotClass);
   document.resourceform.ok.disabled = false;

	var tdObj = document.getElementById("toolbarRow" + id);
	YAHOO.lutsr.accordion.collapse(document.getElementById("recurranceDD"));
	slotobj = tdObj;
	oldClass = slotClass + ' ' + mouserPointer;
	tdObj.className = tdObj.className + ' ' + mouserPointer;
	dialog.cfg.setProperty("context", [tdObj, "tl", "bl"], false);
	document.getElementById("dialogId").value = id;

	/*
	var serverDate = new Date(serverNow);
	serverDate.setSeconds(0);
	var slotDateTime = getDateTime(colDate, slotTime);
	slotDateTime.setMinutes(slotDateTime.getMinutes() + parseInt(duration));

	if (serverNow > slotDateTime) {
		showMessage("Appointments cannot be booked in the past");
		handleCancel();
		return false;
	}*/
	dialog.show();
	rform.name.focus();
	//Disable ok button if selected center is default when logged as super center.
	var selectedCenter = document.getElementById('centerId').value;
	if(addEdit == 'add'){
		if(category == 'DOC' && max_centers_inc_default > 1 && loggedInCenterId == 0) {

			if(selectedCenter == 0){
				document.getElementById("ok").disabled=true;
			}
		}
	}
	if(addEdit == 'edit'){
		if(category == 'DOC' && max_centers_inc_default > 1 && loggedInCenterId == 0) {
			if(selectedCenter == 0){
				document.getElementById("ok").disabled=true;
			}
		}

	}
}


var globalApptId;
var appntIdArray = [];
var dialogpopup;
var slotTime;
var index;

function getAppointmentDetails(appointmentId, appArrStr, dialpopup, slTime, ind) {
	globalApptId = appointmentId;
	index = ind;
	dialogpopup = dialpopup;
	slotTime = slTime;

	if (appArrStr != '') {
		var arr = appArrStr.split(",");
		for (var i = 1; i < arr.length; i++) {
			appntIdArray.push(arr[i]);
		}
	}

	document.getElementById("Prev").disabled = false;
	document.getElementById("Nxt").disabled = false;

	if (appointmentId == appntIdArray[appntIdArray.length - 1]) {
		document.getElementById("Nxt").disabled = true;
		document.getElementById("Prev").disabled = true;
	} else if (appointmentId == appntIdArray[0]) {
		document.getElementById("Prev").disabled = true;
	} else {
		document.getElementById("Prev").disabled = false;
		document.getElementById("Nxt").disabled = false;
	}

	if (appntIdArray.length == 1) {
		document.getElementById("Prev").disabled = true;
		document.getElementById("Nxt").disabled = true;
	}

	var category = rform.category.value;
	rform.appointmentId.value = appointmentId;

	if (appointmentId != '') {
		var ajaxReqObject = newXMLHttpRequest();
		var url = cpath + "/pages/resourcescheduler/addeditappointments.do?method=getAppointmentDetails"
				+ "&appointmentId=" + appointmentId + "&category=" + category + "&slotTime=" + slotTime;
		var reqObject = newXMLHttpRequest();
		reqObject.open("POST", url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ((reqObject.status == 200) && (reqObject.responseText != null)) {
				handleAjaxResponseForAppointmentDetails(reqObject.responseText);
			}
		}
	}
}

function patientPrevAppointmentDetails(prevAppointment) {
	var noShowAppointmentCount = "0/0";
	var appointDate = null;
	if (prevAppointment != null) {
		appointDate = (parseDateStr(prevAppointment.appointment_date)).getTime();
	}

	document.getElementById('prevApptRow').style.display = 'table-row';
	document.getElementById('prevNoShowRow').style.display = 'table-row';

	if (prevAppointment == null) {
		document.getElementById('prevAppointment').innerHTML = getString('js.scheduler.schedulerdashboard.noappointments');
		document.getElementById('noOfNoShows').innerHTML = '--';
	} else {
		if (prevAppointment.lastyeartotalcount == null || prevAppointment.lastyeartotalcount == '') {
			noShowAppointmentCount = "0/0";
		} else {
			if (prevAppointment.lastyearnoshowcount == null || prevAppointment.lastyearnoshowcount == '') {
				noShowAppointmentCount = "0/"+prevAppointment.lastyeartotalcount;
			}
			else{
				noShowAppointmentCount = prevAppointment.lastyearnoshowcount+"/"+prevAppointment.lastyeartotalcount;
			}
		}
		var prevAppStr = '';
		prevAppStr += "Appt Time: "+prevAppointment.appointment_date + "/";
		prevAppStr += prevAppointment.appointment_time + " /Type: ";
		prevAppStr += prevAppointment.schedule_type + "/<br/> Appt For: ";
		prevAppStr += (!empty(prevAppointment.resouce_name) ? prevAppointment.resouce_name : '')+"/Appt Status: ";
		prevAppStr += (!empty(prevAppointment.appointment_status) ? prevAppointment.appointment_status : '');
		document.getElementById('prevAppointment').innerHTML = prevAppStr;
		document.getElementById('noOfNoShows').innerHTML = noShowAppointmentCount+" (no shows/ total appointments)";
	}
}

function handleAjaxResponseForAppointmentDetails(responseText) {
	var schPatInfo = null;
	eval("schPatInfo =" + responseText);

	var appRecordList = schPatInfo.appntDetailsList;
	var prevAppointment = schPatInfo.prevApptDetails;

	patientPrevAppointmentDetails(prevAppointment);

	if (dialogpopup == 'false') {
		var slotTime = schPatInfo.slotTime;
		rform.slotTime.value = slotTime;
		slotTime = slotTime.slice(0, 5);
		document.getElementById("pastTime").innerHTML = " " + slotTime;
	}

	var category = rform.category.value;
	var scheduleId = appRecordList[0]["res_sch_name"];
	var centerId = appRecordList[0]["center_id"];

	if(rform.center_id)
		setSelectedIndex(rform.center_id,centerId);
	else
		rform.center_id.value = centerId;

	if (rform.scheduleNameForAppointment != null) {
		if (rform.category.value == "DOC") {
			setSelectedIndex(rform.scheduleNameForAppointment, scheduleId);
		} else {
			var schName = getscNameFromId(scheduleId);
			document.getElementById("scNames").value = schName;
			document.getElementById("scheduleNameForAppointment").value = scheduleId;
			document.getElementById("scNames").title = schName;
		}
	}
	var prescDocId = appRecordList[0]["presc_doc_id"];
	var condDocId = appRecordList[0]["cond_doc_id"];
	var prescdoctor = appRecordList[0]["presc_doctor"];
	var condDoctor = appRecordList[0]["cond_doctor"];
	var appointmentStatus = appRecordList[0]["appointment_status"];
	document.getElementById("presc_doc_id").value = prescDocId;
	document.getElementById("cond_doc_id").value = condDocId;
	document.getElementById("prescribing_doctor").value = prescdoctor;
	document.getElementById("conducting_doctor").value = condDoctor;
	document.getElementById("currentstatus").textContent = appointmentStatus;

	if(category =="OPE" && !empty(scheduleId) && document.getElementById('scheduler_prior_auth_row')) {
		var	list = findInList(surgeriesJson,"OP_ID",scheduleId);
		var priorAuthRequiredForSurgery = list['PRIOR_AUTH_REQUIRED'];
		if(!empty(priorAuthRequiredForSurgery) && priorAuthRequiredForSurgery != 'N') {
			document.getElementById('scheduler_prior_auth_row').style.display = "table-row";
			document.getElementById('scheduler_prior_auth_no').value = appRecordList[0]['scheduler_prior_auth_no'];
			document.getElementById('scheduler_prior_auth_mode_id').value = appRecordList[0]['scheduler_prior_auth_mode_id'];
		} else {
			document.getElementById('scheduler_prior_auth_row').style.display = "none";
			document.getElementById('scheduler_prior_auth_no').value = "";
			document.getElementById('scheduler_prior_auth_mode_id').value = "";
		}
	}

	var record = appRecordList[0];
	var timeInmillies = record["appointment_time"];

	var curdate = new Date();
	curdate.setTime(timeInmillies);
	var hours = curdate.getHours();
	if (hours < 10) {
		hours = '0' + hours;
	}
	var minutes = curdate.getMinutes();
	if (minutes < 10) {
		minutes = '0' + minutes;
	}
	var time = hours + ':' + minutes;

	if(gArrived == 'Arrived')
		rform.slotTime.value = time;
	setSelectedIndex(rform.time, time);
	rform.mrno.value = record["mr_no"];
	rform.contactId.value = record["contact_id"];
	rform.name.value = record["patient_name"];
	rform.name.readOnly = true;
	rform.complaint.value = record["complaint"];
	rform.remarks.value = record["remarks"];
	//set country and national number of contact
	insertNumberIntoDOM(record["patient_contact"],$("#patContact"),$("#contact_country_code"),
			$("#contact_national"));

	rform.duration.value = record["duration"];
	rform.presc_doc_id.value = record["presc_doc_id"];
	rform.cond_doc_id.value = record["cond_doc_id"];
	rform.prescribing_doctor.value= record["presc_doctor"];

	var activeVisits = getActiveVisits(record["mr_no"],record['appointment_id']);
	loadActiveVisits(rform.patient_id, activeVisits);
	rform.patient_id.value = record["visit_id"];
	onChangeVisit();

	if (rform.category.value == 'DOC') {
		rform.consultationTypes.value = record["consultation_type_id"];
		rform.scheduler_visit_type.value = record["scheduler_visit_type"];
	}

	if (category != 'DOC' && trim(document.getElementById("scNames").value) == '') {
		document.getElementById("scNames").removeAttribute("title");
	}

	loadStatus(record["appointment_status"]);
	rform.status.value = record["appointment_status"];

	if (record["appointment_status"] != 'Arrived') {
		document.getElementById("visitIdRow").style.display = 'none';
		document.getElementById("statusRow").style.display = 'table-row';
	}

	for (var i = 0; i < appRecordList.length; i++) {
		record = appRecordList[i];
		if (rform.centralResource != null) {
			if (category == 'OPE') {
				if (record["resource_type"] == 'THID') {
					setSelectedIndex(rform.centralResource, record["resource_id"]);
				}
			} else if (category == 'SNP') {
				if (record["resource_type"] == 'SRID') {
				    initialselectedResourceValue = record["resource_id"]
					setSelectedIndex(rform.centralResource, record["resource_id"]);
				}
			} else if (category == 'DIA') {
				if (record["resource_type"] == 'EQID') {
				    initialselectedResourceValue = record["resource_id"]
					setSelectedIndex(rform.centralResource, record["resource_id"]);
				}
			}
		}
	}
	deleteRows();
	var j = 0;
	if (appRecordList != null & appRecordList.length > 0) {
		for (var i = 0; i < appRecordList.length; i++) {
			record = appRecordList[i];
			if (record["primary_resource"]) {
				rform.centralResourceSchItemId.value = record["appointment_item_id"];
			}
			if (!record["primary_resource"]) {
				addResourceRow();
				var resourceName = "resourceName" + (j);
				var resourceNameObj = document.getElementById(resourceName);

				var resourceValue = "resourceValue" + (j);
				var resourceValueObj = document.getElementById(resourceValue);

			/*	var hiddenResourceName = "resource_name"+(j);
				var hiddenResourceNameObj = document.getElementById(hiddenResourceName);

				var hiddenResourceValue = "resource_value"+(j);
				var hiddenResourceValueObj = document.getElementById(hiddenResourceValue);*/

				var itemId = "appointment_item_id" + (j);
				document.getElementById(itemId).value = record["appointment_item_id"];

				var primaryResource = "priresource" + (j);
				document.getElementById(primaryResource).value = record["primary_resource"];

				setSelectedIndex(resourceNameObj, record["resource_type"]);
				populateResources(j);
				setSelectedIndex(resourceValueObj, record["resource_id"]);
			/*	resourceNameObj.disabled = true;
				resourceValueObj.disabled = true;
				hiddenResourceNameObj.value = record["resource_type"];
				hiddenResourceValueObj.value = record["resource_id"];*/

				enableButton(j);

				j++;
			}
		}
	}
	addResourceRow();
	enableButton(j);

	rform.method.value = 'editAppointmentDetails';

	resetRecurranceTab();
	showHiderecurringTable(false);
}

function getMrnoDetails() {
	var mrno = rform.mrno.value;
	console.log(mrno);

	if (empty(rform.appointmentId.value))
		resetOnChangeMrno();

	var schPatInfo = null;

	if (trim(mrno) != '') {
		if (trim(mrno).startsWith("contact")) {
			
			var contactId = mrno.split(" ")[1];
			var ajaxobj = newXMLHttpRequest();
			var url = cpath + '/pages/resourcescheduler/addeditappointments.do?method=getContactDetailsJSON&contactId=' + contactId;
			ajaxobj.open("POST", url.toString(), false);
			ajaxobj.send(null);
			if (ajaxobj) {
				if (ajaxobj.readyState == 4) {
					if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
						eval("schPatInfo =" + ajaxobj.responseText);
						console.log(schPatInfo);
						rform.name.value = schPatInfo.patient_name +' '+ (schPatInfo.last_name == null ?  '' : schPatInfo.last_name);
						rform.name.readOnly = true;
						document.getElementById('contact_country_code').disabled = true;
						document.getElementById('contact_national').disabled = true;
						//set country and national number of patient_phone
						insertNumberIntoDOM(schPatInfo.patient_contact,$("#patContact"),$("#contact_country_code"),
								$("#contact_national"));
						rform.mrno.value = '';
						rform.contactId.value = contactId;
					}
				}
			}
			
		} else {
			var ajaxobj = newXMLHttpRequest();
			var url = cpath + '/pages/resourcescheduler/addeditappointments.do?method=getPatientDetailsJSON&mrno=' + mrno;
			if (rform.patient_id != null)
				url += '&patient_id='+ rform.patient_id.value;
			ajaxobj.open("POST", url.toString(), false);
			ajaxobj.send(null);
			if (ajaxobj) {
				if (ajaxobj.readyState == 4) {
					if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {

						eval("schPatInfo =" + ajaxobj.responseText);

						var patient = schPatInfo.patientDetails;
						var prevAppointment = schPatInfo.prevApptDetails;
						var patBillDetails = schPatInfo.billDetails;
						patientPrevAppointmentDetails(prevAppointment);

						if(patBillDetails != null && patient != null) {
							gIsInsurance = patBillDetails.is_tpa && patient.primary_sponsor_id != '' && patient.primary_sponsor_id != null;
						}

						if (patient == null) {
							showMessage("js.scheduler.doctorscheduler.invalid.mrno");
							rform.mrno.focus();
							resetFieldValues();
							document.getElementById('prevApptRow').style.display = 'none';
							document.getElementById('prevNoShowRow').style.display = 'none';
							return false;
						}

						rform.name.value = patient.patient_name + ' ' + (patient.middle_name == null ? '' : patient.middle_name) + ' ' + (patient.last_name == null ? '' : patient.last_name);
						rform.name.readOnly = true;
						document.getElementById('contact_country_code').disabled = true;
						document.getElementById('contact_national').disabled = true;
						//set country and national number of patient_phone
						if (patient.patient_phone) {
							insertNumberIntoDOM(patient.patient_phone,$("#patContact"),$("#contact_country_code"),
									$("#contact_national"));
						}
						rform.salutationName.value = patient.salutation;
						document.getElementById('wardName').textContent = !empty(patient.alloc_ward_name) ? patient.alloc_ward_name : '';
						document.getElementById('bedName').textContent = !empty(patient.alloc_bed_name) ? patient.alloc_bed_name : '';
						document.getElementById('prevApptRow').style.display = 'table-row';
						document.getElementById('prevNoShowRow').style.display = 'table-row';
					}
				}
			}
		}

	}
}

function onChangeVisit() {
	var visitId = rform.patient_id.value;
	var mrno 	= rform.mrno.value;
	var category = rform.category.value;
	var arrived = rform.arrived.value;

	if (rform.category.value == 'DOC') {
		if (rform.patient_id != null && (!empty(visitId) && visitId != 'None')) {
			document.getElementById('consultationTypeCell').style.display = 'table-cell';
			document.getElementById('emptyCell1').style.display = 'none';
			document.getElementById('emptyCell2').style.display = 'none';
		}else {
			document.getElementById('consultationTypeCell').style.display = 'none';
			document.getElementById('emptyCell1').style.display = 'table-cell';
			document.getElementById('emptyCell2').style.display = 'table-cell';
		}
	}

	var patient = findInList(gActiveVisits, "patient_id", visitId);

	if (!empty(patient)) {
		rform.visitType.value = !empty(patient.visit_type) ? patient.visit_type : '';

		document.getElementById('wardName').textContent = !empty(patient.alloc_ward_name) ? patient.alloc_ward_name : '';
		document.getElementById('bedName').textContent = !empty(patient.alloc_bed_name) ? patient.alloc_bed_name : '';
		if(!empty(category) && category == 'OPE' && document.getElementById('scheduler_prior_auth_row')) {
			document.getElementById('scheduler_prior_auth_no').value =
				(!empty(gAppointmentDetails) && !empty(gAppointmentDetails['scheduler_prior_auth_no'])) ?
				 gAppointmentDetails['scheduler_prior_auth_no'] :
				 	(!empty(patient) && !empty(patient.prior_auth_id)) ? patient.prior_auth_id : '';

			document.getElementById('scheduler_prior_auth_mode_id').value =
					(!empty(gAppointmentDetails)&& !empty(gAppointmentDetails['scheduler_prior_auth_mode_id'])) ?
				 	gAppointmentDetails['scheduler_prior_auth_mode_id'] :
				 	(!empty(patient) && !empty(patient.prior_auth_mode_id)) ? patient.prior_auth_mode_id : '';
		}

		var centerId = patient.center_id;

		if (!empty(patient.org_id)) {
			orgId = patient.org_id;
			orgName = patient.org_name;
		} else {
			orgId = "ORG0001";
			orgName = "General";
		}
		if (rform.category.value == 'DOC') {
			var list = getConsultationTypes(orgId, centerId, patient.visit_type);
			loadConsultationTypes(list);
		}
	}else {
		rform.visitType.value = '';
	//	document.getElementById('wardName').textContent = '';
	//	document.getElementById('bedName').textContent = '';
		orgId = "ORG0001";
		orgName = "General";

		if(arrived == 'Arrived' && !empty(category) && category == 'OPE' && document.getElementById('scheduler_prior_auth_row')) {
			document.getElementById('scheduler_prior_auth_no').value =
				(!empty(gAppointmentDetails) && !empty(gAppointmentDetails['scheduler_prior_auth_no'])) ?
				 gAppointmentDetails['scheduler_prior_auth_no'] : '';

			document.getElementById('scheduler_prior_auth_mode_id').value =
					(!empty(gAppointmentDetails)&& !empty(gAppointmentDetails['scheduler_prior_auth_mode_id'])) ?
				 	gAppointmentDetails['scheduler_prior_auth_mode_id'] : '';
		}
	}
	if (category == "DOC" && (!empty(visitId) && visitId != 'None')) {
		var doctorId = document.getElementById('scNames').value;
		var doctor = findInList(scheduleResourceListJSON, "resource_id", doctorId);
		var docChrgObj = rform.consultationTypes;
		var isFirstVisit = true;
		var isIpFollowUp = false;
		if (typeof(docChrgObj) != 'undefined') {
			getRegDetailsForSchedulerSreen("rform");
			setDocRevistCharges(doctorId,doctor,docChrgObj,"rform");
		}
	}
}

function onChangeStatus() {
	var status = document.resourceform.status.value;
	if (status == 'Cancel') {
		document.getElementById('cancel_reason').disabled = false;
	} else {
		document.getElementById('cancel_reason').disabled = true;
	}
}

function clearVisitSelect() {
	var visitSelect = document.getElementById("app_visit_id");

	for (var i = 0; i < visitSelect.options.length; i++) {
		visitSelect.remove(i);
	}

	visitSelect.options.length = 1;
	visitSelect.options[0].text = "None";
	visitSelect.options[0].value = "None";
}

function resetOnChangeMrno() {
	rform.name.value = '';
	rform.name.readOnly = false;
	//reset contact
	clearPhoneField($("#contact_country_code"),$("#contact_national"),defaultCountryCode);
	rform.mrno.readOnly = false;
	rform.salutationName.value = '';
	document.getElementById('contact_country_code').disabled = false;
	document.getElementById('contact_national').disabled = false;

	clearVisitSelect();

	document.getElementById('prevAppointment').innerHTML = '';
	document.getElementById('noOfNoShows').innerHTML = '';
	document.getElementById('prevApptRow').style.display = 'none';
	document.getElementById('prevNoShowRow').style.display = 'none';

	rform.patient_id.selectedIndex = 0;
	onChangeVisit();
	rform.patient_id.disabled = false;
//	rform.complaint.value = '';
//	rform.remarks.value = '';
	if (rform.centralResource != null) {}
	var category = rform.category.value;

	/*if (category == 'DOC') {
		if (rform.consultationTypes) {
		setSelectedIndex(rform.consultationTypes, "");
		}
	}else {
		document.getElementById("scNames").value = '';
		document.getElementById("scheduleNameForAppointment").value = '';
		document.getElementById("scNames").removeAttribute("title");
	} */

//	deleteRows();
//	document.getElementById('currentstatus').textContent = '';
	setSelectedIndex(rform.status, !empty(rform.status.value) ? rform.status.value : 'Booked');

/*	if (category == 'OPE') {
		addResourceRow();
		setSelectedIndex(document.getElementById('resourceName0'), 'SUDOC');
		populateResources(0);
		addResourceRow();
		setSelectedIndex(document.getElementById('resourceName1'), 'ANEDOC');
		populateResources(1);
	} else if (category == 'DOC') {
		addResourceRow();
		setSelectedIndex(document.getElementById('resourceName0'), 'EQID');
		populateResources(0);
	} else if (category == 'DIA') {
		addResourceRow();
		setSelectedIndex(document.getElementById('resourceName0'), 'LABTECH');
		populateResources(0);
	} else if (category == 'SNP') {
		addResourceRow();
	}
	rform.method.value = 'saveAppointment';  */
	document.getElementById('wardName').innerHTML = '';
	document.getElementById('bedName').innerHTML = '';
}

function resetFieldValues() {
	resetOnChangeMrno();
	rform.mrno.value = '';
}

/* Getting the appointment details based on the appointmentId to show in add/edit dialog
 * based on clicking on the next and prev button.
 */
function getNxtPrevAppId(preNxt) {
	if (preNxt == 'nxt') {
		for (var i = 0; i < appntIdArray.length; i++) {
			if (appntIdArray[i] != globalApptId) {
				continue;
			} else {
				globalApptId = appntIdArray[i + 1];
				getAppointmentDetails(appntIdArray[i + 1], '', dialogpopup, slotTime, index);
				break;
			}
		}
		if (globalApptId == appntIdArray[appntIdArray.length - 1]) {
			document.getElementById("Nxt").disabled = true;
			document.getElementById("Prev").disabled = false;
		} else if (globalApptId == appntIdArray[0]) {
			document.getElementById("Prev").disabled = false;
			document.getElementById("Nxt").disabled = false;
		} else {
			document.getElementById("Prev").disabled = false;
			document.getElementById("Nxt").disabled = false;
		}
		document.getElementById('activePatient').style.display = 'block';
	}
	if (preNxt == 'prev') {
		for (var i = 0; i < appntIdArray.length; i++) {
			if (appntIdArray[i] != globalApptId) continue;
			else {
				globalApptId = appntIdArray[i - 1];
				getAppointmentDetails(appntIdArray[i - 1], '', dialogpopup, slotTime, index);
				break;
			}
		}
		if (globalApptId == appntIdArray[appntIdArray.length - 1]) {
			document.getElementById("Nxt").disabled = true;
			document.getElementById("Prev").disabled = true;
		} else if (globalApptId == appntIdArray[0]) {
			document.getElementById("Prev").disabled = true;
			document.getElementById("Nxt").disabled = false;
		} else {
			document.getElementById("Prev").disabled = false;
			document.getElementById("Nxt").disabled = false;
		}

		document.getElementById('activePatient').style.display = 'block';
	}
	document.getElementById('currentstatus').textContent = document.resourceform.status.value;
}

function slotTimeNull() {
	document.getElementById("slotTime").value = "";
	document.getElementById("pastTime").innerHTML = "";
}

/* Adding New Appointment when new button is clicked in dialog */

function addNewAppointment() {
	resetFieldValues();
	rform.complaint.value = '';
	rform.remarks.value = '';
	if (category == 'DOC') {
		if (rform.consultationTypes) {
			setSelectedIndex(rform.consultationTypes, "");
		}

		if(rform.center_id)
			setSelectedIndex(rform.center_id, "");
	}
	showHiderecurringTable(true);

	rform.appointmentId.value = '';
	document.getElementById('activePatient').style.display = 'block';
	document.getElementById('currentStatusField').style.display = 'none';
	document.getElementById('currentStatusValue').style.display = 'none';
	document.getElementById('changeStatuslbl').style.display = 'none';
	document.getElementById('addStatuslbl').style.display = 'table-cell';
	document.getElementById('wardName').textContent = '';
	document.getElementById('bedName').textContent = '';
	document.getElementById('prevApptRow').style.display = 'none';
	document.getElementById('prevNoShowRow').style.display = 'none';
	document.getElementById("visitIdRow").style.display = 'none';
	document.getElementById("statusRow").style.display = 'table-row';

	loadStatus('New');
	setSelectedIndex(rform.status, "Booked");

	var table = document.getElementById("resourceTable");
	var rowCount = table.rows.length;
	var tempRowCount = rowCount - 2;

	for (var i = 1; i < rowCount; i++) {
		table.deleteRow(i);
		rowCount--;
		i--;
	}
	addResourceRow();
	if (category != 'DOC') {
		document.getElementById('scNames').value = "";
		document.getElementById("scNames").removeAttribute("title");
		document.getElementById('scheduleNameForAppointment').value = "";
	}
	rform.name.focus();
	rform.method.value = "saveAppointment";
}


function checkMultipleVisitAndRegisterIfArrived(id, slotClass, dialogpopup, appntIds, addoredit, schName,
	overbook_limit, rowTime, apptid, slotTime, index, colDate, mrNo, contactId, visitId,
	apptapplicable, defaultDuration, paymentStatus, apptDur, appointStatus,centerId, apptList, arrived) {

	gArrived = arrived;
	getAppointmentDetails(apptid, '', 'edit', slotTime, 0);
	rform.center_id.value = centerId;

	var activeVisits = getActiveVisits(mrNo,apptid);
	var visitSelect = rform.patient_id;
	loadActiveVisits(visitSelect, activeVisits);
	document.getElementById("app_visit_id").disabled = false;
	if (visitSelect.options.length == 1) {
		if (!empty(visitSelect.value) && visitSelect.value == 'None') {
			rform.isArrivedDialogOpened.value = 'N';
			registerIfArrived(apptid, contactId, category, mrNo, visitSelect.value, slotTime);
		} else {
			if (!isDuplicateConsultation() && !empty(getResourceScheduleId())) {
				rform.isArrivedDialogOpened.value = 'N';
				registerIfArrived(apptid, contactId, category, mrNo, visitSelect.value, slotTime);
			}else {
				showAddorEditDialog(id, slotClass, dialogpopup, appntIds, 'edit', schName,
					overbook_limit, rowTime, apptid, slotTime, index, colDate, mrNo, contactId, visitSelect.value,
					apptapplicable, defaultDuration, apptDur, appointStatus,centerId, apptList, arrived);
			}
		}
	}else {
		//
		if (schedulerGenerateOrder == 'N' && arrived == 'Arrived' && !(modAdvancedOT == 'Y' && category == 'OPE')) {
			setArrivedStatus(apptid, category);
			return true;
		}
		showAddorEditDialog(id, slotClass, dialogpopup, appntIds, 'edit', schName,
			overbook_limit, rowTime, apptid, slotTime, index, colDate, mrNo, contactId, visitId,
			apptapplicable, defaultDuration, apptDur, appointStatus,centerId, apptList, arrived);

		loadActiveVisits(visitSelect, activeVisits);
		onChangeVisit();
	}
}

// Active visits are loaded
function loadActiveVisits(visitSelect, activeVisits) {

	clearVisitSelect();

	if (activeVisits != null && visitSelect != null) {
		var len = visitSelect.options.length;
		for (var i = 0; i < activeVisits.length; i++) {
			var doctor = activeVisits[i].doctor_name;
			if (empty(doctor)) {
				doctor = 'No Doctor';
			}
			var appendPatientIdAndDoctor = activeVisits[i].patient_id + " (" + doctor + ")";
			var optn = new Option(appendPatientIdAndDoctor, activeVisits[i].patient_id);
			len++;
			visitSelect.options.length = len;
			visitSelect.options[len - 1] = optn;
		}
	}
	setSelectedIndex(visitSelect, "None");
}

function updateAppointmentVisitId(appointment_id, mrno, visitId) {
	var reqObject = newXMLHttpRequest();
	var url = "../../pages/resourcescheduler/addeditappointments.do?"
				+ "method=updateVisitAndPatientMrno&appointment_id=" + appointment_id
				+ "&mrno=" + mrno + "&patient_id=" + visitId;
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (reqObject.responseText != null)) {
			var obj = reqObject.responseText;
			if (obj != null && obj == 'Updated') {
				return true;
			}
		}
	}
	return false;
}

function registerIfArrived(appointmentId, contactId, category, mrNo, visitId, slotTime) {

	rform.appointmentStatus.value = 'Arrived';
	rform.appointmentId.value = appointmentId;
	rform.contactId.value = contactId;
/*	if (!empty(visitId) && visitId != 'None') {
		if (!updateAppointmentVisitId(appointmentId, mrNo, visitId)) {
			showMessage("Failed to update VisitId ");
			return false;
		}
	}*/

	rform.method.value = 'getSchedulerRegistration';
	rform._registrationType.value = category;
	rform._center_id.value = rform.center_id.value;
	rform.resFilter.value = category;
	rform.category.value = category;
	rform.submit();
}

function setArrivedStatus(appointmentId, category) {

	rform.appointmentStatus.value = 'Arrived';
	rform.appointmentId.value = appointmentId;
	rform.method.value = 'setArrivedStatus';
	rform._registrationType.value = category;
	rform._center_id.value = rform.center_id.value;
	rform.resFilter.value = category;
	rform.category.value = category;
	rform.submit();
}



function handleSubmit() {

	var category = rform.category.value;
    rform.centralResource.disabled = false;
	var userCentId = document.getElementById('userCentId').value;
	if(max_centers_inc_default > 1 && userCentId == 0){
		if (category == 'DOC'){
			if(document.getElementById("allcenter").style.display == 'none'){
				var centerId = document.getElementById("_center_id").value;
				document.getElementById("ah_center_id").value = centerId;
			}else{
				if(document.getElementById("allcenter").style.display == 'block'){
					var centerId = document.getElementById("center_id").value;
					document.getElementById("ah_center_id").value = centerId;
				}
			}

		}
	}

	rform.includeResources.value = includeresources();
	mform.includeResources.value = includeresources();
	var dialogId = document.getElementById("dialogId").value;
	var apptId 	 = rform.appointmentId.value;
	var mrno	 = rform.mrno.value;
	var arrived  = rform.arrived.value;
	var contactId  = rform.contactId.value;
	var contact = rform.contact.value;
	var emptyContact = contact == null || contact == '' || contact == undefined;
	var mrNoSelected = !(mrno == null || mrno == '' || mrno == undefined);
	var contactIdSelected = !(contactId == null || contactId == '' || contactId == undefined);
	if (emptyContact && (mrNoSelected || contactIdSelected)) {
		alert("Phone Number cannot be empty. Please edit the patient details");
		rform.centralResource.disabled = true;
		return false;
	}

	if (!validation()) {
		rform.centralResource.disabled = true;
		return false;
	}

	if (arrived != 'Arrived'
			&& document.getElementById("visitIdRow").style.display == 'none') {
		if (!empty(apptId) && addOrEdit == 'edit' && !empty(mrno)) {
			document.resourceform.method.value = 'editAppointmentDetails';
		}
		document.getElementById('ok').disabled = true;
		//alert(rform.center_id.value);
		rform.submit();
	} else {
		if (addOrEdit == 'edit') {

			if (!checkOrderOrConsultation()) return;
			if (!checkDuplicateAppointment()) {	handleCancel();	return;	}
			if (!validateOnlyOnePrescDocForOP()) return false;
			var visitSelect = rform.patient_id;
			if (visitSelect != null && visitSelect.options.length > 1) {
				
				if (visitSelect.value == 'None') {
					var ok = confirm(" Patient has active visit(s)."
									+ "\n Do you want to continue with new registration?");
					if (!ok) {
						visitSelect.focus();
						return;
					}
				}
			}
			registerIfArrived(apptId, contactId, category, mrno, visitSelect.value, rform.slotTime.value);
		}
	}
}

function handleCancel() {
	slotobj.className = oldClass;
	YAHOO.lutsr.accordion.collapse(document.getElementById("recurranceDD"));
	//document.getElementById("recurringTable").style.display="none";
	deleteRows();
	rform.complaint.value = '';
	rform.remarks.value = '';
	rform.appointmentId.value = '';
	if (category == 'DOC') {
		if (rform.consultationTypes) {
			setSelectedIndex(rform.consultationTypes, "");
		}
		loadDoctors();
	}
	document.getElementById("cond_doc_star").style.visibility = 'hidden';
	rform.scheduleNameForAppointment.value = "";
	loadDefaultTimeList();
	dialog.cancel();
}

function loadDoctors() {
	var selectBox = document.getElementById('scNames');
	for(var i=0;i<availbiltyList.length;i++) {
		loadSelectBox(selectBox,availbiltyList,"schedulename","id","-- Select --","");
		setSelectedIndex(selectBox,gschName);
	}
}

function loadDefaultTimeList() {
	var curdate = new Date();
	rform.time.length = 1;
	if (defaultTimeslotJSON != null && defaultTimeslotJSON.length > 0) {
		for (var i = 0; i < defaultTimeslotJSON.length; i++) {
			curdate.setTime(defaultTimeslotJSON[i]);
			var hours = curdate.getHours();
			if (hours < 10) {
				hours = '0' + hours;
			}
			var minutes = curdate.getMinutes();
			if (minutes < 10) {
				minutes = '0' + minutes;
			}
			var time = hours + ':' + minutes;
			var option = new Option(time, time);
			rform.time.options[rform.time.length] = option;
		}
	}
}

function changeCentralResourceValue(obj) {
	var resId = obj.value;
	var	list = findInList(scheduleResourceListJSON,"resource_id",resId);
	document.resourceform.overbook_limit.value = list["overbook_limit"];
}

/* Validations on dialog submit start */

function validation() {
	
	var category = rform.category.value;
	
	if (rform.mrno.value != '') {
		if (document.getElementById("visitIdRow").style.display != 'none' && rform.patient_id.value == '') {
			showMessage('js.scheduler.doctorscheduler.select.visit');
			rform.patient_id.focus();
			return false;
		}
	}

	if (trim(rform.name.value) == '') {
		showMessage('js.scheduler.doctorscheduler.name.required');
		//TODO::set new focus
		rform.name.focus();
		return false;
	}
	//Validate contact number
	if (rform.contact == null || trim(rform.contact.value) == '') {
		showMessage('js.scheduler.doctorscheduler.mobilenumber.required');
		$("#contact_national").focus();
		return false;
	}
	if (trim(rform.contact.value) != '' ) {
      	if($("#contact_valid").val() == 'N') {
				alert(getString("js.registration.patient.mobileNumber") + " " +
		 				getString("js.registration.patient.enter.govt.invalid.string"));
				$("#contact_national").focus();
 	            return false;
 	    }
      }

	if (gPrescDocRequired == 'Y' && trim(rform.presc_doc_id.value) == '') {
		showMessage('js.scheduler.doctorscheduler.prescribingdoctor.required');
		rform.prescribing_doctor.focus();
		return false;
	}
	
	if (category == "DIA" && document.getElementById('conductingDoctorMandatory').value =='O' && trim(rform.cond_doc_id.value) == ''){
		showMessage('js.scheduler.doctorscheduler.conductingdoctor.required');
		rform.conducting_doctor.focus();
		return false;
	}

	if (trim(rform.duration.value) == '') {
		showMessage('js.scheduler.doctorscheduler.duration.required');
		rform.duration.focus();
		return false;
	}

	var currentStatus = document.getElementById('currentstatus').textContent;

	if(empty(rform.center_id.value) && (document.getElementById("allcenter").style.display == 'block')) {
		showMessage("js.scheduler.doctorscheduler.select.center");
		rform.center_id.focus();
		return false;
	}
	if(empty(document.getElementById('_center_id').value) && (document.getElementById("belongcenter").style.display == 'block')) {
		showMessage("js.scheduler.doctorscheduler.select.center");
		document.getElementById('_center_id').focus();
		return false;
	}

	if (!validateCancelReason()) {
		return false;
	}

	
	var index = "";
	if (category == 'DOC')
		index = document.getElementById("scNames").selectedIndex;
	else
		index = document.getElementById("scNames").value;

	var arrived  = rform.arrived.value;

	if(!empty(category) && category == 'OPE' && arrived == 'Arrived' && document.getElementById('scheduler_prior_auth_row')) {
		if(gIsInsurance && priorAuthRequiredForSurgery != 'N' ) {

			if(empty(document.getElementById('scheduler_prior_auth_no').value)) {
				showMessage("js.scheduler.doctorscheduler.priorauthno.required");
				document.getElementById('scheduler_prior_auth_no').focus();
				return false;
			}

			if(empty(document.getElementById('scheduler_prior_auth_mode_id').value)) {
				showMessage("js.scheduler.doctorscheduler.priorauthmode.required");
				document.getElementById('scheduler_prior_auth_mode_id').focus();
				return false;
			}
		}
	}

	if (index == 0 || index == "" || index.length == 0) {
		var msg = '';
		if (category == 'OPE') {
			msg =getString('js.scheduler.doctorscheduler.select.surgery');
		} else if (category == 'DOC') {
			msg = getString('js.scheduler.doctorscheduler.select.doctor');
		} else if (category == 'SNP') {
			msg = getString('js.scheduler.doctorscheduler.select.service');
		} else if (category == 'DIA') {
			msg = getString('js.scheduler.doctorscheduler.select.test');
		}

		if (category == 'SNP' && serviceNameRequired == 'M') {
			alert(msg);
			document.getElementById("scNames").focus();
			return false;
		} else if (category == 'OPE' && surgeryNameRequired == 'M') {
			alert(msg);
			document.getElementById("scNames").focus();
			return false;
		} else if (category == 'DIA' || category == 'DOC') {
			alert(msg);
			document.getElementById("scNames").focus();
			return false;
		}
	}

	var visitSelect = rform.patient_id;
	var arrived  = rform.arrived;

	if (arrived != null && arrived.value == 'Arrived'
		&& visitSelect != null && visitSelect.value != 'None') {

		if (!checkOrganization(orgName)) return false;
	}

	if (rform.centralResource != null) {
		if (rform.centralResource.selectedIndex == 0 && !rform.centralResource.value) {
			var msg = '';
			if (category == 'OPE') {
				msg = getString('js.scheduler.schedulerdashboard.theatre.required');
			} else if (category == 'SNP') {
				msg = getString('js.scheduler.schedulerdashboard.primaryresource.required');
			} else if (category == 'DIA') {
				msg = getString('js.scheduler.schedulerdashboard.equipment.required');
			}
			alert(msg);
			rform.centralResource.focus();
			return false;
		}
	}
	if (rform.time.selectedIndex == 0 && rform.status.value != 'Cancel' && rform.status.value != 'Noshow') {
		showMessage('js.scheduler.doctorscheduler.select.timeofappointment');
		rform.time.focus();
		return false;
	}

	if (rform.duration.value == '') {
		showMessage('js.scheduler.doctorscheduler.duration.appointment.required');
		rform.duration.focus();
		return false;
	}

	var appointmentId = document.resourceform.appointmentId.value;
	var date = document.getElementById('datelbl').textContent;
	var time = document.getElementById('timeList').value;
	var category = document.resourceform.category.value;
	var duration = document.resourceform.duration.value;

	if (category != 'DOC') {
		var selectedCentralResource = rform.centralResource.options[rform.centralResource.selectedIndex].value;
		for (var i = 0; i < availbiltyList.length; i++) {
			var record = availbiltyList[i];
			if (record["id"] == selectedCentralResource) {
				if (record["canschedule"] == false) {
					var msg=record["schedulename"];
					msg+=getString("js.scheduler.todaysappointment.notavailable");
					alert(msg);
					rform.centralResource.focus();
					return false;
				}
			}
		}
	}

	if (document.getElementById("recurringTable").style.display == 'block') {
		var recurno = rform.recurrNo.value;
		var occurno = rform.occurrNo.value;
		var untildt = rform.untilDate.value;

		if (recurno != '0' || (occurno != '0' || untildt != '')) {
			if (recurno == '') {
				showMessage("js.scheduler.doctorscheduler.enter.recurrence");
				rform.recurrNo.focus();
				return false;
			}
			if (occurno == '0' || untildt == '') {
				if (rform.repeatOption.value == 'FOR') {
					if (occurno == '0') {
						showMessage("js.scheduler.doctorscheduler.enter.occurrence");
						rform.occurrNo.focus();
						return false;
					}
				} else if (rform.repeatOption.value == 'UNTIL') {
					if (untildt == '') {
						showMessage("js.scheduler.doctorscheduler.enter.untildate");
						rform.untilDate.focus();
						return false;
					}
				}
			}
			/*if (!doValidateDateField(rform.untilDate, 'future')) {
				rform.untilDate.focus();
				return false;
			}*/

			if (rform.recurranceOption.value == 'W') {
				if (!validateWeekDays()) {
					showMessage("js.scheduler.doctorscheduler.check.weekday");
					return false;
				}
			}
			if (rform.recurranceOption.value == 'M') {
				if (rform.recurrDate.value == '') {
					showMessage("js.scheduler.doctorscheduler.enter.monthdate");
					rform.recurrDate.focus();
					return false;
				}
				/*if (!doValidateDateField(rform.recurrDate, 'future')) {
					rform.recurrDate.focus();
					return false;
				}*/
			}
		}
	}

	// surgeon validation
	var table = document.getElementById('resourceTable');
	var length = table.rows.length;
	var id = length - 1;
	var scheduleId = document.getElementById('scNames').value;
	var otherResource = null;
	var otherResourceType = null;

	if (empty(scheduleId)) {
		document.getElementById('scheduleNameForAppointment').value = '';
	}
	if (rform.category.value == 'OPE' && !empty(scheduleId)) {
		if (!validateSurgeon(id)) return false;
		if (!validateSurgeonValue(id)) return false;
	}

	// check of operation_applicable and visit_type;
	if (!validateOperationApplicable()) return false;

	if (category == 'DOC') {
		if (screenId == "doc_week_scheduler") rform.action = "./docweekview.do";
		else rform.action = "./docappointments.do";
		otherResource = null;
	} else if (category == 'OPE') {
		if (screenId == "ope_week_scheduler") rform.action = "./opeweekview.do";
		else rform.action = "./opeappointments.do";
		otherResourceType = 'SUR';
	} else if (category == 'SNP') {
		rform.action = "./snpappointments.do";
		otherResourceType = 'SER';
	} else if (category == 'DIA') {
		rform.action = "./diaappointments.do";
		otherResourceType = 'TST';
	}

	var centralResource;
	var resourceId;
	var appointmentStatus = document.resourceform.status.value;
	var mrno = rform.mrno.value;
	var name = rform.patName.value;
	var mobno = rform.patContact.value;
	var contactId = rform.contactId.value;

	if (category == 'DOC') {
		centralResource = rform.scNames.value;
		resourceId = document.resourceform.scheduleNameForAppointment.value;
	} else {
		centralResource = rform.centralResource.value;
		resourceId = document.resourceform.centralResource.value;
	}

	otherResource = (category == 'DOC' ? null : document.resourceform.scheduleNameForAppointment.value);
	var apptDate = document.getElementById('datelbl').textContent;
	var apptTime = document.getElementById('timeList').value;
	var overbook_limit = document.resourceform.overbook_limit.value;
	var apptList;



	if(overbook_limit != "" && overbook_limit != null && overbook_limit == 0){
		if(appointmentStatus == 'Cancel'){
		} else if(appointmentStatus == 'Noshow'){
		} else {
		if (!isSlotBooked(apptDate,apptTime,appointmentId,duration,category,"","")) { // checking availability of the slot.
				rform.duration.focus();
				return false;
			}
		}


	}

	if(!empty(appointmentStatus) && appointmentStatus == 'Cancel') {
	} else if(!empty(appointmentStatus) && appointmentStatus == 'Noshow') {
	} else {
		if (!isResourceBooked(date,time,appointmentId,duration,category,null,null,"","")) {// checking secondary resource booking
			return false;
		}

		if (!isResourceUnavailableWithinGivenTime(date,time,category,duration,null,null,"","",otherResource,otherResourceType)) {
			return false;
		}
		if (!getSlotAvailableForPatient(date,time,duration,appointmentId,mrno,name,mobno,contactId)){
			return false;
		}
		if (!getResourceTimings()) {
			var list = filterList(scheduleResourceListJSON,"resource_id",centralResource);
			var msg=list[0].resource_name;
			msg+=getString("js.scheduler.doctorscheduler.notavailable");
			msg+=rform.timeList.value;
			msg+=getString("js.scheduler.doctorscheduler.initial.clock");
			alert(msg);
			return false;
		}
	}

	return true;
}

function isResourceBooked(date,time,appointmentId,duration,category,resourcesList,resourceTypesArr,primaryResource,primaryResourceType) {
	var table = document.getElementById('resourceTable');
	var length = table.rows.length;
	var id = length - 1;
	var primaryRes = "";
	var resources = new Array();
	var resourceTypes = new Array();

	if(empty(primaryResourceType)) {
		if (category == 'DOC')
			primaryResourceType = "OPDOC";
		else if (category == 'SNP')
			primaryResourceType = "SRID";
		else if (category == 'DIA')
			primaryResourceType = "EQID";
		else if (category == 'OPE')
			primaryResourceType = "THID";
	}

	resourceTypes[0] = primaryResourceType;

	if (resourcesList == null) {
		if (category == 'DOC')
			primaryRes = document.resourceform.scheduleNameForAppointment.value;
		else
			primaryRes = document.resourceform.centralResource.value;

		resources[0] = primaryRes;
		var k =1;
		for(var i=0;i<id;i++) {
			var resourceDelete = document.getElementById('resourceDelete'+i).value;
			if((!empty(resourceDelete) && resourceDelete != 'Y')) {
				resources[k] = document.getElementById('resourceValue'+i).value;
				k++;
			}
		}

		var l= 1;
		for(var i=0;i<id;i++) {
			var resourceDelete = document.getElementById('resourceDelete'+i).value;
			if((!empty(resourceDelete) && resourceDelete != 'Y')) {
				resourceTypes[l] = document.getElementById('resourceName'+i).value;
				l++;
			}
		}

	} else {
		primaryRes = primaryResource;
		resources[0] = primaryRes;
		var k =1;
		for(var i=0;i<resourcesList.length;i++) {
			resources[k] = resourcesList[i];
			k++;
		}

		var l= 1;
		for(var i=0;i<resourceTypesArr.length;i++) {
			resourceTypes[l] = resourceTypesArr[i];
			l++;
		}
	}

	var defaultDur = document.resourceform.defaultDuration.value;
	var appointmentId = appointmentId;
	var timeArr = time.split(":");
	var totalCalTime = 60 * timeArr[0] + parseInt(timeArr[1]) + parseInt(duration);
	var totalCalHrAndMins = (totalCalTime / 60);
	var remainder = (totalCalTime % 60);
	var totalCalHrAndMinsStr = totalCalHrAndMins.toString();
	var totalCalHrsArr = null;
	var totalCalHrs = null;

	if (totalCalHrAndMinsStr.indexOf(".") != -1) {
		totalCalHrsArr = totalCalHrAndMinsStr.split(".");
	}

	if (totalCalHrsArr != null) {
		totalCalHrs = totalCalHrsArr[0];
	} else {
		totalCalHrs = totalCalHrAndMinsStr;
	}

	var totalTime = totalCalHrs + ":" + remainder;
	var startTime = date + " " + time;
	var endTime = date + " " + totalTime;

	var reqObject = newXMLHttpRequest();
	var url = cpath+"/pages/resourcescheduler/addeditappointments.do?method=isResourceBooked"
		url +='&startTime='+encodeURIComponent(startTime);
		url +='&endTime='+encodeURIComponent(endTime);
		url +='&appointment_id='+encodeURIComponent(appointmentId);
		url +='&duration='+encodeURIComponent(duration);
		url +='&category='+encodeURIComponent(category);
		url +='&resource_ids='+encodeURIComponent(resources);
		url +='&resource_types='+encodeURIComponent(resourceTypes);
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (reqObject.responseText != null)) {
			if (reqObject.responseText != 'null' && !empty(reqObject.responseText)) {
				var msg=reqObject.responseText;
				alert(msg);
				return false;
			}
		}
		else
			return false;
	}
	return true;
}

function isResourceUnavailableWithinGivenTime(date,time,category,duration,resourcesList,resourceTypesArr,
						primaryResource,primaryResourceType,otherResource,otherResourceType) {
	var table = document.getElementById('resourceTable');
	var length = table.rows.length;
	var id = length - 1;
	var resources = new Array();
	var centralResource = null;
	var resourceTypes = new Array();

	if(empty(primaryResourceType)) {
		if (category == 'DOC')
			primaryResourceType = "OPDOC";
		else if (category == 'SNP')
			primaryResourceType = "SRID";
		else if (category == 'DIA')
			primaryResourceType = "EQID";
		else if (category == 'OPE')
			primaryResourceType = "THID";
	}

	resourceTypes[0] = primaryResourceType;

	if (resourcesList == null) {
		if (category == 'DOC')
			primaryRes = document.resourceform.scheduleNameForAppointment.value;
		else
			primaryRes = document.resourceform.centralResource.value;

		resources[0] = primaryRes;
		var k =1;
		for(var i=0;i<id;i++) {
			var resourceDelete = document.getElementById('resourceDelete'+i).value;
			if((!empty(resourceDelete) && resourceDelete != 'Y')) {
				resources[k] = document.getElementById('resourceValue'+i).value;
				k++;
			}
		}

		var l= 1;
		for(var i=0;i<id;i++) {
			var resourceDelete = document.getElementById('resourceDelete'+i).value;
			if((!empty(resourceDelete) && resourceDelete != 'Y')) {
				resourceTypes[l] = document.getElementById('resourceName'+i).value;
				l++;
			}
		}

	} else {
		primaryRes = primaryResource;
		resources[0] = primaryRes;
		var k =1;
		for(var i=0;i<resourcesList.length;i++) {
			resources[k] = resourcesList[i];
			k++;
		}

		var l= 1;
		for(var i=0;i<resourceTypesArr.length;i++) {
			resourceTypes[l] = resourceTypesArr[i];
			l++;
		}
	}

	if (!empty(otherResource) && otherResourceType != 'SUR'
			&& otherResourceType != 'SER' && otherResourceType != 'TST') {
		resources[resources.length + 1] = otherResource;
		resourceTypes[resourceTypes.length + 1] = otherResourceType;
	}

	var defaultDur = document.resourceform.defaultDuration.value;
	var appointmentId = appointmentId;
	var timeArr = time.split(":");
	var totalCalTime = 60 * timeArr[0] + parseInt(timeArr[1]) + parseInt(duration);
	var totalCalHrAndMins = (totalCalTime / 60);
	var remainder = (totalCalTime % 60);
	var totalCalHrAndMinsStr = totalCalHrAndMins.toString();
	var totalCalHrsArr = null;
	var totalCalHrs = null;

	if (totalCalHrAndMinsStr.indexOf(".") != -1) {
		totalCalHrsArr = totalCalHrAndMinsStr.split(".");
	}

	if (totalCalHrsArr != null) {
		totalCalHrs = totalCalHrsArr[0];
	} else {
		totalCalHrs = totalCalHrAndMinsStr;
	}

	var totalTime = totalCalHrs + ":" + remainder;
	var startTime = date + " " + time;
	var endTime = date + " " + totalTime;
	var schedulableCenterId = document.getElementById("ah_center_id").value;
	if(schedulableCenterId == null || empty(schedulableCenterId)){
		schedulableCenterId = document.getElementById("center_id").value;
	}
	schedulableCenterId = !empty(schedulableCenterId) ? schedulableCenterId : loggedInCenterId;

	var reqObject = newXMLHttpRequest();
	var url = "../../pages/resourcescheduler/addeditappointments.do?method=isResourceUnavailableWithinGivenTime"
		 +"&resource_ids="+resources+"&colDate="+date+"&startTime="+startTime+"&endTime="+endTime+"&category="+category+"&resourceTypes="+resourceTypes+"&schedulableCenterId="+ schedulableCenterId;
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (reqObject.responseText != null)) {
			if (reqObject.responseText != 'null' && !empty(reqObject.responseText)) {
				var msg = reqObject.responseText;
				msg = msg +" "+ getString("js.scheduler.todaysappointment.notavailable");
				alert(msg);
				return false;
			}
		}
	}
	return true;
}

function getSlotAvailableForPatient(date,time,duration,appointmentId,mrno,patName,mobno,contactId) {
	var patAppInfo = null;
	var msg =null;
	var defaultDur = document.resourceform.defaultDuration.value;
	var timeArr = time.split(":");
	var totalCalTime = 60 * timeArr[0] + parseInt(timeArr[1]) + parseInt(duration);
	var totalCalHrAndMins = (totalCalTime / 60);
	var remainder = (totalCalTime % 60);
	var totalCalHrAndMinsStr = totalCalHrAndMins.toString();
	var totalCalHrsArr = null;
	var totalCalHrs = null;

	if (totalCalHrAndMinsStr.indexOf(".") != -1) {
		totalCalHrsArr = totalCalHrAndMinsStr.split(".");
	}

	if (totalCalHrsArr != null) {
		totalCalHrs = totalCalHrsArr[0];
	} else {
		totalCalHrs = totalCalHrAndMinsStr;
	}

	var totalTime = totalCalHrs + ":" + remainder;
	var startTime = date + " " + time;
	var endTime = date + " " + totalTime;

	var reqObject = newXMLHttpRequest();
	var url = cpath+"/pages/resourcescheduler/addeditappointments.do?method=getAppointmentDetailsForDuplication"
		url +='&startTime='+encodeURIComponent(startTime);
		url +='&endTime='+encodeURIComponent(endTime);
		url +='&duration='+encodeURIComponent(duration);
		url +='&appointment_id='+encodeURIComponent(appointmentId);
		url +='&mrno='+encodeURIComponent(mrno);
		url +='&contactId='+encodeURIComponent(contactId);
		url +='&patName='+encodeURIComponent(patName);
		url +='&mobNo='+encodeURIComponent(mobno);
		url +='&category='+encodeURIComponent(category);
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (reqObject.responseText != null)) {
			if (reqObject.responseText != 'null' && !empty(reqObject.responseText)) {
				eval("patAppInfo =" + reqObject.responseText);

				var appDetail = patAppInfo.appInfo;
				if(appDetail.responseContent == 'true') {

						var startBookingTime =formatDateTime(new Date(appDetail.appointment_time),null,null);
						var endBookingTime =formatDateTime(new Date(appDetail.end_appointment_time),null,null);
						//if(reqObject.responseText != null) {
						msg=getString("js.scheduler.todaysappointment.bookedslotfor");
						msg+=" "+appDetail.resource+" ";
						msg+=getString("js.scheduler.todaysappointment.bookedsloton");
						msg+= " "+startBookingTime+" ";
						msg+=getString("js.scheduler.todaysappointment.bookedslotto");
						msg+= " "+endBookingTime + ". ";
						msg+=getString("js.scheduler.todaysappointment.bookedslotreschedule");
						alert(msg);
				return false;
					//}
				}
			}
		}
	}

	return true;

}
function validateCancelReason() {
	var statusObj = document.resourceform.status;
	var reasonObj = document.resourceform.cancel_reason;
	if (statusObj != null && statusObj.value == 'Cancel') {
		if (cancelAppointment != 'A') {
				showMessage("js.scheduler.doctorscheduler.notauthorized");
				statusObj.focus();
				return false;

		}
		if (!reasonObj.disabled && empty(trim(reasonObj.value))) {

			if (cancelAppointment == 'A') {
				var ok = confirm(" Appointment exists. \n Do you want to continue appointment cancellation ?");
				if (ok) {
					showMessage("js.scheduler.doctorscheduler.validreason.cancelappointment");
					reasonObj.focus();
					return false;
				}else {
					return false;
				}
			}
		}
	}
	return true;
}

function isDuplicateConsultation() {
	if (category == 'DOC') {
		if (rform.patient_id.value != 'None' && rform.patient_id.value != '') {
			var ajaxReqObject = newXMLHttpRequest();
			var url = "./addeditappointments.do?method=getDuplicateAppDetails";
			url = url + "&patient_id=" + rform.patient_id.value;
			var reqObject = newXMLHttpRequest();
			reqObject.open("POST", url.toString(), false);
			reqObject.send(null);
			if (reqObject.readyState == 4) {
				if ((reqObject.status == 200) && (reqObject.responseText != null)) {
					eval("var consList = " + reqObject.responseText);
					if (consList != null && consList.length > 0) {
						if (rform.status.value != 'Cancel') {
							if (consList[0].visit_type == 'o') {
								return true;
							}
						}
					}
				}
			}
		}
	}
	return false;
}

function checkDuplicateAppointment() {
	if (category == 'DOC') {
		var isDuplicateCons = isDuplicateConsultation();
		if (isDuplicateCons) {
			if (eClaimModule == 'Y') {
				var msg=getString("js.scheduler.doctorscheduler.consultation.notallowed");
				msg+="\n";
				msg+=getString("js.scheduler.doctorscheduler.notorder.moreoneconsultation");
				alert(msg);
				return false;
			} else {
				return confirm(" Patient has a consultation already."+
								"\n Do you want to continue ?");
			}
		}
	}
	return true;
}

function getResourceScheduleId() {
	var scheduleId = "";
	if (category == 'DOC') {
		scheduleId = document.resourceform.consultationTypes.value;
	} else {
		scheduleId = document.resourceform.scheduleNameForAppointment.value;
	}
	return scheduleId;
}

function checkOrderOrConsultation() {
	var msg = "";
	var schIdObj = null;
	if (category == 'DOC') {
		schIdObj = document.resourceform.consultationTypes;
	} else {
		schIdObj = document.resourceform.scheduleNameForAppointment;
	}
	var scheduleId = getResourceScheduleId();
	var resourcetxt = "";
	if (category == 'SNP')
		resourcetxt = "service";
	else if (category == 'DIA')
		resourcetxt = "test";
	else if (category == 'OPE')
		resourcetxt = "surgery";

	if (category == 'DOC') {
		msg+= getString("js.scheduler.todaysappointment.warning.noconsultation.selected");
		msg+="\n";
		msg+=getString("js.scheduler.todaysappointment.continue");
		//alert(msg);
	} else {
		msg+= getString("js.scheduler.todaysappointment.warningno");
		msg+=" ";
		msg+=resourcetxt;
		msg+=" ";
		msg+=getString("js.scheduler.todaysappointment.is.selected");
		//alert(msg);
	}
	if (category == 'DOC') {
		if(empty(rform.patient_id.value) || (!empty(rform.patient_id.value) && rform.patient_id.value == 'None')) {
		} else {
			if(empty(scheduleId)) {
				var ok = confirm(msg);
				if (!ok) {
					schIdObj.focus();
					return false;
				}
			}
		}
	}else{
		if(empty(scheduleId)) {
			var ok = confirm(msg);
			if (!ok) {
				schIdObj.focus();
				return false;
			}
		}
	}
	return true;
}

function isSlotBooked(date,time,appointmentId,duration,category,primaryResource,primaryResourceType) {
	if(empty(primaryResource)) {
		if(category =='DOC')
			primaryResource = rform.scheduleNameForAppointment.value;
		else
			primaryResource = rform.centralResource.value;
	}

	if(empty(primaryResourceType)) {
		if (category == 'DOC')
			primaryResourceType = "OPDOC";
		else if (category == 'SNP')
			primaryResourceType = "SRID";
		else if (category == 'DIA')
			primaryResourceType = "EQID";
		else if (category == 'OPE')
			primaryResourceType = "THID";
	}

	var timeArr = time.split(":");
	var totalCalTime = 60 * timeArr[0] + parseInt(timeArr[1]) + parseInt(duration);
	var totalCalHrAndMins = (totalCalTime / 60);
	var remainder = (totalCalTime % 60);
	var totalCalHrAndMinsStr = totalCalHrAndMins.toString();
	var totalCalHrsArr = null;
	var totalCalHrs = null;

	if (totalCalHrAndMinsStr.indexOf(".") != -1) {
		totalCalHrsArr = totalCalHrAndMinsStr.split(".");
	}

	if (totalCalHrsArr != null) {
		totalCalHrs = totalCalHrsArr[0];
	} else {
		totalCalHrs = totalCalHrAndMinsStr;
	}

	var totalTime = totalCalHrs + ":" + remainder;
	var startTime = date + " " + time;
	var endTime = date + " " + totalTime;

	var reqObject = newXMLHttpRequest();
	var url = "../../pages/resourcescheduler/addeditappointments.do?method=isSlotBooked&startTime="
			+ startTime + "&endTime=" + endTime + "&schName=" + primaryResource + "&appointmentId=" + appointmentId+"&primary_resource="+primaryResource+"&primary_resource_type="+primaryResourceType;
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (reqObject.responseText != null)) {
			if (!empty(reqObject.responseText)) {
				if (reqObject.responseText == 'true') {
					var msg=getString("js.scheduler.doctorscheduler.change.duration");
							msg+="\n";
							msg+=getString("js.scheduler.doctorscheduler.alreadybooked");

							alert(msg);
					return false;
				}
			}
		}
	}
	return true;
}

function checkOrganization(orgName) {
	var scheduleId = "";
	if (category == 'DOC') {
		scheduleId = document.resourceform.consultationTypes.value;
	} else {
		scheduleId = document.resourceform.scheduleNameForAppointment.value;
	}
	var reqObject = newXMLHttpRequest();
	var url = "../../pages/resourcescheduler/addeditappointments.do?method=isRatePlanApplicable&category=" + category + "&orgId=" + orgId + "&schedule_id=" + scheduleId;
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (reqObject.responseText != null)) {
			if (!empty(reqObject.responseText)) {
				if (reqObject.responseText != 't') {

					if (category == 'SNP') {
						var msg=getString("js.scheduler.doctorscheduler.selectedservice.notapplicable");
						msg+="\n";
						msg+= orgName;
						alert(msg);
						//showMessage("js.scheduler.doctorscheduler.selectedservice.notapplicable: \n" + orgName);
						return false;
					} else if (category == 'DIA') {
					var msg=getString("js.scheduler.doctorscheduler.selectedsurgery.notapplicable");
						msg+="\n";
						msg+= orgName;
						alert(msg);
						//showMessage("js.scheduler.doctorscheduler.selectedsurgery.notapplicable: \n" + orgName);
						return false;
					} else if (category == 'OPE') {
					var msg=getString("js.scheduler.doctorscheduler.selectedtest.notapplicable");
						msg+="\n";
						msg+= orgName;
						alert(msg);
						//showMessage("js.scheduler.doctorscheduler.selectedtest.notapplicable: \n" + orgName);
						return false;
					} else if (category == 'DOC' && !empty(scheduleId)) {
					var msg=getString("js.scheduler.doctorscheduler.selectedconsultationtype.notapplicable");
						msg+="\n";
						msg+= orgName;
						alert(msg);
						//showMessage("js.scheduler.doctorscheduler.selectedconsultationtype.notapplicable: \n" + orgName);
						return false;
					}
				}
			}
		}
	}
	return true;
}

function validateSurgeon(id) {
	var flag = false;
	for (var i = 0; i < id; i++) {
		if (document.getElementById('resourceName' + i).value == 'SUDOC') {
			flag = true;
			break;
		}
	}
	if (!flag && !empty(document.getElementById('scNames').value)) {
		showMessage("js.scheduler.doctorscheduler.select.surgeon");
		document.getElementById('resourceName0').focus();
	}
	return flag;
}

function validateSurgeonValue(id) {
	var flag = true;
	var j = -1;
	for (var i = 0; i < id; i++) {
		if (rform.category.value == 'OPE') {
			if (document.getElementById('resourceName' + i).value == 'SUDOC') {
				if (empty(document.getElementById('resourceValue' + i).value)) {
					showMessage("js.scheduler.doctorscheduler.select.surgeon");
					document.getElementById('resourceValue' + i).focus();
					return false;
				}
			}

			if (document.getElementById('resourceDelete' + i).value == 'Y'
					&& document.getElementById('resourceName' + i).value == 'SUDOC') {
				flag = false;
				j = i;
			}

			if (document.getElementById('resourceDelete' + i).value == 'N'
					&& document.getElementById('resourceName' + i).value == 'SUDOC') {
				flag = true;
				j = i;
			}
		}
	}
	if (!flag) {
		showMessage("js.scheduler.doctorscheduler.surgeonname.required");
		if (j >= 0) document.getElementById('resourceValue' + j).focus();
		return false;
	}
	return flag;
}

function validateOperationApplicable() {
	if (category == 'OPE') {
		var visitType = rform.visitType.value;
		var visitTypetxt = (visitType == 'o' ? "OP Patient" : "IP Patient");
		var surgeryApplicable = (opApplicableFor == 'b') ? visitType : opApplicableFor;
		if (rform.patient_id.value != 'None' && rform.patient_id.value != '' && visitType != surgeryApplicable) {
			var msg = getString("js.scheduler.todaysappointment.operation.notapplicable");
			msg+= visitTypetxt;
			msg+= "\n";
			alert(msg);
			if (visitType == 'o') {
				msg += getString("js.scheduler.todaysappointment.oppatient.convert.ippatient");
				var ok = confirm(msg);
				if (!ok) return false;
			}else {
				if (opApplicableFor != 'b') {
					var opApplicableForTxt = (opApplicableFor == 'i' ? "IP Patient" : "OP Patient");
					msg+= getString("js.scheduler.todaysappointment.visit.none");
					msg+=opApplicableForTxt;
					msg+=getString("js.scheduler.todaysappointment.complete.appointment");
					alert(msg);
					return false;
				}
			}
		}
	}
	return true;
}

/* Validations on dialog submit end */

/* Doctor Non availability */

function markResourceAvailable(schName, rowTime, index, colDate) {
	avlform.resourceId.value = schName;
	avlform.date.value = colDate;
	avlform.slotTime.value = rowTime;
	avlform.includeResources.value = includeresources();
	avlform.submit();
}
var resourceId = null;
function showResourceNonAvailabilityDialog(id, slotClass, schName, rowTime, index, colDate, resourceAvailabiltyCenterId) {

	prevClass = document.getElementById('toolbarRow' + id).className;
	document.getElementById('toolbarRow' + id).className = 'rowbgToolBar';
	gMarkAvlNonAvl = "markNonAvailable";
	nform.date.value = colDate;
	gResAvailCenterId = resourceAvailabiltyCenterId ;
	var ahref = document.getElementById("toolbarRow" + id);
	nonavailabilitydialog.cfg.setProperty("context", [ahref, "tl", "bl"], false);
	document.getElementById("nonAvailabilityDialogId").value = id;
	var dt = nform.date.value;
	var sel = document.getElementById("resourceSchedule" + index);
	var doctorName = sel.options[sel.options.selectedIndex].text;

	nform.nonAvailableDoctor.value = schName;
	document.getElementById("nonAvailableDoctorLbl").textContent = doctorName;
	document.getElementById("nonAvailableDateLbl").textContent = dt;
	resourceId = schName;

	nform.firstSlotFromTime.value = rowTime;
	nform.firstSlotToTime = '';

	var tdObj = document.getElementById("toolbarRow" + id);
	YAHOO.lutsr.accordion.collapse(document.getElementById("recurranceDD"));
	slotobj = tdObj;
	oldClass = slotClass + ' ' + mouserPointer;
	tdObj.className = tdObj.className + ' ' + mouserPointer;

	nonavailabilitydialog.show();
}

function onEditDoctorNonAvailabilitySubmit() {
	nform.includeResources.value = includeresources();
	resForm.includeResources.value = includeresources();

	if (!validateDoctorNonAvailabilityDialog()) return false;
	document.getElementById('nonAvailOk').disabled = true;
	nform.submit();
}

function onEditDoctorNonAvailabilityCancel() {
	slotobj.className = oldClass;
	document.getElementById('firstSlotToTime').value = '';
	nonavailabilitydialog.cancel();
}

function validateDoctorNonAvailabilityDialog() {

	var amsttime = document.getElementById("firstSlotFromTime").value.split(":");
	var amendtime = document.getElementById("firstSlotToTime").value.split(":");

	if(empty(document.getElementById("firstSlotFromTime").value)) {
		showMessage("js.scheduler.doctorscheduler.fromtime.required");
		document.getElementById("firstSlotFromTime").focus();
		return false;
	}

	if(empty(document.getElementById("firstSlotToTime").value)) {
		showMessage("js.scheduler.doctorscheduler.totime.required");
		document.getElementById("firstSlotToTime").focus();
		return false;
	}

	if (!empty(document.getElementById("firstSlotFromTime").value) && !empty(document.getElementById("firstSlotToTime").value)) {
		if (!validateTime(document.getElementById("firstSlotFromTime"))) return false;
		if (!validateTime(document.getElementById("firstSlotToTime"))) return false;
		if (!compareTimes(document.getElementById("firstSlotToTime"), amsttime, amendtime)) return false;
	}

	if (!checkTimeOverlapping(amsttime,amendtime)) {
		showMessage("js.scheduler.doctorscheduler.timeslot.overlapping");
		document.getElementById("firstSlotFromTime").focus();
		return false;
	}

	if (!isAppointmentExistsForAResource()) {
		var msg = getString("js.scheduler.doctorscheduler.this.string")+" ";
		if (category == 'DOC')
			msg +=getString("js.scheduler.doctorscheduler.resource.type.doctor.string")+" ";
		else if (category == 'DIA')
			msg +=getString("js.scheduler.doctorscheduler.resource.type.test.equipment.string")+" ";
		else if (category == 'SNP')
			msg += getString("js.scheduler.doctorscheduler.resource.type.service.resource.string")+" ";
		else if (category == 'OPE')
			msg +=getString("js.scheduler.doctorscheduler.resource.type.theatre.string")+" ";
		msg += getString("js.scheduler.doctorscheduler.appointments.already.exixts.for.resource.msg");
		var ok = confirm(msg);
		if (!ok)
		return false;
	}

	return true;
}

function isAppointmentExistsForAResource() {
	var unavailableStartTime = document.getElementById("firstSlotFromTime").value;
	var unavailableEndTime = document.getElementById("firstSlotToTime").value;
	var scheduleDate = nform.date.value;
	var unavailableStartDateTime = scheduleDate+" "+unavailableStartTime;
	var unavailableEndDateTime =scheduleDate+" "+unavailableEndTime;

	if (!empty(category) && !empty(resourceId)) {
		var reqObject = newXMLHttpRequest();
		var url = "./addeditappointments.do?method=getResourceAppointments"
		url = url + "&resource_type=" + category;
		url = url + "&resource_id=" + resourceId;
		url = url + "&start_non_available_time=" + unavailableStartDateTime;
		url = url + "&end_non_available_time=" + unavailableEndDateTime;
		url = url + "&start_non_available_time=" + unavailableStartDateTime;
		reqObject.open("POST", url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ((reqObject.status == 200) && (reqObject.responseText != null)) {
				eval("response =" + reqObject.responseText);
				if (response.success != "" && response.success == 'true') {
					return false;
				}
			}
		}
	}

	return true;
}

function checkTimeOverlapping(startTime,endTime) {
	var startTime0 = startTime[0];
	var startTime1 = startTime[1];
	var endTime0 = endTime[0];
	var endTime1 = endTime[1];
	var startDate = new Date();
	var endDate  = new Date();

	startDate.setHours(startTime0);
	startDate.setMinutes(startTime1);
	startDate.setSeconds(0);

	endDate.setHours(endTime0);
	endDate.setMinutes(endTime1);
	endDate.setSeconds(0);
	var startTimeInLong = startDate.getTime();
	var endTimeInLong = endDate.getTime();
	var scheduleDate = nform.date.value;
	if(category == 'DOC' && max_centers_inc_default > 1){
		if(gMarkAvlNonAvl == "markNonAvailable")
			scheduleDate = nform.date.value;
		else
			scheduleDate = avlform.date.value;
	}
	var timingList = null;

//passed center id
	var resourceCenterId = document.getElementById("centerId").value;
	resourceCenterId = !empty(resourceCenterId) ? resourceCenterId : loggedInCenterId;
	if (!empty(category) && !empty(resourceId)) {
		var ajaxReqObject = newXMLHttpRequest();
		var url = "./addeditappointments.do?method=getAvlNonAvlTimingList"
		url = url + "&category=" + category;
		url = url + "&scheduleName=" + resourceId;
		url = url + "&scheduleDate=" + scheduleDate;
		url = url + "&resourceCenterId="+ resourceCenterId;
		var reqObject = newXMLHttpRequest();
		reqObject.open("POST", url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ((reqObject.status == 200) && (reqObject.responseText != null)) {
				eval("timingList =" + reqObject.responseText);
				if (!handleResponseContent(timingList,startTimeInLong,endTimeInLong))

					return false;
			}
		}
	}
	return true;
}
function handleResponseContent(timingList,startTimeInLong,endTimeInLong) {
	var dbFromTimeInLong = null;
	var dbToTimeInLong = null;
	var dbAvailCenterId = null;
	var startAvailSlotCenId = gResAvailCenterId;
	var startNotAvailSlotCenId = gResNotAvailCenterId;
	var f_date = new Date();
	var t_date = new Date();
	if (timingList != null && timingList.length > 0) {
		for (var i=0;i<timingList.length;i++) {
			var d1 = new Date(timingList[i].from_time);


			d1.setDate(f_date.getDate());
			d1.setMonth(f_date.getMonth());
			d1.setFullYear(f_date.getFullYear());
			d1.setSeconds(t_date.getSeconds());

			var d2 = new Date(timingList[i].to_time);

			d2.setDate(t_date.getDate());
			d2.setMonth(t_date.getMonth());
			d2.setFullYear(t_date.getFullYear());
			d2.setSeconds(t_date.getSeconds());

			dbFromTimeInLong = d1.getTime();
			dbToTimeInLong = d2.getTime();

			dbAvailCenterId =  timingList[i].center_id;
			var availStatus = timingList[i].availability_status;
			if(gMarkAvlNonAvl == "markNonAvailable"){
				if (availStatus != 'A') {
					if ((endTimeInLong > dbFromTimeInLong) && (endTimeInLong < dbToTimeInLong)
							|| ((dbFromTimeInLong > startTimeInLong) && (endTimeInLong > dbToTimeInLong) && (dbToTimeInLong < endTimeInLong))) {

						return false;
					}
				}
				if(availStatus == 'A' && (category == 'DOC' && max_centers_inc_default > 1)){
					if ((endTimeInLong > dbFromTimeInLong) && (endTimeInLong < dbToTimeInLong)
							|| ((dbFromTimeInLong > startTimeInLong) && (endTimeInLong > dbToTimeInLong) && (dbToTimeInLong < endTimeInLong))) {

						if(dbAvailCenterId != startAvailSlotCenId)
						return false;
					}
				}
			}
			//validation check for markresource-available only for doctor resource
			if(gMarkAvlNonAvl == "markAvailable"){
				if (availStatus != 'N') {
					if ((endTimeInLong > dbFromTimeInLong) && (endTimeInLong < dbToTimeInLong)
							|| ((dbFromTimeInLong > startTimeInLong) && (endTimeInLong > dbToTimeInLong) && (dbToTimeInLong < endTimeInLong))) {

						return false;

					}
				}
				if(availStatus == 'N'){
					dbAvailCenterId = dbAvailCenterId == null? 0:dbAvailCenterId;
					if ((endTimeInLong > dbFromTimeInLong) && (endTimeInLong < dbToTimeInLong)
							|| ((dbFromTimeInLong > startTimeInLong) && (endTimeInLong > dbToTimeInLong) && (dbToTimeInLong < endTimeInLong))) {

						if(dbAvailCenterId != startNotAvailSlotCenId)
						return false;
					}
				}
			}
		}
	}
	return true;
}

function compareTimes(name, time1, time2) {
	if (eval(time1[0]) < eval(time2[0])) {} else {
		if (eval(time1[1]) < eval(time2[1])) {} else {
			showMessage("js.scheduler.doctorscheduler.toslot.morethan.fromslot");
			name.focus();
			return false;
		}
	}
	return true;
}

function getCompleteTime(obj) {
	var time = obj.value;
	if (time != '') {
		if (time.length <= 2) {
			obj.value = time + ":00";
		}
	}
}

function onCenterChangeSubmit(obj) {
	rform.includeResources.value = "";
	mform.includeResources.value = "";
	document.mainform.submit();


}
//On selecting center name populate selected center on add/edit appointment dialogBox when user logged as SuperCenter
function selectAppointmentCenter(obj,addOrEdit){
	var selectedCenter = document.getElementById('centerId').value;
	if(max_centers_inc_default > 1 && loggedInCenterId == 0){
		if(addOrEdit == 'add'){
				if(document.getElementById("allcenter").style.display == 'block'){
					//setSelectedIndex(document.getElementById("center_id"),selectedCenter);
					document.getElementById("center_id").value = selectedCenter;
					document.getElementById("ah_center_id").value = selectedCenter;
					document.getElementById("center_id").disabled = true;
				}
				if(document.getElementById("allcenter").style.display == 'none'){
					//setSelectedIndex(document.getElementById("_center_id"),selectedCenter);
					document.getElementById("_center_id").value = selectedCenter;
					document.getElementById("ah_center_id").value = selectedCenter;
					document.getElementById("_center_id").disabled = true;
				}
			}
		if(addOrEdit == 'edit'){
			if(document.getElementById("allcenter").style.display == 'block'){
				document.getElementById("center_id").value.show;
				document.getElementById("center_id").disabled = true;
			}else{ }
			if(document.getElementById("allcenter").style.display == 'none'){
				document.getElementById("_center_id").value.show;
				document.getElementById("_center_id").disabled = true;
			}else { }
		}
	}
}

function populateCenters(obj,addOrEdit) {
	var resources;
	if(!empty(obj.value)) {
		if(category == 'DOC' && max_centers_inc_default > 1 && loggedInCenterId == 0) {
			makeAjaxCall(obj,addOrEdit);
/*			resources = findInList(availbiltyList,"id",obj.value);
			var resourceCenterId = resources.center_id;
			rform._appointmentCenterId.value =  resourceCenterId;
			centerResourcesList = filterList(resourcesListJSON,"center_id",resourceCenterId);

			if(resourceCenterId != 0) {
				setSelectedIndex(document.getElementById('center_id'),resourceCenterId);
				document.getElementById('center_id').disabled = true;
				document.getElementById('_center_id').value = resourceCenterId;
			} else {
				if(addOrEdit == 'add') {
					setSelectedIndex(document.getElementById('center_id'),"");
					document.getElementById('center_id').disabled = false;
					document.getElementById('_center_id').value = resourceCenterId
				} else {
					setSelectedIndex(document.getElementById('center_id'),document.getElementById('center_id').value);
					document.getElementById('center_id').disabled = false;
					document.getElementById('_center_id').value = document.getElementById('center_id').value;
				}
			}
*/
			var table = document.getElementById("resourceTable");
			var rowCount = table.rows.length-1;
			for(var i = 0;i<rowCount;i++) {
				if(addOrEdit == 'add') {
					document.getElementById('resourceValue'+i).length = 1;
					document.getElementById('resourceName'+i).value = "";
					document.getElementById('resourceValue'+i).value = "";
				}
			}
		}
	}
	if(category == 'DOC' && max_centers_inc_default > 1 && loggedInCenterId == 0) {
		selectAppointmentCenter(obj,addOrEdit);
	}
}

function makeAjaxCall(obj,addOrEdit) {
	var doctorId = obj.value;
	var url = cpath +'/pages/resourcescheduler/docappointments.do?method=getResourseCenter&doctor_id='+doctorId;
		var ajaxObj = newXMLHttpRequest();
		ajaxObj.open("GET", url.toString(), false);
		ajaxObj.send(null);
		if (ajaxObj.readyState == 4 && ajaxObj.status == 200) {
			eval("var response =" +ajaxObj.responseText);
			var resourseCenterList = response.resourseCenterList;
			var cenId = document.getElementById('center_id').value;
			var innerhtml="";
			if(resourseCenterList.length > 1){
				innerhtml= "<option value=''>--Select--</option>";
			}
			for (var i=0; i<resourseCenterList.length; i++) {
				if(resourseCenterList[i].center_id != 0){
						setSelectedIndex(document.getElementById('center_id'),resourseCenterList[i].center_id);
						document.getElementById("allcenter").style.display = 'none';
						document.getElementById("belongcenter").style.display = 'block';
						if(addOrEdit == 'add') {
							document.getElementById('center_id').value = "";
						}

						if(cenId == resourseCenterList[i].center_id && addOrEdit != 'add'){
							innerhtml = innerhtml + "<option value="+resourseCenterList[i].center_id+" selected='selected'>"+resourseCenterList[i].center_name+"</option>";
						}else{
							innerhtml = innerhtml + "<option value="+resourseCenterList[i].center_id+">"+resourseCenterList[i].center_name+"</option>";
						}
				} else{
					document.getElementById("allcenter").style.display = 'block';
					document.getElementById("belongcenter").style.display = 'none';
					if(addOrEdit == 'add') {
						setSelectedIndex(document.getElementById('center_id'),"");
						document.getElementById('_center_id').value = resourseCenterList[i].center_id;
					} else {
						setSelectedIndex(document.getElementById('center_id'),document.getElementById('center_id').value);
						document.getElementById('_center_id').value = document.getElementById('center_id').value;
					}
				}
			}
			document.getElementById('_center_id').innerHTML = innerhtml;
		}
}

function changeCenterId(obj,addOrEdit) {
	document.getElementById('_center_id').value = obj.value;
	if(max_centers_inc_default > 1)
		populateResources(0);
}

/* Secondary Resources */

function populateResources(id) {
	var resourceName = "resourceValue" + id;
	var sourceName = "resourceName" + id;
	var centerId = parseInt(rform._center_id.value);
	document.getElementById(resourceName).length = 1;
	var resourceType = document.getElementById(sourceName).value;
	var list = resourcesListJSON;

	if(!empty(resourceType) && (resourceType == 'DOC' || resourceType == 'OPDOC'
		|| resourceType == 'LABTECH' || resourceType == 'ANEDOC' || resourceType == 'SUDOC'
		|| resourceType == 'ASUDOC' || resourceType == 'PAEDDOC')) {

		if(max_centers_inc_default > 1)
			list = filterListWithValues(list,"center_id",new Array(0,centerId));

		for (var i = 0; i < list.length; i++) {
			var record = list[i];
			if (record["resourcetype"] == document.getElementById(sourceName).value) {
				var option = new Option(record["resourcename"], record["resourceid"]);
				document.getElementById(resourceName).options[document.getElementById(resourceName).length] = option;
			//	document.getElementById('resource_name'+id).value = document.getElementById(sourceName).value;
			}
		}
	} else {
		if(max_centers_inc_default > 1)
			list = filterList(list,"center_id",centerId);
		if (!empty(list)) {
			for (var i = 0; i < list.length; i++) {
				var record = list[i];
				if (record["resourcetype"] == document.getElementById(sourceName).value) {
					var option = new Option(record["resourcename"], record["resourceid"]);
					document.getElementById(resourceName).options[document.getElementById(resourceName).length] = option;
				//	document.getElementById('resource_name'+id).value = document.getElementById(sourceName).value;
				}
			}
		}
	}
}

function addResourceRow() {
	var rtab = document.getElementById("resourceTable");
	var rlen = rtab.rows.length;
	var id = rlen - 1;
	var newRow = "",
		typeTd = "",
		resourceTd = "",
		rcheckbox = "";
	var typeId = "resourceName" + id;
	var resourceId = "resourceValue" + id;
	var imgDeleteCheckId = "resourceCheckBox" + id;
	var resourceDeleteId = "resourceDelete" + id;
	var appointment_item_id = "appointment_item_id" + id;
	var priresource = "priresource" + id;
	var imageSrc = cpath + "/icons/Delete.png";

	newRow = rtab.insertRow(rlen);
	var rowId = "row" + id;
	newRow.id = rowId;
	typeTd = newRow.insertCell(0);
	typeTd.setAttribute('class', 'first');

	var selObj = makeSelect('resourcename', typeId, 'dropdown');
	selObj.setAttribute('onchange', 'populateResources(' + id + ')');
	selObj.setAttribute("style","width:240px");
//	var hiddenNameObj = makeHidden('resource_name','resource_name'+id);

	typeTd.appendChild(selObj);
//	typeTd.appendChild(hiddenNameObj);

	resourceTd = newRow.insertCell(1);
	var selObj = makeSelect('resourcevalue', resourceId, 'dropdown');
//	var hiddenValueObj = makeHidden('resource_value','resource_value'+id);
	selObj.setAttribute('onchange', 'checkDuplicates(' + id + ');enableButton(' + id + ');');
	selObj.setAttribute("style","width:240px");
	resourceTd.appendChild(selObj);
	resourceTd.appendChild(makeHidden('item_id', appointment_item_id, ''));
	resourceTd.appendChild(makeHidden('priresource', priresource, ''));
//	resourceTd.appendChild(hiddenValueObj);

	loadSelectBox(document.getElementById(typeId), resourceTypes, "resource_description", "resource_type", "Select");
	rdeleteImg = newRow.insertCell(2);
	rdeleteImg.setAttribute('class', 'last');

	var imgEl = makeImageHref(imgDeleteCheckId, imageSrc, 'changeElsColor(' + id + ')', '', 'imgDelete');
	rdeleteImg.appendChild(imgEl);
	rdeleteImg.appendChild(makeHidden('rDelete', resourceDeleteId, 'N'));

	enableButton(id);
}

function checkDuplicates(index) {
	var rtab = document.getElementById("resourceTable");
	var rlen = rtab.rows.length - 1;
	var resourceType = document.getElementById("resourceName" + index).value;
	var resourceId = document.getElementById("resourceValue" + index).value;
	for (var k = 0; k < rlen; k++) {
		if ((k != index) && (resourceType == document.getElementById("resourceName" + k).value)) {
			if (resourceId == document.getElementById("resourceValue" + k).value) {
				showMessage("js.scheduler.doctorscheduler.duplicate.entry");
				document.getElementById("resourceValue" + index).selectedIndex = 0;
				return false;
			} else {}
		}
	}
	return true;
}

function deleteRows() {
	var table = document.getElementById("resourceTable");
	var rowCount = table.rows.length;
	for (var i = 1; i < rowCount; i++) {
		table.deleteRow(-1);
	}
}

function changeElsColor(index) {
	var markRowForDelete =
		document.getElementById('resourceDelete' + index).value
		= document.getElementById('resourceDelete' + index).value == 'N' ? 'Y' : 'N';

	if (markRowForDelete == 'Y') {
		addClassName('resourceName' + index, "delete");
		addClassName('resourceValue' + index, "delete");
	} else {
		removeClassName('resourceName' + index, "delete");
		removeClassName('resourceValue' + index, "delete");
	}
}

function enableButton(index) {
	var resourceType = document.getElementById("resourceName" + index).value;
	var resourceId = document.getElementById("resourceValue" + index).value;
	if (resourceType != "" && resourceId != "") {
		rform.addresource.disabled = false;
	} else {
		rform.addresource.disabled = true;
	}
//	document.getElementById('resource_value'+index).value = resourceId;
}

function validateWeekDays() {
	var weekEls = document.getElementsByName("weekCheck");
	for (var i = 0; i < weekEls.length; i++) {
		var el = weekEls[i];
		if (el.checked) {
			return true;
		}
	}
	return false;
}

function showHiderecurringTable(recurr) {
	if (recurr) {
		document.getElementById("recurringTable").style.display = 'block';
	} else {
		document.getElementById("recurringTable").style.display = 'none';
	}
}


/* Recurrence Appointment Table */

function decrRecurr() {
	if (rform.recurrNo.value == '0') {}
	else rform.recurrNo.value = eval(rform.recurrNo.value) - 1;
}

function incrRecurr() {
	if (rform.recurrNo.value == '50') {}
	else rform.recurrNo.value = eval(rform.recurrNo.value) + 1;
}

function decrOccurr() {
	if (rform.occurrNo.value == '0') {}
	else rform.occurrNo.value = eval(rform.occurrNo.value) - 1;
}

function incrOccurr() {
	if (rform.occurrNo.value == '50') {}
	else rform.occurrNo.value = eval(rform.occurrNo.value) + 1;
}

function selectGregorian(obj) {
	$('#monthImagePicker').calendarsPicker('clear');
	$('#untilImagePicker').calendarsPicker('clear');
	$('#monthImagePicker').calendarsPicker('destroy');
	$('#untilImagePicker').calendarsPicker('destroy');
	setRecurrDateWithGregorian();
	setUntilDateWithGregorian();
}

function selectHijri(obj) {
	$('#monthImagePicker').calendarsPicker('clear');
	$('#untilImagePicker').calendarsPicker('clear');
	$('#monthImagePicker').calendarsPicker('destroy');
	$('#untilImagePicker').calendarsPicker('destroy');
	setRecurrDateWithHijri();
	setUntilDateWithHijri();
}

function showHideWeekMonth(option) {
	var val = document.resourceform.repeatOption.value;
	if (option.value == 'W') {
		document.getElementById("weekTD").style.display = 'block';
		document.getElementById("monthTD").style.display = 'none';
		if(hijriPref == 'Y') {
			if(val == 'FOR')
		        document.getElementById("calendarTypeTr").style.display = 'none';
			else
				document.getElementById("calendarTypeTr").style.display = 'block';
		}
	} else if (option.value == 'M') {
		document.getElementById("monthTD").style.display = 'block';
		document.getElementById("weekTD").style.display = 'none';
		$('#monthImagePicker').calendarsPicker('clear');
		if(hijriPref == 'Y') {
		    document.getElementById("calendarTypeTr").style.display = 'block';
		    var gregCalendarType = document.getElementById("calendarTypeG").checked;
		    if(!gregCalendarType) {
		    	setRecurrDateWithHijri();
		    } else {
		    	setRecurrDateWithGregorian();
		    }
		} else {
			setRecurrDateWithGregorian();
		}
	} else {
		document.getElementById("monthTD").style.display = 'none';
		document.getElementById("weekTD").style.display = 'none';
		if(hijriPref == 'Y') {
			if(val == 'FOR')
		        document.getElementById("calendarTypeTr").style.display = 'none';
			else
				document.getElementById("calendarTypeTr").style.display = 'block';
		}
	}
}

function setRecurrDateWithHijri() {
	$('#monthImagePicker').calendarsPicker('clear');
	$('#monthImagePicker').calendarsPicker({calendar: $.calendars.instance('ummalqura'), showOnFocus: true,
		dateFormat: 'yyyy-mm-dd',  showTrigger: '<img src="' + imagePath + '" alt="Popup">', changeMonth: true, onSelect:function() {
			 var dates = $('#monthImagePicker').calendarsPicker('getDate');
			 //alert(dates[0]);
			 if(dates[0] != null) {
				var gregDate = convertHijriToGreg(dates[0]);
				var gregDateArr = gregDate.split("-");
				var date = new Date(gregDateArr[2], gregDateArr[1]-1, gregDateArr[0]);
				var currDate = new Date();
				if(date < currDate) {
					 alert(getString("js.common.date.can.not.be.in.past.string"));
					 document.resourceform.recurrDate.value = "";
					 var datesArr = [];
					 datesArr[0] = document.resourceform.recurrDate.value;
					 $('#monthImagePicker').calendarsPicker('setDate', datesArr);
				 } else {
					    document.resourceform.recurrDate.value = gregDate;
				 }
			 }
		 }
	});
}
function setUntilDateWithHijri() {
	$('#untilImagePicker').calendarsPicker('clear');
	$('#untilImagePicker').calendarsPicker({calendar: $.calendars.instance('ummalqura'), showOnFocus: true,
		dateFormat: 'yyyy-mm-dd',  showTrigger: '<img src="' + imagePath + '" alt="Popup">', changeMonth: true, onSelect:function() {
			 var dates = $('#untilImagePicker').calendarsPicker('getDate');
			 //alert(dates[0]);
			 if(dates[0] != null) {
				 var gregDate = convertHijriToGreg(dates[0]);
				 var gregDateArr = gregDate.split("-");
			     var date = new Date(gregDateArr[2], gregDateArr[1]-1, gregDateArr[0]);
				 var currDate = new Date();
				 if(date < currDate) {
					 alert(getString("js.common.date.can.not.be.in.past.string"));
					 document.resourceform.untilDate.value = "";
					 var datesArr = [];
					 datesArr[0] = document.resourceform.untilDate.value;
					 $('#untilImagePicker').calendarsPicker('setDate', datesArr);
				 } else {
					    document.resourceform.untilDate.value = gregDate;
				 }
			 }
		 }
	});
}

function convertHijriToGreg(dateIn) {
	var url = cpath +'/pages/resourcescheduler/docappointments.do?method=getHijriToGregDate&date='+dateIn;
	var ajaxObj = newXMLHttpRequest();
	ajaxObj.open("GET", url.toString(), false);
	ajaxObj.send(null);
	if (ajaxObj.readyState == 4 && ajaxObj.status == 200) {
		eval("var response =" +ajaxObj.responseText);
		return response.gregDate;
	} else
		return null;
}

function setRecurrDateWithGregorian() {
	$('#monthImagePicker').calendarsPicker('clear');
	$('#monthImagePicker').calendarsPicker({calendar: $.calendars.instance('gregorian'), showOnFocus: true,
		dateFormat: 'yyyy-mm-dd',  showTrigger: '<img src="' + imagePath + '" alt="Popup">', changeMonth: true, onSelect:function() {
			 var dates = $('#monthImagePicker').calendarsPicker('getDate');
			 if(dates[0] != null) {
				 var date = new Date(dates[0]);
				 var day = date.getDate();
				 var month = date.getMonth() + 1;
				 var year = date.getFullYear();
				 if(month < 10) {
					 month = '0' + month;
				 }
				 var dateStr = day + '-' + month + '-' + year;
				 var currDate = new Date();
				 if(date < currDate) {
					 alert(getString("js.common.date.can.not.be.in.past.string"));
					 document.resourceform.recurrDate.value = "";
					 var datesArr = [];
					 datesArr[0] = document.resourceform.recurrDate.value;
					 $('#monthImagePicker').calendarsPicker('setDate', datesArr);
				 } else {
			         document.resourceform.recurrDate.value = dateStr;
				 }
			 }
		 },
		 onChange: function() {
			 $(this).blur();
		 }
	});
}
function setUntilDateWithGregorian() {
	$('#untilImagePicker').calendarsPicker('clear');
	$('#untilImagePicker').calendarsPicker({calendar: $.calendars.instance('gregorian'), showOnFocus: true,
		dateFormat: 'yyyy-mm-dd',  showTrigger: '<img src="' + imagePath + '" alt="Popup">', changeMonth: true, onSelect:function() {
			 var dates = $('#untilImagePicker').calendarsPicker('getDate');
			 if(dates[0] != null) {
				 var date = new Date(dates[0]);
				 var day = date.getDate();
				 var month = date.getMonth() + 1;
				 var year = date.getFullYear();
				 if(month < 10) {
					 month = '0' + month;
				 }
				 var dateStr = day + '-' + month + '-' + year;
				 var currDate = new Date();
				 if(date < currDate) {
					 alert(getString("js.common.date.can.not.be.in.past.string"));
					 document.resourceform.untilDate.value = "";
					 var datesArr = [];
					 datesArr[0] = document.resourceform.untilDate.value;
					 $('#untilImagePicker').calendarsPicker('setDate', datesArr);
				 } else {
			         document.resourceform.untilDate.value = dateStr;
				 }
			 }
		 }
	});
}

function showHideForUntil(option) {
	var val = document.resourceform.recurranceOption.value;
	if (option.value == 'FOR') {
		document.getElementById("forTD").style.display = 'block';
		document.getElementById("untilTD").style.display = 'none';
		if(hijriPref == 'Y') {
			if(val == 'M') {
		        document.getElementById("calendarTypeTr").style.display = 'block';
			}
			else
				document.getElementById("calendarTypeTr").style.display = 'none';
		}
	} else if (option.value == 'UNTIL') {
		document.getElementById("untilTD").style.display = 'block';
		document.getElementById("forTD").style.display = 'none';
		$('#untilImagePicker').calendarsPicker('clear');
		if(hijriPref == 'Y') {
		    document.getElementById("calendarTypeTr").style.display = 'block';
		    var gregCalendarType = document.getElementById("calendarTypeG").checked;
		    if(!gregCalendarType) {
		    	setUntilDateWithHijri();
		    } else {
		    	setUntilDateWithGregorian();
		    }
		} else {
			setUntilDateWithGregorian();
		}
	} else {
		document.getElementById("forTD").style.display = 'block';
		if(hijriPref == 'Y') {
			if(val == 'M')
		        document.getElementById("calendarTypeTr").style.display = 'block';
			else
				document.getElementById("calendarTypeTr").style.display = 'none';
		}
	}
}

function assignWeek(check, day) {
	if (check.checked) {
		document.getElementById(day).disabled = false;
		document.getElementById(day).value = check.value;
	} else {
		document.getElementById(day).disabled = true;
		document.getElementById(day).value = '';
	}
}

function resetRecurranceTab() {
	rform.recurrNo.value = 0;
	rform.recurranceOption.selectedIndex = 0;
	showHideWeekMonth(rform.recurranceOption);
	rform.repeatOption.selectedIndex = 0;
	showHideForUntil(rform.repeatOption);
	rform.occurrNo.value = 0;

	for (var i = 0; i < rform.weekCheck.length; i++) {
		if (rform.weekCheck[i].checked) {
			rform.weekCheck[i].checked = false;
			if (rform.weekCheck[i].value == '2') {
				assignWeek(rform.weekCheck[i], 'mon');
			} else if (rform.weekCheck[i].value == '3') {
				assignWeek(rform.weekCheck[i], 'tue');
			} else if (rform.weekCheck[i].value == '4') {
				assignWeek(rform.weekCheck[i], 'wed');
			} else if (rform.weekCheck[i].value == '5') {
				assignWeek(rform.weekCheck[i], 'thu');
			} else if (rform.weekCheck[i].value == '6') {
				assignWeek(rform.weekCheck[i], 'fri');
			} else if (rform.weekCheck[i].value == '7') {
				assignWeek(rform.weekCheck[i], 'sat');
			} else if (rform.weekCheck[i].value == '1') {
				assignWeek(rform.weekCheck[i], 'sun');
			}
		}
	}
	rform.recurrDate.value = '';
	rform.untilDate.value = '';
}

/* Excluding or Including resources */

function excludeResource(id) {
	var excludeResource = document.getElementById('resourceSchedule' + id).value;
	var includeResources = "";
	var elmts = document.getElementsByName("resourceSchedule");
	for (var i = 0; i < elmts.length; i++) {
		if (elmts[(elmts.length - 1) - i].value != excludeResource) {
			includeResources = elmts[(elmts.length - 1) - i].value + "," + includeResources;
		}
	}
	includeResources = includeResources.substring(0, includeResources.length - 1);
	rform.includeResources.value = includeResources;
	mform.includeResources.value = includeResources;
	document.mainform.submit();
}

function includeresources() {
	var includeResources = "";
	var elmts = document.getElementsByName("resourceSchedule");
	for (var i = 0; i < elmts.length; i++) {
		includeResources = elmts[(elmts.length - 1) - i].value + "," + includeResources;
	}
	includeResources = includeResources.substring(0, includeResources.length - 1);
	return includeResources;
}

function showSchedules() {
	rform.includeResources.value = includeresources();
	mform.includeResources.value = includeresources();
	document.mainform.submit();
}

function addNewSchedulerResourceColumn(index) {

	var newIndex = parseInt(index);

	var schedulerRow = document.getElementById("schedulerRow");
	schedulerRow.appendChild(makeHidden("scheduleName", "scheduleName", ''));

	var td = schedulerRow.insertCell(-1);
	td.setAttribute("height", "100%");
	table = document.createElement("Table");
	table.setAttribute("height", "100%");
	td.appendChild(table);

	row = table.insertRow(-1);
	td = row.insertCell(-1);
	td.setAttribute("style", "height:22px;border-right:0.1em #CCCCCC solid");
	td.setAttribute("id", "resourceTd" + newIndex);

	var div = document.createElement("div");
	div.setAttribute("style", "float:left");
	var select = makeSelect("resourceSchedule", "resourceSchedule" + newIndex, '');
	select.setAttribute("class", "dropdown");
	select.setAttribute("onchange", "showSchedules()");
	div.appendChild(select);

	var anchor = document.createElement("a");
	anchor.href = "javascript:excludeResource(" + newIndex + ")";
	var img = document.createElement("img");
	img.setAttribute("title", "close");
	img.setAttribute("src", cpath + "/images/fileclose.png");
	img.setAttribute("style", "cursor:pointer;vertical-align:-.3em;");
	img.setAttribute("border", "0");
	img.setAttribute("width", "15");
	img.setAttribute("height", "15");
	anchor.appendChild(img);

	div.appendChild(anchor);

	td.appendChild(div);

	row = table.insertRow(-1);
	td = row.insertCell(-1);
	td.setAttribute("height", "100%");

	table = document.createElement("Table");
	table.setAttribute("height", "100%");
	table.setAttribute("style", "border-right:1px #CCCCCC solid");

	td.appendChild(table);

	for (var i = 0; i < rulerIterations; i++) {
		row = table.insertRow(-1);
		td = row.insertCell(-1);
		td.setAttribute("class", "defaultAvailable");
		td.setAttribute("id", "");
		td.setAttribute("style", "height:" + defaultHeight + "px;width: 176px;font-size:11px;");
	}

	loadSelectBox(document.getElementById("resourceSchedule" + newIndex), scheduleResourceListJSON, "resource_name", "resource_id", "-- Select --", "");
	document.getElementById("schTd").setAttribute("colspan", "5");
	//document.getElementById("navigateTd").setAttribute("colspan", "2");
}

function setResources() {
	var elmts = document.getElementsByName("resourceSchedule");
	var valueElmts = document.getElementsByName("scheduleName");
	for (var i = 0; i < elmts.length; i++) {
		setSelectedIndex(elmts[i], valueElmts[i].value);
	}
}

/*function makePopupCalendar(dateId, pos) {
	// the original div is hidden, need to unhide it after a dialog has been constructed over it.
	// The dialog is now hidden, so it will not show even now. But, on click of the button, it will.
	document.getElementById(dateId + "_container").style.display = 'block';
	var popupCal = new YAHOO.widget.Calendar(dateId + "_cal", {
		iframe: false,
		hide_blank_weeks: true,START_WEEKDAY: gCalendarStartDay
	});
	popupCal.selectEvent.subscribe(handleDateSelect, popupCal, false);
	var corner;
	if ((pos != null) && (pos == 'left')) {
		corner = "tr";
	} else {
		corner = "tl";
	}
	popupCal.dateId = dateId;
	calendarObjs[dateId] = popupCal;
	var dialog = new YAHOO.widget.Dialog(dateId + "_container", {
		context: [dateId, corner, "br", ["beforeShow", "windowResize"]],
		width: "16em", // Sam Skin dialog needs to have a width defined (7*2em + 2*1em = 16em).
		draggable: false,
		close: true
	});
	popupCal.renderEvent.subscribe(function () {
		dialog.fireEvent("changeContent");
	});
	dialogObjs[dateId] = dialog;
	dialog.render();
	dialog.hide();
	popupCal.render();
}*/

var submitVal = 0;

/*function handleDateSelect(type, args, obj) {
	var dates = args[0];
	var date = dates[0];
	var year = date[0],
		month = date[1],
		day = date[2];
	var popupCal = obj;
	var dialog = dialogObjs[popupCal.dateId];
	var txtDate = document.getElementById(popupCal.dateId);
	if (!txtDate.readOnly && !txtDate.disabled) txtDate.value = getFullDay(day) + "-" + getFullMonth(month - 1) + "-" + year;
	dialog.hide();
	if (submitVal == 1 && popupCal.dateId == 'name1') {
		rform.date.value = txtDate.value;
		if (rform.choosenWeekDate != null) {
			rform.choosenWeekDate.value = txtDate.value;
			rform.method.value = "getWeekView";
		} else {
			rform.method.value = "getScheduleDetails";
		}
		rform.includeResources.value = includeresources();
		mform.includeResources.value = includeresources();
		rform.submit();
	}
	if (submitVal == 0) submitVal = 1;
}*/

function onActiveCheck() {
	var patientStatus = document.getElementById('active_patient');
	if (!patientStatus.checked) {
		psAc.destroy();
		psAc = Insta.initMRNoAcSearch(cpath, "mrno", "mrnoAcDropdown", 'all',
		function (type, args) {
			getMrnoDetails();
		}, function (type, args) {
			resetFieldValues();
		},null,"Save",null,null,true);
	} else {
		psAc.destroy();
		psAc = Insta.initMRNoAcSearch(cpath, "mrno", "mrnoAcDropdown", 'active',
		function (type, args) {
			getMrnoDetails();
		}, function (type, args) {
			resetFieldValues();
		},null,"Save",null,null,true);
	}
}

var Dom = YAHOO.util.Dom;

/** tooltip is showing on every records.*/
function viewToolTip() {
	var cells = Dom.getElementsByClassName("show_tooltip");
	for (var i = 0; i < cells.length; i++) {
		var cell = cells[i];

		if (extraDetails[cell.id]) {
			var columnsData = extraDetails[cell.id];

			var dataAvailable = false;
			for (var key in columnsData) {
				if (columnsData[key] != null && columnsData[key] != '') dataAvailable = true;
			}

			if (dataAvailable) Insta.Tooltip.contextIds.push(cell.id);
		}
	}
	createTooltip();
	var switcher = document.getElementById('toolTipSwitch');
	if (switcher != null) YAHOO.util.Event.addListener(switcher, 'click', toggleTooltip);
	var ttDisabled = YAHOO.util.Cookie.exists("disableTooltip");
	enableTooltip(!ttDisabled);
}

function getCode(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ((e.which) ? e.which : e.keyCode);
	return charCode;
}

var mouserPointer = "hand";

/* changing the mousecursor to different shape while click on paste appointment.
 */
function changeMouseCursorToProgress() {
	var tdelmts = document.mainform.getElementsByTagName("td");
	for (var i = 0; i < tdelmts.length; i++) {
		if ((tdelmts[i].className).match("hand"))
			tdelmts[i].className = (tdelmts[i].className).replace("hand", "progress")
	}
	mouserPointer = "progress";
}

function changeMouseCursorToHand() {
	var tdelmts = document.mainform.getElementsByTagName("td");
	for (var i = 0; i < tdelmts.length; i++) {
		if ((tdelmts[i].className).match("progress"))
			tdelmts[i].className = (tdelmts[i].className).replace("progress", "hand")
	}
	mouserPointer = "hand";
}

function changeColor(obj, index) {
	var timeBg = document.getElementById(index);
	if (obj.className == 'availble ' + mouserPointer) {
		obj.className = 'onHoverAvailable ' + mouserPointer;
	} else if (obj.className == 'onHoverAvailable ' + mouserPointer) {
		obj.className = 'availble ' + mouserPointer;
	} else if (obj.className == 'notAvailble ' + mouserPointer) {
		obj.className = 'onHoverNotAvailable ' + mouserPointer;
	} else if (obj.className == 'onHoverNotAvailable ' + mouserPointer) {
		obj.className = 'notAvailble ' + mouserPointer;
	} else if (obj.className == 'defaultAvailable ' + mouserPointer) {
		obj.className = 'onHoverdefaultAvailable ' + mouserPointer;
	} else if (obj.className == 'onHoverdefaultAvailable ' + mouserPointer) {
		obj.className = 'defaultAvailable ' + mouserPointer;
	} else if (obj.className == 'bookedSlot ' + mouserPointer) {
		obj.className = 'onHoverBooked ' + mouserPointer;
	} else if (obj.className == 'bookedSlot ' + mouserPointer + " show_tooltip") {
		obj.className = 'onHoverBooked ' + mouserPointer;
	} else if (obj.className == 'onHoverBooked ' + mouserPointer) {
		obj.className = 'bookedSlot ' + mouserPointer;
	}
	if (timeBg.className == 'timeBgMouseOver') timeBg.className = 'timeBgMouseOut';
	else timeBg.className = 'timeBgMouseOver';
}

/* setting the cookie for reschedule appointment. */

var rescheduleAppDetails = null;
var gOpenDialog = null;
function setCookieToRescheduleAppointment(appointmentId, opendialog, colDate) {
	resForm.date.value = colDate;
	//reschdule link should be disbale when when appt center delected as default
	/*gOpenDialog = opendialog;
	var resCenterId = document.getElementById("centerId").value;
	if(category == 'DOC' && max_centers_inc_default>1 && resCenterId == 0){
		gOpenDialog = "false";
		if(gOpenDialog == 'false'){
			enableToolbarItem('Reschedule', false);
		}else enableToolbarItem('PasteAppointment', false);
	}*/



	setSchedulerCookies("reschedulepatient", appointmentId, 15);
	enableToolbarItem('Reschedule', false);
	enableToolbarItem('AddorEdit', true);
	enableToolbarItem('MarkResourceUnavailable', true);
	changeMouseCursorToProgress();
	var msg=getString("js.scheduler.doctorscheduler.appointment.rescheduling");
			msg+= "\n";
			msg+=getString("js.scheduler.doctorscheduler.targetslot.phaseappointment");
			msg+="\n";
			msg+=getString("js.scheduler.doctorscheduler.other.targetslot");
			alert(msg);
	if (opendialog == 'true') enableToolbarItem('PasteAppointment', true);
	else enableToolbarItem('PasteAppointment', false);
}

function getRescheduleAppDetails(appointmentId) {
	var reqObject = newXMLHttpRequest();
	var url = "../../pages/resourcescheduler/addeditappointments.do?method=getRescheduleAppDetails&appointment_id="+appointmentId;
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (reqObject.responseText != null)) {
			if (reqObject.responseText != 'null' && !empty(reqObject.responseText)) {
				eval("rescheduleAppDetails =" + reqObject.responseText);
			}
		}
	}
}


/** shifting the appointment from one slot to another slot while clicking paste appointment link from menu.
 */
function shiftAppointment(slotTime, schName, colDate,category,overbook_limit,isAppointmentApplicable) {
	resForm.date.value = colDate;
	var slotDateTime = getDateTime(colDate, slotTime);
	var appointmentId = getSchedulerCookies("reschedulepatient");
	resForm.appointmentId.value = appointmentId;
	resForm.includeResources.value = includeresources();
	var resCenterId = document.getElementById("centerId").value;
	resCenterId = (loggedInCenterId == 0)?resCenterId:loggedInCenterId;
	resForm.slotTime.value = slotTime;
	resForm.rescheduleResourceId.value = schName;
	if (isAppointmentApplicable == 'false') {
		showMessage("js.scheduler.doctorscheduler.slotisalreadybooked");
		return;
	}

	if(empty(rescheduleAppDetails)) {
		getRescheduleAppDetails(appointmentId);
	}
	var centerList = findInList(primaryResourceCentersList,"RESOURCE_ID",schName);
	var centerId = centerList.CENTER_ID;

	if(category == 'DOC' && max_centers_inc_default > 1) {
		if(resCenterId != rescheduleAppDetails[0].center_id && resCenterId != 0) {

			showMessage("js.scheduler.doctorscheduler.rescheduledappointment.notallowed");
			return;
		}
	}else if(category == 'DOC' && max_centers_inc_default == 1){
		if(centerId != rescheduleAppDetails[0].center_id && centerId != 0) {
			showMessage("js.scheduler.doctorscheduler.rescheduledappointment.notallowed");
			return;
		}
	}else {
		if(centerId != rescheduleAppDetails[0].center_id) {
			showMessage("js.scheduler.doctorscheduler.rescheduledappointment.notallowed");
			return;
		}
	}

	if(document.mainform.centerId)
		resForm.centerId.value = document.mainform.centerId.value;
	resForm.appointment_center.value = rescheduleAppDetails[0].center_id;

	if (rescheduleAppDetails != null && rescheduleAppDetails.length > 0) {
		var appDate = colDate;
		var appTime = slotTime;
		var appDuration = rescheduleAppDetails[0].duration;
		var appCategory = "";
		var fromSchedulerText = "";
		var toSchedulerText = "";
		var mrno = rescheduleAppDetails[0].mr_no;
		var name = rescheduleAppDetails[0].patient_name;
		var mobno = rescheduleAppDetails[0].patient_contact;
		var contactId = rescheduleAppDetails[0].contact_id;
		if(rescheduleAppDetails[0].res_sch_id == 1)
			appCategory = 'DOC';
		else if (rescheduleAppDetails[0].res_sch_id == 2)
			appCategory = 'OPE';
		else if (rescheduleAppDetails[0].res_sch_id == 3)
			appCategory = 'SNP';
		else if (rescheduleAppDetails[0].res_sch_id == 4)
			appCategory = 'DIA';

		if(appCategory == 'DOC') {
			fromSchedulerText = getString("js.scheduler.doctorscheduler.doctor.scheduler.appointment");
		} else if(appCategory == 'OPE') {
			fromSchedulerText = getString("js.scheduler.doctorscheduler.service.scheduler.appointment");
		} else if(appCategory == 'SNP') {
			fromSchedulerText = getString("js.scheduler.doctorscheduler.service.scheduler.appointment");
		} else if(appCategory == 'DIA') {
			fromSchedulerText = getString("js.scheduler.doctorscheduler.test.scheduler");
		}

		if(category == 'DOC') {
			toSchedulerText = getString("js.scheduler.doctorscheduler.doctor.scheduler");
		} else if(category == 'OPE') {
			toSchedulerText = getString("js.scheduler.doctorscheduler.surgery.scheduler");
		} else if(category == 'SNP') {
			toSchedulerText = getString("js.scheduler.doctorscheduler.service.scheduler");
		} else if(category == 'DIA') {
			toSchedulerText = getString("js.scheduler.doctorscheduler.test.scheduler");
		}

		if(category != appCategory) {
			var msg=fromSchedulerText;

			msg+=getString("js.scheduler.doctorscheduler.cannot.paste");
			msg+=toSchedulerText;

			alert(msg);
			changeMouseCursorToHand();
			return ;
		}

		var primaryResource = "";
		var resourcesList = new Array();
		var resourceTypesList = new Array();
		var primaryResourceType = null;
		var index = 0;
		var otherResource = rescheduleAppDetails[0].res_sch_name;;
		var otherResourceType = null;

		if (category == 'DOC') {
			primaryResourceType = "OPDOC";
			otherResource = null;
		} else if (category == 'SNP') {
			primaryResourceType = "SRID";
			otherResourceType = 'SER';
		} else if (category == 'DIA') {
			primaryResourceType = "EQID";
			otherResourceType = 'TST';
		} else if (category == 'OPE') {
			primaryResourceType = "THID";
			otherResourceType = 'SUR';
		}

		for (var i=0;i<rescheduleAppDetails.length;i++) {
			if (category == "DOC" && (rescheduleAppDetails[i].resource_type == "EQID" || rescheduleAppDetails[i].resource_type == "OPDOC")) {
				if(rescheduleAppDetails[i].resource_type == 'OPDOC') {
					resourcesList[index++] = schName;
					resourceTypesList[i] = rescheduleAppDetails[i].resource_type;
				} else {
					resourcesList[index++] = rescheduleAppDetails[i].resource_id;
					resourceTypesList[i] = rescheduleAppDetails[i].resource_type;
				}
			} else if (category == "DIA" && (rescheduleAppDetails[i].resource_type == "EQID" || rescheduleAppDetails[i].resource_type == "LABTECH")) {
				if(rescheduleAppDetails[i].resource_type == 'EQID') {
					resourcesList[index++] = schName;
					resourceTypesList[i] = rescheduleAppDetails[i].resource_type;
				} else {
					resourcesList[index++] = rescheduleAppDetails[i].resource_id;
					resourceTypesList[i] = rescheduleAppDetails[i].resource_type;
				}
			} else if (category == "SNP" && (rescheduleAppDetails[i].resource_type == "SRID" || rescheduleAppDetails[i].resource_type == "DOC")) {
				if(rescheduleAppDetails[i].resource_type == 'SRID') {
					resourcesList[index++] = schName;
					resourceTypesList[i] = rescheduleAppDetails[i].resource_type;
				} else {
					resourcesList[index++] = rescheduleAppDetails[i].resource_id;
					resourceTypesList[i] = rescheduleAppDetails[i].resource_type;
				}
			} else if (category == "OPE" && (rescheduleAppDetails[i].resource_type == "THID" || rescheduleAppDetails[i].resource_type == "SUDOC" ||
						rescheduleAppDetails[i].resource_type == "ANEDOC" || rescheduleAppDetails[i].resource_type == "EQID")) {
				if(rescheduleAppDetails[i].resource_type == 'THID') {
					resourcesList[index++] = schName;
					resourceTypesList[i] = rescheduleAppDetails[i].resource_type;
				} else {
					resourcesList[index++] = rescheduleAppDetails[i].resource_id;
					resourceTypesList[i] = rescheduleAppDetails[i].resource_type;
				}
			} else {
				resourcesList[i] = rescheduleAppDetails[i].resource_id;
				resourceTypesList[i] = rescheduleAppDetails[i].resource_type;
			}

		}

		if(overbook_limit != null && overbook_limit != "" && overbook_limit == 0 && !isSlotBooked(appDate,appTime,appointmentId,appDuration,appCategory,schName,primaryResourceType)) {
			return;
		}

		if (!isResourceBooked(appDate,appTime,appointmentId,appDuration,appCategory,resourcesList,resourceTypesList,"","")) {
			return ;
		}
		if (!getSlotAvailableForPatient(appDate,appTime,appDuration,appointmentId,mrno,name,mobno,contactId)){
			return ;
		}
		if (!isResourceUnavailableWithinGivenTime(appDate,appTime,appCategory,appDuration,resourcesList,resourceTypesList,"","",otherResource,otherResourceType)) {
			return ;
		}
	}

	setSchedulerCookies("reschedulepatient", '', 0);
	changeMouseCursorToProgress();
	resForm.submit();
}

function setSchedulerCookies(cookie_name, cookie_value, expiremins) {
	var expdate = new Date();
	expdate.setDate(expdate.getMinutes() + expiremins);
	document.cookie = cookie_name + "=" + escape(cookie_value) + ((expiremins == null)
							? "" : ";expires=" + expdate.toUTCString());
}

/** getting the cookie by cookie name.
 */
function getSchedulerCookies(cookie_name) {
	if (document.cookie.length > 0) {
		cookie_start = document.cookie.indexOf(cookie_name + "=");

		if (cookie_start != -1) {
			cookie_start = cookie_start + cookie_name.length + 1;
			cookie_end = document.cookie.indexOf(";", cookie_start);

			if (cookie_end == -1) cookie_end = document.cookie.length;

			return unescape(document.cookie.substring(cookie_start, cookie_end));
		}
	}
	return "";
}

function checkSchedulerCookies() {
	var patientname = getSchedulerCookies('reschedulepatient');
	if (patientname != null && patientname != "") {
		changeMouseCursorToProgress();
	}
}

/* Initialize dialogs and autocomplete */

function initDialog() {
	dialog = new YAHOO.widget.Dialog("dialog", {
		width: "647px",
		context: ["schedulerTable", "tl", "bl"],
		visible: false,
		//modal: true,
		constraintoviewport: true
	});
	var escKeyListener = new YAHOO.util.KeyListener(document, {
		keys: 27
	}, {
		fn: handleCancel,
		scope: dialog,
		correctScope: true
	});
	dialog.cfg.queueProperty("keylisteners", escKeyListener);
	dialog.render();
}

function initDoctorNonAvailabilityDialog() {
	nonavailabilitydialog = new YAHOO.widget.Dialog("nonAvailabilityDialog", {
		width: "450px",
		context: ["schedulerTable", "tl", "bl"],
		visible: false,
		modal: true,
		constraintoviewport: true
	});
	var escKeyListener = new YAHOO.util.KeyListener(document, {
		keys: 27
	}, {
		fn: onEditDoctorNonAvailabilityCancel,
		scope: nonavailabilitydialog,
		correctScope: true
	});
	nonavailabilitydialog.cfg.queueProperty("keylisteners", escKeyListener);
	nonavailabilitydialog.render();
}

var schdNames = [];
var schdIds = [];
var schCodes = [];
var schDepts = [];
var list = null;
function schdNamesIdsArray(scNames) {
	if (resNames != null && resNames.length >0) { 
		
		if (mappedRes != null) {
			list = mappedRes;
			list = filterList(list,"prim_res_id",scNames);
			if (list.length >0)
				loadResource(list);
			else { 
				list = resNames;
				loadResource(list);
			}
			
		}
		// no mapped resources
		else {
			list = resNames;
			loadResource(list);
		}
	}
		
}

function loadResource(list) {
	schdIds.length = list.length;
	for (i = 0; i < list.length; i++) {
		var item = list[i];
		schdNames[i] = item["resource_name"];
		schdIds[i] = item["resource_id"];
		schCodes[i] = item["resource_code"];
		schDepts[i] = item['resource_dept_name'];
	}
}
function scheduleNameAutoComplete(scName) {
	schdNamesIdsArray(scName);
	var resList = {
		result: list
	};
	var ds = new YAHOO.util.LocalDataSource(resList);
	ds.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	ds.responseSchema = {
		resultsList: "result",
		fields: [{
			key: "resource_name_dept_code"
		}, {
			key: "resource_name"
		}, {
			key: "resource_id"
		}, {
			key: "resource_code"
		}, {
			key: "resource_dept_name"
		}]
	};

	oAutoComp = new YAHOO.widget.AutoComplete('scNames', 'scNameDropdown', ds);


	oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp.maxResultsDisplayed = 20;
	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.typeAhead = false;
	oAutoComp.useShadow = false;
	oAutoComp.minQueryLength = 1;
	oAutoComp.forceSelection = false;
	oAutoComp.resultTypeList = false;
	oAutoComp.queryMatchContains = true;
	oAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
	oAutoComp.textboxBlurEvent.subscribe(function () {
		var sNames = document.getElementById("scNames").value;
		if (sNames != '') {
			document.getElementById("scNames").title = sNames;
		} else {
			document.getElementById("scNames").removeAttribute("title");
		}
	});
	oAutoComp.itemSelectEvent.subscribe(function () {
		var sNames = document.getElementById("scNames").value;
		if (sNames != '') {
			for (var i = 0; i < list.length; i++) {
				if (list[i]["resource_name_dept_code"] == (sNames)) {
					document.getElementById("scheduleNameForAppointment").value = list[i]["resource_id"];
					if (category == "DIA"){
						document.getElementById("conductingDoctorMandatory").value = resNames[i]["conducting_doc_mandatory"];
						if (document.getElementById("conductingDoctorMandatory").value=='O'){
							document.getElementById("cond_doc_star").style.visibility = 'visible';
							document.getElementById("conducting_doctor").removeAttribute("disabled");
						} else {
							document.getElementById("cond_doc_star").style.visibility = 'hidden';
							document.getElementById("conducting_doctor").setAttribute("disabled", true);
							document.getElementById("conducting_doctor").value = '';
							document.getElementById("cond_doc_id").value = '';
						}
						condDoctorsJson = filterList(doctorsJson,"dept_id",resNames[i]["category"]);
						initConductedDoctorAutoComplete();
					}
					break;
				}
			}
			selectedtime = rform.time.value;
			document.getElementById("scNames").title = sNames;
			isAutoCompSelected = 'selected';
			getResourceList();
			getMappedResources('add');
		} else {
			document.getElementById("scheduleNameForAppointment").value = "";
			document.getElementById("scNames").removeAttribute("title");
		}
	});
}

function getscNameFromId(id) {
	if (!empty(id)) {
		var index = schdIds.indexOf(id);
		return schdNames[index];
	} else {
		return null;
	}
}

function checkFieldValue() {
	if (!checkIfValueInResourceList(rform.scNames.value)) {
		rform.scNames.value = "";
	}
	if(rform.category.value == 'DOC') {
	} else {
		if(empty(rform.scNames.value)) {
			rform.scheduleNameForAppointment.value = "";
			isAutoCompSelected = null;
			getResourceList();
		}
	}
}

function checkIfValueInResourceList(resName) {
	var isFound = false;
	if (typeof(resNames) != 'undefined') {
		for (var i = 0; i < resNames.length; i++) {
			if (resNames[i]===resName) {
				isFound = true;
				break;
			}
		}
	}
	return isFound;
}

function changeCenterValue(obj) {
	if(max_centers_inc_default > 1) {
		getResourceList();
		var centerList = findInList(primaryResourceCentersList,"RESOURCE_ID",obj.value)
		var centerId = centerList.CENTER_ID;
		document.resourceform.center_id.value = centerId;
		document.resourceform._center_id.value = centerId;
	}
}

function initPrescribedDoctorAutoComplete() {
	var datasource = new YAHOO.util.LocalDataSource({result: doctorsJson});
	datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	datasource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "doctor_name"},{key : "doctor_id"} ]
	};

	var autoComp = new YAHOO.widget.AutoComplete('prescribing_doctor','prescribing_doctorAcDropdown', datasource);
	autoComp.typeAhead = false;
	autoComp.useShadow = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 0;
	autoComp.maxResultsDisplayed = 20;
	autoComp.autoHighlight = true;
	autoComp.forceSelection = true;
	autoComp.animVert = false;
	autoComp.useIFrame = true;
	autoComp.formatResult = Insta.autoHighlight;
	autoComp.resultTypeList = false;
	if (gPageDirection == 'rtl') {
		autoComp.autoSnapContainer = false;
	}

 	if (autoComp._elTextbox.value != '') {
		autoComp._bItemSelected = true;
		autoComp._sInitInputValue = autoComp._elTextbox.value;
	}

	autoComp.itemSelectEvent.subscribe(setPrescribingDoctorId);
	autoComp.selectionEnforceEvent.subscribe(clearPrescribingDoctorId);
	return autoComp;
}

function initConductedDoctorAutoComplete() {
	var datasource = new YAHOO.util.LocalDataSource({result: condDoctorsJson});
	datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	datasource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "doctor_name"},{key : "doctor_id"} ]
	};

	var autoComp = new YAHOO.widget.AutoComplete('conducting_doctor','conducting_doctorAcDropdown', datasource);
	autoComp.typeAhead = false;
	autoComp.useShadow = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 0;
	autoComp.maxResultsDisplayed = 20;
	autoComp.autoHighlight = true;
	autoComp.forceSelection = true;
	autoComp.animVert = false;
	autoComp.useIFrame = true;
	autoComp.formatResult = Insta.autoHighlight;
	autoComp.resultTypeList = false;
	if (gPageDirection == 'rtl') {
		autoComp.autoSnapContainer = false;
	}

 	if (autoComp._elTextbox.value != '') {
		autoComp._bItemSelected = true;
		autoComp._sInitInputValue = autoComp._elTextbox.value;
	}

	autoComp.itemSelectEvent.subscribe(setConductingDoctorId);
	autoComp.selectionEnforceEvent.subscribe(clearConductingDoctorId);
	return autoComp;
}

function setPrescribingDoctorId(oSelf, sArgs) {
	document.getElementById("presc_doc_id").value = sArgs[2].doctor_id;
}

function setConductingDoctorId(oSelf, sArgs) {
	document.getElementById("cond_doc_id").value = sArgs[2].doctor_id;
}

function clearPrescribingDoctorId(oSelf, sClearedValue) {
	document.getElementById("presc_doc_id").value = '';
}

function clearConductingDoctorId(oSelf, sClearedValue) {
	document.getElementById("cond_doc_id").value = '';
}

function  validateOnlyOnePrescDocForOP() {

	var activeVisit = document.getElementById('app_visit_id')
					.options[document.getElementById('app_visit_id').selectedIndex].value;
	var prescDoctor = null;
	var selectedPrescDocID = document.getElementById('presc_doc_id').value;
	if (!empty(activeVisit) && activeVisit != 'None' && category != 'DOC' && gOnePrescDocForOP == 'Y') {
		var url = cpath+'/pages/resourcescheduler/snpappointments.do?method=getPrescribedDoctors';
		url += '&visitId='+activeVisit;
		var ajaxObj = newXMLHttpRequest();
		ajaxObj.open("GET", url.toString(), false);
		ajaxObj.send(null);
		if (ajaxObj.readyState == 4 && ajaxObj.status == 200) {
			eval("var response =" +ajaxObj.responseText);
			var patient = response.patient;
			var prescDoctorsList = response.prescDocList;
			if (patient.visit_type == 'o') {
				if (!empty(patient.doctor))
					prescDoctor = patient.doctor;
					for (var i=0; i<prescDoctorsList.length; i++) {
						if (empty(prescDoctor))
							prescDoctor = prescDoctorsList[i].prescribing_dr_id;
						else
							break;
					}
					if (!empty(prescDoctor) && !empty(selectedPrescDocID) && prescDoctor != selectedPrescDocID) {
						alert(getString('js.scheduler.doctorscheduler.one.prescribingdoctor.required'));
						return false;
					}

					return true;
			}
			return true;
		}
	}
	return true;
}

//function  validateOnlyOneCondDocForOP() {
//
//	var activeVisit = document.getElementById('app_visit_id')
//					.options[document.getElementById('app_visit_id').selectedIndex].value;
//	var condDoctor = null;
//	var selectedCondDocID = document.getElementById('cond_doc_id').value;
//	if (!empty(activeVisit) && activeVisit != 'None' && category != 'DOC' && gOneCondDocForOP == 'Y') {
//		var url = cpath+'/pages/resourcescheduler/snpappointments.do?method=getConductedDoctors';
//		url += '&visitId='+activeVisit;
//		var ajaxObj = newXMLHttpRequest();
//		ajaxObj.open("GET", url.toString(), false);
//		ajaxObj.send(null);
//		if (ajaxObj.readyState == 4 && ajaxObj.status == 200) {
//			eval("var response =" +ajaxObj.responseText);
//			var patient = response.patient;
//			var condDoctorsList = response.condDocList;
//			if (patient.visit_type == 'o') {
//				if (!empty(patient.doctor))
//					condDoctor = patient.doctor;
//					for (var i=0; i<condDoctorsList.length; i++) {
//						if (empty(condDoctor))
//							condDoctor = condDoctorsList[i].conducting_dr_id;
//						else
//							break;
//					}
//					if (!empty(condDoctor) && !empty(selectedCondDocID) && condDoctor != selectedCondDocID) {
//						alert(getString('js.scheduler.doctorscheduler.one.conductingdoctor.required'));
//						return false;
//					}
//
//					return true;
//			}
//			return true;
//		}
//	}
//	return true;
//}

//Below code is wriiten for resource avilability dialog box
function initDoctorAvailabilityDialog() {
	availabilitydialog = new YAHOO.widget.Dialog("availability_dialog", {
		width: "450px",
		context: ["schedulerTable", "tl", "bl"],
		visible: false,
		modal: true,
		constraintoviewport: true
	});
	var escKeyListener = new YAHOO.util.KeyListener(document, {
		keys: 27
	}, {
		fn: onEditDoctorAvailabilityCancel,
		scope: availabilitydialog,
		correctScope: true
	});
	availabilitydialog.cfg.queueProperty("keylisteners", escKeyListener);
	availabilitydialog.render();
}

function showResourceAvailabilityDialog(id, slotClass, schName, rowTime, index, colDate, resourceUnAvailCenterId) {
	prevClass = document.getElementById('toolbarRow' + id).className;
	document.getElementById('toolbarRow' + id).className = 'rowbgToolBar';
	gMarkAvlNonAvl = "markAvailable";
	gResNotAvailCenterId = resourceUnAvailCenterId;
	avlform.date.value = colDate;
	var ahref = document.getElementById("toolbarRow" + id);
	availabilitydialog.cfg.setProperty("context", [ahref, "tl", "bl"], false);
	document.getElementById("availabilityDialogId").value = id;
	var dt = avlform.date.value;
	var sel = document.getElementById("resourceSchedule" + index);
	var doctorName = sel.options[sel.options.selectedIndex].text;

	avlform.availableDoctor.value = schName;
	document.getElementById("availableDoctorLbl").textContent = doctorName;
	document.getElementById("availableDateLbl").textContent = dt;
	resourceId = schName;
	avlform.avFirstSlotFromTime.value = rowTime;
	avlform.avFirstSlotToTime = '';

	var tdObj = document.getElementById("toolbarRow" + id);
	YAHOO.lutsr.accordion.collapse(document.getElementById("recurranceDD"));
	slotobj = tdObj;
	oldClass = slotClass + ' ' + mouserPointer;
	tdObj.className = tdObj.className + ' ' + mouserPointer;

	populateCentersAvailability(resourceId);
	availabilitydialog.show();



}

function populateCentersAvailability(obj) {
	var resources;
	if(!empty(obj)) {
		centerAjaxCall(obj);

	}
}

function centerAjaxCall(obj) {
	var doctorId = obj;
	var url = cpath +'/pages/resourcescheduler/docappointments.do?method=getResourseCenter&doctor_id='+doctorId;
		var ajaxObj = newXMLHttpRequest();
		ajaxObj.open("GET", url.toString(), false);
		ajaxObj.send(null);
		if (ajaxObj.readyState == 4 && ajaxObj.status == 200) {
			eval("var response =" +ajaxObj.responseText);
			var resourseCenterList = response.resourseCenterList;
			var loggedCenName = document.getElementById('login_center_name').value;
			//var cenId = document.getElementById('dialog_center').value;
			var innerhtml="";
			if(resourseCenterList.length > 1){
				innerhtml= "<option value=''>--Select--</option>";
			}
			for (var i=0; i<resourseCenterList.length; i++) {
				if(loggedInCenterId == 0){
					if(resourseCenterList[i].center_id == 0){

						document.getElementById("listAllCenter").style.display = 'block';
						document.getElementById("listBelongcenter").style.display ='none';
					}else{
						document.getElementById("listAllCenter").style.display = 'none';
						document.getElementById("listBelongcenter").style.display = 'block';
						var option = new Option(resourseCenterList[i].center_name,resourseCenterList[i].center_id);
						document.getElementById("dialog_center").options[document.getElementById("dialog_center").length] = option;
					}
				}else{
					if(loggedInCenterId == resourseCenterList[i].center_id) {
						var option = new Option(resourseCenterList[i].center_name,resourseCenterList[i].center_id);
						document.getElementById('dialog_center_id').options[document.getElementById('dialog_center_id').length] = option;

						document.getElementById('dialog_label').innerHTML =document.getElementById('dialog_center_id').options
						[document.getElementById('dialog_center_id').selectedIndex].text;

						document.getElementById('dialog_center_id').style.display = 'none';
						document.getElementById('dialog_label').style.display = 'block';

					}else{
						document.getElementById('dialog_center_id').style.display = 'none';
						document.getElementById('dialog_label').style.display = 'block';
						document.getElementById('dialog_label').innerHTML = loggedCenName;

					}
					document.getElementById('dialog_center_hid').value = loggedInCenterId;
				}
			}
		}
}

function fillHidenCenterName(evt){
	document.getElementById("_dialog_center_hid").value = evt.value;
}

function onEditDoctorAvailabilityCancel() {
	slotobj.className = oldClass;
	document.getElementById('avFirstSlotToTime').value = '';
	if(loggedInCenterId == 0){
		document.getElementById("_dialog_center_hid").value='';
		document.getElementById('dialog_center').options.length = 1;
		document.getElementById('_dialog_center').value='';
	}else {
		document.getElementById("dialog_center_id").options.length = 0;
		document.getElementById("dialog_center_hid").value='';
	}


	availabilitydialog.cancel();
}

function onEditDoctorAvailabilitySubmit() {
	avlform.includeResources.value = includeresources();
	resForm.includeResources.value = includeresources();

	if (!validateDoctorAvailabilityDialog()) return false;
	document.getElementById('availOk').disabled = true;
	avlform.submit();
}

function validateDoctorAvailabilityDialog() {

	var amsttime = document.getElementById("avFirstSlotFromTime").value.split(":");
	var amendtime = document.getElementById("avFirstSlotToTime").value.split(":");

	if(empty(document.getElementById("avFirstSlotFromTime").value)) {
		showMessage("js.scheduler.doctorscheduler.fromtime.required");
		document.getElementById("avFirstSlotFromTime").focus();
		return false;
	}

	if(empty(document.getElementById("avFirstSlotToTime").value)) {
		showMessage("js.scheduler.doctorscheduler.totime.required");
		document.getElementById("avFirstSlotToTime").focus();
		return false;
	}

	if (!empty(document.getElementById("avFirstSlotFromTime").value) && !empty(document.getElementById("avFirstSlotToTime").value)) {
		if (!validateTime(document.getElementById("avFirstSlotFromTime"))) return false;
		if (!validateTime(document.getElementById("avFirstSlotToTime"))) return false;
		if (!compareTimes(document.getElementById("avFirstSlotToTime"), amsttime, amendtime)) return false;
	}
	//validation check for center
	if(loggedInCenterId == 0){
		if(empty(document.getElementById("_dialog_center_hid").value)) {
			if(document.getElementById("listAllCenter").style.display =='none'){
				showMessage("js.scheduler.doctorscheduler.select.center");
				document.getElementById("dialog_center").focus();
				return false;
			}else{
				showMessage("js.scheduler.doctorscheduler.select.center");
				document.getElementById("_dialog_center").focus();
				return false;
			}
		}
	}

	if (!checkTimeOverlapping(amsttime,amendtime)) {
		showMessage("js.scheduler.doctorscheduler.timeslot.overlapping");
		document.getElementById("avFirstSlotFromTime").focus();
		return false;
	}
	return true;
}





