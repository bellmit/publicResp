<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"
	isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Regn Cards - Insta HMS</title>
<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="master/registrationcard/registrationcard.js" />

</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<body onload="getDisplay();">

	<h1>${param._method eq 'add' ? 'Add' : 'Edit'} Registration Card Print</h1>
	<insta:feedback-panel/>
	<html:form action="/master/RegistrationCards.do" method="POST"
	enctype="multipart/form-data" onsubmit="return validate();">
		<input type="hidden" name="from" value="${ifn:cleanHtmlAttribute(param.from)}" />
		<input type="hidden" name="cardId" value="${ifn:cleanHtmlAttribute(param.card_id)}" />
		<input type="hidden" name="flag" value="${ifn:cleanHtmlAttribute(param.flag)}" />
		<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
		<fieldset class="fieldSetBorder">
			<table class="formtable">

				<tr>
					<td class="formlabel">Card Name:</td>
					<td><input type="text" name="card_name" id="card_name"
						maxlength="50" value="${ifn:cleanHtmlAttribute(bean.map.card_name)}"/></td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr>
					<td class="formlabel">Visit Type:</td>
					<td><insta:selectoptions name="visit_type" value="${bean.map.visit_type}"
						opvalues="A,I,O" optexts="(All),IP,OP" /></td>
				</tr>

				<tr>
					<td class="formlabel">Rate Plans:</td>
					<td><select name="rate_plan" onchange="checkDuplicate();" class="dropdown">
						<option value="">(All)</option>
						<c:forEach var="ratePlan" items="${ratePlanlists}">
							<c:choose>
								<c:when test="${bean.map.rate_plan == ratePlan.map.org_id}">
									<option value="${ratePlan.map.org_id}" selected="selected">${ratePlan.map.org_name}</option>
								</c:when>
								<c:otherwise>
									<option value="${ratePlan.map.org_id}">${ratePlan.map.org_name}</option>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</select>
					</td>
				</tr>

				<tr >
					<td class="formlabel">Status:</td>
					<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I"
						optexts="Active,InActive" /></td>
				</tr>

				<tr>
					<td class="formlabel" style="white-space:nowrap">Custom Registration Card Template:</td>
					<td><html:file property="custom_reg_card_template" /></td>
					<td >
						<c:url var="cardUrl" value="RegistrationCards.do">
							<c:param name="_method" value="showCustomerRegcard"/>
							<c:param name="cardId" value="${bean.map.card_id}"/>
						</c:url>
						<c:choose>
							<c:when test="${param._method == 'add'}">View</c:when>
							<c:otherwise>
								<a href="${cardUrl}" target="_blank">View</a>
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
				<tr>
					<td class="formlabel" style="white-space:nowrap">Odt File:</td>
					<td><html:file property="odtFile" /></td>
					<td >
						<c:url var="odtUrl" value="RegistrationCards.do">
							<c:param name="_method" value="getOdtFile"/>
							<c:param name="cardId" value="${bean.map.card_id}"/>
						</c:url>
						<c:choose>
							<c:when test="${empty bean.map.odt_file}">Download</c:when>
							<c:otherwise>
								<a href="${odtUrl}" target="_blank">Download</a>
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
				<c:if test="${param._method == 'show'}">
					<tr>
						<td class="formlabel">Last updated User: </td>
						<td class="forminfo">${bean.map.user_name}</td>
					</tr>
					<tr>
						<td class="formlabel">Modified Date: </td>
						<td class="forminfo"><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${bean.map.mod_time}"/></td>
					</tr>
				</c:if>

			</table>
		</fieldset>

		<div class="screenActions">
			<button type="submit" accesskey="S" name="save" id="save" ><b><u>S</u></b>ave</button>
			|
			<c:if test="${param._method != 'add'}">
				<a href="javascript:void(0);" onclick="window.location.href='${cpath}/master/RegistrationCards.do?_method=add&from=add'">Add</a>
			|
			</c:if>
			<a href='#' onclick="docancel()">Registration Card List</a>

		</div>


	</html:form>

	<script>
		var regCardJSON=<%= request.getAttribute("selectedRegcardList") %>;
		var cardsList = <%= request.getAttribute("avlCardsList") %>;

		function docancel()
		{
			window.location.href="${cpath}/master/RegistrationCards.do?_method=list&sortOrder=card_name&sortReverse=false&status=A";
		}
	</script>

</body>
</html>
