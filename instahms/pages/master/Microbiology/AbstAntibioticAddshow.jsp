<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Add/Edit Antibiotic - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="script" file="ajax.js"/>

<script>
	var abstAntibioticList = <%= request.getAttribute("antibioticNamesAndIds") %>;
	var antibioticId = '${bean.map.antibiotic_id}';
	var antibioticName = '${bean.map.antibiotic_name}';
	var backupName = '';

	function keepBackUp(){
		if(document.forms[0]._method.value == 'update'){
				backupName = document.forms[0].antibiotic_name.value;
		}
	}

	function doClose() {
		window.location.href = "${cpath}/master/MicroAntibiotic.do?_method=list&sortOrder=antibiotic_name&sortReverse=false&status=A";
	}

	function checkduplicate(){
			var newantibioticName = trimAll(document.antibioticForm.antibiotic_name.value);
			for(var i=0;i<abstAntibioticList.length;i++){
				item = abstAntibioticList[i];
				if(antibioticId!=item.antibiotic_id){
				   var actualantibioticName = item.antibiotic_name;
				    if (newantibioticName.toLowerCase() == actualantibioticName.toLowerCase()) {
				    	alert(document.antibioticForm.antibiotic_name.value+" already exists pls enter other name");
				    	document.antibioticForm.antibiotic_name.value=antibioticName;
				    	document.antibioticForm.antibiotic_name.focus();
				    	return false;
				    }
			     }
			}

			document.antibioticForm.antibiotic_name.value = trim(document.antibioticForm.antibiotic_name.value);

			if (document.antibioticForm.antibiotic_name.value == '') {
				alert('Antibiotic  name is required');
				document.antibioticForm.antibiotic_name.focus();
				return false;
			}

      }

      <c:if test="${param._method != 'add'}">
  	  	Insta.masterData=${antibioticNamesAndIds};
 	 </c:if>

</script>

</head>
<body onload="keepBackUp();">

	<c:choose>
	     <c:when test="${param._method != 'add'}">
	        <h1 style="float:left">Edit Antibiotic</h1>
		    <c:url var="searchUrl" value="/master/MicroAntibiotic.do"/>
		    <insta:findbykey keys="antibiotic_name,antibiotic_id" method="show" fieldName="antibiotic_id" url="${searchUrl}" />
	     </c:when>
	     <c:otherwise>
	        <h1>Add Antibiotic</h1>
	     </c:otherwise>
	</c:choose>

	<form action="MicroAntibiotic.do" method="POST" name="antibioticForm">
		<input type="hidden" name="_method"	value="${param._method == 'add' ? 'create' : 'update'}">
		<input type="hidden" name="antibiotic_id" value="${bean.map.antibiotic_id}" />

		<insta:feedback-panel />
		<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Antibiotic Name:</td>
				<td><input type="text" name="antibiotic_name"
					value="${bean.map.antibiotic_name}" length="200"
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
				href="MicroAntibiotic.do?_method=add">Add</a>
		</c:if> | <a href="javascript:void(0)" onclick="doClose();">Antibiotic List</a></div>
	</form>

</body>
</html>
