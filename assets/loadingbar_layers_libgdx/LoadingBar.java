import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;

/**
 * LibGDX Scene2D actor for the separated loading bar.
 * Asset paths assume:
 * assets/loading/loading_track_bg.png
 * assets/loading/loading_fill_full.png
 * assets/loading/loading_frame_overlay.png
 * assets/loading/loading_edge_glow.png
 */
public class LoadingBar extends Actor {
    private final TextureRegion track;
    private final TextureRegion fill;
    private final TextureRegion frame;
    private final TextureRegion edgeGlow;

    // Original source canvas: 2048 x 682.
    // Fillable slot coordinates in source-pixel space. X is from left; Y is from top, like PNG/PIL coordinates.
    private static final float SRC_W = 2048f;
    private static final float SRC_H = 682f;
    private static final float SLOT_X = 145f;
    private static final float SLOT_Y_FROM_TOP = 287f;
    private static final float SLOT_W = 1758f;
    private static final float SLOT_H = 80f;

    private float progress = 0f;

    public LoadingBar() {
        track = new TextureRegion(new Texture(Gdx.files.internal("loading/loading_track_bg.png")));
        fill = new TextureRegion(new Texture(Gdx.files.internal("loading/loading_fill_full.png")));
        frame = new TextureRegion(new Texture(Gdx.files.internal("loading/loading_frame_overlay.png")));
        edgeGlow = new TextureRegion(new Texture(Gdx.files.internal("loading/loading_edge_glow.png")));
    }

    public void setProgress(float value) {
        progress = Math.max(0f, Math.min(1f, value));
    }

    public float getProgress() {
        return progress;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color old = batch.getColor();
        batch.setColor(old.r, old.g, old.b, old.a * parentAlpha);

        float x = getX();
        float y = getY();
        float w = getWidth();
        float h = getHeight();

        // 1) Background track.
        batch.draw(track, x, y, w, h);

        // 2) Fill clipped by progress.
        float sx = w / SRC_W;
        float sy = h / SRC_H;
        float slotX = x + SLOT_X * sx;
        float slotY = y + (SRC_H - SLOT_Y_FROM_TOP - SLOT_H) * sy;
        float slotW = SLOT_W * sx;
        float slotH = SLOT_H * sy;
        float visibleW = slotW * progress;

        if (visibleW > 0.5f) {
            batch.flush();
            Rectangle clip = new Rectangle(slotX, slotY, visibleW, slotH);
            Rectangle scissors = new Rectangle();
            ScissorStack.calculateScissors(getStage().getCamera(), batch.getTransformMatrix(), clip, scissors);
            if (ScissorStack.pushScissors(scissors)) {
                batch.draw(fill, x, y, w, h);
                batch.flush();
                ScissorStack.popScissors();
            }

            // 3) Optional moving glow at the front edge.
            float glowW = 120f * sx;
            float glowH = 120f * sy;
            float glowX = slotX + visibleW - glowW * 0.5f;
            float glowY = slotY + slotH * 0.5f - glowH * 0.5f;
            batch.draw(edgeGlow, glowX, glowY, glowW, glowH);
        }

        // 4) Frame overlay always on top.
        batch.draw(frame, x, y, w, h);

        batch.setColor(old);
    }

    public void dispose() {
        track.getTexture().dispose();
        fill.getTexture().dispose();
        frame.getTexture().dispose();
        edgeGlow.getTexture().dispose();
    }
}
