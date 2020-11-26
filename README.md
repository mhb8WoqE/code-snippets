# About
This is little live templates for me to use in IntellijIdea while working on Minecraft mods and other projects.
Contains common code and useful classes but doesn't build them to separate libraries.
The whole point of this is when I need common code I just type a sequence in the editor and it completes into code chunk.
Fast and easy.

# Also
This is my first time trying to document my code and run tests. I hope I did decent.

# ASM
A set of useful methods to work with ASM tree API
Examples [from here](src/main/java/examples):
```java
ClassNode classNode = read(readClass("examples/Ex"));

classNode.methods
        .stream()
        .filter(forMethod("getInt", "()I")) // find method getInt that returns an int
        .findFirst()
        .ifPresent(methodNode -> {
            InsnList list = new InsnList(); // manual instantiation
            compose(
                    getThis(), // equivalent to addInst(() -> new VarInsnNode(Opcodes.ALOAD, 0))
                    // calls static method
                    callStatic("examples/Examples$Hooks", "getIntHook", "(Lexamples/Ex;)V")
            ).accept(list); // modify instructions list
            insertFirst(methodNode.instructions, list); // insert before first instruction in supplied list
        });

classNode.methods
        .stream()
        .filter(forMethod("numbers").and(forMethodDesc("(I)Ljava/lang/String;"))) // combine filters
        .findFirst()
        .ifPresent(methodNode -> {
            forEach(methodNode.instructions, // perform operation for each instruction that matched filter below
                    opcode(Opcodes.ARETURN), // matches instruction with opcode equals to ARETURN
                    insertBefore(supplyCode(compose( // inserts instructions before every matched node
                            getThis(), // loads local var 0 (zero)
                            // calls static method
                            callStatic("examples/Examples$Hooks", "numbersHook", "(Ljava/lang/String;Lexamples/Ex;)Ljava/lang/String;")
                    )))
            );
        });

classNode.methods
        .stream()
        .filter(forMethod("days"))                  // another way
        .filter(forMethodDesc("(I)Ljava/lang/String;"))  // to combine filters
        .findFirst()
        .ifPresent(methodNode -> {
            forEach(methodNode.instructions, // perform operation for each instruction that matched filter below
                    opcode(Opcodes.ARETURN), // matches instruction with opcode equals to ARETURN
                    // inserts instructions before every matched node
                    insertBefore(supplyIf( // creates if statement
                            compose( // sets up if statement
                                    addInst(Opcodes.DUP), // dup String that currently on stack
                                    // calls static method that returns boolean (integer)
                                    callStatic("examples/Examples$Hooks", "daysHook", "(Ljava/lang/String;)Z")
                            ),
                            jumpIfTrue(), // performs jump if value on stack is not equal to 0 (zero)
                            compose( // these instructions will be executed if jump wasn't made
                                    addInst(() -> new LdcInsnNode("Garfield doesn't like this day")),
                                    addInst(Opcodes.ARETURN)
                            ),
                            nothing() // these instructions will be executed if jump was made
                    ))
            );
        });
```