<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<insta:link type="script" file="hmsvalidation.js" />
<c:set var="pagePath" value="<%=URLRoute.DIAG_METHODOLOGY_MASTER_PATH %>"/>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Add New Diagnostic Methodology</title>
<script>

			var chkMethodList = ${ifn:convertListToJson(methList)};
			var hiddenMethodId = '${bean.method_id}';

			function doClose() {
				window.location.href = "${cpath}/${pagePath}/list.htm?&status=A&sortOrder=method_name&sortReverse=false";
			}

			function checkduplicate(){
				chkMethodList = eval(chkMethodList);
				var newMethodName = trimAll(document.editDiagMethodologyForm.method_name.value);
					for(var i=0;i<chkMethodList.length;i++){
						item = chkMethodList[i];
						if(hiddenMethodId!=item.method_id){
						   var actualMethodName= item.method_name;
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


			Insta.masterData=${ifn:convertListToJson(methodologyList)};
		</script>


</head>
<body onload="focus();">

<h1 style="float: left">Edit Diagnostic Methodology</h1>
<c:url var="searchUrl"
	value="${pagePath}/show.htm" />
<insta:findbykey keys="method_name,method_id" method="show"
	fieldName="method_id" url="${searchUrl}" />

<form action="update.htm" method="POST" name="editDiagMethodologyForm">
<input type="hidden" name="method_id" value="${bean.method_id}" />
<insta:feedback-panel />

<fieldset class="fieldsetborder">
<table class="formtable">
	<tr>
		<td class="formlabel">Methodology Name <span style="color: red">*</span>:
		</td>
		<td><input type="text" name="method_name"
			value="${bean.method_name}" onblur="checkduplicate();"
			class="required validate-length" length="70"
			title="Name is required and max length of name can be 70" /></td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class="formlabel">Methodology Desc:</td>
		<td><textarea rows="5" cols="20" name="method_desc">${bean.method_desc}</textarea>
		</td>
	</tr>
	<tr>
		<td class="formlabel">Status:</td>
		<td><insta:selectoptions name="status" value="${bean.status}"
			opvalues="A,I" optexts="Active,Inactive" /></td>
	</tr>
</table>
</fieldset>

<table class="screenActions">
	<tr>
		<td>
		<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
		</td>
		<td>&nbsp;|&nbsp;</td>
		<td><a
			href="${cpath}${pagePath}/add.htm?">Add</a></td>
		<td>&nbsp;|&nbsp;</td>
		<td><a href="javascript:void(0)" onclick="doClose();">Methodology
		List</a></td>
	</tr>
</table>

</form>

</body>
</html>
