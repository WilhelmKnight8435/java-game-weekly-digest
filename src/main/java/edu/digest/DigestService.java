package edu.digest;

public final class DigestService {
    private final InfraiClient infrai;
    private final GameDigest digest = new GameDigest();

    public DigestService(InfraiClient infrai) { this.infrai = infrai; }

    public String publishWeekly(GameDigest.Week week, String queue, String taskUrl) throws Exception {
        String content = digest.render(week);
        infrai.createQueue(queue);
        infrai.publish(queue, content);
        return infrai.schedule(taskUrl);
    }
}
