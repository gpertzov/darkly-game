package net.gpdev.darkly;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class Light {

    public static final float LIGHT_DEFAULT_INTENSITY = 1f;
    private final Sprite sprite;
    private boolean enabled;
    private final boolean functional;

    private float intensity;

    public Light(final Sprite sprite, final boolean enabled) {
        this(sprite, enabled, false, LIGHT_DEFAULT_INTENSITY);
    }

    public Light(final Sprite sprite, final boolean enabled, final boolean functional, final float intensity) {
        this.sprite = sprite;
        this.enabled = enabled;
        this.functional = functional;
        this.intensity = intensity;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isFunctional() {
        return functional;
    }

    public void toggle() {
        enabled = !enabled;
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }
}
