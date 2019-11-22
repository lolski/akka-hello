
java_library(
    name = "automation",
    srcs = ["Pipeline.java", "Workflow.java", "Job.java"],
    deps = [
        "//dependencies/maven/artifacts/com/typesafe/akka:akka-actor-2-13",
        "//dependencies/maven/artifacts/com/typesafe/akka:akka-actor-typed-2-13",
        "//dependencies/maven/artifacts/org/zeroturnaround:zt-exec",
    ],
)

java_test(
    name = "job-test",
    test_class = "JobTest",
    srcs = ["JobTest.java"],
    deps = [
        ":automation",
        "//dependencies/maven/artifacts/com/typesafe/akka:akka-actor-2-13",
        "//dependencies/maven/artifacts/com/typesafe/akka:akka-actor-typed-2-13",
        "//dependencies/maven/artifacts/com/typesafe/akka:akka-actor-testkit-typed-2-13",
        "//dependencies/maven/artifacts/junit:junit",
    ],
)

java_test(
    name = "worflow-test",
    test_class = "WorkflowTest",
    srcs = ["WorkflowTest.java"],
    deps = [
        ":automation",
        "//dependencies/maven/artifacts/com/typesafe/akka:akka-actor-2-13",
        "//dependencies/maven/artifacts/com/typesafe/akka:akka-actor-typed-2-13",
        "//dependencies/maven/artifacts/com/typesafe/akka:akka-actor-testkit-typed-2-13",
        "//dependencies/maven/artifacts/junit:junit",
    ],
)