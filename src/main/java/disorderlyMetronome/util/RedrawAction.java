package disorderlyMetronome.util;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import disorderlyMetronome.battleTimer.EndOfTurnButtonPatches;
import disorderlyMetronome.battleTimer.PlayerCountdownPatch;

public class RedrawAction extends AbstractGameAction {

    public RedrawAction() {
    }

    @Override
    public void update() {
        int drawAmount = (int)DisorderlyConfig.redrawAmount - AbstractDungeon.player.hand.size();
        if (drawAmount>0) {
            AbstractDungeon.actionManager.addToTop(new DrawCardAction(drawAmount));
            EndOfTurnButtonPatches.RefreshBoolean.isRefreshing.set(AbstractDungeon.overlayMenu.endTurnButton, false);

        }
        this.isDone=true;
    }
}
