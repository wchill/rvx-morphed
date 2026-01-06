package app.morphe.extension.shared.settings;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static app.morphe.extension.shared.settings.Setting.parent;

/**
 * Settings shared across multiple apps.
 * <p>
 * To ensure this class is loaded when the UI is created, app specific setting bundles should extend
 * or reference this class.
 */
public class BaseSettings {
    public static final BooleanSetting DEBUG = new BooleanSetting("rvx_morphed_debug", FALSE);
    public static final BooleanSetting DEBUG_TOAST_ON_ERROR = new BooleanSetting("rvx_morphed_debug_toast_on_error", FALSE);

    public static final EnumSetting<AppLanguage> RVX_MORPHED_LANGUAGE = new EnumSetting<>("rvx_morphed_language", AppLanguage.DEFAULT, true);

    public static final BooleanSetting SETTINGS_SEARCH_HISTORY = new BooleanSetting("rvx_morphed_settings_search_history", TRUE, true);
    public static final StringSetting SETTINGS_SEARCH_ENTRIES = new StringSetting("rvx_morphed_settings_search_entries", "", true);

    /**
     * @noinspection DeprecatedIsStillUsed
     */
    @Deprecated
    // The official ReVanced does not offer this, so it has been removed from the settings only. Users can still access settings through import / export settings.
    public static final StringSetting BYPASS_IMAGE_REGION_RESTRICTIONS_DOMAIN = new StringSetting("rvx_morphed_bypass_image_region_restrictions_domain", "yt4.ggpht.com", true);

}
