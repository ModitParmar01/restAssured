package utils;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

public class TestListener implements ITestListener {
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    static {
        try {
            extent = ExtentReportManager.getInstance();
            System.out.println("Extent Reports initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing Extent Reports: " + e.getMessage());
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        if (extent == null) {
            System.err.println("Extent Reports not initialized!");
            return;
        }

        String testName = result.getMethod().getMethodName();
        if (result.getParameters() != null && result.getParameters().length > 0) {
            StringBuilder params = new StringBuilder();
            for (int i = 0; i < result.getParameters().length; i++) {
                if (i > 0)
                    params.append(", ");
                params.append(result.getParameters()[i]);
            }
            testName += " [" + params.toString() + "]";
        }

        System.out.println("Starting test: " + testName);
        try {
            ExtentTest extentTest = extent.createTest(testName);
            test.set(extentTest);
        } catch (Exception e) {
            System.err.println("Error creating test in Extent Report: " + e.getMessage());
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        try {
            if (test.get() != null) {
                test.get().pass("Test passed");
            }
        } catch (Exception e) {
            System.err.println("Error marking test as passed: " + e.getMessage());
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        try {
            if (test.get() != null) {
                test.get().fail(result.getThrowable());
            }
        } catch (Exception e) {
            System.err.println("Error marking test as failed: " + e.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        try {
            if (test.get() != null) {
                test.get().skip("Test skipped");
            }
        } catch (Exception e) {
            System.err.println("Error marking test as skipped: " + e.getMessage());
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        int totalTestCases = context.getPassedTests().size() +
                context.getFailedTests().size() +
                context.getSkippedTests().size();

        System.out.println("Total test cases executed: " + totalTestCases);
        System.out.println("Passed: " + context.getPassedTests().size());
        System.out.println("Failed: " + context.getFailedTests().size());
        System.out.println("Skipped: " + context.getSkippedTests().size());

        try {
            if (extent != null) {
                System.out.println("Flushing Extent Report...");
                ExtentReportManager.setTotalTestCases(totalTestCases);
                extent.flush();
                System.out.println("Extent report generated at: target/ExtentReport.html");
            } else {
                System.err.println("Extent Reports is null, cannot generate report");
            }
        } catch (Exception e) {
            System.err.println("Error flushing Extent Report: " + e.getMessage());
        }
    }
}