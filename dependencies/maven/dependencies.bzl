# Do not edit. bazel-deps autogenerates this file from dependencies/maven/dependencies.yaml.
def _jar_artifact_impl(ctx):
    jar_name = "%s.jar" % ctx.name
    ctx.download(
        output=ctx.path("jar/%s" % jar_name),
        url=ctx.attr.urls,
        sha256=ctx.attr.sha256,
        executable=False
    )
    src_name="%s-sources.jar" % ctx.name
    srcjar_attr=""
    has_sources = len(ctx.attr.src_urls) != 0
    if has_sources:
        ctx.download(
            output=ctx.path("jar/%s" % src_name),
            url=ctx.attr.src_urls,
            sha256=ctx.attr.src_sha256,
            executable=False
        )
        srcjar_attr ='\n    srcjar = ":%s",' % src_name

    build_file_contents = """
package(default_visibility = ['//visibility:public'])
java_import(
    name = 'jar',
    tags = ['maven_coordinates={artifact}'],
    jars = ['{jar_name}'],{srcjar_attr}
)
filegroup(
    name = 'file',
    srcs = [
        '{jar_name}',
        '{src_name}'
    ],
    visibility = ['//visibility:public']
)\n""".format(artifact = ctx.attr.artifact, jar_name = jar_name, src_name = src_name, srcjar_attr = srcjar_attr)
    ctx.file(ctx.path("jar/BUILD"), build_file_contents, False)
    return None

jar_artifact = repository_rule(
    attrs = {
        "artifact": attr.string(mandatory = True),
        "sha256": attr.string(mandatory = True),
        "urls": attr.string_list(mandatory = True),
        "src_sha256": attr.string(mandatory = False, default=""),
        "src_urls": attr.string_list(mandatory = False, default=[]),
    },
    implementation = _jar_artifact_impl
)

def jar_artifact_callback(hash):
    src_urls = []
    src_sha256 = ""
    source=hash.get("source", None)
    if source != None:
        src_urls = [source["url"]]
        src_sha256 = source["sha256"]
    jar_artifact(
        artifact = hash["artifact"],
        name = hash["name"],
        urls = [hash["url"]],
        sha256 = hash["sha256"],
        src_urls = src_urls,
        src_sha256 = src_sha256
    )
    native.bind(name = hash["bind"], actual = hash["actual"])


def list_dependencies():
    return [
    {"artifact": "com.typesafe.akka:akka-actor-typed_2.13:2.6.0-RC2", "lang": "java", "sha1": "54cc8e0584d51291d30091ff44b07960fa2e8d20", "sha256": "8db981a3186a35bf5c835d3563f28f4ff22c736fb4deece9022fea709228abfc", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/com/typesafe/akka/akka-actor-typed_2.13/2.6.0-RC2/akka-actor-typed_2.13-2.6.0-RC2.jar", "source": {"sha1": "7c8d9d6b1e4e8f9dc9d44fae0ada534c24e005b2", "sha256": "a6399822eb08ebf6335a99f2163e55c312cdc9766fab5ce7f877bafc56acdfb9", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/com/typesafe/akka/akka-actor-typed_2.13/2.6.0-RC2/akka-actor-typed_2.13-2.6.0-RC2-sources.jar"} , "name": "com-typesafe-akka-akka-actor-typed_2-13", "actual": "@com-typesafe-akka-akka-actor-typed_2-13//jar", "bind": "jar/com/typesafe/akka/akka-actor-typed-2-13"},
    {"artifact": "com.typesafe.akka:akka-actor_2.13:2.6.0-RC2", "lang": "java", "sha1": "f07395b89006d1edfcbafc7249ad4b60d20f1ebe", "sha256": "44163060e00f92b8297fccd308392e1cdebaa56c4bfe34f2a73611ad04cea8ee", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/com/typesafe/akka/akka-actor_2.13/2.6.0-RC2/akka-actor_2.13-2.6.0-RC2.jar", "source": {"sha1": "d9af038e641a59d96ef822c70cbb232eec4f9ec7", "sha256": "a8c1aacf545b451a403adf7417966b74a76dae58e90cfdb54bc17f46f8dccac7", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/com/typesafe/akka/akka-actor_2.13/2.6.0-RC2/akka-actor_2.13-2.6.0-RC2-sources.jar"} , "name": "com-typesafe-akka-akka-actor_2-13", "actual": "@com-typesafe-akka-akka-actor_2-13//jar", "bind": "jar/com/typesafe/akka/akka-actor-2-13"},
    {"artifact": "com.typesafe:config:1.4.0", "lang": "java", "sha1": "a8b341fe81552834edc231193afd6f56a96f0eff", "sha256": "aadbfd5a524551beef10d3f891d305b83bb27d54703d9a4de7aca2a12d9847e2", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/com/typesafe/config/1.4.0/config-1.4.0.jar", "source": {"sha1": "e748b8348e1910b0935bf662e21e748290229e7f", "sha256": "ffaf8892dc8c61605bd7319c6cdcea022b6c9c28b62776915a809e8de93d8a6e", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/com/typesafe/config/1.4.0/config-1.4.0-sources.jar"} , "name": "com-typesafe-config", "actual": "@com-typesafe-config//jar", "bind": "jar/com/typesafe/config"},
    {"artifact": "org.scala-lang.modules:scala-java8-compat_2.13:0.9.0", "lang": "java", "sha1": "cecd75f8db95161dda4b6ea226c964f6df4a6d8e", "sha256": "42636d2c772f20b2fa6e8be5b461564d7e51c067895b6689711de7d08d1f79cb", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/scala-lang/modules/scala-java8-compat_2.13/0.9.0/scala-java8-compat_2.13-0.9.0.jar", "source": {"sha1": "54cfdbda326cd09baba9b72cdbf7e1d3fc570ca8", "sha256": "6782efc4ffda1864665c6db044ef5d4c2de8a60d39abe1c50f07bca8c34b5b2c", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/scala-lang/modules/scala-java8-compat_2.13/0.9.0/scala-java8-compat_2.13-0.9.0-sources.jar"} , "name": "org-scala-lang-modules-scala-java8-compat_2-13", "actual": "@org-scala-lang-modules-scala-java8-compat_2-13//jar", "bind": "jar/org/scala-lang/modules/scala-java8-compat-2-13"},
    {"artifact": "org.scala-lang:scala-library:2.13.0", "lang": "java", "sha1": "e988ea597113786768900f1c260c694a2ae32a42", "sha256": "bd3b2fa8b922295ccf1537aba1850d455f82003c10484df509d29ff177cc1edc", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/scala-lang/scala-library/2.13.0/scala-library-2.13.0.jar", "source": {"sha1": "80a99c2699c361524e0eb51ebbcce55bbeda6689", "sha256": "4cd39b6e9ec0c5b28bf0b62a841e7aaf16d5728d4e014081a64e5eca0bf6b722", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/scala-lang/scala-library/2.13.0/scala-library-2.13.0-sources.jar"} , "name": "org-scala-lang-scala-library", "actual": "@org-scala-lang-scala-library//jar", "bind": "jar/org/scala-lang/scala-library"},
    {"artifact": "org.slf4j:slf4j-api:1.7.28", "lang": "java", "sha1": "2cd9b264f76e3d087ee21bfc99305928e1bdb443", "sha256": "fb6e4f67a2a4689e3e713584db17a5d1090c1ebe6eec30e9e0349a6ee118141e", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/slf4j/slf4j-api/1.7.28/slf4j-api-1.7.28.jar", "source": {"sha1": "6444f3c8fce32e20f621e264807256c5e65f11c9", "sha256": "b1b8bfa4f2709684606001685d09ef905adc1b72ec53444ade90f44bfbcebcff", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/slf4j/slf4j-api/1.7.28/slf4j-api-1.7.28-sources.jar"} , "name": "org-slf4j-slf4j-api", "actual": "@org-slf4j-slf4j-api//jar", "bind": "jar/org/slf4j/slf4j-api"},
    ]

def maven_dependencies(callback = jar_artifact_callback):
    for hash in list_dependencies():
        callback(hash)
