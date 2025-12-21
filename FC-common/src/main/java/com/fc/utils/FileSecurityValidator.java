package com.fc.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class FileSecurityValidator {

    // 允许的图片MIME类型
    private static final List<String> ALLOWED_IMAGE_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    // 允许的视频MIME类型
    private static final List<String> ALLOWED_VIDEO_MIME_TYPES = Arrays.asList(
            "video/mp4", "video/avi", "video/quicktime", "video/x-msvideo"
    );

    // 图片文件头签名
    private static final byte[] JPEG_SIGNATURE = {(byte)0xFF, (byte)0xD8, (byte)0xFF};
    private static final byte[] PNG_SIGNATURE = {(byte)0x89, 0x50, 0x4E, 0x47};

    private static final Tika tika = new Tika();

    /**
     * 安全验证图片文件
     */
    public static void validateImageFile(MultipartFile file) throws IOException {
        // 1. 基础验证
        validateBasicFileProperties(file, 5 * 1024 * 1024); // 5MB限制

        // 2. 双重MIME类型验证
        String detectedMimeType = tika.detect(file.getBytes());
        String declaredMimeType = file.getContentType();

        log.info("文件MIME类型检测 - 声明类型: {}, 检测类型: {}",
                declaredMimeType, detectedMimeType);

        // 3. 验证MIME类型
        if (!ALLOWED_IMAGE_MIME_TYPES.contains(detectedMimeType)) {
            throw new SecurityException("不支持的文件类型: " + detectedMimeType);
        }

        // 4. 验证文件头签名
        if (!validateFileSignature(file.getBytes(), detectedMimeType)) {
            throw new SecurityException("文件头签名不匹配");
        }

        // 5. 尝试图片解码验证（针对图片文件）
        if (!validateImageContent(file.getBytes())) {
            throw new SecurityException("图片内容验证失败");
        }
    }

    /**
     * 安全验证视频文件
     */
    public static void validateVideoFile(MultipartFile file) throws IOException {
        // 1. 基础验证
        validateBasicFileProperties(file, 100 * 1024 * 1024); // 100MB限制

        // 2. 双重MIME类型验证
        String detectedMimeType = tika.detect(file.getBytes());
        String declaredMimeType = file.getContentType();

        log.info("视频文件MIME类型检测 - 声明类型: {}, 检测类型: {}",
                declaredMimeType, detectedMimeType);

        // 3. 验证MIME类型
        if (!ALLOWED_VIDEO_MIME_TYPES.contains(detectedMimeType)) {
            throw new SecurityException("不支持的视频文件类型: " + detectedMimeType);
        }

        // 4. 验证文件头签名
        if (!validateVideoFileSignature(file.getBytes(), detectedMimeType)) {
            throw new SecurityException("视频文件头签名不匹配");
        }

        // 5. 验证视频文件的基本结构（防止伪装的恶意文件）
        if (!validateVideoFileStructure(file.getBytes(), detectedMimeType)) {
            throw new SecurityException("视频文件结构验证失败");
        }
    }

    /**
     * 基础文件属性验证
     */
    private static void validateBasicFileProperties(MultipartFile file, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new SecurityException("文件不能为空");
        }

        if (file.getSize() > maxSize) {
            throw new SecurityException("文件大小超过限制: " + maxSize + " bytes");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.contains("..")) {
            throw new SecurityException("文件名无效或包含路径遍历字符");
        }
    }

    /**
     * 验证文件头签名
     */
    private static boolean validateFileSignature(byte[] fileData, String mimeType) {
        if (fileData.length < 4) return false;

        switch (mimeType) {
            case "image/jpeg":
                return validateSignature(fileData, JPEG_SIGNATURE);
            case "image/png":
                return validateSignature(fileData, PNG_SIGNATURE);
            default:
                return true; // 对于其他类型，暂时信任Tika检测
        }
    }

    private static boolean validateSignature(byte[] fileData, byte[] signature) {
        for (int i = 0; i < signature.length; i++) {
            if (fileData[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 验证图片内容（尝试解码）
     */
    private static boolean validateImageContent(byte[] imageData) {
        try {
            // 使用ImageIO尝试读取图片，如果失败则说明图片损坏或不是有效图片
            javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(imageData));
            return true;
        } catch (Exception e) {
            log.warn("图片内容验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证视频文件头签名
     */
    private static boolean validateVideoFileSignature(byte[] fileData, String mimeType) {
        if (fileData.length < 8) return false;

        switch (mimeType) {
            case "video/mp4":
                // MP4文件以ftyp开头（通常是第4字节开始）
                return fileData[4] == 'f' && fileData[5] == 't' &&
                        fileData[6] == 'y' && fileData[7] == 'p';
            case "video/avi":
                // AVI文件以RIFF开头
                return fileData[0] == 'R' && fileData[1] == 'I' &&
                        fileData[2] == 'F' && fileData[3] == 'F';
            case "video/quicktime":
                // MOV文件通常以moov或ftyp开头
                return (fileData[4] == 'f' && fileData[5] == 't' &&
                        fileData[6] == 'y' && fileData[7] == 'p') ||
                        (fileData[4] == 'm' && fileData[5] == 'o' &&
                                fileData[6] == 'o' && fileData[7] == 'v');
            default:
                return true; // 对于其他类型，暂时信任Tika检测
        }
    }

    /**
     * 验证视频文件基本结构
     */
    private static boolean validateVideoFileStructure(byte[] fileData, String mimeType) {
        try {
            // 基本结构验证：检查文件是否包含基本的视频容器标记
            if (fileData.length < 100) {
                return false; // 文件太小，不可能是有效的视频文件
            }

            // 对于MP4文件，检查是否包含必要的atom（moov, mdat等）
            if (mimeType.equals("video/mp4")) {
                return validateMp4Structure(fileData);
            }

            // 对于其他格式，进行基本的魔术数字验证
            return validateBasicVideoStructure(fileData);

        } catch (Exception e) {
            log.warn("视频文件结构验证异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证MP4文件结构
     */
    private static boolean validateMp4Structure(byte[] fileData) {
        // 简单的MP4结构验证：检查是否包含必要的atom
        String fileStart = new String(fileData, 0, Math.min(100, fileData.length));
        return fileStart.contains("ftyp") || fileStart.contains("moov") || fileStart.contains("mdat");
    }

    /**
     * 基本视频结构验证
     */
    private static boolean validateBasicVideoStructure(byte[] fileData) {
        // 检查文件是否包含可识别的视频标记
        if (fileData.length < 20) return false;

        // 简单的文件头验证
        return !isAllSameBytes(fileData, 100) && // 不能全部是相同字节
                !isAllZeroBytes(fileData, 100);   // 不能全部是零字节
    }

    /**
     * 检查前n个字节是否全部相同
     */
    private static boolean isAllSameBytes(byte[] data, int length) {
        if (data.length < length) return false;

        byte firstByte = data[0];
        for (int i = 1; i < Math.min(length, data.length); i++) {
            if (data[i] != firstByte) return false;
        }
        return true;
    }

    /**
     * 检查前n个字节是否全部为零
     */
    private static boolean isAllZeroBytes(byte[] data, int length) {
        if (data.length < length) return false;

        for (int i = 0; i < Math.min(length, data.length); i++) {
            if (data[i] != 0) return false;
        }
        return true;
    }
}