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
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SkinService {
    @Value("${app.skins.directory}")
    private String skinsDirectory;

    @Value("${app.skins.url-prefix}")
    private String skinsUrlPrefix;

    @Value("${app.skins.default-skin}")
    private String defaultSkin;

    @Value("${app.skins.max-size-mb}")
    private int maxSizeMb;

    @Value("${app.avatars.directory}")
    private String avatarsDirectory;

    @Value("${app.avatars.url-prefix}")
    private String avatarsUrlPrefix;

    @Value("${app.avatars.size}")
    private int avatarSize;

    @Value("${app.avatars.default}")
    private String defaultAvatar;

    private long maxFileSizeBytes;

    private final SkinRepository skinRepository;
    private final AvatarService avatarService;

    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(skinsDirectory);
            if (!Files.exists(path)) Files.createDirectories(path);

            Path avatarsPath = Paths.get(avatarsDirectory);
            if (!Files.exists(avatarsPath)) Files.createDirectories(avatarsPath);

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

            skin.setAvatarUrl(avatarService.generateAvatarFromSkin(user, skinUrl));
            skin.setAvatarGeneratedAt(LocalDateTime.now());
            skinRepository.save(skin);

            return BaseResponse.success("PNG skin uploaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.error("Failed to upload skin");
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

                avatarService.deleteAvatar(user);

                skin.setSkinUrl(null);
                skin.setAvatarUrl(null);
                skin.setAvatarGeneratedAt(null);
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

            skin.setDefaultSkinUrl(skinsUrlPrefix + "/" + defaultSkin);
            skin.setDefaultAvatarUrl(avatarsUrlPrefix + "/" + defaultAvatar);

            skinRepository.save(skin);
            return BaseResponse.success("Default skin and avatar assigned successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.error("Failed to create default skin");
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
            return BaseResponse.error("Failed to change skin type");
        }
    }

    private boolean isPngFile(String fileName) {
        return fileName != null && fileName.toLowerCase().endsWith(".png");
    }
}