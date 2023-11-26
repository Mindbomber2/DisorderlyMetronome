package disorderlyMetronome.battleTimer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
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
import disorderlyMetronome.util.RedrawAction;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import javax.sound.midi.Patch;


public class PlayerCountdownPatch {

    @SpirePatch(clz = AbstractPlayer.class, method = SpirePatch.CLASS)
    public static class PatchIntoTimer {
        public static SpireField<Boolean> canPlayCard = new SpireField<>(() -> false);
        public static SpireField<Integer> cyclesThisTurn = new SpireField<>(() -> 0);
        public static SpireField<Boolean> isTriggeringEndOfTurn = new SpireField<>(() -> false);
        public static SpireField<Float> currentPlayerTimer = new SpireField<>(() -> 10f);
        public static SpireField<Float> currentMaxPlayerTimer = new SpireField<>(() -> 10f);

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
            float calculatedTime = PatchIntoTimer.calculateTime(p);
            PatchIntoTimer.currentPlayerTimer.set(p, calculatedTime);
            PatchIntoTimer.currentMaxPlayerTimer.set(p, calculatedTime);
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
            float calculatedTime = PatchIntoTimer.calculateTime(__instance);
            PatchIntoTimer.currentPlayerTimer.set(__instance, calculatedTime);
            PatchIntoTimer.currentMaxPlayerTimer.set(__instance, calculatedTime);
        }
    }


    //   ty Alison again
    public static class EnergyPanelModificationPatches {
        public static boolean overridePanel(){
            return DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.COOLDOWN;
        }

        public static String getMessage() {
                return String.valueOf(PatchIntoTimer.currentPlayerTimer.get(AbstractDungeon.player).intValue());
        }

        public static Color getColor() {
            AbstractPlayer p = AbstractDungeon.player;
            Color color = Color.YELLOW;
            if (Math.floor(PatchIntoTimer.currentPlayerTimer.get(p)) > 2 * PatchIntoTimer.currentMaxPlayerTimer.get(p) / 3) {
                color = Color.GREEN;
            } else if (Math.floor(PatchIntoTimer.currentPlayerTimer.get(p)) < PatchIntoTimer.currentMaxPlayerTimer.get(p) / 3) {
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
                                    "if("+EnergyPanelModificationPatches.class.getName()+".overridePanel()){" +
                                    "$3 = "+EnergyPanelModificationPatches.class.getName()+".getMessage();" +
                                    "$6 = "+EnergyPanelModificationPatches.class.getName()+".getColor();" +
                                    "}"+
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

            AbstractPlayer p = AbstractDungeon.player;
            if (!AbstractDungeon.isScreenUp) {
                PatchIntoTimer.currentPlayerTimer.set(p,
                        PatchIntoTimer.currentPlayerTimer.get(p) - Gdx.graphics.getDeltaTime());
                if (PatchIntoTimer.currentPlayerTimer.get(p) <= 0f) {
                    if (DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.COOLDOWN) {
                        PatchIntoTimer.canPlayCard.set(p, true);
                        ProjectedCardManager.playCards();
                    } else if (DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.ENERGY) {
                        if(EnergyPanel.totalCount<p.energy.energyMaster)
                        AbstractDungeon.actionManager.addToTop(new GainEnergyAction(1));
                    }
                    PatchIntoTimer.cyclesThisTurn.set(p, PatchIntoTimer.cyclesThisTurn.get(p) + 1);

                    if (PatchIntoTimer.cyclesThisTurn.get(p) >= 3 && DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.COOLDOWN) {
                        triggerEndOfTurn();
                    } else if (DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.ENERGY) {
                        if(PatchIntoTimer.cyclesThisTurn.get(p) >= DisorderlyConfig.refreshAfterXEnergy){
                            triggerEndOfTurn();
                        }
                    }
                    PatchIntoTimer.resetTimer(p);
                }
            }
        }

        private static void triggerEndOfTurn() {
            AbstractPlayer p = AbstractDungeon.player;
            PatchIntoTimer.isTriggeringEndOfTurn.set(p, true);
            for (AbstractPower power : p.powers) {
                power.atEndOfTurnPreEndTurnCards(true);
                power.atEndOfTurn(true);
                power.atEndOfRound();
                power.atStartOfTurn();
                power.atStartOfTurnPostDraw();
            }
            for (AbstractCard card : p.hand.group) {
                if (card.retain || card.selfRetain) {
                    card.onRetained();
                }
                card.triggerOnEndOfPlayerTurn();
                card.triggerOnEndOfTurnForPlayingCard();
                card.atTurnStartPreDraw();
                card.atTurnStart();
            }
            for (AbstractRelic r : p.relics) {
                r.onPlayerEndTurn();
                r.atTurnStart();
                r.atTurnStartPostDraw();
            }
            PatchIntoTimer.cyclesThisTurn.set(p, 0);
            PatchIntoTimer.isTriggeringEndOfTurn.set(p, false);
        }
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "render")
    public static class RenderEnergyGainBar {
        @SpirePostfixPatch
        public static void timerCtorPatch(AbstractPlayer __instance, SpriteBatch sb) {
            if (!__instance.isDeadOrEscaped()) {
                DrawHealthbarTimers.drawPlayerTimer(sb, __instance, PlayerCountdownPatch.PatchIntoTimer.currentPlayerTimer.get(__instance),
                        PlayerCountdownPatch.PatchIntoTimer.currentMaxPlayerTimer.get(__instance));
            }
            if (!AbstractDungeon.isScreenUp) {
                if (PlayerCountdownPatch.PatchIntoTimer.currentPlayerTimer.get(__instance) <= 0f) {
                    float calculatedTime = PlayerCountdownPatch.PatchIntoTimer.calculateTime(__instance);
                    PlayerCountdownPatch.PatchIntoTimer.currentPlayerTimer.set(__instance, calculatedTime);
                    PlayerCountdownPatch.PatchIntoTimer.currentMaxPlayerTimer.set(__instance, calculatedTime);
                }
            }
        }
    }
}