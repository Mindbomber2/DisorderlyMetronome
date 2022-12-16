package disorderlyMetronome.cardLogic;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

/**
 * All of this belongs to the wonderful AlisonMoon.
 */
public class ProjectSpecificCardAction extends AbstractGameAction {
    private AbstractCard card;

    public ProjectSpecificCardAction(AbstractCard card) {
        this.card = card;
    }

    @Override
    public void update() {
        if (card == null) {
            this.isDone = true;
            return;
        }
        ProjectedCardManager.addCard(card);
        if (AbstractDungeon.player.drawPile.contains(card)) {
            AbstractDungeon.player.drawPile.removeCard(card);
        }
        if (AbstractDungeon.player.hand.contains(card)) {
            AbstractDungeon.player.hand.removeCard(card);
        }
        if (AbstractDungeon.player.discardPile.contains(card)) {
            AbstractDungeon.player.discardPile.removeCard(card);
        }
        if (AbstractDungeon.player.exhaustPile.contains(card)) {
            card.unfadeOut();
            AbstractDungeon.player.exhaustPile.removeCard(card);
        }
        if (AbstractDungeon.player.limbo.contains(card)) {
            AbstractDungeon.player.limbo.removeCard(card);
        }
        this.isDone = true;
    }
}