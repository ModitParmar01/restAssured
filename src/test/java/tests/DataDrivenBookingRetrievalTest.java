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

public class DataDrivenBookingRetrievalTest {

    @DataProvider(name = "bookingRetrievalData")
    public Iterator<Object[]> getBookingRetrievalData() {
        List<Object[]> testData = new ArrayList<>();
        String csvFile = "src/test/resources/testdata/booking_retrieval_data.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                String[] data = line.split(",");
                testData.add(new Object[] {
                        Integer.parseInt(data[0]),
                        Integer.parseInt(data[1]), 
                        data[2]
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testData.iterator();
    }

    @Test(dataProvider = "bookingRetrievalData")
    public void testGetBookingById(int bookingId, int expectedStatus, String testDescription) {
        RestAssured.baseURI = ConfigLoader.getBaseUri();

        RequestSpecification req = RestAssured
                .given()
                .accept(ContentType.JSON)
                .pathParam("bookingId", bookingId);

        if (Boolean.parseBoolean(System.getProperty("useAuth", "true"))) {
            req.header("Authorization", "Bearer " + AuthTokenProvider.getToken());
        }

        Response response = req
                .when()
                .get(Constants.GET_BOOKING_BY_ID)
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
            Assert.assertTrue(response.getStatusCode() == 200, "Booking should be retrieved successfully");

            long responseTime = response.getTime();
            if (responseTime > 5000) {
                System.out.println("Warning: Response time is slow: " + responseTime + " ms");
            }

            System.out.println("Test completed with flexible validations");
            System.out.println("Booking retrieved successfully for ID: " + bookingId);
        } else {
            System.out.println("Booking retrieval failed as expected for ID: " + bookingId);
        }
    }

    @Test
    public void testViewAllBookings() {
        RestAssured.baseURI = ConfigLoader.getBaseUri();

        RequestSpecification req = RestAssured
                .given()
                .accept(ContentType.JSON);

        if (Boolean.parseBoolean(System.getProperty("useAuth", "true"))) {
            req.header("Authorization", "Bearer " + AuthTokenProvider.getToken());
        }

        Response response = req
                .when()
                .get(Constants.GET_BOOKING_LIST)
                .then()
                .log().ifError()
                .statusCode(200)
                .time(lessThan(5000L))

                .extract().response();

        Assert.assertTrue(response.getStatusCode() == 200, "All bookings should be retrieved successfully");

        long responseTime = response.getTime();
        if (responseTime > 10000) {
            System.out.println("Warning: Response time is slow: " + responseTime + " ms");
        }

        System.out.println("Response Time: " + responseTime + " ms");
        System.out.println("Test completed with flexible validations");
        System.out.println("All bookings retrieved successfully");
    }
}