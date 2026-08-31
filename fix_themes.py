import re

with open('app/src/main/java/com/example/domain/model/ThemePreset.kt', 'r') as f:
    content = f.read()

# I will replace some themes to be more anime/vibrant.
# Sakura Cream -> Neon Sakura
content = content.replace(
'''    SAKURA_CREAM(
        displayName = "Sakura Cream",
        description = "Delicate cherry blossom pinks paired with sweet cream tones",
        primaryLight = Color(0xFFE5989B),
        primaryDark = Color(0xFFFFB5A7),
        secondaryLight = Color(0xFFB5838D),
        secondaryDark = Color(0xFFD4A3AB),
        surfaceLight = Color(0xFFFFFFFF),
        surfaceDark = Color(0xFF352B2D),
        bgLight = Color(0xFFFFF0F3),
        bgDark = Color(0xFF231A1C),
        tertiaryLight = Color(0xFFF43F5E),
        tertiaryDark = Color(0xFFFB7185),
        errorLight = Color(0xFFDC2626),
        errorDark = Color(0xFFF87171)
    ),''',
'''    NEON_SAKURA(
        displayName = "Neon Sakura",
        description = "Vibrant hot pink and crisp white with an anime aesthetic",
        primaryLight = Color(0xFFFF006A),
        primaryDark = Color(0xFFFF4D94),
        secondaryLight = Color(0xFF8B5CF6),
        secondaryDark = Color(0xFFA78BFA),
        surfaceLight = Color(0xFFFFFFFF),
        surfaceDark = Color(0xFF1F1118),
        bgLight = Color(0xFFFFF5F8),
        bgDark = Color(0xFF0F050A),
        tertiaryLight = Color(0xFFF43F5E),
        tertiaryDark = Color(0xFFFB7185),
        errorLight = Color(0xFFDC2626),
        errorDark = Color(0xFFF87171)
    ),
    DYNAMIC_MATERIAL(
        displayName = "Dynamic (Material You)",
        description = "Extracts colors from your wallpaper (Android 12+)",
        primaryLight = Color(0xFF10B981),
        primaryDark = Color(0xFF10B981),
        secondaryLight = Color(0xFF06B6D4),
        secondaryDark = Color(0xFF06B6D4),
        surfaceLight = Color(0xFFFFFFFF),
        surfaceDark = Color(0xFF121212),
        bgLight = Color(0xFFF8FAFC),
        bgDark = Color(0xFF000000),
        tertiaryLight = Color(0xFF8B5CF6),
        tertiaryDark = Color(0xFF8B5CF6),
        errorLight = Color(0xFFEF4444),
        errorDark = Color(0xFFEF4444)
    ),''')

with open('app/src/main/java/com/example/domain/model/ThemePreset.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/theme/Theme.kt', 'r') as f:
    theme_content = f.read()

theme_content = theme_content.replace(
'''        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {''',
'''        (dynamicColor || themePreset == ThemePreset.DYNAMIC_MATERIAL) && Build.VERSION.SDK_INT >= Build.VERSION.CODES.S -> {''')

with open('app/src/main/java/com/example/ui/theme/Theme.kt', 'w') as f:
    f.write(theme_content)

