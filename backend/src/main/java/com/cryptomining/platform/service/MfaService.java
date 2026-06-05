package com.cryptomining.platform.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class MfaService {

    private static final int SECRET_SIZE = 20;
    private static final int CODE_DIGITS = 6;
    private static final int TIME_STEP = 30;

    public String generateSecret() {
        byte[] buffer = new byte[SECRET_SIZE];
        new SecureRandom().nextBytes(buffer);
        return Base64.getEncoder().encodeToString(buffer)
            .replace("+", "").replace("/", "").substring(0, 16).toUpperCase();
    }

    public String generateQrCodeUrl(String email, String secret) {
        String issuer = "CryptoMiningPlatform";
        return String.format(
            "otpauth://totp/%s:%s?secret=%s&issuer=%s&digits=%d",
            URLEncoder.encode(issuer, StandardCharsets.UTF_8),
            URLEncoder.encode(email, StandardCharsets.UTF_8),
            secret, URLEncoder.encode(issuer, StandardCharsets.UTF_8), CODE_DIGITS);
    }

    public String generateQrCodeBase64(String qrUrl) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(qrUrl, BarcodeFormat.QR_CODE, 200, 200);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", stream);
        return Base64.getEncoder().encodeToString(stream.toByteArray());
    }

    public boolean verifyCode(String secret, String code) {
        long timeStep = System.currentTimeMillis() / 1000 / TIME_STEP;
        for (int i = -1; i <= 1; i++) {
            if (generateTotp(secret, timeStep + i).equals(code)) {
                return true;
            }
        }
        return false;
    }

    private String generateTotp(String secret, long timeStep) {
        try {
            byte[] key = Base32.decode(secret);
            byte[] data = new byte[8];
            for (int i = 7; i >= 0; i--) {
                data[i] = (byte) (timeStep & 0xff);
                timeStep >>= 8;
            }
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0xf;
            int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
            int otp = binary % (int) Math.pow(10, CODE_DIGITS);
            return String.format("%0" + CODE_DIGITS + "d", otp);
        } catch (Exception e) {
            return "";
        }
    }

    private static class Base32 {
        private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        static byte[] decode(String encoded) {
            encoded = encoded.toUpperCase().replace("=", "");
            byte[] result = new byte[encoded.length() * 5 / 8];
            int buffer = 0, bitsLeft = 0, index = 0;
            for (char c : encoded.toCharArray()) {
                int val = ALPHABET.indexOf(c);
                if (val < 0) continue;
                buffer = (buffer << 5) | val;
                bitsLeft += 5;
                if (bitsLeft >= 8) {
                    result[index++] = (byte) (buffer >> (bitsLeft - 8));
                    bitsLeft -= 8;
                }
            }
            return result;
        }
    }
}
