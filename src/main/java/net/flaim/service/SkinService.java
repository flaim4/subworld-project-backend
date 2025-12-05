package net.flaim.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.flaim.dto.BaseResponse;
import net.flaim.model.SkinType;
import net.flaim.repository.SkinRepository;
import net.flaim.model.Skin;
import net.flaim.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Value("${app.skins.directory:./uploads/skins}")
    private String skinsDirectory;

    @Value("${app.skins.url-prefix:/skins}")
    private String skinsUrlPrefix;

    @Value("${app.skins.default-skin:default.png}")
    private String defaultSkin;

    @Value("${app.skins.max-size-mb:10}")
    private int maxSizeMb;

    private long maxFileSizeBytes;

    private final SkinRepository skinRepository;

    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(skinsDirectory);
            if (!Files.exists(path)) Files.createDirectories(path);

            maxFileSizeBytes = maxSizeMb * 1024L * 1024L;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public BaseResponse<String> upload(User user, MultipartFile file) {
        try {
            if (file.isEmpty()) return BaseResponse.error("File is empty");

            if (file.getSize() > maxFileSizeBytes) return BaseResponse.error("File size exceeds " + maxSizeMb + " MB limit");

            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("image/png")) return BaseResponse.error("Only PNG files are allowed");

            if (!isPngFile(file.getOriginalFilename())) return BaseResponse.error("File must have .png extension");

            Skin skin = skinRepository.findByUser(user).orElse(new Skin());
            skin.setUser(user);

            Path skinsDir = Paths.get(skinsDirectory).toAbsolutePath();
            if (!Files.exists(skinsDir)) Files.createDirectories(skinsDir);

            String filename = "skin_" + user.getId() + ".png";

            Files.copy(file.getInputStream(), skinsDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

            String skinUrl = skinsUrlPrefix + "/" + filename;
            skin.setSkinUrl(skinUrl);
            skinRepository.save(skin);

            return BaseResponse.success("PNG skin uploaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.error("Failed to upload skin: " + e.getMessage());
        }
    }

    @Transactional
    public BaseResponse<String> delete(User user) {
        try {
            Optional<Skin> existingSkin = skinRepository.findByUser(user);

            if (existingSkin.isPresent()) {
                Skin skin = existingSkin.get();
                String skinUrl = skin.getSkinUrl();

                if (skinUrl != null && skinUrl.startsWith(skinsUrlPrefix)) {
                    Path filePath = Paths.get(skinsDirectory).resolve(skinUrl.substring(skinUrl.lastIndexOf("/") + 1)).toAbsolutePath();

                    if (Files.exists(filePath)) Files.delete(filePath);
                }

                skin.setSkinUrl(null);
                skinRepository.save(skin);
                return BaseResponse.success("Skin deleted successfully");
            } else {
                return BaseResponse.success("No skin found to delete");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.error("Failed to delete skin: " + e.getMessage());
        }
    }

    @Transactional
    public BaseResponse<String> createDefault(User user) {
        try {
            Skin skin = skinRepository.findByUser(user).orElse(new Skin());
            skin.setUser(user);

            String defaultSkinUrl = skinsUrlPrefix + "/" + defaultSkin;
            skin.setDefaultSkinUrl(defaultSkinUrl);

            if (skin.getSkinUrl() == null) skin.setSkinUrl(defaultSkinUrl);

            skinRepository.save(skin);
            return BaseResponse.success("Default skin assigned successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.error("Failed to create default skin: " + e.getMessage());
        }
    }

    @Transactional
    public BaseResponse<String> changeType(User user, SkinType newSkinType) {
        try {
            Optional<Skin> existingSkin = skinRepository.findByUser(user);

            if (existingSkin.isPresent()) {
                Skin skin = existingSkin.get();
                if (skin.getSkinType() == newSkinType) return BaseResponse.success("Skin type is already " + newSkinType);
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
            return BaseResponse.error("Failed to change skin type: " + e.getMessage());
        }
    }

    public String getUserSkinUrl(User user) {
        return skinRepository.findByUser(user)
                .map(Skin::getSkinUrl)
                .filter(url -> url != null && !url.trim().isEmpty())
                .orElse(skinsUrlPrefix + "/" + defaultSkin);
    }

    private boolean isPngFile(String fileName) {
        if (fileName == null) return false;
        return fileName.toLowerCase().endsWith(".png");
    }
}