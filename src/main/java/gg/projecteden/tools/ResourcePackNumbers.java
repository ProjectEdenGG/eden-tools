package gg.projecteden.tools;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class ResourcePackNumbers {

	private static final String PATH = "src/main/resources/numbers";

	@AllArgsConstructor
	private enum UVTemplate {
		DIGIT_0("0.5, 0.5, 2.5, 3.5"),
		DIGIT_1("3, 0.5, 5, 3.5"),
		DIGIT_2("5.5, 0.5, 7.5, 3.5"),
		DIGIT_3("8, 0.5, 10, 3.5"),
		DIGIT_4("10.5, 0.5, 12.5, 3.5"),
		DIGIT_5("0.5, 4, 2.5, 7"),
		DIGIT_6("3, 4, 5, 7"),
		DIGIT_7("5.5, 4, 7.5, 7"),
		DIGIT_8("8, 4, 10, 7"),
		DIGIT_9("10.5, 4, 12.5, 7"),
		;

		private final String uv;

		public static String of(int number, int index) {
			return of(Integer.parseInt(String.valueOf(String.valueOf(number).charAt(index))));
		}

		public static String of(int digit) {
			return valueOf("DIGIT_" + digit).uv;
		}
	}

	private enum Color {
		GREEN,
		RED,
		GRAY,
		CYAN
	}

	private static final StringBuilder script = new StringBuilder();

	@Test
	@SneakyThrows
	void generate() {
		for (Color colorType : Color.values()) {
			String color = colorType.name().toLowerCase();

			final String colorFolder = "generated/" + color;
			exec("mkdir -p " + colorFolder);

			for (int i = 0; i < 10; i++) {
				String file = colorFolder + "/" + i + ".json";
				exec("cp template_single.json " + file);
				exec("sed -i 's/__COLOR__/" + color + "/g' " + file);
				exec("sed -i 's/__UP__/" + UVTemplate.of(i, 0) + "/g' " + file);
			}

			for (int i = 10; i < 100; i++) {
				String file = colorFolder + "/" + i + ".json";
				exec("cp template_double.json " + file);
				exec("sed -i 's/__COLOR__/" + color + "/g' " + file);
				exec("sed -i 's/__UP_LEFT__/" + UVTemplate.of(i, 0) + "/g' " + file);
				exec("sed -i 's/__UP_RIGHT__/" + UVTemplate.of(i, 1) + "/g' " + file);
			}

			for (int i = 100; i < 1000; i++) {
				String file = colorFolder + "/" + i + ".json";
				exec("cp template_triple.json " + file);
				exec("sed -i 's/__COLOR__/" + color + "/g' " + file);
				exec("sed -i 's/__UP_LEFT__/" + UVTemplate.of(i, 0) + "/g' " + file);
				exec("sed -i 's/__UP_CENTER__/" + UVTemplate.of(i, 1) + "/g' " + file);
				exec("sed -i 's/__UP_RIGHT__/" + UVTemplate.of(i, 2) + "/g' " + file);
			}
		}

		Files.write(Paths.get(PATH + "/generate.sh"), script.toString().getBytes(), CREATE, TRUNCATE_EXISTING);
	}

	@SneakyThrows
	private static void exec(String command) {
		script.append(command).append(System.lineSeparator());
	}

}
