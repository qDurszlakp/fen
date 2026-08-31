## Task 1 - Roman Numerals ("Roman letters")

**Problem:** Convert integers to Roman numerals and back. Romans wrote numbers with
the letters I(1) V(5) X(10) L(50) C(100) D(500) M(1000), largest first, using the
subtractive rule so a smaller letter before a larger one means "minus" (IV = 4, IX = 9,
XL = 40). Supported range is 1..3999. Your job: implement both directions and reject
invalid input.

Class `com.sandbox.server.playground.RomanPrep`:

```java
static String toRoman(int number)   // 1..3999
static int    fromRoman(String s)   // parse back
```

Rules:
- Subtractive notation: IV, IX, XL, XC, CD, CM.
- `toRoman`: throw `IllegalArgumentException` for number < 1 or > 3999.
- `fromRoman`: uppercase only; throw `IllegalArgumentException` for malformed input
  (`"IIII"`, `"VV"`, `"IC"`, `"IL"`, `""`, `null`, lowercase, unknown chars).
- Round-trip invariant: `fromRoman(toRoman(n)) == n` for all valid n.

Examples:
| int  | roman           |
|------|-----------------|
| 1    | I               |
| 4    | IV              |
| 9    | IX              |
| 58   | LVIII           |
| 1994 | MCMXCIV         |
| 3888 | MMMDCCCLXXXVIII  |

---

## Task 2 - In-Memory File System ("IMFS")

**Problem:** Build a tiny file system that lives only in memory - no disk. It has a single
root `/`, directories that contain other directories and files, and files that hold a
string of content. Paths are absolute and `/`-separated (`/a/b/f.txt`). You expose a
handful of operations (make directory, add/read file, list, remove) and raise errors for
nonsense operations (writing into a directory that doesn't exist, reading a directory as
if it were a file, etc.).

Class `com.sandbox.server.playground.ImfsPrep` - absolute POSIX-style paths, root `/`:

```java
void         mkdir(String path)                    // parent must already exist
void         addFile(String path, String content)  // parent dir must exist; overwrites
String       readFile(String path)
List<String> ls(String path)                       // dir -> child names sorted asc
                                                   // file -> single-element list with file name
void         rm(String path)                       // removes file or dir (recursively)
```

Errors (`IllegalArgumentException` or a custom exception - be consistent):
- mkdir where parent missing, or path already exists
- addFile where parent dir missing
- readFile on missing path or on a directory
- ls / rm on missing path
- any non-absolute path, or empty path

Example session:
```
mkdir("/a")
mkdir("/a/b")
addFile("/a/b/f.txt", "hello")
readFile("/a/b/f.txt")   -> "hello"
ls("/a")                 -> ["b"]
ls("/a/b")               -> ["f.txt"]
rm("/a/b")
ls("/a")                 -> []
```

---

## Task 3 - Expression Language ("Language")

**Problem:** Write a mini interpreter for a one-line-per-statement language. A program is
a string of statements separated by `;`. A statement is either an assignment
(`let x = <expr>`) or a bare arithmetic expression. Expressions use `+ - * /`, unary
minus and parentheses with normal math precedence. `run` evaluates the whole program and
returns the value of the last statement. Undefined variables, division by zero and
malformed syntax are errors.

Class `com.sandbox.server.playground.LangPrep`:

```java
double run(String program)   // returns value of the last statement/expression
```

Grammar:
- Statements separated by `;`. Trailing `;` allowed.
- Assignment: `let <name> = <expr>` ; variables reusable in later statements.
- Expression: integers and decimals, `+ - * /`, unary minus, parentheses.
- Precedence: `* /` over `+ -`; left-associative; parentheses override.

Errors (custom `EvalException`):
- unknown variable
- division by zero
- syntax error (unbalanced parens, trailing operator, empty program)

Examples:
```
run("2 + 3 * 4")                        -> 14.0
run("(2 + 3) * 4")                      -> 20.0
run("-5 + 2")                           -> -3.0
run("let x = 5; x * x")                 -> 25.0
run("let a = 2; let b = a + 3; a * b")  -> 10.0
```

Stretch (only if time): `%` operator, comparison returning 1.0/0.0, `print` statement.
