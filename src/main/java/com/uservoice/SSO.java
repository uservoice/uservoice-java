package com.uservoice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.net.URLCodec;

public class SSO {
    private static final DateFormat dateFormat;

    static {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static String generateToken(String subdomainName, String ssoKey, Map<String, Object> user)
            throws Unauthorized {
        return generateToken(subdomainName, ssoKey, user, 3600);
    }

    public static String generateToken(String subdomainName, String ssoKey, Map<String, Object> user, int validForSeconds)
            throws Unauthorized {
        URLCodec urlCodec = new URLCodec("ASCII");
        Base64 base64 = new Base64();

        JSONObject jsonObj = new JSONObject();
        jsonObj.putAll(user);

        if (!jsonObj.has("expires")) {
            Calendar expirationTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            expirationTime.setTimeInMillis(expirationTime.getTimeInMillis() + validForSeconds * 1000);
            jsonObj.put("expires", dateFormat.format(expirationTime.getTime()));
        }
        byte[] data = jsonObj.toString().getBytes();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] INIT_VECTOR = "OpenSSL for Ruby".getBytes();

        for (int i = 0; i < 16; i++) {
            data[i] ^= INIT_VECTOR[i];
        }
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        try {
            byte[] hash = DigestUtils.sha(ssoKey + subdomainName);
            byte[] saltedHash = new byte[16];
            System.arraycopy(hash, 0, saltedHash, 0, 16);

            SecretKeySpec secretKeySpec = new SecretKeySpec(saltedHash, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(INIT_VECTOR);

            byte[] buf = new byte[1024];

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

            CipherOutputStream cipherOut = new CipherOutputStream(out, cipher);

            int numRead = 0;
            while ((numRead = in.read(buf)) >= 0) {
                cipherOut.write(buf, 0, numRead);
            }
            cipherOut.close();

            return new String(urlCodec.encode(base64.encode(out.toByteArray())));
        } catch (InvalidKeyException e) {
            throw new Unauthorized(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new Unauthorized(e.getMessage());
        } catch (NoSuchPaddingException e) {
            throw new Unauthorized(e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            throw new Unauthorized(e.getMessage());
        } catch (java.io.IOException e) {
            throw new Unauthorized(e.getMessage());
        }
    }
}
