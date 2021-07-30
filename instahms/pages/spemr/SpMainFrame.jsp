<%@page import="java.util.*"%>
<%@ taglib uri="/WEB-INF/taglibs-datagrid.tld" prefix="ui" %>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%
//String strMrno = (String)session.getAttribute("Mrno");

//String strMrno = request.getAttribute("MrNo")!=null?(String)request.getAttribute("MrNo"):"";

//if(strMrno == "")
//strMrno = request.getParameter("MrNo")!=null?request.getParameter("MrNo"):"";

%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">


</head>
<frameset rows="5%,*" border="1" >
	<FRAME   NAME="navigation" SRC="../../pages/frame/Top.jsp" noresize="noresize" scrolling="NO" >
	<frameset cols="17%,*" border="1" >
		<FRAME NAME="navigation" SRC="../../pages/spemr/LeftNavigation1.jsp" noresize="noresize" scrolling="NO" >
		 <frameset rows="23%,*,4%" border="0" frameborder="yes" bordercolor="black" framespacing="0" >
			<FRAME NAME="personaldetails" SRC="../../pages/spemr/patdetails.jsp" noresize="noresize" scrolling="NO" >
			<FRAME NAME="orthosave" SRC="../../pages/spemr/orthosave.jsp" noresize="noresize" scrolling="AUTO" >
			<FRAME NAME="buttons" SRC="../../pages/spemr/buttons.jsp" noresize="noresize" scrolling="NO" >
		</frameset>
	</frameset>
</frameset>
</html>