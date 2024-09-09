package com.groovith.groovith.provider;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailProvider {
    private final JavaMailSender javaMailSender;
    private final String APP_URL = "http://localhost:5173";

    // 인증 메일 전송
    public boolean sendCertificationMail(String email, String certificationNumber) {

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true); // 여기가 Exception 발생시킴

            String htmlContent = getCertificationMessage(certificationNumber); // 메시지 내용

            messageHelper.setTo(email);
            messageHelper.setSubject("[Groovith] 이메일 인증번호입니다.");
            messageHelper.setText(htmlContent, true);

            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // 인증 메일 본문 생성 (+인증번호 포함)
    private String getCertificationMessage(String certificationNumber) {
        String certificationMessage = "";
        certificationMessage += "<h1 style='text-align: center;'>[Groovith] 인증 메일</h1>";
        certificationMessage += "<h3 style='text-align: center;'>인증코드: <strong style='font-size: 32px; letter-spacing: 8px;'>" + certificationNumber + "</strong></h3>";
        return certificationMessage;
    }

    // 비밀번호 변경 메일 전송
    public boolean sendPasswordResetMail(String email, String code) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
            String htmlContent = getPasswordResetMessage(email, code);
            messageHelper.setTo(email);
            messageHelper.setSubject("[Groovith] 비밀번호 재설정 메일입니다.");
            messageHelper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // 비밀번호 변경 본문 생성
    private String getPasswordResetMessage(String email, String code) {
        String resetUrl = APP_URL + "/reset-password?email=" + email + "&code=" + code;

        StringBuilder message = new StringBuilder();
        message.append("<!DOCTYPE html>");
        message.append("<html lang=\"ko\">");
        message.append("<head>");
        message.append("<meta charset='UTF-8'>");
        message.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        message.append("<style>");
        message.append("body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }");
        message.append(".container { max-width: 600px; margin: 50px auto; background-color: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }");
        message.append(".header { text-align: center; padding-bottom: 20px; border-bottom: 1px solid #ddd; }");
        message.append(".content { margin: 20px 0; }");
        message.append(".content a.button { display: inline-block; padding: 10px 20px; background-color: #007bff; text-decoration: none; border-radius: 5px; color: #fff !important; font-weight: bold; }"); // 구체적인 선택자 사용 및 !important 추가
        message.append(".footer { text-align: center; margin-top: 30px; color: #888; font-size: 12px; }");
        message.append("</style>");
        message.append("</head>");
        message.append("<body>");
        message.append("<div class='container'>");
        message.append("<div class='header'>");
        message.append("<h2>비밀번호 재설정</h2>");
        message.append("</div>");
        message.append("<div class='content'>");
        message.append("<p>안녕하세요,</p>");
        message.append("<p>비밀번호 재설정을 요청하셨습니다. 아래 버튼을 클릭하여 비밀번호를 재설정하세요.</p>");
        message.append("<p><a href='").append(resetUrl).append("' class='button'>비밀번호 재설정</a></p>");
        message.append("<p>이 요청을 하지 않으셨다면, 이 이메일을 무시하셔도 됩니다.</p>");
        message.append("</div>");
        message.append("<div class='footer'>");
        message.append("<p>&copy; 2024 Your Company. All rights reserved.</p>");
        message.append("</div>");
        message.append("</div>");
        message.append("</body>");
        message.append("</html>");

        return message.toString();
    }
}
