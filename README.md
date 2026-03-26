# 2DRPG (Java SE, Swing/AWT)

This project is a pure Java SE (Swing/AWT/Java2D) game with no external dependencies.

## Structure

- `src/com/...` Java source code
- `assets/` Game assets
- `Main.java` Entry point

## Compile + Run (no Gradle)

From the project root:

```bash
javac -d . -sourcepath src Main.java
java Main
```

Notes:
- The compile command outputs class files into the project root (`./com/...`) so `java Main` works.
- If you prefer a separate output folder:

```bash
javac -d out -sourcepath src Main.java
java -cp out Main
```
