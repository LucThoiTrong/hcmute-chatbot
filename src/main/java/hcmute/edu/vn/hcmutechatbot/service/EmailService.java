package hcmute.edu.vn.hcmutechatbot.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    // Lấy email từ application.properties để đảm bảo chính xác 100%
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendResetPasswordEmail(String toEmail, String link) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // --- ĐOẠN NÀY QUAN TRỌNG NHẤT ---
            // Tham số 1: Email người gửi (lấy từ config, không lo khoảng trắng)
            // Tham số 2: Tên hiển thị (Bạn đặt tên gì cũng được)
            helper.setFrom(fromEmail, "Uni Mentor Support");
            // --------------------------------

            helper.setTo(toEmail);
            helper.setSubject("Yêu cầu đặt lại mật khẩu");

            String htmlContent = String.format("""
                <div style="font-family: Arial, sans-serif; padding: 20px; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #007bff;">Xin chào!</h2>
                    <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>
                    <p>Vui lòng nhấn vào nút bên dưới để tạo mật khẩu mới (Link hết hạn sau 10 phút):</p>
                    <a href="%s" style="background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block;">Đặt lại mật khẩu</a>
                </div>
                """, link);

            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            System.out.println("Email sent successfully to: " + toEmail);

        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("Lỗi gửi mail: " + e.getMessage());
        }
    }
}