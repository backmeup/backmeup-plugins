package org.backmeup.facebook.utils;

import org.backmeup.plugin.api.Progressable;

public class ConsoleDrawer {
    /**
     * 
     * @param length
     *            the size of the progress
     * @param the
     *            current state of the progress
     * @param first
     *            do you call it the first time for this progress?
     * @param progressor
     *            use this, if you want to use a Progressable instead of the
     *            console. can be null
     */
    public static void drawProgress(int length, int progress, boolean first, Progressable... progressor) {
        Progressable prog = null;
        boolean useProg = false;
        if (progressor != null)
            for (Progressable p : progressor)
                if (p != null) {
                    prog = p;
                    useProg = true;
                }
        if (!first)
            for (int i = 0; i < length + 2; i++) {
                String out = "\b";
                if (useProg)
                    prog.progress(out);
                else
                    System.out.print(out);
            }
        StringBuilder out = new StringBuilder();
        out.append("[");
        for (int i = 0; i < length; i++) {
            if (i < progress)
                out.append("#");
            else
                out.append("-");
        }
        out.append("]");
        if (useProg)
            prog.progress(out.toString());
        else
            System.out.print(out.toString());
    }
}
