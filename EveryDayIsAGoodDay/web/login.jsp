<%-- 
    Document   : login
    Created on : 9 mars 2017, 16:39:02
    Author     : julien.baumgart
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Login Form</h1>
        <form method="POST" action="j_security_check">
            Username: <input type ="text" name="j_username" />
            Password <input type ="password" name="j_password" />
            <input type="submit" value="login" />
            <input type="reset" value="reset" />
        </form>
    </body>
</html>
