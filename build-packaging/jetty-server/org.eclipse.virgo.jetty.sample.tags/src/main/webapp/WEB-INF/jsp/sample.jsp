<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %><%--
--%><%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %><%--
--%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

<head>
	<title>Virgo Admin Console</title>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
</head>

<body class="main tundra">
	<center>
		<p>
			Previous comment: <%=request.getAttribute("lastComment") %>
		</p>
		<p>
			<form:form commandName="commentHandler">
				Comment: <form:input path="comment" />
				<input type="submit" value="Send!" />
			</form:form>
		</p>
	</center>
</body>
</html>