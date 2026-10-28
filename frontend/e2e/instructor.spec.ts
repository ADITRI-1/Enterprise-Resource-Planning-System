import { expect, test } from "@playwright/test";
import { signInAs } from "./helpers";

test("edit a score, compute final grades and see statistics", async ({ page }) => {
  await signInAs(page, "instructor");
  await page.goto("/instructor/sections/CSE101-INS001-26-1");

  const quiz = page.getByLabel("Aarav Sharma Quiz");
  await quiz.fill("21");
  await expect(page.getByRole("button", { name: /Save/ })).toBeDisabled(); // over the maximum of 20
  await quiz.fill("19");
  await page.getByRole("button", { name: /Save \(1\)/ }).click();
  await expect(page.getByText("Scores saved")).toBeVisible();

  await page.getByRole("tab", { name: "Final grades" }).click();
  await page.getByRole("button", { name: "Compute grades" }).click();
  await expect(page.getByText(/Computed 1 final grade/)).toBeVisible();
  // 19/20*20 + 41/50*30 + 80/100*50 = 83.6 -> A-
  await expect(page.getByRole("row", { name: /Aarav Sharma/ })).toContainText("83.60");

  await page.getByRole("tab", { name: "Statistics" }).click();
  await expect(page.getByText("Grade distribution")).toBeVisible();
  await expect(page.getByText("83.6").first()).toBeVisible();
});
