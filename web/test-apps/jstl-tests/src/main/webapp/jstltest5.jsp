<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>
<% pageContext.setAttribute("colors",new String[] {"red","green","blue","orange","black"} );   %>
<table>
<c:forEach var="n"  items="${colors}" varStatus="a">
<tr>
<td> <c:out value="${a.index}" /> </td> 
<td> <c:out value="${a.count}" /> </td>
<td> <c:out value="${a.first}" /> </td>
<td> <c:out value="${a.last}"  /> </td>
<td> <c:out value="${a.current}" /> </td>
<tr>
</c:forEach>
</table>