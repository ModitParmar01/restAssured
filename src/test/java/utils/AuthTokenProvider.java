package utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Map;

public final class AuthTokenProvider {
    private static String cachedToken;

    private AuthTokenProvider() {
    }

    public static synchronized String getToken() {
        if (cachedToken != null && !cachedToken.isEmpty()) {
            return cachedToken;
        }

        RestAssured.baseURI = ConfigLoader.getBaseUri();
        Map<String, String> creds = ConfigLoader.getLoginPayload();

        Response response = RestAssured
                .given()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .formParam("username", creds.get("username"))
                .formParam("password", creds.get("password"))
                .when()
                .post(ConfigLoader.getAuthUri())
                .then()
                .statusCode(200)
                .extract().response();

        cachedToken = response.jsonPath().getString("auth_code");
        if (cachedToken == null || cachedToken.isEmpty()) {
            throw new IllegalStateException("Auth token (auth_code) not returned by auth service");
        }
        return cachedToken;
    }

    public static synchronized void clearToken() {
        cachedToken = null;
    }
}