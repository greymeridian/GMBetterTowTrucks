# GreyMeridian's Better Tow Trucks

A Project Zomboid Build 42 compatibility mod that improves interoperability between **Better Car Physics** and supported tow truck / towing mods.

The mod provides the Java-side compatibility required for supported tow trucks to work correctly with Better Car Physics while preserving their intended towing functionality.

## Compatibility

Designed for **Project Zomboid Build 42**.

Current compatibility targets include:

- Better Car Physics
- Harry's Functional Tow Truck
- Better Towing
- ZombieBuddy

Additional compatibility may be added as required.

## Installation

The recommended installation method is through the Project Zomboid Steam Workshop.

This repository contains the source and development files for the mod and is primarily intended for development, reference, and issue tracking.

## Repository Structure

```text
archive/        Archived or previous development files
media/          Project Zomboid mod files
mod.info        Project Zomboid mod metadata
workshop.txt    Steam Workshop metadata
icon.png        Mod icon
poster.png      Workshop poster
preview.png     Workshop preview image
```

Development builds may also contain Gradle configuration and Java source used to build the mod's Java components.

## Building

The Java components of the mod are built using Gradle.

Build configuration may require local path changes depending on the development environment and Project Zomboid installation location.

Generated Gradle files, compiled classes, and build output are intentionally excluded from version control.

## Credits

Huge thanks to the modders who made this possible through their work on Better Car Physics, Harry's Functional Tow Truck, Better Towing, and ZombieBuddy.

**Better Car Physics** was created by Neidmare.

**Harry's Functional Tow Truck** was originally created by @harrycheez, later edited by @Enobdercas, and is currently maintained by @obtainchi.

**Better Towing** was created by @Aiteron.

Extra thanks to Zed for creating ZombieBuddy and making Java-side compatibility mods significantly more practical.

## License

Project Zomboid and related assets are property of their respective owners. Third-party mods referenced by this project remain the property of their respective authors.
