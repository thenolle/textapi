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

```
/plugins/TextAPI-1.0.1.jar
```

3. Restart the server.

---

## Gradle / Maven Setup

### Local installation (build & install to local repository)

#### Maven (local install)

From the project root:
    ```
    mvn clean install
    ```

This will install the artifact into your local Maven repository:
    ```
    ~/.m2/repository/com/nolly/mc/textapi/
    ```

---

#### Gradle (publish to Maven Local)

If the project supports Maven Local publishing:
    ```
    gradle publishToMavenLocal
    ```

or (recommended wrapper):
    ```
    ./gradlew publishToMavenLocal
    ```

This installs it into:
    ```sh
    ~/.m2/repository/
    ```

---

## Using TextAPI as a dependency

### Maven

Add this to your `pom.xml`:
    ```xml
    <dependency>
        <groupId>com.nolly.mc</groupId>
        <artifactId>textapi</artifactId>
        <version>1.0.1</version>
        <scope>provided</scope>
    </dependency>
    ```

Make sure Maven local is available (for local builds):
    ```xml
    <repositories>
        <repository>
            <id>mavenLocal</id>
            <url>file://${user.home}/.m2/repository</url>
        </repository>
    </repositories>
    ```

---

### Gradle (Groovy DSL)

Add:
    ```gradle
    repositories {
        mavenLocal()
        mavenCentral()
    }
    
    dependencies {
        compileOnly 'com.nolly.mc:textapi:1.0.1'
    }
    ```

---

### Gradle (Kotlin DSL)

Add:
    ```kts
    repositories {
        mavenLocal()
        mavenCentral()
    }
    
    dependencies {
        compileOnly("com.nolly.mc:textapi:1.0.1")
    }
    ```

---

## Configuration

`config.yml`

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

#### Parse & send text

```
/text parse <message>
```

#### Preview components (debug)

```
/text components <message>
```

#### Tokenize input (debug)

```
/text tokens <message>
```

#### List placeholders

```
/text placeholders
```

#### Register placeholder

```
/text register <key> <value>
```

#### Unregister placeholder

```
/text unregister <key>
```

#### Examples

```
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

```txt
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

### Initialize

The plugin initializes automatically, but you can use the API directly:

```kt
TextAPI.parse("<red>Hello {player}</red>", player)
```

---

### Send message

```kt
TextAPI.send(player, "<gradient:#ff0000:#00ff00>Hello</gradient>")
```

---

### Components

```kt
val components = TextAPI.components("<bold>Hello</bold>", player)
```

---

### Tokens

```kt
val tokens = TextAPI.tokens("<red>Hello</red>")
```

---

### Register placeholder

```kt
TextAPI.registerPlaceholder("rank") { player ->
    if (player?.isOp == true) "Admin" else "User"
}
```

---

### Unregister placeholder

```kt
TextAPI.unregisterPlaceholder("rank")
```

---

## Performance Notes

* Parsing is single-pass and lightweight
* Gradient/rainbow expansion is per-character (CPU linear)
* Placeholder resolution is cached per render context
* TPS tracking uses rolling tick buffer (600 ticks)

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
