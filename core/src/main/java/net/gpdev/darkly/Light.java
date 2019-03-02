package net.gpdev.darkly;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class Light {
    private final Sprite sprite;
    private boolean enabled;
    private final boolean functional;

    public Light(final Sprite sprite, final boolean enabled) {
        this(sprite, enabled, false);
    }

    public Light(final Sprite sprite, final boolean enabled, final boolean functional) {
        this.sprite = sprite;
        this.enabled = enabled;
        this.functional = functional;
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
}
