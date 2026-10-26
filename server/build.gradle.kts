plugins {
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClass.set("ptit.e23cnpm01.pikachu.server.controller.GameServer")
}

dependencies {
    implementation(project(":core"))
    runtimeOnly(libs.mysql.connector)
    testImplementation(libs.junit)
}
