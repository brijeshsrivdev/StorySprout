package com.storysprout.api.story;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoryService {
    private static final Set<String> AGES = Set.of("3_5", "6_8", "9_12");
    private static final Set<Integer> DURATIONS = Set.of(1, 3, 5);
    private static final Set<String> STYLES = Set.of("2D", "3D", "HYBRID");
    private static final Set<String> LANGUAGES = Set.of("ENGLISH", "HINDI");

    private final ProjectRepository projects;
    private final StoryRepository stories;
    private final StoryGenerator generator;

    public StoryService(ProjectRepository projects, StoryRepository stories, StoryGenerator generator) {
        this.projects = projects; this.stories = stories; this.generator = generator;
    }

    @Transactional
    public Project createProject(ProjectCreateRequest request) {
        String name = request.name().trim();
        if (name.isBlank()) throw new StoryValidationException("Project name is required");
        Instant now = Instant.now();
        return projects.insert(new Project(UUID.randomUUID(), name, ProjectStatus.DRAFT, now, now));
    }

    @Transactional(readOnly = true)
    public java.util.List<Project> listProjects() { return projects.findAllByUpdatedDesc(); }

    @Transactional
    public StoryCreationResult createStory(UUID projectId, StoryCreateRequest request) {
        projects.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
        validate(request);
        Instant now = Instant.now();
        Story story = new Story(UUID.randomUUID(), projectId, "Untitled Story", request.idea().trim(), request.targetAge(), request.durationMinutes(), request.visualStyle(), request.language(), request.creationMode(), request.creationMode() == StoryCreationMode.AI ? StoryGenerationStatus.GENERATING : StoryGenerationStatus.NOT_REQUESTED, null, now, now);
        stories.insert(story);
        projects.touch(projectId, now);

        if (request.creationMode() == StoryCreationMode.BLANK) return new StoryCreationResult(story, null);
        try {
            StoryGenerationResult generated = generator.generate(new StoryGenerationRequest(request.idea().trim(), request.targetAge(), request.durationMinutes(), request.visualStyle(), request.language()));
            Story completed = stories.updateGeneration(story.id(), StoryGenerationStatus.COMPLETED, generated.generatedTitle(), generated.generatedDraft());
            return new StoryCreationResult(completed, null);
        } catch (StoryGenerationException ex) {
            Story failed = stories.updateGeneration(story.id(), StoryGenerationStatus.FAILED, null, null);
            return new StoryCreationResult(failed, ex.getMessage());
        }
    }

    private void validate(StoryCreateRequest request) {
        if (request.idea() == null || request.idea().isBlank()) throw new StoryValidationException("Story idea is required");
        if (!AGES.contains(request.targetAge())) throw new StoryValidationException("Target age must be 3_5, 6_8, or 9_12");
        if (!DURATIONS.contains(request.durationMinutes())) throw new StoryValidationException("Duration must be 1, 3, or 5 minutes");
        if (!STYLES.contains(request.visualStyle())) throw new StoryValidationException("Visual style must be 2D, 3D, or HYBRID");
        if (!LANGUAGES.contains(request.language())) throw new StoryValidationException("Language must be ENGLISH or HINDI");
    }
}
