YAHOO.util.Event.onContentReady("content", initDentalChartDialogs);
function initDentalChartDialogs() {
	initCrownStatusDialog();
	initRootStatusDialog();
	initSurfaceStatusDialog();
}
var crown_status_dialog = null;
var root_status_dialog = null;
var surface_status_dialog = null;

function initCrownStatusDialog() {
	if (!document.getElementById('crown_status_div')) return ;

	document.getElementById('crown_status_div').style.display = 'block';
	var dialog = new YAHOO.widget.Dialog("crown_status_div", {
		width:"300px",
		context :["crown_status_div", "bl", "tl"],
		visible:false,
		modal:true,
		constraintoviewport:true
	});
	YAHOO.util.Event.addListener('crown_status_ok', 'click', crownStatusUpdate, dialog, true);
	YAHOO.util.Event.addListener('crown_status_close', 'click', crownStatusCancel, dialog, true);
	YAHOO.util.Event.addListener('crown_status_delete', 'click', deleteCrownStatus, dialog, true);
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
			{ fn:crownStatusCancel, scope:dialog, correctScope:true } );
	dialog.cfg.queueProperty("keylisteners", escKeyListener);
	dialog.render();
	crown_status_dialog = dialog;
}

function deleteCrownStatus() {
	var tooth_number = document.getElementsByName("dc_unv_number");
	var tooth_part = document.getElementsByName("dc_tooth_part");
	for (var j=0;j<tooth_number.length;j++) {
		if (tooth_number[j].value == document.getElementById('hid_dc_tooth_number').value
			&& tooth_part[j].value == document.getElementById('hid_dc_tooth_part').value) {
			tooth_number[j].parentNode.parentNode.removeChild(tooth_number[j].parentNode);
		}
	}
	document.getElementById('hid_dc_tooth_number').value = '';
	document.getElementById('hid_dc_tooth_part').value = '';
	document.getElementById('hid_dc_pos_x').value = 0;
	document.getElementById('hid_dc_pos_y').value = 0;

	var tooth_status_obj = document.getElementsByName("crown_status");
	for (var i=0; i<tooth_status_obj.length; i++) {
		tooth_status_obj[i].checked = false;
	}
	crown_status_dialog.cancel();
}

function crownStatusUpdate() {
	var tooth_status_obj = document.getElementsByName("crown_status");
	var selected = false;
	for (var i=0; i<tooth_status_obj.length; i++) {
		if (tooth_status_obj[i].checked) {
			var tooth_number = document.getElementsByName("dc_unv_number");
			var tooth_part = document.getElementsByName("dc_tooth_part");
			for (var j=0;j<tooth_number.length;j++) {
				if (tooth_number[j].value == document.getElementById('hid_dc_tooth_number').value
					&& tooth_part[j].value == document.getElementById('hid_dc_tooth_part').value) {
					tooth_number[j].parentNode.parentNode.removeChild(tooth_number[j].parentNode);
				}
			}

			var div = document.getElementById("dental_chart_image");
			var markerDivs = getElementsByName(div, "dental_chart_marker_div");
			var templateDiv = markerDivs[markerDivs.length-1];
			var clonedDiv = templateDiv.cloneNode(true);
			clonedDiv.style.display = '';
			div.insertBefore(clonedDiv, templateDiv);

			var url = cpath + "/DentalConsultation/Consultation.do?_method=getDentalChartMarkerImage";
			url += "&dc_unv_number="+document.getElementById('hid_dc_tooth_number').value;
			url += "&dc_tooth_part="+document.getElementById('hid_dc_tooth_part').value;
			url += "&dc_status_id="+tooth_status_obj[i].value;
			url += "&mr_no="+document.getElementById('mr_no').value;

			var pos_x = document.getElementById('hid_dc_pos_x').value;
			var pos_y = document.getElementById('hid_dc_pos_y').value;
			// when using formname to retrieve the image element, it is not returning in the elements in order. hence
			// used document.getElementsByName() to retrieve the image element.
			var markerImage = document.getElementsByName("dc_marker_image")[markerDivs.length-1];
			markerImage.src = url;
			markerImage.style.left = (parseFloat(pos_x)) + 'px';
			markerImage.style.top = (parseFloat(pos_y)) + 'px';
			markerImage.style.display = 'block';
			markerImage.title = document.getElementById('crown_'+tooth_status_obj[i].value).value;

			document.getElementsByName("dc_tooth_part")[markerDivs.length-1].value = document.getElementById('hid_dc_tooth_part').value; // crown
			document.getElementsByName("dc_status_id")[markerDivs.length-1].value = tooth_status_obj[i].value;
			document.getElementsByName("dc_unv_number")[markerDivs.length-1].value = document.getElementById('hid_dc_tooth_number').value;

			crown_status_dialog.cancel();
			tooth_status_obj[i].checked = false;
			document.getElementById('hid_dc_tooth_number').value = '';
			document.getElementById('hid_dc_tooth_part').value = '';
			document.getElementById('hid_dc_pos_x').value = 0;
			document.getElementById('hid_dc_pos_y').value = 0;
			return ;
		}
	}
	alert("Please select the status.");
	return false;
}

function crownStatusCancel() {
	var tooth_status_obj = document.getElementsByName("crown_status");
	for (var i=0; i<tooth_status_obj.length; i++) {
		tooth_status_obj[i].checked = false;
	}
	document.getElementById('hid_dc_tooth_number').value = '';
	document.getElementById('hid_dc_tooth_part').value = '';
	document.getElementById('hid_dc_pos_x').value = 0;
	document.getElementById('hid_dc_pos_y').value = 0;
	crown_status_dialog.cancel();
}

function initRootStatusDialog() {
	if (!document.getElementById('root_status_div')) return ;

	document.getElementById('root_status_div').style.display = 'block';
	var dialog = new YAHOO.widget.Dialog("root_status_div", {
		width:"300px",
		context :["root_status_div", "bl", "tl"],
		visible:false,
		modal:true,
		constraintoviewport:true
	});
	YAHOO.util.Event.addListener('root_status_ok', 'click', rootStatusUpdate, dialog, true);
	YAHOO.util.Event.addListener('root_status_close', 'click', rootStatusCancel, dialog, true);
	YAHOO.util.Event.addListener('root_status_delete', 'click', deleteRootStatus, dialog, true);
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
			{ fn:rootStatusCancel, scope:dialog, correctScope:true } );
	dialog.cfg.queueProperty("keylisteners", escKeyListener);
	dialog.render();
	root_status_dialog = dialog;
}

function deleteRootStatus() {
	var tooth_number = document.getElementsByName("dc_unv_number");
	var tooth_part = document.getElementsByName("dc_tooth_part");
	for (var j=0;j<tooth_number.length;j++) {
		if (tooth_number[j].value == document.getElementById('hid_dc_tooth_number').value
			&& tooth_part[j].value == document.getElementById('hid_dc_tooth_part').value) {
			tooth_number[j].parentNode.parentNode.removeChild(tooth_number[j].parentNode);
		}
	}
	document.getElementById('hid_dc_tooth_number').value = '';
	document.getElementById('hid_dc_tooth_part').value = '';
	document.getElementById('hid_dc_pos_x').value = 0;
	document.getElementById('hid_dc_pos_y').value = 0;
	var root_status_obj = document.getElementsByName("root_status");
	for (var i=0; i<root_status_obj.length; i++) {
		root_status_obj[i].checked = false;
	}
	root_status_dialog.cancel();
}

function rootStatusUpdate() {
	var root_status_obj = document.getElementsByName("root_status");
	var selected = false;
	for (var i=0; i<root_status_obj.length; i++) {
		if (root_status_obj[i].checked) {

			var tooth_number = document.getElementsByName("dc_unv_number");
			var tooth_part = document.getElementsByName("dc_tooth_part");
			for (var j=0;j<tooth_number.length;j++) {
				if (tooth_number[j].value == document.getElementById('hid_dc_tooth_number').value
					&& tooth_part[j].value == document.getElementById('hid_dc_tooth_part').value) {
					tooth_number[j].parentNode.parentNode.removeChild(tooth_number[j].parentNode);
				}
			}

			var div = document.getElementById("dental_chart_image");
			var markerDivs = getElementsByName(div, "dental_chart_marker_div");
			var templateDiv = markerDivs[markerDivs.length-1];
			var clonedDiv = templateDiv.cloneNode(true);
			clonedDiv.style.display = '';
			div.insertBefore(clonedDiv, templateDiv);

			var url = cpath + "/DentalConsultation/Consultation.do?_method=getDentalChartMarkerImage";
			url += "&dc_unv_number="+document.getElementById('hid_dc_tooth_number').value;
			url += "&dc_tooth_part="+document.getElementById('hid_dc_tooth_part').value;
			url += "&dc_status_id="+root_status_obj[i].value;
			url += "&mr_no="+document.getElementById('mr_no').value;

			var pos_x = document.getElementById('hid_dc_pos_x').value;
			var pos_y = document.getElementById('hid_dc_pos_y').value;

			// when using formname to retrieve the image element, it is not returning in the elements in order. hence
			// used document.getElementsByName() to retrieve the image element.
			var markerImage = document.getElementsByName("dc_marker_image")[markerDivs.length-1];
			markerImage.src = url;
			markerImage.style.left = parseFloat(pos_x) + 'px';
			markerImage.style.top = (parseFloat(pos_y)) + 'px';
			markerImage.style.display = 'block';
			markerImage.title = document.getElementById('root_'+root_status_obj[i].value).value;

			document.getElementsByName("dc_tooth_part")[markerDivs.length-1].value = document.getElementById('hid_dc_tooth_part').value; // root
			document.getElementsByName("dc_status_id")[markerDivs.length-1].value = root_status_obj[i].value;
			document.getElementsByName("dc_unv_number")[markerDivs.length-1].value = document.getElementById('hid_dc_tooth_number').value;

			root_status_dialog.cancel();
			root_status_obj[i].checked = false;
			document.getElementById('hid_dc_tooth_number').value = '';
			document.getElementById('hid_dc_tooth_part').value = '';
			document.getElementById('hid_dc_pos_x').value = 0;
			document.getElementById('hid_dc_pos_y').value = 0;

			return ;
		}
	}
	alert("Please select the status.");
	return false;
}

function rootStatusCancel() {
	var root_status_obj = document.getElementsByName("root_status");
	for (var i=0; i<root_status_obj.length; i++) {
		root_status_obj[i].checked = false;
	}
	document.getElementById('hid_dc_tooth_number').value = '';
	document.getElementById('hid_dc_tooth_part').value = '';
	document.getElementById('hid_dc_pos_x').value = 0;
	document.getElementById('hid_dc_pos_y').value = 0;
	root_status_dialog.cancel();
}

function initSurfaceStatusDialog() {
	if (!document.getElementById('surface_status_div')) return ;

	document.getElementById('surface_status_div').style.display = 'block';
	var dialog = new YAHOO.widget.Dialog("surface_status_div", {
		width:"300px",
		context :["surface_status_div", "bl", "tl"],
		visible:false,
		modal:true,
		constraintoviewport:true
	});
	YAHOO.util.Event.addListener('surface_status_ok', 'click', surfaceStatusUpdate, dialog, true);
	YAHOO.util.Event.addListener('surface_status_close', 'click', surfaceStatusCancel, dialog, true);
	YAHOO.util.Event.addListener('surface_status_delete', 'click', deleteSurfaceStatus, dialog, true);
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
			{ fn:surfaceStatusCancel, scope:dialog, correctScope:true } );
	dialog.cfg.queueProperty("keylisteners", escKeyListener);
	dialog.render();
	surface_status_dialog = dialog;
}

function deleteSurfaceStatus() {
	var tooth_number = document.getElementsByName("dc_unv_number");
	var tooth_part = document.getElementsByName("dc_tooth_part");
	for (var j=0;j<tooth_number.length;j++) {
		if (tooth_number[j].value == document.getElementById('hid_dc_tooth_number').value
			&& tooth_part[j].value == document.getElementById('hid_dc_tooth_part').value) {
			tooth_number[j].parentNode.parentNode.removeChild(tooth_number[j].parentNode);
		}
	}
	document.getElementById("tooth_surface_option").value = '';
	document.getElementById("tooth_surface_material").value = '';
	document.getElementById("tooth_surface_condition").value = '';
	document.getElementById('material_color_div').style.display = 'none';
	document.getElementById('hid_dc_tooth_number').value = '';
	document.getElementById('hid_dc_tooth_part').value = '';
	document.getElementById('hid_dc_pos_x').value = 0;
	document.getElementById('hid_dc_pos_y').value = 0;

	surface_status_dialog.cancel();

}

function surfaceStatusUpdate() {
	var option = document.getElementById("tooth_surface_option");
	var material = document.getElementById("tooth_surface_material");
	var condition = document.getElementById("tooth_surface_condition");

	var material_text = document.getElementById("tooth_surface_material").options[
		document.getElementById('tooth_surface_material').selectedIndex].text;
	var option_text = document.getElementById("tooth_surface_option").options[
		document.getElementById('tooth_surface_option').selectedIndex].text;
	var condition_text = document.getElementById("tooth_surface_condition").options[
		document.getElementById('tooth_surface_condition').selectedIndex].text;
	if (option.value == '' && material.value == '' && condition.value == '') {
		alert("Please select atleast one of the following \n " +
				"1) Option or \n " +
				"2) Material or \n "+
				"3) Condition");
		return false;
	}
	var tooth_number = document.getElementsByName("dc_unv_number");
	var tooth_part = document.getElementsByName("dc_tooth_part");
	for (var j=0;j<tooth_number.length;j++) {
		if (tooth_number[j].value == document.getElementById('hid_dc_tooth_number').value
			&& tooth_part[j].value == document.getElementById('hid_dc_tooth_part').value) {
			tooth_number[j].parentNode.parentNode.removeChild(tooth_number[j].parentNode);
		}
	}

	var div = document.getElementById("dental_chart_image");
	var markerDivs = getElementsByName(div, "dental_chart_marker_div");
	var templateDiv = markerDivs[markerDivs.length-1];
	var clonedDiv = templateDiv.cloneNode(true);
	clonedDiv.style.display = '';
	div.insertBefore(clonedDiv, templateDiv);

	var url = cpath + "/DentalConsultation/Consultation.do?_method=getDentalChartMarkerImage";
	url += "&dc_unv_number="+document.getElementById('hid_dc_tooth_number').value;
	url += "&dc_tooth_part="+document.getElementById('hid_dc_tooth_part').value;
	url += "&dc_material_id="+material.value;
	url += "&mr_no="+document.getElementById('mr_no').value;

	var pos_x = document.getElementById('hid_dc_pos_x').value;
	var pos_y = document.getElementById('hid_dc_pos_y').value;
	// when using formname to retrieve the image element, it is not returning in the elements in order. hence
	// used document.getElementsByName() to retrieve the image element.
	var markerImage = document.getElementsByName("dc_marker_image")[markerDivs.length-1];
	markerImage.src = url;
	markerImage.style.left = (parseFloat(pos_x)) + 'px';
	markerImage.style.top = (parseFloat(pos_y)) + 'px';
	markerImage.style.display = 'block';
	var title = '';
	if (option.value != '')
		title = option_text;
	if (option.value != '' && material.value != '')
		title += "/"
	if (material.value != '')
		title += material_text;
	if (title != '' && condition.value != '')
		title += '/';
	if (condition.value != '')
		title += condition_text;
	markerImage.title = title;

	document.getElementsByName("dc_tooth_part")[markerDivs.length-1].value = document.getElementById('hid_dc_tooth_part').value;
	document.getElementsByName("dc_status_id")[markerDivs.length-1].value = condition.value;
	document.getElementsByName("dc_unv_number")[markerDivs.length-1].value = document.getElementById('hid_dc_tooth_number').value;
	document.getElementsByName("dc_option_id")[markerDivs.length-1].value = option.value;
	document.getElementsByName("dc_material_id")[markerDivs.length-1].value = material.value;

	surface_status_dialog.cancel();
	option.value = '';
	condition.value = '';
	material.value = '';
	document.getElementById('material_color_div').style.display = 'none';
	document.getElementById('hid_dc_tooth_number').value = '';
	document.getElementById('hid_dc_tooth_part').value = '';
	document.getElementById('hid_dc_pos_x').value = 0;
	document.getElementById('hid_dc_pos_y').value = 0;

	return true;
}

function surfaceStatusCancel() {
	document.getElementById("tooth_surface_option").value = '';
	document.getElementById("tooth_surface_material").value = '';
	document.getElementById("tooth_surface_condition").value = '';
	document.getElementById('material_color_div').style.display = 'none';
	document.getElementById('hid_dc_tooth_number').value = '';
	document.getElementById('hid_dc_tooth_part').value = '';
	document.getElementById('hid_dc_pos_x').value = 0;
	document.getElementById('hid_dc_pos_y').value = 0;
	surface_status_dialog.cancel();
}

function displayColor() {
	var material = document.getElementById('tooth_surface_material').value;
	if (material != '') {
		var record = findInList(surface_materials_json, "material_id", material);
		document.getElementById('material_color_div').style.display = 'block';
		document.getElementById('material_color_div').style.backgroundColor = record.color_code;
	} else {
		document.getElementById('material_color_div').style.display = 'none';
	}
}

function updateDentalChartXY(event) {
	var sel_pos_x = 0;
	var sel_pos_y = 0;
	var obj = document.getElementById("dental_chart_image");
	if (event.offsetX) {
		sel_pos_x = event.offsetX;
		sel_pos_y = event.offsetY;
	} else {
		var offsetLeft = 0;
		var offsetTop = 0;
		if (obj.offsetParent) {
			do {
				offsetLeft += obj.offsetLeft;
				offsetTop += obj.offsetTop;
			} while(obj = obj.offsetParent);
		}
		sel_pos_x = event.pageX - offsetLeft;
		sel_pos_y = event.pageY - offsetTop;

	}
	// centering the marker image.
	var tooth_number = -1;
	var toothNumberFDI = -1;
	var status_dialog = null;
	var tooth_part = "";
	var pos_x = 0;
	var pos_y = 0;
	var teeth = adult_tooth_image_details_json.teeth;
	for (var toothNumber in teeth) {
		var tooth = teeth[toothNumber].toothPart;
		toothNumberFDI = teeth[toothNumber].toothNumberFDI;

		for (var toothpart in tooth) {
			var part = tooth[toothpart];
			var left = parseFloat(part.pos_x);
			var right = left + part.width;
			var top = parseFloat(part.pos_y);
			var bottom = top + parseFloat(part.ht);
			if (sel_pos_x >= left && sel_pos_x <= right && sel_pos_y >= top && sel_pos_y <= bottom) {
				pos_x = part.pos_x;
				pos_y = part.pos_y;
				tooth_number = toothNumber;
				tooth_part = toothpart;
				break;
			}
		}
		if (tooth_part != '') {
			break;
		}
	}
	if (!empty(tooth_part)) {
		document.getElementById('hid_dc_tooth_number').value = tooth_number;
		document.getElementById('hid_dc_tooth_part').value = tooth_part;
		document.getElementById('hid_dc_pos_x').value = pos_x;
		document.getElementById('hid_dc_pos_y').value = pos_y;
		var div = document.getElementById("dental_chart_image");

		if (tooth_part == 'crown') {
			var tooth_number_obj = document.getElementsByName("dc_unv_number");
			var tooth_part_obj = document.getElementsByName("dc_tooth_part");
			for (var j=0; j<tooth_number_obj.length; j++) {
				if (tooth_number_obj[j].value == tooth_number
					&& tooth_part_obj[j].value == tooth_part) {
					var statuses = document.getElementsByName('crown_status');
					for (var k=0; k<statuses.length; k++) {
						if (statuses[k].value == document.getElementsByName("dc_status_id")[j].value) {
							statuses[k].checked = true;
							break;
						}
					}
				}
			}

			document.getElementById('crownStatusLabel').textContent = 'Crown "'+ (tooth_numbering_system == 'U' ? tooth_number : toothNumberFDI) + '" Status';
			crown_status_dialog.cfg.setProperty('context', [div, 'tl', 'tl', ["beforeShow", "windowResize"], [sel_pos_x, sel_pos_y]], false);
			crown_status_dialog.show();
		} else if (tooth_part == 'root') {
			var tooth_number_obj = document.getElementsByName("dc_unv_number");
			var tooth_part_obj = document.getElementsByName("dc_tooth_part");
			for (var j=0; j<tooth_number_obj.length; j++) {
				if (tooth_number_obj[j].value == tooth_number
					&& tooth_part_obj[j].value == tooth_part) {
					var statuses = document.getElementsByName('root_status');
					for (var k=0; k<statuses.length; k++) {
						if (statuses[k].value == document.getElementsByName("dc_status_id")[j].value) {
							statuses[k].checked = true;
							break;
						}
					}
				}
			}

			document.getElementById('rootStatusLabel').textContent = 'Root "'+ (tooth_numbering_system == 'U' ? tooth_number : toothNumberFDI) + '" Status';
			root_status_dialog.cfg.setProperty('context', [div, 'tl', 'tl', ["beforeShow", "windowResize"], [sel_pos_x, sel_pos_y]], false);
			root_status_dialog.show();
		} else if (tooth_part == 'left' || tooth_part == 'right' || tooth_part == 'bottom' || tooth_part == 'top' || tooth_part == 'center') {
			var tooth_number_obj = document.getElementsByName("dc_unv_number");
			var tooth_part_obj = document.getElementsByName("dc_tooth_part");
			// first clear values. if tooth part matches then populate the details.
			document.getElementById('tooth_surface_material').value = '';
			document.getElementById('tooth_surface_condition').value = '';
			document.getElementById('tooth_surface_option').value = '';
			for (var j=0; j<tooth_number_obj.length; j++) {
				if (tooth_number_obj[j].value == tooth_number
					&& tooth_part_obj[j].value == tooth_part) {
					document.getElementById('tooth_surface_material').value = document.getElementsByName("dc_material_id")[j].value;
					document.getElementById('tooth_surface_condition').value = document.getElementsByName("dc_status_id")[j].value;
					document.getElementById('tooth_surface_option').value = document.getElementsByName("dc_option_id")[j].value;
					break;
				}
			}
			displayColor();

			document.getElementById('surfaceLabel').textContent = 'Tooth "'+ (tooth_numbering_system == 'U' ? tooth_number : toothNumberFDI) + '" Surface Status';
			surface_status_dialog.cfg.setProperty('context', [div, 'tl', 'tl', ["beforeShow", "windowResize"], [sel_pos_x, sel_pos_y]], false);
			surface_status_dialog.show();
		}
	}
}
