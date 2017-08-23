<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>
<html>
<body bgcolor=lightblue>
<form method=post action="jstltest2.jsp">
<jsp:useBean id="bean" class="jstl.tests.Player">
<jsp:setProperty name="bean" property="*"   />
</jsp:useBean>
Name <input   type=text   name="name"><br>
Place<input  type=text    name="place"><br>
Game<input   type=text   name="game"><br>
<input type=submit>
</form>

Name: <c:out value="${bean.name}"  /><br>
Place: <c:out value="${bean.place}"  /><br>
Game: <c:out value="${bean.game}"  />
</body>  
</html>