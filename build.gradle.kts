import org.gradle.api.JavaVersion.VERSION_1_8

plugins {
    java
    application
    kotlin("jvm") version "1.3.61"
    kotlin("kapt") version "1.3.61"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.3.61"
}

repositories {
    mavenCentral()
    jcenter()
}

configure<JavaPluginConvention> {
    sourceCompatibility = VERSION_1_8
    targetCompatibility = VERSION_1_8
}

dependencies {
    compile(kotlin("stdlib-jdk8"))

    // 19.3 is "first planned long-term support (LTS) release"
    // but doesn't seem to work with UseJVMCIClassLoader option
    // see https://github.com/ekmett/cadenza/blob/master/build.gradle.kts for possibly working example
    compile("org.graalvm.truffle:truffle-api:19.3.0")
    kapt("org.graalvm.truffle:truffle-dsl-processor:19.3.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
}

application {
    mainClassName = "org.sgdan.grelm.MainKt"
    applicationDefaultJvmArgs = listOf(
            "-Dtruffle.class.path.append=/working/build/libs/truffle-elm.jar:/working/build/install/truffle-elm/lib/kotlin-stdlib-1.3.61.jar:build/install/truffle-elm/lib/kotlinx-serialization-runtime-0.14.0.jar"
    )
}

tasks.test {
    useJUnitPlatform()

    /*
     * See http://www.graalvm.org/docs/reference-manual/languages/jvm/
     *
     * "Disables the class loader used to isolate JVMCI and Graal from
     * application code. This is useful if you want to programmatically
     * invoke Graal."
     */
    jvmArgs = listOf("-XX:-UseJVMCIClassLoader")
}
