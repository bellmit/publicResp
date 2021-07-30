<%@page isELIgnored="false"%>
<%@page import="java.util.ArrayList"%>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<title>Insta HMS</title>
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="js" file="emr/gynocology.js" />
<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="script" file="date_go.js"/>
<script language="JavaScript" src="../../scripts/instagrid.js"></script>
<html>
<head>
<script language="javascript" type="text/javascript">


    function funcClose()
   {
    if(confirm("Do you want to Close?"))
    {
      document.forms[0].action = "../../../ClosePage.do";
      document.forms[0].submit();
      return true;
    }
    else return false;
  }

</script>
</head>
<body onload="onfocus('mrno');">
<form action="gynocologysave.do" name="gynocologysavedata" method="post">
<input	type="hidden" name="familyhistory" value="familyhistory">
<input	type="hidden" name="method" value="">
<table border="0" width="100%" height="100%">
    <tr><td  align="center" valign="top"><span class="pageHeader">OP Consultation </span></td><td></td><td></td></tr>
  <tr height="10"></tr>

  <tr><td align="center" class="resultMessage">
    <logic:notEmpty name="msg" ><bean:write name="msg" scope="request"/></logic:notEmpty></td></tr>
  <tr>
     <td width="40%" valign="top" colspan="2"  align="left">
       <logic:iterate id="patientdetailsId" name="patientdetails">
          <fieldset class="fieldSetBorder"  style="width: 735; height: 110"><legend
         class="fieldSetLabel">Patient&nbsp;Details</legend>
           <table border="0" align="left" cellpadding="0" cellspacing="0">
             <tr>
              <td class="label" colspan="3">MR No : </td><td width="10px"></td>
               <td class="fieldSetLabel"><input type="text" name="mrno" id="mrno"  readonly="readonly" value="<bean:write name="patientdetailsId" property="MR_NO"/>"/><input type="hidden" name="patientid"	id="patientid"  value="<bean:write name="patientdetailsId" property="PATIENT_ID"/>" readonly="readonly"/></td>
               <td width="20px"></td>
                      <td class="label">Age : </td><td width="10px"></td>
                      <td class="label"><input type="text" name="age" id="age" value="<bean:write name="patientdetailsId" property="PATIENT_AGE"/>"  readonly="readonly"/></td>
            </tr>
            <tr height="10"></tr>
            <tr>
              <td class="label" colspan="3">Name : </td><td width="10px"></td>
               <td class="fieldSetLabel"><input type="text" name="patientname" id="patientname" value="<bean:write name="patientdetailsId" property="PATIENT_NAME"/>"  readonly="readonly" /></td>
               <td width="20px"></td>
               <td class="label">N.O.K : </td><td width="10px"></td>
                      <td class="label"><input type="text" name="husname"	id="husname"  value="<bean:write name="patientdetailsId" property="PATIENT_CARE_OFTEXT"/>" readonly="readonly"/></td>
                       <td width="20px"></td>

            </tr>
            <tr height="10"></tr>
            <tr>
              <td class="label" colspan="3">Address : </td><td width="10px"></td>
               <td class="fieldSetLabel"><input type="text" name="address" id="address"  value="<bean:write name="patientdetailsId" property="PATIENT_ADDRESS"/>" readonly="readonly" /></td>
               <td width="20px"></td>
               <td class="label">Contact&nbsp;No : </td><td width="10px"></td>
                      <td class="label"><input type="text" name="telno"	id="telno"  value="<bean:write name="patientdetailsId" property="PATIENT_PHONE"/>" readonly="readonly"/></td>
            </tr>
          </table>
        </fieldset>
      </logic:iterate>
    </td>
  </tr>
   <logic:notEmpty name="GynicDetails">
   <logic:iterate id="GynicDetailsId" name="GynicDetails">
    <tr>
      <!--forexamination-->
   <td valign="top" align="left" width="45%">
         <fieldset class="fieldSetBorder"  style="width: 650; height: 380"><legend class="fieldSetLabel">History</legend>
        <table border="0"  cellpadding="0" cellspacing="0">
            <tr>
              <td>
                <table>
                  <tr>
                    <td class="label">Past&nbsp;History</td>
                     <td class="label">Obstetric&nbsp;History</td>
                     <td class="label">Treatment&nbsp;History</td>
                     <td></td>
                    </tr>
                    <tr>
                       <td class="label"><textarea name="pasthistory" rows="4" cols="25" id="pasthistory" class="text-input"><bean:write name="GynicDetailsId" property="PAST_HISTORY" /></textarea></td>
                 <td class="label" ><textarea name="obspasthistory" rows="4" cols="25" id="obspasthistory" class="text-input"><bean:write name="GynicDetailsId" property="OBS_HISTORY" /></textarea></td>
                 <td class="label"><textarea name="treathistory" rows="4" cols="25" id="treathistory" class="text-input"><bean:write name="GynicDetailsId" property="TREAT_HISTORY" /></textarea></td>
                 <td></td>
                    </tr>
                </table>
              </td>
          </tr>
        <tr>
             <td valign="top" width="55%">
                <table>
                   <tr>
                       <td class="label">Presenting&nbsp;Complaint</td>
                   </tr>
                   <tr>
                       <td class="label">
                         <textarea name="complaints" rows="5" cols="30" id="complaints" class="text-input"><bean:write name="GynicDetailsId" property="COMPLAINTS" /></textarea></td>
                         <td >
                           <table >
                             <tr>
                               <td class="label">Married&nbsp;For&nbsp;:&nbsp;&nbsp;<input type="text" name="marriedfor" id="marriedfor" value="<bean:write name="GynicDetailsId" property="MARRIEDFOR" />" class="text-input"/></td>
                             </tr>
                             <tr>
                               <td width="100%">
                                 <table width="100%">
                                   <tr>
                                     <td class="label" valign="top">G&nbsp;<input type="text" name="gvalue" id="gvalue" value="<bean:write name="GynicDetailsId" property="GVALUE" />"  class="text-input"size="2"/>&nbsp;&nbsp;P&nbsp;<input type="text" name="pvalue" id="pvalue"  value="<bean:write name="GynicDetailsId" property="PVALUE" />"  class="text-input"size="2"/>&nbsp;&nbsp;L&nbsp;<input type="text" name="lvalue" id="lvalue" value="<bean:write name="GynicDetailsId" property="LVALUE" />"  class="text-input"size="2"/>&nbsp;&nbsp;A&nbsp;<input type="text" name="avalue" id="avalue" value="<bean:write name="GynicDetailsId" property="AVALUE" />"  class="text-input"size="2"/>&nbsp;&nbsp;D&nbsp;<input type="text" name="dvalue" id="dvalue" value="<bean:write name="GynicDetailsId" property="DVALUE" />" class="text-input"size="2"/></td>
                                   </tr>
                                 </table>
                               </td>
                             </tr>
                           </table>
                         </td>
                    </tr>
                      <tr><td height="6"></td></tr>
                   <tr>

                     </tr>
                   </table>
                </td>
          </tr>
          <tr>
              <td>
                <table>
                  <tr>
                    <td>
                        <fieldset class="fieldSetBorder"  style="width: 200; height: 80">
                        <legend class="fieldSetLabel">Family&nbsp;History</legend>
                    <table>
                    <tr>
                      <td class="label"><input type="checkbox" name="familyhis" value="dm"></td>
                       <td class="label">DM</td>

                       <td class="label"><input type="checkbox" name="familyhis" value="htn"></td>
                       <td class="label">HTN</td>

                       <td class="label"><input type="checkbox" name="familyhis" value="tb"></td>
                       <td class="label">TB</td>
                       </tr>
                       <tr>
                      <td class="label"><input type="checkbox" name="familyhis" value="ca"></td>
                       <td class="label">CA</td>

                       <td class="label"><input type="checkbox" name="familyhis" value="hasthma"></td>
                       <td class="label">Asthma</td>


                         <td class="label"><input type="checkbox" name="familyhis" value="conscnguinity"></td>
                         <td class="label">Consanguinity</td>
                       </tr>
                     </table>
                  </fieldset>
                </td>
                <td>
                  <table>
                    <tr>
                      <td class="label">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;MC : &nbsp;&nbsp;<input type="text" name="mc" id="mc" value="<bean:write name="GynicDetailsId" property="MC" />" class="text-input"></td>
                    </tr>
                     <tr>
                          <td class="label">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;LMP :&nbsp;&nbsp;&nbsp;<input type="text" name="lmpdate" id="lmpdate" class="text-input"value="<bean:write name="GynicDetailsId" property="LMP" />" readonly="readonly" onclick="openCalendar('lmpdate','All');"></td>
                        </tr>
                  </table>
                </td>
              </tr>
            </table>
          </td>
        </tr>
         </table>
     </fieldset></td>
  </tr>
   <!--end of examination-->
   <tr>
     <td valign="top">
     <fieldset class="fieldSetBorder"  style="width: 735; height: 180"><legend
    class="fieldSetLabel">Examination</legend> <!--2-->
       <table border="0"  cellpadding="0" cellspacing="0">

       <tr>
         <td class="label">WT : </td><td width="10px"></td>
              <td class="label"><input type="text" name="wt" id="wt" value="<bean:write name="GynicDetailsId" property="WEIGHT" />" class="text-input"></td>
              <td width="20px"></td>
              <td class="label">BP : </td><td width="10px"></td>
              <td class="label"><input type="text" name="bp" id="bp" value="<bean:write name="GynicDetailsId" property="BP" />" class="text-input"></td>
       </tr>
       <tr height="10"></tr>
       <tr><td class="fieldSetLabel">G/E</td></tr>
            <tr height="10"></tr>
             <tr>
           <td class="label">Pallor : </td><td width="10px"></td>
               <td class="label"><input type="text" name="pallor" id="pallor" value="<bean:write name="GynicDetailsId" property="PALLOR" />" class="text-input"></td>
               <td width="20px"></td>
               <td class="label">Breast : </td><td width="10px"></td>
               <td class="label"><input type="text" name="breast" id="breast" value="<bean:write name="GynicDetailsId" property="BREAST" />" class="text-input"></td>

                <td class="label">CVS : </td><td width="10px"></td>
               <td class="label"><input type="text" name="cvs" id="cvs" value="<bean:write name="GynicDetailsId" property="CVS" />" class="text-input"></td>
         </tr>
         <tr height="10"></tr>
         <tr>
           <td class="label">PA : </td><td width="10px"></td>
               <td class="label"><input type="text" name="pa" id="pa" value="<bean:write name="GynicDetailsId" property="PA" />" class="text-input"></td>
               <td width="20px"></td>
               <td class="label">PS : </td><td width="10px"></td>
               <td class="label"><input type="text" name="ps" id="ps" value="<bean:write name="GynicDetailsId" property="PS" />" class="text-input"></td>

                <td class="label">PV : </td><td width="10px"></td>
               <td class="label"><input type="text" name="pv" id="pv" value="<bean:write name="GynicDetailsId" property="PV" />" class="text-input"></td>
         </tr>
         <tr height="10"></tr>
         <tr>
               <td class="label">RS : </td><td width="10px"></td>
               <td class="label"><input type="text" name="rs" id="rs" value="<bean:write name="GynicDetailsId" property="RS" />"  class="text-input"></td>
         </tr>

     </table>
   </fieldset></td>
       <!--History-->
    </tr>
  </logic:iterate>
  </logic:notEmpty>
  <logic:empty name="GynicDetails">
    <tr>
      <!--forexamination-->
   <td valign="top" align="left" width="45%">
         <fieldset class="fieldSetBorder"  style="width: 650; height: 380"><legend class="fieldSetLabel">History</legend>
        <table border="0"  cellpadding="0" cellspacing="0">
            <tr>
              <td>
                <table>
                  <tr>
                    <td class="label">Past&nbsp;History</td>
                     <td class="label">Obstetric&nbsp;History</td>
                     <td class="label">Treatment&nbsp;History</td>
                     <td></td>
                    </tr>
                    <tr>
                       <td class="label"><textarea name="pasthistory" rows="4" cols="25" id="pasthistory" class="text-input"></textarea></td>
                 <td class="label" ><textarea name="obspasthistory" rows="4" cols="25" id="obspasthistory" class="text-input"></textarea></td>
                 <td class="label"><textarea name="treathistory" rows="4" cols="25" id="treathistory" class="text-input"></textarea></td>
                 <td></td>
                    </tr>
                </table>
              </td>
          </tr>
        <tr>
             <td valign="top" width="55%">
                <table>
                   <tr>
                       <td class="label">Presenting&nbsp;Complaint</td>
                   </tr>
                   <tr>
                       <td class="label">
                         <textarea name="complaints" rows="5" cols="30" id="complaints" class="text-input"></textarea></td>
                         <td >
                           <table >
                             <tr>
                               <td class="label">Married&nbsp;For&nbsp;:&nbsp;&nbsp;<input type="text" name="marriedfor" id="marriedfor" class="text-input"/></td>
                             </tr>
                             <tr>
                               <td width="100%">
                                 <table width="100%">
                                   <tr>
                                     <td class="label" valign="top">G&nbsp;<input type="text" name="gvalue" id="gvalue" class="text-input"size="2"/>&nbsp;&nbsp;P&nbsp;<input type="text" name="pvalue" id="pvalue" class="text-input"size="2"/>&nbsp;&nbsp;L&nbsp;<input type="text" name="lvalue" id="lvalue" class="text-input"size="2"/>&nbsp;&nbsp;A&nbsp;<input type="text" name="avalue" id="avalue" class="text-input"size="2"/>&nbsp;&nbsp;D&nbsp;<input type="text" name="dvalue" id="dvalue" class="text-input"size="2"/></td>
                                   </tr>
                                 </table>
                               </td>
                             </tr>
                           </table>
                         </td>
                    </tr>
                      <tr><td height="6"></td></tr>
                   <tr>

                     </tr>
                   </table>
                </td>
          </tr>
          <tr>
              <td>
                <table>
                  <tr>
                    <td>
                        <fieldset class="fieldSetBorder"  style="width: 200; height: 80">
                        <legend class="fieldSetLabel">Family&nbsp;History</legend>
                    <table>
                    <tr>
                      <td class="label"><input type="checkbox" name="familyhis" value="dm"></td>
                       <td class="label">DM</td>

                       <td class="label"><input type="checkbox" name="familyhis" value="htn"></td>
                       <td class="label">HTN</td>

                       <td class="label"><input type="checkbox" name="familyhis" value="tb"></td>
                       <td class="label">TB</td>
                       </tr>
                       <tr>
                      <td class="label"><input type="checkbox" name="familyhis" value="ca"></td>
                       <td class="label">CA</td>

                       <td class="label"><input type="checkbox" name="familyhis" value="hasthma"></td>
                       <td class="label">Asthma</td>


                         <td class="label"><input type="checkbox" name="familyhis" value="conscnguinity"></td>
                         <td class="label">Consanguinity</td>
                       </tr>
                     </table>
                  </fieldset>
                </td>
                <td>
                  <table>
                    <tr>
                      <td class="label">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;MC : &nbsp;&nbsp;<input type="text" name="mc" id="mc" class="text-input"></td>
                    </tr>
                     <tr>
                          <td class="label">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;LMP :&nbsp;&nbsp;&nbsp;<input type="text" name="lmpdate" id="lmpdate" class="text-input"readonly="readonly" onclick="openCalendar('lmpdate','All');"></td>
                        </tr>
                  </table>
                </td>
              </tr>
            </table>
          </td>
        </tr>
         </table>
     </fieldset></td>
  </tr>
   <!--end of examination-->
   <tr>
     <td valign="top">
     <fieldset class="fieldSetBorder"  style="width: 735; height: 180"><legend
    class="fieldSetLabel">Examination</legend> <!--2-->
       <table border="0"  cellpadding="0" cellspacing="0">

       <tr>
         <td class="label">WT : </td><td width="10px"></td>
              <td class="label"><input type="text" name="wt" id="wt" class="text-input"></td>
              <td width="20px"></td>
              <td class="label">BP : </td><td width="10px"></td>
              <td class="label"><input type="text" name="bp" id="bp" class="text-input"></td>
       </tr>
       <tr height="10"></tr>
       <tr><td class="fieldSetLabel">G/E</td></tr>
            <tr height="10"></tr>
             <tr>
           <td class="label">Pallor : </td><td width="10px"></td>
               <td class="label"><input type="text" name="pallor" id="pallor" class="text-input"></td>
               <td width="20px"></td>
               <td class="label">Breast : </td><td width="10px"></td>
               <td class="label"><input type="text" name="breast" id="breast" class="text-input"></td>

                <td class="label">CVS : </td><td width="10px"></td>
               <td class="label"><input type="text" name="cvs" id="cvs" class="text-input"></td>
         </tr>
         <tr height="10"></tr>
         <tr>
           <td class="label">PA : </td><td width="10px"></td>
               <td class="label"><input type="text" name="pa" id="pa" class="text-input"></td>
               <td width="20px"></td>
               <td class="label">PS : </td><td width="10px"></td>
               <td class="label"><input type="text" name="ps" id="ps" class="text-input"></td>

                <td class="label">PV : </td><td width="10px"></td>
               <td class="label"><input type="text" name="pv" id="pv" class="text-input"></td>
         </tr>
         <tr height="10"></tr>
         <tr>
               <td class="label">RS : </td><td width="10px"></td>
               <td class="label"><input type="text" name="rs" id="rs" class="text-input"></td>
         </tr>

     </table>
   </fieldset></td>
       <!--History-->
    </tr>
  </logic:empty>
     <tr>
        <td align="center">
          <table>
            <tr>
              <td class="label"><input type="button" name="Save" value="Save" class="text-input" onclick="return validateonsave();"></td>
              <td class="label"><input type="button" name="Clear" value="Clear" class="text-input" onclick="return validateonClear();"></td>
              <td class="label"><input type="button" name="Close" value="Close" class="text-input" onclick="return funcClose();"></td>
            </tr>
          </table>
        </td>
      </tr>
   </table>
        </form>
   </body>
</html>