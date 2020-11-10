public class Reflect {
    public static java.lang.reflect.Field makeAccessible(java.lang.reflect.Field field) {
        field.setAccessible(true);
        return field;
    }

    public static java.lang.reflect.Field unFinal(java.lang.reflect.Field field) {
        try {
            field.setAccessible(true);
            java.lang.reflect.Field modifiers = java.lang.reflect.Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        return field;
    }
}
