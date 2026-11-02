package edu.digest;

import java.util.List;

public final class GameDigestTest {
    public static void main(String[] args) {
        GameDigest.Week week = new GameDigest.Week(
                List.of(new GameDigest.Asset("Forest badge", "Mina")),
                List.of(new GameDigest.LiveEvent("Build night", "Friday 18:00")),
                List.of(new GameDigest.ModerationItem("Castle map", "review")));
        String result = new GameDigest().render(week);
        require(result.contains("Castle map [review]"), "moderation decision must remain visible");
        require(result.contains("Forest badge by Mina"), "player asset must remain visible");
        System.out.println("GameDigestTest passed");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
