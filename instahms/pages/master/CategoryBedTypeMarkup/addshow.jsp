<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Category Markup - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		function focus(){
			setSelectedIndex(document.forms[0].category_id,'${bean.map.category_id}');
			document.forms[0].category_id.focus();
		}
		function validate() {
		    if (document.forms[0].category_id.value == '') {
		    	alert("Select Category");
		    	document.forms[0].category_id.focus();
		    	return false;
		    }
		    if (document.forms[0].markup_rate.value == '') {
		    	alert("Enter markup Rate");
		    	document.forms[0].markup_rate.focus();
		    	return false;
		    }
			document.forms[0].category_id.disabled = false;
			document.forms[0].bed_type.disabled = false;
			document.forms[0].submit();
			return true;
		}
		function makeingDec(objValue,obj){
		    if (objValue!= '' && objValue!= '.') {
				document.getElementById(obj.name).value = parseFloat(objValue).toFixed(2);
			}
			if (objValue == '.') document.getElementById(obj.name).value = 0.00;
        }
		function setSelectedIndex(opt, set_value) {
			  var index=0;
			  for(var i=0; i<opt.options.length; i++) {
			    var opt_value = opt.options[i].value;
			    if (opt_value == set_value) {
			      opt.selectedIndex = i;
			      return;
			    }
			  }
		}
	</script>
	<style>
		input.num {text-align: right; width: 6em;}
	</style>
</head>
<body onload="focus()">

<form action="CategoryBedTypeMarkup.do" method="GET">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Markup Rate</h1>
	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel">Category:</td>
			<td style="width: 22em;" >
				 <select   name="category_id" class="dropdown">
					 <option value="" >.......Select.......</option>
					 <c:forEach var="cats" items="${cat}">
 						<option value="${cats.CATEGORY_ID}" >${cats.CATEGORY}</option>
					</c:forEach>
				 </select><span class="star">*</span>
				 (Billable Categories).
			</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>

		<tr>
			<td class="formlabel">Bed Type:</td>
			<td><insta:selectdb name="bed_type" value="${bean.map.bed_type}" dummyvalue="...ALL..." dummyvalueId="All"
								 table="bedcharges_view" displaycol="bedtype" valuecol="bedtype"  filtercol="organization" filtervalue="ORG0001" /><span class="star">*</span></td>
		<script>
				<c:if test="${param._method == 'show'}" >
					document.forms[0].category_id.disabled = true;
					document.forms[0].bed_type.disabled = true;
				</c:if>
		</script>
		</tr>
		<tr>
			<td class="formlabel">Markup Rate (%):</td>
			<td><input type="text" name="markup_rate" id="markup_rate" value="${bean.map.markup_rate}" class="num" onkeypress="return enterNumAndDotAndMinus(event);" onblur="return makeingDec(this.value,this);"><span class="star">*</span></td>
		</tr>

</table>
</fieldset>
		<div class="screenActions">
			<button type="button" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
			|
			<c:if test="${param._method != 'add'}">
				<a href="javascript:void(0);" onclick="window.location.href='${cpath}/master/CategoryBedTypeMarkup.do?_method=add'">Add</a>
			|
			</c:if>
			<a href="${cpath}/master/CategoryBedTypeMarkup.do?_method=list&sortOrder=category&sortReverse=false">Back To Dash Board</a>
		</div>

</form>


</body>
</html>
