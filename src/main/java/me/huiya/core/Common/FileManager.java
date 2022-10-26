package me.huiya.core.Common;

import me.huiya.core.Repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.beans.ConstructorProperties;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class FileManager {
    private static final Logger logger = LoggerFactory.getLogger(FileManager.class.getClass());

    /**
     * 파일 저장 base 경로
     */
    private static String FILE_DIR;
    
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static byte[] KEY;
    private static byte[] IV;

    @Value("${core.AES-key}")
    public void setKEY(String key) {
        KEY = key.getBytes();
    }

    @Value("${core.AES-iv}")
    public void setIV(String iv) {
        IV = iv.getBytes();
    }

    @Value("${core.FILE_DIR}")
    public void setPath(String path) {
        FILE_DIR = path;
    }

    // Autowired 대신 추천되는 의존성 주입 방식
    private static FileRepository FileRepo;

    @ConstructorProperties({
        "FileRepository",
    })
    public FileManager(FileRepository FileRepo) { this.FileRepo = FileRepo; }

    /**
     * 실제로 파일 암호화 및 저장을 수행하는 메소드
     * @param filePath 파일 경로
     * @param file 파일 객체
     */
    private static void encryptFile(String filePath, MultipartFile file) {
        FileOutputStream lFileOutputStream = null;

        Key key = new SecretKeySpec(KEY, "AES");
        try {
            byte fileBytes[] = file.getBytes();

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV));

            File lOutFile = new File(filePath);
            lFileOutputStream = new FileOutputStream(lOutFile);
            lFileOutputStream.write(Base64.getEncoder().encode(cipher.doFinal(fileBytes)));

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (lFileOutputStream != null) {
                try {
                    lFileOutputStream.close();
                } catch (IOException e) { }
            }
        }
    }

    /**
     * 실제 파일 복호화
     * @param file 파일 경로
     * @return
     */
    private static byte[] decryptFile(File file) {
        if(!file.exists()) {
            return null;
        }

        Key key = new SecretKeySpec(KEY, "AES");
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV));
            return cipher.doFinal(Base64.getDecoder().decode(fileBytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * SHA-256 파일 체크썸 구하기
     * @param file 파일 객체
     * @return hash 값
     */
    public static String getChecksum(MultipartFile file) {
        // file hashing with DigestInputStream
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            DigestInputStream dis = new DigestInputStream(file.getInputStream(), md);
            while (dis.read() != -1) ; //empty loop to clear the data
            md = dis.getMessageDigest();

            // bytes to hex
            StringBuilder result = new StringBuilder();
            for (byte b : md.digest()) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 파일 암호화 및 저장
     * @param file 업로드된 파일
     * @return 파일 해시 값
     */
    private static String _save(MultipartFile file) {
        String hash = getChecksum(file);
        String path = FILE_DIR;

        if((new File(path + File.separator + hash)).exists()) {
            // 해시값으로 파일을 저장하기 때문에
            // 같은 이름의 파일이 존재하면 같은 파일이라고 가정하고,
            // 저장하지 않음.
            logger.debug("파일 중복되므로 저장하지 않음: "+hash);
            return hash;
        }

        // 실제 파일 암호화 및 저장
        encryptFile(path + File.separator + hash, file);

        return hash;
    }

    /**
     * 파일 암호화 및 저장
     * @param file 업로드된 파일
     * @param userId 사용자 ID
     * @return 파일 해시 값
     */
    public static String save(MultipartFile file, Integer userId) {
        String hash = _save(file);

        me.huiya.core.Entity.File _file = FileRepo.findByHash(hash);
        if(_file != null) {
            return hash;
        }

        me.huiya.core.Entity.File fileEntity = new me.huiya.core.Entity.File();
        fileEntity.setHash(hash);
        fileEntity.setName(file.getOriginalFilename());
        fileEntity.setSize(file.getSize());
        fileEntity.setUserId(userId);
        FileRepo.save(fileEntity);

        return hash;
    }

    /**
     * 파일 암호화 및 저장
     * @param file 업로드된 파일
     * @param userId 사용자 ID
     * @param boardId 게시글 ID
     * @return 파일 해시 값
     */
    public static String save(MultipartFile file, Integer userId, Integer boardId) {
        String hash = _save(file);

        me.huiya.core.Entity.File _file = FileRepo.findByHash(hash);
        if(_file != null) {
            return hash;
        }

        me.huiya.core.Entity.File fileEntity = new me.huiya.core.Entity.File();
        fileEntity.setHash(hash);
        fileEntity.setName(file.getOriginalFilename());
        fileEntity.setSize(file.getSize());
        fileEntity.setUserId(userId);
        fileEntity.setBoardId(boardId);
        FileRepo.save(fileEntity);

        return hash;
    }

    /**
     * 파일 복호화
     * @param hash 파일 해시값 (파일 이름)
     * @return 파일 byte[]
     */
    public static byte[] get(String hash) {
        String filepath = FILE_DIR + File.separator + hash.toLowerCase();
        return decryptFile(new File(filepath));
    }
}
