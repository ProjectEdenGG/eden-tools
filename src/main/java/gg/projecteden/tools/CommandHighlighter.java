package gg.projecteden.tools;

import com.google.gson.Gson;
import gg.projecteden.tools.CommandHighlighter.AllCommands.CommandMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static gg.projecteden.tools.utils.StringUtils.ANSI_GREEN;
import static gg.projecteden.tools.utils.StringUtils.ANSI_RED;
import static gg.projecteden.tools.utils.StringUtils.ANSI_RESET;
import static gg.projecteden.utils.StringUtils.isNullOrEmpty;

public class CommandHighlighter {

	static AllCommands commands;

	Map<String, String> testMessages = new LinkedHashMap<>() {{
		put("use /pay <player> <#> to pay a player", "use &c/pay <player> <#>&f to pay a player");
		put("to set a home, use /home set (home) this /command", "to set a home, use &c/home set (home)&f this /command");
		put("/warp is valid command, where as /thatCommand is invalid", "&c/warp&f is valid command, where as /thatCommand is invalid");
		put("/vanish fj and /vanish fq are both valid redirects, /vanish fz is not", "&c/vanish fj&f and &c/vanish fq&f are both valid redirects, &c/vanish&f fz is not");
		put("different/missing brackets /tppos x (y) {z} pitch yaw", "no brackets &c/tppos x (y) {z} pitch yaw&f");
		put("Welcome to the server, run /rules to read the rules, and /faq for other shit", "Welcome to the server, run &c/rules&f to read the rules, and &c/faq&f for other shit");
		put("to do this use /shrug, you can even put text beforehand like this with /shrug [text...] ¯\\_(ツ)_/¯ ", "to do this use &c/shrug&f, you can even put text beforehand like this with &c/shrug [text...]&f ¯\\_(ツ)_/¯ ");
		put("o/ none \\o of //worldedit this <>< should ><> get \\o/ high o7 lighted o> xyz <3", "o/ none \\o of //worldedit this <>< should ><> get \\o/ high o7 lighted o> xyz <3");
	}};

	@Test
	@SneakyThrows
	void test() {
		final Path path = Path.of("src/main/resources/commands/commands-meta.json");
		commands = new Gson().fromJson(String.join("", Files.readAllLines(path)), AllCommands.class);

		for (String message : testMessages.keySet()) {
			System.out.println();
			final String result = process(message);
			final String expecting = testMessages.get(message);
			final boolean correct = expecting.equals(result);
			System.out.println("Input:      " + message.replaceAll("&c", ANSI_RED).replaceAll("&f", ANSI_RESET));
			System.out.println("Result:     " + result.replaceAll("&c", ANSI_RED).replaceAll("&f", ANSI_RESET));
			System.out.println("Expecting:  " + expecting.replaceAll("&c", ANSI_RED).replaceAll("&f", ANSI_RESET));
			System.out.println("Correct:    " + (correct ? ANSI_GREEN : ANSI_RED) + correct + ANSI_RESET);
		}
	}

	static String process(String message) {
		if (!message.contains("/"))
			return message;

		final Pattern pattern = Pattern.compile("/[a-zA-Z\\d_-]+");
		final Matcher matcher = pattern.matcher(message);

		while (matcher.find()) {
			final String group = matcher.group();
			for (CommandMeta command : commands.getCommands()) {
				if (!command.getAllAliases().contains(group.replaceFirst("/", "")))
					continue;

				message = message.replace(group, "&c" + group + getMessageColor());
//				for (Method method : command.getPathMethods()) {
//					final String path = method.getAnnotation(Path.class).value();
//
//				}

				break;
			}
		}

		return message;
	}

	private static String getMessageColor() {
		return "&f";
	}

	@Data
	static class AllCommands {
		private List<CommandMeta> commands = new ArrayList<>();

		@Data
		@Builder
		@NoArgsConstructor
		@AllArgsConstructor
		static class CommandMeta {
			private String name;
			private List<String> aliases;
			private List<PathMeta> paths;
			private Map<String, String> redirects;

			public List<String> getAllAliases() {
				List<String> aliases = getAliases();
				aliases.add(getName());
				return aliases.stream().map(String::toLowerCase).collect(Collectors.toList());
			}

			@Data
			@Builder
			@NoArgsConstructor
			@AllArgsConstructor
			static class PathMeta {
				private List<ArgumentMeta> arguments;

				@Data
				@Builder
				@NoArgsConstructor
				@AllArgsConstructor
				static class ArgumentMeta {
					private String pathName;
					private String parameterName;
					private boolean required;

					public boolean isLiteral() {
						return !isNullOrEmpty(parameterName);
					}
				}
			}

		}

	}
}
