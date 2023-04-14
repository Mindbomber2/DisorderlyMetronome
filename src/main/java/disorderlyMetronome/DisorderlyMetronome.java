package disorderlyMetronome;

import basemod.BaseMod;
import basemod.ModPanel;
import basemod.interfaces.*;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.helpers.ImageMaster;

import java.io.IOException;
import java.util.Properties;

@SuppressWarnings({"unused", "WeakerAccess"})
@SpireInitializer
public class DisorderlyMetronome implements PostInitializeSubscriber{

    public static final String modID = "disorderlyMetronome";

    public static String makeID(String idText) {
        return modID + ":" + idText;
    }

    private ModPanel settingsPanel;

    public static SpireConfig modConfig;

    public DisorderlyMetronome() {
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

            modConfig = new SpireConfig("DisorderlyMetronome", "Config", defaults);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receivePostInitialize() {
        settingsPanel = new ModPanel();
        BaseMod.registerModBadge(ImageMaster.loadImage(modID + "Resources/images/ui/chain48.png"), modID, "Mindbomber", "", settingsPanel);
    }

    private void saveConfig() {
        try {
            modConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
