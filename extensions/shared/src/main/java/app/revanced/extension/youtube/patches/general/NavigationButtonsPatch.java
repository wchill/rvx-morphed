package app.revanced.extension.youtube.patches.general;

import static app.revanced.extension.shared.utils.Utils.hideViewUnderCondition;
import static app.revanced.extension.youtube.shared.NavigationBar.NavigationButton;

import android.view.View;
import android.widget.TextView;

import org.apache.commons.lang3.BooleanUtils;

import java.util.EnumMap;
import java.util.Map;

import app.revanced.extension.shared.utils.ResourceUtils;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.utils.ExtendedUtils;

@SuppressWarnings("unused")
public final class NavigationButtonsPatch {

    private static final boolean ENABLE_NARROW_NAVIGATION_BUTTONS
            = Settings.ENABLE_NARROW_NAVIGATION_BUTTONS.get();

    private static final boolean ENABLE_TRANSLUCENT_NAVIGATION_BAR
            = Settings.ENABLE_TRANSLUCENT_NAVIGATION_BAR.get();

    private static final boolean SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON
            = Settings.SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON.get();

    private static Map<NavigationButton, Boolean> shouldHideMap;

    private static int libraryCairoId = -1;

    /**
     * Injection point.
     */
    public static boolean enableNarrowNavigationButton(boolean original) {
        return ENABLE_NARROW_NAVIGATION_BUTTONS || original;
    }

    /**
     * Injection point.
     */
    public static boolean enableTranslucentNavigationBar() {
        return ENABLE_TRANSLUCENT_NAVIGATION_BAR;
    }

    /**
     * Injection point.
     *
     * @noinspection ALL
     */
    public static void setCairoNotificationFilledIcon(EnumMap enumMap, Enum tabActivityCairo) {
        final int fillBellCairoBlack = ResourceUtils.getDrawableIdentifier("yt_fill_bell_cairo_black_24");
        if (fillBellCairoBlack != 0) {
            // It's very unlikely, but Google might fix this issue someday.
            // If so, [fillBellCairoBlack] might already be in enumMap.
            // That's why 'EnumMap.putIfAbsent()' is used instead of 'EnumMap.put()'.
            enumMap.putIfAbsent(tabActivityCairo, Integer.valueOf(fillBellCairoBlack));
        }
    }

    private static int getLibraryCairoId() {
        if (libraryCairoId == -1) {
            libraryCairoId = ResourceUtils.getIdIdentifier("yt_outline_library_cairo_black_24");
        }
        return libraryCairoId;
    }

    /**
     * Injection point.
     */
    public static int getLibraryDrawableId(int original) {
        if (ExtendedUtils.IS_19_26_OR_GREATER &&
                !ExtendedUtils.isSpoofingToLessThan("19.27.00")) {
            int libraryCairoId = getLibraryCairoId();
            if (libraryCairoId != 0) {
                return libraryCairoId;
            }
        }
        return original;
    }

    private static Map<NavigationButton, Boolean> getHideMap() {
        if (shouldHideMap == null || shouldHideMap.isEmpty()) {
            shouldHideMap = new EnumMap<>(NavigationButton.class) {
                {
                    put(NavigationButton.HOME, Settings.HIDE_NAVIGATION_HOME_BUTTON.get());
                    put(NavigationButton.SHORTS, Settings.HIDE_NAVIGATION_SHORTS_BUTTON.get());
                    put(NavigationButton.SUBSCRIPTIONS, Settings.HIDE_NAVIGATION_SUBSCRIPTIONS_BUTTON.get());
                    put(NavigationButton.CREATE, Settings.HIDE_NAVIGATION_CREATE_BUTTON.get());
                    put(NavigationButton.NOTIFICATIONS, Settings.HIDE_NAVIGATION_NOTIFICATIONS_BUTTON.get());
                    put(NavigationButton.LIBRARY, Settings.HIDE_NAVIGATION_LIBRARY_BUTTON.get());
                }
            };
        }
        return shouldHideMap;
    }

    /**
     * Injection point.
     */
    public static boolean switchCreateWithNotificationButton(boolean original) {
        return Settings.SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON.get() || original;
    }

    /**
     * Injection point.
     */
    public static void navigationTabCreated(NavigationButton button, View tabView) {
        if (BooleanUtils.isTrue(getHideMap().get(button))) {
            tabView.setVisibility(View.GONE);
        }
    }

    /**
     * Injection point.
     */
    public static void hideNavigationLabel(TextView view) {
        hideViewUnderCondition(Settings.HIDE_NAVIGATION_LABEL, view);
    }

    /**
     * Injection point.
     */
    public static void hideNavigationBar(View view) {
        hideViewUnderCondition(Settings.HIDE_NAVIGATION_BAR, view);
    }

}
