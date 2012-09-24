package org.eclipse.virgo.web.enterprise.openejb.initialiser;

import java.io.IOException;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;

public class VirgoOpenEjbAssembler extends Assembler {

    @Override
    public ClassLoader createAppClassLoader(AppInfo appInfo) throws OpenEJBException, IOException {
        return Thread.currentThread().getContextClassLoader();
    }
}
