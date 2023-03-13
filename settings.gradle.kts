rootProject.name = "chronolens"

include("chronolens-model", "chronolens-api", "chronolens-test")
include("chronolens-core")
include("chronolens-cli")
include("services:chronolens-git")
include("services:chronolens-java")
include(
  "analyzers:chronolens-interactive",
  "analyzers:chronolens-churn",
  "analyzers:chronolens-coupling",
  "analyzers:chronolens-decapsulations",
)
