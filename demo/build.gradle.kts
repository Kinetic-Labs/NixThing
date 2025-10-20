plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":nix-thing"))
}

application {
    mainClass = "com.github.kinetic.nixthing.demo.Main"
}
