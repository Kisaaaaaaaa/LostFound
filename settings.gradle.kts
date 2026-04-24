pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        // 添加阿里镜像辅助下载
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        // 高德地图官方仓库
        maven { url = uri("https://maven.amap.com/repository/maven-public/") }
    }
}

rootProject.name = "LostFound"
include(":app")
