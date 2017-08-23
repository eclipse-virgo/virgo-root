/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package example.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Simple MVC Controller which sends e-mails: used to test support for <code>javax.activation</code> and
 * <code>javax.mail</code> within the Server.
 * 
 */
@Controller
public class EmailController {

    private final MailSender mailSender;

    private final SimpleMailMessage templateMessage;

    @Autowired
    public EmailController(MailSender mailSender, SimpleMailMessage templateMessage) {
        this.mailSender = mailSender;
        this.templateMessage = templateMessage;
        System.err.println("### EmailController instantiated...");
    }

    @RequestMapping(value = "/send")
    public void sendEmail(HttpServletRequest request, HttpServletResponse response) throws Exception {
        StringBuilder builder = new StringBuilder("<html>\n<head>\n<title>E-Mail Web Module</title>\n</head>\n<body>\n")//
        .append("<h2>E-Mail Web Module</h2>\n")//
        .append("<p>" + sendMail(ServletRequestUtils.getRequiredStringParameter(request, "subject")) + "</p>\n")//
        .append("</body>\n</html>\n");
        response.getWriter().write(builder.toString());
    }

    private String sendMail(String subject) {
        String results = "Failed to send message: ";
        try {
            SimpleMailMessage msg = new SimpleMailMessage(this.templateMessage);
            msg.setSubject(subject);
            this.mailSender.send(msg);
            results = "Sent message with subject '" + subject + "'!";
        } catch (MailException ex) {
            results += ex.getMessage();
            System.err.println(results);
            ex.printStackTrace();
        }
        return results;
    }

}
