package tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.ConfigLoader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

public class DataDrivenAuthTest {

    @DataProvider(name = "authTestData")
    public Iterator<Object[]> getAuthTestData() {
        List<Object[]> testData = new ArrayList<>();
        String csvFile = "src/test/resources/testdata/auth_test_data.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] data = line.split(",");
                testData.add(new Object[] { data[0], data[1], Integer.parseInt(data[2]), data[3] });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testData.iterator();
    }

    @Test(dataProvider = "authTestData")
    public void testAuthentication(String username, String password, int expectedStatus, String testDescription) {
        RestAssured.baseURI = ConfigLoader.getBaseUri();

        Response response = RestAssured
                .given()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .formParam("username", username)
                .formParam("password", password)
                .when()
                .post(ConfigLoader.getAuthUri())
                .then()
                .log().ifError()
                .statusCode(expectedStatus)
                .time(lessThan(1500L))
                .extract().response();

        System.out.println("Test: " + testDescription + " - Status: " + response.getStatusCode());
        System.out.println("Response Time: " + response.getTime() + " ms");

        try {
            if (response.getHeader("Content-Type") != null) {
                System.out.println("Content-Type: " + response.getHeader("Content-Type"));
            }
            if (response.getHeader("Date") != null) {
                System.out.println("Date Header: " + response.getHeader("Date"));
            }
        } catch (Exception e) {
            System.out.println("Header validation skipped: " + e.getMessage());
        }

        if (expectedStatus == 200) {
            String token = response.jsonPath().getString("auth_code");
            Assert.assertNotNull(token, "Token should not be null for valid credentials");

            long responseTime = response.getTime();
            if (responseTime > 2000) {
                System.out.println("Warning: Response time is slow: " + responseTime + " ms");
            }

            System.out.println("Test completed with flexible validations");
            System.out.println("Authentication successful for: " + username);
        } else {
            System.out.println("Authentication failed as expected for: " + username);
        }
    }

    @Test
    public void testAuthenticationUsingJsonPayload() {
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
                .log().ifError()
                .statusCode(200)
                .time(lessThan(1500L))
                .extract().response();

        String token = response.jsonPath().getString("auth_code");
        Assert.assertNotNull(token, "Token should not be null for valid credentials from JSON payload");

        long responseTime = response.getTime();
        if (responseTime > 2000) {
            System.out.println("Warning: Response time is slow: " + responseTime + " ms");
        }

        System.out.println("Response Time: " + responseTime + " ms");
        System.out.println("Test completed with flexible validations");
        System.out.println("Authentication via JSON payload creds succeeded for: " + creds.get("username"));
    }
}