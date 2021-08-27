plugins {
    val kotlinVersion = "1.5.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.7.0"
}

group = "love.hana.bot.qq"
version = "2.0.0"

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20210307")
    implementation("org.mongodb:mongodb-driver-sync:4.3.1")
    implementation("com.aliyun:aliyun-java-sdk-core:4.5.25")
    implementation("com.aliyun:aliyun-java-sdk-green:3.6.5")
}
