package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class ExtentReportManager {
    private static ExtentReports extent;

    public static ExtentReports getInstance() {
        if (extent == null) {
            String reportPath = "target/ExtentReport.html";
            System.out.println("Extent report will be saved to: " + reportPath);

            ExtentSparkReporter reporter = new ExtentSparkReporter(reportPath);
            reporter.config().setDocumentTitle("API Test Report");
            reporter.config().setReportName("Sprint 4 API Automation");
            reporter.config().setTheme(Theme.STANDARD);

            extent = new ExtentReports();
            extent.attachReporter(reporter);
            extent.setSystemInfo("Tester", "Rauna");
            extent.setSystemInfo("Environment", "QA");
        }
        return extent;
    }

    public static void setTotalTestCases(int count) {
        if (extent != null) {
            extent.setSystemInfo("Total Test Cases Executed", String.valueOf(count));
        }
    }
}