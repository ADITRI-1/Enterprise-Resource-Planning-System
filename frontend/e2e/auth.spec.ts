import { expect, test } from "@playwright/test";
import { signInAs } from "./helpers";

test("wrong password shows a clear message", async ({ page }) => {
  await page.goto("/login");
  await page.getByLabel("Username").fill("stu2");
  await page.getByLabel("Password", { exact: true }).fill("not-it");
  await page.getByRole("button", { name: "Sign in" }).click();
  await expect(page.getByRole("alert")).toHaveText(/Incorrect username or password/);
});

test("each role lands on its own dashboard", async ({ page }) => {
  await signInAs(page, "student");
  await expect(page).toHaveURL(/\/student$/);
  await expect(page.getByRole("heading", { name: /Hello, Aarav/ })).toBeVisible();

  // Students can't open admin pages
  await page.goto("/admin/students");
  await expect(page).toHaveURL(/\/student$/);

  await page.getByRole("button", { name: /Aarav Sharma/ }).click();
  await page.getByRole("menuitem", { name: "Log out" }).click();
  await expect(page).toHaveURL(/\/login/);

  await signInAs(page, "instructor");
  await expect(page).toHaveURL(/\/instructor$/);
  await page.context().clearCookies();

  await signInAs(page, "admin");
  await expect(page.getByRole("heading", { name: "Admin dashboard" })).toBeVisible();
});
