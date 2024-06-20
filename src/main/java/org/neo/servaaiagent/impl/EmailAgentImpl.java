package org.neo.servaaiagent.impl;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.EmailAgentIFC;

public class EmailAgentImpl implements EmailAgentIFC, DBSaveTaskIFC {
    private EmailAgentImpl() {
    }

    public static EmailAgentImpl getInstance() {
        return new EmailAgentImpl();
    }

    @Override
    public Object save(DBConnectionIFC dbConnection) {
        return null;
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new EmailAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                sendEmail(dbConnection, to, subject, body);
                return null;
            }
        });
    }

    @Override
    public void sendEmail(DBConnectionIFC dbConnection, String to, String subject, String body) {
        final String username = CommonUtil.getConfigValue(dbConnection, "email.username");
        final String password = CommonUtil.getConfigValue(dbConnection, "email.password");

        Properties props = new Properties();
        props.put("mail.smtp.auth", CommonUtil.getConfigValue(dbConnection, "mail.smtp.auth"));
        props.put("mail.smtp.starttls.enable", CommonUtil.getConfigValue(dbConnection, "mail.smtp.starttls.enable"));
        props.put("mail.smtp.host", CommonUtil.getConfigValue(dbConnection, "mail.smtp.host"));
        props.put("mail.smtp.port", CommonUtil.getConfigValue(dbConnection, "mail.smtp.port"));

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            // message.setText(body);
            message.setContent(body, "text/html; charset=utf-8");

            Transport.send(message);
        } 
        catch (MessagingException e) {
            throw new NeoAIException(e);
        }
    }
}
