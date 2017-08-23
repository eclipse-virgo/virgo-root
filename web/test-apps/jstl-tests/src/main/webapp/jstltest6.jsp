<%@ page contentType="text/html" %>  
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>  
<c:set var="s" value="SAM,DELHI,MCA,24,90"  />
<html>
<body>
   <table border="1">
   <tr>
   <th>Name</th>
   <th>Place</th>
   <th>Degree</th>
   <th>Age</th>
   <th>Mark</th>
   </tr>
   <tr>
<c:forTokens items="${s}" delims="," var="token"  >
 <td><c:out value="${token}"  /></td>
</c:forTokens>
  </tr>
  </table>
  <br>
  </font>
  </body>
  </html>