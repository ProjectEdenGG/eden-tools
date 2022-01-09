package gg.projecteden.tools;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static gg.projecteden.tools.DyeStationColors.BaseColor.RED;

public class DyeStationColors {

	public static final List<String> red = List.of("#FF756B", "#FF5E52", "#FF4233", "#FF0000", "#C70F00", "#9C0B00", "#6E0800");

	@Getter
	@AllArgsConstructor
	public enum BaseColor {
		RED("#FF0000"),
		ORANGE("#FF8000"),
		YELLOW("#FFFF00"),
		PINK("#FF88AA"),
		WHITE("#FFFFFF"),
		GREEN("#00FF00"),
		PURPLE("#8800AA"),
		BLUE("#0000FF"),
		LIGHT_BLUE("#00BCFF"),
		;

		private final String hex;
		private final List<Color> results = new ArrayList<>();
	}

	public static final List<Color> colorBoxes = new ArrayList<>() {{
		new Color(255, 117, 117);
		new Color(255, 94, 94);
		new Color(255, 66, 66);
		new Color(255, 0, 0);
		new Color(199, 15, 15);
		new Color(156, 11, 11);
		new Color(110, 8, 8);
		new Color(255, 245, 245);
		new Color(255, 222, 222);
		new Color(255, 194, 194);
		new Color(255, 128, 128);
		new Color(199, 143, 143);
		new Color(156, 139, 139);
		new Color(110, 136, 136);
	}};


	@Test
	void colors() {
		final Color baseRed = Color.decode(RED.getHex());

		for (BaseColor baseColor : BaseColor.values()) {
			final Color base = Color.decode(baseColor.getHex());

			for (String hex : red) {
				final Color currentRed = Color.decode(hex);
				final Color modified = new Color(
						base.getRed() + (currentRed.getRed() - baseRed.getRed()),
						base.getGreen() + (currentRed.getGreen() - baseRed.getGreen()),
						base.getBlue() + (currentRed.getBlue() - baseRed.getBlue())
				);
				baseColor.getResults().add(modified);

				System.out.println("new Color(%d, %d, %d);".formatted(modified.getRed(), modified.getGreen(), modified.getGreen()));
			}
		}
	}
}
