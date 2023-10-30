package disorderlyMetronome;

import basemod.*;
import basemod.interfaces.*;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;
import disorderlyMetronome.util.DisorderlyConfig;

import java.io.IOException;
import java.util.*;

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
            for(DisorderlyConfig.ConfigurationVariable variable :
                DisorderlyConfig.ConfigurationVariable.values()) {
                defaults.put(variable.name(), Float.toString(variable.defaultValue));
            }
            defaults.put("COOLDOWN_MODE", Boolean.toString(false));

            modConfig = new SpireConfig("DisorderlyMetronome", "DisorderlyConfig", defaults);
            DisorderlyConfig.loadConfig(modConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float LAYOUT_Y = 740f;
    private float ORIGINAL_LAYOUT_Y = 740f;
    private float LAYOUT_X = 400f;
    private float SPACING_Y = 52f;
    private float curPage = 1;

    @Override
    public void receivePostInitialize() {
        UIStrings optionsTextStrings = CardCrawlGame.languagePack.getUIString(makeID("OptionsMenu"));
        String[] optionsText = optionsTextStrings.TEXT;
        List<DisorderlyConfig.ConfigurationVariable> configurationVariables = Arrays.asList(DisorderlyConfig.ConfigurationVariable.values());
        settingsPanel = new ModPanel();

        //Turn Timer Sliders
        ArrayList<String> labelStrings = new ArrayList<>(Arrays.asList(optionsText));
        float sliderOffset = getSliderPosition(labelStrings.subList(0,4));
        labelStrings.clear();


        ModLabel modeLabel = new ModLabel(optionsText[0], LAYOUT_X, LAYOUT_Y, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, modLabel -> {});
        ModToggleButton normalModeButton = new ModToggleButton(LAYOUT_X + sliderOffset +50, LAYOUT_Y -5, !modConfig.getBool("COOLDOWN_MODE"), true, settingsPanel, button -> {
            modConfig.setBool("COOLDOWN_MODE", false);
            try {
                modConfig.save();
                DisorderlyConfig.loadConfig(modConfig);
            } catch (IOException e) {e.printStackTrace();}
        });

        ModToggleButton cooldownModeButton = new ModToggleButton(LAYOUT_X + sliderOffset + 200, LAYOUT_Y -5, modConfig.getBool("COOLDOWN_MODE"), true, settingsPanel, button -> {
            modConfig.setBool("COOLDOWN_MODE", true);
            try {
                modConfig.save();
                DisorderlyConfig.loadConfig(modConfig);
            } catch (IOException e) {e.printStackTrace();}
        });
        LAYOUT_Y-=SPACING_Y;
        ModRadioButtonGroup modeSelection = new ModRadioButtonGroup();
        modeSelection.addButton(normalModeButton);
        modeSelection.addButton(cooldownModeButton);
        settingsPanel.addUIElement(modeLabel);
        settingsPanel.addUIElement(normalModeButton);
        settingsPanel.addUIElement(cooldownModeButton);
        for(int i=0;i<9;i++){
            DisorderlyConfig.ConfigurationVariable variable = configurationVariables.get(i);
            ModLabel probabilityLabel = new ModLabel(optionsText[i+1], LAYOUT_X, LAYOUT_Y, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, modLabel -> {});
            ModMinMaxSlider probabilitySlider = new ModMinMaxSlider("",
                    LAYOUT_X + sliderOffset,
                    LAYOUT_Y + 7f,
                    variable.min, variable.max, modConfig.getFloat(variable.name()), "%.0f", settingsPanel, slider -> {
                modConfig.setFloat(variable.name(), Math.round(slider.getValue()));
                try {
                    modConfig.save();
                    DisorderlyConfig.loadConfig(modConfig);
                } catch (IOException e) {e.printStackTrace();}
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

    private final float pageOffset = 12000f;
    private HashMap<Integer, ArrayList<IUIElement>> pages = new HashMap<Integer, ArrayList<IUIElement>>() {{
        put(1, new ArrayList<>());
    }};
    private float elementSpace = 50f;
    private float yThreshold = LAYOUT_Y - elementSpace * 12;

    private void registerUIElement(IUIElement elem, boolean decrement) {
        settingsPanel.addUIElement(elem);

        int page = pages.size() + (yThreshold == LAYOUT_Y ? 1 : 0);
        if (!pages.containsKey(page)) {
            pages.put(page, new ArrayList<>());
            LAYOUT_Y = ORIGINAL_LAYOUT_Y;
            elem.setY(LAYOUT_Y);
        }
        if (page > curPage) {
            elem.setX(elem.getX() + pageOffset);
        }
        pages.get(page).add(elem);

        if (decrement) {
            LAYOUT_Y -= elementSpace;
        }
    }

    private void registerUIElement(IUIElement elem, int page) {
        settingsPanel.addUIElement(elem);
        if (!pages.containsKey(page)) {
            pages.put(page, new ArrayList<>());
            LAYOUT_Y = ORIGINAL_LAYOUT_Y;
            elem.setY(LAYOUT_Y);
        }
        if (page > curPage) {
            elem.setX(elem.getX() + pageOffset);
        }
        pages.get(page).add(elem);

    }
}
