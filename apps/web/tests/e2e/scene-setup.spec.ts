import { expect, test } from "@playwright/test";

async function openSceneSetup(page: import("@playwright/test").Page) {
  await page.goto("/create");
  await page.getByRole("button", { name: "Start Blank" }).click();
  await page.getByRole("button", { name: /Continue/ }).click();
  await page.getByLabel("Story idea").fill("Milo stages a garden discovery.");
  await page.getByRole("button", { name: "Ages 6–8" }).click();
  await page.getByRole("button", { name: "Continue" }).click();
  await page.getByRole("button", { name: "+ Add Scene" }).first().click();
  await page.getByLabel("Scene 1 title").fill("Garden Discovery");
  await page.getByLabel("Scene 1 summary").fill("Milo discovers a butterfly in the garden.");
  await page.getByLabel("Scene 1 duration").fill("45");
  await page.getByRole("button", { name: "Save Changes" }).click();
  await expect(page.getByText("Saved", { exact: true })).toBeVisible({ timeout: 15000 });
  await expect(page.getByRole("link", { name: /Continue to Characters/ })).not.toHaveAttribute("aria-disabled", "true");
  await page.getByRole("link", { name: /Continue to Characters/ }).click();
  await page.getByLabel("Name").fill("Milo");
  await page.getByLabel("Role / description").fill("A curious little rabbit");
  await page.getByLabel("Visual description").fill("Small brown rabbit with a blue scarf");
  await page.getByRole("button", { name: "Create & Add to Story" }).click();
  await expect(page.getByRole("heading", { name: "Milo" })).toBeVisible();
  await expect(page.getByRole("link", { name: /Continue to Scene Setup/ })).not.toHaveAttribute("aria-disabled", "true");
  await page.getByRole("link", { name: /Continue to Scene Setup/ }).click();
  await expect(page.getByRole("heading", { name: "Garden Discovery" })).toBeVisible({ timeout: 15000 });
  await expect(page.getByText("Project", { exact: true })).toBeVisible();
  await expect(page.getByText("Story", { exact: true })).toBeVisible();
  await expect(page.getByText("Scene", { exact: true })).toBeVisible();
}

test("stages scene ingredients with accessible selection and preserves existing constraints", async ({ page }) => {
  await openSceneSetup(page);

  const forest = page.getByRole("button", { name: /Sunny Forest/ });
  await forest.click();
  await expect(forest).toHaveAttribute("aria-pressed", "true");

  const milo = page.getByRole("button", { name: /Milo/ }).filter({ hasText: "Milo" }).first();
  await milo.click();
  await expect(milo).toHaveAttribute("aria-pressed", "false");
  await milo.click();
  await expect(milo).toHaveAttribute("aria-pressed", "true");

  await page.getByRole("button", { name: "+ Wooden Chair" }).click();
  await page.getByRole("button", { name: "+ Wooden Chair" }).click();
  await expect(page.getByText("Instance", { exact: false })).toHaveCount(2);

  await page.getByRole("button", { name: "+ Add line" }).click();
  await page.getByLabel("Dialogue 1 speaker type").selectOption("CHARACTER");
  await page.getByLabel("Dialogue 1 character").selectOption({ label: "Milo" });
  await page.getByLabel("Dialogue 1 text").fill("Look at that butterfly!");
  await page.getByRole("button", { name: "+ Add line" }).click();
  await page.getByLabel("Dialogue 2 text").fill("The garden is full of surprises.");
  await page.getByLabel("Dialogue 2 speaker type").selectOption("NARRATOR");
  await page.getByRole("button", { name: "Move dialogue 2 up" }).click();

  await page.getByRole("button", { name: "+ Add action" }).click();
  await page.getByLabel("Action 1 action").selectOption("WAVE");
  await expect(page.getByLabel("Action 1 character")).toHaveValue(/.+/);

  await expect(page.getByText("Unsaved changes", { exact: true })).toBeVisible();
  await page.getByRole("button", { name: "Save Changes" }).click();
  await expect(page.getByText("Scene setup saved.", { exact: true })).toBeVisible();

  await page.reload();
  await expect(page.getByRole("button", { name: /Sunny Forest/ })).toHaveAttribute("aria-pressed", "true");
  await expect(page.getByText("Garden Discovery")).toBeVisible();
  await expect(page.getByRole("link", { name: "Open Editor →" })).toHaveAttribute("href", /\/editor\?scene=/);
});

test("renders empty staging sections and read-only outline duration", async ({ page }) => {
  await openSceneSetup(page);
  await expect(page.getByText("No props yet. Add a few scene ingredients above.")).toBeVisible();
  await expect(page.getByText("No dialogue yet. Stage the conversation when you're ready.")).toBeVisible();
  await expect(page.getByText("No action intent yet.")).toBeVisible();
  await expect(page.getByText("Planned duration · 45s · from Outline")).toBeVisible();
  await expect(page.getByText("Planned duration · 45s · from Outline")).toHaveCount(1);
});

test("supports keyboard navigation for background selection", async ({ page }) => {
  await openSceneSetup(page);
  const garden = page.getByRole("button", { name: /Garden/ });
  await garden.focus();
  await page.keyboard.press("Enter");
  await expect(garden).toHaveAttribute("aria-pressed", "true");
});


test("presents the Editor as the central creative workspace", async ({ page }) => {
  await openSceneSetup(page);
  await page.getByRole("button", { name: /Sunny Forest/ }).click();
  await page.getByRole("button", { name: /Milo/ }).filter({ hasText: "Milo" }).first().click();
  await page.getByRole("button", { name: "Save Changes" }).click();
  await expect(page.getByText("Scene setup saved.", { exact: true })).toBeVisible({ timeout: 15000 });
  await page.getByRole("link", { name: "Open Editor →" }).click();

  await expect(page.getByLabel("Story stage")).toBeVisible();
  await expect(page.getByText("Stage · 1920 × 1080")).toBeVisible();
  await expect(page.getByText("Story Characters")).toBeVisible();
  await expect(page.getByText("Reusable Project characters staged in this Story.")).toBeVisible();
  await expect(page.getByRole("heading", { name: "Properties" })).toBeVisible();
  await expect(page.getByText("Coming next · no timing controls in Editor V1")).toBeVisible();
  await expect(page.getByText("Saved", { exact: true })).toBeVisible();
});

test("keeps Editor selection and inspector interactions keyboard accessible", async ({ page }) => {
  await openSceneSetup(page);
  await page.getByRole("button", { name: /Sunny Forest/ }).click();
  await page.getByRole("button", { name: /Milo/ }).filter({ hasText: "Milo" }).first().click();
  await page.getByRole("button", { name: "Save Changes" }).click();
  await expect(page.getByText("Scene setup saved.", { exact: true })).toBeVisible({ timeout: 15000 });
  await page.getByRole("link", { name: "Open Editor →" }).click();

  const object = page.getByRole("button", { name: "Milo, CHARACTER" });
  await object.focus();
  await page.keyboard.press("Enter");
  await expect(object).toHaveAttribute("aria-pressed", "true");
  await expect(page.getByLabel("X")).toBeVisible();
  await expect(page.getByLabel("Y")).toBeVisible();
  await expect(page.getByLabel("Scale")).toBeVisible();

  await page.getByLabel("X").fill("1000");
  await expect(page.getByText("Unsaved changes", { exact: true })).toBeVisible();
  await page.getByRole("button", { name: "Delete object" }).click();
  await expect(page.getByText("Select something on the stage")).toBeVisible();
});

test("keeps the Timeline boundary non-functional and Scene Setup handoff reversible", async ({ page }) => {
  await openSceneSetup(page);
  await page.getByRole("link", { name: "Open Editor →" }).click();
  await expect(page.getByText("Coming next · no timing controls in Editor V1")).toBeVisible();
  await page.getByRole("link", { name: "Back to Scene Setup" }).click();
  await expect(page.getByRole("heading", { name: "Garden Discovery" })).toBeVisible();
});


test("exposes Preview and Render without reopening the frozen Editor workflow", async ({ page }) => {
  await openSceneSetup(page);
  await page.getByRole("link", { name: "Open Editor →" }).click();
  await expect(page.getByRole("link", { name: "Preview" })).toBeVisible();
  await expect(page.getByRole("button", { name: "Render" })).toBeVisible();
  await expect(page.getByText("Coming next · no timing controls in Editor V1")).toBeVisible();
});


test("Preview renders the saved Composition and uses the canonical visual semantics", async ({ page }) => {
  await openSceneSetup(page);
  await page.getByRole("button", { name: /Sunny Forest/ }).click();
  const milo = page.getByRole("button", { name: /Milo/ }).filter({ hasText: "Milo" }).first();
  await milo.click();
  await page.getByRole("button", { name: "+ Wooden Chair" }).click();
  await page.getByRole("button", { name: "Save Changes" }).click();
  await expect(page.getByText("Scene setup saved.", { exact: true })).toBeVisible({ timeout: 15000 });

  await page.getByRole("link", { name: "Open Editor →" }).click();
  await expect(page.getByText("Saved", { exact: true })).toBeVisible();

  const object = page.getByRole("button", { name: "Milo, CHARACTER" });
  await object.click();
  await page.getByLabel("X").fill("960");
  await page.getByLabel("Y").fill("540");
  await page.getByLabel("Scale").fill("2");
  await page.getByRole("button", { name: "Save" }).click();
  await expect(page.getByText("Saved", { exact: true })).toBeVisible();

  await page.getByRole("link", { name: "Preview" }).click();
  await expect(page.getByRole("heading", { name: /Garden Discovery/ })).toBeVisible();
  await expect(page.getByText("Saved Composition · 1.1", { exact: true })).toBeVisible();
  await expect(page.getByText("1920 × 1080 · 30 fps · static preview", { exact: true })).toBeVisible();
  await expect(page.getByLabel("Story Composition preview")).toBeVisible();
  await expect(page.getByLabel(/CHARACTER/)).toHaveCount(1);
  await expect(page.getByText("wooden_chair")).toBeVisible();
});

test("Preview shows a recoverable error when the saved Composition cannot be loaded", async ({ page }) => {
  await page.route("**/api/v1/stories/*/outline-scenes/*/editor", async route => {
    await route.fulfill({
      status: 500,
      contentType: "application/json",
      body: JSON.stringify({ error: { code: "LOAD_FAILED", message: "Saved Composition is temporarily unavailable.", fields: [] } })
    });
  });
  await page.goto("/stories/test-story/preview?scene=test-scene");
  await expect(page.getByRole("heading", { name: "Preview unavailable" })).toBeVisible();
  await expect(page.getByRole("alert").first()).toContainText("Saved Composition is temporarily unavailable.");
  await expect(page.getByRole("link", { name: "Return to Editor" })).toHaveAttribute("href", /\/stories\/test-story\/editor\?scene=test-scene/);
});

test("Editor blocks Preview while Composition edits are unsaved", async ({ page }) => {
  await openSceneSetup(page);
  await page.getByRole("link", { name: "Open Editor →" }).click();
  const object = page.getByRole("button", { name: /Milo, CHARACTER/ });
  await object.click();
  await page.getByLabel("X").fill("1000");
  await expect(page.getByText("Unsaved changes", { exact: true })).toBeVisible();

  const preview = page.getByRole("link", { name: "Preview" });
  await expect(preview).toHaveAttribute("aria-disabled", "true");
  const initialUrl = page.url();
  await preview.click();
  await expect(page).toHaveURL(initialUrl);
});


test("Render produces a real MP4 through the renderer and artifact storage", async ({ page }) => {
  await page.goto("/create");
  await page.getByRole("button", { name: "Start Blank" }).click();
  await page.getByRole("button", { name: /Continue/ }).click();
  await page.getByLabel("Story idea").fill("A rabbit visits a garden.");
  await page.getByRole("button", { name: "Ages 6–8" }).click();
  await page.getByRole("button", { name: "Continue" }).click();
  await page.getByRole("button", { name: "+ Add Scene" }).first().click();
  await page.getByLabel("Scene 1 title").fill("Render Smoke Test");
  await page.getByLabel("Scene 1 summary").fill("A one second render smoke test.");
  await page.getByLabel("Scene 1 duration").fill("1");
  await page.getByRole("button", { name: "Save Changes" }).click();
  await expect(page.getByText("Saved", { exact: true })).toBeVisible({ timeout: 15000 });

  await expect(page.getByRole("link", { name: /Continue to Characters/ })).not.toHaveAttribute("aria-disabled", "true");
  await page.getByRole("link", { name: /Continue to Characters/ }).click();
  await page.getByLabel("Name").fill("Milo");
  await page.getByLabel("Role \/ description").fill("A curious little rabbit");
  await page.getByLabel("Visual description").fill("Small brown rabbit");
  await page.getByRole("button", { name: "Create & Add to Story" }).click();
  await expect(page.getByRole("heading", { name: "Milo" })).toBeVisible();
  await expect(page.getByRole("link", { name: /Continue to Scene Setup/ })).not.toHaveAttribute("aria-disabled", "true");
  await page.getByRole("link", { name: /Continue to Scene Setup/ }).click();
  await page.getByRole("button", { name: /Garden/ }).first().click();
  await page.getByRole("button", { name: /Milo/ }).filter({ hasText: "Milo" }).first().click();
  await page.getByRole("button", { name: "Save Changes" }).click();
  await expect(page.getByText("Scene setup saved.", { exact: true })).toBeVisible({ timeout: 15000 });
  await page.getByRole("link", { name: "Open Editor →" }).click();
  await expect(page.getByText("Saved", { exact: true })).toBeVisible();

  await page.getByRole("button", { name: "Render" }).click();
  await expect(page.getByText(/rendering|queued/i)).toBeVisible({ timeout: 15000 });
  await expect(page.getByText(/completed · 100% · Composition v1/i)).toBeVisible({ timeout: 120000 });

  const openMp4 = page.getByRole("link", { name: "Open MP4" });
  await expect(openMp4).toBeVisible();
  const artifactUrl = await openMp4.getAttribute("href");
  if (!artifactUrl) throw new Error("Render artifact URL missing");
  const response = await page.request.get(new URL(artifactUrl, page.url()).toString());
  expect(response.status()).toBe(200);
  expect(response.headers()["content-type"]).toContain("video/mp4");
  const body = await response.body();
  expect(body.length).toBeGreaterThan(1000);
  expect(body.subarray(4, 8).toString()).toBe("ftyp");
});


test("RenderJob access remains scoped to its Story and Scene", async ({ page }) => {
  await page.goto("/create");
  await page.getByRole("button", { name: "Start Blank" }).click();
  await page.getByRole("button", { name: /Continue/ }).click();
  await page.getByLabel("Story idea").fill("A rabbit renders a garden scene.");
  await page.getByRole("button", { name: "Ages 6–8" }).click();
  await page.getByRole("button", { name: "Continue" }).click();
  await page.getByRole("button", { name: "+ Add Scene" }).first().click();
  await page.getByLabel("Scene 1 title").fill("Scoped Render");
  await page.getByLabel("Scene 1 summary").fill("A render access scope test.");
  await page.getByLabel("Scene 1 duration").fill("1");
  await page.getByRole("button", { name: "Save Changes" }).click();
  await expect(page.getByText("Saved", { exact: true })).toBeVisible({ timeout: 15000 });
  await expect(page.getByRole("link", { name: /Continue to Characters/ })).not.toHaveAttribute("aria-disabled", "true");
  await page.getByRole("link", { name: /Continue to Characters/ }).click();
  await page.getByLabel("Name").fill("Milo");
  await page.getByLabel("Role \/ description").fill("A rabbit");
  await page.getByLabel("Visual description").fill("A small brown rabbit");
  await page.getByRole("button", { name: "Create & Add to Story" }).click();
  await expect(page.getByRole("heading", { name: "Milo" })).toBeVisible();
  await expect(page.getByRole("link", { name: /Continue to Scene Setup/ })).not.toHaveAttribute("aria-disabled", "true");
  await page.getByRole("link", { name: /Continue to Scene Setup/ }).click();
  await page.getByRole("button", { name: /Milo/ }).filter({ hasText: "Milo" }).first().click();
  await page.getByRole("button", { name: "Save Changes" }).click();
  await expect(page.getByText("Scene setup saved.", { exact: true })).toBeVisible({ timeout: 15000 });
  await page.getByRole("link", { name: "Open Editor →" }).click();

  await page.getByRole("button", { name: "Render" }).click();
  await expect(page.getByText(/completed · 100% · Composition v1/i)).toBeVisible({ timeout: 120000 });
  const openMp4 = page.getByRole("link", { name: "Open MP4" });
  const jobArtifact = await openMp4.getAttribute("href");
  if (!jobArtifact) throw new Error("Render artifact URL missing");
  const jobPath = new URL(jobArtifact, page.url()).pathname;
  const wrongScene = await page.request.get(
    new URL(jobPath.replace(/outline-scenes\/[0-9a-f-]+/, "outline-scenes/00000000-0000-0000-0000-000000000000"), page.url()).toString()
  );
  expect(wrongScene.status()).toBe(404);
});
