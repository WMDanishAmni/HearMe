package com.example.hearme.models;
public class PhraseItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_PHRASE = 1;

    private String text;
    private int type;
    private String category;

    public PhraseItem(String category, String text, int type) {
        this.category = category;
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public int getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }
}
