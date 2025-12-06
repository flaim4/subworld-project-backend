package net.flaim.service;

import lombok.RequiredArgsConstructor;
import net.flaim.dto.BaseResponse;
import net.flaim.model.Skin;
import net.flaim.model.User;
import net.flaim.repository.SkinRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AvatarService {

    @Value("${app.skins.directory}")
    private String skinsDirectory;

    @Value("${app.skins.url-prefix}")
    private String skinsUrlPrefix;

    @Value("${app.avatars.directory}")
    private String avatarsDirectory;

    @Value("${app.avatars.url-prefix}")
    private String avatarsUrlPrefix;

    @Value("${app.avatars.size}")
    private int avatarSize;

    @Value("${app.avatars.default}")
    private String defaultAvatar;

    private final SkinRepository skinRepository;

    @Transactional
    public String generateAvatarFromSkin(User user, String skinUrl) throws IOException {
        Optional<Skin> skinOpt = skinRepository.findByUser(user);
        Skin skin = skinOpt.get();

        String avatarFilename = "avatar_" + user.getId() + ".png";
        String avatarUrl = avatarsUrlPrefix + "/" + avatarFilename;

        Path avatarsDir = Paths.get(avatarsDirectory);
        if (!Files.exists(avatarsDir)) Files.createDirectories(avatarsDir);

        Path skinPath = getSkinPath(skinUrl);

        if (!Files.exists(skinPath)) {
            skin.setAvatarUrl(null);
            skinRepository.save(skin);
            return getDefaultAvatarUrl(skin);
        }

        try {
            if (!ImageIO.write(extractFaceFromSkin(skinPath), "PNG", Paths.get(avatarsDirectory).resolve(avatarFilename).toAbsolutePath().toFile())) throw new IOException();
        } catch (IOException e) {
            throw new IOException();
        }

        skin.setAvatarUrl(avatarUrl);
        skin.setAvatarGeneratedAt(LocalDateTime.now());
        skinRepository.save(skin);

        return avatarUrl;
    }

    private BufferedImage extractFaceFromSkin(Path skinPath) throws IOException {
        BufferedImage skin;
        try (InputStream is = Files.newInputStream(skinPath)) {
            skin = ImageIO.read(is);
        }

        if (skin == null) throw new IOException();

        int skinWidth = skin.getWidth();
        int skinHeight = skin.getHeight();

        BufferedImage face;

        if (skinWidth == 64 && skinHeight == 64) {
            face = extractFaceFrom64x64Skin(skin);
        } else if (skinWidth == 64 && skinHeight == 32) {
            face = extractFaceFrom64x32Skin(skin);
        } else {
            throw new IOException();
        }

        return resizeImage(face, avatarSize, avatarSize);
    }

    private BufferedImage extractFaceFrom64x64Skin(BufferedImage skin) {
        BufferedImage combined = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = combined.createGraphics();

        try {
            g2d.drawImage(scaleImage(skin.getSubimage(8, 8, 8, 8), 16, 16), 0, 0, null);

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    int rgb = scaleImage(skin.getSubimage(40, 8, 8, 8), 16, 16).getRGB(x, y);
                    if ((rgb >>> 24) > 0) combined.setRGB(x, y, rgb);
                }
            }
        } finally {
            g2d.dispose();
        }
        return combined;
    }

    private BufferedImage extractFaceFrom64x32Skin(BufferedImage skin) {
        return scaleImage(skin.getSubimage(8, 8, 8, 8), 16, 16);
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        if (originalImage == null) return new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);

        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();

        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

            g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        } finally {
            g2d.dispose();
        }

        return resizedImage;
    }

    private BufferedImage scaleImage(BufferedImage original, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaled.createGraphics();

        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2d.drawImage(original, 0, 0, width, height, null);
        } finally {
            g2d.dispose();
        }

        return scaled;
    }

    @Transactional
    public BaseResponse<String> deleteAvatar(User user) {
        try {
            Optional<Skin> skinOpt = skinRepository.findByUser(user);
            if (skinOpt.isPresent()) {
                Skin skin = skinOpt.get();
                String avatarUrl = skin.getAvatarUrl();

                if (avatarUrl != null && avatarUrl.startsWith(avatarsUrlPrefix)) {
                    String filename = avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1);
                    Path avatarsDir = Paths.get(avatarsDirectory).toAbsolutePath().normalize();
                    Path filePath = avatarsDir.resolve(filename).normalize();

                    if (filePath.startsWith(avatarsDir) && Files.exists(filePath)) Files.deleteIfExists(filePath);
                }

                skin.setAvatarUrl(null);
                skin.setAvatarGeneratedAt(null);
                skinRepository.save(skin);
            }
            return BaseResponse.success("Avatar deleted successfully");
        } catch (Exception e) {
            return BaseResponse.error("Failed to delete avatar");
        }
    }

    public String getAvatarUrl(User user) {
        Optional<Skin> skinOpt = skinRepository.findByUser(user);

        if (skinOpt.isPresent()) {
            Skin skin = skinOpt.get();

            if (skin.getAvatarUrl() != null && !skin.getAvatarUrl().isEmpty()) return skin.getAvatarUrl();

            if (skin.getDefaultAvatarUrl() != null && !skin.getDefaultAvatarUrl().isEmpty()) return skin.getDefaultAvatarUrl();
        }

        return avatarsUrlPrefix + "/" + defaultAvatar;
    }

    private String getDefaultAvatarUrl(Skin skin) {
        return skin.getDefaultAvatarUrl() != null ?
                skin.getDefaultAvatarUrl() :
                avatarsUrlPrefix + "/" + defaultAvatar;
    }

    private Path getSkinPath(String skinUrl) {
        if (skinUrl.startsWith(skinsUrlPrefix)) return Paths.get(skinsDirectory).resolve(skinUrl.substring(skinUrl.lastIndexOf("/") + 1)).toAbsolutePath();
        return Paths.get(skinsDirectory).resolve("downloaded_" + System.currentTimeMillis() + ".png").toAbsolutePath();
    }
}