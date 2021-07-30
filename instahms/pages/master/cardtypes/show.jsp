<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="com.insta.hms.master.URLRoute"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="pagePath" value="<%=URLRoute.CARD_TYPE_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Card Type - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		function keepBackUp(){
			backupName = document.cardtypemasterform.card_type.value;
		}

		function doClose() {
			window.location.href = "${cpath}${pagePath}/list.htm?sortOrder=card_type" +
						"&sortReverse=false";
		}
		function focus() {
			document.cardtypemasterform.card_type.focus();
		}
		
		Insta.masterData=${ifn:convertListToJson(cardTypeDetails)};
		
       function checkMaximumLength(commission) {
    		var val = parseFloat(commission);
    		if ( val < 0.01 || val > 9.99) {
    			alert("Commission Percentage Should be between 0 and 10");
    			return false;
    		}
    		return true;
       }
       function validateForm() {
    		var commission = document.cardtypemasterform.commission_percentage.value;
    		if(commission != '') {
    			if(!isDecimal(commission,2)) {
    				alert("Invalid Commission Percentage");
    				return false;
    			}
    			if(!checkMaximumLength(commission)) 
    				return false;
    		}

    		return true;
    	}

   	</script>
</head>
<body onload= "keepBackUp();" >
        <h1 style="float:left">Edit Card Type</h1>
        <c:url var="searchUrl" value="${pagePath}/show.htm"/>
        <insta:findbykey keys="card_type,card_type_id" fieldName="card_type_id" method="show" url="${searchUrl}"/>
        
<c:set var="actionUrl" value="${cpath}/${pagePath}/update.htm"/>
<form action="${actionUrl}"  name="cardtypemasterform" method="POST">
	
	<input type="hidden" name="card_type_id" value="${bean.card_type_id}"/>
	
	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel">Card Type:</td>
			<td>
				<input type="text" name="card_type" value="${bean.card_type}" class="required validate-length" length="50" 
						title="Card type is required and max length of name can be 50"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Commission (%):</td>
			<td>
				<input type="text" name="commission_percentage"  value="${bean.commission_percentage}" />
			</td>
		</tr>
		<tr>
			<td class="formlabel">Status:</td>
			<td>
				<insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,InActive"/>
			</td>
		</tr>
	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validateForm();"><b><u>S</u></b>ave</button>
		|
			<a href="${cpath}/${pagePath}/add.htm">Add</a>
		|
			<a href="javascript:void(0)" onclick="doClose();">Card Type List</a>
	</div>

</form>
</body>
</html>
