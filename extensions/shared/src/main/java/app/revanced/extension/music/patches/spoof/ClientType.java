package app.revanced.extension.music.patches.spoof;

import java.util.Locale;

public enum ClientType {
    IOS_MUSIC_6_21(26,
            "Apple",
            "Apple",
            "iPhone16,2",
            "iOS",
            "17.0.2.21A350",
            "6.21"
    ),
    IOS_MUSIC_7_04(IOS_MUSIC_6_21.id,
            IOS_MUSIC_6_21.deviceBrand,
            IOS_MUSIC_6_21.deviceMake,
            IOS_MUSIC_6_21.deviceModel,
            IOS_MUSIC_6_21.osName,
            IOS_MUSIC_6_21.osVersion,
            "7.04"
    );

    /**
     * YouTube
     * <a href="https://github.com/zerodytrash/YouTube-Internal-Clients?tab=readme-ov-file#clients">client type</a>
     */
    public final int id;

    /**
     * Device brand.
     */
    public final String deviceBrand;

    /**
     * Device make.
     */
    public final String deviceMake;

    /**
     * Device model.
     */
    public final String deviceModel;

    /**
     * Device OS name.
     */
    public final String osName;

    /**
     * Device OS version.
     */
    public final String osVersion;

    /**
     * App version.
     */
    public final String clientVersion;

    /**
     * Client user-agent.
     */
    public final String userAgent;

    @SuppressWarnings("ConstantLocale")
    ClientType(int id,
               String deviceBrand,
               String deviceMake,
               String deviceModel,
               String osName,
               String osVersion,
               String clientVersion) {
        this.id = id;
        this.deviceBrand = deviceBrand;
        this.deviceMake = deviceMake;
        this.deviceModel = deviceModel;
        this.osName = osName;
        this.osVersion = osVersion;
        this.clientVersion = clientVersion;
        this.userAgent = String.format(
                Locale.ENGLISH,
                "com.google.ios.youtubemusic/%s (iPhone16,2; U; CPU iOS 17_0_2 like Mac OS X; %s)",
                clientVersion,
                Locale.getDefault()
        );
    }

}
