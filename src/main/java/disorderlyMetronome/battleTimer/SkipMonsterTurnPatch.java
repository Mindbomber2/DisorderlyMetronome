package disorderlyMetronome.battleTimer;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.watcher.SkipEnemiesTurnAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import disorderlyMetronome.util.DisorderlyConfig;

@SpirePatch(clz = GameActionManager.class, method = "callEndOfTurnActions")
public class SkipMonsterTurnPatch {
    @SpirePrefixPatch
    public static void skipMonsterTurn() {
        AbstractDungeon.actionManager.addToBottom(new SkipEnemiesTurnAction());
        if (DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.COOLDOWN || DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.ENERGY) {
            PlayerCountdownPatch.PatchIntoTimer.resetTimer(AbstractDungeon.player);
        }
    }
}