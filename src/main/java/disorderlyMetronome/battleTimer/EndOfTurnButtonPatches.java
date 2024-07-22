package disorderlyMetronome.battleTimer;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.actions.unique.LoseEnergyAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.ui.buttons.EndTurnButton;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import disorderlyMetronome.util.DisorderlyConfig;
import disorderlyMetronome.util.RedrawAction;

public class EndOfTurnButtonPatches {
    @SpirePatch2(clz = EndTurnButton.class, method = "disable", paramtypez = {
            boolean.class
    })
    public static class RefreshFunction {
        @SpireInsertPatch(rloc = 8)
        public static SpireReturn<?> makeButtonRefresh(EndTurnButton __instance) {
            if (DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.ENERGY && !PlayerTimerPatches.PlayerTimerPatch.isTriggeringEndOfTurn.get(AbstractDungeon.player)) {
                RefreshBoolean.isRefreshing.set(__instance, true);
                AbstractDungeon.actionManager.addToBottom(new LoseEnergyAction(1));
                AbstractDungeon.actionManager.addToBottom(new DiscardAction(AbstractDungeon.player, null, AbstractDungeon.player.hand.size(), true, true));
                AbstractDungeon.actionManager.addToBottom((new RedrawAction()));
                return SpireReturn.Return();
            } else if (DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.TIMEATTACK && !PlayerTimerPatches.PlayerTimerPatch.isTriggeringEndOfTurn.get(AbstractDungeon.player)) {
                PlayerTimerPatches.PlayerTimerPatch.currentPlayerTimer.set(AbstractDungeon.player, 0f);
                PlayerTimerPatches.PlayerCooldownUpdatePatch.timeAttackEndTurn();
                return SpireReturn.Return();
            } else {
                return SpireReturn.Continue();
            }
        }
    }

    @SpirePatch2(clz = EndTurnButton.class, method = "updateText")
    public static class ButtonLabel {
        @SpirePrefixPatch()
        public static SpireReturn<?> makeItSayRefresh(@ByRef String[] ___label) {
            if (DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.ENERGY) {
                ___label[0] = "Refresh";
                return SpireReturn.Return();
            } else {
                return SpireReturn.Continue();
            }
        }
    }

    @SpirePatch2(clz = EndTurnButton.class, method = "update")
    public static class ButtonEnabling {
        @SpirePrefixPatch()
        public static void makeItEnabled(EndTurnButton __instance) {
            if (DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.ENERGY) {
                __instance.enabled = EnergyPanel.totalCount >= 1 && !RefreshBoolean.isRefreshing.get(__instance);
            } else if (DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.TIMEATTACK) {
                __instance.enabled = PlayerTimerPatches.PlayerTimerPatch.timeAttackIsPlayerTurn.get(AbstractDungeon.player);
            }
        }
    }


    @SpirePatch(clz = EndTurnButton.class, method = SpirePatch.CLASS)
    public static class RefreshBoolean {
        public static SpireField<Boolean> isRefreshing = new SpireField<>(() -> false);

    }
}