tasks.register<JavaExec>("runDemo") {
    dependsOn(":demo:run")
    group = "application"
    description = "Runs the demo application."
    workingDir = file("demo/example")
}
