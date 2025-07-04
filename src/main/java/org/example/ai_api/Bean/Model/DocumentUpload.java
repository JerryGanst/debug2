package org.example.ai_api.Bean.Model;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
//自定义智能体，文件上传工具类
public class DocumentUpload {
    boolean enabled;
    List<String> files;
}
