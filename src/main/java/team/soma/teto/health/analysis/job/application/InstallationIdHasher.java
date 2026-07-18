package team.soma.teto.health.analysis.job.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Component;

@Component
public class InstallationIdHasher {

    public String hash(String installationId) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(installationId.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private String toHex(byte[] digest) {
        StringBuilder builder = new StringBuilder(digest.length * 2);
        for (byte item : digest) {
            builder.append(String.format("%02x", item));
        }
        return builder.toString();
    }
}
