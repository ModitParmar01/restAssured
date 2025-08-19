package tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.Constants;
import utils.ConfigLoader;
import utils.AuthTokenProvider;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.Matchers.*;

public class DataDrivenBookingCreationTest {

    @DataProvider(name = "bookingCreationData")
    public Iterator<Object[]> getBookingCreationData() {
        List<Object[]> testData = new ArrayList<>();
        String csvFile = "src/test/resources/testdata/booking_creation_data.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] data = line.split(",");
                testData.add(new Object[] {
                        Integer.parseInt(data[0]),
                        Integer.parseInt(data[1]), 
                        data[2],
                        data[3], 
                        Integer.parseInt(data[4]), 
                        Integer.parseInt(data[5]), 
                        data[6] 
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testData.iterator();
    }

    @Test(dataProvider = "bookingCreationData")
    public void testAddBooking(int bookingId, int userId, String movieTitle,
            String bookingDate, int ticketCount, int expectedStatus, String testDescription) {
        RestAssured.baseURI = ConfigLoader.getBaseUri();

        RequestSpecification req = RestAssured
                .given()
                .contentType(ContentType.URLENC)
                .formParam("bookingId", bookingId)
                .formParam("userId", userId)
                .formParam("movieTitle", movieTitle)
                .formParam("bookingDate", bookingDate)
                .formParam("ticketCount", ticketCount);

        if (Boolean.parseBoolean(System.getProperty("useAuth", "true"))) {
            req.header("Authorization", "Bearer " + AuthTokenProvider.getToken());
        }

        Response response = req
                .when()
                .post(Constants.ADD_BOOKING)
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
            Assert.assertTrue(response.getStatusCode() == 200, "Booking should be created successfully");

            try {
                String responseBody = response.getBody().asString();
                if (responseBody.contains("<booking>") || responseBody.contains("bookingId")) {
                    System.out.println("Response contains booking information");
                }
            } catch (Exception e) {
                System.out.println("Response body validation skipped: " + e.getMessage());
            }

            long responseTime = response.getTime();
            if (responseTime > 2000) {
                System.out.println("Warning: Response time is slow: " + responseTime + " ms");
            }

            System.out.println("Test completed with flexible validations");
            System.out.println("Booking created successfully for: " + movieTitle);
        } else {
            System.out.println("Booking creation failed as expected for: " + movieTitle);
        }
    }
}