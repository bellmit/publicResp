<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<insta:link type="script" file="hmsvalidation.js" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Add New Diagnostic Methodology</title>
<script>

			var chkMethodList = <%= request.getAttribute("methList") %>;
			var hiddenMethodId = '${bean.map.method_id}';

			function doClose() {
				window.location.href = "${cpath}/pages/masters/hosp/diagnostics/diagMethodology.do?_method=listDiagMethod&status=A&sortOrder=method_name&sortReverse=false";
			}

			function checkduplicate(){
					var newMethodName = trimAll(document.editDiagMethodologyForm.method_name.value);
					for(var i=0;i<chkMethodList.length;i++){
						item = chkMethodList[i];
						if(hiddenMethodId!=item.METHOD_ID){
						   var actualMethodName= item.METHOD_NAME;
						    if (newMethodName.toLowerCase() == actualMethodName.toLowerCase()) {
						    	alert(document.editDiagMethodologyForm.method_name.value+" already exists pls enter other name");
						    	document.editDiagMethodologyForm.method_name.value='';
						    	document.editDiagMethodologyForm.method_name.focus();
						    	return false;
						    }
					     }
					}
		     }

		     function trimAll(sString){

				while (sString.substring(0,1) == ' '){
				sString = sString.substring(1, sString.length);
				}
				while (sString.substring(sString.length-1, sString.length) == ' ')
				{
					sString = sString.substring(0,sString.length-1);
				}

			return sString;
		}

			function focus(){
				document.editDiagMethodologyForm.method_name.focus();
			}


			<c:if test="${param._method != 'add'}">
	    		Insta.masterData=${methodologyList};
		  </c:if>
		</script>


</head>
<body onload="focus();">


<c:choose>
	<c:when test="${param._method !='add'}">
		<h1 style="float: left">Edit Diagnostic Methodology</h1>
		<c:url var="searchUrl"
			value="/pages/masters/hosp/diagnostics/diagMethodology.do" />
		<insta:findbykey keys="method_name,method_id" method="showDetails"
			fieldName="method_id" url="${searchUrl}" />
	</c:when>
	<c:otherwise>
		<h1>Add Diagnostic Methodology</h1>
	</c:otherwise>
</c:choose>

<form
	action="${cpath}/pages/masters/hosp/diagnostics/diagMethodology.do"
	name="editDiagMethodologyForm" method="get"><input type="hidden"
	name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
<input type="hidden" name="method_id" value="${bean.map.method_id}" />
<insta:feedback-panel />

<fieldset class="fieldsetborder">
<table class="formtable">
	<tr>
		<td class="formlabel">Methodology Name <span style="color: red">*</span>:
		</td>
		<td><input type="text" name="method_name"
			value="${bean.map.method_name}" onblur="checkduplicate();"
			class="required validate-length" length="70"
			title="Name is required and max length of name can be 70" /></td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class="formlabel">Methodology Desc:</td>
		<td><textarea rows="5" cols="20" name="method_desc">${bean.map.method_desc}</textarea>
		</td>
	</tr>
	<tr>
		<td class="formlabel">Status:</td>
		<td><insta:selectoptions name="status" value="${bean.map.status}"
			opvalues="A,I" optexts="Active,Inactive" /></td>
	</tr>
</table>
</fieldset>

<table class="screenActions">
	<tr>
		<td>
		<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
		</td>
		<c:if test="${param._method != 'add' }">
			<td>&nbsp;|&nbsp;</td>
			<td><a
				href="${cpath}/pages/masters/hosp/diagnostics/diagMethodology.do?_method=add">Add</a></td>
		</c:if>
		<td>&nbsp;|&nbsp;</td>
		<td><a href="javascript:void(0)" onclick="doClose();">Methodology
		List</a></td>
	</tr>
</table>

</form>

</body>
</html>
