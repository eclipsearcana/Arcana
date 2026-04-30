package io.eclipse.arcana.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.eclipse.arcana.model.Card;

public class CardRenderer {

    public static final float CARD_W = 90f;
    public static final float CARD_H = 126f;

    private static final Color COL_FILL      = new Color(0.08f, 0.08f, 0.10f, 1f);
    private static final Color COL_MAJOR     = new Color(1.0f,  0.84f, 0.0f,  1f);
    private static final Color COL_WANDS     = new Color(0.9f,  0.45f, 0.1f,  1f);
    private static final Color COL_SWORDS    = new Color(0.4f,  0.6f,  0.9f,  1f);
    private static final Color COL_CUPS      = new Color(0.9f,  0.3f,  0.5f,  1f);
    private static final Color COL_PENTACLES = new Color(0.3f,  0.8f,  0.3f,  1f);
    private static final Color COL_REVERSED  = new Color(0.8f,  0.1f,  0.1f,  0.85f);

    private static final Color COL_BACK_BORDER = new Color(0.5f, 0.45f, 0.25f, 1f);
    private static final Color COL_BACK_FILL   = new Color(0.07f, 0.09f, 0.15f, 1f);

    private static final GlyphLayout LAYOUT = new GlyphLayout();

    /**
     * 카드 도형(배경, 테두리, 코스트 원)을 그린다.
     * ShapeRenderer.begin(Filled) 블록 안에서 호출해야 한다.
     */
    public static void drawShape(ShapeRenderer sr, Card card, float x, float y) {
        Color border = borderColor(card);

        // 테두리 (바깥 사각형)
        sr.setColor(border);
        sr.rect(x, y, CARD_W, CARD_H);

        // 배경 채우기 (2px 안쪽)
        sr.setColor(COL_FILL);
        sr.rect(x + 2, y + 2, CARD_W - 4, CARD_H - 4);

        // 역방향: 빨간 내부 링
        if (card.reversed) {
            sr.setColor(COL_REVERSED);
            sr.rect(x + 4, y + 4, CARD_W - 8, CARD_H - 8);
            sr.setColor(COL_FILL);
            sr.rect(x + 6, y + 6, CARD_W - 12, CARD_H - 12);
        }

        // 코스트 원 (우하단)
        float cx = x + CARD_W - 14, cy = y + 14;
        sr.setColor(border);
        sr.circle(cx, cy, 10f, 16);
        sr.setColor(COL_FILL);
        sr.circle(cx, cy, 8f, 16);
    }

    /**
     * 카드 뒷면 shape
     */
    public static void drawBack(ShapeRenderer sr, float x, float y) {
        sr.setColor(COL_BACK_BORDER);
        sr.rect(x, y, CARD_W, CARD_H);

        sr.setColor(COL_BACK_FILL);
        sr.rect(x + 2, y + 2, CARD_W - 4, CARD_H - 4);

        sr.setColor(COL_BACK_BORDER);
        sr.rect(x + 6,  y + 6,  CARD_W - 12, CARD_H - 12);
        sr.setColor(COL_BACK_FILL);
        sr.rect(x + 8,  y + 8,  CARD_W - 16, CARD_H - 16);
        sr.setColor(COL_BACK_BORDER);
        sr.rect(x + 10, y + 10, CARD_W - 20, CARD_H - 20);
        sr.setColor(COL_BACK_FILL);
        sr.rect(x + 11, y + 11, CARD_W - 22, CARD_H - 22);
    }

    /**
     * 카드 텍스트(이름, 타입 라벨, 코스트, 소멸 마커)를 그린다.
     * SpriteBatch.begin() 블록 안에서 호출해야 한다.
     * font은 FontManager.small 크기를 넘겨줄 것.
     */
    public static void drawText(SpriteBatch batch, BitmapFont font, Card card, float x, float y) {
        Color borderCol = borderColor(card);

        // 카드 이름 — 상단
        font.setColor(1f, 1f, 1f, 1f);
        String name = truncate(font, card.name, CARD_W - 8);
        LAYOUT.setText(font, name);
        font.draw(batch, name, x + (CARD_W - LAYOUT.width) / 2f, y + CARD_H - 4f);

        // 소멸 마커 — 좌상단 (이름과 겹치지 않도록 앞에 배치)
        if (card.isExtinction) {
            font.setColor(COL_MAJOR);
            font.draw(batch, "★", x + 3f, y + CARD_H - 4f);
        }

        // 타입/슈트 라벨 — 중앙
        font.setColor(0.55f, 0.55f, 0.55f, 1f);
        String label = card.type == Card.ArcanaType.MAJOR ? "Major" : cap(card.suit.name());
        LAYOUT.setText(font, label);
        font.draw(batch, label, x + (CARD_W - LAYOUT.width) / 2f, y + CARD_H / 2f + LAYOUT.height / 2f);

        // 코스트 숫자 — 우하단 원 안
        font.setColor(borderCol);
        String costStr = String.valueOf(card.cost);
        LAYOUT.setText(font, costStr);
        font.draw(batch, costStr,
            x + CARD_W - 14 - LAYOUT.width / 2f,
            y + 14 + LAYOUT.height / 2f);

        font.setColor(Color.WHITE);
    }

    private static Color borderColor(Card card) {
        if (card.type == Card.ArcanaType.MAJOR) return COL_MAJOR;
        switch (card.suit) {
            case WANDS:     return COL_WANDS;
            case SWORDS:    return COL_SWORDS;
            case CUPS:      return COL_CUPS;
            case PENTACLES: return COL_PENTACLES;
            default:        return Color.WHITE;
        }
    }

    private static String truncate(BitmapFont font, String text, float maxWidth) {
        LAYOUT.setText(font, text);
        if (LAYOUT.width <= maxWidth) return text;
        for (int len = text.length() - 1; len > 1; len--) {
            String candidate = text.substring(0, len) + "…";
            LAYOUT.setText(font, candidate);
            if (LAYOUT.width <= maxWidth) return candidate;
        }
        return text.substring(0, 1);
    }

    private static String cap(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}
