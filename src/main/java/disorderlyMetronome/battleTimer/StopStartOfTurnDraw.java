package disorderlyMetronome.battleTimer;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import disorderlyMetronome.util.DisorderlyConfig;

@SpirePatch2(clz = DrawCardAction.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {
        AbstractCreature.class,
        int.class,
        boolean.class
})
public class StopStartOfTurnDraw {
    @SpirePrefixPatch
    public static SpireReturn<?> skipStartOfTurnDraw(DrawCardAction __instance, boolean endTurnDraw) {
        if (DisorderlyConfig.gameMode== DisorderlyConfig.GameMode.COOLDOWN || DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.ENERGY) {
            if (endTurnDraw && !(AbstractDungeon.actionManager.turn == 1)) {
                __instance.isDone = true;
                return SpireReturn.Return();
            }
        }
        return SpireReturn.Continue();
    }
}
