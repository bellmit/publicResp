<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<jsp:useBean id="currentDate" class="java.util.Date"/>
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.vieworeditbloodtransfusionslist"/></title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<insta:link type="script" file="ajax.js"/>
<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="BloodTransfusions/bloodtransfusions.js"/>

<script>
</script>
<style type="text/css">
	.deletedRow{
		background-color:#EAEAEA; cursor:pointer;
		border-bottom:1px #666 solid;  border-right:1px #999 solid;
		padding:5px 10px 4px 10px;  color:#707070;
	}
</style>
<insta:js-bundle prefix="clinicaldata.bloodtransfusions"/>
<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
<insta:js-bundle prefix="clinicaldata.common"/>
</head>
<body onload="init();">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:choose>
<c:when test="${param._method=='add' && empty param.mr_no}">
	<h1 style="float: left"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.addbloodtransfusiondetails"/></h1>
	<c:url var="url" value="/clinical/BloodTransfusions.do"/>
	<insta:patientsearch fieldName="mr_no" searchUrl="${url}" searchMethod="show" searchType="mrNo" />
	<form name="BloodTransfusionsForm" action="${cpath}/clinical/BloodTransfusions.do" method="post">
	<input type="hidden" name="_method" value="saveTranfusionDetails">
	<input type="hidden" name="_searchMethod" value="show"/>
	<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">
</c:when>
<c:otherwise>
<c:set var="addText"><insta:ltext key="clinicaldata.common.addoredit.add"/></c:set>
		<c:set var="editText"><insta:ltext key="clinicaldata.common.addoredit.edit"/></c:set>

	<h1>${empty tranfusionInformationList ? addText : editText } <insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.bloodtransfusionsdetails"/></h1>
	<form name="BloodTransfusionsForm" action="${cpath}/clinical/BloodTransfusions.do" method="post">
	<input type="hidden" name="_method" value="saveTranfusionDetails">
	<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">
</c:otherwise>
</c:choose>
<insta:feedback-panel/>
<insta:patientgeneraldetails mrno="${param.mr_no}" addExtraFields="true" showClinicalInfo="true"/>
	<c:if test="${not empty param.mr_no}">
		<br>
		<c:choose>
			<c:when test="${empty dataAsOfDate}">
				<c:set var="valueDate">
					<fmt:formatDate pattern="dd-MM-yyyy" value="${currentDate}"/>
				</c:set>
			</c:when>
			<c:otherwise>
				<c:set var="valueDate">
					<fmt:formatDate pattern="dd-MM-yyyy" value="${dataAsOfDate}"/>
				</c:set>
			</c:otherwise>
		</c:choose>
		<fieldset class="fieldsetborder">
			<div>
				<table class="formtable">
					<tr>
						<td class="formlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.dataasof"/></td>
						<td class="forminput"><insta:datewidget name="data_as_of_date" id="data_as_of_date" value="${valueDate}"/></td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
				</table>
			</div>
			<br>
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr>
					<th class="formlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.transfusiondate"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.type"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.bloodbank"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.batchno"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.expirydate"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.unitstransferred"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.hbsaghcvhiv"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.reaction"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.remarks"/></th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
				</tr>
				<c:set var="length" value="${fn:length(tranfusionInformationList)}"/>
				<c:forEach var="i" begin="1" end="${length+1}" varStatus="loop">
					<c:set var="record" value="${tranfusionInformationList[i-1]}"/>
					<c:if test="${empty record}">
						<c:set var="style" value='style="display:none"'/>
					</c:if>
					<tr ${style}>
						<td class="formlabel">
							<label><fmt:formatDate pattern="dd-MM-yyyy" value="${record.map.transfusion_date}"/></label>
							<input type="hidden" name="transfusion_date" value='<fmt:formatDate pattern="dd-MM-yyyy" value="${record.map.transfusion_date}"/>'/>
							<input type="hidden" name="transfusion_id" value="${ifn:cleanHtmlAttribute(param.transfusion_id)}"/>
							<input type="hidden" name="transfusion_details_id" value="${record.map.transfusion_details_id}"/>
						</td>
						<td><label>${record.map.transfusion_type}</label>
							<input type="hidden" name="transfusion_type"  value="${record.map.transfusion_type}">
						</td>
						<td>
							<label>${record.map.blood_bank}</label>
							<input type="hidden" name="blood_bank"  value="${record.map.blood_bank}">
						</td>
						<td>
							<label>${record.map.batch_no}</label>
							<input type="hidden" name="batch_no" id="batch_no" value="${record.map.batch_no}">
						</td>
						<td>
							<label><fmt:formatDate pattern="dd-MM-yyyy" value="${record.map.expiry_date}"/></label>
							<input type="hidden" name="expiry_date" id="expiry_date" value="<fmt:formatDate pattern="dd-MM-yyyy" value='${record.map.expiry_date}'/>">
						</td>
						<td style="text-align:center">
							<label>${record.map.no_blood_units_transfused}</label>
							<input type="hidden" name="no_blood_units_transfused"  value="${record.map.no_blood_units_transfused}">
						</td>
						<td>
							<label>${record.map.check_for_hbsag_hcv_hiv == 'N' ? 'Negative' : ''}</label>
							<input type="hidden" name="check_for_HbsAg_HCV_HIV"  value="${record.map.check_for_hbsag_hcv_hiv}">
						</td>
						<td>
							<insta:truncLabel value="${record.map.transfusion_reaction}" length="30"/>
							<input type="hidden" name="transfusion_reaction"  value="${record.map.transfusion_reaction}"/>
						</td>
						<td>
						   	<insta:truncLabel value="${record.map.remarks}" length="30"/>
							<input type="hidden" name="remarks"  value="${record.map.remarks}">
							 <input type="hidden" name="hdeleted" value="false">
						</td>
						<td>
							<a title='<insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.addbloodtransfusiondetails"/>'>
								<img src="${cpath}/icons/Delete.png" class="imgDelete button" onclick="deleteItem(this)"/>
							</a>
						<td>
							<a title='<insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.editbloodtransfusiondetails"/>' >
								<img src="${cpath}/icons/Edit.png" class="button" id="editIcon${index}" name="editIcon" onclick="openEditDialogBox(this)"/>
							</a>
						</td>
						<td>&nbsp;</td>
					</tr>
				</c:forEach>
				</table>
				<table class="addButton">
					<tr>
						<td align="right">
							<button type="button" name="btnAddItem" id="btnAddItem" title='<insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.addbloodtransfusiondetails"/>'
									onclick="openDialogBox();"
									accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
						</td>
					</tr>
				</table>
		</fieldset>
		<table >
			<tr>
				<td style="text-align: left"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.lastupdateduserordate"/></td>
				<td>&nbsp;</td>
				<c:if test="${param._method == 'show'}">
					<td style="text-align: left;color:#444;font-weight: bold;">
						${ifn:cleanHtml(userName)}/
						<fmt:formatDate pattern="dd-MM-yyyy" value="${mod_time}"/>
						<fmt:formatDate pattern="HH:mm:ss" value="${mod_time}"/>
					</td>
				</c:if>
			</tr>
		</table>
		<div class="screenActions">
			<button type="submit" accesskey="A" onclick="return validateForm()"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.s"/><b><u><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.a"/></u></b><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.ve"/></button>
			| <a href="${cpath}/clinical/BloodTransfusions.do?_method=list"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.bloodtransfusionlist"/></a>
			| <a href="${cpath}/dialysis/PreDialysisSessions.do?_method=showDialysis&mr_no=${ifn:cleanURL(param.mr_no)}"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.predialysis"/></a>
		</div>
	</c:if>
		<div id="bloodTransfusionDialog" style="visibility:hidden">
		<div class="hd" id="itemdialogheader"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.bloodtransfusion"/>:</div>
		<div class="bd">
			<fieldset class="fieldsetborder">
			<legend class="fieldsetlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.transfusiondetails"/></legend>
			<table class="formtable" cellpadding="0" cellspacing="0">
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.transfusiondate"/>:</td>
					<td colspan="2">
						<input type="hidden" name="dialogId">
						<insta:datewidget name="dialog_transfusion_date" id="dialog_transfusion_date" value=""/>
						<span class="star">*</span>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.transfusiontype"/>:
					</td>
					<td colspan="2">
						<select name="dialog_transfusion_type" id="dialog_transfusion_type" class="dropdown">
							<option value="">${dummyvalue}</option>
							<option value="Blood"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.blood"/></option>
							<option value="Plasma"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.plasma"/></option>
							<option value="Platelets"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.platelets"/></option>
						</select>
						<span class="star">*</span>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.bloodbank"/>:</td>
					<td colspan="2">
						<input type="text" name="dialog_blood_bank" id="dialog_blood_bank" value="" maxlength="50"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.batchno"/></td>
					<td style="width:105px">
						<input type="text" name="dialog_batch_no" id="dialog_batch_no" value="" maxlength="30">
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.expirydate"/>:</td>
					<td colspan="2">
						<insta:datewidget name="dialog_expiry_date" id="dialog_expiry_date" value=""/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.transferredunits"/>:</td>
					<td>
						<input type="text" name="dialog_transferred_units" id="dialog_transferred_units" value="" style="width:60px" onkeypress="return enterNumOnly(event)">
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.checkforhbsaghcvhiv"/>:</td>
					<td colspan="2">
						<label><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.negative"/></label>
						<input type="hidden" name="dialog_HbsAg_HCV_HIV" id="dialog_HbsAg_HCV_HIV" value="N">
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.reaction"/>:</td>
					<td colspan="2">
						<textarea rows="2" cols="15" id="dialog_reaction" onblur="checkLength(this,2000,'Reaction')"></textarea>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.remarks"/>:</td>
					<td colspan="2">
						<textarea rows="2" cols="15" id="dialog_remarks" onblur="checkLength(this,2000,'Remarks')"></textarea>
					</td>
				</tr>
			</table>
			</fieldset>
			<table>
				<tr>
					<td>
						<button type="button" name="Save" accesskey="S" onclick="addRecord();"><b><u><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.adds"/></u></b><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.ave"/></button>
						<button type="button" name="Cancel" accesskey="C" onclick="cancelDialog();"><b><u><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.c"/></u></b><insta:ltext key="clinicaldata.bloodtransfusion.addoreditbloodtransfusions.ancel"/></button>
					</td>
				</tr>
			</table>
		</div>
		</div>
</form>

</body>
</html>