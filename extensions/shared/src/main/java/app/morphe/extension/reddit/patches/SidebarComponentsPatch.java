package app.morphe.extension.reddit.patches;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import app.morphe.extension.reddit.settings.Settings;

@SuppressWarnings("unused")
public final class SidebarComponentsPatch {
    private static final List<?> emptyList = Collections.emptyList();

    public static Iterable<?> hideRecentlyVisitedDivider(Iterable<?> i) {
        return Settings.HIDE_RECENTLY_VISITED_SHELF.get() ? emptyList : i;
    }

    public static Collection<?> hideRecentlyVisitedShelf(Collection<?> c) {
        return Settings.HIDE_RECENTLY_VISITED_SHELF.get() ? emptyList : c;
    }

    public static Iterable<?> hideRedditProDivider(Iterable<?> i) {
        return Settings.HIDE_REDDIT_PRO_SHELF.get() ? emptyList : i;
    }

    public static Collection<?> hideRedditProShelf(Collection<?> c) {
        return Settings.HIDE_REDDIT_PRO_SHELF.get() ? emptyList : c;
    }

    public static Iterable<?> hideGamesOnRedditDivider(Iterable<?> i) {
        return Settings.HIDE_GAMES_ON_REDDIT_SHELF.get() ? emptyList : i;
    }

    public static Collection<?> hideGamesOnRedditShelf(Collection<?> c) {
        return Settings.HIDE_GAMES_ON_REDDIT_SHELF.get() ? emptyList : c;
    }
}
