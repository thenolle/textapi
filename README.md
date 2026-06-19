# TextAPI

![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge\&logo=openjdk\&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-7F52FF?style=for-the-badge\&logo=kotlin\&logoColor=white)
![Spigot](https://img.shields.io/badge/Spigot-1.21+-ED8106?style=for-the-badge)
![License](https://img.shields.io/badge/License-WTFPL-brightgreen?style=for-the-badge)

![GitHub Repo](https://img.shields.io/badge/GitHub-thenolle%2Ftextapi-181717?style=for-the-badge\&logo=github)
![GitHub Release](https://img.shields.io/github/v/release/thenolle/textapi?style=for-the-badge)
![Downloads](https://img.shields.io/github/downloads/thenolle/textapi/total?style=for-the-badge)
![Issues](https://img.shields.io/github/issues/thenolle/textapi?style=for-the-badge)

TextAPI is a high-performance text formatting and parsing system for Spigot 1.21+ servers.

It provides a MiniMessage-like syntax with placeholders, colors, gradients, rainbow effects, hover/click events, runtime tag registration, and a fully extensible API.

---

![Examples](.github/assets/examples.png)

## Features

* Custom tag-based parser
* Placeholder system
* Runtime placeholder registration
* Runtime tag registration
* Click and hover events
* Gradient rendering
* Rainbow rendering
* Pride gradient presets
* Token-level parser API
* Legacy string output
* Component output
* Debug commands
* TPS placeholder support
* Tab-complete enabled `/text` command

---

## Installation

### Build

```bash
mvn clean package
```

### Install

Place the generated jar inside:

```text
/plugins/TextAPI-1.0.3.jar
```

Restart the server.

---

## Using TextAPI

TextAPI can be consumed through:

* Local Maven install
* GitHub Packages

---

## Option 1 — Local Install

Build and install locally:

```bash
mvn clean install
```

Dependency:

```xml
<dependency>
    <groupId>com.nolly.mc</groupId>
    <artifactId>textapi</artifactId>
    <version>1.0.3</version>
    <scope>provided</scope>
</dependency>
```

---

## Option 2 — GitHub Packages

### Repository

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/thenolle/textapi</url>
    </repository>
</repositories>
```

### Dependency

```xml
<dependency>
    <groupId>com.nolly.mc</groupId>
    <artifactId>textapi</artifactId>
    <version>1.0.3</version>
    <scope>provided</scope>
</dependency>
```

### Authentication

`~/.m2/settings.xml`

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">

    <activeProfiles>
        <activeProfile>github</activeProfile>
    </activeProfiles>

    <profiles>
        <profile>
            <id>github</id>

            <repositories>
                <repository>
                    <id>central</id>
                    <url>https://repo.maven.apache.org/maven2</url>
                </repository>

                <repository>
                    <id>github</id>
                    <url>https://maven.pkg.github.com/thenolle/textapi</url>

                    <snapshots>
                        <enabled>true</enabled>
                    </snapshots>
                </repository>
            </repositories>
        </profile>
    </profiles>

    <servers>
        <server>
            <id>github</id>
            <username>YOUR_GITHUB_USERNAME</username>
            <password>YOUR_GITHUB_TOKEN</password>
        </server>
    </servers>

</settings>
```

---

## Gradle (Groovy)

```gradle
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/thenolle/textapi")

        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    compileOnly 'com.nolly.mc:textapi:1.0.3'
}
```

---

## Gradle (Kotlin)

```kts
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/thenolle/textapi")

        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    compileOnly("com.nolly.mc:textapi:1.0.3")
}
```

---

## Configuration

```yml
command:
  enabled: true
```

---

## Commands

### Main Command

```text
/text
```

### Subcommands

```text
/textapi parse <message>
/textapi components <message>
/textapi tokens <message>
/textapi placeholders
/textapi register <key> <value>
/textapi unregister <key>
/textapi examples
```

---

## Syntax Guide

### Colors

```txt
<red>Red</red>
<gold>Gold</gold>
<green>Green</green>
<#ff5500>Hex Color</#ff5500>
```

### Gradients

```txt
<gradient:#ff0000:#00ff00>Gradient Text</gradient>
```

### Rainbow

```txt
<rainbow>Rainbow Text</rainbow>
```

### Decorations

```txt
<bold>Bold</bold>

<italic>Italic</italic>

<underlined>Underline</underlined>

<strikethrough>Strike</strikethrough>

<obfuscated>Magic</obfuscated>
```

### Disable Decorations

```txt
<bold>
    Bold
    <!bold>Not Bold</!bold>
    Bold Again
</bold>
```

### Hover Events

```txt
<hover:show_text:Hello World>
    Hover Me
</hover>
```

### Click Events

```txt
<click:run_command:/spawn>
    Run Command
</click>

<click:suggest_command:/msg >
    Suggest Command
</click>

<click:open_url:https://example.com>
    Open URL
</click>

<click:copy_to_clipboard:Copied Text>
    Copy
</click>
```

### Insertions

```txt
<insert:Hidden Text>
    Hover + Shift Click
</insert>
```

### Placeholders

```txt
Hello {player}

Online: {server_online}

World: {player_world}
```

---

## Pride Gradients

```txt
<pride>Pride</pride>

<trans>Trans</trans>

<bi>Bisexual</bi>

<lesbian>Lesbian</lesbian>

<nonbinary>Nonbinary</nonbinary>

<pan>Pansexual</pan>

<ace>Asexual</ace>

<aro>Aromantic</aro>

<genderfluid>Genderfluid</genderfluid>

<agender>Agender</agender>

<intersex>Intersex</intersex>

<polyam>Polyamorous</polyam>

<demi>Demisexual</demi>

<genderqueer>Genderqueer</genderqueer>
```

---

## Built-in Placeholders

### Player

* `{player}`
* `{player_name}`
* `{player_uuid}`
* `{player_display}`
* `{player_world}`
* `{player_x}`
* `{player_y}`
* `{player_z}`
* `{player_ping}`
* `{player_gamemode}`
* `{player_health}`
* `{player_food}`
* `{player_level}`
* `{player_exp}`
* `{player_ip}`
* `{player_locale}`
* `{player_online}`

### Server

* `{server_name}`
* `{server_version}`
* `{server_motd}`
* `{server_online}`
* `{server_max}`
* `{server_tps}`

### Time

* `{time}`
* `{date}`
* `{datetime}`
* `{timestamp}`

### Misc

* `{newline}`
* `{prefix}`

---

## API Usage

### Parsing

```kt
val text = TextAPI.parse(
	"<red>Hello {player}</red>",
	player
)
```

### Components

```kt
val components = TextAPI.components(
	"<bold>Hello</bold>",
	player
)
```

### Sending Messages

```kt
TextAPI.send(
	player,
	"<gradient:#ff0000:#00ff00>Hello</gradient>"
)
```

### Tokens

```kt
val tokens = TextAPI.tokens(
	"<red>Hello</red>"
)
```

---

## Placeholder Registration

### Register

```kt
TextAPI.registerPlaceholder("rank") { player ->
	if (player?.isOp == true) {
		"Admin"
	} else {
		"User"
	}
}
```

Usage:

```txt
Hello {rank}
```

### Unregister

```kt
TextAPI.unregisterPlaceholder("rank")
```

---

## Runtime Tag Registration

Tags can be registered dynamically so the parser recognizes them as tags rather than placeholders.

### Register

```kt
TextAPI.registerTag("mytag")
```

Usage:

```txt
<mytag>Hello</mytag>
```

### Unregister

```kt
TextAPI.unregisterTag("mytag")
```

### Check Behavior

Without registration:

```txt
<mytag>
```

is treated as:

```txt
{mytag}
```

After registration:

```txt
<mytag>
```

is parsed as a tag token.

This allows plugin developers to build custom renderers, preprocessors, extensions, or future tag implementations without modifying TextAPI internals.

---

## Performance Notes

* Single-pass tokenization
* Linear parsing complexity
* Linear gradient expansion
* Linear rainbow expansion
* Cached placeholder resolution per render context
* Rolling TPS history buffer
* No reflection
* Minimal allocations during rendering

---

## Permissions

```text
com.nolly.mc.textapi.command
```

Default:

```text
op
```

---

## Compatibility

* Spigot 1.21+
* Java 21+
* Kotlin 2.4+
* Bungee Chat Component API

---

## License

<a href="http://www.wtfpl.net/">
<img src="http://www.wtfpl.net/wp-content/uploads/2012/12/wtfpl-badge-4.png"
width="80"
height="15"
alt="WTFPL" />
</a>
