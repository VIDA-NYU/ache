import com.github.gradle.node.npm.task.NpmTask

plugins {
  id("com.github.node-gradle.node") version "3.5.1"
}

node {
  // Enable automatic download and install local copy of Node/NPM
  download.set(true)
  // Version of Node.js to use.
  version.set("18.14.2")
  // Version of npm to use.
  npmVersion.set("9.5.0")
}

val dashboardBuildTask = tasks.register<NpmTask>("build") {
  dependsOn(tasks.npmInstall)
  npmCommand.set(listOf("run", "build"))
}

val dashboardInstallTask = tasks.register<Copy>("install") {
  from("${projectDir}/build/")
  into("${rootProject.projectDir}/ache/src/main/resources/public/")
}
