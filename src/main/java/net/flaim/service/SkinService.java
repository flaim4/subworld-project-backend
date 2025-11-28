package net.flaim.service;

import com.fasterxml.jackson.databind.ser.Serializers;
import lombok.RequiredArgsConstructor;
import net.flaim.dto.BaseResponse;
import net.flaim.model.SkinType;
import net.flaim.repository.SessionRepository;
import net.flaim.repository.SkinRepository;
import org.springframework.beans.factory.annotation.Value;
import net.flaim.model.Skin;
import net.flaim.model.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SkinService {
    @Value("${app.skins.directory:skins}")
    private String skinsDirectory;

    private final SkinRepository skinRepository;

    public BaseResponse<Boolean> upload(User user, MultipartFile file) {
        try {
            Optional<Skin> existingSkin = skinRepository.findByUser(user);

            Skin skin = existingSkin.orElse(new Skin());
            skin.setUser(user);

            Path userDir = Paths.get(skinsDirectory);
            if (!Files.exists(userDir)) {
                Files.createDirectories(userDir);
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);

            String filename = "skin_" + user.getId() + "." + fileExtension;

            Path filePath = userDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            skin.setSkinUrl("/" + skinsDirectory + "/" + filename);

            skinRepository.save(skin);
            return BaseResponse.success(true);
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.error(false);
        }
    }

    public BaseResponse<Boolean> delete(User user) {
        try {
            Optional<Skin> existingSkin = skinRepository.findByUser(user);

            if (existingSkin.isPresent()) {
                Skin skin = existingSkin.get();
                String skinUrl = skin.getSkinUrl();

                if (skinUrl != null) {
                    try {
                        String filename = skinUrl.substring(skinUrl.lastIndexOf("/") + 1);
                        Path filePath = Paths.get(skinsDirectory).resolve(filename);

                        if (Files.exists(filePath)) {
                            Files.delete(filePath);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                skin.setSkinUrl(null);
                skinRepository.save(skin);
                return BaseResponse.success(true);
            } else {
                return BaseResponse.success(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.error(false);
        }
    }

    public BaseResponse<Boolean> createDefault(User user) {
        try {
            Skin skin = new Skin();
            skin.setUser(user);
            skin.setDefaultSkinUrl("/" + skinsDirectory + "/" + "default.png");
            skinRepository.save(skin);
            return BaseResponse.success(true);
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.error(false);
        }
    }

    public BaseResponse<Boolean> changeType(User user, SkinType newSkinType) {
        try {
            Optional<Skin> existingSkin = skinRepository.findByUser(user);

            if (existingSkin.isPresent()) {
                Skin skin = existingSkin.get();
                if (skin.getSkinType() == newSkinType) return BaseResponse.success(true);
                skin.setSkinType(newSkinType);
                skinRepository.save(skin);

                return BaseResponse.success(true);
            } else {
                return BaseResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.error(false);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "png";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
