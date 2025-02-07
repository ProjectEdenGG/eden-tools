package gg.projecteden.tools.utils;

import com.google.gson.Gson;
import lombok.Data;
import lombok.SneakyThrows;

import java.nio.file.Path;

import static java.nio.file.Files.readAllLines;

public class SecretsUtils {
	public static final String FILE = "src/main/resources/secrets.json";

	@SneakyThrows
	public static Secrets getSecrets() {
		return new Gson().fromJson(String.join("", readAllLines(Path.of(FILE))), Secrets.class);
	}

	@Data
	public static class Secrets {
		private DiscordSecrets discord;

		@Data
		public static class DiscordSecrets {
			private String relay;
			private String koda;
		}
	}
}
