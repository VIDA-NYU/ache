package achecrawler.link.frontier;

import crawlercommons.robots.SimpleRobotRules;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is workaround that allows correct serialization and deserialization of robot rules parsed by the
 * SimpleRobotRules from the crawler-commons library.
 */
public class SerializableRobotRules extends SimpleRobotRules {

    /**
     * Same as SimpleRobotRules.RobotRule, but with a public constructor that allows
     * serialization libraries to serialize and deserialize the object correctly.
     */
    public static class OpenRobotRule extends RobotRule {
        public OpenRobotRule() {
            // required by serialization libraries
            this(null, false);
        }

        public OpenRobotRule(String prefix, boolean allow) {
            super(prefix, allow);
        }
    }

    public SerializableRobotRules() {
        // required by serialization libraries
    }

    public SerializableRobotRules(SimpleRobotRules simpleRobotRules) {
        // Copies the SimpleRobotRules object into this SerializableRobotRules
        List<RobotRule> rules = simpleRobotRules.getRobotRules();
        super._rules = new ArrayList<>(rules.size());
        for (RobotRule r : rules) {
            super._rules.add(new OpenRobotRule(r.getPrefix(), r.isAllow()));
        }
        if (simpleRobotRules.isAllowAll()) {
            super._mode = RobotRulesMode.ALLOW_ALL;
        } else if (simpleRobotRules.isAllowNone()) {
            super._mode = RobotRulesMode.ALLOW_NONE;
        } else {
            super._mode = RobotRulesMode.ALLOW_SOME;
        }
    }
}
