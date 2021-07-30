<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Internal Lab - Insta HMS</title>
		
		<script>
			var activeOutHouseTestsExist = '${activeOutHouseTestsExist}';
			
			function validateForm() {
				var status = document.getElementById("status").value;
				if(activeOutHouseTestsExist == 'true'  && status == 'I'){
					alert("There are tests associated with this internal lab. \nPlease inactivate those associations.");
					return false;
				}
				return true;
			}		
		</script>

	</head>
	<body>
		<div class=pageHeader>${ifn:cleanHtml(param._method=='addNewInternalLab' ? 'Add' : 'Edit')} Internal Lab </div>
		<insta:feedback-panel/>
		<form name="internalLabForm" action="${cpath}/pages/masters/hosp/diagnostics/OutHouseMaster.do" method="POST">
			<input type="hidden" name="_method" value="${param._method == 'addNewInternalLab' ? 'createInternalLab' : 'updateInternalLab'}"/>

			<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Internal Lab Details</legend>
				<table class="formTable">
					<tr>
						<td class="formlabel">Center:</td>
						<td class="forminfo">
							<c:choose>
								<c:when test="${param._method=='addNewInternalLab'}">
									<select name="outsource_dest" id="outsource_dest" class="dropdown validate-not-first">
										<option value="">-- Select --</option>
										<c:forEach items="${centers}" var="center">
											<option value="${center.map.center_id}" ${bean.map.center_id == center.map.center_id ? 'selected' : ''}>
												${center.map.center_name}
											</option>
										</c:forEach>
									</select>
								</c:when>
								<c:otherwise>
									<input type="hidden" name="outsource_dest" id="outsource_dest" value="${bean.map.outsource_dest}"/>
									<input type="hidden" name="outsource_dest_id" id="outsource_dest_id" value="${bean.map.outsource_dest_id}"/>
									<insta:getCenterName center_id="${bean.map.outsource_dest}"/>
								</c:otherwise>
							</c:choose>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Status:</td>
						<td>
							<insta:selectoptions name="status" id="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" />
						</td>
					</tr>
				</table>
			</fieldset>
			<div class="screenActions">
				<button type="submit" name="save" accesskey="S" onclick="return validateForm()"><b><u>S</u></b>ave</button>

			| <a href="${cpath}/pages/masters/hosp/diagnostics/OutHouseListMaster.do?_method=getOutHouseList&sortOrder=outsource_name
			&sortReverse=false&status=A">OutSource List</a>
			</div>
		</form>
	</body>
</html>
window.location.href="";