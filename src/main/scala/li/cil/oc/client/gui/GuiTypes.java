package li.cil.oc.client.gui;

import li.cil.oc.common.menu.MenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class GuiTypes {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent e) {
        // ScreenManager.register is not thread-safe.
        e.enqueueWork(() -> {
            MenuScreens.register(MenuTypes.ADAPTER, Adapter::new);
            MenuScreens.register(MenuTypes.ASSEMBLER, Assembler::new);
            MenuScreens.register(MenuTypes.CASE, Case::new);
            MenuScreens.register(MenuTypes.CHARGER, Charger::new);
            MenuScreens.register(MenuTypes.DATABASE, Database::new);
            MenuScreens.register(MenuTypes.DISASSEMBLER, Disassembler::new);
            MenuScreens.register(MenuTypes.DISK_DRIVE, DiskDrive::new);
            MenuScreens.register(MenuTypes.DRONE, Drone::new);
            MenuScreens.register(MenuTypes.PRINTER, Printer::new);
            MenuScreens.register(MenuTypes.RACK, Rack::new);
            MenuScreens.register(MenuTypes.RAID, Raid::new);
            MenuScreens.register(MenuTypes.RELAY, Relay::new);
            MenuScreens.register(MenuTypes.ROBOT, Robot::new);
            MenuScreens.register(MenuTypes.SERVER, Server::new);
            MenuScreens.register(MenuTypes.TABLET, Tablet::new);
        });
    }

    private GuiTypes() {
        throw new Error();
    }
}
