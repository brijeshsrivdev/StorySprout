import { expect, test } from "@playwright/test";

test("builds a reusable character library and preserves membership semantics", async ({ page }) => {
  page.on("dialog", dialog => dialog.accept());
  await page.goto("/create");
  const blankStory = page.getByRole("button", { name: "Start Blank" });
  await blankStory.click();
  await page.getByRole("button", { name: /Continue/ }).click();
  await page.getByLabel("Story idea").fill("A rabbit learns to share.");
  await page.getByRole("button", { name: "Ages 6–8" }).click();
  await page.getByRole("button", { name: "Continue" }).click();
  await page.getByRole("button", { name: "+ Add Scene" }).first().click();
  await page.getByRole("button", { name: "Save Changes" }).click();
  await expect(page.getByText("Saved", { exact: true })).toBeVisible({ timeout: 15000 });
  await expect(page.getByRole("link", { name: /Continue to Characters/ })).not.toHaveAttribute("aria-disabled", "true");
  await page.getByRole("link", { name: /Continue to Characters/ }).click();

  await expect(page.getByRole("heading", { name: "Your Characters" })).toBeVisible();
  await expect(page.getByText("Every story needs a cast.")).toBeVisible();

  await page.getByLabel("Name").fill("Milo");
  await expect(page.locator("label").filter({ hasText: "Name" }).first()).toHaveAttribute("for", "character-form");
  await page.getByLabel("Role / description").fill("A curious rabbit who helps friends.");
  await page.getByLabel("Category").selectOption("ANIMAL");
  await page.getByLabel("Visual description").fill("Brown rabbit with a blue scarf.");
  await page.getByLabel("Personality (optional)").fill("Curious, kind, and brave.");
  await page.getByRole("button", { name: "Create & Add to Story" }).click();

  await expect(page.getByRole("heading", { name: "Milo" })).toBeVisible();
  await expect(page.getByText("Animal", { exact: true }).first()).toBeVisible();
  await expect(page.getByText("Curious, kind, and brave.")).toBeVisible();
  await expect(page.getByRole("heading", { name: "In this Story" })).toBeVisible();
  await expect(page.getByRole("button", { name: "Remove from Story" })).toBeVisible();

  await page.getByRole("button", { name: "Edit" }).click();
  await expect(page.getByRole("heading", { name: "Edit Character" })).toBeVisible();
  await page.getByLabel("Name").fill("Milo the Helper");
  await page.getByRole("button", { name: "Save Character" }).click();
  await expect(page.getByRole("heading", { name: "Milo the Helper" })).toBeVisible();

  await page.getByRole("button", { name: "Remove from Story" }).click();
  await expect(page.getByText("Every story needs a cast.")).toBeVisible();
  await expect(page.getByRole("heading", { name: "Milo the Helper" })).toBeVisible();
  await expect(page.getByRole("button", { name: "Add to Story", exact: true })).toBeVisible();

  await page.getByRole("button", { name: "Delete" }).click();
  await expect(page.getByRole("heading", { name: "Milo the Helper" })).toHaveCount(0);
});

test("AI suggestion remains editable and does not persist until creation", async ({ page }) => {
  await page.goto("/create");
  const blankStory = page.getByRole("button", { name: "Start Blank" });
  await blankStory.click();
  await page.getByRole("button", { name: /Continue/ }).click();
  await page.getByLabel("Story idea").fill("A kind fox learns courage.");
  await page.getByRole("button", { name: "Ages 6–8" }).click();
  await page.getByRole("button", { name: "Continue" }).click();
  await page.getByRole("button", { name: "+ Add Scene" }).first().click();
  await page.getByRole("button", { name: "Save Changes" }).click();
  await page.getByRole("link", { name: /Continue to Characters/ }).click();

  await page.getByLabel("Character idea").fill("A brave fox who helps friends.");
  await page.getByRole("button", { name: "Generate Suggestion" }).click();
  await expect(page.getByText(/AI suggestion is ready/)).toBeVisible();
  await expect(page.getByText("Every story needs a cast.")).toBeVisible();

  await page.getByLabel("Name").fill("Edited Fox");
  await page.getByRole("button", { name: "Create & Add to Story" }).click();
  await expect(page.getByRole("heading", { name: "Edited Fox" })).toBeVisible();
});

test("requires valid character fields and supports keyboard-accessible category selection", async ({ page }) => {
  await page.goto("/create");
  const blankStory = page.getByRole("button", { name: "Start Blank" });
  await blankStory.click();
  await page.getByRole("button", { name: /Continue/ }).click();
  await page.getByLabel("Story idea").fill("A small friend learns kindness.");
  await page.getByRole("button", { name: "Ages 6–8" }).click();
  await page.getByRole("button", { name: "Continue" }).click();
  await page.getByRole("button", { name: "+ Add Scene" }).first().click();
  await page.getByRole("button", { name: "Save Changes" }).click();
  await page.getByRole("link", { name: /Continue to Characters/ }).click();

  const createButton = page.getByRole("button", { name: "Create & Add to Story" });
  await expect(createButton).toBeDisabled();
  await page.getByLabel("Name").fill("Luna");
  await page.getByLabel("Role / description").fill("A thoughtful friend.");
  await page.getByLabel("Visual description").fill("Small star-shaped character.");
  await page.getByLabel("Category").selectOption("OTHER");
  await expect(page.getByLabel("Category")).toHaveValue("OTHER");
  await page.getByLabel("Category").selectOption("ADULT");
  await expect(page.getByLabel("Category")).toHaveValue("ADULT");
  await expect(createButton).toBeEnabled();
});
