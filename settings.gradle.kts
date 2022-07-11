rootProject.name = "chronolens"

include("chronolens-core", "chronolens-cli", "chronolens-test")
include("services:chronolens-git")
include("services:chronolens-java")
include(
    "analyzers:chronolens-churn",
    "analyzers:chronolens-coupling",
    "analyzers:chronolens-decapsulations"
)
