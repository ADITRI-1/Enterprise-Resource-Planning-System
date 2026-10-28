import { expect, type Page } from "@playwright/test";

export const ACCOUNTS = {
  admin: { username: "admin1", password: "Admin@123" },
  instructor: { username: "inst1", password: "Inst@123" },
  student: { username: "stu1", password: "Stu@123" },
  student2: { username: "stu2", password: "Stu@123" },
};

export async function signIn(page: Page, username: string, password: string) {
  await page.goto("/login");
  await page.getByLabel("Username").fill(username);
  await page.getByLabel("Password", { exact: true }).fill(password);
  await page.getByRole("button", { name: "Sign in" }).click();
  await expect(page).not.toHaveURL(/\/login/);
}

export async function signInAs(page: Page, who: keyof typeof ACCOUNTS) {
  await signIn(page, ACCOUNTS[who].username, ACCOUNTS[who].password);
}
