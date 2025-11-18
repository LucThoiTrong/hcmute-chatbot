package hcmute.edu.vn.hcmutechatbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subject {
    private String subjectName; // tenMonHoc
    private String description; // moTaMonHoc
    private int credits; // soTinChi
    private SubjectType subjectType; // loaiMonHoc
}
