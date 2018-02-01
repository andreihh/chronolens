public interface IEnumeration {
    default int getInt() {
        return 2;
    }
}

public enum Colors implements IEnumeration {
    RED, GREEN, BLUE;
}

abstract class IClass {

}

class SampleClass extends IClass implements IEnumeration {
    private int i = 0;

    /**
     * This is a simple {@code Java=doc}.
     */
    private static class StaticInnerClass {
        public StaticInnerClass(final int i) {
        }

        /**
         * This is a simple {@code Java=doc}.
         */
        @interface Ann {
            String stringField();

            /**
             * This is a simple {@code Java=doc}.
             */
            int version() default 1;
        }
    }

    /**
     * This is a simple {@code Java=doc}.
     */
    public void countTo(

            int target
    ) {
        for (
                i = 1;
                i <= target; ++i)
            System.out.println(i);
    }

    public int countTo(final long target) {
        for (i = 1; i <= target; i++) {
            System
                    .out
                    .println(
                            i
                    );}
        /**
         * This is a simple {@code Java=doc}.
         */
        return
                0;

    }
    public class InnerClass {}
    public interface InnerInterface {}
    public enum Planet {
        MERCURY (3.303e+23, 2.4397e6),
        VENUS   (4.869e+24, 6.0518e6),
        EARTH   (5.976e+24, 6.37814e6),
        MARS    (6.421e+23, 3.3972e6),
        JUPITER (1.9e+27,   7.1492e7),
        SATURN  (5.688e+26, 6.0268e7),
        /**
         * This is a simple {@code Java=doc}.
         */
        URANUS  (8.686e+25, 2.5559e7),
        NEPTUNE (1.024e+26, 2.4746e7);

        public enum Currency {
            /**
             * This is a simple {@code Java=doc}.
             */
            PENNY(1) {
                /**
                 * This is a simple {@code Java=doc}.
                 */
                @Override
                public String color() {
                    return "copper";
                }
            },
            NICKLE(5) {
                @Override
                public String color() {
                    return "bronze";
                }
            },
            DIME(10) {
                @Override
                public String color() {
                    return "silv\n\n \ner";
                }
            },
            QUARTER(25) {
                @Override
                public String color() {
                    return "silver";
                }
            };
            private int value;

            public abstract String color();

            private Currency(int value) {
                this.value = value;
            }

        }

        private final double mass;   // in kilograms
        private final double radius; // in meters
        Planet(double mass, double radius) {
            this.mass = mass;
            this.radius = radius;
        }
        private double mass() { return mass; }
        private double radius() { return radius; }

        // universal gravitational constant  (m3 kg-1 s-2)
        public static final double G = 6.67300E-11;

        double surfaceGravity() {
            return G * mass / (radius * radius);
        }
        double surfaceWeight(double otherMass) {
            return otherMass * surfaceGravity();
        }
        public static void main(String[] args) {
            if (args.length != 1) {
                System.err.println("Usage: java Planet <earth_weight>");
                System.exit(-1);
            }
            double earthWeight = Double.parseDouble(args[0]);
            double mass = earthWeight/EARTH.surfaceGravity();
            for (Planet p : Planet.values())
                System.out.printf("Your weight on %s is %f%n",
                                  p, p.surfaceWeight(mass));
        }
    }
}
