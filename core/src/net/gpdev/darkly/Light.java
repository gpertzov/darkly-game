package net.gpdev.darkly;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class Light {
    private final Sprite sprite;
    private boolean enabled;

    public Light(final Sprite sprite, final boolean enabled) {
        this.sprite = sprite;
        this.enabled = enabled;
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

    public void toggle() {
        enabled = !enabled;
    }
}
