package com.hepimusic.main.requests

import com.amplifyframework.datastore.generated.model.RequestPlayer
import com.amplifyframework.datastore.generated.model.RequestPlayerCopy
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo

data class EndPointData(
    val endpointId: String,
    val info: DiscoveredEndpointInfo,
    val player: RequestPlayerCopy? = null
)