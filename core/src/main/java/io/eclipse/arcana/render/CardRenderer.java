package io.eclipse.arcana.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.eclipse.arcana.model.Card;

public class CardRenderer {

    public static final float CARD_W = 160f;
    public static final float CARD_H = CARD_W * 2.09f;

    private static final float COST_SIZE   = 48f;
    private static final float COST_OFFSET = 14f;

    private static final Color COL_FILL      = new Color(0.08f, 0.08f, 0.10f, 1f);
    private static final Color COL_MAJOR     = new Color(1.0f,  0.84f, 0.0f,  1f);
    private static final Color COL_WANDS     = new Color(0.9f,  0.45f, 0.1f,  1f);
    private static final Color COL_SWORDS    = new Color(0.4f,  0.6f,  0.9f,  1f);
    private static final Color COL_CUPS      = new Color(0.9f,  0.3f,  0.5f,  1f);
    private static final Color COL_PENTACLES = new Color(0.3f,  0.8f,  0.3f,  1f);
    private static final Color COL_REVERSED  = new Color(0.8f,  0.1f,  0.1f,  0.85f);
    private static final Color COL_EFFECT_POSITIVE = new Color(0.20f, 0.92f, 0.48f, 1f);
    private static final Color COL_EFFECT_NEGATIVE = new Color(0.95f, 0.25f, 0.28f, 1f);
    private static final Color COL_EFFECT_LOCKED = new Color(0.70f, 0.32f, 1f, 1f);
    private static final Color COL_EFFECT_SPECIAL = new Color(0.20f, 0.82f, 1f, 1f);

    private static final Color COL_BACK_BORDER = new Color(0.5f, 0.45f, 0.25f, 1f);
    private static final Color COL_BACK_FILL   = new Color(0.07f, 0.09f, 0.15f, 1f);

    private static final GlyphLayout LAYOUT = new GlyphLayout();

    // 앞면 렌더

    /**
     * 카드 전체 렌더
     */
    public static void draw(ShapeRenderer sr, SpriteBatch batch, BitmapFont font,
                            Card card, float x, float y) {
        drawShape(sr, card, x, y);
        // shape은 sr.begin() 블록, text는 batch.begin() 블록 — 호출부에서 분리해야 함
        // 이 메서드는 편의용이 아니라 아래 두 개를 각각 쓸 것
    }

    /**
     * 카드 일러스트 텍스처로 렌더
     * SpriteBatch.begin() 블록 안에서 호출
     * illust가 null이면 아무것도 안 그림 → drawShape으로 대신 그릴 것
     */
    public static void drawIllust(SpriteBatch batch, Texture illust, float x, float y, boolean reversed) {
        if (illust == null) return;
        if (reversed) {
            batch.draw(illust,
                x, y, CARD_W, CARD_H,
                0, 0, illust.getWidth(), illust.getHeight(),
                false, true);
        } else {
            batch.draw(illust, x, y, CARD_W, CARD_H);
        }
    }

    /**
     * 코스트 이미지를 카드의 좌상단에 배치하는 메소드
     * @param batch
     * @param font
     * @param costTex
     * @param card
     * @param x
     * @param y
     * @param size
     */
    public static void drawCost(SpriteBatch batch, BitmapFont font,
                                Texture costTex, Card card, int effectiveCost,
                                float x, float y, float size) {
        float cx = x - COST_OFFSET;
        float cy = y + CARD_H - size + COST_OFFSET;

        if (costTex != null) {
            batch.draw(costTex, cx, cy, size, size);
        } else {
            Color borderCol = borderColor(card);
            font.setColor(borderCol);
            String costStr = String.valueOf(effectiveCost);
            LAYOUT.setText(font, costStr);
            font.draw(batch, costStr,
                x + CARD_W - 14 - LAYOUT.width / 2f,
                y + 14 + LAYOUT.height / 2f);
            font.setColor(Color.WHITE);
        }
    }

    // Shape 렌더

    /**
     * Shape Renderer
     * @param sr
     * @param card
     * @param x
     * @param y
     */
    public static void drawShape(ShapeRenderer sr, Card card, float x, float y) {
        Color border = borderColor(card);

        sr.setColor(border);
        sr.rect(x, y, CARD_W, CARD_H);

        sr.setColor(COL_FILL);
        sr.rect(x + 2, y + 2, CARD_W - 4, CARD_H - 4);

        if (card.reversed) {
            sr.setColor(COL_REVERSED);
            sr.rect(x + 4, y + 4, CARD_W - 8, CARD_H - 8);
            sr.setColor(COL_FILL);
            sr.rect(x + 6, y + 6, CARD_W - 12, CARD_H - 12);
        }

        // 기존 코스트 원 — shape fallback일 때만 그림
        float cx = x + CARD_W - 14, cy = y + 14;
        sr.setColor(border);
        sr.circle(cx, cy, 10f, 16);
        sr.setColor(COL_FILL);
        sr.circle(cx, cy, 8f, 16);
    }

    /**
     * 카드 뒷면 shape
     * ShapeRenderer.begin(Filled) 블록 안에서 호출
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

    public static void drawEffectBorder(ShapeRenderer sr, Card card, float x, float y) {
        drawEffectBorder(sr, card, x, y, false);
    }

    public static void drawEffectBorder(ShapeRenderer sr, Card card, float x, float y,
                                        boolean forcePositive) {
        Color color = forcePositive ? COL_EFFECT_POSITIVE : effectColor(card);
        if (color == null) return;

        sr.setColor(color.r, color.g, color.b, 0.08f);
        sr.rect(x - 10f, y - 10f, CARD_W + 20f, CARD_H + 20f);
        sr.setColor(color.r, color.g, color.b, 0.18f);
        sr.rect(x - 6f, y - 6f, CARD_W + 12f, CARD_H + 12f);
        sr.setColor(0.03f, 0.025f, 0.07f, 0.96f);
        sr.rect(x - 2f, y - 2f, CARD_W + 4f, CARD_H + 4f);

        float corner = 25f;
        float thickness = 3f;
        sr.setColor(color.r, color.g, color.b, 0.90f);
        sr.rect(x - 6f, y - 6f, corner, thickness);
        sr.rect(x - 6f, y - 6f, thickness, corner);
        sr.rect(x + CARD_W + 6f - corner, y - 6f, corner, thickness);
        sr.rect(x + CARD_W + 3f, y - 6f, thickness, corner);
        sr.rect(x - 6f, y + CARD_H + 3f, corner, thickness);
        sr.rect(x - 6f, y + CARD_H + 6f - corner, thickness, corner);
        sr.rect(x + CARD_W + 6f - corner, y + CARD_H + 3f, corner, thickness);
        sr.rect(x + CARD_W + 3f, y + CARD_H + 6f - corner, thickness, corner);
    }

    // 텍스트 렌더

    /**
     * 카드 앞면 텍스트 — shape fallback일 때 사용
     * 텍스처 렌더 시에는 drawCost()만 호출하면 됨
     * SpriteBatch.begin() 블록 안에서 호출
     */
    public static void drawText(SpriteBatch batch, BitmapFont font, Card card, float x, float y) {
        Color borderCol = borderColor(card);

        // 카드 이름 — 상단
        font.setColor(1f, 1f, 1f, 1f);
        String name = truncate(font, card.name, CARD_W - 8);
        LAYOUT.setText(font, name);
        font.draw(batch, name, x + (CARD_W - LAYOUT.width) / 2f, y + CARD_H - 4f);

        // 소멸 마커
        if (card.isExtinction) {
            font.setColor(COL_MAJOR);
            font.draw(batch, "★", x + 3f, y + CARD_H - 4f);
        }

        // 타입/수트 라벨 — 중앙
        font.setColor(0.55f, 0.55f, 0.55f, 1f);
        String label = card.type == Card.ArcanaType.MAJOR ? "Major" : cap(card.suit.name());
        LAYOUT.setText(font, label);
        font.draw(batch, label,
            x + (CARD_W - LAYOUT.width) / 2f,
            y + CARD_H / 2f + LAYOUT.height / 2f);

        // 코스트 숫자 — shape fallback용 (drawCost의 else 브랜치와 중복되므로
        // drawShape + drawText 세트로 쓸 때만 호출)
        font.setColor(borderCol);
        String costStr = String.valueOf(card.cost);
        LAYOUT.setText(font, costStr);
        font.draw(batch, costStr,
            x + CARD_W - 14 - LAYOUT.width / 2f,
            y + 14 + LAYOUT.height / 2f);

        font.setColor(Color.WHITE);
    }

    // 내부 유틸

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

    private static Color effectColor(Card card) {
        if (card.lockedInHand || card.effectMark == Card.EffectMark.LOCKED) return COL_EFFECT_LOCKED;
        if (card.effectMark == Card.EffectMark.NEGATIVE) return COL_EFFECT_NEGATIVE;
        if (card.effectMark == Card.EffectMark.POSITIVE) return COL_EFFECT_POSITIVE;
        if (card.effectMark == Card.EffectMark.SPECIAL || card.isCloned || card.isIllusion) return COL_EFFECT_SPECIAL;
        return null;
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
