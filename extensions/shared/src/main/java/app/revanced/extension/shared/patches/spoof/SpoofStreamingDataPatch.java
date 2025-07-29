package app.revanced.extension.shared.patches.spoof;

import android.annotation.TargetApi;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protos.youtube.api.innertube.StreamingDataOuterClass.StreamingData;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import app.revanced.extension.shared.innertube.client.YouTubeAppClient.ClientType;
import app.revanced.extension.shared.innertube.utils.ThrottlingParameterUtils;
import app.revanced.extension.shared.patches.PatchStatus;
import app.revanced.extension.shared.patches.spoof.requests.StreamingDataRequest;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.utils.Logger;
import app.revanced.extension.shared.utils.ResourceUtils;
import app.revanced.extension.shared.utils.Utils;
import app.revanced.extension.youtube.shared.VideoInformation;

@TargetApi(26)
@SuppressWarnings("unused")
public class SpoofStreamingDataPatch extends BlockRequestPatch {
    private static final boolean SPOOF_STREAMING_DATA_USE_IOS =
            PatchStatus.SpoofStreamingDataIOS() && BaseSettings.SPOOF_STREAMING_DATA_USE_IOS.get();
    private static final boolean SPOOF_STREAMING_DATA_USE_TV =
            SPOOF_STREAMING_DATA && BaseSettings.SPOOF_STREAMING_DATA_USE_TV.get();
    private static final boolean SPOOF_STREAMING_DATA_USE_TV_ALL =
            SPOOF_STREAMING_DATA_USE_TV && BaseSettings.SPOOF_STREAMING_DATA_USE_TV_ALL.get();
    private static final boolean SPOOF_STREAMING_DATA_TV_USE_LATEST_JS =
            BaseSettings.SPOOF_STREAMING_DATA_TV_USE_LATEST_JS.get();

    /**
     * Any unreachable ip address.  Used to intentionally fail requests.
     */
    private static final String UNREACHABLE_HOST_URI_STRING = "https://127.0.0.0";
    private static final Uri UNREACHABLE_HOST_URI = Uri.parse(UNREACHABLE_HOST_URI_STRING);

    /**
     * Parameters causing playback issues.
     */
    private static final String[] AUTOPLAY_PARAMETERS = {
            "YAHI", // Autoplay in feed.
            "SAFg"  // Autoplay in scrim.
    };
    /**
     * Parameters used when playing clips.
     */
    private static final String CLIPS_PARAMETERS = "kAIB";
    /**
     * Prefix present in all Short player parameters signature.
     */
    private static final String SHORTS_PLAYER_PARAMETERS = "8AEB";
    /**
     * If {@link SpoofStreamingDataPatch#SPOOF_STREAMING_DATA_USE_TV_ALL} is false,
     * Autoplay in feed, Clips, and Shorts will not use the TV client for fast playback.
     * The player parameter is used to detect the video type.
     */
    @NonNull
    private static volatile String reasonSkipped = "";

    /**
     * Injection point.
     */
    public static boolean isSpoofingEnabled() {
        return SPOOF_STREAMING_DATA;
    }

    /**
     * Injection point.
     * Called after {@link #getStreamingData(String)}
     * Used for {@link ClientType#TV}, {@link ClientType#TV_SIMPLY} and {@link ClientType#TV_EMBEDDED}.
     */
    public static boolean lastSpoofedClientIsTV() {
        return SPOOF_STREAMING_DATA_USE_TV &&
                StreamingDataRequest.getLastSpoofedClientIsTV();
    }

    /**
     * Injection point.
     * This method is only invoked when playing a livestream on an iOS client.
     */
    public static boolean fixHLSCurrentTime(boolean original) {
        if (!SPOOF_STREAMING_DATA) {
            return original;
        }
        return false;
    }

    /**
     * Injection point.
     * Skip response encryption in OnesiePlayerRequest.
     */
    public static boolean skipResponseEncryption(boolean original) {
        if (!SPOOF_STREAMING_DATA) {
            return original;
        }
        return false;
    }

    /**
     * Injection point.
     * Turns off a feature flag that interferes with video playback.
     */
    public static boolean usePlaybackStartFeatureFlag(boolean original) {
        if (!SPOOF_STREAMING_DATA) {
            return original;
        }
        return false;
    }

    /**
     * Injection point.
     */
    public static void fetchStreams(String url, Map<String, String> requestHeader) {
        if (SPOOF_STREAMING_DATA) {
            String id = Utils.getVideoIdFromRequest(url);
            if (id == null) {
                Logger.printException(() -> "Ignoring request with no id: " + url);
                return;
            } else if (id.isEmpty()) {
                return;
            }

            StreamingDataRequest.fetchRequest(id, requestHeader, reasonSkipped);
        }
    }

    /**
     * Injection point.
     * Called before {@link #getStreamingData(String)}.
     */
    public static boolean isValidVideoId(@Nullable String videoId) {
        return videoId != null && !videoId.isEmpty() && !"zzzzzzzzzzz".equals(videoId);
    }

    /**
     * Injection point.
     * Fix playback by replace the streaming data.
     * Called after {@link #fetchStreams(String, Map)}.
     */
    public static StreamingData getStreamingData(@NonNull String videoId) {
        if (SPOOF_STREAMING_DATA) {
            try {
                StreamingDataRequest request = StreamingDataRequest.getRequestForVideoId(videoId);
                if (request != null) {
                    // This hook is always called off the main thread,
                    // but this can later be called for the same video id from the main thread.
                    // This is not a concern, since the fetch will always be finished
                    // and never block the main thread.
                    // But if debugging, then still verify this is the situation.
                    if (BaseSettings.DEBUG.get() && !request.fetchCompleted() && Utils.isCurrentlyOnMainThread()) {
                        Logger.printException(() -> "Error: Blocking main thread");
                    }

                    var stream = request.getStream();
                    if (stream != null) {
                        Logger.printDebug(() -> "Overriding video stream: " + videoId);
                        return stream;
                    }
                }

                Logger.printDebug(() -> "Not overriding streaming data (video stream is null): " + videoId);
            } catch (Exception ex) {
                Logger.printException(() -> "getStreamingData failure", ex);
            }
        }

        return null;
    }

    /**
     * Injection point.
     * Called after {@link #getStreamingData(String)}.
     */
    @Nullable
    public static byte[] removeVideoPlaybackPostBody(Uri uri, int method, byte[] postData) {
        if (SPOOF_STREAMING_DATA) {
            try {
                final int methodPost = 2;
                if (method == methodPost) {
                    String path = uri.getPath();
                    if (path != null && path.contains("videoplayback")) {
                        return null;
                    }
                }
            } catch (Exception ex) {
                Logger.printException(() -> "removeVideoPlaybackPostBody failure", ex);
            }
        }

        return postData;
    }

    /**
     * Injection point.
     * <p>
     * Since {@link SpoofStreamingDataPatch} is on a shared path,
     * {@link VideoInformation#newPlayerResponseParameter(String, String, String, boolean)} is not used.
     */
    @Nullable
    public static String newPlayerResponseParameter(@NonNull String newlyLoadedVideoId, @Nullable String playerParameter,
                                                    @Nullable String newlyLoadedPlaylistId, boolean isShortAndOpeningOrPlaying) {
        if (SPOOF_STREAMING_DATA_USE_TV) {
            reasonSkipped = "";
            if (!SPOOF_STREAMING_DATA_USE_TV_ALL && playerParameter != null) {
                if (playerParameter.startsWith(SHORTS_PLAYER_PARAMETERS)) {
                    reasonSkipped = "Shorts";
                }  else if (StringUtils.startsWithAny(playerParameter, AUTOPLAY_PARAMETERS)) {
                    reasonSkipped = "Autoplay in feed";
                } else if (playerParameter.length() > 150 || playerParameter.startsWith(CLIPS_PARAMETERS)) {
                    reasonSkipped = "Clips";
                }
            }
        }

        return playerParameter; // Return the original value since we are observing and not modifying.
    }

    /**
     * Injection point.
     * <p>
     * It takes about 3-5 seconds to download the JavaScript and initialize the Cipher class.
     * Initialize it before the video starts.
     * Used for {@link ClientType#TV}, {@link ClientType#TV_SIMPLY} and {@link ClientType#TV_EMBEDDED}.
     */
    public static void initializeJavascript() {
        if (SPOOF_STREAMING_DATA_USE_TV) {
            // Download JavaScript and initialize the Cipher class
            CompletableFuture.runAsync(() -> ThrottlingParameterUtils.initializeJavascript(
                    SPOOF_STREAMING_DATA_TV_USE_LATEST_JS
            ));
        }
    }

    /**
     * Injection point.
     */
    public static String appendSpoofedClient(String videoFormat) {
        try {
            if (SPOOF_STREAMING_DATA && BaseSettings.SPOOF_STREAMING_DATA_STATS_FOR_NERDS.get()
                    && !TextUtils.isEmpty(videoFormat)) {
                // Force LTR layout, to match the same LTR video time/length layout YouTube uses for all languages
                return "\u202D" + videoFormat + String.format("\u2009(%s)", StreamingDataRequest.getLastSpoofedClientName()); // u202D = left to right override
            }
        } catch (Exception ex) {
            Logger.printException(() -> "appendSpoofedClient failure", ex);
        }

        return videoFormat;
    }

    public static String[] getEntries() {
        Collection<String> entries = Arrays
                .stream(ResourceUtils.getStringArray("revanced_spoof_streaming_data_type_entries"))
                .collect(Collectors.toList());
        if (SPOOF_STREAMING_DATA_USE_IOS) {
            entries.add(ResourceUtils.getString("revanced_spoof_streaming_data_type_entry_ios_deprecated"));
        }
        if (SPOOF_STREAMING_DATA_USE_TV) {
            entries.add(ResourceUtils.getString("revanced_spoof_streaming_data_type_entry_tv"));
            entries.add(ResourceUtils.getString("revanced_spoof_streaming_data_type_entry_tv_simply"));
        }
        return entries.toArray(new String[0]);
    }

    public static String[] getEntryValues() {
        Collection<String> entryValues = Arrays
                .stream(ResourceUtils.getStringArray("revanced_spoof_streaming_data_type_entry_values"))
                .collect(Collectors.toList());
        if (SPOOF_STREAMING_DATA_USE_IOS) {
            entryValues.add("IOS_DEPRECATED");
        }
        if (SPOOF_STREAMING_DATA_USE_TV) {
            entryValues.add("TV");
            entryValues.add("TV_SIMPLY");
        }
        return entryValues.toArray(new String[0]);
    }

    public static boolean notSpoofingToAndroid() {
        return !PatchStatus.SpoofStreamingData()
                || !BaseSettings.SPOOF_STREAMING_DATA.get()
                || !BaseSettings.SPOOF_STREAMING_DATA_TYPE.get().name().startsWith("ANDROID"); // IOS, TV
    }

    public static final class ClientAndroidVRAvailability implements Setting.Availability {
        @Override
        public boolean isAvailable() {
            return BaseSettings.SPOOF_STREAMING_DATA.get() &&
                    BaseSettings.SPOOF_STREAMING_DATA_TYPE.get().name().startsWith("ANDROID_VR");
        }
    }

    public static final class ClientAndroidVRNoAuthAvailability implements Setting.Availability {
        @Override
        public boolean isAvailable() {
            return BaseSettings.SPOOF_STREAMING_DATA.get() &&
                    BaseSettings.SPOOF_STREAMING_DATA_TYPE.get() == ClientType.ANDROID_VR_NO_AUTH;
        }
    }

    public static final class ClientiOSAvailability implements Setting.Availability {
        @Override
        public boolean isAvailable() {
            return BaseSettings.SPOOF_STREAMING_DATA.get() &&
                    BaseSettings.SPOOF_STREAMING_DATA_TYPE.get().name().startsWith("IOS");
        }
    }

    public static final class ClientTVAvailability implements Setting.Availability {
        @Override
        public boolean isAvailable() {
            return BaseSettings.SPOOF_STREAMING_DATA.get() &&
                    BaseSettings.SPOOF_STREAMING_DATA_TYPE.get().name().startsWith("TV");
        }
    }

    public static final class ForceOriginalAudioAvailability implements Setting.Availability {
        private static final boolean AVAILABLE_ON_LAUNCH = SpoofStreamingDataPatch.notSpoofingToAndroid();

        @Override
        public boolean isAvailable() {
            // Check conditions of launch and now. Otherwise if spoofing is changed
            // without a restart the setting will show as available when it's not.
            return AVAILABLE_ON_LAUNCH && SpoofStreamingDataPatch.notSpoofingToAndroid();
        }
    }

    public static final class HideAudioFlyoutMenuAvailability implements Setting.Availability {
        private static final boolean AVAILABLE_ON_LAUNCH = SpoofStreamingDataPatch.notSpoofingToAndroid();

        @Override
        public boolean isAvailable() {
            // Check conditions of launch and now. Otherwise if spoofing is changed
            // without a restart the setting will show as available when it's not.
            return AVAILABLE_ON_LAUNCH && SpoofStreamingDataPatch.notSpoofingToAndroid();
        }
    }
}
