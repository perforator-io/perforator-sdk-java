/*
 * Copyright Perforator, Inc. and contributors. All rights reserved.
 *
 * Use of this software is governed by the Business Source License
 * included in the LICENSE file.
 *
 * As of the Change Date specified in that file, in accordance with
 * the Business Source License, use of this software will be governed
 * by the Apache License, Version 2.0.
 */
package io.perforator.sdk.loadgenerator.core.cdp;

import org.openqa.selenium.devtools.CdpInfo;

public class NoOpCdpInfo extends CdpInfo {

    private NoOpCdpInfo() {
        this(getVersion());
    }

    private NoOpCdpInfo(int majorVersion) {
        super(majorVersion, dt -> new NoOpDomains());
    }

    private static int getVersion() {
        return StackWalker.getInstance(
                StackWalker.Option.RETAIN_CLASS_REFERENCE
        ).walk(frames -> {
            return frames.filter(
                    frame -> frame.getClassName().startsWith(NoOpCdpInfo.class.getName() + "$")
            ).map(
                    frame -> Integer.valueOf(frame.getClassName().replaceAll(".*\\$v", ""))
            ).findFirst();
        }).orElseThrow();
    }

    public static class v110 extends NoOpCdpInfo {}
    public static class v111 extends NoOpCdpInfo {}
    public static class v112 extends NoOpCdpInfo {}
    public static class v113 extends NoOpCdpInfo {}
    public static class v114 extends NoOpCdpInfo {}
    public static class v115 extends NoOpCdpInfo {}
    public static class v116 extends NoOpCdpInfo {}
    public static class v117 extends NoOpCdpInfo {}
    public static class v118 extends NoOpCdpInfo {}
    public static class v119 extends NoOpCdpInfo {}
    public static class v120 extends NoOpCdpInfo {}
    public static class v121 extends NoOpCdpInfo {}
    public static class v122 extends NoOpCdpInfo {}
    public static class v123 extends NoOpCdpInfo {}
    public static class v124 extends NoOpCdpInfo {}
    public static class v125 extends NoOpCdpInfo {}
    public static class v126 extends NoOpCdpInfo {}
    public static class v127 extends NoOpCdpInfo {}
    public static class v128 extends NoOpCdpInfo {}
    public static class v129 extends NoOpCdpInfo {}
    public static class v130 extends NoOpCdpInfo {}
    public static class v131 extends NoOpCdpInfo {}
    public static class v132 extends NoOpCdpInfo {}
    public static class v133 extends NoOpCdpInfo {}
    public static class v134 extends NoOpCdpInfo {}
    public static class v135 extends NoOpCdpInfo {}
    public static class v136 extends NoOpCdpInfo {}
    public static class v137 extends NoOpCdpInfo {}
    public static class v138 extends NoOpCdpInfo {}
    public static class v139 extends NoOpCdpInfo {}
    public static class v140 extends NoOpCdpInfo {}
    

}
