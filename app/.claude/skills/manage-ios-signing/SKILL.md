---
name: manage-ios-signing
description: Use when adding/removing an iOS tester device, regenerating the ad-hoc provisioning profile, or renewing the iOS distribution certificate for Firebase App Distribution. Triggered by phrases like "add an iOS tester", "register a new device", "regenerate the provisioning profile", "renew the iOS certificate", "update IOS_PROVISIONING_PROFILE_BASE64", "iOS signing secrets".
---

# Skill: Manage iOS Ad-Hoc Signing (Certificate, Provisioning Profile, GitHub Secrets)

> `build-ios` (`.github/workflows/build-app.yml`) and `release-ios` (`.github/workflows/release-app.yml`), both at the repo root, sign a real `.ipa` using an Apple Distribution certificate + ad-hoc provisioning profile, stored as five GitHub Secrets. This skill covers the two operations that come up again: adding a tester's device, and renewing the certificate.

## Looking up current values

This file intentionally does **not** hardcode the Apple Team ID, Firebase App ID, or provisioning profile name — this repo is public, and those specifics belong in the agent's private memory or the Apple/Firebase consoles, not in a committed file anyone can read. To find them:

- Team ID / cert expiry: Apple Developer portal → Account → Membership; or `security find-certificate` on a machine with the cert imported.
- Firebase iOS App ID: `firebase apps:list --project <project-id>` (bundle ID is `com.momosi.trucktrack`, fixed and non-sensitive — it's embedded in the shipped app anyway).
- Which/how many devices the current ad-hoc profile covers: Apple Developer portal → Profiles → open the profile, or decode it locally (`security cms -D -i profile.mobileprovision`, never in CI logs — see "Public repo" section below).
- Existing secret names (not values): `gh secret list --repo piskula/TruckerTracker`.

Ad-hoc profiles cap at 100 devices/year total; every device added means editing the profile and re-downloading it.

## The 5 secrets involved

| Secret | Changes when... |
|---|---|
| `IOS_TEAM_ID` | Never (unless you switch Apple Developer accounts) |
| `FIREBASE_IOS_APP_ID` | Never (unless the Firebase app is re-registered) |
| `IOS_DISTRIBUTION_CERTIFICATE_BASE64` | Certificate renewal only (yearly) |
| `IOS_DISTRIBUTION_CERTIFICATE_PASSWORD` | Same time as the cert above |
| `IOS_PROVISIONING_PROFILE_BASE64` | **Every time a tester device is added/removed** |

Adding a tester only touches the last one. You do **not** need to regenerate the certificate to add a device.

## Adding a new tester device

1. Get the tester's iPhone UDID. On Windows: connect the iPhone via USB, open iTunes, go to the device summary page, click the serial number once (cycles to show the UDID), right-click → Copy. On a Mac: Xcode → Window → Devices and Simulators.
2. Apple Developer portal → **Certificates, Identifiers & Profiles → Devices → +** → register the UDID.
3. **Profiles** → find the existing ad-hoc profile (or create a new one: **+** → **Ad Hoc** → App ID `com.momosi.trucktrack` → the Apple Distribution cert → select **all** devices that should be covered, including the new one) → download the `.mobileprovision`.
4. Also invite the tester's email to both Firebase App Distribution tester groups (`internal-testers`, `release` — these are project-level, shared across Android/iOS). Find the Firebase project ID with `firebase projects:list` if you don't have it handy:
   ```bash
   firebase appdistribution:testers:add <email> --group-alias internal-testers --project <firebase-project-id>
   firebase appdistribution:testers:add <email> --group-alias release --project <firebase-project-id>
   ```
5. Base64-encode and update the secret — **see "Uploading secrets safely" below, this step has bitten us twice.**

## Renewing the certificate (only when it expires, ~yearly)

1. Generate a CSR (works fine on Windows via Git Bash, no Mac needed — the leading-`/subj` MSYS path-mangling gotcha requires `MSYS_NO_PATHCONV=1`):
   ```bash
   openssl genrsa -out ios_distribution.key 2048
   MSYS_NO_PATHCONV=1 openssl req -new -key ios_distribution.key -out ios_distribution.csr \
     -subj "/emailAddress=you@example.com/CN=Your Name/C=XX"
   ```
2. Apple Developer portal → **Certificates → +** → **Apple Distribution** → upload the `.csr` → download the resulting `.cer`.
3. Convert and bundle into a `.p12` — **this is the step that broke twice.** OpenSSL 3.x (this machine has 3.2.1) defaults to AES-256/SHA-256 PKCS12 encryption, which macOS's Keychain Services `security import` (used in CI, `macos-15` runners) **cannot parse** — it fails with the cryptic `Cannot parse a NULL or zero-length data`, even though the file decodes byte-for-byte correctly from the GitHub secret. You must force the legacy RC2/3DES + SHA1 format explicitly:
   ```bash
   openssl x509 -inform DER -in distribution.cer -out distribution.pem

   openssl pkcs12 -export -legacy \
     -inkey ios_distribution.key \
     -in distribution.pem \
     -out ios_distribution.p12 \
     -name "iOS Distribution" \
     -certpbe PBE-SHA1-3DES \
     -keypbe PBE-SHA1-3DES \
     -macalg sha1 \
     -password "pass:<a-generated-password>"
   ```
   Sanity-check before uploading: `openssl pkcs12 -info -in ios_distribution.p12 -passin "pass:..." -noout` should show `pbeWithSHA1And3-KeyTripleDES-CBC` for both bags and `MAC: sha1` — if it shows AES, the `-legacy`/`-certpbe`/`-keypbe` flags didn't take (check the OpenSSL install has the legacy provider available).
4. Base64-encode and update **both** `IOS_DISTRIBUTION_CERTIFICATE_BASE64` and `IOS_DISTRIBUTION_CERTIFICATE_PASSWORD`.
5. The old certificate's devices/profile don't carry over automatically — you'll likely need a new ad-hoc profile referencing the new certificate too (see "Adding a new tester" above, step 3).

## Uploading secrets safely (the other thing that broke)

**Never** pipe a secret value through PowerShell to `gh secret set` (`Get-Content -Raw | gh secret set NAME`) — Windows PowerShell 5.1's text pipeline to a native process re-encodes the string, which silently corrupted a 16KB base64 payload into invalid data (decoded to `error decoding base64 input stream` in CI) even though the source file was verified correct. Always use Bash with **raw file redirection**, which passes bytes through untouched:

```bash
gh secret set IOS_PROVISIONING_PROFILE_BASE64 --repo piskula/TruckerTracker < profile.mobileprovision.base64.txt
```

Watch trailing newlines too: `echo` and some `Get-Content` paths append one, which is invisible but changes the exact byte sequence gh uploads — this broke `IOS_DISTRIBUTION_CERTIFICATE_PASSWORD` (25 bytes uploaded vs. 24-byte actual password) even though both round-tripped visually the same. When piping a password/short value, use `printf '%s'` instead of `echo`, or strip the trailing newline explicitly:
```bash
printf '%s' "$(cat p12_password.txt)" | gh secret set IOS_DISTRIBUTION_CERTIFICATE_PASSWORD --repo piskula/TruckerTracker
```

Base64-encode with GNU `base64 -w0 file > file.b64` (single line, no wrapping) on this machine — that's fine for encoding. **Decoding happens in CI on macOS**, where `/usr/bin/base64` is the BSD variant and needs `-D`, not GNU's `--decode` long flag (both builds `.github/workflows/*.yml` already use `-D` — don't regress this if editing those files).

After updating any secret, verify with a fresh CI run before assuming it worked — `gh workflow run build-app.yml --repo piskula/TruckerTracker --ref main` (uses the `workflow_dispatch` trigger, no throwaway commit needed) and watch the `build-ios` job.

## Public repo — logging discipline

`piskula/TruckerTracker` is a **public** repository, so Actions logs are visible to anyone, not just collaborators. If you add debug output while troubleshooting this pipeline again:
- Byte counts, `file` type output, variable *lengths* (`${#VAR}`), and exit codes are safe.
- **Never** dump decoded certificate/profile file *content* (e.g. `head`/`cat` on the decoded `.p12` or `.mobileprovision`/plist) — the profile plist contains the tester's device UDID and profile display name (often a real name), and dumping it leaks that publicly. `security cms -D -i profile.mobileprovision` output is exactly this risk.
- GitHub auto-masks log text that exactly matches a registered secret value, but that only protects the literal secret string — it does nothing for derived content like a decoded plist's fields. Don't rely on masking; just don't print that content in the first place.
- Remove any temporary `DEBUG` lines once a fix is confirmed working — don't leave diagnostic scaffolding in the workflow permanently.
