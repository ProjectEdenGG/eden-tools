package gg.projecteden.tools;

import com.google.gson.Gson;
import gg.projecteden.tools.ResourcePackCharacters.FontFile.Provider;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ResourcePackCharacters {
	private static final String FONT_FILE_URL = "https://raw.githubusercontent.com/ProjectEdenGG/Saturn/main/assets/minecraft/font/default.json";
	private static final String FOLDER = "src/main/resources/characters/";
	private static final List<String> TYPES = List.of("chinese", "unicode");

	@Test
	@SneakyThrows
	void chars() {
		final Request request = new Builder().url(FONT_FILE_URL).build();
		final Response response = new OkHttpClient().newCall(request).execute();
		final FontFile fontFile = new Gson().fromJson(Objects.requireNonNull(response.body()).string(), FontFile.class);

		for (String type : TYPES) {
			final List<String> chars = new ArrayList<>(Arrays.asList(Files.readString(Path.of(FOLDER + type + ".txt")).split("")));

			for (Provider provider : fontFile.providers)
				if (provider.chars != null)
					for (String s : provider.chars)
						chars.removeAll(Arrays.asList(s.split("")));

			System.out.println("Available " + type + " characters: " + chars);
		}
	}

	static class FontFile {
		private List<Provider> providers = new ArrayList<>();

		static class Provider {
			private String file;
			private final List<String> chars = new ArrayList<>();
		}
	}

}
