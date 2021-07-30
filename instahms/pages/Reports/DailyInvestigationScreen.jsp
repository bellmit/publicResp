<%@page import="java.util.List"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>
<head>
<title>Daily Investigations - Insta HMS</title>
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
 function validate(){
        if(document.getElementById('mrNo').value==""){
	    alert("Select the MR No");
	    document.getElementById('mrNo').focus();
	    return false;
	    }

        if(document.getElementById('date').value==""){
	    alert("Select the Date");
	    return false;
	    }else if(isFutureDate('date')==-1){ // Future Date Validation
	    alert("Selected date should not be greater than Current date");
	    return false;
	    }
	  return true;
    }

    function report(){
     if(validate()){
      url="diagreport.do?&method=getDailyInvestigationsReport&date="+document.getElementById('date').value+"&mrNo="+document.getElementById('mrNo').value;
     // alert(url);
      window.open(url);
      document.getElementById('date').value="";
      document.getElementById('mrNo').options[0].selected=true;
     }

    }
	</script>

</head>
<body   >

<FORM method="POST">




<!-- Main tab--> <TABLE width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
                    <TR>
                      <TD width="10" height="20"><insta:link type="image" file="sub_img01.gif"/></TD>
                      <TD height="20" background="../../images/sub_img03.gif" class="subtitle" width="100%" align="center">Daily Investigations </TD>
                      <TD width="10" height="20"><insta:link type="image" file="sub_img02.gif"/></TD>
                    </TR>

                    <TR>
                      <TD width="10" background="../../images/sub_img07.gif" height="100%"><insta:link type="image" file="spacer.gif"/></TD>
                      <TD valign="top" bgcolor="#fbe6d3" width="100%" height="100%">
     <!-- Center tab-->    <TABLE width="100%" border="0" cellpadding="0" cellspacing="0" class="tabletext" height="100%">
                              <TR><TD height="9" width="100%"></TD></TR>
                              <TR><TD class="leftnavbg2" width="100%" valign="top" align="middle">
       <!-- Constant Cell-->   <table width="820" height="10" border="0" cellpadding="0" cellspacing="0" bordercolor='white'>
                                  <tr width="100%" height="10" valign="top"><td colspan='2'>&nbsp;</td></tr>
                                  <tr width="100%" height="10" valign="top"><td colspan='2'>&nbsp;</td></tr>
                                  <tr width="100%" height="10" valign="top">
                                      <td align='right' width='50%'><span style="font-family:Verdana;font-size:12px; color:#800000">Select MR No :&nbsp;&nbsp;</span></td>
                                      <td align='left'  width='50%' >
                                              <select id='mrNo' name='mrNo' class='forminput' style='width:120'>
                                                <option value="">.......SELECT...........</option>
                                                <logic:present name='mrnoList'>
                                                <logic:iterate name='mrnoList' id='list'>
                                                  <option value='<bean:write name="list" property="MR_NO" />'><bean:write name="list" property="MR_NO" /></option>
                                                </logic:iterate>
                                                </logic:present> </select> </td></tr>
                                  <tr width="100%" height="10" valign="top"><td colspan='2'>&nbsp;</td></tr>
                                  <tr width="100%" height="10" valign="top">
                                      <td align='right' width='50%'><span style="font-family:Verdana;font-size:12px; color:#800000">Select Date&nbsp;&nbsp; :&nbsp;&nbsp;</span></td>
                                      <td align='left'  width='50%'><input type="text" name="date" size="20" id="date" onclick="javascript:show_calendar('forms[0].date');"  class="forminput" readonly></td></tr>
                                  <tr width="100%" height="10" valign="top"><td colspan='2'>&nbsp;</td></tr>
                                  <tr width="100%" height="10" valign="top"><td colspan='2'>&nbsp;</td></tr>
                                  <tr width="100%" height="10" valign="top"><td colspan='2' align = center><input type="button" value="Report" name="B1" class="button" onclick="report()"></td></tr>
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
