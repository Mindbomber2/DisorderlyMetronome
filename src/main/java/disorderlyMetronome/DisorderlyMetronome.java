package disorderlyMetronome;

import basemod.BaseMod;
import basemod.ModLabel;
import basemod.ModMinMaxSlider;
import basemod.ModPanel;
import basemod.interfaces.*;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@SuppressWarnings({"unused", "WeakerAccess"})
@SpireInitializer
public class DisorderlyMetronome implements PostInitializeSubscriber, EditStringsSubscriber{

    public static final String modID = "disorderlyMetronome";

    public static String makeID(String idText) {
        return modID + ":" + idText;
    }

    private ModPanel settingsPanel;

    public static SpireConfig modConfig;

    public DisorderlyMetronome() {
    }

    @Override
    public void receiveEditStrings() {
        BaseMod.loadCustomStringsFile(UIStrings.class, modID + "Resources/localization/eng/UI-Strings.json");
    }

    public static String makePath(String resourcePath) {
        return modID + "Resources/" + resourcePath;
    }

    public static String makeImagePath(String resourcePath) {
        return modID + "Resources/images/" + resourcePath;
    }

    public static void initialize() {
        BaseMod.subscribe(new DisorderlyMetronome());
        try {
            Properties defaults = new Properties();
            defaults.put("TURN_TIMER_PLAYER", Float.toString(5F));
            defaults.put("TURN_TIMER_NORMAL", Float.toString(25F));
            defaults.put("TURN_TIMER_ELITE", Float.toString(30F));
            defaults.put("TURN_TIMER_BOSS", Float.toString(40F));

            modConfig = new SpireConfig("DisorderlyMetronome", "Config", defaults);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receivePostInitialize() {
        UIStrings UIStrings = CardCrawlGame.languagePack.getUIString(makeID("OptionsMenu"));
        String[] TEXT = UIStrings.TEXT;
        UIStrings VariableNames = CardCrawlGame.languagePack.getUIString(makeID("VariableNames"));
        String[] VariableNamesText = VariableNames.TEXT;
        settingsPanel = new ModPanel();


        //Turn Timer Sliders
        ArrayList<String> labelStrings = new ArrayList<>(Arrays.asList(TEXT));
        float sliderOffset = getSliderPosition(labelStrings.subList(0,4));
        labelStrings.clear();

        float LAYOUT_Y = 740f;
        float LAYOUT_X = 400f;
        float SPACING_Y = 52f;

        for(int i=0;i<4;i++){
            String varName = VariableNamesText[i];
            ModLabel probabilityLabel = new ModLabel(TEXT[i], LAYOUT_X, LAYOUT_Y, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, modLabel -> {});
            ModMinMaxSlider probabilitySlider = new ModMinMaxSlider("",
                    LAYOUT_X + sliderOffset,
                    LAYOUT_Y + 7f,
                    1, 60, modConfig.getFloat(VariableNamesText[i]), "%.0f", settingsPanel, slider -> {
                modConfig.setFloat(varName, Math.round(slider.getValue()));
                try {modConfig.save();} catch (IOException e) {e.printStackTrace();}
            });
            settingsPanel.addUIElement(probabilityLabel);
            settingsPanel.addUIElement(probabilitySlider);
            LAYOUT_Y-=SPACING_Y;
        }



        BaseMod.registerModBadge(ImageMaster.loadImage(modID + "Resources/images/ui/chain48.png"), modID, "Mindbomber", "", settingsPanel);
    }

    private void saveConfig() {
        try {
            modConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Get the longest text so all sliders are centered
    private static float getSliderPosition(List<String> stringsToCompare) {
        float longest = 0;
        for (String s : stringsToCompare) {
            longest = Math.max(longest, FontHelper.getWidth(FontHelper.charDescFont, s, 1f / Settings.scale));
        }
        return longest + 40f;
    }

}
