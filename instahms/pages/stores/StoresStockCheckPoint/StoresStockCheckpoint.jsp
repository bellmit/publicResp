<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>
<head>
<title><insta:ltext key="storemgmt.checkpointdetails.addedit.title"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="i18nSupport" content="true"/>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="tableSearch.js"/>
<script>

function checkDuplicates(){
	var cName=trimAll(document.getElementById("checkpoint_name").value);
	'<c:forEach items="${chkpoints}" var="chkpointsid">'
		var chkName='${chkpointsid.CHECKPOINT_NAME}';
		var chkId = '${chkpointsid.CHECKPOINT_ID}';
		var cId = document.stockpointform.checkpoint_id.value;
		if (chkId != cId) {
			if(chkName==cName){
				showMessage("js.stores.mgmt.checkpointname.alreadyexists");
				document.getElementById("checkpoint_name").value = "";
				document.getElementById("checkpoint_name").focus();
				return false;
		    }
		}
	'</c:forEach>'
}
function Save(btn){
	var field1=document.stockpointform.checkpoint_name.value;
    if(trimAll(field1)==""){
	    showMessage("js.stores.mgmt.checkpointname.notempty");
	    document.stockpointform.checkpoint_name.value="";
	    document.stockpointform.checkpoint_name.focus();
	    return false;
    }
    btn.disabled = true;
    document.stockpointform.action="stockcheckpoint.do?_method=saveChkpointDetails";
  	document.stockpointform.submit();
  	return true;
 }

function funcClose(){
	document.getElementById('_method').value = 'viewCheckpoints';
	return true;
}
function chk1(e){
	  var key=0;
	  if(window.event || !e.which)
	  {
		 key = e.keyCode;
   	  }
	  else
	  {
		 key = e.which;
	  }
      if(document.stockpointform.remarks.value.length<3900 || key==8)
      {
        key=key;
        return true;
      }
      else
      {
       key=0;
       return false;
     }
}
function chklen1(){
  if(document.stockpointform.remarks.value.length > 39){
  	showMessage("js.stores.mgmt.remarksshouldbe.3900chars");
  	var s = document.stockpointform.remarks.value;
  	s = s.substring(0,3900);
  	document.stockpointform.remarks.value = s;
  }
}
</script>
<insta:js-bundle prefix="stores.mgmt"/>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath }"/>
<body class="yui-skin-sam" >
<h1><insta:ltext key="storemgmt.checkpointdetails.addedit.checkpointdetails"/></h1>
<form method="GET" action="${cpath }/pages/stores/stockcheckpoint.do" name="stockpointform">
<input type="hidden" name="operation" value="${not empty chkdto.map.checkpoint_id ? 'update' : 'insert'}" />
<input type="hidden" name="_method" value="saveChkpointDetails" id="_method"/>
<input type="hidden" name="checkpoint_id" value="${chkdto.map.checkpoint_id}"/>

<fieldset  class="fieldSetBorder">
<legend class="fieldSetLabel"> <insta:ltext key="storemgmt.checkpointdetails.addedit.checkpoint"/></legend>
<table   class="formtable" >
  <tr>
     <td class="formlabel"><insta:ltext key="storemgmt.checkpointdetails.addedit.checkpointname"/>:</td>
     <td ><input type="text" name="checkpoint_name" id="checkpoint_name" value="${chkdto.map.checkpoint_name }"  maxlength="50" class="required" onChange="return checkDuplicates();" title='<insta:ltext key="storemgmt.checkpointdetails.addedit.longtemplate"/>'><span class="star">*</span></td>
 </tr>
  <tr>
     <td class="formlabel"><insta:ltext key="storemgmt.checkpointdetails.addedit.remarks"/>:</td>
     <td><textarea rows="3" cols="40" name="remarks" id="remarks" onkeypress="return chk1(event);" onblur="chklen1();">${chkdto.map.remarks}</textarea><insta:ltext key="storemgmt.checkpointdetails.addedit.maxcharacters"/></td>
 </tr>
  </table></fieldset>

  <div class="screenActions">
		<button type="button" accesskey="S" name="save" class="button" onclick="return Save(this);" ><b><u><insta:ltext key="storemgmt.checkpointdetails.addedit.s"/></u></b><insta:ltext key="storemgmt.checkpointdetails.addedit.ave"/></button>
		<a href="${cpath }/pages/stores/stockcheckpoint.do?_method=viewCheckpoints&sortOrder=checkpoint_name&sortReverse=false"><insta:ltext key="storemgmt.checkpointdetails.addedit.backtodashboard"/></a>
  </div>

  </form>
  </body>
  </html>
