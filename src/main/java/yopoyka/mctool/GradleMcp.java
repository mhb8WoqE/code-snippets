package yopoyka.mctool;

public class GradleMcp implements IMcp {
    public static GradleMcp instance = new GradleMcp();
    protected java.io.File fieldsFile;
    protected java.io.File methodsFile;
    protected java.util.Map<String, String> fieldMap;
    protected java.util.Map<String, String> methodMap;

    {
        try {
            final Class<?> gradle = Class.forName("net.minecraftforge.gradle.GradleStartCommon", false, net.minecraft.launchwrapper.Launch.classLoader);
            final java.lang.reflect.Field csv_dir = gradle.getDeclaredField("CSV_DIR");
            csv_dir.setAccessible(true);
            final java.io.File csvDir = (java.io.File) csv_dir.get(null);
            fieldsFile = csvDir.toPath().resolve("fields.csv").toFile();
            methodsFile = csvDir.toPath().resolve("methods.csv").toFile();
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            if (Boolean.parseBoolean(System.getProperty("yopoyka.gradlemcp.verbose"))) {
                System.out.println("GradleStartCommon is not loaded!");
                e.printStackTrace();
            }
        }
    }

    public String fromSrg(String srgName) {
        if (srgName.startsWith("field"))
            return fromSrgField(srgName);
        if (srgName.startsWith("func"))
            return fromSrgMethod(srgName);
        return srgName;
    }

    public String fromSrgMethod(String srgName) {
        if (methodMap == null)
            if (methodsFile == null)
                methodMap = java.util.Collections.emptyMap();
            else
                methodMap = readLines(methodsFile);

        return methodMap.getOrDefault(srgName, srgName);
    }

    public String fromSrgField(String srgName) {
        if (fieldMap == null)
            if (fieldsFile == null)
                fieldMap = java.util.Collections.emptyMap();
            else
                fieldMap = readLines(fieldsFile);

        return fieldMap.getOrDefault(srgName, srgName);
    }

    protected static java.util.Map<String, String> readLines(java.io.File file) {
        try {
            final java.util.Map<String, String> map = new java.util.HashMap<>();
            final java.util.List<String> lines = java.nio.file.Files.readAllLines(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            lines.forEach(line -> {
                final String[] split = line.split(",", 3);
                map.put(split[0], split[1]);
            });
            return map;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return java.util.Collections.emptyMap();
        }
    }
}
