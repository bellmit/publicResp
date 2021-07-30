var toolbar = {};
	toolbar.Arrived= {
		title: toolbarOptions["arrived"]["name"],
		imageSrc: "icons/Edit.png",
		href: '#',
		onclick: null,
		description: toolbarOptions["arrived"]["description"]
	};

	toolbar.AddOrEdit= {
		title: toolbarOptions["resources"]["name"],
		imageSrc: "icons/Edit.png",
		href: '#',
		onclick: null,
		description: toolbarOptions["resources"]["description"]
	};

	toolbar.MrnoUpdate= {
		title: toolbarOptions["updatemrno"]["name"],
		imageSrc: "icons/Edit.png",
		href: '#',
		onclick: null,
		description: toolbarOptions["updatemrno"]["description"]
	};
	toolbar.AddDentalConsulation= {
			title: toolbarOptions["showDentalConsulation"]["name"],
			imageSrc: "icons/Edit.png",
			onclick: null,
			href: "DentalConsultation/Consultation.do?_method=show" ,
			description: toolbarOptions["showDentalConsulation"]["description"],
			show: (modDentalModule == 'Y')
	};
	toolbar.AuditLog= {
			title: toolbarOptions["auditlog"]["name"],
			imageSrc: "icons/Edit.png",
			href: 'schedulerAppointments/auditlog/AuditLogSearch.do?_method=getAuditLogDetails',
			onclick:null,
			description: toolbarOptions["auditlog"]["description"]
			
		};
	toolbar.Print= {
			title: toolbarOptions["printappointment"]["name"],
			imageSrc: "icons/Edit.png",
			href: 'pages/resourcescheduler/todaysappointments.do?_method=printAppointments',
			onclick:null,
			description: toolbarOptions["printappointment"]["description"],
			show:true
		};


function clearSearchParameters(form) {
	clearForm(form);
	disableNamesAndResetHiddenValues();
}

var setHrefs = function (params, id, enableList, toolbarKey) {
		if (empty(toolbarKey)) toolbarKey = defaultKey;
		var i = 0;
		document.getElementById('divToolBar' + toolbarKey).onmouseout = function () {
			if (document.getElementById('divToolBar' + toolbarKey).style.display = 'none'
					&& document.getElementById('toolbarRow' + id).className != ''
					&& (document.getElementById('mrnoDialog_c').style.visibility == 'visible'
						|| document.getElementById('resourceDialog_c').style.visibility == 'visible')) {
				document.getElementById('toolbarRow' + id).className = 'rowbgToolBar';
				document.getElementById('divToolBar' + toolbarKey).style.display = 'none';
			} else {
				document.getElementById('toolbarRow' + id).className = '';
				document.getElementById('divToolBar' + toolbarKey).style.display = 'none';
			}
		}

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

				if (key == 'MrnoUpdate') {
					anchor.href = 'javascript:showMrnoSearchDialog('
									+ params['appointment_id'] + ',"' + params['category'] + '","'
									+ params['startTime']+ '","' + params['time'] + '","'
									+ params['appointment_duration'] + '","'
									+ params['appointment_patientname'] + '","'
									+ params['appointment_patientcontact'] + '","'
									+ params['appointment_res_sch_id'] + '","'
									+ params['appointment_res_sch_name'] + '","'
									+ params['appointment_booked_resource_id'] + '","'
									+ params['appointment_booked_resource'] + '","'
									+ params['appointment_consultation'] + '","'
									+ params['bedname']+ '","' + params['wardname'] + '","'
									+ params['complaint'] + '","' + params['presc_doctor'] + '","'
									+ params['appointment_mrno'] + '",' + id + ',"Not Arrived")';

				} else if (key == 'Arrived') {
					anchor.href = 'javascript:checkMultipleVisitAndRegisterIfArrived('
									+ params['appointment_id'] + ',"' + params['category'] + '","'
									+ params['startTime']+ '","' + params['time'] + '","'
									+ params['appointment_duration'] + '","'
									+ params['appointment_patientname'] + '","'
									+ params['appointment_patientcontact'] + '","'
									+ params['appointment_res_sch_id']+'","'
									+ params['appointment_res_sch_name'] + '","'
									+ params['appointment_booked_resource_id'] + '","'
									+ params['appointment_booked_resource'] + '","'
									+ params['appointment_consultation'] + '","'
									+ params['bedname']+ '","' + params['wardname'] + '","'
									+ params['complaint'] + '","' + params['presc_doctor'] + '","'
									+ params['appointment_status'] + '","'
									+ params['appointment_mrno'] + '",' + id + ',"Arrived")';

				} else if (key == 'AddOrEdit') {
					anchor.href = 'javascript:addOrEditResources(' + params['appointment_id']
									+ ',"' + params['category'] + '", "' + params['appt_center_id'] + '","' + params['appt_center_name'] + '",' + id + ')';
				
				}else if(key == 'AddDentalConsulation') {
					anchor.href = cpath + "/" + href;
				
				}else if(key == 'AuditLog'){
					
					href= href + '&appointment_id=' + params['appointment_id'];
					anchor.href = cpath +"/"+ href + '&al_table=scheduler_appointments_audit_log'+'&appointment_id@type=integer';
					
				}else if(key == 'Print'){
					var searchHref = 'pages/resourcescheduler/appointments.do?_method=printAppointments';
					
					var href = screenId != 'today_resource_scheduler'? searchHref : href ;
					anchor.href= cpath +"/"+ href +'&appointment_id=' + params['appointment_id']+'&category='+ params['category'];
						
				}else {
					anchor.href = cpath + "/" + href;
				}
				if (enableList) {
					if (key == 'Arrived') {
						if (!empty(params['appointment_package_id'])) {
							enableToolbarItem(key, false);
						} else {
							enableToolbarItem(key, enableList[i]);
						}
					} else if (key == 'AddOrEdit') {
						if (!empty(params['appointment_package_id'])) {
							enableToolbarItem(key, false);
						} else {
							enableToolbarItem(key, enableList[i]);
						}
					} else if (key == 'MrnoUpdate'){
						if (!empty(params['appointment_package_id'])) {
							enableToolbarItem(key, false);
						} else {
							enableToolbarItem(key, enableList[i]);
						}
					} else if (key == 'AddDentalConsulation') {
						if (!empty(params['appointment_package_id'])) {
							enableToolbarItem(key, false);
						} else {
							enableToolbarItem(key, enableList[i]);
						}
					} else {
						enableToolbarItem(key, enableList[i]);
					}
				} else {
					enableToolbarItem(key, true);
				}
			} else {
				debug("No anchor for " + 'toolbarAction' + key + ":");
			}

			i++;
		}
		document.getElementById('toolbarTitleDiv_defaultPrint').className ='actionTitle enabled';
	}

var psAc = null;
var psAc1 = null;

var default_gp_first_consultation = null,
	default_gp_revisit_consultation = null,
	default_sp_first_consultation = null,
	default_sp_first_consultation = null;
	var gPreviousDocVisits = null;
	var gFollowUpDocVisits = null;
	var gPatientLastIpVisit = null;

function initScheduler() {
	createToolbar(toolbar);
	//alert(toolbar);
	default_gp_first_consultation	= regPref.default_gp_first_consultation;
	default_gp_revisit_consultation	= regPref.default_gp_revisit_consultation;
	default_sp_first_consultation	= regPref.default_sp_first_consultation;
	default_sp_revisit_consultation	= regPref.default_sp_revisit_consultation;
	initResourceDialog();
	initMrnoDialog();
	psAc = Insta.initMRNoAcSearch(cpath, "_searchmrno", "mrnoAcDropdown", "active",
	function (type, args) {
		getPatientDetails();
	}, function (type, args) {
		clearDetails();
	});

	psAc1 = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'active', null, null);
	document.getElementById('_mr_no').checked = true;
	doctorsAutocomp();
	servicesAutocomp();
	testsAutocomp();
	surgeriesAutocomp();
	enableNames();
}
var resourceCategory = null;
var gApptCenterId = '';
function addOrEditResources(appointment_id, category,apptCenterId,apptCenterName,id) {
	gApptCenterId = apptCenterId;
	clearDetails();
	resourceCategory = category;
	var button = document.getElementById("toolbarRow" + id);
	resourceDialog.cfg.setProperty("context", [button, "tr", "br"], false);
	document.getElementById("resourceDialogId").value = id;
	appointmentCenter = apptCenterId;
	if(max_centers_inc_default > 1) {
		document.getElementById('appt_center').textContent = apptCenterName;
	}
	getAppointmentResources(appointment_id, category,apptCenterId);
	resourceDialog.show();
}

function initResourceDialog() {
	resourceDialog = new YAHOO.widget.Dialog("resourceDialog", {
		width: "400px",
		context: ["apptTable", "tr", "br"],
		visible: false,
		modal: true,
		constraintoviewport: true,
		buttons: [{
			text: "OK",
			handler: handleResourceSubmit,
			isDefault: true
		}, {
			text: "Cancel",
			handler: handleResourceCancel
		}]
	});
	resourceDialog.render();
}

function handleResourceSubmit() {
	var dialogId = document.getElementById("resourceDialogId").value;
	document.getElementById('toolbarRow' + dialogId).className = '';
	resourceDialog.hide();
	if(!validationResources(dialogId)) {
		return false;
	}
	if (!updateAppointmentResourcesAndStatus()) {
		return;
	}
	document.resourcesForm._method.value = "saveAppointmentDetails";
	document.resourcesForm.submit();
}

function handleResourceCancel() {
	var id = document.getElementById("resourceDialogId").value;
	document.getElementById('toolbarRow' + id).className = '';
	this.cancel();
}

function validationResources(dialogId) {
	var resourceIdsArr = new Array();
	var resourceTypesArr = new Array();
	var obj = appointmentDetails.appntDetailsList;
	var appointmentId = null;
	var startTime = null;
	var endTime = null;
	var duration = null;
	var category = null;
	var appointmentDate = null;
	var rtab = document.getElementById("resourceTable");
	var rlen = rtab.rows.length;
	var id = rlen-1;

	if(obj != null && obj.length >0) {
		var resource = obj[0];
	 	appointmentId = resource['appointment_id'];
	 	startTime = resource['text_appointment_date_time'];
	 	endTime = resource['text_end_appointment_time'];
	 	duration = resource['duration'];
	 	appointmentDate = resource["text_appointment_date"];
	 	category = resourceCategory;
	}

	for(var i=0;i<id;i++) {
		var res_type = document.getElementById('type'+i).value;
		var res_id = document.getElementById('resource'+i).value;
		var resourceDelete = document.getElementById('resourceDelete'+i).value;
		var primaryResource = document.getElementById('priresource'+i).value;

		if((!empty(resourceDelete) && resourceDelete != 'Y')) {
			resourceIdsArr[i] = res_id;
			resourceTypesArr[i] = res_type;
		}
	}

	if(!isResourceBooked(startTime,endTime,appointmentId,duration,category,resourceIdsArr,resourceTypesArr)) {
		return false;
	}

	if (!isResourceUnavailableWithinGivenTime(startTime,endTime,category,resourceIdsArr,resourceTypesArr,appointmentDate)) {
		return false;
	}

	return true;
}
function isResourceUnavailableWithinGivenTime(startTime,endTime,category,resources,resourceTypes,date){
	var schedulableCenterId = gApptCenterId;
	schedulableCenterId = !empty(schedulableCenterId) ? schedulableCenterId :0;
	var reqObject = newXMLHttpRequest();
	var url = "../../pages/resourcescheduler/addeditappointments.do?method=isResourceUnavailableWithinGivenTime"
		 +"&resource_ids="+resources+"&colDate="+date+"&startTime="+startTime+"&endTime="+endTime+"&category="+category+"&resourceTypes="+resourceTypes+"&schedulableCenterId="+ schedulableCenterId;;
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (reqObject.responseText != null)) {
			if (reqObject.responseText != 'null' && !empty(reqObject.responseText)) {
				var msg=reqObject.responseText;
				msg+=getString("js.scheduler.todaysappointment.notavailable");
				alert(msg);
				return false;
			}
		}
	}
	return true;
}

function isResourceBooked(startTime,endTime,appointmentId,duration,category,resourceIds,resourceTypes) {
	var reqObject = newXMLHttpRequest();
	if (resourceIds == null || resourceIds == '') {
	  alert("Appointment Cannot be booked without Primary Resource");
  	return false;
	}
	var url = cpath+"/pages/resourcescheduler/addeditappointments.do?method=isResourceBooked";
		url +='&startTime='+encodeURIComponent(startTime);
		url +='&endTime='+encodeURIComponent(endTime);
		url +='&appointment_id='+encodeURIComponent(appointmentId);
		url +='&duration='+encodeURIComponent(duration);
		url +='&category='+encodeURIComponent(category);
		url +='&resource_ids='+encodeURIComponent(resourceIds);
		url +='&resource_types='+encodeURIComponent(resourceTypes);
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (reqObject.responseText != null)) {
			if (reqObject.responseText != 'null' && !empty(reqObject.responseText)) {
				var msg=reqObject.responseText;
				msg+=getString("js.scheduler.todaysappointment.alreadybooked");
				alert(msg);
				return false;
			}
		}
	}
	return true;
}

var resourceTypeFilteredJSON = null;
var appointmentDetails = null;
var appointmentCenter = null;

function getAppointmentResources(apptnumber, category,apptCenterId) {
	clearDetails();
	document.resourcesForm.appointment_id.value = apptnumber;
	document.resourcesForm._category.value = category;
	if (resourceTypeJSON != null) resourceTypeFilteredJSON = filterList(resourceTypeJSON, "CATEGORY", category);

	var reqObject = newXMLHttpRequest();
	var url = "../../pages/resourcescheduler/addeditappointments.do?method=getAppointmentDetails&appointmentId=" + apptnumber;
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (reqObject.responseText != null)) {
			eval("var list =" + reqObject.responseText);
			appointmentDetails = list;
			var obj = list.appntDetailsList;
			if (obj != null && obj != '') {
				var item = obj[0];
				document.resourcesForm.appointment_status.value = item["appointment_status"];
				deleteRows();
				for (var i = 0; i < obj.length; i++) {
					var resource = obj[i];
					addResourceRow();

					var typeId = "type" + i;
					var resourceId = "resource" + i;
					var appointment_item_id = "appointment_item_id" + i;
					var priresource = "priresource" + i;

					var resourceTypeObj = document.getElementById(typeId);
					var resourceValueObj = document.getElementById(resourceId);
					var item_id = document.getElementById(appointment_item_id);
					var priresource = document.getElementById(priresource);

					setSelectedIndex(resourceTypeObj, resource["resource_type"]);
					selectResource(i, resource["resource_id"], resource["res_sch_name"]);
					item_id.value = resource["appointment_item_id"];
					priresource.value = resource["primary_resource"];
					if(priresource.value == 'true') {
						document.getElementById('type'+i).disabled = true;
					}
					if (resource["primary_resource"] == true) {
						var imageSrc = cpath + "/icons/Delete1.png";
						var imgDeleteCheckId = "resourceCheckBox" + i;
						var resourceDeleteId = "resourceDelete" + i;

						document.getElementById("row" + i).getElementsByTagName("TD")[2].innerHTML =
								'<a href="javascript:void(0)"> '
								+ '<img src="' + imageSrc + '" name="' + imgDeleteCheckId
								+ '" class="imgDelete" id="' + imgDeleteCheckId + '" /></a>'
								+ '<input type="hidden" name="_rDelete" id="' + resourceDeleteId
								+ '" value="N"/>';
					}
					enableButton(i);
				}
				return false;
			} else {
				return true;
			}
		}
	}
}

function selectResource(index, resourceid, resSchName) {
	var resourcetypeid = "type" + index;
	var resourcevalueid = "resource" + index;
	var resourceType = document.getElementById(resourcetypeid).value;
	var resourceValue = document.getElementById(resourcevalueid);
	var list = null;
	var centerId = appointmentCenter;
	if(!empty(resourceType)) {
		if ((resourceType == 'SUDOC' || resourceType == 'ASUDOC' || resourceType == 'PAEDDOC') && surgeonsJSON != null) {
			list = filterList(surgeonsJSON,"SCHEDULE","T");
			if(max_centers_inc_default > 1) {
				if(centerId != '0')
					list = filterListWithValues(list,"CENTER_ID",new Array(0,centerId));
			}
			loadSelectBox(resourceValue, list, "DOCTOR_NAME", "DOCTOR_ID", "Select");
		} else if (resourceType == 'ANEDOC' && anesthestistsJSON != null) {
			list = filterList(anesthestistsJSON,"SCHEDULE","T");
			if(max_centers_inc_default > 1) {
				if(centerId != '0')
					list = filterListWithValues(list,"CENTER_ID",new Array(0,centerId));
			}
			loadSelectBox(resourceValue, list, "DOCTOR_NAME", "DOCTOR_ID", "Select");
		}

		else if (resourceType == 'THID' && theatresJSON != null) {
			if (mappedTheatresJson != null) {
				list = mappedTheatresJson;
				list = filterList(list, "operation_id", resSchName);
				if (list.length > 0) {
					if (max_centers_inc_default > 1) {
						if (centerId != '0')
							list = filterList(list, "center_id", centerId);
					}
					loadSelectBox(resourceValue, list, "theatre_name",
          								"theatre_id", "Select");
				}
			}
			if (list == null || list.length <= 0) {
				list = theatresJSON;
				if (max_centers_inc_default > 1) {
					if (centerId != '0')
						list = filterList(list, "CENTER_ID", centerId);
				}
				loadSelectBox(resourceValue, list, "THEATRE_NAME",
						"THEATRE_ID", "Select");
			}
		}
			else if (resourceType == 'EQID' && equipmentsJSON != null) {
			if (mappedEquipmentResourcesJson != null) {
				list = mappedEquipmentResourcesJson;
				list = filterList(list,"test_id",resSchName);
				if(max_centers_inc_default > 1) {
					if(centerId != '0')
						list = filterList(list,"center_id",centerId);
				}
				loadSelectBox(resourceValue, list, "equipment_name", "eq_id", "Select");
			} else {
				list = equipmentsJSON;
				if(max_centers_inc_default > 1) {
					if(centerId != '0')
						list = filterList(list,"CENTER_ID",centerId);
				}
				loadSelectBox(resourceValue, list, "EQUIPMENT_NAME", "EQ_ID", "Select");
			}
		} else if ((resourceType == 'OPDOC' || resourceType == 'DOC') && scheduleDoctorJSON != null) {
			list = scheduleDoctorJSON;
			if(max_centers_inc_default > 1) {
				if(centerId != '0') {
					list = filterListWithValues(list,"CENTER_ID",new Array(0,centerId));
				}
			}
			loadSelectBox(resourceValue, list, "DOCTOR_NAME", "DOCTOR_ID", "Select");
		} else if (resourceType == 'LABTECH' && labTechniciansJSON != null) {
			list = labTechniciansJSON;
			if(max_centers_inc_default > 1) {
				if(centerId != '0')
					list = filterListWithValues(list,"CENTER_ID",new Array(0,centerId));
			}
			loadSelectBox(resourceValue, list, "DOCTOR_NAME", "DOCTOR_ID", "Select");
		} else if (resourceType == 'SRID' && serviceResourcesListJson != null) {
			if (mappedServiceResourcesJson != null) {
				list = mappedServiceResourcesJson;
				list = filterList(list,"service_id",resSchName);
			} else {
				list = serviceResourcesListJson;				
			}
			if(max_centers_inc_default > 1) {
				if(centerId != '0')
					list = filterList(list,"center_id",centerId);
			}
			loadSelectBox(resourceValue, list, "serv_resource_name", "serv_res_id", "Select");
		} else {
			list = genericResourceListJson
			if(!empty(list))
				list = filterList(genericResourceListJson,"SCHEDULEABLE","T");
			if(!empty(list)) {
				if(max_centers_inc_default > 1) {
					if(centerId != '0') {
						list = filterList(list,"CENTER_ID",centerId);
					}
				}
			}
			if(!empty(list))
				list = filterList(list,"SCHEDULER_RESOURCE_TYPE",resourceType);
			if(!empty(list))
				loadSelectBox(resourceValue, list, "GENERIC_RESOURCE_NAME", "GENERIC_RESOURCE_ID","Select");
		}
	}
	setSelectedIndex(resourceValue, resourceid);
}


function addResourceRow() {
	var rtab = document.getElementById("resourceTable");
	var rlen = rtab.rows.length;
	var id = rlen - 1;
	var newRow = "",
		typeTd = "",
		resourceTd = "",
		rcheckbox = "";

	var typeId = "type" + id;
	var resourceId = "resource" + id;
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
	typeTd.innerHTML = '<select name="_rType" class="dropdown" style="width: 150;" id="'
						+ typeId + '" onchange="selectResource(' + id + ')"></select>';
	resourceTd = newRow.insertCell(1);
	resourceTd.innerHTML = '<select name="_rValue" class="dropdown" style="width: 150;" id="'
						+ resourceId + '" onchange="checkDuplicates(' + id + ');enableButton(' + id + ');"></select> '
						+ '<input type="hidden" name="_item_id" id="' + appointment_item_id + '" value=""/> '
						+ '<input type="hidden" name="_priresource" id="' + priresource + '" value=""/>';
	loadSelectBox(document.getElementById(typeId), resourceTypeFilteredJSON,
											"RESOURCE_DESCRIPTION", "RESOURCE_TYPE", "Select");
	rdeleteImg = newRow.insertCell(2);
	rdeleteImg.setAttribute('class', 'last');
	rdeleteImg.innerHTML = '<a href="javascript:void(0)" onclick="changeElsColor(' + id + ');"> '
						+ '<img src="' + imageSrc + '" name="' + imgDeleteCheckId + '" class="imgDelete" id="'
						+ imgDeleteCheckId + '" /></a>' + '<input type="hidden" name="_rDelete" id="'
						+ resourceDeleteId + '" value="N"/>';
	enableButton(id);
}

function deleteRows() {
	var table = document.getElementById("resourceTable");
	var rowCount = table.rows.length;
	for (var i = 1; i < rowCount; i++) {
		table.deleteRow(-1);
	}
}

function changeElsColor(index) {
	var markRowForDelete = document.getElementById('resourceDelete' + index).value =
					document.getElementById('resourceDelete' + index).value == 'N' ? 'Y' : 'N';

	if (markRowForDelete == 'Y') {
		addClassName('type' + index, "delete");
		addClassName('resource' + index, "delete");
	} else {
		removeClassName('type' + index, "delete");
		removeClassName('resource' + index, "delete");
	}
}

function validate() {
	var app_id = document.resourcesForm.appointment_id.value;
	if (app_id == '') {
		showMessage("js.scheduler.todaysappointment.select.appointment");
		return false;
	}
	return true;
}

function updateAppointmentResourcesAndStatus() {
	if (!validate()) {
		return false;
	}
	var tablen = document.getElementById("resourceTable").rows.length;
	if (tablen > 1) {
		for (var j = 0; j < (tablen - 1); j++) {
			var resourceType = "type" + j;
			var resourceValue = "resource" + j;
			var resourceCheck = "resourceCheckBox" + j;
			var resourceDelete = "resourceDelete" + j;
			var appointmentItemId = "appointment_item_id" + j;

			if (document.getElementById(resourceType).value != ""
					&& document.getElementById(resourceValue).value != "") {

				var type = document.getElementById(resourceType).value;
				var resource = document.getElementById(resourceValue).value;
				var check = document.getElementById(resourceCheck).value;
				var appItemId = document.getElementById(appointmentItemId).value;
				var del = document.getElementById(resourceDelete).value;

				var innerResourceTab = document.getElementById("InnerResourceTable");
				var trObj = "",
					tdObj = "";
				trObj = innerResourceTab.insertRow(-1);

				tdObj = trObj.insertCell(0);
				tdObj.innerHTML = '<input type="hidden" name="_resourceCheck" id="' + resourceCheck
								+ '" value="' + check + '">';

				tdObj = trObj.insertCell(1);
				tdObj.innerHTML = '<input type="hidden" name="_resourceDelete" id="' + resourceDelete
								+ '" value="' + del + '">';

				tdObj = trObj.insertCell(2);
				tdObj.innerHTML = '<input type="hidden" name="_appointmentItemId" id="' + appointmentItemId
								+ '" value="' + appItemId + '">';

				tdObj = trObj.insertCell(3);
				tdObj.innerHTML = '<input type="hidden" name="_resourceType" id="' + resourceType
								+ '" value="' + type + '">';

				tdObj = trObj.insertCell(4);
				tdObj.innerHTML = '<input type="hidden" name="_resourceValue" id="' + resourceValue
								+ '" value="' + resource + '">';
			} else {}
		}
	}
	return true;
}

function checkDuplicates(index) {
	var rtab = document.getElementById("resourceTable");
	var rlen = rtab.rows.length;
	var resourceType = document.getElementById("type" + index).value;
	var resourceId = document.getElementById("resource" + index).value;
	for (var k = 0; k < (rlen - 1); k++) {
		if ((k != index) && (resourceType == document.getElementById("type" + k).value)) {
			if ((document.getElementById("priresource" + k).value == "true")
						|| (resourceId == document.getElementById("resource" + k).value)) {
				showMessage("js.scheduler.todaysappointment.duplicate.entry");
				document.getElementById("resource" + index).selectedIndex = 0;
				return false;
			} else {}
		}
	}
}

function enableButton(index) {
	var resourceType = document.getElementById("type" + index).value;
	var resourceId = document.getElementById("resource" + index).value;
	if (resourceType != "" && resourceId != "") {
		document.resourcesForm.addresource.disabled = false;
	} else {
		document.resourcesForm.addresource.disabled = true;
	}
}

function initMrnoDialog() {
	mrnoDialog = new YAHOO.widget.Dialog("mrnoDialog", {
		width: "600px",
		context: ["apptTable", "tl", "bl"],
		visible: false,
		modal: true,
		constraintoviewport: true,
	});
	mrnoDialog.render();
}

function showMrnoVisitDialog(appointment_id, category, startTime, time, duration, name,
	contact, resSchName, bookedResource, apptConsultation, bedname, wardname, complaint, prescDoc,mrno, id, arrived) {

	var button = document.getElementById("toolbarRow" + id);
	mrnoDialog.cfg.setProperty("context", [button, "tl", "bl"], false);
	document.getElementById("mrnoDialogId").value = id;

	setAppointmentDetails(appointment_id, category, startTime, time, duration, name,
		contact, resSchName, bookedResource, apptConsultation, bedname, wardname, complaint,prescDoc, mrno, arrived);

	mrnoDialog.show();
}

function setAppointmentDetails(appointment_id, category, startTime, time, duration, name,
		contact, resSchName, bookedResource, apptConsultation, bedname, wardname, complaint,prescDoc, mrno, arrived) {

	var primaryResourcelbl = "";
	if (category == 'DOC') primaryResourcelbl = 'Doctor';
	else if (category == 'OPE') primaryResourcelbl = 'Theatre';
	else if (category == 'SNP') primaryResourcelbl = 'Service Resource';
	else if (category == 'DIA') primaryResourcelbl = 'Equipment';
	else {};

	var secondResourcelbl = "";
	if (category == 'DOC') secondResourcelbl = 'Consultation';
	else if (category == 'OPE') secondResourcelbl = 'Surgery';
	else if (category == 'SNP') secondResourcelbl = 'Service';
	else if (category == 'DIA') secondResourcelbl = 'Test';
	else {};

	document.schedulerMrnoForm.arrived.value = arrived;

	document.schedulerMrnoForm.appointmentId.value = appointment_id;
	document.schedulerMrnoForm._category.value = category;
	document.schedulerMrnoForm._name.value = name;
	document.schedulerMrnoForm._contact.value = contact;
	document.getElementById("v_mrno").textContent = mrno;
	document.getElementById("v_name").textContent = name;
	document.getElementById("v_contact").textContent = contact;

	document.getElementById("v_time").textContent = time;
	document.getElementById("v_duration").textContent = duration;

	document.getElementById("v_wardname").textContent = wardname;
	document.getElementById("v_bedname").textContent = bedname;
	document.getElementById("v_complaint").textContent = complaint;
	document.getElementById("v_prescDoc").textContent = prescDoc;
	document.getElementById("v_secondResourcelbl").textContent = secondResourcelbl;
	document.getElementById("v_secondResource").textContent = (category == 'DOC') ? resSchName : resSchName;
	document.getElementById("v_primaryResourcelbl").textContent = primaryResourcelbl;
	document.getElementById("v_primaryResource").textContent = bookedResource;

	setSelectedIndex(document.getElementById("app_visit_id"), "");
}

var schPatientInfo = null;
var activeVisits = null;
var patientDetails = null;
var patbillDetails = null;
var gIsInsurance = false;

function getPatientDetails() {
	var mrno = document.schedulerMrnoForm._searchmrno.value;
	var visitSelect = document.schedulerMrnoForm.patient_id;

	schPatientInfo = null;
	activeVisits = null;
	patientDetails = null;
	patbillDetails = null;

	//clearVisitSelect();

	if (mrno != '') {
		var ajaxobj = newXMLHttpRequest();
		var url = cpath + '/pages/resourcescheduler/addeditappointments.do?'+
								'method=getPatientDetailsJSON&mrno=' + mrno;
		if(!empty(visitSelect) && visitSelect.value != 'None')
			url += '&patient_id='+ rform.patient_id.value;
		ajaxobj.open("POST", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var schPatientInfo =" + ajaxobj.responseText);

					activeVisits = !empty(schPatientInfo.activeVisits) ? schPatientInfo.activeVisits : null;
					patientDetails = !empty(schPatientInfo.patientDetails) ? schPatientInfo.patientDetails : null;
					patBillDetails = !empty(schPatientInfo.billDetails) ? schPatientInfo.billDetails : null;

					document.schedulerMrnoForm._searchPatientFirstName.value = patientDetails.patient_name;
					document.schedulerMrnoForm._searchPatientLastName.value = patientDetails.last_name;
					document.schedulerMrnoForm._searchPatientPhone.value = patientDetails.patient_phone;
					if(patBillDetails != null && patientDetails != null)
						gIsInsurance = patBillDetails.is_tpa && patientDetails.primary_sponsor_id != '' && patientDetails.primary_sponsor_id != null;
				}
			}
		}
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

function clearDetails() {

	schPatientInfo = null;
	activeVisits = null;
	patientDetails = null;
	patBillDetails = null;

	document.schedulerMrnoForm.arrived.value = "";

	document.schedulerMrnoForm.appointmentId.value = "";
	document.schedulerMrnoForm._category.value = "";
	document.schedulerMrnoForm.appointment_status.value = "";

	document.schedulerMrnoForm._name.value = "";
	document.schedulerMrnoForm._contact.value = "";
	document.schedulerMrnoForm._searchmrno.value = "";
	document.schedulerMrnoForm._searchmrno.readOnly = true;
	document.schedulerMrnoForm._searchmrno.disabled = true;

	for (var i = 0; i < document.schedulerMrnoForm._mrnoexists.length; i++) {
		document.schedulerMrnoForm._mrnoexists[i].checked = false;
	}
	document.schedulerMrnoForm._searchPatientFirstName.value = "";
	document.schedulerMrnoForm._searchPatientLastName.value = "";
	document.schedulerMrnoForm._searchPatientPhone.value = "";

	document.getElementById("v_mrno").textContent = "";
	document.getElementById("v_name").textContent = "";
	document.getElementById("v_contact").textContent = "";
	document.getElementById("v_time").textContent = "";
	document.getElementById("v_duration").textContent = "";
	document.getElementById("v_wardname").textContent = "";
	document.getElementById("v_bedname").textContent = "";
	document.getElementById("v_complaint").textContent = "";
	document.getElementById("v_secondResourcelbl").textContent = "";
	document.getElementById("v_secondResource").textContent = "";
	document.getElementById("v_primaryResourcelbl").textContent = "";
	document.getElementById("v_primaryResource").textContent = "";
	document.getElementById("v_prescDoc").textContent = "";

	clearVisitSelect();
	setSelectedIndex(document.getElementById("app_visit_id"), "");
}
var scheduler_id = null;
var mr_no = null;
var scheduler_category = null;
var priorAuthRequired;
function checkMultipleVisitAndRegisterIfArrived(appointment_id, category,
	startTime, time, duration, name, contact,schedulerId, resSchName, bookedResourceId,bookedResource,
	apptConsultation, bedname, wardname, complaint,prescDoc, appoint_status, mrno, id, arrived) {
	clearDetails();
	scheduler_id = schedulerId;
	if(empty(resSchName)) {
		var resList = null;
		var bookedResList = null;
		if(category == 'DOC') {
			resList = findInList(docJson, "DOCTOR_ID", schedulerId);
			resSchName = resList["DOCTOR_NAME"];
			bookedResource = resSchName;
		} else if(category == 'DIA') {
			resList = findInList(testsJson, "TEST_ID", schedulerId);
			resSchName = resList["TEST_NAME"];
			bookedResList = findInList(equipmentsJSON, "EQ_ID", bookedResourceId);
			bookedResource = bookedResList['EQUIPMENT_NAME']
		} else if(category == 'SNP') {
			if(empty(schedulerId)) {
				resSchName = "";
			} else {
				resList = findInList(servicesJson, "SERVICE_ID", schedulerId);
				resSchName = resList["SERVICE_NAME"];
			}
			bookedResList = findInList(serviceResourcesListJson, "serv_res_id", bookedResourceId);
			bookedResource = bookedResList['serv_resource_name'];
		} else if(category == 'OPE') {
			if(empty(schedulerId)) {
				resSchName = "";
			} else {
				resList = findInList(surgeriesJson, "OP_ID", schedulerId);
				resSchName = resList["OPERATION_NAME"];
			}
			bookedResList = findInList(theatresJSON, "THEATRE_ID", bookedResourceId);
			bookedResource = bookedResList['THEATRE_NAME'];
		}
	}
	mr_no = mrno;
	scheduler_category = category;
	if (category == 'DOC' && document.getElementById('consultationTypeCell')) {
		document.getElementById('consultationTypeCell').style.display = 'none';
		document.getElementById('emptyCell1').style.display = 'table-cell';
		document.getElementById('emptyCell2').style.display = 'table-cell';
		document.getElementById('emptyCell3').style.display = 'table-cell';
		document.getElementById('emptyCell4').style.display = 'table-cell';
		document.getElementById('emptyCell5').style.display = 'none';
		document.getElementById('emptyCell6').style.display = 'none';
	}

	if (category =='OPE' && document.getElementById('scheduler_prior_auth_no') && !empty(schedulerId)) {
		var list = findInList(surgeriesJson, "OP_ID", schedulerId);
		priorAuthRequired = list['PRIOR_AUTH_REQUIRED'];
		if (!empty(priorAuthRequired) && priorAuthRequired != 'N') {
			document.getElementById('scheduler_prior_auth_row').style.display = "table-row";
		} else {
			document.getElementById('scheduler_prior_auth_row').style.display = "none";
		}

		var apptDetails = getPatientAppointmentDetails(appointment_id);
		if (!empty(apptDetails)) {
			document.getElementById('scheduler_prior_auth_no').value = !empty(apptDetails.scheduler_prior_auth_no) ? apptDetails.scheduler_prior_auth_no : "";
			document.getElementById('scheduler_prior_auth_mode_id').value = !empty(apptDetails.scheduler_prior_auth_mode_id) ? apptDetails.scheduler_prior_auth_mode_id : "";
		}
	}
	document.schedulerMrnoForm._mrnoexists[0].checked = true;
	enableSearchMrnoField(document.schedulerMrnoForm._mrnoexists[0]);
	document.schedulerMrnoForm._searchmrno.value = mrno;
	document.schedulerMrnoForm._searchmrno.disabled = true;
	getPatientDetails();
	document.getElementById("mrnoVisitDialoglbl").textContent = "Appointment Visit Association";
	document.getElementById("v_noMrnoRow").style.display = 'none';
	document.getElementById("v_chooseMrNoRow").style.display = 'none';
	document.getElementById("v_VisitIdRow").style.display = 'table-row';

	setAppointmentDetails(appointment_id, category, startTime, time, duration, name,
		contact, resSchName, bookedResource, apptConsultation, bedname, wardname, complaint,prescDoc, mrno, arrived);

	var visitSelect = document.getElementById("app_visit_id");
	loadActiveVisits(visitSelect, activeVisits);

	if (visitSelect.options.length == 1) {
		if (!empty(visitSelect.value) && visitSelect.value == 'None') {
			registerIfArrived(appointment_id, category);
		} else {
			if(!isDuplicateConsultation(category) && !empty(getResourceScheduleId(category))) {
				registerIfArrived(appointment_id, category);
			} else {
				showMrnoVisitDialog(appointment_id, category, startTime, time, duration, name,
					contact, resSchName, bookedResource, apptConsultation, bedname, wardname, complaint, prescDoc, mrno, id, arrived);
				loadActiveVisits(visitSelect, activeVisits);
			}
		}
	} else {
		//If the scheduler generate order is set to N then no need to open the dialog to choose visit,
		//just mark the status as Arrived Ref BUG# 42006
		if (schedulerGenerateOrder == 'N' && arrived == 'Arrived' && !(modAdvancedOT == 'Y' && category == 'OPE')) {
			setArrivedStatus(appointment_id, category);
			return true;
		}
		showMrnoVisitDialog(appointment_id, category, startTime, time, duration, name,
			contact, resSchName, bookedResource, apptConsultation, bedname, wardname, complaint,prescDoc, mrno, id, arrived);
	}
}

// Active visits are loaded
function loadActiveVisits(visitSelect, activeVisits) {

	clearVisitSelect();
	if (activeVisits != null && visitSelect != null && modInsExt != 'Y') {
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

function registerIfArrived(appointment_id, category) {

	document.schedulerMrnoForm.appointment_status.value = 'Arrived';
	document.schedulerMrnoForm.appointmentId.value = appointment_id;
	if ((category != "SNP") && (category != "DOC") && (category != "DIA") && (category != "OPE")) {
		document.schedulerMrnoForm._method.value = "saveAppointmentDetails";
		document.schedulerMrnoForm.submit();
		return true;
	} else {
		document.schedulerMrnoForm._method.value = "getSchedulerRegistration";
		document.schedulerMrnoForm._registrationType.value = category;
		document.schedulerMrnoForm._category.value = category;
	}

	var dialogId = document.getElementById("mrnoDialogId").value;
	if (document.getElementById('toolbarRow' + dialogId) != null) {
		document.getElementById('toolbarRow' + dialogId).className = '';
		mrnoDialog.hide();
	}
	document.schedulerMrnoForm.submit();
}

function setArrivedStatus(appointmentId, category) {

	document.schedulerMrnoForm.appointment_status.value = 'Arrived';
	document.schedulerMrnoForm.appointmentId.value = appointmentId;
	document.schedulerMrnoForm._method.value = "setArrivedStatus";
	document.schedulerMrnoForm.submit();
}

function showMrnoSearchDialog(appointment_id, category,
	startTime, time, duration, name, contact,schedulerId,resSchName,bookedResourceId, bookedResource,
	apptConsultation, bedname, wardname, complaint,prescDoc, mrno, id, arrived) {

	clearDetails();

	if(empty(resSchName)) {
		var resList = null;
		var bookedResList = null;
		if(category == 'DOC') {
			resList = findInList(docJson, "DOCTOR_ID", schedulerId);
			resSchName = resList["DOCTOR_NAME"];
			bookedResource = resSchName;
		} else if(category == 'DIA') {
			resList = findInList(testsJson, "TEST_ID", schedulerId);
			resSchName = resList["TEST_NAME"];
			bookedResList = findInList(equipmentsJSON, "EQ_ID", bookedResourceId);
			bookedResource = bookedResList['EQUIPMENT_NAME']
		} else if(category == 'SNP') {
			if(empty(schedulerId)) {
				resSchName = "";
			} else {
				resList = findInList(servicesJson, "SERVICE_ID", schedulerId);
				resSchName = resList["SERVICE_NAME"];
			}
			bookedResList = findInList(serviceResourcesListJson, "serv_res_id", bookedResourceId);
			bookedResource = bookedResList['serv_resource_name'];
		} else if(category == 'OPE') {
			if(empty(schedulerId)) {
				resSchName = "";
			} else {
				resList = findInList(surgeriesJson, "OP_ID", schedulerId);
				resSchName = resList["OPERATION_NAME"];
			}
			bookedResList = findInList(theatresJSON, "THEATRE_ID", bookedResourceId);
			bookedResource = bookedResList['THEATRE_NAME'];
		}
	}

	gIsInsurance = false;
	document.getElementById("mrnoVisitDialoglbl").textContent = "Patient Details";
	document.getElementById("v_noMrnoRow").style.display = 'table-row';
	document.getElementById("v_chooseMrNoRow").style.display = 'table-row';
	document.getElementById("v_VisitIdRow").style.display = 'none';

	showMrnoVisitDialog(appointment_id, category, startTime, time, duration, name,
	contact, resSchName,bookedResource, apptConsultation, bedname, wardname, complaint,prescDoc, mrno, id, arrived);
}

function validateAndConfirm() {
	var errorMsg = validatePatientDetails();
	var appPatientName = document.schedulerMrnoForm._name.value;
	var appPatientContact = document.schedulerMrnoForm._contact.value;

	var patientMsg = getString("js.scheduler.todaysappointment.fail.update");
	 patientMsg+="\n\t";
	 patientMsg+=getString("js.scheduler.todaysappointment.name");
	 patientMsg+=appPatientName;
	 patientMsg+= " \n\t";
	 patientMsg+=getString("js.scheduler.todaysappointment.contactno");
	 patientMsg+=appPatientContact;

	if (errorMsg != "Patient Details:") {
		var ok = confirm(errorMsg + " \n\n Does not match with... \n\n " + patientMsg
									+ " \n\n Do you want to continue with this Mrno ?")
		if (ok) return true;
		else {
			document.schedulerMrnoForm._searchmrno.value = '';
			document.schedulerMrnoForm._searchmrno.readOnly = true;
			for (var i = 0; i < document.schedulerMrnoForm._mrnoexists.length; i++) {
				document.schedulerMrnoForm._mrnoexists[i].checked = false;
			}
			document.schedulerMrnoForm._searchPatientFirstName.value = "";
			document.schedulerMrnoForm._searchPatientLastName.value = "";
			document.schedulerMrnoForm._searchPatientPhone.value = "";
			return false;
		}
	}
	return true;
}

function validatePatientDetails() {
	var patientFName = document.schedulerMrnoForm._searchPatientFirstName.value;
	var patientLName = document.schedulerMrnoForm._searchPatientLastName.value;
	var patientContact = document.schedulerMrnoForm._searchPatientPhone.value;

	var appPatientName = document.schedulerMrnoForm._name.value;
	var appPatientContact = document.schedulerMrnoForm._contact.value;

	var errorMsg = getString("js.scheduler.todaysappointment.patient.details");

	if (appPatientName.search(patientFName) == -1) {
		errorMsg = errorMsg + getString("js.scheduler.todaysappointment.fname") + patientFName;
	}
	if (appPatientName.search(patientLName) == -1) {
		errorMsg = errorMsg + getString("js.scheduler.todaysappointment.lname") + patientLName;
	}
	if (appPatientContact != patientContact) {
		errorMsg = errorMsg + getString("js.scheduler.todaysappointment.cnumber")+ patientContact;
	}
	return errorMsg;
}

function onEditMrnoCancel() {
	var id = document.getElementById("mrnoDialogId").value;
	document.getElementById('toolbarRow' + id).className = '';
	clearDetails();
	mrnoDialog.cancel();
}

function onEditMrnoSubmit() {
	var appId = document.schedulerMrnoForm.appointmentId.value;
	var category = document.schedulerMrnoForm._category.value;
	var arrived = document.schedulerMrnoForm.arrived.value;

	var visitSelect = document.getElementById("app_visit_id");
	if (visitSelect != null && visitSelect.options.length > 1) {

		if (visitSelect.value == 'None') {
			var ok = confirm(" Patient has active visit(s)."
							+ "\n Do you want to continue with new registration?");
			if (!ok) {
				visitSelect.focus();
				return false;
			}
		}
	}

	if(!empty(category) && category == 'OPE' && arrived == 'Arrived' && document.getElementById('scheduler_prior_auth_row')) {
		if(gIsInsurance && priorAuthRequired != 'N') {

			if(empty(document.getElementById('scheduler_prior_auth_no').value)) {
				showMessage("js.scheduler.todaysappointment.authno.required");
				document.getElementById('scheduler_prior_auth_no').focus();
				return false;
			}

			if(empty(document.getElementById('scheduler_prior_auth_mode_id').value)) {
				showMessage("js.scheduler.todaysappointment.authno.required");
				document.getElementById('scheduler_prior_auth_mode_id').focus();
				return false;
			}
		}
	}

	if (arrived != 'Arrived') {
		if (!validateMrnoOptions()) {
			showMessage("js.scheduler.todaysappointment.select.anyoption");
			document.schedulerMrnoForm._mrnoexists[0].focus();
			return false;
		}

		if (!validateMrnoDialog()) {
			return false;
		}

		if (!validateAndConfirm()) {
			return false;
		}

		if (!updateAppointmentMrno(appId)) {
		 	showMessage("js.scheduler.todaysappointment.fail.update");
		 	return false;
		}
		document.appointmentSearchForm._method.value = "getTodaysPatientAppointments";
		document.appointmentSearchForm.submit();
	}

	if (document.schedulerMrnoForm.arrived.value == 'Arrived') {

		if (!checkDuplicateAppointment(category)) {
			var dialogId = document.getElementById("mrnoDialogId").value;
			document.getElementById('toolbarRow' + dialogId).className = '';
			mrnoDialog.hide();
			return false;
		}
		if (!checkOrderOrConsultation(category)) return false;

		if (document.getElementById("v_mrno").textContent !=''
			&& document.schedulerMrnoForm.patient_id
			&& document.schedulerMrnoForm.patient_id.value != 'None'
			&& document.schedulerMrnoForm.patient_id.value != '' ) {
		/*	if (!updateAppointmentMrno(appId)) {
			 	alert("Failed to update ");
			 	return false;
			}*/
		}

		registerIfArrived(appId, category);
	}

	var dialogId = document.getElementById("mrnoDialogId").value;
	document.getElementById('toolbarRow' + dialogId).className = '';
	mrnoDialog.hide();
}

function updateAppointmentMrno(appointment_id) {
	var mrno = document.schedulerMrnoForm._searchmrno.value;
	var visitId = document.schedulerMrnoForm.patient_id.value;
	var reqObject = newXMLHttpRequest();
	var url = "../../pages/resourcescheduler/addeditappointments.do?"+
			  "method=updateVisitAndPatientMrno&appointment_id=" + appointment_id
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

function validateMrnoOptions() {
	for (var i = 0; i < document.schedulerMrnoForm._mrnoexists.length; i++) {
		if (document.schedulerMrnoForm._mrnoexists[i].checked) {
			return true;
		}
	}
	return false;
}

function validateMrnoDialog() {
	for (var i = 0; i < document.schedulerMrnoForm._mrnoexists.length; i++) {
		if (document.schedulerMrnoForm._mrnoexists[i].checked) {
			if (document.schedulerMrnoForm._mrnoexists[i].value == 'Y') {
				var searchmrno = document.schedulerMrnoForm._searchmrno.value;
				var visit = document.schedulerMrnoForm.patient_id.value;
				if (trimAll(searchmrno) == '') {
					showMessage("js.scheduler.todaysappointment.enter.mrno");
					document.schedulerMrnoForm._searchmrno.focus();
					return false;
				}

				if (document.getElementById("v_noMrnoRow").style.display == 'none' && visit == '') {
					showMessage("js.scheduler.todaysappointment.select.visit");
					document.schedulerMrnoForm.patient_id.focus();
					return false;
				}
			}
		}
	}
	return true;
}

function isDuplicateConsultation(category) {
	if (category == 'DOC') {
		if (document.schedulerMrnoForm.patient_id
				&& document.schedulerMrnoForm.patient_id.value != 'None'
				&& document.schedulerMrnoForm.patient_id.value != '') {
			var ajaxReqObject = newXMLHttpRequest();
			var url = "./addeditappointments.do?method=getDuplicateAppDetails";
			url = url + "&patient_id=" + document.schedulerMrnoForm.patient_id.value;
			var reqObject = newXMLHttpRequest();
			reqObject.open("POST", url.toString(), false);
			reqObject.send(null);
			if (reqObject.readyState == 4) {
				if ((reqObject.status == 200) && (reqObject.responseText != null)) {
					eval("var consList = " + reqObject.responseText);
					if (consList != null && consList.length > 0) {
						if (consList[0].visit_type == 'o') {
							return true;
						}
					}
				}
			}
		}
	}
	return false;
}

function checkDuplicateAppointment(category) {
	if (category == 'DOC') {
		var isDuplicateCons = isDuplicateConsultation(category);
		if (isDuplicateCons) {
			if (eClaimModule == 'Y') {
				var msg=getString("js.scheduler.todaysappointment.consultation.notallowed");
					msg+="\n";
					msg+=getString("js.scheduler.todaysappointment.cannotorder.onevisit");
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

function getResourceScheduleId(category) {
	var scheduleId = document.getElementById("v_secondResource").textContent;
	return scheduleId;
}

function checkOrderOrConsultation(category) {
	var msg = "";
	var scheduleId = getResourceScheduleId(category);
	var resourcetxt = "";
	if (category == 'SNP')
		resourcetxt = "service";
	else if (category == 'DIA')
		resourcetxt = "test";
	else if (category == 'OPE')
		resourcetxt = "surgery";

	if (category == 'DOC') {
		msg = getString("js.scheduler.todaysappointment.warning.noconsultation.selected");
		msg+=" \n";
		msg+=getString("js.scheduler.todaysappointment.continue");
		//alert(msg);
	} else {
		msg = getString("js.scheduler.todaysappointment.warningno");
		msg+=resourcetxt;
		msg+=getString("js.scheduler.todaysappointment.is.selected");
		msg+="\n";
		msg+=getString("js.scheduler.todaysappointment.noorder.placed");
		msg+=" \n";
		msg+=getString("js.scheduler.todaysappointment.continue");
		//alert(msg);
	}
	var schIdObj = document.schedulerMrnoForm.consultationTypes;
	if (category == 'DOC') {
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

function onActiveCheck() {
	var patientStatus = document.getElementById('active_patient');
	if (patientStatus.checked) {
		psAc.destroy();
		psAc = Insta.initMRNoAcSearch(cpath, "_searchmrno", "mrnoAcDropdown", 'active',
		function (type, args) {
			getPatientDetails();
		}, function (type, args) {
			clearDetails();
		});
	} else {
		psAc.destroy();
		psAc = Insta.initMRNoAcSearch(cpath, "_searchmrno", "mrnoAcDropdown", 'all',
		function (type, args) {
			getPatientDetails();
		}, function (type, args) {
			clearDetails();
		});
	}
}

function changePatientStatus() {
	var status = '';

	if (document.getElementById('_mr_no').checked) {
		status = 'active';
	} else {
		status = 'all';
	}
	if (status == 'active') {
		psAc1.destroy();
		psAc1 = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', status, null, null);
	} else {
		psAc1.destroy();
		psAc1 = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', status, null, null);
	}
}

function enableSearchMrnoField(radio) {
	if (radio.checked && radio.value == 'Y') {
		document.schedulerMrnoForm._searchmrno.value = '';
		document.schedulerMrnoForm._searchmrno.readOnly = false;
		document.schedulerMrnoForm._searchmrno.disabled = false;
		document.schedulerMrnoForm.patient_id.disabled = false;
	} else {
		document.schedulerMrnoForm._searchmrno.value = '';
		document.schedulerMrnoForm._searchmrno.readOnly = true;
		document.schedulerMrnoForm._searchmrno.disabled = true;
		setSelectedIndex(document.schedulerMrnoForm.patient_id, "");
		document.schedulerMrnoForm.patient_id.disabled = true;
	}
}


function changeStatusOfAllSelectedAppointments(action) {
	var checkBoxes = document.appointmentSearchForm._cancelAppointment;
	var anyChecked = false;
	var disabledCount = 0;
	var totalCancellation = 1;
	if (checkBoxes.length) {
		totalCancellation = checkBoxes.length;
		for (var i=0; i<checkBoxes.length; i++) {
			if (!checkBoxes[i].disabled && checkBoxes[i].checked) {
				anyChecked = true;
				break;
			}
		}

		for (var i=0; i<checkBoxes.length; i++) {
			if (checkBoxes[i].disabled)
				disabledCount++;
		}

	} else {
		var checkBox = document.appointmentSearchForm._cancelAppointment;
		if (!checkBox.disabled && checkBox.checked)
			anyChecked = true;
		if (checkBox.disabled)
			disabledCount++;
	}
	var appIds = new Array();
	if (checkBoxes.length) {
		var l = 0;
		for(var k=0;k<checkBoxes.length;k++){
			if (checkBoxes[k].checked && !checkBoxes[k].disabled) {
				appIds[l] = checkBoxes[k].value;
				l++;
			}
		}
	} else {
		appIds = checkBox.value;
	}

	if (action == 'cancel') {
		if (!anyChecked) {
			showMessage("js.scheduler.todaysappointment.appointments.cancel");
			return false;
		}
		var cancelReason = document.appointmentSearchForm._cancel_reason;
			if (typeof(cancelReason) && empty(cancelReason.value)) {
				showMessage("js.scheduler.todaysappointment.cancelreason.required");
				document.getElementById("cancelText").style.display = "table-cell";
				document.getElementById("cancelInput").style.display = "table-cell";
				cancelReason.focus();
				return false;
			}

			if(!updateAppointmentCancelReason(appIds)) {
				showMessage("js.scheduler.todaysappointment.fail.savereason");
				return false;
			}
	} else if(action == 'noshow') {
		if (!anyChecked) {
			showMessage("js.scheduler.todaysappointment.appointments.noshow");
			return false;
		}
		if (!updateAppointmentStatus(appIds)) {
			return false;
		}
	} else if(action== "confirmed") {
		if (!anyChecked) {
			showMessage("js.scheduler.todaysappointment.appointments.confirm");
			return false;
		}
		if (!confirmAppointments(appIds)) {
			return false;
		}
	}
	document.appointmentSearchForm._method.value = "getTodaysPatientAppointments";
	document.appointmentSearchForm.submit();
}


function confirmAppointments(appIds) {
	var filterValue = document.resourcesForm.resFilter.value;
	var reqObj = newXMLHttpRequest();
	var url = "../../pages/resourcescheduler/todaysappointments.do?"+
				"_method=confirmAppointments&appointment_status=Confirmed&resFilter=" + filterValue+
				"&appointment_id=" + appIds;
	reqObj.open("POST", url.toString(), false);
	reqObj.send(null);
	if (reqObj.readyState == 4) {
		if ((reqObj.status == 200) && (reqObj.responseText != null)) {
			var obj = reqObj.responseText;
			if (obj != null && obj == 'Updated') {
				return true;
			} else if (obj != null && obj == 'alreadyConfirmed') {
				showMessage("js.scheduler.todaysappointment.uncheck.appointments");
				return false;
			} else if (obj != null && obj == 'arrived') {
				var msg=getString("js.scheduler.todaysappointment.uncheck.arriveappointments");
				msg+="\n";
				 msg+=getString("js.scheduler.todaysappointment.appointments.notconfirm");
				 alert(msg);
				return false;
			}
		}
	}
	showMessage("js.scheduler.todaysappointment.fail.saveappointment");
	return false;
}

function updateAppointmentStatus(appIds) {
	var reqObj = newXMLHttpRequest();
	var url = "../../pages/resourcescheduler/todaysappointments.do?"+
				"_method=updateAppointmentStatus&appointment_status=Noshow&"+
				"&appointment_id=" + appIds;
	reqObj.open("POST", url.toString(), false);
	reqObj.send(null);
	if (reqObj.readyState == 4) {
		if ((reqObj.status == 200) && (reqObj.responseText != null)) {
			var obj = reqObj.responseText;
			if (obj != null && obj == 'Updated') {
				return true;
			} else if (obj != null && obj == 'arrived') {
				var msg=getString("js.scheduler.todaysappointment.uncheck.arriveappointments");
				msg+="\n";
				msg+=getString("js.scheduler.todaysappointment.appointments.noshow");
				alert(msg);
				return false;
			}
		}
	}
	showMessage("js.scheduler.todaysappointment.fail.saveappointment");
	return false;
}

function updateAppointmentCancelReason(appIds) {
	var filterValue = document.resourcesForm.resFilter.value;
	var cancelReason = document.getElementById('_cancel_reason').value;
	var reqObj = newXMLHttpRequest();
	var url = "../../pages/resourcescheduler/todaysappointments.do?"+
				"_method=updateAppointmentCancelReason&appointment_status=Cancel&"+
				"cancel_reason=" + cancelReason + "&resFilter=" + filterValue + "&appointment_id=" + appIds;
	reqObj.open("POST", url.toString(), false);
	reqObj.send(null);
	if (reqObj.readyState == 4) {
		if ((reqObj.status == 200) && (reqObj.responseText != null)) {
			var obj = reqObj.responseText;
			if (obj != null && obj == 'Updated') {
				return true;
			}
		}
	}
	return false;
}

function doctorsAutocomp() {
	YAHOO.example.doctorNamesArray = [];
	YAHOO.example.doctorNamesArray.length = docJson.length;
	for (var i = 0; i < docJson.length; i++) {
		var item = docJson[i];
		YAHOO.example.doctorNamesArray[i] = item["DOCTOR_NAME"];
	}

	YAHOO.example.ACJSArray = new function () {
		datasource = new YAHOO.widget.DS_JSArray(YAHOO.example.doctorNamesArray);
		var autoComp = new YAHOO.widget.AutoComplete('doctor_name', 'docDropdown', datasource);
		autoComp.prehighlightClassName = "yui-ac-prehighlight";
		autoComp.typeAhead = true;
		autoComp.useShadow = true;
		autoComp.allowBrowserAutocomplete = false;
		autoComp.minQueryLength = 0;
		autoComp.maxResultsDisplayed = 20;
		autoComp.autoHighlight = false;
		autoComp.forceSelection = true;
		autoComp.filterResults = Insta.queryMatchWordStartsWith;
		autoComp.formatResult = Insta.autoHighlightWordBeginnings;

		autoComp.itemSelectEvent.subscribe(function (sType, aArgs) {
			var data = document.appointmentSearchForm.doctor_name.value;
			for (var i = 0; i < docJson.length; i++) {
				if (data == docJson[i].DOCTOR_NAME) {
					document.getElementById("doctor").value = docJson[i].DOCTOR_ID;
				}
			}
		});
	}
}

function servicesAutocomp() {
	YAHOO.example.serviceNamesArray = [];
	var resList = {
		result: servicesJson
	};
	var datasource = new YAHOO.util.LocalDataSource(resList);
	datasource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	datasource.responseSchema = {
	resultsList : "result",
	fields: [{key:"SERVICE_NAME"},{key:"SERVICE_ID"},{key:"DEPT_NAME"}
			 ]
	};

	var autoComp = new YAHOO.widget.AutoComplete('service_name', 'servDropdown', datasource);
	autoComp.prehighlightClassName = "yui-ac-prehighlight";
	autoComp.typeAhead = false;
	autoComp.useShadow = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 1;
	autoComp.maxResultsDisplayed = 20;
	autoComp.autoHighlight = true;
	autoComp.forceSelection = false;
	autoComp.resultTypeList = false;
	autoComp.queryMatchContains = true;
	autoComp.formatResult = Insta.autoHighlightWordBeginnings;

	autoComp.itemSelectEvent.subscribe(function (sType, aArgs) {
		var data = document.appointmentSearchForm.service_name.value;
		var record = aArgs[2];
		document.getElementById('service').value = record.SERVICE_ID;
	});
}

function testsAutocomp() {
	YAHOO.example.testNamesArray = [];
	var resList = {
		result: testsJson
	};
	var datasource = new YAHOO.util.LocalDataSource(resList);
	datasource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	datasource.responseSchema = {
	resultsList : "result",
	fields: [{key:"TEST_NAME"},{key:"TEST_ID"}
			 ]
	};
	var autoComp = new YAHOO.widget.AutoComplete('test_name', 'testDropdown', datasource);
	autoComp.prehighlightClassName = "yui-ac-prehighlight";
	autoComp.typeAhead = true;
	autoComp.useShadow = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 0;
	autoComp.maxResultsDisplayed = 20;
	autoComp.autoHighlight = false;
	autoComp.forceSelection = true;
	autoComp.resultTypeList = false;
	autoComp.filterResults = Insta.queryMatchWordStartsWith;
	autoComp.formatResult = Insta.autoHighlightWordBeginnings;

	autoComp.itemSelectEvent.subscribe(function (sType, aArgs) {
		var data = document.appointmentSearchForm.test_name.value;
		var record = aArgs[2];
		document.getElementById('test').value = record.TEST_ID;
	});
}

function surgeriesAutocomp() {
	YAHOO.example.surgeryNamesArray = [];
	var resList = {
		result: surgeriesJson
	};
	var datasource = new YAHOO.util.LocalDataSource(resList);
	datasource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	datasource.responseSchema = {
	resultsList : "result",
	fields: [{key:"OPERATION_NAME"},{key:"OP_ID"}
			 ]
	};

	var autoComp = new YAHOO.widget.AutoComplete('surgery_name', 'surgDropdown', datasource);
	autoComp.prehighlightClassName = "yui-ac-prehighlight";
	autoComp.typeAhead = true;
	autoComp.useShadow = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 0;
	autoComp.maxResultsDisplayed = 20;
	autoComp.autoHighlight = false;
	autoComp.forceSelection = true;
	autoComp.resultTypeList = false;
	autoComp.filterResults = Insta.queryMatchWordStartsWith;
	autoComp.formatResult = Insta.autoHighlightWordBeginnings;

	autoComp.itemSelectEvent.subscribe(function (sType, aArgs) {
		var data = document.appointmentSearchForm.surgery_name.value;
		var record = aArgs[2];
		document.getElementById('surgery').value = record.OP_ID;
	});
}

function enableNames() {
	var appntType = document.getElementById("resFilter").options[document.getElementById("resFilter").selectedIndex].value;
	if (appntType == "DOC") {
		document.getElementById("doctor_name").disabled = false;
		document.getElementById("surgery_name").disabled = true;
		document.getElementById("service_name").disabled = true;
		document.getElementById("test_name").disabled = true;

		document.getElementById("doctor").disabled = false;
		document.getElementById("surgery").disabled = true;
		document.getElementById("service").disabled = true;
		document.getElementById("test").disabled = true;

	} else if (appntType == "OPE") {
		document.getElementById("doctor_name").disabled = true;
		document.getElementById("surgery_name").disabled = false;
		document.getElementById("service_name").disabled = true;
		document.getElementById("test_name").disabled = true;

		document.getElementById("doctor").disabled = true;
		document.getElementById("surgery").disabled = false;
		document.getElementById("service").disabled = true;
		document.getElementById("test").disabled = true;

	} else if (appntType == "SNP") {
		document.getElementById("doctor_name").disabled = true;
		document.getElementById("surgery_name").disabled = true;
		document.getElementById("service_name").disabled = false;
		document.getElementById("test_name").disabled = true;

		document.getElementById("doctor").disabled = true;
		document.getElementById("surgery").disabled = true;
		document.getElementById("service").disabled = false;
		document.getElementById("test").disabled = true;

	} else if (appntType == "DIA") {
		document.getElementById("doctor_name").disabled = true;
		document.getElementById("surgery_name").disabled = true;
		document.getElementById("service_name").disabled = true;
		document.getElementById("test_name").disabled = false;

		document.getElementById("doctor").disabled = true;
		document.getElementById("surgery").disabled = true;
		document.getElementById("service").disabled = true;
		document.getElementById("test").disabled = false;

	} else {
		document.getElementById("doctor_name").disabled = true;
		document.getElementById("surgery_name").disabled = true;
		document.getElementById("service_name").disabled = true;
		document.getElementById("test_name").disabled = true;

		document.getElementById("doctor").disabled = true;
		document.getElementById("surgery").disabled = true;
		document.getElementById("service").disabled = true;
		document.getElementById("test").disabled = true;
	}
	return true;
}

function disableNamesAndResetHiddenValues() {
	document.getElementById("doctor_name").disabled = true;
	document.getElementById("surgery_name").disabled = true;
	document.getElementById("service_name").disabled = true;
	document.getElementById("test_name").disabled = true;

	document.getElementById("doctor").value = "";
	document.getElementById("surgery").value = "";
	document.getElementById("service").value = "";
	document.getElementById("test").value = "";
}

function validateSearchForm() {
	var fromTime = document.getElementById('appoint_time0');
	var toTime = document.getElementById('appoint_time1');
	if (fromTime.value != null) {
		if (!validateTime(fromTime)) {
			fromTime.focus();
			return false;
		}
	}

	if (toTime.value != null) {
		if (!validateTime(toTime)) {
			toTime.focus();
			return false;
		}
	}
	return true;
}

function onChangeVisitOfTodaysAppScreen() {
	rform = document.schedulerMrnoForm;
	var category = scheduler_category;
	var visitId = rform.patient_id.value;
	var mrno 	= document.getElementById('v_mrno').textContent;
	var appointmentId = rform.appointmentId.value;
	var arrived = document.schedulerMrnoForm.arrived.value;

	if (!empty(mrno)) {
		gActiveVisits = getActiveVisits(mrno,appointmentId)
	}
	if (category == 'DOC') {
			document.getElementById('consultationTypeCell').style.display = 'none';
			document.getElementById('emptyCell1').style.display = 'table-cell';
			document.getElementById('emptyCell2').style.display = 'table-cell';
			document.getElementById('emptyCell3').style.display = 'table-cell';
			document.getElementById('emptyCell4').style.display = 'table-cell';
			document.getElementById('emptyCell5').style.display = 'none';
			document.getElementById('emptyCell6').style.display = 'none';
	}

	var patient = findInList(gActiveVisits, "patient_id", visitId);

	if (!empty(patient)) {
		rform.visitType.value = !empty(patient.visit_type) ? patient.visit_type : '';
		document.getElementById('v_wardname').textContent = !empty(patient.alloc_ward_name) ? patient.alloc_ward_name : '';
		document.getElementById('v_bedname').textContent = !empty(patient.alloc_bed_name) ? patient.alloc_bed_name : '';

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

		var centerId = -1;
		if (!empty(patient.center_id)) {
			centerId = patient.center_id;
		}
		if (!empty(patient.org_id)) {
			orgId = patient.org_id;
			orgName = patient.org_name;
		} else {
			orgId = "ORG0001";
			orgName = "General";
		}
		if (category == 'DOC') {
			var list = getConsultationTypes(orgId, centerId, patient.visit_type);
			loadConsultationTypes(list);
		}
	}else {
		rform.visitType.value = '';
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
		var doctorId = scheduler_id;
		var doctor = findInList(scheduleResourceListJSON, "resource_id", doctorId);
		var docChrgObj = rform.consultationTypes;
		if (typeof(docChrgObj) != 'undefined') {
			getRegDetailsForSchedulerSreen("schedulerMrnoForm");
			setDocRevistCharges(doctorId,doctor,docChrgObj,"schedulerMrnoForm");
		}
	}
}

// initOTBookings for OT Bookings screen..

function initOTBookings() {
	default_gp_first_consultation	= regPref.default_gp_first_consultation;
	default_gp_revisit_consultation	= regPref.default_gp_revisit_consultation;
	default_sp_first_consultation	= regPref.default_sp_first_consultation;
	default_sp_revisit_consultation	= regPref.default_sp_revisit_consultation;
	initResourceDialog();
	initMrnoDialog();
	psAc = Insta.initMRNoAcSearch(cpath, "_searchmrno", "mrnoAcDropdown", "active",
	function (type, args) {
		getPatientDetails();
	}, function (type, args) {
		clearDetails();
	});

	psAc1 = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'active', null, null);
	document.getElementById('_mr_no').checked = true;
	surgeriesAutocomp();
}