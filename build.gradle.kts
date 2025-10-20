tasks.register("runDemo") {
    dependsOn(":demo:run")
    group = "application"
    description = "Runs the demo application."
}
