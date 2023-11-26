package disorderlyMetronome.battleTimer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class DrawHealthbarTimers {
    public static float HEALTH_BAR_HEIGHT = 20.0F * Settings.scale;

    public static void drawMonsterTimer(SpriteBatch sb, AbstractMonster __instance, float currentTimer, float maxTimer){
        float x = __instance.hb.cX - __instance.hb.width / 2.0F;
        float y = __instance.hb.cY - __instance.hb.height / 2.0F;
        float timerBarWidth = __instance.hb.width * currentTimer / maxTimer;
        if (AbstractDungeon.ascensionLevel <= 5) { sb.setColor(Color.GREEN.cpy());
        } else if (AbstractDungeon.ascensionLevel <= 10) { sb.setColor(Color.ORANGE.cpy());
        } else if (AbstractDungeon.ascensionLevel <= 15) { sb.setColor(Color.RED.cpy());
        } else { sb.setColor(Color.PURPLE.cpy()); }
        sb.draw(ImageMaster.HEALTH_BAR_L, x - HEALTH_BAR_HEIGHT, y, HEALTH_BAR_HEIGHT, HEALTH_BAR_HEIGHT);
        sb.draw(ImageMaster.HEALTH_BAR_B, x, y, timerBarWidth, HEALTH_BAR_HEIGHT);
        sb.draw(ImageMaster.HEALTH_BAR_R, x + timerBarWidth, y, HEALTH_BAR_HEIGHT, HEALTH_BAR_HEIGHT);
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.HALF_EVEN);
        df.setMinimumFractionDigits(1);
        FontHelper.renderFontCentered(sb, FontHelper.healthInfoFont, df.format(currentTimer), __instance.hb.cX, y +10f * Settings.scale, Color.WHITE.cpy());
    }

    public static void drawPlayerTimer(SpriteBatch sb, AbstractPlayer __instance, float currentTimer, float maxTimer){
        float x = __instance.hb.cX - __instance.hb.width / 2.0F;
        float y = __instance.hb.cY - __instance.hb.height / 2.0F;
        float timerBarWidth = __instance.hb.width * currentTimer / maxTimer;
        sb.setColor(Color.GREEN.cpy());
        sb.draw(ImageMaster.HEALTH_BAR_L, x - HEALTH_BAR_HEIGHT, y, HEALTH_BAR_HEIGHT, HEALTH_BAR_HEIGHT);
        sb.draw(ImageMaster.HEALTH_BAR_B, x, y, timerBarWidth, HEALTH_BAR_HEIGHT);
        sb.draw(ImageMaster.HEALTH_BAR_R, x + timerBarWidth, y, HEALTH_BAR_HEIGHT, HEALTH_BAR_HEIGHT);
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.HALF_EVEN);
        df.setMinimumFractionDigits(1);
        FontHelper.renderFontCentered(sb, FontHelper.healthInfoFont, df.format(currentTimer), __instance.hb.cX, y +10f * Settings.scale, Color.WHITE.cpy());
    }
}
