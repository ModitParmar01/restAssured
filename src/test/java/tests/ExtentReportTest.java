package tests;

import org.testng.annotations.Test;
import utils.ExtentReportManager;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

public class ExtentReportTest {

    @Test
    public void testExtentReportGeneration() {
        System.out.println("Testing Extent Report generation...");

        try {
            System.out.println("Testing ExtentReportManager...");
            ExtentReports extent = ExtentReportManager.getInstance();
            System.out.println("ExtentReportManager.getInstance() successful: " + (extent != null));

            if (extent != null) {
                ExtentTest test = extent.createTest("Manual Test", "Testing Extent Reports manually");
                test.pass("Manual test passed");

                System.out.println("Attempting to flush Extent Report...");
                extent.flush();
                System.out.println("Extent Report flushed successfully");
            }
        } catch (Exception e) {
            System.err.println("Error in Extent Reports test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}