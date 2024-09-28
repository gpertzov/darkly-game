package net.gpdev.darkly;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;

import static com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

/**
 * Fill-bar Widget
 */
public class FillBarWidget extends WidgetGroup {
    private final Image fillBarImage;

    public FillBarWidget(final AtlasRegion barRegion, final AtlasRegion fillbarRegion, final Color color) {
        final Image barImage = new Image(barRegion);
        fillBarImage = new Image(fillbarRegion);
        fillBarImage.setPosition(fillbarRegion.offsetX, fillbarRegion.offsetY);
        addActor(barImage);
        addActor(fillBarImage);
        fillBarImage.setColor(color);
    }

    /**
     * Set fill-bar length as fraction of widget size
     *
     * @param value 0.0f - 1.0f
     */
    public void setValue(final float value) {
        fillBarImage.setWidth(fillBarImage.getPrefWidth() * value);
    }
}
