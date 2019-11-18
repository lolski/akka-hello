java_binary(
    name = "akka-hello",
    main_class = "Main",
    srcs = ["Main.java", "Workflow.java", "Job.java", "Message.java"],
    deps = [
        "//dependencies/maven/artifacts/com/typesafe/akka:akka-actor-typed-2-13",
        "//dependencies/maven/artifacts/com/typesafe/akka:akka-actor-2-13",
        "//dependencies/maven/artifacts/org/zeroturnaround:zt-exec",
    ],
)