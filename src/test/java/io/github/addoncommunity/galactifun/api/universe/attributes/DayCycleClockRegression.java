package io.github.addoncommunity.galactifun.api.universe.attributes;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.bukkit.World;

/** Runs the real DayCycle against the documented World time boundary. */
public final class DayCycleClockRegression {
    private static int passes;
    private static int failures;
    private static final String NO_CLOCK = "Cannot set time in world without world clock";

    public static void main(String[] args) {
        test("eternal daylight remains6000 on a time-capable world", () -> {
            Fixture f = new Fixture(null, true); DayCycle.ETERNAL_DAY.applyEffects(f.world);
            require(f.lastSet == 6000L && Boolean.FALSE.equals(f.daylight), "daylight changed");
        });
        test("eternal night remains18000 on a time-capable world", () -> {
            Fixture f = new Fixture(null, false); DayCycle.ETERNAL_NIGHT.applyEffects(f.world);
            require(f.lastSet == 18000L && Boolean.FALSE.equals(f.daylight), "night changed");
        });
        test("earthlike advances by100 ticks even with fixed-time flag", () -> {
            Fixture f = new Fixture(null, true); f.time = 23990L; DayCycle.EARTH_LIKE.tick(f.world);
            require(f.lastSet == 24090L, "clock advancement changed");
        });
        test("eternal tick does not change the existing time", () -> {
            Fixture f = new Fixture(null, false); f.time = 1234L; DayCycle.ETERNAL_DAY.tick(f.world);
            require(f.lastSet == Long.MIN_VALUE && f.time == 1234L, "eternal tick mutated time");
        });
        test("clockless26.3 world does not abort applyEffects", () -> {
            Fixture f = new Fixture(new IllegalArgumentException(NO_CLOCK), false);
            DayCycle.ETERNAL_DAY.applyEffects(f.world);
            require(f.time == 0L, "clockless world changed");
        });
        test("clockless26.3 world does not abort tick", () -> {
            Fixture f = new Fixture(new IllegalArgumentException(NO_CLOCK), false);
            DayCycle.EARTH_LIKE.tick(f.world);
            require(f.time == 0L, "clockless world changed");
        });
        test("tick does not swallow an unrelated time failure", () -> {
            RuntimeException expected = new IllegalArgumentException("fixture-listener-rejected");
            Fixture f = new Fixture(expected, false);
            try { DayCycle.EARTH_LIKE.tick(f.world); throw new AssertionError("unrelated failure swallowed"); }
            catch (IllegalArgumentException actual) { require(actual == expected, "failure identity changed"); }
        });
        test("applyEffects does not swallow an unrelated time failure", () -> {
            RuntimeException expected = new IllegalArgumentException("fixture-listener-rejected");
            Fixture f = new Fixture(expected, false);
            try { DayCycle.ETERNAL_NIGHT.applyEffects(f.world); throw new AssertionError("unrelated failure swallowed"); }
            catch (IllegalArgumentException actual) { require(actual == expected, "failure identity changed"); }
        });
        System.out.println("DAYCYCLE_TEST_DONE pass=" + passes + " fail=" + failures);
        if (failures != 0) System.exit(1);
    }

    private static void require(boolean value, String detail) { if (!value) throw new AssertionError(detail); }
    private interface Checked { void run() throws Throwable; }
    private static void test(String name, Checked body) {
        try { body.run(); passes++; System.out.println("PASS " + name); }
        catch (Throwable failure) { failures++; System.out.println("FAIL " + name + " | " + failure.getClass().getName() + ":" + failure.getMessage()); }
    }

    private static final class Fixture implements InvocationHandler {
        final RuntimeException timeFailure;
        final boolean fixedTime;
        final World world;
        long time;
        long lastSet = Long.MIN_VALUE;
        Boolean daylight;
        Fixture(RuntimeException timeFailure, boolean fixedTime) {
            this.timeFailure = timeFailure; this.fixedTime = fixedTime;
            world = (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class<?>[]{World.class}, this);
        }
        @Override public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "getTime", "getFullTime" -> time;
                case "setTime" -> { if (timeFailure != null) throw timeFailure; lastSet = (long) args[0]; time = lastSet; yield null; }
                case "setGameRule" -> { daylight = (Boolean) args[1]; yield true; }
                case "isFixedTime" -> fixedTime;
                case "toString" -> "SyntheticClockWorld";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new AssertionError("Unexpected World access: " + method.getName());
            };
        }
    }
}
