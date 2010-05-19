<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<body  bgcolor=lightblue>
 <form   method=post  action="jstltest1.jsp">
 NAME <input  type=text  name="text1"><br>
 PLACE<input  type=text  name="text2"><br>
 <input type=submit>
 </form>

 NAME:<c:out    value="${param.text1}"  /><br>
 PLACE:<c:out   value="${param.text2}"  />
</body>
</html>