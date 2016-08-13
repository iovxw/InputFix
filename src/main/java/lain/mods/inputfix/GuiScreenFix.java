package lain.mods.inputfix;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import lain.mods.inputfix.interfaces.IGuiScreen;
import lain.mods.inputfix.utils.ReflectionHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class GuiScreenFix
{

    private static class Proxy implements IGuiScreen
    {

        private GuiScreen gui;

        @Override
        public void keyTyped(char c, int k)
        {
            try
            {
                if (gui != null)
                    keyTyped.invoke(gui, c, k);
            }
            catch (Throwable t)
            {
                throw new RuntimeException(t);
            }
        }

        public Proxy setGui(GuiScreen gui)
        {
            this.gui = gui;
            return this;
        }

    }

    private static final ThreadLocal<Proxy> proxies = new ThreadLocal<Proxy>()
    {

        @Override
        protected Proxy initialValue()
        {
            return new Proxy();
        }

    };

    private static final Method keyTyped = ReflectionHelper.findMethod(GuiScreen.class, new String[] { "func_73869_a", "keyTyped" }, new Class<?>[] { char.class, int.class });

    public static void handleKeyboardInput(GuiScreen gui)
    {
        Proxy p = proxies.get().setGui(gui);

        if (InputFixSetup.impl != null)
            InputFixSetup.impl.handleKeyboardInput(p);
        else if (Keyboard.getEventKeyState())
            p.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());

        try
        {
            Field mcField = ReflectionHelper.findField(GuiScreen.class, new String[] { "field_146297_k", "mc" });
            Minecraft mc = (Minecraft) mcField.get(gui);
            Method dispatchKeypresses = ReflectionHelper.findMethod(Minecraft.class, new String[] { "func_152348_aa", "dispatchKeypresses" }, new Class<?>[] {});

            dispatchKeypresses.invoke(mc);
        }
        catch (Throwable t)
        {
            throw new RuntimeException(t);
        }
    }

}
