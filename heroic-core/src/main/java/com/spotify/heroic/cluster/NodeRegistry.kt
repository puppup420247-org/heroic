/*
 * Copyright (c) 2019 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.heroic.cluster

import com.google.common.collect.LinkedListMultimap
import com.google.common.collect.Lists
import com.google.common.collect.Multimap
import eu.toolchain.async.AsyncFramework
import java.util.Optional
import java.util.function.Predicate

data class NodeRegistry(
    val async: AsyncFramework?,
    val entries: List<ClusterNode>,
    val totalNodes: Int
) {
    val onlineNodes: Int = entries.size
    val offlineNodes: Int = totalNodes - entries.size
    val shards: Set<Map<String, String>>
        get() = buildShards().keySet()

    private fun buildShards(): Multimap<Map<String, String>, ClusterNode> {
        val shards = LinkedListMultimap.create<Map<String, String>, ClusterNode>()

        for (e in entries) {
            shards.put(e.metadata().tags, e)
        }

        return shards
    }

    fun getNodesInShard(shard: Map<String, String>): List<ClusterNode> {
        val shardToNode = buildShards()

        val nodesInShard = shardToNode.get(shard)
        val result = Lists.newArrayList<ClusterNode>()
        result.addAll(nodesInShard)

        return result
    }

    fun getNodeInShardButNotWithId(
            shard: Map<String, String>, exclude: Predicate<ClusterNode>
    ): Optional<ClusterNode> {
        val shardToNode = buildShards()

        val nodesInShard = shardToNode.get(shard)

        val filteredList = nodesInShard
            .filter { it.isAlive }
            .filter { exclude.test(it) }

        if (!filteredList.isEmpty()) {
            return Optional.of(filteredList.random())
        }

        return Optional.empty()
    }
}
