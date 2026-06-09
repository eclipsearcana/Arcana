package io.eclipse.arcana;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class LoadingBar extends Actor {
    public static final String ASSET_ROOT = "loadingbar_layers_libgdx/";
    public static final String TRACK_PATH = ASSET_ROOT + "loading_track_bg.png";
    public static final String FILL_PATH = ASSET_ROOT + "loading_fill_full.png";
    public static final String FRAME_PATH = ASSET_ROOT + "loading_frame_overlay.png";
    public static final String EDGE_GLOW_PATH = ASSET_ROOT + "loading_edge_glow.png";

    public static final int SRC_W = 2048;
    public static final int SRC_H = 682;
    public static final int SLOT_X = 145;
    public static final int SLOT_Y_FROM_TOP = 287;
    public static final int SLOT_W = 1758;
    public static final int SLOT_H = 80;

    private final Texture trackTexture;
    private final Texture fillTexture;
    private final Texture frameTexture;
    private final Texture edgeGlowTexture;
    private final TextureRegion fillRegion;

    // Gradient & Particle visual effects
    private final Texture whiteTexture;
    private final Texture starTexture;
    private final Sprite gradientSprite;
    private final com.badlogic.gdx.utils.Array<StarParticle> particles = new com.badlogic.gdx.utils.Array<>();
    private float particleTimer;
    private float stateTime;

    private float progress;
    private float displayedProgress;
    private float smoothingSpeed = 6f;
    private boolean smoothProgress = true;
    private boolean drawEdgeGlow = true;
    private boolean disposed;

    private static class StarParticle {
        float x;
        float y;
        float vx;
        float vy;
        float size;
        float life;
        float maxLife;
        float rotation;
        float spinSpeed;
    }

    public LoadingBar() {
        trackTexture = loadTexture(TRACK_PATH);
        fillTexture = loadTexture(FILL_PATH);
        frameTexture = loadTexture(FRAME_PATH);
        edgeGlowTexture = loadTexture(EDGE_GLOW_PATH);
        fillRegion = new TextureRegion(fillTexture);
        
        // Load star texture from TitleA (menu star)
        starTexture = loadTexture("TitleA/starA.png");

        // Create 1x1 white texture for custom gradient rendering
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fill();
        whiteTexture = new Texture(pixmap);
        pixmap.dispose();
        gradientSprite = new Sprite(whiteTexture);

        setSize(SRC_W, SRC_H);
    }

    private Texture loadTexture(String path) {
        Texture texture = new Texture(ArcanaFiles.asset(path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        return texture;
    }

    public void setProgress(float value) {
        progress = MathUtils.clamp(value, 0f, 1f);
        if (!smoothProgress) displayedProgress = progress;
    }

    public float getProgress() {
        return progress;
    }

    public float getDisplayedProgress() {
        return displayedProgress;
    }

    public void setSmoothProgress(boolean enabled) {
        smoothProgress = enabled;
        if (!enabled) displayedProgress = progress;
    }

    public boolean isSmoothProgress() {
        return smoothProgress;
    }

    public void setSmoothingSpeed(float speed) {
        smoothingSpeed = Math.max(0f, speed);
    }

    public float getSmoothingSpeed() {
        return smoothingSpeed;
    }

    public void setDrawEdgeGlow(boolean enabled) {
        drawEdgeGlow = enabled;
    }

    public boolean isDrawEdgeGlow() {
        return drawEdgeGlow;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!smoothProgress) {
            displayedProgress = progress;
        } else {
            float speed = progress >= 1f ? Math.max(12f, smoothingSpeed * 2f) : smoothingSpeed;
            displayedProgress = MathUtils.lerp(
                displayedProgress,
                progress,
                MathUtils.clamp(delta * speed, 0f, 1f)
            );
            if (Math.abs(displayedProgress - progress) < 0.0005f) displayedProgress = progress;
        }

        stateTime += delta;

        // Update active particles
        float currentFillW = SLOT_W * displayedProgress;
        for (int i = particles.size - 1; i >= 0; i--) {
            StarParticle p = particles.get(i);
            p.x += p.vx * delta;
            p.y += p.vy * delta;
            p.rotation += p.spinSpeed * delta;

            if (p.x > currentFillW) {
                p.life -= delta * 4f; // accelerate fade-out when past the edge
            } else {
                p.life -= delta;
            }

            if (p.life <= 0f) {
                particles.removeIndex(i);
            }
        }

        // Spawn new particles inside the active fill bar region
        if (currentFillW > 5f && !disposed) {
            particleTimer += delta;
            float spawnInterval = 0.04f; // spawn a particle every 40ms
            while (particleTimer >= spawnInterval) {
                particleTimer -= spawnInterval;
                if (particles.size < 60) {
                    StarParticle p = new StarParticle();
                    // Spawn: 30% near leading edge for spark effect, 70% randomly distributed
                    if (MathUtils.randomBoolean(0.3f)) {
                        p.x = MathUtils.random(Math.max(0f, currentFillW - 80f), currentFillW);
                        p.vx = MathUtils.random(20f, 60f); // drift slowly with the front edge
                    } else {
                        p.x = MathUtils.random(0f, currentFillW);
                        p.vx = MathUtils.random(60f, 150f); // flow quickly to the right
                    }

                    p.y = MathUtils.random(8f, SLOT_H - 8f);
                    p.vy = MathUtils.random(-15f, 15f);
                    p.size = MathUtils.random(6f, 16f);
                    p.maxLife = MathUtils.random(0.6f, 1.2f);
                    p.life = p.maxLife;
                    p.rotation = MathUtils.random(0f, 360f);
                    p.spinSpeed = MathUtils.random(-120f, 120f);
                    particles.add(p);
                }
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float oldR = batch.getColor().r;
        float oldG = batch.getColor().g;
        float oldB = batch.getColor().b;
        float oldA = batch.getColor().a;
        float alpha = parentAlpha * getColor().a;
        batch.setColor(getColor().r, getColor().g, getColor().b, alpha);

        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();
        float scaleX = width / SRC_W;
        float scaleY = height / SRC_H;

        // 1) Background track
        batch.draw(trackTexture, x, y, width, height);

        int visibleSrcW = Math.round(SLOT_W * displayedProgress);
        if (visibleSrcW > 0) {
            // 2) Base fill bar
            fillRegion.setRegion(SLOT_X, SLOT_Y_FROM_TOP, visibleSrcW, SLOT_H);

            float slotLocalX = SLOT_X * scaleX;
            float slotLocalY = (SRC_H - SLOT_Y_FROM_TOP - SLOT_H) * scaleY;
            float visibleLocalW = visibleSrcW * scaleX;
            float slotLocalW = SLOT_W * scaleX;
            float slotLocalH = SLOT_H * scaleY;

            batch.draw(fillRegion, x + slotLocalX, y + slotLocalY, visibleLocalW, slotLocalH);

            // 3) Gradient Overlay & Particles (Additive Blending)
            int srcFunc = batch.getBlendSrcFunc();
            int destFunc = batch.getBlendDstFunc();
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE); // Additive blending

            // Animated gradient: Purple (left) to Gold (right), shifting alpha over time
            float pulse = (float) Math.sin(stateTime * 3.5f) * 0.08f + 0.92f;
            Color purple = new Color(0.45f, 0.08f, 0.75f, 0.5f * pulse);
            Color gold = new Color(0.95f, 0.78f, 0.18f, 0.65f * pulse);

            drawGradientRect(batch, x + slotLocalX, y + slotLocalY, visibleLocalW, slotLocalH,
                purple, purple, gold, gold);

            // Flowing star particles
            for (StarParticle p : particles) {
                float ratio = p.life / p.maxLife;
                float pAlpha = ratio < 0.2f ? (ratio / 0.2f) : (ratio > 0.8f ? (1f - ratio) / 0.2f : 1f);
                pAlpha = MathUtils.clamp(pAlpha, 0f, 1f) * alpha;

                if (pAlpha > 0.01f) {
                    float pW = p.size * scaleX;
                    float pH = p.size * scaleY;
                    float pX = x + slotLocalX + p.x * scaleX - pW * 0.5f;
                    float pY = y + slotLocalY + p.y * scaleY - pH * 0.5f;

                    batch.setColor(1f, 1f, 1f, pAlpha);
                    batch.draw(
                        starTexture,
                        pX, pY,
                        pW * 0.5f, pH * 0.5f,
                        pW, pH,
                        1f, 1f,
                        p.rotation,
                        0, 0,
                        starTexture.getWidth(), starTexture.getHeight(),
                        false, false
                    );
                }
            }

            // Restore batch blend function and color
            batch.setBlendFunction(srcFunc, destFunc);
            batch.setColor(getColor().r, getColor().g, getColor().b, alpha);

            // 4) Front Edge Glow (only if enabled)
            if (drawEdgeGlow) {
                float glowW = edgeGlowTexture.getWidth() * scaleX;
                float glowH = edgeGlowTexture.getHeight() * scaleY;
                float edgeX = x + slotLocalX + visibleLocalW;
                float minEdgeX = x + slotLocalX + glowW * 0.5f;
                float maxEdgeX = x + slotLocalX + slotLocalW - glowW * 0.5f;
                edgeX = MathUtils.clamp(edgeX, minEdgeX, Math.max(minEdgeX, maxEdgeX));
                float edgeY = y + slotLocalY + slotLocalH * 0.5f;

                batch.draw(
                    edgeGlowTexture,
                    edgeX - glowW * 0.5f,
                    edgeY - glowH * 0.5f,
                    glowW,
                    glowH
                );
            }
        }

        // 5) Frame overlay always on top
        batch.draw(frameTexture, x, y, width, height);
        batch.setColor(oldR, oldG, oldB, oldA);
    }

    private void drawGradientRect(Batch batch, float x, float y, float width, float height,
                                  Color c1, Color c2, Color c3, Color c4) {
        gradientSprite.setBounds(x, y, width, height);
        float[] vertices = gradientSprite.getVertices();
        vertices[2] = c1.toFloatBits();  // bottom left
        vertices[7] = c2.toFloatBits();  // top left
        vertices[12] = c3.toFloatBits(); // top right
        vertices[17] = c4.toFloatBits(); // bottom right
        gradientSprite.draw(batch);
    }

    public void dispose() {
        if (disposed) return;
        disposed = true;
        trackTexture.dispose();
        fillTexture.dispose();
        frameTexture.dispose();
        edgeGlowTexture.dispose();
        whiteTexture.dispose();
        starTexture.dispose();
    }
}

