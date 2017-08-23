<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %><%-- 
--%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%-- 
--%><%!
    private String pageTitle = "Stand-alone Web Module";
%><%-- 
--%><jsp:useBean id='now' scope='page' class='java.util.Date' /><%-- 
--%><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title><%= pageTitle %></title>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
</head>

<body>
	<div>
		<h1><%= pageTitle %></h1>
		<table>
			<tr><td align="right">String from Spring-DM:</td><td>${stringFromSpringDM}</td></tr>
		</table>
	</div>
</body>
</html>