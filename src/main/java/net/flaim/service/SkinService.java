package net.flaim.service;

import lombok.RequiredArgsConstructor;
import net.flaim.dto.BaseResponse;
import net.flaim.model.SkinType;
import net.flaim.repository.SkinRepository;
import net.flaim.model.Skin;
import net.flaim.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SkinService {
    @Value("${app.skins.directory:skins}")
    private String skinsDirectory;

    private final SkinRepository skinRepository;

    public BaseResponse<String> upload(User user, MultipartFile file) {
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
            return BaseResponse.success("Skin uploaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.error("Failed to upload skin");
        }
    }

    public BaseResponse<String> delete(User user) {
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
                return BaseResponse.success("Skin deleted successfully");
            } else {
                return BaseResponse.success("No skin found to delete");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.error("Failed to delete skin");
        }
    }

    public BaseResponse<String> createDefault(User user) {
        try {
            Skin skin = new Skin();
            skin.setUser(user);
            skin.setDefaultSkinUrl("/" + skinsDirectory + "/" + "default.png");
            skinRepository.save(skin);
            return BaseResponse.success("Default skin created successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.error("Failed to create default skin");
        }
    }

    public BaseResponse<String> changeType(User user, SkinType newSkinType) {
        try {
            Optional<Skin> existingSkin = skinRepository.findByUser(user);

            if (existingSkin.isPresent()) {
                Skin skin = existingSkin.get();
                if (skin.getSkinType() == newSkinType) {
                    return BaseResponse.success("Skin type is already " + newSkinType);
                }
                skin.setSkinType(newSkinType);
                skinRepository.save(skin);

                return BaseResponse.success("Skin type changed to " + newSkinType);
            } else {
                Skin newSkin = new Skin();
                newSkin.setUser(user);
                newSkin.setSkinType(newSkinType);
                skinRepository.save(newSkin);
                return BaseResponse.success("Skin created with type " + newSkinType);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.error("Failed to change skin type");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "png";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}