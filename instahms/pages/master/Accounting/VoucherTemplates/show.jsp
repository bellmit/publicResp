<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>${ifn:cleanHtml(param.title)} - Insta HMS</title>
		<insta:link type="js" file="master/accounting/accountvctemplate.js"/>

	</head>
	<body>
		<h1>${ifn:cleanHtml(param.title)}</h1>

		<insta:feedback-panel/>

		<form action="accountingvouchertemplates.do" method="POST" name="customform">

			<input type="hidden" name="_method" value="update"/>
			<input type="hidden" name="customized" value=${ifn:cleanHtmlAttribute(param.customized)}>
			<input type="hidden" name="resetToDefault" value="false"/>
			<input type="hidden" name="title" value="${ifn:cleanHtmlAttribute(param.title)}">
			<input type="hidden" name="voucher_type" value="${ifn:cleanHtmlAttribute(param.voucher_type)}">

			<table class="formtable">
				<tr>
					<td class="formlabel">Reson for Customization:</td>
					<td>
						<textarea rows="2" cols="40" name="reason" id="reason"><c:out value="${reason}"></c:out></textarea>
					</td>
					<td class="formlabel"></td>
					<td></td>
				</tr>
			</table>

			<div style="padding-top: 5px;">
				<textarea id="template_content" name="template_content"
							style="width: 700px; height: 500px;"><c:out value="${template_content}"/></textarea>
			</div>

		</form>

		<table class="screenActions">
			<tr>
				<td><input type="button" value="Save" onclick="doSave()"></td>
				<td><input type="button" value="Reset to Default" onclick="return doReset()"></td>
				<c:url var="dashboardUrl" value="accountingvouchertemplates.do">
					<c:param name="_method" value="list"/>
				</c:url>
				<td>
					&nbsp;|&nbsp;<a href="javascript:void(0);" onclick="return gotoLocation('${dashboardUrl}')"/>Voucher Template List</a>
				</td>
			</tr>
		</table>
	</body>
</html>