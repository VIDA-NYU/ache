import com.github.gradle.node.npm.task.NpmTask

plugins {
  id("com.github.node-gradle.node") version "7.1.0"
}

node {
  // Enable automatic download and install local copy of Node/NPM
  download.set(true)
  // Version of Node.js to use.
  version.set("22.18.0")
  // Version of npm to use.
  npmVersion.set("11.5.2")
}

val dashboardBuildTask = tasks.register<NpmTask>("build") {
  dependsOn(tasks.npmInstall)
  npmCommand.set(listOf("run", "build"))
}

val dashboardInstallTask = tasks.register<Copy>("install") {
  dependsOn(dashboardBuildTask)
  from("${projectDir}/build/")
  into("${rootProject.projectDir}/ache/src/main/resources/public/")
}
