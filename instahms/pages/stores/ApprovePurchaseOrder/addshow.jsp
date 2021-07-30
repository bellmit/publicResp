<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="currTime" value="<%= (new java.util.Date()) %>"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="storeprocurement.approve.rejectpo.addshow.approve.or.rejectpo.instahms"/></title>
<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="ajax.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="prefRejRemarks" value="<%= GenericPreferencesDAO.getGenericPreferences().getForceRemarksForPoItemReject() %>"/>
<c:set var="validatePOReq" value="${genPrefs.poToBeValidated == 'Y'}"/>
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
						msg+=" ";
						msg+=parseFloat(i+1);
						msg+=" ";
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
	 	initDialog();
	}
	function showRemarksDialog(id){
		var button = document.getElementById("row"+id);
		dialog.cfg.setProperty("context",[button, "tr", "br"], false);
		document.getElementById("dialogId").value = id;
		document.approvalpoform.poremarks.value = document.getElementById("item_remarks"+id).value;
		dialog.show();
		document.getElementById("poremarks").focus();
	}
	function initDialog(){
	dialog = new YAHOO.widget.Dialog("dialog",
		{
			width:"400px",
			context : ["formtable", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true,
		} );
		var escKeyListener = new YAHOO.util.KeyListener("dialog", { keys:27 },
	                                              { fn:handleCancel,
	                                              	scope:dialog,
	                                              	correctScope:true} );
		dialog.cfg.queueProperty("keylisteners", escKeyListener);

		dialog.render();
	}

	function handleCancel() {
		dialog.cancel();
		document.approvalpoform.save.focus();
	}
	function onKeyPressAddRemarks(e) {
		e = (e) ? e : event;
		var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
		if ( charCode==13 || charCode==3 ) {
			addRemarks();
			return false;
		} else {
			return e;
		}
	}

	function addRemarks(){
		var dialogId = document.getElementById("dialogId").value;
		var description = document.approvalpoform.poremarks.value;
		if (rejRemarksNeeded == 'Y'){
			if (document.approvalpoform.poremarks.value.trim() == ""){
				showMessage("js.stores.procurement.enterremarks.rejecteditem");
				showRemarksDialog(dialogId);
				return false;
			}
		}
		if (dialogId != ''){
			document.getElementById("item_remarks"+dialogId).value = description;
		}
		document.approvalpoform.poremarks.value = "";
		dialog.cancel();
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
<c:set target="${itemStatus}" property="A" value="Validated"/>
<c:set target="${itemStatus}" property="A" value="Approved"/>
<c:set target="${itemStatus}" property="AV" value="Amended Validated"/>
<c:set target="${itemStatus}" property="AA" value="Amended Approved"/>
<c:set target="${itemStatus}" property="R" value="Rejected"/>
<c:set target="${itemStatus}" property="P" value="Partially Fullfilled"/>
<c:set target="${itemStatus}" property="F" value="Fullfilled"/>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="O" value="Open"/>
<c:set target="${statusDisplay}" property="A" value="Approved"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<c:set target="${statusDisplay}" property="AO" value="Amended Open"/>
<c:set target="${statusDisplay}" property="AA" value="Amended Approved"/>
<c:set target="${statusDisplay}" property="FC" value="Force Closed"/>
<c:set target="${statusDisplay}" property="X" value="Cancelled"/>

<body onload="checkstoreallocation();">
<c:set var="addedit">
 <insta:ltext key="storeprocurement.approve.rejectpo.addshow.addeditremarks"/>
</c:set>
<div id="storecheck" style="display: block;" >
<form action="StoresPOApproval.do" method="POST" name="approvalpoform">
	<input type="hidden" name="_method" value="update">
	<input type="hidden" name="poNo" value="${podetails.map.po_no}">
	<input type="hidden" id="dialogId" value=""/>

	<h1> <insta:ltext key="storeprocurement.approve.rejectpo.addshow.approve.or.rejectpo"/> </h1>
	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder" >
	   <legend class="fieldSetLabel"><insta:ltext key="storeprocurement.approve.rejectpo.addshow.po_details"/></legend>
		 <table   class="formtable" >
		 	<tr>
		 		<td class="formlabel"><insta:ltext key="storeprocurement.approve.rejectpo.addshow.pono"/>:</td>
				<td class="forminfo" >${podetails.map.po_no}</td>
				<td class="formlabel"><insta:ltext key="storeprocurement.approve.rejectpo.addshow.user"/>:</td>
				<td class="forminfo" >${podetails.map.user_id}</td>
				<td class="formlabel"><insta:ltext key="storeprocurement.approve.rejectpo.addshow.store"/>:</td>
				<td class="forminfo">${ifn:cleanHtml(storeName) }</td>
				<td class="formlabel"><insta:ltext key="storeprocurement.approve.rejectpo.addshow.status"/>:</td>
				<td class="forminfo">
					<c:choose>
						<c:when test="${(validatePOReq ? (podetails.map.status eq 'V' || podetails.map.status eq 'AV') : (podetails.map.status eq 'O' || podetails.map.status eq 'AO'))}">
							<select name="status_main" class="dropdown">
								<c:choose>
									<c:when test="${podetails.map.status=='V' || podetails.map.status=='O'}">
										<option value="A" ${podetails.map.status=='A'? 'selected' : ''}><insta:ltext key="storeprocurement.approve.rejectpo.addshow.approved"/></option>
									</c:when>
									<c:when test="${podetails.map.status=='AV' || podetails.map.status=='AO'}">
										<option value="AA" ${podetails.map.status=='AA'? 'selected' : ''}><insta:ltext key="storeprocurement.approve.rejectpo.addshow.amended.approved"/></option>
									</c:when>
								</c:choose>
								<option value="FC" ${podetails.map.status=='FC'? 'selected' : ''}><insta:ltext key="storeprocurement.approve.rejectpo.addshow.forceclosed"/></option>
							</select>
						</c:when>
						<c:otherwise>
							${statusDisplay[podetails.map.status]}
						</c:otherwise>
					</c:choose>
				</td>
			</tr>
			<tr>
			  <td class="formlabel"><insta:ltext key="storeprocurement.approve.rejectpo.addshow.remarks"/>:</td>
			  <td colspan="4"><input type="text" name="remarks" id="remarks"
			      value="${(validatePOReq ? (podetails.map.status eq 'V' || podetails.map.status eq 'AV') : (podetails.map.status eq 'O' || podetails.map.status eq 'AO')) ? '' : (podetails.map.status eq 'A' || podetails.map.status eq 'AA')? podetails.map.approver_remarks : podetails.map.closure_reasons}"
			       <c:if test="${validatePOReq ? (podetails.map.status ne 'V' && podetails.map.status ne 'AV') : (podetails.map.status ne 'O' && podetails.map.status ne 'AO')}"> <insta:ltext key="storeprocurement.approve.rejectpo.addshow.readonly"/></c:if> ></td>
			</tr>
		 </table>
	</fieldset>
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="storeprocurement.approve.rejectpo.addshow.itemslist"/></legend>
		<table id="poItemListTab" class="datatable" width="100%">
			<tr>
				<th ><insta:ltext key="storeprocurement.approve.rejectpo.addshow.item"/></th>
				<th ><insta:ltext key="storeprocurement.approve.rejectpo.addshow.qty"/></th>
				<th><insta:ltext key="storeprocurement.approve.rejectpo.addshow.bonusqty"/></th>
				<th ><insta:ltext key="storeprocurement.approve.rejectpo.addshow.avbl.qty"/></th>
				<th ><insta:ltext key="storeprocurement.approve.rejectpo.addshow.issuetype"/></th>
				<th ><insta:ltext key="storeprocurement.approve.rejectpo.addshow.approve.or.reject"/></th>
				<th><insta:ltext key="storeprocurement.approve.rejectpo.addshow.remarks"/></th>
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
					<c:choose>
						<c:when test="${validatePOReq ? (podetails.map.status eq 'V' || podetails.map.status eq 'AV') : (podetails.map.status eq 'O' || podetails.map.status eq 'AO')}">
							<td>
								<c:set var="ar" value="A"/>
								<c:set var="arr" value="AA"/>
								<c:if test="${po.map.status eq 'A' || po.map.status eq 'R' || po.map.status eq 'AA'}"><c:set var="ar" value="${po.map.status}"/></c:if>
								<c:choose>
									<c:when test="${po.map.status eq 'AV' || po.map.status eq 'AO'}">
										<insta:radio name="item_approve_reject${i}" radioText="Approve,Reject" radioValues="AA,R" value="${arr}" onclick="changeApprovalReject(this.value,'${i}')"/>
									</c:when>
									<c:otherwise>
										<insta:radio name="item_approve_reject${i}" radioText="Approve,Reject" radioValues="A,R" value="${ar}" onclick="changeApprovalReject(this.value,'${i}')"/>
									</c:otherwise>
								</c:choose>
								
								<input type="hidden" name="status" id="approve_reject${i}" value="${ar}"/>
								<c:set var="ar" value=""/>
							</td>
						</c:when>
						<c:otherwise>
							<td>
								${itemStatus[po.map.status]}
							</td>
						</c:otherwise>
					</c:choose>
					<td><a title="${addedit}" onclick="return showRemarksDialog(${i});">
					<img class="button" src="${cpath}/icons/zoom.png"></a>
					<input type="hidden" name="item_remarks"  value="${po.map.item_remarks}" id="item_remarks${i}"/></td>
				</tr>
			</c:forEach>
		</table>
	</fieldset>
	<div id="dialog" style="visibility:hidden">
			<div class="bd">
				<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="storeprocurement.approve.rejectpo.addshow.addremarks"/></legend>
				 <table cellpadding="0" cellspacing="0" width="100%" id="newtable">
					<tr>
						<td><insta:ltext key="storeprocurement.approve.rejectpo.addshow.remarks"/></td>
					</tr>

					<tr>
						<td>
							<input type="text" name="poremarks" id="poremarks"
									tabindex="1"  style="width:25em;" maxlength="100" onkeypress="return onKeyPressAddRemarks(event);">
						</td>
					</tr>
				</table>
				</fieldset>
				<table>
					<tr><td>&nbsp;</td></tr>
					<tr>
						<td>
							<button type="button" id="OK" name="OK" accesskey="A"  style="display: inline;" class="button" onclick="addRemarks();"tabindex="2"><label><b><insta:ltext key="storeprocurement.approve.rejectpo.addshow.ok"/></b></label></button>
							<button type="button" id="Cancel" name="Cancel" accesskey="A"  style="display: inline;" class="button" onclick="handleCancel();"tabindex="7"><label><b><insta:ltext key="storeprocurement.approve.rejectpo.addshow.cancel"/></b></label></button>
						</td>
					</tr>
				</table>
			</div>
		</div>
	<div class="screenActions">
	 	<c:if test="${(validatePOReq ? (podetails.map.status eq 'V' || podetails.map.status eq 'AV') :(podetails.map.status eq 'O' || podetails.map.status eq 'AO'))}">
			<button type="submit" accesskey="S" name="save" class="button" onclick="return validations();"><b><u><insta:ltext key="storeprocurement.approve.rejectpo.addshow.s"/></u></b><insta:ltext key="storeprocurement.approve.rejectpo.addshow.ave"/></button>
		</c:if>
		<a href="${cpath }/stores/StoresPOApproval.do?_method=list&sortOrder=po_no&sortReverse=true&status=O&status=V&status=AO&status=AV"><insta:ltext key="storeprocurement.approve.rejectpo.addshow.backtodashboard"/></a>
		<c:if test="${not empty podetails.map.po_no}">
			<insta:screenlink target="_blank" screenId="po_audit_log" extraParam="?_method=getAuditLogDetails&po_no=${podetails.map.po_no}&al_table=store_po_main_audit_log" label="Audit Log" addPipe="true"/>
		</c:if>
	</div>

</form>
</div>
</body>
</html>
