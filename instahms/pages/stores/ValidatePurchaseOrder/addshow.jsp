<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="currTime" value="<%= (new java.util.Date()) %>"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title><insta:ltext key="stores.procurement.addshow.validatepo.instahms"/></title>
<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="ajax.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="prefRejRemarks" value="<%= GenericPreferencesDAO.getGenericPreferences().getForceRemarksForPoItemReject() %>"/>
<script>
	var list = '${polist}';
	var deptId = '${ifn:cleanJavaScript(dept_id)}';
	var gRoleId = '${roleId}';
	var rejRemarksNeeded = '${prefRejRemarks}';

	function changeApprovalReject(value,id) {
		document.getElementById("approve_reject"+id).value = value;
		if ((value == 'R') && (rejRemarksNeeded == 'Y')){
			return showRemarksDialog(id);
		}
	}
	function validations() {
		if(document.forms[0].status_main.value == 'A') {
			var len = document.getElementById('poItemListTab').rows.length-1;
			var allrejects = true;
			var valid = true;
			for (var i=0;i<len;i++){
				if (document.getElementById("approve_reject"+i).value == 'R'){
					if ((rejRemarksNeeded == 'Y') && (document.getElementById("item_remarks"+i).value.trim() == "")){
						var msg=getString('js.stores.procurement.enterremarks.itemonrow');
						msg+=parseFloat(i+1);
						msg+=getString('js.stores.procurement.beenrejected');
						alert(msg);
						allrejects = false;
						valid = false;
						showRemarksDialog(i);
						break;
					}
				}
			}

			for (var i=0;i<len;i++) {
				if(document.getElementById("approve_reject"+i).value == 'A') {
					allrejects = false;
					break;
				}
			}
			if (allrejects) {
				if ( !confirm("There are no items selected approved for purchase. Do you wish to continue?"))
					return false;
			}
			if (!(valid)){
				return false;
			}
		}
		return true;
	}

	function checkstoreallocation() {
	 	if(gRoleId != 1 && gRoleId != 2) {
	 		if(deptId == "") {
	 		showMessage("js.stores.procurement.noassignedstore.notaccessthisscreen");
	 		document.getElementById("storecheck").style.display = 'none';
	 		}
	 	}
	}

</script>
<insta:js-bundle prefix="stores.procurement"/>
</head>

<jsp:useBean id="IssueDisplay" class="java.util.HashMap"/>
<c:set target="${IssueDisplay}" property="P" value="Permanent"/>
<c:set target="${IssueDisplay}" property="C" value="Consumable"/>
<c:set target="${IssueDisplay}" property="L" value="Loaneable"/>
<c:set target="${IssueDisplay}" property="R" value="Retail Only"/>

<jsp:useBean id="itemStatus" class="java.util.HashMap"/>
<c:set target="${itemStatus}" property="A" value="Approved"/>
<c:set target="${itemStatus}" property="R" value="Rejected"/>
<c:set target="${itemStatus}" property="P" value="Partially Fullfilled"/>
<c:set target="${itemStatus}" property="F" value="Fullfilled"/>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="O" value="Open"/>
<c:set target="${statusDisplay}" property="A" value="Approved"/>
<c:set target="${statusDisplay}" property="AO" value="Amended Open"/>
<c:set target="${statusDisplay}" property="AA" value="Amended Approved"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<c:set target="${statusDisplay}" property="FC" value="Force Closed"/>
<c:set target="${statusDisplay}" property="X" value="Cancelled"/>

<body onload="checkstoreallocation();">
<div id="storecheck" style="display: block;" >
<form action="viewpo.do" method="POST" name="validatepoform">
	<input type="hidden" name="_method" value="validatePO">
	<input type="hidden" name="poNo" value="${podetails.map.po_no}">
	<input type="hidden" id="dialogId" value=""/>

	<h1> <insta:ltext key="stores.procurement.addshow.validatepo"/> </h1>
	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder" >
	   <legend class="fieldSetLabel"><insta:ltext key="stores.procurement.addshow.podetails"/></legend>
		 <table   class="formtable" >
		 	<tr>
		 		<td class="formlabel"><insta:ltext key="stores.procurement.addshow.pono"/>:</td>
				<td class="forminfo" >${podetails.map.po_no}</td>
				<td class="formlabel"><insta:ltext key="stores.procurement.addshow.user"/>:</td>
				<td class="forminfo" >${podetails.map.user_id}</td>
				<td class="formlabel"><insta:ltext key="stores.procurement.addshow.store"/>:</td>
				<td class="forminfo">${ifn:cleanHtml(storeName)}</td>
			</tr>
			<tr>
			  <td class="formlabel"><insta:ltext key="stores.procurement.addshow.status"/>:</td>
			  <td class="forminfo">
				<c:choose>
					<c:when test="${podetails.map.status eq 'O'}">
							<select name="status_main" class="dropdown">
								<option value="V" ${podetails.map.status=='V'? 'selected' : ''}><insta:ltext key="stores.procurement.addshow.validate"/></option>
								<option value="FC" ${podetails.map.status=='FC'? 'selected' : ''}><insta:ltext key="stores.procurement.addshow.forceclosed"/></option>
							</select>
					</c:when>
					<c:when test="${podetails.map.status eq 'AO'}">
							<select name="status_main" class="dropdown">
								<option value="AV" ${podetails.map.status=='AV'? 'selected' : ''}><insta:ltext key="stores.procurement.addshow.amendedvalidate"/></option>
								<option value="FC" ${podetails.map.status=='FC'? 'selected' : ''}><insta:ltext key="stores.procurement.addshow.forceclosed"/></option>
							</select>
					</c:when>
					<c:otherwise>
							${podetails.map.status}
						</c:otherwise>
				</c:choose>
			  </td>
			  <td class="formlabel"><insta:ltext key="stores.procurement.addshow.remarks"/>:</td>
			  <td ><input type="text" name="remarks" id="remarks"
			      value="${(podetails.map.status eq 'O' || podetails.map.status eq 'AO') ? '' : (podetails.map.status eq 'A' || podetails.map.status eq 'AA')? podetails.map.approver_remarks : podetails.map.closure_reasons}"
			       <c:if test="${(podetails.map.status ne 'O' && podetails.map.status ne 'AO')}"> <insta:ltext key="stores.procurement.addshow.readonly"/></c:if> ></td>
			</tr>
		 </table>
	</fieldset>
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="stores.procurement.addshow.itemslist"/></legend>
		<table id="poItemListTab" class="datatable" width="100%">
			<tr>
				<th ><insta:ltext key="stores.procurement.addshow.item"/></th>
				<th ><insta:ltext key="stores.procurement.addshow.qty"/></th>
				<th><insta:ltext key="stores.procurement.addshow.bonusqty"/></th>
				<th ><insta:ltext key="stores.procurement.addshow.avbl.qty"/></th>
				<th ><insta:ltext key="stores.procurement.addshow.issuetype"/></th>
			</tr>
			<c:forEach var="po" items="${polist}" varStatus="status">
				<c:set var="i" value="${status.index}"/>
				<tr id="row${i}">
					<td style="width:25em;whitespace:normal;"><label id="itemnamelbl${i}">${po.map.medicine_name}</label>
						<input type="hidden" name="medicine_name" value="${po.map.medicine_name}"/>
						<input type="hidden" name="medicine_id" value="${po.map.medicine_id}"/>
						<input type="hidden" name="item_qty" value="${po.map.qty}"/>
						</td>
					<td align="right"><label id="itemqtylbl${i}">${po.map.qty}</label></td>
					<td align="right"><label id="itembonusqtylbl${i}">${po.map.bonus_qty}</label></td>
					<td align="right"><label id="itemavblqtylbl${i}">${po.map.availableqty}</label></td>
					<td><label id="issuetypelbl${i}">${IssueDisplay[po.map.issue_type]}</label></td>
				</tr>
			</c:forEach>
		</table>
	</fieldset>
	<div class="screenActions">
	 	<c:if test="${(podetails.map.status eq 'O' || podetails.map.status eq 'AO')}">
			<button type="submit" accesskey="S" name="save" class="button" onclick="return validations();"><b><u><insta:ltext key="stores.procurement.addshow.s"/></u></b><insta:ltext key="stores.procurement.addshow.ave"/></button>
		</c:if>
		<a href="${cpath }/stores/StoresPOValidate.do?_method=list&sortOrder=po_no&sortReverse=true&status=O"><insta:ltext key="stores.procurement.addshow.backtodashboard"/></a>
		<c:if test="${not empty podetails.map.po_no}">
			<insta:screenlink target="_blank" screenId="po_audit_log" extraParam="?_method=getAuditLogDetails&po_no=${podetails.map.po_no}&al_table=store_po_main_audit_log" label="Audit Log" addPipe="true"/>
		</c:if>
	</div>

</form>
</div>
</body>
</html>
