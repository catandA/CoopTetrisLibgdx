package me.catand.cooptetris.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

public class LanguageManager {
    private static LanguageManager instance;
    private Properties properties;
    private Locale currentLocale;
    private static final String BUNDLE_PATH = "i18n/strings";
    private static String cachedCharacterSet;
    private static final Object cacheLock = new Object();

    private LanguageManager() {
        // 尝试从配置中加载语言设置
        try {
            ConfigManager configManager = new ConfigManager();
            String savedLanguage = configManager.getConfig().getLanguage();
            setLanguage(savedLanguage);
        } catch (Exception e) {
            // 如果加载失败，使用默认语言
            try {
                setLanguage("en");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }

        return instance;
    }

    /**
     * 重新加载语言设置，从配置中读取
     */
    public void reloadLanguageFromConfig() {
        try {
            ConfigManager configManager = new ConfigManager();
            String savedLanguage = configManager.getConfig().getLanguage();
            setLanguage(savedLanguage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLanguage(String languageCode) {
        currentLocale = new Locale(languageCode);

        try {
            properties = new Properties();

            // 直接指定要加载的文件
            String filePath;
            if (languageCode.equals("zh")) {
                filePath = "i18n/strings_zh.properties";
            } else {
                filePath = "i18n/strings.properties";
            }

            FileHandle fileHandle = Gdx.files.internal(filePath);

            if (fileHandle.exists()) {
                // 直接读取文件内容并加载到Properties
                String content = fileHandle.readString("UTF-8");

                // 加载内容到Properties
                java.io.StringReader reader = new java.io.StringReader(content);
                properties.load(reader);
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 保存语言设置到配置中
        try {
            ConfigManager configManager = new ConfigManager();
            Config config = configManager.getConfig();
            config.setLanguage(languageCode);
            configManager.saveSettings(config);
        } catch (Exception e) {
        }
    }

    public String get(String key) {
        if (properties == null) {
            return key;
        }
        try {
            String value = properties.getProperty(key);
            return value != null ? value : key;
        } catch (Exception e) {
            return key;
        }
    }

    public String get(String key, Object... args) {
        String value = get(key);
        try {
            return String.format(value, args);
        } catch (Exception e) {
            return value;
        }
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public String getCurrentLanguageCode() {
        return currentLocale.getLanguage();
    }

    /**
     * 获取所有需要的字符集，用于字体生成
     * 包含默认ASCII字符和所有可能在UI中使用的中文字符
     */
    public static String getAllCharacters() {
        synchronized (cacheLock) {
            if (cachedCharacterSet == null) {
                Set<Character> charSet = new HashSet<>();

                // 添加默认ASCII字符
                for (char c : com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.DEFAULT_CHARS.toCharArray()) {
                    charSet.add(c);
                }

                // 从所有翻译文件中提取字符
                extractCharactersFromTranslations(charSet);

                // 转换为字符串
                StringBuilder sb = new StringBuilder(charSet.size());
                for (char c : charSet) {
                    sb.append(c);
                }

                cachedCharacterSet = sb.toString();
            }
            return cachedCharacterSet;
        }
    }

    /**
     * 从所有翻译文件中提取字符
     */
    private static void extractCharactersFromTranslations(Set<Character> charSet) {
        // 尝试加载所有可能的翻译文件
        String[] languageCodes = {"zh", "en"}; // 可以根据需要添加其他语言

        for (String langCode : languageCodes) {
            try {
                FileHandle fileHandle = Gdx.files.internal(BUNDLE_PATH + "_" + langCode + ".properties");
                if (fileHandle.exists()) {
                    String content = fileHandle.readString("UTF-8");
                    // 提取文件中的所有非ASCII字符和关键ASCII字符
                    for (char c : content.toCharArray()) {
                        // 跳过控制字符和属性文件中的特殊字符
                        if (c > 32) { // 排除控制字符
                            charSet.add(c);
                        }
                    }
                }
            } catch (Exception e) {
                // 忽略文件不存在或读取错误的情况
            }
        }

        // 尝试加载默认翻译文件
        try {
            FileHandle fileHandle = Gdx.files.internal(BUNDLE_PATH + ".properties");
            if (fileHandle.exists()) {
                String content = fileHandle.readString("UTF-8");
                for (char c : content.toCharArray()) {
                    if (c > 32) {
                        charSet.add(c);
                    }
                }
            }
        } catch (Exception e) {
            // 忽略错误
        }
    }
}
