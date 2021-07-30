<%@ page isErrorPage="true"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<html>
<head>
<script>
<!--
   if (top.frames[2].length  >0)
{

 top.display.location=self.location;
}
// -->
</script>
</head>
<body>
<%
if(exception!=null){
System.out.println("its not null  "+exception.toString());
}else{
System.out.println("its  null");
}
request.setAttribute("exception",exception);
%>
<logic:forward name="errorPage"/>
</body>
</html>




