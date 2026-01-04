package com.document.global.utils;

import com.document.global.vo.FileReadBinary;
import com.document.global.vo.UploadFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {

    /**
     * 파일 업로드
     *
     * @param multipartFile 업로드 파일
     * @param fileStorePath 파일 저장소 경로
     */
    public static UploadFile uploadFile(MultipartFile multipartFile, String fileStorePath) {
        LocalDate date = LocalDate.now();
        String filePath = String.format("%02d-%02d-%02d", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        return uploadFile(multipartFile, fileStorePath, filePath);
    }

    /**
     * 파일 업로드
     *
     * @param multipartFile 업로드 파일
     * @param fileStorePath 파일 저장소 경로
     * @param filePath      파일 경로
     */
    public static UploadFile uploadFile(MultipartFile multipartFile, String fileStorePath, String filePath) {
        try {
            // 원본 파일명
            String originFileName = multipartFile.getOriginalFilename();
            if (originFileName == null) {
                throw new RuntimeException("not found origin file name");
            }

            // 파일명 인코딩 (for MAC)
            originFileName = Normalizer.normalize(originFileName, Normalizer.Form.NFC);

            // 파일명
            String fileName = StringUtil.generateRandomId();

            // IP
            String ip = "127.0.0.1";
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
            String ext = extSplit[extSplit.length - 1];

            // 파일 저장 경로
            Path fullPath = Paths.get(fileStorePath, filePath);
            Path fullFilePath = Paths.get(fileStorePath, filePath, fileName);

            // 저장 경로 디렉토리 생성
            if (!fullPath.toFile().exists()) {
                FileUtil.mkdirs(fullPath.toString());
            }

            // 파일 존재 여부 체크
            if (fullFilePath.toFile().exists()) {
                throw new IOException("same name file already exists");
            }

            // 파일 저장
            multipartFile.transferTo(fullFilePath);

            // 파일 정보 반환
            return UploadFile.builder()
                    .originFileName(originFileName)
                    .fileName(fileName)
                    .ip(ip)
                    .filePath(fullPath.toFile().getAbsolutePath())
                    .fileSize((int) multipartFile.getSize())
                    .ext(ext)
                    .url(fullFilePath.toFile().getAbsolutePath())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 파일 이동
     *
     * @param src 현재 경로
     * @param dst 이동 경로
     */
    public static void moveFile(String src, String dst) {

        Path srcPath = Paths.get(src);
        Path dstPath = Paths.get(dst);

        if (srcPath.toFile().exists()) {
            boolean isSuccess = srcPath.toFile().renameTo(dstPath.toFile());

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
    public static void copyFile(String src, String dst) {

        Path srcPath = Paths.get(src);
        Path dstPath = Paths.get(dst);

        try {
            Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 파일 삭제
     *
     * @param target 파일 경로
     */
    public static void deleteFile(String target) {
        Path targetPath = Paths.get(target);

        if (targetPath.toFile().exists()) {
            boolean isSuccess = targetPath.toFile().exists() && targetPath.toFile().delete();

            if (!isSuccess) {
                throw new RuntimeException("delete file error");
            }
        }
    }

    /**
     * 파일 내용 추출
     *
     * @param target 파일 경로
     * @return 추출 문자열
     */
    public static String read(String target) {
        Path targetPath = Paths.get(target);

        try {
            return new String(Files.readAllBytes(targetPath));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 파일 내용 추출
     *
     * @param fileReadBinary 파일 수집 바이너리 파일 경로
     * @param target         파일 경로
     * @return 추출 문자열
     */
    public static String readFile(FileReadBinary fileReadBinary, String target) {
        try {
            Path targetPath = Paths.get(target);

            StringBuilder contentBuilder = new StringBuilder();
            String os = System.getProperty("os.name").toLowerCase();

            String[] cmd;
            if (os.contains("windows")) {
                if (!new File(fileReadBinary.getWindows()).exists())
                    throw new RuntimeException("read binary not exists");
                cmd = new String[]{"cmd.exe", "/c", fileReadBinary.getWindows(), "-NO_WITHPAGE", "-C", "utf8", targetPath.toString()};
            } else if (os.contains("mac")) {
                if (!new File(fileReadBinary.getMac()).exists()) throw new RuntimeException("read binary not exists");
                cmd = new String[]{fileReadBinary.getMac(), "-NO_WITHPAGE", "-C", "utf8", targetPath.toString()};
            } else {
                if (!new File(fileReadBinary.getLinux()).exists()) throw new RuntimeException("read binary not exists");
                cmd = new String[]{fileReadBinary.getLinux(), "-NO_WITHPAGE", "-C", "utf8", targetPath.toString()};
            }

            Process process = Runtime.getRuntime().exec(cmd);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }

            return contentBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 디렉토리 생성
     *
     * @param target 디렉토리 경로
     */
    public static void mkdirs(String target) {

        Path targetPath = Paths.get(target);
        boolean isSuccess = targetPath.toFile().mkdirs();

        if (!isSuccess) {
            throw new RuntimeException("make directory error");
        }
    }

    /**
     * 디렉토리 삭제
     *
     * @param target 디렉토리 경로
     */
    public static boolean deleteDirectory(String target) {
        Path targetPath = Paths.get(target);
        boolean isSuccess = true;

        if (targetPath.toFile().exists()) {
            File[] files = targetPath.toFile().listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        isSuccess &= deleteDirectory(file.toPath().toString());
                    } else {
                        isSuccess &= file.delete();
                    }
                }
            }

            isSuccess &= targetPath.toFile().delete();
        }

        if (!isSuccess) {
            throw new RuntimeException("delete directory error");
        }

        return true;
    }

    /**
     * 파일 압축 해제
     *
     * @param src 압축 파일
     * @param dst 압축 해제 경로
     */
    public static void decompression(String src, String dst) {
        Path srcPath = Paths.get(src);
        Path dstPath = Paths.get(dst);

        if (!srcPath.toFile().exists()) {
            throw new RuntimeException("not found zip file");
        }

        if (!dstPath.toFile().exists()) {
            mkdirs(dstPath.toString());
        } else {
            throw new RuntimeException("already exist dst directory");
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(srcPath.toFile()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = newFile(dstPath.toFile(), entry);

                if (entry.isDirectory()) {
                    // 폴더는 생성만 하고 넘어감
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new RuntimeException("failed to create directory " + newFile);
                    }
                } else {
                    // 부모 폴더가 없으면 생성
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new RuntimeException("failed to create directory " + parent);
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
    private static File newFile(File dstDir, ZipEntry zipEntry) throws IOException {
        File dstFile = new File(dstDir, zipEntry.getName());

        String dst = dstDir.getCanonicalPath();

        if (!dstFile.getCanonicalPath().startsWith(dst + File.separator)) {
            throw new IOException("entry is outside of the target dir: " + zipEntry.getName());
        }

        return dstFile;
    }
}
