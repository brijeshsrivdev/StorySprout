import { expect, test } from "@playwright/test";

test("creates a character, reuses it, and preserves it after removing story membership", async ({ page }) => {
  page.on("dialog", dialog => dialog.accept());
  await page.goto("/create");
  const blankStory = page.getByRole("button", { name: "Blank Story" });
  await expect(async () => {
    await blankStory.click();
    await expect(blankStory).toHaveAttribute("aria-pressed", "true");
  }).toPass();
  await page.getByRole("button", { name: "Continue" }).click();
  await page.getByLabel("Story idea").fill("A rabbit learns to share.");
  await page.getByLabel("Target age").selectOption("6_8");
  await page.getByRole("button", { name: "Continue" }).click();
  await expect(page).toHaveURL(/\/stories\/.+\/outline$/);
  await page.getByRole("button", { name: "+ Add Scene" }).click();
  await page.getByRole("button", { name: "Save Changes" }).click();
  await page.getByRole("link", { name: /Continue to Characters/ }).click();
  await expect(page).toHaveURL(/\/stories\/.+\/characters$/);
  await page.getByLabel("Name").fill("Milo");
  await page.getByLabel("Role description").fill("A curious rabbit who helps friends.");
  await page.getByLabel("Visual description").fill("Brown rabbit with a blue scarf.");
  await page.getByRole("button", { name: "Create & Add to Story" }).click();
  await expect(page.getByRole("heading", { name: "Milo" })).toBeVisible();
  await page.getByRole("button", { name: "Remove" }).click();
  await expect(page.getByText("No characters yet")).toBeVisible();
  await expect(page.getByRole("heading", { name: "Milo" })).toBeVisible();
  await expect(page.getByRole("button", { name: "Add to Story", exact: true })).toBeVisible();
});


test("AI suggestion fills editable fields without creating a character until explicit creation", async ({ page }) => {
  await page.goto("/create");
  const blankStory = page.getByRole("button", { name: "Blank Story" });
  await expect(async () => {
    await blankStory.click();
    await expect(blankStory).toHaveAttribute("aria-pressed", "true");
  }).toPass();
  await page.getByRole("button", { name: "Continue" }).click();
  await page.getByLabel("Story idea").fill("A kind fox learns courage.");
  await page.getByLabel("Target age").selectOption("6_8");
  await page.getByRole("button", { name: "Continue" }).click();
  await page.getByRole("button", { name: "+ Add Scene" }).click();
  await page.getByRole("button", { name: "Save Changes" }).click();
  await page.getByRole("link", { name: /Continue to Characters/ }).click();
  await page.getByPlaceholder(/brave little fox/).fill("A brave fox who helps friends.");
  await page.getByRole("button", { name: "Generate Suggestion" }).click();
  await expect(page.getByLabel("Name")).toHaveValue("Milo");
  await expect(page.getByText("No characters yet")).toBeVisible();
});

