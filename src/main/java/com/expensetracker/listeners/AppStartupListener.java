package com.expensetracker.listeners;

import com.expensetracker.util.MonthlyReportScheduler;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * AppStartupListener - Initializes background services when the application starts.
 * Handles graceful shutdown of scheduled tasks.
 */
@WebListener
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("🚀 QuickExpense Application Starting...");

        // Start the monthly report scheduler
        MonthlyReportScheduler.getInstance().start();

        System.out.println("✅ QuickExpense Application Initialized Successfully!");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("🛑 QuickExpense Application Shutting Down...");

        // Stop the monthly report scheduler
        MonthlyReportScheduler.getInstance().stop();

        System.out.println("✅ QuickExpense Shutdown Complete.");
    }
}
