package gg.projecteden.tools;

import gg.projecteden.api.common.utils.MathUtils;
import gg.projecteden.api.common.utils.RandomUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Supplier;

public class MendingIntegrity {
	private static final double MAX_INTEGRITY = 100;

	@Test
	void averageFullRepairs() {
		final Supplier<Integer> repairAmountSupplier = () -> RandomUtils.randomInt(6, 12);

		for (Material material : Material.values()) {
			final int iterations = 100;

			int totalRepairedAmount = 0;

			for (int i = 0; i < iterations; i++) {
				final ItemStack item = new ItemStack(material);

				while (item.getMendingIntegrity() > 0) {
					final Integer repairAmount = repairAmountSupplier.get();
					item.setRepairedAmount(item.getRepairedAmount() + repairAmount);

					if (RandomUtils.chanceOf(80))
						continue;

					updateIntegrity(item, repairAmount);
				}

				totalRepairedAmount += item.getRepairedAmount();
			}


			System.out.println(material.name() + ": " + totalRepairedAmount / (material.getMaxDurability() * iterations));
		}
	}

	public static void updateIntegrity(ItemStack item, int repairAmount) {
		double integrity = item.getMendingIntegrity();

		int maxDurability = item.getType().getMaxDurability();
		integrity = getNewIntegrity(integrity, repairAmount, maxDurability);

		item.setMendingIntegrity(integrity);
	}

	private static double getNewIntegrity(double integrity, double repairAmount, double maxDurability) {
		double removeAmount = (repairAmount / maxDurability) * 100;

		integrity -= removeAmount;
		integrity = clamp(integrity);

		return integrity;
	}

	private static double clamp(double integrity) {
		integrity = round(integrity);
		return MathUtils.clamp(integrity, 0, MAX_INTEGRITY);
	}

	private static double round(double integrity) {
		return round(integrity, 2);
	}

	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	@Data
	private static final class ItemStack {
		private final Material type;
		private int repairedAmount;
		private double mendingIntegrity = 100;
	}

	@Getter
	@AllArgsConstructor
	private enum Material {
		GOLDEN_AXE(32),
		GOLDEN_HOE(32),
		GOLDEN_PICKAXE(32),
		GOLDEN_SHOVEL(32),
		GOLDEN_SWORD(32),
		LEATHER_HELMET(55),
		WOODEN_AXE(59),
		WOODEN_HOE(59),
		WOODEN_PICKAXE(59),
		WOODEN_SHOVEL(59),
		WOODEN_SWORD(59),
		FISHING_ROD(64),
		LEATHER_BOOTS(65),
		LEATHER_LEGGINGS(75),
		GOLDEN_HELMET(77),
		LEATHER_CHESTPLATE(80),
		GOLDEN_BOOTS(91),
		GOLDEN_LEGGINGS(105),
		GOLDEN_CHESTPLATE(112),
		STONE_AXE(131),
		STONE_HOE(131),
		STONE_PICKAXE(131),
		STONE_SHOVEL(131),
		STONE_SWORD(131),
		CHAINMAIL_HELMET(165),
		IRON_HELMET(165),
		CHAINMAIL_BOOTS(195),
		IRON_BOOTS(195),
		CHAINMAIL_LEGGINGS(225),
		IRON_LEGGINGS(225),
		SHEARS(238),
		CHAINMAIL_CHESTPLATE(240),
		IRON_CHESTPLATE(240),
		IRON_AXE(250),
		IRON_HOE(250),
		IRON_PICKAXE(250),
		IRON_SHOVEL(250),
		IRON_SWORD(250),
		TRIDENT(250),
		TURTLE_HELMET(275),
		SHIELD(336),
		DIAMOND_HELMET(363),
		BOW(384),
		NETHERITE_HELMET(407),
		DIAMOND_BOOTS(429),
		CROSSBOW(465),
		NETHERITE_BOOTS(481),
		DIAMOND_LEGGINGS(495),
		DIAMOND_CHESTPLATE(528),
		NETHERITE_LEGGINGS(555),
		NETHERITE_CHESTPLATE(592),
		DIAMOND_AXE(1561),
		DIAMOND_HOE(1561),
		DIAMOND_PICKAXE(1561),
		DIAMOND_SHOVEL(1561),
		DIAMOND_SWORD(1561),
		NETHERITE_AXE(2031),
		NETHERITE_HOE(2031),
		NETHERITE_PICKAXE(2031),
		NETHERITE_SHOVEL(2031),
		NETHERITE_SWORD(2031),
		;

		private final int maxDurability;
	}

}
