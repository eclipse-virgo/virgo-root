<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>  
<body bgcolor=lightblue>  
<form method=post action=jstltest3.jsp>  
    <select  name="combo1"> 
      <option value="foo">foo 
      <option value="bar">bar
     </select>
<input type=submit>
</form>
<c:set var="s" value="${param.combo1}" />
<c:out value="${s}"        />
<br> 
<c:if test="${s eq 'foo'}"     >
   <c:out  value="Good Morning...FOO!" />
</c:if>
<c:if test="${s == 'bar'}"          >  
  <c:out value="How Are You?....BAR!" />  
</c:if>
</body>  
</html>  