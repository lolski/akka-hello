java_library(
    name = "job",
    srcs = ["Job.java", "Message.java"],
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
        "job",
        "//dependencies/maven/artifacts/com/typesafe/akka:akka-actor-2-13",
        "//dependencies/maven/artifacts/com/typesafe/akka:akka-actor-typed-2-13",
        "//dependencies/maven/artifacts/com/typesafe/akka:akka-actor-testkit-typed-2-13",
        "//dependencies/maven/artifacts/junit:junit",
    ],
)