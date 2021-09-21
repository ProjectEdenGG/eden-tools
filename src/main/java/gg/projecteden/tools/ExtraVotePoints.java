package gg.projecteden.tools;

import gg.projecteden.utils.RandomUtils;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExtraVotePoints {

	private static final Map<String, Map<Integer, Integer>> trials = new LinkedHashMap<>() {{
		put("Normal", new LinkedHashMap<>() {{
			put(1500, 50);
			put(500, 25);
			put(200, 15);
			put(100, 10);
			put(50, 5);
		}});
		put("Blast", new LinkedHashMap<>() {{
			put(1250, 50);
			put(400, 25);
			put(175, 15);
			put(50, 10);
			put(25, 5);
		}});
		put("Blast+Amix", new LinkedHashMap<>() {{
			put(1250, 50);
			put(400, 25);
			put(175, 15);
			put(50, 10);
			put(15, 5);
		}});
		put("True Double", new LinkedHashMap<>() {{
			put(1500 / 2, 50);
			put(500 / 2, 25);
			put(200 / 2, 15);
			put(100 / 2, 10);
			put(50 / 2, 5);
		}});
		put("1.5x", new LinkedHashMap<>() {{
			put(Double.valueOf(1500 / 1.5).intValue(), 50);
			put(Double.valueOf(500 / 1.5).intValue(), 25);
			put(Double.valueOf(200 / 1.5).intValue(), 15);
			put(Double.valueOf(100 / 1.5).intValue(), 10);
			put(Double.valueOf(50 / 1.5).intValue(), 5);
		}});
	}};

	private int extraVotePoints(String trial) {
		for (Map.Entry<Integer, Integer> pair : trials.get(trial).entrySet())
			if (RandomUtils.randomInt(pair.getKey()) == 1)
				return pair.getValue();
		return 0;
	}

	@Test
	void votePointsSimulation() {
		trials.keySet().forEach(trial -> {
			System.out.println("Running trial " + trial + ": " + trials.get(trial));
			int timesExtra = 0;
			int totalExtra = 0;
			int votes = 0;
			for (int player = 0; player < 100; player++) {
				int playerTimesExtra = 0;
				for (int day = 0; day < 30; day++) {
					int extra = 0;
					for (int vote = 0; vote < 6; vote++) {
						++votes;
						int extraToday = extraVotePoints(trial);
						totalExtra += extraToday;
						if (extraToday > 0)
							++extra;
					}
//					System.out.println("    Day " + day + ": " + extra);
					playerTimesExtra += extra;
				}
//				System.out.println("  Total extra for player " + player + ": " + playerTimesExtra);
				timesExtra += playerTimesExtra;
			}
			System.out.println("  Total extra given: " + totalExtra);
			System.out.println("  Total times extra: " + timesExtra);
		});
	}
}
