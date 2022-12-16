package disorderlyMetronome;

import basemod.BaseMod;
import basemod.interfaces.*;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;

@SuppressWarnings({"unused", "WeakerAccess"})
@SpireInitializer
public class DisorderlyMetronome implements PostInitializeSubscriber{

    public static final String modID = "disorderlyMetronome";

    public static String makeID(String idText) {
        return modID + ":" + idText;
    }

    public DisorderlyMetronome() {
        BaseMod.subscribe(this);
    }

    public static String makePath(String resourcePath) {
        return modID + "Resources/" + resourcePath;
    }

    public static String makeImagePath(String resourcePath) {
        return modID + "Resources/images/" + resourcePath;
    }

    public static void initialize() {
        DisorderlyMetronome thismod = new DisorderlyMetronome();
    }

    @Override
    public void receivePostInitialize() {

    }
}
