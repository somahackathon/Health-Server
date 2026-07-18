package team.soma.teto.health.ai.client;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    private Mode mode = Mode.REAL;
    private Mode fitnessMode;
    private Mode postureMode;
    private URI baseUrl;
    private String apiKey;
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(10);
    private String fitnessPath = "/fitness/analyze";
    private String posturePath = "/posture/analyze";
    private int retryCount = 0;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Mode getFitnessMode() {
        return fitnessMode;
    }

    public void setFitnessMode(Mode fitnessMode) {
        this.fitnessMode = fitnessMode;
    }

    public Mode getPostureMode() {
        return postureMode;
    }

    public void setPostureMode(Mode postureMode) {
        this.postureMode = postureMode;
    }

    public URI getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(URI baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getFitnessPath() {
        return fitnessPath;
    }

    public void setFitnessPath(String fitnessPath) {
        this.fitnessPath = fitnessPath;
    }

    public String getPosturePath() {
        return posturePath;
    }

    public void setPosturePath(String posturePath) {
        this.posturePath = posturePath;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public enum Mode {
        REAL,
        MOCK
    }
}
