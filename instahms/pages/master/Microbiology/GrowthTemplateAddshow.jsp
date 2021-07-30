<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Add/Edit Growth Template - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js" />

<script>
	var growthList = <%= request.getAttribute("growthNamesAndIds") %>;
	var templateId = '${bean.map.growth_template_id}';
	var templateNames = '${bean.map.growth_template_name}';
	var backupName = '';

	function keepBackUp(){
		if(document.forms[0]._method.value == 'update'){
				backupName = document.forms[0].growth_template_name.value;
		}
	}

	function doClose() {
		window.location.href = "${cpath}/master/MicrogrowthTemplate.do?_method=list&sortOrder=growth_template_name&sortReverse=false&status=A";
	}

	function checkduplicate(){
			var newHistoName = trimAll(document.templateForm.growth_template_name.value);
			for(var i=0;i<growthList.length;i++){
				item = growthList[i];
				if(templateId!=item.growth_template_id){
				   var actualHistoName = item.growth_template_name;
				    if (newHistoName.toLowerCase() == actualHistoName.toLowerCase()) {
				    	alert(document.templateForm.growth_template_name.value+" already exists pls enter other name");
				    	document.templateForm.growth_template_name.value=templateNames;
				    	document.templateForm.growth_template_name.focus();
				    	return false;
				    }
			     }
			}

			document.templateForm.growth_template_name.value = trim(document.templateForm.growth_template_name.value);
			document.templateForm.growth_template_detailed.value = trim(document.templateForm.growth_template_detailed.value);
			if (document.templateForm.growth_template_name.value == '') {
				alert('Template name is required');
				document.templateForm.growth_template_name.focus();
				return false;
			}
			if (document.templateForm.growth_template_detailed.value == '') {
				alert('Template Details is required');
				document.templateForm.growth_template_detailed.focus();
				return false;
			}

      }

      function chklen(){
		  document.templateForm.growth_template_detailed.value = trim(document.templateForm.growth_template_detailed.value);

		  	 if(document.templateForm.growth_template_detailed.value.length>500)
		  	 {
		  	    var s = document.templateForm.growth_template_detailed.value;
		  	    s = s.substring(0,500);
		    	document.templateForm.growth_template_detailed.value = s;
		  	    alert("Template details should be 500 characters only");
		  	    document.templateForm.growth_template_detailed.focus();
		  	 }
	  }

      <c:if test="${param._method != 'add'}">
  	  	Insta.masterData=${growthNamesAndIds};
 	 </c:if>

</script>

</head>
<body onload="keepBackUp();">

	<c:choose>
	     <c:when test="${param._method != 'add'}">
	        <h1 style="float:left">Edit Template</h1>
		    <c:url var="searchUrl" value="/master/MicrogrowthTemplate.do"/>
		    <insta:findbykey keys="growth_template_name,growth_template_id" method="show" fieldName="growth_template_id" url="${searchUrl}" />
	     </c:when>
	     <c:otherwise>
	        <h1>Add Template</h1>
	     </c:otherwise>
	</c:choose>

	<form action="MicrogrowthTemplate.do" method="POST" name="templateForm">
		<input type="hidden" name="_method"	value="${param._method == 'add' ? 'create' : 'update'}">
		<input type="hidden" name="growth_template_id" value="${bean.map.growth_template_id}" />

		<insta:feedback-panel />
		<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Template Name:</td>
				<td><input type="text" name="growth_template_name"
					value="${bean.map.growth_template_name}" maxlength="200"
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
				<td><textarea name="growth_template_detailed" id="growth_template_detailed" cols="65" rows="5" onblur="return chklen();">${bean.map.growth_template_detailed}</textarea></td>
			</tr>
		</table>
		</fieldset>

		<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return checkduplicate()"><b><u>S</u></b>ave</button>
		<c:if test="${param._method=='show'}">| <a
				href="MicrogrowthTemplate.do?_method=add">Add</a>
		</c:if> | <a href="javascript:void(0)" onclick="doClose();">Template List</a></div>
	</form>

</body>
</html>
