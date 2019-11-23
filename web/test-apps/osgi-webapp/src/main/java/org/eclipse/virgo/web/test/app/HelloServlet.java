package org.eclipse.virgo.web.test.app;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HelloServlet extends HttpServlet implements Servlet {

    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        response.getOutputStream().println("Hello");
    }

}
