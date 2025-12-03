package net.flaim.controller;

import lombok.RequiredArgsConstructor;
import net.flaim.annotation.RequiresAuth;
import net.flaim.annotation.CurrentSession;
import net.flaim.dto.BaseResponse;
import net.flaim.model.Session;
import net.flaim.model.SkinType;
import net.flaim.service.SkinService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/skin")
@RequiredArgsConstructor
public class SkinController {

    private final SkinService skinService;

    @PostMapping("/upload")
    @RequiresAuth
    public ResponseEntity<BaseResponse<String>> upload(@RequestParam("file") MultipartFile file, @CurrentSession Session session) {
        BaseResponse<String> response = skinService.upload(session.getUser(), file);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @DeleteMapping("/delete")
    @RequiresAuth
    public ResponseEntity<BaseResponse<String>> delete(@CurrentSession Session session) {
        BaseResponse<String> response = skinService.delete(session.getUser());
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PatchMapping("/type")
    @RequiresAuth
    public ResponseEntity<BaseResponse<String>> changeType(@RequestParam SkinType skinType, @CurrentSession Session session) {
        BaseResponse<String> response = skinService.changeType(session.getUser(), skinType);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }
}