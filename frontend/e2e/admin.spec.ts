import { expect, test } from "@playwright/test";
import { signIn, signInAs } from "./helpers";

test("new student gets a temporary password and must change it", async ({ page, browser }) => {
  await signInAs(page, "admin");
  await page.goto("/admin/students");
  await page.getByRole("button", { name: /New student/ }).click();
  await page.getByLabel("Full name").fill("Meera Iyer");
  await page.getByLabel("Roll number").fill("2024010");
  await page.getByLabel("Program").selectOption("BTECH-CSE");
  await page.getByLabel(/Batch/).fill("2024");
  await page.getByLabel("Date of birth").fill("2006-04-12");
  await page.getByRole("button", { name: "Save" }).click();

  const dialog = page.getByRole("dialog", { name: "Temporary password" });
  await expect(dialog).toBeVisible();
  const password = (await dialog.locator("dd .font-mono").last().innerText()).trim();

  const student = await (await browser.newContext()).newPage();
  await signIn(student, "meera_bt2024010", password);
  await expect(student.getByText("Choose your own password")).toBeVisible();
  await student.getByLabel("Temporary password").fill(password);
  await student.getByLabel("New password", { exact: true }).fill("Meera2026x");
  await student.getByLabel("Confirm new password").fill("Meera2026x");
  await student.getByRole("button", { name: "Change password" }).click();
  await expect(student.getByRole("heading", { name: "Hello, Meera" })).toBeVisible();
});

test("maintenance mode shows a banner and blocks changes for students", async ({ page, browser }) => {
  await signInAs(page, "admin");
  const toggle = page.getByRole("switch", { name: "Maintenance mode" });
  await toggle.click();
  await expect(page.getByText("Maintenance is ON")).toBeVisible();
  try {
    const student = await (await browser.newContext()).newPage();
    await signIn(student, "stu2", "Stu@123");
    await student.goto("/student/catalog");
    await expect(student.getByText(/maintenance mode/i).first()).toBeVisible();
    await expect(student.getByRole("button", { name: "Register" }).first()).toBeDisabled();
  } finally {
    await toggle.click();
    await expect(page.getByText("System is live")).toBeVisible();
  }
});

test("audit log and analytics", async ({ page }) => {
  await signInAs(page, "admin");
  await page.goto("/admin/audit");
  await expect(page.getByText("POST auth/login").first()).toBeVisible();

  await page.goto("/admin/analytics");
  await expect(page.getByText("Section fill")).toBeVisible();
  await expect(page.locator(".recharts-surface").first()).toBeVisible();
});

test("prerequisites can't form a cycle", async ({ page }) => {
  await signInAs(page, "admin");
  await page.goto("/admin/courses");
  await page.getByRole("button", { name: "Prerequisites for CSE101" }).click();
  await page.getByRole("dialog").getByLabel(/CSE301/).check();
  await page.getByRole("dialog").getByRole("button", { name: "Save" }).click();
  await expect(page.getByRole("alert")).toContainText("circular");
});
