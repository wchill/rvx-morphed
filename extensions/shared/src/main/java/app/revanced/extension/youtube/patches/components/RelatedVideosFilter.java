package app.revanced.extension.youtube.patches.components;

import app.revanced.extension.shared.patches.components.ByteArrayFilterGroup;
import app.revanced.extension.shared.patches.components.Filter;
import app.revanced.extension.shared.patches.components.StringFilterGroup;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.RootView;

@SuppressWarnings("unused")
public final class RelatedVideosFilter extends Filter {
    private final ByteArrayFilterGroup relatedVideo =
            new ByteArrayFilterGroup(
                    null,
                    "relatedH"
            );

    public RelatedVideosFilter() {
        addPathCallbacks(
                new StringFilterGroup(
                        Settings.HIDE_RELATED_VIDEOS,
                        "video_lockup_with_attachment.eml"
                )
        );
    }

    @Override
    public boolean isFiltered(String path, String identifier, String allValue, byte[] buffer,
                              StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        return RootView.isPlayerActive() && relatedVideo.check(buffer).isFiltered();
    }
}
