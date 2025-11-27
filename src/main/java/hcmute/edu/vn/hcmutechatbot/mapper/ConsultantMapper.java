package hcmute.edu.vn.hcmutechatbot.mapper;

import hcmute.edu.vn.hcmutechatbot.dto.response.ConsultantResponse;
import hcmute.edu.vn.hcmutechatbot.model.Lecturer;
import hcmute.edu.vn.hcmutechatbot.model.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConsultantMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "fullName", source = "fullName")
    ConsultantResponse toLecturerConsultantResponse(Lecturer lecturer);

    // Ánh xạ từ Student sang ConsultantResponse
    @Mapping(target = "id", source = "studentId")
    @Mapping(target = "fullName", source = "fullName")
    ConsultantResponse toStudentConsultantResponse(Student student);
}