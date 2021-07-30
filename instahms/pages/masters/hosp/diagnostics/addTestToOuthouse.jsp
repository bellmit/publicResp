<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>
		${ifn:cleanHtml(param._method=='showOuthouseTestDetails'?'Update Diag Outsource':'Add Diag Outsource')} - Insta HMS
	</title>

	<insta:link type="css" file="hmsNew.css" />
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="/master/DiagOutsources/DiagOutsource.js" />
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<script>
		var centerOutsourcesJSON = <%=request.getAttribute("centerOutsourcesJSON") %>;
		
	</script>
</head>
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<body class="yui-skin-sam" onload="init();">
	<div class="pageHeader">
		${ifn:cleanHtml(param._method=='showOuthouseTestDetails'?'Update Diag Outsource':'Add Diag Outsource')}
	</div>
	<insta:feedback-panel/>
	<form name="outHouseForm" method="POST" action="${cpath}/pages/masters/hosp/diagnostics/OutHouseMaster.do">
		<c:set var="bean" value="${outhouseTests[0]}" />
		<input type="hidden" name="_method" id="_method" 
		value="${ifn:cleanHtmlAttribute(param._method=='showOuthouseTestDetails'?'updateTestCharge':'insertTestToOuthouse')}">
		<input type="hidden" name="outsourceDestId" id="outsourceDestId" value="${bean.map.outsource_dest_id}"/>

		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Out Source Details</legend>
			<table class="formtable">
				<tr>
				    <td class="formlabel">Test Name:</td>
					<td class="forminfo">
						<c:choose>
							<c:when test="${param._method=='showOuthouseTestDetails'}">
								${bean.map.test_name}
								<input type="hidden" name="test_id" id="test_id" value="${bean.map.test_id}"/>
							</c:when>
							<c:otherwise>
								<select name="test_id" id="test_id" Class="dropdown">
									<option value="">..Select..</option>
									<c:forEach var="test" items="${testDetails}">
											<option value="${test.TEST_ID}">${test.TEST_NAME}</option>
									</c:forEach>
								</select>
							</c:otherwise>
						</c:choose>
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>					
			</table>
		
		<div class="resultList" style="margin-top: 10px">
			<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="itemsTable" border="0" width="100%" style="margin-top: 8px">
				<tr>
					<th>Source Center</th>
					<th>Outsource</th>
					<th>Charge</th>
					<th>Default</th>
					<th>Status</th>					
					<th style="width: 16px;"></th>
					<th style="width: 16px"></th>
				</tr>
				<c:set var="numOuthouseTests" value="${fn:length(outhouseTests)}"/>
				<c:forEach begin="1" end="${numOuthouseTests+1}" var="i" varStatus="loop">
					<c:set var="testDetail" value="${outhouseTests[i-1].map}"/>
					
					<c:if test="${empty testDetail}">
						<c:set var="style" value='style="display:none"'/>
					</c:if>
					<tr ${style}>
						<td>
							<label>${testDetail.source_center_name}</label>
							<input type="hidden" name="diag_outsource_detail_id" value="${testDetail.diag_outsource_detail_id}" />
							<input type="hidden" name="source_center_id" value="${testDetail.source_center_id}" />
							<input type="hidden" name="status" value="${testDetail.status}" />
							<input type="hidden" name="default_outsource" value="${testDetail.default_outsource}" />
							<input type="hidden" name="diag_test_id" value="${testDetail.test_id}" />
							<input type="hidden" name="charge" value="${testDetail.charge}" />
							<input type="hidden" name="outsource_dest_id" value="${testDetail.outsource_dest_id}" />
							<input type="hidden" name="db_status" value="${testDetail.status}" />
							<input type="hidden" name="added" value="false" />
							<input type="hidden" name="edited" value='false'/>
							<input type="hidden" name="delItem" value='false'/>
						</td>
						<td >
							${testDetail.outsource_name}
						</td>
						<td>
							${testDetail.charge}
						</td>
						<td>
							${testDetail.default_outsource eq 'N' ? 'No' : 'Yes'}			
						</td>
						<td>
							${testDetail.status eq 'A' ? 'Active' : 'InActive' }
						</td>
						<td style="text-align: center">
							<a href="javascript:Cancel Item" onclick="return cancelItem(this);" title="Cancel Item" >
								<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
							</a>
						</td>
						<td style="text-align: center">
							<input type="hidden" name="delPayment" id="delPayment" value="false" />
							<a name="_editAnchor" href="javascript:Edit" onclick="return showEditItemDialog(this);"
								title="Edit Diag Outsource Details">
								<img src="${cpath}/icons/Edit.png" class="button" />
							</a>
						</td>
					</tr>
				</c:forEach>
			</table>
			<table class="addButton" style="height:25px;">
				<tr>
					<td style="width: 16px;text-align: right">
						<button type="button" name="btnAddItem" id="btnAddItem" title="Add Diag Outsource Details (Alt_Shift_+)"
							onclick="showAddItemDialog(this); return false;"
							accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
					</td>					
				</tr>
			</table>			
		</div>
		
		<div id="addItemDialog" style="display: none">
			<div class="bd">
				<div id="addItemDialogFields">
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Add Outsource</legend>
							<table class="formtable">
								<tr>
									<td class="formlabel" >Source Center: </td>
									<td>
										<select name="ad_center_id" id="ad_center_id" class="dropdown" onchange="fillOutsources();">
											<option value="">-- Select --</option>
											<c:forEach items="${centers}" var="center">
												<option value="${center.map.center_id}" >
													${center.map.center_name}
												</option>
											</c:forEach>
										</select>
									</td>
									<td class="formlabel" >Outsource: </td>
									<td> 
										<select name="ad_outsources" id="ad_outsources" value="" class="dropdown">
											<option>-- Select --</option>											
										</select>
									</td>
									<td class="formlabel" >Charge: </td>
									<td>
										<input type="text" id="ad_charge" name="ad_charge" class="number" onkeypress="return enterNumOnly(event);"/>											
									</td>
								</tr>
								<tr>									
									<td class="formlabel">Status: </td>
									<td><insta:selectoptions name="ad_status" id="ad_status" value="" opvalues="A,I" optexts="Active,Inactive" /></td>
									<td class="formlabel">Default: </td>
									<td><insta:selectoptions name="ad_default" id="ad_default" value="" opvalues="N,Y" optexts="No,Yes" /></td>
								</tr>
							</table>
					</fieldset>
				</div>
				<table style="margin-top: 10">
					<tr>
						<td>
							<button type="button" name="Add" id="Add" accesskey="A" >
								<b><u>A</u></b>dd
							</button>
							<input type="button" name="Close" value="Close" id="Close"/>
						</td>
					</tr>
				</table>
			</div>
		</div>
		</fieldset>

		<div id="editItemDialog" style="display: none">
			<input type="hidden" name="editRowId" id="editRowId" value=""/>
			<div class="bd">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Edit Outsource</legend>
						<table class="formtable">
							<tr>
								<td class="formlabel">Source Center: </td>
								<td>
									<select name="ed_center_id" id="ed_center_id" class="dropdown" onchange="fillOutsourcesForEdit();">
										<c:forEach items="${centers}" var="center">
											<option value="${center.map.center_id}" >
												${center.map.center_name}
											</option>
										</c:forEach>
									</select>
								</td>
								<td class="formlabel">Outsource: </td>
								<td>
									<select name="ed_outsources" id="ed_outsources" value="" class="dropdown">
											<option value="">-- Select --</option>
											
									</select>
								</td>
								<td class="formlabel">Charge: </td>
								<td >
									<input type="text" id="ed_charge" name="ed_charge" class="number" onkeypress="return enterNumOnly(event);" />
								</td>
							</tr>
							<tr>								
								<td class="formlabel">Status: </td>
								<td ><insta:selectoptions name="ed_status" id="ed_status" value="" opvalues="A,I" optexts="Active,Inactive" /></td>
								<td class="formlabel">Default: </td>
								<td ><insta:selectoptions name="ed_default" id="ed_default" value="" opvalues="N,Y" optexts="No,Yes" /></td>
							</tr>
						</table>
						<table style="margin-top: 10">
							<tr>
								<td>
									<input type="button" id="editOk" name="editok" value="Ok">
									<input type="button" id="editCancel" name="cancel" value="Cancel" />
									<input type="button" id="editPrevious" name="previous" value="<<Previous" />
									<input type="button" id="editNext" name="next" value="Next>>"/>
								</td>
							</tr>
						</table>
					</fieldset>
			</div>
		</div>
		
		<div class="screenActions">
				<button type="button" name="save"  accesskey="S" onclick="return validateForm();"><b><u>S</u></b>ave</button>
				<c:if test="${param._method=='showOuthouseTestDetails'}">|
					<a href="${cpath}/pages/masters/hosp/diagnostics/OutHouseMaster.do?_method=addNewTestToOutHouse">Add</a>
				</c:if>
		| <a href="${cpath}/pages/masters/hosp/diagnostics/OutHouseMaster.do?_method=getOutHouseMasterDetails&test_status=A">Diag OutSource List</a>
		</div>
	</form>
	</body>
</html>