<%@ page contentType="text/html;charset=windows-1252" %>
<%
    Object userid = session.getAttribute("userid");
    if(userid==null){
    out.println("<script>");
    out.println("window.open('../../index.jsp','_top')");
    out.println("</script>");
    
  }
%>

