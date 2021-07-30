<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Add/Edit Organism - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js" />

<script>
	var organismList = <%= request.getAttribute("organismNamesAndIds") %>;
	var organismId = '${bean.map.organism_id}';
	var organismName = '${bean.map.organism_name}';
	var backupName = '';

	function keepBackUp(){
		if(document.forms[0]._method.value == 'update'){
				backupName = document.forms[0].organism_name.value;
		}
	}

	function doClose() {
		window.location.href = "${cpath}/master/MicroOrganism.do?_method=list&sortOrder=organism_name&sortReverse=false&status=A";
	}

	function checkduplicate(){
			var newOrganismName = trimAll(document.organismForm.organism_name.value);
			for(var i=0;i<organismList.length;i++){
				item = organismList[i];
				if(organismId!=item.organism_id){
				   var actualOrganismName = item.organism_name;
				    if (newOrganismName.toLowerCase() == actualOrganismName.toLowerCase()) {
				    	alert(document.organismForm.organism_name.value+" already exists pls enter other name");
				    	document.organismForm.organism_name.value=organismName;
				    	document.organismForm.organism_name.focus();
				    	return false;
				    }
			     }
			}

			document.organismForm.organism_name.value = trim(document.organismForm.organism_name.value);

			if (document.organismForm.organism_name.value == '') {
				alert('Organism  name is required');
				document.organismForm.organism_name.focus();
				return false;
			}

			if (document.organismForm.org_group_id.value == '') {
				alert('Organism Group is required');
				document.organismForm.org_group_id.focus();
				return false;
			}

      }

      <c:if test="${param._method != 'add'}">
  	  	Insta.masterData=${organismNamesAndIds};
 	 </c:if>

</script>

</head>
<body onload="keepBackUp();">

	<c:choose>
	     <c:when test="${param._method != 'add'}">
	        <h1 style="float:left">Edit Organism</h1>
		    <c:url var="searchUrl" value="/master/MicroOrganism.do"/>
		    <insta:findbykey keys="organism_name,organism_id" method="show" fieldName="organism_id" url="${searchUrl}" />
	     </c:when>
	     <c:otherwise>
	        <h1>Add Organism</h1>
	     </c:otherwise>
	</c:choose>

	<form action="MicroOrganism.do" method="POST" name="organismForm">
		<input type="hidden" name="_method"	value="${param._method == 'add' ? 'create' : 'update'}">
		<input type="hidden" name="organism_id" value="${bean.map.organism_id}" />

		<insta:feedback-panel />
		<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Organism Name:</td>
				<td><input type="text" name="organism_name"
					value="${bean.map.organism_name}" length="200"
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
			<tr>
				<td class="formlabel">Organism Group:</td>
				<td><insta:selectdb name="org_group_id" id="org_group_id" table="micro_org_group_master"
									valuecol="org_group_id" displaycol="org_group_name" value="${bean.map.org_group_id}" dummyvalue="---Select---" dummyvalueId=""/>
				</td>
			</tr>
		</table>
		</fieldset>

		<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return checkduplicate()"><b><u>S</u></b>ave</button>
		<c:if test="${param._method=='show'}">| <a
				href="MicroOrganism.do?_method=add">Add</a>
		</c:if> | <a href="javascript:void(0)" onclick="doClose();">Organism List</a></div>
	</form>

</body>
</html>
