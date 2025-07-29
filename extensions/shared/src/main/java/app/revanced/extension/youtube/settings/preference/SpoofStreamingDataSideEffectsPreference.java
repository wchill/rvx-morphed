package app.revanced.extension.youtube.settings.preference;

import static app.revanced.extension.shared.utils.StringRef.str;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.utils.Utils;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings({"deprecation", "unused"})
public class SpoofStreamingDataSideEffectsPreference extends Preference {

    private final SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        // Because this listener may run before the ReVanced settings fragment updates Settings,
        // this could show the prior config and not the current.
        //
        // Push this call to the end of the main run queue,
        // so all other listeners are done and Settings is up to date.
        Utils.runOnMainThread(this::updateUI);
    };

    public SpoofStreamingDataSideEffectsPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SpoofStreamingDataSideEffectsPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SpoofStreamingDataSideEffectsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpoofStreamingDataSideEffectsPreference(Context context) {
        super(context);
    }

    private void addChangeListener() {
        Setting.preferences.preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    private void removeChangeListener() {
        Setting.preferences.preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        updateUI();
        addChangeListener();
    }

    @Override
    protected void onPrepareForRemoval() {
        super.onPrepareForRemoval();
        removeChangeListener();
    }

    private void updateUI() {
        String audioClientName = Settings.SPOOF_STREAMING_DATA_DEFAULT_CLIENT_AUDIO.get().name().toLowerCase();
        String videoClientName = Settings.SPOOF_STREAMING_DATA_DEFAULT_CLIENT_VIDEO.get().name().toLowerCase();

        boolean audioClientIsTV = audioClientName.startsWith("tv");
        boolean videoClientIsTV = videoClientName.startsWith("tv");

        String summaryTextKeyPrefix = "revanced_spoof_streaming_data_side_effects";
        String audioClientSummaryTextKey = summaryTextKeyPrefix + "_audio_" + audioClientName;
        String videoClientSummaryTextKey = summaryTextKeyPrefix + "_video_" + videoClientName;

        if (!videoClientIsTV && !Settings.SPOOF_STREAMING_DATA_USE_TV.get()) {
            videoClientSummaryTextKey += "_without_tv";
        }

        String audioClientSummaryText = str(audioClientSummaryTextKey);
        String videoClientSummaryText = str(videoClientSummaryTextKey);

        StringBuilder sb = new StringBuilder();
        if (audioClientIsTV && videoClientIsTV) {
            sb.append(videoClientSummaryText);
        } else {
            sb.append(audioClientSummaryText);
            if (!videoClientSummaryText.isEmpty() && !audioClientSummaryText.equals(videoClientSummaryText)) {
                sb.append("\n");
                sb.append(videoClientSummaryText);
            }
        }

        setSummary(sb.toString());
        setEnabled(Settings.SPOOF_STREAMING_DATA.get());
        setSelectable(false);
    }
}