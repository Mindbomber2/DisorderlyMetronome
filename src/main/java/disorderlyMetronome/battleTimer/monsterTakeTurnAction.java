package disorderlyMetronome.battleTimer;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class monsterTakeTurnAction extends AbstractGameAction {

    private AbstractMonster __instance;

    public monsterTakeTurnAction(AbstractMonster m) {
        __instance = m;
        PlayerTimerPatches.PlayerTimerPatch.timeAttackIsMonsterTurn.set(AbstractDungeon.player, true);
    }

    @Override
    public void update() {
        __instance.takeTurn();
        __instance.rollMove();
        __instance.createIntent();
        PlayerTimerPatches.PlayerTimerPatch.timeAttackIsMonsterTurn.set(AbstractDungeon.player, false);
        this.isDone = true;
    }
}
