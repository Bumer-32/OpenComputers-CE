package li.cil.oc.common.capabilities;

import li.cil.oc.api.internal.Colored;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.SidedEnvironment;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.Callable;

@Mod.EventBusSubscriber(modid = "opencomputers", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Capabilities {
    public static Capability<Colored> ColoredCapability = CapabilityManager.get(new CapabilityToken<>(){});

    public static Capability<Environment> EnvironmentCapability = CapabilityManager.get(new CapabilityToken<>(){});

    public static Capability<SidedEnvironment> SidedEnvironmentCapability = CapabilityManager.get(new CapabilityToken<>(){});

    // *legacy of the past*
    // java 7 doesn't have generic type constraints
    // java 7 doesn't have lambdas
    // java 7 doesn't generic type covariance
    private static class StupidJavaTookTooManyYearsToIntroduceLambdas<T> implements Callable<T> {

        StupidJavaTookTooManyYearsToIntroduceLambdas(Class cls) {
            _cls = cls;
        }

        @Override
        public T call() throws Exception {
            return (T)_cls.newInstance();
        }

        private Class _cls;
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(Colored.class);
        event.register(Environment.class);
        event.register(SidedEnvironment.class);
    }

    private Capabilities() {
    }
}
