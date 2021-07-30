<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Add Center Outhouse - Insta HMS</title>
		
	</head>
	<body>
		<div class=pageHeader>${ifn:cleanHtml(param._method=='add' ? 'Add' : 'Edit')} Center Outhouse </div>
		<insta:feedback-panel/>
		<form name="outhouseCenterForm" action="CenterOuthousesAction.do" method="POST">
			<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}"/>
			<input type="hidden" name="outsource_dest_type" id="outsource_dest_type" value="O"/>
			<input type="hidden" name="old_center_id" value="${ifn:cleanHtmlAttribute(param.center_id)}" />
			<input type="hidden" name="old_outhouse_id" value="${ifn:cleanHtmlAttribute(param.outhouse_id)}" />

			<fieldset class="fieldSetBorder">
				<table class="formTable">
					<tr>
						<td class="formlabel">Outhouse:</td>
						<td>
							<insta:selectdb name="outsource_id" id="outsource_id" table="outhouse_master"
								valuecol="oh_id" displaycol="oh_name" value="${param.outsource_id}"
								dummyvalue="-- Select --" filtered="true" class="dropdown validate-not-first"/>
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
				<button type="submit" name="save" ${param._method == 'add' ? '' : 'disabled'} accesskey="S"><b><u>S</u></b>ave</button>
				|
				<c:url var="url" value="CenterOuthousesAction.do">
					<c:param name="_method" value="list"/>
				</c:url>
				<a href="<c:out value='${url}'/>">List</a>
				<c:url var="addUrl" value="CenterOuthousesAction.do">
					<c:param name="_method" value="add"/>
				</c:url>
				| <a href="<c:out value='${addUrl}'/>">Add</a>

			</div>
		</form>
	</body>
</html>
