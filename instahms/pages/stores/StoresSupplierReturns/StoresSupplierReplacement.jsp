<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page import="org.apache.struts.Globals"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<html>
<head>
<c:set var="prefDecimalQty" value="<%= GenericPreferencesDAO.getGenericPreferences().getAllowdecimalsforqty() %>"/>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

	<insta:link type="js" file="ajax.js" />
	<insta:link type="script" file="hmsvalidation.js"/>

	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />

<script>
var popurl = '${pageContext.request.contextPath}';
var deptId = '${ifn:cleanJavaScript(dept_id)}';
var gRoleId = '${roleId}';
var cpath = '${pageContext.request.contextPath}';
function fun () {
	document.supplierreplaceform.returnType.value = '${return_medicine.map.return_type}';
}
function deleteItem(checkBox, rowId) {
	var itemListTable = document.getElementById("medList");
	var row = itemListTable.rows[rowId];
	var deletedInput = document.getElementById('hdeleted'+rowId);
	if (checkBox.checked) {
		deletedInput.value = 'true';
		document.getElementById('hreplacingqty'+rowId).readOnly = false;
		document.getElementById('mon'+rowId).readOnly = false;
		document.getElementById('h1year'+rowId).readOnly = false;
		document.getElementById('hmrp'+rowId).readOnly = false;
		document.getElementById('replacingBatch'+rowId).value = document.getElementById('hbatchno'+rowId).value;
		document.getElementById('replacingBatch'+rowId).readOnly = false;
	} else {
		deletedInput.value = 'false';
		document.getElementById('hreplacingqty'+rowId).value = 0;
		document.getElementById('hreplacingqty'+rowId).readOnly = true;
		document.getElementById('mon'+rowId).readOnly = true;
		document.getElementById('h1year'+rowId).readOnly = true;
		document.getElementById('hmrp'+rowId).readOnly = true;
		document.getElementById('replacingBatch'+rowId).value = '';
		document.getElementById('replacingBatch'+rowId).readOnly = true;
	}
	resetTotals();
}
function onchangeQty (rowId) {
	var retQty = parseFloat(document.getElementById("hretqty"+rowId).value);
	var repQty = parseFloat(document.getElementById("hreplacedqty"+rowId).value);
	var replacingQty = parseFloat(document.getElementById("hreplacingqty"+rowId).value);
	if (replacingQty > (retQty - repQty)) {
		alert("Replacing Qty should be less than or equal to (Returned Qty - Replaced Qty)");
		document.getElementById("hreplacingqty"+rowId).value = 0;
		document.getElementById("hreplacingqty"+rowId).focus();
	}
	resetTotals();
}
function resetTotals(){
	var totalNoOfRows;
	totalNoOfRows = document.getElementById("medList").rows.length-1;
	var tempTotQty = 0.00;
	var tempActTotQty = 0.00;
	var temprepTotQty = 0.00;
	for (var i=1;i<totalNoOfRows;i++) {
		tempActTotQty = eval(parseFloat(tempActTotQty) + parseFloat(formatAmountObj(document.getElementById("hretqty"+i))));
		temprepTotQty = eval(parseFloat(temprepTotQty) + parseFloat(formatAmountObj(document.getElementById("hreplacedqty"+i))));
		if (document.getElementById("delItem"+i).checked) {
			tempTotQty = eval(parseFloat(tempTotQty) + parseFloat(formatAmountObj(document.getElementById("hreplacingqty"+i))));
		}
	}

	document.getElementById("totQty").value = formatAmountValue(tempTotQty,false);
	document.getElementById("actTotQty").value = formatAmountValue(tempActTotQty,false);
	document.getElementById("repTotQty").value = formatAmountValue(temprepTotQty,false);

}

function isSerialBatchNoAlreadyPresent(medId,batchNo) {
	var ajaxobj = newXMLHttpRequest();
	var url = cpath+'/stores/StoresSupplierReturnslist.do?_method=isSerialBatchNoAlreadyPresent&medId='+medId+'&batchNo='+batchNo;
	var reqObject = newXMLHttpRequest();
	reqObject.open("POST",url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ( (reqObject.status == 200) && (reqObject.responseText!=null) ) {
			if (reqObject.responseText == 'Y') {
					return true;
				}
			}
		}
	return false;
}

function replaceStock(){
	resetTotals();
	var length = document.getElementById('medList').rows.length-2;
 	if (length < 1) {
 		alert("No rows in the grid !");
 		return false;
 	}
    var noneChecked = false;
 	var itemListTable = document.getElementById("medList");
	var numRows = itemListTable.rows.length-1;

	for (var k=1;k<numRows;k++) {
    	if (document.getElementById("delItem"+k).checked) {
    		if (document.getElementById("hmrp"+k).value == '') {
	    		alert("pls enter MRP");
	    		document.getElementById("hmrp"+k).focus();
	    		return false;
	    	}
    		if (document.getElementById("hreplacingqty"+k).value == 0) {
	    		alert("Replacing qty should not be zero");
	    		document.getElementById("hreplacingqty"+k).focus();
	    		return false;
	    	}
	        if (document.getElementById("replacingBatch"+k).value == '') {
	    		alert("Enter Replacing batch no");
	    		document.getElementById("replacingBatch"+k).focus();
	    		return false;
	    	}
	    	var currentBatch = document.getElementById("hbatchno"+k).value;
	    	var replacingBatch = document.getElementById("replacingBatch"+k).value;
	    	var medId = document.getElementById('hmedId'+k).value;
	    	if(isSerialBatchNoAlreadyPresent(medId,replacingBatch)
	    		&& replacingBatch !=currentBatch) {
				alert("This is Batch/Serial No. is already in use...\nPlease enter a different Replacing Batch No.")
				document.getElementById("replacingBatch"+k).focus();
				return false;
	    	}
	    	var replacedqty = document.getElementById("hreplacedqty"+k).value;
	    	var replacingqty = document.getElementById("hreplacingqty"+k).value;
	    	var totqty = parseFloat(replacedqty) + parseFloat(replacingqty);
	    	if (totqty > parseFloat(document.getElementById("hretqty"+k).value)){
	    		alert("Cannot replace more quantity than what is returned");
	    		document.getElementById("hreplacingqty"+k).focus();
	    		return false;
	    	}
    		noneChecked = true;
    	}
	}
    if (!noneChecked) {
    	alert("none of the rows in the grid are checked \n so no record(s) to save");
		return false;
    }
    document.supplierreplaceform.returnType.disabled = false;
    document.supplierreplaceform.saveStk.disabled = true;
	document.supplierreplaceform.action = "StoresSupplierReturnslist.do?_method=makeSupplierReplacement";
	document.supplierreplaceform.store.disabled = false;
	document.supplierreplaceform.submit();
}

function chkMon1(id){
	if(document.getElementById('mon'+id).value == 0 || document.getElementById('mon'+id).value > 12 ){
		alert("month should be 1-12 only");
		document.getElementById('mon'+id).value='';
		document.getElementById('mon'+id).focus();
		return false;
	}
	if(document.getElementById('mon'+id).value.length == 1){
		document.getElementById('mon'+id).value = '0'+document.getElementById('mon'+id).value;
	}
}

function chkYear1(id){
	if(document.getElementById('h1year'+id).value.length == 1){
		document.getElementById('h1year'+id).value = '0'+document.getElementById('h1year'+id).value;
	}
	if (document.getElementById('h1year'+id).value != '') {
		document.getElementById('hyear'+id).value = convertTwoDigitYear(document.getElementById('h1year'+id).value);
	}
}
function convertTwoDigitYear(year) {
    // convert 2 digit years intelligently
    var now = new Date();
    var century = now.getFullYear();
    var s = century.toString();
    var yearPrefix = s.substring(0,2);
    return (yearPrefix+year);
}


function checkstoreallocation() {
 	if(gRoleId != 1 && gRoleId != 2) {
 		if(deptId == "") {
 		alert("There is no assigned store, hence you dont have any access to this screen");
 		document.getElementById("storecheck").style.display = 'none';
 		}
 	}
}
function makeingDecValidate(objValue,obj,id){
	if (objValue == '') return false;
    if (isAmount(objValue)) {
		document.getElementById(obj.name+id).value = parseFloat(objValue).toFixed(decDigits);
	}else document.getElementById(obj.name+id).value = parseFloat(0).toFixed(decDigits);
}
</script>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<body onload="fun();resetTotals();checkstoreallocation();">
<div id="storecheck" style="display: block;" >
 <form method="POST" action="StoresSupplierReturnslist.do" name="supplierreplaceform">
 <input type="hidden" name="suppId" id="suppId" value="${return_medicine.map.supplier_id}"/>
 <input type="hidden" name="retno" id="retno" value="${return_medicine.map.return_no }"/>
 <input type="hidden" name="_method" value="makeSupplierReplacement"/>

 <h1><insta:ltext key="storeprocurement.supplierreplacement.returndetails.supplierreplacement"/></h1>

<input type="hidden" name="qty_unit" value="${return_medicine.map.ret_qty_unit}">
<fieldset class="fieldSetBorder">
           <legend class="fieldSetLabel"><insta:ltext key="storeprocurement.supplierreplacement.returndetails.originalreturndetails"/></legend>
				<table class="formtable" >
					<tr>
						<td class="formlabel"><insta:ltext key="storeprocurement.supplierreplacement.returndetails.returnno"/>:</td>
						<td class="forminfo">${return_medicine.map.return_no }</td>
						<td class="formlabel"><insta:ltext key="storeprocurement.supplierreplacement.returndetails.supplier"/>:</td>
						<td class="forminfo">${return_medicine.map.supplier_name }</td>
						<td class="formlabel"><insta:ltext key="storeprocurement.supplierreplacement.returndetails.store"/>:</td>
						 <td class="forminfo"><b><insta:getStoreName store_id="${return_medicine.map.store_id}"/></b>
							<input type = "hidden" name="store" id="store" value="${return_medicine.map.store_id}" />
							</td>

					</tr>
					<tr>
					    <td class="formlabel"><insta:ltext key="storeprocurement.supplierreplacement.returndetails.returntype"/>:</td>
						<td ><insta:selectoptions name="returnType" value="" opvalues="D,E,N,O"
					    	class="dropdown" optexts="Damage,Expiry,Non-moving,Others"/>
						</td>
						<td class="formlabel"><insta:ltext key="storeprocurement.supplierreplacement.returndetails.remarks"/>:</td>
						<td colspan="1">
						<input type="text" name="remarks" id="remarks" maxlength="500" tabindex="4" onblur="upperCase(remarks)" size="60" value="${return_medicine.map.remarks}">
						</td>
						<td class="formlabel"></td>
						<td>
						<input type="checkbox" name="gatepass" id="gatepass" ><insta:ltext key="storeprocurement.supplierreplacement.returndetails.gatepass"/>
						</td>
					</tr>

				</table>
			</fieldset>
			<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="storeprocurement.supplierreplacement.returndetails.itemlist"/></legend>
			<table class="dataTable" width="100%" cellspacing="0" cellpadding="0" id="medList" border="0">
				<tr >
					<th><insta:ltext key="storeprocurement.supplierreplacement.returndetails.replace"/></th>
					<th><insta:ltext key="storeprocurement.supplierreplacement.returndetails.itemname"/></th>
					<th><insta:ltext key="storeprocurement.supplierreplacement.returndetails.manf"/></th>
					<th><insta:ltext key="storeprocurement.supplierreplacement.returndetails.batch.or.serial.no"/></th>
					<th><insta:ltext key="storeprocurement.supplierreplacement.returndetails.mrp"/></th>
					<th><insta:ltext key="storeprocurement.supplierreplacement.returndetails.expdate.mmyy.in.brackets"/></th>
					<th style="text-align:right;"><insta:ltext key="storeprocurement.supplierreplacement.returndetails.returnedqty"/></th>
					<th style="text-align:right;"><insta:ltext key="storeprocurement.supplierreplacement.returndetails.replacedqty"/></th>
					<th style="text-align:right;"><insta:ltext key="storeprocurement.supplierreplacement.returndetails.replacingqty"/></th>
					<th><insta:ltext key="storeprocurement.supplierreplacement.returndetails.replacingbatchno"/></th>
					<th style="text-align:right;"><insta:ltext key="storeprocurement.supplierreplacement.returndetails.uom"/></th>
				</tr>
				<c:set var="i" value="1"/>
				<c:forEach items="${list}" var="med" varStatus="status">
			    <tr>
				     <td>
							<input type="checkbox" name="delItem" id="delItem${i}"
								onclick="deleteItem(this, ${i})" checked/>
						<input type="hidden" name="hdeleted" id="hdeleted${i}" value="true"/>
				     </td>
				     <td class="forminfo" style="width:10em;padding-left: 0.5em;" valign="middle"><label id="medName">${med.map.medicine_name }</label>
					 <input type="hidden" name="hmedId" id='hmedId${i}' value="${med.map.medicine_id }">
				     </td>
				     <td  style="width:10em;padding-left: 0.5em;" valign="middle"><label id="manufacturerName">${med.map.manf_mnemonic }</label></td>
				  	 <td style="padding-left: 0.5em;">
				  	 	${med.map.batch_no}
				  	 	<input type="hidden" name="hbatchno" id='hbatchno${i}' value="${med.map.batch_no }">
				  	 	<input type="hidden" name="itemBatchId" id='"itemBatchId"${i}' value="${med.map.item_batch_id }">
				  	 	<input type="hidden" name="return_detail_no" value="${med.map.return_detail_no }"/>
			  	 	 </td>
				     <td style="padding-right: 0.5em;text-align:right;"><input type="hidden" name="hpkgsz" id='hpkgsz${i}' value="${ifn:afmt(med.map.issue_base_unit) }" >
				     <input type="text" name="hmrp" id='hmrp${i}' class="number" value="${ifn:afmt(med.map.mrp) }" onkeypress="return enterNumAndDot(event);" onblur="return makeingDecValidate(this.value,this,${i})"></td>
				     <td style="padding-left: 0.5em;">
				     <input type="text" name="mon" id="mon${i}" value='<fmt:formatDate value="${med.map.date}" pattern="MM"/>' class="timefield" maxlength="2"
						     onchange="return chkMon1(${i});" onkeypress="return enterNumOnlyzeroToNine(event);"  />-
						     <input type="text" name="h1year" id="h1year${i}" value='<fmt:formatDate value="${med.map.date}" pattern="yy"/>' class="timefield" maxlength="2" onchange="return chkYear1(${i});"
						      onkeypress="return enterNumOnlyzeroToNine(event);"/><input type="hidden" name="hyear" id ="hyear${i}" value="<fmt:formatDate value="${med.map.date}" pattern="yyyy"/>" size="2" maxlength="2" ></td>
				     <td style="padding-left: 0.5em;text-align:right;"><input type="hidden" class="number" readonly name="hretqty" id='hretqty${i}' value="${med.map.returnedqty}" >${ifn:afmt(med.map.returnedqty) }</td>
				     <td style="padding-left: 0.5em;text-align:right;"><input type="hidden" class="number" readonly name="hreplacedqty" id='hreplacedqty${i}' value="${med.map.replacedqty }" >${ifn:afmt(med.map.replacedqty) }</td>
				     <td align="right" style="padding-left: 0.5em;text-align:right;">
				     <input type="text" class="number"  name="hreplacingqty" id='hreplacingqty${i}' onblur="onchangeQty(${i})"
				     value="${ifn:afmt(med.map.returnedqty-med.map.replacedqty)}" onkeypress="return validateQtyField(event,'${prefDecimalQty }');"
				             <c:if test="${med.map.identification eq 'S'}">readOnly</c:if> /></td>
				     <td >
				     	<c:set var="batchNoReadOnly" value="${med.map.batch_no_applicable eq 'N' ? 'readonly' : '' }"/>
				     	<input type="text" name="replacingBatch" id='replacingBatch${i}' value="${med.map.batch_no }" size="15" ${batchNoReadOnly }/>
			     	</td>
				     <td style="padding-left: 0.5em;text-align:right;"><label name="issueUnitsLabel" id='issueUnitsLabel${i}'>${med.map.issue_units }</label></td>
 				 </tr>
				<c:set var="i" value="${i+1}"/>
 				</c:forEach>
				<tr>
				 <td colspan="7">&nbsp;</td>
			     <td align="right" class="button"><insta:ltext key="storeprocurement.supplierreplacement.returndetails.totalitems"/>: </td>
			     <td style="width:4em" align="right">
				   <input type="text" readonly  name="totQty" id="totQty" class="number"
							value="0"/><input type="hidden" readonly  name="repTotQty" id="repTotQty"  value="0"/><input type="hidden" readonly  name="actTotQty" id="actTotQty"  value="0"/>
			     </td>
			     </tr>
			 </table>
			 </fieldset>

	 <div class="screenActions">
            <button type="button" name="saveStk" accesskey="R" class="button" onclick="return replaceStock();"><b><u><insta:ltext key="storeprocurement.supplierreplacement.returndetails.r"/></u></b><insta:ltext key="storeprocurement.supplierreplacement.returndetails.eturn"/></button>
            <a href="${pageContext.request.contextPath}/stores/StoresSupplierReturns.do?_method=getSupplierReturns&sortOrder=return_no&sortReverse=true"><insta:ltext key="storeprocurement.supplierreplacement.returndetails.backtodashboard"/></a>
     </div>
</form>
</div>
</body>
</html>
