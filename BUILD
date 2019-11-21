java_library(
    name = "pipeline-factory",
    srcs = ["PipelineFactory.java", "Pipeline.java", "Workflow.java", "Job.java", "Message.java"],
    deps = [
        "//dependencies/maven/artifacts/com/typesafe/akka:akka-actor-2-13",
        "//dependencies/maven/artifacts/com/typesafe/akka:akka-actor-typed-2-13",
        "//dependencies/maven/artifacts/org/zeroturnaround:zt-exec",
    ],
)

java_test(
    name = "pipeline-factory-test",
    test_class = "PipelineFactoryTest",
    srcs = ["PipelineFactoryTest.java"],
    deps = [
        "pipeline-factory",
        "//dependencies/maven/artifacts/com/typesafe/akka:akka-actor-2-13",
        "//dependencies/maven/artifacts/com/typesafe/akka:akka-actor-typed-2-13",
        "//dependencies/maven/artifacts/com/typesafe/akka:akka-actor-testkit-typed-2-13",
        "//dependencies/maven/artifacts/junit:junit",
    ],
)