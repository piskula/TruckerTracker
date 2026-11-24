---
paths:
  - "app/**"
---

# TruckTrack Illustrations

Use the notification-permission illustration from [PR #85](https://github.com/piskula/TruckerTracker/pull/85)
as the visual authority for product illustrations: permission requests, empty states, and full-screen
errors. Extend that family rather than introducing a new illustration style.

## References

The original is `notification_permission_illustration.xml` in
`app/feature/notification-permission/impl/src/commonMain/composeResources/drawable/` on PR #85.
Until that feature is merged, retrieve it from the PR rather than assuming it exists locally.
The error illustrations in `app/core/ui-library/src/commonMain/composeResources/drawable/`
are local examples of the same family.

Inspect the original or a local family member before creating an illustration. When using an image
generation tool, supply that artwork as an actual image reference, not just this written description.
Do not silently substitute another aesthetic if the reference cannot be loaded.

## Visual Language

- Flat, friendly vector scenes with rounded geometry and generous negative space.
- Use a square 240 x 240 canvas and transparent outer margins for this family.
  A pale-blue circular backdrop is an established option, not a required template.
- Deep-teal main objects (`#00526D`, `#006687`), pale blue (`#BDE9FF`, `#E6F4FB`),
  white surfaces, and muted blue-gray details (`#9DBBC8`, `#C7D9E1`).
- A small amber accent (`#A64F00`) for an attention or recovery cue; green (`#006D3C`)
  only when communicating success.
- Choose a composition and subject that explain the specific state. A phone, clipboard, truck,
  road, disconnected cable, or another relevant scene can be completely different from the reference.
  Keep the palette, flat geometry, rounded forms, visual density, and calm tone consistent.
- Do not repeatedly reuse the same phone scene with a different tiny symbol. A truck detail,
  circular badge, dots, and ground ellipse are optional, not mandatory ingredients.
- Minimal rounded strokes, a restrained translucent ground ellipse, and at most a few background dots.
- No gradients, photorealism, 3D rendering, textures, heavy outlines, dramatic shadows, emoji,
  faces, embedded text, logos, licence plates, or environment-specific identifiers.

Errors should feel calm and recoverable, not alarming or playful at the user's expense.
Keep offline and generic-failure scenes visually distinct; do not imply that every failure is offline.

## What Makes an Illustration Strong

Match the reference's visual storytelling, not just its colors. Aim for a small, composed scene,
not an enlarged interface icon.

- **Clear hierarchy:** choose one dominant subject. Make the state cue smaller and subordinate,
  with contextual details third in importance. In the reference, the phone leads, the bell explains
  the purpose, and the tiny truck grounds it in the product. Do not let the subject and state symbol
  compete at equal visual weight.
- **Depth through composition:** use selective overlap, layering, and clear foreground/background
  relationships rather than gradients or 3D effects. A smaller state cue overlapping the main
  subject can feel more cohesive than a large symbol floating above it.
- **Visual rhythm:** vary shape sizes and spacing. Combine a strong silhouette with a few
  meaningful, softly drawn details; do not flatten everything into equally simple blocks or add
  decoration just to fill space.
- **Optical balance, not forced symmetry:** center individual symbols cleanly inside their
  containers, but balance the whole scene by visual weight. Intentional asymmetry can feel more
  natural than stacking every element on one vertical axis. Symmetry is a choice, not a quality
  requirement.
- **Recognizable subjects:** retain the proportions and distinctive details that make a truck
  read as a truck, not a bus or generic vehicle. Simplification must preserve recognition at the
  actual display size.
- **One coherent scene:** integrate the state cue with the subject instead of assembling two
  unrelated icons. Keep the meaning immediate without making every illustration the same scene
  with a swapped badge.

Before accepting artwork, compare it beside the notification reference at the intended display
size. Check what draws the eye first, whether the state reads immediately, whether overlaps and
spacing feel deliberate, and whether the subject remains recognizable. A palette match alone is
not enough.

## Reusable Generation Prompt

> Create a TruckTrack product illustration for [STATE]. Match the supplied notification-permission
> reference as one coherent illustration family: flat vector geometry, rounded forms, deep-teal
> objects, white surfaces, pale-blue supporting shapes, muted blue-gray details, and restrained
> amber accents. Illustrate [SCENE] so the meaning reads at 200 dp. The subject and composition
> may be completely different from the reference: match its style, not its layout or tiny symbols.
> Build a composed scene with one dominant subject, a smaller integrated state cue, selective
> overlap, varied shape sizes, and a few meaningful details. Balance visual weight naturally;
> do not force symmetry or stack equally prominent icons. Keep the subject recognizable when small.
> Compose on a 240 x 240 square with generous margins and transparent outer corners.
> No text, lettering, branding, gradients, texture, photorealism, or 3D effects.

Replace the bracketed concepts; retain the style constraints. Prefer authored vector artwork for
simple geometric scenes like the reference. If raster generation is needed, request a transparent
background and inspect the actual output before integrating it.

## Integration

- Put all product illustrations in `:core:ui-library`'s `commonMain/composeResources/drawable/`
  and expose them through `TruckTrackIllustrations`, including artwork used by only one feature.
  Name their resource files with an `_illustration` suffix so automated checks can distinguish them
  from photography such as the sign-in hero.
- Prefer Compose Multiplatform vector XML with literal colors and supported paths/groups.
  Do not reference Android-only resources or add a runtime image-loading dependency.
- Access every illustration through `TruckTrackIllustrations`, never directly through `Res.drawable`.
  Only `illustrations/TruckTrackIllustrations.kt` may reference illustration drawable resources. For example, render
  `painterResource(TruckTrackIllustrations.NoConnection)`, not
  `painterResource(Res.drawable.error_no_connection_illustration)`.
- Render with `Image` and `painterResource`, without tinting the artwork.
- Keep the catalogs type-consistent: `TruckTrackIcons` exposes `ImageVector` icons;
  `TruckTrackIllustrations` exposes `DrawableResource` illustrations. Neither is a filesystem path.
- The client Detekt rule `TruckTrack > UseTruckTrackIllustrations` enforces the drawable-access boundary.
  Photography is outside this illustration family and does not have to move into the illustration catalog.
- Treat illustrations as decorative (`contentDescription = null`) when adjacent localized text
  already explains the state. Keep the error message and recovery action as real UI, not image text.
- Preserve existing failure classification, localized copy, callbacks, and test tags.
- Check both light and dark surfaces, narrow screens, large text, and short/landscape windows.
  Keep the recovery action reachable by scrolling when needed.
- Start with one illustration asset for both themes. Compare it on the actual light and dark
  app surface colors before adding variants. Only add a dark palette variant if readability or
  excessive brightness warrants it; preserve the same shapes and composition.
