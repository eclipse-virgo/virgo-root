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
	<title><fmt:message key="title.AccountsListing"/></title>
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
			<h1><fmt:message key="title.AccountsListing"/></h1>

			<table>
				<thead>
					<tr>
						<th><fmt:message key="label.AccountNumber"/></th>
						<th><fmt:message key="label.Name"/></th>
					</tr>
				</thead>
				<c:forEach items="${accounts}" var="account">
					<tr>
						<td>
							<c:url var="showUrl" value="/admin/accounts/show">
								<c:param name="number" value="${account.number}"/>
							</c:url>
							<a href="${showUrl}">${account.number}</a>
						</td>
						<td>${account.name}</td>
					</tr>
				</c:forEach>
			</table>

		</div> <!-- End "main" DIV -->
	</div>
</div>
</body>
</html>			