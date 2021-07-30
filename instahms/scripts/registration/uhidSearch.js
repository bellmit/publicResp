function systemSelect() {

	if(document.getElementById('all_systems').checked) {
		document.getElementById('local_system').disabled = true;
	}
	else if(document.getElementById('local_system').checked) {
		document.getElementById('all_systems').disabled = true;
	}
	else {
		document.getElementById('all_systems').disabled = false;
		document.getElementById('local_system').disabled = false;
	}

}
function validate() {

	if(!document.getElementById('local_system').checked)
		document.getElementById('all_systems').checked = true;
	document.UHIDSearchForm.submit();

}

var toolbar = {}
toolbar.OpRegistration={
	title: toolbarOptions["OpRegistration"]["name"],
	imageSrc: "icons/Edit.png",
	href: '/patients/opregistration/index.htm',
	onclick: null,
	description: toolbarOptions["OpRegistration"]["description"]
};

toolbar.IpRegistration= {
	title: toolbarOptions["IpRegistration"]["name"],
	imageSrc: "icons/Edit.png",
	href: '/pages/registration/IpRegistration.do?_method=getdetails',
	onclick: null,
	description: toolbarOptions["IpRegistration"]["description"],
};

function init() {
	if(null != error && "" != error)
		alert(error);
	createToolbar(toolbar);

	if(systemType == 'l')
		document.getElementById('local_system').checked = true;
	if(systemType == 'a')
		document.getElementById('all_systems').checked = true;
	
	systemSelect();
}

var uhidSetHrefs = function(params, id, enableList, toolbarKey, event, validateOnRClick) {
	if (empty(gToolbars[toolbarKey])) return false;

	var i=0;
	var toolbar = gToolbars[toolbarKey];
	for (var key in toolbar) {
		var data = toolbar[key];
		var anchor = document.getElementById('toolbarAction' + toolbarKey + key);
		if (empty(anchor)) {
			debug("No anchor for " + 'toolbarAction'+ toolbarKey + key + ":");
			i++;
			continue;
		}
		var href = data.href;
		if (!empty(href) && href != '/patients/opregistration/index.htm') {
			for (var paramname in params) {
				var paramvalue = params[paramname];
				if (paramname.charAt(0) == '%') {
					// replace a component of the href
					href = href.replace(paramname, paramvalue);
				} else {
					// append as param=value
					href += "&" + paramname + "=" + encodeURIComponent(paramvalue);
				}
			}
			anchor.href = cpath + "/" + href;
		} else if (!empty(href) && href == '/patients/opregistration/index.htm') {
			href = cpath + href;
			var isUhidPatient = Object.keys(params).indexOf("uhidPatient") != -1;
			href += "#/filter/default/patient/" + (isUhidPatient ? 'new' : params['mr_no']) + "/registration";
			if (isUhidPatient) {
				var seperator = "?";
				if (params['uhidPatientAge']) {
					href += seperator + "patient_age=" + encodeURIComponent(params['uhidPatientAge']);
					seperator = "&";
				}
				if (params['uhidPatientFirstName']) {
					href += seperator + "patient_name=" + encodeURIComponent(params['uhidPatientFirstName']);
					seperator = "&";
				}
				if (params['uhidPatientGender']) {
					href += seperator + "pat_gender=" + encodeURIComponent(params['uhidPatientGender']);
					seperator = "&";
				}
				if (params['uhidPatientLastName']) {
					href += seperator + "patient_last_name=" + encodeURIComponent(params['uhidPatientLastName']);
					seperator = "&";
				}
				if (params['uhidPatientMiddleName']) {
					href += seperator + "patient_middle_name=" + encodeURIComponent(params['uhidPatientMiddleName']);
					seperator = "&";
				}
				if (params['uhidPatientUHID']) {
					href += seperator + "oldmrno=" + encodeURIComponent(params['uhidPatientUHID']);
					seperator = "&";
					href += seperator + "user_provided_mr_no=" + encodeURIComponent(params['uhidPatientUHID']);
				}
				if (params['uhidPatientPhone']) {
					href += seperator + "mobile_no=" + encodeURIComponent(params['uhidPatientPhone']);
					seperator = "&";
				}
				href += seperator + "retain_route_params=true&";
			} else {
				href +="?retain_route_params=true&";
			}
			anchor.href = href;
		}

		var enable = true;
		if (enableList) {
			enableToolbarItem(key, enableList[i], toolbarKey);
			enable = enableList[i];
		} else {
			enableToolbarItem(key, enable, toolbarKey);
		}

		if (!empty(data.onclick) && enable) {
			setParams(anchor, params, id, toolbar, validateOnRClick);
		}
		i++;
	}
	return true;
}
