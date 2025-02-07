package gg.projecteden.tools;

import com.google.gson.Gson;
import gg.projecteden.tools.utils.SecretsUtils;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BridgeArchiveRoles {
	static final String ROLE_ARCHIVE = "src/main/resources/role-archive/role-archive.json";
	static final String LAST_MESSAGE_IDS = "src/main/resources/role-archive/last-message-ids.json";

	@SneakyThrows
	@Test
	void archive() {
		String token = SecretsUtils.getSecrets().getDiscord().getRelay();

		JDA client = JDABuilder.createDefault(token).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();

		client.addEventListener(new ListenerAdapter() {
			@Override
			public void onReady(@NotNull ReadyEvent event) {
				LocalDateTime startTime = LocalDateTime.now();
				System.out.println("Started: " + startTime);

				try {
					Guild guild = client.getGuildById(132680070480396288L);
					if (guild == null) {
						System.out.println("Guild not found!");
						return;
					}
					System.out.println("Guild: " + guild.getName());

					List<Long> bridgeIds = Arrays.asList(
							331277920729432065L // #bridge
//							, 331842528854802443L // #staff-bridge
//							, 331846903266279424L // #operator-bridge
//							, 151881902813478914L // #operators
//							, 133950052249894913L // #admins
					);

					List<TextChannel> bridges = new ArrayList<>();
					for (Long id : bridgeIds) {
						TextChannel channel = guild.getTextChannelById(id);
						if (channel != null)
							bridges.add(channel);
					}

					System.out.println("Bridges: " + bridges.stream().map(TextChannel::getName).collect(Collectors.joining(", ")));

					// Fetch specific bot members
					List<Long> botUserIds = Arrays.asList(
							352231755551473664L, // Relay
							223794142583455744L  // Koda
					);

					List<Member> bots = new ArrayList<>();
					for (Long userId : botUserIds) {
						Member bot = guild.getMemberById(userId);
						if (bot != null)
							bots.add(bot);
					}

					if (bots.isEmpty()) {
						throw new Exception("No bots found!");
					}

					System.out.println("Bots: " + bots.stream().map(member -> member.getUser().getName()).collect(Collectors.joining(", ")));

					String now = LocalDateTime.now().toString().replaceAll(":", "-");
					if (Files.exists(Path.of(ROLE_ARCHIVE)))
						Files.copy(Path.of(ROLE_ARCHIVE), Path.of(ROLE_ARCHIVE + '-' + now + ".bak"));
					if (Files.exists(Path.of(LAST_MESSAGE_IDS)))
						Files.copy(Path.of(LAST_MESSAGE_IDS), Path.of(LAST_MESSAGE_IDS + '-' + now + ".bak"));

					String existingData = Files.exists(Path.of(ROLE_ARCHIVE)) ? Files.readString(Path.of(ROLE_ARCHIVE)) : null;
					final Map<String, Map<String, List<String>>> data;
					if (existingData != null && !existingData.isBlank()) {
						data = new Gson().fromJson(existingData, Map.class);
						System.out.println("Loaded data for " + data.size() + " channels and " + data.values().stream().mapToInt(Map::size).sum() + " roles");
					} else {
						data = new HashMap<>();
						System.out.println("No existing data found");
					}

					String existingLastMessageId = Files.exists(Path.of(LAST_MESSAGE_IDS)) ? Files.readString(Path.of(LAST_MESSAGE_IDS)) : null;
					final Map<String, String> lastMessageIds;
					if (existingLastMessageId != null && !existingLastMessageId.isBlank()) {
						lastMessageIds = new Gson().fromJson(existingLastMessageId, Map.class);
						System.out.println("Loaded " + lastMessageIds.size() + " last message IDs: " + lastMessageIds);
					} else {
						lastMessageIds = new HashMap<>();
						System.out.println("No existing last message ids found");
					}

					Role topRole = guild.getRoleById(331279736691228676L); // @Griffin - All bridge roles must be under this, all other roles above
					Role defaultRole = guild.getPublicRole();

					System.out.println("Top role: " + topRole.getName());
					System.out.println("Default role: " + defaultRole.getName());

					Pattern mentionPattern = Pattern.compile("<@&(\\d{18,19})>");
					for (TextChannel bridge : bridges) {
						Runnable writeToFile = () -> {
							try {
								try (FileWriter file = new FileWriter(ROLE_ARCHIVE)) {
									JSONObject jsonObject = new JSONObject(data);
									file.write(jsonObject.toString());
								}
								try (FileWriter file = new FileWriter(LAST_MESSAGE_IDS)) {
									JSONObject jsonObject = new JSONObject(lastMessageIds);
									file.write(jsonObject.toString());
								}
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						};

						String channelId = bridge.getId();
						data.putIfAbsent(channelId, new HashMap<>());

						AtomicReference<Runnable> process = new AtomicReference<>(null);
						process.set(() -> {
							String lastMessageId = lastMessageIds.computeIfAbsent(channelId, $ -> bridge.getLatestMessageId());
							System.out.println("Getting history before: " + net.dv8tion.jda.api.utils.TimeUtil.getTimeCreated(Long.parseLong(lastMessageId)).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
							MessageHistory history = bridge.getHistoryBefore(lastMessageId, 100).timeout(60, TimeUnit.SECONDS).complete();
							List<Message> messages = history.getRetrievedHistory();

							if (messages == null || messages.isEmpty()) {
								System.out.println("Found no more messages");
								return;
							}

							for (Message message : messages) {
								System.out.println("Processing message: " + message.getId() + " - " + message.getContentRaw());
								if (!botUserIds.contains(message.getAuthor().getIdLong())) {
									continue;
								}

								Matcher matcher = mentionPattern.matcher(message.getContentRaw());
								if (matcher.find()) {
									long roleId = Long.parseLong(matcher.group(1));
									Role role = guild.getRoleById(roleId);
									if (role == null || !(topRole.compareTo(role) >= 0 && role.compareTo(defaultRole) > 0)) {
										continue;
									}

									data.get(channelId).computeIfAbsent(String.valueOf(roleId), $ -> new ArrayList<>()).addFirst(message.getId());
								}
							}

							lastMessageIds.put(channelId, messages.getLast().getId());
							writeToFile.run();
							process.get().run();
						});

						process.get().run();
					}

					LocalDateTime endTime = LocalDateTime.now();
					System.out.println("Completed: " + endTime);
					System.out.println("Duration: " + Duration.between(startTime, endTime).toMillis() + "ms");

					client.shutdown();
				} catch (
						Exception e) {
					e.printStackTrace();
					client.shutdown();
				}
			}
		});

		// Start the bot
		client.awaitReady();
	}
}
