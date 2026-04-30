package io.eclipse.arcana;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;

public class FontManager implements Disposable {

    /** ~11 world-units — 카드 텍스트, 버튼 라벨, 소형 힌트 */
    public final BitmapFont small;
    /** ~16 world-units — HUD 메인, 턴 화살표 */
    public final BitmapFont normal;
    /** ~40 world-units — 게임오버 타이틀 */
    public final BitmapFont title;

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
    }

    private FreeTypeFontGenerator loadGenerator() {
        if (Gdx.files.internal("fonts/NotoSansKR-Regular.ttf").exists())
            return new FreeTypeFontGenerator(Gdx.files.internal("fonts/NotoSansKR-Regular.ttf"));
        // Windows 시스템 폰트 fallback (맑은 고딕)
        return new FreeTypeFontGenerator(Gdx.files.absolute("C:/Windows/Fonts/malgun.ttf"));
    }

    private BitmapFont make(int px) {
        FreeTypeFontParameter p = new FreeTypeFontParameter();
        p.size = Math.max(8, px);
        p.incremental = true;  // 한글 등 유니코드 글리프를 필요할 때만 생성
        return generator.generateFont(p);
    }

    @Override
    public void dispose() {
        small.dispose();
        normal.dispose();
        title.dispose();
        generator.dispose();  // incremental 폰트는 generator보다 먼저 dispose
    }
}
