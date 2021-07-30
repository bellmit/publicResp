<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page import="org.apache.struts.Globals"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<c:set var="storesList" value="${ifn:listAll('stores','dept_name')}"/>
<c:set var="storesJSONList" value="${ifn:JSONlist('stores')}"/>
<c:set var="template1">
<insta:ltext key="salesissues.raisepatientindent.addshow.template1"/>
</c:set>
<c:set var="template2">
<insta:ltext key="salesissues.raisepatientindent.addshow.template2"/>
</c:set>
<c:set var="template3">
<insta:ltext key="salesissues.raisepatientindent.addshow.template3"/>
</c:set>
<c:set var="returnText">
<insta:ltext key="salesissues.raisepatientindent.addshow.return"/>
</c:set>
<c:set var="editpatientindent">
<insta:ltext key="salesissues.raisepatientindent.addshow.editpatientindent"/>
</c:set>
<c:set var="editpatientreturnindent">
<insta:ltext key="salesissues.raisepatientindent.addshow.editpatientreturnindent"/>
</c:set>
<c:set var="raisepatientindent">
<insta:ltext key="salesissues.raisepatientindent.addshow.raisepatientindent"/>
</c:set>
<c:set var="raisepatientreturnindent">
<insta:ltext key="salesissues.raisepatientindent.addshow.raisepatientreturnindent"/>
</c:set>
<c:set var="returnindent">
<insta:ltext key="salesissues.raisepatientindent.addshow.returnindent"/>
</c:set>
<c:set var="indent">
<insta:ltext key="salesissues.raisepatientindent.addshow.indent"/>
</c:set>
<c:set var="notValidUser" scope="request" value="${genPrefs.max_centers_inc_default > 1 && centerId == 0 && param._method == 'addshow'}"/>
<c:if test="${notValidUser}">
	<c:set var="error" scope="request" value="${template2}"/>
</c:if>
<c:set var="textColor" value="${returns ? 'red' : '' }"/>

<c:set var="notValidUser" scope="request" value="${genPrefs.max_centers_inc_default > 1 && centerId == 0 && param._method == 'show'}"/>
<c:if test="${notValidUser}">
	<c:set var="error" scope="request" value="${template3}"/>
</c:if>

<c:set var="storeExists" value="false"/>
<c:forEach items="${storesList}" var="store">${store.map.auto_fill_indents }
	<c:if test="${ store.map.status == 'A' && store.map.center_id == centerId && store.map.auto_fill_indents}">
		<c:set var="storeExists" value="true"/>
	</c:if>
</c:forEach>

<c:if test="${!storeExists}">
	<c:set var="error" scope="request" value="${template1}"/>
</c:if>

<c:set var="showAvblQty" value="${actionRightsMap.show_avbl_qty eq 'A' or roleId eq '1' or roleId eq '2'}"/>
<c:set var="allowDecimalsForQty" value='<%=GenericPreferencesDAO.getAllPrefs().get("allow_decimals_in_qty_for_issues")%>' />
<c:set var="gStockNegative" value='<%= GenericPreferencesDAO.getAllPrefs().get("stock_negative_sale") %>' />
<c:set var="includeZeros" value="${gStockNegative != 'D' ? 'Y' : 'N'}"/>
<html>
<head>

	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="stores/patient_indent.js"/>
	<insta:link type="js" file="stores/storeMedicinesAjax.js" />

	<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
	<jsp:useBean id="currentDate" class="java.util.Date"/>
	<style type="">
		td.return {color: red;}
	</style>
	<script type="text/javascript">

var cpath = '${cpath}';
var isReturn = ${returns};
var visitSelected = ${param.patient_id != null};
var patIndentNoSelected = ${param.patient_indent_no != null};
var itemDispenseStatuseditable = ${indentMain.map.status == 'F'  && indentMain.map.dispense_status != 'C'};
var indentStatus = '${indentMain.map.status}';
var validUser = ${!(genPrefs.max_centers_inc_default > 1 && centerId == 0 && ( param._method == 'show' || param._method == 'addshow' ))};
var indentDispenseStatus = '${indentMain.map.dispense_status}';
var storeExists = ${storeExists};
var showAvblQty = ${showAvblQty};
var allowDecimalsForQty = '${allowDecimalsForQty}';
var method = '${ifn:cleanJavaScript(param._method)}';
var storesJsonList = ${storesJSONList};
var cpath = '${cpath}';
var stop_doctor_orders = ${empty param.stop_doctor_orders ? false : param.stop_doctor_orders};
var gStockNegative = '${gStockNegative}';
var genericNames = JSON.parse('${ifn:cleanJavaScript(genericNames)}');

<c:if test="${returns}">
var returnIndentableItems = ${returnIndentableItems};
var returnIndentableBatchItems = ${returnIndentableBatchItems};
</c:if>
	var storeMedicineAjaxUrlParamQueryStr = '&hosp=${ifn:cleanURL(sesHospitalId)}&includeZeroStock=${includeZeros}&includeConsignmentStock=Y&includeUnapprovedStock=Y&issueType=CR';
	var doctorDetails = ${doctorDetails};
	var jMedicineNames = null;
	</script>


	<%-- item master: when using show all items --%>
	<script src="${cpath}/pages/stores/getItemMaster.do?ts=${master_timestamp}&hosp=${ifn:cleanURL(sesHospitalId)}&billable=Y&issueType=CR"></script>
	
	<c:if test="${titlePrefix == 'Edit'}">
	<title>${returns ? editpatientreturnindent : editpatientindent }</title>
	</c:if>
	<c:if test="${titlePrefix == 'Raise'}">
	<title>${returns ? raisepatientreturnindent : raisepatientindent }</title>
	</c:if>

	<insta:link type="js" file="ajax.js" />
	<insta:js-bundle prefix="sales.issues"/>
	<insta:js-bundle prefix="registration.patient"/>
</head>

<body class="yui-skin-sam">
<c:set var="status">
<insta:ltext key="salesissues.raisepatientindent.addshow.open"/>,
<insta:ltext key="salesissues.raisepatientindent.addshow.finalized"/>,
<insta:ltext key="salesissues.raisepatientindent.addshow.cancelled"/>
</c:set>
<c:set var="priority">
<insta:ltext key="salesissues.raisepatientindent.addshow.normal"/>,
<insta:ltext key="salesissues.raisepatientindent.addshow.urgent"/>
</c:set>
<c:set var="dispensestatus">
<insta:ltext key="salesissues.raisepatientindent.addshow.open"/>,
<insta:ltext key="salesissues.raisepatientindent.addshow.closed"/>
</c:set>
<c:set var="showitems">
<insta:ltext key="salesissues.raisepatientindent.addshow.iteminstores"/>,
<insta:ltext key="salesissues.raisepatientindent.addshow.allitems"/>
</c:set>
<c:set var="patientindentaddreturn">
<insta:ltext key="salesissues.raisepatientindent.addshow.patientindentaddreturn"/>
</c:set>
<c:set var="patientindentadd">
<insta:ltext key="salesissues.raisepatientindent.addshow.patientindentadd"/>
</c:set>
<c:set var="patientIndent">
<insta:ltext key="salesissues.patientindents.list.patientindents"/>
</c:set>
<c:set var="raiseText">
<insta:ltext key="salesissues.raisepatientindent.addshow.raiseText"/>
</c:set>
<c:set var="saveText">
<insta:ltext key="salesissues.raisepatientindent.addshow.saveText"/>
</c:set>
<c:set var="okText">
 <insta:ltext key="salesissues.sales.details.okText"/>
</c:set>
<c:set var="cancelText">
 <insta:ltext key="salesissues.sales.details.cancelText"/>
</c:set>
<c:set var="searchText">
 <insta:ltext key="salesissues.sales.details.searchText"/>
</c:set>
<c:set var="clearText">
 <insta:ltext key="salesissues.sales.details.clearText"/>
</c:set>
<h1 style="float: left; color: ${textColor };">${ifn:cleanHtml(titlePrefix)}&nbsp;<insta:ltext key="salesissues.raisepatientindent.addshow.patient"/>&nbsp;${returns ? returnindent : indent }&nbsp;${ifn:cleanHtml(param.patient_indent_no)}&nbsp;&nbsp;<c:if test="${titlePrefix == 'Edit'}"><a href="${cpath}/stores/PatientIndentAdd.do?_method=getIndentPrint&patient_indent_no=${ifn:cleanURL(param.patient_indent_no)}" target="_" title='<insta:ltext key="salesissues.raisepatientindent.addshow.indent.print.details"/>'><img src='${cpath}/icons/Report.png' alt='Print'></a></c:if></h1>
<c:if test="${titlePrefix == raiseText}">
	<insta:patientsearch searchType="visit" searchUrl="${returns ? patientindentaddreturn : patientindentadd}.do" buttonLabel="Find"
		searchMethod="addshow" fieldName="patient_id" activeOnly="true"/>
</c:if>
<div style="clear: both"/>

<insta:feedback-panel/>
<insta:patientdetails patient="${patient}" showClinicalInfo="false"/>
<form name="patientIndentForm" method="post" >
<input type="hidden" name="_method" value="${param.patient_indent_no == null ? 'create' : 'update' }"/>
<input type="hidden" name="indent_type" value="${returns ? 'R' :'I' }"/>
<input type="hidden" id="dialogId" value=""/>


<input type="hidden" name="visit_id" value="${patient.patient_id}"/>
<fmt:formatDate var="expectedDt" value="${indentMain == null ? currentDate : indentMain.map.expected_date}" pattern="dd-MM-yyyy"/>
<fmt:formatDate var="expectedTm" value="${indentMain == null ? currentDate : indentMain.map.expected_date}" pattern="HH:mm"/>

<div id="genericSearchDialog" style="display: none;" >
	<div class="bd">
		<fieldset class="fieldsetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="salesissues.sales.details.searchitemusinggenericname"/></legend>
			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.name"/> :</td>
					<td valign="top" >
						<div id="medicine_name_wrapper" >
							<input type="text" name="medicinename" id="medicinename"  class="field" style="width:245px"/>
							<div id="medicinename_dropdown" style="width:245px"></div>
						</div>
					</td>
				</tr>

				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.generic"/>&nbsp;<insta:ltext key="salesissues.sales.details.name"/>:</td>
					<td valign="top">
						<div id="generic_name_wrapper" >
							<input type="text" name="generic_name" id="generic_name" class="field" style="width:245px"/>
							<div id="generic_name_dropdown" style="width:245px"></div>
						</div>
					</td>
				</tr>
				<tr height="10px"></tr>
				<tr height="10px">
					<td colspan="2" align="right">
						<input type="button" name="" value="${searchText}" onclick="searchMedicine();"/>
						<input type="button" name="" value="${clearText}" onclick="clearFeilds();"/>
					</td>
				</tr>
			</table>
			<div id="resultsDiv" align="right">
				<select name="results" id="results" multiple="multiple" style="width:30em;height: 10em" class="listbox"></select>
			</div>
		</fieldset>
		<table>
			<tr>
				<td><input type="button" value="${okText}" onclick="setMedicine();"/></td>
				<td><input type="button" value="${cancelText}" onclick="closeGenericSearchDialog()"/></td>
			</tr>
		</table>
	</div>
</div>

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="salesissues.raisepatientindent.addshow.indentdetails"/> </legend>
	<table class="formtable">
		<tr>
			<td class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.raisedby"/>:</td>
			<td class="forminfo">
				<c:out value="${titlePrefix == raiseText ? userid : indentMain.map.username}"/>
			</td>
			<td class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.indentstore"/>:</td>
			<td class="forminfo">
				<select name="indent_store" class="dropdown" onchange="onChangeStore();">
					<c:forEach items="${storesList}" var="store">${store.map.auto_fill_indents }
						<c:if test="${ store.map.status == 'A' && store.map.center_id == centerId && store.map.auto_fill_indents}">
							<option value="${store.map.dept_id }"
								${(indentMain.map.indent_store!= null ? indentMain.map.indent_store : defaultStore) == store.map.dept_id ? 'selected' : '' }
							>${store.map.dept_name }</option>
						</c:if>
					</c:forEach>
				</select>
			</td>
			<td class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.status"/>:</td>
			<td>
				<insta:selectoptions name="status" value="${indentMain.map.status}"
				 		opvalues="O,F,C" optexts="${status}"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.expecteddate"/>:</td>
			<td>
				<insta:datewidget name="expected_date_dt" btnPos="left" value="${expectedDt }" />
				<input type="text" class="timefield" size="4" name="expected_date_tm" value="${expectedTm }" />
			</td>
			<td class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.remarks"/>:</td>
			<td>
				<input type="text" name="remarks" value="${indentMain.map.remarks }"/>
			</td>
			<td class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.dispensestatus"/>:</td>
			<td class="forminfo">${indentMain.map.dispense_status eq 'O' ? 'Open' : indentMain.map.dispense_status eq 'P' ? 'Partial' : indentMain.map.dispense_status eq 'C' ? 'Closed' : ''}</td>
		</tr>
		<c:if test="${!returns}">
			<tr>
				<td class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.priority"/>:</td>
				<td>
					<insta:selectoptions name="priority" value="${indentMain.map.priority}"
					 		opvalues="N,U" optexts="${priority}"/>
				</td>
				<td class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.prescribing.doctor"/>:</td>
				<td class="yui-skin-sam" valign="top" >
					<div style="width: 138px">
						<input type="text" id="prescribing_doctor_name" name="prescribing_doctor_name" value="${indentMain.map.prescribing_doctor_name}"/>
						<div id="prescribing_doctor_name_drop_down" style="width: 250px"></div>
					</div>
				</td>
			</tr>
		</c:if>
	</table>

</fieldset>

<div class="resultlist" style="margin-bottom: 5px">
	<table id="indentItemListTab" class="detailList"  cellpadding="0" cellspacing="0" width="100%">
	<tr>
		<th style="width: 400px"><insta:ltext key="salesissues.raisepatientindent.addshow.item"/></th>

		<c:if test="${returns}">
			<th><insta:ltext key="salesissues.raisepatientindent.addshow.batch"/></th>
			<th><insta:ltext key="salesissues.raisepatientindent.addshow.batchexp"/></th>
		</c:if>

		<th><insta:ltext key="salesissues.raisepatientindent.addshow.drugtype"/></th>
		<th><insta:ltext key="salesissues.raisepatientindent.addshow.drugcode"/></th>
		<th class="number"><insta:ltext key="salesissues.raisepatientindent.addshow.req.qty"/></th>
		<th class="number"><insta:ltext key="salesissues.raisepatientindent.addshow.recd.qty"/></th>
		<c:if test="${actionRightsMap.show_avbl_qty eq 'A' or roleId eq '1' or roleId eq '2'}">
			<th class="number"><insta:ltext key="salesissues.raisepatientindent.addshow.avbl.qty"/></th>
		</c:if>
		<th><insta:ltext key="salesissues.raisepatientindent.addshow.uom"/></th>
		<th class="number"><insta:ltext key="salesissues.raisepatientindent.addshow.pkgsize"/></th>
		<th style="width:24px"></th>		<%-- edit icon --%>
		<th style="width:24px"></th>		<%-- trash icon --%>
	</tr>
	<c:set var="numCharges" value="${fn:length(indentDetails)}"/>
	<c:set var="itemClass" value="${returns ? 'return' : ''}"/>
	<c:forEach begin="1" end="${numCharges+1}" var="i" varStatus="loop">
		<c:set var="item" value="${indentDetails[i-1].map}"/>

		<c:set var="qtyReqDisplay"
					value="${item.qty_unit == 'I' ? item.qty_required : (item.qty_required/item.issue_base_unit) }"/>
		<c:set var="qtyRecDisplay"
					value="${item.qty_unit == 'I' ? item.qty_received : (item.qty_received/item.issue_base_unit) }"/>
		<c:set var="qtyAvblDisplay"
					value="${item.qty_unit == 'I' ? item.qty_avbl : (item.qty_avbl/item.issue_base_unit) }"/>
		<c:set var="qtyUnitDisplay"
					value="${item.qty_unit == 'I' ? item.issue_units : item.package_uom}"/>
		<c:set var="itemDispenseStatusOnload" value="${item.item_dispense_status}"/>

		<c:set var="flagColor">
			<c:choose>
				<c:when test="${item.item_dispense_status == 'C'}">blue</c:when>
				<c:otherwise>empty</c:otherwise>
			</c:choose>
		</c:set>
		<fmt:formatDate var="expiryDt" value="${item.exp_dt}" pattern="MMM-yyyy"/>
		<tr style="${empty item ? 'display:none' : ''}">
			<td class="${itemClass }"><img src="${cpath}/images/${flagColor}_flag.gif" id="stFlag${i}" />
				<insta:truncLabel value="${item.medicine_name  }" length="50"/>
				<input type="hidden" name="medicine_id" value="${item.medicine_id  }"/>
				<input type="hidden" name="medicine_name" value="${item.medicine_name }"/>
				<input type="hidden" name="patient_indent_no" value="${item.patient_indent_no }"/>
				<input type="hidden" name="indent_type" value="${empty item ? 'I' : item.indent_type }"/>
				<input type="hidden" name="indent_item_id" value="${item.indent_item_id }"/>
				<input type="hidden" name="sale_item_id" value="${item.sale_item_id }"/>
				<input type="hidden" name="item_issue_no" value="${item.item_issue_no }"/>
				<input type="hidden" name="username" value="${ifn:cleanHtmlAttribute(titlePrefix == raiseText ? userid : indentMain.map.username)}"/>
				<input type="hidden" name="qty_required" value="${item.qty_required }"/>
				<input type="hidden" name="qty_required_display" value="${qtyReqDisplay}"/>
				<input type="hidden" name="qty_received" value="${item.qty_received }"/>
				<input type="hidden" name="qty_received_display" value="${qtyRecDisplay}"/>
				<input type="hidden" name="qty_avbl" value="${item.qty_avbl }"/>
				<input type="hidden" name="qty_avbl_display" value="${qtyAvblDisplay}"/>
				<input type="hidden" name="qty_unit" value="${item.qty_unit }"/>
				<input type="hidden" name="qty_unit_display" value="${qtyUnitDisplay}"/>
				<input type="hidden" name="itemDispenseStatusOnload" value="${itemDispenseStatusOnload}"/>

				<input type="hidden" name="code_type" value="${item.code_type}"/>
				<input type="hidden" name="item_code" value="${item.item_code}"/>
				<input type="hidden" name="category" value="${item.category }"/>
				<input type="hidden" name="manf_name" value="${item.orig_manf_name }"/>
				<input type="hidden" name="package_type" value="${item.package_type }"/>
				<input type="hidden" name="issue_base_unit" value="${item.issue_base_unit }"/>
				<input type="hidden" name="issue_units" value="${item.issue_units }"/>
				<input type="hidden" name="package_uom" value="${item.package_uom }"/>
				<input type="hidden" name="dispense_status" value="${item.item_dispense_status }"/>
				<input type="hidden" name="deleted" value=""/>
				<input type="hidden" name="process_type" value="${item.process_type }" />
				<input type="hidden" name="editable" value="${ (item == null || item.dispense_status == 'O') && (param._method == 'addshow' || ( param._method == 'show' && indentMain.map.status == 'O')) }"/>
				<input type="hidden" name="batch_no" value="${item.batch_no}"/>
				<input type="hidden" name="item_batch_id" value="${item.item_batch_id}"/>
				<input type="hidden" name="exp_dt" value="${expiryDt}"/>

			</td>
			<c:if test="${returns}">
				<td>
					<label>${item.batch_no}</label>
				</td>
				<td>
					<label>${expiryDt}</label>
				</td>
			</c:if>
			<td>
				<label>${item.code_type}</label>
			</td>
			<td>
				<label>${item.item_code}</label>
			</td>
			<td class="number">
				<label>${qtyReqDisplay}</label>
			</td>
			<td class="number">
				<label>${qtyRecDisplay}</label>
			</td>
			<c:if test="${showAvblQty}">
				<td class="number">${qtyAvblDisplay}</td>
			</c:if>
			<td>${item.qty_unit == 'I' ? item.issue_units : item.package_uom }</td>
			<td class="number">${item.issue_base_unit }</td>
			<td>
				<c:if test="${empty indentMain || ( indentMain.map.dispense_status != 'C' && indentMain.map.status == 'O' )}">
					<a href="javascript:void(0)" title='<insta:ltext key="salesissues.raisepatientindent.addshow.cancelindent"/>' >
						<img src="${cpath}/icons/delete.gif" class="button" onclick="deleteIndentItem(this)"/>
					</a>
				</c:if>
			</td>
			<td>
				<a href="javascript:void(0)" name="btnEditCharges" id="btnEditCharges${i}"
					title='<insta:ltext key="salesissues.raisepatientindent.addshow.editindentdetails"/>' onclick="showEditDialog(this);">
					<img src="${cpath}/icons/Edit.png" class="button" />
				</a>
			</td>
		</tr>
	</c:forEach>
</table>
</div>
<table width="100%" class="addButton" style="visibility: ${!storeExists || param._method == 'view' || (param._method == 'show' && indentMain.map.status != 'O') ? 'hidden' : 'visible'}">
	<tr>
		<td width="100%"></td>
		<td style="width: 20px">
			<button type="button" name="btnAddItem" id="btnAddItem" title='<insta:ltext key="salesissues.raisepatientindent.addshow.addindent"/>'
				onclick="showAddDialog(this);" accesskey="+" class="imgButton">
			<img src="${cpath}/icons/Add.png">
			</button>
		</td>
	</tr>
</table>

<c:set var="editable" value="${param._method == 'addshow' || ( param._method == 'show' && indentMain.map.status == 'O') }"/>
<input type="button" name="save" id="save" value="${saveText}"  onclick="return raiseIndent()"/>
<insta:screenlink addPipe="true" screenId="stores_patient_indent_list" label="${patientIndent}" extraParam="?_method=list&dispense_status=O&dispense_status=P"/>

</form>

<div class="legend">
	<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
	<div class="flagText"><insta:ltext key="salesissues.raisepatientindent.addshow.closed"/></div>
</div>

<form name="dlgForm">
<div id="addIndentDialog" style="display: none">
<div class="bd">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="salesissues.raisepatientindent.addshow.add.or.edititems"/></legend>
		<table class="formtable" id="addIndentItemsTable">
			<tr style="visibility: ${returns ?'hidden' : 'table-row' }">
				<td class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.showitems"/>:</td>
				<td colspan="3">
					<insta:radio radioText="${showitems}" radioIds="eShowItemsS,eShowItemsA" value="S" radioValues="S,A"
								name="show_items" onclick="initItemAutoComplete();"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.item"/>:</td>
				<td valign="top" colspan="2">
					<div id="item_wrapper" style="width: 20em; padding-bottom:0.2em; ">
		    		<input type="text" name="medicine_name" id="medicine_name" style="width: 20em" maxlength="100"/>
			    	<div id="item_dropdown" class="scrolForContainer"></div>
					</div>
					<input type="hidden" name="medicine_id" id="medicine_id"/>
					<input type="hidden" name="code_type"/>
					<input type="hidden" name="item_code"/>
					<input type="hidden" name="category"/>
					<input type="hidden" name="manf_name"/>
					<input type="hidden" name="package_type"/>
					<input type="hidden" name="qty_avbl"/>
					<input type="hidden" name="qty_avbl_display"/>
					<input type="hidden" name="qty_received_display"/>
					<input type="hidden" name="issue_base_unit"/>
					<input type="hidden" name="issue_units"/>
					<input type="hidden" name="package_uom"/>
					<input type="hidden" name="process_type" />
				</td>
				<td align="left">
					<input type="button" name="searchGen" id = "searchGen" value=".." style="display: ${returns ? 'none' : 'block' }"
					title="Search for Equivalent Drugs by Generic Names" onclick="showSearchWindow();"/>
					<input type="hidden" name="addMedicineId" value=""/>
				</td>
			</tr>
			<c:if test="${returns}">
			<tr>
				<td class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.batchno"/>:</td>
				<%--
				<td valign="top" colspan="3">
					<div id="batch_wrapper" style="width: 15em; padding-bottom:0.2em";>
						<input type="text" name="batch_no" id="batch_no" style="width: 15em; color:${color}">
						<div id="batch_dropdown"></div>
					</div>
					<input type="hidden" name="batch_exp_qty" id="batch_exp_qty"/>
				</td>
				--%>
				<td>
				<select name="batch_no" class="dropdown" id="batch_no" onchange="onChangeBatch();">
				</select>
				<input type="hidden" name="item_batch_id" id="item_batch_id"/>
				<input type="hidden" name="exp_dt" id="exp_dt"/>
				</td>

			</tr>
			</c:if>
			<tr>
				<td class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.drugtype"/>:</td>
					<td id="lbl_code_type" class="forminfo">
					<label></label>
				</td>
				<td class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.drugcode"/>:</td>
					<td id="lbl_item_code" class="forminfo">
					<label></label>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.category"/>:</td>
				<td id="lbl_category" class="forminfo">
					<label></label>
				</td>
				<td class="formlabel" ><insta:ltext key="salesissues.raisepatientindent.addshow.manufacturer"/>:</td>
				<td id="lbl_manf_name" class="forminfo">
					<label></label>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.packagetype"/>:</td>
				<td id="lbl_package_type" class="forminfo"><label></label></td>
				<c:if test="${showAvblQty}">
				<td class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.availableqty"/>:</td>
				<td id="lbl_qty_avbl_display" class="forminfo"><label></label></td>
				</c:if>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.requiredqty"/>:</td>
				<td><input type="text" name="qty_required_display"/><label></label></td>
				<td class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.uom"/>:</td>
				<td>
					<select name="qty_unit" class="dropdown" onchange="onChangeUOM()"></select>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.receivedqty"/>:</td>
				<td id="lbl_qty_received_display" class="forminfo"><label></label></td>
				<td class="formlabel" id="dispense_status_label_td" style="visibility: hidden"><insta:ltext key="salesissues.raisepatientindent.addshow.dispensestatus"/>:</td>
				<td style="visibility: hidden" id="dispense_status_td">
					<insta:selectoptions name="dispense_status" value=""
				 		opvalues="O,C" optexts="${dispensestatus}"/>
				</td>
			</tr>
		</table>
	</fieldset>

	<table>
		<input type="button" name="dialogAdd" value="Add" onclick="onDialogSave()"/>
		<input type="button" name="dialogX" value="Cancel" onclick="closeDialog()"/>
	</table>

</div>
</div>
</form>
</body>
</html>
