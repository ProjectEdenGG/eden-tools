package gg.projecteden.tools;

import gg.projecteden.tools.utils.IOUtils;
import gg.projecteden.tools.utils.ImageUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class Armor {
	final List<String> ORDER = List.of("wither", "warden", "berserker", "brown_berserk", "copper", "damascus", "druid",
			"hellfire", "jarl", "mythril", "tank", "thor", "wizard", "wolf", "fishing");
	final List<String> SLOTS = List.of("boots", "chestplate", "helmet", "leggings");

	final String MODEL_TEMPLATE = """
			{
				"parent": "projecteden/items/armor/template_armor",
				"textures": {
					"0": "projecteden/items/armor/%s/%s"
				}
			}
			""";

	private static final String RESOURCES_PATH = "src/main/resources/armor";
	private static final Path MODELS_PATH = Paths.get(RESOURCES_PATH + "/templates/models");
	private static final File TEXTURES_FOLDER = Paths.get(RESOURCES_PATH + "/templates/textures").toFile();
	private static final String ITEM_TEXTURE_INPUT_PATH = RESOURCES_PATH + "/templates/textures/%s/%s.png";

	private static final String ITEM_MODEL_OUTPUT_PATH = RESOURCES_PATH + "/output/models/item/leather_%s.json";
	private static final String ITEM_TEXTURE_OUTPUT_PATH = RESOURCES_PATH + "/output/textures/projecteden/items/armor/%s/%s.png";
	private static final File LAYER_OUTPUT_FOLDER = Paths.get(RESOURCES_PATH + "/output/textures/models/armor").toFile();
	private static final String MODEL_OUTPUT_PATH = RESOURCES_PATH + "/output/models/projecteden/items/armor/%s/%s.json";

	/*
		After running, open the "output" folder in explorer, copy the models and textures folder, and
		paste it onto the "minecraft" folder in Saturn via Intellij. Click "Overwrite for all"
		Remember to delete any folders in Saturn if necessary
	 */

	@Test
	@SneakyThrows
	void run() {
		for (var layer : List.of(1, 2)) {
			var layerFile = "layer_%s.png".formatted(layer);
			var combined = ImageUtils.newImage(64, (ORDER.size() + 1) * 32);
			var leather = ImageUtils.read(TEXTURES_FOLDER, "leather/" + layerFile);
			var graphics = combined.getGraphics();
			graphics.drawImage(leather, 0, 0, null);

			var index = 1;
			for (var type : ORDER) {
				var image = ImageUtils.read(TEXTURES_FOLDER, type + "/" + layerFile);
				graphics.drawImage(image, 0, 32 * index++, null);
			}

			ImageUtils.write(combined, LAYER_OUTPUT_FOLDER, "leather_" + layerFile);
		}

		for (var type : ORDER)
			for (var slot : SLOTS)
				IOUtils.fileWrite(MODEL_OUTPUT_PATH.formatted(type, slot), (writer, outputs) ->
						outputs.add(MODEL_TEMPLATE.formatted(type, slot)));

		for (var type : ORDER)
			for (var slot : SLOTS) {
				final Path output = Paths.get(ITEM_TEXTURE_OUTPUT_PATH.formatted(type, slot));
				output.toFile().getParentFile().mkdirs();
				Files.copy(Paths.get(ITEM_TEXTURE_INPUT_PATH.formatted(type, slot)), output, StandardCopyOption.REPLACE_EXISTING);
			}


		for (var slot : SLOTS) {
			var modelId = 1;
			var overrides = new ArrayList<String>();
			for (var type : ORDER)
				overrides.add("{\"predicate\": {\"custom_model_data\": %s}, \"model\": \"projecteden/items/armor/%s/%s\"}".formatted(modelId++, type, slot));
			final String template = Files.readString(MODELS_PATH.resolve("leather_" + slot + ".json")).replace("__OVERRIDES__", String.join(",\n\t\t", overrides));
			final Path path = Paths.get(ITEM_MODEL_OUTPUT_PATH.formatted(slot));
			path.toFile().getParentFile().mkdirs();
			Files.write(path, template.getBytes(), CREATE, TRUNCATE_EXISTING);
		}

		var modelId = 1;
		System.out.println("CustomArmorType enum:");
		for (var type : ORDER)
			System.out.printf("\t%s(%d),%n", type.toUpperCase(), modelId++);
	}
}
