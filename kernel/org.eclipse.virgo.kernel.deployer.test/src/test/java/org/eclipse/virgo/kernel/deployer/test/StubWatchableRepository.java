package org.eclipse.virgo.kernel.deployer.test;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.WatchableRepository;


public class StubWatchableRepository implements WatchableRepository {

    private int checkCount = 0;
    
    @Override
    public Set<String> getArtifactLocations(String filename) {
        return new HashSet<>();
    }

    @Override
    public void forceCheck() {
        this.checkCount++;
    }

    int getCheckCount(){
        return this.checkCount;
    }

}
