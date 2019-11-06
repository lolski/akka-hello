load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_jar")

def graknlabs_bazel_deps():
    http_jar(
        name = "bazel_deps",
        urls = ["https://github.com/graknlabs/bazel-deps/releases/download/0.3/grakn-bazel-deps-0.3.jar"],
    )