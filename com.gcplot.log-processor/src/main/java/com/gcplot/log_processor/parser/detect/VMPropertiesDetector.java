package com.gcplot.log_processor.parser.detect;

import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.GarbageCollectorType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         10/30/16
 */
public abstract class VMPropertiesDetector {

    /**
     * Very rough vm version detector from the log files. Shouldn't be trusted at all,
     * just as a hint for the actual parser.
     */
    public static VMProperties detect(InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            Iterator<String> i = br.lines().iterator();
            while (i.hasNext()) {
                String s = i.next();
                if (s.indexOf(" (young)") > 0 || s.indexOf(" (mixed)") > 0 || s.indexOf("G1Ergonomics") > 0) {
                    return new VMProperties(VMVersion.HOTSPOT_1_6, GarbageCollectorType.ORACLE_G1);
                } else if (s.indexOf("[Times:") > 0) {
                    return new VMProperties(VMVersion.HOTSPOT_1_6, GarbageCollectorType.ORACLE_PAR_OLD_GC);
                } else if (s.contains("CMS-initial-mark") || s.contains("PSYoungGen")) {
                    return new VMProperties(VMVersion.HOTSPOT_1_5, GarbageCollectorType.ORACLE_CMS);
                } else if (s.contains(": [GC")) {
                    return new VMProperties(VMVersion.HOTSPOT_1_4, GarbageCollectorType.ORACLE_PAR_OLD_GC);
                } else if (s.contains("[GC") || s.contains("[Full GC") || s.contains("[Inc GC")) {
                    return new VMProperties(VMVersion.HOTSPOT_1_3_1, GarbageCollectorType.ORACLE_PAR_OLD_GC);
                } else if (s.contains("<GC: managing allocation failure: need ")) {
                    return new VMProperties(VMVersion.HOTSPOT_1_2_2, GarbageCollectorType.ORACLE_PAR_OLD_GC);
                }
            }
        }
        return VMProperties.EMPTY;
    }

}
