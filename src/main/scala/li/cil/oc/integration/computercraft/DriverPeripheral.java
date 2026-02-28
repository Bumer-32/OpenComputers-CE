package li.cil.oc.integration.computercraft;

import dan200.computercraft.api.filesystem.Mount;
import dan200.computercraft.api.filesystem.WritableMount;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaTask;
import dan200.computercraft.api.lua.ObjectArguments;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.WorkMonitor;
import li.cil.oc.OpenComputers;
import li.cil.oc.Settings;
import li.cil.oc.api.FileSystem;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.BlacklistedPeripheral;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.util.Reflection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

public final class DriverPeripheral implements li.cil.oc.api.driver.DriverBlock {
    private static Set<Class<?>> blacklist;

    private boolean isBlacklisted(final Object o) {
        // Check for our interface first, as that has priority.
        if (o instanceof BlacklistedPeripheral) {
            return ((BlacklistedPeripheral) o).isPeripheralBlacklisted();
        }

        // Delayed initialization of the resolved classes to allow registering
        // additional entries via IMC.
        if (blacklist == null) {
            blacklist = new HashSet<>();
            for (String name : Settings.get().peripheralBlacklist()) {
                final Class<?> clazz = Reflection.getClass(name);
                if (clazz != null) {
                    blacklist.add(clazz);
                }
            }
        }
        for (Class<?> clazz : blacklist) {
            if (clazz.isInstance(o))
                return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static Capability<IPeripheral> getPeripheralCapability() {
        try {
            Class<?> clazz = Class.forName("dan200.computercraft.shared.Capabilities");
            return (Capability<IPeripheral>) clazz.getField("CAPABILITY_PERIPHERAL").get(null);
        } catch (Exception e) {
            OpenComputers.log().warn("Could not access ComputerCraft Capabilities via reflection.", e);
            return null;
        }
    }

    private static final Capability<IPeripheral> PERIPHERAL_CAP = getPeripheralCapability();

    private IPeripheral findPeripheral(final Level world, final BlockPos pos, final Direction side) {
        try {
            if (PERIPHERAL_CAP == null) return null;
            final BlockEntity be = world.getBlockEntity(pos);
            if (be == null) return null;
            final IPeripheral p = be.getCapability(PERIPHERAL_CAP, side).orElse(null);
            if (!isBlacklisted(p)) {
                return p;
            }
        } catch (Exception e) {
            OpenComputers.log().warn("Error accessing ComputerCraft peripheral @ ({}, {}, {}).", pos.getX(), pos.getY(), pos.getZ(), e);
        }
        return null;
    }

    @Override
    public boolean worksWith(final Level world, final BlockPos pos, final Direction side) {
        final BlockEntity tileEntity = world.getBlockEntity(pos);
        return tileEntity != null
                // This ensures we don't get duplicate components, in case the
                // tile entity is natively compatible with OpenComputers.
                && !li.cil.oc.api.network.Environment.class.isAssignableFrom(tileEntity.getClass())
                // The black list is used to avoid peripherals that are known
                // to be incompatible with OpenComputers when used directly.
                && !isBlacklisted(tileEntity)
                // Actual check if it's a peripheral.
                && findPeripheral(world, pos, side) != null;
    }

    @Override
    public ManagedEnvironment createEnvironment(final Level world, final BlockPos pos, final Direction side) {
        return new Environment(findPeripheral(world, pos, side));
    }

    public static class Environment extends li.cil.oc.api.prefab.AbstractManagedEnvironment implements li.cil.oc.api.network.ManagedPeripheral, NamedBlock {
        protected final IPeripheral peripheral;
        protected final String[] methodNames;
        protected final Map<String, FakeComputerAccess> accesses = new HashMap<>();

        public Environment(final IPeripheral peripheral) {
            this.peripheral = peripheral;
            if (peripheral instanceof IDynamicPeripheral dynamic) {
                methodNames = dynamic.getMethodNames();
            } else {
                methodNames = new String[0];
            }
            setNode(Network.newNode(this, Visibility.Network).create());
        }

        @Override
        public String[] methods() {
            return methodNames;
        }

        @Override
        public Object[] invoke(final String name, final Context context, final Arguments args) throws Exception {
            if (!(peripheral instanceof IDynamicPeripheral dynamic)) throw new NoSuchMethodException();

            final String[] names = dynamic.getMethodNames();
            int index = -1;
            for (int i = 0; i < names.length; i++) {
                if (names[i].equals(name)) {
                    index = i;
                    break;
                }
            }
            if (index == -1) throw new NoSuchMethodException();

            final FakeComputerAccess access;
            if (accesses.containsKey(context.node().address())) {
                access = accesses.get(context.node().address());
            } else {
                // The calling context is not visible to us, meaning we never got
                // an onConnect for it. Create a temporary access.
                access = new FakeComputerAccess(this, context);
            }

            final Object[] argArray = CallableHelper.convertArguments(args);
            return dynamic.callMethod(access, UnsupportedLuaContext.instance(), index, new ObjectArguments(argArray)).getResult();
        }

        @Override
        public void onConnect(final Node node) {
            super.onConnect(node);
            if (node.host() instanceof Context && !accesses.containsKey(node.address())) {
                final FakeComputerAccess access = new FakeComputerAccess(this, (Context) node.host());
                accesses.put(node.address(), access);
                peripheral.attach(access);
            }
        }

        @Override
        public void onDisconnect(final Node node) {
            super.onDisconnect(node);
            if (node.host() instanceof Context) {
                final FakeComputerAccess access = accesses.remove(node.address());
                if (access != null) {
                    peripheral.detach(access);
                }
            } else if (node == this.node()) {
                for (FakeComputerAccess access : accesses.values()) {
                    peripheral.detach(access);
                    access.close();
                }
                accesses.clear();
            }
        }

        @Override
        public String preferredName() {
            return peripheral.getType();
        }

        @Override
        public int priority() {
            return -1; // Lower than 'real' OC components
        }

        /**
         * Map interaction with the computer to our format as good as we can.
         */
        public static class FakeComputerAccess implements IComputerAccess {
            protected final Environment owner;
            protected final Context context;
            protected final Map<String, ManagedEnvironment> fileSystems = new HashMap<>();

            public FakeComputerAccess(final Environment owner, final Context context) {
                this.owner = owner;
                this.context = context;
            }

            public void close() {
                for (ManagedEnvironment fileSystem : fileSystems.values()) {
                    fileSystem.node().remove();
                }
                fileSystems.clear();
            }

            @Override
            public String mount(final String desiredLocation, final Mount mount) {
                if (fileSystems.containsKey(desiredLocation)) {
                    return null;
                }
                return mount(desiredLocation, FileSystem.asManagedEnvironment(DriverComputerCraftMedia.fromComputerCraft(mount)));
            }

            @Override
            public String mount(final String desiredLocation, final Mount mount, final String driveName) {
                if (fileSystems.containsKey(desiredLocation)) {
                    return null;
                }
                return mount(desiredLocation, FileSystem.asManagedEnvironment(DriverComputerCraftMedia.fromComputerCraft(mount), driveName));
            }

            @Override
            public String mountWritable(final String desiredLocation, final WritableMount mount) {
                if (fileSystems.containsKey(desiredLocation)) {
                    return null;
                }
                return mount(desiredLocation, FileSystem.asManagedEnvironment(DriverComputerCraftMedia.fromComputerCraft(mount)));
            }

            @Override
            public String mountWritable(final String desiredLocation, final WritableMount mount, final String driveName) {
                if (fileSystems.containsKey(desiredLocation)) {
                    return null;
                }
                return mount(desiredLocation, FileSystem.asManagedEnvironment(DriverComputerCraftMedia.fromComputerCraft(mount), driveName));
            }

            private String mount(final String path, final ManagedEnvironment fileSystem) {
                fileSystems.put(path, fileSystem); // TODO: This is per peripheral/Environment. It would be far better with per computer
                context.node().connect(fileSystem.node());
                return path;
            }

            @Override
            public void unmount(final String location) {
                final ManagedEnvironment fileSystem = fileSystems.remove(location);
                if (fileSystem != null) {
                    fileSystem.node().remove();
                }
            }

            @Override
            public int getID() {
                return context.node().address().hashCode();
            }

            @Override
            public void queueEvent(final String event, final Object... arguments) {
                context.signal(event, arguments);
            }

            @Override
            public String getAttachmentName() {
                return owner.node().address();
            }

            @Override
            public @NotNull Map<String, IPeripheral> getAvailablePeripherals() {
                return Collections.emptyMap();
            }

            @Override
            public IPeripheral getAvailablePeripheral(final String name) {
                return null;
            }

            @Override
            public @NotNull WorkMonitor getMainThreadMonitor() {
                return new WorkMonitor() {
                    @Override
                    public boolean canWork() {
                        return false;
                    }

                    @Override
                    public boolean shouldWork() {
                        return false;
                    }

                    @Override
                    public void trackWork(long l, @NotNull TimeUnit timeUnit) {

                    }
                };
            }
        }

        /**
         * Since we abstract away anything language specific, we cannot support the
         * Lua context specific operations ComputerCraft provides.
         */
        public static final class UnsupportedLuaContext implements ILuaContext {
            private static final UnsupportedLuaContext Instance = new UnsupportedLuaContext();

            private UnsupportedLuaContext() {}

            public static UnsupportedLuaContext instance() {
                return Instance;
            }

            @Override
            public long issueMainThreadTask(@NotNull LuaTask luaTask) throws LuaException {
                return 0;
            }
        }
    }
}