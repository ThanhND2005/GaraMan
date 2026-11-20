package vn.garaman.web;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.*;
import java.util.*;

public final class Password2 {
    private Password2() {
    }

    public static String hash(String password) throws Exception {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return "pbkdf2$210000$" + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(derive(password, salt));
    }

    public static boolean verify(String password, String encoded) throws Exception {
        String[] x = encoded.split("\\$");
        if (x.length != 4 || !"pbkdf2".equals(x[0]) || !"210000".equals(x[1]))
            return false;
        return MessageDigest.isEqual(derive(password, Base64.getDecoder().decode(x[2])),
                Base64.getDecoder().decode(x[3]));
    }

    private static byte[] derive(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 210000, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new IllegalArgumentException("Usage: Password2 <password>");
        System.out.println(hash(args[0]));
    }
}
