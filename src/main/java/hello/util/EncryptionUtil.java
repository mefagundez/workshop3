package hello.util;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class EncryptionUtil {
    private static SecretKeySpec skeySpec;

    static {
        try {
            System.out.println("Static constructing!");
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            SecureRandom random = new SecureRandom();
            keyGen.init(random);
            SecretKey secretKey = keyGen.generateKey();
            byte[] encodedSecretKey = secretKey.getEncoded();
            skeySpec = new SecretKeySpec(encodedSecretKey, "AES");
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    public static byte[] encrypt(byte[] input)
            throws GeneralSecurityException, NoSuchPaddingException{
        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        return cipher.doFinal(input);

    }


    public static byte[] decrypt(byte[] input) throws GeneralSecurityException, NoSuchPaddingException{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        return cipher.doFinal(input);
    }

}

