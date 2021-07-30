<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Add Center Internal Lab - Insta HMS</title>
	

	</head>
	<body>
		<div class=pageHeader>${ifn:cleanHtml(param._method=='addCenterToInternalLab' ? 'Add' : 'Edit')} Center Internal Lab </div>
		<insta:feedback-panel/>
		<form name="internalLabCenterForm" action="CenterOuthousesAction.do" method="POST">
			<input type="hidden" name="_method" value="create"/>
			<input type="hidden" name="outsource_dest_type" id="outsource_dest_type" value="C"/>
			<fieldset class="fieldSetBorder">
				<table class="formTable">
					<tr>
						<td class="formlabel">Internal Lab Center:</td>
						<td>
							<select name="outsource_id" id="outsource_id" class="dropdown validate-not-first">
								<option value="">-- Select --</option>
								<c:forEach items="${internalLabDetails}" var="lab">
									<option value="${lab.map.center_id}" ${outSourceId  == lab.map.center_id ? 'selected' : ''}>
										${lab.map.center_name}
									</option>
								</c:forEach>
							</select>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Center</td>
						<td>
							<select name="center_id" id="center_id" class="dropdown validate-not-first">
								<option value="">-- Select --</option>
								<c:forEach items="${centers}" var="center">
									<option value="${center.map.center_id}" ${bean.map.center_id == center.map.center_id ? 'selected' : ''}>
										${center.map.center_name}
									</option>
								</c:forEach>
							</select>
						</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
				</table>
			</fieldset>
			<div class="screenActions">
				<button type="submit" name="save" ${param._method == 'addCenterToInternalLab' ? '' : 'disabled'} accesskey="S"><b><u>S</u></b>ave</button>
				|
				<c:url var="url" value="CenterOuthousesAction.do">
					<c:param name="_method" value="list"/>
				</c:url>
				<a href="<c:out value='${url}'/>">List</a>
				<c:url var="addUrl" value="CenterOuthousesAction.do">
					<c:param name="_method" value="addCenterToInternalLab"/>
				</c:url>
				| <a href="<c:out value='${addUrl}'/>">Add</a>

			</div>
		</form>
	</body>
</html>
