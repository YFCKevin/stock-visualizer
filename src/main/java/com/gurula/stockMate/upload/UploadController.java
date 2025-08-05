package com.gurula.stockMate.upload;

import com.gurula.stockMate.config.ConfigProperties;
import com.gurula.stockMate.member.Member;
import com.gurula.stockMate.member.MemberContext;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class UploadController {
    private final ConfigProperties configProperties;

    public UploadController(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Operation(summary = "筆記區塊上傳圖片")
    @PostMapping("/image/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("uploadFile") MultipartFile file) {
        final Member member = MemberContext.getMember();
        final String memberId = member.getId();

        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("msg", "無檔案");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        try {
            // 確保資料夾存在
            String memberFolderPath = configProperties.getPicSavePath() + memberId + "/";
            File uploadDir = new File(memberFolderPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 產生唯一檔名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String uniqueFilename = UUID.randomUUID() + extension;

            // 儲存檔案
            File destination = new File(memberFolderPath + uniqueFilename);
            file.transferTo(destination);

            result.put("url", configProperties.getGlobalDomain() + "uploads/" + memberId + "/" + uniqueFilename);
            return ResponseEntity.ok(result);

        } catch (IOException e) {
            result.put("msg", "上傳失敗：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}
