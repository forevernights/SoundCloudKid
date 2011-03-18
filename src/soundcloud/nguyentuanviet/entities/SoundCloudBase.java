package soundcloud.nguyentuanviet.entities;

import org.urbanstew.soundcloudapi.SoundCloudAPI;
import org.urbanstew.soundcloudapi.SoundCloudAPI.OAuthVersion;

public final class SoundCloudBase {
	private static String consumerKey="AIBMBzom4aIwS64tzA3uvg";
	private static String consumerSecret="FwvM54R1RaYSOatSuRJA8lvqnycVLeH4OqIDGf4zI";
	private static final SoundCloudAPI api = new SoundCloudAPI(consumerKey, consumerSecret, SoundCloudAPI.USE_PRODUCTION.with(OAuthVersion.V2_0));
	public SoundCloudAPI get(){
		return api;
	}
}
