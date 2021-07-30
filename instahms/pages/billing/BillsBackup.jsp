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
<c:set var="billLaterPrintValues" value="BILL-DET-ALL,BILL-CHS-ALL,BILL-SUM-ALL,BILL-DET-SNP,BILL-DET-DIA,BILL-DET-OPE,BILL-DET-MED,CUSTOM-BUILTIN_HTML,CUSTOM-BUILTIN_TEXT"/>
<c:set var="billLaterPrintTexts" value="Bill - Detailed,Bill - Charge Summary,Bill - Group Summary,Bill Extract - Services,Bill Extract - Diagnostics,Bill Extract - Operations,Bill Extract - Pharmacy,Built-in Default HTML template,Built-in Default Text template"/>
<c:set var="billNowPrintValues" value="BILL-DET-ALL,CUSTOM-BUILTIN_HTML,CUSTOM-BUILTIN_TEXT"/>
<c:set var="billNowPrintTexts" value="Bill - Detailed,Built-in Default HTML template,Built-in Default Text template"/>
<c:set var="billLaterAllValues" value="${billLaterPrintValues}"/>
<c:set var="billLaterAllTexts"  value="${billLaterPrintTexts}"/>
<c:set var="billNowAllValues" value="${billNowPrintValues}"/>
<c:set var="billNowAllTexts"  value="${billNowPrintTexts}"/>
<c:set var="billLaterPrintDefault" value="${genPrefs.billLaterPrintDefault}"/>
<c:set var="billNowPrintDefault" value="${genPrefs.billNowPrintDefault}"/>
<html>
<head>
<title><insta:ltext key="billing.billsbackup.export.billsbackup.instahms"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>

	<script>



 function validateDate(){

	var fromDate=document.billsBackupForm.fromDate.value;
	var toDate=document.billsBackupForm.toDate.value;
	if(fromDate == "" || toDate == ""){
	showMessage("js.billing.billsbackup.fromtodate.notempty");
	return false;
	}
	var fromDateArray=fromDate.split("-");
	var toDateArray=toDate.split("-");
	if(fromDateArray[1] != toDateArray[1] || fromDateArray[2] != toDateArray[2]){
	showMessage("js.billing.billsbackup.fromtodate.notexceedmonth");
	return false;
	}
	return true;

 }



 function onChangePrintType() {

    var theForm=document.billsBackupForm;
	var billType=getRadioSelection(theForm.printType);

	if (billType == "HOSP") {
		theForm._method.value="getBillsBackup";
		makeEnabled();
	} else {
		theForm._method.value="getPharmacyBillsBackUp";
		makeDisabled();
	}
 }
 function makeDisabled() {

      var theForm=document.billsBackupForm;
	  theForm.billType.disabled=true;
	  theForm.patientType.disabled=true;
	  theForm.printBillBn.disabled=true;
	  theForm.printBillBl.disabled=true;
 }

 function makeEnabled() {

      var theForm=document.billsBackupForm;
 	  theForm.billType.disabled=false;
	  theForm.patientType.disabled=false;
	  theForm.printBillBn.disabled=false;
	  theForm.printBillBl.disabled=false;
 }

	</script>
	<insta:js-bundle prefix="billing.billsbackup"/>
</head>
<body>
<c:set var="export">
	<insta:ltext key="billing.billsbackup.export.export"/>
</c:set>
<div class="pageHeader"><insta:ltext key="billing.billsbackup.export.billsbackup"/></div>
<insta:feedback-panel/>
<form name="billsBackupForm" action="BillsBackup.do" onsubmit="return validateDate();">
<input type="hidden" name="_method" value=getBillsBackup>
<fieldset class="fieldSetBorder">
	<table  class="formtable" align="left">
	<tr>
			<td style="padding-bottom: 7px" align="right">
				<input type="radio" name="printType" value="HOSP" checked
					onclick="onChangePrintType()"/><insta:ltext key="billing.billsbackup.export.hospitalbills"/></td>
			<td><input type="radio" name="printType" value="PHARMA"
					onclick="onChangePrintType()"/><insta:ltext key="billing.billsbackup.export.pharmacybills"/></td>
	</tr>
	<tr>
		    <td class="formlabel"><insta:ltext key="billing.billsbackup.export.billfinalizeddatefrom"/>:</td>
			<td><insta:datewidget name="fromDate" value="today"/></td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
	</tr>
	<tr>
		    <td class="formlabel"><insta:ltext key="billing.billsbackup.export.billfinalizeddateto"/>:</td>
			<td><insta:datewidget name="toDate" value="today"/></td>
	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="billing.billsbackup.export.billtype"/> :</td>
			<td>
				<select name="billType" id="billType" class="dropdown">
					<option value="*"><insta:ltext key="billing.billsbackup.export.all"/></option>
					<option value="P"><insta:ltext key="billing.billsbackup.export.billnow"/></option>
					<option value="C"><insta:ltext key="billing.billsbackup.export.billlater"/></option>
				</select>
			</td>
	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="billing.billsbackup.export.patienttype"/> :</td>
			<td>
				<select name="patientType" id="patientType" class="dropdown">
					<option value="*"><insta:ltext key="billing.billsbackup.export.all"/></option>
					<option value="i"><insta:ltext key="billing.billsbackup.export.ip"/></option>
					<option value="o"><insta:ltext key="billing.billsbackup.export.op"/></option>
					<option value="t"><insta:ltext key="billing.billsbackup.export.incomingdiag"/></option>
				</select>
			</td>
	</tr>

   <c:if test="${not empty templateList}">
	  <c:forEach var="temp" items="${templateList}">
		 <c:set var="templateValues" value="${templateValues},CUSTOM-${temp.map.template_name}"/>
		 <c:set var="templateTexts" value="${templateTexts},${temp.map.template_name}"/>
	  </c:forEach>
   </c:if>

   <c:set var="billLaterAllValues" value="${billLaterAllValues},${templateValues}"/>
   <c:set var="billLaterAllTexts"  value="${billLaterAllTexts},${templateTexts}"/>
   <c:set var="billNowAllValues"   value="${billNowAllValues},${templateValues}"/>
   <c:set var="billNowAllTexts"    value="${billNowAllTexts},${templateTexts}"/>

	<tr>
	    	<td class="formlabel"><insta:ltext key="billing.billsbackup.export.billnowprintformat"/> :</td>
	    	<td><insta:selectoptions name="printBillBn" id="bnPrintFormat"
			opvalues="${billNowAllValues}" optexts="${billNowAllTexts}" value="${billNowPrintDefault}"/>
		</td>
	</tr>
	<tr>
	    	<td class="formlabel"><insta:ltext key="billing.billsbackup.export.billlaterprintformat"/> :</td>
	    	<td><insta:selectoptions name="printBillBl" id="blPrintFormat"
			opvalues="${billLaterAllValues}" optexts="${billLaterAllTexts}" value="${billLaterPrintDefault}"/>
		   </td>
	</tr>
	<tr>
	    	<td class="formlabel"><insta:ltext key="billing.billsbackup.export.printsettings"/>:</td>
	    	<td><insta:selectdb name="printerType" table="printer_definition" filtercol="print_mode" filtervalue="P"
				valuecol="printer_id"  displaycol="printer_definition_name" value="${pref.map.printer_id}"/>
		</td>
	</tr>
	<tr>
	    	<td class="formlabel"> </td>
	    	<td><input type="Submit" name="export" value="${export}"  class="button"></td>
	</tr>
	</table>
  </fieldset>
 </form>
</body>
</html>
