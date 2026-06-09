package io.eclipse.arcana;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.eclipse.arcana.model.Card;
import io.eclipse.arcana.model.Suit;

public class Core extends Game {
    private static final float BACKGROUND_MUSIC_DURATION = 137.143f;
    private static final float BACKGROUND_MUSIC_VOLUME = 0.34f;
    private static final float BACKGROUND_MUSIC_FADE_IN = 4f;
    private static final float BACKGROUND_MUSIC_FADE_OUT = 6f;
    private static final float BACKGROUND_MUSIC_RESTART_DELAY = 3f;

    public final ArcanaAssets assets = new ArcanaAssets();

    private final DebugContext debugContext;
    private final DebugWindowOpener debugWindowOpener;
    private Music backgroundMusic;
    private float backgroundMusicFadeIn;
    private float backgroundMusicRestartDelay = -1f;

    public Core() {
        this(null);
    }

    public Core(DebugWindowOpener debugWindowOpener) {
        this.debugContext = debugWindowOpener == null ? null : new DebugContext();
        this.debugWindowOpener = debugWindowOpener;
    }

    @Override
    public void create() {
        setScreen(new LoadingScreen(this));
        if (debugWindowOpener != null) {
            debugWindowOpener.open(debugContext);
        }
    }

    @Override
    public void render() {
        updateBackgroundMusic(Gdx.graphics.getDeltaTime());
        super.render();
    }

    public void startBackgroundMusic() {
        if (backgroundMusic == null) {
            backgroundMusic = Gdx.audio.newMusic(ArcanaFiles.asset("music/background.wav"));
            backgroundMusic.setLooping(false);
            backgroundMusic.setOnCompletionListener(music -> {
                music.stop();
                backgroundMusicRestartDelay = BACKGROUND_MUSIC_RESTART_DELAY;
            });
        }
        if (backgroundMusic.isPlaying() || backgroundMusicRestartDelay >= 0f) return;
        playBackgroundMusicFromStart();
    }

    public void stopBackgroundMusic() {
        backgroundMusicRestartDelay = -1f;
        backgroundMusicFadeIn = 0f;
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.setVolume(0f);
        }
    }

    private void updateBackgroundMusic(float delta) {
        if (backgroundMusic == null) return;
        if (backgroundMusicRestartDelay >= 0f) {
            backgroundMusicRestartDelay -= delta;
            if (backgroundMusicRestartDelay <= 0f) {
                backgroundMusicRestartDelay = -1f;
                playBackgroundMusicFromStart();
            }
            return;
        }
        if (!backgroundMusic.isPlaying()) return;

        backgroundMusicFadeIn = Math.min(BACKGROUND_MUSIC_FADE_IN, backgroundMusicFadeIn + delta);
        float fadeIn = MathUtils.clamp(backgroundMusicFadeIn / BACKGROUND_MUSIC_FADE_IN, 0f, 1f);
        float remaining = BACKGROUND_MUSIC_DURATION - backgroundMusic.getPosition();
        float fadeOut = MathUtils.clamp(remaining / BACKGROUND_MUSIC_FADE_OUT, 0f, 1f);
        backgroundMusic.setVolume(BACKGROUND_MUSIC_VOLUME * Math.min(fadeIn, fadeOut));
    }

    private void playBackgroundMusicFromStart() {
        backgroundMusicFadeIn = 0f;
        backgroundMusic.setPosition(0f);
        backgroundMusic.setVolume(0f);
        backgroundMusic.play();
    }

    public void startGame(Suit suit) {
        stopBackgroundMusic();
        setScreen(new MainScreen(this, debugContext, suit));
    }

    public void startGame(Suit suit, Array<Card> playerDraftMajors, Array<Card> opponentDraftMajors) {
        stopBackgroundMusic();
        setScreen(new MainScreen(this, debugContext, suit, playerDraftMajors, opponentDraftMajors));
    }

    public void showTutorial() {
        setScreen(new TutorialScreen(this));
    }

    public void showDeckSelect() {
        setScreen(new MinorDeckSelectScreen(this));
    }

    public void showDraft(Suit suit) {
        setScreen(new DraftScreen(this, suit));
    }

    public void showTitleScreen() {
        setScreen(new LoadingScreen(this));
    }

    public void showTitleScreen(boolean isBright) {
        setScreen(new LoadingScreen(this, isBright));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
            backgroundMusic = null;
        }
        assets.dispose();
    }
}
