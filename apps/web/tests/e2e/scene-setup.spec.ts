import { expect, test } from "@playwright/test";

test("configures Scene Setup, initializes Editor, edits Character and persists Composition", async ({ page }) => {
  await page.goto("/create");
  await page.getByRole("button", { name: "Blank Story" }).click();
  await page.getByRole("button", { name: "Continue" }).click();
  await page.getByLabel("Story idea").fill("Milo finds a butterfly in the garden.");
  await page.getByLabel("Target age").selectOption("6_8");
  await page.getByRole("button", { name: "Continue" }).click();
  await expect(page).toHaveURL(/\/stories\/.+\/outline$/);
  await page.getByRole("button", { name: "+ Add Scene" }).click();
  await page.getByLabel("Scene 1 title").fill("Garden Discovery");
  await page.getByLabel("Scene 1 summary").fill("Milo discovers a butterfly in the garden.");
  await page.getByLabel("Scene 1 duration").fill("45");
  await page.getByRole("button", { name: "Save Changes" }).click();
  await page.getByRole("link", { name: /Continue to Characters/ }).click();
  await page.getByLabel("Name").fill("Milo");
  await page.getByLabel("Role description").fill("A curious little rabbit");
  await page.getByLabel("Visual description").fill("Small brown rabbit with a blue scarf");
  await page.getByRole("button", { name: "Create & Add to Story" }).click();
  await page.getByRole("link", { name: /Continue to Scene Setup/ }).click();
  await expect(page.getByRole("heading", { name: "Garden Discovery" })).toBeVisible();
  await page.getByRole("button", { name: /Sunny Forest/ }).click();
  await page.getByRole("button", { name: /Milo/ }).click();
  await page.getByRole("button", { name: /Wooden Chair/ }).click();
  await page.getByRole("button", { name: "Save Changes" }).click();
  await expect(page.getByRole("button", { name: "Saved" })).toBeVisible();
  await page.reload();
  await page.getByRole("link", { name: "Open Editor →" }).click();
  await expect(page).toHaveURL(/\/stories\/.+\/editor\?scene=/);

  const editorResponse = page.waitForResponse(response =>
    response.url().includes("/api/v1/stories/") && response.url().includes("/editor") && response.request().method() === "GET"
  );
  await page.waitForTimeout(500);
  const response = await editorResponse;
  expect(response.ok(), await response.text()).toBeTruthy();

  await expect(page.getByRole("heading", { name: "Properties" })).toBeVisible({ timeout: 15000 });
  await expect(page.getByText("CHARACTER", { exact: true })).toBeVisible({ timeout: 5000 });
  await expect(page.getByRole("main")).toContainText("1920 by 1080 logical stage");

  await page.getByRole("button", { name: /Milo/ }).first().click();
  await page.getByLabel("X").fill("900");
  await page.getByLabel("Scale").fill("1.5");
  await page.getByRole("button", { name: "Save" }).click();
  await expect(page.getByText("Saved", { exact: true })).toBeVisible();

  await page.reload();
  await page.getByRole("button", { name: /Milo/ }).first().click();
  await expect(page.getByLabel("X")).toHaveValue("900");
  await expect(page.getByLabel("Scale")).toHaveValue("1.5");
  await expect(page.getByText("Saved", { exact: true })).toBeVisible();
});
