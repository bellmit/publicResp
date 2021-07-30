<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page import="com.insta.hms.stores.StoresDBTablesUtil" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Diag Dept - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="masters/diagdeptstores.js"/>

	<script>
		var selectedCenter = '';
		var storeList = <%= StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.GET_STORES_MASTER) %>;

		function doClose() {
			window.location.href = "${cpath}/master/DiagnosticDepartmentMaster.do?_method=list&sortOrder=display_order" +
						"&status=A";
		}
		function focus(){
			document.dDeptForm.ddept_name.focus();
			init();
		}

		<c:if test="${param._method != 'add'}">
		    Insta.masterData=${dDeptsList};
		</c:if>
	</script>

</head>
<body onload="focus();">
<c:choose>
    <c:when test="${param._method != 'add'}">
       <h1 style="float:left">Edit Diagnostic Department</h1>
       <c:url var="searchUrl" value="/master/DiagnosticDepartmentMaster.do"/>
       <insta:findbykey keys="ddept_name,ddept_id" fieldName="ddept_id" method="show" url="${searchUrl}" />
    </c:when>
    <c:otherwise>
        <h1>Add Department</h1>
    </c:otherwise>
</c:choose>

<form action="DiagnosticDepartmentMaster.do" name="dDeptForm" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="ddept_id" value="${bean.map.ddept_id}"/>
	</c:if>

	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Diagnostic Department Details</legend>
		<table class="formtable" align="center">
			<tr>
				<td class="formlabel">Diagnostic Department Name:</td>
				<td>
					<input type="text" name="ddept_name"value="<c:out value="${bean.map.ddept_name}" escapeXml="true"/>"
						onblur="capWords(ddept_name)" class="required validate-length" maxlength="100" title="DepartmentName is required " />
				</td>
				<td class="formlabel">Category:</td>
				<td>
					<insta:selectoptions name="category" value="${bean.map.category}" opvalues="DEP_LAB,DEP_RAD" optexts="LABORATORY,RADIOLOGY" />
				</td>
				<td class="formlabel">Technician Designation:</td>
				<td>
					<input type="text" name="designation" value="<c:out value="${bean.map.designation}" escapeXml="true"/>"
						class="required validate-length" maxlength="100" title="Designation is required " />
				</td>
			</tr>
			<tr>
				<td class="formlabel">Display Order:</td>
				<td>
					<input type="text" name="display_order" value="${bean.map.display_order}"
						class="required validate-number" maxlength="100" title="Displayorder is required " />
				</td>
				<td class="formlabel">Status</td>
				<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
				<c:if test="${max_centers_inc_default == 1}">
					<td class="formlabel">Store:</td>
					<td>
						<insta:selectdb name="deptid" id="deptid" table="stores"  valuecol="dept_id" displaycol="dept_name"
								style="width: 13em" value="${deptId}"/>
					</td>
				</c:if>
			</tr>
			<tr></tr>
			</table>
		</fieldset>
		<c:if test="${max_centers_inc_default > 1}">
			<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Stores for Consumables</legend>
			<table style="width:300px" class="detailList" id="diagdeptTbl" >
				<tr class="header">
					<th class="first">Center</th>
					<th>Store</th>
					<th>&nbsp;</th>
				</tr>
					<c:forEach items="${diagdeptstores}" var="dept" varStatus="st">
					<tr id="row${st.index}">
						<input type="hidden" name="center_id" id="" value="${dept.center_id}" />
						<input type="hidden" name="store_id" id="" value="${dept.store_id}" />
						<input type="hidden" name="selectedrow" id="selectedrow${st.index+1}" value="false"/>
						<input type="hidden" name="added" id="added${st.index+1}" value="N"/>
						<td>${dept.center_name}</td>
						<td>${dept.dept_name}</td>
						<td style="text-align: center"><img src="${cpath}/icons/Delete.png" onclick="changeElsColor(${st.index+1}, this);"/></td>
					</tr>
					<c:set var="newIndexFORdummyRow" value="${st.index+1}"/>
					</c:forEach>
					<tr id="" style="display: none">
						<input type="hidden" name="center_id" id="" value="" />
						<input type="hidden" name="store_id" id="" value="" />
						<input type="hidden" name="selectedrow" id="selectedrow0" value="false"/>
						<input type="hidden" name="added" id="added${st.index+1}" value="N"/>
						<td></td>
						<td></td>
						<td style="text-align: center"><img src="${cpath}/icons/Delete.png" onclick="changeElsColor('${newIndexFORdummyRow}', this);"/></td>
					</tr>
					<tr>
						<td colspan="2"></td>
						<td style="text-align: center">
							<button type="button" name="addresults" Class="imgButton" Id="addresults" onclick="showDialog(this)" >
								<img src="${cpath}/icons/Add.png"/>
							</button>
						</td>
					</tr>
			</table>
		</fieldset>
	</c:if>
	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button></td>
			<c:if test="${param._method != 'add'}">
				<td>&nbsp;|&nbsp;<td>
				<td><a href="${cpath}/master/DiagnosticDepartmentMaster.do?_method=add&store_id=-1">Add</a></td>
			</c:if>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">Diagnostic Departments</a></td>
		</tr>
	</table>
</form>
	<div name="diagdeptDIV" id="diagdeptDIV" style="visibility: none">
		<div class="bd">
			<fieldSet class="fieldSetBorder">
				<table class="formTable">
					<tr>
						<td>Center:</td>
						<td>
							<select class="dropdown" name="centerId" id="centerId" onchange="onSelectCenter();">
								<option value="">-- Select --</option>
								<c:forEach items="${centerList}" var="center">
										<c:if test="${center.map.center_id != 0}">
											<option value="${center.map.center_id}">${center.map.center_name}</option>
										</c:if>
								</c:forEach>
							</select>
						</td>
					</tr>
					<tr>
						<td>Store</td>
						<td>
							<select class="dropdown" name="storeId" id="storeId">
								<option value="">-- Select --</option>
							</select>
						</td>
					</tr>
				</table>
			</fieldSet>
			<div>
				<input type="button" value="Add" onclick="addToTable()"> |
				<input type="button" value="Cancel" onclick="handleCancel()">
			</div>
		</div>
	</div>
</body>
</html>
