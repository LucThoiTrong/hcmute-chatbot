package hcmute.edu.vn.hcmutechatbot.mapper;

import hcmute.edu.vn.hcmutechatbot.dto.response.NotificationResponse;
import hcmute.edu.vn.hcmutechatbot.model.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    // CASE 1: Mặc định (Không truyền senderName -> field senderName sẽ là null)
    @Mapping(target = "isRead", expression = "java(notification.checkIsRead(notification, userId))")
    @Mapping(target = "senderName", ignore = true) // Explicitly ignore nếu không muốn map
    NotificationResponse toResponse(Notification notification, String userId);

    // CASE 2: Có thêm senderName (Map tham số senderName vào field response)
    @Mapping(target = "isRead", expression = "java(notification.checkIsRead(notification, userId))")
    @Mapping(source = "senderName", target = "senderName")
    NotificationResponse toResponse(Notification notification, String userId, String senderName);
}