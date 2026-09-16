# Content Safety Baseline

StorySprout creates content intended for children. Safety is therefore a product requirement, not only an infrastructure concern.

## Initial principles
- Generated and creator-supplied content must be handled with child-appropriate safety in mind.
- Future story, image, voice and other generation workflows must have explicit safety requirements in their feature specifications.
- Do not assume an external AI provider's safety controls alone are sufficient for StorySprout's product behavior.
- Avoid unnecessary collection or transmission of personal data to external providers.
- Treat generated media, prompts, titles, dialogue and metadata as potentially user-controlled input and validate them at appropriate boundaries.
- Safety failures should produce understandable failure states rather than silently publishing or rendering unsafe content.
- Publishing-related safeguards must be specified before any publishing capability is implemented.

## Not implemented yet
No content moderation service, policy engine, parental controls, publishing safety gate, or provider-specific safety integration is implemented by this documentation. Those are future features requiring specifications, acceptance criteria and tests.

This document is an engineering baseline, not a claim of regulatory compliance or certification.
