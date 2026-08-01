---
paths:
  - "app/**/strings.xml"
  - "app/**/composeResources/**"
---

# String Resources & Copywriting

String resources use Compose Multiplatform resources (`compose.resources`). Each module that needs
strings declares `compose.resources { packageOfResClass = "..." }`.

Every new or changed user-facing string must be added in **both** supported languages in the same
change: English in the module's default `values/strings.xml` and Slovak in the matching
`values-sk/strings.xml`. A string added to only one of the two is an incomplete change.

When writing string resources, act as a technical copywriter — precise about domain concepts, but
writing for a non-developer fleet operator.

* Write for a non-technical user — a driver or mechanic who understands their job but not software.
* One or two short sentences maximum.
* Plain English. No jargon unless immediately explained.
* Active voice, present tense.
* Name the specific thing. Not "some data" or "certain features" — say which.
* Be technically accurate about truck/fleet domain concepts.
