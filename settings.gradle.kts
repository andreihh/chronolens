rootProject.name = "chronolens"

include("chronolens-model", "chronolens-api", "chronolens-test")
include("chronolens-core")
include("app:chronolens-cli")
include("plugins:vcs:chronolens-git")
include("plugins:parsers:chronolens-java")
include(
  "plugins:analyzers:chronolens-interactive",
  "plugins:analyzers:chronolens-churn",
  "plugins:analyzers:chronolens-coupling",
  "plugins:analyzers:chronolens-decapsulations",
)
