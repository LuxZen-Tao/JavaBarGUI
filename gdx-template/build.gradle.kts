plugins {
    java
}

val gdxVersion: String by project
val projectVersion: String by project

allprojects {
    version = projectVersion
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }
}

project(":core") {
    dependencies {
        add("implementation", project(":sim"))
        add("implementation", "com.badlogicgames.gdx:gdx:$gdxVersion")
    }
}

project(":lwjgl3") {
    apply(plugin = "application")

    extensions.configure<JavaApplication> {
        mainClass.set("Lwjgl3Launcher")
    }

    dependencies {
        add("implementation", project(":core"))
        add("implementation", "com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
        add("runtimeOnly", "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
    }

    tasks.named<JavaExec>("run") {
        workingDir = rootProject.file("assets")
    }
}
