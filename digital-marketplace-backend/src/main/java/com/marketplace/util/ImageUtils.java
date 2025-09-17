package com.marketplace.util;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public final class ImageUtils {
    private ImageUtils() {}

    public static String calculateFileHash(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash file", e);
        }
    }

    public static Map<String, Object> getImageDimensions(MultipartFile file) {
        Map<String, Object> map = new HashMap<>();
        try (InputStream in = file.getInputStream()) {
            BufferedImage img = ImageIO.read(in);
            if (img != null) {
                int w = img.getWidth();
                int h = img.getHeight();
                map.put("width", w);
                map.put("height", h);
                map.put("aspectRatio", h != 0 ? (double) w / (double) h : null);
            }
        } catch (IOException ignored) { }
        return map;
    }

    public static Map<String, Object> extractMetadata(MultipartFile file) {
        // TODO: Echte Metadaten-Extraktion (EXIF, etc.)
        return Collections.emptyMap();

    }

    public static List<String> extractColorPalette(MultipartFile file) {
        // TODO: Echte Farbpalette extrahieren
        return Arrays.asList("#000000", "#FFFFFF");
    }

    public static String getFileExtension(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx + 1) : "";
    }

    public static boolean isValidImage(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            BufferedImage img = ImageIO.read(in);
            return img != null;
        } catch (IOException e) {
            return false;
        }
    }
}

