package com.restkeeper.email.listener;
import com.alibaba.fastjson.JSON;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailAddress;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.rabbitmq.client.AMQP;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.email.EmailObject;
import com.restkeeper.utils.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.Collections;

@Component
@Slf4j
public class EmailMessageListener {

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${azure.communication.email.connection-string}")
    private String connectionString;

    @Value("${azure.communication.email.senderEmail}")
    private String senderEmail;


    @RabbitListener(queues = SystemCode.Email_ACCOUNT_QUEUE)
    public void getAccountMessage(String message){
        log.info("Email Sending Listener received Message: " + message);

        //parameter convertion
        EmailObject emailObject = JSON.parseObject(message, EmailObject.class);

        //email send
        try {
            EmailSendResult emailSendResult = this.sendEmail(emailObject);

            log.info("Email Id: " + emailSendResult.getId() + "is " + emailSendResult.getStatus());

            if (emailSendResult.getError() != null) {
                log.info("Email Sending Error: " + emailSendResult.getError());
            }

        } catch (Exception e){
            log.error("Exception occurred while sending email: ", e);

        }


    }

    private String getTemplateNameByMessageType(MessageType messageType) {
        switch (messageType) {
            case ENTERPRISE_ACCOUNT_CREATION:
                return "enterprise_account_creation";  // 对应 account_activation.html
            case ENTERPRISE_ACCOUNT_PASSWORD_RESET:
                return "enterprise_account_password_reset";      // 对应 password_reset.html
            case ENTERPRISE_ACCOUNT_INFORMATION_UPDATE:
                return "enterprise_account_information_update";  // 对应 account_information_update.html
            default:
                throw new IllegalArgumentException("Unknown message type: " + messageType);
        }
    }

    private EmailSendResult sendEmail(EmailObject emailObject){

        EmailClient emailClient = new EmailClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        //data for email context
        Context context = new Context();
        context.setVariable("recipientEmail", emailObject.getRecipientEmail());
        context.setVariable("shopId", emailObject.getShopId());
        context.setVariable("password", emailObject.getPassword());

        //choose email template
        String templateName = getTemplateNameByMessageType(emailObject.getMessageType());
        String bodyHtml = templateEngine.process(templateName, context);

        EmailMessage emailMessage = new EmailMessage()
                .setSenderAddress(senderEmail)
                .setToRecipients(Collections.singletonList(new EmailAddress(emailObject.getRecipientEmail())))
                .setSubject(emailObject.getSubject())
                .setBodyHtml(bodyHtml);

        SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(emailMessage, null);
        PollResponse<EmailSendResult> result = poller.waitForCompletion();

        return result.getValue();
    }

}
