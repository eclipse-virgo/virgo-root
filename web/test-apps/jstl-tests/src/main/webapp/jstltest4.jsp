<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<body bgcolor=lightblue>
<form  method=post  action="jstltest4.jsp">
  <select name="combo1">
     <option value="1">1   </option>
     <option value="2">2   </option>
     <option value="3">3   </option>
     <option value="4">4   </option>
     <option value="5">5   </option>
     <option value="6">6   </option>
     <option value="7">7   </option>
  </select>
<input  type=submit>
  <c:set var="s"   value="${param.combo1}"  />
    Today is  
  <br>
<font size=24 color=red>
<c:choose>  
 <c:when  test="${s==1}">Sunday   </c:when>  
 <c:when  test="${s==2}">Monday</c:when>  
 <c:when  test="${s==3}">Tuesday</c:when>  
 <c:when  test="${s==4}">Wednesday</c:when>  
 <c:when  test="${s==5}">Thursday</c:when>
<c:otherwise> 
    <h4>select between 1 & 5</h4>
 </c:otherwise>
</c:choose>  
</body>  
</html>  