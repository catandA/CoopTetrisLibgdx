package me.catand.cooptetris.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import me.catand.cooptetris.Main;

/**
 * 字体工具类 - 提供创建带动态字体的UI组件的方法
 * 参考原项目 RenderedText 的实现方式
 */
public class FontUtils {

    /**
     * 创建带动态字体的 TextButton
     * 根据按钮文本内容动态获取字体
     */
    public static TextButton createTextButton(String text, Skin skin, int fontSize, Color color) {
        BitmapFont font = Main.platform.getFont(fontSize, text, false, false);
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        style.font = font;
        TextButton button = new TextButton(text, style);
        button.setColor(color);
        return button;
    }

    /**
     * 创建带动态字体的 Label
     * 根据标签文本内容动态获取字体
     */
    public static Label createLabel(String text, Skin skin, int fontSize, Color color) {
        BitmapFont font = Main.platform.getFont(fontSize, text, false, false);
        Label.LabelStyle style = new Label.LabelStyle(font, color);
        return new Label(text, style);
    }

    /**
     * 创建带动态字体的 SelectBox
     * 根据选项内容动态获取字体，包含所有选项的字符
     */
    public static <T> SelectBox<T> createSelectBox(Skin skin, int fontSize, Color color, T... items) {
        // 收集所有选项的文本
        StringBuilder allText = new StringBuilder();
        if (items != null) {
            for (T item : items) {
                if (item != null) {
                    allText.append(item.toString());
                }
            }
        }

        BitmapFont font = Main.platform.getFont(fontSize, allText.toString(), false, false);
        SelectBox.SelectBoxStyle style = new SelectBox.SelectBoxStyle(skin.get(SelectBox.SelectBoxStyle.class));
        style.font = font;
        style.listStyle.font = font;

        SelectBox<T> selectBox = new SelectBox<>(style);
        if (items != null && items.length > 0) {
            selectBox.setItems(items);
        }
        return selectBox;
    }

    /**
     * 为现有的 SelectBox 更新字体
     * 根据当前选项内容动态获取字体
     */
    public static <T> void updateSelectBoxFont(SelectBox<T> selectBox, Skin skin, int fontSize) {
        StringBuilder allText = new StringBuilder();
        for (T item : selectBox.getItems()) {
            if (item != null) {
                allText.append(item.toString());
            }
        }

        BitmapFont font = Main.platform.getFont(fontSize, allText.toString(), false, false);
        SelectBox.SelectBoxStyle style = new SelectBox.SelectBoxStyle(selectBox.getStyle());
        style.font = font;
        style.listStyle.font = font;
        selectBox.setStyle(style);
    }

    /**
     * 为现有的 TextButton 更新字体
     * 根据按钮文本内容动态获取字体
     */
    public static void updateButtonFont(TextButton button, Skin skin, int fontSize) {
        String text = button.getText().toString();
        BitmapFont font = Main.platform.getFont(fontSize, text, false, false);
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(button.getStyle());
        style.font = font;
        button.setStyle(style);
    }

    /**
     * 为现有的 Label 更新字体
     * 根据标签文本内容动态获取字体
     */
    public static void updateLabelFont(Label label, int fontSize) {
        String text = label.getText().toString();
        BitmapFont font = Main.platform.getFont(fontSize, text, false, false);
        Label.LabelStyle style = new Label.LabelStyle(label.getStyle());
        style.font = font;
        label.setStyle(style);
    }

    /**
     * 创建带动态字体的 TextField
     * 根据messageText和可能输入的字符动态获取字体
     */
    public static TextField createTextField(Skin skin, int fontSize, String messageText, String allowedChars) {
        // 收集所有可能显示的字符：messageText + 允许输入的字符
        String allChars = messageText;
        if (allowedChars != null && !allowedChars.isEmpty()) {
            allChars += allowedChars;
        }
        // 添加常用中文字符（用于输入）
        allChars += "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-.";

        BitmapFont font = Main.platform.getFont(fontSize, allChars, false, false);
        TextField.TextFieldStyle style = new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
        style.font = font;
        style.messageFont = font;

        TextField textField = new TextField("", style);
        if (messageText != null && !messageText.isEmpty()) {
            textField.setMessageText(messageText);
        }
        return textField;
    }

    /**
     * 为现有的 TextField 更新字体
     * 根据当前文本和messageText动态获取字体
     */
    public static void updateTextFieldFont(TextField textField, Skin skin, int fontSize) {
        String text = textField.getText();
        String messageText = textField.getMessageText();
        String allChars = text + messageText;
        // 添加常用中文字符
        allChars += "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-.";

        BitmapFont font = Main.platform.getFont(fontSize, allChars, false, false);
        TextField.TextFieldStyle style = new TextField.TextFieldStyle(textField.getStyle());
        style.font = font;
        style.messageFont = font;
        textField.setStyle(style);
    }
}
