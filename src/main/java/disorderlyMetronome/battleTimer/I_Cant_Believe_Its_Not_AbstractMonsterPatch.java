package disorderlyMetronome.battleTimer;

import basemod.abstracts.CustomMonster;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import disorderlyMetronome.util.DisorderlyConfig;

import java.util.Random;


public class I_Cant_Believe_Its_Not_AbstractMonsterPatch {

    @SpirePatch(clz = CustomMonster.class, method = SpirePatch.CLASS)
    public static class patchIntoTimer {
        public static SpireField<Float> currentMonsterTimer = new SpireField<>(() -> 10f);
        public static SpireField<Float> currentMaxMonsterTimer = new SpireField<>(() -> 10f);

        public static float calculateTime(AbstractMonster __instance) {
            float f = 0;
            if (AbstractDungeon.isPlayerInDungeon()) {
                switch (__instance.type) {
                    case BOSS:
                        f = DisorderlyConfig.monsterTimerBoss;
                        break;
                    case ELITE:
                        f = DisorderlyConfig.monsterTimerElite;
                        break;
                    case NORMAL:
                        f = DisorderlyConfig.monsterTimerNormal;
                        break;
                    default:
                        f = DisorderlyConfig.monsterTimerNormal;
                        break;
                }
                Random random = new Random();
                f += random.nextInt((int)DisorderlyConfig.monsterTimerVariance*2) - DisorderlyConfig.monsterTimerVariance;
            }
            return f;
        }
    }

    @SpirePatch(clz = CustomMonster.class, method = SpirePatch.CONSTRUCTOR,
            paramtypez = {
                    String.class,
                    String.class,
                    int.class,
                    float.class,
                    float.class,
                    float.class,
                    float.class,
                    String.class,
                    float.class,
                    float.class
            }
    )

    public static class constructorTimer {
        @SpirePostfixPatch
        public static void timerCtorPatch(CustomMonster __instance, String name, String id, int maxHealth, float hb_x, float hb_y, float hb_w, float hb_h, String imgUrl, float offsetX, float offsetY) {
            System.out.println("Patching ctor of " + __instance.name);
            float calculatedTime = patchIntoTimer.calculateTime(__instance);
            patchIntoTimer.currentMonsterTimer.set(__instance,calculatedTime);
            patchIntoTimer.currentMaxMonsterTimer.set(__instance, calculatedTime);
        }
    }

    @SpirePatch(clz = CustomMonster.class, method = "render")
    public static class timerRenderPatch {
        @SpirePostfixPatch
        public static void timerCtorPatch(CustomMonster __instance, SpriteBatch sb) {
            if(!__instance.isDeadOrEscaped()) {
                DrawHealthbarTimers.drawMonsterTimer(sb, __instance, patchIntoTimer.currentMonsterTimer.get(__instance),
                        patchIntoTimer.currentMaxMonsterTimer.get(__instance));
            }
            if(!AbstractDungeon.isScreenUp) {
                patchIntoTimer.currentMonsterTimer.set(__instance,
                        patchIntoTimer.currentMonsterTimer.get(__instance) - Gdx.graphics.getDeltaTime());
                if (patchIntoTimer.currentMonsterTimer.get(__instance) <= 0f) {
                    AbstractDungeon.actionManager.addToBottom(new monsterTakeTurnAction(__instance));
                    TurnbasedPowerStuff.triggerMonsterTurnPowers(__instance);
                    float calculatedTime = patchIntoTimer.calculateTime(__instance);
                    patchIntoTimer.currentMonsterTimer.set(__instance, calculatedTime);
                    patchIntoTimer.currentMaxMonsterTimer.set(__instance, calculatedTime);
                }
            }
        }
    }
}