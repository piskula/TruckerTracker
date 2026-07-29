val gitHooksDir = File(rootDir, "../.git/hooks")
if (gitHooksDir.exists()) {
    val target = File(gitHooksDir, "pre-commit")
    val source = File(rootDir, "../.githooks/pre-commit")
    if (!target.exists() || target.readText() != source.readText()) {
        source.copyTo(target, overwrite = true)
        target.setExecutable(true)
    }
}
