package com.document.extractor.application.utils;

import com.document.extractor.adapter.propery.FileProperty;
import com.document.extractor.application.vo.FileVo;
import com.document.global.utils.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@RequiredArgsConstructor
public class FileUtil {

    private final FileProperty fileProperty;

    /**
     * 파일 저장
     *
     * @param multipartFile 업로드 파일
     * @throws IOException 업로드 실패 예외
     */
    public FileVo uploadFile(MultipartFile multipartFile) throws IOException {
        LocalDate date = LocalDate.now();
        String filePath = String.format("%02d-%02d-%02d", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        return uploadFile(multipartFile, filePath);
    }

    /**
     * 파일 저장
     *
     * @param multipartFile 업로드 파일
     * @param filePath      파일 경로
     * @throws IOException 업로드 실패 예외
     */
    public FileVo uploadFile(MultipartFile multipartFile, String filePath) throws IOException {

        String originFileName = "";
        String fileName = "";
        String ip = "127.0.0.1";
        String ext = "";

        // 원본 파일명
        originFileName = multipartFile.getOriginalFilename();
        if (originFileName == null) originFileName = "Unknown";

        // 파일명
        fileName = StringUtil.generateRandomId();

        // IP
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            Enumeration<InetAddress> addresses = ni.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();

                if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                    ip = addr.getHostAddress();
                }
            }
        }

        // 파일 확장자
        String[] extSplit = originFileName.trim().split("\\.");
        ext = extSplit[extSplit.length - 1];

        // 파일 저장 경로
        Path fullPath = Paths.get(fileProperty.getFileStorePath(), filePath);
        Path fullFilePath = Paths.get(fileProperty.getFileStorePath(), filePath, fileName);

        // 저장 경로 디렉토리 생성
        if (!fullPath.toFile().exists()) {
            mkdirs(fullPath);
        }

        // 파일 존재 여부 체크
        if (fullFilePath.toFile().exists()) {
            throw new IOException("File already exists!");
        }

        // 파일 저장
        multipartFile.transferTo(fullFilePath);

        // 파일 정보 반환
        return FileVo.builder()
                .originFileName(originFileName)
                .fileName(fileName)
                .ip(ip)
                .filePath(fullPath.toFile().getAbsolutePath())
                .fileSize((int) multipartFile.getSize())
                .ext(ext)
                .url(fullFilePath.toFile().getAbsolutePath())
                .build();
    }

    /**
     * 파일 이동
     *
     * @param src 현재 경로
     * @param dst 이동 경로
     */
    public void moveFile(Path src, Path dst) {
        if (src.toFile().exists()) {
            boolean isSuccess = src.toFile().renameTo(dst.toFile());

            if (!isSuccess) {
                throw new RuntimeException("move file error");
            }
        }
    }

    /**
     * 파일 복사
     *
     * @param src 현재 경로
     * @param dst 이동 경로
     */
    public void copyFile(Path src, Path dst) {
        try {
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(" copy file error");
        }
    }

    /**
     * 파일 삭제
     *
     * @param path 파일 경로
     */
    public void deleteFile(Path path) {
        if (path.toFile().exists()) {
            boolean isSuccess = path.toFile().exists() && path.toFile().delete();

            if (!isSuccess) {
                throw new RuntimeException("delete file error");
            }
        }
    }

    /**
     * 파일 내용 추출
     *
     * @param path 파일 경로
     * @return 추출 문자열
     */
    public String read(Path path) {

        String content = "";

        try {
            content = new String(Files.readAllBytes(path));
        } catch (IOException ignored) {
        }

        return content;
    }

    /**
     * 파일 내용 추출
     *
     * @param path 파일 경로
     * @return 추출 문자열
     */
    public String readFile(Path path) {
        StringBuilder contentBuilder = new StringBuilder();

        try {
            String os = System.getProperty("os.name").toLowerCase();

            String[] cmd;
            if (os.contains("windows")) {
                cmd = new String[]{"cmd.exe", "/c", fileProperty.getSnfPath().getWindows(), "-NO_WITHPAGE", "-C", "utf8", path.toString()};
            } else if (os.contains("mac")) {
                cmd = new String[]{fileProperty.getSnfPath().getMac(), "-NO_WITHPAGE", "-C", "utf8", path.toString()};
            } else {
                cmd = new String[]{fileProperty.getSnfPath().getLinux(), "-NO_WITHPAGE", "-C", "utf8", path.toString()};
            }

            Process process = Runtime.getRuntime().exec(cmd);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("snf extract error");
        }

        return contentBuilder.toString();
    }

    /**
     * 디렉토리 생성
     *
     * @param path 디렉토리 경로
     */
    public void mkdirs(Path path) {

        boolean isSuccess = path.toFile().mkdirs();

        if (!isSuccess) {
            throw new RuntimeException("make directory error");
        }
    }

    /**
     * 디렉토리 삭제
     *
     * @param path 디렉토리 경로
     */
    public boolean deleteDirectory(Path path) {
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
     * 파일 압축 해제
     *
     * @param zipFile 압축 파일
     * @param destDir 압축 해제 경로
     */
    public void decompression(File zipFile, File destDir) {
        if (!destDir.exists()) {
            mkdirs(destDir.toPath());
        } else {
            throw new RuntimeException("already exist destDir");
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
        } catch (IOException e) {
            throw new RuntimeException("decompression error");
        }
    }

    /**
     * 보안 문제 방지 파일 생성 (Zip Slip 방어)
     */
    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
