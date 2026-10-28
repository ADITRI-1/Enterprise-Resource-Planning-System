import { expect, test } from "@playwright/test";
import { signInAs } from "./helpers";

test.beforeEach(async ({ page }) => signInAs(page, "student"));

test("register for a section, see it in registrations and timetable, then drop it", async ({ page }) => {
  await page.goto("/student/catalog");
  const mth = page.getByRole("row", { name: /MTH101/ });
  await mth.getByRole("button", { name: "Register" }).click();
  await page.getByRole("dialog").getByRole("button", { name: "Register" }).click();
  await expect(page.getByText("Registered for MTH101")).toBeVisible();
  await expect(mth.getByText("Registered")).toBeVisible();

  await page.goto("/student/timetable");
  await expect(page.getByText("MTH101").filter({ visible: true }).first()).toBeVisible();

  await page.goto("/student/registrations");
  const row = page.getByRole("row", { name: /MTH101/ });
  await row.getByRole("button", { name: "Drop" }).click();
  await page.getByRole("dialog").getByRole("button", { name: "Drop course" }).click();
  await expect(row.getByText("dropped")).toBeVisible();

  // The bell picked up both events
  await page.getByRole("button", { name: /Notifications/ }).click();
  await expect(page.getByText("Dropped MTH101")).toBeVisible();
});

test("catalog shows unmet prerequisites instead of a register button", async ({ page }) => {
  await page.goto("/student/catalog");
  const row = page.getByRole("row", { name: /CSE301/ });
  await expect(row.getByText("Needs CSE201")).toBeVisible();
  await expect(row.getByRole("button", { name: "Register" })).toHaveCount(0);
});

test("grades page offers a PDF transcript", async ({ page }) => {
  await page.goto("/student/grades");
  await expect(page.getByText("CGPA").first()).toBeVisible();
  const download = page.waitForEvent("download");
  await page.getByRole("button", { name: "Transcript PDF" }).click();
  expect((await download).suggestedFilename()).toMatch(/^transcript-.*\.pdf$/);
});
