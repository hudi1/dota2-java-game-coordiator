package org.tomass.dota.gc.handlers.callbacks.match;

import java.util.ArrayList;
import java.util.List;

import org.tomass.dota.gc.config.AppConfig;
import org.tomass.protobuf.dota.DotaGcmessagesClientWatch.CMsgGCToClientFindTopSourceTVGamesResponse.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class TopSourceTvGamesCallback extends CallbackMsg {

    private List<Game> games = new ArrayList<>();

    private boolean specificGame;

    private final String NEW_LINE = System.getProperty("line.separator");

    public TopSourceTvGamesCallback(Builder builder, AppConfig appConfig) {
        this.specificGame = builder.getSpecificGames();
        for (org.tomass.protobuf.dota.DotaGcmessagesClientWatch.CSourceTVGameSmall.Builder sourceGame : builder
                .getGameListBuilderList()) {
            Game game = new Game();
            game.setAverageMmr(sourceGame.getAverageMmr());
            game.setDireScore(sourceGame.getDireScore());
            game.setRadiantScore(sourceGame.getRadiantScore());
            game.setRadiantLead(sourceGame.getRadiantLead());
            game.setLobbyId(sourceGame.getLobbyId());
            game.setMatchId(sourceGame.getMatchId());
            game.setDelay(sourceGame.getDelay());
            for (org.tomass.protobuf.dota.DotaGcmessagesClientWatch.CSourceTVGameSmall.Player sourcePlayer : sourceGame
                    .getPlayersList()) {
                Player player = new Player();
                player.setAccountId(sourcePlayer.getAccountId());
                player.setHeroId(sourcePlayer.getHeroId());
                player.setHero(appConfig.getHeroes().get(player.getHeroId()));
                player.setName(appConfig.getNotableplayers().get(player.getAccountId()));
                game.getPlayers().add(player);
            }
            games.add(game);
        }
    }

    public List<Game> getGames() {
        return games;
    }

    public void setGames(List<Game> games) {
        this.games = games;
    }

    @Override
    public String toString() {
        return "TopSourceTvGamesCallback [games=" + games + "]";
    }

    public boolean isSpecificGame() {
        return specificGame;
    }

    public void setSpecificGame(boolean specificGame) {
        this.specificGame = specificGame;
    }

    public class Game extends CallbackMsg {

        private Integer averageMmr;
        private Long lobbyId;
        private Integer radiantLead;
        private Integer radiantScore;
        private Integer direScore;
        private Long matchId;
        private Integer delay;
        private List<Player> players;

        public Integer getAverageMmr() {
            return averageMmr;
        }

        public void setAverageMmr(Integer averageMmr) {
            this.averageMmr = averageMmr;
        }

        public Long getLobbyId() {
            return lobbyId;
        }

        public void setLobbyId(Long lobbyId) {
            this.lobbyId = lobbyId;
        }

        public Integer getRadiantLead() {
            return radiantLead;
        }

        public void setRadiantLead(Integer radiantLead) {
            this.radiantLead = radiantLead;
        }

        public Integer getRadiantScore() {
            return radiantScore;
        }

        public void setRadiantScore(Integer radiantScore) {
            this.radiantScore = radiantScore;
        }

        public Integer getDireScore() {
            return direScore;
        }

        public void setDireScore(Integer direScore) {
            this.direScore = direScore;
        }

        public Long getMatchId() {
            return matchId;
        }

        public void setMatchId(Long matchId) {
            this.matchId = matchId;
        }

        public Integer getDelay() {
            return delay;
        }

        public void setDelay(Integer delay) {
            this.delay = delay;
        }

        public List<Player> getPlayers() {
            if (players == null) {
                players = new ArrayList<>();
            }
            return players;
        }

        public void setPlayers(List<Player> players) {
            this.players = players;
        }

        @Override
        public String toString() {
            return "Game [averageMmr=" + averageMmr + ", lobbyId=" + lobbyId + ", radiantLead=" + radiantLead
                    + ", radiantScore=" + radiantScore + ", direScore=" + direScore + ", matchId=" + matchId
                    + ", delay=" + delay + ", players=" + players + "]";
        }

        public String toWellPrintedString() {
            StringBuilder builder = new StringBuilder();

            builder.append("Game: " + NEW_LINE);
            builder.append("Average mmr: " + getAverageMmr() + NEW_LINE);
            builder.append("Delay: " + getDelay() + NEW_LINE);
            builder.append("Dire score: " + getDireScore() + NEW_LINE);
            builder.append("Radiant score: " + getRadiantScore() + NEW_LINE);
            builder.append("Lobby id: " + getLobbyId() + NEW_LINE);
            builder.append("Match id: " + getMatchId() + NEW_LINE);
            builder.append("Radiant lead: " + getRadiantLead() + NEW_LINE);
            builder.append(NEW_LINE);
            for (int i = 0; i < getPlayers().size(); i++) {
                builder.append("Player: " + NEW_LINE);
                builder.append("Account id: " + getPlayers().get(i).getAccountId() + NEW_LINE);
                builder.append("Hero id: " + getPlayers().get(i).getHeroId() + NEW_LINE);
                builder.append("Name: " + getPlayers().get(i).getName() + NEW_LINE);
                builder.append("Hero: " + getPlayers().get(i).getHero() + NEW_LINE);
                if (i == 4) {
                    builder.append(NEW_LINE);
                }
            }

            return builder.toString();
        }

    }

    public class Player {
        private Integer accountId;
        private Integer heroId;
        private String name;
        private String hero;

        public Integer getAccountId() {
            return accountId;
        }

        public void setAccountId(Integer accountId) {
            this.accountId = accountId;
        }

        public Integer getHeroId() {
            return heroId;
        }

        public void setHeroId(Integer heroId) {
            this.heroId = heroId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHero() {
            return hero;
        }

        public void setHero(String hero) {
            this.hero = hero;
        }

        @Override
        public String toString() {
            return "Player [accountId=" + accountId + ", heroId=" + heroId + ", name=" + name + ", hero=" + hero + "]";
        }

    }
}
