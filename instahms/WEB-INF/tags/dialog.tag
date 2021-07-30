<%@ tag dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="insta" tagdir="/WEB-INF/tags" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="title" required="false" %>
<%@ attribute name="fieldDefs" required="false" %>
<%@ attribute name="data" required="false" type="java.util.List"%>

<%--
<%@ attribute name="rowTemplate" required="true" fragment="true" %>
--%>
<%--
	Generates a inline-editable data table .
	The row of the table is determined by the templateRow attribute. 
	The data provided will be pre-filled into the table.
	
--%>
<form name="patientApprovalDialogForm">
<div id="${id}" style="visibility:hidden;">
<div class="bd">
<div id="PatientApprovalDialogFields">
<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">${title}</legend>
	<table class="formtable" id="dialogTable" style="width:100%">
		<jsp:doBody/>
	</table>
</fieldset>
</div>
<table style="margin-top: 10">
	<tr>
		<td>
			<button type="button" accesskey="A"
				onclick="onDialogSave();">
				<b><u><insta:ltext 
				key="insurance.patientapprovallist.patientapprovals.a"/></u></b><insta:ltext 
				key="insurance.patientapprovallist.patientapprovals.dd"/></button>
		</td>
		<td>
			<input type="button" value="Cancel" onclick="onDialogCancel();" />
		</td>
		<%--
		<td>
			<button type="button" id="prevDialog" accesskey="V" onclick="onDialogPrevNext(false);" disabled>
			&lt;&lt; <insta:ltext key="insurance.patientapprovallist.patientapprovals.pre"/><u><b><insta:ltext 
			key="insurance.patientapprovallist.patientapprovals.v"/></b></u><insta:ltext 
			key="insurance.patientapprovallist.patientapprovals.ious"/></button>
		</td>
		<td>
			<button type="button" id="nextDialog" accesskey="N" onclick="onDialogPrevNext(true);" disabled>
			<u><b><insta:ltext 
			key="insurance.patientapprovallist.patientapprovals.n"/></b></u><insta:ltext 
			key="insurance.patientapprovallist.patientapprovals.ext"/> &gt;&gt;</button>
		</td>
		--%>
	</tr>
</table>
</div>
</div>
</form>