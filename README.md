# ascii-image-generator
A Java based ASCII image generator that creates ASCII art from images. Features a powerful `CLI` version as well as a simpler `Main` version with less features (for now).
Images should be present in the `images` directory for the `Main` version.

## Build & Run Instructions
1. Clone this repository: `git clone https://github.com/ida5428/ascii-image-generator.git`
2. Change to the project directory: `cd ascii-image-generator`
3. Compile:
   - Windows:
      - `javac -d bin src\Main.java src\CLI.java`
   - UNIX (macOS/Linux):
      - `javac -d bin src/Main.java src/CLI.java`
4. Run: `java -cp bin CLI --help` **OR** `java -cp bin Main`

## License
This project is licensed under the [GNU General Public License v3.0].

[GNU General Public License v3.0]: https://www.gnu.org/licenses/gpl-3.0.en.html
