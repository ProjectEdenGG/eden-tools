package gg.projecteden.tools;

import gg.projecteden.utils.MathUtils;
import gg.projecteden.utils.RandomUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static gg.projecteden.tools.ColorUtils.hex;

public class Presents {

	private enum PresentSide {
		BOW,
		LINE,
		CROSS,
//		COMBINED,
	}

	private enum PresentLine {
		INLINE,
		OUTLINE,
	}

	@Getter
	@AllArgsConstructor
	private enum PresentColor {
		RED_CONCRETE(new Color(142, 32, 32)), // #8e2020
		ORANGE_TERRACOTTA(new Color(161, 83, 37)), // #a15325
		ORANGE_CONCRETE(new Color(224, 97, 0)), // #e06100
		YELLOW_CONCRETE(new Color(240, 175, 21)), // #f0af15
		LIME_CONCRETE(new Color(94, 168, 24)), // #5ea818
		GREEN_TERRACOTTA(new Color(76, 83, 42)), // #4c532a
		BLUE_CONCRETE(new Color(44, 46, 143)), // #2c2e8f
		CYAN_CONCRETE(new Color(21, 119, 136)), // #157788
		LIGHT_BLUE_TERRACOTTA(new Color(113, 108, 137)), // #716c89
		BLUE_TERRACOTTA(new Color(74, 59, 91)), // #4a3b5b
		PURPLE_CONCRETE(new Color(100, 31, 156)), // #641f9c
		MAGENTA_CONCRETE(new Color(169, 48, 159)), // #a9309f
		PINK_CONCRETE(new Color(213, 101, 142)), // #d5658e
		PINK_TERRACOTTA(new Color(255, 121, 121)), // #a14e4e
		WHITE_CONCRETE(new Color(207, 213, 214)), // #cfd5d6
		CYAN_TERRACOTTA(new Color(86, 91, 91)), // #565b5b
		GRAY_CONCRETE(new Color(54, 57, 61)), // #36393d
		BLACK_CONCRETE(new Color(8, 10, 15)), // #080a0f
		;

		private final Color color;
	}

	private static final String PRESENTS_PATH = "src/main/resources/presents";
	private static final File TEMPLATES_FOLDER = Paths.get(PRESENTS_PATH + "/templates").toFile();
	private static final File BACKGROUNDS_FOLDER = Paths.get(PRESENTS_PATH + "/backgrounds").toFile();
	private static final File COLORS_FOLDER = Paths.get(PRESENTS_PATH + "/colors").toFile();
	private static final File RESULTS_FOLDER = Paths.get(PRESENTS_PATH + "/generated").toFile();
	private static final File PATHS_FILE = Paths.get(PRESENTS_PATH + "/paths.txt").toFile();
	private static final int VARIATION = 3;

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
	@SneakyThrows
	void generate() {
		RESULTS_FOLDER.delete();
		RESULTS_FOLDER.mkdir();

		PATHS_FILE.delete();
		PATHS_FILE.createNewFile();

		Map<String, BufferedImage> backgrounds = getBackgrounds();

		int generated = 0;

		final BiFunction<String, PresentColor, BufferedImage> getColoredImage = (file, colorType) ->
				ImageUtils.replace(ImageUtils.read(TEMPLATES_FOLDER, file), Color.WHITE, () -> {
					final Supplier<Integer> random = () -> RandomUtils.randomInt(-VARIATION, VARIATION);
					final Function<Integer, Integer> offset = value -> value = MathUtils.clamp(value + random.get(), 0, 255);
					final Color color = colorType.getColor();
					final int red = offset.apply(color.getRed());
					final int green = offset.apply(color.getGreen());
					final int blue = offset.apply(color.getBlue());
					return new Color(red, green, blue);
				});

		StringBuilder paths = new StringBuilder();
		for (String background : backgrounds.keySet()) {
			for (PresentSide presentSide1 : PresentSide.values()) {
				for (PresentColor color1 : PresentColor.values()) {
					if (background.equals(color1.name().toLowerCase()))
						continue;
					
					final String side = presentSide1.name().toLowerCase();

					final BufferedImage image1 = getColoredImage.apply(String.format("%s-%s.png", side, "outline"), color1);

					for (PresentColor color2 : PresentColor.values()) {
						if (color1 == color2)
							continue;
						
						if (background.equals(color2.name().toLowerCase()))
							continue;

						final BufferedImage result = ImageUtils.combine(backgrounds.get(background), image1, image2);
						final String file = String.format("%s-%s-%s-%s.png", background, color1, color2, side).toLowerCase();
						paths.append(file).append(System.lineSeparator());
						ImageUtils.write(result, new File(RESULTS_FOLDER, file));
						++generated;
					}
				}
			}
		}

		try(FileWriter writer = new FileWriter(PATHS_FILE)) {
			writer.write(paths.toString());
		}

		System.out.println("Generated " + generated + " images");
	}

	private Map<String, BufferedImage> getBackgrounds() throws IOException {
		return new HashMap<>() {{
			Files.walk(BACKGROUNDS_FOLDER.toPath()).forEach(path -> {
				try {
					if (!path.toUri().toString().contains(".png"))
						return;

					final String name = path.getFileName().toString().split("\\.")[0];
					put(name, ImageUtils.read(path.toFile()));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			});
		}};
	}

}
