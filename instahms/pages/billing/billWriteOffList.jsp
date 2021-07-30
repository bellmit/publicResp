<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="org.apache.struts.Globals" %>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
	<title><insta:ltext key="billing.billlist.list.patient.writeoff"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />

<script>
var psAc = null;

function init() {
	psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'active', null, null);
	initEditDialog();
	createToolbar(toolbar);
}

function changeStatus() {
	var status = '';
	if (document.getElementById('_mr_no').checked) {
		status = 'active';
	} else {
		status = 'all';
	}
	if (status == 'active') {
		psAc.destroy();
		psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', status, null, null);
	} else {
		psAc.destroy();
		psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', status, null, null);
	}
 }

function ValidateWriteOffAppproval() {
	var checkBox = document.getElementsByName('approve');
	var anyChecked = false;
	for (var i=0; i<checkBox.length; i++) {
		if (!checkBox[i].disabled && checkBox[i].checked) {
			anyChecked = true;
			break;
		}
	}
	if (!anyChecked) {
		alert ("Please select atleast one bill to approve.");
		return false;
	}
	document.BillWriteOffForm._method.value='approveWriteOff';
	document.BillWriteOffForm.submit();
}

var editDialog;

function initEditDialog() {

	var dialogDiv = document.getElementById("editDialog");
	dialogDiv.style.display = 'block';
	editDialog = new YAHOO.widget.Dialog("editDialog",{
			width:"520px",
			context :["resultTable", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleCancelDialog,
	                                                scope:editDialog,
	                                                correctScope:true } );
	editDialog.cfg.queueProperty("keylisteners", escKeyListener);
	YAHOO.util.Event.addListener('editPrevious', 'click', openPrevious, editDialog, true);
	YAHOO.util.Event.addListener('editNext', 'click', openNext, editDialog, true);
	editDialog.render();
}

function handleCancelDialog(){
	editDialog.hide();
}

function openPrevious(id, previous, next) {
	var id = document.getElementById('dialogId').value ;
	id = parseInt(id);
	var table = document.getElementById("resultTable");
	var row = table.rows[id+1];
	if (!setRemarks())
			return false;
	if (id != 0) {
		showEditDialog(document.getElementsByName('editAnchor')[parseInt(id)-1]);
	}
}

function openNext() {
	var id = document.getElementById('dialogId').value ;
	id = parseInt(id);
	var table = document.getElementById("resultTable");
	var row = table.rows[id+1];
	if (!setRemarks())
			return false;
	if (id+1 != document.getElementById('resultTable').rows.length-1) {
		showEditDialog(document.getElementsByName('editAnchor')[parseInt(id)+1]);
	}
}

function showEditDialog(obj){
	var row = getThisRow(obj);
	var index = getThisRow( obj ).rowIndex;
	var id = index-1;
	document.getElementById("dialogId").value = id;
	document.getElementById("eRemarks").value = document.getElementById("writeOffRemarks"+id).value;

	setNodeText("eBillNo", document.getElementById("billNo"+id).value);
	setNodeText("eBillAmt", document.getElementById("billTotalAmt"+id).value);
	setNodeText("ePatAmt", document.getElementById("patDueAmt"+id).value);
	setNodeText("netPatDue", document.getElementById("netPatDueAmt"+id).value);
	setNodeText("patCreditAmt", document.getElementById("patCreditAmt"+id).value);
	editDialog.cfg.setProperty("context",[row.cells[13], "tr", "bl"], false);
	editDialog.show();
}
function setRemarks(){
	var id = parseInt(document.getElementById("dialogId").value);
	var table = document.getElementById("resultTable");
	var row = table.rows[id+1];
	var remarks = document.getElementById("eRemarks").value;
	setIndexedValue("writeOffRemarks",id,remarks);
	setNodeText(row.cells[12], remarks, 20, remarks);
	editDialog.cancel();
	return true;
}
function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(BillWriteOffForm, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function remarksLimit(field,maxlen) {
	if(field.value.length > maxlen){
		while(field.value.length > maxlen){
			field.value=field.value.replace(/.$/,'');
		}
		alert("Write off remarks has been truncated!");
	}
}

var toolbar = {
	Edit: {
		title: "View/Edit Bill",
		imageSrc: "icons/Edit.png",
		href: 'billing/BillAction.do?_method=getCreditBillingCollectScreen',
		onclick: null,
		description: "View and/or Edit the contents of this bill"
	}
};

	</script>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="billWriteOffList" value="${pagedList.dtoList}"/>

<c:set var="hasResults" value="${not empty billWriteOffList}"/>

<body onload="init();">

<c:set var="visittype">
<insta:ltext key="billing.billlist.list.ip"/>,
<insta:ltext key="billing.billlist.list.op"/>,
<insta:ltext key="billing.billlist.list.open"/>,
<insta:ltext key="billing.billlist.list.incomingtest"/>
</c:set>

<c:set var="visitstatus">
<insta:ltext key="billing.billlist.list.active"/>,
<insta:ltext key="billing.billlist.list.inactive"/>
</c:set>

<c:set var="patientWriteOff">
<insta:ltext key="billing.billlist.list.marked.writeoff"/>,
<insta:ltext key="billing.billlist.list.writeoff.approved"/>
</c:set>

<c:set var="optype">
<insta:ltext key="billing.billlist.list.mainvisit"/>,
<insta:ltext key="billing.billlist.list.followup.with.consultation"/>,
<insta:ltext key="billing.billlist.list.followup.without.consultation"/>,
<insta:ltext key="billing.billlist.list.revisit"/>,
<insta:ltext key="billing.billlist.list.outside"/>
</c:set>
<c:set var="mrno">
<insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="visitid">
<insta:ltext key="billing.billlist.list.visitid"/>
</c:set>
<c:set var="billno">
<insta:ltext key="billing.billlist.list.billno"/>
</c:set>

<h1><insta:ltext key="billing.billlist.list.patient.writeoff"/></h1>

<insta:feedback-panel/>

<form name="BillWriteOffSearchForm" method="GET" >
	<input type="hidden" name="_method" value="getBillsWriteOffList">

	<input type="hidden" name="_searchMethod" value="getBillsWriteOffList"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="getBillsWriteOffList" optionsId="optionalFilter" closed="${hasResults}"
		validateFunction="doSearch()" clearFunction="clearSearch">

		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="billing.billlist.list.mrno.or.patientname"/>:</div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<div id="mrnoContainer" style="width: 300px"></div>
					</div>
				</div>
			</div>

			<div class="sboField">
				<div class="sboFieldLabel">&nbsp;
					<div class="sboFieldInput">
						<input type="checkbox" name="_mr_no" id="_mr_no" onclick="changeStatus()"/><insta:ltext key="billing.writeOff.list.activeOnly"/>
					</div>
				</div>
			</div>

			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="billing.billlist.list.billno"/>:</div>
				<div class="sboFieldInput">
					<input type="text" name="bill_no" value="${ifn:cleanHtmlAttribute(param.bill_no)}" />
					<input type="hidden" name="bill_no@op" value="ilike" />
				</div>
			</div>

		</div>


		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
				<td>
					<div class="sfLabel"><insta:ltext key="billing.writeoff.list.writeOffStatus"/>:</div>
					<div class="sfField">
						<insta:checkgroup name="patient_writeoff" selValues="${paramValues.patient_writeoff}"
							opvalues="M,A" optexts="Marked For Writeoff, Writeoff Approved"/>
					</div>
				</td>
				<td>
					<div class="sfLabel"><insta:ltext key="billing.billlist.list.finalizeddate"/></div>
					<div class="sfField">
						<div class="sfFieldSub"><insta:ltext key="billing.billlist.list.from"/>:</div>
							<insta:datewidget name="finalized_date" id="f_date0" value="${paramValues.finalized_date[0]}"/>
							<input type="hidden" name="finalized_date@type" value="date"/>
							<input type="hidden" name="finalized_date@op" value="ge"/>
							<input type="hidden" name="finalized_date@cast" value="y"/>
					</div>
					<div class="sfField">
						<div class="sfFieldSub"><insta:ltext key="billing.billlist.list.to"/>:</div>
						<insta:datewidget name="finalized_date" id="f_date1" value="${paramValues.finalized_date[1]}"/>
						<input type="hidden" name="finalized_date@op" value="le"/>
					</div>
				</td>
				<td>
					<div class="sfLabel"><insta:ltext key="billing.writeoff.list.patientDue"/>&lt;= :</div>
					<div class="sfField">
						<input type="text" name="patient_due" name="patient_due0" value="${paramValues.patient_due[0]}">
						<input type="hidden" name="patient_due@op" value="le,ge"/>
						<input type="hidden" name="patient_due@type" value="numeric"/>
					</div>
					<div class="sfLabel"><insta:ltext key="billing.writeoff.list.patientDue"/>&gt;= :</div>
					<div class="sfField">
						<input type="text" name="patient_due" name="patient_due1" value="${paramValues.patient_due[1]}">
						<input type="hidden" name="patient_due@type" value="numeric"/>
					</div>
				</td>
				</tr>
			</table>
		</div>
	</insta:search>

</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}" showTooltipButton="true"/>

<form name="BillWriteOffForm">
	<input type="hidden" name="_method" value="approveWriteOff">

	<div class="resultList">
		<table class="resultList dialog_displayColumns" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				<th><input type="checkbox" onclick="return checkOrUncheckAll('approve',this)" id="approveAll"/></th>
				<th style="text-align: right">#</th>
				<insta:sortablecolumn name="mr_no" title="${mrno}"/>
				<insta:sortablecolumn name="visit_id" title="${visitid}"/>
				<th><insta:ltext key="ui.label.patient.name"/></th>
				<insta:sortablecolumn name="bill_no" title="${billno}"/>
				<th style="text-align: right;"><insta:ltext key="billing.billlist.list.billamt"/></th>
				<th style="text-align: right;"><insta:ltext key="billing.billlist.list.taxamt"/></th>
				<th style="text-align: right;" title="${sponseramount}"><insta:ltext key="billing.billlist.list.spnramt"/></th>
				<th style="text-align: right;"><insta:ltext key="billing.billlist.list.patamt"/></th>
				<th style="text-align: right;"><insta:ltext key="billing.billlist.list.pattax"/></th>
				<th style="text-align: right;" title="${patientpaidamount}"><insta:ltext key="billing.billlist.list.patpaid"/></th>
				<th style="text-align: right;"><insta:ltext key="billing.billlist.list.patientcreditamt"/></th>
				<th style="text-align: right;" title="${patientdue}"><insta:ltext key="billing.billlist.list.patdue"/></th>

				<th style="text-align: right;"><insta:ltext key="billing.writeoff.list.writtenOffAmt"/></th>
				<th style="text-align: left;"><insta:ltext key="billing.writeoff.list.writeOffRemarks" /></th>
				<th></th>
			</tr>

			<c:forEach var="bill" items="${billWriteOffList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{mrNo: '${bill.mr_no}', billNo: '${bill.bill_no}', visitId: '${bill.visit_id}',
						patient_id: '${bill.visit_id}', bill_type: '${bill.bill_type}', bill_no: '${bill.bill_no}'},[true])";
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
					<td>
						<c:set var="billDueAmt" value ="${bill.total_amount + bill.total_tax - bill.primary_total_claim - bill.secondary_total_claim - bill.total_claim_tax - bill.total_receipts - bill.deposit_set_off - bill.points_redeemed_amt}"/>
						<c:set var="appChkDisable" value="${(bill.patient_writeoff == 'A'  || billDueAmt gt writeOffLimit) ? 'disabled' : ''}"/>
						<c:set var="flagReq" value="${billDueAmt gt writeOffLimit}"/>
						<input type="checkbox" name="approve" id="approve" ${appChkDisable} value="${bill.bill_no}"/>
						<input type="hidden" name="writeOffRemarks" id="writeOffRemarks${st.index}" value="${bill.writeoff_remarks}"/>
						<input type="hidden" name="billNo" id="billNo${st.index}" value="${bill.bill_no}"/>
						<input type="hidden" name="billTotalAmt" id="billTotalAmt${st.index}" value="${bill.total_amount}"/>
						<input type="hidden" name="patDueAmt" id="patDueAmt${st.index}" value="${billDueAmt}"/>
						<input type="hidden" name="patCreditAmt" id="patCreditAmt${st.index}" value="${bill.total_credit_patient}"/>
						<input type="hidden" name="netPatDueAmt" id="netPatDueAmt${st.index}" value="${bill.net_patient_due}"/>
					</td>
					<td style="text-align: right">${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
					<td>
					<c:choose>
						<c:when test="${flagReq}">
							<img class="flag" src="${cpath}/images/grey_flag.gif"/>
						</c:when>
						<c:otherwise>
							<img class="flag" src="${cpath}/images/empty_flag.gif"/>
						</c:otherwise>
					</c:choose>
						${bill.mr_no}
					</td>
					<td>${bill.visit_id}</td>
					<td><insta:truncLabel value="${bill.patient_name}" length="30"/></td>
					<td>${bill.bill_no} </td>
					<td style="text-align: right;">${bill.total_amount}</td>
					<td style="text-align: right;">${bill.total_tax}</td>
					<td style="text-align: right;">${bill.primary_total_claim + bill.secondary_total_claim}</td>
					<td style="text-align: right;">${bill.total_amount - bill.primary_total_claim - bill.secondary_total_claim}</td>
					<td style="text-align: right;">${bill.total_tax - bill.total_claim_tax}</td>
					<td style="text-align: right;">${bill.total_receipts + bill.deposit_set_off + bill.points_redeemed_amt}</td>
					<td style="text-align: right;">${bill.total_credit_patient}</td>
					<td style="text-align: right;">${billDueAmt}</td>
					<td style="text-align: right;">${bill.writtenoff_amt}</td>
					<td><insta:truncLabel value="${bill.writeoff_remarks}" length="20"/>
					</td>
					<td>
						<a name="editAnchor" href="javascript:void(0)" onclick="return showEditDialog(this);" title="Edit Write Off Remarks"/>
							<img src="${cpath}/icons/Edit.png" class="button" style="float: right"/>
						</a>
					</td>
				</tr>
			</c:forEach>
		</table>
		<c:if test="${param._method == 'getBillsWriteOffList'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>
	</div>

	<div style="float: left; margin-top: 10px">
		<button type="button" accesskey="A" onclick="ValidateWriteOffAppproval();">
			<label><b><u><insta:ltext key="billing.writeoff.list.a"/></u></b><insta:ltext key="billing.writeoff.list.pproveWriteoff"/></label>
		</button>
	</div>
	<div class="legend">
 			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Exceeded Write Off Limit</div>
 	</div>
</form>
<form name="editForm">
	<div id="editDialog" style="display:none">
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="billing.writeoff.list.writeOffRemarks" /></legend>
				<table class="formtable" width="100">
					<tr>
						<td class="formlabel">Bill No:</td><td class="forminfo"><label id="eBillNo" style="float: left"></label></td>
						<td class="formlabel">Bill Amt:</td><td class="forminfo"><label id="eBillAmt" style="float: left"></label></td>
						<td class="formlabel">Patient Due:</td><td class="forminfo"><label id="ePatAmt" style="float: left"></label></td>
					</tr>
					<tr>
						<td class="formlabel">&nbsp;</td>
						<td class="forminfo">&nbsp;</td>
						
						<td class="formlabel">Patient Credit Amount:</td><td class="forminfo"><label id="patCreditAmt" style="float: left"></label></td>
						<td class="formlabel">Net Patient Due:</td><td class="forminfo"><label id="netPatDue" style="float: left"></label></td>
					</tr>
					<tr>
						<td class="formlabel">Remarks:</td>
						<td class="forminfo" colspan="5">
							<textarea name="eRemarks" id="eRemarks"  onkeyup="remarksLimit(this,200)" onblur="remarksLimit(this,200)"></textarea>
							<input type="hidden" name="dialogId" id="dialogId" />
						</td>
					</tr>
					<tr>
						<td colspan="6">
							<input type="button" name="btnOK" id="btnOK" value="OK" onclick="setRemarks();"/>
							<input type="button" name="btnX" id="btnX" value="Cancel" onclick="handleCancelDialog();"/>
							<input type="button" id="editPrevious" name="editPrevious" value="<<Previous" />
							<input type="button" id="editNext" name="editNext" value="Next>>" />
						</td>
					</tr>
				</table>
			</fieldset>
		</div>
	</div>
</form>
</body>
</html>

