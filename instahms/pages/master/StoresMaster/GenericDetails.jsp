<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<html>
<head>
<title>Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="tableSearch.js"/>
<script>

var sublist = ${scList};
var generics = JSON.parse('${ifn:cleanJavaScript(generics)}');
function Save(){
var field1=document.genericForm.generic_name.value;
    if(trimAll(field1)==""){
	    alert("Generic Name Should Not Be Empty");
	    document.genericForm.generic_name.value="";
	    document.genericForm.generic_name.focus();
	    return false;
    }
    
    if ( !checkDuplicates() ){
    	return false;
    }
    document.genericForm.action="StoresMastergendetails.do?_method=saveGenericDetails";
  	document.genericForm.submit();
  	return true;
 }

 function checkDuplicates(){
	var genericName=document.getElementById("generic_name").value;
	var filteredGenericName = findInList(generics, 'generic_name', genericName);
	if ( filteredGenericName ==  null ){
		return true;
	}
	var genName=  filteredGenericName.generic_name;
	var genericId = filteredGenericName.generic_code;
	var genId = document.genericForm.generic_code.value;
	if (genericId != genId) {
		if(genericName==genName){
               alert("The generic Name Already Exists Please Enter Another Name");
			document.getElementById("generic_name").value = "";
			document.getElementById("generic_name").focus();
			return false;
	    }
	}
	return true;
}
function selectsubclassi(){
        var i=0;
    	var form = document.genericForm;
     	var classi_id = form.classification_id.value;
     	form.sub_classification_id.length = parseFloat(1);
        var index = 1;

    	for(i=0;i<sublist.length;i++) {
      			if(sublist[i].classification_id == classi_id) {
        			form.sub_classification_id.length = parseFloat(index)+parseFloat(1);
        			form.sub_classification_id.options[index].text = sublist[i].sub_classification_name;
        			form.sub_classification_id.options[index].value = sublist[i].sub_classification_id;
        			if(form.operation.value == 'update')
        			{
        			  if(sublist[i].sub_classification_id == '${gendto.map.sub_classification_id}')
        			  form.sub_classification_id.selectedIndex = index;
        			}
        			index++;
     			 }
       }
}
   <c:if test="${not empty gendto}" >
          Insta.masterData=${genericDetailsLists} ;
   </c:if>
</script>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath }"/>
<body class="yui-skin-sam" onload="selectsubclassi()"; >
<c:choose>
    <c:when test="${not empty gendto}">
         <h1 style="float:left">Edit Generic Details</h1>
         <c:url var="searchUrl" value="/master/StoresMastergendetails.do"/>
         <insta:findbykey keys="generic_name,generic_code" fieldName="generic_id" method="getGenericDetailsScreen" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
        <h1>Add Generic Details</h1>
    </c:otherwise>
</c:choose>
<form method="GET" action="" name="genericForm">
<input type="hidden" name="operation" value="${not empty gendto.map.generic_code ? 'update' : 'insert' }">
<input type="hidden" name="generic_code" value="${gendto.map.generic_code}"/>
<input type="hidden" name="_method" value="saveGenericDetails"/>

<fieldset  class="fieldSetBorder">
<legend class="fieldSetLabel"> Generic</legend>
<table   class="formtable" >
  <tr>
     <td class="formlabel">Generic Name:</td>
     <td ><input type="text" name="generic_name" id="generic_name" value="<c:out value='${gendto.map.generic_name}'/>"  maxlength="100" ><span class="star">*</span></td>
     <td class="formlabel">Classification Name:</td>
     <td><insta:selectdb name="classification_id" table="generic_classification_master" valuecol="classification_id"
											displaycol="classification_name" value="${gendto.map.classification_id}"  dummyvalue="..select.." filtered="false" onchange="selectsubclassi()"/>
	 </td>
	 <td>&nbsp;</td>
	 <td>&nbsp;</td>
 </tr>
 <tr>
   <td class="formlabel">sub Classification Name:</td>
   <td><select name="sub_classification_id" id="sub_classification_id" class="dropdown" tabindex="350">
           <option value="">..select..</option>
      </select>
  </td>
  <td class="formlabel">Standard Adult Dose:</td>
  <td><input type="text" name="standard_adult_dose" id="standard_adult_dose" size="20" value="${gendto.map.standard_adult_dose}">
     </td>
 </tr>
 <tr>
    <td class="formlabel">Criticality:</td>
    <td>
	  <insta:selectoptions name="criticality" optexts="Vital,Essential,Desirable" class="dropdown" style="width: 138px"
							opvalues="V,E,D" dummyvalue="..select.." value="${gendto.map.criticality}"/>
	</td>
	<td class="formlabel">Status:</td>
	<td><insta:radio name="status" radioValues="A,I" value="${not empty gendto.map.status ? gendto.map.status : 'A'}"
		radioText="Active,Inactive" radioIds="active,inactive" /></td>
</tr>
  </table></fieldset>

  <div class="screenActions">
		<button type="button" accesskey="S" name="save" class="button" onclick="return Save()" ><b><u>S</u></b>ave</button>
		|
		<c:if test="${not empty gendto}">
			<a href="javascript:void(0);" onclick="window.location.href='${cpath}/master/StoresMastergendetails.do?_method=getGenericDetailsScreen'">Add</a>
		|
		</c:if>
		<a href="${cpath }/master/StoresMastergendetails.do?_method=getGenericDashBoard&sortOrder=generic_name&sortReverse=false&status=A">Back To DashBoard</a>
  </div>

  </form></body></html>
