<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="screenId" value="view_consolidated_bill" />
<html>
	<head>		
		<title>View Consolidated Bill - Insta HMS</title>
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">			
	</head>
	<body>
		<c:choose>
			<c:when test="${(cnsldtdBillDetBean.map.is_consolidated_credit_note)}">
				<h1>View Consolidated Credit Note</h1>
			</c:when>
			<c:otherwise>
				<h1>View Consolidated Bill</h1>
			</c:otherwise>
		</c:choose>
		<insta:feedback-panel/>
	    <insta:patientgeneraldetails mrno="${param.mr_no}" />
	    <form name="viewConsolidatedBillForm" action="${cpath}/billing/consolidatedbill.do" method="POST">
	    	<input type="hidden" name="_method"  value="printConsolidatedBill"/>
	    	<input type="hidden" name="sponsor_id" id="sponsor_id" value=""/>
	    	<input type="hidden" name="template_name" id="template_name" value=""/>
	    	<input type="hidden" name="consolidated_bill_no" id="cnsldtdBillDetBean" value="${cnsldtdBillDetBean.map.consolidated_bill_no}"/>
		    <fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Consolidated Bill Details</legend>			
				<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
					<tr>
						<td class="formlabel" >Consolidated Bill No:</td>						
						<td class="forminfo">${cnsldtdBillDetBean.map.consolidated_bill_no}</td>
						<td class="formlabel" >Status:</td>						
						<td class="forminfo">${cnsldtdBillDetBean.map.status == 'A' ? 'Open' : cnsldtdBillDetBean.map.status == 'F' ? 'Finalized' : 'Closed'}</td>
						<td class="formlabel" >Open Date:</td>						
						<td class="forminfo">${cnsldtdBillDetBean.map.open_date}</td>
					</tr>
					<tr>
						<td class="formlabel" >Amount:</td>						
						<td class="forminfo">${consolidatedBillTotals.amount}</td>
						<td class="formlabel" >Patient Amount:</td>						
						<td class="forminfo">${consolidatedBillTotals.patient_amt}</td>
						<td class="formlabel" >Patient Due:</td>						
						<td class="forminfo">${consolidatedBillTotals.patient_due}</td>
					</tr>
					<tr>
						<td class="formlabel" >Sponsor Amount:</td>						
						<td class="forminfo">${consolidatedBillTotals.sponsor_amount}</td>						
						<td class="formlabel" >Sponsor Due:</td>						
						<td class="forminfo">${consolidatedBillTotals.sponsor_due}</td>
					</tr>
				</table>				
			</fieldset>	
			<legend class="fieldSetLabel" style="margin-top: 8px"></legend>
			<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="ConsolidatedBilltable" border="0" style="margin-top: 8px">
				<tr>
					<th>Visit Id</th>
					<th>Visit Date</th>
					<th>Bill Open Date</th>
                    <th>Sponsor</th>
                    <th>Item</th>
					<th>Total</th>
					<th>Sponsor Amount</th>
					<th>Patient Amount</th>						
				</tr> 
				<c:set var="prevVisit" value=""/>					
				<c:forEach var="bill" items="${consolidatedBillList}" varStatus="st">
					<tr>
						<td>
							<c:choose>
								<c:when test="${prevVisit ==  bill.visit_id}">
								</c:when>
								<c:otherwise>
									${bill.visit_id}
								</c:otherwise>
							</c:choose>
						</td>
						<td>
							<c:choose>
								<c:when test="${prevVisit ==  bill.visit_id}">
								</c:when>
								<c:otherwise>
									${bill.visit_date}
								</c:otherwise>
							</c:choose>							
						</td>
						<td>
							<c:choose>
								<c:when test="${prevbill_no ==  bill.bill_no}">
								</c:when>
								<c:otherwise>
									<a href="<c:out value='BillAction.do?_method=getCreditBillingCollectScreen&mrNo=${ifn:cleanURL(param.mr_no)}&billNo=${bill.bill_no}&visitId=${bill.visit_id}&patient_id=${bill.visit_id}&bill_no=${bill.bill_no}&screenId=${screenId}&main_visitId=${bill.main_visit_id}&is_cn=${cnsldtdBillDetBean.map.is_consolidated_credit_note}'/>"><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${bill.open_date}"/></a>
								</c:otherwise>
							</c:choose>							
						</td>
                        <td>${bill.sponsor_name}</td>
						<td>${bill.item_name}</td>
						<td>${bill.amount}</td>
						<td>${bill.sponsor_amt}</td>
						<td>${bill.patient_amt}</td>
						<c:set var="prevVisit" value="${bill.visit_id}"/>
						<c:set var="prevbill_no" value="${bill.bill_no}"/>
					</tr>
				</c:forEach>
			</table>
			<legend class="fieldSetLabel" style="margin-top: 8px"></legend>
			<table cellpadding="0" cellspacing="0"  border="0" width="100%">
				<tr>
					<td align="left">
						<button type="button" id="printButton" accessKey="P" onclick = "consolidatedBillPrint()">							
							<b><u>P</u></b>rint</button>
						<insta:screenlink screenId="consolidated_bill" extraParam="?_method=list&status=A"
							label="Consolidated Bill List" addPipe="true"/>
					</td>					
					<td align="right">Template List :											
						<select class="dropdown" name="template_id" id="template_id" onchange="onChangeTemplates(this)">
							<option value="" selected>--SELECT--</option>							
							<c:forEach var="template" items="${availableTemplateList}">
							<option value="${fn:escapeXml(template.map.sponsor_id)}">							
								<c:out value="${template.map.template_name}"/>
							</option>
						</c:forEach>
						</select>
					</td>
				</tr>
			</table>	
		</form>		
		<script type="text/javascript">
			function onChangeTemplates(templateListObj){
				document.getElementById('sponsor_id').value = templateListObj.value;
				document.getElementById('template_name').value = templateListObj.options[templateListObj.selectedIndex].text;
			}
			
			function consolidatedBillPrint(){
				var templateId = document.getElementById('template_id').value;
				if(templateId == "" || templateId == null){
					alert("Please select Template List");
					return false;
				}
				var consolidatedBillNo = document.getElementById("cnsldtdBillDetBean").value;
				var sposnorId = document.getElementById('sponsor_id').value;
				var templateName = document.getElementById('template_name').value;				
				var url = cpath + "/billing/consolidatedbill.do?_method=printConsolidatedBill&consolidated_bill_no="+consolidatedBillNo+
					"&sponsor_id="+sposnorId+"&template_name="+templateName;	
				window.open(url);
			} 
		</script>
	</body>
</html>