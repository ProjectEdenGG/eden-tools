package gg.projecteden.tools;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static gg.projecteden.tools.DyeStationColors.BaseColor.RED;

public class DyeStationColors {

	public static final List<String> red = List.of("#FF756B", "#FF5E52", "#FF4233", "#FF0000", "#C70F00", "#9C0B00", "#6E0800");

	@Getter
	@AllArgsConstructor
	public enum BaseColor {
		RED("#FF0000"),
		ORANGE("#FF8000"),
		YELLOW("#FFFF00"),
		GREEN("#00FF00"),
		LIGHT_BLUE("#00BCFF"),
		BLUE("#0000FF"),
		PURPLE("#8800AA"),
		PINK("#FF88AA"),
		WHITE("#FFFFFF"),
		;

		private final String hex;
		private final List<Color> results = new ArrayList<>();
	}

	public static final List<Color> colorBoxes = new ArrayList<>() {{
// RED
		new Color(255, 117, 107);
		new Color(255, 94, 82);
		new Color(255, 66, 51);
		new Color(255, 0, 0);
		new Color(199, 15, 0);
		new Color(156, 11, 0);
		new Color(110, 8, 0);
// ORANGE
		new Color(255, 245, 107);
		new Color(255, 222, 82);
		new Color(255, 194, 51);
		new Color(255, 128, 0);
		new Color(199, 143, 0);
		new Color(156, 139, 0);
		new Color(110, 136, 0);
// YELLOW
		new Color(255, 255, 0);
// GREEN
		new Color(0, 255, 0);
// LIGHT_BLUE
		new Color(0, 188, 255);
// BLUE
		new Color(0, 0, 255);
// PURPLE
		new Color(136, 94, 252);
		new Color(136, 66, 221);
		new Color(136, 0, 170);
		new Color(80, 15, 170);
		new Color(37, 11, 170);
// PINK
		new Color(255, 230, 252);
		new Color(255, 202, 221);
		new Color(255, 136, 170);
		new Color(199, 151, 170);
		new Color(156, 147, 170);
		new Color(110, 144, 170);
// WHITE
		new Color(255, 255, 255);
	}};


	@Test
	void colors() {
		final Color redColor = Color.decode(RED.getHex());

		for (BaseColor baseColor : BaseColor.values()) {
			final Color base = Color.decode(baseColor.getHex());

			System.out.println(baseColor.name());
			for (String hex : DyeStationColors.red) {
				int baseRed = base.getRed();
				int baseGreen = base.getGreen();
				int baseBlue = base.getBlue();
				System.out.printf("Base: (%d, %d, %d);%n", baseRed, baseGreen, baseBlue);

				final Color currentRed = Color.decode(hex);
				int curRed = currentRed.getRed();
				int curGreen = currentRed.getGreen();
				int curBlue = currentRed.getBlue();
				System.out.printf("Current: (%d, %d, %d);%n", curRed, curGreen, curBlue);

				System.out.printf("Red: (%d, %d, %d);%n", redColor.getRed(), redColor.getGreen(), redColor.getBlue());

				int newRed = baseRed + (curRed - redColor.getRed());
				int newGreen = baseGreen + (curGreen - redColor.getGreen());
				int newBlue = baseBlue + (curBlue - redColor.getBlue());

//				if(newRed > 255 || newRed < 0)
//					newRed = baseRed - (curRed - redColor.getRed());
//				if(newGreen > 255 || newGreen < 0)
//					newGreen = baseGreen - (curGreen - redColor.getGreen());
//				if(newBlue > 255 || newBlue < 0)
//					newBlue = baseBlue - (curBlue - redColor.getBlue());

				System.out.printf("New: (%d, %d, %d);%n", newRed, newGreen, newBlue);
				try {
					final Color modified = new Color(newRed, newGreen, newBlue);

					baseColor.getResults().add(modified);

					System.out.println();
				}catch(Exception ignored){}
			}

			System.out.println();
		}

		for (BaseColor baseColor : BaseColor.values()) {
			System.out.println("// " + baseColor.name());
			for (Color color : baseColor.getResults()) {
				System.out.printf("new Color(%d, %d, %d);%n", color.getRed(), color.getGreen(), color.getBlue());
			}
		}
	}
}
