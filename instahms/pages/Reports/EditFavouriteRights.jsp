<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean id="currentDate" class="java.util.Date"/>
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Edit Favourite Report Rights -- Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<insta:link type="script" file="ajax.js"/>
<insta:link type="script" file="date_go.js"/>


<script>
var defaultShowFields = ${rolesWithRights}

function initSelectedFields() {
	var lstFlds = document.inputform.listFields;
	var avlbFlds = document.inputform.avlbListFlds;
	for (var indx = 0; indx < defaultShowFields.length; indx++) {
		moveSelectedOption(avlbFlds, lstFlds, defaultShowFields[indx]);
	}
}


var avlFlds;
var selFlds;

function swapOptions(obj, i, j) {
	var o = obj.options;
	var i_selected = o[i].selected;
	var j_selected = o[j].selected;
	var temp = new Option(o[i].text, o[i].value, o[i].title, o[i].defaultSelected, o[i].selected);
	temp.setAttribute("title", o[i].title);
	var temp2 = new Option(o[j].text, o[j].value, o[j].title, o[j].defaultSelected, o[j].selected);
	temp2.setAttribute("title", o[j].title);

	o[i] = temp2;
	o[j] = temp;
	o[i].selected = j_selected;
	o[j].selected = i_selected;
}

function moveOptionUp(obj) {
	if (!hasOptions(obj)) {
		return;
	}
	for (i = 0; i < obj.options.length; i++) {
		if (obj.options[i].selected) {
			if (i != 0 && !obj.options[i - 1].selected) {
				swapOptions(obj, i, i - 1);
				obj.options[i - 1].selected = true;
			}
		}
	}
}

function moveOptionDown(obj) {
	if (!hasOptions(obj)) {
		return;
	}
	for (i = obj.options.length - 1; i >= 0; i--) {
		if (obj.options[i].selected) {
			if (i != (obj.options.length - 1) && !obj.options[i + 1].selected) {
				swapOptions(obj, i, i + 1);
				obj.options[i + 1].selected = true;
			}
		}
	}
}

function sortSelect(obj) {
	var o = new Array();
	if (!hasOptions(obj)) {
		return;
	}
	for (var i = 0; i < obj.options.length; i++) {
		o[o.length] = new Option(obj.options[i].text, obj.options[i].value, obj.options[i].defaultSelected, obj.options[i].selected);
		(o[i]).title = obj.options[i].title;
		(o[i]).value = obj.options[i].value;

	}
	if (o.length == 0) {
		return;
	}
	o = o.sort(function(val1, val2) {
		if(val2.text+"" == "(Summary Fields)")
			return -1;

		if ((val1.text + "") < (val2.text + "")) {
			return - 1;
		}
		if ((val1.text + "") > (val2.text + "")) {
			return 1;
		}
		return 0;
	});

	for (var i = 0; i < o.length; i++) {
		obj.options[i] = new Option(o[i].text, o[i].defaultSelected, o[i].selected);
		obj.options[i].title = o[i].title;
		obj.options[i].value = o[i].value;
	}
}

function createListElements(from, to) {
	avlFlds = document.getElementById(from);
	selFlds = document.getElementById(to);
}

function hasOptions(obj) {
	if (obj != null && obj.options != null) {
		return true;
	}
	return false;
}

/*
 * Move a single named field (no need to mark as selected)
 * from one list to another
 */
function moveSelectedOption(fromList, toList, fieldName) {
	if (!hasOptions(fromList)) {
		return;
	}
	for (var i = 0; i < fromList.options.length; i++) {
		var o = fromList.options[i];
		if (o.value == fieldName) {
			if (!hasOptions(toList)) {
				var index = 0;
			} else {
				var index = toList.options.length;
			}
			toList.options[index] = new Option(o.text, o.value, o.title, false, false);
			toList.options[index].setAttribute("title", o.title);

			// Delete the selected options from  the available list.
			fromList.options[i] = null;
			break;
		}
	}

	// Only the 'toList' may need sorting
	if (toList.id=='avlbListFlds' || toList.id=='avlbSummFlds') {
		sortSelect(toList);
	}

	fromList.selectedIndex = -1;
	toList.selectedIndex = -1;
}

/*
 * Move all fields in the from list marked as selected to the to list
 */
function moveSelectedOptions(from, to, sort) {

	if (!hasOptions(from)) {
		return;
	}
	for (var i = 0; i < from.options.length; i++) {
		var o = from.options[i];
		if (o.selected) {
			if (!hasOptions(to)) {
				var index = 0;
			} else {
				var index = to.options.length;
			}
			to.options[index] = new Option(o.text, o.value, o.title, false, false);
			to.options[index].setAttribute("title", o.title);
		}
	}
	// Delete the selected options from  the available list.
	for (var i = (from.options.length - 1); i >= 0; i--) {
		var o = from.options[i];
		if (o.selected) {
			from.options[i] = null;
		}
	}
	//********If needed, the fields in the list can be sorted after addition or deletion.******
	if(from.id=='avlbListFlds' || from.id=='avlbSummFlds'){
		sortSelect(from);
	}else if(to.id=='avlbListFlds' || to.id=='avlbSummFlds'){
		sortSelect(to);
	}
	from.selectedIndex = -1;
	to.selectedIndex = -1;
}

function addListFields() {
	createListElements('avlbListFlds', 'listFields');
	moveSelectedOptions(avlFlds, selFlds, 'from');
}

function removeListFields() {
	createListElements('avlbListFlds', 'listFields');
	moveSelectedOptions(selFlds, avlFlds);
}


function disableSortFields(){
	document.getElementById("customOrder1").selectedIndex = 0;
	document.getElementById("customOrder1").disabled = true;
	document.getElementById("sort1").disabled = true;
	document.getElementById("customOrder2").selectedIndex = 0;
	document.getElementById("customOrder2").disabled = true;
	document.getElementById("sort2").disabled = true;
}

function enableSortFields(){
	document.getElementById("customOrder1").removeAttribute("disabled");
	document.getElementById("sort1").removeAttribute("disabled");
	document.getElementById("customOrder2").removeAttribute("disabled");
	document.getElementById("sort2").removeAttribute("disabled");
}


function onSave(){
	for (var indx = 0; indx < document.inputform.listFields.length; indx++) {
		document.inputform.listFields[indx].selected = true;
	}
	document.inputform.submit();
}

</script>
</head>

<body onload="initSelectedFields();" class="yui-skin-sam">
<form name="inputform" action="FavouriteReportsDashboard.do" >
<input type="hidden" name="favourite_report_id" id="favourite_report_id" value= "${reportBean.map.report_id}" />
<input type="hidden" name="report_group" id="report_group" value= "${reportBean.map.report_group}" />
<input type="hidden" name="_method" id="_method" value= "saveReportRights" />
<h1>Edit Report Rights</h1>
	<table  width="100%">
		<tr>
			<td>
				<fieldset class="fieldSetBorder" >
					<table class="formtable" width="100%">
						<tr>
							<td class="formLabel">
								Report Name :
							</td>
							<td class="forminfo">
								${reportBean.map.report_title}

							</td>
							<td  class="formLabel">
								Report Type :
							</td>
							<td class="forminfo">
								${reportBean.map.parent_report_name}
							</td>
							<td  class="formLabel">
								Report Group :
							</td>
							<td class="forminfo">
								${reportBean.map.report_group}
							</td>
						<tr>
							<td class="formLabel">
								Created By :
							</td>
							<td class="forminfo">
								${reportBean.map.user_name}
							</td>
							<td class="formLabel">
								Created Date :
							</td>
							<td class="forminfo">
								<fmt:formatDate value="${reportBean.map.created_date}" pattern="dd-MM-yyyy"/>
							</td>
							<td class="formLabel">
								Period :
							</td>
							<td class="forminfo">
								${period}
							</td>
						</tr>
						<tr>
							<td class="formLabel">Allow Date Change :</td>
							<td class="forminfo"><input type="checkbox" name="allow_date_change" id="aloow_date_change"
								${reportBean.map.allow_date_change ? 'checked':''} /></td>
						</tr>
					</table>
				</fieldset>
			</td>
		</tr>
		<tr>
			<td>
				<br/>

				<fieldset class="fieldSetBorder"  align="center">
					<legend><b>Access Rights</b></legend>
					<br/>
					<table align="center" width="342" style="padding-right: 5px; padding-left: 50px; border-width: 0px; margin: 0px; padding-bottom: 20px;" >
							<tr>
								<td align="center" style="padding-right: 4pt; border-width:0px; margin:0px; width:134px;">
									Available Roles
									<br />
									<br />
									<select size="15" style="width:12em;padding-left:5;color:#666666;" multiple name="avlbListFlds" id="avlbListFlds" onDblClick="moveSelectedOptions(this,this.form.listFields);">
										<c:forEach var="fieldName" items="${roles}" varStatus="i">
											<c:if test="${fieldName['role_id'] ne 1 && fieldName['role_id'] ne 2}">
												<option value="${fieldName['role_id']}" title="${fieldName['role_name']}">${fieldName["role_name"]}</option>
											</c:if>
										</c:forEach>
									</select>
								</td>
								<td valign="top" align="left" style="padding-right:0;">
									<br/><br/>
									<input type="button" name="addLstFldsButton" value=">"  title="ADD >" onclick="addListFields();"/>
								</td>
								<td valign="top" align="center" style="width:134px;padding-left:4pt;">
									Selected Roles
									<br />
									<br />
									<select name="listFields" size="15" style="width:12em;padding-left:5; color:#666666;"  id="listFields" multiple onDblClick="moveSelectedOptions(this,this.form.avlbListFlds);">
									</select>
								</td>
								<td>
									<div align="center">
										<button type="button" style="border-width:thin;border-style:none; background-color:#FFFFFF;" onclick="moveOptionUp(listFields);"> <img src="${cpath}/icons/std_up.png" width=10 height=8/> </button>
										<br />
										<br />
										<button type="button" style="border-width:thin;border-style:none; background-color:#FFFFFF;" onclick="moveOptionDown(listFields);"><img src="${cpath}/icons/std_down.png" width=10 height=8/></button>
										<br />
										<br />
										<button type="button" style="border-width:thin;border-style:none; background-color:#FFFFFF;" onclick="removeListFields();"><img src="${cpath}/icons/std_cancel.png"  height="8"  width="10"/></button>
										<br /><br />
										<br /><br />
										<br /><br />
										<br /><br />
										<br /><br />
									</div>
								</td>
							</tr>
						</table>
				</fieldset>
			</td>
		</tr>
	</table>
	<div class="screenActions">
		<table align="left">
			<tr>
				<td><input type="button" name="save_button" value="Save" onclick="onSave();"/> &nbsp;</td>
				<td>
					<insta:screenlink target="_parent" screenId="fav_rpt_dashbd"
						extraParam="?_method=getReport&report_group=${reportBean.map.report_group}&report_group%40op=eq&sortOrder=report_title"
						label="Favourite Report DashBoard" addPipe="true"/>
				</td>
			</tr>
		</table>
	</div>

</form>
</body>
</html>