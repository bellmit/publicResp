<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
	<insta:link type="css" file="homepagePlaceholder.css"/>
	<insta:link type="script" file="jquery-2.2.4.min.js" />
	<insta:link type="script" file="bootstrap.min.js" />
	<insta:link type="script" file="jssor.slider.mini.js" />
	<insta:link type="script" file="homepagePlaceholder.js" />
</head>

<div id="homepage-placeholder">
	<div data-u="slides" id="homepage-placeholder-slides">
	    <div>
	        <img data-u="image" src="${cpath}/images/homepage/placeholder/slide/Menu.png" />	
	    </div>
	    <div>
	        <img data-u="image" src="${cpath}/images/homepage/placeholder/slide/Messages.png" />
	    </div>
	    <div>
	        <img data-u="image" src="${cpath}/images/homepage/placeholder/slide/Help.png" />
	    </div>
	    <div>
	        <img data-u="image" src="${cpath}/images/homepage/placeholder/slide/User.png" />
	    </div>
	</div>

	<!-- bullet navigator container start-->
	<div data-u="navigator" class="slider-bullets">
	    <div data-u="prototype"></div>
	</div>
	<!-- bullet navigator container end-->
</div>
