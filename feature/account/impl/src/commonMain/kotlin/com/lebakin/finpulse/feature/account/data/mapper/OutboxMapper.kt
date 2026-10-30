package com.lebakin.finpulse.feature.account.data.mapper

import com.lebakin.finpulse.feature.account.api.SyncChange
import com.lebakin.finpulse.feature.account.api.SyncKind
import com.lebakin.finpulse.feature.account.api.SyncOrigin
import com.lebakin.finpulse.feature.account.data.OutboxEntity

internal fun OutboxEntity.toDomain(): SyncChange = SyncChange(
    id = recordId,
    kind = SyncKind.valueOf(kind),
    origin = SyncOrigin.valueOf(origin),
    revision = revision,
    body = body,
)

internal fun SyncChange.toEntity(): OutboxEntity = OutboxEntity(
    rowKey = "${kind.name}:$id",
    recordId = id,
    kind = kind.name,
    origin = origin.name,
    revision = revision,
    body = body,
)
