<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ page import="java.lang.String"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<%@page import="com.insta.hms.master.FixedAssetMaster.FixedAssetMasterDAO"%>
<%@page import="com.insta.hms.resourcemanagement.MaintenanceScheduleDAO"%>
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Maint. Schedule - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="instadate.js" />

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>

<style>
.scrolForContainer .yui-ac-content{
	 max-height:11em;overflow:auto;overflow-x:hidden; /* scrolling */
    _height:11em; /* ie6 */
}

.yui-ac {
	padding-bottom: 2em;
}
</style>

	<script>
		var assets = <%=FixedAssetMasterDAO.getTableDataInJSON
		(FixedAssetMasterDAO.GET_ACTIVE_ASSETS, (String)request.getSession().getAttribute("userid"),
				(Integer)request.getSession().getAttribute("roleId"),(String)request.getSession().getAttribute("multiStoreAccess"),
				(String)request.getSession().getAttribute("pharmacyStoreId"))%>;
		var maintActivities = <%=StoresDBTablesUtil.getTableDataInJSON(FixedAssetMasterDAO.GET_MAINT_DETAILS)%>;

		var hiddenAssetId = '${bean.map.asset_id}';
		var backupName = '';
		var dateId = "next_maint_date";
		var popupCal;
		var chkAssetName = <%= request.getAttribute("maintSchedules") %>;
		var contractors = ${contractors};
		var contr_name = '${contractor_name}';

		var oAutoComp = null;
		var centerId = '${centerId}';
		function initAssetAutoComplete() {
			var assetNames = []; var issueItemNames = []; var j = 0; var k = 0;
			if (null != assets){
			if(centerId == 0) {
				for (var i=0; i<assets.length; i++ ) {
				 	if (assets[i].ASSET_STATUS == 'A')
				 		assetNames[j++] = assets[i].MEDICINE_NAME+'-'+assets[i].BATCH_NO+'   ('+assets[i].DEPT_NAME+')';
				}
			}else {
				for (var i=0; i<assets.length; i++ ) {
				 	if (assets[i].ASSET_STATUS == 'A' && assets[i].CENTER_ID == centerId)
				 		assetNames[j++] = assets[i].MEDICINE_NAME+'-'+assets[i].BATCH_NO+'   ('+assets[i].DEPT_NAME+')';

				}
			}
		}

			//YAHOO.util.Event.addListener(itemname,"keypress",isEventEnterEscAuto);
			YAHOO.example.ACJSAddArray = new function() {
				var dataSource = null;

				dataSource = new YAHOO.widget.DS_JSArray(assetNames);

				if(oAutoComp != null) {
					oAutoComp.destroy();
				}

				oAutoComp = new YAHOO.widget.AutoComplete('asset_name', 'asset_dropdown', dataSource);
				oAutoComp.maxResultsDisplayed = 50;
				oAutoComp.allowBrowserAutocomplete = false;
				oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
				oAutoComp.typeAhead = false;
				oAutoComp.useShadow = false;
				oAutoComp.minQueryLength = 0;
				oAutoComp.forceSelection = true;
				oAutoComp.filterResults = Insta.queryMatchWordStartsWith;
				oAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
				oAutoComp.itemSelectEvent.subscribe(onSelectAsset);
			}
		}

		function onSelectAsset(){
			document.getElementById("lastMaintDate").textContent  = '';
			document.getElementById("next_maint_date").value= '';

			var assetSel = document.forms[0].asset_name.value;
			var assetId = document.forms[0].asset_id;
			var batchNo = document.forms[0].batch_no;
			for (var i=0; i<assets.length; i++){
				var asset = assets[i]
				if ((null != asset) && (asset.MEDICINE_NAME != '')){
					var assetString = asset.MEDICINE_NAME+'-'+asset.BATCH_NO+'   ('+assets[i].DEPT_NAME+')';
					if (assetString.trim() == assetSel){
						assetId.value = asset.MEDICINE_ID;
						batchNo.value = asset.BATCH_NO;
					}
				}
			}
			var lastmaintDate = null;

			for(var k=0; k< maintActivities.length; k++){
				var activity = maintActivities[k];
				if((null != activity) && (activity.ASSET_ID == assetId.value) && (activity.BATCH_NO == batchNo.value)){
						if(lastmaintDate == null && activity.MAINT_DATE != null && activity.MAINT_DATE != '')
							lastmaintDate = activity.MAINT_DATE;
						if(lastmaintDate < activity.MAINT_DATE && activity.MAINT_DATE != null && activity.MAINT_DATE != '')
							lastmaintDate = activity.MAINT_DATE
				}
			}
			if(lastmaintDate != null) {
				var dateObj = new Date(lastmaintDate);
				document.getElementById("lastMaintDate").textContent = formatDate(dateObj, 'ddmmyyyy', '-');
				document.getElementById("maint_last_date").value = formatDate(dateObj, 'ddmmyyyy', '-');
				setNextMaintDate('select',null, popupCal);
			}
		}

		function keepBackUp(){

			if(document.forms[0].method.value == 'update'){
				backupName = document.forms[0].asset_id.value
			}
			setTimeout("getCalendarObj()", 1000);
			document.forms[0].contractor_name.value =contr_name;
			initAssetAutoComplete();
			initContractorNames();

			document.forms[0].asset_id.value=hiddenAssetId;
		}


		function getCalendarObj(){
			popupCal = calendarObjs[dateId];
		}

		function doClose() {
			window.location.href = "${cpath}/resourcemanagement/MaintenanceSchedule.do?method=list";
		}

	  function checkduplicate(){
	  	var nextMaintDate = document.forms[0].next_maint_date;
		if(document.forms[0].asset_name.value == '' || document.forms[0].asset_name.value == '-'){
	    	alert("Please select Asset Name");
	    	document.forms[0].asset_name.focus();
	     	return false;
	    }
		if(document.forms[0].contractor_name.value == ''){
	    	alert("Please select Contractor Name");
	     	document.forms[0].contractor_name.focus();
	     	return false;
	    }
	    if (nextMaintDate.value == '') {
	     	alert("Please enter the Next Maint Date");
	     	document.forms[0].next_maint_date.focus();
	     	return false;
	    }
	    if (!(doValidateDateField(nextMaintDate))) {
			return false;
	    }
	}

var itAutoComplete = null;

function initContractorNames() {
	if (itAutoComplete != undefined) {
		itAutoComplete.destroy();
	}

  YAHOO.example.itemArray = [];
	var i=0;
	for(var j=0; j<contractors.length; j++)
		{
		   YAHOO.example.itemArray.length = i+1;
			YAHOO.example.itemArray[i] = contractors[j];
			i++;
		}

   YAHOO.example.ACJSArray = new function() {
		datasource = new YAHOO.util.LocalDataSource({result : YAHOO.example.itemArray});
		datasource.reponseType = YAHOO.util.LocalDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : 'result',
			fields : [ 	{key : 'contractor_name'},
						{key : 'contractor_id'}
					]
		};

		itAutoComplete = new YAHOO.widget.AutoComplete('contractor_name','contractor_name_dropdown', datasource);
		itAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
		itAutoComplete.useShadow = true;
		itAutoComplete.minQueryLength = 0;
		itAutoComplete.allowBrowserAutocomplete = false;
		itAutoComplete.resultTypeList = false;
		itAutoComplete.forceSelection = true;
		itAutoComplete.maxResultsDisplayed = 20;


		itAutoComplete.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
			document.forms[0].contractor_name.value = elItem[2].contractor_name;
			document.forms[0].contractor_id.value = elItem[2].contractor_id;
		});
       itAutoComplete.selectionEnforceEvent.subscribe(function(){
			document.forms[0].contractor_name.value = '';
		});
}
}

function setNextMaintDate(type,args,obj) {
			var lastDate = document.getElementById('maint_last_date').value;
			var dateObj;
			if(args == null && lastDate != null) {
				var myarray=lastDate.split("-");

					var d = parseInt(myarray[0],10);
					var m = parseInt(myarray[1],10);
					var y = parseInt(myarray[2],10);

					dateObj = new Date(y, m-1, d);

			}else{
				var dates = args[0];
				var date = dates[0];
				dateObj = new Date(date[0],date[1]-1,date[2]);
			}

			if(lastDate != null && lastDate != '' ) {
				var txtDate = document.getElementById("next_maint_date");

				var frequency = document.getElementById('maint_frequency').value;
				var lastDate = document.getElementById('maint_last_date').value;

				var nextPeriod,num_of_days;

				if(frequency == 'Weekly' ) num_of_days = 7;

				else if(frequency == 'Every two weeks' ) num_of_days = 14;

				else if(frequency == 'Monthly' ) num_of_days = 30;

				else if(frequency == 'Quarterly' ) num_of_days = 90;

				else if(frequency == 'Semi-annually' ) num_of_days = 180;

				else if(frequency == 'Annually' ) num_of_days = 365;

				nextPeriod = new Date(dateObj.getTime() + num_of_days * 24 * 60 * 60 * 1000);

				var fullDate = nextPeriod.getDate();
				if (fullDate < 10){
					fullDate = "0" + fullDate;
				}

				var fullMonth = nextPeriod.getMonth()+1;
				if(fullMonth == 0){
					fullMonth = 12;
				}
				if (fullMonth < 10){
					fullMonth = "0" + fullMonth;
				}

				if(frequency != 'Custom' )
					txtDate.value = fullDate + "-" + fullMonth + "-" + nextPeriod.getFullYear();
				else
					txtDate.value = '';
			}

		}

	</script>

</head>
<body onload="keepBackUp(); setNextMaintDate('select',null, popupCal);">

<form action="MaintenanceSchedule.do" method="POST">
	<input type="hidden" name="method" value="${param.method == 'add' ? 'create' : 'update'}">
	<c:if test="${param.method == 'show'}">
		<input type="hidden" name="maint_id" value="${bean.map.maint_id}"/>
	</c:if>

	<div class="pageHeader">${param.method == 'add' ? 'Add' : 'Edit'} Maintenance Schedule</div>
	<span align="center" class="error">${ifn:cleanHtml(error)}</span>

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Asset</legend>
	<table class="formtable" width="100%">
		<tr>
			<td class="formlabel">Asset:</td>
			<td valign="top">
				<div id="item_wrapper" style="width: 23em; padding-bottom:0.2em; ">
  						<input type="text" name="asset_name" id="asset_name" style="width: 22em" maxlength="100" value="${ifn:cleanHtmlAttribute(param.asset_name)}${empty param.asset_name ? '' : '-' }${ifn:cleanHtmlAttribute(param.batch_no)}" tabindex="1"/>
   					<div id="asset_dropdown"></div>
    				</div>
    				<input type="hidden" id="asset_id" name="asset_id" value="${bean.map.asset_id}"/>
    				<input type="hidden" id="batch_no" name="batch_no" value="${bean.map.batch_no}"/><span style="float: right; margin-left: 240px" class="star">*</span>
			</td>
			<td class="formlabel">Frequency:</td>
			<td>
				<insta:selectoptions name="maint_frequency" id="maint_frequency"
					opvalues="Weekly,Every two weeks,Monthly,Quarterly,Semi-annually,Annually,Custom"
					 optexts="Weekly,Every two weeks,Monthly,Quarterly,Semi-annually,Annually,Custom"
					 value="${bean.map.maint_frequency}"  onChange="setNextMaintDate('select',null, popupCal)" />
			</td>
			<td class="formlabel">&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="formlabel">Last Maintenance Date:</td>
			<td>
				<input type="hidden" name="maint_last_date" id="maint_last_date" value="<fmt:formatDate pattern="dd-MM-yyyy" value="${lastMaintDate}"/>"/>
				<label id="lastMaintDate">
					<fmt:formatDate pattern="dd-MM-yyyy" value="${lastMaintDate}"/>
				</label>
			</td>
			<td class="formlabel">Next Maintenance Date:</td>
			<td><insta:datewidget name="next_maint_date"  valueDate="${bean.map.next_maint_date}" required="true" title="Next Maint Date is required or Date is not valid."/></td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
	</table>
	</fieldset>

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Maintenance Organization</legend>
		<table class="formtable" width="100%">
			<tr>
				<td class="formlabel" >Contractor Name:</td>
				<td valign="top">
					<div id="contractor_name_wrapper" style="width: 15em;padding-bottom: 10px;">
						<input type="text" name="contractor_name" id="contractor_name"  class="field" style="width: 11.5em" />
						<div id="contractor_name_dropdown" ></div>
					</div><span style="margin-left: 140px; display: inline;" class="star">*</span>
					<input type="hidden" name="contractor_id" id="contractor_id" value="${bean.map.contractor_id}" />
				</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
		</table>
	</fieldset>

	<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Hospital Department</legend>
		<table width="100%" class="formtable">
			<tr>
				<td class="formlabel">Name:</td>
				<td>
					<input type="text" name="department" value="${bean.map.department}"   />
				</td>
				<td class="formlabel">Contact:</td>
				<td style="padding-right: 154px">
					<input type="text" name="department_contact" value="${bean.map.department_contact}" onblur="capWords(department_contact);"  />
				</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				</tr>
		</table>
	</fieldset>

		<div class="screenActions">
			<input type="submit" value="Save" onclick="return checkduplicate();"/>
			<a href="#" onclick="doClose();return true;"> | Close</a>
		</div>

</form>

</body>
</html>
