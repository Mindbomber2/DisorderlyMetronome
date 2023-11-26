package disorderlyMetronome;

import basemod.*;
import basemod.interfaces.*;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;
import disorderlyMetronome.util.DisorderlyConfig;
import org.lwjgl.BufferUtils;

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
            defaults.put("GAMEMODE", DisorderlyConfig.GameMode.ENERGY.name());

            modConfig = new SpireConfig("DisorderlyMetronome", "DisorderlyConfig", defaults);
            DisorderlyConfig.loadConfig(modConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float LAYOUT_Y = 750f;
    private float ORIGINAL_LAYOUT_Y = 750f;
    private float LAYOUT_X = 400f;
    private float SPACING_Y = 52f;
    private int curPage = 1;
    private List<DisorderlyConfig.ConfigurationVariable> configurationVariables = Arrays.asList(DisorderlyConfig.ConfigurationVariable.values());

    @Override
    public void receivePostInitialize() {
        UIStrings optionsMenuGeneralStrings = CardCrawlGame.languagePack.getUIString(makeID("OptionsMenuGeneral"));
        String[] optionsMenuGeneralText = optionsMenuGeneralStrings.TEXT;
        UIStrings optionsMenuEnergyModeStrings = CardCrawlGame.languagePack.getUIString(makeID("OptionsMenuEnergyMode"));
        String[] optionsMenuEnergyModeText = optionsMenuEnergyModeStrings.TEXT;
        UIStrings optionsMenuTimeAttackModeStrings = CardCrawlGame.languagePack.getUIString(makeID("OptionsMenuTimeAttackMode"));
        String[] optionsMenuTimeAttackModeText = optionsMenuTimeAttackModeStrings.TEXT;
        UIStrings optionsMenuCooldownModeStrings = CardCrawlGame.languagePack.getUIString(makeID("OptionsMenuCooldownMode"));
        String[] optionsMenuCooldownModeText = optionsMenuCooldownModeStrings.TEXT;

        settingsPanel = new ModPanel();

        float sliderOffset = getSliderPosition(Arrays.asList(optionsMenuGeneralText));


        ModLabel modeLabel = new ModLabel(optionsMenuGeneralText[0], LAYOUT_X, LAYOUT_Y, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, modLabel -> {});
        ModToggleButton energyModeButton = new ModToggleButton(LAYOUT_X + sliderOffset + 50, LAYOUT_Y -5, modConfig.getString("GAMEMODE").equals(DisorderlyConfig.GameMode.ENERGY.name()), true, settingsPanel, button -> {
            modConfig.setString("GAMEMODE", DisorderlyConfig.GameMode.ENERGY.name());
            try {
                modConfig.save();
                DisorderlyConfig.loadConfig(modConfig);
            } catch (IOException e) {e.printStackTrace();}
        });

        ModToggleButton timeAttackModeButton = new ModToggleButton(LAYOUT_X + sliderOffset +150, LAYOUT_Y -5, modConfig.getString("GAMEMODE").equals(DisorderlyConfig.GameMode.TIMEATTACK.name()), true, settingsPanel, button -> {
            modConfig.setString("GAMEMODE", DisorderlyConfig.GameMode.TIMEATTACK.name());
            try {
                modConfig.save();
                DisorderlyConfig.loadConfig(modConfig);
            } catch (IOException e) {e.printStackTrace();}
        });

        ModToggleButton cooldownModeButton = new ModToggleButton(LAYOUT_X + sliderOffset + 250, LAYOUT_Y -5, modConfig.getString("GAMEMODE").equals(DisorderlyConfig.GameMode.COOLDOWN.name()), true, settingsPanel, button -> {
            modConfig.setString("GAMEMODE", DisorderlyConfig.GameMode.COOLDOWN.name());
            try {
                modConfig.save();
                DisorderlyConfig.loadConfig(modConfig);
            } catch (IOException e) {e.printStackTrace();}
        });

        LAYOUT_Y-=SPACING_Y;
        ModRadioButtonGroup modeSelection = new ModRadioButtonGroup();
        modeSelection.addButton(energyModeButton);
        modeSelection.addButton(timeAttackModeButton);
        modeSelection.addButton(cooldownModeButton);
        registerUIElement(modeLabel, 1);
        registerUIElement(energyModeButton, 1);
        registerUIElement(timeAttackModeButton, 1);
        registerUIElement(cooldownModeButton, 1);
        int offset = 1;
        addPageOptions(Arrays.copyOfRange(optionsMenuGeneralText, 1 ,optionsMenuGeneralText.length), 1, offset);
        offset+= optionsMenuGeneralText.length-1;
        LAYOUT_Y=ORIGINAL_LAYOUT_Y;
        addPageOptions(optionsMenuEnergyModeText, 2, offset);
        offset+= optionsMenuEnergyModeText.length;
        LAYOUT_Y=ORIGINAL_LAYOUT_Y;
        addPageOptions(optionsMenuTimeAttackModeText, 3, offset);
        offset+= optionsMenuTimeAttackModeText.length;
        LAYOUT_Y=ORIGINAL_LAYOUT_Y;
        addPageOptions(optionsMenuCooldownModeText, 4, offset);


        ModLabeledButton FlipPageBtn = new ModLabeledButton("Next Page", LAYOUT_X + 450f, ORIGINAL_LAYOUT_Y + 45f, Settings.CREAM_COLOR, Color.WHITE, FontHelper.cardEnergyFont_L, settingsPanel,
                    button ->
                    {
                        if (pages.containsKey(curPage + 1)) {
                            changePage(curPage + 1);
                        } else {
                            changePage(1);
                        }
                    });
            settingsPanel.addUIElement(FlipPageBtn);



        BaseMod.registerModBadge(ImageMaster.loadImage(modID + "Resources/images/ui/chain48.png"), modID, "Mindbomber", "", settingsPanel);
    }

    private void addPageOptions(String[] optionsText, int page, int offset) {
        float sliderOffset = getSliderPosition(Arrays.asList(optionsText));

        for(int i=0;i< optionsText.length;i++){
            DisorderlyConfig.ConfigurationVariable variable = configurationVariables.get(i+offset-1);
            ModLabel probabilityLabel = new ModLabel(optionsText[i], LAYOUT_X, LAYOUT_Y, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, modLabel -> {});
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
            registerUIElement(probabilityLabel, page);
            registerUIElement(probabilitySlider, page);
            LAYOUT_Y-=SPACING_Y;
        }
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

    private void changePage(int i) {
        for (IUIElement e : pages.get(curPage)) {
            e.setX(e.getX() + pageOffset);
        }

        for (IUIElement e : pages.get(i)) {
            e.setX(e.getX() - pageOffset);
        }
        curPage = i;
    }
}
