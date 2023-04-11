package gg.projecteden.tools;

import gg.projecteden.api.common.EdenAPI;
import gg.projecteden.api.common.utils.Env;

public class API extends EdenAPI {

	@Override
	public Env getEnv() {
		return Env.TEST;
	}

	@Override
	public void shutdown() {

	}

}
