plugins {
//    id("dev.isxander.modstitch.base") version "0.5.12"
    id("dev.isxander.modstitch.base") version "0.8.4"
    id("dev.isxander.modstitch.publishing") version "0.8.4"
}

fun prop(name: String, consumer: (prop: String) -> Unit) {
    (findProperty(name) as? String?)
        ?.let(consumer)
}

val minecraft = property("deps.minecraft") as String;

sourceSets {
    main {
        resources {
            srcDir(rootProject.file("src/resources_per_version/$minecraft/resources"))
        }
    }
}
// All dependencies should be specified through modstitch's proxy configuration.
// Wondering where the "repositories" block is? Go to "stonecutter.gradle.kts"
// If you want to create proxy configurations for more source sets, such as client source sets,
// use the modstitch.createProxyConfigurations(sourceSets["client"]) function.
repositories {
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://maven.jamieswhiteshirt.com/libs-release/") {
        content {
            includeGroup("com.jamieswhiteshirt")
        }
    }
}

dependencies {
    modstitch.loom {
        modstitchModImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric-api")}")
        modstitchModImplementation("com.terraformersmc:modmenu:${property("modmenu_version")}")

    }
    val loaderSuffix = when {
        modstitch.isLoom -> "fabric"
        modstitch.isModDevGradleRegular -> "neoforge"
        else -> "forge"
    }

    modstitchModApi("me.shedaniel.cloth:cloth-config-$loaderSuffix:${property("cloth_config_version")}") {
        if (modstitch.isLoom) {
            exclude(group = "net.fabricmc.fabric-api")
        }
    }
    val rtreeVersion=property("rtree_version")
    val opac=property("opac")
    val goml=property("goml")
    val flan=property("flan")
    val ftbChunks=property("ftb_chunks")
    val ftbLibrary=property("ftb_library")
    if(rtreeVersion!=""){
        modstitchModCompileOnly("com.jamieswhiteshirt:rtree-3i-lite-fabric:${rtreeVersion}")
    }
    if(opac!=""){
        modstitchModCompileOnly("$opac")
    }
    if(goml!=""){
        modstitchModCompileOnly("$goml")
    }
    if(flan!=""){
        modstitchModCompileOnly("$flan")
    }
    if(ftbChunks!=""){
        modstitchModCompileOnly("$ftbChunks")
    }
    if(ftbLibrary!=""){
        modstitchModCompileOnly("$ftbLibrary")
    }
}

modstitch {
    minecraftVersion = minecraft

    // Alternatively use stonecutter.eval if you have a lot of versions to target.
    // https://stonecutter.kikugie.dev/stonecutter/guide/setup#checking-versions
    //javaTarget = when (minecraft) {
    //    "1.20.1" -> 17
    //    "1.21.1" -> 21
    //    "1.21.9" -> 21
    //    else -> throw IllegalArgumentException("Please store the java version for ${property("deps.minecraft")} in build.gradle.kts!")
    //}

    // If parchment doesnt exist for a version yet you can safely
    // omit the "deps.parchment" property from your versioned gradle.properties
    parchment {
        prop("deps.parchment") { mappingsVersion = it }
    }

    // This metadata is used to fill out the information inside
    // the metadata files found in the templates folder.
    metadata {
        modId = "wands"
        modName = "BuildingWands"
        modVersion = "3.2.0"
        modGroup = "net.nicguzzo"
        modAuthor = "Nicguzzo"

        fun <K: Any, V: Any> MapProperty<K, V>.populate(block: MapProperty<K, V>.() -> Unit) {
            block()
        }

        replacementProperties.populate {
            // You can put any other replacement properties/metadata here that
            // modstitch doesn't initially support. Some examples below.
            put("mod_issue_tracker", "https://github.com/nicguzzo/deepslateinstamine/issues")

            put("minecraft_version_range", when (property("deps.minecraft")) {
                "1.20.1" -> "[1.20.1,)"
                "1.21.1" -> "[1.21.1,)"
                "1.21.11" -> "[1.21.11,)"
                "26.1.2" -> "[26.1.2,)"
                "26.2" -> "[26.2,)"
                else -> throw IllegalArgumentException("Please set mc range version for ${property("deps.minecraft")} in build.gradle.kts")
            })
            put("forge_version_range", when (property("deps.minecraft")) {
                "1.20.1" -> "[47,)"
                "1.21.1" -> ""
                "1.21.11" -> ""
                "26.1.2" -> ""
                "26.2" -> ""
                else -> throw IllegalArgumentException("Please set forge version for ${property("deps.minecraft")} in build.gradle.kts")
            })

            put("pack_format", when (property("deps.minecraft")) {
                "1.20.1" -> 15
                "1.21.1" -> 34
                "1.21.11" -> 75
                "26.1.2" -> 84
                "26.2" -> 88
                else -> throw IllegalArgumentException("Please store the resource pack version for ${property("deps.minecraft")} in build.gradle.kts! https://minecraft.wiki/w/Pack_format")
            }.toString())
        }
    }

    // Fabric Loom (Fabric)
    loom {
        // It's not recommended to store the Fabric Loader version in properties.
        // Make sure its up to date.
        fabricLoaderVersion = "0.19.2"

        // Configure loom like normal in this block.
        configureLoom {
            runs {
                all {
                    programArgs("--width", "1920", "--height", "1080")
                }
            }
        }
    }

    // ModDevGradle (NeoForge, Forge, Forgelike)
    moddevgradle {
        if (sc.current.project.endsWith("neoforge")) {

            if (sc.current.parsed eq "1.21.1") { neoFormVersion = "1.21.1-20240808.144430" }
            else if (sc.current.parsed eq "26.1.2") { neoFormVersion = "26.1.2-1" }

            neoForgeVersion = "${property("deps.neoforge")}"
        } else
        {
            forgeVersion = "${property("deps.forge")}"
        }
        //enable {
        //    prop("deps.forge") { forgeVersion = it }
        //    prop("deps.neoform") { neoFormVersion = it }
        //    prop("deps.neoforge") { neoForgeVersion = it }
        //    prop("deps.mcp") { mcpVersion = it }
        //}

        // Configures client and server runs for MDG, it is not done by default
        defaultRuns()

        // This block configures the `neoforge` extension that MDG exposes by default,
        // you can configure MDG like normal from here
        //configureNeoforge {
        //    runs.all {
        //        programArgs("--width", "1024", "--height", "768")
        //    }
        //}
    }

    mixin {
        // You do not need to specify mixins in any mods.json/toml file if this is set to
        // true, it will automatically be generated.
        addMixinsToModManifest = true

        configs.register("wands")

        // Most of the time you wont ever need loader specific mixins.
        // If you do, simply make the mixin file and add it like so for the respective loader:
        if (isLoom) configs.register("wands-fabric")
        if (isModDevGradleRegular) configs.register("wands-neoforge")
        if (isModDevGradleLegacy) configs.register("wands-forge")
    }
}

// Stonecutter constants for mod loaders.
// See https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants
var constraint: String = name.split("-")[1]

stonecutter {
    //consts(
    //    "fabric" to constraint.equals("fabric"),
    //    "neoforge" to constraint.equals("neoforge"),
    //    "forge" to constraint.equals("forge"),
    //    "vanilla" to constraint.equals("vanilla")
    //)
    constants {
        put("fabric"  , modstitch.isLoom)
        put("neoforge", modstitch.isModDevGradleRegular)
        put("forge"   , modstitch.isModDevGradleLegacy)
        put("has_opac",       !("${property("opac")}".isNullOrEmpty()) )
        put("has_ftb_chunks", !("${property("ftb_chunks")}".isNullOrEmpty()) )
        put("has_flan",       !("${property("flan")}".isNullOrEmpty()) )
        put("has_goml",       !("${property("goml")}".isNullOrEmpty()) )
        //put("has_opac",       false)
        //put("has_ftb_chunks", false)
        //put("has_flan",       false)
        //put("has_goml",       false)
    }
    replacements {
        string {
            direction = eval(current.version, ">=26.1")

            replace(".GuiGraphics", ".GuiGraphicsExtractor")
            replace("GuiGraphics gui", "GuiGraphicsExtractor gui")
            replace("GuiGraphics.class", "GuiGraphicsExtractor.class")
            replace("ClickType", "ContainerInput")
            replace("TooltipComponentCallback", "ClientTooltipComponentCallback")
            replace("gui.drawString(", "gui.text(")
            replace("gui.renderItem(", "gui.item(")
            replace("ExtendedScreenHandlerType<","ExtendedMenuType<")
            replace("playS2C()","clientboundPlay()")
            replace("playC2S()","serverboundPlay()")
            replace("ExtendedScreenHandlerFactory<","ExtendedMenuProvider<")

        }

        //string {
        //    direction = eval(current.version, ">=1.21.11")
        //   replace("new ResourceLocation", "Identifier.fromNamespaceAndPath")
        //    replace("ResourceLocation", "Identifier")
        //    replace(".location()", ".identifier()")
        //}
        //string {
        //    direction = eval(current.version, ">=1.21.1", "<1.21.11")
        //    replace("new ResourceLocation", "ResourceLocation.fromNamespaceAndPath")
        //}
        //string {
        //    direction = eval(current.version, ">=1.21.11")
        //    replace(".location()", ".identifier()")
        //}
        //string {
        //    direction = eval(current.version, ">=1.21.11")
        //    replace("import net.minecraft.Util;", "import net.minecraft.util.Util;")
        //}
    }
}

java {
    withSourcesJar()
    val javaCompat = if (stonecutter.eval(stonecutter.current.version, "<=1.21.1")) {
        JavaVersion.VERSION_21
    } else {
        JavaVersion.VERSION_25
    }
    sourceCompatibility = javaCompat
    targetCompatibility = javaCompat
}

publishMods {

    //changelog.set(System.getenv("DINSTAMINE_CHANGELOG"))
    changelog.set("support for 26.1.2 using modstitch")
    type.set(STABLE)

    curseforge {
        projectId.set("363392")
        accessToken.set(providers.environmentVariable("CF_TOKEN"))
        minecraftVersions.add(property("deps.minecraft") as String)
    }
    modrinth {
        projectId.set("XkisZUfp")
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        minecraftVersions.add(property("deps.minecraft") as String)
    }
}
