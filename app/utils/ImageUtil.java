package utils;

import enums.ImageSize;
import org.imgscalr.Scalr;
import play.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for image transformations
 *
 * @author joel
 *
 */
public class ImageUtil {

	private ImageUtil(){}

	/**
	 * Read an image from the given stream, scale it, and return
	 * an input stream for reading the scaled image
	 *
	 * @param in Input stream
	 * @param contentType Ex: image/jpeg
	 * @param size Maximum scaled width or height
	 * @return Scaled image as input stream
	 */
	public static InputStream scale(InputStream in, String contentType, ImageSize size) {

		BufferedImage image = imageFromStream(in);
		BufferedImage resizedImage = Scalr.resize(image, size.toInteger());

		return inputStreamFromBufferedImage(resizedImage, contentType);
	}

    /**
     *
     * Scales and square crops an image.
     * Useful for thumbnails that need to be consistent in aspect ratios
     *
     * @param in Input stream
     * @param contentType Ex: image/jpeg
     * @param size maximum size of square cropped image
     * @return Scaled and cropped image as input stream
     */
    public static InputStream scaleAndCrop(InputStream in, String contentType, ImageSize size) {

        BufferedImage image = imageFromStream(in);

        int width, height;
        if (image.getWidth() < image.getHeight()) {
            width = size.toInteger();
            height = Math.round((float) width / image.getWidth() * image.getHeight());
        } else {
            height = size.toInteger();
            width = Math.round((float) height / image.getHeight() * image.getWidth());
        }

        BufferedImage resizedImage = Scalr.resize(image, width, height);
        resizedImage = squareCropImage(resizedImage);

        return inputStreamFromBufferedImage(resizedImage, contentType);
    }

    private static ByteArrayInputStream inputStreamFromBufferedImage(BufferedImage image, String contentType) {
        byte[] buffer = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            String fmt = formatNameFromContentType(contentType);
            ImageIO.write(image, fmt, baos );
            baos.flush();
            buffer = baos.toByteArray();
            baos.close();
        } catch (IOException e) {
            Logger.error("Unable to resize image: %s", e.getMessage());
            return null;
        }

        return new ByteArrayInputStream(buffer);
    }

    /**
     *
     * Square crops an image
     *
     * @param image The image to be scaled
     * @return The cropped image
     */
    private static BufferedImage squareCropImage(BufferedImage image) {
        BufferedImage croppedImage = null;

        int size;
        int fat;
        int remainder;
        if (image.getWidth() > image.getHeight()) {
            size = image.getHeight();
            fat = (image.getWidth() - size) / 2;
            remainder = (image.getWidth() - size) % 2;
            croppedImage = image.getSubimage(fat + remainder, 0, size, size);
        } else {
            size = image.getWidth();
            fat = (image.getHeight() - size) / 2;
            remainder = (image.getHeight() - size) % 2;
            croppedImage = image.getSubimage(0, fat + remainder, size, size);
        }

        return croppedImage;
    }

	private static BufferedImage imageFromStream(InputStream in){
		try {
			return ImageIO.read(in);
		} catch (IOException e) {
			Logger.error("Unable to read image: %s", e.getMessage());
			return null;
		}
	}

	private static String formatNameFromContentType(String contentType){
		final String VALID_PREFIX = "image/";

		if((contentType == null) || !(contentType.startsWith(VALID_PREFIX))){
			return null;
		}
		String type = contentType.substring(VALID_PREFIX.length());
		return type;

	}

}
