package io.eclipse.arcana;

import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;

public class FontManager implements Disposable {
    private static final float FONT_SUPERSAMPLE = 2f;

    private static final String[] FONT_CANDIDATES = {
        "fonts/nanum-square-neo/NanumSquareNeo-bRg.ttf",
        "fonts/nanum-square-round/NanumSquareRoundR.ttf",
        "fonts/nanum-square-neo/NanumSquareNeo-cBd.ttf",
        "fonts/nanum-square-round/NanumSquareRoundB.ttf"
    };

    private static final String KOREAN_UI_CHARS =
        "가각간갈감갑강개거검게격결경계고공과관광괴교구국군굴권귀그극근글금기깃"
            + "나난날남내너네녀노누느는늘능니"
            + "다단달담당대더데도동되두드득든들디"
            + "라락랑래러레로루르른를리림"
            + "마막만많말망매머메명모무문물미민"
            + "바박반발방배버법베별보복본부분불브비"
            + "사삭산살상새생서선설성세소속손수순술스습시식신실심십"
            + "아악안알암압앙애액야양어억언얼엄업없에여역연열염영예오와완왕왜외요용우운울움월위유윤율으은을음의이익인일임입있"
            + "자작잔장재저적전절점정제조족종좋주준줄중즈지직진질집"
            + "차착찬참창채처척천철체초최추춘출충치"
            + "카커코크키"
            + "타탄탈탑태턴테토통투트특티"
            + "파판패페포표피필"
            + "하한할함합항해행허험혁현형혜호화확환활황회효후훈훔흐흑흔힘"
            + "ㄱㄴㄷㄹㅁㅂㅅㅇㅈㅊㅋㅌㅍㅎㅏㅑㅓㅕㅗㅛㅜㅠㅡㅣ"
            + "★☆←→↑↓—–…·ㆍ「」『』[]()!?.,:/+-=%";

    /** ~11 world-units — 카드 텍스트, 버튼 라벨, 소형 힌트 */
    public final BitmapFont small;
    /** ~16 world-units — HUD 메인, 턴 화살표 */
    public final BitmapFont normal;
    /** ~40 world-units — 게임오버 타이틀 */
    public final BitmapFont title;
    /** ~14 world-units — 툴팁 본문 */
    public final BitmapFont tooltipBody;
    /** ~20 world-units — 툴팁 제목 */
    public final BitmapFont tooltipTitle;

    private final FreeTypeFontGenerator generator;

    /**
     * @param pxPerUnit 월드 유닛 1당 물리 픽셀 수.
     *                  HdpiMode.Pixels 기준: Gdx.graphics.getWidth() / worldWidth
     */
    public FontManager(float pxPerUnit) {
        generator = loadGenerator();
        small  = make(Math.round(11 * pxPerUnit));
        normal = make(Math.round(16 * pxPerUnit));
        title  = make(Math.round(40 * pxPerUnit));
        tooltipBody = make(Math.round(9 * pxPerUnit));
        tooltipTitle = make(Math.round(13 * pxPerUnit));
    }

    private FreeTypeFontGenerator loadGenerator() {
        for (String path : FONT_CANDIDATES) {
            com.badlogic.gdx.files.FileHandle fontFile = ArcanaFiles.asset(path);
            if (fontFile.exists()) {
                return new FreeTypeFontGenerator(fontFile);
            }
        }

        throw new com.badlogic.gdx.utils.GdxRuntimeException(
            "Missing Korean font in assets/fonts. Tried Nanum Square Neo and Nanum Square Round.");
    }

    private BitmapFont make(int px) {
        FreeTypeFontParameter p = new FreeTypeFontParameter();
        p.size = Math.max(8, Math.round(px * FONT_SUPERSAMPLE));
        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS + KOREAN_UI_CHARS;
        p.incremental = true;  // 한글 등 유니코드 글리프를 필요할 때만 생성
        p.kerning = true;
        p.minFilter = TextureFilter.Linear;
        p.magFilter = TextureFilter.Linear;

        BitmapFont font = generator.generateFont(p);
        font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
        font.getData().setScale(1f / FONT_SUPERSAMPLE);
        return font;
    }

    @Override
    public void dispose() {
        small.dispose();
        normal.dispose();
        title.dispose();
        tooltipBody.dispose();
        tooltipTitle.dispose();
        generator.dispose();  // incremental 폰트는 generator보다 먼저 dispose
    }
}
