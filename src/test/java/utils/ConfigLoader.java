package utils;

import io.restassured.path.json.JsonPath;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class ConfigLoader {
    private static final Properties properties = new Properties();

    static {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new IllegalStateException("config.properties not found on classpath");
            }
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    private ConfigLoader() {
    }

    public static String getBaseUri() {
        String value = properties.getProperty("baseURI");
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("Property 'baseURI' not found in config.properties");
        }
        return value;
    }

    public static String getAuthUri() {
        String value = properties.getProperty("authURI");
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("Property 'authURI' not found in config.properties");
        }
        return value;
    }

    public static Map<String, String> getLoginPayload() {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("login_payload.json")) {
            if (in == null) {
            throw new IllegalStateException("login_payload.json not found on classpath");
            }
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            JsonPath jsonPath = new JsonPath(json);
            Map<String, String> payload = new HashMap<>();
            payload.put("username", jsonPath.getString("username"));
            payload.put("password", jsonPath.getString("password"));
            return payload;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load login_payload.json", e);
        }
    }
}