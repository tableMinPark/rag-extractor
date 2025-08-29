package com.extractor.adapter.utils;

import com.extractor.domain.vo.document.OriginalDocumentVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {

    /**
     * 멀티 파트 파일 업로드
     * @param multipartFile 멀티 파트 파일
     */
    public static OriginalDocumentVo uploadFile(String dir, MultipartFile multipartFile) {

        if (multipartFile == null || multipartFile.getOriginalFilename() == null) {
            throw new RuntimeException("multipart filename is null");
        }



        String fileName = UUID.randomUUID().toString().replace("-", "");
        String originalFileName = multipartFile.getOriginalFilename();
        Path path = Paths.get(dir);
        Path fullPath = path.resolve(fileName);

        if (!path.toFile().exists()) {
            path.toFile().mkdirs();
        }

        int dotIndex = originalFileName.lastIndexOf(".");
        if (dotIndex == -1) throw new RuntimeException("multipart file extension is empty");

        String extension = originalFileName.substring(dotIndex + 1).toLowerCase();

        try {
            multipartFile.transferTo(fullPath);
        } catch (IOException e) {
            throw new RuntimeException("upload file error");
        }

        return OriginalDocumentVo.builder()
                .fileName(fileName)
                .originalFileName(originalFileName)
                .path(path)
                .fullPath(fullPath)
                .extension(extension)
                .build();
    }

    /**
     * 파일 삭제
     * @param path 파일 경로
     */
    public static boolean deleteFile(Path path) {
        if (path.toFile().exists()) {
            return path.toFile().delete();
        }
        return false;
    }

    /**
     * 디렉토리 삭제
     * @param path 디렉토리 경로
     */
    public static boolean deleteDirectory(Path path) {
        boolean isSuccess = true;

        if (path.toFile().exists()) {
            File[] files = path.toFile().listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        isSuccess &= deleteDirectory(file.toPath());
                    } else {
                        isSuccess &= file.delete();
                    }
                }
            }

            isSuccess &= path.toFile().delete();
        }

        return isSuccess;
    }

    /**
     * 파일 내용 추출
     * @param path 파일 경로
     */
    public static String readFile(Path path) {

        String content = "";

        try {
            content = new String(Files.readAllBytes(path));
        } catch (IOException ignored) {}

        return content;
    }

    /**
     * 파일 압축 해제
     * @param zipFile 압축 파일
     * @param destDir 압축 해제 경로
     */
    public static Path decompression(File zipFile, File destDir) {
        if (!destDir.exists()) {
            destDir.mkdirs(); // 대상 폴더 없으면 생성
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = newFile(destDir, entry);

                if (entry.isDirectory()) {
                    // 폴더라면 생성만 하고 넘어감
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new RuntimeException("Failed to create directory " + newFile);
                    }
                } else {
                    // 부모 폴더가 없으면 생성
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new RuntimeException("Failed to create directory " + parent);
                    }

                    // 파일 추출
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }

            return destDir.toPath();

        } catch (IOException e) {
            throw new RuntimeException("decompression error");
        }
    }

    /**
     * 보안 문제 방지 파일 생성 (Zip Slip 방어)
     */
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
