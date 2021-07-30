<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
  <head>
    <meta http-equiv="refresh" content="5">
  </head>
  <style>
    @font-face { font-family: CamphorWeb-Bold; src: url("../fonts/Camphor-web-bold.ttf"); }
    @font-face { font-family: CamphorWeb-Regular; src: url("../fonts/Camphor-web-regular.ttf"); }
    body {
      padding:0;
      outline:0;
      border:0;
      margin:0;
      overflow: hidden;
      font-family: CamphorWeb-Regular;
      color: #253667;      
    }
    .loader,
    .loader:after {
      border-radius: 50%;
      width: 10em;
      height: 10em;
    }
    .loader {
      margin: 60px auto;
      font-size: 10px;
      position: relative;
      text-indent: -9999em;
      border-top: 1.1em solid rgba(73, 89, 110, 0.2);
      border-right: 1.1em solid rgba(73, 89, 110, 0.2);
      border-bottom: 1.1em solid rgba(73, 89, 110, 0.2);
      border-left: 1.1em solid #ffffff;
      -webkit-transform: translateZ(0);
      -ms-transform: translateZ(0);
      transform: translateZ(0);
      -webkit-animation: load8 1.1s infinite linear;
      animation: load8 1.1s infinite linear;
    }
    @-webkit-keyframes load8 {
      0% {
        -webkit-transform: rotate(0deg);
        transform: rotate(0deg);
      }
      100% {
        -webkit-transform: rotate(360deg);
        transform: rotate(360deg);
      }
    }
    @keyframes load8 {
      0% {
        -webkit-transform: rotate(0deg);
        transform: rotate(0deg);
      }
      100% {
        -webkit-transform: rotate(360deg);
        transform: rotate(360deg);
      }
    }
    a {
      color: #008EFF;
      text-decoration: none;
    }
    a:hover {
      text-decoration: underline;
    }
  </style>
  <script>
    // Gets triggered if the report get force downloaded (likely in case of CSV)
    window.setTimeout(function(){
      self.close();
    }, 13000);
  </script>
  <body>
    <div style="position: fixed;top: 50%;left:50%;margin-top:-5em;margin-left:-5em;transform: translateY(-50%);">
      <div class="loader">Loading...</div>
      <div style="margin-top:-2em;text-align: center;font-size: 16px;padding-bottom:10px;">Generating report</div>
      <div style="text-align: center;font-size: 12px;color:rgb(102,102,102);">You can also access this report from <br/><a href="${cpath}/reportdashboard.htm">Report Dashboard</a> anytime in next 24hrs</div>
    </div>
  </body>
</html>