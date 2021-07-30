<%@ page contentType="text/html;charset=windows-1252"%>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
    <html>
    <head>
    <title>Change Password - Insta HMS</title>
    <insta:link type="script" file="usermanager/checkPasswordStrength.js"/>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <insta:link type="css" file="hms.css"/>

    <script>
	var passwordRules = ${passwordRules};
    function submitFun() {
        var oldpwd=document.getElementById("oldpwd").value;
        var newpwd=document.getElementById("pwd").value;
        if(oldpwd==""){
            alert("Enter Old Password");
            document.getElementById("oldpwd").focus();
            return false;
        }
        if(oldpwd==newpwd) {
	        alert("New password is same as the old password");
	        document.forms[0].pwd.value="";
            document.forms[0].cpwd.value="";
            document.forms[0].pwd.focus();
	        return false;
        }
        if(!checkPasswordStrength(document.forms[0].pwd)) {
			return false;
		}
        if(!passequal()){
            return false;
        }
        if(window.confirm("Do you want to change the password")) return true;
        else return false;
    }

    function passequal() {
        var str = document.getElementById("pwd").value;
        var str1= document.getElementById("cpwd").value;

        if(str=="") {
            alert("Enter New Password")
            document.forms[0].pwd.focus();
            return false;
        }
        if(!(str==str1)) {
            alert("Passwords mismatch !! Fill again !!");
            document.forms[0].pwd.value="";
            document.forms[0].cpwd.value="";
            document.forms[0].pwd.focus();
            return false;
        }
        return true;
    }

    function init() {
    	document.getElementById("oldpwd").value="";
    }
    </script>
</head>

<body onload="init();" class="setMargin" class="yui-skin-sam">
<form action="ChangePassword.do?method=updatePassword" onsubmit="return submitFun()" method="post">
<input type="hidden" name="uid" id="uid" value="<%=session.getAttribute("userid")%>"/>
<table width="100%" class="formtable" height="80%"  border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td colspan="3" width="100%" height="5%">&nbsp;</td>
  </tr>
  <tr>
      <td width="5%" height="100%"></td>
    <td valign="top" height="100%" width="100%">
    <fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Password Details</legend>
      <table width="100%"  border="0" cellspacing="0" cellpadding="0" height="100%">
        <tr>
          <td height="100%" width="100%" valign="top">
            <table cellspacing="0" cellpadding="0" width="100%" height="100%" align="center">
              <tr height="15%">
                <td class="resultMessage"><logic:present name="msg"><bean:write name="msg"/></logic:present></td>
               </tr>
              <tr height="40">
                <td align='right' width="50%" class="label">Old Password</td>
                <td align='left' width="50%" >&nbsp;&nbsp;&nbsp;&nbsp;<input name="oldpwd" type="password" class="forminput" id="oldpwd" maxlength="50" title="OldPassword" ><span class="star">&nbsp;*</span></td>
               </tr>
               <tr height="40">
                <td valign='middle' class="label" align="right" >New Password</td>
                <td valign='middle' class='bodybg' align="left"  >
                &nbsp;&nbsp;&nbsp;&nbsp;<input name="pwd" type="password" class="forminput" id="pwd" maxlength="50" title="Password"><span class="star">&nbsp;*</span>
                </td>
              </tr>
              <tr height="40">
                <td valign='middle' class="label" align='right'>Confirm Password</td>
                <td valign='middle' align='left'  >
                &nbsp;&nbsp;&nbsp;&nbsp;<input name="cpwd" type="password" class="forminput" id="cpwd" maxlength="50" title="Confirm Password" ><span class="star">&nbsp;*</span>
                </td>
              </tr>
              <tr height="20%">
                <td align="center" colspan="2"><input type="submit" name="Submit" value="Submit" class="button" >
                <input type="reset" name="reset" value="Reset" class="button"></td>
             </tr>
              <tr>
                <td colspan="2" align="center"></td>
              </tr>
            </table>
          </td>
        </tr>
        <tr>
        <td>
        <table width="100%" border="0" cellpadding="0" class="details">
            <tr>
                <td width="100%" class="totalBG" align="center"><jsp:include page="frame/footer.jsp" /></td>
              </tr>
            </table>
        </td>
        </tr>
      </table>
      </fieldset>
      <p>&nbsp;
    </td>
    <td width="3" height="427"></td>
  </tr>
</table>
</form>
</body>
</html>
