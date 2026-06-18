# TextAPI

![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Spigot](https://img.shields.io/badge/Spigot-1.21+-ED8106?style=for-the-badge)
![License](https://img.shields.io/badge/License-WTFPL-brightgreen?style=for-the-badge)

![GitHub Repo](https://img.shields.io/badge/GitHub-thenolle%2Ftextapi-181717?style=for-the-badge&logo=github)
![GitHub Release](https://img.shields.io/github/v/release/thenolle/textapi?style=for-the-badge)
![Downloads](https://img.shields.io/github/downloads/thenolle/textapi/total?style=for-the-badge)
![Issues](https://img.shields.io/github/issues/thenolle/textapi?style=for-the-badge)

TextAPI is a high-performance text formatting and parsing system for Spigot 1.21+ servers.  
It provides a MiniMessage-like syntax with placeholders, gradients, rainbow effects, hover/click events, and a fully extensible placeholder API.

---

![Examples](.github/assets/examples.png)

## Features

- Custom tag-based text parser (`<red>`, `<gradient>`, `<rainbow>`, etc.)
- Placeholder system (`{player}`, `{server_online}`, custom placeholders)
- Click and hover events (Bungee Chat API)
- Gradient and rainbow text rendering
- Pride gradient presets
- Token-level parsing API
- Full command-based debugging tools
- Runtime placeholder registration
- TPS placeholder support
- Component + legacy string output
- Tab-complete enabled `/text` command

---

## Installation

1. Build the plugin:

```bash
mvn clean package
````

2. Place the generated jar in your server:

```text
/plugins/TextAPI-1.0.2.jar
```

3. Restart the server.

---

## Using TextAPI (GitHub Packages or Local Install)

TextAPI is distributed via **GitHub Packages Maven registry**.

You have **two options**:

* Install locally (`mvn install`)
* Use GitHub Packages repository

---

## Option 1 — Local install (simplest)

### Build and install into local Maven cache:

```bash
mvn clean install
```

Then use dependency normally:

```xml
<dependency>
    <groupId>com.nolly.mc</groupId>
    <artifactId>textapi</artifactId>
    <version>1.0.2</version>
    <scope>provided</scope>
</dependency>
```

No repository required.

---

## Option 2 — GitHub Packages

### Step 1: Add repository

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/thenolle/textapi</url>
    </repository>
</repositories>
```

---

### Step 2: Add dependency

```xml
<dependency>
    <groupId>com.nolly.mc</groupId>
    <artifactId>textapi</artifactId>
    <version>1.0.2</version>
    <scope>provided</scope>
</dependency>
```

---

### Step 3: Authenticate GitHub Packages

Create or edit:

```text
~/.m2/settings.xml
```

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

### Step 4: Install dependency

```bash
mvn install
```

---

## Gradle (Groovy DSL)

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
    compileOnly 'com.nolly.mc:textapi:1.0.2'
}
```

---

## Gradle (Kotlin DSL)

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
    compileOnly("com.nolly.mc:textapi:1.0.2")
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

### Main command

```
/text
```

### Subcommands

```
/text parse <message>
/text components <message>
/text tokens <message>
/text placeholders
/text register <key> <value>
/text unregister <key>
/text examples
```

---

## Syntax Guide

### Colors

```txt
<red>Red</red>
<gold>Gold</gold>
<#ff5500>Hex color</#ff5500>
```

### Gradients

```txt
<gradient:#ff0000:#00ff00>Gradient text</gradient>
```

### Rainbow

```txt
<rainbow>Rainbow text</rainbow>
```

### Decorations

```txt
<bold>Bold</bold>
<italic>Italic</italic>
<underlined>Underline</underlined>
<strikethrough>Strike</strikethrough>
```

### Hover events

```txt
<hover:show_text:Hello world>Hover me</hover>
```

### Click events

```txt
<click:run_command:/spawn>Run command</click>
<click:suggest_command:/msg >Suggest command</click>
<click:open_url:https://example.com>Open URL</click>
```

### Placeholders

```
Hello {player}
Online: {server_online}
World: {player_world}
```

---

## Built-in Placeholders

### Player

* `{player}`
* `{player_name}`
* `{player_uuid}`
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

```kt
TextAPI.parse("<red>Hello {player}</red>", player)

TextAPI.send(player, "<gradient:#ff0000:#00ff00>Hello</gradient>")

val components = TextAPI.components("<bold>Hello</bold>", player)

val tokens = TextAPI.tokens("<red>Hello</red>")

TextAPI.registerPlaceholder("rank") {
    if (it?.isOp == true) "Admin" else "User"
}

TextAPI.unregisterPlaceholder("rank")
```

---

## Performance Notes

* Single-pass parsing
* Linear gradient/rainbow expansion
* Cached placeholder resolution per render context
* Rolling TPS buffer (600 ticks)

---

## Permissions

```
com.nolly.mc.textapi.command
```

Default: `op`

---

## Compatibility

* Spigot 1.21+
* Java 21
* Kotlin 2.4+
* Bungee Chat Component API

---

## License

<a href="http://www.wtfpl.net/">
<img src="http://www.wtfpl.net/wp-content/uploads/2012/12/wtfpl-badge-4.png"
width="80" height="15" alt="WTFPL" />
</a>
