<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="genPrefs" value="<%= GenericPreferencesDAO.getGenericPreferences() %>" />
<c:set var="prefStockEntryStatus" value="${genPrefs.stock_entry_agnst_do}"/>
<c:set var="prefVat" value="${genPrefs.showVAT}" scope="request"/>
<c:set var="taxLabel" value="${genPrefs.procurement_tax_label}" scope="request"/>
<c:set var="prefExpItemProc" value="${genPrefs.expiredItemsProcurement}"/>
<c:set var="prefProcExpireDays" value="${genPrefs.procurementExpiryDays}"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="storemgmt.edititembatchdetails.edit.title"/></title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="instadate.js" />
<insta:link type="script" file="common.js" />
<insta:link type="script" file="stores/storescommon.js" />
<insta:js-bundle prefix="stores.procurement"/>
<c:set var="doAllowStatus">
	<c:choose>
		<c:when test="${fn:toLowerCase(prefStockEntryStatus) eq 'y' && userCenterId != 0}">true</c:when>
		<c:otherwise>false</c:otherwise>
	</c:choose>
</c:set>

<script type="text/javascript">
	
	var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
	function chkMon(){
			if (document.itemBatchForm.expmon.value != '') {
				if(document.itemBatchForm.expmon.value == 0 || document.itemBatchForm.expmon.value > 12 ){
					alert("Month should be 1-12 only");
					document.itemBatchForm.expmon.value='';
					document.itemBatchForm.expmon.focus();
					return false;
				}
				if(document.itemBatchForm.expmon.value.length == 1){
					document.itemBatchForm.expmon.value = '0'+document.itemBatchForm.expmon.value;
				}
			}
         }

		function chkYear(){
			if (document.itemBatchForm.expyear.value != '') {
				if(document.itemBatchForm.expyear.value.length == 1){
					document.itemBatchForm.expyear.value = '0'+document.itemBatchForm.expyear.value;
				}
			}
		}

		function getLastDayForMonth(month, year) {
			var dt = new Date(parseInt(year),parseInt(month),0);
			return [(dt.getDate() < 10 ? ('0'+ dt.getDate()) : dt.getDate()),(dt.getMonth() < 9 ? ('0'+ (dt.getMonth()+1)) : (dt.getMonth()+1)),dt.getFullYear()].join('-');
		}
		function validate() {
			var procExpireDays = '${prefProcExpireDays}';
			var prefExpItemProc = '${prefExpItemProc}';
			
			
			var mrp = document.itemBatchForm.mrp;
			var expDtMon = document.itemBatchForm.expmon;
			var expDtYr = document.itemBatchForm.expyear;

			var typeOfAction = prefExpItemProc;
			var noOfDays = parseInt(procExpireDays);
			
			if(!empty(expDtMon.value) && !empty(expDtYr.value)) {
				document.itemBatchForm.exp_dt.value = getLastDayForMonth(document.itemBatchForm.expmon.value,
										convertTwoDigitYear(document.itemBatchForm.expyear.value));
			} else {
				if (!validateRequired(itemBatchForm.expmon, 'Month of Expiry is required')) return false;
				if (!validateRequired(itemBatchForm.expyear, 'Year of Expiry is required')) return false;
				//document.itemBatchForm.exp_dt.value = "";
			}
		  	if (expDtMon.value != '' && expDtYr.value != '') {
		        var month = expDtMon.value;
		        var year1 = expDtYr.value;
		        var exdate = getLastDayForMonth(month, convertTwoDigitYear(year1));
		        var dateOfExpiry = parseDateStr(exdate);
		        var daysToExpire = daysDiff(getDatePart(getServerDate()), dateOfExpiry);
		        if ((daysToExpire < 0)) {
		            showMessage("js.stores.procurement.expirydate.past");
		            expDtMon.focus();
		            return false;
		        }
		    }
			if (!chkExpireDate(expDtYr, expDtMon, typeOfAction, noOfDays)) {
			   	return false;
			}
			if ( !validateMRP() ){
				return false;
			}

			return true;
		}

		function convertTwoDigitYear(year) {
		    // convert 2 digit years intelligently
		    if (year == '') return year;
		    var now = new Date();
		    var century = now.getFullYear();
		    var s = century.toString();
		    var yearPrefix = s.substring(0,2);
		    return (yearPrefix+year);
        }

       function validateMRP(){
    	   var doAllowStatus = '${doAllowStatus}';
    	   if(!eval(doAllowStatus)) {
			  	var mrpObj = itemBatchForm.mrp;
			   	if (itemBatchForm.billable.value == 'true' && parseFloat(itemBatchForm.mrp.value) == 0) {
					alert(itemBatchForm.medicine_name.value +" is Billable item, \n MRP Should not be zero");
					itemBatchForm.mrp.focus();
					return false;
				}
			   	if (parseFloat(itemBatchForm.mrp.value)<parseFloat(itemBatchForm.package_cp.value) && itemBatchForm.billable.value == 'true') {
			       alert("MRP " + itemBatchForm.mrp.value + " should be greater than Rate " + formatAmountObj(itemBatchForm.package_cp));
			       itemBatchForm.mrp.focus();
			       return false;
			  	}
    	   }
			return true;
       }
</script>

</head>
<body>
<form action="StoreItemBatchDetails.do" method="POST" name="itemBatchForm" autocomplete="off">
	<input type="hidden" name="_method" value="updateItemBatchDetails">
	<input type="hidden" name="item_batch_id" id="item_batch_id" value="${itemBatchBean.map.item_batch_id}"/>
	<input type="hidden" name="dept_id" id="dept_id" value="${itemBatchBean.map.dept_id}"/>

	<h1><insta:ltext key="storemgmt.edititembatchdetails.edit.edititembatchdetails"/></h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel"><insta:ltext key="storemgmt.edititembatchdetails.edit.itembatchdetails"/></legend>

		<table class="formtable">
			<tr>
				<td class="formlabel"><insta:ltext key="storemgmt.edititembatchdetails.edit.itemname"/>:</td>
				<td class="forminfo">
					<label id="medicine_name_lbl">${itemBatchBean.map.medicine_name}</label>
					<input type="hidden" name="medicine_name" value="${itemBatchBean.map.medicine_name}"/>
				</td>
				<td class="formlabel"><insta:ltext key="storemgmt.edititembatchdetails.edit.category"/>:</td>
				<td class="forminfo">
					<label id="medicine_category_lbl">${itemBatchBean.map.category}</label>
				</td>
				<td class="formlabel"><insta:ltext key="storemgmt.edititembatchdetails.edit.manufacturer"/></td>
				<td class="forminfo">${itemBatchBean.map.manf_name}</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="storemgmt.edititembatchdetails.edit.batch.serialno"/>:</td>
				 <td class="forminfo">
				 	<label id="medicine_batch_lbl">${itemBatchBean.map.batch_no}</label>
				 </td>
				 <td class="formlabel"><insta:ltext key="storemgmt.edititembatchdetails.edit.expiry.mmyy.in.brackets"/>:</td>
				 <td >
				 	<input type="text" name="expmon" id="expmon" value='<fmt:formatDate value="${itemBatchBean.map.exp_dt}" pattern="MM"/>' class="timefield" maxlength="2" onblur="chkMon();" onkeypress="return enterNumOnlyzeroToNine(event);">-
				 	<input type="text" name="expyear" id="expyear" value='<fmt:formatDate value="${itemBatchBean.map.exp_dt}" pattern="yy"/>' class="timefield" maxlength="2" onkeypress="return enterNumOnlyzeroToNine(event);" onblur="chkYear();">
				 	<input type="hidden" name="exp_dt" value='<fmt:formatDate value="${itemBatchBean.map.exp_dt}" pattern="dd-MM-yyyy"/>'>
				 </td>
				 <td class="formlabel"><insta:ltext key="storemgmt.edititembatchdetails.edit.bin.rack"/>:</td>
				 <td class="forminfo">${itemBatchBean.map.bin}</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="storemgmt.edititembatchdetails.edit.unituom"/>:</td>
				 <td class="forminfo">${itemBatchBean.map.issue_units}</td>
				 <td class="formlabel"><insta:ltext key="storemgmt.edititembatchdetails.edit.packageuom"/>:</td>
				 <td class="forminfo">${itemBatchBean.map.package_uom}</td>
				 <td class="formlabel"><insta:ltext key="storemgmt.edititembatchdetails.edit.packagesize"/>:</td>
				 <td class="forminfo">${itemBatchBean.map.issue_base_unit}</td>
			</tr>
			<tr>
				<c:if test="${!doAllowStatus}">
					<td class="formlabel"><insta:ltext key="storemgmt.edititembatchdetails.edit.mrp"/>:</td>
					<td>
						<input type="text" name="mrp" id="mrp" class="number" onkeypress="return enterNumAndDot(event);" value="${itemBatchBean.map.mrp}" onchange="return formatAmountObj(this);"/>
						<input type="hidden" name="package_cp" value="${itemBatchBean.map.package_cp }"/>
						<input type="hidden" name="orig_mrp" value="${itemBatchBean.map.mrp }"/>
						<input type="hidden" name="billable" value="${itemBatchBean.map.billable }"/>
					</td>
				</c:if>
				<c:if test="${prefVat == 'Y' && !doAllowStatus}">
					<td class="formlabel"><insta:ltext key="storemgmt.edititembatchdetails.edit.${taxLabel}"/>:</td>
					<td class="forminfo">${itemBatchBean.map.tax_rate}</td>
				</c:if>
				<td class="formlabel"><insta:ltext key="storemgmt.edititembatchdetails.edit.qtyavbl"/>:</td>
				<td class="forminfo">${itemBatchBean.map.qty}</td>
			</tr>
		</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="U" onclick="return validate();"><b><u><insta:ltext key="storemgmt.edititembatchdetails.edit.u"/></u></b><insta:ltext key="storemgmt.edititembatchdetails.edit.pdate"/></button>
		<c:if test="${not empty urlRightsMap.pharma_view_store_item_batch_list && urlRightsMap.pharma_view_store_item_batch_list == 'A'}">
			<a href="${cpath}/stores/StoreItemBatchDetailsList.do?_method=list&sortOrder=medicine_name&sortReverse=false"><insta:ltext key="storemgmt.edititembatchdetails.edit.storeitembatcheslist"/></a>
		</c:if>
	</div>
</form>

</body>
</html>
