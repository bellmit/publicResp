<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Add/Edit No Growth Template - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js" />

<script>
	var noGrowthList = <%= request.getAttribute("nogrowthNamesAndIds") %>;
	var templateId = '${bean.map.nogrowth_template_id}';
	var templateNames = '${bean.map.nogrowth_template_name}';
	var backupName = '';

	function keepBackUp(){
		if(document.forms[0]._method.value == 'update'){
				backupName = document.forms[0].nogrowth_template_name.value;
		}
	}

	function doClose() {
		window.location.href = "${cpath}/master/MicroNogrowthTemplate.do?_method=list&sortOrder=nogrowth_template_name&sortReverse=false&status=A";
	}

	function checkduplicate(){
			var newHistoName = trimAll(document.teplateForm.nogrowth_template_name.value);
			for(var i=0;i<noGrowthList.length;i++){
				item = noGrowthList[i];
				if(templateId!=item.nogrowth_template_id){
				   var actualHistoName = item.nogrowth_template_name;
				    if (newHistoName.toLowerCase() == actualHistoName.toLowerCase()) {
				    	alert(document.teplateForm.nogrowth_template_name.value+" already exists pls enter other name");
				    	document.teplateForm.nogrowth_template_name.value=templateNames;
				    	document.teplateForm.nogrowth_template_name.focus();
				    	return false;
				    }
			     }
			}

			document.teplateForm.nogrowth_template_name.value = trim(document.teplateForm.nogrowth_template_name.value);
			document.teplateForm.nogrowth_template_detailed.value = trim(document.teplateForm.nogrowth_template_detailed.value);
			if (document.teplateForm.nogrowth_template_name.value == '') {
				alert('Template name is required');
				document.teplateForm.nogrowth_template_name.focus();
				return false;
			}
			if (document.teplateForm.nogrowth_template_detailed.value == '') {
				alert('Template Details is required');
				document.teplateForm.nogrowth_template_detailed.focus();
				return false;
			}

      }

      function chklen(){
		  document.teplateForm.nogrowth_template_detailed.value = trim(document.teplateForm.nogrowth_template_detailed.value);

		  	 if(document.teplateForm.nogrowth_template_detailed.value.length>500)
		  	 {
		  	    var s = document.teplateForm.nogrowth_template_detailed.value;
		  	    s = s.substring(0,500);
		    	document.teplateForm.nogrowth_template_detailed.value = s;
		  	    alert("Template details should be 500 characters only");
		  	    document.teplateForm.nogrowth_template_detailed.focus();
		  	 }
	  }

      <c:if test="${param._method != 'add'}">
  	  	Insta.masterData=${nogrowthNamesAndIds};
 	 </c:if>

</script>

</head>
<body onload="keepBackUp();">

	<c:choose>
	     <c:when test="${param._method != 'add'}">
	        <h1 style="float:left">Edit Template</h1>
		    <c:url var="searchUrl" value="/master/MicroNogrowthTemplate.do"/>
		    <insta:findbykey keys="nogrowth_template_name,nogrowth_template_id" method="show" fieldName="nogrowth_template_id" url="${searchUrl}" />
	     </c:when>
	     <c:otherwise>
	        <h1>Add Template</h1>
	     </c:otherwise>
	</c:choose>

	<form action="MicroNogrowthTemplate.do" method="POST" name="teplateForm">
		<input type="hidden" name="_method"	value="${param._method == 'add' ? 'create' : 'update'}">
		<input type="hidden" name="nogrowth_template_id" value="${bean.map.nogrowth_template_id}" />

		<insta:feedback-panel />
		<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Template Name:</td>
				<td><input type="text" name="nogrowth_template_name"
					value="${bean.map.nogrowth_template_name}" length="200"
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
				<td class="formlabel">Template Details:</td>
				<td><textarea name="nogrowth_template_detailed" id="nogrowth_template_detailed" cols="65" rows="5" onblur="return chklen();">${bean.map.nogrowth_template_detailed}</textarea></td>
			</tr>
		</table>
		</fieldset>

		<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return checkduplicate()"><b><u>S</u></b>ave</button>
		<c:if test="${param._method=='show'}">| <a
				href="MicroNogrowthTemplate.do?_method=add">Add</a>
		</c:if> | <a href="javascript:void(0)" onclick="doClose();">Template List</a></div>
	</form>

</body>
</html>
