package hcmute.edu.vn.hcmutechatbot.mapper;

import hcmute.edu.vn.hcmutechatbot.dto.response.AdvisoryDomainResponse;
import hcmute.edu.vn.hcmutechatbot.dto.response.FacultyResponse;
import hcmute.edu.vn.hcmutechatbot.model.AdvisoryDomain;
import hcmute.edu.vn.hcmutechatbot.model.Faculty;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FacultyMapper {
    @Mapping(target = "advisoryDomains", source = "advisoryDomains")
    FacultyResponse toFacultyResponse(Faculty faculty);

    AdvisoryDomainResponse toDomainResponse(AdvisoryDomain domain);
}
