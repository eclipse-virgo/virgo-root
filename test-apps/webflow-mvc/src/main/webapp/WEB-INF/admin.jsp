<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title><fmt:message key="title.home"/></title>
</head>
<body>
<div id="page">
	<div id="header" class="clearfix">
		<div id="branding">
			<img src="<c:url value="/images/springsource_banner_green.png"/>" />
		</div>
	</div>
	<div id="content" class="clearfix">
		<div id="main">
			<h1><fmt:message key="title.home"/></h1>
			<h4><fmt:message key="message.Welcome"/></h4>
			<ul class="controlLinks">
				<li>
					<c:url var="accountsUrl" value="/admin/accounts/list"/>
					<a href="${accountsUrl}"><fmt:message key="navigate.Accounts"/></a>
				</li>
			</ul>
		</div> <!-- End "main" DIV -->
	</div>
</div>
</body>
</html>			