<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Insurance - Insta HMS</title>
	<insta:link type="script" file="Insurance/insurance.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<style type="text/css">
		.Doc_R { background-color: #EAD6BB;}
		table.legend {border-collapse : collapse ; margin-left : 6px }
		table.legend td {border : 1px solid grey ;padding 2px 5px }
		table.search td { white-space: nowrap }
	</style>
	<script>
		function dashboard() {
			window.location.href = "${cpath}/Insurance/InsuranceDashboard.do?_method=list";
	}
	</script>
</head>

<body>
	<div class="pageHeader">View Message</div>
	<insta:feedback-panel/>
	<c:choose>
		<c:when test="${not empty visit_id}">
			<insta:patientdetails  visitid="${visit_id}" />
		</c:when>
		<c:otherwise>
			<insta:patientgeneraldetails  mrno="${mr_no}" />
		</c:otherwise>
	</c:choose>

	<form name="viewmessage" action="InsuranceViewMessage.do" method="POST">
		<input type="hidden" name="_method" value="show"/>
		<input type="hidden" name="insurance_id" value="${ifn:cleanHtmlAttribute(insurance_id)}">
		<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(mr_no)}">
		<input type="hidden" name="visit_id" value="${ifn:cleanHtmlAttribute(visit_id)}">

		<table align="left" class="formTable" width="100%">
			<tr>
				<td width="100%" >
				<fieldset class="fieldSetBorder">
					<table width="100%" >
						<tr>
							<td width="10%">To : </td>
							<td width="90%">
								<input type="text" name="email_to" id="email_to" size="60" value="${msgInfo.map.email_to}" readonly="readonly"/>
							</td>
						</tr>
						<tr>
							<td width="10%">CC : </td>
							<td width="90%">
								<input type="text" name="email_cc" id="email_cc" size="60" value="${msgInfo.map.email_cc}" readonly="readonly"/>
							</td>
						</tr>
						<tr>
							<td width="10%">Subject : </td>
							<td width="90%"><input type="text" name="email_subject" id="email_subject" size="80"  value="${msgInfo.map.email_subject}" readonly="readonly" /></td>
						</tr>
						<tr>
							<td width="10%">Message : </td>
							<td colspan="2"><textarea name="email_body" id="email_body" rows="5" cols="90" readonly="readonly">${msgInfo.map.email_body}</textarea></td>
						</tr>
					</table>
				</fieldset>
				</td>
			</tr>
			<tr height="20"/>
				<c:if test="${not empty DocsInfo}">
					<tr><th>Attachments</th></tr>
					<tr>
						<td>
							<table align="center" class="dashboard">
								<tr><th>Document Name</th></tr>
								<c:forEach items="${DocsInfo}" var="pdoc" >
									<tr>
										<c:url var="url" value="InsuranceViewMessage.do">
											<c:param name="_method" value="getDocument" />
											<c:param name="attachment_id" value="${pdoc.map.attachment_id}" />
											<c:param name="doc_type" value="S"/>
											<c:param name="mr_no" value="${mr_no}"/>
										</c:url>
										<td><a href="<c:out value='${url}'/>" target="_blank">${pdoc.map.doc_title}</a></td>
									</tr>
								</c:forEach>
							</table>
						</td>
					</tr>
				</c:if>
			</tr>
		</table>
		<div class="screenActions">
			<a href="javascript:void(0)" onclick="funClose();">Case List</a> |
			<a href="AddOrEditCase.do?_method=addshow&insurance_id=${ifn:cleanURL(insurance_id)}">Case</a> |
			<c:choose>
				<c:when test="${not empty visit_id}">
					<a href="UploadReceivedDocs.do?_method=add&visit_id=${ifn:cleanURL(visit_id)}&insurance_id=${ifn:cleanURL(insurance_id)}">Upload</a> |
				</c:when>
				<c:otherwise>
					<a href="UploadReceivedDocs.do?_method=add&mr_no=${ifn:cleanURL(mr_no)}&insurance_id=${ifn:cleanURL(insurance_id)}">Upload</a> |
				</c:otherwise>
			</c:choose>
			<a href="InsuranceHistory.do?_method=show&mr_no=${ifn:cleanURL(mr_no)}&visit_id=${ifn:cleanURL(visit_id)}&insurance_id=${ifn:cleanURL(insurance_id)}">History</a>
		</div>
	</form>
</body>
</html>
