package disorderlyMetronome.battleTimer;

import basemod.abstracts.CustomMonster;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import disorderlyMetronome.util.DisorderlyConfig;

import java.util.Random;

//THIS SHOULDN'T BE NECESSARY
public class I_Cant_Believe_Its_Not_AbstractMonsterPatch {

    @SpirePatch(clz = CustomMonster.class, method = SpirePatch.CLASS)
    public static class MonsterTimerPatch {
        public static SpireField<Float> currentMonsterTimer = new SpireField<>(() -> 10f);
        public static SpireField<Float> currentMaxMonsterTimer = new SpireField<>(() -> 10f);

        public static float calculateTime(CustomMonster __instance) {
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
                        throw new IllegalArgumentException("Wtf. New Monster Type just dropped");
                }

                Random random = new Random();
                //modify timer by random amount in range [-variance,+variance]
                f += random.nextInt((int) DisorderlyConfig.monsterTimerVariance * 2) - DisorderlyConfig.monsterTimerVariance;
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
            System.out.println("Hitbox Width: " + __instance.hb.width);
            float calculatedTime = MonsterTimerPatch.calculateTime(__instance);
            MonsterTimerPatch.currentMonsterTimer.set(__instance, calculatedTime);
            MonsterTimerPatch.currentMaxMonsterTimer.set(__instance, calculatedTime);
        }
    }

    @SpirePatch(clz = CustomMonster.class, method = "render")
    public static class HandleMonsterTimerPatch {
        @SpirePostfixPatch
        public static void handleMonsterTimer(CustomMonster __instance, SpriteBatch sb) {
            //draw timer
            if (!__instance.isDeadOrEscaped()) {
                DrawHealthbarTimers.drawMonsterTimer(sb, __instance, MonsterTimerPatch.currentMonsterTimer.get(__instance),
                        MonsterTimerPatch.currentMaxMonsterTimer.get(__instance));
            }
            //calculate timer
            if (!AbstractDungeon.isScreenUp) {
                //speed up timer if in time attack mode and it's nobodies turn otherwise pause
                if (DisorderlyConfig.gameMode == DisorderlyConfig.GameMode.TIMEATTACK) {
                    if (PlayerTimerPatches.PlayerTimerPatch.timeAttackIsPlayerTurn.get(AbstractDungeon.player) == false && PlayerTimerPatches.PlayerTimerPatch.timeAttackIsMonsterTurn.get(AbstractDungeon.player) == false) {
                        MonsterTimerPatch.currentMonsterTimer.set(__instance,
                                MonsterTimerPatch.currentMonsterTimer.get(__instance) - (5 * Gdx.graphics.getDeltaTime()));
                    }
                } else {
                    MonsterTimerPatch.currentMonsterTimer.set(__instance,
                            MonsterTimerPatch.currentMonsterTimer.get(__instance) - Gdx.graphics.getDeltaTime());
                }
            }
            //trigger turn
            if (MonsterTimerPatch.currentMonsterTimer.get(__instance) <= 0f) {
                AbstractDungeon.actionManager.addToBottom(new monsterTakeTurnAction(__instance));
                TurnbasedPowerStuff.triggerMonsterTurnPowers(__instance);
                float calculatedTime = MonsterTimerPatch.calculateTime(__instance);
                MonsterTimerPatch.currentMonsterTimer.set(__instance, calculatedTime);
                MonsterTimerPatch.currentMaxMonsterTimer.set(__instance, calculatedTime);
            }
        }
    }
}