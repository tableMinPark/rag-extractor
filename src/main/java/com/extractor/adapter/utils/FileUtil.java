package com.extractor.adapter.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {

    /**
     * 폴더 생성
     * @param path 디렉토리 경로
     */
    public static void mkdirs(Path path) {

        boolean isSuccess = path.toFile().mkdirs();

        if (!isSuccess) {
            throw new RuntimeException("make directory error");
        }
    }

    /**
     * 파일 이동
     * @param src 현재 경로
     * @param dst 이동 경로
     */
    public static Path moveFile(Path src, Path dst) {
        boolean isSuccess = src.toFile().renameTo(dst.toFile());

        if (!isSuccess) {
            throw new RuntimeException("move file error");
        }

        return dst;
    }

    /**
     * 파일 복사
     * @param src 현재 경로
     * @param dst 이동 경로
     */
    public static Path copyFile(Path src, Path dst) {
        try {
            return Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(" copy file error");
        }
    }

    /**
     * 파일 삭제
     * @param path 파일 경로
     */
    public static void deleteFile(Path path) {

        boolean isSuccess = path.toFile().exists() && path.toFile().delete();

        if (!isSuccess) {
            throw new RuntimeException("delete file error");
        }
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

        if (!isSuccess) {
            throw new RuntimeException("delete directory error");
        }

        return true;
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
