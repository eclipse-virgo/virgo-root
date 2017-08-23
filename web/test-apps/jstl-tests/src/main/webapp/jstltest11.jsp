<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
Before forwarded to another jsp
<jsp:include page="jstltest2.jsp">
 <jsp:param value="name" name="sam"/>
 <jsp:param value="place" name="delhi"/>
 <jsp:param value="game" name="tennis"/>
</jsp:include><br></br>
After forwarded to another jsp