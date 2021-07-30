<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="currTime" value="<%= (new java.util.Date()) %>"/>
<html>
<c:set var="allowDecimalsForQty" value="<%= GenericPreferencesDAO.getGenericPreferences().getAllowdecimalsforqty()%>" />
<c:set var="indentApprovedBy"
	value='<%= GenericPreferencesDAO.getAllPrefs().get("indent_approval_by") %>'
	scope="request" />
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="storemgmt.indentapprovallist.addshow.title"/></title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="ajax.js"/>

<script>
    var deptId = '${ifn:cleanJavaScript(dept_id)}';
	var gRoleId = '${roleId}';
	var allowBackDate = '${actionRightsMap.allow_backdate}';
	var allowDecimalsForQty = '${allowDecimalsForQty}';

	function initDialog(){
		dialog = new YAHOO.widget.Dialog("dialog",
		{
			width:"300px",
			context : ["indentItemListTab", "tr", "br"],
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

	var i=0;
	ITEM_COL = i++; REQUIRED_QTY_COL = i++; AVAILABLE_QTY_COL = i++; PKG_SIZE_COL=i++; ISSUE_UOM_COL = i++;
	APPROVE_REJECT_COL=i++;INDENT_EDIT_COL=i++;
	var allowDecimalsForQty = '${allowDecimalsForQty}';

function handleCancel() {
	dialog.cancel();
	document.forms[0].save.focus();
}

var fieldEdited = false;

function setEdited() {
	fieldEdited = true;
}

function handleNextIndent() {
	var id = document.getElementById("dialogId").value;
	var row = getIndentRow(id);
	var nRow = YAHOO.util.Dom.getNextSibling(row);
    if (nRow != null) {
        //YAHOO.util.Dom.removeClass(row, 'editing');
        //YAHOO.util.Dom.addClass(row, 'selectedRow');
		var anchor = YAHOO.util.Dom.getFirstChild(nRow.cells[INDENT_EDIT_COL]);
		var rowObj = getThisRow(anchor);
		if (rowObj != null) {
			var rowId = rowObj.rowIndex -1;
			if (fieldEdited) {
				if(updateQty()){
					fieldEdited = false;
				} else {
					fieldEdited = true;
					return false;
				}
			}
			openDialogBox(rowObj,rowId);
		}
    } else {
		if(fieldEdited) {
			if(updateQty()) {
		    	fieldEdited = false;
		    	dialog.show();
		    } else {
		    	fieldEdited = true;
		    	return false;
		    }
		}
	}
}
function handlePreviousIndent() {
	var id = document.getElementById("dialogId").value;
	var row = getIndentRow(id);
	var prevRow = YAHOO.util.Dom.getPreviousSibling(row);
    if (prevRow != null) {
        //YAHOO.util.Dom.removeClass(row, 'editing');
        //YAHOO.util.Dom.addClass(prevRow, 'selectedRow');
		var anchor = YAHOO.util.Dom.getFirstChild(prevRow.cells[INDENT_EDIT_COL]);
		var rowObj = getThisRow(anchor);
		if (rowObj != null) {
		var rowId = rowObj.rowIndex -1;
			if (fieldEdited) {
				if(updateQty()){
					fieldEdited = false;
				} else {
					fieldEdited = true;
					return false;
				}
			}
			openDialogBox(rowObj,rowId);
		} else {
			if(fieldEdited) {
				if(updateQty()) {
			    	fieldEdited = false;
			    	dialog.show();
			    } else {
			    	fieldEdited = true;
			    	return false;
			    }
			}
		}
    }
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getIndentRow(i) {
	i = parseInt(i);
	var table = document.getElementById("indentItemListTab");
	return table.rows[i+1];
}

function getRow(i) {
	i = parseInt(i);
	var table = document.getElementById("indentItemListTab");
	return table.rows[i];
}

function makeingDecLabel(objValue,obj){
	if (objValue == '') objValue = 0;
    if (isAmount(objValue)) {
		document.getElementById(obj.id).textContent = parseFloat(objValue).toFixed(2);
	}else {
		document.getElementById(obj.id).textContent = 0.00;
	}
}

function onKeyPressAddQty(e) {
		e = (e) ? e : event;
		var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
		if ( charCode==13 || charCode==3 ) {
			if (!updateQty()) return false;
			dialog.cancel();
			return false;
		}
}

function openDialogBox(obj,id){
	var button = document.getElementById("dialogShow"+id);
	document.getElementById("qty").value = document.getElementById("item_qty"+id).value;

	dialog.cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById("dialogId").value = id;

	dialog.show();
	document.getElementById("qty").focus();
	return false;
}

	function updateQty(){
		if (!isValidNumber(document.getElementById("qty"), allowDecimalsForQty)) return false;

		var itemtable = document.getElementById("indentItemListTab");
		var tabLen = itemtable.rows.length;
		var dialogId  = document.getElementById("dialogId").value;
		var qtyToBeUpdated = document.getElementById("qty").value;
		//now update the qty
		document.getElementById("item_qty"+dialogId).value = document.getElementById("qty").value;
		var labelObj = document.getElementById("itemqtylbl"+dialogId);
		var qtyValue = document.getElementById("qty").value;
		var identification = document.getElementById("identification"+dialogId).value;

		if (qtyToBeUpdated <= 0) {
			showMessage("js.stores.mgmt.approvedqty.greaterthan0");
			document.getElementById("qty").focus();
			return false;
		}
		if(identification == 'S')
			if(!isInteger(qtyValue)) {
				showMessage("js.stores.mgmt.serialitemqty.wholenumbers");
				document.getElementById("qty").focus();
				return false;
			}
		makeingDecLabel(qtyValue, labelObj);
		dialog.cancel();
		return true;
	}

	function changeApprovalReject(value,id) {
		document.getElementById("approve_reject"+id).value = value;
	}

	function validateFields() {
		if (!doValidateDateField(document.forms[0].expected_date))
			return false;

		if (document.forms[0].expected_date.value == "")  {
			showMessage("js.stores.mgmt.indents.expecteddate.required");
			document.forms[0].expected_date.focus();
			return false;
		}
		if (!(validateTime(document.forms[0].expected_time))) return false;
		if (gRoleId != 1 && gRoleId != 2 && allowBackDate != 'A') {
			if (document.forms[0].expected_date.value != '') {
				msg = validateDateStr(document.forms[0].expected_date.value,'future');
				if ( msg != null) {
					alert(msg);
					document.forms[0].expected_date.focus();
			        return false;
		        }
			}
		}
		document.forms[0].submit();
	}

	function checkstoreallocation() {
 		if(gRoleId != 1 && gRoleId != 2 && accessstores != 'A') {
 			if(deptId == "") {
 			showMessage("js.stores.mgmt.noassignedstore.notaccessthisscreen");
 			document.getElementById("storecheck").style.display = 'none';
 			}
 		}
	}
</script>
<insta:js-bundle prefix="stores.mgmt.indents"/>
<insta:js-bundle prefix="stores.mgmt"/>
</head>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="P" value="Permanent"/>
<c:set target="${statusDisplay}" property="C" value="Consumable"/>
<c:set target="${statusDisplay}" property="L" value="Loaneable"/>
<c:set target="${statusDisplay}" property="R" value="Retail Only"/>

<jsp:useBean id="indentStatus" class="java.util.HashMap"/>
<c:set target="${indentStatus}" property="O" value="Open"/>
<c:set target="${indentStatus}" property="A" value="Approved"/>
<c:set target="${indentStatus}" property="R" value="Rejected"/>
<c:set target="${indentStatus}" property="X" value="Cancelled"/>
<c:set target="${indentStatus}" property="P" value="Processed"/>
<c:set target="${indentStatus}" property="C" value="Closed"/>

<jsp:useBean id="itemStatus" class="java.util.HashMap"/>
<c:set target="${itemStatus}" property="O" value="Open"/>
<c:set target="${itemStatus}" property="A" value="Approved"/>
<c:set target="${itemStatus}" property="R" value="Rejected"/>
<c:set target="${itemStatus}" property="T" value="Trasfered"/>
<c:set target="${itemStatus}" property="C" value="Closed"/>

<jsp:useBean id="indentType" class="java.util.HashMap"/>
<c:set target="${indentType}" property="S" value="Stock Transfer"/>
<c:set target="${indentType}" property="U" value="Dept / Ward Issue"/>
<body onload="checkstoreallocation();initDialog(); " class="yui-skin-sam"">
<c:set var="itemstatus">
<insta:ltext key="storemgmt.indentapprovallist.addshow.approve"/>,
<insta:ltext key="storemgmt.indentapprovallist.addshow.reject"/>
</c:set>
<div id="storecheck" style="display: block;" >
<form action="StoresIndentApproval.do" method="POST">
	<input type="hidden" name="_method" value="update">
	<input type="hidden" name="indentNo" value="${indentdetails.map.indent_no}">
	<input type="hidden" name="dept_from" value="${indentdetails.map.dept_from}"/>
	<input type="hidden" id="dialogId" value=""/>
	<h1> <insta:ltext key="storemgmt.indentapprovallist.addshow.approve.rejectindent"/> </h1>
	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder" >
	   <legend class="fieldSetLabel">${indentType[indentdetails.map.indent_type] }</legend>
		 <table   class="formtable" >
		 	<tr>
				<td class="formlabel"><insta:ltext key="storemgmt.indentapprovallist.addshow.indentno"/>:</td>
				<td class="forminfo">${indentdetails.map.indent_no}</td>
				<td class="formlabel"><insta:ltext key="storemgmt.indentapprovallist.addshow.raisedby"/>:</td>
				<td class="forminfo" align="left">${indentdetails.map.requester_name}</td>
				<td class="formlabel"><insta:ltext key="storemgmt.indentapprovallist.addshow.indentstore"/>:</td>
				<td class="forminfo">${ifn:cleanHtmlAttribute(storeName)}</td>

				<td class="formlabel"><insta:ltext key="storemgmt.indentapprovallist.addshow.status"/>:</td>
				<td class="forminfo">
					<c:choose>
						<c:when test="${indentdetails.map.status eq 'O'}">
							<select name="status_main" class="dropdown">
								<option value="A" ${indentdetails.map.status=='A'? 'selected' : ''}><insta:ltext key="storemgmt.indentapprovallist.addshow.approved"/></option>
								<option value="R" ${indentdetails.map.status=='R'? 'selected' : ''}><insta:ltext key="storemgmt.indentapprovallist.addshow.rejected"/></option>
							</select>
						</c:when>
						<c:otherwise>
							${indentStatus[indentdetails.map.status]}
						</c:otherwise>
					</c:choose>
				</td>
			</tr>
			<tr>
				<td class="formlabel"></td>
				<td></td>
				<td colspan="2">
					<c:choose>
						<c:when test="${indentdetails.map.indent_type eq 'U'}">
							<c:choose>
								<c:when test="${indentdetails.map.location_type eq 'D'}">
									 <table>
										<tr>
											<td class="formlabel"><insta:ltext key="storemgmt.indentapprovallist.addshow.dept"/>:</td>
											<td class="forminfo">${indentdetails.map.dept_name}</td>
										</tr>
									 </table>
								</c:when>
								<c:otherwise>
									<table>
										<tr>
											<td class="formlabel"><insta:ltext key="storemgmt.indentapprovallist.addshow.ward"/>:</td>
											<td class="forminfo">${indentdetails.map.ward_name}</td>
										</tr>
									</table>
								</c:otherwise>
							</c:choose>
						</c:when>
						<c:otherwise>
							<table>
								<tr>
									<td class="formlabel"><insta:ltext key="storemgmt.indentapprovallist.addshow.requestingstore"/>:</td>
									<td class="forminfo">
										<c:choose>
											<c:when test="${indentdetails.map.indent_type eq 'S'}">
												${ifn:cleanHtmlAttribute(deptFrom)}
											</c:when>
											<c:otherwise>
												${ifn:cleanHtmlAttribute(storeName)}
											</c:otherwise>
										</c:choose>
									</td>
								</table>
						</c:otherwise>
					</c:choose>
				</td>
				<c:choose>
					<c:when test="${indentdetails.map.status eq 'A' || indentdetails.map.status eq 'C'}">
						<td class="formlabel"><insta:ltext key="storemgmt.indentapprovallist.addshow.expecteddate"/>:</td>
						<td class="forminfo"><fmt:formatDate value="${indentdetails.map.expected_date}" pattern="dd-MM-yyyy" var="expecteddt"/>
						<fmt:formatDate value="${indentdetails.map.expected_date}" pattern="HH:mm" var="expectedtime"/>
						<fmt:formatDate pattern="HH:mm" value="${currTime}" var="currenttime"/>
						<c:out value="${expecteddt} ${expectedtime}"/></td>
						<td class="formlabel"><insta:ltext key="storemgmt.indentapprovallist.addshow.reason"/>:</td>
						<td class="forminfo">
							<c:if test="${indentdetails.map.status eq 'A'}">${indentdetails.map.approver_remarks}</c:if>
							<c:if test="${indentdetails.map.status eq 'C'}">${indentdetails.map.closure_reasons}</c:if>
						</td>
					</c:when>
					<c:otherwise>
						<td class="formlabel"><insta:ltext key="storemgmt.indentapprovallist.addshow.expecteddate"/>:</td>
						<td><fmt:formatDate value="${indentdetails.map.expected_date}" pattern="dd-MM-yyyy" var="expecteddt"/>
						<fmt:formatDate value="${indentdetails.map.expected_date}" pattern="HH:mm" var="expectedtime"/>
						<fmt:formatDate pattern="HH:mm" value="${currTime}" var="currenttime"/>
						<insta:datewidget name="expected_date" value="${expecteddt}"/>
						<input type="text" name="expected_time" id="" value="${not empty expectedtime? expectedtime : currenttime}" class="timefield" ></td>
						<td class="formlabel"><insta:ltext key="storemgmt.indentapprovallist.addshow.reason"/>:</td>
						<td><input type="text" size="60" name="remarks"  maxlength="100" value='<c:out value="${indentdetails.map.remarks}"/>'/></td>
					</c:otherwise>
				</c:choose>
			</tr>
		 </table>
	</fieldset>
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="storemgmt.indentapprovallist.addshow.itemslist"/></legend>
		<table id="indentItemListTab" class="datatable" width="100%">
			<tr>
				<th ><insta:ltext key="storemgmt.indentapprovallist.addshow.item"/></th>
				<th ><insta:ltext key="storemgmt.indentapprovallist.addshow.qtyreq"/></th>
				<th ><insta:ltext key="storemgmt.indentapprovallist.addshow.qtyavbl"/></th>
				<c:if test="${indentdetails.map.indent_type eq 'S'}">
					<th ><insta:ltext key="storemgmt.indentapprovallist.addshow.req.qtyavbl"/></th>
				</c:if>
				<th><insta:ltext key="storemgmt.indentapprovallist.addshow.pkgsize"/></th>
				<th><insta:ltext key="storemgmt.indentapprovallist.addshow.unituom"/></th>
				<th ><insta:ltext key="storemgmt.indentapprovallist.addshow.approve.reject"/></th>
				<th></th>
			</tr>
			<c:forEach var="indent" items="${indentlist}" varStatus="status">
				<c:set var="i" value="${status.index}"/>
				<tr id="row${i}">
					<td><label id="itemnamelbl${i}"><c:out value="${indent.map.medicine_name}"/></label>
					<input type="hidden" name="medicine_name" value=<insta:encodeComponent value="${indent.map.medicine_name}"/> >
					<input type="hidden" name="medicine_id" value="${indent.map.medicine_id}"/>
					<input type="hidden" id="item_qty${i}"name="item_qty" value="${indent.map.qty}"/>
					<input type="hidden" id="identification${i}" name="identification" value="${indent.map.identification}"/></td>
					<td align="right"><label id="itemqtylbl${i}">${indent.map.qty}</label></td>
					<td align="right"><label id="itemavblqtylbl${i}">${indent.map.availableqty}</label></td>
					<c:if test="${indentdetails.map.indent_type eq 'S'}">
						<td align="right"><label id="availableqtyfor_reqstore${i}">${indent.map.availableqtyfor_reqstore}</label></td>
					</c:if>
					<td>${indent.map.issue_base_unit}</td>
					<td>${indent.map.issue_units}</td>
					<c:choose>
						<c:when test="${indentdetails.map.status eq 'O'}">
							<td>
								<c:set var="ar" value="A"/>
								<c:if test="${indent.map.status eq 'A' || indent.map.status eq 'R'}"><c:set var="ar" value="${indent.map.status}"/></c:if>
								<insta:radio name="item_approve_reject${i}" radioText="${itemstatus}" radioValues="A,R" value="${ar}" onclick="changeApprovalReject(this.value,'${i}')"/>
								<input type="hidden" name="status" id="approve_reject${i}" value="${ar}"/>
								<c:set var="ar" value=""/>
							</td>
							<td>
								<button id="editBut${i}" class="imgButton" onclick="openDialogBox(this,${i}); return false;"
								accesskey="+" title='<insta:ltext key="storemgmt.indentapprovallist.addshow.editqty"/>'>
								<img class="button" name="edit" id="edit${i }" src="../icons/Edit.png"	style="cursor:pointer;" />
								<div id="dialogShow${i}"></div>
								</button>
							</td>
						</c:when>
						<c:otherwise>
							<td>
								${itemStatus[indent.map.status]}
							</td>
						</c:otherwise>
					</c:choose>
				</tr>
			</c:forEach>
		</table>
	</fieldset>
	<div id="dialog" style="visibility:hidden">
			<div class="bd">
				<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="storemgmt.indentapprovallist.addshow.editqty"/></legend>
				 <table cellpadding="0" class="detailFormTable" cellspacing="0" width="100%" id="newtable" >
					<tr>
						<th style="text-align:left" ><insta:ltext key="storemgmt.indentapprovallist.addshow.approveqty"/></th>
						<td>
							<input type="text" name="qty" id="qty" class="number"
									tabindex="1" onkeypress="return onKeyPressAddQty(event);" onchange="setEdited();">
						</td>
					</tr>
					<tr></tr>
				</table>
				</fieldset>
				<table>
					<tr><td>&nbsp;</td></tr>
					<tr>
						<td>
							<button type="button" id="OK" name="OK" accesskey="A"  style="display: inline;" class="button" onclick="updateQty();"tabindex="2"><label><b><insta:ltext key="storemgmt.indentapprovallist.addshow.ok"/></b></label></button>
							<button type="button" id="Cancel" name="Cancel" accesskey="A"  style="display: inline;" class="button" onclick="handleCancel();"tabindex="3"><label><b><insta:ltext key="storemgmt.indentapprovallist.addshow.cancel"/></b></label></button>
							<button type="button" id="next" name="next" accesskey="P"  style="display: inline;" class="button" onclick="handlePreviousIndent();"tabindex="4"><label><b>&lt;&lt;<insta:ltext key="storemgmt.indentapprovallist.addshow.prev"/></b></label></button>
							<button type="button" id="prev" name="prev" accesskey="N"  style="display: inline;" class="button" onclick="handleNextIndent();"tabindex="5"><label><b><insta:ltext key="storemgmt.indentapprovallist.addshow.next"/>&gt;&gt;</b></label></button>
						</td>
					</tr>
				</table>
			</div>
		</div>
	<div class="screenActions">
	 	<c:if test="${indentdetails.map.status eq 'O'}">
			<button type="button" accesskey="S" name="save" class="button" onclick="return validateFields()" ><b><u><insta:ltext key="storemgmt.indentapprovallist.addshow.s"/></u></b><insta:ltext key="storemgmt.indentapprovallist.addshow.ave"/></button>
		</c:if>
		<a href="${cpath }/stores/StoresIndentApproval.do?_method=list&status=O&sortOrder=indent_no&sortReverse=true&indent_type=${indentdetails.map.indent_type}"><insta:ltext key="storemgmt.indentapprovallist.addshow.backtodashboard"/></a>
	</div>

</form>
</div>
<script>
		var accessstores = '${multiStoreAccess}';
</script>
</body>
</html>
