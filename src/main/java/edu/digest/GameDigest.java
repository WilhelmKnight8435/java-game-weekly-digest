package edu.digest;

import java.util.List;
import java.util.stream.Collectors;

public final class GameDigest {
    public record Asset(String title, String creator) {}
    public record LiveEvent(String title, String time) {}
    public record ModerationItem(String title, String status) {}
    public record Week(List<Asset> assets, List<LiveEvent> events, List<ModerationItem> queue) {}

    public String render(Week week) {
        String assets = week.assets().stream().map(a -> "- " + a.title() + " by " + a.creator()).collect(Collectors.joining("\n"));
        String events = week.events().stream().map(e -> "- " + e.title() + " (" + e.time() + ")").collect(Collectors.joining("\n"));
        String moderation = week.queue().isEmpty()
                ? "No moderation items are waiting."
                : week.queue().stream().map(q -> "- " + q.title() + " [" + q.status() + "]").collect(Collectors.joining("\n"));
        return "Weekly player digest\n\nPlayer assets\n" + assets + "\n\nLive events\n" + events
                + "\n\nModeration queue\n" + moderation;
    }
}
