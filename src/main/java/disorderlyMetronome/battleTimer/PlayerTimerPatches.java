package disorderlyMetronome.battleTimer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import disorderlyMetronome.cardLogic.ProjectedCardManager;
import disorderlyMetronome.util.DisorderlyConfig;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;


public class PlayerTimerPatches {

    @SpirePatch(clz = AbstractPlayer.class, method = SpirePatch.CLASS)
    public static class PlayerTimerPatch {
        public static SpireField<Boolean> canPlayCard = new SpireField<>(() -> false);
        public static SpireField<Integer> cyclesThisTurn = new SpireField<>(() -> 0);
        public static SpireField<Boolean> isTriggeringEndOfTurn = new SpireField<>(() -> false);
        public static SpireField<Float> currentPlayerTimer = new SpireField<>(() -> 10f);
        public static SpireField<Float> currentMaxPlayerTimer = new SpireField<>(() -> 10f);
        public static SpireField<Boolean> timeAttackIsPlayerTurn = new SpireField<>(() -> false);
        public static SpireField<Boolean> timeAttackIsMonsterTurn = new SpireField<>(() -> false);

        //TODO: fix calculation for first turn. Probably use start of combat hook
        public static float calculateTime(AbstractPlayer __instance) {
            if (DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.COOLDOWN) {
                return DisorderlyConfig.cdmDelay;
            } else if (DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.ENERGY) {
                if (__instance.energy != null) {
                    return DisorderlyConfig.energyGainRate * 3 / __instance.energy.energyMaster;
                } else {
                    return DisorderlyConfig.energyGainRate;
                }
            } else if (DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.TIMEATTACK) {
                if (__instance.energy != null) {
                    return DisorderlyConfig.baseTurnDuration + DisorderlyConfig.turnDurationBonus * __instance.energy.energyMaster;
                } else {
                    return DisorderlyConfig.baseTurnDuration + DisorderlyConfig.turnDurationBonus * 3;
                }
            } else {
                //invalid mode
                return -1;
            }
        }

        public static void resetTimer(AbstractPlayer p) {
            float calculatedTime = PlayerTimerPatch.calculateTime(p);
            PlayerTimerPatch.currentPlayerTimer.set(p, calculatedTime);
            PlayerTimerPatch.currentMaxPlayerTimer.set(p, calculatedTime);
        }

        public static void onBattleStart(AbstractPlayer p) {
            System.out.println("onBattleStart call");
            resetTimer(p);
            timeAttackIsPlayerTurn.set(p, true);
            timeAttackIsMonsterTurn.set(p, false);
            cyclesThisTurn.set(p, 0);
            canPlayCard.set(p, false);
        }
    }

    @SpirePatch2(clz = AbstractPlayer.class, method = SpirePatch.CONSTRUCTOR,
            paramtypez = {
                    String.class,
                    AbstractPlayer.PlayerClass.class
            }
    )

    public static class ConstructorTimer {
        @SpirePostfixPatch
        public static void timerCtorPatch(AbstractPlayer __instance) {
            System.out.println("Patching ctor of " + __instance.name);
            float calculatedTime = PlayerTimerPatch.calculateTime(__instance);
            PlayerTimerPatch.currentPlayerTimer.set(__instance, calculatedTime);
            PlayerTimerPatch.currentMaxPlayerTimer.set(__instance, calculatedTime);
        }
    }


    //   ty Alison again
    public static class EnergyPanelModificationPatches {
        public static boolean overridePanel() {
            return DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.COOLDOWN;
        }

        public static String getMessage() {
            return String.valueOf(PlayerTimerPatch.currentPlayerTimer.get(AbstractDungeon.player).intValue());
        }

        public static Color getColor() {
            AbstractPlayer p = AbstractDungeon.player;
            Color color = Color.YELLOW;
            if (Math.floor(PlayerTimerPatch.currentPlayerTimer.get(p)) > 2 * PlayerTimerPatch.currentMaxPlayerTimer.get(p) / 3) {
                color = Color.GREEN;
            } else if (Math.floor(PlayerTimerPatch.currentPlayerTimer.get(p)) < PlayerTimerPatch.currentMaxPlayerTimer.get(p) / 3) {
                color = Color.RED;
            }
            return color;
        }

        @SpirePatch2(clz = EnergyPanel.class, method = "render")
        public static class BeDifferentColorPls {
            @SpireInstrumentPatch
            public static ExprEditor patch() {
                return new ExprEditor() {
                    @Override
                    //Method call is basically the equivalent of a methodcallmatcher of an insert patch, checks the edit method against every method call in the function you#re patching
                    public void edit(MethodCall m) throws CannotCompileException {
                        //If the method is from the class FontHelper and the method is called renderFontCentered
                        if (m.getClassName().equals(FontHelper.class.getName()) && m.getMethodName().equals("renderFontCentered")) {
                            m.replace("{" +
                                    //$3 refers to the third input parameter of the method, in this case the message
                                    //You need the full package to your class for this to work
                                    "if(" + EnergyPanelModificationPatches.class.getName() + ".overridePanel()){" +
                                    "$3 = " + EnergyPanelModificationPatches.class.getName() + ".getMessage();" +
                                    "$6 = " + EnergyPanelModificationPatches.class.getName() + ".getColor();" +
                                    "}" +
                                    //Call the method as normal
                                    "$proceed($$);" +
                                    "}");
                        }
                    }
                };
            }
        }
    }

    @SpirePatch2(clz = EnergyPanel.class, method = "update")
    public static class PlayerCooldownUpdatePatch {
        @SpirePostfixPatch
        public static void letItGoDown() {

            AbstractPlayer player = AbstractDungeon.player;
            if (!AbstractDungeon.isScreenUp) {
                //Speed up timers if nobodys turn in time attack
                //TODO: Add vault visuals during speedup maybe?
                if (DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.TIMEATTACK && PlayerTimerPatch.timeAttackIsPlayerTurn.get(player) == false && PlayerTimerPatch.timeAttackIsMonsterTurn.get(player) == false) {
                    PlayerTimerPatch.currentPlayerTimer.set(player,
                            PlayerTimerPatch.currentPlayerTimer.get(player) - (5 * Gdx.graphics.getDeltaTime()));
                    //pause timer if in time attack mode and it's a monster's turn
                } else if (DisorderlyConfig.gameMode != DisorderlyConfig.GameMode.TIMEATTACK || PlayerTimerPatches.PlayerTimerPatch.timeAttackIsMonsterTurn.get(AbstractDungeon.player) == false) {
                    PlayerTimerPatch.currentPlayerTimer.set(player,
                            PlayerTimerPatch.currentPlayerTimer.get(player) - Gdx.graphics.getDeltaTime());
                }

                if (PlayerTimerPatch.currentPlayerTimer.get(player) <= 0f) {
                    if (DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.COOLDOWN) {
                        PlayerTimerPatch.canPlayCard.set(player, true);
                        ProjectedCardManager.playCards();
                    } else if (DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.ENERGY) {
                        if (EnergyPanel.totalCount < player.energy.energyMaster)
                            AbstractDungeon.actionManager.addToTop(new GainEnergyAction(1));
                    }
                    PlayerTimerPatch.cyclesThisTurn.set(player, PlayerTimerPatch.cyclesThisTurn.get(player) + 1);

                    if ((DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.COOLDOWN && PlayerTimerPatch.cyclesThisTurn.get(player) >= 3)
                            || (DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.ENERGY && PlayerTimerPatch.cyclesThisTurn.get(player) >= DisorderlyConfig.triggerRoundEndEffectAfterXEnergy)) {
                        triggerEndOfTurn();
                        triggerStartOfTurn();
                    } else if (DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.TIMEATTACK) {
                        if (PlayerTimerPatch.timeAttackIsPlayerTurn.get(player) == true) {
                            timeAttackEndTurn();
                            //20 Second delay until next turn
                            PlayerTimerPatch.currentPlayerTimer.set(player, 20f);
                            PlayerTimerPatch.currentMaxPlayerTimer.set(player, 20f);
                        } else {
                            AbstractDungeon.actionManager.addToTop(new DrawCardAction(5));
                            player.energy.recharge();
                            player.loseBlock(player.currentBlock/2);
                            triggerStartOfTurn();
                            PlayerTimerPatch.timeAttackIsPlayerTurn.set(player, true);
                        }


                    }
                }
            }
        }

        public static void timeAttackEndTurn() {
            AbstractPlayer player = AbstractDungeon.player;
            //player.endTurnQueued=true;
            triggerEndOfTurn();
            AbstractDungeon.actionManager.addToBottom(new DiscardAction(player, null, AbstractDungeon.player.hand.size(), true, true));
            PlayerTimerPatch.timeAttackIsPlayerTurn.set(player, false);
        }

        private static void triggerEndOfTurn() {
            AbstractPlayer p = AbstractDungeon.player;
            PlayerTimerPatch.isTriggeringEndOfTurn.set(p, true);
            for (AbstractPower power : p.powers) {
                power.atEndOfTurnPreEndTurnCards(true);
                power.atEndOfTurn(true);
                power.atEndOfRound();

            }
            for (AbstractCard card : p.hand.group) {
                if (card.retain || card.selfRetain) {
                    card.onRetained();
                }
                card.triggerOnEndOfPlayerTurn();
                card.triggerOnEndOfTurnForPlayingCard();
            }
            for (AbstractRelic r : p.relics) {
                r.onPlayerEndTurn();
            }
        }

        private static void triggerStartOfTurn() {
            AbstractPlayer p = AbstractDungeon.player;
            for (AbstractPower power : p.powers) {
                power.atStartOfTurn();
                power.atStartOfTurnPostDraw();
            }
            for (AbstractCard card : p.hand.group) {
                card.atTurnStartPreDraw();
                card.atTurnStart();
            }
            for (AbstractRelic r : p.relics) {
                r.atTurnStart();
                r.atTurnStartPostDraw();
            }
            PlayerTimerPatch.cyclesThisTurn.set(p, 0);
            PlayerTimerPatch.isTriggeringEndOfTurn.set(p, false);
            PlayerTimerPatch.resetTimer(p);
        }
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "render")
    public static class RenderPlayerTimerBar {
        @SpirePostfixPatch
        public static void renderPlayerTimer(AbstractPlayer __instance, SpriteBatch sb) {
            if (!__instance.isDeadOrEscaped()) {
                DrawHealthbarTimers.drawPlayerTimer(sb, __instance, PlayerTimerPatch.currentPlayerTimer.get(__instance),
                        PlayerTimerPatch.currentMaxPlayerTimer.get(__instance));
            }
            if (!AbstractDungeon.isScreenUp) {
                if (PlayerTimerPatch.currentPlayerTimer.get(__instance) <= 0f) {
                    float calculatedTime = PlayerTimerPatch.calculateTime(__instance);
                    PlayerTimerPatch.currentPlayerTimer.set(__instance, calculatedTime);
                    PlayerTimerPatch.currentMaxPlayerTimer.set(__instance, calculatedTime);
                }
            }
        }
    }
}