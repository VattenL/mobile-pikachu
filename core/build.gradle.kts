plugins {
    `java-library`
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    // Message dùng JsonObject của Gson, client và server cùng cần nên khai báo api
    api(libs.gson)
    testImplementation(libs.junit)
}
