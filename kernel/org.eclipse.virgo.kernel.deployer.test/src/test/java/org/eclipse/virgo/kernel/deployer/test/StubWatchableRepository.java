package org.eclipse.virgo.kernel.deployer.test;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.WatchableRepository;


public class StubWatchableRepository implements WatchableRepository {

    private int checkCount = 0;
    
    @Override
    public Set<String> getArtifactLocations(String filename) {
        return new HashSet<String>();
    }

    @Override
    public void forceCheck() throws Exception {
        this.checkCount++;
    }
    
    public void resetCheckCount(){
        this.checkCount = 0;
    }
    
    public int getCheckCount(){
        return this.checkCount;
    }

}
