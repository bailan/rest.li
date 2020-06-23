/*
   Copyright (c) 2012 LinkedIn Corp.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.linkedin.d2.balancer.strategies.relative;

import com.linkedin.d2.balancer.clients.TrackerClient;
import com.linkedin.d2.balancer.strategies.degrader.LoadBalancerQuarantine;
import com.linkedin.d2.balancer.strategies.degrader.RingFactory;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * The helper class that builds an object of {@link PartitionState}
 */
public class PartitionStateDataBuilder {
  private static final int DEFAULT_PARTITION_ID = 0;
  private static final int DEFAULT_POINTS_PER_WEIGHT = 100;

  private final RingFactory<URI> _ringFactory;
  private long _clusterGenerationId;
  private Set<TrackerClient> _recoveryTrackerClients = new HashSet<>();
  private Map<TrackerClient, LoadBalancerQuarantine> _quarantineMap = new HashMap<>();
  private Map<TrackerClient, TrackerClientState> _trackerClientStateMap = new HashMap<>();

  PartitionStateDataBuilder(RingFactory<URI> ringFactory)
  {
    _ringFactory = ringFactory;
    _clusterGenerationId = 0;
  }

  PartitionStateDataBuilder setClusterGenerationId(long clusterGenerationId)
  {
    _clusterGenerationId = clusterGenerationId;
    return this;
  }

  PartitionStateDataBuilder setTrackerClientStateMap(List<TrackerClient> trackerClients,
      List<Double> healthScores, List<TrackerClientState.HealthState> healthStates, List<Integer> callCountList,
      double initialHealthScore, int minCallCount)
  {
    if (trackerClients.size() != healthScores.size() || trackerClients.size() != healthStates.size() || trackerClients.size() != callCountList.size())
    {
      throw new IllegalArgumentException("The size of the tracker client and health scores have to match!");
    }
    for (int index = 0; index < trackerClients.size(); index ++)
    {
      TrackerClientState trackerClientState = new TrackerClientState(initialHealthScore, minCallCount);
      trackerClientState.setHealthScore(healthScores.get(index));
      trackerClientState.setHealthState(healthStates.get(index));
      trackerClientState.setCallCount(callCountList.get(index));
      _trackerClientStateMap.put(trackerClients.get(index), trackerClientState);
    }
    return this;
  }

  PartitionStateDataBuilder setRecoveryClients(Set<TrackerClient> trackerClients)
  {
    _recoveryTrackerClients = trackerClients;
    return this;
  }

  PartitionStateDataBuilder setQuarantineMap(Map<TrackerClient, LoadBalancerQuarantine> quarantineMap)
  {
    _quarantineMap = quarantineMap;
    return this;
  }

  PartitionState build()
  {
    return new PartitionState(DEFAULT_PARTITION_ID, _ringFactory, DEFAULT_POINTS_PER_WEIGHT,
        _recoveryTrackerClients, _clusterGenerationId, _quarantineMap, new HashMap<>(), new HashMap<>(),
        _trackerClientStateMap, new ArrayList<>());
  }
}