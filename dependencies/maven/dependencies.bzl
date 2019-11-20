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
    {"artifact": "com.typesafe.akka:akka-actor-testkit-typed_2.13:2.6.0", "lang": "java", "sha1": "99206922c3bd7ed9a3337602c2036227880d2fe6", "sha256": "7514ad1473fbc9311017c1515b4f231bd77cc4b014b4276e5da2e17cff69378d", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/com/typesafe/akka/akka-actor-testkit-typed_2.13/2.6.0/akka-actor-testkit-typed_2.13-2.6.0.jar", "source": {"sha1": "6d795a534ff9f592c518b38130d1ae287a8bc144", "sha256": "47ec49bb14ab187654ae07b08189504dcfe740d9769136a65e934f67a4db1167", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/com/typesafe/akka/akka-actor-testkit-typed_2.13/2.6.0/akka-actor-testkit-typed_2.13-2.6.0-sources.jar"} , "name": "com-typesafe-akka-akka-actor-testkit-typed_2-13", "actual": "@com-typesafe-akka-akka-actor-testkit-typed_2-13//jar", "bind": "jar/com/typesafe/akka/akka-actor-testkit-typed-2-13"},
    {"artifact": "com.typesafe.akka:akka-actor-typed_2.13:2.6.0", "lang": "java", "sha1": "d07df11cc0f028e9c3332bb016126f215d21ab68", "sha256": "46a3296b9a2fea3e6dc3de63d4af5ca24a82a23af8f446b9de8b54d7a41ae28e", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/com/typesafe/akka/akka-actor-typed_2.13/2.6.0/akka-actor-typed_2.13-2.6.0.jar", "source": {"sha1": "51b4ed7233c91dc8811f44bb9f4c9a4f2039f232", "sha256": "401c2d4a47dd9a76de8dcb09313b7d95d5dda42d703bfbef56ff75d99083f29c", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/com/typesafe/akka/akka-actor-typed_2.13/2.6.0/akka-actor-typed_2.13-2.6.0-sources.jar"} , "name": "com-typesafe-akka-akka-actor-typed_2-13", "actual": "@com-typesafe-akka-akka-actor-typed_2-13//jar", "bind": "jar/com/typesafe/akka/akka-actor-typed-2-13"},
    {"artifact": "com.typesafe.akka:akka-actor_2.13:2.6.0", "lang": "java", "sha1": "0bda861175835e18d77f550cd1aababb5d2faf8a", "sha256": "38600d882fd0d912c25c25131d3c21230dd2a9d6ccb50069127468a3b289f2fe", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/com/typesafe/akka/akka-actor_2.13/2.6.0/akka-actor_2.13-2.6.0.jar", "source": {"sha1": "c084fdd04861b17609afd8302b0f85a16af2ef2f", "sha256": "16c2c5c5c8440b3d2f42bd531e1ae8e9122e786907e31e1ab6368e56324e5123", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/com/typesafe/akka/akka-actor_2.13/2.6.0/akka-actor_2.13-2.6.0-sources.jar"} , "name": "com-typesafe-akka-akka-actor_2-13", "actual": "@com-typesafe-akka-akka-actor_2-13//jar", "bind": "jar/com/typesafe/akka/akka-actor-2-13"},
    {"artifact": "com.typesafe.akka:akka-slf4j_2.13:2.6.0", "lang": "java", "sha1": "fe604ac7ee8d1b870f46c705ae74c1b7867698bc", "sha256": "70830eb1a5cadf6f6d43db6ca735b92c1707abac19862260549edc76a76afc99", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/com/typesafe/akka/akka-slf4j_2.13/2.6.0/akka-slf4j_2.13-2.6.0.jar", "source": {"sha1": "0fa8b738eb9f7ac4df70e08b6eab204879b38603", "sha256": "cd9df3c3962eb46889dcb02c1d4590809e70c5be1decd1e33e6625b1d5f2a8d4", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/com/typesafe/akka/akka-slf4j_2.13/2.6.0/akka-slf4j_2.13-2.6.0-sources.jar"} , "name": "com-typesafe-akka-akka-slf4j_2-13", "actual": "@com-typesafe-akka-akka-slf4j_2-13//jar", "bind": "jar/com/typesafe/akka/akka-slf4j-2-13"},
    {"artifact": "com.typesafe.akka:akka-testkit_2.13:2.6.0", "lang": "java", "sha1": "6d05b20703459f2af0c5bb56f181869575d372c8", "sha256": "86e80afb6b18a4b9b0610ed0cba89ef9ff781bd178a28f593de1c037adfe8ae4", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/com/typesafe/akka/akka-testkit_2.13/2.6.0/akka-testkit_2.13-2.6.0.jar", "source": {"sha1": "40d6a56c492c5d6c928d72dc7c562cdac9e978cb", "sha256": "b5673f01c8d0792b473df4ffc4b4c796ea539663698dc58d9d0f0768ede73b3c", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/com/typesafe/akka/akka-testkit_2.13/2.6.0/akka-testkit_2.13-2.6.0-sources.jar"} , "name": "com-typesafe-akka-akka-testkit_2-13", "actual": "@com-typesafe-akka-akka-testkit_2-13//jar", "bind": "jar/com/typesafe/akka/akka-testkit-2-13"},
    {"artifact": "com.typesafe:config:1.4.0", "lang": "java", "sha1": "a8b341fe81552834edc231193afd6f56a96f0eff", "sha256": "aadbfd5a524551beef10d3f891d305b83bb27d54703d9a4de7aca2a12d9847e2", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/com/typesafe/config/1.4.0/config-1.4.0.jar", "source": {"sha1": "e748b8348e1910b0935bf662e21e748290229e7f", "sha256": "ffaf8892dc8c61605bd7319c6cdcea022b6c9c28b62776915a809e8de93d8a6e", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/com/typesafe/config/1.4.0/config-1.4.0-sources.jar"} , "name": "com-typesafe-config", "actual": "@com-typesafe-config//jar", "bind": "jar/com/typesafe/config"},
    {"artifact": "junit:junit:4.12", "lang": "java", "sha1": "2973d150c0dc1fefe998f834810d68f278ea58ec", "sha256": "59721f0805e223d84b90677887d9ff567dc534d7c502ca903c0c2b17f05c116a", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/junit/junit/4.12/junit-4.12.jar", "source": {"sha1": "a6c32b40bf3d76eca54e3c601e5d1470c86fcdfa", "sha256": "9f43fea92033ad82bcad2ae44cec5c82abc9d6ee4b095cab921d11ead98bf2ff", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/junit/junit/4.12/junit-4.12-sources.jar"} , "name": "junit-junit", "actual": "@junit-junit//jar", "bind": "jar/junit/junit"},
    {"artifact": "org.hamcrest:hamcrest-core:1.3", "lang": "java", "sha1": "42a25dc3219429f0e5d060061f71acb49bf010a0", "sha256": "66fdef91e9739348df7a096aa384a5685f4e875584cce89386a7a47251c4d8e9", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar", "source": {"sha1": "1dc37250fbc78e23a65a67fbbaf71d2e9cbc3c0b", "sha256": "e223d2d8fbafd66057a8848cc94222d63c3cedd652cc48eddc0ab5c39c0f84df", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3-sources.jar"} , "name": "org-hamcrest-hamcrest-core", "actual": "@org-hamcrest-hamcrest-core//jar", "bind": "jar/org/hamcrest/hamcrest-core"},
    {"artifact": "org.scala-lang.modules:scala-java8-compat_2.13:0.9.0", "lang": "java", "sha1": "cecd75f8db95161dda4b6ea226c964f6df4a6d8e", "sha256": "42636d2c772f20b2fa6e8be5b461564d7e51c067895b6689711de7d08d1f79cb", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/scala-lang/modules/scala-java8-compat_2.13/0.9.0/scala-java8-compat_2.13-0.9.0.jar", "source": {"sha1": "54cfdbda326cd09baba9b72cdbf7e1d3fc570ca8", "sha256": "6782efc4ffda1864665c6db044ef5d4c2de8a60d39abe1c50f07bca8c34b5b2c", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/scala-lang/modules/scala-java8-compat_2.13/0.9.0/scala-java8-compat_2.13-0.9.0-sources.jar"} , "name": "org-scala-lang-modules-scala-java8-compat_2-13", "actual": "@org-scala-lang-modules-scala-java8-compat_2-13//jar", "bind": "jar/org/scala-lang/modules/scala-java8-compat-2-13"},
    {"artifact": "org.scala-lang:scala-library:2.13.0", "lang": "java", "sha1": "e988ea597113786768900f1c260c694a2ae32a42", "sha256": "bd3b2fa8b922295ccf1537aba1850d455f82003c10484df509d29ff177cc1edc", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/scala-lang/scala-library/2.13.0/scala-library-2.13.0.jar", "source": {"sha1": "80a99c2699c361524e0eb51ebbcce55bbeda6689", "sha256": "4cd39b6e9ec0c5b28bf0b62a841e7aaf16d5728d4e014081a64e5eca0bf6b722", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/scala-lang/scala-library/2.13.0/scala-library-2.13.0-sources.jar"} , "name": "org-scala-lang-scala-library", "actual": "@org-scala-lang-scala-library//jar", "bind": "jar/org/scala-lang/scala-library"},
# duplicates in org.slf4j:slf4j-api promoted to 1.7.28
# - com.typesafe.akka:akka-actor-typed_2.13:2.6.0 wanted version 1.7.28
# - com.typesafe.akka:akka-slf4j_2.13:2.6.0 wanted version 1.7.28
# - org.zeroturnaround:zt-exec:1.11 wanted version 1.7.2
    {"artifact": "org.slf4j:slf4j-api:1.7.28", "lang": "java", "sha1": "2cd9b264f76e3d087ee21bfc99305928e1bdb443", "sha256": "fb6e4f67a2a4689e3e713584db17a5d1090c1ebe6eec30e9e0349a6ee118141e", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/slf4j/slf4j-api/1.7.28/slf4j-api-1.7.28.jar", "source": {"sha1": "6444f3c8fce32e20f621e264807256c5e65f11c9", "sha256": "b1b8bfa4f2709684606001685d09ef905adc1b72ec53444ade90f44bfbcebcff", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/slf4j/slf4j-api/1.7.28/slf4j-api-1.7.28-sources.jar"} , "name": "org-slf4j-slf4j-api", "actual": "@org-slf4j-slf4j-api//jar", "bind": "jar/org/slf4j/slf4j-api"},
    {"artifact": "org.zeroturnaround:zt-exec:1.11", "lang": "java", "sha1": "ee0092fd27178e1a966aa90771478338aa0c8f05", "sha256": "5b9131ca9de1477e3f38bb4e6ef951ed13d1edf34329d1a98d6f48e174523233", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/zeroturnaround/zt-exec/1.11/zt-exec-1.11.jar", "source": {"sha1": "c16101d68a54140a42e925240d9b0ba40f96c7e2", "sha256": "065a8cb9a82b7b5b28314269d0eb4875c1a713b959a03a635fb8b7371c8ea820", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/zeroturnaround/zt-exec/1.11/zt-exec-1.11-sources.jar"} , "name": "org-zeroturnaround-zt-exec", "actual": "@org-zeroturnaround-zt-exec//jar", "bind": "jar/org/zeroturnaround/zt-exec"},
    ]

def maven_dependencies(callback = jar_artifact_callback):
    for hash in list_dependencies():
        callback(hash)
