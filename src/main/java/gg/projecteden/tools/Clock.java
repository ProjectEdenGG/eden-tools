package gg.projecteden.tools;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class Clock {

	private static final String PATH = "src/main/resources/clock";

	@AllArgsConstructor
	private enum Hand {
		HOUR(1, 12),
		MINUTE(0, 59),
		;

		private final int min;
		private final int max;
	}

	private static final StringBuilder script = new StringBuilder();

	@Test
	@SneakyThrows
	void generate() {
		for (Hand hand : Hand.values()) {
			final String name = hand.name().toLowerCase();
			final String folder = "generated/" + name;
			exec("mkdir -p " + folder);

			for (int i = hand.min; i <= hand.max; i++) {
				String file = folder + "/" + i + ".json";
				exec("cp template_" + name + ".json " + file);
				exec("sed -i 's/__INDEX__/" + i + "/g' " + file);
			}
		}

		Files.write(Paths.get(PATH + "/generate.sh"), script.toString().getBytes(), CREATE, TRUNCATE_EXISTING);
	}

	@SneakyThrows
	private static void exec(String command) {
		script.append(command).append(System.lineSeparator());
	}

}
