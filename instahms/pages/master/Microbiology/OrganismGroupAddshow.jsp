<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Add/Edit Organism Group - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js" />

<script>
	var orgGrpList = <%= request.getAttribute("orgGrpNamesAndIds") %>;
	var orgGrpId = '${bean.map.org_group_id}';
	var orgGrpName = '${bean.map.org_group_name}';
	var backupName = '';

	function keepBackUp(){
		if(document.forms[0]._method.value == 'update'){
				backupName = document.forms[0].org_group_name.value;
		}
	}

	function doClose() {
		window.location.href = "${cpath}/master/Organismgroup.do?_method=list&sortOrder=org_group_name&sortReverse=false&status=A";
	}

	function checkduplicate(){
			var newHistoName = trimAll(document.organismgroup.org_group_name.value);
			for(var i=0;i<orgGrpList.length;i++){
				item = orgGrpList[i];
				if(orgGrpId!=item.org_group_id){
				   var actualHistoName = item.org_group_name;
				    if (newHistoName.toLowerCase() == actualHistoName.toLowerCase()) {
				    	alert(document.organismgroup.org_group_name.value+" already exists pls enter other name");
				    	document.organismgroup.org_group_name.value=orgGrpName;
				    	document.organismgroup.org_group_name.focus();
				    	return false;
				    }
			     }
			}

			document.organismgroup.org_group_name.value = trim(document.organismgroup.org_group_name.value);

			if (document.organismgroup.org_group_name.value == '') {
				alert('Organism Group Name is required');
				document.organismgroup.org_group_name.focus();
				return false;
			}
      }

      <c:if test="${param._method != 'add'}">
  	  	Insta.masterData=${orgGrpNamesAndIds};
 	 </c:if>

</script>

</head>
<body onload="keepBackUp();">

	<c:choose>
	     <c:when test="${param._method != 'add'}">
	        <h1 style="float:left">Edit Organism Group</h1>
		    <c:url var="searchUrl" value="/master/Organismgroup.do"/>
		    <insta:findbykey keys="org_group_name,org_group_id" method="show" fieldName="org_group_id" url="${searchUrl}" />
	     </c:when>
	     <c:otherwise>
	        <h1>Add Organism Group</h1>
	     </c:otherwise>
	</c:choose>

	<form action="Organismgroup.do" method="POST" name="organismgroup">
		<input type="hidden" name="_method"	value="${param._method == 'add' ? 'create' : 'update'}">
		<input type="hidden" name="org_group_id" value="${bean.map.org_group_id}" />

		<insta:feedback-panel />
		<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Organism Group Name:</td>
				<td><input type="text" name="org_group_name"
					value="${bean.map.org_group_name}" length="100"
					style="border-style: " /></td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="${bean.map.status}"
					opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
		</table>
		</fieldset>

		<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return checkduplicate()"><b><u>S</u></b>ave</button>
		<c:if test="${param._method=='show'}">| <a
				href="Organismgroup.do?_method=add">Add</a>
		</c:if> | <a href="javascript:void(0)" onclick="doClose();">Organism Group List</a></div>
	</form>

</body>
</html>
