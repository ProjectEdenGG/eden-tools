package gg.projecteden.tools;

import gg.projecteden.tools.utils.ImageUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class ResourcePackTextures {

	static boolean isPowerOfTwo(int value) {
		return value != 0 && ((value & (value - 1)) == 0);
	}

	@Test
	@SneakyThrows
	void textures(){
		String folderName = "src\\main\\resources\\textures";
		Path folderPath = Paths.get(folderName);
		Set<Path> textures = new HashSet<>();

		try (var walker = Files.walk(folderPath)) {
			walker.forEach(path -> {
				try {
					final String uri = path.toUri().toString();

					if (uri.endsWith(".png")){
						textures.add(path);
					}

				} catch (Exception ex) {
					System.out.println(path.getFileName().toString());
					ex.printStackTrace();
				}
			});
		}

		int notPower2 = 0;
		for (Path path : textures) {
			final BufferedImage texture = ImageUtils.read(path.toFile());
			int height = texture.getHeight();
			int width = texture.getWidth();
			boolean pow2 = isPowerOfTwo(height) && isPowerOfTwo(width);

			if(!pow2){
				notPower2++;
				String subFolderName = path.toString().replace(folderName + "\\", "");
				System.out.println("(H=" + height + ", W=" + width + ") " + subFolderName);
			}
		}

		System.out.println();
		System.out.println("Total: " + textures.size());
		System.out.println("Not pow 2: " + notPower2);
	}
}
