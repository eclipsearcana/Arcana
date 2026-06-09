package io.eclipse.arcana;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

import io.eclipse.arcana.model.Card;
import io.eclipse.arcana.model.Suit;

public class ArcanaAssets {

    private boolean queued;

    private final AssetManager manager = new AssetManager(new FileHandleResolver() {
        @Override
        public FileHandle resolve(String fileName) {
            return ArcanaFiles.asset(fileName);
        }
    });

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
        if (queued) return;
        queued = true;

        queueTitleSet("TitleA/", "A");
        queueTitleSet("TitleB/", "B");
        queueTexture("choose/background_choose.png");
        queueTexture("choose/wands.png");
        queueTexture("choose/wands_back.png");
        queueTexture("choose/WANDS_transparent.png");
        queueTexture("choose/WANDS_features.png");
        queueTexture("choose/swords.png");
        queueTexture("choose/swords_back.png");
        queueTexture("choose/SWORDS_transparent.png");
        queueTexture("choose/SWORDS_features.png");
        queueTexture("choose/pentacles.png");
        queueTexture("choose/pentacles_back.png");
        queueTexture("choose/PENTACLES_transparent.png");
        queueTexture("choose/PENTACLES_features.png");
        queueTexture("choose/cups.png");
        queueTexture("choose/cups_back.png");
        queueTexture("choose/CUPS_transparent.png");
        queueTexture("choose/CUPS_features.png");

        // 배경 일러스트
        String bg = "ui/Game_Background.png";
        if (fileExists(bg)) manager.load(bg, Texture.class);
        queueTexture("ui/Draft_Background.png");
        queueTexture("ui/StatFrame.png");
        queueTexture("ui/HPBar.png");
        queueTexture("ui/CostUI.png");
        for (Suit suit : SUITS) {
            queueTexture("ui/rune/" + suit.name().toLowerCase() + "_masked.png");
        }

        // 뒷면 일러스트
        String back = "cards/back.png";
        if (fileExists(back)) manager.load(back, Texture.class);

        // 메이저 카드 일러스트
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

        // 마이너 카드 일러스트
        for (Suit suit : SUITS) {
            String suitName = cap(suit.name());
            for (String rank : MINOR_RANKS) {
                String png = "cards/minor/" + suitName + "/" + rank + ".png";
                String jpg = "cards/minor/" + suitName + "/" + rank + ".jpg";
                if      (fileExists(png)) manager.load(png, Texture.class);
                else if (fileExists(jpg)) manager.load(jpg, Texture.class);
            }

            // 마이너 카드 코스트 이러스트
            for (int cost = 1; cost <= 4; cost++) {
                String costPath = "cards/minor/Cost/" + suitName + "_" + cost + ".png";
                if (fileExists(costPath)) manager.load(costPath, Texture.class);

                // 역방향 코스트는 4만 존재함
                if (cost == 4) {
                    String revPath = "cards/minor/Cost/" + suitName + "_4_Reverse.png";
                    if (fileExists(revPath)) manager.load(revPath, Texture.class);
                }
            }
        }
    }

    private void queueTitleSet(String folder, String suffix) {
        queueTexture(folder + "title" + suffix + ".png");
        queueTexture(folder + "logo" + suffix + ".png");
        queueTexture(folder + "start" + suffix + ".png");
        queueTexture(folder + "settings" + suffix + ".png");
        queueTexture(folder + "exit" + suffix + ".png");
        queueTexture(folder + "star" + suffix + ".png");
    }

    private void queueTexture(String path) {
        if (fileExists(path) && !manager.isLoaded(path)) {
            manager.load(path, Texture.class);
        }
    }

    public float update() {
        manager.update();
        return manager.getProgress();
    }

    public boolean updateStep() {
        return manager.update();
    }

    public float progress() {
        return manager.getProgress();
    }

    public void finishLoading() {
        queueAll();
        manager.finishLoading();
    }

    // 텍스쳐 반환
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
        return cardCost(card, card.effectiveCost());
    }

    public Texture cardCost(Card card, int effectiveCost) {
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
            int imageCost = effectiveCost >= 1 && effectiveCost <= 4 ? effectiveCost : card.cost;
            String base = "cards/minor/Cost/" + suitName + "_" + imageCost + ".png";

            if (card.reversed && imageCost == 4) {
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

    public Texture draftBackground() {
        String path = "ui/Draft_Background.png";
        return manager.isLoaded(path) ? manager.get(path, Texture.class) : null;
    }

    public Texture hpBar() {
        String path = "ui/HPBar.png";
        return manager.isLoaded(path) ? manager.get(path, Texture.class) : null;
    }

    public Texture statFrame() {
        String path = "ui/StatFrame.png";
        return manager.isLoaded(path) ? manager.get(path, Texture.class) : null;
    }

    public Texture costUi() {
        String path = "ui/CostUI.png";
        return manager.isLoaded(path) ? manager.get(path, Texture.class) : null;
    }

    public Texture rune(Suit suit) {
        if (suit == null) return null;
        String path = "ui/rune/" + suit.name().toLowerCase() + "_masked.png";
        return manager.isLoaded(path) ? manager.get(path, Texture.class) : null;
    }

    public Texture chooseTexture(String name) {
        String path = "choose/" + name + ".png";
        return manager.isLoaded(path) ? manager.get(path, Texture.class) : null;
    }

    public Texture titleTexture(String folder, String name, String suffix) {
        String path = folder + name + suffix + ".png";
        return manager.isLoaded(path) ? manager.get(path, Texture.class) : null;
    }

    public void dispose() {
        manager.dispose();
    }

    // png -> jpg 순차 로드
    private Texture getLoaded(String png, String jpg) {
        if (manager.isLoaded(png)) return manager.get(png, Texture.class);
        if (manager.isLoaded(jpg)) return manager.get(jpg, Texture.class);
        return null;
    }

    private boolean fileExists(String path) {
        return ArcanaFiles.asset(path).exists();
    }

    private String cap(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}
