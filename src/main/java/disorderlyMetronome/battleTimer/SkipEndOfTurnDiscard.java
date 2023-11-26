package disorderlyMetronome.battleTimer;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.DiscardAtEndOfTurnAction;
import disorderlyMetronome.util.DisorderlyConfig;

@SpirePatch2(clz = DiscardAtEndOfTurnAction.class, method = "update")
public class SkipEndOfTurnDiscard {
    @SpirePrefixPatch
    public static SpireReturn<?> skipEndOfTurnDiscard(DiscardAtEndOfTurnAction __instance) {
        if(DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.ENERGY || DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.COOLDOWN) {
            __instance.isDone = true;
            return SpireReturn.Return();
        } else {
            return SpireReturn.Continue();
        }
    }
}
