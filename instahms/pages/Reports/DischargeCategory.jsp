<%@page import="java.util.List"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>
<head>
<title>Discharge Category - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<insta:link type="css" file="hms.css"/>
<insta:link type="css" file="aw.css"/>
<insta:link type="script" file="DatePick.js"/>
<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="tableSearch.js"/>
<insta:link type="script" file="ajax.js"/>
<insta:link type="script" file="aw.js"/>

	<script language="javascript" type="text/javascript">

	var mr_no="";
    var typ="";

	function cvtsFunc(){
	    document.getElementById('cvts').style.display='block' ;
	    document.getElementById('gen').style.display='none' ;

	}
	function genFunc(){
	    document.getElementById('cvts').style.display='none' ;
	    document.getElementById('gen').style.display='block' ;

	}
	function print(){

	if(document.forms[0].type[0].checked){

	         if(document.forms[0].mrnoC.value==""){
	         alert("Select the MRNO");
	         return false;
	         }
	         mr_no=document.forms[0].mrnoC.value;
	         typ="CVTS";
	         window.open("bedmanagementreport.do?method=getDischargeCategoryReport&mrno="+mr_no+"&type="+typ);
		}
	if(document.forms[0].type[1].checked){

	         if(document.forms[0].mrnoG.value==""){
	         alert("Select the MRNO");
	         return false;
	         }
	         mr_no=document.forms[0].mrnoG.value;
	         typ="GEN";
	         window.open("bedmanagementreport.do?method=getDischargeCategoryReport&mrno="+mr_no+"&type="+typ);
		}
	}

	</script>

</head>
<body>

<form>




<!-- Main tab--> <TABLE width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
                    <TR>
                      <TD width="10" height="20"><insta:link type="image" file="sub_img01.gif"/></TD>
                      <TD height="20" background="../../images/sub_img03.gif" class="subtitle" width="100%" align="center">Discharge Report Category</TD>
                      <TD width="10" height="20"><insta:link type="image" file="sub_img02.gif"/></TD>
                    </TR>

                    <TR>
                      <TD width="10" background="../../images/sub_img07.gif" height="100%"><insta:link type="image" file="spacer.gif"/></TD>
                      <TD valign="top" bgcolor="#fbe6d3" width="100%" height="100%">
     <!-- Center tab-->    <TABLE width="100%" border="0" cellpadding="0" cellspacing="0" class="tabletext" height="100%">
                              <TR><TD height="9" width="100%"></TD></TR>
                              <TR><TD class="leftnavbg2" width="100%" valign="top" align="middle">
                                 <table width="820" height="1" border="0" cellpadding="0" cellspacing="0" bordercolor='white'>
           <!-- Constant Cell-->  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>

                                  <tr width="100%" valign="top" style="display:block" id="cvts" class="one"><td align='center'>
                                        <span style="color:#800000;font-family:verdhana:font-size:12px">Select MR No :</span>
                          				<select name="mrnoC" class="forminput" style="width:158">
                          				   <option value="">..............Select..............</option>
                          				   <logic:present name="mrnoCVTSList">
                          				   <logic:iterate id="mrnoc" name ="mrnoCVTSList">
                          					 <option value='<bean:write name="mrnoc" property="MR_NO" />'><bean:write name="mrnoc" property="MR_NO"/>
                          				   </logic:iterate>
                          				   </logic:present>
                          				 </select> </td></tr>

                                  <tr width="100%" valign="top"  style="display:none" id="gen" class="one"><td align='center'>
                                         <span style="color:#800000;font-family:verdhana:font-size:12px">Select MR No :</span>
                          				 <select name="mrnoG" class="forminput" style="width:158">
                          				    <option value="">..............Select..............</option>
                          				    <logic:present name="mrnoGenList">
                          					<logic:iterate id="mrnog" name ="mrnoGenList">
                          					  <option value='<bean:write name="mrnog" property="MR_NO" />'><bean:write name="mrnog" property="MR_NO"/>
                          					</logic:iterate>
                          				    </logic:present>
                          				 </select></td></tr>

                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td align='center'>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td align='center'>
                                        <span style="color:#800000;font-family:verdhana:font-size:12px">
                          				<input type="radio" name="type" value="CVTS" id="CVTS" checked  onClick=" cvtsFunc()">CVTS  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                          			    <input type="radio" name="type" value="GEN"  id="GEN" onClick=" genFunc()" >GENERAL</span></td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td align='center'>
                                        <INPUT class=button type="BUTTON"   value="Report" name=Submit10  onclick=" print()" ></td></tr>

           <!-- Constant Cell-->  </table>
                               </TD></TR>
     <!-- Center tab-->    </TABLE>
                       </TD>
                      <TD width="10" background="../../images/sub_img08.gif" height="100%"><insta:link type="image" file="spacer.gif"/></TD>
                    </TR>

                    <TR>
                      <TD width="10" height="1"><insta:link type="image" file="sub_img04.gif"/></TD>
                      <TD height="1" background="../../images/sub_img06.gif" width="796"><insta:link type="image" file="spacer.gif"/></TD>
                      <TD width="10" height="1"><insta:link type="image" file="sub_img05.gif"/></TD>
                    </TR>

<!-- Main tab--> </TABLE>


        </FORM>
  </body>
</html>
