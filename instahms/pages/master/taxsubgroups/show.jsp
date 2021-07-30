<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="pagePath" value="<%=URLRoute.ITEM_SUB_GROUP_PATH %>"/>
<html>
<head>
<insta:link type="script" file="hmsvalidation.js" />
<script>

function initshow(){
	var taxRate = document.getElementById('tax_rate').value;
	if(taxRate == null || taxRate == ''){
		document.getElementById('exempt').checked=true;
		document.getElementById("tax_rate").readOnly = true;
		document.getElementById('tax_rate_expr').disabled = true;
	}
}

function checkDuplicates() {
	document.getElementById('item_subgroup_name').value = trim(document.getElementById('item_subgroup_name').value);
	//var code = document.getElementById('subgroup_code').value.toLowerCase();
	var itemGroupName = document.getElementById('item_group_id').value;
	var id = document.itemSubGroupForm.item_subgroup_id.value;
	var itemSubgroupName = document.getElementById('item_subgroup_name').value;
	//var subGroupCode = document.getElementById('subgroup_code').value;
	var rateTax = document.getElementById('tax_rate').value;
	var validityStart = document.getElementById('validity_start').value;
	var status = document.getElementById('status').value;
	//var validityend = document.getElementById('validity_end').value;
	
	if(itemGroupName == "") {
		alert("Please enter tax group.");
		document.getElementById('item_group_id').focus();
		return false;
	}
	if(itemSubgroupName == "") {
		alert("Please enter tax sub group.");
		document.getElementById('item_subgroup_name').focus();
		return false;
	}
	/* if(subGroupCode == ""){
		alert("Please enter tax sub group code.");
		document.getElementById('subgroup_code').focus();
		return false;
	} */
	if(rateTax == ""){
		if(!document.getElementById('exempt').checked){
			alert("Please enter tax rate.");
			document.getElementById('tax_rate').focus();
			return false;
		}
	}
	
	if ( validityStart== null || validityStart== ""){
		alert("Please enter validity start date");
		document.getElementById('validity_start').focus();
		return false;
	}
	if(status == 'I') {
		if(${subgroupHasMasterReferences}){
			var res=confirm("Tax subgroup mapped to items, continues as marking tax subgroup with inactive ? ");
			if(res == true){
				return true;
			}
				return false;
		}
		
	}
	
	return compareDate();
}
function compareDate() {
	var start = getDatePart(parseDateStr(itemSubGroupForm.validity_start.value)); 
	var end = getDatePart(parseDateStr(itemSubGroupForm.validity_end.value));
	if(document.getElementById('validity_end').value!=""){
		if (daysDiff(start,end) < 0) {
	    	   alert("End Date should be greater than Start Date....!"); 
	       		document.getElementById("validity_end").focus();
	       		return false;
	    }
	}
    return true;
}

function numbersonly(e) 
{
    var key;
    var keychar;

    if (window.event) 
        key = window.event.keyCode;
    else if (e) 
        key = e.which;
    else 
        return true;

    keychar = String.fromCharCode(key);

    if ((key==null) || (key==0) || (key==8) ||  (key==9) || (key==13) || (key==27))
       return true;     
    else if ((("0123456789").indexOf(keychar) > -1))
       return true;
    else
       return false;
} 
function numbersAndFloatsOnly(e) 
{
    var key;
    var keychar;

    if (window.event) 
        key = window.event.keyCode;
    else if (e) 
        key = e.which;
    else 
        return true;

    keychar = String.fromCharCode(key);

    if ((key==null) || (key==0) || (key==8) ||  (key==9) || (key==13) || (key==27))
       return true;     
    else if ((("0123456789").indexOf(keychar) > -1))
       return true;
    else if (e && (keychar == "."))
       return true;        
    else
       return false;
}

function ExemptTaxRate(){
	var exempt = document.getElementById('exempt');
	if (exempt.checked)
	{
		document.getElementById("tax_rate").readOnly = true;
		document.getElementById('tax_rate').value = "";
		document.getElementById("tax_rate_expr").disabled = true;
		document.getElementById('tax_rate_expr').value = "";
	}else{
		document.getElementById("tax_rate").readOnly = false;
		document.getElementById("tax_rate_expr").disabled = false;
	}
	
}

</script>

<style type="text/css">
  #myAutoComplete{
	 width:15em; /* set width here or else widget will expand to fit its container */
     padding-bottom:2em;
  }
</style>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Edit Tax Sub Group - Insta HMS</title>
</head>

<body onload="initshow();"  class="yui-skin-sam">
<h1>Edit Tax Sub Group</h1>
<insta:feedback-panel/>

<c:set var="actionUrl" value="${cpath}${pagePath}/update.htm"/>
<form action="${actionUrl}" method="POST" name="itemSubGroupForm">
	<input type="hidden" name="item_subgroup_id" id="item_subgroup_id" value="${bean.item_subgroup_id}"/>
	<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Tax Sub Group</legend>
		<table class="formtable" >
			<tr>
				<td class="formlabel">Tax Group:</td>
				<td>
					<insta:selectdb name="item_group_id" id="item_group_id" table="item_groups" valuecol="item_group_id"
							displaycol="item_group_name"
							value="${bean.item_group_id}"  orderby="item_group_name" dummyvalue="-- Select --" dummyvalueId="" 
							filtercol ="item_group_type_id"  filtervalue ="TAX"/>
							<span class="star">*</span>
				</td>
				<td class="formlabel">Tax Sub Group:</td>
				<td>
					<input type="text" name="item_subgroup_name" id="item_subgroup_name" value="${bean.item_subgroup_name}"  class="required validate-length"
					length="50" title="Max length of Tax subgroup  can be 50"  />
					<span class="star">*</span>
				</td>
				
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" id="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
			<tr>
				<%-- <td class="formlabel">Tax Sub Group Code:</td>
				<td>
					<input type="text" name="subgroup_code" id="subgroup_code" value="${bean.subgroup_code}"  class="required validate-length"
					length="6" title="Max length of tax sub group code can be 6"  />
					<input type="hidden" name="subgroup_code_org_hid" value="${bean.subgroup_code}"/>
					<span class="star">*</span>
				</td> --%>
				<td class="formlabel">Display Order:</td>
				<td>
					<input type="text" name="item_subgroup_display_order" id="item_subgroup_display_order" value="${bean.item_subgroup_display_order}"  onkeypress="return numbersonly(event);"  />
				</td>
			</tr>
			
		</table>
	</fieldset>
	
	<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Tax Sub Group Details</legend>
		<table class="formtable">
			<tr>
				<td class="formlabel">Tax rate:</td>
				<td>
					<input type="text" name="tax_rate" id=tax_rate  value="${bean.tax_rate}"  onkeypress="return numbersAndFloatsOnly(event);"  />
					<span class="star">*</span>
					<input type="checkbox" id="exempt" id="exempt" value="" onclick="ExemptTaxRate();" />Exempt
				</td>
			</tr>
			<tr>
					<td class="formlabel">Validity Start:</td>
					<td>
						<fmt:formatDate value="${bean.validity_start}" pattern="dd-MM-yyyy" var="from_date"/>
						<insta:datewidget name="validity_start" value="${from_date}" id="validity_start"
							btnPos="left" title="Valid from start" />
							<span class="star">*</span>
					</td>
					<td class="formlabel">Validity End:</td>
					<td>
						<fmt:formatDate value="${bean.validity_end}" pattern="dd-MM-yyyy" var="to_date"/>
						<insta:datewidget name="validity_end" value="${to_date}" id="validity_end"
							 btnPos="left" title="Valid Upto" />
					</td>
			</tr>
			<tr>
				<td class="formlabel">Tax Rate Expression:</td>
				<td colspan="5">
				<c:choose>
					<c:when test="${roleId == 1 || roleId == 2}">
						<textarea rows="3" cols="100" name="tax_rate_expr" id="tax_rate_expr"  maxlength="1000">${bean.tax_rate_expr}</textarea>
					</c:when>
					<c:otherwise>
						<insta:truncLabel value="${bean.tax_rate_expr}" length="30"/>
					</c:otherwise>
				</c:choose>
				</td>
			</tr> 
			
		</table>
	
	</fieldset>
	
<dl class="accordion" style="margin-bottom: 10px;">
	<dt>
		<span>Expression Help</span>
		<div class="clrboth"></div>
	</dt>
	<dd id="expr_tokens">
		<div class="bd">
			<table class="resultList">
				<tr>
					<td colspan="7"><b> Sample expression:</b>
						<#if center_id == 0 && dept_name == 'DEP0002'> 20<#else>5 &lt;/#if>
					</td>
				</tr>
				<tr>
					<td colspan="7"><b>Available Tokens:</b></td>
				</tr>
				<tr>
					<td>center_id</td>
					<td>dept_name(Admitting department id)</td>
				</tr>
			</table>
		</div>
	</dd>
</dl>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return checkDuplicates()"><b><u>S</u></b>ave</button>
		| <a href="${cpath}${pagePath}/add.htm">&nbsp; Add &nbsp;</a>
		| <a href="${cpath}${pagePath}/list.htm?list&status=A">Tax Sub Group List</a>
	</div>

</form>
</body>
</html>
