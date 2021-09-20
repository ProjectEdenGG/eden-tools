package gg.projecteden.tools;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static gg.projecteden.tools.ImageUtils.hex;

public class Presents {

	private enum PresentSide {
		BOW,
		LINE,
		CROSS,
		COMBINED,
	}

	private enum PresentLine {
		INLINE,
		OUTLINE,
	}

	@Getter
	@AllArgsConstructor
	private enum PresentColor {
		RED(new Color(142, 32, 32)), // #8e2020
		ORANGE(new Color(224, 97, 0)), // #e06100
		YELLOW(new Color(240, 175, 21)), // #f0af15
		LIME(new Color(94, 168, 24)), // #5ea818
		BLUE(new Color(44, 46, 143)), // #2c2e8f
		CYAN(new Color(21, 119, 136)), // #157788
		PURPLE(new Color(100, 31, 156)), // #641f9c
		MAGENTA(new Color(169, 48, 159)), // #a9309f
		PINK(new Color(213, 101, 142)), // #d5658e
		WHITE(new Color(207, 213, 214)), // #cfd5d6
		GRAY(new Color(54, 57, 61)), // #36393d
		BLACK(new Color(8, 10, 15)), // #080a0f
		;

		private final Color color;
	}

	private static final String PRESENTS_PATH = "src/main/resources/presents";
	private static final File TEMPLATES_FOLDER = Paths.get(PRESENTS_PATH + "/templates").toFile();
	private static final File COLORS_FOLDER = Paths.get(PRESENTS_PATH + "/colors").toFile();
	private static final File RESULTS_FOLDER = Paths.get(PRESENTS_PATH + "/generated").toFile();

	@Test
	@SneakyThrows
	void colors() {
		Files.walk(COLORS_FOLDER.toPath()).forEach(path -> {
			try {
				if (!path.toUri().toString().contains(".png"))
					return;

				final String name = path.getFileName().toString().split("\\.")[0];
				final Color average = ImageUtils.average(ImageUtils.read(path.toFile()));
				System.out.printf("%S(new Color(%d, %d, %d)), // %s%n", name, average.getRed(), average.getGreen(), average.getBlue(), hex(average));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	@Test
	void combine() {
		for (PresentLine presentLine : PresentLine.values()) {
			final String lineType = presentLine.name().toLowerCase();
			final BufferedImage bow = ImageUtils.read(TEMPLATES_FOLDER, String.format("%s-%s.png", "bow", lineType));
			final BufferedImage line = ImageUtils.read(TEMPLATES_FOLDER, String.format("%s-%s.png", "line", lineType));
			final BufferedImage cross = ImageUtils.read(TEMPLATES_FOLDER, String.format("%s-%s.png", "cross", lineType));
			final int dimension = bow.getHeight() * 2;
			BufferedImage combined = ImageUtils.newSquareImage(dimension);
			Graphics graphics = combined.getGraphics();
			graphics.drawImage(bow, 0, 0, null);
			graphics.drawImage(line, 0, dimension, null);
			graphics.drawImage(cross, dimension, 0, null);
			ImageUtils.write(combined, TEMPLATES_FOLDER, String.format("%s-%s.png", "combined", lineType));
		}

		System.out.println("Generated combined templates");
	}

	@Test
	void generate() {
		int generated = 0;

		for (PresentSide presentSide1 : PresentSide.values()) {
			for (PresentLine presentLine1 : PresentLine.values()) {
				for (PresentColor color1 : PresentColor.values()) {
					final String side = presentSide1.name().toLowerCase();
					final String line1 = presentLine1.name().toLowerCase();

					final String file1 = String.format("%s-%s.png", side, line1);
					final BufferedImage image1 = ImageUtils.read(TEMPLATES_FOLDER, file1);

					for (PresentLine presentLine2 : PresentLine.values()) {
						if (presentLine1 == presentLine2)
							continue;

						final String line2 = presentLine2.name().toLowerCase();

						for (PresentColor color2 : PresentColor.values()) {
							if (color1 == color2)
								continue;

							final String file2 = String.format("%s-%s.png", side, line2);
							final BufferedImage image2 = ImageUtils.read(TEMPLATES_FOLDER, file2);

							ImageUtils.replace(image1, Color.WHITE, color1.getColor());
							ImageUtils.replace(image2, Color.WHITE, color2.getColor());

							final BufferedImage result = ImageUtils.combine(image1, image2);
							final String file = String.format("%s-%s-%s.png", side, color1, color2);
							ImageUtils.write(result, new File(RESULTS_FOLDER, file.toLowerCase()));
							++generated;
						}
					}
				}
			}
		}

		System.out.println("Generated " + generated + " images");
	}
}
