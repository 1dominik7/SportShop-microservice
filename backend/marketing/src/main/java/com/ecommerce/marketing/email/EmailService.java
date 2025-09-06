package com.ecommerce.marketing.email;

import com.ecommerce.marketing.config.dto.OrderLineResponse;
import com.ecommerce.marketing.config.dto.ShippingMethodResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${mail.sender}")
    private String senderEmail;

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendEmail(
            String email,
            EmailTemplateName emailTemplate,
            String discountCode,
            String subject
    ) throws MessagingException {
        String templateName;

        if (emailTemplate == null) {
            templateName = "newsletter";
        } else {
            templateName = emailTemplate.getName();
        }

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MULTIPART_MODE_MIXED,
                UTF_8.name()
        );
        String userName = email.substring(0, email.indexOf("@"));
        Map<String, Object> properties = new HashMap<>();
        properties.put("userName", userName);
        properties.put("discount_code", discountCode);

        Context context = new Context();
        context.setVariables(properties);

        helper.setFrom(senderEmail);
        helper.setTo(email);
        helper.setSubject(subject);

        String template = templateEngine.process(templateName, context);

        helper.setText(template, true);
        mailSender.send(mimeMessage);
    }

    @Async
    public void sendOrderConfirmationEmail(
            EmailTemplateName emailTemplate,
            String email,
            List<OrderLineResponse> orderLines,
            Integer orderId,
            LocalDateTime orderDate,
            Double totalPrice,
            ShippingMethodResponse shippingMethodResponse,
            String subject
    ) throws MessagingException {
        String templateName;
        if (emailTemplate == null) {
            templateName = "order-email";
        } else {
            templateName = emailTemplate.getName();
        }

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MULTIPART_MODE_MIXED, UTF_8.name());

        String userName = email.substring(0, email.indexOf("@"));

        Map<String, Object> properties = new HashMap<>();
        properties.put("email", email);
        properties.put("orderId", orderId);
        properties.put("userName", userName);
        properties.put("orderDate", orderDate);
        properties.put("orderItems", orderLines);
        properties.put("totalPrice", totalPrice);
        properties.put("shippingMethod", shippingMethodResponse.getName());
        properties.put("shippingCost", shippingMethodResponse.getPrice());


        Context context = new Context();
        context.setVariables(properties);

        helper.setFrom(senderEmail);
        helper.setTo(email);
        helper.setSubject(subject);

        String template = templateEngine.process("order-email", context);
        helper.setText(template, true);

        mailSender.send(mimeMessage);
    }
}
