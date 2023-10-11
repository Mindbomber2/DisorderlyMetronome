package disorderlyMetronome.cardLogic;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.actions.common.MoveCardsAction;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.actions.utility.UnlimboAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.OverlayMenu;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.ui.panels.ExhaustPanel;
import com.megacrit.cardcrawl.vfx.BobEffect;
import javassist.CtBehavior;

/**
 * All of this belongs to the wonderful AlisonMoon.
 */
public class ProjectedCardManager {
    public static final float Y_OFFSET = 70f * Settings.scale;
    public static final float X_OFFSET = 100f * Settings.scale;
    public static final CardGroup cards = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
    private static final BobEffect bob = new BobEffect(3.0f * Settings.scale, 3.0f);
    public static AbstractCard hovered;

    public static void render(SpriteBatch sb) {
        for (AbstractCard card : cards.group) {
            if (card != hovered) {
                card.render(sb);
            }
        }
        if (hovered != null) {
            hovered.render(sb);
            TipHelper.renderTipForCard(hovered, sb, hovered.keywords);
        }
    }

    public static void update() {
        bob.update();
        int i = 0;
        hovered = null;
        for (AbstractCard card : cards.group) {
            card.target_y = AbstractDungeon.player.hb.cY + AbstractDungeon.player.hb.height/2f + Y_OFFSET + bob.y;
            card.target_x = AbstractDungeon.player.hb.cX + X_OFFSET * (cards.size()-1) / 2f - X_OFFSET * i;
            card.targetAngle = 0f;
            card.update();
            card.hb.update();
            if (card.hb.hovered && hovered == null) {
                card.targetDrawScale = 0.75f;
                hovered = card;
            } else {
                card.targetDrawScale = 0.2f;
            }
            card.applyPowers();
            i++;
        }
    }

    public static void playCards() {
        //TODO has bugs if you open a screen, the cards darken and don't re-lighten. For example Tools of Trade. Limbo fading out is the reason
        for (AbstractCard card : cards.group) {
            AbstractDungeon.player.limbo.group.add(card);
            AbstractDungeon.actionManager.addToBottom(new AbstractGameAction() {
                @Override
                public void update() {
                    //for (AbstractCard card : cards.group) {

                    card.targetDrawScale = 0.75F;
                    card.applyPowers();
                    ProjectedCardField.projectedField.set(card, true);
                    AbstractDungeon.actionManager.addToBottom(new NewQueueCardAction(card, AbstractCardPatch.patchSpireField.cardTarget.get(card), false, true));
                    AbstractDungeon.actionManager.addToBottom(new UnlimboAction(card));
                    //}

                    this.isDone = true;
                }
            });
        }
        cards.clear();

    }

    public static void addCard(AbstractCard card) {
        addCard(card, true);
    }

    public static void addCard(AbstractCard card, boolean playSFX) {
        card.targetAngle = 0f;
        card.beginGlowing();
        if(!cards.isEmpty()){
            //AbstractDungeon.actionManager.addToTop(new MoveCardsAction(AbstractDungeon.player.hand, cards, cards.size()));
            for (AbstractCard c : cards.group) {
                AbstractDungeon.player.hand.addToTop(c);
            }
            cards.clear();
        }
        cards.addToTop(card);
        if (playSFX) {
            CardCrawlGame.sound.play("ORB_SLOT_GAIN", 0.1F);
        }
    }

    @SpirePatch2(clz = AbstractCard.class, method = SpirePatch.CLASS)
    public static class ProjectedCardField {
        public static SpireField<Boolean> projectedField = new SpireField<>(() -> false);
    }

    @SpirePatch2(clz = UseCardAction.class, method = SpirePatch.CLASS)
    public static class ProjectedActionField {
        public static SpireField<Boolean> projectedField = new SpireField<>(() -> false);
    }

    @SpirePatch2(clz = UseCardAction.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {AbstractCard.class, AbstractCreature.class})
    public static class InheritProjectedField {
        @SpirePrefixPatch
        public static void pushProjected(UseCardAction __instance, AbstractCard card) {
            if (ProjectedCardField.projectedField.get(card)) {
                ProjectedActionField.projectedField.set(__instance, true);
                ProjectedCardField.projectedField.set(card, false);
            }
        }
    }

    @SpirePatch2(clz = AbstractPlayer.class, method = "applyStartOfTurnCards")
    public static class PlayCards {
        @SpirePrefixPatch
        public static void playCards() {
            ProjectedCardManager.playCards();
        }
    }

    @SpirePatch2(clz = OverlayMenu.class, method = "render")
    public static class RenderPanel {
        @SpireInsertPatch(locator = Locator.class)
        public static void render(OverlayMenu __instance, SpriteBatch sb) {
            ProjectedCardManager.render(sb);
        }

        public static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher m = new Matcher.MethodCallMatcher(ExhaustPanel.class, "render");
                return LineFinder.findInOrder(ctBehavior, m);
            }
        }
    }

    @SpirePatch2(clz = AbstractPlayer.class, method = "combatUpdate")
    public static class UpdatePile {
        @SpirePostfixPatch
        public static void update(AbstractPlayer __instance) {
            ProjectedCardManager.update();
        }
    }

    @SpirePatch2(clz = AbstractPlayer.class, method = "preBattlePrep")
    @SpirePatch2(clz = AbstractPlayer.class, method = "onVictory")
    public static class EmptyCards {
        @SpirePostfixPatch
        public static void yeet() {
            cards.clear();
        }
    }

    /*@SpirePatch2(clz = UseCardAction.class, method = "update")
    public static class AscendedFix {
        @SpireInsertPatch(locator = Locator.class)
        public static SpireReturn<?> yeet(UseCardAction __instance, AbstractCard ___targetCard) {
            if (___targetCard.hasTag(CustomTags.STARLIGHT_ASCENDED)) {
                ProjectedCardManager.addCard(___targetCard, false);
                __instance.isDone = true;
                AbstractDungeon.player.cardInUse = null;
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }

        public static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher m = new Matcher.FieldAccessMatcher(AbstractCard.class, "purgeOnUse");
                return LineFinder.findInOrder(ctBehavior, m);
            }
        }
    }*/
}