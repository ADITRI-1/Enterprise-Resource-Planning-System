import { defineConfig, devices } from "@playwright/test";

/**
 * Browser tests against a real backend. Both servers run on their own ports with a freshly
 * seeded database, so they never touch your dev data:
 *
 *   npx playwright install chromium   # once
 *   npm run test:e2e
 */
const API_PORT = 8081;
const WEB_PORT = 5174;

export default defineConfig({
  testDir: "./e2e",
  fullyParallel: false,
  workers: 1, // the specs share one database
  retries: process.env.CI ? 1 : 0,
  timeout: 45_000,
  reporter: process.env.CI ? [["github"], ["html", { open: "never" }]] : "list",
  use: {
    baseURL: `http://localhost:${WEB_PORT}`,
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
  },
  projects: [{ name: "chromium", use: { ...devices["Desktop Chrome"] } }],
  webServer: [
    {
      command: "./mvnw -q -B spring-boot:test-run -Dspring-boot.run.main-class=com.iiitd.erp.DevServer",
      cwd: "..",
      url: `http://localhost:${API_PORT}/api/system/maintenance`,
      env: { SERVER_PORT: String(API_PORT), ERP_DB_DIR: "target/e2e-postgres", ERP_DB_PORT: "54330", ERP_FRESH: "true" },
      timeout: 300_000,
      reuseExistingServer: false,
      gracefulShutdown: { signal: "SIGTERM", timeout: 15_000 },
      stdout: "ignore",
    },
    {
      command: `npx vite --port ${WEB_PORT} --strictPort`,
      url: `http://localhost:${WEB_PORT}/login`,
      env: { API_TARGET: `http://localhost:${API_PORT}` },
      timeout: 60_000,
      reuseExistingServer: false,
    },
  ],
});
