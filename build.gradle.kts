plugins {
    id("java")
    id("com.diffplug.spotless") version "8.0.0"
    checkstyle
}

group = "com.github.yuyuvu"
version = "1.0"

//  указываем, что версия файлов скомпилированных классов должна быть совместима со всеми JDK, начиная с JDK 17
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

// добавляем JUnit и Mockito для тестов и Jackson для сериализации и десериализации JSON
dependencies {
    // https://mvnrepository.com/artifact/tools.jackson.core/jackson-databind
    implementation("tools.jackson.core:jackson-databind:3.0.0")
    // https://mvnrepository.com/artifact/org.junit/junit-bom
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // https://mvnrepository.com/artifact/org.mockito/mockito-core
    testImplementation("org.mockito:mockito-core:5.20.0")
    // https://mvnrepository.com/artifact/org.mockito/mockito-junit-jupiter
    testImplementation("org.mockito:mockito-junit-jupiter:5.20.0")
}

tasks.test {
    useJUnitPlatform()

    // Добавляем Java agent Mockito, чтобы избежать предупреждений
    // об удалении некоторых функций в будущих версиях JVM
    val mockitoPath = configurations.testRuntimeClasspath.get().files.find {
        it.name.contains("mockito-core")
    }
    if (mockitoPath != null) {
        jvmArgs(
            "-javaagent:$mockitoPath",
            "-Xshare:off"
        )
    }
}

// указываем, что везде нужна кодировка UTF-8; без этого кириллица отображается в консоли неправильно
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<JavaExec> {
    systemProperty("file.encoding", "UTF-8")
    systemProperty("sun.jnu.encoding", "UTF-8")
    jvmArgs = listOf(
        "-Dfile.encoding=UTF-8",
        "-Dsun.stdout.encoding=UTF-8",
        "-Dsun.stderr.encoding=UTF-8"
    )
}

// указываем основной класс с main для MANIFEST.MF, а также то, что нам нужен fat-jar со всеми зависимостями
tasks.jar {
    archiveBaseName.set("url-shortener-service")
    manifest {
        attributes["Main-Class"] = "com.github.yuyuvu.urlshortener.Main"
    }
    val dependencies = configurations.runtimeClasspath.get().map(::zipTree)
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Настройки checkstyle и spotless
// Checkstyle осуществляет дополнительные проверки (например импортов и документации),
// поэтому добавлен вместе со spotless

spotless {
    java {
        googleJavaFormat("1.31.0")
    }
}

checkstyle {
    toolVersion = "12.1.1"
    configFile = rootProject.file("config/checkstyle/google_checks.xml")
    // запрещаем сборку с предупреждениями и ошибками
    maxErrors = 0
    maxWarnings = 0
}

// Указываем явный порядок сборки. Получаем итоговый jar после всех проверок и тестов.
tasks.check {
    dependsOn(tasks.spotlessApply)
    dependsOn(tasks.checkstyleMain)
    dependsOn(tasks.checkstyleTest)
}

tasks.test {
    dependsOn(tasks.spotlessApply)
}

tasks.assemble {
    dependsOn(tasks.check )
}

tasks.jar {
    dependsOn(tasks.check )
}

// Явно указываем Gradle, что jar необходимо пересоздавать при каждой сборке
tasks.withType<Jar> {
    outputs.upToDateWhen { false }
}