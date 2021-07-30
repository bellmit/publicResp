<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.bob.hms.common.AutoIncrementId,java.util.ArrayList,java.util.Iterator" %>
	<html>
	<head>
	<title>Search MRNo - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="aw.js"/>
	<insta:link type="css" file="aw.css"/>
	<SCRIPT language="javascript" type="text/javascript">

	function getMrnoList(){
	  var condition ="";
	  for(var i =0;i<document.forms[0].condition.length;i++){
	    	if(document.forms[0].condition[i].checked){
	    		condition=document.forms[0].condition[i].value;
	    		break;
	        }
	    }

		if(document.forms[0].conditionValue.value =="" && condition!=4){
	    	alert("Enter Search Criteria");
	     	document.forms[0].conditionValue.focus();
	      	for(var i =0;i<document.forms[0].condition.length;i++){
	    		if(document.forms[0].condition[i].checked){
	    			document.forms[0].condition[i].checked=false;
	    			break;
	        	}
	    	}
	    	resetAll();
	      	return;
	 	}
	    if(condition==""){
	    	alert("Check one Radio button");
	     	return;
	    }
    	if(window.XMLHttpRequest){
	    	reqObject = new  XMLHttpRequest();
	 	} else if(window.ActiveXObject) {
      		reqObject=new ActiveXObject("MSXML2.XMLHTTP");
     	}
    	if(reqObject) {
	    	reqObject.onreadystatechange=function() {
				if(reqObject.readyState == 4 && reqObject.status == 200) {
					respXML=reqObject.responseXML;
					if(respXML!=null){
						getMrnosXMLData();
					}
				}//if
			} //function
	    	var url="./MrnoSearchAction.do?method=getSearchResults&searchType="+condition+"&searchVal="+document.forms[0].conditionValue.value.toUpperCase()+"&index=${ifn:cleanURL(index)}";
			<c:if test="${not empty requestScope.admitType}">
				url+="&admitType=<%=request.getAttribute("admitType")%>&searchBedType=<%=request.getAttribute("searchBedType")%>&wardNo=<%=request.getAttribute("wardNo")%>";
			</c:if>
			reqObject.open("POST",url.toString(), true);
			reqObject.send(null);
		}
	}

	var doc;
	var x;
  	function getMrnosXMLData() {
	 	x = respXML.documentElement;
     	populateMrnoGrid();
  	}

 	function populateMrnoGrid() {
 		var len=x.childNodes[0].childNodes.length;
 	 	var resultDivHtml = '<table width="100%" cellpadding=0 cellspacing=0 border=1  style="font-family:verdana;font-size:14;font-weight:bold;color:darkgreen" >'
                         			+'<tr style="font-size:12;font-weight:bold;background:#8FBC8F;color:black"> <td width="10%">&nbsp;SNo</td><td width="20%" align="center">MRNO</td><td width ="45%" align="center">PATIENT NAME</TD><td width="25%" align="center">Mobile No/Phone No</td></tr>';

 		if(len ==0){
 			resultDivHtml+="<tr><td  width='100%' colspan='4' align='center'>No Results found</td></tr>";
 		}else{
	  		var nodes = x.childNodes[0].childNodes;
	    	for(var i=0;i<len;i++){
	    		resultDivHtml+='<tr ><td width="10%" height="20px">&nbsp;'+(i+1)+'</td><td width="20%" height="25px">&nbsp;<a href="javascript:setMrno(\''+nodes[i].attributes.getNamedItem('class1').nodeValue+'\')" style="color:darkgreen">'+nodes[i].attributes.getNamedItem('class1').nodeValue+'</a></td><td width ="45%" style="font-size:13">&nbsp;'+nodes[i].attributes.getNamedItem('class2').nodeValue+'</TD><td width="25%" style="font-size:13">&nbsp;'+nodes[i].attributes.getNamedItem('class3').nodeValue+'</td></tr>';
	    	}
		}
	    resultDivHtml+="</table>";
	    document.getElementById("resultHeadding").innerHTML="<u>SEARCH RESULTS</u>";
	    document.getElementById("resultDiv").innerHTML=resultDivHtml;
		document.getElementById("resultDiv").style.visibility="visible";
 	}

	function setMrno(mrno){
		opener.document.<%=request.getAttribute("form")%>.<%=request.getAttribute("field")%>.value=mrno;
        /* setting focus and moving it out does not work on firefox, call onblur directly */
		opener.document.<%=request.getAttribute("form")%>.<%=request.getAttribute("field")%>.onblur();
		//opener.document.<%=request.getAttribute("form")%>.<%=request.getAttribute("nextField")%>.focus();
		var frm='<%=request.getAttribute("form")%>';
		if(frm=="diagcancellform")
			window.opener.removeData();
		window.close();

	}

	function resetAll(){
		document.forms[0].conditionValue.value="";
		if(document.forms[0].condition[0].checked) {
			document.forms[0].condition[0].checked=false;
		}else if(document.forms[0].condition[1].checked){
			document.forms[0].condition[1].checked=false;
		}else if(document.forms[0].condition[2].checked){
			document.forms[0].condition[2].checked=false;
		}else if(document.forms[0].condition[3].checked){
			document.forms[0].condition[3].checked=false;
		}
		document.getElementById("resultHeadding").innerHTML="";
		document.getElementById("resultDiv").style.visibility="hidden";
	}
	</SCRIPT>

</head>
<body class="setMargin">
<form method="post">

<table border="0" width="100%" height="100%" cellpadding="0%" cellspacing="0%" align="center">
   <!--tr>
	  <td class="topLeftCurve"></td>
	  <td width="97%" align="center" class="topLine">
	  <span class="pageHeader">Search
	   </span>
	  </td>
	  <td class="topRightCurve"></td>
	</tr-->
	 <tr>
		  <td height="100%" class="leftLine" width="1%"></td>
		  <td valign="top" height="100%" width="0%">
			<table border="0" width="100%" height="100%" cellpadding="0%" cellspacing="0%">
			<tr>
			<tr>
          		<td width="100%" height="2%" align="center" class="topLineDownSpace">
          		<span class="pageHeader">Search</span>
        	<tr>
			<td width="100%" height="2%" class="topLineDownSpace"><span class="resultMessage">

			</span></td>
			</tr>
			<tr>
			<td class="totalBG" height="100%" valign="top" width="0%" align="center">
			<table  border="0" width="100%" height="100%" cellpadding="0%" cellspacing="0%" align="center">
					<tr>
						<!-- To maintain specific height from the outlet BEGIN -->
						<td colspan="2" height="20px" width="0%"></td>
						<!-- To maintain specific height from the outlet BEGIN -->
					</tr>
					<tr>
					<!-- To maintain specific width from the outlet BEGIN -->
					<td width="0%"></td>
					<!-- To maintain specific width from the outlet BEGIN -->
					<td  width="100%">
					<!--  Actual Design begins in below table -->
						<!--  DESIGN BEGIN  -->
						<table cellpadding="0" cellspacing="0"  border="0">
						<tr>
						<td valign="top" class="totalbg" width="100%" height="100%" align="center">
                      <table width="100%"  border="0" cellpadding="5" cellspacing="0" class="tabletext" >


                        <tr class="label">
                          <td   width="100%" valign="top" align="center">
		                          Search Criteria <INPUT type="text" name="conditionValue" size="25" class="forminput" style="height:18"></td>
		                   </tr>
		                   <tr class="totalbg" align="center"><td class="label">
		                           <input type="radio" name="condition" value="1" onclick="getMrnoList();">First Name&nbsp;&nbsp;
		                          <input type="radio" name="condition" value="2" onclick="getMrnoList();">Last Name&nbsp;&nbsp;
		                          <input type="radio" name="condition" value="3" onclick="getMrnoList();">Mobile No/Phone No&nbsp;&nbsp;
		                          <input type="radio" name="condition" value="4" onclick="getMrnoList();">All&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		                          <input type="button" value="Reset" onclick="resetAll()" class="button"/></td>
                        </tr>
                          <tr class="label"><td height="30" align="center"><div id ='resultHeadding'></div></td></tr>

                        <tr class="totalbg">
                         <td width="100%" class="totalbg" height="269px" >
                         	<DIV id="resultDiv" style="width:100%;height:269px;overflow:auto;">
                         	</DIV>


                         </td>
                        </tr>

                        </table>
                        </td>
                        <td width="100%">&nbsp;&nbsp;&nbsp;&nbsp;</td>
						</tr>
						</table>
						<!--  DESIGN END  -->
					</td>
				</tr>
			</table>
			</td>
			</tr>
			</table>
		  </td>
		  <td height="100%" class="rightLine"></td>
	 </tr>
	  <tr>
	  <td class="bottomLeftCurve"></td>
	  <td width="96%" class="bottomLine"></td>
	  <td  class="bottomRightCurve"></td>
	</tr>
  </table>
</form>
</body>
</html>
