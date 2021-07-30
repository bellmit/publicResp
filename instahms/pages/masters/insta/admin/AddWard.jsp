<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insta HMS</title>
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="js" file="masters/wardandbedmaster.js" />
<insta:link type="js" file="ajax.js" />
<script>
	var cpath = '${cpath}';
	var multiCenters = ${multiCenters };
	function doClose() {
		window.location.href = "${cpath}/pages/masters/insta/admin/WardAndBedMasterAction.do?method=getWardandBedMaster";
	}


</script>

</head>
<body onload="init()">
<html:form
	action="/pages/masters/insta/admin/WardAndBedMasterAction.do?" >
	<c:choose>
		<c:when test="${! empty ward}">
			<input type="hidden" name="method" value="editWardDetails" />
			<input type="hidden" name="ward" value="${ifn:cleanHtmlAttribute(ward)}"/>
			<input type="hidden" name="wardId" id="wardId" value="${ifn:cleanHtmlAttribute(wardId)}"/>
			<input type="hidden" name="dummyValue" value="editWard">
		</c:when>
		<c:otherwise>
			<input type="hidden" name="method" value="${ifn:cleanHtmlAttribute(method)}"/>
			<input type="hidden" name="dummyValue" value="insert">
		</c:otherwise>
	</c:choose>

	<table class="formtable" width="100%" >
		<tr>
			<h1>Ward Details</h1>
		</tr>
		<insta:feedback-panel/>

		<c:if test="${method eq 'editWardDetails'}">
		<tr>
			<td>
			<fieldset class="fieldSetBorder">
			<table class="formtable" width="100%">
				<tr>
					<td class="formlabel">Ward Name :</td>
					<td class="forminfo"><b>${wardName} </b></td>
					<td class="formlabel">Description:</td>
					<td><input type="text" name="description"
						value="${description}" onblur="capWords(description)"></td>
					<td class="formlabel">Status: </td>
					<td>
						<select name="wardStatus" id="wardStatus" style="width: 8em" class="dropdown" onchange="return canBeInactivate('${ifn:cleanJavaScript(wardId)}', '', this);">
							<option value="A" <c:if test="${status eq 'A'}" > Selected </c:if>>Active</option>
							<option value="I" <c:if test="${status eq 'I'}" > Selected </c:if>>Inactive</option>
						</select>
					</td>
					<input type="hidden" name="wardId" value="${ifn:cleanHtmlAttribute(wardId)}" />
				</tr>

				<tr>
					<c:if test="${multiCenters }">
						<td class="formlabel">Center:</td>
						<td class="forminfo">
							<insta:getCenterName center_id="${center_id}"/>
						</td>
					</c:if>
					<td></td>
				</tr>
                <tr>
                			<td class="formlabel">Allowed Gender: </td>
                			<td>
                            						<select name="allowedGender" id="allowedGender" style="width: 8em" class="dropdown" onchange="return canBeInactivate('${ifn:cleanJavaScript(wardId)}', '', this);">
                            							<option value="ALL" <c:if test="${allowedGender eq 'ALL'}" > Selected </c:if>>All</option>
                            							<option value="M" <c:if test="${allowedGender eq 'M'}" > Selected </c:if>>Male</option>
                            							<option value="F" <c:if test="${allowedGender eq 'F'}" > Selected </c:if>>Female</option>
                            						</select>
                            					</td>
                		</tr>
				<tr>
					<td colspan="3" >
					<table class="dataTable" cellpadding="0" cellspacing="0" width="100%">
						<tr>
							<th>Bed Type</th>
							<th>No.Of Beds</th>
							<th>Add More Beds</th>
						</tr>

						<c:forEach var="row" items="${wardDetils}">
							<tr>
								<td>${row.map.bed_type}</td>
								<td align="center">${row.map.count}</td>
								<td><input type="text" size='3' name="noOfBedToAdd"
									onkeypress="return enterNumOnlyzeroToNine(event)"
									class="number"></td>

								<input type="hidden" name="bedType" value="${row.map.bed_type}" />
							</tr>
						</c:forEach>
					</table>
					</td>
				</tr>
			</table>
			</fieldset>
			</td>
		</tr>
		</c:if>

		<c:if test="${method eq 'insertNewWardDetails'}">
		<tr>
			<%-- New Ward Screen--%>
			<td>
			<fieldset class="fieldSetBorder" >
			<table class="formtable" width="100%">
				<tr>
					<td class="formlabel">Ward Name:</td>
					<td><input type="text" name="wardName"
						onblur="capWords(wardName)" value="${ifn:cleanHtmlAttribute(ward)}"></td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td class="formlabel">Status:</td>
					<td><select name="wardStatus" style="width: 8em" class="dropdown">
						<option value="A">Active</option>
						<option value="I">Inactive</option>
					</select></td>
				</tr>
				<tr>
					<td class="formlabel">Description:</td>
					<td><input type="text" name="description"
						onblur="capWords(description)"></td>
				</tr>
				<c:if test="${multiCenters }">
				<tr>
					<td class="formlabel">Center:</td>
					<td class="forminput">
						<select name="center_id" class="dropdown" id="center_id">
							<option value="">--Select--</option>
							<c:forEach items="${centers}" var="center">
								<option value="${center.map.center_id }">${center.map.center_name }</option>
							</c:forEach>
						</select>
					</td>
				</tr>
				</c:if>
				<tr>
                                			<td class="formlabel">Allowed Gender: </td>
                                			<td>
                                            						<select name="allowedGender" id="allowedGender" style="width: 8em" class="dropdown" onchange="return canBeInactivate('${ifn:cleanJavaScript(wardId)}', '', this);">
                                            							<option value="ALL" <c:if test="${allowedGender eq 'ALL'}" > Selected </c:if>>All</option>
                                            							<option value="M" <c:if test="${allowedGender eq 'M'}" > Selected </c:if>>Male</option>
                                            							<option value="F" <c:if test="${allowedGender eq 'F'}" > Selected </c:if>>Female</option>
                                            						</select>
                                            					</td>
                                		</tr>
				<tr>
					<td colspan="4" ><b><u>Add Beds To The Ward</u></b></td>
				</tr>
				<tr>
					<td colspan="4">
					<table class="formtable" cellpadding="0"
						cellspacing="0" width="100%">
						<tr>
							<td colspan="2"><select name="bedType1" id="bedType1" class="dropdown">
								<option value="">--Select BedType--</option>
								<c:forEach var="item" items="${bedTypes}">
									<option value="<c:out value="${item.map.bed_type_name}"/>">${item.map.bed_type_name}</option>
								</c:forEach>
							</select> <input type="text" size='3' name="noOfBedToAdd1"
								id="noOfBedToAdd1"
								onkeypress="return enterNumOnlyzeroToNine(event)" class="number">
							<button type="button" name="Add a Row" accesskey="A"
								onclick="validateBedType()"><b><u>A</u></b>dd a Row</button></td>
						</tr>

						<tr>
							<td>
							<table class="dashboard" id="prescriptionTable" cellpadding="0" cellspacing="0" width="100%">
								<tr>
									<th>BedType</th>
									<th>No Of Beds</th>
								</tr>
							</table>
							</td>
						</tr>

					</table>
					</td>
				</tr>
			</table>
			</fieldset>
			</td>
		</tr>
		</c:if>
	</table>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate()"><b><u>S</u></b>ave</button>
		|
		<c:if test="${param.method != 'getNewWardScreen'}">
			<a href="${cpath}/pages/masters/insta/admin/WardAndBedMasterAction.do?method=getNewWardScreen">Add New Ward</a>
		|
		</c:if>
		<a href="javaScript:void(0)" onclick="doClose();">Ward And Bed List</a>
	</div>

</html:form>
</body>

</html>
