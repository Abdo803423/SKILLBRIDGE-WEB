// Top-level build file for SkillBridge unified Web, Desktop & Android ecosystem.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.secrets) apply false
    alias(libs.plugins.google.services) apply false
}

val ensureNodeModules = tasks.register<Exec>("ensureNodeModules") {
    group = "build"
    description = "Installs Node dependencies when node_modules is missing."
    commandLine("sh", "-c", "[ -x node_modules/.bin/vite ] || npm ci --no-audit --no-fund")
}

tasks.register<Exec>("assembleDebug") {
    group = "build"
    description = "Builds the SkillBridge Web, Mobile PWA & Desktop bundle via Vite."
    dependsOn(ensureNodeModules)
    commandLine("npm", "run", "build")
}

tasks.register<Exec>("lint") {
    group = "verification"
    description = "Runs ESLint (including Firebase security rules) and TypeScript type-checking."
    dependsOn(ensureNodeModules)
    commandLine("npm", "run", "lint")
}

tasks.register<Delete>("clean") {
    group = "build"
    delete("dist")
}
