package top.xpyvip.bingWallpaper.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import top.xpyvip.bingWallpaper.utils.StringUtils;

@Getter
@AllArgsConstructor
public enum ImageResolution {

    UHD("UHD", 3840, 2160),
    I1920X1080("1920x1080", 1920, 1080),
    I1366X768("1366x768", 1366, 768),
    I1280X768("1280x768", 1280, 768),
    I1024X768("1024x768", 1024, 768),
    I800X600("800x600", 800, 600),
    I800X480("800x480", 800, 480),
    I768X1280("768x1280", 768, 1280),
    I720X1280("720x1280", 720, 1280),
    I640X480("640x480", 640, 480),
    I480X800("480x800", 480, 800),
    I400X240("400x240", 400, 240),
    I320X240("320x240", 320, 240),
    I240X320("240x320", 240, 320);
    private final String resolution;
    private final int width;
    private final int height;

    public static ImageResolution find(String resolutionName) {
        if (StringUtils.isBlank(resolutionName)) {
            return null;
        }
        for (ImageResolution resolution : values()) {
            if (resolution.getResolution().equals(resolutionName.toLowerCase())) {
                return resolution;
            }
        }
        return null;
    }
}
