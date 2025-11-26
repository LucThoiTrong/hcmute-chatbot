package hcmute.edu.vn.hcmutechatbot.mapper;

import hcmute.edu.vn.hcmutechatbot.dto.response.NotificationResponse;
import hcmute.edu.vn.hcmutechatbot.model.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    @Mapping(target = "isRead", expression = "java(notification.checkIsRead(notification, userId))")
    NotificationResponse toResponse(Notification notification, String userId);
}
