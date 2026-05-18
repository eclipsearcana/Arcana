package io.eclipse.arcana;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

import io.eclipse.arcana.model.Card;
import io.eclipse.arcana.model.Suit;

public class ArcanaAssets {

    private final AssetManager manager = new AssetManager();

    private static final String[] MAJOR_IDS = {
        "Fool", "Magician", "Priestess", "Empress", "Emperor",
        "Hierophant", "Lovers", "Chariot", "Strength", "Hermit",
        "Fortune", "Justice", "HangedMan", "Death", "Temperance",
        "Devil", "Tower", "Star", "Moon", "Sun", "Judgement", "World"
    };

    // minor 랭크 → 파일명
    private static final String[] MINOR_RANKS = {
        "Ace", "Two", "Three", "Four", "Five", "Six", "Seven",
        "Eight", "Nine", "Ten", "Page", "Knight", "Queen", "King"
    };

    private static final Suit[] SUITS = {
        Suit.WANDS, Suit.SWORDS, Suit.CUPS, Suit.PENTACLES
    };

    public void queueAll() {
        // 배경 일러스트
        String bg = "ui/Game_Background.png";
        if (fileExists(bg)) manager.load(bg, Texture.class);

        // ── 뒷면 일러스트 ─────────────────────────────────────────────────────
        String back = "cards/back.png";
        if (fileExists(back)) manager.load(back, Texture.class);

        // ── Major 일러스트 ───────────────────────────────────────────────────
        for (String id : MAJOR_IDS) {
            String png = "cards/major/" + id + ".png";
            String jpg = "cards/major/" + id + ".jpg";
            if      (fileExists(png)) manager.load(png, Texture.class);
            else if (fileExists(jpg)) manager.load(jpg, Texture.class);

            String cost    = "cards/major/Cost/" + id + ".png";
            String costRev = "cards/major/Cost/" + id + "_Reverse.png";  // ← 추가
            if (fileExists(cost))    manager.load(cost,    Texture.class);
            if (fileExists(costRev)) manager.load(costRev, Texture.class);  // ← 추가
        }

        String foolCopy = "cards/major/Cost/Fool_Copy.png";
        if (fileExists(foolCopy)) manager.load(foolCopy, Texture.class);

        String magicianContract = "cards/major/Cost/Magician_Contract.png";
        if (fileExists(magicianContract)) manager.load(magicianContract, Texture.class);

        String magicianIllusion = "cards/major/Cost/Magician_Illusion.png";
        if (fileExists(magicianIllusion)) manager.load(magicianIllusion, Texture.class);

        // ── Minor 일러스트 ───────────────────────────────────────────────────
        for (Suit suit : SUITS) {
            String suitName = cap(suit.name());
            for (String rank : MINOR_RANKS) {
                String png = "cards/minor/" + suitName + "/" + rank + ".png";
                String jpg = "cards/minor/" + suitName + "/" + rank + ".jpg";
                if      (fileExists(png)) manager.load(png, Texture.class);
                else if (fileExists(jpg)) manager.load(jpg, Texture.class);
            }

            // ── Minor 코스트 (수트+코스트 숫자 조합, 1~4) ───────────────────
            for (int cost = 1; cost <= 4; cost++) {
                String costPath = "cards/minor/Cost/" + suitName + "_" + cost + ".png";
                if (fileExists(costPath)) manager.load(costPath, Texture.class);

                // 역방향 코스트는 4만 있음
                if (cost == 4) {
                    String revPath = "cards/minor/Cost/" + suitName + "_4_Reverse.png";
                    if (fileExists(revPath)) manager.load(revPath, Texture.class);
                }
            }
        }
    }

    public float update() {
        manager.update();
        return manager.getProgress();
    }

    public void finishLoading() {
        queueAll();
        manager.finishLoading();
    }

    // ── 텍스처 반환 ───────────────────────────────────────────────────────────

    public Texture cardIllust(Card card) {
        if (card.type == Card.ArcanaType.MAJOR) {
            return getLoaded("cards/major/" + card.id + ".png",
                "cards/major/" + card.id + ".jpg");
        } else {
            // card.id = "Cups/Ace", "Wands/Knight" 등
            return getLoaded("cards/minor/" + card.id + ".png",
                "cards/minor/" + card.id + ".jpg");
        }
    }

    public Texture cardCost(Card card) {
        if (card.isCloned) {
            return manager.get("cards/major/Cost/Fool_Copy.png", Texture.class);
        }

        if (card.powerMultiplier > 1.1f && !card.isIllusion) {
            return manager.get("cards/major/Cost/Magician_Contract.png", Texture.class);
        }

        if (card.isIllusion) {
            return manager.get("cards/major/Cost/Magician_Illusion.png", Texture.class);
        }

        if (card.type == Card.ArcanaType.MAJOR) {
            String base = "cards/major/Cost/" + card.id + ".png";
            String rev  = "cards/major/Cost/" + card.id + "_Reverse.png";

            if (card.reversed && manager.isLoaded(rev)) {
                return manager.get(rev, Texture.class);
            }
            return manager.isLoaded(base) ? manager.get(base, Texture.class) : null;
        } else {
            String suitName = cap(card.suit.name());
            String base = "cards/minor/Cost/" + suitName + "_" + card.cost + ".png";

            if (card.reversed && card.cost == 4) {
                String rev = "cards/minor/Cost/" + suitName + "_4_Reverse.png";
                if (manager.isLoaded(rev)) return manager.get(rev, Texture.class);
            }
            return manager.isLoaded(base) ? manager.get(base, Texture.class) : null;
        }
    }

    public Texture cardBack() {
        String path = "cards/back.png";
        return manager.isLoaded(path) ? manager.get(path, Texture.class) : null;
    }

    public Texture background() {
        String path = "ui/Game_Background.png";
        return manager.isLoaded(path) ? manager.get(path, Texture.class) : null;
    }

    public void dispose() {
        manager.dispose();
    }

    // ── 내부 유틸 ─────────────────────────────────────────────────────────────

    // png 먼저, 없으면 jpg
    private Texture getLoaded(String png, String jpg) {
        if (manager.isLoaded(png)) return manager.get(png, Texture.class);
        if (manager.isLoaded(jpg)) return manager.get(jpg, Texture.class);
        return null;
    }

    private boolean fileExists(String path) {
        return com.badlogic.gdx.Gdx.files.internal(path).exists();
    }

    private String cap(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}
