<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page isELIgnored="false"%>
<%
response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-store");
response.setHeader("Expires", "0");
%>

<html>
  <head>
    <title>Bed Utilization Report - Insta HMS</title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

    <insta:link type="script" file="hmsvalidation.js"/>
    <insta:link type="script" file="date_go.js"/>

    <script language="javascript" type="text/javascript">

      function validate(){
        var temp="";
        var wardSelected = false;
        var len = document.forms[0].allWardNames.length;
        var options = document.forms[0].allWardNames;

        for (var i=0;i<len;i++){
          if (options[i].selected == true){
            wardSelected=true;
            if(temp==""){
              temp = options[i].value;
              wardId = "'"+temp+"'";
            }else{
              temp = options[i].value;
              wardId = ','+"'"+temp+"'";
            }
          }
        }
        if(!wardSelected){
          alert("Select atleast one ward name for report");
          return false;
        }
      }

	  var wardNameListJSON = ${wardNameListJSON};
      function filterWards(){
			var centerId = document.getElementById("centerId").value;
			var filteredWardsList = filterList(wardNameListJSON, "center_id", centerId);
			if ( centerId == "" )
				filteredWardsList = wardNameListJSON;
			loadSelectBox(document.getElementById("allWardNames"), filteredWardsList, 'ward_name', 'ward_no', "--All--", "");
      }

    </script>

  </head>
  <body>
    <form action="bedutilizationreport.do" method="GET" target="_blank">
      <input type="hidden" name="method" value="getBedUtilizationReport">
      <input type="hidden" name="wardId">
    <div class="pageHeader">Bed Utilization Report</div>

    <table align="center" class="formtable">



      <tr valign="top" height="100">
      <c:choose>
        <c:when test="${ multiCenters && centerId == 0 }">
          <td class="formlabel">Center:</td>
          <td>
            <select name="centerId" id="centerId" class="dropdown" onchange="filterWards();">
              <option value="">--All--</option>
              <c:forEach items="${centers}" var="center">
                <option value="${center.map.center_id }"
                <c:if test="${center.map.center_id eq param.center_id }"></c:if>>
                ${center.map.center_name }
                </option>
              </c:forEach>
            </select>
          </td>
          </c:when>
          <c:when test="${ multiCenters}">
          	<input type="hidden" name="centerId" value="${centerId }"/>
          </c:when>
        </c:choose>
        <td class="formlabel">Wards:</td>
        <td >
          <select name="allWardNames" id="allWardNames" multiple="multiple" size="5" style="width: 15em;">
          	<option value="" selected>--All--</option>
            <c:forEach var="wardNames" items="${wardNameList}">
              <option value="${wardNames.map.ward_no}">${wardNames.map.ward_name}</option>
            </c:forEach>
          </select>
        </td>
        <td class="formlabel">Departments:</td>
        <td  style="width: 15em;">
          <insta:selectdb name="dept_name" table="department" valuecol="dept_id"
                displaycol="dept_name" size="5" style="width: 15em" multiple="true"
                  values="${paramValues.dept_name}" class="noClass" dummyvalue="--All--" dummyvalueId="" />
        </td>
      </tr>
      <tr>
        <td>
        &nbsp;&nbsp;
        </td>
      </tr>
      <tr>
        <td>
          <button type="submit" accesskey="G" onclick="return validate()"><b><u>G</u></b>enerate Report</button>
        </td>

      </tr>
    </table>
    </form>
  </body>

</html>
