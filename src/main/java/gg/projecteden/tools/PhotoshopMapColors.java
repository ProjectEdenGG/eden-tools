package gg.projecteden.tools;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import gg.projecteden.tools.PhotoshopMapColors.PhotoshopConfig.PhotoshopColor;
import gg.projecteden.tools.PhotoshopMapColors.PhotoshopConfig.PhotoshopColor.ColorType;
import gg.projecteden.tools.PhotoshopMapColors.PhotoshopConfig.PhotoshopColor.ColorType.ColorData.ProfileType;
import gg.projecteden.tools.PhotoshopMapColors.PhotoshopConfig.PhotoshopColor.ColorType.ColorData.RGB;
import gg.projecteden.tools.PhotoshopMapColors.PhotoshopConfig.PhotoshopColor.ColorType.ColorData.RGB.ColorComparator;
import gg.projecteden.tools.PhotoshopMapColors.PhotoshopConfig.PhotoshopColor.ColorType.Rel;
import gg.projecteden.tools.PhotoshopMapColors.PhotoshopConfig.PhotoshopConfigData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PhotoshopMapColors {
	public static final String CONFIG = "src/main/resources/mapcolors/mapcolors.json";
	public static final String GROUP_UUID = "ce441c47-3c39-4aac-b055-93010b49fcbf";

	@Test
	@SneakyThrows
	void toPhotoshop() {
		final String json = String.join("", Files.readAllLines(Path.of(CONFIG)));
		MapColorsConfig config = new Gson().fromJson(json, MapColorsConfig.class);

		List<Color> colorSortTest = new ArrayList<>();

		final PhotoshopConfig photoshopConfig = new PhotoshopConfig();
		List<PhotoshopColor> colors = new ArrayList<>();
		for (MapColor color : config.getColors()) {
			int index = 0;
			for (RGB sibling : color.getSiblings()) {
				++index;
				final PhotoshopColor photoshopColor = new PhotoshopColor(color.getName() + " " + index);
				photoshopColor.getChildren().add(new ColorType(Rel.PRIMARY, sibling));
				photoshopColor.getChildren().add(new ColorType(Rel.RENDITION, sibling));
				colorSortTest.add(sibling.asColor());
				colors.add(photoshopColor);
			}
		}

		photoshopConfig.getChildren().add(new PhotoshopConfigData("elements", colors));
		photoshopConfig.getChildren().add(new PhotoshopConfigData("groups", List.of(Map.of(
			"id", GROUP_UUID,
			"name", "Map Colors Sorted",
			"type", "application/vnd.adobe.library.group+dcx",
			"library#classifier", "$default",
			"library#order", "n"
		))));

		Rainbow.build();
		System.out.println("rainbow size: " + rainbow.size());
		System.out.println("colors size: " + colorSortTest.size());
		colorSortTest.sort(new ColorComparator());
		rainbow.removeIf(color -> !colorSortTest.contains(color));
		System.out.println(rainbow);

		for (PhotoshopColor color : colors)
			color.setOrder(String.valueOf(colorSortTest.indexOf(color.getChildren().get(0).getColor_data().getValue().asColor())));

		System.out.println(new Gson().toJson(photoshopConfig));
	}

	public static final List<Color> colorBoxes = new ArrayList<>() {{

	}};

	public static <K extends Comparable<? super K>, V> LinkedHashMap<K, V> sortByKey(Map<K, V> map) {
		return collect(map.entrySet().stream().sorted(Entry.comparingByKey()));
	}

	public static <K extends Comparable<? super K>, V> LinkedHashMap<K, V> sortByKeyReverse(Map<K, V> map) {
		return reverse(sortByKey(map));
	}

	public static <K, V> LinkedHashMap<K, V> reverse(LinkedHashMap<K, V> sorted) {
		LinkedHashMap<K, V> reverse = new LinkedHashMap<>();
		List<K> keys = new ArrayList<>(sorted.keySet());
		Collections.reverse(keys);
		keys.forEach(key -> reverse.put(key, sorted.get(key)));
		return reverse;
	}

	public static <K, V> LinkedHashMap<K, V> collect(Stream<Entry<K, V>> stream) {
		return stream.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	@Data
	public static class MapColorsConfig {
		private List<MapColor> colors;
	}

	@Data
	public static class MapColor {
		private int id;
		private String name;
		private RGB base;
		private final List<RGB> siblings = new ArrayList<>();

		public List<RGB> getSiblings() {
			return List.of(
				base,
				RGB.of(base, .86),
				RGB.of(base, .71),
				RGB.of(base, .53)
			);
		}
	}

	@Data
	public static class PhotoshopConfig {
		@SerializedName("manifest-format-version")
		private int version = 6;
		private UUID id = UUID.randomUUID();
		private String name = "Minecraft Map Colors";
		private String type = "application/vnd.adobe.library+dcx";
		private String state = "unmodified";
		@SerializedName("library#version")
		private int library_version = 1;
		private List<PhotoshopConfigData> children = new ArrayList<>();

		@Data
		public static class PhotoshopConfigData {
			private UUID id;
			private String name;
			private List<?> children;

			public PhotoshopConfigData(String name, List<?> children) {
				this.id = UUID.randomUUID();
				this.name = name;
				this.children = children;
			}
		}

		@Data
		public static class PhotoshopColor {
			private UUID id;
			private String name;
			private UUID path;
			private String type = "application/vnd.adobe.element.color+dcx";
			private List<ColorType> children = new ArrayList<>();
			@SerializedName("library#groups")
			private LibraryGroups library_groups;

			public void setOrder(String order) {
				library_groups = new LibraryGroups(order);
			}

			public PhotoshopColor(String name) {
				this.id = UUID.randomUUID();
				this.name = name;
				this.path = this.id;
			}

			public static class LibraryGroups {
				@SerializedName("$default#" + GROUP_UUID)
				public OrderData orderData;

				public LibraryGroups(String order) {
					this.orderData = new OrderData(order);
				}

				@Data
				@AllArgsConstructor
				public static class OrderData {
					private String order;
				}
			}

			@Data
			public static class ColorType {
				private UUID id;
				private String type = "application/vnd.adobe.color+json";
				@SerializedName("library#rel")
				private Rel library_rel;
				@SerializedName("library#representationOrder")
				private int library_representationOrder;
				@SerializedName("color#data")
				private ColorData color_data;

				public ColorType(Rel library_rel, RGB rgb) {
					this.id = UUID.randomUUID();
					this.library_rel = library_rel;
					this.library_representationOrder = library_rel.ordinal();
					this.color_data = new ColorData(library_rel.getProfileType(), rgb);
				}

				@AllArgsConstructor
				@Getter
				public enum Rel {
					@SerializedName("primary")
					PRIMARY(ProfileType.sRGB),
					@SerializedName("rendition")
					RENDITION(ProfileType.RGB),
					;

					private final ProfileType profileType;
				}

				@Data
				public static class ColorData {
					private String type = "process";
					private ProfileType profileName;
					private String mode = "RGB";
					private RGB value;

					public ColorData(ProfileType profileName, RGB rgb) {
						this.profileName = profileName;
						this.value = rgb;
					}

					public enum ProfileType {
						@SerializedName("Untagged RGB")
						RGB,
						@SerializedName("sRGB IEC61966-2.1")
						sRGB,
					}

					@Data
					@AllArgsConstructor
					@Accessors(fluent = true)
					public static class RGB {
						private int r, g, b;

						public static RGB of(RGB base, double shade) {
							return new RGB((int) (base.r() * shade), (int) (base.g() * shade), (int) (base.b() * shade));
						}

						public Color asColor() {
							return new Color(r, g, b);
						}

						public static final class RGBComparator implements Comparator<RGB> {
							@Override
							public int compare(RGB c1, RGB c2) {
								float[] hsb1 = Color.RGBtoHSB(c1.r(), c1.g(), c1.b(), null);
								float[] hsb2 = Color.RGBtoHSB(c2.r(), c2.g(), c2.b(), null);
								if (hsb1[0] < hsb2[0])
									return -1;
								if (hsb1[0] > hsb2[0])
									return 1;
								if (hsb1[1] < hsb2[1])
									return -1;
								if (hsb1[1] > hsb2[1])
									return 1;
								if (hsb1[2] < hsb2[2])
									return -1;
								if (hsb1[2] > hsb2[2])
									return 1;
								return 0;
							}
						}

						public static final class ColorComparator implements Comparator<Color> {
							@Override
							public int compare(Color c1, Color c2) {
								float[] hsb1 = Color.RGBtoHSB(c1.getRed(), c1.getGreen(), c1.getBlue(), null);
								float[] hsb2 = Color.RGBtoHSB(c2.getRed(), c2.getGreen(), c2.getBlue(), null);
								if (hsb1[0] < hsb2[0])
									return -1;
								if (hsb1[0] > hsb2[0])
									return 1;
								if (hsb1[1] < hsb2[1])
									return -1;
								if (hsb1[1] > hsb2[1])
									return 1;
								if (hsb1[2] < hsb2[2])
									return -1;
								if (hsb1[2] > hsb2[2])
									return 1;
								return 0;
							}
						}

						@Override
						public String toString() {
							return "new Color(%d, %d, %d)".formatted(r, g, b);
						}

						public String toHex() {
							return "#" + Integer.toHexString(asColor().getRGB()).substring(2);
						}

					}
				}
			}
		}
	}

	public static final Set<Color> rainbow = new HashSet<>();

	@Test
	void rainbow() {
		Rainbow.build();
		System.out.println(rainbow.size());
		final ArrayList<Color> list = new ArrayList<>(rainbow);
		System.out.println(list.get(0));
		System.out.println(list.get(1000));
		System.out.println(list.get(100000));
		System.out.println(list.get(10000000));
	}

	public static class Rainbow {
		public static void build() {
			int colors = 9999999;
			final double f = (6.48 / (double) colors);
			for (int i = 0; i < colors; ++i) {
				double r = Math.sin(f * i + 0.0D) * 127.0D + 128.0D;
				double g = Math.sin(f * i + (2 * Math.PI / 3)) * 127.0D + 128.0D;
				double b = Math.sin(f * i + (4 * Math.PI / 3)) * 127.0D + 128.0D;
				rainbow.add(new Color((int) r, (int) g, (int) b));
			}
		}
	}

}
