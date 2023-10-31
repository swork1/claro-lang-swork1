load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:jvm.bzl", "jvm_import_external")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_file")
# -- load statements -- #

def _non_module_deps_impl(ctx):
  http_archive(
    name = "jflex_rules",
    url = "https://github.com/jflex-de/bazel_rules/archive/v1.8.2.tar.gz",
    sha256 = "bd41584dd1d9d99ef72909b3c1af8ba301a89c1d8fdc59becab5d2db1d006455",
    strip_prefix = "bazel_rules-1.8.2",
    patches = [
      "//patched_jcup:cup_rule_diff.patch"
    ],
    patch_args = [
      "-p1"
    ],
  )
  http_file(
    name = "bootstrapping_claro_compiler_tarfile",
    sha256 = "70dc17225d48cacfda9d9e2c28742dc7432b4b6b7971646552ab5f592cf1e0e1",
    url = "https://github.com/JasonSteving99/claro-lang/releases/download/v0.1.302/claro-cli-install.tar.gz",
  )
# -- repo definitions -- #

non_module_deps = module_extension(implementation = _non_module_deps_impl)
