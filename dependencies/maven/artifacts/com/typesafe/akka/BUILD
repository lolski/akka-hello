java_library(
    name = "akka-actor-2-13",
    exports = [
        "//external:jar/com/typesafe/akka/akka-actor-2-13"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/com/typesafe:config",
        "//dependencies/maven/artifacts/org/scala-lang/modules:scala-java8-compat-2-13",
        "//dependencies/maven/artifacts/org/scala-lang:scala-library"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "akka-actor-typed-2-13",
    exports = [
        "//external:jar/com/typesafe/akka/akka-actor-typed-2-13"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/org/scala-lang:scala-library",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
        ":akka-actor-2-13"
    ],
    visibility = [
        "//visibility:public"
    ]
)


