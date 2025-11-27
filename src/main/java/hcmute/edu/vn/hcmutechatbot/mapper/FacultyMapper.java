package hcmute.edu.vn.hcmutechatbot.mapper;

import hcmute.edu.vn.hcmutechatbot.dto.response.AdvisoryDomainResponse;
import hcmute.edu.vn.hcmutechatbot.dto.response.FacultyResponse;
import hcmute.edu.vn.hcmutechatbot.model.AdvisoryDomain;
import hcmute.edu.vn.hcmutechatbot.model.Faculty;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FacultyMapper {
    FacultyResponse toFacultyResponse(Faculty faculty);
    AdvisoryDomainResponse toDomainResponse(AdvisoryDomain domain);
}
