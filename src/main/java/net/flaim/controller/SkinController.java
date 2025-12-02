package net.flaim.controller;

import lombok.RequiredArgsConstructor;
import net.flaim.dto.BaseResponse;
import net.flaim.model.SkinType;
import net.flaim.service.SessionService;
import net.flaim.service.SkinService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/skin")
@RequiredArgsConstructor
public class SkinController {

    private final SkinService skinService;
    private final SessionService sessionService;

    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<String>> upload(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String token) {
        BaseResponse<String> response = skinService.upload(sessionService.getUser(token), file);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<BaseResponse<String>> delete(@RequestHeader("Authorization") String token) {
        BaseResponse<String> response = skinService.delete(sessionService.getUser(token));
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PatchMapping("/type")
    public ResponseEntity<BaseResponse<String>> changeType(@RequestParam SkinType skinType, @RequestHeader("Authorization") String token) {
        BaseResponse<String> response = skinService.changeType(sessionService.getUser(token), skinType);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }
}