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
	<title><fmt:message key="title.AccountDetails"/></title>
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
			<h1><fmt:message key="title.AccountDetails"/></h1>
			<div id="entityDetails">
				<div id="accountDetails">
					<ul>
						<li><fmt:message key="label.Account"/>: ${account.number}</li>
						<li><fmt:message key="label.Name"/>: ${account.name}</li>
						<li><fmt:message key="label.Date"/>: <fmt:formatDate value="${account.dateOfBirth.date}"/></li>
						<li><fmt:message key="label.Email"/>: ${account.email}</li>
						<li><fmt:message key="label.ReceiveNewsletter"/>: 
							<fmt:message key="label.${account.receiveNewsletter}"/></li>
						<li><fmt:message key="label.ReceiveMonthlyEmailUpdate"/>: 
							<fmt:message key="label.${account.receiveMonthlyEmailUpdate}"/></li>
					</ul>
				</div>
				<div id="beneficiaries">
					<h5><fmt:message key="label.Beneficiaries"/></h5>
					<c:if test="${! empty account.beneficiaries}">
						<ul>
							<li>
								<table>
									<thead>
										<tr>
											<th><fmt:message key="label.Name"/></th>
											<th><fmt:message key="label.AllocationPercentage"/></th>
											<th><fmt:message key="label.Savings"/></th>
										</tr>
									</thead>
									<tbody>
										<c:forEach items="${account.beneficiaries}" var="beneficiary">
											<tr>
												<td>${beneficiary.name}</td>
												<td>${beneficiary.allocationPercentage}</td>
												<td>${beneficiary.savings}</td>
											</tr>
										</c:forEach>
									</tbody>
								</table>
							</li>
						</ul>
					</c:if>
					<c:if test="${empty account.beneficiaries}">
						<fmt:message key="label.noBeneficiaries"/>
					</c:if>
				</div>
			</div>
		</div> <!-- End "main" DIV -->
	</div>
</div>
</body>
</html>