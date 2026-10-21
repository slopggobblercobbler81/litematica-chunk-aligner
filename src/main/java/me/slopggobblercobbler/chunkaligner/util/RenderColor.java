package me.slopggobblercobbler.chunkaligner.util;

/**
 * Utility for ARGB color packaging, blending, and conversion for OpenGL / Sodium render pipelines.
 */
public record RenderColor(int alpha, int red, int green, int blue) {

    public static final RenderColor SLIME_GREEN = new RenderColor(100, 30, 230, 50);
    public static final RenderColor SLIME_BORDER = new RenderColor(230, 60, 255, 80);
    public static final RenderColor GRID_CYAN = new RenderColor(60, 0, 200, 255);
    public static final RenderColor PORTAL_PURPLE = new RenderColor(140, 180, 40, 255);
    public static final RenderColor FARM_ORANGE = new RenderColor(120, 255, 160, 20);
    public static final RenderColor DESPAWN_RED = new RenderColor(80, 255, 40, 40);

    public static RenderColor fromArgb(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return new RenderColor(a, r, g, b);
    }

    public static RenderColor fromRgb(int rgb, int alpha) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return new RenderColor(alpha, r, g, b);
    }

    public int toArgb() {
        return ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
    }

    public float rFloat() {
        return red / 255.0f;
    }

    public float gFloat() {
        return green / 255.0f;
    }

    public float bFloat() {
        return blue / 255.0f;
    }

    public float aFloat() {
        return alpha / 255.0f;
    }

    public RenderColor withAlpha(int newAlpha) {
        return new RenderColor(newAlpha, red, green, blue);
    }

    public RenderColor lerp(RenderColor target, float factor) {
        float f = Math.clamp(factor, 0.0f, 1.0f);
        int na = Math.round(this.alpha + (target.alpha - this.alpha) * f);
        int nr = Math.round(this.red + (target.red - this.red) * f);
        int ng = Math.round(this.green + (target.green - this.green) * f);
        int nb = Math.round(this.blue + (target.blue - this.blue) * f);
        return new RenderColor(na, nr, ng, nb);
    }
}
