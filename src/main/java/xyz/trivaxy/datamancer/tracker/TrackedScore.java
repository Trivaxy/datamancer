package xyz.trivaxy.datamancer.tracker;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;

public class TrackedScore implements Trackable {

    private final String objective;
    private final String scoreHolder;

    public TrackedScore(String objective, String scoreHolder) {
        this.objective = objective;
        this.scoreHolder = scoreHolder;
    }

    @Override
    public boolean canTrack(MinecraftServer server) {
        return getScoreInfo(server) != null;
    }

    @Override
    public Component getTitle(MinecraftServer server) {
        return Component.score(scoreHolder, objective);
    }

    @Override
    public Component getValue(MinecraftServer server) {
        return getScoreInfo(server).formatValue(StyledFormat.SIDEBAR_DEFAULT);
    }

    @Override
    public String getId() {
        return "score:" + scoreHolder + ":" + objective;
    }

    private ReadOnlyScoreInfo getScoreInfo(MinecraftServer server) {
        return server.getScoreboard().getPlayerScoreInfo(ScoreHolder.forNameOnly(scoreHolder), server.getScoreboard().getObjective(objective));
    }
}
