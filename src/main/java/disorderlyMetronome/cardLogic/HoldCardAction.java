package disorderlyMetronome.cardLogic;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import disorderlyMetronome.battleTimer.PlayerTimerPatches;

public class HoldCardAction extends AbstractGameAction {
    private AbstractCard card;
    private AbstractMonster monster;
    private int energyOnUse;

    public HoldCardAction(AbstractCard c, AbstractMonster m, int e) {
        this.card = c;
        this.monster = m;
        this.energyOnUse = e;
    }

    @Override
    public void update() {
        if (PlayerTimerPatches.PlayerTimerPatch.canPlayCard.get(AbstractDungeon.player) == false) {
            AbstractCardPatch.patchSpireField.cardTarget.set(card, monster);
            AbstractDungeon.actionManager.addToTop(new ProjectSpecificCardAction(card));
        }
        this.isDone=true;
    }
}
