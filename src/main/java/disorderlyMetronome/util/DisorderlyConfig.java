package disorderlyMetronome.util;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;

public abstract class DisorderlyConfig {
    public static boolean cooldownMode = false;

    //monster config
    public static float monsterTimerNormal = -1f;
    public static float monsterTimerElite = -1f;
    public static float monsterTimerBoss = -1f;
    public static float monsterTimerVariance = -1f;


    //standard mode config
    public static float baseTurnDuration = -1f;
    public static float turnDurationBonus = -1f;

    //cooldown mod config
    public static float cdmHandSize = -1f;
    public static float cdmDelay = -1f;
    public static float cdmCooldownMultiplier = -1f;


    public static void loadConfig(SpireConfig modConfig) {
        cooldownMode=modConfig.getBool("COOLDOWN_MODE");
        monsterTimerNormal= modConfig.getFloat(ConfigurationVariable.MONSTER_TIMER_NORMAL.name());
        monsterTimerElite= modConfig.getFloat(ConfigurationVariable.MONSTER_TIMER_ELITE.name());
        monsterTimerBoss= modConfig.getFloat(ConfigurationVariable.MONSTER_TIMER_BOSS.name());
        monsterTimerVariance= modConfig.getFloat(ConfigurationVariable.MONSTER_TIMER_VARIANCE.name());
        baseTurnDuration= modConfig.getFloat(ConfigurationVariable.BASE_TURN_DURATION.name());
        turnDurationBonus= modConfig.getFloat(ConfigurationVariable.TURN_DURATION_BONUS.name());
        cdmHandSize= modConfig.getFloat(ConfigurationVariable.CDM_HAND_SIZE.name());
        cdmDelay= modConfig.getFloat(ConfigurationVariable.CDM_DELAY.name());
        cdmCooldownMultiplier= modConfig.getFloat(ConfigurationVariable.CDM_COOLDOWN_MULTIPLIER.name());
        
    }

    public enum ConfigurationVariable {
        MONSTER_TIMER_NORMAL(1F, 60F, 25F),
        MONSTER_TIMER_ELITE(1F, 60F, 30F),
        MONSTER_TIMER_BOSS(1F, 60F, 40F),
        MONSTER_TIMER_VARIANCE(0F, 10F, 5F),
        BASE_TURN_DURATION(1F, 60F, 5F),
        TURN_DURATION_BONUS(0F, 60F, 5F),
        CDM_HAND_SIZE(0F, 10F, 5F),
        CDM_DELAY(0F, 20F, 5F),
        CDM_COOLDOWN_MULTIPLIER(0F, 10F, 5F);

        public final float min;
        public final float max;
        public final float defaultValue;

        ConfigurationVariable(float min, float max, float defaultValue) {
            this.min = min;
            this.max = max;
            this.defaultValue = defaultValue;
        }
    }
}
