package top.xpyvip.bingWallpaper.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.MediaType;
import top.xpyvip.bingWallpaper.utils.StringUtils;

@Getter
@AllArgsConstructor
public enum ImageType {

    JPG("JPG", MediaType.IMAGE_JPEG_VALUE),
    jpg("jpg", MediaType.IMAGE_JPEG_VALUE),
    JPEG("JPEG", MediaType.IMAGE_JPEG_VALUE),
    jpeg("jpeg", MediaType.IMAGE_JPEG_VALUE),
    PNG("PNG", MediaType.IMAGE_PNG_VALUE),
    png("png", MediaType.IMAGE_PNG_VALUE),
    webp("webp", "image/webp");
    private final String type;
    private final String mediaType;
    public static ImageType find(String typeName) {
        if (StringUtils.isBlank(typeName)) {
            return null;
        }
        for (ImageType type : values()) {
            if (type.getType().equals(typeName)) {
                return type;
            }
        }
        return null;
    }
}
