<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Add/Edit Histo Impression - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js" />

<script>
	var histoList = <%= request.getAttribute("histoNamesAndIds") %>;
	var impressionId = '${bean.map.impression_id}';
	var shortImpression = '${bean.map.short_impression}';
	var backupName = '';
	var conductionCyto = '${ifn:cleanJavaScript(conductionCyto)}';

	function keepBackUp(){
		if(document.forms[0]._method.value == 'update'){
				backupName = document.forms[0].short_impression.value;
		}
		if (conductionCyto != null && conductionCyto =='Y') {
			window.close();
		}
	}

	function doClose() {
		window.location.href = "${cpath}/master/HistoImpression.do?_method=list&sortOrder=short_impression&sortReverse=false&status=A";
	}

	function checkduplicate(){
			var newHistoName = trimAll(document.histoImpression.short_impression.value);
			for(var i=0;i<histoList.length;i++){
				item = histoList[i];
				if(impressionId!=item.impression_id){
				   var actualHistoName = item.short_impression;
				    if (newHistoName.toLowerCase() == actualHistoName.toLowerCase()) {
				    	alert(document.histoImpression.short_impression.value+" already exists pls enter other name");
				    	document.histoImpression.short_impression.value=shortImpression;
				    	document.histoImpression.short_impression.focus();
				    	return false;
				    }
			     }
			}

			document.histoImpression.short_impression.value = trim(document.histoImpression.short_impression.value);
			document.histoImpression.impression_details.value = trim(document.histoImpression.impression_details.value);
			if (document.histoImpression.short_impression.value == '') {
				alert('Short Impression name is required');
				document.histoImpression.short_impression.focus();
				return false;
			}
			if (document.histoImpression.impression_details.value == '') {
				alert('Impression Details is required');
				document.histoImpression.impression_details.focus();
				return false;
			}
      }

      function chklen(){
		  document.histoImpression.impression_details.value = trim(document.histoImpression.impression_details.value);

		  	 if(document.histoImpression.impression_details.value.length>500)
		  	 {
		  	    var s = document.histoImpression.impression_details.value;
		  	    s = s.substring(0,500);
		    	document.histoImpression.impression_details.value = s;
		  	    alert("Impression details should be 500 characters only");
		  	    document.histoImpression.impression_details.focus();
		  	 }
	  }

      <c:if test="${param._method != 'add'}">
  	  	Insta.masterData=${histoNamesAndIds};
 	 </c:if>

</script>

</head>
<body onload="keepBackUp();">

	<c:choose>
	     <c:when test="${param._method != 'add'}">
	        <h1 style="float:left">Edit Histo Impression</h1>
		    <c:url var="searchUrl" value="/master/HistoImpression.do"/>
		    <insta:findbykey keys="short_impression,impression_id" method="show" fieldName="impression_id" url="${searchUrl}" />
	     </c:when>
	     <c:otherwise>
	        <h1>Add Histo Impression</h1>
	     </c:otherwise>
	</c:choose>

	<form action="HistoImpression.do" method="POST" name="histoImpression">
		<input type="hidden" name="_method"	value="${(param._method == 'add'|| param._method == 'addFromConductionScreen') ? 'create' : 'update'}">
		<input type="hidden" name="impression_id" value="${bean.map.impression_id}" />

		<insta:feedback-panel />
		<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Short Impression:</td>
				<c:choose>
					<c:when test="${param._method=='addFromConductionScreen'}">
							<td>
								 <input type="text" name="short_impression"value="${ifn:cleanHtmlAttribute(impressionName)}" maxlength="100"
								 style="border-style: "/>
								 <input type="hidden" name="cytoConduction" id="cytoConduction" value="Y">
						  	</td>
						</td>
					</c:when>
					<c:otherwise>
						<td><input type="text" name="short_impression"
							value="${bean.map.short_impression}" length="100"
							style="border-style: " /></td>
					</c:otherwise>
				</c:choose>
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
				<td class="formlabel">Impression Details:</td>
				<td><textarea name="impression_details" id="impression_details" cols="65" rows="5" onblur="return chklen();">${bean.map.impression_details}</textarea></td>
			</tr>
		</table>
		</fieldset>

		<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return checkduplicate()"><b><u>S</u></b>ave</button>
		<c:if test="${param._method=='show'}">| <a
				href="HistoImpression.do?_method=add">Add</a>
		</c:if> | <a href="javascript:void(0)" onclick="doClose();">Histo Impression List</a></div>
	</form>

</body>
</html>
