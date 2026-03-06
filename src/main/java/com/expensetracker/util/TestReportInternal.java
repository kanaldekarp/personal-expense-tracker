package com.expensetracker.util;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

public class TestReportInternal {
    public static void main(String[] args) {
        System.out.println("🚀 Starting Headless Report Generation Test...");

        try {
            // 1. Setup paths
            String projectRoot = System.getProperty("user.dir");
            String relativePath = "src/main/webapp/reports/HeadlessTestReport.txt";
            String fullPath = projectRoot + File.separator + relativePath;

            System.out.println("📂 Target File Path: " + fullPath);

            // 2. Mock Data
            int userId = 1; // Assuming user 'testuser' exists from previous tests
            LocalDate fromDate = LocalDate.now().minusDays(30);
            LocalDate toDate = LocalDate.now();

            // 3. Execute Report Generator (simulating the Thread logic synchronously for
            // testing)
            System.out.println("⏳ Generating Report for User ID: " + userId);

            ReportGeneratorThread generator = new ReportGeneratorThread(userId, fromDate, toDate, fullPath);
            generator.run(); // Calling run() directly to execute in main thread

            // 4. Verify File Creation
            File reportFile = new File(fullPath);
            if (reportFile.exists() && reportFile.length() > 0) {
                System.out.println("✅ Report File Created Successfully!");
                System.out.println("📄 File Size: " + reportFile.length() + " bytes");

                // Read content preview
                List<String> lines = Files.readAllLines(reportFile.toPath());
                System.out.println("\n--- Report Content Preview ---");
                lines.stream().limit(5).forEach(System.out::println);
                System.out.println("------------------------------");

                System.out.println("🎉 Headless Verification PASSED. The backend logic works.");
            } else {
                System.out.println("❌ Verification FAILED: File was not created or is empty.");
            }

        } catch (Exception e) {
            System.out.println("❌ Exception during test:");
            e.printStackTrace();
        }
    }
}
